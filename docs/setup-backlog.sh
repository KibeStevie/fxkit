#!/usr/bin/env bash
# =============================================================================
# FXKit: Product Backlog script (Phases 1 and 2)      Sprint 0, Story 11
#
# Extends setup-github.sh. Creates (safe to re-run; issues matched by title):
#   1. New epic labels for Phases 1 and 2
#   2. 11 Phase 1 issues (Sprints 1-2)  -> milestone "Phase 1: Design Foundation"
#   3. 12 Phase 2 issues (Sprints 3-4)  -> milestone "Phase 2: First Components"
#   4. Every issue is added to the "FXKit Roadmap" board with its Points
#
# Issues are created in PRIORITY ORDER, so they land on the board in that
# order. Story points come from docs/FXKIT_DEVELOPMENT_PHASES.md.
#
# Prerequisites: same as setup-github.sh (gh, jq, gh auth login,
#                gh auth refresh -s project)
#
# Usage:
#   DRY_RUN=yes ./setup-backlog.sh     # print what would be created, touch nothing
#   ./setup-backlog.sh                 # create everything
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

MS_PHASE1="Phase 1: Design Foundation"
MS_PHASE2="Phase 2: First Components"

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
# Phase 1
label "epic:tokens"    "0052cc" "Epic: design tokens (colors, spacing, radius, type, shadow)"
label "epic:utilities" "006b75" "Epic: utility CSS classes"
label "epic:theming"   "5319e7" "Epic: light/dark theme and ThemeManager"
# Phase 2
label "epic:button"    "d93f0b" "Epic: FxButton"
label "epic:card"      "e99695" "Epic: FxCard"
label "epic:fxml"      "bfd4f2" "Epic: FXML support, Scene Builder and component checklist"

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
# PHASE 1: DESIGN FOUNDATION  (Sprints 1-2)
# =============================================================================
PHASE_NO="1"; MILESTONE="$MS_PHASE1"
say "Phase 1: Design Foundation"

# ---- Sprint 1: Tokens -------------------------------------------------------
# Sprint Goal: "Every color, spacing and radius value lives in one token file."

spike "Spike: How JavaFX CSS differs from web CSS" 3 "epic:tokens" "Design tokens" "1" <<'EOF'
## Question to answer
Which web-CSS habits do NOT work in JavaFX CSS, and what do we use instead? Cover at least:
- No `var()`, `calc()` or media queries: how do looked-up colors work, and can they hold non-color values (sizes, effects)?
- Inheritance and the `.root` selector
- Pseudo-classes (`:hover`, `:focused`, `:pressed`, `:disabled`)
- `-fx-` property names, `-fx-effect: dropshadow(...)`, `-fx-background-radius` vs `-fx-border-radius`
- Style classes vs IDs, and `scene.getStylesheets()`

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] Findings written up in `docs/css-notes.md`
- [ ] Anything that blocks the token design is flagged (feeds the token stories in this sprint)
- [ ] Follow-up stories created if needed

## Findings
EOF

story "Create tokens.css and load it in the showcase app" 2 "epic:tokens" "Design tokens" "1" <<'EOF'
## User story
As a JavaFX developer, I want all design tokens in one stylesheet that ships with the library, so that I can change the look of the whole UI from a single place.

## Acceptance criteria
- [ ] `tokens.css` exists in `fxkit-core` resources and defines tokens on `.root`
- [ ] The showcase loads it with `scene.getStylesheets()` and it works when started with `./run.sh` (module path)
- [ ] The temporary inline `setStyle(...)` in `ShowcaseApp` is replaced by a token
- [ ] Changing one token value visibly changes the showcase window

## Tasks
- [ ] Choose the resource location (JPMS note: resources inside a package are encapsulated unless that package is opened or exported to the caller, so verify the showcase can actually load the file)
- [ ] Create `tokens.css` with a single test token (e.g. `-fxk-blue-500`)
- [ ] Load it in `ShowcaseApp` and use the token on the label
- [ ] Rebuild with `mvn clean install` and run
EOF

story "Define color scales 50 to 900 as looked-up colors" 5 "epic:tokens" "Design tokens" "1" <<'EOF'
## User story
As a JavaFX developer, I want consistent color scales, so that I can pick shades by name instead of writing hex values.

