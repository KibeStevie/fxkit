# ADR-003: FxCard's base class — VBox, or Control + Skin?

**Status:** Accepted
**Date:** 2026-09-19
**Story:** #31 (FxCard with header, body and footer slots)

## Question to answer

`ADR-002` guessed that `FxCard` would need a custom `Control` + `Skin`, because it "is not a
specialization of any single existing control" and has "several independently laid-out children."
Story #31's own task list asks to confirm that guess rather than assume it: *"Choose the base class (a
`VBox`-style container is likely enough for the layout; confirm against the ADR)."* This ADR makes that
call.

## Re-applying ADR-002's rule of thumb

> Subclass an existing `javafx.scene.control` class when the component *is* that control with a
> different look and a small set of extra typed properties... Build a `Control` + `Skin` when the
> component needs a node structure or interaction model that no existing control has.

`FxCard` turns out not to need either horn of that as originally framed:

- It isn't a specialization of one existing control (so "subclass `Button`"-style reasoning doesn't
  apply directly) - but the property it needs (`elevation`) is exactly the same shape as `FxButton`'s
  `variant`/`size`: a "which style" switch synced to a style class via the existing
  `EnumStyleClassSync` helper. No new interaction model.
- Its "several independently laid-out children" turn out to be three *optional*, vertically-stacked
  slots with a divider between whichever are present - which is precisely what `javafx.scene.layout.VBox`
  already does. There is no interaction model to build (no keyboard behavior, no gestures, no
  accessibility role beyond what a plain container already has), so the reasons ADR-002 lists for
  reaching for `Control` + `Skin` (re-implementing keyboard activation, focus traversal, accessible
  role) don't apply here - there was nothing like that to re-implement in the first place.

## Options

**A. Subclass `javafx.scene.layout.VBox`.**
Hold header/body/footer as `Node` properties, each wrapped in its own `StackPane` (for a stable CSS
target: `.fxk-card-header`/`-body`/`-footer`) that is only added to `getChildren()` while its slot is
non-empty; insert a `Separator` between adjacent present wrappers. Elevation is a typed enum property
synced to a style class exactly like `FxButton`'s `variant`/`size` (#26's helper).

**B. Custom `Control` with a hand-written `Skin`.**
Define `FxCard` as a `Control`, build the header/body/footer node structure in a `Skin<FxCard>`, and
reproduce layout (VBox already does this), CSS hookup (a `Control`'s skin still needs one), and gains
nothing: there's no keyboard/gesture behavior or accessibility role beyond "a container" to justify
owning the extra machinery.

## Comparison

| Criterion | A. Subclass `VBox` | B. `Control` + `Skin` |
|---|---|---|
| Speed to build | Fast - properties, three wrapper panes, a rebuild method + CSS | Slower - a `Skin` that reimplements the same vertical stacking `VBox` already provides |
| Scene Builder friendliness | Shows up like any `Pane` subclass once the module is opened to `javafx.fxml` (already true for `dev.fxkit.core.components`, #27); `header`/`body`/`footer`/`elevation` are standard JavaFX properties, so they appear in the Inspector automatically | Same opening requirement, plus the skin needs wiring before anything renders, for no visual benefit |
| Styling control | Every visual need (border, radius, background, padding per slot, divider, shadow) is a CSS declaration keyed off a style class on the card or a slot wrapper - no missing hook | Same CSS reach, but the skin also owns the node structure the CSS targets - more to maintain for zero extra styling power here |
| Behavior we'd re-implement | None: no keyboard, gesture or accessibility behavior FxCard needs beyond a plain container's | None *needed*, but a `Skin` obligates us to at least stand up the plumbing (`createDefaultSkin()`, layout children) for behavior that was never required |
| Empty-slot handling ("takes no space") | Free: an empty slot's wrapper is simply never added to `getChildren()` | Same technique, just inside a `Skin` instead of a `Pane` subclass |
| Risk | Low: standard container-subclassing pattern, same shape as `FxButton`'s already-accepted pattern | Higher: skins are the most failure-prone part of custom controls, for a component that doesn't need one |

## Decision

**Option A: `FxCard extends javafx.scene.layout.VBox`.**

This *narrows* ADR-002's rule of thumb rather than contradicting it: "several independently laid-out
children" alone doesn't require `Control` + `Skin` when a `Pane` (or a subclass like `VBox`) already lays
those children out correctly and nothing about *how the user interacts with the component* differs from
a plain container. The dividing question is behavior, not child count:

- **Subclass an existing `javafx.scene.control` class when** the component *is* that control with a
  different look and a small set of extra typed properties (unchanged from ADR-002).
- **Subclass an existing `javafx.scene.layout` class when** the component is a layout of optional/typed
  child slots with no interaction model of its own beyond what that layout class already provides - as
  here.
- **Build a `Control` + `Skin` when** the component needs a node structure, interaction model, or
  accessibility role that no existing class (control *or* layout) provides.

## Consequences

- `FxCard`'s implementation is small: three `Node` properties, three `StackPane` wrappers, a rebuild
  method, an `elevation` enum property, and `components.css` - no new `Skin`.
- Because it *is* a `VBox`, anything a caller might already know about `VBox` (e.g. spacing, alignment on
  the outer container) still applies if they choose to use it, though `FxCard`'s own layout is driven by
  the header/body/footer properties rather than direct `getChildren()` manipulation.
- `@DefaultProperty("body")` (see `FxCard`'s Javadoc) overrides `Pane`'s own default property
  (`children`), so a single unnamed nested FXML element sets the body slot rather than being appended
  directly to the child list - keeping FXML usage consistent with the Java API.
- If a future requirement needs behavior no container provides (e.g. a collapsible card with its own
  keyboard shortcut), that specific case should revisit this decision rather than retrofitting it onto
  `VBox`, per ADR-002's original guidance.
