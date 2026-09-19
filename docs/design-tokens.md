# FXKit design tokens

Source of truth: `fxkit-core/src/main/resources/dev/fxkit/core/tokens.css`.
Install everything with `ThemeManager.apply(scene, Theme.LIGHT)` (or `Theme.DARK`). It adds `tokens.css` and
`utilities.css` for you. To add `tokens.css` by hand: `scene.getStylesheets().add(FxKit.tokensStylesheet())`.

## What is a token, and what is not

| Kind | Where it lives | Works as a looked-up token? |
|---|---|---|
| Color scales, semantic colors | `tokens.css` | Yes |
| Shadows (whole `dropshadow(...)` effect) | `tokens.css` | Yes |
| Spacing, radius, font size, font weight | `tools/scales.txt` | **No.** JavaFX CSS cannot use a looked-up value in `-fx-padding`, `-fx-background-radius` or `-fx-font-size` (see `css-notes.md`, section 8). These scales are written straight into the utility classes instead |

## Naming

| Kind | Pattern | Example | Use it in |
|---|---|---|---|
| Raw color | `-fxk-<scale>-<step>` | `-fxk-blue-500` | **Only** inside `tokens.css` (to define semantic tokens) and palette utilities |
| Semantic color | `-fxk-<role>` | `-fxk-primary` | Components, utilities, your own CSS |
| Shadow | `-fxk-shadow-<size>` | `-fxk-shadow-md` | `-fx-effect: -fxk-shadow-md;` |

Scales: `gray`, `blue`, `red`, `green`, `yellow`. Steps: `50, 100, 200, 300, 400, 500, 600, 700, 800, 900`
(50 = lightest, 900 = darkest). Plus `-fxk-white` and `-fxk-black`.
Palette values come from Tailwind CSS v3 (MIT).

**Rule:** components use **semantic** and **shadow** tokens, never the raw scales. That is what lets the dark
theme restyle everything by redefining those tokens only.

## Semantic tokens

`ThemeManager` switches between the two columns by adding or removing the style class `dark` on the scene root.

| Token | Light | Dark | Purpose |
|---|---|---|---|
| `-fxk-background` | `gray-50` `#f9fafb` | `gray-900` `#111827` | App / window background |
| `-fxk-surface` | `white` `#ffffff` | `gray-800` `#1f2937` | Cards, panels, inputs |
| `-fxk-surface-alt` | `gray-100` `#f3f4f6` | `gray-700` `#374151` | Subtle contrast areas, table stripes |
| `-fxk-text` | `gray-900` `#111827` | `gray-100` `#f3f4f6` | Body text |
| `-fxk-text-muted` | `gray-500` `#6b7280` | `gray-400` `#9ca3af` | Secondary text, placeholders |
| `-fxk-border` | `gray-200` `#e5e7eb` | `gray-700` `#374151` | Default borders, dividers |
| `-fxk-border-strong` | `gray-300` `#d1d5db` | `gray-600` `#4b5563` | Input borders, emphasis |
| `-fxk-focus-ring` | `blue-500` `#3b82f6` | `blue-400` `#60a5fa` | Keyboard focus outline |
| `-fxk-primary` | `blue-600` `#2563eb` | `blue-400` `#60a5fa` | Main action / brand |
| `-fxk-primary-hover` | `blue-700` `#1d4ed8` | `blue-300` `#93c5fd` | Primary, hovered |
| `-fxk-primary-pressed` | `blue-800` `#1e40af` | `blue-500` `#3b82f6` | Primary, pressed |
| `-fxk-on-primary` | `white` | `gray-900` | Text/icon on a primary background |
| `-fxk-danger` | `red-600` `#dc2626` | `red-400` `#f87171` | Destructive actions, errors |
| `-fxk-danger-hover` | `red-700` `#b91c1c` | `red-300` `#fca5a5` | Danger, hovered |
| `-fxk-on-danger` | `white` | `gray-900` | Text on danger |
| `-fxk-success` | `green-700` `#15803d` | `green-400` `#4ade80` | Success states |
| `-fxk-success-hover` | `green-800` `#166534` | `green-300` `#86efac` | Success, hovered |
| `-fxk-on-success` | `white` | `gray-900` | Text on success |
| `-fxk-warning` | `yellow-500` `#eab308` | `yellow-400` `#facc15` | Warnings |
| `-fxk-warning-hover` | `yellow-600` `#ca8a04` | `yellow-300` `#fde047` | Warning, hovered |
| `-fxk-on-warning` | `gray-900` | `gray-900` | Text on warning (dark text: yellow is too light for white) |

### Why the dark theme uses bright fills and dark "on" text

On dark surfaces, the 400-level colors read well both as fills and as text or borders. White text on the 500-level
fills would only reach about 3.7 : 1, so the dark theme pairs bright fills with `gray-900` text instead.
The showcase's palette page shows every pair in both themes.

### Contrast (WCAG ratios)

| Pair | Light | Dark |
|---|---|---|
| `text` on `surface` | 17.7 : 1 | 13.3 : 1 |
| `text` on `background` | | 16.1 : 1 |
| `text-muted` on `surface` | 4.8 : 1 | 5.8 : 1 |
| `on-primary` on `primary` | 5.2 : 1 | 7.0 : 1 |
| `on-danger` on `danger` | 4.8 : 1 | 6.4 : 1 |
| `on-success` on `success` | 5.0 : 1 | 10.2 : 1 |
| `on-warning` on `warning` | 9.3 : 1 | 11.6 : 1 |

All text pairs meet the 4.5 : 1 target in both themes. `success` uses `green-700` in the light theme because white on
`green-600` is only 3.3 : 1.

**Known gap:** `border` and `border-strong` are decorative dividers (about 1.4 to 1.9 : 1 against the surface).
Input boundaries should reach 3 : 1 for accessibility, so this is revisited with the input components in Phase 4.

## Elevation (shadows)

| Token | Light | Dark |
|---|---|---|
| `-fxk-shadow-sm` | `dropshadow(gaussian, rgba(0,0,0,0.10), 3, 0, 0, 1)` | alpha 0.40 |
| `-fxk-shadow-md` | `dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 3)` | alpha 0.45 |
| `-fxk-shadow-lg` | `dropshadow(gaussian, rgba(0,0,0,0.16), 16, 0, 0, 6)` | alpha 0.50 |

Use as `-fx-effect: -fxk-shadow-md;` or the class `.shadow-md`. A node has only **one** effect.

## Scales that are not tokens

Defined in `tools/scales.txt` and turned into utility classes by `tools/GenerateUtilities.java`.

| Scale | Steps (name = value in px) |
|---|---|
| Spacing (`p-`, `gap-`) | `0`=0, `1`=4, `2`=8, `3`=12, `4`=16, `5`=20, `6`=24, `8`=32, `10`=40, `12`=48, `16`=64 |
| Radius (`rounded-`) | `none`=0, `sm`=2, `base`=4, `md`=6, `lg`=8, `xl`=12, `2xl`=16, `full`=9999 (a pill) |
| Font size (`text-`) | `xs`=11, `sm`=12, `base`=14, `lg`=16, `xl`=18, `2xl`=24, `3xl`=30 |
| Font weight (`font-`) | `normal`, `medium`=500, `semibold`=600, `bold` |

The base unit is 4 px. The font scale is tuned for desktop (14 px base) rather than the web's 16 px.
Components in Phase 2 should use these same values so that a `md` button, a `p-4` card and a `rounded-md` input line up.
See [utility-classes.md](utility-classes.md).
