package dev.fxkit.core.internal;

import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

/**
 * Keeps one fixed style class on a {@link Node} in sync with the current value of a boolean
 * property.
 *
 * <p>Where {@link EnumStyleClassSync} swaps between several classes built from an enum's constants,
 * this is for the simpler case of a single style class that is either present or absent - for
 * example {@code FxButton}'s {@code pill} and {@code outline} flags. The class is added when the
 * property is {@code true} and removed when it is {@code false} or {@code null}.
 *
 * <p>Not part of FXKit's public API: internal, shared between components in
 * {@code dev.fxkit.core.components}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class FxButton extends Button {
 *     private final BooleanProperty pill = new SimpleBooleanProperty(this, "pill", false);
 *
 *     public FxButton() {
 *         BooleanStyleClassSync.sync(this, "fxk-btn-pill", pill);
 *     }
 * }
 * }</pre>
 */
public final class BooleanStyleClassSync {

    private BooleanStyleClassSync() {
        // static utility class, not meant to be instantiated
    }

    /**
     * Adds {@code styleClass} to {@code node} if {@code property} is currently {@code true}, then
     * keeps it in sync as {@code property} changes.
     *
     * @param node       the node whose {@link Node#getStyleClass()} is kept in sync
     * @param styleClass the style class to add or remove, for example {@code "fxk-btn-pill"}
     * @param property   the boolean property to track
     * @throws NullPointerException if {@code node}, {@code styleClass} or {@code property} is
     *                               {@code null}
     */
    public static void sync(Node node, String styleClass, ObservableValue<Boolean> property) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(styleClass, "styleClass");
        Objects.requireNonNull(property, "property");

        property.addListener((observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                if (!node.getStyleClass().contains(styleClass)) {
                    node.getStyleClass().add(styleClass);
                }
            } else {
                node.getStyleClass().remove(styleClass);
            }
        });

        if (Boolean.TRUE.equals(property.getValue())) {
            node.getStyleClass().add(styleClass);
        }
    }
}