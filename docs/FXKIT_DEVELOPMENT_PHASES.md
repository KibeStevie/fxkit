# FXKit: Development Phases & Scrum Plan

> Companion to `PROJECT_OVERVIEW.md`.
> **Stack:** Java 25 · JavaFX 25 · Maven · VS Code
> **Method:** Agile / Scrum
> **Working name:** FXKit (placeholder)

This document is the roadmap for the whole project: every phase, what happens in it, how it maps to Scrum sprints, and what "done" means. It is a **living document**. Re-estimate and adjust it during retrospectives, because the plan is expected to change as you learn.

---

## Table of Contents

1. [Product Goal](#1-product-goal)
2. [Phases at a Glance](#2-phases-at-a-glance)
3. [How Scrum Works on This Project](#3-how-scrum-works-on-this-project)
4. [Definitions of Ready and Done](#4-definitions-of-ready-and-done)
5. [Phase 0: Setup](#phase-0-setup-sprint-0)
6. [Phase 1: Design Foundation](#phase-1-design-foundation-sprints-1-2)
7. [Phase 2: First Components (Button & Card)](#phase-2-first-components-sprints-3-4)
8. [Phase 3: Showcase App](#phase-3-showcase-app-sprint-5)
9. [Phase 4: Inputs & Feedback Components](#phase-4-inputs--feedback-components-sprints-6-8)
10. [Phase 5: Composite Components](#phase-5-composite-components-sprints-9-13)
11. [Phase 6: App Shell & Templates](#phase-6-app-shell--templates-sprints-14-16)
12. [Phase 7: Quality, Docs, Tooling & First Release](#phase-7-quality-docs-tooling--first-release-sprints-17-19)
13. [Phase 8: Growth & Maintenance](#phase-8-growth--maintenance-ongoing)
14. [Cross-Phase Practices](#14-cross-phase-practices)
15. [Templates](#15-templates)

---

## 1. Product Goal

In Scrum, the Product Goal is the long-term objective that every sprint moves toward.

> **A JavaFX developer can add one dependency, apply one stylesheet, and build a polished desktop app screen from ready-made components in under 10 minutes.**

Every backlog item should trace back to this goal. If it doesn't, question whether it belongs.

---

## 2. Phases at a Glance

Sprint numbers assume **2-week sprints**. With 1-week sprints, halve the calendar time. The durations are rough estimates for a solo, part-time developer who is also learning, so recalibrate after Sprint 3.

| Phase | Name | Sprints | ~Duration | Milestone |
|---|---|---|---|---|
| 0 | Setup | 0 | 2 weeks | "Hello JavaFX" runs, backlog exists |
| 1 | Design Foundation | 1–2 | 4 weeks | Tokens, utility CSS, light/dark theme |
| 2 | First Components | 3–4 | 4 weeks | **M1 "First Light":** `Button` + `Card` usable in Java and FXML |
| 3 | Showcase App | 5 | 2 weeks | **M2 "Showcase":** demo app with pages per component |
| 4 | Inputs & Feedback | 6–8 | 6 weeks | **`v0.1.0-alpha`:** core component set |
| 5 | Composite Components | 9–13 | 10 weeks | `FormField`, `Modal`, `Toast`, `DataTable` |
| 6 | App Shell & Templates | 14–16 | 6 weeks | **`v0.1.0-beta`:** full screens from templates |
| 7 | Quality, Docs, Tooling, Release | 17–19 | 6 weeks | **`v0.1.0`:** first public release |
| 8 | Growth & Maintenance | 20+ | Ongoing | Regular minor releases |

**Total to first public release:** about 19 sprints (roughly 9 months at 2-week sprints).

```
Phase 0 ─► 1 ─► 2 ─► 3 ─► 4 ─► 5 ─► 6 ─► 7 ─► 8
Setup    Design  First  Show-  Inputs Compo- Shell  Release Growth
         Found.  Comps  case          sites  & Tmpl
```

---

## 3. How Scrum Works on This Project

### 3.1 Roles (one person, three hats)

| Role | Responsibility | Solo-dev tip |
|---|---|---|
| **Product Owner** | Owns the Product Backlog and decides priority | Prioritize by value to the Product Goal, not by what's fun |
| **Scrum Master** | Keeps the process healthy and removes blockers | Run the retrospective honestly |
| **Developer** | Builds the increment | Only commit to what you can finish |

Find a **peer, mentor or community** (a Discord, a Java user group, a friend) to act as a stakeholder at Sprint Review. Outside eyes catch what you won't see yourself.

### 3.2 Sprint cadence

**Recommended sprint length:** 2 weeks (use 1 week if you work on this full-time).

| Event | When | Suggested time-box | Output |
|---|---|---|---|
| **Sprint Planning** | Day 1 | 1 to 2 hrs | Sprint Goal + Sprint Backlog |
| **Daily Scrum** | Every working day | 10 min (written log is fine solo) | What I did, what's next, what's blocking me |
| **Sprint Review** | Last day | 1 hr | Demo the working increment; update the backlog |
| **Sprint Retrospective** | After the review | 45 min | 1 to 3 concrete improvements |

### 3.3 Artifacts and commitments

| Artifact | Commitment | Where it lives |
|---|---|---|
| **Product Backlog** | Product Goal | GitHub Projects board (or Trello/Jira) |
| **Sprint Backlog** | Sprint Goal | The "Sprint N" column/iteration on the board |
| **Increment** | Definition of Done | The `main` branch (always working) |

### 3.4 Backlog structure

```
Epic (a component or capability, e.g. "Button")
 └── User Story (a slice of value)
      └── Task (a technical step, ≤ 1 day)
```

- **User story format:** *As a `<JavaFX developer>`, I want `<capability>`, so that `<benefit>`.*
- **Estimation:** story points on the Fibonacci scale (1, 2, 3, 5, 8). **Anything above 8 must be split.**
- **Velocity:** don't trust it until Sprint 3 or 4. Start with a conservative commitment and adjust.
- **Spikes:** time-boxed research stories (for example, "Spike: can Scene Builder load custom controls?"). Output is knowledge, not code. Time-box them to 1 day.

### 3.5 Sprint 0

Scrum doesn't formally define a "Sprint 0", but a setup sprint is common practice. Here it covers environment, repo and skeleton so that Sprint 1 starts building immediately.

---

## 4. Definitions of Ready and Done

### 4.1 Definition of Ready (a story can enter a sprint when...)

- [ ] It has a clear user story and acceptance criteria.
- [ ] It's small enough to finish inside one sprint (≤ 8 points).
- [ ] Dependencies are known and unblocked.
- [ ] You understand it well enough to start. Otherwise, write a spike first.

### 4.2 Definition of Done (a story is done when...)

**All stories**
- [ ] Acceptance criteria met.
- [ ] Code compiles with `mvn clean verify` and all tests pass.
- [ ] Code reviewed (self-review with a checklist, or peer review via pull request).
- [ ] Merged to `main`.

**Additionally, for every component**
- [ ] **Java API:** typed properties (getter, setter, `xxxProperty()`).
- [ ] **FXML support:** works from FXML (no-arg constructor, properties settable via attributes).
- [ ] **Styling:** uses design tokens only (no hard-coded colors), light and dark themes verified.
- [ ] **States:** hover, focused, pressed and disabled handled where they apply.
- [ ] **Events:** standard JavaFX events, plus custom ones where needed.
- [ ] **Showcase page** added or updated.
- [ ] **Tests:** unit tests for property-to-style logic; UI tests where practical.
- [ ] **Javadoc** on the public API.

---

# Phase Details

## Phase 0: Setup (Sprint 0)

**Goal:** a working development environment, a repository, and a "Hello JavaFX" window running from Maven in VS Code.

**Sprint Goal (suggested):** *"I can build and run a JavaFX window from the command line and from VS Code."*

### Epics and stories

**Epic: Environment**
- [ ] Install JDK 25; set `JAVA_HOME`; verify `java -version` and `mvn -v`. `[2]`
- [ ] Set up VS Code: *Extension Pack for Java*, *Maven for Java*, XML and CSS support. `[2]`
- [ ] Install Scene Builder (needed later for FXML work). `[1]`

**Epic: Repository & build**
- [ ] Create a Git repo with `.gitignore`, `README.md`, `LICENSE` (pick a license now, e.g. MIT or Apache 2.0). `[2]`
- [ ] Create the parent `pom.xml` with properties for Java and JavaFX versions (25). `[3]`
- [ ] Create modules `fxkit-core` (library) and `fxkit-showcase` (demo app). `[3]`
- [ ] Add JavaFX dependencies (`javafx-controls`, `javafx-fxml`) and the `javafx-maven-plugin`. `[3]`

**Epic: Skeleton**
- [ ] Write a minimal `Application` subclass that opens a window (`mvn javafx:run` works). `[2]`
- [ ] Add `module-info.java` to both modules (export packages, open them for FXML reflection). `[3]`
- [ ] Set up the project board with Product Backlog columns and labels (`epic`, `story`, `spike`, `bug`). `[2]`
- [ ] Write the initial Product Backlog from the phases in this document. `[3]`

### Learning objectives
Maven lifecycle and multi-module builds · the JavaFX application lifecycle (`start()`, `Stage`, `Scene`) · JPMS basics · Git workflow.

### Exit criteria
- `mvn clean install` succeeds from the root.
- The showcase app opens a window from VS Code and from the terminal.
- The Product Backlog exists and the Sprint 1 backlog is ready.

---

## Phase 1: Design Foundation (Sprints 1–2)

**Goal:** the design system that every component will build on: color scales, spacing, radius, typography, utility classes and a light/dark theme.

### Sprint 1: Tokens
**Sprint Goal:** *"Every color, spacing and radius value lives in one token file."*

- [ ] **Spike:** review how JavaFX CSS differs from web CSS (no `var()`, no `calc()`, no media queries; looked-up colors, pseudo-classes). Write your findings down in `docs/css-notes.md`. `[3]`
- [ ] Define **color scales 50 to 900** for: `gray`, `blue`, `red`, `green`, `yellow` as looked-up colors on `.root` (e.g. `-fxk-blue-500`). `[5]`
- [ ] Define **semantic tokens** that map to scales: `-fxk-primary`, `-fxk-surface`, `-fxk-text`, `-fxk-border`, `-fxk-danger`, and so on. `[3]`
- [ ] Define spacing, radius, font-size and shadow scales. `[3]`
- [ ] Create `tokens.css` and load it in the showcase app. `[2]`

### Sprint 2: Utilities & themes
**Sprint Goal:** *"I can style any node with utility classes and switch between light and dark."*

- [ ] **Utility classes:** background (`.bg-blue-500`), text color (`.text-gray-700`), border color, radius (`.rounded-md`), padding (`.p-4`), font size and weight. `[5]`
- [ ] **Decision (ADR):** JavaFX CSS has **no `margin` property**. Decide how to handle margin and gap. Options are layout-pane properties such as `-fx-spacing`, `-fx-hgap`, `-fx-vgap`, `-fx-padding`, plus Java helpers for `setMargin`. `[2]`
- [ ] **Dark theme:** redefine semantic tokens under `.root.dark`. `[3]`
- [ ] **`ThemeManager`** class: `ThemeManager.apply(scene, Theme.DARK)` toggles the class and stylesheets. `[3]`
- [ ] **Color palette page** (a simple scene showing every swatch) to verify the tokens visually. `[3]`
- [ ] **Spike (stretch):** a script that *generates* the utility CSS from one palette definition, since writing thousands of classes by hand won't scale. `[3]`

### Learning objectives
JavaFX CSS reference · looked-up colors and inheritance · pseudo-classes (`:hover`, `:focused`, `:disabled`) · style classes vs IDs · `scene.getStylesheets()`.

### Exit criteria
- One token change (for example `-fxk-blue-500`) visibly changes the palette page.
- Light and dark themes both render correctly.
- Utility classes are documented in a short reference file.

---

## Phase 2: First Components (Sprints 3–4)

**Goal:** the first two components, `FxButton` and `FxCard`, which set the pattern for everything after them.

### Sprint 3: `FxButton`
**Sprint Goal:** *"A button with variants and sizes that works in Java and FXML."*

- [ ] **Spike / ADR:** extend the existing `Button` (fast, Scene Builder friendly) vs custom `Control` + `Skin`. Document the decision and when to use each. `[3]`
- [ ] Create `FxButton` with a **`variant`** property: `PRIMARY`, `SECONDARY`, `DANGER`, `SUCCESS`, `GHOST`, `OUTLINE`. `[5]`
- [ ] Add a **`size`** property: `SM`, `MD`, `LG`. `[3]`
- [ ] Style states: hover, pressed, focused, disabled. `[3]`
- [ ] Create a reusable helper for **enum-to-style-class syncing**, so other components can use it. `[3]`
- [ ] Unit tests: changing `variant` or `size` updates style classes correctly. `[2]`

### Sprint 4: `FxCard` and FXML
**Sprint Goal:** *"A card component and confirmed FXML support for both components."*

- [ ] Create `FxCard` with header, body and footer slots. `[5]`
- [ ] Card styling: border, radius, background, shadow ("elevation" levels). `[3]`
- [ ] Verify **FXML loading** of both components (`@DefaultProperty`, no-arg constructors, `module-info` opens). `[5]`
- [ ] **Spike:** load the library in Scene Builder (import the JAR) and document the steps. `[3]`
- [ ] Write the **Component Checklist** (from the Definition of Done) as `docs/new-component-checklist.md`. `[2]`
- [ ] **Stretch:** icon support on `FxButton` via Ikonli. `[3]`

### Learning objectives
JavaFX properties, bindings and listeners · `PseudoClass` · `@DefaultProperty` and FXML loading · custom controls vs subclassing · unit testing UI logic.

### Exit criteria (**M1 "First Light"**)
- `FxButton` and `FxCard` work in pure Java and in FXML.
- Both look right in light and dark themes.
- The Component Checklist exists and was followed for both.

---

## Phase 3: Showcase App (Sprint 5)

**Goal:** a demo application that documents and manually tests every component.

**Sprint Goal (suggested):** *"Every existing component has a live demo page I can browse."*

- [ ] App layout: navigation list on the left, content on the right (`BorderPane`, `ListView`, `ScrollPane`). `[5]`
- [ ] Simple page navigation (swap the content node). `[3]`
- [ ] Pages for **Button** (all variants and sizes and states) and **Card**. `[3]`
- [ ] **Colors** page (palette from Phase 1). `[2]`
- [ ] **Theme toggle** in the top bar. `[2]`
- [ ] Show a **code snippet** next to each demo (Java and FXML). `[3]`

### Learning objectives
Layout panes · navigation patterns · FXML controllers · organizing a demo app.

### Exit criteria (**M2 "Showcase"**)
- The showcase runs, and every existing component has a page.
- **From now on, every new component ships with a showcase page** (part of the Definition of Done).

---

## Phase 4: Inputs & Feedback Components (Sprints 6–8)

**Goal:** the core set of form and feedback components, and consistent event handling across them.

### Sprint 6: Text inputs
**Sprint Goal:** *"Text input with sizes, states and validation styling."*

- [ ] `FxTextField`: sizes, placeholder, focus ring, disabled and read-only. `[5]`
- [ ] Validation states: `DEFAULT`, `SUCCESS`, `ERROR` (border colors from tokens). `[3]`
- [ ] `FxTextArea`. `[3]`
- [ ] **Event convention (ADR):** how components expose events (for example `onValueChange`), following JavaFX's `ObjectProperty<EventHandler<...>>` pattern. `[3]`
- [ ] Showcase pages. `[2]`

### Sprint 7: Selection controls
**Sprint Goal:** *"Checkbox, radio, toggle switch and select, all consistent."*

- [ ] `FxCheckbox` and `FxRadioButton`. `[5]`
- [ ] `FxToggle` (switch): likely a custom `Control` + `Skin`, so a good first look at skins. `[5]`
- [ ] `FxSelect` (styled `ComboBox`, including the popup list). `[5]`
- [ ] Showcase pages. `[2]`

### Sprint 8: Feedback & display
**Sprint Goal:** *"Badge, alert, progress and avatar."*

- [ ] `FxBadge` (color variants and sizes). `[3]`
- [ ] `FxAlert`: inline info, success, warning and danger, with an optional dismiss button and `onClose` event. `[5]`
- [ ] `FxProgressBar` (styled `ProgressBar`). `[2]`
- [ ] `FxAvatar` (image with circular clip, initials fallback). `[3]`
- [ ] Typography helpers (headings and text styles as classes). `[2]`
- [ ] Showcase pages and a refactor pass over shared code. `[3]`

### Learning objectives
Focus and keyboard handling · custom events · `Control` + `Skin` for complex widgets · styling popups · `Clip` and `ImageView`.

### Exit criteria (**`v0.1.0-alpha`**)
- All listed components meet the Definition of Done.
- Event conventions are documented and applied consistently.
- Optional: publish an alpha build to GitHub Packages for early feedback.

---

## Phase 5: Composite Components (Sprints 9–13)

**Goal:** the high-value components that save the most time: forms with validation, dialogs, notifications and data tables.

### Sprint 9: `FormField` & validation
**Sprint Goal:** *"A form field with label, input, helper text and error message, driven by validators."*

- [ ] `FormField` wrapping any input with label, helper text and error text. `[5]`
- [ ] `Validator` interface + built-ins: `required`, `minLength`, `maxLength`, `email`, `regex`. `[5]`
- [ ] Validation triggers: on change, on blur, on submit. `[3]`
- [ ] `FxForm` helper: validate all fields, get values, `onSubmit`. `[5]`

### Sprint 10: `Modal` / `Dialog`
**Sprint Goal:** *"A modal dialog with overlay, focus handling and close behavior."*

- [ ] **Spike / ADR:** in-scene overlay (`StackPane` layer) vs a separate `Stage`. Trade-offs: theming, positioning, blocking behavior. `[3]`
- [ ] `Modal` with header, body, footer, close button, ESC to close, click-outside to close. `[8]`
- [ ] Confirm dialog helper (`Modal.confirm(...)`). `[3]`

### Sprint 11: `Toast` & `Tabs`
**Sprint Goal:** *"Non-blocking notifications and tabbed content."*

- [ ] `ToastManager`: show, auto-dismiss, stack positions, variants. `[5]`
- [ ] Animation for toasts (fade and slide using `Timeline`/`Transition`). `[3]`
- [ ] `FxTabs` (styled `TabPane`). `[3]`
- [ ] `Accordion`/collapsible section (stretch). `[3]`

### Sprints 12–13: `DataTable<T>`
**Sprint Goal (12):** *"Define columns fluently and render data."*
**Sprint Goal (13):** *"Sorting, filtering, pagination and selection."*

- [ ] Fluent column API: `column("Name", User::getName)`. `[5]`
- [ ] Styling: header, striped rows, hover, selected row, empty state. `[5]`
- [ ] Sorting (using `SortedList`). `[3]`
- [ ] Search/filter box (using `FilteredList`). `[3]`
- [ ] Pagination component and page-size selector. `[8]`
- [ ] Row selection and `onRowSelected` event. `[3]`
- [ ] Row actions (edit/delete buttons per row). `[5]`
- [ ] Loading state (skeleton or spinner). `[3]`
- [ ] Performance check with 10,000 rows. `[3]`

### Learning objectives
Generics and functional interfaces · `TableView`, cell factories and value factories · `ObservableList`, `FilteredList`, `SortedList` · scene layering for overlays · animations.

### Exit criteria
- A complete login form or CRUD form can be built with `FxForm` + `FormField` in minutes.
- `DataTable<T>` renders, sorts, filters and paginates real data smoothly.
- APIs feel consistent with earlier components.

---

## Phase 6: App Shell & Templates (Sprints 14–16)

**Goal:** the pieces that give a new project an instant application structure, and ready-made screens developers can copy.

### Sprint 14: Layout components
**Sprint Goal:** *"A sidebar and navbar that form an app shell."*

- [ ] `Sidebar` (collapsible, with icons, active item highlight). `[8]`
- [ ] `Navbar` / top bar (title, actions, user menu). `[5]`
- [ ] `AppShell` combining sidebar + navbar + content area. `[5]`
- [ ] `Breadcrumb`. `[2]`

### Sprint 15: Navigation
**Sprint Goal:** *"Move between screens without rewriting navigation each time."*

- [ ] Lightweight `Navigator`: register screens, navigate by name, back/forward. `[8]`
- [ ] Integration with `AppShell` and `Sidebar`. `[3]`
- [ ] Documented pattern for passing data between screens. `[3]`

### Sprint 16: Templates
**Sprint Goal:** *"Four copy-and-adapt screens: login, dashboard, settings and CRUD."*

- [ ] **Login** screen template (FXML + Java). `[3]`
- [ ] **Dashboard** template (stat cards + table). `[5]`
- [ ] **Settings** template (tabbed form). `[3]`
- [ ] **CRUD** template (table + modal form). `[5]`
- [ ] Showcase "Templates" section. `[3]`

### Learning objectives
Composition over inheritance · FXML `fx:include` · navigation and state patterns (light MVC/MVVM) · responsive layouts with JavaFX panes.

### Exit criteria (**`v0.1.0-beta`**)
- You can build a small real app (login, dashboard, CRUD) using only FXKit components and templates.
- **Dogfooding test:** build one real mini-project with the library and log every friction point as backlog items.

---

## Phase 7: Quality, Docs, Tooling & First Release (Sprints 17–19)

**Goal:** turn a working library into a releasable product other people can adopt.

### Sprint 17: Quality
**Sprint Goal:** *"Tests, CI and accessibility are in place."*

- [ ] Automated UI tests with **TestFX** for key components. `[8]`
- [ ] **CI** with GitHub Actions: build and test on every push and PR. `[5]`
- [ ] **Accessibility pass:** keyboard navigation, visible focus, accessible text/roles. `[5]`
- [ ] Fix issues found while dogfooding in Phase 6. `[5]`

### Sprint 18: Documentation & tooling
**Sprint Goal:** *"A new user can start a project from docs or from a generated project."*

- [ ] Complete **Javadoc** for the public API. `[5]`
- [ ] **Documentation site** (Markdown on GitHub Pages or a docs generator): Getting Started, Theming, Component reference, Cookbook. `[8]`
- [ ] **Maven archetype** (`fxkit-archetype`) that generates a project with the library, theme, app shell and a sample screen. `[8]`
- [ ] Finalize the **utility-class generator** script (if not done in Phase 1). `[3]`
- [ ] Scene Builder integration guide. `[2]`

### Sprint 19: Release
**Sprint Goal:** *"Publish v0.1.0."*

- [ ] Add project files: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, issue and PR templates, `CHANGELOG.md`. `[3]`
- [ ] Build **installers for the showcase app** with `jpackage` (optional, but good for demos). `[5]`
- [ ] Prepare Maven publishing (sources JAR, Javadoc JAR, signing, POM metadata) and publish to **Maven Central** (or GitHub Packages first as a dry run). `[8]`
- [ ] Release notes, version tag `v0.1.0`, announcement post. `[3]`
- [ ] Create a "Getting feedback" channel (GitHub Discussions). `[1]`

### Learning objectives
Testing UI · CI/CD · documentation practices · packaging and publishing · semantic versioning · open-source project hygiene.

### Exit criteria (**`v0.1.0`**)
- The library is published and installable by a stranger from the docs alone.
- The archetype produces a running project.
- CI is green on `main`.

---

## Phase 8: Growth & Maintenance (Ongoing)

**Goal:** keep improving based on real feedback, and keep up with the JavaFX ecosystem.

**Working mode:** continue normal sprints. Each sprint mixes **feedback-driven fixes**, **planned features** and **maintenance**.

**Backlog candidates (prioritize by user feedback):**

- More components: `DatePicker`, dropdown menu, stepper/wizard, tree view, skeleton loaders, tooltip and popover, pagination as a standalone component, command palette
- Theme customization: custom palettes, a "theme builder" tool, a theme JSON to CSS generator
- CLI (`fxkit new my-app`) beyond the Maven archetype
- Gradle support and instructions
- More templates (invoice, chat, kanban)
- Performance and memory improvements
- Localization/RTL support
- Compatibility upgrades: JavaFX ships new releases on a roughly six-month cadence, so plan regular dependency updates
- Community: triage issues, review pull requests, publish a public roadmap

**Release rhythm:** patch releases for bug fixes as needed; a minor release (`0.2.0`, `0.3.0`...) every 4 to 6 sprints. Move to `1.0.0` when the API has been stable through several releases.

---

## 14. Cross-Phase Practices

### 14.1 Git workflow
- `main` is always working and releasable.
- Work on short-lived branches: `feature/fx-button`, `fix/card-shadow`, `spike/scene-builder`.
- Open a **pull request** even when working solo; the PR description is your review checklist.
- Use **Conventional Commits**: `feat: add FxButton variants`, `fix: correct focus ring color`, `docs: update theming guide`.

### 14.2 Versioning
Semantic Versioning (`MAJOR.MINOR.PATCH`). Pre-1.0 releases (`0.x`) may make breaking changes, so document them in `CHANGELOG.md`.

### 14.3 Architecture Decision Records (ADRs)
Whenever you make a significant choice (for example, subclass vs custom `Control`, overlay vs `Stage`), write a short note in `docs/adr/`:

```
# ADR-001: <title>
Status: Accepted | Date: YYYY-MM-DD
Context: what problem are we solving?
Options: A, B, C with trade-offs
Decision: what we chose and why
Consequences: what this means going forward
```

These notes also help you remember *why* things are the way they are, and they are a great learning record.

### 14.4 Testing strategy
| Level | Tool | What it covers |
|---|---|---|
| Unit | JUnit 5 | Property-to-style mapping, validators, helpers |
| UI | TestFX | Clicks, typing, focus, dialogs |
| Visual | Showcase app | Manual check of every variant and theme |
| Integration | Archetype project | A generated project builds and runs in CI |

### 14.5 Refactoring
Plan a small **refactoring budget** (about 10 to 15% of each sprint). Early components will teach you better patterns, and you'll want to go back and apply them.

### 14.6 Metrics to watch (keep it light)
- Sprint Goal met? (yes/no)
- Points completed vs planned (velocity trend)
- Bugs found after "done"
- Time to create a new component (should shrink)

### 14.7 Risk watchlist
| Risk | Mitigation |
|---|---|
| JavaFX CSS limitations block a design | Spike early; use `Skin` or Java code where CSS can't do it |
| Scope creep | Finish components fully; put new ideas in the backlog, not the sprint |
| Motivation dips on a long solo project | Demo to someone each sprint; keep sprints small with visible wins |
| Inconsistent APIs across components | Follow ADRs and the Component Checklist; refactor in retrospectives |
| Scene Builder quirks | Treat Scene Builder support as a spike and document the workarounds |

---

## 15. Templates

### 15.1 User story

```
Title: FxButton supports variants
As a JavaFX developer,
I want to set a button's variant (primary, danger, ghost...),
so that I can style it without writing CSS.

Acceptance criteria
- Given a FxButton, when I call setVariant(DANGER), then the button uses the danger style.
- Variant can be set from FXML: variant="DANGER".
- Works in light and dark themes.

Estimate: 5    Epic: Button    Phase: 2
```

### 15.2 Sprint Planning sheet

```
Sprint #:            Dates:
Sprint Goal:
Capacity (hours):
Committed stories (with points):
Risks / dependencies:
```

### 15.3 Sprint Review checklist
- [ ] Was the Sprint Goal met?
- [ ] Demo the increment (screen recording or live).
- [ ] Collect feedback from your peer or mentor.
- [ ] Update the Product Backlog (new items, reprioritization).

### 15.4 Retrospective

```
What went well?
What didn't?
What will I change next sprint? (max 3, specific and testable)
What did I learn about Java/JavaFX this sprint?
```

The last question is there on purpose. Learning is a goal of this project, so track it.

---

## Quick Start: Your Next Actions

1. Create the GitHub repository and project board.
2. Copy the Phase 0 stories into the board as your first backlog items.
3. Run **Sprint Planning for Sprint 0**: choose a goal and start.
4. Book your first **Sprint Review** date now, so there's a deadline and an audience.
