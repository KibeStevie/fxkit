# FXKit: Project Overview

> **Working name:** FXKit (placeholder, rename freely)
> **Status:** Planning / Phase 0
> **Stack:** Java 25 · JavaFX 25 · Maven · VS Code

---

## 1. The One-Liner

**FXKit is a Tailwind-inspired UI component library for JavaFX that lets developers build desktop applications quickly, without rebuilding buttons, forms, cards and tables on every project.**

## 2. The Elevator Pitch

If you have built more than one JavaFX app, you have built the same things more than once: a styled button, a form with validation, a card, a data table, a dialog. Every new project starts with days of repeated UI work before the real application begins.

Web developers solved this years ago. Libraries like Flowbite React, paired with Tailwind CSS, give them ready-made components and a shared design language, so they assemble screens instead of building primitives.

FXKit brings that same experience to JavaFX. It provides a consistent design system, a set of ready-to-use components, and a familiar utility-class styling approach, so a JavaFX developer can go from an empty project to a good-looking working screen in minutes.

---

## 3. The Problem

| Pain point | What it looks like today |
|---|---|
| **Repeated work** | Every project re-implements buttons, inputs, cards, tables and dialogs from scratch. |
| **Inconsistent look** | Default JavaFX styling looks dated, and each project invents its own styles. |
| **High setup cost** | Getting a new project to "presentable" takes days of CSS and layout work. |
| **Scattered tooling** | Existing libraries each cover one slice (themes, or a few controls, or forms) and rarely share one design language. |
| **Steep CSS learning curve** | JavaFX CSS is powerful but unfamiliar, and there is no shared vocabulary such as spacing scales or color scales. |

## 4. The Vision

A JavaFX developer should be able to:

1. Add one dependency to their `pom.xml`.
2. Apply one stylesheet.
3. Compose polished screens from ready-made components, styled with a familiar set of utility classes.

The library should feel the way Tailwind and Flowbite feel on the web: **predictable, consistent, customizable, and fast to work with.**

## 5. Goals

### Primary goals

- **Speed:** cut the time to start a new JavaFX app and reach a polished UI.
- **Consistency:** one design system (colors, spacing, radius, typography) shared by every component.
- **Familiarity:** a Tailwind-like styling vocabulary, so developers who know Tailwind feel at home.
- **Customizability:** override colors, sizes and variants without forking the library.
- **Usability from both worlds:** components work in pure Java code *and* in FXML / Scene Builder.

### Secondary goals

- Light and dark themes out of the box.
- Good documentation with a live showcase app.
- Project scaffolding (Maven archetype or CLI) for one-command project creation.
- A codebase clean enough that contributors can add components easily.

### Personal learning goal

This project is also a learning path. It is built **small and scaled slowly** so the author (and future contributors) can learn Java, JavaFX, CSS styling, the properties/event system, Maven and module packaging along the way.

## 6. Non-Goals (for now)

Being explicit about what we are *not* doing keeps the scope manageable.

- Not a replacement for JavaFX itself, since we build on top of it.
- Not a full application framework (no dependency injection, routing or state management at the start).
- Not mobile or web targeting (desktop first).
- Not reinventing what already works well. For example, we will use Ikonli for icons instead of building our own icon system.
- Not trying to ship every component on day one.

## 7. Who It Is For

- **JavaFX developers** starting new desktop projects who are tired of repeating UI work.
- **Developers coming from web** (React, Tailwind, Bootstrap) who want a familiar mental model in JavaFX.
- **Students and beginners** who want good-looking apps without mastering JavaFX CSS first.
- **Small teams and solo developers** building internal tools, dashboards and business apps.

---

## 8. Core Principles

1. **Small first, then scale.** Ship a few excellent components before building many average ones.
2. **One design system.** Every component reads from the same tokens, so nothing looks out of place.
3. **Props over hacks.** Customization happens through clear, typed properties, not by editing CSS internals.
4. **Standard JavaFX patterns.** Use JavaFX properties, events and style classes, so components behave the way JavaFX developers expect.
5. **Works everywhere.** Every component is usable from Java code and from FXML.
6. **Document as we build.** Each component gets a showcase page, which serves as documentation and a manual test.

---

## 9. The Design System

### 9.1 Color scales (Tailwind-style)

Every color has a scale of shades from **50 (lightest) to 900 (darkest)**:

```
blue-50   blue-100  blue-200  blue-300  blue-400
blue-500  blue-600  blue-700  blue-800  blue-900
```

The same scale exists for the other palette colors (gray, red, green, yellow, and so on). Components pick shades from these scales, so changing the palette changes the whole app.

JavaFX CSS does not support CSS variables (`var()`), so the tokens are defined as **looked-up colors** on `.root`:

```css
.root {
    -fxk-blue-50:  #eff6ff;
    -fxk-blue-500: #3b82f6;
    -fxk-blue-600: #2563eb;
    -fxk-blue-900: #1e3a8a;
    /* ...remaining shades and colors */
}
```

### 9.2 Utility classes

Developers style nodes with short, predictable style classes:

```css
.bg-blue-500   { -fx-background-color: -fxk-blue-500; }
.text-gray-700 { -fx-text-fill: -fxk-gray-700; }
.border-red-300 { -fx-border-color: -fxk-red-300; }
.rounded-lg    { -fx-background-radius: 8; }
.p-4           { -fx-padding: 16; }
```

```java
Label title = new Label("Dashboard");
title.getStyleClass().addAll("text-gray-900", "text-2xl", "font-bold");
```

