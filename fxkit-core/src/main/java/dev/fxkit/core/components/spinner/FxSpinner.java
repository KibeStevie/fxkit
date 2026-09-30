package dev.fxkit.core.components.spinner;

import java.util.Objects;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * A loading spinner, modelled on Flowbite React's {@code <Spinner>}: a gray
 * track ring with a
 * colored arc that rotates once per second.
 *
 * <pre>{@code
 * FxSpinner spinner = new FxSpinner("Loading results");
 * spinner.setColor(FxSpinner.Color.SUCCESS);
 * spinner.setSize(FxSpinner.Size.LG);
 * }</pre>
 *
 * <h2>Flowbite mapping (spinner theme)</h2>
 * 
 * <pre>
 *   base            inline animate-spin text-gray-200      -> track ring, 1s linear infinite rotation
 *   color           fill-primary-600 / cyan / red / ...    -> {@link Color}
 *   size            h-3 / h-4 / h-6 / h-8 / h-10           -> {@link Size}: 12 / 16 / 24 / 32 / 40 px
 *   light.off       dark:text-gray-600, gray dark:fill-gray-300
 *   light.on        no dark variants                       -> {@link #lightProperty()}
 *   aria-label      accessible label                       -> {@link #setAccessibleText(String)}
 * </pre>
 *
 * <h2>Geometry is set in Java, colors in CSS</h2>
 * JavaFX CSS cannot set a {@code Circle}'s radius or make a stroke width
 * proportional to the node's
 * size, so the ring geometry (radius, stroke width) is derived from
 * {@link Size#pixels()} here.
 * Everything else (stroke colors, line cap) lives in {@code components.css} /
 * {@code colors.css}, so
 * the spinner follows the light/dark theme like every other FXKit component.
 * The size and color
 * style classes ({@code fxk-spinner-size-*}, {@code fxk-spinner-color-*}) are
 * still applied, so your
 * own stylesheet can hook into them.
 *
 * <h2>Alignment</h2>
 * The spinner has a fixed square size, so alignment is the parent's job,
 * exactly like any other
 * node: {@code HBox.setAlignment(Pos.CENTER)},
 * {@code StackPane.setAlignment(spinner, Pos.TOP_RIGHT)}
 * and so on. There is no alignment property on the spinner itself.
 *
 * <h2>Animation lifecycle</h2>
 * The rotation runs only while the spinner is in a scene and visible, and
 * pauses otherwise, so a
 * hidden spinner costs no animation pulses.
 *
 * <h2>Accessibility</h2>
 * The node reports the {@link AccessibleRole#PROGRESS_INDICATOR} role. Its
 * accessible text
 * (Flowbite's {@code aria-label}) defaults to {@code "Loading"}; pass something
 * more specific.
 */
public class FxSpinner extends Pane {

    /**
     * Spinner diameters, matching Flowbite's {@code h-3 w-3} ... {@code h-10 w-10}.
     */
    public enum Size {
        XS(12), SM(16), MD(24), LG(32), XL(40);

        private final double pixels;

        Size(double pixels) {
            this.pixels = pixels;
        }

        /** @return the width and height of the spinner in pixels */
        public double pixels() {
            return pixels;
        }
    }

    /**
     * Arc colors. {@code INFO}/{@code CYAN}, {@code FAILURE}/{@code RED},
     * {@code SUCCESS}/{@code GREEN}
     * and {@code WARNING}/{@code YELLOW} are synonyms, as elsewhere in FXKit.
     * {@code DEFAULT} follows the
     * theme's primary color.
     */
    public enum Color {
        DEFAULT, GRAY,
        INFO, CYAN,
        FAILURE, RED,
        SUCCESS, GREEN,
        WARNING, YELLOW,
        PINK, PURPLE,
        INDIGO, BLUE, LIME, TEAL
    }

    /**
     * Ring thickness as a fraction of the diameter (Flowbite's SVG: 9.08 of 100).
     */
    private static final double STROKE_RATIO = 0.0908;

    /**
     * The arc Flowbite draws: roughly the top-right quarter of the ring. Degrees,
     * counter-clockwise from 3 o'clock.
     */
    private static final double ARC_START_ANGLE = 20;
    private static final double ARC_LENGTH = 80;

    /** Tailwind's {@code animate-spin}: one full turn per second, linear. */
    private static final Duration SPIN_DURATION = Duration.seconds(1);

    private static final String DEFAULT_ACCESSIBLE_TEXT = "Loading";

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", Color.DEFAULT);
    private final BooleanProperty light = new SimpleBooleanProperty(this, "light", false);

    private final Circle track = new Circle();
    private final Arc arc = new Arc();
    /**
     * Holds only the arc, so rotating it turns the arc around the ring's center
     * while the track stays put.
     */
    private final Pane rotor = new Pane();
    private final RotateTransition spin = new RotateTransition(SPIN_DURATION, rotor);

    /**
     * Creates a medium, default-colored spinner with the accessible text
     * {@code "Loading"}.
     */
    public FxSpinner() {
        this(DEFAULT_ACCESSIBLE_TEXT);
    }

    /**
     * Creates a medium, default-colored spinner.
     *
     * @param accessibleText what screen readers announce, Flowbite's
     *                       {@code aria-label}
     */
    public FxSpinner(String accessibleText) {
        getStyleClass().add("fxk-spinner");
        EnumStyleClassSync.sync(this, "fxk-spinner-size-", size);
        EnumStyleClassSync.sync(this, "fxk-spinner-color-", color);
        BooleanStyleClassSync.sync(this, "fxk-spinner-light", light);

        track.getStyleClass().add("fxk-spinner-track");
        track.setFill(null);

        arc.getStyleClass().add("fxk-spinner-arc");
        arc.setType(ArcType.OPEN);
        arc.setFill(null);
        arc.setStartAngle(ARC_START_ANGLE);
        arc.setLength(ARC_LENGTH);

        rotor.getStyleClass().add("fxk-spinner-rotor");
        rotor.getChildren().add(arc);
        getChildren().addAll(track, rotor);

        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);

        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        setAccessibleText(accessibleText);

        size.addListener((observable, oldValue, newValue) -> updateGeometry());
        updateGeometry();

        sceneProperty().addListener((observable, oldValue, newValue) -> updateAnimation());
        visibleProperty().addListener((observable, oldValue, newValue) -> updateAnimation());
        updateAnimation();
    }

    // ---- size
    // ---------------------------------------------------------------------------------

    /** @return the size property; {@code null} is treated as {@link Size#MD} */
    public ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public Size getSize() {
        return size.get();
    }

    public void setSize(Size value) {
        size.set(Objects.requireNonNull(value, "size"));
    }

    // ---- color
    // --------------------------------------------------------------------------------

    /**
     * @return the color property; {@code null} falls back to the theme's primary
     *         color
     */
    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color value) {
        color.set(Objects.requireNonNull(value, "color"));
    }

    // ---- light
    // --------------------------------------------------------------------------------

    /**
     * Flowbite's {@code light} flag. When {@code false} (the default) the spinner
     * has a dark-theme
     * variant: a darker track, and a lighter arc for {@link Color#GRAY}. When
     * {@code true} it keeps its
     * light-theme colors in the dark theme, for use on a surface that stays light.
     *
     * @return the light property
     */
    public BooleanProperty lightProperty() {
        return light;
    }

    public boolean isLight() {
        return light.get();
    }

    public void setLight(boolean value) {
        light.set(value);
    }

    // ---- internals
    // ----------------------------------------------------------------------------

    private void updateGeometry() {
        Size current = size.get() == null ? Size.MD : size.get();
        double diameter = current.pixels();
        double stroke = diameter * STROKE_RATIO;
        double center = diameter / 2;
        double radius = center - stroke / 2; // stroke is centered on the path, so keep it inside the box

        setMinSize(diameter, diameter);
        setPrefSize(diameter, diameter);
        setMaxSize(diameter, diameter);

        rotor.setMinSize(diameter, diameter);
        rotor.setPrefSize(diameter, diameter);
        rotor.setMaxSize(diameter, diameter);

        track.setCenterX(center);
        track.setCenterY(center);
        track.setRadius(radius);
        track.setStrokeWidth(stroke);

        arc.setCenterX(center);
        arc.setCenterY(center);
        arc.setRadiusX(radius);
        arc.setRadiusY(radius);
        arc.setStrokeWidth(stroke);
    }

    private void updateAnimation() {
        boolean shouldRun = getScene() != null && isVisible();
        if (shouldRun) {
            if (spin.getStatus() != Animation.Status.RUNNING) {
                spin.play();
            }
        } else {
            spin.pause();
        }
    }
}
