package dev.fxkit.core.internal;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import dev.fxkit.core.internal.ClassNameParser.Declaration;
import dev.fxkit.core.internal.ClassNameParser.Scope;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;

/**
 * Applies {@code FxButton.className}-style utility strings to a node as inline style, optionally
 * scoped by pseudo-class prefix ({@code hover:}, {@code pressed:}, {@code armed:}, {@code focus:},
 * {@code disabled:}). Parsing lives in {@link ClassNameParser}; this class owns the node-facing,
 * stateful part.
 *
 * <h2>Why inline style, not stylesheet classes</h2>
 * In JavaFX CSS, inline style ({@link Node#setStyle}) always outranks stylesheet class rules for the
 * same property, regardless of class specificity or declaration order. Resolving colors to inline
 * style is what lets {@code className} override {@code Variant}/{@code Color}/{@code Size} (which
 * remain plain style classes) without ever touching those classes itself.
 *
 * <h2>Ownership</h2>
 * This class remembers the exact text it appended to the node's inline style and the exact style
 * classes it added. On every reapply, and on removal, it takes out only that text and only those
 * classes. Inline declarations you set yourself (even for the same properties) and style classes you
 * added yourself (even ones with the same name) are never removed. If you overwrite the node's style
 * after this class wrote it, your value is kept as yours and this class's declarations are appended
 * again at the next state change, so they come last and win for the same property.
 *
 * <h2>Lifetime</h2>
 * The per-node state lives in the node's own {@link Node#getProperties() properties}, so it is
 * garbage-collected together with the node. There is no static registry.
 *
 * <h2>Known limitation</h2>
 * {@code ring-*} / {@code ring-offset-*} tokens are recognized (so they don't trigger an
 * "unrecognized token" warning) but currently produce no visual effect - FXKit already draws a
 * uniform focus ring via {@code .fxk-btn:focused} in {@code components.css}.
 *
 * <p>Call on the JavaFX Application Thread. Not part of FXKit's public API.
 */
public final class ClassNameStyler {

    private ClassNameStyler() {
        // static utility class, not meant to be instantiated
    }

    private static final Logger LOG = Logger.getLogger("dev.fxkit.className");

    /** Key under which the per-node {@link Installed} state is kept in {@link Node#getProperties()}. */
    private static final String STATE_KEY = "dev.fxkit.core.internal.ClassNameStyler.state";

    // ---- public entry point ----------------------------------------------------------------

    /**
     * (Re)applies {@code className} to {@code node}. Removes whatever this class previously installed
     * on {@code node} first, so repeated calls (e.g. from a property listener) never leak
     * declarations, classes or pseudo-state listeners from an earlier value.
     *
     * @param node      the node to style; typically an {@code FxButton}
     * @param className space-separated utility tokens, optionally prefixed with a pseudo-class scope
     *                  (e.g. {@code "hover:"}); {@code null} or blank clears any previous styling
     */
    public static void apply(Node node, String className) {
        apply(node, className, ClassNameVocabulary.shipped());
    }

    /** Visible for tests: same as {@link #apply(Node, String)} with an explicit vocabulary. */
    static void apply(Node node, String className, ClassNameVocabulary vocabulary) {
        uninstall(node);
        if (className == null || className.isBlank()) {
            return;
        }
        ClassNameParser.Result parsed = ClassNameParser.parse(className, vocabulary, LOG::warning);
        if (parsed.isEmpty()) {
            return;
        }
        Installed installed = new Installed(parsed);
        installed.attachListeners(node);
        installed.recompute(node);
        node.getProperties().put(STATE_KEY, installed);
    }

    private static void uninstall(Node node) {
        Object previous = node.getProperties().remove(STATE_KEY);
        if (previous instanceof Installed installed) {
            installed.detach(node);
        }
    }

    // ---- live state, one per node -------------------------------------------------------------

    private static final class Installed {
        final ClassNameParser.Result parsed;
        final Map<Scope, ChangeListener<Boolean>> listeners = new EnumMap<>(Scope.class);

        /** Exact text this instance last appended to the node's inline style ({@code ""} if none). */
        String ownedStyle = "";
        /** Style classes this instance added (and therefore may remove); never ones already present. */
        Set<String> ownedClasses = Set.of();

        Installed(ClassNameParser.Result parsed) {
            this.parsed = parsed;
        }

        void attachListeners(Node node) {
            addListener(node, Scope.HOVER, node.hoverProperty());
            addListener(node, Scope.FOCUSED, node.focusedProperty());
            addListener(node, Scope.PRESSED, node.pressedProperty());
            addListener(node, Scope.DISABLED, node.disabledProperty());
            if (node instanceof ButtonBase bb) {
                addListener(node, Scope.ARMED, bb.armedProperty());
            }
        }

