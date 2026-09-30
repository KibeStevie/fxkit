package dev.fxkit.core.components.button;

import java.util.Locale;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.components.spinner.FxSpinner;
import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.ClassNameStyler;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;

/**
 * A {@link Button} styled by FXKit's design tokens, with typed {@link Variant},
 * {@link Color} and
 * {@link Size} properties so it can be styled without writing CSS, plus a
 * {@link #classNameProperty()}
 * escape hatch for anything those don't cover.
 *
 * <p>
 * {@code FxButton} is a plain subclass of {@code Button}: every standard
 * {@code Button}/
 * {@code ButtonBase} API keeps working exactly as it does today.
 *
 * <h2>Variant, Color and Size</h2>
 * Each of {@link #variantProperty()}, {@link #colorProperty()} and
 * {@link #sizeProperty()} is kept in
 * sync with a style class via {@link EnumStyleClassSync}. {@link Color} has a
 * visual effect under two
 * variants only - {@link Variant#DEFAULT} (solid fill) and
 * {@link Variant#OUTLINE} (border + text,
 * filling solid on hover, Flowbite React's
 * {@code <Button outline color="...">}) - both styled in
 * {@code colors.css}. Every other variant (PRIMARY, DANGER, ...) pins its own
 * colors from semantic
 * tokens in {@code components.css} and never carries a {@code fxk-btn-color-*}
 * class's selector match.
 *
 * <h2>Pill</h2>
 * {@link #pillProperty()} fully rounds the button's corners (Tailwind's
 * {@code rounded-full}),
 * independent of {@code Variant}, {@code Color} or {@code Size}.
 *
 * <h2>Processing</h2>
 * {@link #processingProperty()} is Flowbite React's {@code isProcessing}: while
 * {@code true} the
 * button shows an {@link FxSpinner} in place of its icon and, if it has text,
 * swaps the text for
 * {@link #processingLabelProperty()} ({@code "Loading..."} by default). The
 * spinner is sized from the
 * button's {@link Size} and painted in the button's current text color, so it
 * stays visible on every
 * variant and color, in both themes, and through hover/pressed changes. While
 * processing, the button
 * does not fire its action (so a slow save cannot be submitted twice) but is
 * not dimmed like a
 * disabled button; set {@code disable} as well if you want that look. When
 * processing ends, the
 * previous text and icon are restored.
 *
 * <h2>className</h2>
 * {@link #classNameProperty()} accepts Tailwind-like utility tokens, resolved
 * to inline style by
 * {@link ClassNameStyler} - see that class's javadoc for the full grammar,
 * including gradients
 * ({@code "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br"})
 * and
 * pseudo-class scoping ({@code hover:}, {@code pressed:}, {@code armed:},
 * {@code focus:},
 * {@code disabled:}). Because it resolves to inline style, {@code className}
 * always overrides
 * {@code Variant}/{@code Color} for the same CSS property without ever touching
 * their style classes.
 *
 * <h2>Java</h2>
 * 
 * <pre>{@code
 * FxButton save = new FxButton("Save");
 * save.setVariant(FxButton.Variant.PRIMARY);
 * save.setSize(FxButton.Size.LG);
 * save.setOnAction(e -> saveForm());
 *
 * FxButton outlined = new FxButton("Green");
 * outlined.setVariant(FxButton.Variant.OUTLINE);
 * outlined.setColor(FxButton.Color.GREEN);
 * outlined.setPill(true);
 *
 * FxButton gradient = new FxButton("Blue");
 * gradient.setClassName(
 *         "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 text-white hover:bg-gradient-to-br");
 *
 * FxButton busy = new FxButton("Save");
 * busy.setProcessingLabel("Saving...");
 * busy.setProcessing(true); // spinner + "Saving...", clicks ignored until setProcessing(false)
 * }</pre>
 *
 * <h2>FXML</h2>
 * 
 * <pre>{@code
 * <FxButton text="Save" variant="PRIMARY" size="LG" onAction="#handleSave"/>
 * <FxButton text="Green" variant="OUTLINE" color="GREEN" pill="true"/>
 * <FxButton text="Save" processing="true" processingLabel="Saving..."/>
 * }</pre>
 */
public class FxButton extends Button {

    public static final String STYLE_CLASS = "fxk-btn";

