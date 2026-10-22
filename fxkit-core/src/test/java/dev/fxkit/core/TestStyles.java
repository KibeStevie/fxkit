package dev.fxkit.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Answers "does the shipped CSS style this class?". Used to catch an enum constant whose style class
 * has no rule: the component would set the class, JavaFX would silently ignore it, and the
 * component would render unstyled.
 */
public final class TestStyles {

    private static final String DIR = "/dev/fxkit/core/";
    private static final Pattern COMMENT = Pattern.compile("(?s)/\\*.*?\\*/");

    private static String css;

    private TestStyles() {
        // static utility class, not meant to be instantiated
    }

    /** @return the style class {@code EnumStyleClassSync} builds for {@code value}, e.g. {@code "on-primary"} */
    public static String styleName(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** @return whether {@code .styleClass} appears in a selector of components.css, colors.css or utilities.css */
    public static boolean hasRuleFor(String styleClass) {
        Pattern selector = Pattern.compile("\\." + Pattern.quote(styleClass) + "(?![A-Za-z0-9_-])");
        return selector.matcher(allCss()).find();
    }

    private static synchronized String allCss() {
        if (css == null) {
            StringBuilder all = new StringBuilder();
            for (String name : new String[] {"components.css", "colors.css", "utilities.css"}) {
                all.append(COMMENT.matcher(read(name)).replaceAll(" ")).append('\n');
            }
            css = all.toString();
        }
        return css;
    }

    private static String read(String name) {
        try (InputStream in = TestStyles.class.getResourceAsStream(DIR + name)) {
            if (in == null) {
                throw new IllegalStateException(name + " not found at " + DIR);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + name, e);
        }
    }
}
