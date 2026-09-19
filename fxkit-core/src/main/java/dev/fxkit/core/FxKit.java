package dev.fxkit.core;

/**
 * Entry point and metadata for the FXKit library.
 *
 * <p>For now this only exposes the library version. It exists so the showcase
 * app can prove that it really depends on {@code fxkit-core}. Later it can host
 * global setup such as theme initialisation.
 */
public final class FxKit {

    /** Current library version. TODO: read from the POM via resource filtering. */
    public static final String VERSION = "0.1.0-SNAPSHOT";

    private FxKit() {
        // static utility class, not meant to be instantiated
    }

    /**
     * @return the FXKit version string
     */
    public static String version() {
        return VERSION;
    }
}
