package dev.fxkit.core.components;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A {@link Button} styled by FXKit's design tokens, with a typed {@link Variant} property so it can be
 * styled without writing CSS.
 *
 * <p>{@code FxButton} is a plain subclass of {@code Button} (see
 * {@code docs/adr/ADR-002-button-base-class.md} for why): every standard {@code Button}/{@code ButtonBase}
 * API — {@code setOnAction}, {@code setDefaultButton}, {@code setGraphic}, mnemonic parsing, keyboard
 * activation, and its accessibility role — keeps working exactly as it does today. The only thing
 * {@code FxButton} adds is {@link #variantProperty()} (plus {@link #sizeProperty()} and
 * {@link #iconProperty()}), which select the look.
 *
 * <p>Requires FXKit's stylesheets to be installed on the scene (see
 * {@code dev.fxkit.core.theme.ThemeManager#apply}); without them the button falls back to the plain
 * JavaFX look.
 *
 * <h2>Icons (#36, stretch)</h2>
 * An icon can come from either of two places, and both work from Java or FXML:
 * <ul>
 *   <li>{@link #setIcon(Ikon)} for an <a href="https://kordamp.org/ikonli/">Ikonli</a> {@link Ikon} -
 *       FxButton wraps it in an {@code org.kordamp.ikonli.javafx.FontIcon} and installs that as the
 *       button's {@code graphic} for you, colored to match the current {@link Variant} (see
 *       {@code components.css}). Ikonli's icon-pack modules are optional: add whichever pack you want
 *       (e.g. {@code ikonli-materialdesign2-pack}) to your own project; FxKit only depends on
 *       {@code ikonli-core}/{@code ikonli-javafx}, never a specific pack.</li>
 *   <li>{@link #setGraphic(javafx.scene.Node)} (inherited from {@code Labeled}) for any other
 *       {@code Node} - unchanged from plain {@code Button}, including a hand-built {@code FontIcon} of
 *       your own (e.g. {@code <graphic><FontIcon iconLiteral="fas-save"/></graphic>} in FXML).</li>
 * </ul>
 * For an icon-only button, set accessible text or a tooltip so it remains usable with a screen reader -
 * {@link #setIcon(Ikon, String)} does both in one call.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxButton save = new FxButton("Save");
 * save.setVariant(FxButton.Variant.PRIMARY);
 * save.setSize(FxButton.Size.LG);
 * save.setOnAction(e -> saveForm());
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxButton text="Save" variant="PRIMARY" size="LG" onAction="#handleSave"/>
 * }</pre>
 */
public class FxButton extends Button {

    /** Base style class every {@code FxButton} carries, regardless of variant or size. */
    public static final String STYLE_CLASS = "fxk-btn";

    /** Prefix used for the variant style class, e.g. {@code fxk-btn-danger} for {@link Variant#DANGER}. */
    private static final String VARIANT_STYLE_CLASS_PREFIX = "fxk-btn-";

    /** Prefix used for the size style class, e.g. {@code fxk-btn-size-lg} for {@link Size#LG}. */
    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-btn-size-";

    /** The default variant a new {@code FxButton} is created with. */
    public static final Variant DEFAULT_VARIANT = Variant.PRIMARY;

    /** The default size a new {@code FxButton} is created with. */
    public static final Size DEFAULT_SIZE = Size.MD;

    /**
     * The look of an {@code FxButton}. Each variant is styled from semantic tokens only
     * (see {@code docs/design-tokens.md}), so it looks right in both light and dark themes.
     */
    public enum Variant {

        /** The main, high-emphasis action on a screen. Solid fill using the brand/primary token. */
        PRIMARY,

        /** A lower-emphasis action, often paired with a {@link #PRIMARY} button. Neutral, outlined fill. */
        SECONDARY,

        /** A destructive or irreversible action (delete, remove, discard). Solid fill using the danger token. */
        DANGER,

        /** A positive or confirming action (confirm, complete, approve). Solid fill using the success token. */
        SUCCESS,

        /** The lowest-emphasis action: no fill or border until hovered or pressed. */
        GHOST,

        /** A medium-emphasis action: no fill, but a visible border, using the primary token. */
        OUTLINE
    }

    /**
     * The size of an {@code FxButton}. Each size sets padding and font size from the same spacing and
     * font-size scales as the rest of FXKit ({@code tools/scales.txt}), independently of {@link Variant}:
     * any variant can be combined with any size.
     */
    public enum Size {

        /** Compact padding ({@code space-1}/{@code space-3}) and {@code text-sm} (12px). Dense layouts, toolbars. */
        SM,

        /** The default: {@code space-2}/{@code space-4} padding and {@code text-base} (14px). */
        MD,

        /** Roomier padding ({@code space-3}/{@code space-6}) and {@code text-lg} (16px). Primary calls to action. */
        LG
    }

    private final ObjectProperty<Variant> variant =
            new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT);

    private final ObjectProperty<Size> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    /**
     * Creates an {@code FxButton} with no text and the {@linkplain #DEFAULT_VARIANT default variant}
     * and {@linkplain #DEFAULT_SIZE default size}.
     */
    public FxButton() {
        initialize();
    }

    /**
     * Creates an {@code FxButton} with the given text, and the {@linkplain #DEFAULT_VARIANT default
     * variant} and {@linkplain #DEFAULT_SIZE default size}.
     *
     * @param text the button's text
     */
    public FxButton(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, VARIANT_STYLE_CLASS_PREFIX, variant);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
        icon.addListener((observable, oldValue, newValue) -> applyIcon(newValue));
    }

    /**
     * Puts {@code icon}'s glyph on the button's {@code graphic}, reusing the existing
     * {@code FontIcon} if the graphic already is one (so repeatedly changing the icon doesn't churn
     * through node instances), or clears the graphic if {@code icon} is {@code null} and it was FxButton
     * that put a {@code FontIcon} there in the first place. A graphic set directly via
     * {@link #setGraphic(javafx.scene.Node)} that isn't a {@code FontIcon} is left alone.
     */
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

    /**
     * @return the button's current variant
     */
    public Variant getVariant() {
        return variant.get();
    }

    /**
     * Sets the button's variant. Applies the new variant's style immediately and removes the previous
     * variant's style.
     *
     * @param variant the variant to apply; must not be {@code null}
     */
    public void setVariant(Variant variant) {
        this.variant.set(variant);
    }

    /**
     * @return the variant property, for binding or listening
     */
    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    /**
     * @return the button's current size
     */
    public Size getSize() {
        return size.get();
    }

    /**
     * Sets the button's size. Applies the new size's style immediately and removes the previous size's
     * style. Independent of {@link #setVariant(Variant)}: any size can be combined with any variant.
     *
     * @param size the size to apply; must not be {@code null}
     */
    public void setSize(Size size) {
        this.size.set(size);
    }

    /**
     * @return the size property, for binding or listening
     */
    public ObjectProperty<Size> sizeProperty() {
        return size;
    }

    /**
     * @return the button's current icon, or {@code null} if none is set
     */
    public Ikon getIcon() {
        return icon.get();
    }

    /**
     * Sets the button's icon from an Ikonli {@link Ikon}. Installs (or updates) a {@code FontIcon} as
     * the button's {@code graphic}, colored to match the current {@link Variant}; pass {@code null} to
     * remove it. For an icon-only button, pair this with {@link #setAccessibleText(String)} or
     * {@link #setTooltip(javafx.scene.control.Tooltip)} - or use {@link #setIcon(Ikon, String)}, which
     * does both - so the button stays usable with a screen reader.
     *
     * @param icon the icon to show, or {@code null} to clear it
     */
    public void setIcon(Ikon icon) {
        this.icon.set(icon);
    }

    /**
     * Sets the button's icon (see {@link #setIcon(Ikon)}) and also sets {@code accessibleText} as both
     * this button's {@linkplain #setAccessibleText(String) accessible text} and, if no
     * {@linkplain #setTooltip(javafx.scene.control.Tooltip) tooltip} is already set, a new tooltip with
     * that same text. Intended for icon-only buttons (no {@link #setText(String)}), where the icon alone
     * isn't accessible.
     *
     * @param icon           the icon to show, or {@code null} to clear it
     * @param accessibleText the accessible text (and, if none is set yet, tooltip text) to give the
     *                       button; must not be {@code null}
     */
    public void setIcon(Ikon icon, String accessibleText) {
        setIcon(icon);
        setAccessibleText(accessibleText);
        if (getTooltip() == null) {
            setTooltip(new Tooltip(accessibleText));
        }
    }

    /**
     * @return the icon property, for binding or listening
     */
    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }
}