    private static final String VARIANT_STYLE_CLASS_PREFIX = "fxk-btn-";
    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-btn-size-";
    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-btn-color-";
    private static final String PILL_STYLE_CLASS = "fxk-btn-pill";
    private static final String PROCESSING_STYLE_CLASS = "fxk-btn-processing";

    public static final Variant DEFAULT_VARIANT = Variant.PRIMARY;
    public static final Size DEFAULT_SIZE = Size.MD;
    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /** Flowbite's default {@code processingLabel}. */
    public static final String DEFAULT_PROCESSING_LABEL = "Loading...";

    /**
     * How much of the text color the spinner's track keeps (the arc uses the full
     * text color).
     */
    private static final double SPINNER_TRACK_OPACITY = 0.3;

    /**
     * The look of an {@code FxButton}. {@link #DEFAULT} and {@link #OUTLINE} take
     * their color from
     * {@link #colorProperty()} instead of a fixed semantic token; every other
     * variant is styled from
     * semantic tokens only, so it looks right in both light and dark themes
     * regardless of
     * {@code Color}.
     */
    public enum Variant {

        /**
         * The main, high-emphasis action on a screen. Solid fill using the
         * brand/primary token.
         */
        PRIMARY,

        /**
         * Solid fill whose color comes from {@link #colorProperty()} instead of a fixed
         * semantic
         * token. {@link Color#DEFAULT} uses the primary token; combine with any other
         * {@link Color}
         * for flowbite-style named colors. {@code Color} has no effect under any
         * variant except this
         * one and {@link #OUTLINE}.
         */
        DEFAULT,

        /**
         * A lower-emphasis action, often paired with a {@link #PRIMARY} button.
         * Neutral, outlined fill.
         */
        SECONDARY,

        /**
         * A destructive or irreversible action (delete, remove, discard). Solid fill
         * using the danger token.
         */
        DANGER,

        /**
         * A positive or confirming action (confirm, complete, approve). Solid fill
         * using the success token.
         */
        SUCCESS,

        /** The lowest-emphasis action: no fill or border until hovered or pressed. */
        GHOST,

        /**
         * A medium-emphasis action: no fill, but a visible border, filling solid on
         * hover/press -
         * Flowbite React's {@code <Button outline color="...">}. Colored by
         * {@link #colorProperty()}
         * exactly like {@link #DEFAULT}; {@link Color#DEFAULT} uses the primary token.
         */
        OUTLINE
    }

    /**
     * A named fill for {@link Variant#DEFAULT} (solid) and {@link Variant#OUTLINE}
     * (border + text,
     * filling solid on hover), both styled in {@code colors.css}. Has no visual
     * effect under any
     * other variant.
     */
    public enum Color {
        DEFAULT, ALTERNATIVE, DARK, LIGHT,
        BLUE, CYAN, GRAY, GREEN, INDIGO,
        LIME, PINK, PURPLE, RED, TEAL, YELLOW
    }

    /**
     * The size of an {@code FxButton}. Independent of {@link Variant}: any size can
     * be combined with
     * any variant.
     */
    public enum Size {
        /**
         * 32px tall, compact horizontal padding, smallest text. Dense toolbars, table
         * row actions.
         */
        XS,

        /** 36px tall. Secondary actions in a form or toolbar. */
        SM,

        /** The default: 40px tall, base text. */
        MD,

        /** 48px tall, base text. Primary calls to action. */
        LG,

