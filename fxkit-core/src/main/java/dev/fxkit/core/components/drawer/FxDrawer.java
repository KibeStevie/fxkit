package dev.fxkit.core.components.drawer;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A panel that slides in from one edge of its container, over the rest of the UI (Flowbite's
 * {@code Drawer}).
 *
 * <p>An {@code FxDrawer} is an <em>overlay layer</em>: put it as the <b>last child</b> of a
 * {@link StackPane} that also holds your page, and it fills that pane. While closed it is invisible
 * and never intercepts the mouse (unless {@link #edgeProperty() edge} is on, see below). The layer
 * is clipped to its own bounds, so it also works inside a smaller container than the window.
 *
 * <pre>{@code
 * FxDrawer drawer = new FxDrawer(
 *         new FxDrawerHeader("Menu"),
 *         new FxDrawerItems(new Label("Hello")));
 * drawer.setPosition(FxDrawer.Position.RIGHT);
 *
 * Scene scene = new Scene(new StackPane(page, drawer), 960, 720);
 * drawer.setOpen(true);
 * }</pre>
 *
 * <h2>Closing</h2>
 * Flowbite's {@code open}/{@code onClose} pair maps to {@link #openProperty()} and
 * {@link #onCloseProperty()}. A <em>close request</em> (a click on the backdrop, the header's close
 * button, or Escape) sets {@code open} to {@code false} and then runs {@code onClose}, if any. Call
 * {@link #requestClose()} yourself to do the same from code.
 *
 * <h2>Backdrop and body scrolling</h2>
 * With {@link #backdropProperty() backdrop} on (the default) the page is dimmed and blocked, and a
 * click on the dimmed area closes the drawer. {@link #bodyScrollingProperty() bodyScrolling} decides
 * whether the mouse wheel still scrolls the page behind an open drawer: off (the default) swallows
 * those wheel events, on forwards them to the page.
 *
 * <h2>Edge</h2>
 * With {@link #edgeProperty() edge} on, a closed drawer is not hidden completely: its first
 * {@link #edgeSizeProperty() edgeSize} pixels stay visible, so the user can click (or, with an
 * {@link FxDrawerHeader#onClickProperty() onClick} on the header, toggle) it back open.
 *
 * <h2>Styling</h2>
 * Style classes: {@code fxk-drawer} on the layer, {@code fxk-drawer-position-<position>},
 * {@code fxk-drawer-edge} while {@code edge} is on, {@code fxk-drawer-backdrop},
 * {@code fxk-drawer-panel}, {@code fxk-drawer-content} and {@code fxk-drawer-padded}. Colors come from
 * {@code colors.css}, structure from {@code components.css}.
 *
 * <p>Like all scene-graph access, call these methods on the JavaFX Application Thread.
 */
@DefaultProperty("content")
public class FxDrawer extends StackPane {

    /** Which edge of the container the drawer is attached to. Flowbite's {@code position}. */
    public enum Position {
        TOP, RIGHT, BOTTOM, LEFT
    }

    /** Flowbite's {@code w-80}: the width of a {@link Position#LEFT} / {@link Position#RIGHT} drawer. */
    public static final double DEFAULT_PANEL_WIDTH = 320;

    /** Flowbite's {@code bottom-16}: how much of a closed {@code edge} drawer stays visible. */
    public static final double DEFAULT_EDGE_SIZE = 64;

    /** Tailwind's {@code transition-transform}: 150ms, cubic-bezier(0.4, 0, 0.2, 1). */
    private static final Duration DEFAULT_TRANSITION = Duration.millis(150);

    private static final Interpolator EASE = Interpolator.SPLINE(0.4, 0, 0.2, 1);

    private final BooleanProperty open = new SimpleBooleanProperty(this, "open", false);
    private final ObjectProperty<Position> position = new SimpleObjectProperty<>(this, "position", Position.LEFT);
    private final BooleanProperty backdrop = new SimpleBooleanProperty(this, "backdrop", true);
    private final BooleanProperty bodyScrolling = new SimpleBooleanProperty(this, "bodyScrolling", false);
    private final BooleanProperty edge = new SimpleBooleanProperty(this, "edge", false);
    private final DoubleProperty edgeSize = new SimpleDoubleProperty(this, "edgeSize", DEFAULT_EDGE_SIZE);
    private final DoubleProperty panelWidth = new SimpleDoubleProperty(this, "panelWidth", DEFAULT_PANEL_WIDTH);
    private final BooleanProperty padded = new SimpleBooleanProperty(this, "padded", true);
    private final BooleanProperty closeOnEscape = new SimpleBooleanProperty(this, "closeOnEscape", true);
    private final ObjectProperty<Duration> transitionDuration =
            new SimpleObjectProperty<>(this, "transitionDuration", DEFAULT_TRANSITION);
    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose");

    private final Region backdropNode = new Region();
    private final VBox content = new VBox();
    private final Panel panel = new Panel(content);

    /** 0 = closed, 1 = open. Animated; everything visual is derived from it. */
    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0);
    private Timeline animation;

    private final EventHandler<ScrollEvent> sceneScrollFilter = this::onSceneScroll;
    private final EventHandler<KeyEvent> sceneKeyFilter = this::onSceneKey;

    public FxDrawer() {
        getStyleClass().add("fxk-drawer");
        setPickOnBounds(false); // only the backdrop and the panel are hit, never the empty layer
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        backdropNode.getStyleClass().add("fxk-drawer-backdrop");
        backdropNode.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        backdropNode.setVisible(false);
        backdropNode.setOpacity(0);
        backdropNode.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                requestClose();
            }
            e.consume();
        });

        content.getStyleClass().add("fxk-drawer-content");
        panel.getStyleClass().add("fxk-drawer-panel");
        panel.setFitToWidth(true);
        panel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panel.setFocusTraversable(false);
        panel.setVisible(false);

        getChildren().addAll(backdropNode, panel);

        EnumStyleClassSync.sync(this, "fxk-drawer-position-", position);
        BooleanStyleClassSync.sync(this, "fxk-drawer-edge", edge);
        BooleanStyleClassSync.sync(content, "fxk-drawer-padded", padded);

        position.addListener((o, was, is) -> layoutPanel());
        panelWidth.addListener((o, was, is) -> layoutPanel());
        edge.addListener((o, was, is) -> {
            updatePanelOffset();
            syncVisibility();
        });
        edgeSize.addListener((o, was, is) -> updatePanelOffset());
        backdrop.addListener((o, was, is) -> syncVisibility());
        panel.layoutBoundsProperty().addListener((o, was, is) -> updatePanelOffset());
        progress.addListener((o, was, is) -> {
            updatePanelOffset();
            backdropNode.setOpacity(is.doubleValue());
            syncVisibility();
        });
        open.addListener((o, was, is) -> animateTo(is));

        sceneProperty().addListener((o, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(ScrollEvent.SCROLL, sceneScrollFilter);
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, sceneKeyFilter);
            }
            if (newScene != null) {
                newScene.addEventFilter(ScrollEvent.SCROLL, sceneScrollFilter);
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, sceneKeyFilter);
            }
        });

        layoutPanel();
    }

    /**
     * @param children the initial children of the drawer, typically an {@link FxDrawerHeader} and an
     *                 {@link FxDrawerItems}
     */
    public FxDrawer(Node... children) {
        this();
        content.getChildren().addAll(children);
    }

    // ---- public API ---------------------------------------------------------------------------

    /**
     * The children of the drawer, stacked top to bottom. This is also FXML's default property.
     *
     * @return the live list of children
     */
    public ObservableList<Node> getContent() {
        return content.getChildren();
    }

    /**
     * Closes the drawer the way the user would: sets {@code open} to {@code false}, then runs
     * {@link #onCloseProperty() onClose}. Does nothing if the drawer is already closed.
     */
    public void requestClose() {
        if (!isOpen()) {
            return;
        }
        setOpen(false);
        Runnable handler = getOnClose();
        if (handler != null) {
            handler.run();
        }
    }

    /** Whether the drawer is open. Flowbite's {@code open}. Changes animate; default {@code false}. */
    public final BooleanProperty openProperty() {
        return open;
    }

    public final boolean isOpen() {
        return open.get();
    }

    public final void setOpen(boolean value) {
        open.set(value);
    }

    /** The edge the drawer is attached to. Flowbite's {@code position}; default {@link Position#LEFT}. */
    public final ObjectProperty<Position> positionProperty() {
        return position;
    }

    public final Position getPosition() {
        return position.get();
    }

    public final void setPosition(Position value) {
        position.set(value == null ? Position.LEFT : value);
    }

    /**
     * Whether an open drawer dims the page and closes on a click outside of it. Flowbite's
     * {@code backdrop}; default {@code true}. With it off the page stays fully clickable.
     */
    public final BooleanProperty backdropProperty() {
        return backdrop;
    }

    public final boolean isBackdrop() {
        return backdrop.get();
    }

    public final void setBackdrop(boolean value) {
        backdrop.set(value);
    }

    /**
     * Whether the mouse wheel still scrolls the page behind an open drawer. Flowbite's
     * {@code bodyScrolling}; default {@code false}.
     */
    public final BooleanProperty bodyScrollingProperty() {
        return bodyScrolling;
    }

    public final boolean isBodyScrolling() {
        return bodyScrolling.get();
    }

    public final void setBodyScrolling(boolean value) {
        bodyScrolling.set(value);
    }

    /**
     * Whether a closed drawer keeps {@link #edgeSizeProperty() edgeSize} pixels visible. Flowbite's
     * {@code edge}; default {@code false}.
     */
    public final BooleanProperty edgeProperty() {
        return edge;
    }

    public final boolean isEdge() {
        return edge.get();
    }

    public final void setEdge(boolean value) {
        edge.set(value);
    }

    /** How much of a closed {@code edge} drawer stays visible; default {@value #DEFAULT_EDGE_SIZE}. */
    public final DoubleProperty edgeSizeProperty() {
        return edgeSize;
    }

    public final double getEdgeSize() {
        return edgeSize.get();
    }

    public final void setEdgeSize(double value) {
        edgeSize.set(value);
    }

    /**
     * Width of a {@link Position#LEFT} / {@link Position#RIGHT} drawer; default
     * {@value #DEFAULT_PANEL_WIDTH}. Top and bottom drawers span the container and are as tall as
     * their content (up to the container's height, then they scroll).
     */
    public final DoubleProperty panelWidthProperty() {
        return panelWidth;
    }

    public final double getPanelWidth() {
        return panelWidth.get();
    }

    public final void setPanelWidth(double value) {
        panelWidth.set(value);
    }

    /**
     * Whether the content has Flowbite's {@code p-4} padding; default {@code true}. Turn it off
     * (Flowbite's {@code className="p-0"}) when the children bring their own padding, as in the
     * swipeable-edge example.
     */
    public final BooleanProperty paddedProperty() {
        return padded;
    }

    public final boolean isPadded() {
        return padded.get();
    }

    public final void setPadded(boolean value) {
        padded.set(value);
    }

    /** Whether the Escape key is a close request while open; default {@code true}. */
    public final BooleanProperty closeOnEscapeProperty() {
        return closeOnEscape;
    }

    public final boolean isCloseOnEscape() {
        return closeOnEscape.get();
    }

    public final void setCloseOnEscape(boolean value) {
        closeOnEscape.set(value);
    }

    /**
     * Length of the slide animation; default 150ms, Tailwind's {@code transition-transform}.
     * {@code null} or zero switches the animation off.
     */
    public final ObjectProperty<Duration> transitionDurationProperty() {
        return transitionDuration;
    }

    public final Duration getTransitionDuration() {
        return transitionDuration.get();
    }

    public final void setTransitionDuration(Duration value) {
        transitionDuration.set(value);
    }

    /** Runs after every close request, once {@code open} is {@code false}. Flowbite's {@code onClose}. */
    public final ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public final Runnable getOnClose() {
        return onClose.get();
    }

    public final void setOnClose(Runnable value) {
        onClose.set(value);
    }

    // ---- layout ---------------------------------------------------------------------------------

    private boolean isVerticalEdge() {
        Position p = getPosition();
        return p == Position.LEFT || p == Position.RIGHT;
    }

    /** Sizes and aligns the panel for the current position. */
    private void layoutPanel() {
        if (isVerticalEdge()) {
            // Flowbite: h-screen w-80
            double w = getPanelWidth();
            panel.setMinWidth(w);
            panel.setPrefWidth(w);
            panel.setMaxWidth(w);
            panel.setMinHeight(Region.USE_COMPUTED_SIZE);
            panel.setPrefHeight(Region.USE_COMPUTED_SIZE);
            panel.setMaxHeight(Double.MAX_VALUE);
        } else {
            // Flowbite: w-full, as tall as its content
            panel.setMinWidth(Region.USE_COMPUTED_SIZE);
            panel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            panel.setMaxWidth(Double.MAX_VALUE);
            panel.setMinHeight(Region.USE_COMPUTED_SIZE);
            panel.setPrefHeight(Region.USE_COMPUTED_SIZE);
            panel.setMaxHeight(Region.USE_PREF_SIZE);
        }
        StackPane.setAlignment(panel, switch (getPosition()) {
            case LEFT -> Pos.CENTER_LEFT;
            case RIGHT -> Pos.CENTER_RIGHT;
            case TOP -> Pos.TOP_CENTER;
            case BOTTOM -> Pos.BOTTOM_CENTER;
        });
        updatePanelOffset();
    }

    /**
     * Slides the panel: fully open at progress 1, and at progress 0 moved off its edge by its own size
     * (Flowbite's {@code translate-x-full} and friends), minus {@code edgeSize} when {@code edge} is on.
     */
    private void updatePanelOffset() {
        double size = isVerticalEdge() ? panel.getLayoutBounds().getWidth() : panel.getLayoutBounds().getHeight();
        double hidden = Math.max(0, size - (isEdge() ? getEdgeSize() : 0));
        double offset = (1 - progress.get()) * hidden;
        switch (getPosition()) {
            case LEFT -> {
                panel.setTranslateX(-offset);
                panel.setTranslateY(0);
            }
            case RIGHT -> {
                panel.setTranslateX(offset);
                panel.setTranslateY(0);
            }
            case TOP -> {
                panel.setTranslateX(0);
                panel.setTranslateY(-offset);
            }
            case BOTTOM -> {
                panel.setTranslateX(0);
                panel.setTranslateY(offset);
            }
        }
    }

    /** A closed drawer is invisible (so it cannot take focus) unless {@code edge} keeps its strip showing. */
    private void syncVisibility() {
        boolean engaged = isOpen() || progress.get() > 0;
        panel.setVisible(engaged || isEdge());
        backdropNode.setVisible(isBackdrop() && progress.get() > 0);
    }

    private void animateTo(boolean opening) {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
        double target = opening ? 1 : 0;
        Duration duration = getTransitionDuration();
        if (getScene() == null || duration == null || duration.toMillis() <= 0) {
            progress.set(target);
            syncVisibility();
            return;
        }
        syncVisibility(); // so an opening panel is visible from its first frame
        animation = new Timeline(new KeyFrame(duration, new KeyValue(progress, target, EASE)));
        animation.setOnFinished(e -> {
            animation = null;
            syncVisibility();
        });
        animation.play();
    }

    // ---- scene-level input ----------------------------------------------------------------------

    private void onSceneKey(KeyEvent event) {
        if (isOpen() && isCloseOnEscape() && event.getCode() == KeyCode.ESCAPE) {
            event.consume();
            requestClose();
        }
    }

    /**
     * Implements {@code bodyScrolling}. Wheel events over the panel are left alone. Elsewhere, with
     * body scrolling off they are swallowed (this also covers {@code backdrop = false}, where nothing
     * of the drawer sits under the pointer). With it on, the backdrop would swallow them by being the
     * target, so they are re-sent to the page node underneath instead.
     */
    private void onSceneScroll(ScrollEvent event) {
        if (!isOpen() || isWithin(panel, event.getTarget())) {
            return;
        }
        if (!isBodyScrolling()) {
            event.consume();
        } else if (event.getTarget() == backdropNode) {
            Node under = nodeBeneath(event.getSceneX(), event.getSceneY());
            ScrollEvent forwarded = under == null ? null : event.copyFor(event.getSource(), under);
            event.consume();
            if (forwarded != null) {
                Event.fireEvent(under, forwarded);
            }
        }
    }

    /** The deepest pickable node at the given scene point among the layer's siblings drawn below it. */
    private Node nodeBeneath(double sceneX, double sceneY) {
        Parent parent = getParent();
        if (parent == null) {
            return null;
        }
        var siblings = parent.getChildrenUnmodifiable();
        for (int i = siblings.indexOf(this) - 1; i >= 0; i--) {
            Node hit = deepestAt(siblings.get(i), sceneX, sceneY);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    private static Node deepestAt(Node node, double sceneX, double sceneY) {
        if (!node.isVisible() || node.isMouseTransparent()) {
            return null;
        }
        Point2D local = node.sceneToLocal(sceneX, sceneY);
        if (local == null || !node.getBoundsInLocal().contains(local)) {
            return null;
        }
        if (node instanceof Parent parent) {
            var children = parent.getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node hit = deepestAt(children.get(i), sceneX, sceneY);
                if (hit != null) {
                    return hit;
                }
            }
        }
        return node.isPickOnBounds() ? node : null;
    }

    // ---- helpers shared with FxDrawerHeader -------------------------------------------------------

    /** @return whether {@code target} is {@code ancestor} or one of its descendants */
    static boolean isWithin(Node ancestor, Object target) {
        for (Node n = target instanceof Node node ? node : null; n != null; n = n.getParent()) {
            if (n == ancestor) {
                return true;
            }
        }
        return false;
    }

    /** @return the nearest {@code FxDrawer} above {@code node}, or {@code null} */
    static FxDrawer findDrawer(Node node) {
        for (Node n = node.getParent(); n != null; n = n.getParent()) {
            if (n instanceof FxDrawer drawer) {
                return drawer;
            }
        }
        return null;
    }

    /**
     * The scrolling surface. Overridden so a top/bottom drawer is as tall as its content: the stock
     * ScrollPane does not report a content-based preferred height for a given width.
     */
    private static final class Panel extends ScrollPane {

        Panel(Node content) {
            super(content);
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected double computePrefHeight(double width) {
            Node content = getContent();
            if (content == null) {
                return super.computePrefHeight(width);
            }
            double inner = width < 0 ? -1 : Math.max(0, width - snappedLeftInset() - snappedRightInset());
            return snappedTopInset() + content.prefHeight(inner) + snappedBottomInset();
        }
    }
}
