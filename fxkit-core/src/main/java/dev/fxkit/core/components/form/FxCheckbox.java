package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;

/**
 * A {@link CheckBox} styled by FXKit's design tokens: Flowbite React's {@code <Checkbox>}, with a
 * typed {@link Color} for the checked fill.
 *
 * <p>{@code FxCheckbox} is a plain subclass of {@code CheckBox}: {@code selected}, {@code indeterminate},
 * {@code allowIndeterminate}, {@code onAction} and the rest keep working. Its text is the label
 * (Flowbite's separate {@code <Label>}, 8px away); leave it empty and pair the box with an
 * {@link FxLabel#FxLabel(String, javafx.scene.Node) FxLabel} when the label needs a link or a second
 * line.
 *
 * <h2>Color</h2>
 * {@link Color#DEFAULT} follows the theme's primary color. The class prefix
 * ({@code fxk-choice-color-*}) is shared with {@link FxRadio}; see {@code form-colors.css}.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxCheckbox remember = new FxCheckbox("Remember me");
 * remember.setSelected(true);
 *
 * FxCheckbox terms = new FxCheckbox("I agree with the terms and conditions");
 * terms.setColor(FxCheckbox.Color.GREEN);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxCheckbox text="Remember me" selected="true"/>
 * <FxCheckbox text="Eligible for international shipping" disable="true"/>
 * }</pre>
 */
public class FxCheckbox extends CheckBox {

    public static final String STYLE_CLASS = "fxk-check";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-choice-color-";

    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /** Flowbite's checkbox {@code color}: the fill of the checked (or indeterminate) box. */
    public enum Color {
        DEFAULT, DARK, FAILURE, GRAY, INFO, LIGHT, PURPLE, SUCCESS, WARNING,
        BLUE, CYAN, GREEN, INDIGO, LIME, PINK, RED, TEAL, YELLOW
    }

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    public FxCheckbox() {
        initialize();
    }

    public FxCheckbox(String text) {
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
