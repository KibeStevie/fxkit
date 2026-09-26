package dev.fxkit.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;

/**
 * Parses {@code FxButton.className} into inline style, optionally scoped by pseudo-class prefix
 * ({@code hover:}, {@code pressed:}, {@code armed:}, {@code focus:}, {@code disabled:}).
 *
 * <h2>Why inline style, not stylesheet classes</h2>
 * In JavaFX CSS, inline style ({@link Node#setStyle}) always outranks stylesheet class rules for the
 * same property, regardless of class specificity or declaration order. Resolving {@code className} to
 * inline style is what lets it override {@code Variant}/{@code Color}/{@code Size} (which remain plain
 * style classes) without ever touching those classes itself.
 *
 * <h2>Gradients</h2>
 * {@code from-}/{@code via-}/{@code to-} stops are collected <b>once, globally</b> across the whole
 * className string, regardless of which scope they're written under - matching how Tailwind itself
 * works, where stops are declared once and only the direction changes per state (e.g.
 * {@code bg-gradient-to-r ... hover:bg-gradient-to-br} reuses the same {@code from-}/{@code via-}/
 * {@code to-} stops declared in the base scope). Any number of {@code via-} stops is supported; a
 * {@code bg-gradient-to-*} token appearing in any scope (base or pseudo-scoped) produces a gradient
 * declaration for that scope using those shared stops.
 *
 * <h2>Ownership</h2>
 * Only the inline-style declarations and style classes this class itself last wrote for a given node
 * are ever removed or replaced on reapply; anything else already present (set directly, or by other
 * code) is left untouched.
 *
 * <h2>Known limitation</h2>
 * {@code ring-*} / {@code ring-offset-*} tokens are recognized (so they don't trigger an "unrecognized
 * token" warning) but currently produce no visual effect - FXKit already draws a uniform focus ring via
 * {@code .fxk-btn:focused} in {@code components.css}. Per-color focus rings are not implemented yet.
 *
 * <p>Not part of FXKit's public API.
 */
public final class ClassNameStyler {

    private ClassNameStyler() {
        // static utility class, not meant to be instantiated
    }

    private static final Logger LOG = Logger.getLogger("dev.fxkit.className");

    private static final List<String> FAMILIES = List.of(
            "blue", "cyan", "gray", "green", "indigo", "lime",
            "pink", "purple", "red", "teal", "yellow");

    private static final Set<String> SHADES = Set.of(
            "50", "100", "200", "300", "400", "500", "600", "700", "800", "900");

    /**
     * Font-size names this environment generates as {@code .text-<name>} utility classes (see
     * {@code utilities.css}). Used to disambiguate {@code text-lg} (a font size, stays a style class)
     * from {@code text-blue-500} (a color, becomes inline {@code -fx-text-fill}). Confirm against
     * {@code tools/scales.txt} if this list drifts.
     */
    private static final Set<String> FONT_SIZE_NAMES = Set.of(
            "xs", "sm", "base", "lg", "xl", "2xl", "3xl");

    private static final Map<String, PseudoScope> SCOPE_PREFIXES = Map.of(
            "hover", PseudoScope.HOVER,
            "pressed", PseudoScope.PRESSED,
            "armed", PseudoScope.ARMED,
            "focus", PseudoScope.FOCUSED,
            "disabled", PseudoScope.DISABLED);

    /** direction -> JavaFX linear-gradient() "from <x%> <y%> to <x%> <y%>" coordinate pair. */
    private static final Map<String, String> GRADIENT_COORDS = Map.ofEntries(
            Map.entry("t", "from 0% 100% to 0% 0%"),
            Map.entry("tr", "from 0% 100% to 100% 0%"),
            Map.entry("r", "from 0% 0% to 100% 0%"),
            Map.entry("br", "from 0% 0% to 100% 100%"),
            Map.entry("b", "from 0% 0% to 0% 100%"),
            Map.entry("bl", "from 100% 0% to 0% 100%"),
            Map.entry("l", "from 100% 0% to 0% 0%"),
            Map.entry("tl", "from 100% 100% to 0% 0%"));

