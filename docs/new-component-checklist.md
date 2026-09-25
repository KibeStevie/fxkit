# New component checklist

Walk through this for every new FXKit component (`dev.fxkit.core.components`). It's the Definition of
Done (`docs/FXKIT_DEVELOPMENT_PHASES.md`, section 4.2) turned into concrete steps, plus what `FxButton`
and `FxCard` actually needed - so it's grounded in two real components, not guessed in advance (#35).

## 1. Decide the base class first

Before writing any code, answer this with `docs/adr/ADR-002-button-base-class.md`'s rule of thumb
(narrowed by `docs/adr/ADR-003-card-base-class.md` for layout-shaped components):

- **Subclass an existing `javafx.scene.control` class** when the component *is* that control with a
  different look and a small set of extra typed properties (`FxButton extends Button`).
- **Subclass an existing `javafx.scene.layout` class** when the component is a layout of optional/typed
  child slots with no interaction model of its own beyond what that layout class already provides
  (`FxCard extends VBox`).
- **Build a `Control` + `Skin`** only when neither fits - a node structure, interaction model or
  accessibility role that nothing existing provides.

Write a new ADR only if the decision isn't already covered by the two above; otherwise just cite the
relevant one in the component's Javadoc, like both existing components do.

## 2. Java API

- [ ] Package: `dev.fxkit.core.components`.
- [ ] Public no-arg constructor (required for FXML - see section 4). Add other constructors as
      convenience overloads that end up calling the same initialization, not duplicating it
      (`FxButton(String text)` calls `initialize()` too).
- [ ] Every "which style" switch (variant, size, elevation, ...) is a typed enum property: private
      `ObjectProperty<E>` field, `getX()`/`setX(E)`/`xProperty()`, wired in the constructor with
      `dev.fxkit.core.internal.EnumStyleClassSync.sync(this, "fxk-<component>-<name>-", property)`. Don't
      hand-write the listener - that's exactly what the helper is for.
- [ ] A `public static final String STYLE_CLASS` constant for the component's own base style class
      (`fxk-btn`, `fxk-card`), added to `getStyleClass()` in the constructor before any
      `EnumStyleClassSync.sync` call.
- [ ] `public static final <Enum> DEFAULT_<NAME>` constants for every enum property's default, so the
      default is documented and referenceable (`FxButton.DEFAULT_VARIANT`, `FxCard.DEFAULT_ELEVATION`).
- [ ] Any slot or child content (`FxCard`'s header/body/footer) is a plain `ObjectProperty<Node>`, not a
      raw `getChildren()` exposure - so the component controls layout/CSS around it and FXML can address
      it by name (see section 4).

## 3. Styling (tokens only)

- [ ] Add a new, clearly-commented section to `components.css` (not `utilities.css` - that's for classes
      callers compose themselves) explaining what each style class does and any subtlety (see the
      `FxCard` divider comment for an example of documenting a non-obvious CSS interaction).
- [ ] **Every color, border and shadow comes from a semantic or elevation token** (`-fxk-primary`,
      `-fxk-shadow-md`, ...), never a raw `-fxk-<scale>-<step>` value or a literal hex/rgb - see
      `docs/design-tokens.md`. This is what makes light/dark theming automatic.
- [ ] Spacing, radius and font size ARE literal values in CSS (JavaFX CSS can't look up a value for
      `-fx-padding`/`-fx-background-radius`/`-fx-font-size` - `docs/css-notes.md` section 8) - but they
      must still match `tools/scales.txt`'s scale (comment the scale name next to the literal, e.g.
      `-fx-padding: 8px 16px 8px 16px; /* space-2 / space-4 */`), not an arbitrary number.
- [ ] **States**: `:hover`, `:pressed`/`:armed`, `:focused`, `:disabled` handled wherever the component
      is interactive; each variant pins its own resting colors on `:disabled` rather than relying on
      `-fx-opacity` alone catching everything (a stray `:hover` pseudo-class can still be set on a
      disabled node in JavaFX). A non-interactive component (`FxCard` itself) doesn't need these, but its
      *contents* (a button in the footer) still do, for free, since they're their own components.
- [ ] **Verify both themes.** There's no automated check for this yet - open the showcase, use the theme
      toggle, and look.

## 4. FXML support