        private void addListener(Node node, Scope scope, ObservableValue<Boolean> prop) {
            if (!parsed.usesScope(scope)) {
                return;
            }
            ChangeListener<Boolean> listener = (obs, oldValue, newValue) -> recompute(node);
            prop.addListener(listener);
            listeners.put(scope, listener);
        }

        /**
         * Rebuilds the merged declarations and classes from scratch: base ({@code NONE}) first, then
         * each currently-true pseudo-scope layered on top in a fixed priority order, later entries
         * overwriting earlier ones for the same CSS property - matching how {@code components.css}
         * orders {@code :hover} before {@code :pressed}/{@code :armed}, with {@code :disabled}
         * always having the final say.
         */
        void recompute(Node node) {
            Map<String, String> props = new LinkedHashMap<>();
            Set<String> classes = new LinkedHashSet<>();

            applyScope(Scope.NONE, props, classes);
            for (Scope scope : List.of(Scope.HOVER, Scope.FOCUSED, Scope.ARMED, Scope.PRESSED, Scope.DISABLED)) {
                if (isActive(node, scope)) {
                    applyScope(scope, props, classes);
                }
            }

            writeStyle(node, props);
            writeClasses(node, classes);
        }

        private void writeStyle(Node node, Map<String, String> props) {
            StringBuilder sb = new StringBuilder();
            props.forEach((property, value) -> {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(new Declaration(property, value).css());
            });
            String ours = sb.toString();

            String userPart = withoutOwnedStyle(node.getStyle());
            // The separator we insert is part of what we own, so removing our block restores the
            // caller's text byte for byte.
            String separator = "";
            if (!ours.isEmpty() && !userPart.isBlank()) {
                separator = userPart.stripTrailing().endsWith(";") ? " " : "; ";
            }
            String owned = ours.isEmpty() ? "" : separator + ours;
            String combined = userPart + owned;

            String current = node.getStyle() == null ? "" : node.getStyle();
            if (!combined.equals(current)) {
                node.setStyle(combined.isEmpty() ? null : combined);
            }
            ownedStyle = owned;
        }

        /** The inline style minus the one block this instance wrote; everything else is the caller's. */
        private String withoutOwnedStyle(String current) {
            String style = current == null ? "" : current;
            if (!ownedStyle.isEmpty()) {
                int at = style.lastIndexOf(ownedStyle);
                if (at >= 0) {
                    style = style.substring(0, at) + style.substring(at + ownedStyle.length());
                }
            }
            return style;
        }

        private void writeClasses(Node node, Set<String> wanted) {
            List<String> present = node.getStyleClass();
            node.getStyleClass().removeAll(minus(ownedClasses, wanted));
            Set<String> nowOwned = new LinkedHashSet<>();
            for (String styleClass : wanted) {
                if (ownedClasses.contains(styleClass) && present.contains(styleClass)) {
                    nowOwned.add(styleClass); // already ours from the last round
                } else if (!present.contains(styleClass)) {
                    present.add(styleClass);
                    nowOwned.add(styleClass);
                }
                // else: the caller already had this class; it stays theirs and is never removed by us
            }
            ownedClasses = nowOwned;
        }

        private static Set<String> minus(Set<String> a, Set<String> b) {
            Set<String> result = new LinkedHashSet<>(a);
            result.removeAll(b);
            return result;
        }

        private boolean isActive(Node node, Scope scope) {
            return switch (scope) {
                case HOVER -> node.isHover();
                case FOCUSED -> node.isFocused();
                case PRESSED -> node.isPressed();
                case DISABLED -> node.isDisabled();
                case ARMED -> node instanceof ButtonBase bb && bb.isArmed();
                case NONE -> true;
            };
        }

        private void applyScope(Scope scope, Map<String, String> props, Set<String> classes) {
            for (Declaration decl : parsed.declarations().getOrDefault(scope, List.of())) {
                props.put(decl.property(), decl.value());
            }
            classes.addAll(parsed.classes().getOrDefault(scope, Set.of()));
        }

        void detach(Node node) {
            listeners.forEach((scope, listener) -> {
                switch (scope) {
                    case HOVER -> node.hoverProperty().removeListener(listener);
                    case FOCUSED -> node.focusedProperty().removeListener(listener);
                    case PRESSED -> node.pressedProperty().removeListener(listener);
                    case DISABLED -> node.disabledProperty().removeListener(listener);
                    case ARMED -> {
                        if (node instanceof ButtonBase bb) {
                            bb.armedProperty().removeListener(listener);
                        }
                    }
                    case NONE -> { /* no listener */ }
                }
            });
            listeners.clear();

            String remaining = withoutOwnedStyle(node.getStyle());
            String current = node.getStyle() == null ? "" : node.getStyle();
            if (!remaining.equals(current)) {
                node.setStyle(remaining.isEmpty() ? null : remaining);
            }
            node.getStyleClass().removeAll(ownedClasses);
            ownedStyle = "";
            ownedClasses = Set.of();
        }
    }
}