    /** Nodes currently styled by this class, so a reapply (or a null className) can clean up first. */
    private static final Map<Node, Installed> OWNED = new WeakHashMap<>();

    enum PseudoScope { NONE, HOVER, FOCUSED, ARMED, PRESSED, DISABLED }

    // ---- public entry point ----------------------------------------------------------------

    /**
     * (Re)applies {@code className} to {@code node}. Removes whatever this class previously installed
     * on {@code node} first, so repeated calls (e.g. from a property listener) never leak declarations,
     * classes or pseudo-state listeners from an earlier value.
     *
     * @param node      the node to style; typically an {@code FxButton}
     * @param className space-separated utility tokens, optionally prefixed with a pseudo-class scope
     *                  (e.g. {@code "hover:"}); {@code null} or blank clears any previous styling
     */
    public static void apply(Node node, String className) {
        uninstall(node);
        if (className == null || className.isBlank()) {
            return;
        }

        // ---- Pass 1: tokenize into scopes; collect gradient stops and directions globally -----
        Map<PseudoScope, List<String>> rawTokensByScope = new EnumMap<>(PseudoScope.class);
        List<String> globalStops = new ArrayList<>();
        Map<PseudoScope, String> directionByScope = new EnumMap<>(PseudoScope.class);

        for (String raw : className.trim().split("\\s+")) {
            if (raw.isEmpty()) {
                continue;
            }
            PseudoScope scope = PseudoScope.NONE;
            String token = raw;
            int colon = raw.indexOf(':');
            if (colon > 0 && SCOPE_PREFIXES.containsKey(raw.substring(0, colon))) {
                scope = SCOPE_PREFIXES.get(raw.substring(0, colon));
                token = raw.substring(colon + 1);
            }

            if (token.startsWith("bg-gradient-to-")) {
                directionByScope.put(scope, token.substring("bg-gradient-to-".length()));
            } else if (token.startsWith("from-")) {
                addStop(globalStops, 0, raw, token.substring(5));
            } else if (token.startsWith("via-")) {
                addStop(globalStops, globalStops.size(), raw, token.substring(4));
            } else if (token.startsWith("to-") && resolveColorToken(token.substring(3)) != null) {
                addStop(globalStops, globalStops.size(), raw, token.substring(3));
            } else {
                rawTokensByScope.computeIfAbsent(scope, s -> new ArrayList<>()).add(token);
            }
        }

        // ---- Pass 2: build declarations / classes per scope ------------------------------------
        Map<PseudoScope, List<String>> declsByScope = new EnumMap<>(PseudoScope.class);
        Map<PseudoScope, Set<String>> classesByScope = new EnumMap<>(PseudoScope.class);

        for (var entry : directionByScope.entrySet()) {
            PseudoScope scope = entry.getKey();
            String direction = entry.getValue();
            List<String> decls = declsByScope.computeIfAbsent(scope, s -> new ArrayList<>());
            String coords = GRADIENT_COORDS.get(direction);
            if (coords == null) {
                LOG.warning("Unknown gradient direction: bg-gradient-to-" + direction);
            } else if (globalStops.size() < 2) {
                LOG.warning("bg-gradient-to-" + direction
                        + " needs at least a from-* and a to-* stop in the className string");
            } else {
                decls.add("-fx-background-color: " + gradientCss(coords, globalStops) + ";");
            }
        }

        for (var entry : rawTokensByScope.entrySet()) {
            List<String> decls = declsByScope.computeIfAbsent(entry.getKey(), s -> new ArrayList<>());
            Set<String> classes = classesByScope.computeIfAbsent(entry.getKey(), s -> new LinkedHashSet<>());
            for (String token : entry.getValue()) {
                resolvePlainToken(token, decls, classes);
            }
        }

        Installed installed = new Installed(declsByScope, classesByScope);
        installed.attachListeners(node);
        installed.recompute(node);
        OWNED.put(node, installed);
    }

    private static void uninstall(Node node) {
        Installed prev = OWNED.remove(node);
        if (prev != null) {
            prev.detach(node);
        }
    }

