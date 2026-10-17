package dev.fxkit.core.components.drawer;

import javafx.beans.DefaultProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * The body of an {@link FxDrawer} (Flowbite's {@code DrawerItems}): a plain vertical container for
 * whatever the drawer shows under its {@link FxDrawerHeader}.
 *
 * <p>Flowbite's theme gives it no styling of its own ({@code items.base} is empty), and neither does
 * FXKit: add utility classes such as {@code p-4} when you want spacing.
 *
 * <pre>{@code
 * FxDrawerItems items = new FxDrawerItems(new Label("Hello"), new Button("Go"));
 * items.getStyleClass().add("p-4");
 * }</pre>
 */
@DefaultProperty("children")
public class FxDrawerItems extends VBox {

    public FxDrawerItems() {
        getStyleClass().add("fxk-drawer-items");
    }

    /** @param children the initial children */
    public FxDrawerItems(Node... children) {
        this();
        getChildren().addAll(children);
    }
}
