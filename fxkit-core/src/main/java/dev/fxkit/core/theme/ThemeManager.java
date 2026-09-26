package dev.fxkit.core.theme;

import java.util.List;
import java.util.Objects;

import dev.fxkit.core.FxKit;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Applies an FXKit {@link Theme} to a {@link Scene} with one call:
 *
 * <pre>{@code
 * Scene scene = new Scene(root, 960, 720);
 * ThemeManager.apply(scene, Theme.DARK);
 * }</pre>
 *
 * <p>What {@code apply} does:
 * <ol>
 *   <li>makes sure FXKit's stylesheets ({@code tokens.css}, {@code utilities.css},
 *       {@code components.css}, then {@code colors.css}) are in the scene, exactly once, and
 *       placed <em>before</em> your own stylesheets so your CSS can override them;</li>
 *   <li>adds or removes the {@value #DARK_CLASS} style class on the scene's root node,
 *       which switches the semantic tokens ({@code .root.dark} in {@code tokens.css}).</li>
 * </ol>
 *
 * <p>The current theme is read from the root's style classes, so this class holds no state.
 * If you replace the root with {@code scene.setRoot(...)}, call {@code apply} again.
 * Only FXKit tokens follow the theme: JavaFX's built-in controls keep their own look until
 * FXKit's components (Phase 2 onwards) replace them.
 *
 * <p>Like all scene-graph access, call these methods on the JavaFX Application Thread
 * once the scene is showing.
 */
public final class ThemeManager {

    /** Style class that switches the semantic tokens to the dark theme. */
    public static final String DARK_CLASS = "dark";

    private ThemeManager() {
        // static utility class, not meant to be instantiated
    }

    /**
     * Installs FXKit's stylesheets (if missing) and switches the scene to {@code theme}.
     * Safe to call repeatedly: stylesheets are never duplicated.
     *
     * @param scene the scene to style
     * @param theme the theme to show
     */
    public static void apply(Scene scene, Theme theme) {
        Objects.requireNonNull(scene, "scene");
        Objects.requireNonNull(theme, "theme");

        installStylesheets(scene.getStylesheets());

        ObservableList<String> rootClasses = scene.getRoot().getStyleClass();
        rootClasses.removeIf(DARK_CLASS::equals);
        if (theme == Theme.DARK) {
            rootClasses.add(DARK_CLASS);
        }
    }

    /**
     * @param scene the scene to inspect
     * @return the theme currently applied to the scene ({@link Theme#LIGHT} if none was applied)
     */
    public static Theme current(Scene scene) {
        Objects.requireNonNull(scene, "scene");
        Parent root = scene.getRoot();
        return root.getStyleClass().contains(DARK_CLASS) ? Theme.DARK : Theme.LIGHT;
    }

    /**
     * Switches the scene to the other theme.
     *
     * @param scene the scene to switch
     * @return the theme that is now applied
     */
    public static Theme toggle(Scene scene) {
        Theme next = current(scene).opposite();
        apply(scene, next);
        return next;
    }

    /**
     * Puts tokens.css, utilities.css, components.css, then colors.css at the front of the list, each
     * only if absent. colors.css is last because its selectors (.fxk-btn-default.fxk-btn-color-*) are
     * more specific than anything in components.css, so it only needs to come after it conceptually -
     * components.css is what defines .fxk-btn-default in the first place.
     */
    private static void installStylesheets(List<String> sheets) {
        String[] fxkit = {
                FxKit.tokensStylesheet(),
                FxKit.utilitiesStylesheet(),
                FxKit.componentsStylesheet(),
                FxKit.colorsStylesheet()
        };
        for (int position = 0; position < fxkit.length; position++) {
            if (!sheets.contains(fxkit[position])) {
                sheets.add(position, fxkit[position]);
            }
        }
    }
}