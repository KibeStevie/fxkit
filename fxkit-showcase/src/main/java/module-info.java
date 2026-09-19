/**
 * FXKit showcase: demo application with one page per component.
 */
module dev.fxkit.showcase {
    requires dev.fxkit.core;
    requires javafx.controls;
    requires javafx.fxml;

    // JavaFX's launcher (in javafx.graphics) creates our Application subclass by
    // reflection, so this package must be visible to it.
    exports dev.fxkit.showcase to javafx.graphics;

    // FXMLLoader (javafx.fxml) needs reflective access to controllers in this package.
    opens dev.fxkit.showcase to javafx.fxml;
}
