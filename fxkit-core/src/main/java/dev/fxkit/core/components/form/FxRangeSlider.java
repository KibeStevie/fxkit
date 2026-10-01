package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Slider;

/**
 * A {@link Slider} styled by FXKit's design tokens: Flowbite React's {@code <RangeSlider>}, which is
 * an HTML {@code <input type="range">} - ONE thumb that picks a number between a minimum and a
 * maximum, not a two-thumb interval.
 *
 * <p>{@code FxRangeSlider} is a plain subclass of {@code Slider}: {@code min}, {@code max},
 * {@code value}, {@code blockIncrement}, tick marks and snapping keep working. Only the horizontal
 * orientation is styled. Disabling it dims it (Flowbite's {@code disabled}).
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxRangeSlider volume = new FxRangeSlider();
 * volume.setValue(40);
 * volume.setSize(FxRangeSlider.Size.LG);
 * volume.valueProperty().addListener((obs, was, now) -> setVolume(now.doubleValue()));
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxRangeSlider min="0" max="100" value="50" size="SM"/>
 * }</pre>
 */
public class FxRangeSlider extends Slider {

    public static final String STYLE_CLASS = "fxk-range";

    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-range-size-";

    public static final Size DEFAULT_SIZE = Size.MD;

    /** Flowbite's {@code sizing}: the track is 4 / 8 / 12px tall ({@code h-1} / {@code h-2} / {@code h-3}). */
    public enum Size {
        SM, MD, LG
    }

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    public FxRangeSlider() {
        initialize();
    }

    public FxRangeSlider(double min, double max, double value) {
        super(min, max, value);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
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
}
