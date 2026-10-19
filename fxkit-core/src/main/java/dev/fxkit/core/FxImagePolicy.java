package dev.fxkit.core;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Which image URLs FXKit components are willing to load ({@code FxAvatar}, {@code FxCarousel.image},
 * {@code FxMegaMenuImageCard}).
 *
 * <p>An image URL is code-adjacent input: if your app lets a user, a server response or a file
 * choose the URL, it decides what your app fetches. This policy is the one place to say what is
 * acceptable. A URL that fails it is not loaded; the component logs a warning (scheme and host
 * only, never the full URL) and shows its normal "no image" state.
 *
 * <h2>Schemes</h2>
 * The default allow-list is {@code file}, {@code jar}, {@code http}, {@code https} and {@code data}.
 * A value without a scheme ({@code "/images/a.png"}) is a classpath resource and is always allowed.
 * An app that only ships its own pictures should narrow it:
 *
 * <pre>{@code
 * FxImagePolicy.setAllowedSchemes(Set.of("file", "jar")); // no network access for images
 * }</pre>
 *
 * <h2>Always refused, whatever the allow-list says</h2>
 * <ul>
 *   <li>control characters (line breaks, NUL, ...) anywhere in the URL;</li>
 *   <li>{@code file:} URLs that point at another machine ({@code file://server/share/a.png},
 *       {@code file:////server/share/a.png}, backslash and percent-encoded variants). On Windows
 *       these make the OS open a network share and can leak the user's credentials to that server;</li>
 *   <li>a {@code jar:} URL whose inner URL is itself not allowed
 *       ({@code jar:http://...!/a.png} needs {@code http} to be allowed);</li>
 *   <li>a scheme-less value that is a UNC path ({@code \\server\share\a.png}, {@code //server/a.png});</li>
 *   <li>a {@code data:} URI that is not {@code data:image/...} or is longer than 2,000,000 characters.</li>
 * </ul>
 *
 * <p>The policy is process-wide and thread-safe. It limits <em>where</em> images come from; it does
 * not inspect the image data.
 */
public final class FxImagePolicy {

    private static final Set<String> DEFAULT_SCHEMES = Set.of("file", "jar", "http", "https", "data");

    /** At least two characters, so a Windows drive letter ("C:\...") is not mistaken for a scheme. */
    private static final Pattern SCHEME = Pattern.compile("^([A-Za-z][A-Za-z0-9+.\\-]+):");

    private static final int MAX_JAR_NESTING = 4;

    /**
     * Longest {@code data:} URI accepted, in characters (about 1.5 MB of decoded image). A data URI
     * is held fully in memory as a String before any decode limit applies, so it needs its own cap.
     */
    private static final int MAX_DATA_URI_LENGTH = 2_000_000;

    private static volatile Set<String> allowedSchemes = DEFAULT_SCHEMES;

    private FxImagePolicy() {
        // static utility class, not meant to be instantiated
    }

    /** @return the schemes currently allowed, lower-case, as an immutable set */
    public static Set<String> getAllowedSchemes() {
        return allowedSchemes;
    }

    /**
     * Replaces the allow-list. Scheme names are case-insensitive; an empty collection allows only
     * classpath resources (values without a scheme).
     *
     * @param schemes for example {@code Set.of("file", "jar", "https")}
     * @throws NullPointerException if {@code schemes} or one of its elements is {@code null}
     */
    public static void setAllowedSchemes(Collection<String> schemes) {
        Objects.requireNonNull(schemes, "schemes");
        Set<String> normalized = new HashSet<>();
        for (String scheme : schemes) {
            normalized.add(Objects.requireNonNull(scheme, "scheme").strip().toLowerCase(Locale.ROOT));
        }
        allowedSchemes = Set.copyOf(normalized);
    }

    /** Restores the default allow-list. */
    public static void resetAllowedSchemes() {
        allowedSchemes = DEFAULT_SCHEMES;
    }

    /**
     * @param url an image URL or classpath resource name
     * @return whether components may load it; {@code false} for {@code null} or blank
     */
    public static boolean isAllowed(String url) {
        return url != null && isAllowed(url.strip(), 0);
    }

    private static boolean isAllowed(String url, int depth) {
        if (url.isEmpty() || depth > MAX_JAR_NESTING || hasControlCharacter(url)) {
            return false;
        }
        Matcher matcher = SCHEME.matcher(url);
        if (!matcher.find()) {
            // A classpath resource or relative name - but never a UNC path ("\\\\server\\share\\a.png" or
            // "//server/share/a.png"), which Windows resolves as a network share.
            String normalized = url.replace('\\', '/');
            return !normalized.startsWith("//");
        }
        String scheme = matcher.group(1).toLowerCase(Locale.ROOT);
        if (!allowedSchemes.contains(scheme)) {
            return false;
        }
        String rest = url.substring(matcher.end());
        return switch (scheme) {
            case "file" -> isLocalFile(rest);
            case "data" -> url.length() <= MAX_DATA_URI_LENGTH
                    && rest.regionMatches(true, 0, "image/", 0, "image/".length());
            case "jar" -> {
                int bang = rest.indexOf("!/");
                yield isAllowed(bang < 0 ? rest : rest.substring(0, bang), depth + 1);
            }
            default -> true;
        };
    }

    private static boolean hasControlCharacter(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 0x20 || c == 0x7f) {
                return true;
            }
        }
        return false;
    }

    /**
     * A file URL is local if it names no host (or "localhost") and its path does not start with "//",
     * which Windows reads as a network share ("\\\\server\\share").
     *
     * <p>The URL's structure (host versus path) is judged first, on the raw text; only the path is
     * then percent-decoded, because the platform decodes it before use: {@code file:/%2F%2Fserver/x}
     * is a network path even though it contains no literal "//".
     */
    private static boolean isLocalFile(String rest) {
        String text = rest.replace('\\', '/');
        String rawPath = text;
        if (text.startsWith("//")) {
            int slash = text.indexOf('/', 2);
            String authority = slash < 0 ? text.substring(2) : text.substring(2, slash);
            if (!authority.isEmpty() && !authority.equalsIgnoreCase("localhost")) {
                return false;
            }
            rawPath = slash < 0 ? "" : text.substring(slash);
        }
        String path;
        try {
            path = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException malformedEscape) {
            return false;
        }
        return !path.replace('\\', '/').startsWith("//");
    }
}
