package dev.fxkit.core.components.rating;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * A row of {@link FxRatingStar}s, optionally followed by text (Flowbite's {@code <Rating>}).
 *
 * <p>Like Flowbite, an {@code FxRating} is a plain container: put stars in it, then whatever should
 * sit next to them. {@link #of(int, int)} builds the common "n of m stars" row in one call, and
 * {@link #text}, {@link #score}, {@link #dot()} and {@link #link} create the pieces of Flowbite's
 * "with text" and "count" examples.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Default rating
 * FxRating.of(4, 5);
 *
 * // Rating with text
 * FxRating withText = FxRating.of(4, 5);
 * withText.getChildren().add(FxRating.text("4.95 out of 5"));
 *
 * // Rating count
 * Hyperlink reviews = FxRating.link("73 reviews");
 * reviews.setOnAction(e -> showReviews());
 * FxRating count = new FxRating(new FxRatingStar(), FxRating.score("4.95"), FxRating.dot(), reviews);
 *
 * // Star sizing
 * FxRating big = FxRating.of(4, 5);
 * big.setSize(FxRating.Size.LG);
 * }</pre>
 *
 * <p>Styling lives in {@code components.css}; the stars' colors follow the light/dark theme.
 */
public class FxRating extends HBox {

    /** Star size (Flowbite's {@code size} prop). */
    public enum Size {
        /** 20px stars (Flowbite {@code sm}, the default). */
        SM,
        /** 28px stars. */
        MD,
        /** 40px stars. */
        LG
    }

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.SM);

    /** Creates an empty rating; add {@link FxRatingStar}s and other nodes to {@link #getChildren()}. */
    public FxRating() {
        getStyleClass().add("fxk-rating");
        EnumStyleClassSync.sync(this, "fxk-rating-size-", size);
        // Children keep their own height and are centered, so a 20px star and a 15px label line up.
        setFillHeight(false);
    }

    /**
     * @param children the stars and other nodes to show, in order
     */
    public FxRating(Node... children) {
        this();
        getChildren().addAll(children);
    }

    /**
     * @param size     the star size
     * @param children the stars and other nodes to show, in order
     */
    public FxRating(Size size, Node... children) {
        this(children);
        setSize(size);
    }

    /**
     * Creates a rating of {@code total} stars of which the first {@code filled} are filled.
     *
     * @param filled number of filled stars, from 0 to {@code total}
     * @param total  number of stars
     * @return a new rating
     * @throws IllegalArgumentException if {@code total} is negative or {@code filled} is outside
     *                                  {@code 0..total}
     */
    public static FxRating of(int filled, int total) {
        if (total < 0 || filled < 0 || filled > total) {
            throw new IllegalArgumentException(
                    "filled must be between 0 and total, was filled=" + filled + ", total=" + total);
        }
        FxRating rating = new FxRating();
        for (int i = 0; i < total; i++) {
            rating.getChildren().add(new FxRatingStar(i < filled));
        }
        rating.setAccessibleText(filled + " out of " + total + " stars");
        return rating;
    }

    // ---- building blocks for text next to the stars ----------------------------------------

    /**
     * Muted label to put after the stars, for example {@code "4.95 out of 5"}
     * (Flowbite: {@code ml-2 text-sm font-medium text-gray-500}).
     *
     * @param text the text to show
     * @return a styled {@link Label}
     */
    public static Label text(String text) {
        Label label = new Label(Objects.requireNonNull(text, "text"));
        label.getStyleClass().add("fxk-rating-text");
        return label;
    }

    /**
     * Bold score to put after the stars, for example {@code "4.95"}
     * (Flowbite: {@code ml-2 text-sm font-bold text-gray-900}).
     *
     * @param score the score to show
     * @return a styled {@link Label}
     */
    public static Label score(String score) {
        Label label = new Label(Objects.requireNonNull(score, "score"));
        label.getStyleClass().add("fxk-rating-score");
        return label;
    }

    /**
     * Small round separator between a score and a link
     * (Flowbite: {@code mx-1.5 h-1 w-1 rounded-full bg-gray-500}).
     *
     * @return a styled {@link Region}
     */
    public static Region dot() {
        Region dot = new Region();
        dot.getStyleClass().add("fxk-rating-dot");
        // mx-1.5 (6px) minus the 2px gap FxRating puts between its children.
        HBox.setMargin(dot, new Insets(0, 4, 0, 4));
        return dot;
    }

    /**
     * Underlined link such as {@code "73 reviews"}; set its action with
     * {@link Hyperlink#setOnAction}
     * (Flowbite: {@code text-sm font-medium text-gray-900 underline hover:no-underline}).
     *
     * @param text the link text
     * @return a styled {@link Hyperlink}
     */
    public static Hyperlink link(String text) {
        Hyperlink link = new Hyperlink(Objects.requireNonNull(text, "text"));
        link.getStyleClass().add("fxk-rating-link");
        return link;
    }

    // ---- size -------------------------------------------------------------------------------

    /** Star size. Default {@link Size#SM}. */
    public final ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public final Size getSize() {
        return size.get();
    }

    public final void setSize(Size value) {
        size.set(value);
    }
}
