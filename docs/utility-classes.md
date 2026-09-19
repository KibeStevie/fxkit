# FXKit utility classes

Tailwind-style style classes for any JavaFX node, with no CSS to write. They live in
`fxkit-core/src/main/resources/dev/fxkit/core/utilities.css`, which is **generated**; see
[Regenerating](#regenerating). Install them with one call:

```java
Scene scene = new Scene(root);
ThemeManager.apply(scene, Theme.LIGHT);   // adds tokens.css + utilities.css, picks the theme
```

## Using them

**Java**

```java
Label title = new Label("Dashboard");
title.getStyleClass().addAll("text-2xl", "font-bold", "text-body");

VBox card = new VBox(title);
card.getStyleClass().addAll("bg-surface", "border", "rounded-lg", "p-6", "gap-3", "shadow-md");
```

**FXML.** Separate the classes with **commas**, not spaces. With spaces, FXML creates *one* class with spaces in
its name and nothing is styled (tested).

```xml
<VBox styleClass="bg-surface, border, rounded-lg, p-6, gap-3, shadow-md"> ... </VBox>
```

## Two kinds of color classes

| Kind | Examples | Follows the theme? | Use it for |
|---|---|---|---|
| **Palette** | `.bg-blue-500`, `.text-gray-700`, `.border-red-300` | **No.** Same color in light and dark | Things that must keep one color (a brand badge, a chart series) |
| **Semantic** | `.bg-surface`, `.text-muted`, `.border-default`, `.bg-primary` | **Yes** | Almost everything else. Prefer these |

If you use `.bg-white` on a card, it stays white in the dark theme. Use `.bg-surface` and it turns dark.

## Class reference

`{scale}` is one of `gray blue red green yellow`; `{step}` is `50 100 200 300 400 500 600 700 800 900`.
`white` and `black` also exist. `{N}` is a step from the **space** scale below.

### Color

| Class | Sets | Notes |
|---|---|---|
| `.bg-{scale}-{step}`, `.bg-white`, `.bg-black`, `.bg-transparent` | `-fx-background-color` | Palette |
| `.bg-{semantic}` | `-fx-background-color` | Every semantic token: `background surface surface-alt text text-muted border border-strong focus-ring primary primary-hover primary-pressed on-primary danger danger-hover on-danger success success-hover on-success warning warning-hover on-warning` |
| `.text-{scale}-{step}`, `.text-white`, `.text-black` | `-fx-text-fill` and `-fx-fill` | Palette. `-fx-fill` colors `Text` nodes; `-fx-text-fill` colors labels and controls |
| `.text-body`, `.text-muted`, `.text-primary`, `.text-danger`, `.text-success`, `.text-warning`, `.text-on-primary`, `.text-on-danger`, `.text-on-success`, `.text-on-warning` | same | Semantic |
| `.border-{scale}-{step}`, `.border-white`, `.border-black`, `.border-transparent` | `-fx-border-color` | Palette |
| `.border-default`, `.border-strong`, `.border-focus`, `.border-primary`, `.border-danger`, `.border-success`, `.border-warning` | `-fx-border-color` | Semantic |

### Border width, radius, shadow

| Class | Result |
|---|---|
| `.border`, `.border-2`, `.border-4` | 1 / 2 / 4 px border in the default border color (add a `.border-*` color class to change it) |
| `.border-0` | no border |
| `.rounded-none .rounded-sm .rounded .rounded-md .rounded-lg .rounded-xl .rounded-2xl .rounded-full` | radius 0 / 2 / 4 / 6 / 8 / 12 / 16 px / fully round. Sets background **and** border radius |
| `.shadow-sm .shadow-md .shadow-lg` | elevation shadows (stronger in the dark theme) |
| `.shadow-none` | removes the effect |

`.border` needs no color class: it already sets the default border color. Color classes are written *after* it in
the file, so `class="border border-blue-500"` gives a blue border.

### Spacing (scale `space`)

| N | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 8 | 10 | 12 | 16 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| px | 0 | 4 | 8 | 12 | 16 | 20 | 24 | 32 | 40 | 48 | 64 |

| Class | Sets | Applies to |
|---|---|---|
| `.p-N` | padding on all sides | any Region or control |
| `.px-N`, `.py-N` | left+right / top+bottom padding | same |
| `.pt-N`, `.pr-N`, `.pb-N`, `.pl-N` | one side | same |
| `.gap-N` | `-fx-spacing`, `-fx-hgap`, `-fx-vgap` | the **parent** layout pane (HBox, VBox, FlowPane, GridPane, TilePane) |
| `.gap-x-N`, `.gap-y-N` | `-fx-hgap` / `-fx-vgap` only | FlowPane, GridPane, TilePane |

> **Only one padding class per node works.** All of them set the same `-fx-padding` property, so with
> `class="px-4 py-2"` the later rule replaces the earlier one. Use `.p-N`, or `setPadding(new Insets(...))`.
>
> **There is no `m-*` (margin).** JavaFX CSS has no margin property. Use `.gap-N` on the parent, padding on the
> container, or FXML/Java margins. The reasoning is in [ADR-001](adr/ADR-001-margin-and-gap.md).

### Typography

| Class | Result |
|---|---|
| `.text-xs .text-sm .text-base .text-lg .text-xl .text-2xl .text-3xl` | 11 / 12 / 14 / 16 / 18 / 24 / 30 px |
| `.font-normal .font-medium .font-semibold .font-bold` | weight normal / 500 / 600 / bold |

`text-*` is used for both color (`.text-blue-500`) and size (`.text-lg`). The names never collide.
`medium` and `semibold` only look different if the installed font has those weights; otherwise JavaFX shows the
nearest one.

## Naming convention

- Follows Tailwind where JavaFX allows it: `bg-`, `text-`, `border-`, `rounded-`, `p-`, `gap-`, `shadow-`, `font-`.
- Palette classes end in `-{scale}-{step}`; semantic classes use a role name (`surface`, `muted`, `primary`).
- Lowercase, hyphen-separated, no prefix. If a clash with your own classes ever appears, that is a reason to add
  a prefix later, before 1.0.

## Cascade order (why some combinations do not work)

All utilities are single-class selectors, so they have equal specificity. When two of them set the same property,
**the one written later in `utilities.css` wins**, not the one later in your `styleClass` list. Combine only
classes that set different properties (`bg-*` with `p-*` with `rounded-*` is fine).

Any rule in a stylesheet you add **after** ThemeManager's (for example your own `app.css`) overrides utilities.
Inline `setStyle(...)` overrides everything.

## Regenerating

`utilities.css` is produced by `tools/GenerateUtilities.java` from two inputs:

| Input | Contains |
|---|---|
| `tokens.css` | color scales, semantic colors and shadow tokens (discovered automatically) |
| `tools/scales.txt` | spacing, radius, font-size and font-weight scales |

```bash
java tools/GenerateUtilities.java            # rewrite utilities.css (run from the repository root)
java tools/GenerateUtilities.java --check    # fail if utilities.css is out of date
```

Add a color scale to `tokens.css` (say `purple-50 ... purple-900`), run the command, and every `bg-`, `text-` and
`border-` class for it appears. Then commit both files.
