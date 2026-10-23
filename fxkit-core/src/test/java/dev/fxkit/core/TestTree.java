package dev.fxkit.core;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.Parent;

/** Collects every node with a style class, in scene-graph order (the root first, then depth-first). */
public final class TestTree {

    private TestTree() {
        // static utility class, not meant to be instantiated
    }

    public static <T extends Node> List<T> all(Node root, String styleClass, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(root, styleClass, type, found);
        return found;
    }

    private static <T extends Node> void collect(Node node, String styleClass, Class<T> type, List<T> found) {
        if (node.getStyleClass().contains(styleClass)) {
            found.add(type.cast(node));
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, styleClass, type, found);
            }
        }
    }
}