## Acceptance criteria
- [ ] Scales for `gray`, `blue`, `red`, `green` and `yellow`, each with steps 50, 100, 200, 300, 400, 500, 600, 700, 800, 900
- [ ] Named `-fxk-<scale>-<step>` (e.g. `-fxk-blue-500`) and defined on `.root` in `tokens.css`
- [ ] Each scale runs light to dark with a visible, even progression
- [ ] No hard-coded hex values are used anywhere outside `tokens.css`

## Tasks
- [ ] Choose the palette values (if you borrow from an existing palette, check its license and credit it)
- [ ] Add the 50 tokens to `tokens.css`
- [ ] Spot-check a few tokens on a temporary showcase node
EOF

story "Define semantic color tokens" 3 "epic:tokens" "Design tokens" "1" <<'EOF'
## User story
As a JavaFX developer, I want semantic color tokens (primary, surface, text, ...), so that components and themes refer to meaning instead of specific shades.

## Acceptance criteria
- [ ] At least `-fxk-primary`, `-fxk-surface`, `-fxk-text`, `-fxk-border` and `-fxk-danger` exist, each defined in terms of a color-scale token (e.g. `-fxk-primary: -fxk-blue-600`)
- [ ] Tokens for success, warning and muted text are added if the design needs them
- [ ] A short table of semantic tokens and their light-theme values is added to the docs
- [ ] Rule agreed: components use semantic tokens, never the raw scales

## Tasks
- [ ] Decide the list and the mapping to the scales
- [ ] Add them to `tokens.css`
- [ ] Document them
EOF

story "Define spacing, radius, font-size and shadow scales" 3 "epic:tokens" "Design tokens" "1" <<'EOF'
## User story
As a JavaFX developer, I want scales for spacing, radius, font size and shadows, so that layouts and components stay visually consistent.

## Acceptance criteria
- [ ] Spacing scale defined (choose a base unit, e.g. 4px)
- [ ] Radius scale defined (e.g. sm, md, lg, full)
- [ ] Font-size scale defined (e.g. xs to 3xl)
- [ ] Shadow scale defined (sm, md, lg)
- [ ] Wherever JavaFX CSS cannot express a token as a looked-up value (see the CSS spike), the workaround is written down

## Tasks
- [ ] Pick the values for each scale
- [ ] Add them to `tokens.css`
- [ ] Note any limitation in `docs/css-notes.md`
EOF

# ---- Sprint 2: Utilities & themes -------------------------------------------
# Sprint Goal: "I can style any node with utility classes and switch between light and dark."

spike "Decision (ADR): How to handle margin and gap in JavaFX CSS" 2 "epic:utilities" "Utility classes" "2" <<'EOF'
## Question to answer
JavaFX CSS has no `margin` property. How do we offer margin and gap utilities?
Options to compare:
- Layout-pane CSS properties: `-fx-spacing`, `-fx-hgap`, `-fx-vgap`, `-fx-padding`
- Java helpers wrapping `setMargin` (e.g. `Insets`-based)
- Wrapper containers, or leaving margin out of the utility set

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] `docs/adr/ADR-001-margin-and-gap.md` following the ADR format (Status, Context, Options, Decision, Consequences)
- [ ] Follow-up stories created for anything the decision adds

## Findings
EOF

story "Utility classes: background, text, border, radius, padding, font" 5 "epic:utilities" "Utility classes" "2" <<'EOF'
## User story
As a JavaFX developer, I want utility classes, so that I can style any node from Java or FXML without writing CSS.

## Acceptance criteria
- [ ] Working classes for background color (`.bg-blue-500`), text color (`.text-gray-700`), border color, radius (`.rounded-md`), padding (`.p-4`), font size and font weight
- [ ] Every class draws from the tokens (no hard-coded values)
- [ ] Naming convention is written down
- [ ] A short reference file (e.g. `docs/utility-classes.md`) lists the classes
- [ ] The showcase demonstrates a node styled only with utility classes

## Tasks
- [ ] Decide the naming convention
- [ ] Write `utilities.css` (hand-written for now; see the generator spike)
- [ ] Load it in the showcase
- [ ] Write the reference file

Depends on: the margin/gap ADR (for the padding classes) and the Phase 1 token stories.
EOF

story "Dark theme: redefine semantic tokens under .root.dark" 3 "epic:theming" "Theming" "2" <<'EOF'
## User story
As a JavaFX developer, I want a dark theme, so that my app supports both light and dark modes without extra CSS.

