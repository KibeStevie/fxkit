# JavaFX CSS vs web CSS: notes for FXKit

Output of the spike *"How JavaFX CSS differs from web CSS"* (#14).
Reference: the official *JavaFX CSS Reference Guide* (search "JavaFX CSS reference" for your JavaFX version).

**How to read this:** items marked ✅ are well-established JavaFX behavior. Items marked 🧪 must be
confirmed by running the two-minute experiment in section 8, and the result written down. Those
results decide how the spacing, font-size and shadow tokens (#18) are built.

---

## 1. What does not exist (and what to use instead)

| Web CSS habit | In JavaFX CSS | FXKit approach |
|---|---|---|
| `var(--x)` custom properties | ✅ No `var()`. The substitute is the **looked-up color**: `-name: value;` defined once, referenced by name | Tokens are looked-up names on `.root` (`-fxk-blue-500`) |
| `calc()` | ✅ Not supported | Precompute values in the token file; do arithmetic in Java if needed |
| Media queries, `@media`, `prefers-color-scheme` | ✅ Not supported | Themes switch by a **style class on the root** (`.root.dark`), toggled from Java (`ThemeManager`, Sprint 2) |
| `margin` | ✅ Not a property | Pane properties: `-fx-padding`, `-fx-spacing` (HBox/VBox), `-fx-hgap`/`-fx-vgap` (GridPane, FlowPane). Per-child margins are set in Java with `setMargin`. Decision goes in ADR-001 (Sprint 2) |
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
- 🧪 Lookups for **non-color values** (sizes, `dropshadow(...)` effects) are the open question. See section 8.

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
- 🧪 Whether the shadow's color can be a token (`rgba(...)` with a lookup inside) is part of the experiment.

## 6. Selectors and applying styles

- ✅ Style classes (`.card`) via `node.getStyleClass().add("card")`; IDs (`#login`) via `setId(...)`.
  FXKit uses **classes only**, so users can combine them (this is what makes utility classes work).
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

## 8. Experiment to record (2 minutes) 🧪

Temporarily add this to `fxkit-showcase/src/main/resources/dev/fxkit/showcase/showcase.css`, run `./run.sh`,
and look at the title and at the console:

```css
.root {
    -fxk-test-size:   40px;
    -fxk-test-shadow: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 0, 2);
    -fxk-test-shadow2: dropshadow(gaussian, -fxk-primary, 8, 0, 0, 2);
}
.showcase-title {
    -fx-font-size: -fxk-test-size;
    -fx-effect:    -fxk-test-shadow;
}
```

Then change the last line to `-fx-effect: -fxk-test-shadow2;` and run again. Remove the experiment afterwards and fill in:

| Question | Result (works / warning / ignored) |
|---|---|
| Can a size (`40px`) be a looked-up value used in `-fx-font-size`? | _fill in_ |
| Can a whole `dropshadow(...)` be a looked-up value used in `-fx-effect`? | _fill in_ |
| Can a token color be used inside `dropshadow(...)`? | _fill in_ |

**Consequences for #18** (choose after you have the results):

- If sizes work as lookups → spacing, radius and font-size scales become tokens just like colors.
- If they do not → the scales live in the **utility classes** instead (`.p-4 { -fx-padding: 16px; }`), and the token
  file keeps colors only. The scale values are then defined once in the utility generator (see the Sprint 2 stretch spike).
- If a whole shadow works as a lookup → shadow tokens as planned; if not → one class per elevation level.
