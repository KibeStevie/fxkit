package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.RadioButton;

/**
 * A {@link RadioButton} styled by FXKit's design tokens: Flowbite React's {@code <Radio>}, with a
 * typed {@link Color} for the selected fill.
 *
 * <p>{@code FxRadio} is a plain subclass of {@code RadioButton}. Flowbite's {@code name} attribute is
 * JavaFX's {@link javafx.scene.control.ToggleGroup}: radios that share a group allow one choice, and
 * {@code userData} is a good place for Flowbite's {@code value}. The text is the label.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * ToggleGroup countries = new ToggleGroup();
 *
 * FxRadio us = new FxRadio("United States");
 * us.setToggleGroup(countries);
 * us.setUserData("USA");
 * us.setSelected(true);
 *
 * FxRadio de = new FxRadio("Germany");
 * de.setToggleGroup(countries);
 * de.setUserData("Germany");
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxRadio text="United States" toggleGroup="$countries" selected="true"/>
 * }</pre>
 */
public class FxRadio extends RadioButton {

    public static final String STYLE_CLASS = "fxk-radio";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-choice-color-";

    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /** Flowbite's radio {@code color}: the fill of the selected circle. Same set as {@link FxCheckbox.Color}. */
    public enum Color {
        DEFAULT, DARK, FAILURE, GRAY, INFO, LIGHT, PURPLE, SUCCESS, WARNING,
        BLUE, CYAN, GREEN, INDIGO, LIME, PINK, RED, TEAL, YELLOW
    }

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    public FxRadio() {
        initialize();
    }

    public FxRadio(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
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
