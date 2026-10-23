package dev.fxkit.core.components.card;

import dev.fxkit.core.internal.ImageSources;
import javafx.beans.InvalidationListener;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * The picture slot of an {@link FxCard}: Flowbite's {@code img} part ({@code rounded-t-lg}, or
 * {@code h-auto w-48 object-cover rounded-l-lg} when the card is horizontal).
 *
 * <p>JavaFX CSS cannot express "height from the aspect ratio" or "object-fit: cover", so the geometry
 * is done here, the same way {@code FxSpinner} sets its own size; {@code components.css} only paints
 * the placeholder background.
 *
 * <ul>
 *   <li><b>Vertical</b>: the picture is as wide as the card and as tall as its aspect ratio says;
 *       only the top corners are rounded.</li>
 *   <li><b>Horizontal</b>: the frame is {@link #HORIZONTAL_WIDTH} wide and as tall as the card; the
 *       picture covers it (centered crop, no distortion); only the left corners are rounded.</li>
 * </ul>
 *
 * <p>The content is normally an {@link ImageView}. Any other node (from
 * {@link FxCard#renderImageProperty()}) is resized to fill the frame when it is resizable.
 *
 * <p>Not part of FXKit's public API.
 */
final class CardImage extends Region {

    /** Flowbite's {@code md:w-48}. */
    static final double HORIZONTAL_WIDTH = 192;

    /** The card's 8px radius minus its 1px border, so the picture sits flush inside the border. */
    private static final double RADIUS = 7;

    private final Rectangle clip = new Rectangle();
    private final InvalidationListener relayout = observable -> requestLayout();

    private Node content;
    private Image watched;
    private boolean horizontal;

    CardImage() {
        getStyleClass().add("fxk-card-image");
        setClip(clip);
    }

    Node getContent() {
        return content;
    }

    void setContent(Node node) {
        if (content == node) {
            return;
        }
        unwatch();
        if (content != null) {
            getChildren().remove(content);
        }
        content = node;
        if (node != null) {
            if (node instanceof ImageView view) {
                // Fitted and placed in layoutChildren(); its own size changes must not trigger a
                // layout pass of the card (that would loop).
                view.setManaged(false);
                watch(view.getImage());
            }
            getChildren().add(node);
        }
        requestLayout();
    }

    void setHorizontal(boolean horizontal) {
        if (this.horizontal != horizontal) {
            this.horizontal = horizontal;
            requestLayout();
        }
    }

    /** A background-loaded image has no size yet; the layout must run again once it has one. */
    private void watch(Image image) {
        watched = image;
        if (image != null) {
            image.widthProperty().addListener(relayout);
            image.heightProperty().addListener(relayout);
        }
    }

    private void unwatch() {
        if (watched != null) {
            watched.widthProperty().removeListener(relayout);
            watched.heightProperty().removeListener(relayout);
            watched = null;
        }
    }

    // ---- sizing ----------------------------------------------------------------------------

    @Override
    public Orientation getContentBias() {
        return horizontal ? null : Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        return horizontal ? HORIZONTAL_WIDTH : naturalWidth();
    }

    @Override
    protected double computePrefHeight(double width) {
        if (content == null) {
            return 0;
        }
        double w = horizontal ? HORIZONTAL_WIDTH : (width > 0 ? width : naturalWidth());
        if (content instanceof ImageView view) {
            Image image = view.getImage();
            if (image == null || !(image.getWidth() > 0) || !(image.getHeight() > 0)) {
                return 0;
            }
            return Math.ceil(w * image.getHeight() / image.getWidth());
        }
        return content.prefHeight(w);
    }

    @Override
    protected double computeMinWidth(double height) {
        return horizontal ? HORIZONTAL_WIDTH : 0;
    }

    @Override
    protected double computeMaxWidth(double height) {
        return horizontal ? HORIZONTAL_WIDTH : Double.MAX_VALUE;
    }

    private double naturalWidth() {
        if (content instanceof ImageView view) {
            Image image = view.getImage();
            return image == null ? 0 : image.getWidth();
        }
        return content == null ? 0 : content.prefWidth(-1);
    }

    // ---- layout ----------------------------------------------------------------------------

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();

        // A rectangle rounded on all four corners, grown past the edges that must stay square:
        // those corners fall outside the frame and are never seen.
        clip.setWidth(horizontal ? w + RADIUS : w);
        clip.setHeight(horizontal ? h : h + RADIUS);
        clip.setArcWidth(2 * RADIUS);
        clip.setArcHeight(2 * RADIUS);

        if (content == null || !(w > 0) || !(h > 0)) {
            return;
        }
        if (content instanceof ImageView view) {
            fit(view, w, h);
        } else if (content.isResizable()) {
            content.resizeRelocate(0, 0, w, h);
        } else {
            content.relocate(0, 0);
        }
    }

    private void fit(ImageView view, double w, double h) {
        Image image = view.getImage();
        if (image == null) {
            return;
        }
        if (horizontal) {
            // object-cover: fill the box, crop the overflow equally on both sides
            ImageSources.Crop crop = ImageSources.coverCrop(image.getWidth(), image.getHeight(), w, h);
            view.setPreserveRatio(false);
            view.setFitWidth(w);
            view.setFitHeight(h);
            view.setViewport(crop == null ? null : new Rectangle2D(crop.x(), crop.y(), crop.width(), crop.height()));
        } else {
            // h-auto: as wide as the card, as tall as the aspect ratio says
            view.setViewport(null);
            view.setPreserveRatio(true);
            view.setFitWidth(w);
            view.setFitHeight(0);
        }
        view.relocate(0, 0);
    }
}
