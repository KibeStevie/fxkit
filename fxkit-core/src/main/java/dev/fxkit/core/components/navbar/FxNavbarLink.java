package dev.fxkit.core.components.navbar;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Hyperlink;

/**
 * One menu entry of an {@link FxNavbar} (Flowbite's {@code <NavbarLink>}).
 *
 * <p>It is a {@link Hyperlink}: the click handler is {@code setOnAction} (Flowbite's {@code href} /
 * {@code onClick}) and {@code setDisable(true)} gives Flowbite's {@code disabled} look.
 * {@link #activeProperty() active} marks the current page.
 *
 * <pre>{@code
 * FxNavbarLink home = new FxNavbarLink("Home", true);
 * FxNavbarLink about = new FxNavbarLink("About");
 * about.setOnAction(e -> router.go("/about"));
 * }</pre>
 */
public class FxNavbarLink extends Hyperlink {

    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", false);

    public FxNavbarLink() {
        this(null, false);
    }

    /**
     * @param text the label
     */
    public FxNavbarLink(String text) {
        this(text, false);
    }

    /**
     * @param text   the label
     * @param active whether this link is the current page
     */
    public FxNavbarLink(String text, boolean active) {
        super(text);
        getStyleClass().add("fxk-navbar-link");
        setMnemonicParsing(false);
        setMaxWidth(Double.MAX_VALUE); // stacked rows fill the navbar's width
        BooleanStyleClassSync.sync(this, "fxk-navbar-link-active", this.active);
        setActive(active);
    }

    /** A link is never "visited": Hyperlink would otherwise restyle it after the first click. */
    @Override
    public void fire() {
        super.fire();
        setVisited(false);
    }

    /**
     * @return whether this link is the current page (Flowbite's {@code active})
     */
    public final BooleanProperty activeProperty() {
        return active;
    }

    public final boolean isActive() {
        return active.get();
    }

    public final void setActive(boolean active) {
        this.active.set(active);
    }
}