- [ ] No-arg constructor (section 2) - `FXMLLoader` needs one.
- [ ] Every settable thing is a real JavaBean property (`getX`/`setX`), not just a field - FXML attributes
      and nested elements both go through the setter, not the field.
- [ ] Enum properties just work via FXML's built-in enum-constant coercion
      (`variant="DANGER"`, `size="LG"`, `elevation="MD"`) - nothing extra needed.
- [ ] List-typed things like `styleClass` work via FXML's built-in comma-separated-string coercion
      (`styleClass="text-lg, font-semibold"`) - also nothing extra needed.
- [ ] If the component has more than one child slot (`FxCard`), decide its `@DefaultProperty`
      (`javafx.beans.DefaultProperty`, class-level annotation) deliberately - a `Pane` subclass inherits
      `@DefaultProperty("children")`, which lets a nested element bypass your slot properties entirely
      unless you override it. `FxCard`'s is `@DefaultProperty("body")`; a single-slot or non-layout
      component often doesn't need one.
- [ ] Confirm `module-info.java` covers it: the component's package needs `exports` (compile-time use
      from other modules) AND `opens ... to javafx.fxml;` (FXMLLoader's reflective construction). Both
      were already true for the whole `dev.fxkit.core.components` package after `FxButton` (#27), so
      `FxCard` needed no module-info change at all - check first before assuming you need one.
- [ ] Add (or extend) the FXML demo in the showcase (`fxml-demo.fxml` / `FxmlDemoPage.java`) so the
      component is proven to load from a *different* module than the one that defines it - that's what
      actually exercises the `exports`/`opens` wiring, not just compiling.
- [ ] Write down anything that needed a workaround in `docs/fxml-notes.md` (or update it) - `FxCard`'s
      `@DefaultProperty` override is the example there.

## 5. Showcase page

- [ ] One page per component (`ButtonPage`/`CardPage`), added to `ShowcaseApp`'s page list.
- [ ] Cover every enum value at least once (a grid of variant x size for `FxButton`, one card per
      `Elevation` for `FxCard`).
- [ ] Cover edge cases the acceptance criteria call out explicitly (disabled state; `FxCard`'s "empty
      slots take no space" via a card with only some slots filled).
- [ ] No hand-set colors/padding/fonts on showcase nodes for the component itself - if the page needs to
      set something by hand to make the component look right, that's a sign the component's own CSS is
      missing a rule, not a showcase problem to paper over.

## 6. Tests

- [ ] One test class per component under `dev.fxkit.core.components`, JUnit 5.
- [ ] `@ExtendWith(dev.fxkit.core.testsupport.JavaFxToolkit.class)` on the class if it constructs
      anything that needs the JavaFX toolkit running - which is any `javafx.scene.control.Control`
      (`FxButton` itself; also `Separator`, `Tooltip`, or any other control a component builds
      internally, not just the component under test - `FxCard`'s tests need it purely because filling
      more than one slot makes it build a `Separator`). Plain `Region`/`Pane`/`Node` construction doesn't
      need it. See `JavaFxToolkit`'s own Javadoc for why.
- [ ] Cover: the default state (base style class present, defaults match the documented `DEFAULT_*`
      constants); each property change adds the new style class AND removes the old one (never both, or
      neither); properties combine independently (changing one doesn't disturb another); the property
      object itself reflects direct `.set()` calls, not just the setter method.
- [ ] Tests run on the classpath, not the module path (`useModulePath=false` in the parent POM's surefire
      config) - a JPMS module boundary between test classes and the classes they test causes reflection
      failures that have nothing to do with the component's own logic (see the fxkit git history for two
      real examples of exactly that).

## 7. Javadoc

- [ ] Class-level: what the component is, which ADR decided its base class (if any), what it requires on
      the scene (`ThemeManager#apply`), a `<h2>Java</h2>` and `<h2>FXML</h2>` snippet.
- [ ] Every public field, constructor and method - `getX`/`setX`/`xProperty()` triads all get their own
      `@return`/`@param`, even though they're short; don't skip the "obvious" ones.
- [ ] Document what happens at the edges: what does `null` mean for an optional property (`FxCard`'s
      slots), what's the default, does setting one property affect another.

## Walkthrough log

This checklist was written after `FxButton` and `FxCard` were already built (#27-#33), then walked
through against both to check for gaps (#35's own acceptance criterion). Everything above matches what
both components already do — no gaps needing a follow-up fix were found for either.
