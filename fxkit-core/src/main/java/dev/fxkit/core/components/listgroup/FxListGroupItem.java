package dev.fxkit.core.components.listgroup;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

/**
 * One row of an {@link FxListGroup}: Flowbite's {@code ListGroupItem}.
 *
 * <p>It is a {@link Button}, so it is focusable, fires {@code onAction} on click or Space, and goes
 * inert when {@link #setDisable(boolean) disabled}. That one class covers all three of Flowbite's
 * usages:
 * <ul>
 *   <li><b>plain item</b> - just text;</li>
 *   <li><b>button / link</b> - set {@link #setOnAction(javafx.event.EventHandler) onAction}. Flowbite's
 *       {@code href} and {@code onClick} look identical (its theme gives {@code href.on} and
 *       {@code href.off} the same classes), so FXKit has a single handler. To open a URL, call
 *       {@code HostServices.showDocument(...)} from it;</li>
 *   <li><b>icon</b> - set {@link #setIcon(Ikon) icon}.</li>
 * </ul>
 *
 * <p>Flowbite's {@code active} is {@link #activeProperty()}: a solid primary fill with
 * {@code on-primary} text. FXKit does not decide for you what makes an item active - the group has no
 * built-in selection - so set it from your own handler (see {@code ListGroupTestApp} for a
 * single-select example). {@code disabled} is the standard {@link #disableProperty()}.
 *
 * <p>Style classes: {@code fxk-list-group-item}, plus {@code fxk-list-group-item-active} while active.
 * The {@code -first} / {@code -last} position classes are added by the owning {@link FxListGroup}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * FxListGroupItem profile = new FxListGroupItem("Profile", FontAwesomeSolid.USER);
 * profile.setActive(true);
 * profile.setOnAction(e -> openProfile());
 *
 * FxListGroupItem download = new FxListGroupItem("Download");
 * download.setDisable(true);
 * }</pre>
 */
public class FxListGroupItem extends Button {

    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", false);
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    /** The graphic this item created for {@link #iconProperty()}, or {@code null} if it has none. */
    private FontIcon iconNode;

    /** Creates an item with no text. */
    public FxListGroupItem() {
        this(null, null);
    }

    /**
     * Creates an item with text.
     *
     * @param text the item's text; may be {@code null}
     */
    public FxListGroupItem(String text) {
        this(text, null);
    }

    /**
     * Creates an item with text and an icon.
     *
     * @param text the item's text; may be {@code null}
     * @param icon the icon shown before the text; may be {@code null}
     */
    public FxListGroupItem(String text, Ikon icon) {
        super(text);
        getStyleClass().add("fxk-list-group-item");
        setMaxWidth(Double.MAX_VALUE); // Flowbite: w-full
        setMnemonicParsing(false);     // "Save_as" is just text in a list, not a mnemonic

        BooleanStyleClassSync.sync(this, "fxk-list-group-item-active", active);
        this.icon.addListener((observable, oldValue, newValue) -> refreshIcon());
        setIcon(icon);
    }

    /**
     * Whether this item is the active one: Flowbite's {@code active} prop. Defaults to
     * {@code false}.
     *
     * @return the active property
     */
    public final BooleanProperty activeProperty() {
        return active;
    }

    public final boolean isActive() {
        return active.get();
    }

    public final void setActive(boolean value) {
        active.set(value);
    }

    /**
     * The icon shown before the text: Flowbite's {@code icon} prop. {@code null} (the default)
     * means no icon. This is a shorthand for {@link #setGraphic}: while it is non-{@code null} it
     * owns the graphic, and setting it back to {@code null} removes only the graphic it created.
     *
     * @return the icon property
     */
    public final ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public final Ikon getIcon() {
        return icon.get();
    }

    public final void setIcon(Ikon value) {
        icon.set(value);
    }

    private void refreshIcon() {
        Ikon ikon = icon.get();
        if (ikon == null) {
            if (iconNode != null && getGraphic() == iconNode) {
                setGraphic(null);
            }
            iconNode = null;
            return;
        }
        if (iconNode == null || getGraphic() != iconNode) {
            iconNode = new FontIcon();
            iconNode.getStyleClass().add("fxk-list-group-icon");
            setGraphic(iconNode);
        }
        iconNode.setIconCode(ikon);
    }
}
