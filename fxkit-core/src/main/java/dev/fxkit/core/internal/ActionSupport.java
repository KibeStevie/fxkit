package dev.fxkit.core.internal;

import java.util.Objects;

import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Makes a plain (non-control) node behave like a button: focusable, announced as a button, and
 * activated by a primary-button click or by Enter / Space while focused.
 *
 * <p>Used by components that are laid out with ordinary panes (for example a title + description
 * item, or a toggle with its chevron pushed to the far end) where subclassing {@code Button} would
 * make that layout impossible.
 *
 * <p>Not part of FXKit's public API: internal, shared between components in
 * {@code dev.fxkit.core.components}.
 */
public final class ActionSupport {

    private ActionSupport() {
        // static utility class, not meant to be instantiated
    }

    /**
     * @param node   the node to make activatable
     * @param action what to run on activation; skipped while {@code node} is disabled
     * @throws NullPointerException if {@code node} or {@code action} is {@code null}
     */
    public static void install(Node node, Runnable action) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(action, "action");

        node.setFocusTraversable(true);
        node.setAccessibleRole(AccessibleRole.BUTTON);
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && !node.isDisabled()) {
                action.run();
                event.consume();
            }
        });
        node.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if ((code == KeyCode.ENTER || code == KeyCode.SPACE) && !node.isDisabled()) {
                action.run();
                event.consume();
            }
        });
    }
}
