package dev.fxkit.core.components.sidebar;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A vertical group of {@link FxSidebarItem}s and {@link FxSidebarCollapse}s, mirroring Flowbite's
 * {@code <SidebarItemGroup>}. Items are 8px apart. When an {@link FxSidebar} holds several groups,
 * every group after the first is separated from the previous one by a horizontal line (Flowbite's
 * "content separator").
 *
 * <p>Must be a direct child of {@link FxSidebar#getContent()} for the first-group rule to apply.
 * The default FXML property is {@code children}.
 */
public class FxSidebarItemGroup extends VBox {

    public static final String STYLE_CLASS = "fxk-sidebar-group";

    public FxSidebarItemGroup() {
        getStyleClass().add(STYLE_CLASS);
    }

    /** Convenience constructor adding {@code items} in order. */
    public FxSidebarItemGroup(Node... items) {
        this();
        getChildren().addAll(items);
    }
}
