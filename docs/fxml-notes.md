# FXML notes (#33: verify FXML loading for FxButton and FxCard)

What it took to get `FxButton` and `FxCard` loading from FXML in `fxkit-showcase` — a different module
from the one that defines them (`fxkit-core`) — via `dev.fxkit.showcase.FxmlDemoPage` and
`fxml-demo.fxml`.

## What already worked, unchanged

- **`module-info.java`.** `fxkit-core` already had everything FXML needs, added for `FxButton` back in
  Phase 2 (#27):
  ```java
  exports dev.fxkit.core.components;
  opens dev.fxkit.core.components to javafx.fxml;
  ```
  `exports` lets `dev.fxkit.showcase` reference the `FxButton`/`FxCard` types at compile time; `opens`
  lets `javafx.fxml`'s `FXMLLoader` construct and set properties on them reflectively at runtime. Since
  `FxCard` lives in the same package, it's covered by the exact same two lines — no module-info change
  was needed for this story.
- **No-arg constructors and standard property setters.** `FXMLLoader` needs a public no-arg constructor
  and a public setter (or a settable property) for every attribute it sees. `FxButton` already had both;
  `FxCard` needed the same, which is why its constructor takes no arguments and `header`/`body`/`footer`/
  `elevation` are all plain `ObjectProperty`s with `get`/`set` pairs.
- **`fx:controller` was not needed.** The demo FXML builds a plain node tree with no event wiring, so
  there's no controller class and nothing to reflectively construct beyond the nodes FXML already knows
  how to build.

## The one thing that needed deciding: FxCard's slots in FXML

`FxCard`'s header/body/footer aren't ordinary children — only *one* node belongs in each slot, and
`VBox` (FxCard's base class, see `docs/adr/ADR-003-card-base-class.md`) already has its own list-typed
default property (`children`, inherited from `Pane`). Left alone, `<FxCard><Label .../></FxCard>` would
add the label straight to `getChildren()` instead of going through a slot property, bypassing the
wrapper/divider logic entirely.

The fix is `@DefaultProperty("body")` on `FxCard` itself, which overrides the inherited one:

```java
@DefaultProperty("body")
public class FxCard extends VBox { ... }
```

That makes a single unnamed nested element set the body slot (`<FxCard><Label text="..."/></FxCard>`),
while `header` and `footer` are addressed with the standard named-element syntax already used for things
like `BorderPane`'s `top`/`bottom`/`left`/`right`:

```xml
<FxCard elevation="MD">
    <header><Label text="Delete account"/></header>
    <Label text="This can't be undone." wrapText="true"/>  <!-- default property: sets body -->
    <footer><FxButton text="Delete" variant="DANGER"/></footer>
</FxCard>
```

## Enum and list attributes, for reference

- `FxButton`'s `variant`/`size` and `FxCard`'s `elevation` are plain enum properties, so FXML's
  case-sensitive enum-constant coercion just works: `variant="DANGER"`, `size="LG"`, `elevation="MD"`.
- `styleClass="text-lg, font-semibold, text-body"` (comma-separated) works on any `Node` out of the box —
  this is `FXMLLoader`'s built-in coercion from a string to a list-typed property with no setter
  (`Node.getStyleClass()` only has a getter), not something FXKit had to add.

## Not yet covered

Scene Builder's own library/inspector experience (importing the JAR, whether `variant`/`size`/`elevation`
show up as enum dropdowns) is out of scope for this story — that's the separate spike, "Load FXKit in
Scene Builder" (#33's sibling issue in the same sprint).
