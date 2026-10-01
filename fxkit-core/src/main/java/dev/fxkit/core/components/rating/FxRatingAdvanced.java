package dev.fxkit.core.components.rating;

import java.text.DecimalFormat;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * One row of a rating breakdown: a label, a progress bar and the percentage
 * (Flowbite's {@code <RatingAdvanced percentFilled={70}>5 star</RatingAdvanced>}).
 *
 * <p>Stack several rows in a {@code VBox}, below an {@link FxRating} with the average score, to show
 * how many reviews each score received:
 *
 * <pre>{@code
 * VBox breakdown = new VBox(8,
 *         new FxRatingAdvanced("5 star", 70),
 *         new FxRatingAdvanced("4 star", 17),
 *         new FxRatingAdvanced("3 star", 8),
 *         new FxRatingAdvanced("2 star", 4),
 *         new FxRatingAdvanced("1 star", 1));
 * }</pre>
 *
 * <p>{@link #percentFilled} is clamped to 0..100 when shown. The bar has a fixed default width
 * (see {@link #setBarWidth(double)}); label and percentage have a minimum width so that the bars of
 * stacked rows line up.
 */
public class FxRatingAdvanced extends HBox {

    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final DoubleProperty percentFilled = new SimpleDoubleProperty(this, "percentFilled", 0);

    private final Track track = new Track();
    private final Label label = new Label();
    private final Label percent = new Label();

    /** Creates an empty row (no label, 0%). */
    public FxRatingAdvanced() {
        getStyleClass().add("fxk-rating-advanced");
        setFillHeight(false);

        label.getStyleClass().add("fxk-rating-advanced-label");
        percent.getStyleClass().add("fxk-rating-advanced-percent");
        label.textProperty().bind(text);

        percentFilled.addListener((obs, oldValue, newValue) -> refresh());
        refresh();

        // Read by screen readers instead of the three separate nodes: "5 star, 70%".
        accessibleTextProperty().bind(Bindings.createStringBinding(
                () -> (getText() == null || getText().isEmpty() ? "" : getText() + ", ")
                        + format(clamp(getPercentFilled())),
                text, percentFilled));

        getChildren().addAll(label, track, percent);
    }

    /**
     * @param text          the label before the bar, for example {@code "5 star"}
     * @param percentFilled how full the bar is, from 0 to 100
     */
    public FxRatingAdvanced(String text, double percentFilled) {
        this();
        setText(text);
        setPercentFilled(percentFilled);
    }

    private void refresh() {
        double value = clamp(getPercentFilled());
        percent.setText(format(value));
        track.setPercent(value);
    }

    private static double clamp(double value) {
        return Double.isNaN(value) ? 0 : Math.max(0, Math.min(100, value));
    }

    /** {@code 70} becomes {@code "70%"}, {@code 8.5} becomes {@code "8.5%"}. */
    private static String format(double value) {
        return new DecimalFormat("0.#").format(value) + "%";
    }

    // ---- properties -------------------------------------------------------------------------

    /** The label before the bar, for example {@code "5 star"}. */
    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
    }

    /** How full the bar is, from 0 to 100 (values outside are clamped). Default 0. */
    public final DoubleProperty percentFilledProperty() {
        return percentFilled;
    }

    public final double getPercentFilled() {
        return percentFilled.get();
    }

    public final void setPercentFilled(double value) {
        percentFilled.set(value);
    }

    /**
     * Sets the width of the bar in pixels, overriding the default from {@code components.css}.
     *
     * @param width the bar width
     */
    public final void setBarWidth(double width) {
        track.setPrefWidth(width);
    }

    // ---- the bar ----------------------------------------------------------------------------

    /**
     * The grey track with the colored fill inside. The fill's width is a fraction of the track's, which
     * CSS cannot express, so it is laid out here.
     */
    private static final class Track extends Pane {

        private final Region fill = new Region();
        private double percent;

        Track() {
            getStyleClass().add("fxk-rating-advanced-track");
            fill.getStyleClass().add("fxk-rating-advanced-fill");
            getChildren().add(fill);
        }

        void setPercent(double percent) {
            this.percent = percent;
            requestLayout();
        }

        @Override
        protected void layoutChildren() {
            fill.resizeRelocate(0, 0, getWidth() * percent / 100.0, getHeight());
        }
    }
}
