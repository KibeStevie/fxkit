package dev.fxkit.core;

import java.net.URL;

/**
 * Entry point and metadata for the FXKit library.
 *
 * <p>Exposes the library version and the location of the FXKit design-token
 * stylesheet. Later it can host global setup such as theme initialisation
 * (see the planned {@code ThemeManager}).
 */
public final class FxKit {

    /** Current library version. TODO: read from the POM via resource filtering. */
    public static final String VERSION = "0.1.0-SNAPSHOT";

    /** Name of the design-token stylesheet, located next to this class. */
    private static final String TOKENS_CSS = "tokens.css";

    private FxKit() {
        // static utility class, not meant to be instantiated
    }

    /**
     * @return the FXKit version string
     */
    public static String version() {
        return VERSION;
    }

    /**
     * Returns the URL of FXKit's design-token stylesheet, ready to pass to
     * {@code scene.getStylesheets().add(...)}.
     *
     * <p>Why a method instead of asking for the file by path? In a named Java
     * module, resources inside a package are encapsulated: other modules cannot
     * find them unless the package is opened. Because this method runs inside
     * {@code dev.fxkit.core}, the lookup succeeds and callers only receive the
     * resulting URL string.
     *
     * @return the external form of the {@code tokens.css} URL
     * @throws IllegalStateException if the stylesheet is missing from the module
     */
    public static String tokensStylesheet() {
        URL url = FxKit.class.getResource(TOKENS_CSS);
        if (url == null) {
            throw new IllegalStateException(
                    TOKENS_CSS + " not found in module dev.fxkit.core "
                            + "(expected at dev/fxkit/core/" + TOKENS_CSS + " in src/main/resources)");
        }
        return url.toExternalForm();
    }
}
