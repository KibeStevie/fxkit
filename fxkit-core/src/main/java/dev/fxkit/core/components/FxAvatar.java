package dev.fxkit.core.components;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

/**
 * An avatar: a user's picture, initials or a placeholder silhouette, with an optional status dot
 * and optional content (name, e-mail, ...) beside it. Port of Flowbite React's {@code <Avatar>}.
 *
 * <pre>
 * FxAvatar (HBox, .fxk-avatar)
 * ├─ inner (StackPane, .fxk-avatar-inner)      fixed square, size comes from Size
 * │   ├─ frame (StackPane, .fxk-avatar-frame)  the ring + padding for bordered / stacked
 * │   │   └─ face (StackPane, .fxk-avatar-face)  clipped; holds ONE of: image | initials | placeholder
 * │   └─ status dot (Region, .fxk-avatar-status)
 * └─ content (optional Node, e.g. a name + subtitle)
 * </pre>
 *
 * <p>Which face is shown: an {@link #imageProperty() image} if it is set and loaded without error,
 * otherwise the {@link #placeholderInitialsProperty() initials} if non-blank, otherwise the
 * placeholder silhouette. A failed image load therefore falls back automatically.
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * img / alt            image / alt
 * rounded              rounded
 * bordered + color     bordered + color        (ring drawn outside the avatar, like Tailwind's ring-2)
 * placeholderInitials  placeholderInitials
 * status / statusPosition  status / statusPosition (default TOP_LEFT, as in Flowbite)
 * stacked              stacked                 (use inside {@link FxAvatarGroup})
 * size xs..xl          size XS..XL
 * children             content / setTexts(...)
 * </pre>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * FxAvatar a = new FxAvatar("/images/people/profile-picture-5.jpg");
 * a.setRounded(true);
 * a.setStatus(FxAvatar.Status.ONLINE);
 * a.setTexts("Jese Leos", "Joined in August 2014");
 * }</pre>
 */
public class FxAvatar extends HBox {

    /** Flowbite sizes: xs 24px, sm 32px, md 40px, lg 80px, xl 144px (pixel values live in components.css). */
    public enum Size {
        XS(24), SM(32), MD(40), LG(80), XL(144);

        private final double px;

        Size(double px) {
            this.px = px;
        }
    }

    /** Ring color for {@link #setBordered(boolean) bordered} avatars. Values live in colors.css. */
    public enum Color {
        INFO, CYAN, GRAY, FAILURE, RED, SUCCESS, GREEN, WARNING, YELLOW,
        INDIGO, PURPLE, PINK, BLUE, LIME, TEAL, DARK, LIGHT
    }

    /** Dot color: {@code ONLINE} success, {@code BUSY} danger, {@code AWAY} warning, {@code OFFLINE} muted. */
    public enum Status { ONLINE, BUSY, AWAY, OFFLINE }

    /** Where the dot sits. Every non-center edge pushes the dot 4px outside the avatar (Flowbite's {@code -top-1 -left-1}). */
    public enum StatusPosition {
        TOP_LEFT(Pos.TOP_LEFT, -1, -1),
        TOP_CENTER(Pos.TOP_CENTER, 0, -1),
        TOP_RIGHT(Pos.TOP_RIGHT, 1, -1),
        CENTER_LEFT(Pos.CENTER_LEFT, -1, 0),
        CENTER(Pos.CENTER, 0, 0),
        CENTER_RIGHT(Pos.CENTER_RIGHT, 1, 0),
        BOTTOM_LEFT(Pos.BOTTOM_LEFT, -1, 1),
        BOTTOM_CENTER(Pos.BOTTOM_CENTER, 0, 1),
        BOTTOM_RIGHT(Pos.BOTTOM_RIGHT, 1, 1);

        private final Pos alignment;
        private final int dx;
        private final int dy;

        StatusPosition(Pos alignment, int dx, int dy) {
            this.alignment = alignment;
            this.dx = dx;
            this.dy = dy;
        }
    }