## Acceptance criteria
- [ ] Semantic tokens are redefined under `.root.dark`
- [ ] Adding the `dark` style class to the root flips every semantic-token-based style
- [ ] Body text on the surface color has readable contrast in both themes (target 4.5:1 or better)
- [ ] Dark values only touch semantic tokens, never the raw scales

## Tasks
- [ ] Choose dark values for each semantic token
- [ ] Add the `.root.dark` block to `tokens.css`
- [ ] Check contrast for text, border and danger colors
EOF

story "ThemeManager: switch between light and dark" 3 "epic:theming" "Theming" "2" <<'EOF'
## User story
As a JavaFX developer, I want `ThemeManager.apply(scene, Theme.DARK)`, so that I can switch themes with one call.

## Acceptance criteria
- [ ] `Theme` enum with `LIGHT` and `DARK`
- [ ] `ThemeManager.apply(scene, theme)` sets the root style class and ensures the FXKit stylesheets are added exactly once (calling it twice does not duplicate them)
- [ ] The package is exported in `module-info.java`
- [ ] Javadoc on the public API; basic tests if feasible

## Tasks
- [ ] Create `Theme` and `ThemeManager`
- [ ] Load `tokens.css` and `utilities.css` through the manager (replace the manual loading in the showcase)
- [ ] Try it from `ShowcaseApp`
EOF

story "Color palette page in the showcase" 3 "epic:theming" "Theming" "2" <<'EOF'
## User story
As a maintainer, I want a page showing every color token, so that I can check the palette and both themes visually.

## Acceptance criteria
- [ ] A scene showing every swatch, grouped by scale and labeled with its token name
- [ ] Semantic tokens shown separately
- [ ] A light/dark toggle on the page
- [ ] Changing one token (e.g. `-fxk-blue-500`) visibly changes the page

## Tasks
- [ ] Build the page (a simple grid is fine; the full showcase layout comes in Phase 3)
- [ ] Wire the toggle to `ThemeManager`
- [ ] Verify both themes
EOF

spike "Spike (stretch): Generate the utility CSS from one palette definition" 3 "epic:utilities" "Utility classes" "2, stretch" <<'EOF'
## Question to answer
Writing thousands of utility classes by hand will not scale. Can a small script generate `utilities.css` (and possibly `tokens.css`) from one palette definition?

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] Proof-of-concept script (language of your choice, or a Maven step) that generates the color utilities
- [ ] Recommendation: adopt it now, later, or not at all (findings note or ADR)
- [ ] Follow-up story created if adopted (the finalized generator is planned for Phase 7)

## Findings
EOF

# =============================================================================
# PHASE 2: FIRST COMPONENTS  (Sprints 3-4)
# =============================================================================
PHASE_NO="2"; MILESTONE="$MS_PHASE2"
say "Phase 2: First Components"

# ---- Sprint 3: FxButton -----------------------------------------------------
# Sprint Goal: "A button with variants and sizes that works in Java and FXML."

spike "Spike: Extend Button or build a custom Control and Skin?" 3 "epic:button" "FxButton" "3" <<'EOF'
## Question to answer
Should `FxButton` extend the existing JavaFX `Button`, or be a custom `Control` with its own `Skin`? Compare speed to build, Scene Builder friendliness, styling control, and accessibility/behavior we would have to re-implement. Also decide when the pattern should be used for later components.

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] `docs/adr/ADR-002-button-base-class.md` (Status, Context, Options, Decision, Consequences)
- [ ] A short rule of thumb for "subclass" vs "Control + Skin" for future components

## Findings
EOF

story "Reusable helper: sync an enum property to a style class" 3 "epic:button" "FxButton" "3" <<'EOF'
## User story
As a maintainer, I want a helper that keeps one style class in sync with an enum property, so that every component maps properties to styling the same way.

## Acceptance criteria
- [ ] Given a node, a class prefix and an enum property, exactly one matching style class is present at any time (e.g. `fxk-btn-danger`), and changing the value swaps it
- [ ] Works for any enum type (generic)
- [ ] JUnit 5 is set up in the build and the helper has unit tests
- [ ] Javadoc on the public API

## Tasks
- [ ] Add JUnit 5 and `maven-surefire-plugin` to the parent POM (neither is there yet)
- [ ] Implement the helper (listener on the property, initial sync at creation)
- [ ] Write unit tests (initial value, change, null handling)
- [ ] Decide where it lives (internal package vs public API) and update `module-info.java` accordingly
EOF

