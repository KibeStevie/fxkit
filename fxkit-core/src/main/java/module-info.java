/**
 * FXKit core: design tokens, utility CSS and UI components for JavaFX.
 */
module dev.fxkit.core {

    // "requires transitive": our public API will expose JavaFX types
    // (e.g. FxButton extends Button), so anyone who requires dev.fxkit.core
    // automatically gets javafx.controls (and javafx.graphics/base) too.
    requires transitive javafx.controls;

    // Needed once components load FXML or ship FXML files.
    requires javafx.fxml;

    // Ikonli (#36, stretch: icon support on FxButton). "transitive" because FxButton.Ikon-typed API
    // (setIcon(Ikon), iconProperty()) puts org.kordamp.ikonli.Ikon in our own public API, same reasoning
    // as "requires transitive javafx.controls" above for Button. ikonli.javafx (FontIcon) is only ever
    // used internally by FxButton, never in a public method signature, so it stays a plain requires.
    requires transitive org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    // Packages other modules are allowed to use.
    exports dev.fxkit.core;
    exports dev.fxkit.core.theme;
    exports dev.fxkit.core.components;

    // Lets FXMLLoader create FxButton (and later components) by reflection from FXML.
    opens dev.fxkit.core.components to javafx.fxml;

    // dev.fxkit.core.internal (EnumStyleClassSync, ...) is intentionally not exported or
    // opened: it is shared only between packages inside this module, never public API.
}
