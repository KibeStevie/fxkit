package dev.fxkit.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(patterns, "patterns");

        List<File> usable = new ArrayList<>();
        List<File> rejected = new ArrayList<>();
        for (File candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            if (candidate.isFile() && matches(candidate, patterns)) {
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
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        for (String pattern : patterns) {
            if (pattern == null) {
                continue;
            }
            String p = pattern.toLowerCase(Locale.ROOT);
            if (p.equals("*") || p.equals("*.*") || fileName.endsWith(p.startsWith("*") ? p.substring(1) : p)) {
                return true;
            }
        }
        return false;
    }
}
