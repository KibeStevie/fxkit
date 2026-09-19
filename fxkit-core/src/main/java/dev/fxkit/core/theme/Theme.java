package dev.fxkit.core.theme;

/**
 * The color themes FXKit ships with.
 *
 * @see ThemeManager
 */
public enum Theme {

    /** The default theme: the semantic tokens as defined on {@code .root}. */
    LIGHT,

    /** Redefines the semantic tokens under {@code .root.dark}. */
    DARK;

    /**
     * @return the other theme
     */
    public Theme opposite() {
        return this == LIGHT ? DARK : LIGHT;
    }
}
