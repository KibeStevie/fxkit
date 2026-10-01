package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Helper or validation text under a form control: Flowbite React's {@code <HelperText>}
 * ({@code mt-2 text-sm}).
 *
 * <p>It is a {@link TextFlow}, so it wraps to the width of its parent and can hold inline nodes
 * (bold lead-ins, a {@code Hyperlink}). {@link #textProperty()} is a shortcut for the plain case:
 * setting it replaces all children with one {@code Text}. For rich content, add children yourself:
 *
 * <pre>{@code
 * FxHelperText help = new FxHelperText();
 * help.setColor(FxHelperText.Color.FAILURE);
 * help.getChildren().addAll(FxHelperText.strong("Oops!"), new Text(" Username already taken!"));
 * }</pre>
 *
 * <p>{@link Color} uses the same names as {@link FxLabel.Color}; set both (and the control's own color)
 * to get Flowbite's validation look.
 */
public class FxHelperText extends TextFlow {

    public static final String STYLE_CLASS = "fxk-helper-text";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-helper-text-color-";
    private static final String STRONG_STYLE_CLASS = "fxk-helper-text-strong";

    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    public enum Color {
        DEFAULT, INFO, FAILURE, WARNING, SUCCESS
    }

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    public FxHelperText() {
        initialize();
    }

    public FxHelperText(String text) {
        initialize();
        setText(text);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        text.addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                getChildren().clear();
            } else {
                getChildren().setAll(new Text(newValue));
            }
        });
    }

    /** A {@code Text} in the medium weight, for lead-ins such as "Oops!" ({@code font-medium}). */
    public static Text strong(String text) {
        Text strong = new Text(text);
        strong.getStyleClass().add(STRONG_STYLE_CLASS);
        return strong;
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

    public String getText() {
        return text.get();
    }

    /** Replaces all children with this plain text; {@code null} or empty clears them. */
    public void setText(String text) {
        this.text.set(text);
    }

    public StringProperty textProperty() {
        return text;
    }
}
