package dev.fxkit.core.components.progress;

import java.util.Locale;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * A progress bar, modelled on Flowbite React's {@code <Progress>}.
 *
 * <pre>{@code
 * FxProgress plain = new FxProgress(45);
 *
 * FxProgress labelled = new FxProgress(50);
 * labelled.setTextLabel("Flowbite");
 * labelled.setSize(FxProgress.Size.LG);
 * labelled.setLabelProgress(true);
 * labelled.setLabelText(true);
 *
 * FxProgress positioned = new FxProgress(45);
 * positioned.setSize(FxProgress.Size.LG);
 * positioned.setTextLabel("Flowbite");
 * positioned.setLabelText(true);
 * positioned.setLabelProgress(true);
 * positioned.setTextLabelPosition(FxProgress.LabelPosition.OUTSIDE);
 * positioned.setProgressLabelPosition(FxProgress.LabelPosition.INSIDE);
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <table>
 * <caption>React prop to FxProgress property</caption>
 * <tr>
 * <td>{@code progress}</td>
 * <td>{@link #progressProperty()} (0 to 100, values outside are clamped)</td>
 * </tr>
 * <tr>
 * <td>{@code size}</td>
 * <td>{@link #sizeProperty()}: {@code SM / MD / LG / XL} = 6 / 10 / 16 / 24
 * px</td>
 * </tr>
 * <tr>
 * <td>{@code color}</td>
 * <td>{@link #colorProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code textLabel}</td>
 * <td>{@link #textLabelProperty()}, also the accessible text</td>
 * </tr>
 * <tr>
 * <td>{@code labelText}</td>
 * <td>{@link #labelTextProperty()}: show the text label</td>
 * </tr>
 * <tr>
 * <td>{@code labelProgress}</td>
 * <td>{@link #labelProgressProperty()}: show the "45%" label</td>
 * </tr>
 * <tr>
 * <td>{@code textLabelPosition}</td>
 * <td>{@link #textLabelPositionProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code progressLabelPosition}</td>
 * <td>{@link #progressLabelPositionProperty()}</td>
 * </tr>
 * </table>
 *
 * <h2>Structure</h2>
 * 
 * <pre>
 * .fxk-progress                       VBox, carries the size and color classes
 *   .fxk-progress-label               HBox, only present when an OUTSIDE label is shown
 *   .fxk-progress-track               the rounded, clipped track
 *     .fxk-progress-bar               the fill, sized to progress / 100 of the track
 * </pre>
 * 
 * JavaFX CSS cannot express "width: 45%", so {@code Track} sizes the fill in
 * {@code layoutChildren}.
 * Everything else (heights, colors, fonts) is CSS: see the FxProgress blocks in
 * {@code components.css} and {@code colors.css}.
 *
 * <p>
 * Inside labels are drawn on the fill and clipped to it, so they only read well
 * on
 * {@link Size#LG} and {@link Size#XL}; on {@code SM} and {@code MD} the bar is
 * thinner than the text,
 * as in Flowbite.
 */
public class FxProgress extends VBox {

    /** Bar height. */
    public enum Size {
        SM, MD, LG, XL
    }

    /** Fill color. {@code DEFAULT} follows the theme's primary token. */
    public enum Color {
        DEFAULT, DARK, BLUE, RED, GREEN, YELLOW, INDIGO, PURPLE, CYAN, GRAY, LIME, PINK, TEAL
    }

    /** Where a label is drawn: on the fill, or in a row above the bar. */
    public enum LabelPosition {
        INSIDE, OUTSIDE
    }

    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0);
    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.DEFAULT);
    private final StringProperty textLabel = new SimpleStringProperty(this, "textLabel", "progressbar");
    private final BooleanProperty labelText = new SimpleBooleanProperty(this, "labelText", false);
    private final BooleanProperty labelProgress = new SimpleBooleanProperty(this, "labelProgress", false);
    private final ObjectProperty<LabelPosition> textLabelPosition = new SimpleObjectProperty<>(this,
            "textLabelPosition", LabelPosition.INSIDE);
    private final ObjectProperty<LabelPosition> progressLabelPosition = new SimpleObjectProperty<>(this,
            "progressLabelPosition", LabelPosition.INSIDE);

    private final Label outsideText = newLabel("fxk-progress-label-item");
    private final Label outsidePercent = newLabel("fxk-progress-label-item");
    private final Region labelSpacer = new Region();
    private final HBox labelRow = new HBox(outsideText, labelSpacer, outsidePercent);

    private final Label insideText = newLabel("fxk-progress-bar-text");
    private final Label insidePercent = newLabel("fxk-progress-bar-text");
    private final Track track = new Track(new HBox(insideText, insidePercent));

    public FxProgress() {
        this(0);
    }

    /**
     * @param progress the initial progress, 0 to 100
     */
    public FxProgress(double progress) {
        getStyleClass().add("fxk-progress");
        EnumStyleClassSync.sync(this, "fxk-progress-size-", size);
        EnumStyleClassSync.sync(this, "fxk-progress-color-", color);

        // w-full: fill whatever the parent offers
        setMaxWidth(Double.MAX_VALUE);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);

        labelRow.getStyleClass().add("fxk-progress-label");
        HBox.setHgrow(labelSpacer, Priority.ALWAYS);
        getChildren().addAll(labelRow, track);

        InvalidationListener refresh = observable -> refresh();
        this.progress.addListener(refresh);
        textLabel.addListener(refresh);
        labelText.addListener(refresh);
        labelProgress.addListener(refresh);
        textLabelPosition.addListener(refresh);
        progressLabelPosition.addListener(refresh);

        this.progress.set(progress);
        refresh();
    }

    // ---- state -> nodes
    // ------------------------------------------------------------------------

    private void refresh() {
        double value = clampedProgress();
        String text = getTextLabel();
        boolean hasText = text != null && !text.isEmpty();

        boolean showText = isLabelText() && hasText;
        boolean showPercent = isLabelProgress();
        boolean textOutside = showText && getTextLabelPosition() == LabelPosition.OUTSIDE;
        boolean percentOutside = showPercent && getProgressLabelPosition() == LabelPosition.OUTSIDE;
        boolean textInside = showText && !textOutside;
        boolean percentInside = showPercent && !percentOutside;

        String percent = formatPercent(value);
        outsideText.setText(text);
        insideText.setText(text);
        outsidePercent.setText(percent);
        insidePercent.setText(percent);

        show(outsideText, textOutside);
        show(outsidePercent, percentOutside);
        // Flowbite uses justify-between: with one label it stays at the start, with two
        // they split.
        show(labelSpacer, textOutside && percentOutside);
        show(labelRow, textOutside || percentOutside);
        show(insideText, textInside);
        show(insidePercent, percentInside);

        track.setFraction(value / 100.0);
        setAccessibleText(hasText ? text : null);
    }

    private double clampedProgress() {
        double value = progress.get();
        if (Double.isNaN(value)) {
            return 0;
        }
        return Math.max(0, Math.min(100, value));
    }

    /** "45%", or "45.5%" when the value is not whole. */
    private static String formatPercent(double value) {
        if (value == Math.rint(value)) {
            return (long) value + "%";
        }
        return String.format(Locale.ROOT, "%.1f%%", value);
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static Label newLabel(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        // never ellipsize: an inside label that does not fit is clipped by the bar, not
        // shortened
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case MIN_VALUE -> 0.0;
            case MAX_VALUE -> 100.0;
            case VALUE -> clampedProgress();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    // ---- properties
    // ----------------------------------------------------------------------------

    /** Progress from 0 to 100. Values outside that range are clamped when drawn. */
    public DoubleProperty progressProperty() {
        return progress;
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double value) {
        progress.set(value);
    }

    public ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public Size getSize() {
        return size.get();
    }

    public void setSize(Size value) {
        size.set(value);
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
     * The label text. Default {@code "progressbar"}, which is also the accessible
     * text.
     */
    public StringProperty textLabelProperty() {
        return textLabel;
    }

    public String getTextLabel() {
        return textLabel.get();
    }

    public void setTextLabel(String value) {
        textLabel.set(value);
    }

    /**
     * Whether the text label is shown (Flowbite's {@code labelText}). Default
     * {@code false}.
     */
    public BooleanProperty labelTextProperty() {
        return labelText;
    }

    public boolean isLabelText() {
        return labelText.get();
    }

    public void setLabelText(boolean value) {
        labelText.set(value);
    }

    /**
     * Whether the percentage label is shown (Flowbite's {@code labelProgress}).
     * Default {@code false}.
     */
    public BooleanProperty labelProgressProperty() {
        return labelProgress;
    }

    public boolean isLabelProgress() {
        return labelProgress.get();
    }

    public void setLabelProgress(boolean value) {
        labelProgress.set(value);
    }

    public ObjectProperty<LabelPosition> textLabelPositionProperty() {
        return textLabelPosition;
    }

    public LabelPosition getTextLabelPosition() {
        return textLabelPosition.get();
    }

    public void setTextLabelPosition(LabelPosition value) {
        textLabelPosition.set(value);
    }

    public ObjectProperty<LabelPosition> progressLabelPositionProperty() {
        return progressLabelPosition;
    }

    public LabelPosition getProgressLabelPosition() {
        return progressLabelPosition.get();
    }

    public void setProgressLabelPosition(LabelPosition value) {
        progressLabelPosition.set(value);
    }

    // ---- track
    // ---------------------------------------------------------------------------------

    /**
     * The track and its fill. Sizes the fill to a fraction of the track's width
     * (which CSS cannot do),
     * clips everything to the track's pill shape (Flowbite's
     * {@code overflow-hidden rounded-full}) and
     * clips the fill's own content to the fill, so an inside label never spills
     * onto the empty track.
     */
    private static final class Track extends Pane {

        private final HBox bar;
        private final Rectangle trackClip = new Rectangle();
        private final Rectangle barClip = new Rectangle();
        private double fraction;

        Track(HBox bar) {
            this.bar = bar;
            getStyleClass().add("fxk-progress-track");
            bar.getStyleClass().add("fxk-progress-bar");
            bar.setAlignment(Pos.CENTER);
            bar.setClip(barClip);
            getChildren().add(bar);
            setClip(trackClip);
        }

        void setFraction(double fraction) {
            this.fraction = fraction;
            requestLayout();
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();

            trackClip.setWidth(width);
            trackClip.setHeight(height);
            trackClip.setArcWidth(height); // arc = height gives a full pill
            trackClip.setArcHeight(height);

            double barWidth = width * fraction;
            barClip.setWidth(barWidth);
            barClip.setHeight(height);
            bar.resizeRelocate(0, 0, barWidth, height);

            // text-center when the labels fit; start-aligned when they overflow, so the
            // first
            // characters stay visible rather than being cut off on both sides
            bar.setAlignment(bar.prefWidth(-1) > barWidth ? Pos.CENTER_LEFT : Pos.CENTER);
        }
    }
}
