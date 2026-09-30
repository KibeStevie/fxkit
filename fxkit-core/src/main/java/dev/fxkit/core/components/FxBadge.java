package dev.fxkit.core.components;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * An {@link HBox} styled by FXKit's design tokens, mirroring Flowbite React's {@code <Badge>}: a
 * typed {@link Color}, a {@link Size} ({@code xs}/{@code sm}), an optional
 * {@link #iconProperty() icon}, and - from Flowbite's "dismissible badges (chips)" - an optional
 * {@link #onDismissProperty() close button}.
 *
 * <h2>Text, icon, icon-only</h2>
 * A badge with {@link #textProperty() text} renders {@code [icon] text}. With an icon and no text
 * it becomes Flowbite's icon-only badge: fully rounded, uniform padding (the
 * {@code fxk-badge-icon-only} style class is toggled automatically).
 *
 * <h2>Dismissing</h2>
 * The close button only appears once {@link #onDismissProperty()} is set, and the badge switches
 * to the bordered "chip" look. Clicking it (or calling {@link #dismiss()}) runs the callback and
 * then removes the badge from its parent, if the parent is a {@link Pane}.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxBadge info = new FxBadge("Default");                       // Color.INFO, Size.XS
 * FxBadge failure = new FxBadge("Failure", FxBadge.Color.FAILURE);
 *
 * FxBadge withIcon = new FxBadge("2 minutes ago");
 * withIcon.setIcon(SomeIconPack.CHECK);
 *
 * FxBadge iconOnly = new FxBadge();
 * iconOnly.setColor(FxBadge.Color.GRAY);
 * iconOnly.setIcon(SomeIconPack.CHECK);
 *
 * FxBadge small = new FxBadge("Default");
 * small.setSize(FxBadge.Size.SM);
 *
 * FxBadge chip = new FxBadge("Brand", FxBadge.Color.BLUE);
 * chip.setOnDismiss(() -> System.out.println("removed"));
 * }</pre>
 */
public class FxBadge extends HBox {

    public static final String STYLE_CLASS = "fxk-badge";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-badge-color-";
    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-badge-size-";
    private static final String ICON_ONLY_STYLE_CLASS = "fxk-badge-icon-only";
    private static final String DISMISSIBLE_STYLE_CLASS = "fxk-badge-dismissible";
    private static final String TEXT_STYLE_CLASS = "fxk-badge-text";
    private static final String ICON_STYLE_CLASS = "fxk-badge-icon";
    private static final String CLOSE_STYLE_CLASS = "fxk-badge-close";

    public static final Color DEFAULT_COLOR = Color.INFO;
    public static final Size DEFAULT_SIZE = Size.XS;

    /**
     * A named color for an {@code FxBadge}, styled in {@code colors.css}. Several constants are
     * synonyms sharing one CSS rule (e.g. {@link #FAILURE}/{@link #RED}), matching Flowbite's own
     * theme map.
     */
    public enum Color {
        INFO, GRAY, FAILURE, SUCCESS, WARNING, INDIGO, PURPLE, PINK,
        BLUE, CYAN, DARK, LIGHT, GREEN, LIME, RED, TEAL, YELLOW
    }

    /** Flowbite's badge sizes; only the font size differs. */
    public enum Size { XS, SM }

    private final ObjectProperty<Color> color =
            new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final ObjectProperty<Size> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final StringProperty text = new SimpleStringProperty(this, "text");

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObjectProperty<Runnable> onDismiss = new SimpleObjectProperty<>(this, "onDismiss");

    private FontIcon iconNode;
    private Label textNode;
    private Button closeButton;

    public FxBadge() {
        initialize();
    }

    /** Convenience constructor equivalent to {@code new FxBadge().setText(text)}. */
    public FxBadge(String text) {
        this();
        setText(text);
    }

    /** Convenience constructor setting both text and color. */
    public FxBadge(String text, Color color) {
        this(text);
        setColor(color);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        // A badge hugs its content instead of stretching to fill a VBox/HBox slot (Flowbite: h-fit).
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);

        text.addListener((observable, oldValue, newValue) -> refresh());
        icon.addListener((observable, oldValue, newValue) -> refresh());
        onDismiss.addListener((observable, oldValue, newValue) -> refresh());

        refresh();
    }

    /**
     * Rebuilds the children (icon, text, close button) and the two layout-mode style classes from
     * the current property values. A badge changes rarely enough that a full rebuild is fine.
     */
    private void refresh() {
        getChildren().clear();

        Ikon currentIcon = icon.get();
        String currentText = text.get();
        boolean hasText = currentText != null && !currentText.isBlank();
        boolean dismissible = onDismiss.get() != null;

        if (currentIcon != null) {
            if (iconNode == null) {
                iconNode = new FontIcon();
                iconNode.getStyleClass().add(ICON_STYLE_CLASS);
            }
            iconNode.setIconCode(currentIcon);
            getChildren().add(iconNode);
        }

        if (hasText) {
            if (textNode == null) {
                textNode = new Label();
                textNode.getStyleClass().add(TEXT_STYLE_CLASS);
            }
            textNode.setText(currentText);
            getChildren().add(textNode);
        }

        if (dismissible) {
            if (closeButton == null) {
                closeButton = new Button("\u00D7"); // close glyph; no icon-pack dependency
                closeButton.getStyleClass().add(CLOSE_STYLE_CLASS);
                closeButton.setOnAction(event -> dismiss());
            }
            getChildren().add(closeButton);
        }

        setStyleClassPresent(ICON_ONLY_STYLE_CLASS, currentIcon != null && !hasText && !dismissible);
        setStyleClassPresent(DISMISSIBLE_STYLE_CLASS, dismissible);
    }

    private void setStyleClassPresent(String styleClass, boolean present) {
        if (present) {
            if (!getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        } else {
            getStyleClass().remove(styleClass);
        }
    }

    /**
     * Runs {@link #getOnDismiss()} (if set), then removes this badge from its parent's children
     * (if its parent is a {@link Pane}). Called automatically when the close button is clicked;
     * also callable directly.
     */
    public void dismiss() {
        Runnable callback = onDismiss.get();
        if (callback != null) {
            callback.run();
        }
        if (getParent() instanceof Pane parentPane) {
            parentPane.getChildren().remove(this);
        }
    }

    public Color getColor() { return color.get(); }
    public void setColor(Color color) { this.color.set(color); }
    public ObjectProperty<Color> colorProperty() { return color; }

    public Size getSize() { return size.get(); }
    public void setSize(Size size) { this.size.set(size); }
    public ObjectProperty<Size> sizeProperty() { return size; }

    public String getText() { return text.get(); }
    public void setText(String text) { this.text.set(text); }
    public StringProperty textProperty() { return text; }

    public Ikon getIcon() { return icon.get(); }
    public void setIcon(Ikon icon) { this.icon.set(icon); }
    public ObjectProperty<Ikon> iconProperty() { return icon; }

    /** @return the dismiss callback, or {@code null} if the badge has no close button */
    public Runnable getOnDismiss() { return onDismiss.get(); }

    /**
     * Sets the dismiss callback and, as a side effect, whether the close button (and chip border)
     * is shown at all - pass {@code null} to hide it.
     *
     * @param onDismiss run when the close button is clicked, before the badge removes itself from
     *                  its parent; {@code null} hides the close button
     */
    public void setOnDismiss(Runnable onDismiss) { this.onDismiss.set(onDismiss); }
    public ObjectProperty<Runnable> onDismissProperty() { return onDismiss; }
}
