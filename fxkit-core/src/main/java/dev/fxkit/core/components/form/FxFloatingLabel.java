package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * A text field whose label sits inside the field like a placeholder and floats up when the field
 * is focused or has text. Port of Flowbite React's {@code FloatingLabel}.
 *
 * <pre>{@code
 * FxFloatingLabel email = new FxFloatingLabel(FxFloatingLabel.Variant.OUTLINED, "Email");
 * email.setColor(FxFloatingLabel.Color.ERROR);
 * email.textProperty().addListener((o, was, is) -> validate(is));
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <ul>
 *   <li>{@code variant} filled / outlined / standard -> {@link Variant}</li>
 *   <li>{@code sizing} sm / md -> {@link Size}</li>
 *   <li>{@code color} default / success / error -> {@link Color}</li>
 *   <li>{@code disabled} -> {@link #setDisable(boolean)} (dims to 50%)</li>
 *   <li>{@code peer-placeholder-shown} / {@code peer-focus} -> the label rests in the field while it is
 *       empty and unfocused, and floats (scale 0.75, 300ms) otherwise</li>
 * </ul>
 *
 * <h2>Layout notes</h2>
 * The floating label of the OUTLINED and STANDARD variants rises above the field's top edge, so
 * those variants reserve a few pixels above the field (6px and 9px). Put the three variants in an
 * {@code HBox} with {@code Pos.BOTTOM_LEFT} to line the fields up.
 *
 * <p>The OUTLINED label paints a "cutout" behind itself while floating so the border does not strike
 * through it. It uses the looked-up color {@code -fxk-floating-cutout} (default
 * {@code -fxk-surface}); redefine it on this node or an ancestor when the field sits on another
 * background.
 *
 * <h2>Style classes</h2>
 * {@code fxk-floating}, {@code fxk-floating-<variant>}, {@code fxk-floating-size-<size>},
 * {@code fxk-floating-color-<color>}, plus the state classes {@code fxk-floating-focused} and
 * {@code fxk-floating-floated}. Parts: {@code fxk-floating-input}, {@code fxk-floating-label}.
 */
public class FxFloatingLabel extends Pane {

    /** Flowbite's {@code variant}. */
    public enum Variant { FILLED, OUTLINED, STANDARD }

    /** Flowbite's {@code sizing}. */
    public enum Size { SM, MD }

    /** Flowbite's {@code color}. */
    public enum Color { DEFAULT, SUCCESS, ERROR }

    private static final double FLOAT_SCALE = 0.75;
    private static final Duration DURATION = Duration.millis(300); // duration-300
    private static final Interpolator EASE = Interpolator.SPLINE(0.4, 0, 0.2, 1); // Tailwind default ease

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.FILLED);
    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.DEFAULT);
    private final StringProperty label = new SimpleStringProperty(this, "label", "");

    private final TextField input = new TextField();
    private final Label floatingLabel = new Label();
    /** Scales about the label's top-left corner (Tailwind's {@code origin-[0]}). */
    private final Scale labelScale = new Scale(1, 1, 0, 0);
    /** 0 = resting inside the field, 1 = floated. */
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0);
    private final Timeline timeline = new Timeline();
    /** Held in a field: bindings only listen to their dependencies weakly. */
    private final BooleanBinding floated;

    public FxFloatingLabel() {
        this(Variant.FILLED, "");
    }

    public FxFloatingLabel(String label) {
        this(Variant.FILLED, label);
    }

    public FxFloatingLabel(Variant variant, String label) {
        getStyleClass().add("fxk-floating");
        input.getStyleClass().add("fxk-floating-input");
        floatingLabel.getStyleClass().add("fxk-floating-label");
        floatingLabel.setMouseTransparent(true); // clicks fall through to the input
        floatingLabel.getTransforms().add(labelScale);
        floatingLabel.textProperty().bind(this.label);
        getChildren().addAll(input, floatingLabel);

        setFocusTraversable(false); // the inner input takes focus
        setMaxWidth(Double.MAX_VALUE);
        setOnMousePressed(e -> input.requestFocus()); // the strip above an OUTLINED/STANDARD field

        EnumStyleClassSync.sync(this, "fxk-floating-", this.variant);
        EnumStyleClassSync.sync(this, "fxk-floating-size-", this.size);
        EnumStyleClassSync.sync(this, "fxk-floating-color-", this.color);
        this.variant.addListener((o, was, is) -> requestLayout()); // the reserved strip depends on it

        floated = input.focusedProperty().or(input.textProperty().isNotEmpty());
        BooleanStyleClassSync.sync(this, "fxk-floating-focused", input.focusedProperty());
        BooleanStyleClassSync.sync(this, "fxk-floating-floated", floated);
        floated.addListener((o, was, is) -> moveLabel(is, true));
        progress.addListener((o, was, is) -> applyProgress());

        setVariant(variant == null ? Variant.FILLED : variant);
        setLabel(label);
        moveLabel(floated.get(), false);
    }

    // ---- properties -------------------------------------------------------------------------

    public final ObjectProperty<Variant> variantProperty() { return variant; }
    public final Variant getVariant() { return variant.get(); }
    public final void setVariant(Variant value) { variant.set(value); }

    public final ObjectProperty<Size> sizeProperty() { return size; }
    public final Size getSize() { return size.get(); }
    public final void setSize(Size value) { size.set(value); }

    public final ObjectProperty<Color> colorProperty() { return color; }
    public final Color getColor() { return color.get(); }
    public final void setColor(Color value) { color.set(value); }

    /** The label text (Flowbite's {@code label}). */
    public final StringProperty labelProperty() { return label; }
    public final String getLabel() { return label.get(); }
    public final void setLabel(String value) { label.set(value == null ? "" : value); }

    /** The text typed into the field; the same property as the inner {@link TextField}'s. */
    public final StringProperty textProperty() { return input.textProperty(); }
    public final String getText() { return input.getText(); }
    public final void setText(String value) { input.setText(value); }

    /**
     * The inner text field, for everything this class does not wrap ({@code setOnAction},
     * {@code setEditable}, {@code selectAll()}, ...). Do not restyle its background or border.
     */
    public final TextField getInput() { return input; }

    @Override
    public void requestFocus() {
        input.requestFocus();
    }

    // ---- floating ---------------------------------------------------------------------------

    private void moveLabel(boolean up, boolean animate) {
        timeline.stop();
        double target = up ? 1 : 0;
        if (!animate || getScene() == null) {
            progress.set(target);
            return;
        }
        timeline.getKeyFrames().setAll(new KeyFrame(DURATION, new KeyValue(progress, target, EASE)));
        timeline.playFromStart();
    }

    /** Scales and lifts the label for the current progress; its layout position is the resting one. */
    private void applyProgress() {
        double p = progress.get();
        double s = 1 - (1 - FLOAT_SCALE) * p;
        labelScale.setX(s);
        labelScale.setY(s);

        double labelHeight = floatingLabel.getHeight();
        double restTop = input.getLayoutY() + (input.getHeight() - labelHeight) / 2;
        double floatTop = input.getLayoutY() + floatedCenterY() - FLOAT_SCALE * labelHeight / 2;
        floatingLabel.setTranslateY(p * (floatTop - restTop));
    }

    // Per-variant geometry, from Flowbite's classes (top-4 / top-2 -translate-y-4 / top-3 -translate-y-6).

    private Variant effectiveVariant() {
        return getVariant() == null ? Variant.FILLED : getVariant();
    }

    /** Space reserved above the field for the floated label that rises past its top edge. */
    private double overhang() {
        return switch (effectiveVariant()) {
            case FILLED -> 0;
            case OUTLINED -> 6;
            case STANDARD -> 9;
        };
    }

    /** Label's left edge inside the field: left-2.5, left-1 (+ its own px-2), left-0. */
    private double labelX() {
        return switch (effectiveVariant()) {
            case FILLED -> 10;
            case OUTLINED -> 4;
            case STANDARD -> 0;
        };
    }

    /** Vertical center of the floated label, measured from the field's top edge. */
    private double floatedCenterY() {
        return switch (effectiveVariant()) {
            case FILLED -> 7.5;     // sits in the padded top strip
            case OUTLINED -> 0.5;   // straddles the top border
            case STANDARD -> -4.5;  // above the field
        };
    }

    // ---- layout -----------------------------------------------------------------------------

    @Override
    protected double computePrefWidth(double height) {
        Insets in = getInsets();
        return in.getLeft() + input.prefWidth(-1) + in.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets in = getInsets();
        return in.getTop() + overhang() + input.prefHeight(-1) + in.getBottom();
    }

    @Override
    protected void layoutChildren() {
        Insets in = getInsets();
        double x = in.getLeft();
        double y = in.getTop() + overhang();
        double w = getWidth() - in.getLeft() - in.getRight();
        double h = getHeight() - y - in.getBottom();
        input.resizeRelocate(x, y, w, h);

        double labelHeight = floatingLabel.prefHeight(-1);
        double labelWidth = Math.min(floatingLabel.prefWidth(-1), Math.max(0, w - labelX()));
        floatingLabel.resizeRelocate(x + labelX(), y + (h - labelHeight) / 2, labelWidth, labelHeight);
        applyProgress();
    }
}
