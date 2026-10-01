package dev.fxkit.core.components.carousel;

import java.util.Objects;
import java.util.function.IntConsumer;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A slideshow: any number of slides, previous / next controls, and one
 * indicator dot per slide.
 * Modelled on Flowbite React's {@code Carousel}.
 *
 * <pre>{@code
 * FxCarousel carousel = new FxCarousel(
 *         FxCarousel.image("file:img/one.png"),
 *         FxCarousel.image("file:img/two.png"),
 *         myCustomPane);
 * carousel.setSize(FxCarousel.Size.LG);
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <table>
 * <caption>Flowbite props and their FxCarousel counterparts</caption>
 * <tr>
 * <td>{@code <Carousel>} children</td>
 * <td>{@link #getSlides()} (or the constructor)</td>
 * </tr>
 * <tr>
 * <td>{@code slide} (default {@code true})</td>
 * <td>{@link #slideProperty() slide}</td>
 * </tr>
 * <tr>
 * <td>{@code slideInterval} (3000)</td>
 * <td>{@link #slideIntervalProperty() slideInterval}, in ms</td>
 * </tr>
 * <tr>
 * <td>{@code leftControl} / {@code rightControl}</td>
 * <td>{@link #leftControlProperty()} / {@link #rightControlProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code indicators} (true)</td>
 * <td>{@link #indicatorsProperty() indicators}</td>
 * </tr>
 * <tr>
 * <td>{@code pauseOnHover} (false)</td>
 * <td>{@link #pauseOnHoverProperty() pauseOnHover}</td>
 * </tr>
 * <tr>
 * <td>{@code onSlideChange(index)}</td>
 * <td>{@link #onSlideChangeProperty() onSlideChange}</td>
 * </tr>
 * <tr>
 * <td>container {@code h-56 sm:h-64 xl:h-80 2xl:h-96}</td>
 * <td>{@link #sizeProperty() size} (224 / 256 / 320 / 384 px)</td>
 * </tr>
 * </table>
 *
 * <h2>Behavior</h2>
 * <ul>
 * <li>Auto-sliding wraps around: after the last slide it scrolls back to the
 * first, like Flowbite.
 * {@link #next()} and {@link #previous()} wrap too. Any manual navigation
 * restarts the interval.</li>
 * <li>Auto-sliding only runs while the carousel is in a scene, and is skipped
 * while that scene's
 * window is hidden. A static carousel ({@code slide = false}) still has working
 * controls
 * and indicators.</li>
 * <li>Slides can be dragged with the mouse (or a touch, which JavaFX turns into
 * mouse events).
 * Dragging does not wrap: it stops at the first and last slide.</li>
 * <li>The indicator dots are mouse-only (not focus-traversable) to avoid one
 * Tab stop per slide;
 * the two controls are the keyboard path.</li>
 * <li>Flowbite's touch-and-hold pause has no separate equivalent: JavaFX
 * reports no hover for touch.</li>
 * </ul>
 *
 * <h2>Slides</h2>
 * Every slide is laid out as a full-size page of the carousel:
 * <ul>
 * <li>a {@link Region} (any {@code Pane}, {@code Control}, ...) is stretched to
 * fill the page, the
 * equivalent of Flowbite's {@code h-full} - wrap a {@code Label} in a
 * {@code StackPane} to center it;</li>
 * <li>an {@link ImageView} is scaled to the page width (aspect ratio kept) and
 * centered vertically;
 * taller images are cropped, like Flowbite's images. Its {@code fitWidth}
 * becomes bound;</li>
 * <li>any other node is centered at its natural size.</li>
 * </ul>
 * A node can be in the list only once.
 *
 * <h2>Height</h2>
 * The carousel fills the width it is given. Its height comes from {@link Size}
 * (default
 * {@link Size#MD}) which is applied by CSS. To use your own height, set
 * {@code size} to {@code null}
 * and then call {@link #setPrefHeight(double)} - a pref height set in code
 * would otherwise lose
 * to the stylesheet.
 *
 * <h2>Styling</h2>
 * Structure: {@code .fxk-carousel} &gt; {@code .fxk-carousel-viewport} (clipped
 * to 8px corners)
 * &gt; track &gt; {@code .fxk-carousel-slide}; plus two
 * {@code .fxk-carousel-control} buttons and
 * {@code .fxk-carousel-indicators} holding {@code .fxk-carousel-indicator} dots
 * (the selected one also has
 * {@code .fxk-carousel-indicator-active}). Colors are looked-up tokens defined
 * in {@code components.css}
 * ({@code -fxk-carousel-control-bg}, {@code -fxk-carousel-dot}, ...), so a
 * caller can recolor one
 * carousel by redefining them in its inline style.
 */
public class FxCarousel extends StackPane {

    /**
     * The carousel's height. Flowbite's example container is
     * {@code h-56 sm:h-64 xl:h-80 2xl:h-96}; a
     * desktop app has no breakpoints, so each step is a fixed size instead.
     */
    public enum Size {
        /** 224px ({@code h-56}). */
        SM,
        /** 256px ({@code h-64}). The default. */
        MD,
        /** 320px ({@code h-80}). */
        LG,
        /** 384px ({@code h-96}). */
        XL
    }

    private static final String ACTIVE_INDICATOR = "fxk-carousel-indicator-active";

    /**
     * rounded-lg. The viewport clip is drawn in Java, so this mirrors the radius
     * used in CSS.
     */
    private static final double RADIUS = 8;
    /** Flowbite's {@code scroll-smooth}. */
    private static final Duration TRANSITION = Duration.millis(450);
    /** How far (px) the mouse must move before a press becomes a drag. */
    private static final double DRAG_SLOP = 5;
    /**
     * Fraction of a slide the drag must cover to move to the neighbouring slide.
     */
    private static final double SWIPE_THRESHOLD = 0.15;
    /** Pref width when the parent does not stretch the carousel. */
    private static final double DEFAULT_PREF_WIDTH = 480;

    private final ObservableList<Node> slides = FXCollections.observableArrayList();

    private final BooleanProperty slide = new SimpleBooleanProperty(this, "slide", true);
    private final IntegerProperty slideInterval = new SimpleIntegerProperty(this, "slideInterval", 3000);
    private final BooleanProperty indicators = new SimpleBooleanProperty(this, "indicators", true);
    private final BooleanProperty pauseOnHover = new SimpleBooleanProperty(this, "pauseOnHover", false);
    private final ObjectProperty<Node> leftControl = new SimpleObjectProperty<>(this, "leftControl");
    private final ObjectProperty<Node> rightControl = new SimpleObjectProperty<>(this, "rightControl");
    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<IntConsumer> onSlideChange = new SimpleObjectProperty<>(this, "onSlideChange");
    private final ReadOnlyIntegerWrapper selectedIndex = new ReadOnlyIntegerWrapper(this, "selectedIndex", 0);

    /** Fractional slide index currently shown; animated between whole numbers. */
    private final DoubleProperty position = new SimpleDoubleProperty(this, "position", 0);
    private final BooleanProperty dragging = new SimpleBooleanProperty(this, "dragging", false);

    private final StackPane viewport = new StackPane();
    private final Track track = new Track();
    private final HBox indicatorBox = new HBox();
    private final Button leftButton = new Button();
    private final Button rightButton = new Button();
    private final Timeline timer = new Timeline();
    private Timeline transition;

    // drag state
    private boolean pressed;
    private double pressX;
    private double dragBaseX;
    private double dragBasePosition;

    /**
     * Creates a carousel showing {@code initialSlides} (possibly none), with a
     * {@link Size#MD} height,
     * auto-sliding every 3 seconds, controls and indicators.
     *
     * @param initialSlides the first slides
     */
    public FxCarousel(Node... initialSlides) {
        getStyleClass().add("fxk-carousel");
        EnumStyleClassSync.sync(this, "fxk-carousel-size-", size);
        BooleanStyleClassSync.sync(this, "fxk-carousel-dragging", dragging);

        buildViewport();
        buildControls();
        buildIndicators();
        getChildren().addAll(viewport, leftButton, rightButton, indicatorBox);

        slides.addListener((ListChangeListener<Node>) change -> slidesChanged());
        selectedIndex.addListener((observable, oldValue, newValue) -> {
            updateIndicators();
            IntConsumer callback = getOnSlideChange();
            if (callback != null) {
                callback.accept(newValue.intValue());
            }
        });

        leftControl.addListener(observable -> refreshControls());
        rightControl.addListener(observable -> refreshControls());
        refreshControls();

        slide.addListener(observable -> updateTimer());
        slideInterval.addListener(observable -> updateTimer());
        pauseOnHover.addListener(observable -> updateTimer());
        dragging.addListener(observable -> updateTimer());
        hoverProperty().addListener(observable -> updateTimer());
        sceneProperty().addListener(observable -> updateTimer());

        slides.addAll(initialSlides);
    }

    // ---- convenience factories
    // ---------------------------------------------------------------

    /**
     * An {@link ImageView} for {@code url}, loaded in the background, ready to be
     * used as a slide.
     *
     * @param url an image URL, for example {@code "file:img/one.png"} or a
     *            classpath resource's
     *            {@code toExternalForm()}
     * @return the image view
     */
    public static ImageView image(String url) {
        return image(new Image(Objects.requireNonNull(url, "url"), true));
    }

    /**
     * An {@link ImageView} for {@code image}, ready to be used as a slide.
     *
     * @param image the image
     * @return the image view
     */
    public static ImageView image(Image image) {
        return new ImageView(Objects.requireNonNull(image, "image"));
    }

    // ---- navigation
    // --------------------------------------------------------------------------

    /**
     * Shows the next slide, wrapping from the last to the first, and restarts the
     * auto-slide interval.
     */
    public void next() {
        advance(1);
        updateTimer();
    }

    /**
     * Shows the previous slide, wrapping from the first to the last, and restarts
     * the auto-slide interval.
     */
    public void previous() {
        advance(-1);
        updateTimer();
    }

    /**
     * Shows the slide at {@code index} and restarts the auto-slide interval.
     *
     * @param index the slide to show, {@code 0} to {@code getSlides().size() - 1}
     * @throws IndexOutOfBoundsException if there is no such slide
     */
    public void select(int index) {
        Objects.checkIndex(index, slides.size());
        goTo(index);
        updateTimer();
    }

    private void advance(int step) {
        int count = slides.size();
        if (count == 0) {
            return;
        }
        goTo(Math.floorMod(selectedIndex.get() + step, count));
    }

    private void goTo(int target) {
        selectedIndex.set(target);
        animateTo(target);
    }

    private void animateTo(double target) {
        stopTransition();
        if (getScene() == null) {
            position.set(target); // nothing to animate on screen yet
            return;
        }
        transition = new Timeline(new KeyFrame(TRANSITION,
                new KeyValue(position, target, Interpolator.EASE_BOTH)));
        transition.play();
    }

    private void stopTransition() {
        if (transition != null) {
            transition.stop();
            transition = null;
        }
    }

    // ---- auto-slide
    // --------------------------------------------------------------------------

    /**
     * (Re)starts the interval if auto-sliding should currently run, otherwise stops
     * it. Called whenever
     * anything it depends on changes, and after every manual navigation (Flowbite
     * restarts its
     * interval whenever the active slide changes).
     */
    private void updateTimer() {
        timer.stop();
        boolean run = slide.get()
                && slides.size() > 1
                && getScene() != null
                && !dragging.get()
                && !(pauseOnHover.get() && isHover());
        if (!run) {
            return;
        }
        timer.getKeyFrames().setAll(new KeyFrame(
                Duration.millis(Math.max(1, slideInterval.get())), event -> autoAdvance()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.playFromStart();
    }

    private void autoAdvance() {
        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()) {
            return;
        }
        advance(1);
    }

    // ---- building the parts
    // ------------------------------------------------------------------

    private void buildViewport() {
        viewport.getStyleClass().add("fxk-carousel-viewport");
        viewport.getChildren().add(track);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(RADIUS * 2);
        clip.setArcHeight(RADIUS * 2);
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());
        viewport.setClip(clip);

        // The track is laid out once (slide i at x = i * width); showing slide p just
        // shifts it.
        track.translateXProperty().bind(position.multiply(-1).multiply(track.widthProperty()));

        viewport.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onPressed);
        viewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        viewport.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onReleased);
    }

    private void buildControls() {
        configureControl(leftButton, Pos.CENTER_LEFT, "fxk-carousel-control-left", "Previous slide");
        configureControl(rightButton, Pos.CENTER_RIGHT, "fxk-carousel-control-right", "Next slide");
        leftButton.setOnAction(event -> previous());
        rightButton.setOnAction(event -> next());
    }

    private static void configureControl(Button button, Pos alignment, String styleClass, String accessibleText) {
        button.getStyleClass().addAll("fxk-carousel-control", styleClass);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setAccessibleText(accessibleText);
        // absolute top-0 h-full: the whole edge of the carousel is the click target
        button.setMaxHeight(Double.MAX_VALUE);
        StackPane.setAlignment(button, alignment);
    }

    private void buildIndicators() {
        indicatorBox.getStyleClass().add("fxk-carousel-indicators");
        indicatorBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        indicatorBox.setPickOnBounds(false); // the gaps between dots do not swallow drags
        StackPane.setAlignment(indicatorBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(indicatorBox, new Insets(0, 0, 20, 0)); // bottom-5
        indicatorBox.visibleProperty().bind(indicators);
        indicatorBox.managedProperty().bind(indicators);
    }

    private void refreshControls() {
        Node left = getLeftControl();
        Node right = getRightControl();
        leftButton.setGraphic(left != null ? left : defaultControlGraphic(true));
        rightButton.setGraphic(right != null ? right : defaultControlGraphic(false));
    }

    /**
     * Flowbite's {@code DefaultLeftRight}: a translucent circle holding a chevron.
     */
    private static Node defaultControlGraphic(boolean left) {
        Polyline chevron = new Polyline(left
                ? new double[] { 15.75, 4.5, 8.25, 12, 15.75, 19.5 }
                : new double[] { 8.25, 4.5, 15.75, 12, 8.25, 19.5 });
        chevron.getStyleClass().add("fxk-carousel-control-icon");
        StackPane circle = new StackPane(chevron);
        circle.getStyleClass().add("fxk-carousel-control-circle");
        return circle;
    }

    private void slidesChanged() {
        stopTransition();
        track.getChildren().clear();
        for (Node node : slides) {
            track.getChildren().add(new Slide(node));
        }

        int count = slides.size();
        int index = count == 0 ? 0 : Math.min(selectedIndex.get(), count - 1);
        selectedIndex.set(index);
        position.set(index);

        rebuildIndicators();
        updateTimer();
    }

    private void rebuildIndicators() {
        indicatorBox.getChildren().clear();
        for (int i = 0; i < slides.size(); i++) {
            final int target = i;
            Button dot = new Button();
            dot.getStyleClass().add("fxk-carousel-indicator");
            dot.setFocusTraversable(false);
            dot.setAccessibleText("Slide " + (i + 1));
            dot.setOnAction(event -> select(target));
            indicatorBox.getChildren().add(dot);
        }
        updateIndicators();
    }

    private void updateIndicators() {
        ObservableList<Node> dots = indicatorBox.getChildren();
        for (int i = 0; i < dots.size(); i++) {
            ObservableList<String> classes = dots.get(i).getStyleClass();
            classes.remove(ACTIVE_INDICATOR);
            if (i == selectedIndex.get()) {
                classes.add(ACTIVE_INDICATOR);
            }
        }
    }

    // ---- dragging
    // ----------------------------------------------------------------------------

    private void onPressed(MouseEvent event) {
        pressed = event.getButton() == MouseButton.PRIMARY && slides.size() > 1;
        pressX = event.getSceneX();
    }

    private void onDragged(MouseEvent event) {
        if (!pressed) {
            return;
        }
        if (!dragging.get()) {
            if (Math.abs(event.getSceneX() - pressX) < DRAG_SLOP) {
                return;
            }
            // From here the track follows the mouse. The base is taken now, so it never
            // jumps by the slop.
            stopTransition();
            dragBaseX = event.getSceneX();
            dragBasePosition = position.get();
            dragging.set(true);
        }
        double width = viewport.getWidth();
        if (width <= 0) {
            return;
        }
        double dragged = (event.getSceneX() - dragBaseX) / width;
        double lastIndex = slides.size() - 1;
        position.set(Math.max(0, Math.min(lastIndex, dragBasePosition - dragged)));
    }

    private void onReleased(MouseEvent event) {
        pressed = false;
        if (!dragging.get()) {
            return;
        }
        int base = selectedIndex.get();
        double delta = position.get() - base;
        int target = base;
        if (delta > SWIPE_THRESHOLD) {
            target = base + 1;
        } else if (delta < -SWIPE_THRESHOLD) {
            target = base - 1;
        }
        target = Math.max(0, Math.min(slides.size() - 1, target));
        dragging.set(false); // also restarts the auto-slide interval
        goTo(target);
    }

    // ---- properties
    // --------------------------------------------------------------------------

    /**
     * The slides, in order. Modify it directly; indicators and the auto-slide timer
     * follow.
     *
     * @return the live list of slides
     */
    public final ObservableList<Node> getSlides() {
        return slides;
    }

    /**
     * Whether the carousel moves to the next slide by itself ({@code true} by
     * default). Setting it to
     * {@code false} makes a static carousel; controls and indicators keep working.
     *
     * @return the property
     */
    public final BooleanProperty slideProperty() {
        return slide;
    }

    public final boolean isSlide() {
        return slide.get();
    }

    public final void setSlide(boolean value) {
        slide.set(value);
    }

    /**
     * Milliseconds between automatic slide changes. Default {@code 3000}.
     *
     * @return the property
     */
    public final IntegerProperty slideIntervalProperty() {
        return slideInterval;
    }

    public final int getSlideInterval() {
        return slideInterval.get();
    }

    public final void setSlideInterval(int millis) {
        slideInterval.set(millis);
    }

    /**
     * Whether the row of indicator dots is shown. Default {@code true}.
     *
     * @return the property
     */
    public final BooleanProperty indicatorsProperty() {
        return indicators;
    }

    public final boolean isIndicators() {
        return indicators.get();
    }

    public final void setIndicators(boolean value) {
        indicators.set(value);
    }

    /**
     * Whether auto-sliding pauses while the mouse is over the carousel. Default
     * {@code false}.
     *
     * @return the property
     */
    public final BooleanProperty pauseOnHoverProperty() {
        return pauseOnHover;
    }

    public final boolean isPauseOnHover() {
        return pauseOnHover.get();
    }

    public final void setPauseOnHover(boolean value) {
        pauseOnHover.set(value);
    }

    /**
     * Replaces the default left control (a translucent circle with a chevron). Like
     * Flowbite's
     * {@code leftControl}, the node replaces the <em>whole</em> visual inside the
     * button, the circle
     * included; the button itself still spans the full height of the left edge and
     * calls
     * {@link #previous()}. {@code null} restores the default.
     *
     * @return the property
     * @see #setLeftControlText(String)
     */
    public final ObjectProperty<Node> leftControlProperty() {
        return leftControl;
    }

    public final Node getLeftControl() {
        return leftControl.get();
    }

    public final void setLeftControl(Node node) {
        leftControl.set(node);
    }

    /**
     * Shortcut for a text control, as in Flowbite's {@code leftControl="left"}.
     *
     * @param text the text, shown in the carousel's control color
     */
    public final void setLeftControlText(String text) {
        setLeftControl(controlLabel(text));
    }

    /**
     * Replaces the default right control; see {@link #leftControlProperty()}.
     *
     * @return the property
     * @see #setRightControlText(String)
     */
    public final ObjectProperty<Node> rightControlProperty() {
        return rightControl;
    }

    public final Node getRightControl() {
        return rightControl.get();
    }

    public final void setRightControl(Node node) {
        rightControl.set(node);
    }

    /**
     * Shortcut for a text control, as in Flowbite's {@code rightControl="right"}.
     *
     * @param text the text, shown in the carousel's control color
     */
    public final void setRightControlText(String text) {
        setRightControl(controlLabel(text));
    }

    private static Label controlLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("fxk-carousel-control-text");
        return label;
    }

    /**
     * The carousel's height, applied by CSS. Default {@link Size#MD}. {@code null}
     * leaves the height
     * to {@link #setPrefHeight(double)} or, failing that, to the slides' own
     * preferred heights.
     *
     * @return the property
     */
    public final ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public final Size getSize() {
        return size.get();
    }

    public final void setSize(Size value) {
        size.set(value);
    }

    /**
     * Called with the new slide index whenever the shown slide changes, whether by
     * auto-slide, the
     * controls, the indicators, dragging, or {@link #select(int)}. Not called for
     * the initial slide.
     * Runs on the JavaFX Application Thread.
     *
     * @return the property
     */
    public final ObjectProperty<IntConsumer> onSlideChangeProperty() {
        return onSlideChange;
    }

    public final IntConsumer getOnSlideChange() {
        return onSlideChange.get();
    }

    public final void setOnSlideChange(IntConsumer callback) {
        onSlideChange.set(callback);
    }

    /**
     * Index of the slide being shown (or animating in). While the carousel has no
     * slides it is {@code 0}.
     * Change it with {@link #select(int)}, {@link #next()} or {@link #previous()}.
     *
     * @return the read-only property
     */
    public final ReadOnlyIntegerProperty selectedIndexProperty() {
        return selectedIndex.getReadOnlyProperty();
    }

    public final int getSelectedIndex() {
        return selectedIndex.get();
    }

    // ---- inner layout nodes
    // ------------------------------------------------------------------

    /**
     * Lays the slides side by side: slide {@code i} at {@code x = i * width}. The
     * viewport clips
     * everything but the visible page.
     */
    private static final class Track extends Pane {

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            int index = 0;
            for (Node child : getChildren()) {
                child.resizeRelocate(index * width, 0, width, height);
                index++;
            }
        }

        @Override
        protected double computeMinWidth(double height) {
            return 0;
        }

        @Override
        protected double computeMinHeight(double width) {
            return 0;
        }

        @Override
        protected double computePrefWidth(double height) {
            return DEFAULT_PREF_WIDTH;
        }

        @Override
        protected double computePrefHeight(double width) {
            double max = 0;
            for (Node child : getChildren()) {
                max = Math.max(max, child.prefHeight(width));
            }
            return max;
        }
    }

    /**
     * One page of the track, holding the caller's node. See the class docs,
     * "Slides".
     */
    private static final class Slide extends Pane {

        Slide(Node content) {
            getStyleClass().add("fxk-carousel-slide");
            if (content instanceof ImageView image) {
                image.setPreserveRatio(true);
                image.fitWidthProperty().bind(widthProperty());
            }
            getChildren().add(content);
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            for (Node child : getManagedChildren()) {
                if (child.isResizable()) {
                    child.resizeRelocate(0, 0, width, height);
                } else {
                    Bounds bounds = child.getLayoutBounds();
                    child.relocate((width - bounds.getWidth()) / 2, (height - bounds.getHeight()) / 2);
                }
            }
        }

        @Override
        protected double computeMinWidth(double height) {
            return 0;
        }

        @Override
        protected double computeMinHeight(double width) {
            return 0;
        }

        @Override
        protected double computePrefWidth(double height) {
            double max = 0;
            for (Node child : getManagedChildren()) {
                max = Math.max(max, child.prefWidth(height));
            }
            return max;
        }

        @Override
        protected double computePrefHeight(double width) {
            double max = 0;
            for (Node child : getManagedChildren()) {
                max = Math.max(max, child.prefHeight(width));
            }
            return max;
        }
    }
}
