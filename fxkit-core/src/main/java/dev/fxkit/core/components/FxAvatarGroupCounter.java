package dev.fxkit.core.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Hyperlink;

/**
 * The "+99" bubble at the end of an {@link FxAvatarGroup} (Flowbite's {@code <Avatar.Counter>}).
 * Flowbite renders a link; here that is a {@link Hyperlink}, so use {@code setOnAction} for the
 * "view all users" behavior.
 */
public class FxAvatarGroupCounter extends Hyperlink {

    private final IntegerProperty total = new SimpleIntegerProperty(this, "total", 0);

    public FxAvatarGroupCounter() {
        this(0);
    }

    /** @param total number shown as {@code +total} */
    public FxAvatarGroupCounter(int total) {
        getStyleClass().add("fxk-avatar-counter");
        this.total.addListener((o, ov, nv) -> refreshText());
        setTotal(total);
        refreshText();
    }

    private void refreshText() {
        setText("+" + getTotal());
    }

    public final IntegerProperty totalProperty() { return total; }
    public final int getTotal() { return total.get(); }
    public final void setTotal(int value) { total.set(value); }
}
