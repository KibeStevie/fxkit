package dev.fxkit.core.components.alert;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A {@link VBox} styled by FXKit's design tokens, mirroring Flowbite React's {@code <Alert>}: a
 * typed {@link Color}, an optional {@link #iconProperty() icon}, an optional
 * {@link #onDismissProperty() close button}, and an optional {@link #borderAccentProperty() top
 * border accent}.
 *
 * <h2>Body content</h2>
 * By default the alert renders {@link #titleProperty() title} (bold) followed by
 * {@link #messageProperty() message} - the FXKit equivalent of Flowbite's
 * {@code <span className="font-medium">Title</span> message} pattern. Setting
 * {@link #contentProperty() content} to any {@link Node} overrides this entirely (title/message
 * are then ignored), for fully custom bodies.
 *
 * <h2>Dismissing</h2>
 * The close button only appears once {@link #onDismissProperty()} is set. Clicking it (or calling
 * {@link #dismiss()} directly, e.g. after a timeout) runs the callback and then removes the alert
 * from its parent's children, if any.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxAlert info = new FxAlert("Info alert!", "Change a few things up and try submitting again.");
 * info.setColor(FxAlert.Color.INFO);
 *
 * FxAlert failure = new FxAlert("Info alert!", "Change a few things up and try submitting again.");
 * failure.setColor(FxAlert.Color.FAILURE);
 * failure.setIcon(SomeIconPack.INFO_CIRCLE);
 *
 * FxAlert dismissible = new FxAlert("Info alert!", "Change a few things up and try submitting again.");
 * dismissible.setColor(FxAlert.Color.SUCCESS);
 * dismissible.setOnDismiss(() -> System.out.println("Alert dismissed!"));
 *
 * FxAlert accented = new FxAlert("Info alert!", "Change a few things up and try submitting again.");
 * accented.setColor(FxAlert.Color.WARNING);
 * accented.setBorderAccent(true);
 * }</pre>
 */
public class FxAlert extends VBox {

    public static final String STYLE_CLASS = "fxk-alert";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-alert-color-";
    private static final String BORDER_ACCENT_STYLE_CLASS = "fxk-alert-border-accent";
    private static final String WRAPPER_STYLE_CLASS = "fxk-alert-wrapper";
    private static final String ICON_STYLE_CLASS = "fxk-alert-icon";
    private static final String TEXT_STYLE_CLASS = "fxk-alert-text";
    private static final String TITLE_STYLE_CLASS = "fxk-alert-title";
    private static final String CLOSE_STYLE_CLASS = "fxk-alert-close";

    public static final Color DEFAULT_COLOR = Color.INFO;

    /**
     * A named color for an {@code FxAlert}, styled in {@code colors.css}. Several constants are
     * synonyms sharing one CSS rule (e.g. {@link #INFO}/{@link #CYAN}), matching Flowbite's own
     * theme map one-for-one.
     */
    public enum Color {
        INFO, GRAY, FAILURE, SUCCESS, WARNING, RED, GREEN, YELLOW,
        BLUE, CYAN, PINK, LIME, DARK, INDIGO, PURPLE, TEAL, LIGHT
    }

    private final ObjectProperty<Color> color =
            new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final StringProperty title = new SimpleStringProperty(this, "title");

    private final StringProperty message = new SimpleStringProperty(this, "message");

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    private final BooleanProperty borderAccent =
            new SimpleBooleanProperty(this, "borderAccent", false);

    private final ObjectProperty<Runnable> onDismiss = new SimpleObjectProperty<>(this, "onDismiss");

    private final HBox wrapper = new HBox();
    private FontIcon iconNode;
    private Button closeButton;

    public FxAlert() {
        initialize();
    }

    /** Convenience constructor equivalent to {@code new FxAlert().setMessage(message)}. */
    public FxAlert(String message) {
        this();
        setMessage(message);
    }

    /** Convenience constructor equivalent to setting both {@code title} and {@code message}. */
    public FxAlert(String title, String message) {
        this();
        setTitle(title);
        setMessage(message);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        wrapper.getStyleClass().add(WRAPPER_STYLE_CLASS);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(wrapper);

        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        BooleanStyleClassSync.sync(this, BORDER_ACCENT_STYLE_CLASS, borderAccent);

        icon.addListener((observable, oldValue, newValue) -> refresh());
        title.addListener((observable, oldValue, newValue) -> refresh());
        message.addListener((observable, oldValue, newValue) -> refresh());
        content.addListener((observable, oldValue, newValue) -> refresh());
        onDismiss.addListener((observable, oldValue, newValue) -> refresh());

        refresh();
    }

    /**
     * Rebuilds {@link #wrapper}'s children (icon, body, close button) from the current property
     * values. Simple full-rebuild on any relevant change - an alert's content changes rarely enough
     * that this isn't worth optimizing into targeted inserts/removals.
     */
    private void refresh() {
        wrapper.getChildren().clear();

        Ikon currentIcon = icon.get();
        if (currentIcon != null) {
            if (iconNode == null) {
                iconNode = new FontIcon();
                iconNode.getStyleClass().add(ICON_STYLE_CLASS);
            }
            iconNode.setIconCode(currentIcon);
            wrapper.getChildren().add(iconNode);
        }

        Node body = content.get() != null ? content.get() : buildDefaultBody();
        HBox.setHgrow(body, Priority.ALWAYS);
        wrapper.getChildren().add(body);

        if (onDismiss.get() != null) {
            if (closeButton == null) {
                closeButton = new Button("\u00D7"); // "x" close glyph; no icon-pack dependency
                closeButton.getStyleClass().add(CLOSE_STYLE_CLASS);
                closeButton.setOnAction(event -> dismiss());
            }
            wrapper.getChildren().add(closeButton);
        }
    }

    /** Builds the default title+message body, used whenever {@link #contentProperty()} is unset. */
    private Node buildDefaultBody() {
        String currentTitle = title.get();
        String currentMessage = message.get();

        TextFlow flow = new TextFlow();
        if (currentTitle != null && !currentTitle.isBlank()) {
            Text titleText = new Text(currentTitle);
            titleText.getStyleClass().addAll(TEXT_STYLE_CLASS, TITLE_STYLE_CLASS);
            flow.getChildren().add(titleText);

            if (currentMessage != null && !currentMessage.isBlank()) {
                Text messageText = new Text(" " + currentMessage);
                messageText.getStyleClass().add(TEXT_STYLE_CLASS);
                flow.getChildren().add(messageText);
            }
        } else if (currentMessage != null && !currentMessage.isBlank()) {
            Text messageText = new Text(currentMessage);
            messageText.getStyleClass().add(TEXT_STYLE_CLASS);
            flow.getChildren().add(messageText);
        }
        return flow;
    }

    /**
     * Runs {@link #getOnDismiss()} (if set), then removes this alert from its parent's children
     * (if its parent is a {@link Pane}). Called automatically when the close button is clicked;
     * also callable directly, e.g. to auto-dismiss an alert after a delay.
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

    public Ikon getIcon() { return icon.get(); }
    public void setIcon(Ikon icon) { this.icon.set(icon); }
    public ObjectProperty<Ikon> iconProperty() { return icon; }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    public String getMessage() { return message.get(); }
    public void setMessage(String message) { this.message.set(message); }
    public StringProperty messageProperty() { return message; }

    /**
     * @return the custom body node, if one was set ({@code null} means the default
     *         title/message-built body is shown instead)
     */
    public Node getContent() { return content.get(); }

    /**
     * Overrides the alert's body with an arbitrary node, ignoring {@code title}/{@code message}.
     * Pass {@code null} to go back to the default title/message body.
     *
     * @param content the node to show, or {@code null} to use the default body
     */
    public void setContent(Node content) { this.content.set(content); }
    public ObjectProperty<Node> contentProperty() { return content; }

    public boolean isBorderAccent() { return borderAccent.get(); }
    public void setBorderAccent(boolean borderAccent) { this.borderAccent.set(borderAccent); }
    public BooleanProperty borderAccentProperty() { return borderAccent; }

    /**
     * @return the dismiss callback, or {@code null} if the alert has no close button
     */
    public Runnable getOnDismiss() { return onDismiss.get(); }

    /**
     * Sets the dismiss callback and, as a side effect, whether the close button is shown at all -
     * pass {@code null} to hide it entirely.
     *
     * @param onDismiss run when the close button is clicked, before the alert removes itself from
     *                  its parent; {@code null} hides the close button
     */
    public void setOnDismiss(Runnable onDismiss) { this.onDismiss.set(onDismiss); }
    public ObjectProperty<Runnable> onDismissProperty() { return onDismiss; }
}
