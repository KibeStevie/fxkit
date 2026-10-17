package dev.fxkit.core.components.popover;

import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Flowbite React's {@code <Popover>}: wraps a trigger node (the {@link #anchorProperty() anchor}) and
 * shows any node (the {@link #contentProperty() content}) in a floating, bordered panel next to it.
 *
 * <p>{@code FxPopover} is a thin {@link StackPane} around the anchor, so it lays out exactly like the
 * anchor would (put it in an {@code HBox}, a {@code TextFlow}, a form, ...). The panel itself is a
 * {@link Popup}, i.e. a separate window; FxPopover copies the owner scene's stylesheets and the
 * {@code dark} root class onto it (and keeps them in sync while it is open), so the tokens in
 * {@code components.css} resolve exactly as in the main scene.
 *
 * <h2>Trigger</h2>
 * {@link Trigger#CLICK} (Flowbite's default) toggles the popover when the anchor is clicked (or, for a
 * {@link ButtonBase} anchor, fired - which also covers Space/Enter). {@link Trigger#HOVER} opens on
 * mouse enter and closes shortly after the mouse leaves both the anchor and the popover, so the
 * pointer can travel into the panel and use what is inside. For both triggers a press outside the
 * anchor, or {@code Esc}, closes the popover.
 *
 * <h2>Placement</h2>
 * {@link Placement} is the side of the anchor the panel appears on ({@code BOTTOM} by default). If the
 * panel would leave the screen on that side it flips to the opposite one, and it is shifted along the
 * edge to stay on screen; the arrow keeps pointing at the anchor's center either way. The panel is
 * re-positioned when the anchor moves, resizes or the owner window moves.
 *
 * <h2>Controlled use</h2>
 * {@link #openProperty()} is Flowbite's {@code open}/{@code onOpenChange} pair in one property: bind
 * to it, listen to it, or call {@link #show()}, {@link #hide()} and {@link #toggle()}. User
 * interaction (trigger, outside press, {@code Esc}) writes to it too. Setting it to {@code true}
 * before the window is showing is allowed; the popover opens as soon as it can.
 *
 * <h2>Content</h2>
 * The content is any node; give it the size you want (Flowbite's {@code w-64} is
 * {@code setPrefWidth(256)}). {@link FxPopoverPanel} is a ready-made title + body layout for the common
 * case. The panel clips its content to a rounded rectangle (Flowbite's {@code overflow-hidden
 * rounded-[7px]}).
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxPopover popover = new FxPopover(
 *         new FxButton("Default popover"),
 *         new FxPopoverPanel("Popover title", "And here's some amazing content."));
 * popover.setPlacement(FxPopover.Placement.RIGHT);
 * popover.setTrigger(FxPopover.Trigger.HOVER);
 * popover.setArrow(false);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxPopover placement="TOP" trigger="CLICK">
 *     <FxButton text="Popover top"/>
 *     <content>
 *         <FxPopoverPanel title="Popover title" text="And here's some amazing content."/>
 *     </content>
 * </FxPopover>
 * }</pre>
 *
 * <p>Like all scene-graph access, use it on the JavaFX Application Thread.
 */
@DefaultProperty("anchor")
public class FxPopover extends StackPane {

    public static final String STYLE_CLASS = "fxk-popover-anchor";

    static final String POPUP_STYLE_CLASS = "fxk-popover-popup";
    static final String BOX_STYLE_CLASS = "fxk-popover";
    static final String CONTENT_STYLE_CLASS = "fxk-popover-content";
    static final String ARROW_FILL_STYLE_CLASS = "fxk-popover-arrow-fill";
    static final String ARROW_LINE_STYLE_CLASS = "fxk-popover-arrow-line";

    public static final Placement DEFAULT_PLACEMENT = Placement.BOTTOM;
    public static final Trigger DEFAULT_TRIGGER = Trigger.CLICK;

    /** Distance between the anchor and the panel's edge (floating-ui {@code offset(8)}). */
    private static final double GAP = 8;

    /** The arrow is an 8px square rotated 45 degrees, so its tip is 4 * sqrt(2) from the edge. */
    private static final double ARROW_TIP = 4 * Math.sqrt(2);

    /** Keeps the arrow clear of the panel's rounded corners. */
    private static final double ARROW_EDGE_INSET = 12;

    /** 8px panel radius minus its 1px border. */
    private static final double CONTENT_RADIUS = 7;

    private static final Duration HOVER_CLOSE_DELAY = Duration.millis(150);

    /** The side of the anchor the popover appears on (Flowbite's {@code placement}). */
    public enum Placement {
        TOP, RIGHT, BOTTOM, LEFT;

        Placement opposite() {
            return switch (this) {
                case TOP -> BOTTOM;
                case BOTTOM -> TOP;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }

        boolean vertical() {
            return this == TOP || this == BOTTOM;
        }
    }

    /** What opens the popover (Flowbite's {@code trigger}). */
    public enum Trigger {
        HOVER, CLICK
    }

    private record Position(double x, double y, Placement placement, double arrowCenter) {
    }

    private final ObjectProperty<Node> anchor = new SimpleObjectProperty<>(this, "anchor");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<Placement> placement = new SimpleObjectProperty<>(this, "placement",
            DEFAULT_PLACEMENT);
    private final ObjectProperty<Trigger> trigger = new SimpleObjectProperty<>(this, "trigger", DEFAULT_TRIGGER);
    private final BooleanProperty arrow = new SimpleBooleanProperty(this, "arrow", true);
    private final BooleanProperty open = new SimpleBooleanProperty(this, "open", false);

    private Popup popup;
    private PopoverRoot root;

    /** True while this class itself writes {@code open}, so its own writes don't re-enter the listener. */
    private boolean updatingOpen;
    private boolean repositionQueued;

    private Scene watchedScene;
    private Window watchedWindow;
    private Scene attachedScene;
    private Window attachedWindow;

    private final PauseTransition closeTimer = new PauseTransition(HOVER_CLOSE_DELAY);

    // ---- listeners (fields so they can be removed again) ---------------------------------------

    private final ChangeListener<Window> windowListener = (obs, oldWindow, newWindow) -> watchWindow(newWindow);

    private final ChangeListener<Boolean> showingListener = (obs, wasShowing, showing) -> {
        if (showing) {
            if (isOpen()) {
                show();
            }
        } else {
            hidePopup();
        }
    };

    /**
     * A ChangeListener on purpose: an InvalidationListener on a lazily validated property (window x/y,
     * the anchor's transform) is only told about the first invalidation until someone reads the value.
     */
    private final ChangeListener<Object> repositionListener = (obs, oldValue, newValue) -> queueReposition();

    private final InvalidationListener themeListener = obs -> {
        if (attachedScene != null && popup != null) {
            syncTheme(attachedScene);
            queueReposition();
        }
    };

    private final EventHandler<MouseEvent> outsidePressFilter = event -> {
        if (!isInsideAnchor(event.getTarget())) {
            setOpenInternal(false);
        }
    };

    private final EventHandler<KeyEvent> escapeFilter = event -> {
        if (event.getCode() == KeyCode.ESCAPE && isOpen()) {
            setOpenInternal(false);
            Node anchorNode = getAnchor();
            if (anchorNode != null && anchorNode.isFocusTraversable()) {
                anchorNode.requestFocus();
            }
            event.consume();
        }
    };

    private final EventHandler<MouseEvent> clickFilter = event -> {
        // A ButtonBase anchor is handled through its ActionEvent instead (covers keyboard too).
        if (getTrigger() == Trigger.CLICK && !(getAnchor() instanceof ButtonBase)
                && event.getButton() == MouseButton.PRIMARY && !isAnchorDisabled()) {
            toggle();
        }
    };

    private final EventHandler<ActionEvent> actionHandler = event -> {
        if (getTrigger() == Trigger.CLICK && event.getTarget() == getAnchor() && !isAnchorDisabled()) {
            toggle();
        }
    };

    private final EventHandler<MouseEvent> anchorEnteredHandler = event -> {
        if (getTrigger() == Trigger.HOVER && !isAnchorDisabled()) {
            closeTimer.stop();
            setOpenInternal(true);
        }
    };

    private final EventHandler<MouseEvent> anchorExitedHandler = event -> {
        if (getTrigger() == Trigger.HOVER) {
            closeTimer.playFromStart();
        }
    };

    // ---- construction --------------------------------------------------------------------------

    public FxPopover() {
        initialize();
    }

    /**
     * @param anchor  the trigger node the popover is attached to
     * @param content what the popover shows
     */
    public FxPopover(Node anchor, Node content) {
        initialize();
        setAnchor(anchor);
        setContent(content);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);

        closeTimer.setOnFinished(event -> {
            boolean pointerInside = root != null && root.isHover();
            if (!pointerInside && getTrigger() == Trigger.HOVER) {
                setOpenInternal(false);
            }
        });

        anchor.addListener((obs, oldAnchor, newAnchor) -> {
            uninstallTriggers(oldAnchor);
            if (newAnchor == null) {
                getChildren().clear();
            } else {
                getChildren().setAll(newAnchor);
            }
            installTriggers(newAnchor);
            if (popup != null && popup.isShowing()) {
                queueReposition();
            }
        });

        content.addListener((obs, oldContent, newContent) -> {
            if (popup != null && popup.isShowing()) {
                if (newContent == null) {
                    setOpenInternal(false);
                } else {
                    root.setContent(newContent);
                    queueReposition();
                }
            }
        });

        placement.addListener((obs, oldValue, newValue) -> queueReposition());
        arrow.addListener((obs, oldValue, newValue) -> {
            if (root != null) {
                root.setArrowVisible(newValue);
            }
            queueReposition();
        });

        open.addListener((obs, wasOpen, isOpen) -> {
            if (updatingOpen) {
                return;
            }
            closeTimer.stop();
            if (isOpen) {
                show();
            } else {
                hidePopup();
            }
        });

        sceneProperty().addListener((obs, oldScene, newScene) -> watchScene(newScene));
    }

    // ---- public API ----------------------------------------------------------------------------

    /** Opens the popover (same as {@code setOpen(true)}). */
    public void show() {
        if (!isOpen()) {
            setOpen(true);
            return;
        }
        showPopup();
    }

    /** Closes the popover (same as {@code setOpen(false)}). */
    public void hide() {
        setOpen(false);
    }

    /** Opens the popover if it is closed, closes it if it is open. */
    public void toggle() {
        setOpen(!isOpen());
    }

    // ---- trigger wiring ------------------------------------------------------------------------

    private void installTriggers(Node node) {
        if (node == null) {
            return;
        }
        node.addEventFilter(MouseEvent.MOUSE_CLICKED, clickFilter);
        node.addEventHandler(ActionEvent.ACTION, actionHandler);
        node.addEventHandler(MouseEvent.MOUSE_ENTERED, anchorEnteredHandler);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, anchorExitedHandler);
    }

    private void uninstallTriggers(Node node) {
        if (node == null) {
            return;
        }
        node.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickFilter);
        node.removeEventHandler(ActionEvent.ACTION, actionHandler);
        node.removeEventHandler(MouseEvent.MOUSE_ENTERED, anchorEnteredHandler);
        node.removeEventHandler(MouseEvent.MOUSE_EXITED, anchorExitedHandler);
    }

    private boolean isAnchorDisabled() {
        Node node = getAnchor();
        return node == null || node.isDisabled();
    }

    private boolean isInsideAnchor(EventTarget target) {
        Node anchorNode = getAnchor();
        if (anchorNode == null || !(target instanceof Node node)) {
            return false;
        }
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == anchorNode) {
                return true;
            }
        }
        return false;
    }

    /** Writes {@code open} from inside this class and applies it, without the listener re-entering. */
    private void setOpenInternal(boolean value) {
        if (isOpen() == value) {
            return;
        }
        closeTimer.stop();
        updatingOpen = true;
        try {
            open.set(value);
        } finally {
            updatingOpen = false;
        }
        if (value) {
            showPopup();
        } else {
            hidePopup();
        }
    }

    // ---- scene / window tracking ---------------------------------------------------------------

    private void watchScene(Scene scene) {
        if (watchedScene != null) {
            watchedScene.windowProperty().removeListener(windowListener);
        }
        watchedScene = scene;
        if (scene == null) {
            watchWindow(null);
            // Removed from the scene graph: nothing to point at any more.
            if (isOpen()) {
                setOpenInternal(false);
            }
            return;
        }
        scene.windowProperty().addListener(windowListener);
        watchWindow(scene.getWindow());
    }

    private void watchWindow(Window window) {
        if (watchedWindow != null) {
            watchedWindow.showingProperty().removeListener(showingListener);
        }
        watchedWindow = window;
        if (window != null) {
            window.showingProperty().addListener(showingListener);
            if (window.isShowing() && isOpen()) {
                showPopup();
            }
        }
    }

    // ---- popup lifecycle -----------------------------------------------------------------------

    private void ensurePopup() {
        if (popup != null) {
            return;
        }
        popup = new Popup();
        popup.setAutoFix(false);
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);

        root = new PopoverRoot();
        popup.getContent().add(root);

        // Esc while the popup window itself has the focus (e.g. after clicking into a text field).
        popup.getScene().addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);

        // Hover: the pointer may leave the anchor and enter the panel without closing it.
        root.hoverProperty().addListener((obs, wasHover, hover) -> {
            if (hover) {
                closeTimer.stop();
            } else if (getTrigger() == Trigger.HOVER) {
                closeTimer.playFromStart();
            }
        });

        // The panel grows or shrinks with its content; keep it glued to the anchor.
        root.layoutBoundsProperty().addListener(repositionListener);

        popup.setOnHidden(event -> {
            detach();
            if (isOpen()) {
                updatingOpen = true;
                try {
                    open.set(false);
                } finally {
                    updatingOpen = false;
                }
            }
        });
    }

    private void showPopup() {
        Node anchorNode = getAnchor();
        Node contentNode = getContent();
        Scene scene = getScene();
        if (anchorNode == null || contentNode == null || scene == null || scene.getWindow() == null
                || !scene.getWindow().isShowing()) {
            return; // stays "open" and is retried once the window is showing (see watchWindow)
        }

        ensurePopup();
        syncTheme(scene);
        root.setContent(contentNode);
        root.setArrowVisible(isArrow());
        root.setMaxWidth(visualBoundsFor(screenBounds(anchorNode)).getWidth());
        root.applyCss();

        Position position = computePosition();
        if (position == null) {
            return;
        }
        root.update(position.placement(), position.arrowCenter());

        if (popup.isShowing()) {
            popup.setX(position.x());
            popup.setY(position.y());
        } else {
            attach(scene);
            popup.show(scene.getWindow(), position.x(), position.y());
        }
    }

    private void hidePopup() {
        closeTimer.stop();
        if (popup != null && popup.isShowing()) {
            popup.hide(); // onHidden detaches and syncs `open`
        } else {
            detach();
        }
    }

    private void attach(Scene scene) {
        if (attachedScene != null) {
            return;
        }
        attachedScene = scene;
        attachedWindow = scene.getWindow();

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressFilter);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);

        scene.getRoot().getStyleClass().addListener(themeListener);
        scene.getStylesheets().addListener(themeListener);

        Node anchorNode = getAnchor();
        if (anchorNode != null) {
            anchorNode.localToSceneTransformProperty().addListener(repositionListener);
            anchorNode.layoutBoundsProperty().addListener(repositionListener);
        }
        attachedWindow.xProperty().addListener(repositionListener);
        attachedWindow.yProperty().addListener(repositionListener);
    }

    private void detach() {
        if (attachedScene == null) {
            return;
        }
        attachedScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressFilter);
        attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
        attachedScene.getRoot().getStyleClass().removeListener(themeListener);
        attachedScene.getStylesheets().removeListener(themeListener);

        Node anchorNode = getAnchor();
        if (anchorNode != null) {
            anchorNode.localToSceneTransformProperty().removeListener(repositionListener);
            anchorNode.layoutBoundsProperty().removeListener(repositionListener);
        }
        if (attachedWindow != null) {
            attachedWindow.xProperty().removeListener(repositionListener);
            attachedWindow.yProperty().removeListener(repositionListener);
        }
        attachedScene = null;
        attachedWindow = null;
    }

    /** Copies what makes tokens resolve in the popup's own scene: stylesheets and the dark class. */
    private void syncTheme(Scene owner) {
        popup.getScene().getStylesheets().setAll(owner.getStylesheets());
        var rootClasses = popup.getScene().getRoot().getStyleClass();
        rootClasses.removeIf(ThemeManager.DARK_CLASS::equals);
        if (ThemeManager.current(owner) == Theme.DARK) {
            rootClasses.add(ThemeManager.DARK_CLASS);
        }
    }

    // ---- positioning ---------------------------------------------------------------------------

    private void queueReposition() {
        if (repositionQueued || popup == null || !popup.isShowing()) {
            return;
        }
        repositionQueued = true;
        Platform.runLater(() -> {
            repositionQueued = false;
            if (popup == null || !popup.isShowing()) {
                return;
            }
            root.setArrowVisible(isArrow());
            Position position = computePosition();
            if (position == null) {
                return;
            }
            root.update(position.placement(), position.arrowCenter());
            popup.setX(position.x());
            popup.setY(position.y());
        });
    }

    private static Bounds screenBounds(Node node) {
        node.getLocalToSceneTransform(); // revalidates the cached transform before localToScreen
        return node.localToScreen(node.getBoundsInLocal());
    }

    private static Rectangle2D visualBoundsFor(Bounds anchorBounds) {
        if (anchorBounds == null) {
            return Screen.getPrimary().getVisualBounds();
        }
        var screens = Screen.getScreensForRectangle(anchorBounds.getMinX(), anchorBounds.getMinY(),
                Math.max(1, anchorBounds.getWidth()), Math.max(1, anchorBounds.getHeight()));
        return (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
    }

    private Position computePosition() {
        Node anchorNode = getAnchor();
        if (anchorNode == null || anchorNode.getScene() == null) {
            return null;
        }
        Bounds a = screenBounds(anchorNode);
        if (a == null) {
            return null;
        }
        Rectangle2D screen = visualBoundsFor(a);

        Insets pad = root.getInsets();
        double w = Math.min(root.prefWidth(-1), root.getMaxWidth());
        double h = root.prefHeight(w);
        double boxW = w - pad.getLeft() - pad.getRight();
        double boxH = h - pad.getTop() - pad.getBottom();

        Placement p = resolvePlacement(getPlacement() == null ? DEFAULT_PLACEMENT : getPlacement(),
                a, boxW, boxH, screen);

        double x;
        double y;
        switch (p) {
            case BOTTOM -> {
                x = a.getCenterX() - w / 2;
                y = a.getMaxY() + GAP - pad.getTop();
            }
            case TOP -> {
                x = a.getCenterX() - w / 2;
                y = a.getMinY() - GAP - boxH - pad.getTop();
            }
            case RIGHT -> {
                x = a.getMaxX() + GAP - pad.getLeft();
                y = a.getCenterY() - h / 2;
            }
            default -> { // LEFT
                x = a.getMinX() - GAP - boxW - pad.getLeft();
                y = a.getCenterY() - h / 2;
            }
        }

        // Shift along the edge to stay on screen (floating-ui shift()).
        x = Math.max(screen.getMinX(), Math.min(x, screen.getMaxX() - w));
        y = Math.max(screen.getMinY(), Math.min(y, screen.getMaxY() - h));

        double arrowCenter = p.vertical() ? a.getCenterX() - x : a.getCenterY() - y;
        return new Position(x, y, p, arrowCenter);
    }

    /** Keeps the wanted side if the panel fits there, else flips if the opposite side fits or is roomier. */
    private static Placement resolvePlacement(Placement wanted, Bounds a, double boxW, double boxH,
            Rectangle2D screen) {
        double need = GAP + (wanted.vertical() ? boxH : boxW);
        if (room(wanted, a, screen) >= need) {
            return wanted;
        }
        Placement flipped = wanted.opposite();
        if (room(flipped, a, screen) >= need) {
            return flipped;
        }
        return room(wanted, a, screen) >= room(flipped, a, screen) ? wanted : flipped;
    }

    private static double room(Placement p, Bounds a, Rectangle2D screen) {
        return switch (p) {
            case BOTTOM -> screen.getMaxY() - a.getMaxY();
            case TOP -> a.getMinY() - screen.getMinY();
            case RIGHT -> screen.getMaxX() - a.getMaxX();
            case LEFT -> a.getMinX() - screen.getMinX();
        };
    }

    // ---- the popup's scene graph ---------------------------------------------------------------

    /**
     * Popup root: transparent padding (room for the shadow and the arrow tip) around the panel box,
     * with the arrow drawn on top of the box on whichever edge faces the anchor.
     *
     * <p>The arrow is two shapes in one group: a filled triangle that also covers the box's own 1px
     * border where the arrow joins it (so the join is seamless), and a stroked two-sided outline. Both
     * are modelled pointing up and rotated about the box edge.
     */
    private static final class PopoverRoot extends StackPane {

        private final StackPane box = new StackPane();
        private final StackPane contentHolder = new StackPane();
        private final Group arrowGroup = new Group();
        private final Rotate arrowRotation = new Rotate(0, 0, 0);

        private Placement placement = DEFAULT_PLACEMENT;
        private double arrowCenter;
        private boolean arrowVisible = true;

        PopoverRoot() {
            getStyleClass().add(POPUP_STYLE_CLASS);
            box.getStyleClass().add(BOX_STYLE_CLASS);
            contentHolder.getStyleClass().add(CONTENT_STYLE_CLASS);

            Rectangle clip = new Rectangle();
            clip.setArcWidth(CONTENT_RADIUS * 2);
            clip.setArcHeight(CONTENT_RADIUS * 2);
            clip.widthProperty().bind(contentHolder.widthProperty());
            clip.heightProperty().bind(contentHolder.heightProperty());
            contentHolder.setClip(clip);
            box.getChildren().add(contentHolder);

            // 1px beyond the base on each side so the fill hides the box border under the arrow.
            double base = ARROW_TIP + 1;
            Polygon fill = new Polygon(0, -ARROW_TIP, base, 1, -base, 1);
            fill.getStyleClass().add(ARROW_FILL_STYLE_CLASS);
            Polyline line = new Polyline(-base, 1, 0, -ARROW_TIP, base, 1);
            line.setFill(null);
            line.getStyleClass().add(ARROW_LINE_STYLE_CLASS);
            arrowGroup.getChildren().addAll(fill, line);
            arrowGroup.getTransforms().add(arrowRotation);
            arrowGroup.setManaged(false);
            arrowGroup.setMouseTransparent(true);

            getChildren().addAll(box, arrowGroup);
        }

        void setContent(Node node) {
            if (contentHolder.getChildren().size() != 1 || contentHolder.getChildren().get(0) != node) {
                contentHolder.getChildren().setAll(node);
            }
        }

        void setArrowVisible(boolean visible) {
            if (arrowVisible != visible) {
                arrowVisible = visible;
                requestLayout();
            }
        }

        void update(Placement newPlacement, double newArrowCenter) {
            if (placement != newPlacement || arrowCenter != newArrowCenter) {
                placement = newPlacement;
                arrowCenter = newArrowCenter;
                requestLayout();
            }
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            arrowGroup.setVisible(arrowVisible);
            if (!arrowVisible) {
                return;
            }
            double bx = box.getLayoutX();
            double by = box.getLayoutY();
            double bw = box.getWidth();
            double bh = box.getHeight();
            double x;
            double y;
            double angle;
            switch (placement) {
                case BOTTOM -> { // panel below the anchor: arrow on the top edge, pointing up
                    x = clamp(arrowCenter, bx + ARROW_EDGE_INSET, bx + bw - ARROW_EDGE_INSET);
                    y = by;
                    angle = 0;
                }
                case TOP -> {
                    x = clamp(arrowCenter, bx + ARROW_EDGE_INSET, bx + bw - ARROW_EDGE_INSET);
                    y = by + bh;
                    angle = 180;
                }
                case RIGHT -> { // panel right of the anchor: arrow on the left edge, pointing left
                    x = bx;
                    y = clamp(arrowCenter, by + ARROW_EDGE_INSET, by + bh - ARROW_EDGE_INSET);
                    angle = -90;
                }
                default -> { // LEFT
                    x = bx + bw;
                    y = clamp(arrowCenter, by + ARROW_EDGE_INSET, by + bh - ARROW_EDGE_INSET);
                    angle = 90;
                }
            }
            arrowRotation.setAngle(angle);
            arrowGroup.setLayoutX(x);
            arrowGroup.setLayoutY(y);
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(value, max));
        }
    }

    // ---- properties ----------------------------------------------------------------------------

    public Node getAnchor() {
        return anchor.get();
    }

    /** The trigger node this popover wraps (the one child of this pane). */
    public void setAnchor(Node anchor) {
        this.anchor.set(anchor);
    }

    public ObjectProperty<Node> anchorProperty() {
        return anchor;
    }

    public Node getContent() {
        return content.get();
    }

    /** The node shown inside the popover (Flowbite's {@code content}). */
    public void setContent(Node content) {
        this.content.set(content);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Placement getPlacement() {
        return placement.get();
    }

    public void setPlacement(Placement placement) {
        this.placement.set(placement);
    }

    public ObjectProperty<Placement> placementProperty() {
        return placement;
    }

    public Trigger getTrigger() {
        return trigger.get();
    }

    public void setTrigger(Trigger trigger) {
        this.trigger.set(trigger);
    }

    public ObjectProperty<Trigger> triggerProperty() {
        return trigger;
    }

    /** @return whether the popover shows an arrow pointing at the anchor (Flowbite's {@code arrow}) */
    public boolean isArrow() {
        return arrow.get();
    }

    public void setArrow(boolean arrow) {
        this.arrow.set(arrow);
    }

    public BooleanProperty arrowProperty() {
        return arrow;
    }

    /** @return whether the popover is open (Flowbite's {@code open}) */
    public boolean isOpen() {
        return open.get();
    }

    public void setOpen(boolean open) {
        this.open.set(open);
    }

    public BooleanProperty openProperty() {
        return open;
    }
}
