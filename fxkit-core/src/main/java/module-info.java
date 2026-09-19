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

    // Packages other modules are allowed to use.
    exports dev.fxkit.core;
    exports dev.fxkit.core.theme;
    exports dev.fxkit.core.components;

    // Lets FXMLLoader create FxButton (and later components) by reflection from FXML.
    opens dev.fxkit.core.components to javafx.fxml;

    // dev.fxkit.core.internal (EnumStyleClassSync, ...) is intentionally not exported or
    // opened: it is shared only between packages inside this module, never public API.
}
