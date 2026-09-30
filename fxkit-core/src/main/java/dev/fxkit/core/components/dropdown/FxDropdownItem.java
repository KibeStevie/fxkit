package dev.fxkit.core.components.dropdown;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * One clickable entry of an {@link FxDropdown} (Flowbite's {@code <DropdownItem>}).
 *
 * <p>It is a {@link Button}, so the click handler is the usual {@code onAction}, and it can be
 * disabled. When the owning dropdown has {@code dismissOnClick} set (the default), the menu closes
 * after the handler has run.
 *
 * <pre>{@code
 * FxDropdownItem settings = new FxDropdownItem("Settings", FontAwesomeSolid.COG);
 * settings.setOnAction(e -> openSettings());
 * }</pre>
 *
 * <p>Keyboard highlighting is not real focus: the dropdown marks one item as <em>active</em> (arrow
 * keys, typeahead, or the mouse moving over it) and styles it through the {@code :active}
 * pseudo-class, so the trigger button keeps focus the whole time.
 */
public class FxDropdownItem extends Button {

    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    public FxDropdownItem() {
        this(null, null);
    }

    /**
     * @param text the label
     */
    public FxDropdownItem(String text) {
        this(text, null);
    }

    /**
     * @param text the label
     * @param icon an Ikonli icon shown before the label, or {@code null} for none
     */
    public FxDropdownItem(String text, Ikon icon) {
        super(text);
        getStyleClass().add("fxk-dropdown-item");
        setMnemonicParsing(false);       // "Sign_out" should show an underscore, not a mnemonic
        setFocusTraversable(false);      // the dropdown drives highlighting; Tab must not land here
        setMaxWidth(Double.MAX_VALUE);   // fill the menu's width so the whole row is clickable

        this.icon.addListener((observable, oldIcon, newIcon) ->
                setGraphic(newIcon == null ? null : new FontIcon(newIcon)));
        setIcon(icon);
    }

    /**
     * @return the icon shown before the label (Flowbite's {@code icon} prop); may be {@code null}
     */
    public final ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public final Ikon getIcon() {
        return icon.get();
    }

    public final void setIcon(Ikon icon) {
        this.icon.set(icon);
    }

    /** Called by {@link FxDropdown} only. */
    void setActive(boolean active) {
        pseudoClassStateChanged(ACTIVE, active);
    }
}
