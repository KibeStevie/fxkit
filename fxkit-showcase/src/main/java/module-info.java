/**
 * FXKit showcase: demo application with one page per component.
 */
module dev.fxkit.showcase {
    requires dev.fxkit.core;
    requires javafx.controls;
    requires javafx.fxml;

    // Ikonli (#36, stretch): org.kordamp.ikonli.core comes to us transitively via dev.fxkit.core
    // ("requires transitive", since Ikon is part of FxButton's own public API), but org.kordamp.ikonli.
    // javafx (FontIcon, used directly in fxml-demo.fxml and ButtonPage.java) is only a plain requires on
    // fxkit-core's side, so it isn't passed on - this module needs its own requires for it. The pack
    // (org.kordamp.ikonli.devicons) needs its own requires too, purely so it's part of the module graph
    // at all: nothing here references its Java API directly (see ButtonPage.java), but its icon-code
    // string (di-java) can only resolve at runtime if its module is actually present.
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.devicons;

    // JavaFX's launcher (in javafx.graphics) creates our Application subclass by
    // reflection, so this package must be visible to it.
    exports dev.fxkit.showcase to javafx.graphics;

    // FXMLLoader (javafx.fxml) needs reflective access to controllers in this package.
    opens dev.fxkit.showcase to javafx.fxml;
}
