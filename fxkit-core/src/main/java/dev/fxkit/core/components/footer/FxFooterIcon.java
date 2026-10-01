package dev.fxkit.core.components.footer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Flowbite's {@code <FooterIcon icon={...} href="...">}: a small (20px) icon link, used for social
 * media accounts.
 *
 * <p>The icon is an Ikonli {@link Ikon} (the same icon type {@code FxButton.setIcon} takes), so
 * react-icons' {@code BsFacebook} becomes e.g. {@code FontAwesomeBrands.FACEBOOK}. It is muted, and
 * turns brighter on hover in the dark theme (Flowbite: {@code dark:hover:text-white}).
 *
 * <p>An icon-only link has no text for a screen reader, so give it one:
 * {@code icon.setAccessibleText("Facebook")}. Style class {@code fxk-footer-icon}.
 */
public class FxFooterIcon extends Hyperlink {

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    public FxFooterIcon(Ikon icon) {
        getStyleClass().add("fxk-footer-icon");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.icon.addListener((o, oldIcon, newIcon) -> {
            if (newIcon == null) {
                setGraphic(null);
            } else if (getGraphic() instanceof FontIcon fontIcon) {
                fontIcon.setIconCode(newIcon);
            } else {
                setGraphic(new FontIcon(newIcon));
            }
        });
        setIcon(icon);
    }

    public final ObjectProperty<Ikon> iconProperty() { return icon; }
    public final Ikon getIcon() { return icon.get(); }
    public final void setIcon(Ikon value) { icon.set(value); }
}
