package dev.fxkit.core.components.sidebar;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Package-private helpers shared by the sidebar components.
 *
 * <p>The main one is {@link #bindCollapsed}: Flowbite passes "is the sidebar collapsed" down through
 * React context. JavaFX has no context, so every item, collapse, and logo keeps a read-only
 * {@code collapsed} property that this class binds to the nearest {@link FxSidebar} ancestor.
 *
 * <p>The lookup runs whenever the node's {@code scene} changes, not when its {@code parent} changes.
 * A node's scene is only set once the whole chain up to the root is attached, so it does not matter
 * in which order you assemble the tree. (FxSidebar's ScrollPane creates its content's parent chain
 * lazily with its skin, which would break a parent-based lookup.)
 */
final class SidebarSupport {

    private SidebarSupport() {
        // static utility class, not meant to be instantiated
    }

    /** Keeps {@code collapsed} equal to the nearest ancestor sidebar's {@code collapsed} (false if none). */
    static void bindCollapsed(Node node, ReadOnlyBooleanWrapper collapsed) {
        node.sceneProperty().addListener((observable, oldScene, newScene) -> syncCollapsed(node, collapsed));
        syncCollapsed(node, collapsed);
    }

    private static void syncCollapsed(Node node, ReadOnlyBooleanWrapper collapsed) {
        collapsed.unbind();
        for (Node n = node.getParent(); n != null; n = n.getParent()) {
            if (n instanceof FxSidebar sidebar) {
                collapsed.bind(sidebar.collapsedProperty());
                return;
            }
        }
        collapsed.set(false);
    }

    /** Runs {@code action} on a primary-button click, or on Enter / Space while the node has focus. */
    static void onActivate(Node node, Runnable action) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                action.run();
            }
        });
        node.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                action.run();
                event.consume();
            }
        });
    }

    /** Shows or hides a node, taking it out of layout when hidden (Tailwind's {@code hidden}). */
    static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    static void setStyleClassPresent(Node node, String styleClass, boolean present) {
        if (present) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    /** First code point of {@code text} as a string: what an icon-less item shows when collapsed. */
    static String initial(String text) {
        return text.substring(0, text.offsetByCodePoints(0, 1));
    }
}