    private static final double DOT_OVERHANG = 4; // Flowbite -top-1 etc.
    private static final double PLACEHOLDER_VIEWBOX = 20;

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.LIGHT);
    private final BooleanProperty rounded = new SimpleBooleanProperty(this, "rounded", false);
    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", false);
    private final BooleanProperty stacked = new SimpleBooleanProperty(this, "stacked", false);
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(this, "status");
    private final ObjectProperty<StatusPosition> statusPosition =
            new SimpleObjectProperty<>(this, "statusPosition", StatusPosition.TOP_LEFT);
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");
    private final StringProperty alt = new SimpleStringProperty(this, "alt");
    private final StringProperty placeholderInitials = new SimpleStringProperty(this, "placeholderInitials");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final StackPane inner = new StackPane();
    private final StackPane frame = new StackPane();
    private final StackPane face = new StackPane();
    private final Region statusDot = new Region();
    private final ImageView imageView = new ImageView();
    private final Label initialsLabel = new Label();
    private final Group placeholder = buildPlaceholder();

    /** Re-evaluates viewport and face whenever the current image loads, resizes or fails. */
    private final InvalidationListener imageStateListener = o -> {
        updateViewport();
        refreshFace();
    };

    public FxAvatar() {
        getStyleClass().add("fxk-avatar");
        inner.getStyleClass().add("fxk-avatar-inner");
        frame.getStyleClass().add("fxk-avatar-frame");
        face.getStyleClass().add("fxk-avatar-face");
        statusDot.getStyleClass().add("fxk-avatar-status");
        initialsLabel.getStyleClass().add("fxk-avatar-initials");

        // An ImageView is not resizable, so a StackPane reports the image's natural size (e.g. 200px)
        // as its MINIMUM size and refuses to shrink below it. Zero the minimums so the fixed-size
        // inner box below actually controls the layout.
        frame.setMinSize(0, 0);
        face.setMinSize(0, 0);
        frame.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        face.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        size.addListener(o -> applySize());
        applySize();

        EnumStyleClassSync.sync(this, "fxk-avatar-size-", size);
        EnumStyleClassSync.sync(this, "fxk-avatar-color-", color);
        BooleanStyleClassSync.sync(this, "fxk-avatar-rounded", rounded);
        BooleanStyleClassSync.sync(this, "fxk-avatar-bordered", bordered);
        BooleanStyleClassSync.sync(this, "fxk-avatar-stacked", stacked);
        EnumStyleClassSync.sync(statusDot, "fxk-avatar-status-", status);

        // ---- face: clip to the rounded shape, image fills it (cover) ----
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(face.widthProperty());
        clip.heightProperty().bind(face.heightProperty());
        InvalidationListener arcs = o -> {
            double arc = isRounded() ? Math.max(face.getWidth(), face.getHeight()) : 8; // 8 = 4px radius
            clip.setArcWidth(arc);
            clip.setArcHeight(arc);
        };
        face.widthProperty().addListener(arcs);
        face.heightProperty().addListener(arcs);
        rounded.addListener(arcs);
        face.setClip(clip);

        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(face.widthProperty());
        imageView.fitHeightProperty().bind(face.heightProperty());

        placeholder.scaleXProperty().bind(face.widthProperty().divide(PLACEHOLDER_VIEWBOX));
        placeholder.scaleYProperty().bind(face.widthProperty().divide(PLACEHOLDER_VIEWBOX));
        placeholder.translateYProperty().bind(face.heightProperty().multiply(0.1)); // sits low, like -bottom-1

        // ---- status dot ----
        statusDot.visibleProperty().bind(status.isNotNull());
        statusDot.managedProperty().bind(statusDot.visibleProperty());
        statusDot.setMouseTransparent(true);
        statusPosition.addListener((o, ov, nv) -> positionDot());
        positionDot();

        // ---- accessibility ----
        inner.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        alt.addListener((o, ov, nv) -> inner.setAccessibleText(nv));

        // ---- wiring ----
        frame.getChildren().setAll(face);
        inner.getChildren().setAll(frame, statusDot);
        getChildren().setAll(inner);

        image.addListener((o, oldImage, newImage) -> {
            if (oldImage != null) {
                oldImage.widthProperty().removeListener(imageStateListener);
                oldImage.heightProperty().removeListener(imageStateListener);
                oldImage.errorProperty().removeListener(imageStateListener);
            }
            imageView.setImage(newImage);
            if (newImage != null) {
                newImage.widthProperty().addListener(imageStateListener);
                newImage.heightProperty().addListener(imageStateListener);
                newImage.errorProperty().addListener(imageStateListener);
            }
            imageStateListener.invalidated(image);
        });
        placeholderInitials.addListener(o -> refreshFace());
        content.addListener((o, oldNode, newNode) -> {
            if (oldNode != null) {
                getChildren().remove(oldNode);
            }
            if (newNode != null) {
                getChildren().add(newNode);
            }
        });

        refreshFace();
    }

    /** @param imageUrl image URL or classpath resource, loaded in the background; may be {@code null} */
    public FxAvatar(String imageUrl) {
        this();
        setImage(imageUrl);
    }

    // ---- internals ------------------------------------------------------------------------

    private static Group buildPlaceholder() {
        // Flowbite's 20x20 "user" glyph: a head circle above a half-disc body.
        Rectangle bounds = new Rectangle(0, 0, PLACEHOLDER_VIEWBOX, PLACEHOLDER_VIEWBOX);
        bounds.setFill(javafx.scene.paint.Color.TRANSPARENT); // pins the group's layout bounds to the viewbox
        bounds.setMouseTransparent(true);

        Circle head = new Circle(10, 6, 3);
        Path body = new Path(new MoveTo(3, 18), new ArcTo(7, 7, 0, 17, 18, false, true), new ClosePath());
        head.getStyleClass().add("fxk-avatar-placeholder");
        body.getStyleClass().add("fxk-avatar-placeholder");
        body.setStroke(null);
        head.setStroke(null);

        Group group = new Group(bounds, head, body);
        group.setMouseTransparent(true);
        return group;
    }

    /** Center-crops the image to a square so a non-square photo covers the face instead of stretching. */
    private void updateViewport() {
        Image img = image.get();
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            imageView.setViewport(null);
            return;
        }
        double w = img.getWidth();
        double h = img.getHeight();
        double side = Math.min(w, h);
        imageView.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
    }

    private void refreshFace() {
        Image img = image.get();
        String initials = placeholderInitials.get();
        initialsLabel.setText(initials);

        Node shown;
        if (img != null && !img.isError()) {
            shown = imageView;
        } else if (initials != null && !initials.isBlank()) {
            shown = initialsLabel;
        } else {
            shown = placeholder;
        }
        if (face.getChildren().size() != 1 || face.getChildren().get(0) != shown) {
            face.getChildren().setAll(shown);
        }
    }

    /** Pins the inner square to the Size in pixels. Set from code so it never depends on CSS. */
    private void applySize() {
        Size s = getSize() == null ? Size.MD : getSize();
        inner.setMinSize(s.px, s.px);
        inner.setPrefSize(s.px, s.px);
        inner.setMaxSize(s.px, s.px);
    }

    private void positionDot() {
        StatusPosition p = getStatusPosition() == null ? StatusPosition.TOP_LEFT : getStatusPosition();
        StackPane.setAlignment(statusDot, p.alignment);
        statusDot.setTranslateX(p.dx * DOT_OVERHANG);
        statusDot.setTranslateY(p.dy * DOT_OVERHANG);
    }

    // ---- convenience ----------------------------------------------------------------------

    /**
     * Shows a name and a smaller, muted line beside the avatar (Flowbite's "Avatar with text"
     * example). Replaces any existing {@link #contentProperty() content}.
     *
     * @param title    the primary line, e.g. a user name
     * @param subtitle the secondary line, e.g. "Joined in August 2014"; may be {@code null}
     */
    public void setTexts(String title, String subtitle) {
        VBox box = new VBox();
        box.getStyleClass().add("fxk-avatar-texts");
        Label primary = new Label(title);
        primary.getStyleClass().add("fxk-avatar-title");
        box.getChildren().add(primary);
        if (subtitle != null && !subtitle.isBlank()) {
            Label secondary = new Label(subtitle);
            secondary.getStyleClass().add("fxk-avatar-subtitle");
            box.getChildren().add(secondary);
        }
        setContent(box);
    }

    /** Loads {@code url} in the background and shows it; {@code null} clears the image. */
    public final void setImage(String url) {
        setImage(url == null || url.isBlank() ? null : new Image(url, true));
    }

    // ---- properties -----------------------------------------------------------------------

    public final ObjectProperty<Size> sizeProperty() { return size; }
    public final Size getSize() { return size.get(); }
    public final void setSize(Size value) { size.set(value); }

    public final ObjectProperty<Color> colorProperty() { return color; }
    public final Color getColor() { return color.get(); }
    public final void setColor(Color value) { color.set(value); }

    public final BooleanProperty roundedProperty() { return rounded; }
    public final boolean isRounded() { return rounded.get(); }
    public final void setRounded(boolean value) { rounded.set(value); }

    public final BooleanProperty borderedProperty() { return bordered; }
    public final boolean isBordered() { return bordered.get(); }
    public final void setBordered(boolean value) { bordered.set(value); }

    public final BooleanProperty stackedProperty() { return stacked; }
    public final boolean isStacked() { return stacked.get(); }
    public final void setStacked(boolean value) { stacked.set(value); }

    public final ObjectProperty<Status> statusProperty() { return status; }
    public final Status getStatus() { return status.get(); }
    public final void setStatus(Status value) { status.set(value); }

    public final ObjectProperty<StatusPosition> statusPositionProperty() { return statusPosition; }
    public final StatusPosition getStatusPosition() { return statusPosition.get(); }
    public final void setStatusPosition(StatusPosition value) { statusPosition.set(value); }

    public final ObjectProperty<Image> imageProperty() { return image; }
    public final Image getImage() { return image.get(); }
    public final void setImage(Image value) { image.set(value); }

    /** Description of the image for assistive technology (Flowbite's {@code alt}). */
    public final StringProperty altProperty() { return alt; }
    public final String getAlt() { return alt.get(); }
    public final void setAlt(String value) { alt.set(value); }

    public final StringProperty placeholderInitialsProperty() { return placeholderInitials; }
    public final String getPlaceholderInitials() { return placeholderInitials.get(); }
    public final void setPlaceholderInitials(String value) { placeholderInitials.set(value); }

    /** Node shown to the right of the avatar (Flowbite's children). */
    public final ObjectProperty<Node> contentProperty() { return content; }
    public final Node getContent() { return content.get(); }
    public final void setContent(Node value) { content.set(value); }
}