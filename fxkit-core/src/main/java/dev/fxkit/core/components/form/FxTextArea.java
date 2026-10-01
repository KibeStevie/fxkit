package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextArea;

/**
 * A {@link TextArea} styled by FXKit's design tokens: Flowbite React's {@code <Textarea>}, with a
 * {@link Color} and a shadow.
 *
 * <p>{@code FxTextArea} is a plain subclass of {@code TextArea}: {@code prefRowCount} is Flowbite's
 * {@code rows} (4 by default here), and text wraps by default like a browser's textarea. It shares the
 * {@code fxk-field-color-*} classes with {@link FxTextInput}, so a form's validation colors are
 * defined once.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxTextArea comment = new FxTextArea();
 * comment.setPromptText("Leave a comment...");
 * comment.setPrefRowCount(6);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxTextArea promptText="Leave a comment..." prefRowCount="4" color="INFO"/>
 * }</pre>
 */
public class FxTextArea extends TextArea {

    public static final String STYLE_CLASS = "fxk-textarea";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-field-color-";
    private static final String SHADOW_STYLE_CLASS = "fxk-field-shadow";

    public static final Color DEFAULT_COLOR = Color.GRAY;

    /** Default {@code rows}. */
    public static final int DEFAULT_ROW_COUNT = 4;

    /** Flowbite's {@code color}. GRAY is the neutral default; the rest signal a validation state. */
    public enum Color {
        GRAY, INFO, FAILURE, WARNING, SUCCESS
    }

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final BooleanProperty shadow = new SimpleBooleanProperty(this, "shadow", false);

    public FxTextArea() {
        initialize();
    }

    public FxTextArea(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().addAll(FxTextInput.FIELD_STYLE_CLASS, STYLE_CLASS);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        BooleanStyleClassSync.sync(this, SHADOW_STYLE_CLASS, shadow);
        setWrapText(true);
        setPrefRowCount(DEFAULT_ROW_COUNT);
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

    /** @return whether the textarea has a drop shadow (Flowbite's {@code shadow}) */
    public boolean isShadow() {
        return shadow.get();
    }

    public void setShadow(boolean shadow) {
        this.shadow.set(shadow);
    }

    public BooleanProperty shadowProperty() {
        return shadow;
    }
}
