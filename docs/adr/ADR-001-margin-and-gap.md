# ADR-001: How to handle margin and gap

**Status:** Accepted
**Date:** 2026-09-19
**Story:** #19 (spike: Decision (ADR): How to handle margin and gap in JavaFX CSS)

## Context

Tailwind-style utilities such as `m-4` and `gap-4` are central to the FXKit vision. JavaFX CSS has **no `margin`
property** and no generic `gap`. Space around and between nodes comes from four separate mechanisms:

| Mechanism | Where it applies | Reachable from CSS? |
|---|---|---|
| `-fx-padding` | Inside any `Region` | Yes |
| `-fx-spacing` | Between children of `HBox` / `VBox` | Yes |
| `-fx-hgap`, `-fx-vgap` | Between children of `FlowPane`, `GridPane`, `TilePane` | Yes |
| Margin (`Insets`) | Around one child, inside its parent | **No.** Set in Java (`VBox.setMargin(child, insets)`) or FXML (`<VBox.margin>`). It is stored on the child *per parent type*, so the code must know which kind of pane the child is in. |

Facts confirmed by experiment (see `css-notes.md`, section 8): a property a node does not have (for example
`-fx-spacing` on a `Label`) is **silently ignored**, with no warning.

## Options

**A. Gap and padding utilities only; no margin utilities.**
`.gap-*` writes `-fx-spacing`, `-fx-hgap` and `-fx-vgap` together; `.p-*` writes `-fx-padding`. Margin stays in Java/FXML.

**B. A, plus a Java helper** such as `Spacing.margin(node, 8)` that calls the right `setMargin` for the node's parent.
Works only once the node has a parent, and needs a branch for every pane type.

**C. Wrapper containers** that add the margin as padding of an extra node.
Adds nodes to the scene graph, hides the real structure and makes CSS selectors harder.

**D. Emulate margin with padding on the child itself.**
Rejected outright: the child's background paints *into* its padding, so it is not a margin.

## Decision

**Option A now. Option B is deferred** until real screens show a need for it. C and D are rejected.

Concretely:

- `.gap-N` sets `-fx-spacing`, `-fx-hgap` and `-fx-vgap` in one class. Because unsupported properties are ignored,
  one class works on any layout pane. Put it on the **parent**.
- `.gap-x-N` sets only `-fx-hgap`, and `.gap-y-N` only `-fx-vgap` (FlowPane, GridPane, TilePane).
  For an `HBox`, use `.gap-N`: its `-fx-spacing` is already horizontal.
- `.p-N`, `.px-N`, `.py-N`, `.pt-N`, `.pr-N`, `.pb-N`, `.pl-N` all set the **same** property, `-fx-padding`.
  **Only one padding class per node takes effect.** `class="px-4 py-2"` does not work: the later rule wins.
  Use `.p-N`, or `setPadding(new Insets(...))`, when you need different values per side.
- There are **no `m-*` classes.** Documentation tells users: gap on the parent, padding on containers,
  margin via FXML or Java.

## Consequences

- Layouts get consistent spacing from the same scale (`space` in `tools/scales.txt`), which is what matters most.
- Users coming from Tailwind will look for `m-4`. `utility-classes.md` explains why it is missing and what to use.
- The single-`-fx-padding` limitation is real and documented, not hidden.
- Components (Phase 2 onward) should expose their own `spacing` / `padding` properties instead of asking users
  to fake them with margins.
- **Possible follow-up story:** a `Spacing` helper (Option B), only if the showcase or the templates in Phase 6
  show repeated need for programmatic margins.
