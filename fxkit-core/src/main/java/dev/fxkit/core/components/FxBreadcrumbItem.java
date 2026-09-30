package dev.fxkit.core.components;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;

/**
 * One entry of an {@link FxBreadcrumb}: a label with an optional icon.
 *
 * <p>Flowbite's {@code BreadcrumbItem} is a link when it has an {@code href} and plain text when it
 * does not. JavaFX has no {@code href}, so the same rule is keyed to the action instead:
 * <ul>
 *   <li>with an {@code onAction} handler the item is <em>interactive</em>: clickable, keyboard
 *       focusable, and it gets a hover state (Flowbite's {@code href.on});</li>
 *   <li>without one it is the <em>current page</em>: muted, not clickable, not focusable
 *       (Flowbite's {@code href.off}).</li>
 * </ul>
 *
 * <p>It extends {@link Hyperlink} so text, graphic, mnemonic and keyboard activation (Space/Enter)
 * come for free; {@code components.css} strips Hyperlink's default look.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * new FxBreadcrumbItem("Home", FontAwesomeSolid.HOME, e -> navigateHome());
 * new FxBreadcrumbItem("Projects", e -> navigateProjects());
 * new FxBreadcrumbItem("FXKit"); // current page
 * }</pre>
 */
public class FxBreadcrumbItem extends Hyperlink {

    private static final String STYLE_CLASS = "fxk-breadcrumb-item";
    private static final String LINK_STYLE_CLASS = "fxk-breadcrumb-item-link";

    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    // Held in a field on purpose: a binding is only weakly referenced by the property it observes,
    // so without this strong reference it could be garbage-collected and the style class would
    // silently stop following onAction.
    private final BooleanBinding interactive;

    public FxBreadcrumbItem() {
        this(null, null, null);
    }

    public FxBreadcrumbItem(String text) {
        this(text, null, null);
    }

    public FxBreadcrumbItem(String text, EventHandler<ActionEvent> onAction) {
        this(text, null, onAction);
    }

    public FxBreadcrumbItem(String text, Ikon icon) {
        this(text, icon, null);
    }

    public FxBreadcrumbItem(String text, Ikon icon, EventHandler<ActionEvent> onAction) {
        super(text);
        getStyleClass().add(STYLE_CLASS);

        interactive = onActionProperty().isNotNull();
        BooleanStyleClassSync.sync(this, LINK_STYLE_CLASS, interactive);
        interactive.addListener((obs, was, is) -> updateInteractivity(is));
        updateInteractivity(interactive.get());

        this.icon.addListener((obs, oldIcon, newIcon) -> setGraphic(newIcon == null ? null : new FontIcon(newIcon)));

        setOnAction(onAction);
        setIcon(icon);
    }

    /** A current-page item must not take focus or swallow clicks; a link item must do both. */
    private void updateInteractivity(boolean isInteractive) {
        setFocusTraversable(isInteractive);
        setMouseTransparent(!isInteractive);
    }

    /**
     * @return the icon shown before the text (Flowbite's {@code icon} prop), or {@code null} for none
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

    /**
     * @return {@code true} if this item has an action, i.e. it is a link rather than the current page
     */
    public final boolean isInteractive() {
        return interactive.get();
    }
}
