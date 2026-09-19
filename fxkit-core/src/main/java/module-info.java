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

    // Phase 2: when the components package exists, open it to FXML so that
    // FXMLLoader can create our controls by reflection:
    // opens dev.fxkit.core.components to javafx.fxml;
}
