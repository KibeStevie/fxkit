package dev.fxkit.core.internal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Turns a Tailwind-like {@code className} string into inline-style declarations and style classes,
 * grouped by pseudo-class scope ({@code hover:}, {@code focus:}, ...).
 *
 * <p>This class is pure: it touches no JavaFX types, so it can be unit-tested without a toolkit.
 * {@link ClassNameStyler} owns everything stateful (listeners, writing to the node).
 *
 * <h2>Resolution rules for one token</h2>
 * <ol>
 *   <li>{@code ring*}: recognized, currently a no-op;</li>
 *   <li>{@code bg-X}, {@code text-X}, {@code border-X} where {@code X} is a color in
 *       {@link ClassNameVocabulary#isColor(String)}: inline color declaration;</li>
 *   <li>otherwise, a style class that exists in {@code utilities.css}
 *       ({@code text-lg}, {@code text-center}, {@code border-2}, {@code p-4}, ...);</li>
 *   <li>otherwise: a warning, and the token is ignored (never turned into a guessed CSS value).</li>
 * </ol>
 *
 * <h2>Gradients</h2>
 * {@code from-}, {@code via-} and {@code to-} stops are collected once for the whole string and
 * always ordered from, via..., to, whatever order they were written in. Each scope that has a
 * {@code bg-gradient-to-*} direction gets a gradient declaration using those shared stops, placed
 * after the scope's other declarations so the gradient wins over a plain {@code bg-*} color in the
 * same scope (as in Tailwind).
 *
 * <p>Not part of FXKit's public API.
 */
final class ClassNameParser {

    private ClassNameParser() {
        // static utility class, not meant to be instantiated
    }

    enum Scope { NONE, HOVER, FOCUSED, ARMED, PRESSED, DISABLED }

    /** One CSS declaration; {@code property} is the key later scopes overwrite. */
    record Declaration(String property, String value) {
        String css() {
            return property + ": " + value + ";";
        }
    }

    record Result(Map<Scope, List<Declaration>> declarations, Map<Scope, Set<String>> classes) {
        boolean usesScope(Scope scope) {
            return declarations.containsKey(scope) || classes.containsKey(scope);
        }

        boolean isEmpty() {
            return declarations.isEmpty() && classes.isEmpty();
        }
    }

    private static final Map<String, Scope> SCOPE_PREFIXES = Map.of(
            "hover", Scope.HOVER,
            "pressed", Scope.PRESSED,
            "armed", Scope.ARMED,
            "focus", Scope.FOCUSED,
            "disabled", Scope.DISABLED);

    private static final String GRADIENT_PREFIX = "bg-gradient-to-";

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

    static Result parse(String className, ClassNameVocabulary vocabulary, Consumer<String> warn) {
        Map<Scope, List<Declaration>> declarations = new EnumMap<>(Scope.class);
        Map<Scope, Set<String>> classes = new EnumMap<>(Scope.class);
        if (className == null || className.isBlank()) {
            return new Result(declarations, classes);
        }

        Map<Scope, String> directionByScope = new EnumMap<>(Scope.class);
        Map<Scope, List<String>> plainByScope = new EnumMap<>(Scope.class);
        // Stops are collected per scope so that "hover:from-red-500" changes the hover gradient only.
        Map<Scope, String> fromByScope = new EnumMap<>(Scope.class);
        Map<Scope, String> toByScope = new EnumMap<>(Scope.class);
        Map<Scope, List<String>> viaByScope = new EnumMap<>(Scope.class);

        // ---- pass 1: split off scope prefixes and gradient tokens ---------------------------
        for (String raw : className.trim().split("\\s+")) {
            Scope scope = Scope.NONE;
            String token = raw;
            int colon = raw.indexOf(':');
            if (colon > 0 && SCOPE_PREFIXES.containsKey(raw.substring(0, colon))) {
                scope = SCOPE_PREFIXES.get(raw.substring(0, colon));
                token = raw.substring(colon + 1);
            }

            if (token.startsWith(GRADIENT_PREFIX)) {
                directionByScope.put(scope, token.substring(GRADIENT_PREFIX.length()));
            } else if (token.startsWith("from-")) {
                String stop = stop(token.substring(5), raw, vocabulary, warn);
                if (stop != null) {
                    fromByScope.put(scope, stop);
                }
            } else if (token.startsWith("via-")) {
                String stop = stop(token.substring(4), raw, vocabulary, warn);
                if (stop != null) {
                    viaByScope.computeIfAbsent(scope, s -> new ArrayList<>()).add(stop);
                }
            } else if (token.startsWith("to-") && vocabulary.isColor(token.substring(3))) {
                String stop = stop(token.substring(3), raw, vocabulary, warn);
                if (stop != null) {
                    toByScope.put(scope, stop);
                }
            } else {
                plainByScope.computeIfAbsent(scope, s -> new ArrayList<>()).add(token);
            }
        }

        // ---- pass 2: resolve plain tokens, then gradients (gradient last = wins in its scope) --
        for (var entry : plainByScope.entrySet()) {
            List<Declaration> decls = declarations.computeIfAbsent(entry.getKey(), s -> new ArrayList<>());
            Set<String> cls = classes.computeIfAbsent(entry.getKey(), s -> new LinkedHashSet<>());
            for (String token : entry.getValue()) {
                resolvePlain(token, vocabulary, decls, cls, warn);
            }
        }

        // A scope gets a gradient if it names a direction, or if it only overrides stops and the base
        // state has a direction (Tailwind: "hover:from-red-500" re-colors the gradient the base declared).
        Set<Scope> gradientScopes = new LinkedHashSet<>(directionByScope.keySet());
        if (directionByScope.containsKey(Scope.NONE)) {
            gradientScopes.addAll(fromByScope.keySet());
            gradientScopes.addAll(viaByScope.keySet());
            gradientScopes.addAll(toByScope.keySet());
        }
        for (Scope scope : gradientScopes) {
            String direction = directionByScope.getOrDefault(scope, directionByScope.get(Scope.NONE));
            String coords = GRADIENT_COORDS.get(direction);
            List<String> stops = new ArrayList<>();
            String from = fromByScope.getOrDefault(scope, fromByScope.get(Scope.NONE));
            if (from != null) {
                stops.add(from);
            }
            stops.addAll(viaByScope.containsKey(scope) ? viaByScope.get(scope)
                    : viaByScope.getOrDefault(Scope.NONE, List.of()));
            String to = toByScope.getOrDefault(scope, toByScope.get(Scope.NONE));
            if (to != null) {
                stops.add(to);
            }
            if (coords == null) {
                warn.accept("Unknown gradient direction: " + GRADIENT_PREFIX + direction);
            } else if (stops.size() < 2) {
                // only complain where the string actually asked for a gradient
                if (directionByScope.containsKey(scope)) {
                    warn.accept(GRADIENT_PREFIX + direction
                            + " needs at least a from-* and a to-* stop in the className string");
                }
            } else {
                declarations.computeIfAbsent(scope, s -> new ArrayList<>())
                        .add(new Declaration("-fx-background-color", gradientCss(coords, stops)));
            }
        }

        // drop scopes that ended up with nothing, so no listener is attached for them
        declarations.values().removeIf(List::isEmpty);
        classes.values().removeIf(Set::isEmpty);
        return new Result(declarations, classes);
    }

    // ---- helpers ---------------------------------------------------------------------------

    private static String stop(String colorName, String rawToken, ClassNameVocabulary vocabulary,
                               Consumer<String> warn) {
        String resolved = resolveColor(colorName, vocabulary);
        if (resolved == null) {
            warn.accept("Unrecognized gradient stop: " + rawToken);
        }
        return resolved;
    }

    /** @return the CSS value for a color name ({@code "-fxk-primary"}), or {@code null} if unknown */
    private static String resolveColor(String name, ClassNameVocabulary vocabulary) {
        if (!vocabulary.isColor(name)) {
            return null;
        }
        return name.equals("transparent") ? "transparent" : "-fxk-" + name;
    }

    private static String gradientCss(String coords, List<String> stops) {
        StringBuilder sb = new StringBuilder("linear-gradient(").append(coords);
        int n = stops.size();
        for (int i = 0; i < n; i++) {
            int pct = Math.round(100f * i / (n - 1));
            sb.append(", ").append(stops.get(i)).append(' ').append(pct).append('%');
        }
        return sb.append(')').toString();
    }

    private static void resolvePlain(String token, ClassNameVocabulary vocabulary,
                                     List<Declaration> outDecls, Set<String> outClasses,
                                     Consumer<String> warn) {
        if (isRing(token)) {
            return; // recognized, currently a no-op - see ClassNameStyler "Known limitation"
        }

        if (token.startsWith("bg-")) {
            String color = resolveColor(token.substring(3), vocabulary);
            if (color != null) {
                outDecls.add(new Declaration("-fx-background-color", color));
                return;
            }
        } else if (token.startsWith("text-")) {
            String color = resolveColor(token.substring(5), vocabulary);
            if (color != null) {
                outDecls.add(new Declaration("-fx-text-fill", color));
                outDecls.add(new Declaration("-fx-fill", color));
                return;
            }
        } else if (token.startsWith("border-")) {
            String color = resolveColor(token.substring(7), vocabulary);
            if (color != null) {
                outDecls.add(new Declaration("-fx-border-color", color));
                return;
            }
        }

        if (vocabulary.isUtilityClass(token)) {
            outClasses.add(token);
            return;
        }
        warn.accept("Unrecognized className token: " + token
                + " (not a known color, not a utility class in utilities.css, and not valid "
                + "gradient/pseudo-scope syntax)");
    }

    private static boolean isRing(String token) {
        return token.equals("ring")
                || token.matches("ring(-offset)?(-\\d+)?")
                || token.matches("ring(-offset)?-(white|black|[a-z]+-\\d{2,3})");
    }
}
