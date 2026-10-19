package dev.fxkit.core.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The one place that decides which files a file field accepts, so every way of getting a file into
 * a field (a drop, the native dialog, code calling {@code pick(...)}) is validated identically.
 *
 * <p>Pure Java, no JavaFX types: it takes the extension patterns as plain strings (the
 * {@code getExtensions()} of each {@code FileChooser.ExtensionFilter}), so it is unit-testable
 * without a toolkit.
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>a candidate must be an existing regular file (directories and missing files are rejected);</li>
 *   <li>with no patterns every file is accepted; otherwise the file name must end with one of them
 *       ({@code "*.png"}, {@code "*.tar.gz"}; {@code "*"} and {@code "*.*"} match anything),
 *       compared case-insensitively;</li>
 *   <li>when {@code multiple} is off only the first accepted file is kept; the others are neither
 *       selected nor reported as rejected (they passed validation, there just is room for one).</li>
 * </ul>
 *
 * <p>This is a name-based check, not content validation: a file called {@code a.png} can contain
 * anything. Treat it as a usability filter, and validate content (type, size) before using it.
 *
 * <p>Not part of FXKit's public API.
 */
public final class FileSelection {

    private FileSelection() {
        // static utility class, not meant to be instantiated
    }

    /**
     * @param accepted the files to select, in the order offered (at most one unless multiple)
     * @param rejected the files that failed validation, in the order offered
     */
    public record Result(List<File> accepted, List<File> rejected) {
    }

    /**
     * @param candidates the offered files; {@code null} elements are ignored
     * @param patterns   extension patterns, empty to accept any file
     * @param multiple   whether more than one accepted file may be selected
     * @return the accepted and rejected files (both immutable)
     * @throws NullPointerException if {@code candidates} or {@code patterns} is {@code null}
     */
    public static Result select(List<File> candidates, List<String> patterns, boolean multiple) {
        return select(candidates, patterns, multiple, 0);
    }

    /**
     * Same as {@link #select(List, List, boolean)}, plus a size limit and a symlink check.
     *
     * <p>A symbolic link is judged by its <em>target</em> as well as by its own name: a link called
     * {@code photo.png} that points at {@code /etc/passwd} (or at a {@code .exe}) is rejected, because
     * the name filter would otherwise be satisfied by a name the attacker chose.
     *
     * @param maxBytes largest accepted file size in bytes; {@code 0} or negative means no limit
     */
    public static Result select(List<File> candidates, List<String> patterns, boolean multiple, long maxBytes) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(patterns, "patterns");

        List<File> usable = new ArrayList<>();
        List<File> rejected = new ArrayList<>();
        for (File candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if (candidate.isFile() && matches(candidate, patterns) && linkTargetMatches(candidate, patterns)
                    && (maxBytes <= 0 || candidate.length() <= maxBytes)) {
                usable.add(candidate);
            } else {
                rejected.add(candidate);
            }
        }
        List<File> accepted = multiple || usable.size() <= 1 ? usable : usable.subList(0, 1);
        return new Result(List.copyOf(accepted), List.copyOf(rejected));
    }

    /** @return whether the file's name matches one of {@code patterns}; true if there are none */
    public static boolean matches(File file, List<String> patterns) {
        if (patterns.isEmpty()) {
            return true;
        }
        String fileName = file.getName();
        for (String pattern : patterns) {
            if (pattern == null) {
                continue;
            }
            String p = pattern.strip();
            if (p.equals("*") || p.equals("*.*") || globToRegex(p).matcher(fileName).matches()) {
                return true;
            }
        }
        return false;
    }

    /** For a symlink, the real target must match the patterns too; for anything else this is true. */
    private static boolean linkTargetMatches(File candidate, List<String> patterns) {
        try {
            if (patterns.isEmpty() || !Files.isSymbolicLink(candidate.toPath())) {
                return true;
            }
            return matches(candidate.toPath().toRealPath().toFile(), patterns);
        } catch (IOException | SecurityException unreadable) {
            return false;
        }
    }

    /** {@code *} and {@code ?} wildcards, everything else literal, whole name, case-insensitive. */
    private static Pattern globToRegex(String glob) {
        StringBuilder regex = new StringBuilder();
        StringBuilder literal = new StringBuilder();
        for (char c : glob.toCharArray()) {
            if (c == '*' || c == '?') {
                if (literal.length() > 0) {
                    regex.append(Pattern.quote(literal.toString()));
                    literal.setLength(0);
                }
                regex.append(c == '*' ? ".*" : ".");
            } else {
                literal.append(c);
            }
        }
        if (literal.length() > 0) {
            regex.append(Pattern.quote(literal.toString()));
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
