#!/usr/bin/env bash
# =============================================================================
# FXKit: Product Backlog script (Phases 3 and 4)
#
# Sibling of docs/setup-backlog.sh (Phases 1-2). Same tool, same conventions,
# extended forward. Creates (safe to re-run; issues matched by title):
#   1. New epic labels for Phases 3 and 4
#   2.  3 Phase 3 issues (Sprint 5)    -> milestone "Phase 3: Showcase App"
#   3. 17 Phase 4 issues (Sprints 6-8) -> milestone "Phase 4: Inputs & Feedback"
#   4. Every issue is added to the "FXKit Roadmap" board with its Points
#
# Assumes setup-github.sh already ran (repo, board, Points field, and all 9
# phase milestones exist) and Phase 2 is done: FxButton, FxCard,
# EnumStyleClassSync, ThemeManager, the token/utility CSS, JUnit 5 +
# JavaFxToolkit, and docs/new-component-checklist.md are all in place. Every
# component story below assumes that checklist as its Definition of Done and
# only calls out what's specific to that component, to avoid repeating the
# same 7 bullets seventeen times.
#
# Issues are created in PRIORITY ORDER, so they land on the board in that
# order.
#
# Prerequisites: same as setup-github.sh (gh, jq, gh auth login,
#                gh auth refresh -s project)
#
# Usage:
#   DRY_RUN=yes ./setup-backlog-phase3-4.sh     # print what would be created, touch nothing
#   ./setup-backlog-phase3-4.sh                 # create everything
# =============================================================================
set -euo pipefail

# ------------------------------ CONFIG ---------------------------------------
REPO_NAME="fxkit"
PROJECT_TITLE="FXKit Roadmap"       # must match the board created by setup-github.sh
DRY_RUN="${DRY_RUN:-no}"            # yes | no
PAUSE="${PAUSE:-1}"                 # seconds between issues (avoids GitHub rate limits)
# -----------------------------------------------------------------------------