story "FxButton with a variant property" 5 "epic:button" "FxButton" "3" <<'EOF'
## User story
As a JavaFX developer, I want to set a button's variant (primary, danger, ghost, ...), so that I can style it without writing CSS.

## Acceptance criteria
- [ ] `FxButton` exists with a `Variant` enum: `PRIMARY`, `SECONDARY`, `DANGER`, `SUCCESS`, `GHOST`, `OUTLINE`
- [ ] Typed property: `getVariant()`, `setVariant()`, `variantProperty()`, with a documented default
- [ ] `setVariant(DANGER)` applies the danger style; changing the variant removes the old style
- [ ] Each variant looks right in light and dark themes, using tokens only
- [ ] No-arg constructor and a constructor taking text
- [ ] Javadoc on the public API

## Tasks
- [ ] Follow the ADR decision on the base class
- [ ] Create the package `dev.fxkit.core.components`, export it, and uncomment the `opens dev.fxkit.core.components to javafx.fxml;` line in core's `module-info.java`
- [ ] Implement the property using the enum-to-style-class helper
- [ ] Write the variant CSS in a component stylesheet
- [ ] Try all variants in the showcase

Depends on: the ADR spike, the helper story, and the Phase 1 tokens and theme.
EOF

story "FxButton sizes: SM, MD, LG" 3 "epic:button" "FxButton" "3" <<'EOF'
## User story
As a JavaFX developer, I want small, medium and large buttons, so that I can fit them to the layout.

## Acceptance criteria
- [ ] `Size` enum with `SM`, `MD`, `LG` and a `size` property (getter, setter, `sizeProperty()`), default `MD`
- [ ] Each size changes padding and font size using spacing and font-size tokens
- [ ] Size and variant can be combined freely
- [ ] Javadoc on the public API

## Tasks
- [ ] Add the property using the enum-to-style-class helper
- [ ] Add the size CSS
- [ ] Check all 6 variants x 3 sizes in the showcase
EOF

story "FxButton states: hover, pressed, focused, disabled" 3 "epic:button" "FxButton" "3" <<'EOF'
## User story
As a JavaFX developer, I want buttons to react to hover, press, focus and disabled, so that my UI feels responsive and is keyboard friendly.

## Acceptance criteria
- [ ] Distinct hover, pressed, focused (visible focus ring) and disabled styles for every variant
- [ ] All state colors come from tokens
- [ ] Verified in both light and dark themes

## Tasks
- [ ] Add `:hover`, `:pressed`, `:focused` and `:disabled` rules per variant
- [ ] Check keyboard focus (Tab, then Space or Enter)
- [ ] Review each state in both themes
EOF

story "Unit tests for FxButton variant and size" 2 "epic:button" "FxButton" "3" <<'EOF'
## User story
As a maintainer, I want tests for FxButton's property-to-style logic, so that refactors don't silently break styling.

## Acceptance criteria
- [ ] Changing `variant` updates the style classes (old removed, new added)
- [ ] Changing `size` updates the style classes
- [ ] Default values are asserted
- [ ] Tests run in `mvn clean verify`

## Tasks
- [ ] Work out how to run controls in a unit test (the JavaFX toolkit may need to be started, e.g. with `Platform.startup(...)`, or the test may avoid needing it)
- [ ] Write the tests
- [ ] Confirm they pass in `mvn clean verify`

Depends on: the JUnit 5 setup from the helper story.
EOF

# ---- Sprint 4: FxCard and FXML ----------------------------------------------
# Sprint Goal: "A card component and confirmed FXML support for both components."

story "FxCard with header, body and footer slots" 5 "epic:card" "FxCard" "4" <<'EOF'
## User story
As a JavaFX developer, I want a card with header, body and footer areas, so that I can group content consistently.

## Acceptance criteria
- [ ] `FxCard` has header, body and footer slots, each settable from Java (e.g. `setHeader(Node)`)
- [ ] Empty slots take no space
- [ ] No-arg constructor
- [ ] Follows the pattern chosen in the button ADR (subclass or Control + Skin)
- [ ] Javadoc on the public API

## Tasks
- [ ] Choose the base class (a `VBox`-style container is likely enough for the layout; confirm against the ADR)
- [ ] Implement the three slots
- [ ] Try it in the showcase with sample content
EOF

