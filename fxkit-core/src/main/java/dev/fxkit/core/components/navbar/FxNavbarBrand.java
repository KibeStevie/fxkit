package dev.fxkit.core.components.navbar;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;

/**
 * The logo and name at the start of an {@link FxNavbar} (Flowbite's {@code <NavbarBrand>}).
 *
 * <p>It is a {@link Hyperlink}, so "go to the home page" is the usual {@code setOnAction}. The logo is
 * the graphic and the name is the text, 12px apart (Flowbite's {@code mr-3}). Any node works as a logo:
 * an {@code ImageView}, or a {@code FontIcon}, which {@code components.css} paints in the primary
 * color at 32px.
 *
 * <pre>{@code
 * ImageView logo = new ImageView(new Image("/images/logo.png", 0, 36, true, true));
 * FxNavbarBrand brand = new FxNavbarBrand("Flowbite React", logo);
 * brand.setOnAction(e -> goHome());
 * }</pre>
 */
public class FxNavbarBrand extends Hyperlink {

    public FxNavbarBrand() {
        this(null, null);
    }

    /**
     * @param text the name shown after the logo
     */
    public FxNavbarBrand(String text) {
        this(text, null);
    }

    /**
     * @param text the name shown after the logo; may be {@code null}
     * @param logo the logo, or {@code null} for none
     */
    public FxNavbarBrand(String text, Node logo) {
        super(text);
        getStyleClass().add("fxk-navbar-brand");
        setMnemonicParsing(false);
        setContentDisplay(ContentDisplay.LEFT);
        setGraphic(logo);
    }

    /** A brand is never "visited": Hyperlink would otherwise restyle it after the first click. */
    @Override
    public void fire() {
        super.fire();
        setVisited(false);
    }
}