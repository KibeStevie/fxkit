package dev.fxkit.core.components.form;

import org.kordamp.ikonli.Ikon;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * A {@link TextField} styled by FXKit's design tokens: Flowbite React's {@code <TextInput>}, with
 * typed {@link Size} and {@link Color} properties, an optional left/right icon, an {@link #addonProperty()
 * addon}, a shadow and a password mode.
 *
 * <p>{@code FxTextInput} is a plain subclass of {@code TextField}: text, prompt text, {@code editable}
 * (Flowbite's {@code readOnly}), {@code disable} and every other {@code TextField} API keep working
 * exactly as they do today. The icons and the addon are drawn by {@code FxTextInputSkin} inside the
 * field's own border, so the field is still ONE node that can be bound, focused and validated like
 * any other.
 *
 * <h2>Size and Color</h2>
 * {@link Size} is Flowbite's {@code sizing} (SM, MD, LG: padding, font size and a fixed height).
 * {@link Color} is Flowbite's {@code color}; GRAY is the neutral default, the others are validation
 * colors. The color classes ({@code fxk-field-color-*}) are shared with {@link FxTextArea} and
 * {@link FxFileInput}, and are defined in {@code form-colors.css}.
 *
 * <h2>Icons and addon</h2>
 * {@link #iconProperty()} / {@link #rightIconProperty()} take an Ikonli {@link Ikon}, like
 * {@code FxButton.icon}. {@link #addonProperty()} is a short text in a box before the text, such as
 * {@code "@"}.
 *
 * <h2>Password</h2>
 * {@link #typeProperty()} switches the field between {@link Type#TEXT} and {@link Type#PASSWORD}
 * (bullets, and copy/cut disabled, like {@code PasswordField}).
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxTextInput email = new FxTextInput();
 * email.setPromptText("name@flowbite.com");
 * email.setSize(FxTextInput.Size.LG);
 *
 * FxTextInput password = new FxTextInput();
 * password.setType(FxTextInput.Type.PASSWORD);
 *
 * FxTextInput username = new FxTextInput();
 * username.setAddon("@");
 * username.setColor(FxTextInput.Color.FAILURE);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxTextInput promptText="name@flowbite.com" size="LG" color="SUCCESS" shadow="true"/>
 * <FxTextInput type="PASSWORD"/>
 * }</pre>
 */
public class FxTextInput extends TextField {

    public static final String STYLE_CLASS = "fxk-input";

    /** Shared by every text-like form control; the color classes and the focus ring hang off it. */
    static final String FIELD_STYLE_CLASS = "fxk-field";

    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-input-size-";
    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-field-color-";
    private static final String SHADOW_STYLE_CLASS = "fxk-field-shadow";
    private static final String ICON_LEFT_STYLE_CLASS = "fxk-input-icon-left";
    private static final String ICON_RIGHT_STYLE_CLASS = "fxk-input-icon-right";

    public static final Size DEFAULT_SIZE = Size.MD;
    public static final Color DEFAULT_COLOR = Color.GRAY;
    public static final Type DEFAULT_TYPE = Type.TEXT;

    /** Flowbite's {@code sizing}. Independent of {@link Color}. */
    public enum Size {
        /** 34px tall, 8px padding, {@code text-xs}. */
        SM,
        /** The default: 42px tall, 10px padding, {@code text-sm}. */
        MD,
        /** 58px tall, 16px padding, {@code text-base}. */
        LG
    }

    /** Flowbite's {@code color}. GRAY is the neutral default; the rest signal a validation state. */
    public enum Color {
        GRAY, INFO, FAILURE, WARNING, SUCCESS
    }

    /** Flowbite's {@code type}, limited to what a JavaFX text field can express. */
    public enum Type {
        TEXT, PASSWORD
    }

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(this, "type", DEFAULT_TYPE);

    private final BooleanProperty shadow = new SimpleBooleanProperty(this, "shadow", false);

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObjectProperty<Ikon> rightIcon = new SimpleObjectProperty<>(this, "rightIcon");

    private final StringProperty addon = new SimpleStringProperty(this, "addon");

    public FxTextInput() {
        initialize();
    }

    public FxTextInput(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().addAll(FIELD_STYLE_CLASS, STYLE_CLASS);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        BooleanStyleClassSync.sync(this, SHADOW_STYLE_CLASS, shadow);

        // The padding that leaves room for an icon is CSS, so presence is a style class.
        icon.addListener((observable, oldValue, newValue) -> setPresent(ICON_LEFT_STYLE_CLASS, newValue != null));
        rightIcon.addListener((observable, oldValue, newValue) -> setPresent(ICON_RIGHT_STYLE_CLASS, newValue != null));

        // Masking happens in the skin when it builds its text node, so a new type needs a new skin.
        type.addListener((observable, oldValue, newValue) -> {
            if (getSkin() != null) {
                setSkin(createDefaultSkin());
            }
        });
    }

    private void setPresent(String styleClass, boolean present) {
        getStyleClass().remove(styleClass);
        if (present) {
            getStyleClass().add(styleClass);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FxTextInputSkin(this);
    }

    /** A password field never puts its text on the clipboard. */
    @Override
    public void cut() {
        if (getType() != Type.PASSWORD) {
            super.cut();
        }
    }

    /** A password field never puts its text on the clipboard. */
    @Override
    public void copy() {
        if (getType() != Type.PASSWORD) {
            super.copy();
        }
    }

    // ---- properties
    // -----------------------------------------------------------------------------

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

    public Type getType() {
        return type.get();
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    /** @return whether the field has a drop shadow (Flowbite's {@code shadow}) */
    public boolean isShadow() {
        return shadow.get();
    }

    public void setShadow(boolean shadow) {
        this.shadow.set(shadow);
    }

    public BooleanProperty shadowProperty() {
        return shadow;
    }

    public Ikon getIcon() {
        return icon.get();
    }

    /** Shows an icon at the start of the field (Flowbite's {@code icon}); {@code null} removes it. */
    public void setIcon(Ikon icon) {
        this.icon.set(icon);
    }

    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public Ikon getRightIcon() {
        return rightIcon.get();
    }

    /** Shows an icon at the end of the field (Flowbite's {@code rightIcon}); {@code null} removes it. */
    public void setRightIcon(Ikon rightIcon) {
        this.rightIcon.set(rightIcon);
    }

    public ObjectProperty<Ikon> rightIconProperty() {
        return rightIcon;
    }

    public String getAddon() {
        return addon.get();
    }

    /** Shows a text box before the field's text (Flowbite's {@code addon}); {@code null} or empty removes it. */
    public void setAddon(String addon) {
        this.addon.set(addon);
    }

    public StringProperty addonProperty() {
        return addon;
    }
}