story "FxCard styling: border, radius, background and elevation" 3 "epic:card" "FxCard" "4" <<'EOF'
## User story
As a JavaFX developer, I want cards that look polished with adjustable elevation, so that I can create visual hierarchy.

## Acceptance criteria
- [ ] Border, radius, background and shadow come from tokens
- [ ] An `elevation` property with levels (e.g. `NONE`, `SM`, `MD`, `LG`) mapped to the shadow scale
- [ ] Looks right in both themes
- [ ] Section dividers between header, body and footer are styled

## Tasks
- [ ] Write the card CSS
- [ ] Add the elevation property using the enum-to-style-class helper
- [ ] Review both themes
EOF

story "Verify FXML loading for FxButton and FxCard" 5 "epic:fxml" "FXML & tooling" "4" <<'EOF'
## User story
As a JavaFX developer, I want to use FxButton and FxCard in FXML, so that I can build screens declaratively or in Scene Builder.

## Acceptance criteria
- [ ] An FXML file uses `<FxButton variant="DANGER" size="LG" text="Delete"/>` and loads without errors
- [ ] `FxCard` slots can be filled in FXML (e.g. via `@DefaultProperty` or named child elements)
- [ ] Works from the showcase module (a different module than core), so `exports` and `opens` in `module-info.java` are proven
- [ ] Any workarounds are documented

## Tasks
- [ ] Add `@DefaultProperty` where needed
- [ ] Confirm the no-arg constructors and property setters
- [ ] Add an FXML demo to the showcase
- [ ] Fix `module-info.java` (`exports` plus `opens ... to javafx.fxml`) if loading fails
EOF

spike "Spike: Load FXKit in Scene Builder" 3 "epic:fxml" "FXML & tooling" "4" <<'EOF'
## Question to answer
Can Scene Builder load the FXKit JAR, show FxButton and FxCard in its library, and let us set variant and size in the inspector? What does it take?

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] Step-by-step notes under `docs/` (import the JAR, what works, what doesn't)
- [ ] Known limitations and workarounds listed
- [ ] Follow-up stories created (Scene Builder support is a known risk; a full integration guide is planned for Phase 7)

## Findings
EOF

story "Write the Component Checklist (docs/new-component-checklist.md)" 2 "epic:fxml" "FXML & tooling" "4" <<'EOF'
## User story
As a maintainer, I want a checklist for creating new components, so that every component is built the same way.

## Acceptance criteria
- [ ] `docs/new-component-checklist.md` exists, based on the Definition of Done (API, FXML, tokens, states, events, showcase page, tests, Javadoc)
- [ ] Includes the practical steps learned from FxButton and FxCard (package, `module-info`, stylesheet, helper usage)
- [ ] The checklist was followed for both components and gaps were fixed

## Tasks
- [ ] Draft the checklist from the Definition of Done
- [ ] Add the lessons from this sprint
- [ ] Walk through it once for FxButton and FxCard
EOF

story "Stretch: icon support on FxButton via Ikonli" 3 "epic:button" "FxButton" "4, stretch" <<'EOF'
## User story
As a JavaFX developer, I want to put an icon on a button, so that actions are easier to recognize.

## Acceptance criteria
- [ ] `FxButton` accepts an icon (Ikonli `Ikon` or a `Node` graphic) settable from Java and FXML
- [ ] Icon color follows the variant's text color (tokens)
- [ ] Icon-only buttons work and remain accessible (tooltip or accessible text)
- [ ] The Ikonli dependency is added to the POM without breaking `module-info.java`

## Tasks
- [ ] Add the Ikonli dependency (check its module names for `requires`)
- [ ] Implement the icon property
- [ ] Style icon-only buttons
- [ ] Add a showcase example
EOF

# ------------------------------ 4. SUMMARY -----------------------------------
say "4/4  Done"
info "Created: $CREATED   Skipped (already existed): $SKIPPED   Points created: $TOTAL_POINTS"
if [[ "$DRY_RUN" == "yes" ]]; then
  info "This was a dry run. Re-run without DRY_RUN=yes to create the issues."
else
  info "Board: $PROJECT_URL"
  info ""
  info "Next (Sprint 1 planning, in the web UI):"
  info "  - Open the Backlog view; the items are already in priority order"
  info "  - Move the Sprint 1 items to Sprint Backlog and set Sprint = Sprint 1"
fi
