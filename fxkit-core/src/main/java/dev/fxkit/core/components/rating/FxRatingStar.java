package dev.fxkit.core.components.rating;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Region;

/**
 * One star of an {@link FxRating}: filled or empty (Flowbite's {@code <RatingStar filled={...} />}).
 *
 * <p>A star is a plain {@link Region} painted from CSS ({@code components.css}); it has no
 * behavior of its own. Its size comes from the enclosing {@link FxRating#sizeProperty() size}, and
 * its colors from the rating's {@code -fxk-rating-filled} / {@code -fxk-rating-empty} tokens, so
 * a star only looks right inside an {@code FxRating}.
 *
 * <pre>{@code
 * new FxRating(new FxRatingStar(), new FxRatingStar(), new FxRatingStar(false));
 * }</pre>
 */
public class FxRatingStar extends Region {

    private final BooleanProperty filled = new SimpleBooleanProperty(this, "filled", true);

    /** Creates a filled star. */
    public FxRatingStar() {
        this(true);
    }

    /**
     * @param filled whether the star is filled ({@code true}) or empty ({@code false})
     */
    public FxRatingStar(boolean filled) {
        this.filled.set(filled);
        getStyleClass().add("fxk-rating-star");
        BooleanStyleClassSync.sync(this, "fxk-rating-star-filled", this.filled);
        setFocusTraversable(false);
    }

    /** Whether the star is filled. Default {@code true}. */
    public final BooleanProperty filledProperty() {
        return filled;
    }

    public final boolean isFilled() {
        return filled.get();
    }

    public final void setFilled(boolean value) {
        filled.set(value);
    }
}