    private static void addStop(List<String> stops, int index, String rawToken, String colorPart) {
        String resolved = resolveColorToken(colorPart);
        if (resolved == null) {
            LOG.warning("Unrecognized gradient stop: " + rawToken);
            return;
        }
        stops.add(index, resolved);
    }

    private static String gradientCss(String coords, List<String> stops) {
        StringBuilder sb = new StringBuilder("linear-gradient(").append(coords);
        int n = stops.size();
        for (int i = 0; i < n; i++) {
            int pct = Math.round(100f * i / (n - 1));
            sb.append(", ").append(stops.get(i)).append(' ').append(pct).append('%');
        }
        return sb.append(")").toString();
    }

    // ---- resolving one non-gradient token ---------------------------------------------------

    private static void resolvePlainToken(String token, List<String> outDecls, Set<String> outClasses) {
        if (token.equals("ring") || token.matches("ring(-offset)?(-\\d+)?")
                || token.matches("ring(-offset)?-(white|black|[a-z]+-\\d{2,3})")) {
            // Recognized, currently a no-op - see class javadoc "Known limitation".
            return;
        }
        if (token.startsWith("bg-")) {
            String c = resolveColorToken(token.substring(3));
            if (c != null) {
                outDecls.add("-fx-background-color: " + c + ";");
                return;
            }
        } else if (token.startsWith("text-")) {
            String suffix = token.substring(5);
            if (FONT_SIZE_NAMES.contains(suffix)) {
                outClasses.add("text-" + suffix);
                return;
            }
            String c = resolveColorToken(suffix);
            if (c != null) {
                outDecls.add("-fx-text-fill: " + c + "; -fx-fill: " + c + ";");
                return;
            }
        } else if (token.equals("border") || token.matches("border-[024]")) {
            outClasses.add(token);
            return;
        } else if (token.startsWith("border-")) {
            String c = resolveColorToken(token.substring(7));
            if (c != null) {
                outDecls.add("-fx-border-color: " + c + ";");
                return;
            }
        } else if (token.equals("rounded") || token.startsWith("rounded-")
                || token.equals("shadow") || token.equals("shadow-none") || token.startsWith("shadow-")
                || token.matches("p[xytrbl]?-.+") || token.startsWith("gap") || token.startsWith("font-")) {
            outClasses.add(token);
            return;
        } else if (token.startsWith("bg-gradient-")) {
            // A named, generator-produced gradient class (e.g. "bg-gradient-blue" from
            // tools/gradients.txt) - not the directional "bg-gradient-to-*" grammar, just a plain
            // style class, same as any other utilities.css entry.
            outClasses.add(token);
            return;
        }
        LOG.warning("Unrecognized className token: " + token
                + " (not a known utility class and not valid gradient/color/pseudo-scope syntax)");
    }

    private static String resolveColorToken(String token) {
        if (token.equals("transparent")) {
            return "transparent";
        }
        if (token.equals("white") || token.equals("black")) {
            return "-fxk-" + token;
        }
        int dash = token.lastIndexOf('-');
        if (dash > 0) {
            String family = token.substring(0, dash);
            String shade = token.substring(dash + 1);
            if (FAMILIES.contains(family) && SHADES.contains(shade)) {
                return "-fxk-" + token;
            }
        }
        // semantic tokens: primary, surface, danger, text, muted, border, ...
        if (token.matches("[a-z][a-z-]*") && !token.isEmpty()) {
            return "-fxk-" + token;
        }
        return null;
    }

    // ---- live state, applied per node --------------------------------------------------------

    private static final class Installed {
        final Map<PseudoScope, List<String>> declsByScope;
        final Map<PseudoScope, Set<String>> classesByScope;
        final Map<PseudoScope, ChangeListener<Boolean>> listeners = new EnumMap<>(PseudoScope.class);
        Set<String> currentOwnedClasses = Set.of();

        Installed(Map<PseudoScope, List<String>> decls, Map<PseudoScope, Set<String>> classes) {
            this.declsByScope = decls;
            this.classesByScope = classes;
        }