The full set of color-by-shade classes is large, so it will eventually be **generated by a script** from a single palette definition rather than written by hand.

### 9.3 Other tokens

- **Spacing scale** (4, 8, 12, 16, 24, and so on)
- **Border radius** (sm, md, lg, full)
- **Typography** (sizes and weights)
- **Shadows**
- **Themes:** light and dark, switched by toggling a style class on the root node

> **A note on limits:** JavaFX CSS is a subset of web CSS. There is no `var()`, no `calc()`, no media queries and no flex/grid. Our approach works *with* these limits (looked-up colors, style classes, pseudo-classes such as `:hover`, `:focused` and `:disabled`) instead of pretending they do not exist.

---

## 10. Props and Event Handlers

Components expose their configuration as **JavaFX properties**, so they can be set in code, in FXML, or bound to other values.

```java
FxButton save = new FxButton("Save");
save.setVariant(Variant.PRIMARY);   // PRIMARY, SECONDARY, DANGER, GHOST
save.setSize(Size.LG);              // SM, MD, LG
save.setDisabled(false);
save.setOnAction(e -> saveForm());  // standard JavaFX event handler
```

The same component in FXML:

```xml
<FxButton text="Save" variant="PRIMARY" size="LG" onAction="#handleSave"/>
```

**Planned conventions:**

- Every visual option (variant, size, state) is a typed property with a getter, setter and `...Property()` method.
- Standard events reuse JavaFX's own (`onAction`, `onMouseClicked`).
- Composite components add their own events where needed (for example `onRowSelected`, `onPageChange`, `onValidate`, `onClose`).
- Properties are bindable, so UI state can follow application state.

---

## 11. Architecture

The library is built in layers. Each layer only depends on the ones below it.

```
┌────────────────────────────────────────────┐
│ 5. Tooling      Maven archetype / CLI      │
├────────────────────────────────────────────┤
│ 4. Templates    login, dashboard, CRUD     │
├────────────────────────────────────────────┤
│ 3. Composites   DataTable, FormField,      │
│                 Modal, Toast, Sidebar      │
├────────────────────────────────────────────┤
│ 2. Core         Button, Card, Input,       │
│                 Badge, Alert               │
├────────────────────────────────────────────┤
│ 1. Foundation   Tokens, utility CSS,       │
│                 light/dark themes          │
└────────────────────────────────────────────┘
```

### Planned project structure

```
fxkit/
├── fxkit-core/          # the library (components + CSS)
│   └── src/main/
│       ├── java/        # components, module-info.java
│       └── resources/   # css (tokens, utilities, components)
├── fxkit-showcase/      # demo app: one page per component
└── pom.xml              # parent Maven build
```

### Technical baseline

| Area | Decision |
|---|---|
| Language | Java 25 |
| UI toolkit | JavaFX 25 |
| Build | Maven (multi-module) |
| IDE | VS Code |
| Modules | JPMS (`module-info.java`), with packages opened for FXML |
| Icons | Ikonli |
| Testing | JUnit and TestFX (as components stabilize) |
| Distribution | Maven artifact (Maven Central later) |

---

## 12. Roadmap

We go in small steps. Each phase ends with something that runs and can be shown.

| Phase | Goal | You will learn |
|---|---|---|
| **0. Setup** | Maven multi-module project, VS Code configured, "Hello JavaFX" running | Maven, JavaFX basics, module setup |
| **1. Foundation** | Color tokens (50 to 900), spacing, radius, light/dark theme, first utility classes | JavaFX CSS, looked-up colors, style classes |
| **2. First components** | `Button` and `Card`, with variants and sizes | Properties, style classes, custom controls |
| **3. Showcase app** | Demo app with one page per component | Layouts, app structure |
| **4. Inputs** | `Input`, `Checkbox`, `Select`, `Badge`, `Alert` | Events, FXML support |
| **5. Composites** | `FormField` with validation, `Modal`, `Toast`, `DataTable` | Generics, bindings, TableView |
| **6. App shell and templates** | Sidebar and navbar layout, login and dashboard screens | Composition, FXML |
| **7. Tooling and docs** | Maven archetype or CLI, documentation site, first public release | Packaging, publishing |

**Rule for every component:** it is only "done" when it has (1) a Java API, (2) FXML support, (3) light and dark styling, and (4) a showcase page.

---

## 13. Success Criteria

The project is on track if:

- A developer can add the dependency and get a styled button, card and form in **under 10 minutes**.
- Changing one color token restyles the whole showcase app.
- Every component can be used from both Java code and FXML.
- A new contributor can add a component by following an existing one as a pattern.
- The author understands every line of the code, since learning is part of the goal.

## 14. Risks and Open Questions

| Item | Notes |
|---|---|
| **JavaFX CSS limitations** | No variables, `calc()` or media queries. We work within the looked-up color model. |
| **Utility class volume** | Colors × shades × properties is thousands of classes, so we need a generator script. |
| **Scene Builder support** | Custom controls need extra setup (module and FXML registration) to appear in the palette. |
| **Scope creep** | Keep to the roadmap and finish components fully before adding new ones. |
| **Distribution style** | Open question: dependency only, or also "copy the component into your project" (shadcn-style)? |
| **Naming and licensing** | Choose the final name, package name and license (for example MIT or Apache 2.0) before the first public release. |

---

## 15. Summary

FXKit aims to do for JavaFX what Tailwind and Flowbite did for the web: replace repeated, low-value UI work with a consistent, customizable set of building blocks, so developers spend their time on the application itself. It starts small (tokens, a button, a card), grows step by step, and is built as much to teach its author as to help its users.
