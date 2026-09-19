# FXKit design tokens

Source of truth: `fxkit-core/src/main/resources/dev/fxkit/core/tokens.css`.
Load it with `scene.getStylesheets().add(FxKit.tokensStylesheet())`.

## Naming

| Kind | Pattern | Example | Use it in |
|---|---|---|---|
| Raw color | `-fxk-<scale>-<step>` | `-fxk-blue-500` | **Only** inside `tokens.css` (to define semantic tokens) |
| Semantic color | `-fxk-<role>` | `-fxk-primary` | Components, utilities, your own CSS |

Scales: `gray`, `blue`, `red`, `green`, `yellow`. Steps: `50, 100, 200, 300, 400, 500, 600, 700, 800, 900`
(50 = lightest, 900 = darkest). Plus `-fxk-white` and `-fxk-black`.
Palette values come from Tailwind CSS v3 (MIT).

**Rule:** components use **semantic** tokens, never the raw scales. That is what lets the dark theme
(Sprint 2) change the whole look by redefining semantic tokens only.

## Semantic tokens (light theme)

| Token | Points to | Hex | Purpose |
|---|---|---|---|
| `-fxk-background` | `gray-50` | `#f9fafb` | App / window background |
| `-fxk-surface` | `white` | `#ffffff` | Cards, panels, inputs |
| `-fxk-surface-alt` | `gray-100` | `#f3f4f6` | Subtle contrast areas, table stripes |
| `-fxk-text` | `gray-900` | `#111827` | Body text |
| `-fxk-text-muted` | `gray-500` | `#6b7280` | Secondary text, placeholders |
| `-fxk-border` | `gray-200` | `#e5e7eb` | Default borders, dividers |
| `-fxk-border-strong` | `gray-300` | `#d1d5db` | Input borders, emphasis |
| `-fxk-focus-ring` | `blue-500` | `#3b82f6` | Keyboard focus outline |
| `-fxk-primary` | `blue-600` | `#2563eb` | Main action / brand |
| `-fxk-primary-hover` | `blue-700` | `#1d4ed8` | Primary, hovered |
| `-fxk-primary-pressed` | `blue-800` | `#1e40af` | Primary, pressed |
| `-fxk-on-primary` | `white` | `#ffffff` | Text/icon on a primary background |
| `-fxk-danger` | `red-600` | `#dc2626` | Destructive actions, errors |
| `-fxk-danger-hover` | `red-700` | `#b91c1c` | Danger, hovered |
| `-fxk-on-danger` | `white` | `#ffffff` | Text on danger |
| `-fxk-success` | `green-700` | `#15803d` | Success states |
| `-fxk-success-hover` | `green-800` | `#166534` | Success, hovered |
| `-fxk-on-success` | `white` | `#ffffff` | Text on success |
| `-fxk-warning` | `yellow-500` | `#eab308` | Warnings |
| `-fxk-warning-hover` | `yellow-600` | `#ca8a04` | Warning, hovered |
| `-fxk-on-warning` | `gray-900` | `#111827` | Text on warning (dark text: yellow is too light for white) |

### Contrast (WCAG ratios, light theme)

| Pair | Ratio |
|---|---|
| `text` on `surface` | 17.7 : 1 |
| `text-muted` on `surface` | 4.8 : 1 |
| `on-primary` on `primary` | 5.2 : 1 |
| `on-danger` on `danger` | 4.8 : 1 |
| `on-success` on `success` | 5.0 : 1 |
| `on-warning` on `warning` | 9.3 : 1 |

All pairs meet the 4.5 : 1 target for body text. `success` uses `green-700` rather than `green-600`
because white on `green-600` is only 3.3 : 1.

## Not here yet

Spacing, radius, font-size and shadow scales come with story #18, after the CSS spike shows which of them
JavaFX can express as tokens (see `css-notes.md`, section 8).
