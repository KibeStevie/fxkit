package dev.fxkit.core.components.blockquote;

import java.util.Locale;
import java.util.Objects;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A quoted piece of text (Flowbite React's {@code <Blockquote>}).
 *
 * <p>Flowbite's theme gives the blockquote one look, {@code "text-xl font-semibold italic text-gray-900
 * dark:text-white"}, and everything else (alignment, size, solid background, ...) is done by passing
 * Tailwind classes. FXKit turns those class combinations into properties:
 *
 * <pre>
 * FxBlockquote (VBox, .fxk-blockquote)
 * ├─ quote icon (Region, .fxk-blockquote-icon)   only when {@link #iconVisibleProperty() iconVisible}
 * └─ ONE of: custom content (any Node) | text (Label, .fxk-blockquote-text, wraps)
 * </pre>
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * root.base  text-xl font-semibold italic text-gray-900   defaults: Size.MD, Weight.SEMIBOLD, italic
 * text-lg / text-xl / text-2xl                            Size.SM / MD / LG   (16 / 18 / 24px)
 * text-left / text-center / text-right                    TextAlign.LEFT / CENTER / RIGHT
 * font-medium / font-semibold                             Weight.MEDIUM / SEMIBOLD
 * border-l-4 border-gray-300 bg-gray-50 p-4               solid = true
 * &lt;svg&gt; quote glyph above the text                       iconVisible = true, or {@link #quoteIcon(IconSize)}
 * children                                                text / content
 * </pre>
 * Flowbite's {@code my-4} / {@code mb-3} margins are layout, not style: give the blockquote a
 * margin with {@link VBox#setMargin(Node, javafx.geometry.Insets)} or the parent's spacing.
 *
 * <p>The quote marks inside the text are part of the text, exactly as in Flowbite: write
 * {@code "\"Flowbite is just awesome.\""} if you want them.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Default blockquote
 * FxBlockquote quote = new FxBlockquote("\"Flowbite is just awesome.\"");
 *
 * // Solid background
 * quote.setSolid(true);
 *
 * // Blockquote icon (icon above the text)
 * quote.setIconVisible(true);
 *
 * // Alignment and size
 * quote.setTextAlignment(FxBlockquote.TextAlign.CENTER);
 * quote.setSize(FxBlockquote.Size.LG);
 *
 * // User testimonial: icon, blockquote and caption stacked in a centered figure
 * FxAvatar avatar = new FxAvatar();
 * avatar.setSize(FxAvatar.Size.XS);
 * avatar.setRounded(true);
 * FxBlockquote text = new FxBlockquote("\"Flowbite is just awesome.\"");
 * text.setSize(FxBlockquote.Size.LG);
 * text.setWeight(FxBlockquote.Weight.MEDIUM);
 * text.setTextAlignment(FxBlockquote.TextAlign.CENTER);
 * FxBlockquoteCaption caption = new FxBlockquoteCaption(avatar, "Micheal Gough", "CEO at Google");
 * caption.setCentered(true);
 * VBox figure = new VBox(FxBlockquote.quoteIcon(FxBlockquote.IconSize.LG), text, caption);
 * figure.setAlignment(Pos.TOP_CENTER);
 * }</pre>
 *
 * <p>Styling lives in {@code components.css}; every color follows the light/dark theme.
 */
public class FxBlockquote extends VBox {

    /** Text size (Flowbite's {@code text-lg} / {@code text-xl} / {@code text-2xl}). */
    public enum Size {
        /** 16px (Flowbite {@code text-lg}). */
        SM,
        /** 18px (Flowbite {@code text-xl}, the default). */
        MD,
        /** 24px (Flowbite {@code text-2xl}). */
        LG
    }

    /** Horizontal alignment of the text (Flowbite's {@code text-left} / {@code text-center} / {@code text-right}). */
    public enum TextAlign {
        /** The default. */
        LEFT,
        CENTER,
        RIGHT
    }

    /** Font weight (Flowbite's {@code font-semibold}, or the {@code font-medium} the testimonial example uses). */
    public enum Weight {
        /** 500. */
        MEDIUM,
        /** 600, the default. */
        SEMIBOLD
    }

    /** Size of the quote glyph (Flowbite's {@code h-8 w-8} in the icon example, {@code h-10 w-10} in the testimonial). */
    public enum IconSize {
        /** 32px (the default). */
        MD,
        /** 40px. */
        LG
    }

    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<TextAlign> textAlignment =
            new SimpleObjectProperty<>(this, "textAlignment", TextAlign.LEFT);
    private final ObjectProperty<Weight> weight = new SimpleObjectProperty<>(this, "weight", Weight.SEMIBOLD);
    private final BooleanProperty solid = new SimpleBooleanProperty(this, "solid", false);
    private final BooleanProperty iconVisible = new SimpleBooleanProperty(this, "iconVisible", false);

    private final Label label = new Label();
    private final Region icon = quoteIcon(IconSize.MD);

    /** Creates an empty blockquote; set {@link #setText(String) text} or {@link #setContent(Node) content}. */
    public FxBlockquote() {
        getStyleClass().add("fxk-blockquote");

        label.getStyleClass().add("fxk-blockquote-text");
        label.setWrapText(true);
        // Fill the row, so the text can be aligned inside it, and never shrink below the wrapped height
        // (otherwise a Label ellipsizes instead of growing).
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.textProperty().bind(text);

        EnumStyleClassSync.sync(this, "fxk-blockquote-size-", size);
        EnumStyleClassSync.sync(this, "fxk-blockquote-align-", textAlignment);
        EnumStyleClassSync.sync(this, "fxk-blockquote-weight-", weight);
        BooleanStyleClassSync.sync(this, "fxk-blockquote-solid", solid);

        iconVisible.addListener(o -> refresh());
        content.addListener(o -> refresh());
        refresh();
    }

    /**
     * @param text the quote, including any quotation marks you want shown
     */
    public FxBlockquote(String text) {
        this();
        setText(text);
    }

    /**
     * @param content the node shown instead of the text, for example a {@code Label} with a
     *                different style, or several paragraphs in a {@code VBox}
     */
    public FxBlockquote(Node content) {
        this();
        setContent(content);
    }

    private void refresh() {
        Node body = content.get() != null ? content.get() : label;
        if (isIconVisible()) {
            getChildren().setAll(icon, body);
        } else {
            getChildren().setAll(body);
        }
    }

    // ---- building block for figures ---------------------------------------------------------

    /**
     * The quote glyph on its own, for figures where it sits outside the blockquote (Flowbite's
     * testimonial example puts the {@code <svg>} before the {@code <Blockquote>}). Painted from CSS
     * with the 18x14 Flowbite path; its color follows the theme.
     *
     * @param size the glyph's size
     * @return a styled {@link Region}
     */
    public static Region quoteIcon(IconSize size) {
        Objects.requireNonNull(size, "size");
        Region glyph = new Region();
        glyph.getStyleClass().addAll("fxk-blockquote-icon",
                "fxk-blockquote-icon-" + size.name().toLowerCase(Locale.ROOT));
        glyph.setMouseTransparent(true);
        glyph.setFocusTraversable(false);
        return glyph;
    }

    // ---- properties -------------------------------------------------------------------------

    /** The quote. Ignored while {@link #contentProperty() content} is set. Default empty. */
    public final StringProperty textProperty() { return text; }
    public final String getText() { return text.get(); }
    public final void setText(String value) { text.set(value); }

    /** A node shown instead of the text; {@code null} goes back to the text. */
    public final ObjectProperty<Node> contentProperty() { return content; }
    public final Node getContent() { return content.get(); }
    public final void setContent(Node value) { content.set(value); }

    /** Text size. Default {@link Size#MD}. {@code null} behaves like the default. */
    public final ObjectProperty<Size> sizeProperty() { return size; }
    public final Size getSize() { return size.get(); }
    public final void setSize(Size value) { size.set(value); }

    /**
     * Text alignment. Default {@link TextAlign#LEFT}. {@code null} behaves like the default. Named
     * {@code textAlignment} because {@link VBox#alignmentProperty()} (a {@code Pos}) already exists.
     */
    public final ObjectProperty<TextAlign> textAlignmentProperty() { return textAlignment; }
    public final TextAlign getTextAlignment() { return textAlignment.get(); }
    public final void setTextAlignment(TextAlign value) { textAlignment.set(value); }

    /** Font weight. Default {@link Weight#SEMIBOLD}. {@code null} behaves like the default. */
    public final ObjectProperty<Weight> weightProperty() { return weight; }
    public final Weight getWeight() { return weight.get(); }
    public final void setWeight(Weight value) { weight.set(value); }

    /** Solid background with a 4px left border (Flowbite's second example). Default {@code false}. */
    public final BooleanProperty solidProperty() { return solid; }
    public final boolean isSolid() { return solid.get(); }
    public final void setSolid(boolean value) { solid.set(value); }

    /** Shows a quote glyph above the text (32px). Default {@code false}. */
    public final BooleanProperty iconVisibleProperty() { return iconVisible; }
    public final boolean isIconVisible() { return iconVisible.get(); }
    public final void setIconVisible(boolean value) { iconVisible.set(value); }
}
