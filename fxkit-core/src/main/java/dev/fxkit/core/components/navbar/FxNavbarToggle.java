package dev.fxkit.core.components.navbar;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * The hamburger button of an {@link FxNavbar} (Flowbite's {@code <NavbarToggle>}).
 *
 * <p>{@link FxNavbar} owns one toggle and shows it only while the navbar is compact (Flowbite's
 * {@code md:hidden}); clicking it expands or collapses the stacked links. You normally never create
 * one yourself, use {@link FxNavbar#getToggle()} to reach it, for example to change its accessible
 * text.
 *
 * <p>The icon is three stroked bars in a 24px box (Flowbite's {@code h-6 w-6}). Its color comes from
 * the looked-up color {@code -fxk-navbar-toggle-fg} in {@code components.css}, so it follows the theme.
 */
public class FxNavbarToggle extends Button {

    /** Flowbite's screen-reader title for the toggle. */
    public static final String DEFAULT_ACCESSIBLE_TEXT = "Open main menu";

    public FxNavbarToggle() {
        getStyleClass().add("fxk-navbar-toggle");
        setMnemonicParsing(false);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        SVGPath bars = new SVGPath();
        bars.setContent("M4 6 L20 6 M4 12 L20 12 M4 18 L20 18");
        bars.getStyleClass().add("fxk-navbar-toggle-icon");

        StackPane box = new StackPane(bars);
        box.getStyleClass().add("fxk-navbar-toggle-icon-box");
        box.setMouseTransparent(true); // clicks always land on the button itself
        setGraphic(box);

        setAccessibleText(DEFAULT_ACCESSIBLE_TEXT);
    }
}