package dev.fxkit.core.components.toast;

import java.util.ArrayList;
import java.util.List;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * A short message with an optional icon and close button, modelled on Flowbite
 * React's
 * {@code <Toast>}, {@code <ToastToggle>} and the "feedback toast" example.
 *
 * <pre>{@code
 * // Default toast: colored icon box, message, close button
 * FxToast toast = new FxToast(FontAwesomeSolid.FIRE, "Set yourself free.");
 * toast.setDismissible(true);
 *
 * // Colored
 * FxToast ok = new FxToast(FontAwesomeSolid.CHECK, "Item moved successfully.");
 * ok.setColor(FxToast.Color.SUCCESS);
 *
 * // Feedback toast: bare icon, no box, no close button
 * FxToast sent = new FxToast(FontAwesomeBrands.TELEGRAM_PLANE, "Message sent successfully.");
 * sent.setVariant(FxToast.Variant.FEEDBACK);
 * }</pre>
 *
 * <p>
 * FxToast is just the visual; it does not position itself on screen. Put it in
 * a
 * {@code VBox}/{@code StackPane} of your own. {@link #dismiss()} fades the
 * toast out, then hides it
 * and removes it from layout (Flowbite's {@code closed: opacity-0 ease-out});
 * remove it from its
 * parent in {@link #onDismissedProperty()} if you want it gone from the scene
 * graph.
 *
 * <p>
 * Styling: {@code components.css} (structure) and {@code colors.css} (per-color
 * values).
 */
public class FxToast extends HBox {

    /** Flowbite's two toast layouts. */
    public enum Variant {
        /** Icon inside a 32px colored rounded box (Flowbite's default toast). */
        DEFAULT,
        /** Bare colored icon, no box (Flowbite's "feedback toast"). */
        FEEDBACK
    }

    /**
     * Toast colors. Flowbite's cyan / green / red / orange, named by meaning like
     * FxAlert's.
     * WARNING uses the yellow scale because FXKit's palette has no orange.
     */
    public enum Color {
        INFO, SUCCESS, FAILURE, WARNING
    }

    private static final double CLOSE_OFFSET = -6; // Flowbite's -m-1.5

    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.DEFAULT);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.INFO);
    private final BooleanProperty dismissible = new SimpleBooleanProperty(this, "dismissible", false);
    private final ObjectProperty<Duration> autoDismiss = new SimpleObjectProperty<>(this, "autoDismiss");
    private final ReadOnlyBooleanWrapper closed = new ReadOnlyBooleanWrapper(this, "closed", false);
    private final ObjectProperty<EventHandler<ActionEvent>> onDismissed = new SimpleObjectProperty<>(this,
            "onDismissed");

    private final Label messageLabel = new Label();
    private final StackPane iconBox = new StackPane();
    private final Button closeButton = new Button();

    private Animation autoTimer;
    private FadeTransition fade;

    public FxToast() {
        this(null, "");
    }

    public FxToast(String text) {
        this(null, text);
    }

    public FxToast(Ikon icon, String text) {
        getStyleClass().add("fxk-toast");
        setAlignment(Pos.CENTER_LEFT);

        messageLabel.getStyleClass().add("fxk-toast-text");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.textProperty().bind(this.text);

        iconBox.getStyleClass().add("fxk-toast-icon-box");

        SVGPath cross = new SVGPath();
        cross.setContent("M0 0 L10 10 M10 0 L0 10");
        cross.getStyleClass().add("fxk-toast-close-icon");
        closeButton.getStyleClass().add("fxk-toast-close");
        closeButton.setGraphic(cross);
        closeButton.setAccessibleText("Close");
        closeButton.setOnAction(e -> dismiss());
        HBox.setMargin(closeButton, new Insets(CLOSE_OFFSET, CLOSE_OFFSET, CLOSE_OFFSET, 0));

        EnumStyleClassSync.sync(this, "fxk-toast-", variant);
        EnumStyleClassSync.sync(this, "fxk-toast-color-", color);

        this.text.set(text == null ? "" : text);
        this.icon.set(icon);

        this.icon.addListener((o, a, b) -> refresh());
        content.addListener((o, a, b) -> refresh());
        dismissible.addListener((o, a, b) -> refresh());

        autoDismiss.addListener((o, a, b) -> restartAutoDismiss());
        sceneProperty().addListener((o, a, b) -> restartAutoDismiss());
        hoverProperty().addListener((o, wasHover, isHover) -> {
            if (autoTimer == null)
                return;
            if (isHover) {
                autoTimer.pause(); // don't vanish while the pointer is on the toast
            } else if (autoTimer.getStatus() == Animation.Status.PAUSED) {
                autoTimer.play();
            }
        });

        refresh();
    }

    // ---- structure
    // ------------------------------------------------------------------------

    private void refresh() {
        List<Node> kids = new ArrayList<>();

        Ikon ikon = icon.get();
        if (ikon != null) {
            FontIcon glyph = new FontIcon(ikon);
            glyph.getStyleClass().add("fxk-toast-icon");
            iconBox.getChildren().setAll(glyph);
            kids.add(iconBox);
        }

        Node body = content.get() != null ? content.get() : messageLabel;
        HBox.setHgrow(body, Priority.ALWAYS); // Flowbite's ml-auto: pushes the close button to the end
        kids.add(body);

        if (dismissible.get()) {
            kids.add(closeButton);
        }
        getChildren().setAll(kids);
    }

    // ---- dismissing
    // -----------------------------------------------------------------------

    /**
     * Fades the toast out (300ms ease-out), then hides it, removes it from layout,
     * sets
     * {@link #closedProperty()} and fires {@link #onDismissedProperty()}. Does
     * nothing if the toast is
     * already closed or fading. Call on the JavaFX Application Thread.
     */
    public void dismiss() {
        if (closed.get() || fade != null)
            return;
        if (autoTimer != null)
            autoTimer.stop();

        fade = new FadeTransition(Duration.millis(300), this);
        fade.setToValue(0);
        fade.setInterpolator(Interpolator.EASE_OUT);
        fade.setOnFinished(e -> {
            fade = null;
            setVisible(false);
            setManaged(false);
            closed.set(true);
            EventHandler<ActionEvent> handler = onDismissed.get();
            if (handler != null)
                handler.handle(new ActionEvent(this, this));
        });
        fade.play();
    }

    private void restartAutoDismiss() {
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
        Duration after = autoDismiss.get();
        if (after == null || closed.get() || fade != null || getScene() == null)
            return;

        PauseTransition timer = new PauseTransition(after);
        timer.setOnFinished(e -> dismiss());
        autoTimer = timer;
        timer.play();
    }

    // ---- properties
    // -----------------------------------------------------------------------

    /** The message. Ignored while {@link #contentProperty() content} is set. */
    public StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String value) {
        text.set(value == null ? "" : value);
    }

    /**
     * Custom body that replaces the text label (Flowbite's arbitrary
     * {@code children}).
     */
    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node value) {
        content.set(value);
    }

    /** Icon shown at the start; {@code null} for none. */
    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public Ikon getIcon() {
        return icon.get();
    }

    public void setIcon(Ikon value) {
        icon.set(value);
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Variant getVariant() {
        return variant.get();
    }

    public void setVariant(Variant value) {
        variant.set(value);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color value) {
        color.set(value);
    }

    /**
     * Shows the close button (Flowbite's {@code <ToastToggle />}). Default
     * {@code false}.
     */
    public BooleanProperty dismissibleProperty() {
        return dismissible;
    }

    public boolean isDismissible() {
        return dismissible.get();
    }

    public void setDismissible(boolean value) {
        dismissible.set(value);
    }

    /**
     * If set, the toast dismisses itself this long after it is added to a scene.
     * The countdown
     * pauses while the pointer is over the toast. {@code null} (default) means it
     * stays until
     * {@link #dismiss()}.
     */
    public ObjectProperty<Duration> autoDismissProperty() {
        return autoDismiss;
    }

    public Duration getAutoDismiss() {
        return autoDismiss.get();
    }

    public void setAutoDismiss(Duration value) {
        autoDismiss.set(value);
    }

    /** {@code true} once the dismiss fade has finished. */
    public ReadOnlyBooleanProperty closedProperty() {
        return closed.getReadOnlyProperty();
    }

    public boolean isClosed() {
        return closed.get();
    }

    /** Called after the toast has faded out and been hidden. */
    public ObjectProperty<EventHandler<ActionEvent>> onDismissedProperty() {
        return onDismissed;
    }

    public EventHandler<ActionEvent> getOnDismissed() {
        return onDismissed.get();
    }

    public void setOnDismissed(EventHandler<ActionEvent> value) {
        onDismissed.set(value);
    }
}
