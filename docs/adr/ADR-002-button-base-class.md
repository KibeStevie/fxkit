# ADR-002: FxButton's base class — extend Button, or Control + Skin?

**Status:** Accepted
**Date:** 2026-09-19
**Story:** #25 (spike: Extend Button or build a custom Control and Skin?)
**Time-box:** 1 day

## Question to answer

Should `FxButton` extend the existing JavaFX `Button`, or be a custom `Control` with its own `Skin`?
Compare speed to build, Scene Builder friendliness, styling control, and accessibility/behavior we
would otherwise have to re-implement. Also decide when each pattern should be used for later components.

## Context

`FxButton` (#27) only needs to add **typed properties that select a look** (`variant`, and later `size`):
the same click, focus, keyboard-activation (Space/Enter), mnemonic and default-button behavior that
`Button`/`ButtonBase` already implement is exactly what we want to keep. Nothing about *what the control
does* is changing — only *how it is painted*, and painting is fully reachable through CSS and style
classes (see `docs/css-notes.md`, `docs/design-tokens.md`).

## Options

**A. Subclass `javafx.scene.control.Button`.**
Add JavaFX properties (`variantProperty`), and sync each one to a style class with the helper from #26.
Styling happens entirely in a stylesheet (`components.css`) against `ButtonSkin`'s existing structure
(one `LabeledText`/graphic inside a `StackPane`-like layout).

**B. Custom `Control` with a hand-written `Skin`.**
Build the visual tree ourselves (a region, a label, event handlers for press/hover/focus) and implement
a `Skin<FxButtonBase>`. Gives full control over the node tree, at the cost of re-implementing everything
`ButtonBase` already provides: keyboard activation, mnemonic parsing, `ButtonBase.defaultButtonProperty`/
`cancelButtonProperty`, focus traversal, and the accessibility role JavaFX exposes for `Button`
(`AccessibleRole.BUTTON` with the label as its text, screen-reader "pressable" semantics, etc.).

## Comparison

| Criterion | A. Subclass `Button` | B. `Control` + `Skin` |
|---|---|---|
| Speed to build | Fast — only new properties + CSS | Slow — full skin, behavior, focus/keyboard handling from scratch |
| Scene Builder friendliness | Shows up like any `Button` subclass once the JAR/module is on the classpath and the package is opened to `javafx.fxml`; properties appear in the Inspector automatically because they are standard JavaFX properties | Same requirement to open the package, but the skin must also be wired up (`getSkinBase()`/CSS `-fx-skin` or `createDefaultSkin()`) before Scene Builder can render it, which is extra setup for no visual benefit here |
| Styling control | Every visual difference we need (fill, text color, border, radius, padding per state) is a CSS declaration keyed off a style class — no missing hook | Same CSS reach, but we would also own the node structure the CSS targets, which is more to maintain for zero extra styling power in this case |
| Accessibility / behavior we'd re-implement | None — inherited for free from `ButtonBase`/`Button` | Keyboard activation, mnemonic parsing, default/cancel button flags, focus traversal, accessible role — all on us to reproduce and keep correct |
| Risk | Low: standard JavaFX subclassing pattern | Higher: skins are the most failure-prone part of custom controls (padding/measurement bugs, missed pseudo-classes) |

## Decision

**Option A: `FxButton extends javafx.scene.control.Button`.**

Variants and (later) sizes are implemented as JavaFX properties whose value is mirrored onto a style
class (`fxk-btn-<variant>`) using the enum-to-style-class helper (#26). All visual differences between
variants live in `components.css` and are expressed with semantic/elevation tokens only, never literal
colors, per `docs/design-tokens.md`.

### Rule of thumb for future components

- **Subclass an existing `javafx.scene.control` class when:**
  the component *is* that control with a different look and a small set of extra typed properties
  (variant, size, and similar "which style" switches) — the interaction model, keyboard behavior and
  accessibility role we want already exist on the base class.
- **Build a `Control` + `Skin` when:**
  the component needs a node structure or interaction model that no existing control has — for example
  a composite with several independently laid-out children (`FxCard`'s header/body/footer slots),
  a control with custom keyboard/gesture behavior, or one where the "look" is not just CSS but changes
  what nodes exist (e.g. a segmented control, a rating widget).

`FxCard` (#28+) is expected to fall into the second bucket, since it is not a specialization of any
single existing control.

## Consequences

- `FxButton`'s implementation is small: properties + `components.css`. No new `Skin`, no re-implemented
  keyboard/accessibility behavior.
- Because it *is* a `Button`, every existing `Button` API (`setOnAction`, `setDefaultButton`,
  `setMnemonicParsing`, graphic/text, etc.) keeps working unchanged — nothing to document as "missing."
- The trade-off is that `FxButton` cannot restructure the internal node tree `ButtonSkin` builds. This
  is not a limitation for variant/size styling, but a future requirement such as a built-in loading
  spinner *inside* the button may need either a `graphic` swap (still works with a subclass) or,
  if that turns out to be insufficient, a revisit of this decision for that specific case.
- This ADR's rule of thumb is the default answer for "subclass vs `Control`+`Skin`" questions on later
  components, so each new component does not need its own spike.