        /** 52px tall, base text, roomiest padding. Hero sections, standalone CTAs. */
        XL
    }

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT);

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final BooleanProperty pill = new SimpleBooleanProperty(this, "pill", false);

    private final BooleanProperty processing = new SimpleBooleanProperty(this, "processing", false);

    private final StringProperty processingLabel = new SimpleStringProperty(this, "processingLabel",
            DEFAULT_PROCESSING_LABEL);

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final StringProperty className = new SimpleStringProperty(this, "className", "");

    /**
     * While processing: the graphic and text to restore afterwards, and the spinner
     * standing in for them.
     */
    private Node savedGraphic;
    private String savedText;
    private FxSpinner processingSpinner;

    /**
     * True while this class itself is writing graphic/text, so its own writes
     * aren't mistaken for the caller's.
     */
    private boolean applyingProcessing;

    public FxButton() {
        initialize();
    }

    public FxButton(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, VARIANT_STYLE_CLASS_PREFIX, variant);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        BooleanStyleClassSync.sync(this, PILL_STYLE_CLASS, pill);
        BooleanStyleClassSync.sync(this, PROCESSING_STYLE_CLASS, processing);
        icon.addListener((observable, oldValue, newValue) -> applyIcon(newValue));
        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(this, newValue));

        processing.addListener((observable, oldValue, newValue) -> applyProcessing(newValue));
        size.addListener((observable, oldValue, newValue) -> {
            if (processingSpinner != null) {
                processingSpinner.setSize(spinnerSizeFor(newValue));
            }
        });
        processingLabel.addListener((observable, oldValue, newValue) -> {
            if (processing.get()) {
                showProcessingText();
            }
        });
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (!applyingProcessing && processing.get()) {
                // The caller changed the text while processing: remember it for later, keep
                // showing the label.
                savedText = newValue;
                showProcessingText();
            }
        });
    }

    // ---- icon
    // -----------------------------------------------------------------------------------

    private void applyIcon(Ikon icon) {
        if (processing.get()) {
            // The spinner owns the graphic right now; update what will be restored instead.
            savedGraphic = graphicFor(savedGraphic, icon);
        } else {
            setGraphic(graphicFor(getGraphic(), icon));
        }
    }

    /**
     * Returns the graphic that should result from setting {@code icon} while
     * {@code current} is showing.
     */
    private static Node graphicFor(Node current, Ikon icon) {
        if (icon == null) {
            return current instanceof FontIcon ? null : current;
        }
        if (current instanceof FontIcon fontIcon) {
            fontIcon.setIconCode(icon);
            return fontIcon;
        }
        return new FontIcon(icon);
    }

    // ---- processing
    // -----------------------------------------------------------------------------

    private void applyProcessing(boolean on) {
        if (on) {
            savedGraphic = getGraphic();
            savedText = getText();

            processingSpinner = new FxSpinner(processingAccessibleText());
            processingSpinner.setSize(spinnerSizeFor(getSize()));
            // Paint the spinner in the button's current text color: visible on every
            // variant and color,
            // both themes, and through hover/pressed changes (textFill is what CSS last
            // resolved).
            processingSpinner.styleProperty().bind(Bindings.createStringBinding(
                    this::spinnerStyle, textFillProperty()));

            applyingProcessing = true;
            try {
                setGraphic(processingSpinner);
            } finally {
                applyingProcessing = false;
            }
            showProcessingText();
        } else {
            FxSpinner spinner = processingSpinner;
            processingSpinner = null;
            if (spinner != null) {
                spinner.styleProperty().unbind();
            }
            applyingProcessing = true;
            try {
                setGraphic(savedGraphic);
                setText(savedText);
            } finally {
                applyingProcessing = false;
            }
            savedGraphic = null;
            savedText = null;
        }
    }

    /**
     * Shows the processing label, but only for buttons that have text: an icon-only
     * button stays icon-only.
     */
    private void showProcessingText() {
        String label = getProcessingLabel();
        boolean swap = label != null && !label.isBlank() && savedText != null && !savedText.isEmpty();
        applyingProcessing = true;
        try {
            setText(swap ? label : savedText);
        } finally {
            applyingProcessing = false;
        }
    }

    private String processingAccessibleText() {
        String label = getProcessingLabel();
        return label == null || label.isBlank() ? "Loading" : label;
    }

    private String spinnerStyle() {
        Paint fill = getTextFill();
        if (!(fill instanceof javafx.scene.paint.Color text)) {
            return "";
        }
        return "-fxk-spinner-arc: " + rgba(text, 1.0) + "; -fxk-spinner-track: "
                + rgba(text, SPINNER_TRACK_OPACITY) + ";";
    }

    private static String rgba(javafx.scene.paint.Color c, double opacityFactor) {
        return String.format(Locale.ROOT, "rgba(%d, %d, %d, %.3f)",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255),
                c.getOpacity() * opacityFactor);
    }

    /**
     * Spinner diameter for a button size: 12px on XS, 16px on SM..LG, 24px on XL.
     */
    private static FxSpinner.Size spinnerSizeFor(Size buttonSize) {
        Size s = buttonSize == null ? DEFAULT_SIZE : buttonSize;
        return switch (s) {
            case XS -> FxSpinner.Size.XS;
            case SM, MD, LG -> FxSpinner.Size.SM;
            case XL -> FxSpinner.Size.MD;
        };
    }

    /**
     * Ignores activation while {@link #isProcessing()}, so a slow action cannot be
     * triggered twice.
     */
    @Override
    public void fire() {
        if (!isProcessing()) {
            super.fire();
        }
    }

    // ---- properties
    // -----------------------------------------------------------------------------

    public Variant getVariant() {
        return variant.get();
    }

    public void setVariant(Variant variant) {
        this.variant.set(variant);
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Size getSize() {
        return size.get();
    }

    public void setSize(Size size) {
        this.size.set(size);
    }

    public ObjectProperty<Size> sizeProperty() {
        return size;
    }

    /**
     * @return the button's named color; only visible under {@link Variant#DEFAULT}
     *         or
     *         {@link Variant#OUTLINE}
     */
    public Color getColor() {
        return color.get();
    }

    /**
     * Sets the button's named color. Only has a visible effect when
     * {@link #getVariant()} is
     * {@link Variant#DEFAULT} (solid fill) or {@link Variant#OUTLINE} (border +
     * text, filling solid
     * on hover) - every other variant pins its own colors and ignores this
     * property.
     *
     * @param color the named color to apply
     */
    public void setColor(Color color) {
        this.color.set(color);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    /**
     * @return whether the button's corners are fully rounded (Tailwind's
     *         {@code rounded-full})
     */
    public boolean isPill() {
        return pill.get();
    }

    /**
     * Fully rounds the button's corners, independent of
     * {@code Variant}/{@code Color}/{@code Size}.
     *
     * @param pill {@code true} for fully rounded corners
     */
    public void setPill(boolean pill) {
        this.pill.set(pill);
    }

    public BooleanProperty pillProperty() {
        return pill;
    }

    /**
     * @return whether the button is showing its processing state (spinner,
     *         processing label, no action)
     */
    public boolean isProcessing() {
        return processing.get();
    }

    /**
     * Flowbite's {@code isProcessing}. While {@code true} the button shows a
     * spinner in place of its icon,
     * replaces its text (if any) with {@link #getProcessingLabel()}, and ignores
     * activation. Setting it
     * back to {@code false} restores the previous text and icon.
     *
     * @param processing {@code true} to show the processing state
     */
    public void setProcessing(boolean processing) {
        this.processing.set(processing);
    }

    public BooleanProperty processingProperty() {
        return processing;
    }

    /**
     * @return the text shown while processing; {@code null} or blank keeps the
     *         button's own text
     */
    public String getProcessingLabel() {
        return processingLabel.get();
    }

    /**
     * Flowbite's {@code processingLabel}. Also used as the spinner's accessible
     * text. Icon-only buttons
     * (no text) stay icon-only while processing; only the spinner replaces the
     * icon.
     *
     * @param processingLabel the text to show while processing; {@code null} or
     *                        blank keeps the current text
     */
    public void setProcessingLabel(String processingLabel) {
        this.processingLabel.set(processingLabel);
    }

    public StringProperty processingLabelProperty() {
        return processingLabel;
    }

    /**
     * @return the button's current {@code className} string (never {@code null};
     *         empty if unset)
     */
    public String getClassName() {
        return className.get();
    }

    /**
     * Sets arbitrary Tailwind-like utility tokens; see the class javadoc's
     * "className" section for the
     * grammar. Pass {@code ""} or {@code null} to clear everything this property
     * previously applied.
     *
     * @param className space-separated utility tokens, optionally
     *                  pseudo-class-scoped
     */
    public void setClassName(String className) {
        this.className.set(className);
    }

    public StringProperty classNameProperty() {
        return className;
    }

    public Ikon getIcon() {
        return icon.get();
    }

    public void setIcon(Ikon icon) {
        this.icon.set(icon);
    }

    public void setIcon(Ikon icon, String accessibleText) {
        setIcon(icon);
        setAccessibleText(accessibleText);
        if (getTooltip() == null) {
            setTooltip(new Tooltip(accessibleText));
        }
    }

    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }
}