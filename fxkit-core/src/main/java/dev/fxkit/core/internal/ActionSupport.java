package dev.fxkit.core.internal;

import java.util.Objects;

import javafx.event.EventTarget;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Makes a plain (non-control) node behave like a button: focusable, announced as a button, and
 * activated by a primary-button click or by Enter / Space while it has the focus.
 *
 * <p>Used by components that are laid out with ordinary panes (for example a title + description
 * item, or a toggle with its chevron pushed to the far end) where subclassing {@code Button} would
 * make that layout impossible.
 *
 * <h2>Keyboard</h2>
 * <ul>
 *   <li>Holding Enter or Space activates <em>once</em>, not once per auto-repeat; releasing the key
 *       re-arms it. Repeats are swallowed so they do not scroll a surrounding pane either.</li>
 *   <li>Only a key pressed while the node itself has the focus counts. Space or Enter typed in a
 *       focusable child (a text field, a button inside the item) belongs to that child and is left
 *       alone; before, it bubbled up and also activated the parent.</li>
 * </ul>
 *
 * <h2>Mouse</h2>
 * A primary click anywhere on the node activates it, including on its labels and icons. A click on a
 * focusable child (a close button inside the item, a text field) is that child's business and does
 * not also activate the parent.
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
            if (event.getButton() == MouseButton.PRIMARY
                    && !node.isDisabled()
                    && !isInsideFocusableChild(event.getTarget(), node)) {
                event.consume();
                action.run();
            }
        });

        KeyActivation keys = new KeyActivation();
        node.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (keys.pressed(event.getCode(), event.getTarget() == node, !node.isDisabled())) {
                case FIRE -> {
                    event.consume();
                    action.run();
                }
                case CONSUME -> event.consume();
                case IGNORE -> {
                    // not for us: let it bubble
                }
            }
        });
        node.addEventHandler(KeyEvent.KEY_RELEASED, event -> keys.released(event.getCode()));
        node.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                keys.reset();
            }
        });
        node.disabledProperty().addListener((observable, wasDisabled, isDisabled) -> {
            if (isDisabled) {
                keys.reset();
            }
        });
    }

    /**
     * Whether the click landed on, or inside, a child of {@code root} that takes the focus itself:
     * a button, a text field, another {@code ActionSupport} node. Labels, icons and plain panes do
     * not, so a click on them still counts as a click on {@code root}.
     */
    private static boolean isInsideFocusableChild(EventTarget target, Node root) {
        Node node = target instanceof Node hit ? hit : null;
        while (node != null && node != root) {
            if (node.isFocusTraversable()) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }
}
