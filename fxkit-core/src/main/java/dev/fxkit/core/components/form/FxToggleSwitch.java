package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * An on/off switch: Flowbite React's {@code <ToggleSwitch>}, built on {@link ToggleButton} so it
 * keeps {@code selected}, {@code onAction}, keyboard activation (Space) and focus handling.
 *
 * <p>Flowbite's {@code checked} / {@code onChange} are {@link #selectedProperty()} and a listener on
 * it. The text is Flowbite's {@code label}, 12px after the track; leave it empty for a bare switch.
 * The track and knob are the button's graphic (style classes {@code fxk-toggle-track} and
 * {@code fxk-toggle-knob}); do not replace the graphic.
 *
 * <p>Unlike the browser, the knob jumps instead of sliding: JavaFX CSS has no transitions.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxToggleSwitch news = new FxToggleSwitch("Toggle me");
 * news.selectedProperty().addListener((obs, was, now) -> save(now));
 *
 * FxToggleSwitch large = new FxToggleSwitch("Notifications");
 * large.setSize(FxToggleSwitch.Size.LG);
 * large.setColor(FxToggleSwitch.Color.GREEN);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxToggleSwitch text="Toggle me (checked)" selected="true"/>
 * }</pre>
 */
public class FxToggleSwitch extends ToggleButton {

    public static final String STYLE_CLASS = "fxk-toggle";

    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-toggle-size-";
    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-toggle-color-";

    public static final Size DEFAULT_SIZE = Size.MD;
    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /** Flowbite's {@code sizing}. */
    public enum Size {
        /** 36 x 20px track, 16px knob. */
        SM,
        /** The default: 44 x 24px track, 20px knob. */
        MD,
        /** 52 x 28px track, 24px knob. */
        LG
    }

    /** Flowbite's toggle {@code color}: the fill of the track while on. */
    public enum Color {
        DEFAULT, BLUE, DARK, FAILURE, GRAY, GREEN, LIGHT, RED, PURPLE, SUCCESS,
        YELLOW, WARNING, CYAN, LIME, INDIGO, TEAL, INFO, PINK
    }

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    public FxToggleSwitch() {
        initialize();
    }

    public FxToggleSwitch(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);

        StackPane track = new StackPane();
        track.getStyleClass().add("fxk-toggle-track");
        Region knob = new Region();
        knob.getStyleClass().add("fxk-toggle-knob");
        track.getChildren().add(knob);

        setGraphic(track);
        setContentDisplay(ContentDisplay.LEFT);
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

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }
}
