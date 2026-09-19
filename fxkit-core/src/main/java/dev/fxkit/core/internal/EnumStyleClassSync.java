package dev.fxkit.core.internal;

import java.util.Locale;
import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

/**
 * Keeps one style class on a {@link Node} in sync with the current value of an enum property.
 *
 * <p>Every FXKit component that exposes a "which style" property (a {@code variant}, a {@code size},
 * and so on) uses this helper instead of writing its own listener. Given a node, a class-name prefix
 * and an {@link ObservableValue} of some enum type, exactly one style class built from that prefix and
 * the current value is present on the node at any time: setting a new value removes the class for the
 * old one and adds the class for the new one, in that order.
 *
 * <p>Not part of FXKit's public API: this class lives in an internal package so it can be shared freely
 * between components in {@code dev.fxkit.core.components} without becoming something downstream code
 * is expected to depend on.
 *
 * <h2>Naming</h2>
 * An enum constant {@code DANGER} with prefix {@code "fxk-btn-"} becomes the style class
 * {@code fxk-btn-danger}: the constant name is lower-cased and underscores become hyphens, matching
 * the plain, hyphenated names every other FXKit style class uses (see {@code docs/utility-classes.md}).
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class FxButton extends Button {
 *     private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.PRIMARY);
 *
 *     public FxButton() {
 *         getStyleClass().add("fxk-btn");
 *         EnumStyleClassSync.sync(this, "fxk-btn-", variant);
 *     }
 * }
 * }</pre>
 */
public final class EnumStyleClassSync {

    private EnumStyleClassSync() {
        // static utility class, not meant to be instantiated
    }

    /**
     * Adds the style class for {@code property}'s current value to {@code node}, then keeps it in sync
     * as {@code property} changes: the style class for the old value is removed before the style class
     * for the new value is added. A {@code null} value simply means no style class from this property is
     * present (nothing is added), which also covers the property starting out {@code null}.
     *
     * @param node     the node whose {@link Node#getStyleClass()} is kept in sync
     * @param prefix   the style-class prefix, for example {@code "fxk-btn-"}
     * @param property the enum property to track
     * @param <E>      the enum type
     * @throws NullPointerException if {@code node}, {@code prefix} or {@code property} is {@code null}
     */
    public static <E extends Enum<E>> void sync(Node node, String prefix, ObservableValue<E> property) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(property, "property");

        property.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                node.getStyleClass().remove(styleClass(prefix, oldValue));
            }
            if (newValue != null) {
                node.getStyleClass().add(styleClass(prefix, newValue));
            }
        });

        E initial = property.getValue();
        if (initial != null) {
            node.getStyleClass().add(styleClass(prefix, initial));
        }
    }

    /** Builds {@code prefix + <lower-hyphen-case name>}, e.g. {@code "fxk-btn-" + ON_PRIMARY} to {@code "fxk-btn-on-primary"}. */
    private static String styleClass(String prefix, Enum<?> value) {
        return prefix + value.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
