package dev.fxkit.core.components.megamenu;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** The two stroked glyphs the mega menu draws itself, so it needs no icon pack. */
final class FxMegaMenuIcons {

    private FxMegaMenuIcons() {
    }

    /** Chevron pointing down, in a 16px box (Flowbite's {@code HiChevronDown}). */
    static StackPane chevronDown() {
        SVGPath path = new SVGPath();
        path.setContent("M4 6 L8 10 L12 6");
        path.getStyleClass().add("fxk-megamenu-chevron");
        StackPane box = new StackPane(path);
        box.getStyleClass().add("fxk-megamenu-chevron-box");
        box.setMouseTransparent(true);
        return box;
    }

    /** Arrow pointing right, in a 16px box (Flowbite's {@code HiArrowRight}). */
    static StackPane arrowRight() {
        SVGPath path = new SVGPath();
        path.setContent("M3 8 L13 8 M9 4 L13 8 L9 12");
        path.getStyleClass().add("fxk-megamenu-arrow");
        StackPane box = new StackPane(path);
        box.getStyleClass().add("fxk-megamenu-arrow-box");
        box.setMouseTransparent(true);
        return box;
    }
}
