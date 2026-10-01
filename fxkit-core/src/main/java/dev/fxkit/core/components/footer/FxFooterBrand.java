package dev.fxkit.core.components.footer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Flowbite's {@code <FooterBrand src="..." alt="..." name="..." href="...">}: a logo followed by the
 * brand name in large, semibold text. The whole brand is clickable ({@link #setOnAction}).
 *
 * <p><b>Logo.</b> {@link #setImage(Image)} shows a raster image (PNG, JPEG, GIF) scaled to 32px high
 * ({@code h-8}), keeping its aspect ratio. JavaFX's {@code Image} cannot read SVG, so Flowbite's
 * {@code logo.svg} will not load: use a PNG, or pass any node - an Ikonli {@code FontIcon}, an
 * {@code SVGPath} - to {@link #setGraphic(javafx.scene.Node)}. A {@code FontIcon} is sized (32px) and
 * colored by the stylesheet.
 *
 * <p>Style class {@code fxk-footer-brand}. Flowbite's {@code mb-4 sm:mb-0} (a mobile-only margin) is
 * not modelled.
 */
public class FxFooterBrand extends Hyperlink {

    private static final double LOGO_HEIGHT = 32; // h-8

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");

    public FxFooterBrand(String name) {
        this(name, null);
    }

    public FxFooterBrand(String name, Image image) {
        super(name);
        getStyleClass().add("fxk-footer-brand");
        setContentDisplay(ContentDisplay.LEFT);
        this.image.addListener((o, oldImage, newImage) -> setGraphic(newImage == null ? null : logo(newImage)));
        setImage(image);
    }

    /** The brand name (Flowbite's {@code name}); the same as {@link #textProperty()}. */
    public final StringProperty nameProperty() { return textProperty(); }
    public final String getName() { return getText(); }
    public final void setName(String value) { setText(value); }

    public final ObjectProperty<Image> imageProperty() { return image; }
    public final Image getImage() { return image.get(); }
    public final void setImage(Image value) { image.set(value); }

    private static ImageView logo(Image image) {
        ImageView view = new ImageView(image);
        view.setFitHeight(LOGO_HEIGHT);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }
}