say()  { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
die()  { printf '\n\033[31mError:\033[0m %s\n' "$*" >&2; exit 1; }

MS_PHASE3="Phase 3: Showcase App"
MS_PHASE4="Phase 4: Inputs & Feedback"

# ------------------------------ PRE-FLIGHT -----------------------------------
if [[ "$DRY_RUN" == "yes" ]]; then
  say "DRY RUN: nothing will be created on GitHub"
  REPO="(your-user)/$REPO_NAME"
else
  command -v gh >/dev/null 2>&1 || die "GitHub CLI (gh) not found. Install it from https://cli.github.com"
  command -v jq >/dev/null 2>&1 || die "jq not found. Install it from https://jqlang.github.io/jq"
  gh auth status >/dev/null 2>&1 || die "Not logged in. Run: gh auth login"
  gh project list --owner "@me" >/dev/null 2>&1 \
    || die "Missing 'project' permission. Run: gh auth refresh -s project"

  OWNER="$(gh api user -q .login)"
  REPO="$OWNER/$REPO_NAME"
  gh repo view "$REPO" >/dev/null 2>&1 || die "Repo $REPO not found. Run setup-github.sh first."
  info "Logged in as: $OWNER"
  info "Target repo:  $REPO"
fi

# ------------------------------ 1. LABELS ------------------------------------
say "1/4  Labels"
label() {
  [[ "$DRY_RUN" == "yes" ]] && { info "label: $1"; return 0; }
  gh label create "$1" --color "$2" --description "$3" --repo "$REPO" --force >/dev/null && info "label: $1"
}
# Phase 3
label "epic:showcase"   "0e8a16" "Epic: showcase navigation shell, code snippets and pages"
# Phase 4
label "epic:text-input" "1d76db" "Epic: FxTextField, FxTextArea and the event-handler convention"
label "epic:selection"  "5319e7" "Epic: FxCheckbox, FxRadioButton, FxToggle, FxSelect"
label "epic:feedback"   "d93f0b" "Epic: FxBadge, FxAlert, FxProgressBar, FxAvatar, typography"

# ------------------------------ 2. BOARD LOOKUP ------------------------------
say "2/4  Project board"
if [[ "$DRY_RUN" == "yes" ]]; then
  info "(skipped in dry run)"
else
  PROJECT_NUMBER="$(gh project list --owner "@me" --format json \
    | jq -r --arg t "$PROJECT_TITLE" '.projects[]? | select(.title==$t) | .number' | head -n1)"
  [[ -n "$PROJECT_NUMBER" ]] || die "Project '$PROJECT_TITLE' not found. Run setup-github.sh first."

  PROJECT_JSON="$(gh project view "$PROJECT_NUMBER" --owner "@me" --format json)"
  PROJECT_ID="$(jq -r .id <<<"$PROJECT_JSON")"
  PROJECT_URL="$(jq -r .url <<<"$PROJECT_JSON")"

  POINTS_FIELD_ID="$(gh project field-list "$PROJECT_NUMBER" --owner "@me" --format json \
    | jq -r '.fields[]? | select(.name=="Points") | .id' | head -n1)"
  [[ -n "$POINTS_FIELD_ID" ]] || die "'Points' field not found on the board. Run setup-github.sh first."
  info "Board: $PROJECT_URL (#$PROJECT_NUMBER)"
fi

# ------------------------------ 3. HELPERS -----------------------------------
say "3/4  Creating issues"
EXISTING_TITLES=""
[[ "$DRY_RUN" == "yes" ]] || \
  EXISTING_TITLES="$(gh issue list --repo "$REPO" --state all --limit 500 --json title -q '.[].title' || true)"

CREATED=0; SKIPPED=0; TOTAL_POINTS=0
PHASE_NO=""; MILESTONE=""

add_to_board() {
  local url="$1" points="$2" item_json item_id
  item_json="$(gh project item-add "$PROJECT_NUMBER" --owner "@me" --url "$url" --format json)"
  item_id="$(jq -r .id <<<"$item_json")"
  if [[ -n "$item_id" && "$item_id" != "null" ]]; then
    gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" \
      --field-id "$POINTS_FIELD_ID" --number "$points" >/dev/null
  fi
}

# create_item KIND "Title" POINTS "epic-label" "Epic name" "sprint"   <<'EOF' ...body... EOF
#   KIND is "story" or "spike" (becomes the label). The body comes from stdin.
create_item() {
  local kind="$1" title="$2" points="$3" epic_label="$4" epic_name="$5" sprint="$6" body url
  body="$(cat)"                                   # always consume stdin

  if grep -Fxq -- "$title" <<<"$EXISTING_TITLES"; then
    info "skip (exists): $title"; SKIPPED=$((SKIPPED + 1)); return 0
  fi

  body+=$'\n\n---\n'"**Points:** ${points} · **Epic:** ${epic_name} · **Phase:** ${PHASE_NO} (Sprint ${sprint})"

  if [[ "$DRY_RUN" == "yes" ]]; then
    info "[$points pts] ($kind, $epic_label, sprint $sprint) $title"
  else
    url="$(gh issue create --repo "$REPO" \
            --title "$title" --body "$body" \
            --label "$kind,$epic_label" --milestone "$MILESTONE")"
    add_to_board "$url" "$points"
    info "[$points pts] $title"
    sleep "$PAUSE"
  fi
  CREATED=$((CREATED + 1)); TOTAL_POINTS=$((TOTAL_POINTS + points))
}
story() { create_item story "$@"; }
spike() { create_item spike "$@"; }

# =============================================================================
# PHASE 3: SHOWCASE APP  (Sprint 5)
# =============================================================================
PHASE_NO="3"; MILESTONE="$MS_PHASE3"
say "Phase 3: Showcase App"

# ---- Sprint 5: navigation shell ---------------------------------------------
# Sprint Goal: "Every existing component has a live demo page I can browse, one at a time."
#
# Already done in Phase 2 and NOT repeated here: the theme toggle (ShowcaseApp's
# header button) and the color palette page (PalettePage, #23). What's missing
# is turning the current single stacked-in-one-scroll layout into the
# navigate-between-pages shell the phases doc describes, plus code snippets.

story "Showcase navigation shell: sidebar + swappable content area" 5 "epic:showcase" "Navigation shell" "5" <<'EOF'
## User story
As a maintainer, I want a sidebar that lists every showcase page, so that I can jump straight to one instead of scrolling through all of them stacked together.

## Acceptance criteria
- [ ] A sidebar (left) lists page names; a content area (center) shows the selected page's content
- [ ] Selecting a sidebar entry swaps the content area to that page, without reloading the whole window
- [ ] The content area still scrolls independently for a tall page (reuse the existing `ScrollPane` / `showcase-scroll` approach)
- [ ] The header (title, version, theme toggle) stays visible above both the sidebar and the content area, and still restyles whichever page is showing when the theme is toggled
- [ ] The sidebar itself is styled only with FXKit's existing utility classes (`bg-surface-alt`, `p-*`, `gap-*`, ...) - no new hard-coded colors
- [ ] A sensible default page is shown on launch (e.g. the first entry)

## Tasks
- [ ] Decide the sidebar widget (a `ListView<String>` or a `VBox` of buttons both work; pick whichever is simpler to style with utility classes)
- [ ] Build a small page registry: a name plus a `Supplier<Node>` (or similar) for each page, so pages are only built when first shown
- [ ] Wire sidebar selection to `BorderPane.setCenter(...)`
- [ ] Style the selected sidebar entry so it's visually distinct (a semantic-token background is enough; no need for a new component yet)
EOF

story "Wire existing pages into the navigation shell" 3 "epic:showcase" "Navigation shell" "5" <<'EOF'
## User story
As a maintainer, I want the existing pages behind the new sidebar, so that the showcase has one page per component instead of one long scroll.

## Acceptance criteria
- [ ] `PalettePage` ("Colors"), `ButtonPage`, `CardPage` and `FxmlDemoPage` each become one sidebar entry
- [ ] The old approach (all four stacked in one `VBox` inside one `ScrollPane`, in `ShowcaseApp`) is removed
- [ ] `mvn javafx:run` shows exactly one page's content at a time, matching the entry selected in the sidebar
- [ ] Nothing about what each page itself shows changes (this story is only about how they're reached)

## Tasks
- [ ] Register each page's `create()` (with a display name) in the page registry from the previous story
- [ ] Remove the stacked `VBox` from `ShowcaseApp`
- [ ] Re-check both themes and the FXML demo page still work from inside the shell

Depends on: "Showcase navigation shell: sidebar + swappable content area".
EOF

story "Code snippet panel next to each demo" 3 "epic:showcase" "Code snippets" "5" <<'EOF'
## User story
As a JavaFX developer browsing the showcase, I want to see the code next to each example, so that I can copy it into my own project.

## Acceptance criteria
- [ ] `ButtonPage` and `CardPage` each show a read-only, monospaced snippet of the Java that builds the example above it (or a representative one, if the page has several)
- [ ] `FxmlDemoPage` shows its FXML source the same way
- [ ] Snippets use FXKit's own tokens/utilities for their box (e.g. `bg-surface-alt`, the existing `.mono` class) - no new hard-coded colors or fonts
- [ ] Long lines wrap or the snippet area scrolls horizontally; it never pushes the rest of the page off-screen

## Tasks
- [ ] Decide how a snippet is stored: a Java text block right next to the code that builds the matching demo node is simplest and keeps the two in sync by construction
- [ ] Build one small reusable helper (e.g. a `codeBlock(String)` node builder) rather than repeating the styling per page
- [ ] Apply it to `ButtonPage`, `CardPage` and `FxmlDemoPage`
- [ ] (Not required, nice to have) a "Copy" button on each snippet
EOF

# =============================================================================
# PHASE 4: INPUTS & FEEDBACK  (Sprints 6-8)
# =============================================================================
PHASE_NO="4"; MILESTONE="$MS_PHASE4"
say "Phase 4: Inputs & Feedback"

# ---- Sprint 6: text inputs ---------------------------------------------------
# Sprint Goal: "Text input with sizes and validation styling, and an
# event-handler convention every later component follows."

spike "Decision (ADR): event-handler convention for FXKit components" 2 "epic:text-input" "Events" "6" <<'EOF'
## Question to answer
Beyond the JavaFX events components already get for free (`onAction`, focus, key events), some upcoming
components need to announce something that isn't just "a property changed" - `FxAlert`'s dismiss button,
for instance. What is FXKit's own convention for that?

Compare, and pick one (or a documented combination):
- A plain typed property plus its `Property()` (e.g. `valueProperty()`, `selectedProperty()`) and let
  callers add their own listener - matches most of JavaFX's own controls
- An explicit `ObjectProperty<EventHandler<ActionEvent>>` with a getter/setter/property, the same shape as
  `ButtonBase.onAction` (`setOnClose(...)`, `onCloseProperty()`), for something that is a one-off
  occurrence rather than a value to observe
- A custom `Event` subclass (like `javafx.scene.control.TableColumn.CellEditEvent`) for cases carrying
  extra data with the notification

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] `docs/adr/ADR-004-event-convention.md` (Status, Context, Options, Decision, Consequences)
- [ ] A short rule of thumb: when to use a plain property vs an `onX` handler property vs a custom Event
- [ ] `docs/new-component-checklist.md` updated with a line pointing to this ADR (section 2, alongside the enum-property rule)

## Findings
EOF

story "FxTextField with sizes and validation states" 5 "epic:text-input" "FxTextField" "6" <<'EOF'
## User story
As a JavaFX developer, I want a text field with sizes and a validation state, so that forms look consistent and show errors without extra CSS.

## Acceptance criteria
- [ ] `FxTextField` extends `TextField` (the checklist's "is that control with a different look and a small set of typed properties" rule - cite it directly, no new ADR needed)
- [ ] `Size` enum `SM`, `MD`, `LG` using the **same** padding/font-size values as `FxButton.Size`, so a field and a button of matching size line up in a row
- [ ] `ValidationState` enum `DEFAULT`, `SUCCESS`, `ERROR`, each mapped to a border color token (`-fxk-border-strong`, `-fxk-success`, `-fxk-danger`); a documented default
- [ ] Placeholder text works via the inherited `promptText` (nothing new to add)
- [ ] No-arg constructor and a constructor taking initial text
- [ ] Meets every item in `docs/new-component-checklist.md` (style-class constant, `DEFAULT_*` constants, `EnumStyleClassSync` for both enums, tokens-only CSS, `:hover`/`:focused`/`:disabled` states, tests, Javadoc)

## Tasks
- [ ] Implement per the checklist
- [ ] Add a "text input" section to `components.css`, sharing a base style class (e.g. `fxk-input`) that `FxTextArea` will reuse next
- [ ] Confirm `module-info.java` needs no change (the `dev.fxkit.core.components` package is already exported and opened)

Depends on: the event-convention ADR is not required for this story (a text field's own value already has `textProperty()`), but read it first since `FxTextArea` and later components will need it.
EOF

story "FxTextArea" 3 "epic:text-input" "FxTextArea" "6" <<'EOF'
## User story
As a JavaFX developer, I want a multi-line text input that matches FxTextField's look, so that a form's short and long fields feel like one family.

## Acceptance criteria
- [ ] `FxTextArea` extends `TextArea`
- [ ] Shares `FxTextField`'s `Size` and `ValidationState` enums and, where sensible, its base style class (`fxk-input`) so both are styled by the same CSS rules
- [ ] No-arg constructor and a constructor taking initial text
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement, reusing `FxTextField`'s enums directly (don't duplicate them) if they fit as-is, or explain in Javadoc why a separate set was needed
- [ ] Extend the shared "text input" CSS section rather than starting a new one
- [ ] Tests, Javadoc

Depends on: "FxTextField with sizes and validation states".
EOF

story "Inputs showcase page" 3 "epic:showcase" "Showcase" "6" <<'EOF'
## User story
As a maintainer, I want a showcase page for the text input components, so that they can be reviewed the same way FxButton and FxCard already are.

## Acceptance criteria
- [ ] A new sidebar entry shows `FxTextField` (every size x every validation state, plus disabled and a placeholder example) and `FxTextArea`
- [ ] Includes a code snippet, per the Phase 3 snippet pattern
- [ ] Both themes checked

## Tasks
- [ ] Build the page following `ButtonPage`/`CardPage` as the pattern
- [ ] Register it in the navigation shell's page list

Depends on: the Phase 3 navigation-shell and code-snippet stories, and both text input components.
EOF

# ---- Sprint 7: selection controls -------------------------------------------
# Sprint Goal: "Checkbox, radio, toggle switch and select, all consistent."

story "FxCheckbox and FxRadioButton" 5 "epic:selection" "FxCheckbox & FxRadioButton" "7" <<'EOF'
## User story
As a JavaFX developer, I want styled checkboxes and radio buttons, so that selection controls match the rest of FXKit.

## Acceptance criteria
- [ ] `FxCheckbox extends CheckBox` and `FxRadioButton extends RadioButton` (both are the checklist's "subclass an existing control" case)
- [ ] Both share a `Size` enum consistent with `FxButton`/`FxTextField`'s scale
- [ ] Checked, unchecked, indeterminate (checkbox only), hover, focus and disabled states all styled from tokens
- [ ] `FxRadioButton` groups normally via the standard `ToggleGroup` - nothing FXKit-specific needed for grouping
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement both (one story since they share almost all their CSS: the box/indicator look, not the label look)
- [ ] Add a "selection controls" section to `components.css`
- [ ] Showcase examples: a few standalone checkboxes plus one grouped set of radio buttons
EOF

spike "Decision (ADR): FxToggle base class - subclass an existing control, or Control+Skin?" 2 "epic:selection" "FxToggle" "7" <<'EOF'
## Question to answer
JavaFX has no built-in on/off switch. Two real options:
- **Subclass `CheckBox`**, restyled to look like a sliding switch. Its `selected`/`indeterminate` state,
  keyboard handling (Space toggles) and accessibility role are already correct; only the *look* changes.
  This is close to how many web toggle switches are, under the hood, a restyled checkbox.
- **Build a `Control` + `Skin`** from scratch: a real node structure (track + knob), your own
  `selectedProperty()`, and hand-written mouse/keyboard handling and accessibility.

Apply `docs/new-component-checklist.md` section 1's rule of thumb: "Build a `Control`+`Skin` only when
neither existing-class option fits." Decide which this is, and if it's Control+Skin, write down what a
minimal Skin needs: the `Skin` interface, `createDefaultSkin`, computing min/pref sizes, and toggling the
`:selected` pseudo-class in response to both mouse and keyboard input.

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] `docs/adr/ADR-005-toggle-base-class.md`
- [ ] If Control+Skin: a short outline of the Skin's structure to build from next

## Findings
EOF

story "FxToggle: structure and selected property" 5 "epic:selection" "FxToggle" "7" <<'EOF'
## User story
As a JavaFX developer, I want an on/off switch control, so that I don't have to fake one out of a checkbox and CSS myself every time.

## Acceptance criteria
- [ ] Follows the base-class decision from ADR-005
- [ ] A `selected`/`selectedProperty()` boolean (reusing `CheckBox`'s own if that's the chosen base class) that drives which side the knob sits on
- [ ] Keyboard accessible: Tab focuses it, Space toggles it, same as a checkbox
- [ ] `Size` enum consistent with the other selection controls
- [ ] No-arg constructor

## Tasks
- [ ] Implement per the ADR
- [ ] If Control+Skin: build the minimal Skin (track + knob nodes, position driven by `selectedProperty()`)
- [ ] Confirm keyboard toggling works before moving to styling (next story)
EOF

story "FxToggle: styling and states" 3 "epic:selection" "FxToggle" "7" <<'EOF'
## User story
As a JavaFX developer, I want the toggle to look and feel like a switch, so that flipping it reads clearly as on/off.

## Acceptance criteria
- [ ] On/off, hover, focus and disabled states styled from tokens, matching the other selection controls
- [ ] The knob visibly moves between the off and on positions when clicked or toggled by keyboard
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Style the track and knob (colors, radius - a fully round knob and pill-shaped track, per `tools/scales.txt`'s `radius full`)
- [ ] Move the knob on state change; if JavaFX CSS transitions aren't reliably available in this JavaFX version, an instant position change is an acceptable fallback - note whichever was used and why in the class Javadoc
- [ ] Verify both themes
- [ ] Tests, Javadoc

Depends on: "FxToggle: structure and selected property".
EOF

story "FxSelect (styled ComboBox)" 5 "epic:selection" "FxSelect" "7" <<'EOF'
## User story
As a JavaFX developer, I want a styled dropdown, so that a select control matches the rest of a form built with FXKit.

## Acceptance criteria
- [ ] `FxSelect<T> extends ComboBox<T>` (generic, so it works with any item type - the checklist's "subclass" case again)
- [ ] `Size` enum consistent with the other input/selection controls
- [ ] The control itself (border, radius, arrow) AND its popup list (row hover, row selected) are styled from tokens in both themes
- [ ] Existing `ComboBox` API (item converter, custom cell factory, editable mode) keeps working unchanged
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement the `Size` property
- [ ] Find the right selectors for the popup content (JavaFX's Modena stylesheet's `.combo-box-popup .list-cell`/`.list-view` selectors are the starting point - the popup isn't a normal scene-graph child, so this needs a bit of digging; write down what worked in `docs/css-notes.md` if it's non-obvious)
- [ ] Showcase with a handful of sample items
EOF

story "Selection controls showcase page" 3 "epic:showcase" "Showcase" "7" <<'EOF'
## User story
As a maintainer, I want a showcase page for the selection controls, so that FxCheckbox, FxRadioButton, FxToggle and FxSelect can all be reviewed together.

## Acceptance criteria
- [ ] One sidebar entry covering all four components, each across its sizes and a disabled example
- [ ] `FxRadioButton` shown as a grouped set, not just standalone
- [ ] Includes code snippets
- [ ] Both themes checked

## Tasks
- [ ] Build the page
- [ ] Register it in the navigation shell

Depends on: the Phase 3 navigation-shell and code-snippet stories, and all four selection components.
EOF

# ---- Sprint 8: feedback & display --------------------------------------------
# Sprint Goal: "Badge, alert, progress and avatar."

story "FxBadge" 3 "epic:feedback" "FxBadge" "8" <<'EOF'
## User story
As a JavaFX developer, I want a small status badge, so that I can label things (new, beta, a count) without building one from scratch each time.

## Acceptance criteria
- [ ] `FxBadge extends Label` (small, non-interactive - the checklist's "subclass" case)
- [ ] `Variant` enum reusing `FxButton.Variant`'s palette naming (`PRIMARY`, `SECONDARY`, `DANGER`, `SUCCESS`; decide whether `GHOST`/`OUTLINE` make sense for a badge too, and document the choice either way)
- [ ] Pill shape (`radius full`, from `tools/scales.txt`) and compact padding
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement per the checklist
- [ ] Add a "badge" section to `components.css`
- [ ] Showcase: one badge per variant
EOF

story "FxAlert" 5 "epic:feedback" "FxAlert" "8" <<'EOF'
## User story
As a JavaFX developer, I want an inline alert/banner, so that I can show info, success, warning or error messages consistently, with an optional dismiss.

## Acceptance criteria
- [ ] Base class decided and cited: `FxCard`'s "layout of optional/typed slots" rule (`docs/adr/ADR-003-card-base-class.md`) is the closest fit if it needs a message plus an optional icon and close button as slots - use it unless a concrete reason rules it out, in which case write a short new ADR
- [ ] `Variant` enum `INFO`, `SUCCESS`, `WARNING`, `DANGER`, tokens only
- [ ] An optional icon (reuse the `Ikon`-typed property pattern from `FxButton`, #36 - same idea, not copy-pasted code if it can be shared)
- [ ] An optional dismiss button; dismissing it fires an `onClose` handler, following the convention decided in ADR-004
- [ ] A text/message property (and consider a body `Node` slot, like `FxCard`, for richer content - decide and document)
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement per the chosen base class
- [ ] Wire the dismiss button through the ADR-004 event convention
- [ ] Add an "alert" section to `components.css`
- [ ] Showcase: all four variants, with and without a dismiss button

Depends on: the event-convention ADR (#see Sprint 6) and, if reused, FxButton's icon property.
EOF

story "FxProgressBar" 3 "epic:feedback" "FxProgressBar" "8" <<'EOF'
## User story
As a JavaFX developer, I want a progress bar that follows FXKit's palette, so that it doesn't clash with the rest of a themed screen.

## Acceptance criteria
- [ ] `FxProgressBar extends ProgressBar`
- [ ] `Variant` enum (at least `PRIMARY`, `SUCCESS`, `DANGER`, `WARNING`) recoloring the bar's fill from tokens, replacing JavaFX's fixed accent color
- [ ] Indeterminate mode (`ProgressIndicator.INDETERMINATE_PROGRESS`) still works and looks right in both themes
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement the `Variant` property
- [ ] Find and override the right selectors for the track and the fill (JavaFX's Modena stylesheet uses `.track` and `.bar`)
- [ ] Showcase: a determinate bar at a few values, per variant, plus one indeterminate example
EOF

story "FxAvatar" 3 "epic:feedback" "FxAvatar" "8" <<'EOF'
## User story
As a JavaFX developer, I want a circular avatar with an initials fallback, so that a user's photo (or their initials, if there is none) shows consistently.

## Acceptance criteria
- [ ] Circular image when one is set; falls back to initials (on a token-colored background) when no image is set, or the image fails to load
- [ ] `Size` enum, reusing the shared scale (consider adding an `XL` specifically for avatars - decide and document)
- [ ] Base class: a `StackPane` subclass fits the checklist's "layout of optional/typed slots" rule (same reasoning as `FxCard`) - an `ImageView` clipped to a circle, stacked with an initials `Label`, toggling which is visible
- [ ] Meets every item in `docs/new-component-checklist.md`

## Tasks
- [ ] Implement, deciding and documenting the initials algorithm (e.g. first letter of the first and last words of a "display name" property)
- [ ] Add an "avatar" section to `components.css`
- [ ] Showcase: with an image, with initials, across sizes
EOF

story "Typography helper classes" 2 "epic:feedback" "Typography" "8" <<'EOF'
## User story
As a JavaFX developer, I want ready-made heading/text style classes, so that I don't have to remember to combine `text-2xl` + `font-bold` + `text-body` every time I want a heading.

## Acceptance criteria
- [ ] A small set of classes (e.g. `.heading-1` through `.heading-4`, maybe `.body-text`/`.caption`) that each combine an existing font-size, weight and color token in one class
- [ ] Documented in `docs/utility-classes.md`
- [ ] Decide and document where they live: a new section of `components.css` (hand-written, since they're compositions of existing utility values, not a new scale) vs adding them to the generator - either is fine, but the reasoning should be written down

## Tasks
- [ ] Pick the values (reusing `tools/scales.txt`'s font scale, nothing new to invent)
- [ ] Add the classes
- [ ] Update the docs
- [ ] One example in the showcase
EOF

story "Feedback & display showcase page" 3 "epic:showcase" "Showcase" "8" <<'EOF'
## User story
As a maintainer, I want a showcase page for FxBadge, FxAlert, FxProgressBar, FxAvatar and the typography classes, so that Phase 4's last components can be reviewed together.

## Acceptance criteria
- [ ] One sidebar entry (or, if it gets too long, split into two - decide when building it) covering all five
- [ ] Every variant/size shown; disabled, dismiss and indeterminate edge cases covered
- [ ] Includes code snippets
- [ ] Both themes checked

## Tasks
- [ ] Build the page(s)
- [ ] Register in the navigation shell

Depends on: the Phase 3 navigation-shell and code-snippet stories, and FxBadge, FxAlert, FxProgressBar, FxAvatar, and the typography classes.
EOF

story "Component Checklist walkthrough for Phase 4" 2 "epic:fxml" "Checklist" "8" <<'EOF'
## User story
As a maintainer, I want to walk `docs/new-component-checklist.md` against every Phase 4 component, so that gaps are caught the same way they were for FxButton and FxCard in Phase 2.

## Acceptance criteria
- [ ] Every Phase 4 component (`FxTextField`, `FxTextArea`, `FxCheckbox`, `FxRadioButton`, `FxToggle`, `FxSelect`, `FxBadge`, `FxAlert`, `FxProgressBar`, `FxAvatar`) is checked against every section of the checklist
- [ ] Any gap found is fixed, not just noted
- [ ] The checklist's "Walkthrough log" section is updated to record this pass, same as it records the Phase 2 one

## Tasks
- [ ] Go through the checklist once per component
- [ ] Fix anything missing
- [ ] Update the log

Depends on: every other Phase 4 story.
EOF

# ------------------------------ 4. SUMMARY -----------------------------------
say "4/4  Done"
info "Created: $CREATED   Skipped (already existed): $SKIPPED   Points created: $TOTAL_POINTS"
if [[ "$DRY_RUN" == "yes" ]]; then
  info "This was a dry run. Re-run without DRY_RUN=yes to create the issues."
else
  info "Board: $PROJECT_URL"
  info ""
  info "Next (Sprint 5 planning, in the web UI):"
  info "  - Open the Backlog view; the items are already in priority order"
  info "  - Move the Sprint 5 items to Sprint Backlog and set Sprint = Sprint 5"
fi
