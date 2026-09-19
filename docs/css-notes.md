# JavaFX CSS vs web CSS: notes for FXKit

Output of the spike *"How JavaFX CSS differs from web CSS"* (#14).
Reference: the official *JavaFX CSS Reference Guide* (search "JavaFX CSS reference" for your JavaFX version).

**How to read this:** items marked ✅ are well-established JavaFX behavior; ❌ means it does not work.
Section 8 records the experiment that decided how the spacing, radius, font-size and shadow scales (#18) are built.

---

## 1. What does not exist (and what to use instead)

| Web CSS habit | In JavaFX CSS | FXKit approach |
|---|---|---|
| `var(--x)` custom properties | ✅ No `var()`. The substitute is the **looked-up color**: `-name: value;` defined once, referenced by name | Tokens are looked-up names on `.root` (`-fxk-blue-500`) |
| `calc()` | ✅ Not supported | Precompute values in the token file; do arithmetic in Java if needed |
| Media queries, `@media`, `prefers-color-scheme` | ✅ Not supported | Themes switch by a **style class on the root** (`.root.dark`), toggled from Java (`ThemeManager`, Sprint 2) |
| `margin` | ✅ Not a property | Pane properties: `-fx-padding`, `-fx-spacing` (HBox/VBox), `-fx-hgap`/`-fx-vgap` (GridPane, FlowPane). Per-child margins are set in Java with `setMargin`. Decided in [ADR-001](adr/ADR-001-margin-and-gap.md): gap and padding utilities, no `m-*` |
| `gap`, flexbox, grid | ✅ No CSS layout | Use layout panes in Java/FXML; CSS only tunes their spacing and padding |
| Unprefixed property names (`color`, `border-radius`) | ✅ Every property is `-fx-` prefixed | e.g. `-fx-text-fill`, `-fx-background-radius` |
| `rem`, `%` font sizing, `em` | Limited: `px`, `pt`, `em`, `mm` etc. exist, but `rem` does not | Use `px` in the font-size scale |
| `//` comments | ✅ Only `/* ... */` | |

## 2. Looked-up colors (our variable mechanism)

```css
.root {
    -fxk-blue-500: #3b82f6;        /* define once */
    -fxk-primary:  -fxk-blue-500;  /* a token can point to another token */
}
.some-node {
    -fx-background-color: -fxk-primary;   /* use by name */
}
```

- ✅ Lookups are resolved by walking **up the parent chain** of the node being styled. Defining tokens on
  `.root` (the scene's root node, which always has the style class `root`) makes them available everywhere.
- ✅ A lookup can refer to another lookup, so semantic tokens can be built from the raw scales.
- ✅ Inline styles (`node.setStyle("-fx-background-color: -fxk-primary;")`) can use lookups too.
- ✅ Color functions exist: `derive(color, +20%)`, `ladder(...)`, `rgba(...)`, `web(...)`. Useful for hover
  shades if we ever want computed ones; FXKit uses explicit scale steps instead so every shade is predictable.
- ✅/❌ Lookups for **non-color values**: shadows work, most sizes do not. Results in section 8.

## 3. Inheritance and `.root`

- ✅ `.root` is applied to the scene's root node automatically.
- ✅ JavaFX's built-in stylesheet (**Modena**) also defines its own lookups on `.root` (for example `-fx-base`,
  `-fx-accent`) and derives most control colors from them. This is a future opportunity: overriding a few
  Modena lookups could re-skin standard controls. FXKit does **not** depend on this yet.
- ✅ Font properties (`-fx-font-*`) inherit down the node tree; most others (backgrounds, borders) do not.
  This is why `-fx-text-fill` is set on labels and controls individually rather than on the root.

## 4. Pseudo-classes (states)

- ✅ Built-in: `:hover`, `:pressed`, `:focused`, `:disabled`, `:selected`, `:armed`, and more depending on the control.
- ✅ Custom pseudo-classes are created in Java with `PseudoClass.getPseudoClass("name")` and toggled with
  `pseudoClassStateChanged(...)`. Planned for the component work in Phase 2.
- ✅ Syntax is the same as the web: `.button:hover { ... }`.

## 5. Shape, border and shadow

- ✅ `-fx-background-radius` and `-fx-border-radius` are **separate**. A rounded border without a rounded
  background leaves square corners showing. Set both.
- ✅ `-fx-background-insets` and `-fx-border-insets` shift the painted areas, so the same look can be built in
  different ways. Keep one convention for all components.
- ✅ Shadows use `-fx-effect: dropshadow(gaussian, <color>, <radius>, <spread>, <offsetX>, <offsetY>)`.
  A node has **one** effect, so a shadow and an inner glow cannot both be set through CSS on the same node.
- ✅ A shadow's color can be a token, and a whole `dropshadow(...)` can be a looked-up value (section 8).

## 6. Selectors and applying styles

- ✅ Style classes (`.card`) via `node.getStyleClass().add("card")`; IDs (`#login`) via `setId(...)`.
  FXKit uses **classes only**, so users can combine them (this is what makes utility classes work).
- ✅ In FXML, `styleClass` is **comma-separated**: `styleClass="a, b, c"`. With spaces (`"a b c"`) FXML creates
  **one** class literally named `a b c` and nothing is styled (tested).
- ✅ Descendant (`.a .b`) and child (`.a > .b`) selectors work.
- ✅ Specificity works as on the web: more classes beat fewer. Watch out for it: `.showcase-root .label`
  would override a plain `.showcase-title`. Keep selectors flat.
- ✅ Priority order, lowest to highest: JavaFX's user-agent stylesheet (Modena) → stylesheets on the
  **Scene** → stylesheets on a **Parent** → inline `setStyle(...)`. Inline always wins, so avoid it in components.

## 7. Loading stylesheets

- ✅ `scene.getStylesheets().add(url)` takes a **URL string**, not a file path. Use
  `SomeClass.class.getResource("file.css").toExternalForm()`.
- ⚠️ **JPMS trap:** in a named module, resources inside a package are encapsulated. Another module cannot find
  them unless the package is `opens`-ed. FXKit avoids this with `FxKit.tokensStylesheet()`: the lookup
  runs inside `fxkit-core` and callers only receive the URL.
- ✅ A missing stylesheet or a mistyped property is usually **not an exception**. JavaFX prints a warning to the
  console and carries on, so check the console output when styles seem to be ignored.

## 8. Experiment: which values can be looked-up tokens? (done)

**Tested on:** OpenJFX **11.0.11** (JDK 21, headless, software renderer). The looked-up mechanism is old and unchanged
in spirit, but FXKit targets JavaFX 25, so **please re-run the experiment below once on 25** and correct this table if any
row differs. If a ❌ turns into a ✅, tokens for that property can be added later without breaking the utilities,
because the utilities use literal values.

| Question | Result |
|---|---|
| Size lookup in `-fx-font-size: -fxk-size;` | ❌ **Parse error**: `Expected '<number>' while parsing '-fx-font-size'`. The declaration is dropped. Same for an `em` lookup |
| Size lookup in `-fx-padding`, `-fx-background-radius`, `-fx-border-radius`, `-fx-background-insets` | ❌ Logged as `ClassCastException: Double cannot be cast to Size`. The value is ignored (padding 0, radius 0). Unit-less (`16`) and `px` lookups both fail |
| Size lookup in `-fx-spacing`, `-fx-hgap`, `-fx-vgap`, `-fx-min-width`, `-fx-pref-height` | ✅ Works |
| A whole `dropshadow(...)` as a looked-up value in `-fx-effect` | ✅ Works |
| A token color inside `dropshadow(...)`, inside a lookup or inline | ✅ Works |
| A lookup that points to another size lookup | ❌ Same failure as the property it ends up in |
| `-fx-spacing` / `-fx-hgap` on a node that has no such property (a `Label`) | ✅ Silently ignored, **no warning**. This is what lets one `.gap-N` class work on any pane |
| Literal `-fx-padding` with 2 or 4 values, `-fx-background-radius: 9999px`, `-fx-effect: null`, `-fx-font-weight: 600` | ✅ All parse. A 9999px radius renders as a full pill |

**Consequences (story #18):**

- **Colors and shadows are tokens** (`tokens.css`). A whole shadow is one lookup, so the dark theme can redefine the
  three `-fxk-shadow-*` values.
- **Spacing, radius, font size and weight are not tokens.** Their scales live in `tools/scales.txt`, and
  `tools/GenerateUtilities.java` writes the literal values into `utilities.css`. Component CSS in Phase 2 should use the
  same literal values from that table. If JavaFX 25 turns out to accept size lookups, this can be revisited.

**To re-run on JavaFX 25** (two minutes): temporarily add this to `showcase.css`, run `./run.sh`, then check the
title size and the console for `WARNING: ... CSS Error parsing` or `ClassCastException`. Remove it afterwards.

```css
.root {
    -fxk-test-size:   40px;
    -fxk-test-shadow: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);
}
.showcase-scroll {
    -fx-padding: -fxk-test-size;      /* 11: ClassCastException warning, padding ignored */
    -fx-effect:  -fxk-test-shadow;    /* 11: works */
}
```