        void attachListeners(Node node) {
            addListener(node, PseudoScope.HOVER, node.hoverProperty());
            addListener(node, PseudoScope.FOCUSED, node.focusedProperty());
            addListener(node, PseudoScope.PRESSED, node.pressedProperty());
            addListener(node, PseudoScope.DISABLED, node.disabledProperty());
            if (node instanceof ButtonBase bb) {
                addListener(node, PseudoScope.ARMED, bb.armedProperty());
            }
        }

        private void addListener(Node node, PseudoScope scope, ObservableValue<Boolean> prop) {
            if (!declsByScope.containsKey(scope) && !classesByScope.containsKey(scope)) {
                return;
            }
            ChangeListener<Boolean> listener = (obs, oldValue, newValue) -> recompute(node);
            prop.addListener(listener);
            listeners.put(scope, listener);
        }

        /**
         * Recomputes the merged inline style and style classes from scratch: base ({@code NONE})
         * declarations first, then each currently-true pseudo-scope layered on top in a fixed
         * priority order, later entries overwriting earlier ones for the same CSS property - matching
         * how {@code components.css} itself orders {@code :hover} before {@code :pressed}/{@code
         * :armed}, with {@code :disabled} always having the final say.
         */
        void recompute(Node node) {
            java.util.LinkedHashMap<String, String> props = new java.util.LinkedHashMap<>();
            Set<String> classes = new LinkedHashSet<>();

            applyScope(PseudoScope.NONE, props, classes);
            if (isActive(node, PseudoScope.HOVER)) applyScope(PseudoScope.HOVER, props, classes);
            if (isActive(node, PseudoScope.FOCUSED)) applyScope(PseudoScope.FOCUSED, props, classes);
            if (isActive(node, PseudoScope.ARMED)) applyScope(PseudoScope.ARMED, props, classes);
            if (isActive(node, PseudoScope.PRESSED)) applyScope(PseudoScope.PRESSED, props, classes);
            if (isActive(node, PseudoScope.DISABLED)) applyScope(PseudoScope.DISABLED, props, classes);

            String rebuilt = String.join(" ", props.values());
            String existing = node.getStyle() == null ? "" : node.getStyle();
            node.setStyle(strip(existing) + rebuilt);

            node.getStyleClass().removeAll(currentOwnedClasses);
            node.getStyleClass().addAll(classes);
            currentOwnedClasses = classes;
        }

        private boolean isActive(Node node, PseudoScope scope) {
            return switch (scope) {
                case HOVER -> node.isHover();
                case FOCUSED -> node.isFocused();
                case PRESSED -> node.isPressed();
                case DISABLED -> node.isDisabled();
                case ARMED -> node instanceof ButtonBase bb && bb.isArmed();
                case NONE -> true;
            };
        }

        private void applyScope(PseudoScope scope, java.util.LinkedHashMap<String, String> props,
                                 Set<String> classes) {
            for (String decl : declsByScope.getOrDefault(scope, List.of())) {
                String property = decl.substring(0, decl.indexOf(':')).trim();
                props.put(property, decl);
            }
            classes.addAll(classesByScope.getOrDefault(scope, Set.of()));
        }

        /**
         * Best-effort: removes only declarations this class is known to produce, by property name, so
         * a fresh {@link #recompute} doesn't duplicate them. Anything else in the inline style (set by
         * other code) is preserved verbatim. If other code also sets one of these same properties
         * inline, whichever write happens last wins - the same rule JavaFX CSS itself uses.
         */
        private String strip(String existing) {
            if (existing.isBlank()) {
                return "";
            }
            return Arrays.stream(existing.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.startsWith("-fx-background-color")
                            && !s.startsWith("-fx-text-fill")
                            && !s.startsWith("-fx-fill")
                            && !s.startsWith("-fx-border-color"))
                    .map(s -> s + "; ")
                    .reduce("", String::concat);
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
            node.getStyleClass().removeAll(currentOwnedClasses);
            node.setStyle(strip(node.getStyle() == null ? "" : node.getStyle()));
        }
    }
}