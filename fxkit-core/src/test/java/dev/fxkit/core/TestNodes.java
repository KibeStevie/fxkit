package dev.fxkit.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javafx.scene.Node;

/**
 * Finds nodes by style class without reflection, so tests can look inside a component's private
 * parts the way CSS would. {@code Node.lookup} needs no scene, so this works on unattached nodes.
 */
public final class TestNodes {

    private TestNodes() {
        // static utility class, not meant to be instantiated
    }

    /** @return the first node (the root included) that has {@code styleClass}, or {@code null} */
    public static <T extends Node> T find(Node root, String styleClass, Class<T> type) {
        Node hit = root.lookup("." + styleClass);
        return hit == null ? null : type.cast(hit);
    }

    /** Like {@link #find} but fails the test with a clear message when nothing has the style class. */
    public static <T extends Node> T require(Node root, String styleClass, Class<T> type) {
        T hit = find(root, styleClass, type);
        assertNotNull(hit, "no node with style class ." + styleClass + " inside " + root);
        return hit;
    }

    /** @return whether the root or any node below it has {@code styleClass} */
    public static boolean has(Node root, String styleClass) {
        return root.lookup("." + styleClass) != null;
    }
}
