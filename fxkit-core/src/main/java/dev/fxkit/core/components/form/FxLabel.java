package dev.fxkit.core.components.form;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * A {@link Label} styled by FXKit's design tokens: Flowbite React's {@code <Label>}
 * ({@code text-sm font-medium}), with a {@link Color} for validation states.
 *
 * <p>{@code FxLabel} is a plain subclass of {@code Label}: every standard {@code Label} API keeps
 * working. {@link Label#setLabelFor(Node)} is Flowbite's {@code htmlFor}: besides JavaFX's mnemonic
 * handling, a primary-button click on the label focuses the target, and also activates it when it is
 * a {@link ButtonBase} (checkbox, radio, toggle) or an {@link FxFileInput}.
 *
 * <h2>Color</h2>
 * {@link Color#DEFAULT} follows the theme's text color; the other colors are Flowbite's validation
 * colors and are defined in {@code form-colors.css}. Disabling the label dims it (Flowbite's
 * {@code disabled}).
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxTextInput email = new FxTextInput();
 * FxLabel label = new FxLabel("Your email", email);
 * label.setColor(FxLabel.Color.FAILURE);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxLabel text="Your email" color="SUCCESS"/>
 * }</pre>
 */
public class FxLabel extends Label {

    public static final String STYLE_CLASS = "fxk-label";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-label-color-";

    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /** Flowbite's label {@code color}. */
    public enum Color {
        DEFAULT, INFO, FAILURE, WARNING, SUCCESS
    }

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    public FxLabel() {
        initialize();
    }

    public FxLabel(String text) {
        super(text);
        initialize();
    }

    /**
     * @param text     the label's text
     * @param labelFor the control this label describes (Flowbite's {@code htmlFor})
     */
    public FxLabel(String text, Node labelFor) {
        super(text);
        initialize();
        setLabelFor(labelFor);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::forwardClick);
    }

    /** Clicking the label acts on its target, like a browser's {@code <label for="...">}. */
    private void forwardClick(MouseEvent event) {
        Node target = getLabelFor();
        if (target == null || event.getButton() != MouseButton.PRIMARY || isDisabled() || target.isDisabled()) {
            return;
        }
        target.requestFocus();
        if (target instanceof ButtonBase button) {
            button.fire();
        } else if (target instanceof FxFileInput fileInput) {
            fileInput.choose();
        }
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
