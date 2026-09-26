package dev.fxkit.core.components;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.ClassNameStyler;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * A {@link Button} styled by FXKit's design tokens, with typed {@link Variant}, {@link Color} and
 * {@link Size} properties so it can be styled without writing CSS, plus a {@link #classNameProperty()}
 * escape hatch for anything those don't cover.
 *
 * <p>{@code FxButton} is a plain subclass of {@code Button}: every standard {@code Button}/
 * {@code ButtonBase} API keeps working exactly as it does today.
 *
 * <h2>Variant, Color and Size</h2>
 * Each of {@link #variantProperty()}, {@link #colorProperty()} and {@link #sizeProperty()} is kept in
 * sync with a style class via {@link EnumStyleClassSync}. {@link Color} has a visual effect under two
 * variants only - {@link Variant#DEFAULT} (solid fill) and {@link Variant#OUTLINE} (border + text,
 * filling solid on hover, Flowbite React's {@code <Button outline color="...">}) - both styled in
 * {@code colors.css}. Every other variant (PRIMARY, DANGER, ...) pins its own colors from semantic
 * tokens in {@code components.css} and never carries a {@code fxk-btn-color-*} class's selector match.
 *
 * <h2>Pill</h2>
 * {@link #pillProperty()} fully rounds the button's corners (Tailwind's {@code rounded-full}),
 * independent of {@code Variant}, {@code Color} or {@code Size}.
 *
 * <h2>className</h2>
 * {@link #classNameProperty()} accepts Tailwind-like utility tokens, resolved to inline style by
 * {@link ClassNameStyler} - see that class's javadoc for the full grammar, including gradients
 * ({@code "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br"}) and
 * pseudo-class scoping ({@code hover:}, {@code pressed:}, {@code armed:}, {@code focus:},
 * {@code disabled:}). Because it resolves to inline style, {@code className} always overrides
 * {@code Variant}/{@code Color} for the same CSS property without ever touching their style classes.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxButton save = new FxButton("Save");
 * save.setVariant(FxButton.Variant.PRIMARY);
 * save.setSize(FxButton.Size.LG);
 * save.setOnAction(e -> saveForm());
 *
 * FxButton outlined = new FxButton("Green");
 * outlined.setVariant(FxButton.Variant.OUTLINE);
 * outlined.setColor(FxButton.Color.GREEN);
 * outlined.setPill(true);
 *
 * FxButton gradient = new FxButton("Blue");
 * gradient.setClassName(
 *     "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 text-white hover:bg-gradient-to-br");
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxButton text="Save" variant="PRIMARY" size="LG" onAction="#handleSave"/>
 * <FxButton text="Green" variant="OUTLINE" color="GREEN" pill="true"/>
 * }</pre>
 */
public class FxButton extends Button {

    public static final String STYLE_CLASS = "fxk-btn";

    private static final String VARIANT_STYLE_CLASS_PREFIX = "fxk-btn-";
    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-btn-size-";
    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-btn-color-";
    private static final String PILL_STYLE_CLASS = "fxk-btn-pill";

    public static final Variant DEFAULT_VARIANT = Variant.PRIMARY;
    public static final Size DEFAULT_SIZE = Size.MD;
    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /**
     * The look of an {@code FxButton}. {@link #DEFAULT} and {@link #OUTLINE} take their color from
     * {@link #colorProperty()} instead of a fixed semantic token; every other variant is styled from
     * semantic tokens only, so it looks right in both light and dark themes regardless of
     * {@code Color}.
     */
    public enum Variant {

        /** The main, high-emphasis action on a screen. Solid fill using the brand/primary token. */
        PRIMARY,

        /**
         * Solid fill whose color comes from {@link #colorProperty()} instead of a fixed semantic
         * token. {@link Color#DEFAULT} uses the primary token; combine with any other {@link Color}
         * for flowbite-style named colors. {@code Color} has no effect under any variant except this
         * one and {@link #OUTLINE}.
         */
        DEFAULT,

        /** A lower-emphasis action, often paired with a {@link #PRIMARY} button. Neutral, outlined fill. */
        SECONDARY,

        /** A destructive or irreversible action (delete, remove, discard). Solid fill using the danger token. */
        DANGER,

        /** A positive or confirming action (confirm, complete, approve). Solid fill using the success token. */
        SUCCESS,

        /** The lowest-emphasis action: no fill or border until hovered or pressed. */
        GHOST,

        /**
         * A medium-emphasis action: no fill, but a visible border, filling solid on hover/press -
         * Flowbite React's {@code <Button outline color="...">}. Colored by {@link #colorProperty()}
         * exactly like {@link #DEFAULT}; {@link Color#DEFAULT} uses the primary token.
         */
        OUTLINE
    }

    /**
     * A named fill for {@link Variant#DEFAULT} (solid) and {@link Variant#OUTLINE} (border + text,
     * filling solid on hover), both styled in {@code colors.css}. Has no visual effect under any
     * other variant.
     */
    public enum Color {
        DEFAULT, ALTERNATIVE, DARK, LIGHT,
        BLUE, CYAN, GRAY, GREEN, INDIGO,
        LIME, PINK, PURPLE, RED, TEAL, YELLOW
    }

    /**
     * The size of an {@code FxButton}. Independent of {@link Variant}: any size can be combined with
     * any variant.
     */
    public enum Size {
        /** 32px tall, compact horizontal padding, smallest text. Dense toolbars, table row actions. */
        XS,

        /** 36px tall. Secondary actions in a form or toolbar. */
        SM,

        /** The default: 40px tall, base text. */
        MD,

        /** 48px tall, base text. Primary calls to action. */
        LG,

        /** 52px tall, base text, roomiest padding. Hero sections, standalone CTAs. */
        XL
    }

    private final ObjectProperty<Variant> variant =
            new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT);

    private final ObjectProperty<Size> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<Color> color =
            new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final BooleanProperty pill = new SimpleBooleanProperty(this, "pill", false);

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final StringProperty className = new SimpleStringProperty(this, "className", "");

    public FxButton() {
        initialize();
    }

    public FxButton(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, VARIANT_STYLE_CLASS_PREFIX, variant);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        BooleanStyleClassSync.sync(this, PILL_STYLE_CLASS, pill);
        icon.addListener((observable, oldValue, newValue) -> applyIcon(newValue));
        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(this, newValue));
    }

    private void applyIcon(Ikon icon) {
        if (icon == null) {
            if (getGraphic() instanceof FontIcon) {
                setGraphic(null);
            }
            return;
        }
        if (getGraphic() instanceof FontIcon fontIcon) {
            fontIcon.setIconCode(icon);
        } else {
            setGraphic(new FontIcon(icon));
        }
    }

    public Variant getVariant() { return variant.get(); }
    public void setVariant(Variant variant) { this.variant.set(variant); }
    public ObjectProperty<Variant> variantProperty() { return variant; }

    public Size getSize() { return size.get(); }
    public void setSize(Size size) { this.size.set(size); }
    public ObjectProperty<Size> sizeProperty() { return size; }

    /**
     * @return the button's named color; only visible under {@link Variant#DEFAULT} or
     *         {@link Variant#OUTLINE}
     */
    public Color getColor() { return color.get(); }

    /**
     * Sets the button's named color. Only has a visible effect when {@link #getVariant()} is
     * {@link Variant#DEFAULT} (solid fill) or {@link Variant#OUTLINE} (border + text, filling solid
     * on hover) - every other variant pins its own colors and ignores this property.
     *
     * @param color the named color to apply
     */
    public void setColor(Color color) { this.color.set(color); }
    public ObjectProperty<Color> colorProperty() { return color; }

    /**
     * @return whether the button's corners are fully rounded (Tailwind's {@code rounded-full})
     */
    public boolean isPill() { return pill.get(); }

    /**
     * Fully rounds the button's corners, independent of {@code Variant}/{@code Color}/{@code Size}.
     *
     * @param pill {@code true} for fully rounded corners
     */
    public void setPill(boolean pill) { this.pill.set(pill); }
    public BooleanProperty pillProperty() { return pill; }

    /**
     * @return the button's current {@code className} string (never {@code null}; empty if unset)
     */
    public String getClassName() { return className.get(); }

    /**
     * Sets arbitrary Tailwind-like utility tokens; see the class javadoc's "className" section for the
     * grammar. Pass {@code ""} or {@code null} to clear everything this property previously applied.
     *
     * @param className space-separated utility tokens, optionally pseudo-class-scoped
     */
    public void setClassName(String className) { this.className.set(className); }
    public StringProperty classNameProperty() { return className; }

    public Ikon getIcon() { return icon.get(); }

    public void setIcon(Ikon icon) {
        this.icon.set(icon);
    }

    public void setIcon(Ikon icon, String accessibleText) {
        setIcon(icon);
        setAccessibleText(accessibleText);
        if (getTooltip() == null) {
            setTooltip(new Tooltip(accessibleText));
        }
    }

    public ObjectProperty<Ikon> iconProperty() { return icon; }
}