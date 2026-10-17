package dev.fxkit.core.components.clipboard;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * A button that copies a string to the system clipboard and briefly confirms
 * it. Port of
 * flowbite-react's {@code Clipboard}, {@code ClipboardWithIcon} and
 * {@code ClipboardWithIconText},
 * which are one component here, selected by {@link Variant}:
 *
 * <ul>
 * <li>{@link Variant#DEFAULT}: a solid, full-width button with a text label.
 * The label changes to
 * {@link #copiedLabelProperty() copiedLabel} after copying (Flowbite's
 * {@code Clipboard}).</li>
 * <li>{@link Variant#ICON}: a small icon-only button meant to sit inside an
 * input. The icon turns
 * into a check mark, and a tooltip says "Copied" (Flowbite's
 * {@code ClipboardWithIcon}).</li>
 * <li>{@link Variant#ICON_TEXT}: an outlined button with icon and label, meant
 * to sit inside an
 * input. Icon and label both switch to the success state (Flowbite's
 * {@code ClipboardWithIconText}).</li>
 * </ul>
 *
 * <p>
 * After a copy, {@link #copiedProperty() copied} is {@code true} for
 * {@link #resetDelayProperty()
 * resetDelay} (2 seconds by default), then goes back to {@code false}. Copying
 * again while copied
 * restarts that timer.
 *
 * <h2>Placing it inside an input</h2>
 * Flowbite positions the icon variants with {@code absolute end-2 top-1/2}.
 * Here,
 * {@link #overlay(Region, FxClipboard)} does the same with a {@link StackPane}:
 * 
 * <pre>{@code
 * FxClipboard copy = new FxClipboard("npm install fxkit");
 * copy.setVariant(FxClipboard.Variant.ICON);
 * StackPane host = FxClipboard.overlay(field, copy);
 * }</pre>
 *
 * <h2>Sizing</h2>
 * {@code DEFAULT} sets its max width and height to {@code Double.MAX_VALUE} so
 * it fills its grid
 * cell like Flowbite's {@code w-full}; the icon variants stay at their
 * preferred size. The variant
 * sets these limits itself, so set your own max size after changing the
 * variant.
 *
 * <h2>Style classes</h2>
 * {@code fxk-clip},
 * {@code fxk-clip-default | fxk-clip-icon | fxk-clip-icon-text} and, while
 * copied, {@code fxk-clip-copied} (see {@code components.css}).
 *
 * <p>
 * Like all scene-graph access, call these methods on the JavaFX Application
 * Thread.
 */
public class FxClipboard extends Button {

    /** Which of Flowbite's three clipboard buttons this is. */
    public enum Variant {
        /** Solid, full-width text button. */
        DEFAULT,
        /** Icon-only button for use inside an input; shows a tooltip. */
        ICON,
        /** Outlined icon + text button for use inside an input. */
        ICON_TEXT
    }

    public static final String DEFAULT_LABEL = "Copy";
    public static final String DEFAULT_COPIED_LABEL = "Copied";
    public static final String DEFAULT_TOOLTIP_TEXT = "Copy to clipboard";
    public static final String DEFAULT_COPIED_TOOLTIP_TEXT = "Copied";
    public static final Duration DEFAULT_RESET_DELAY = Duration.seconds(2);

    // Flowbite's icons (viewBox 18x20 and 16x12), drawn as SVGPath so the module
    // needs no icon pack.
    private static final String CLIPBOARD_PATH = "M16 1h-3.278A1.992 1.992 0 0 0 11 0H7a1.993 1.993 0 0 0-1.722 1H2a2 2 0 0 0-2 2v15a2 2 0 0 0 "
            + "2 2h14a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2Zm-3 14H5a1 1 0 0 1 0-2h8a1 1 0 0 1 0 2Zm0-4H5a1 1 0 0 "
            + "1 0-2h8a1 1 0 1 1 0 2Zm0-5H5a1 1 0 0 1 0-2h2V2h4v2h2a1 1 0 1 1 0 2Z";
    private static final String CHECK_PATH = "M1 5.917 L5.724 10.5 L15 1.5";

    private final StringProperty valueToCopy = new SimpleStringProperty(this, "valueToCopy", "");
    private final StringProperty label = new SimpleStringProperty(this, "label", DEFAULT_LABEL);
    private final StringProperty copiedLabel = new SimpleStringProperty(this, "copiedLabel", DEFAULT_COPIED_LABEL);
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.DEFAULT);
    private final ObjectProperty<Duration> resetDelay = new SimpleObjectProperty<>(this, "resetDelay",
            DEFAULT_RESET_DELAY);
    private final StringProperty tooltipText = new SimpleStringProperty(this, "tooltipText", DEFAULT_TOOLTIP_TEXT);
    private final StringProperty copiedTooltipText = new SimpleStringProperty(this, "copiedTooltipText",
            DEFAULT_COPIED_TOOLTIP_TEXT);
    private final ObjectProperty<Runnable> onCopy = new SimpleObjectProperty<>(this, "onCopy");
    private final ReadOnlyBooleanWrapper copied = new ReadOnlyBooleanWrapper(this, "copied", false);

    private final PauseTransition resetTimer = new PauseTransition(DEFAULT_RESET_DELAY);

    // ICON variant's tooltip: a Popup, created on first use.
    private Popup tipPopup;
    private StackPane tipRoot;
    private Label tipLabel;

    public FxClipboard() {
        this("", DEFAULT_LABEL);
    }

    /**
     * @param valueToCopy the text placed on the clipboard when the button is
     *                    clicked
     */
    public FxClipboard(String valueToCopy) {
        this(valueToCopy, DEFAULT_LABEL);
    }

    /**
     * @param valueToCopy the text placed on the clipboard when the button is
     *                    clicked
     * @param label       the button text before copying ({@code DEFAULT} and
     *                    {@code ICON_TEXT})
     */
    public FxClipboard(String valueToCopy, String label) {
        setValueToCopy(valueToCopy);
        setLabel(label);

        getStyleClass().add("fxk-clip");
        EnumStyleClassSync.sync(this, "fxk-clip-", variant);
        BooleanStyleClassSync.sync(this, "fxk-clip-copied", copied.getReadOnlyProperty());

        setMinWidth(USE_PREF_SIZE);
        resetTimer.setOnFinished(e -> copied.set(false));

        this.label.addListener((o, was, is) -> refresh());
        this.copiedLabel.addListener((o, was, is) -> refresh());
        this.variant.addListener((o, was, is) -> {
            applyVariantSizing();
            refresh();
        });
        this.tooltipText.addListener((o, was, is) -> refresh());
        this.copiedTooltipText.addListener((o, was, is) -> refresh());
        this.copied.addListener((o, was, is) -> refresh());
        applyVariantSizing();
        refresh();

        initTooltip();
    }

    // ---- behavior
    // ---------------------------------------------------------------------------

    /**
     * Copies, then runs the regular {@code onAction} handler. Does nothing while
     * disabled.
     */
    @Override
    public void fire() {
        if (!isDisabled()) {
            copy();
            super.fire();
        }
    }

    /**
     * Puts {@link #getValueToCopy() valueToCopy} ({@code ""} if {@code null}) on
     * the system
     * clipboard, switches to the copied state and (re)starts the reset timer. Works
     * even if the
     * button is disabled, so code can copy on its own.
     */
    public void copy() {
        String value = getValueToCopy();
        ClipboardContent content = new ClipboardContent();
        content.putString(value == null ? "" : value);
        Clipboard.getSystemClipboard().setContent(content);

        copied.set(true);
        resetTimer.stop();
        Duration delay = getResetDelay() == null ? DEFAULT_RESET_DELAY : getResetDelay();
        if (!delay.isIndefinite() && !delay.isUnknown()) {
            resetTimer.setDuration(delay.lessThan(Duration.ZERO) ? Duration.ZERO : delay);
            resetTimer.playFromStart();
        }

        Runnable callback = getOnCopy();
        if (callback != null) {
            callback.run();
        }
    }

    /** Leaves the copied state right away (cancels the reset timer). */
    public void reset() {
        resetTimer.stop();
        copied.set(false);
    }

    // ---- layout helper
    // ----------------------------------------------------------------------

    /**
     * Stacks {@code clipboard} on the right end of {@code field}, like Flowbite's
     * {@code absolute end-2 top-1/2} (8px from the edge for {@code ICON}, 10px for
     * {@code ICON_TEXT}), and returns the {@link StackPane} holding both. Meant for
     * the two icon
     * variants.
     *
     * @param field     the input to sit on
     * @param clipboard the button to place inside it
     * @return the stack holding both nodes
     */
    public static StackPane overlay(Region field, FxClipboard clipboard) {
        StackPane pane = new StackPane(field, clipboard);
        StackPane.setAlignment(clipboard, Pos.CENTER_RIGHT);
        Runnable inset = () -> StackPane.setMargin(clipboard,
                new Insets(0, clipboard.getVariant() == Variant.ICON_TEXT ? 10 : 8, 0, 0));
        inset.run();
        clipboard.variantProperty().addListener((o, was, is) -> inset.run());
        return pane;
    }

    // ---- content
    // ----------------------------------------------------------------------------

    private Variant variantOrDefault() {
        return getVariant() == null ? Variant.DEFAULT : getVariant();
    }

    /**
     * Only runs when the variant changes (not on every refresh), so a max size the
     * caller sets
     * afterwards survives copying.
     */
    private void applyVariantSizing() {
        if (variantOrDefault() == Variant.DEFAULT) {
            setMaxWidth(Double.MAX_VALUE); // w-full
            setMaxHeight(Double.MAX_VALUE);
        } else {
            setMaxWidth(USE_PREF_SIZE);
            setMaxHeight(USE_PREF_SIZE);
        }
    }

    private void refresh() {
        boolean done = isCopied();
        String text = done ? getCopiedLabel() : getLabel();
        switch (variantOrDefault()) {
            case DEFAULT -> {
                setText(text);
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                setAccessibleText(null);
            }
            case ICON -> {
                setText(null);
                setGraphic(glyph(done, 16)); // h-4 w-4
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAccessibleText(done ? getCopiedTooltipText() : getTooltipText());
            }
            case ICON_TEXT -> {
                setText(text);
                setGraphic(glyph(done, 12)); // h-3 w-3
                setContentDisplay(ContentDisplay.LEFT);
                setAccessibleText(null);
            }
        }
    }

    /**
     * The clipboard icon, or the check mark when {@code success}, fitted into a
     * size x size box.
     */
    private static Node glyph(boolean success, double size) {
        double w = success ? 16 : 18;
        double h = success ? 12 : 20;

        SVGPath path = new SVGPath();
        path.setContent(success ? CHECK_PATH : CLIPBOARD_PATH);
        path.getStyleClass().add(success ? "fxk-clip-glyph-success" : "fxk-clip-glyph-default");

        // A canvas the size of the SVG viewBox, scaled down like an <svg> with h-4 w-4.
        Pane canvas = new Pane(path);
        canvas.setMinSize(w, h);
        canvas.setPrefSize(w, h);
        canvas.setMaxSize(w, h);
        double scale = size / Math.max(w, h);
        canvas.setScaleX(scale);
        canvas.setScaleY(scale);

        StackPane box = new StackPane(new Group(canvas));
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        return box;
    }

    // ---- tooltip (ICON variant)
    // -------------------------------------------------------------

    private void initTooltip() {
        hoverProperty().addListener((o, was, is) -> updateTip());
        disabledProperty().addListener((o, was, is) -> updateTip());
        copied.addListener((o, was, is) -> updateTip());
        variant.addListener((o, was, is) -> updateTip());
        tooltipText.addListener((o, was, is) -> updateTip());
        copiedTooltipText.addListener((o, was, is) -> updateTip());
        sceneProperty().addListener((o, was, is) -> updateTip());
    }

    /**
     * Hover text while idle, copied text while copied; nothing for the other
     * variants.
     */
    private void updateTip() {
        Scene scene = getScene();
        String text = null;
        if (variantOrDefault() == Variant.ICON && !isDisabled() && scene != null
                && scene.getWindow() != null && scene.getWindow().isShowing()) {
            text = isCopied() ? getCopiedTooltipText() : (isHover() ? getTooltipText() : null);
        }
        if (text == null || text.isEmpty()) {
            hideTip();
        } else {
            showTip(text);
        }
    }

    private void showTip(String text) {
        Scene scene = getScene();
        Window owner = scene.getWindow();
        if (tipPopup == null) {
            buildTip();
        }
        tipLabel.setText(text);

        // The popup is a separate window: give it the owner's stylesheets and theme.
        if (!tipPopup.getScene().getStylesheets().equals(scene.getStylesheets())) {
            tipPopup.getScene().getStylesheets().setAll(scene.getStylesheets());
        }
        boolean dark = ThemeManager.current(scene) == Theme.DARK;
        tipRoot.getStyleClass().remove(ThemeManager.DARK_CLASS);
        if (dark) {
            tipRoot.getStyleClass().add(ThemeManager.DARK_CLASS);
        }

        if (!tipPopup.isShowing()) {
            Bounds bounds = localToScreen(getLayoutBounds());
            if (bounds == null) {
                return;
            }
            tipPopup.show(owner, bounds.getMinX(), bounds.getMinY());
        }
        Platform.runLater(this::positionTip);
    }

    private void hideTip() {
        if (tipPopup != null && tipPopup.isShowing()) {
            tipPopup.hide();
        }
    }

    private void buildTip() {
        tipLabel = new Label();
        tipLabel.getStyleClass().add("fxk-clip-tip-text");

        StackPane tip = new StackPane(tipLabel);
        tip.getStyleClass().add("fxk-clip-tip");

        // "root" so the -fxk-* tokens resolve on the popup's own content, like on a
        // Scene root.
        tipRoot = new StackPane(tip);
        tipRoot.getStyleClass().addAll("root", "fxk-clip-tip-popup");
        tipRoot.setMouseTransparent(true);
        tipRoot.layoutBoundsProperty().addListener((o, was, is) -> Platform.runLater(this::positionTip));

        tipPopup = new Popup();
        tipPopup.setAutoHide(false);
        tipPopup.setHideOnEscape(false);
        tipPopup.getContent().add(tipRoot);
        tipPopup.setOnShown(e -> positionTip());
    }

    /**
     * Centers the tooltip above the button. The popup's own 6px padding is the
     * visual gap.
     */
    private void positionTip() {
        if (tipPopup == null || !tipPopup.isShowing() || getScene() == null) {
            return;
        }
        Bounds bounds = localToScreen(getLayoutBounds());
        double width = tipPopup.getWidth();
        double height = tipPopup.getHeight();
        if (bounds == null || Double.isNaN(width) || Double.isNaN(height)) {
            return;
        }
        tipPopup.setX(bounds.getMinX() + bounds.getWidth() / 2 - width / 2);
        tipPopup.setY(bounds.getMinY() - height);
    }

    // ---- properties
    // -------------------------------------------------------------------------

    /** The text placed on the clipboard. {@code null} copies an empty string. */
    public final StringProperty valueToCopyProperty() {
        return valueToCopy;
    }

    public final String getValueToCopy() {
        return valueToCopy.get();
    }

    public final void setValueToCopy(String value) {
        valueToCopy.set(value);
    }

    /**
     * Button text before copying ({@code DEFAULT} and {@code ICON_TEXT}). Default
     * {@code "Copy"}.
     */
    public final StringProperty labelProperty() {
        return label;
    }

    public final String getLabel() {
        return label.get();
    }

    public final void setLabel(String value) {
        label.set(value);
    }

    /**
     * Button text while copied ({@code DEFAULT} and {@code ICON_TEXT}). Default
     * {@code "Copied"}.
     */
    public final StringProperty copiedLabelProperty() {
        return copiedLabel;
    }

    public final String getCopiedLabel() {
        return copiedLabel.get();
    }

    public final void setCopiedLabel(String value) {
        copiedLabel.set(value);
    }

    /** Which button this is. Default {@link Variant#DEFAULT}. */
    public final ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public final Variant getVariant() {
        return variant.get();
    }

    public final void setVariant(Variant value) {
        variant.set(value);
    }

    /**
     * How long the copied state lasts. Default 2 seconds.
     * {@link Duration#INDEFINITE} keeps it until
     * {@link #reset()}; {@code null} means the default.
     */
    public final ObjectProperty<Duration> resetDelayProperty() {
        return resetDelay;
    }

    public final Duration getResetDelay() {
        return resetDelay.get();
    }

    public final void setResetDelay(Duration value) {
        resetDelay.set(value);
    }

    /**
     * {@code ICON} tooltip while hovering. {@code null} or empty: none. Default
     * "Copy to clipboard".
     */
    public final StringProperty tooltipTextProperty() {
        return tooltipText;
    }

    public final String getTooltipText() {
        return tooltipText.get();
    }

    public final void setTooltipText(String value) {
        tooltipText.set(value);
    }

    /**
     * {@code ICON} tooltip while copied. {@code null} or empty: none. Default
     * "Copied".
     */
    public final StringProperty copiedTooltipTextProperty() {
        return copiedTooltipText;
    }

    public final String getCopiedTooltipText() {
        return copiedTooltipText.get();
    }

    public final void setCopiedTooltipText(String value) {
        copiedTooltipText.set(value);
    }

    /** Runs after every copy, once the clipboard has been written. */
    public final ObjectProperty<Runnable> onCopyProperty() {
        return onCopy;
    }

    public final Runnable getOnCopy() {
        return onCopy.get();
    }

    public final void setOnCopy(Runnable value) {
        onCopy.set(value);
    }

    /** {@code true} from a copy until the reset delay has passed. */
    public final ReadOnlyBooleanProperty copiedProperty() {
        return copied.getReadOnlyProperty();
    }

    public final boolean isCopied() {
        return copied.get();
    }
}
