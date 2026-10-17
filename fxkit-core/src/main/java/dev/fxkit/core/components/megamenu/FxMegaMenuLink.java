package dev.fxkit.core.components.megamenu;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * One text link inside a mega menu dropdown: Flowbite's
 * {@code <a href="#" className="hover:text-primary-600">}, optionally with the small icon of the
 * "Mega menu with icons" example.
 *
 * <p>It is a {@link Hyperlink}, so the click handler is {@code setOnAction}. The text is muted and
 * turns primary on hover; the icon follows.
 *
 * <pre>{@code
 * FxMegaMenuLink about = new FxMegaMenuLink("About Us");
 * about.setOnAction(e -> router.go("/about"));
 *
 * // With a Flowbite icon: paste the <path d="..."> data and the size of its viewBox.
 * FxMegaMenuLink withIcon = new FxMegaMenuLink("About Us", FxMegaMenuLink.svgIcon("M10 .5a9.5 ...", 20));
 *
 * // The "full width" look: hover underline
 * about.setUnderlineOnHover(true);
 * }</pre>
 */
public class FxMegaMenuLink extends Hyperlink {

    /** Flowbite's {@code h-3 w-3}. */
    public static final double ICON_SIZE = 12;

    private final BooleanProperty underlineOnHover = new SimpleBooleanProperty(this, "underlineOnHover", false);

    public FxMegaMenuLink() {
        this(null, null);
    }

    /**
     * @param text the label
     */
    public FxMegaMenuLink(String text) {
        this(text, null);
    }

    /**
     * @param text the label
     * @param icon shown before the text, or {@code null}; see {@link #svgIcon(String, double)}
     */
    public FxMegaMenuLink(String text, Node icon) {
        super(text);
        getStyleClass().add("fxk-megamenu-link");
        setMnemonicParsing(false);
        setContentDisplay(ContentDisplay.LEFT);
        setGraphic(icon);
        BooleanStyleClassSync.sync(this, "fxk-megamenu-link-underline", underlineOnHover);
    }

    /**
     * Builds a 12px icon from SVG path data, so Flowbite's inline {@code <svg>} icons can be pasted
     * as they are. The path is filled with the muted text color and primary on hover
     * ({@code components.css}).
     *
     * @param pathData    the {@code d} attribute of the {@code <path>} (join several paths with a space)
     * @param viewBoxSize the larger side of the SVG's {@code viewBox}, e.g. {@code 20} for
     *                    {@code viewBox="0 0 20 20"}
     * @return a fixed 12 x 12 node
     */
    public static Node svgIcon(String pathData, double viewBoxSize) {
        SVGPath path = new SVGPath();
        path.setContent(pathData);
        path.getStyleClass().add("fxk-megamenu-icon");
        double scale = ICON_SIZE / viewBoxSize;
        path.setScaleX(scale);
        path.setScaleY(scale);

        StackPane box = new StackPane(new Group(path));
        box.getStyleClass().add("fxk-megamenu-icon-box");
        box.setMinSize(ICON_SIZE, ICON_SIZE);
        box.setPrefSize(ICON_SIZE, ICON_SIZE);
        box.setMaxSize(ICON_SIZE, ICON_SIZE);
        box.setMouseTransparent(true);
        return box;
    }

    /** A link is never "visited": Hyperlink would otherwise restyle it after the first click. */
    @Override
    public void fire() {
        super.fire();
        setVisited(false);
    }

    /**
     * @return whether the text is underlined on hover (the "full width" examples'
     *         {@code hover:underline})
     */
    public final BooleanProperty underlineOnHoverProperty() {
        return underlineOnHover;
    }

    public final boolean isUnderlineOnHover() {
        return underlineOnHover.get();
    }

    public final void setUnderlineOnHover(boolean underlineOnHover) {
        this.underlineOnHover.set(underlineOnHover);
    }

    /**
     * @return the icon before the text (the link's graphic); may be {@code null}
     */
    public final Node getIcon() {
        return getGraphic();
    }

    public final void setIcon(Node icon) {
        setGraphic(icon);
    }
}
