package dev.fxkit.core.components.megamenu;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A vertical list of links inside a mega menu dropdown: Flowbite's
 * {@code <div className="space-y-4 p-4">} (16px between rows, 16px padding).
 *
 * <pre>{@code
 * FxMegaMenuColumn column = new FxMegaMenuColumn(
 *         new FxMegaMenuLink("About Us"),
 *         new FxMegaMenuLink("Library"));
 * }</pre>
 *
 * <p>Set {@link #flushProperty() flush} to drop the padding, for lists that sit directly in a grid
 * that already has its own (Flowbite's plain {@code <ul className="space-y-4">}).
 */
public class FxMegaMenuColumn extends VBox {

    private final BooleanProperty flush = new SimpleBooleanProperty(this, "flush", false);

    public FxMegaMenuColumn(Node... children) {
        super(children);
        getStyleClass().add("fxk-megamenu-column");
        BooleanStyleClassSync.sync(this, "fxk-megamenu-column-flush", flush);
    }

    /**
     * @return {@code true} to remove the 16px padding
     */
    public final BooleanProperty flushProperty() {
        return flush;
    }

    public final boolean isFlush() {
        return flush.get();
    }

    public final void setFlush(boolean flush) {
        this.flush.set(flush);
    }
}
