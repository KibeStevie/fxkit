package dev.fxkit.core.components;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

/**
 * A {@link Button} styled by FXKit's design tokens, with a typed {@link Variant} property so it can be
 * styled without writing CSS.
 *
 * <p>{@code FxButton} is a plain subclass of {@code Button} (see
 * {@code docs/adr/ADR-002-button-base-class.md} for why): every standard {@code Button}/{@code ButtonBase}
 * API — {@code setOnAction}, {@code setDefaultButton}, {@code setGraphic}, mnemonic parsing, keyboard
 * activation, and its accessibility role — keeps working exactly as it does today. The only thing
 * {@code FxButton} adds is {@link #variantProperty()}, which selects the look.
 *
 * <p>Requires FXKit's stylesheets to be installed on the scene (see
 * {@code dev.fxkit.core.theme.ThemeManager#apply}); without them the button falls back to the plain
 * JavaFX look.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxButton save = new FxButton("Save");
 * save.setVariant(FxButton.Variant.PRIMARY);
 * save.setOnAction(e -> saveForm());
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxButton text="Save" variant="PRIMARY" onAction="#handleSave"/>
 * }</pre>
 */
public class FxButton extends Button {

    /** Base style class every {@code FxButton} carries, regardless of variant. */
    public static final String STYLE_CLASS = "fxk-btn";

    /** Prefix used for the variant style class, e.g. {@code fxk-btn-danger} for {@link Variant#DANGER}. */
    private static final String VARIANT_STYLE_CLASS_PREFIX = "fxk-btn-";

    /** The default variant a new {@code FxButton} is created with. */
    public static final Variant DEFAULT_VARIANT = Variant.PRIMARY;

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

    private final ObjectProperty<Variant> variant =
            new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT);

    /**
     * Creates an {@code FxButton} with no text and the {@linkplain #DEFAULT_VARIANT default variant}.
     */
    public FxButton() {
        initialize();
    }

    /**
     * Creates an {@code FxButton} with the given text and the {@linkplain #DEFAULT_VARIANT default variant}.
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
}
