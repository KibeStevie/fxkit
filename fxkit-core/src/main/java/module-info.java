/**
 * FXKit core: design tokens, utility CSS and UI components for JavaFX.
 */
module dev.fxkit.core {

    // "requires transitive": our public API will expose JavaFX types
    // (e.g. FxButton extends Button), so anyone who requires dev.fxkit.core
    // automatically gets javafx.controls (and javafx.graphics/base) too.
    requires transitive javafx.controls;

    // Ikonli (#36, stretch: icon support on FxButton). "transitive" because
    // FxButton.Ikon-typed API
    // (setIcon(Ikon), iconProperty()) puts org.kordamp.ikonli.Ikon in our own
    // public API, same reasoning
    // as "requires transitive javafx.controls" above for Button. ikonli.javafx
    // (FontIcon) is only ever
    // used internally by FxButton, never in a public method signature, so it stays
    // a plain requires.
    requires transitive org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;

    // Packages other modules are allowed to use.
    exports dev.fxkit.core;
    exports dev.fxkit.core.theme;
    exports dev.fxkit.core.components.accordion;
    exports dev.fxkit.core.components.avatar;
    exports dev.fxkit.core.components.alert;
    exports dev.fxkit.core.components.badge;
    exports dev.fxkit.core.components.blockquote;
    exports dev.fxkit.core.components.breadcrumb;
    exports dev.fxkit.core.components.button;
    exports dev.fxkit.core.components.card;
    exports dev.fxkit.core.components.carousel;
    exports dev.fxkit.core.components.clipboard;
    exports dev.fxkit.core.components.drawer;
    exports dev.fxkit.core.components.dropdown;
    exports dev.fxkit.core.components.datepicker;
    exports dev.fxkit.core.components.form;
    exports dev.fxkit.core.components.footer;
    exports dev.fxkit.core.components.hr;
    exports dev.fxkit.core.components.list;
    exports dev.fxkit.core.components.listgroup;
    exports dev.fxkit.core.components.modal;
    exports dev.fxkit.core.components.megamenu;
    exports dev.fxkit.core.components.navbar;
    exports dev.fxkit.core.components.pagination;
    exports dev.fxkit.core.components.progress;
    exports dev.fxkit.core.components.popover;
    exports dev.fxkit.core.components.rating;
    exports dev.fxkit.core.components.spinner;
    exports dev.fxkit.core.components.sidebar;
    exports dev.fxkit.core.components.toast;
    exports dev.fxkit.core.components.timeline;
    exports dev.fxkit.core.components.tabs;
    exports dev.fxkit.core.components.table;
    exports dev.fxkit.core.components.tooltip;
}
