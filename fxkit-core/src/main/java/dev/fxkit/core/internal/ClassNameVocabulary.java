package dev.fxkit.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What {@link ClassNameParser} is allowed to resolve: the color tokens defined in
 * {@code tokens.css} and the utility classes defined in {@code utilities.css}.
 *
 * <p>Reading the vocabulary from the shipped stylesheets (instead of guessing from the token's
 * spelling) is what stops {@code text-center} or {@code bg-bleu} from turning into an undefined
 * looked-up color such as {@code -fxk-center}, which JavaFX silently drops. It also removes the
 * hand-maintained font-size list: whatever {@code utilities.css} generates is automatically valid.
 *
 * <p>Not part of FXKit's public API.
 */
final class ClassNameVocabulary {

    private static final Logger LOG = Logger.getLogger("dev.fxkit.className");

    private static final String RESOURCE_DIR = "/dev/fxkit/core/";
    private static final Pattern TOKEN_DEFINITION = Pattern.compile("(?m)^\\s*-fxk-([a-z0-9-]+)\\s*:");
    private static final Pattern CLASS_SELECTOR = Pattern.compile("\\.([A-Za-z][A-Za-z0-9_-]*)");
    private static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    private static final List<String> FAMILIES = List.of(
            "blue", "cyan", "gray", "green", "indigo", "lime",
            "pink", "purple", "red", "teal", "yellow");
    private static final List<String> SHADES = List.of(
            "50", "100", "200", "300", "400", "500", "600", "700", "800", "900");

    private static final class Holder {
        static final ClassNameVocabulary SHIPPED = load();
    }

    private final Set<String> colorTokens;
    /** {@code null} means "unknown": every plain utility class is then accepted (lenient fallback). */
    private final Set<String> utilityClasses;

    /**
     * @param colorTokens    names without the {@code -fxk-} prefix, e.g. {@code "primary"}, {@code "blue-500"}
     * @param utilityClasses style class names, or {@code null} to accept any
     */
    ClassNameVocabulary(Set<String> colorTokens, Set<String> utilityClasses) {
        this.colorTokens = Set.copyOf(colorTokens);
        this.utilityClasses = utilityClasses == null ? null : Set.copyOf(utilityClasses);
    }

    /** The vocabulary of the stylesheets shipped in this module (loaded once). */
    static ClassNameVocabulary shipped() {
        return Holder.SHIPPED;
    }

    /** @return whether {@code name} (no {@code -fxk-} prefix) is a color that can be looked up */
    boolean isColor(String name) {
        return name.equals("transparent") || colorTokens.contains(name);
    }

    /** @return whether {@code styleClass} exists in {@code utilities.css} */
    boolean isUtilityClass(String styleClass) {
        return utilityClasses == null || utilityClasses.contains(styleClass);
    }

    // ---- loading ---------------------------------------------------------------------------

    private static ClassNameVocabulary load() {
        Set<String> colors = new HashSet<>();
        Set<String> classes = null;
        try {
            for (Matcher m = TOKEN_DEFINITION.matcher(read("tokens.css")); m.find(); ) {
                String name = m.group(1);
                // shadow tokens are whole dropshadow(...) effects, not colors
                if (!name.startsWith("shadow-")) {
                    colors.add(name);
                }
            }
            classes = selectorsOutsideBlocks(read("utilities.css"));
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "Could not read FXKit stylesheets to build the className vocabulary; "
                    + "falling back to the raw palette only", e);
        }
        if (colors.isEmpty()) {
            colors.add("white");
            colors.add("black");
            for (String family : FAMILIES) {
                for (String shade : SHADES) {
                    colors.add(family + "-" + shade);
                }
            }
        }
        return new ClassNameVocabulary(colors, classes == null || classes.isEmpty() ? null : classes);
    }

    private static String read(String name) throws IOException {
        try (InputStream in = ClassNameVocabulary.class.getResourceAsStream(RESOURCE_DIR + name)) {
            if (in == null) {
                throw new IOException(name + " not found at " + RESOURCE_DIR);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Collects {@code .class} names that appear in selectors, ignoring comments and declaration blocks. */
    static Set<String> selectorsOutsideBlocks(String css) {
        String text = COMMENT.matcher(css).replaceAll(" ");
        StringBuilder selectors = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
                selectors.append(' ');
            } else if (c == '}') {
                depth = Math.max(0, depth - 1);
            } else if (depth == 0) {
                selectors.append(c);
            }
        }
        Set<String> found = new HashSet<>();
        for (Matcher m = CLASS_SELECTOR.matcher(selectors); m.find(); ) {
            found.add(m.group(1));
        }
        return found;
    }
}
