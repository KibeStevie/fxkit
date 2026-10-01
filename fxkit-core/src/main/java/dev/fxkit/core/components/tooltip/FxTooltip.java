package dev.fxkit.core.components.tooltip;

import java.util.List;

import dev.fxkit.core.internal.EnumStyleClassSync;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * Flowbite React's {@code <Tooltip>}: wraps a <em>target</em> node and shows a small floating label
 * next to it when the target is hovered (or clicked).
 *
 * <p>
 * {@code FxTooltip} is a {@link StackPane} that hugs its target (Flowbite's {@code w-fit}), so it can
 * be dropped anywhere the target would have gone. The floating label is a {@link Popup}, i.e. a
 * separate window; like {@code FxDropdown}, it copies the owner scene's stylesheets and the
 * {@code dark} root class onto itself each time it opens, so the tokens resolve exactly as they do in
 * the main scene (apply your theme with {@link ThemeManager#apply} first).
 *
 * <h2>Variant</h2>
 * {@link #variantProperty()} is Flowbite's {@code style}: {@link Variant#LIGHT} (white, bordered),
 * {@link Variant#DARK} (dark gray, no border) and {@link Variant#AUTO} (light in the light theme, dark
 * in the dark theme). Named {@code variant} rather than {@code style} because {@code Node.style} is
 * already JavaFX's inline-CSS property.
 *
 * <h2>Placement, trigger, arrow, animation</h2>
 * <ul>
 * <li>{@link #placementProperty()}: {@code TOP} (default), {@code RIGHT}, {@code BOTTOM},
 * {@code LEFT}. If the tooltip would leave the screen it flips to the opposite side and slides along
 * the edge; the arrow keeps pointing at the target.</li>
 * <li>{@link #triggerProperty()}: {@code HOVER} (default) or {@code CLICK} (click toggles; a click
 * elsewhere or Escape closes it).</li>
 * <li>{@link #arrowProperty()}: {@code false} removes the arrow.</li>
 * <li>{@link #animationProperty()}: fade duration; {@link Animation#NONE} disables it.</li>
 * </ul>
 *
 * <h2>Java</h2>
 *
 * <pre>{@code
 * FxTooltip tip = new FxTooltip("Tooltip content", new FxButton("Default tooltip"));
 *
 * FxTooltip dark = new FxTooltip("Tooltip content", new FxButton("Dark tooltip"));
 * dark.setVariant(FxTooltip.Variant.DARK);
 * dark.setPlacement(FxTooltip.Placement.RIGHT);
 *
 * FxTooltip click = new FxTooltip("Tooltip content", new FxButton("Tooltip click"));
 * click.setTrigger(FxTooltip.Trigger.CLICK);
 * click.setArrow(false);
 * click.setAnimation(FxTooltip.Animation.DURATION_500);
 * }</pre>
 *
 * <h2>FXML</h2>
 *
 * <pre>{@code
 * <FxTooltip content="Tooltip content" variant="DARK" placement="BOTTOM">
 *     <FxButton text="Dark tooltip"/>
 * </FxTooltip>
 * }</pre>
 */
@DefaultProperty("target")
public class FxTooltip extends StackPane {

    public static final String STYLE_CLASS = "fxk-tooltip-target";

    private static final String POPUP_STYLE_CLASS = "fxk-tooltip-popup";
    private static final String BOX_STYLE_CLASS = "fxk-tooltip";
    private static final String CONTENT_STYLE_CLASS = "fxk-tooltip-content";
    private static final String ARROW_STYLE_CLASS = "fxk-tooltip-arrow";
    private static final String VARIANT_STYLE_CLASS_PREFIX = "fxk-tooltip-";

    public static final Variant DEFAULT_VARIANT = Variant.LIGHT;
    public static final Placement DEFAULT_PLACEMENT = Placement.TOP;
    public static final Trigger DEFAULT_TRIGGER = Trigger.HOVER;
    public static final Animation DEFAULT_ANIMATION = Animation.DURATION_300;

    /** Flowbite's floating-ui {@code offset(8)}: gap between the target and the tooltip box. */
    private static final double GAP = 8;

    /** Flowbite's arrow: {@code h-2 w-2} (8px) square, rotated 45 degrees, centered on the box edge. */
    private static final double ARROW_SIZE = 8;

    /** Keeps the arrow clear of the box's rounded corners when it has to slide off-center. */
    private static final double ARROW_EDGE_INSET = ARROW_SIZE / 2 + 8;

    /** Long text wraps at this width (Flowbite has no limit; an unbounded popup is rarely wanted). */
    private static final double MAX_CONTENT_WIDTH = 320;

    /** Where the tooltip sits relative to its target. */
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
    }

    /** Flowbite's {@code style}. */
    public enum Variant {
        /** White box with a gray border, in both themes. The default. */
        LIGHT,

        /** Dark gray box, no border; a little lighter in the dark theme so it stays visible. */
        DARK,

        /** {@link #LIGHT} in the light theme, {@link #DARK}-style in the dark theme. */
        AUTO
    }

    /** What opens the tooltip. */
    public enum Trigger {
        /** Open while the pointer is over the target. The default. */
        HOVER,

        /** Click the target to toggle; click elsewhere or press Escape to close. */
        CLICK
    }

    /** Flowbite's {@code animation}: the fade-in / fade-out duration. */
    public enum Animation {
        /** Flowbite's {@code animation={false}}. */
        NONE(0),
        DURATION_150(150),
        DURATION_300(300),
        DURATION_500(500),
        DURATION_1000(1000);

        private final double millis;

        Animation(double millis) {
            this.millis = millis;
        }

        Duration duration() {
            return Duration.millis(millis);
        }
    }

    private final ObjectProperty<Node> target = new SimpleObjectProperty<>(this, "target");
    private final StringProperty content = new SimpleStringProperty(this, "content", "");
    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<>(this, "contentNode");
    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT);
    private final ObjectProperty<Placement> placement = new SimpleObjectProperty<>(this, "placement",
            DEFAULT_PLACEMENT);
    private final ObjectProperty<Trigger> trigger = new SimpleObjectProperty<>(this, "trigger", DEFAULT_TRIGGER);
    private final BooleanProperty arrow = new SimpleBooleanProperty(this, "arrow", true);
    private final ObjectProperty<Animation> animation = new SimpleObjectProperty<>(this, "animation",
            DEFAULT_ANIMATION);

    /** The logical open state; the popup window may stay up a little longer while it fades out. */
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    private final Popup popup = new Popup();
    private final StackPane popupRoot = new StackPane();
    private final StackPane box = new StackPane();
    private final Label label = new Label();
    private final Region arrowNode = new Region();

    private FadeTransition fade;
    private Scene filterScene;
    private final EventHandler<MouseEvent> outsidePressFilter = e -> {
        if (!isInsideThis(e.getPickResult().getIntersectedNode())) {
            hide();
        }
    };
    private final EventHandler<KeyEvent> escapeFilter = e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
            hide();
        }
    };

    public FxTooltip() {
        initialize();
    }

    public FxTooltip(String content) {
        initialize();
        setContent(content);
    }

    public FxTooltip(String content, Node target) {
        initialize();
        setContent(content);
        setTarget(target);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        // w-fit: never grow beyond the target, whatever the parent offers.
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // ---- popup content: root (tokens + padding for shadow/arrow) > box > label, plus the arrow
        popupRoot.getStyleClass().add(POPUP_STYLE_CLASS);
        box.getStyleClass().add(BOX_STYLE_CLASS);
        label.getStyleClass().add(CONTENT_STYLE_CLASS);
        label.setWrapText(true);
        label.setMaxWidth(MAX_CONTENT_WIDTH);
        arrowNode.getStyleClass().add(ARROW_STYLE_CLASS);
        arrowNode.setRotate(45);
        arrowNode.setMouseTransparent(true);
        arrowNode.visibleProperty().bind(arrow);
        arrowNode.managedProperty().bind(arrow);
        // Drawn after the box so it covers the box's border where they meet (Flowbite: z-10 over the base).
        popupRoot.getChildren().addAll(box, arrowNode);
        popupRoot.setMouseTransparent(true);
        EnumStyleClassSync.sync(popupRoot, VARIANT_STYLE_CLASS_PREFIX, variant);

        popup.getContent().add(popupRoot);
        popup.setAutoFix(false); // we flip and shift ourselves
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);
        popup.setConsumeAutoHidingEvents(false);
        popup.showingProperty().addListener((observable, wasShowing, isShowing) -> {
            if (!isShowing) {
                // Closed from outside (owner window hidden, ...): drop all open state.
                stopFade();
                showing.set(false);
                removeDismissFilters();
            }
        });

        // ---- wiring
        target.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                getChildren().clear();
            } else {
                getChildren().setAll(List.of(newValue));
            }
        });
        content.addListener((observable, oldValue, newValue) -> contentChanged());
        contentNode.addListener((observable, oldValue, newValue) -> contentChanged());
        placement.addListener((observable, oldValue, newValue) -> reposition());
        arrow.addListener((observable, oldValue, newValue) -> reposition());
        trigger.addListener((observable, oldValue, newValue) -> hideNow());
        variant.addListener((observable, oldValue, newValue) -> reposition());

        hoverProperty().addListener((observable, wasHover, isHover) -> {
            if (getTrigger() == Trigger.HOVER) {
                if (isHover) {
                    show();
                } else {
                    hide();
                }
            }
        });
        // A filter, not a handler: it must see the click even if the target consumes it.
        addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (getTrigger() == Trigger.CLICK && e.getButton() == MouseButton.PRIMARY) {
                if (isShowing()) {
                    hide();
                } else {
                    show();
                }
            }
        });
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                hideNow();
            }
        });

        refreshContent();
    }

    // ---- show / hide
    // -----------------------------------------------------------------------------

    /**
     * Opens the tooltip. Does nothing if the target is not in a showing window or there is no content.
     */
    public void show() {
        if (showing.get() || !hasContent()) {
            return;
        }
        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()) {
            return;
        }
        Bounds screen = localToScreen(getBoundsInLocal());
        if (screen == null) {
            return;
        }
        showing.set(true);
        stopFade();
        if (!popup.isShowing()) {
            applyOwnerTheme(scene);
            popupRoot.setOpacity(0);
            popup.show(this, screen.getMinX(), screen.getMinY());
        }
        reposition();
        installDismissFilters(scene);
        fadeTo(1, null);
    }

    /** Closes the tooltip (fading out first, unless animation is {@link Animation#NONE}). */
    public void hide() {
        if (!showing.get()) {
            return;
        }
        showing.set(false);
        removeDismissFilters();
        if (popup.isShowing()) {
            // If show() is called again mid-fade, stopFade() cancels this and the popup stays.
            fadeTo(0, () -> {
                if (!showing.get()) {
                    popup.hide();
                }
            });
        }
    }

    private void hideNow() {
        showing.set(false);
        removeDismissFilters();
        stopFade();
        popup.hide();
    }

    private void fadeTo(double opacity, Runnable afterwards) {
        stopFade();
        Duration duration = getAnimation() == null ? Duration.ZERO : getAnimation().duration();
        if (duration.lessThanOrEqualTo(Duration.ZERO)) {
            popupRoot.setOpacity(opacity);
            if (afterwards != null) {
                afterwards.run();
            }
            return;
        }
        FadeTransition transition = new FadeTransition(duration, popupRoot);
        transition.setFromValue(popupRoot.getOpacity());
        transition.setToValue(opacity);
        transition.setOnFinished(e -> {
            fade = null;
            if (afterwards != null) {
                afterwards.run();
            }
        });
        fade = transition;
        transition.play();
    }

    private void stopFade() {
        if (fade != null) {
            fade.stop();
            fade = null;
        }
    }

    // ---- dismiss (outside click, Escape)
    // ---------------------------------------------------------------

    private void installDismissFilters(Scene scene) {
        if (filterScene == scene) {
            return;
        }
        removeDismissFilters();
        filterScene = scene;
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressFilter);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
    }

    private void removeDismissFilters() {
        if (filterScene != null) {
            filterScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressFilter);
            filterScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
            filterScene = null;
        }
    }

    private boolean isInsideThis(Node node) {
        for (Node n = node; n != null; n = n.getParent()) {
            if (n == this) {
                return true;
            }
        }
        return false;
    }

    // ---- content and theme
    // ---------------------------------------------------------------------------

    private boolean hasContent() {
        String text = getContent();
        return getContentNode() != null || (text != null && !text.isBlank());
    }

    private void contentChanged() {
        refreshContent();
        reposition();
    }

    private void refreshContent() {
        Node node = getContentNode();
        if (node != null) {
            box.getChildren().setAll(node);
        } else {
            String text = getContent();
            label.setText(text == null ? "" : text);
            box.getChildren().setAll(label);
        }
        setAccessibleHelp(getContent());
    }

    /**
     * The popup is its own window, so give it what the main scene has: the stylesheets and the theme
     * class on its root (the same thing FxDropdown does).
     */
    private void applyOwnerTheme(Scene owner) {
        Scene popupScene = popup.getScene();
        popupScene.getStylesheets().setAll(owner.getStylesheets());
        ObservableList<String> rootClasses = popupScene.getRoot().getStyleClass();
        if (!rootClasses.contains("root")) {
            rootClasses.add("root");
        }
        rootClasses.removeIf(ThemeManager.DARK_CLASS::equals);
        if (ThemeManager.current(owner) == Theme.DARK) {
            rootClasses.add(ThemeManager.DARK_CLASS);
        }
    }

    // ---- positioning
    // -----------------------------------------------------------------------------------

    /**
     * Places the box {@link #GAP} px from the target on the requested side, flips to the opposite side
     * if it does not fit on the screen, slides it along the edge if it overhangs, then points the arrow
     * at the target's center.
     */
    private void reposition() {
        if (!popup.isShowing()) {
            return;
        }
        Bounds t = localToScreen(getBoundsInLocal());
        if (t == null) {
            return;
        }
        popupRoot.applyCss();
        double width = popupRoot.prefWidth(-1);
        double height = popupRoot.prefHeight(width);
        Insets pad = popupRoot.getInsets(); // room around the box for the shadow and the arrow tip
        double boxW = width - pad.getLeft() - pad.getRight();
        double boxH = height - pad.getTop() - pad.getBottom();
        Rectangle2D screen = visualBoundsFor(t);

        Placement side = getPlacement() == null ? DEFAULT_PLACEMENT : getPlacement();
        double[] origin = boxOrigin(side, t, boxW, boxH);
        if (!fits(side, origin, boxW, boxH, screen)) {
            Placement flipped = side.opposite();
            double[] alt = boxOrigin(flipped, t, boxW, boxH);
            if (fits(flipped, alt, boxW, boxH, screen)) {
                side = flipped;
                origin = alt;
            }
        }
        double boxX = Math.max(screen.getMinX(), Math.min(origin[0], screen.getMaxX() - boxW));
        double boxY = Math.max(screen.getMinY(), Math.min(origin[1], screen.getMaxY() - boxH));

        pointArrow(side, t, boxX, boxY, boxW, boxH);

        popup.setX(boxX - pad.getLeft());
        popup.setY(boxY - pad.getTop());
        popup.sizeToScene();
    }

    private static double[] boxOrigin(Placement side, Bounds t, double boxW, double boxH) {
        double centerX = t.getMinX() + t.getWidth() / 2;
        double centerY = t.getMinY() + t.getHeight() / 2;
        return switch (side) {
            case TOP -> new double[] { centerX - boxW / 2, t.getMinY() - GAP - boxH };
            case BOTTOM -> new double[] { centerX - boxW / 2, t.getMaxY() + GAP };
            case LEFT -> new double[] { t.getMinX() - GAP - boxW, centerY - boxH / 2 };
            case RIGHT -> new double[] { t.getMaxX() + GAP, centerY - boxH / 2 };
        };
    }

    /** Only the axis the tooltip sits on matters; along the other axis it can always slide. */
    private static boolean fits(Placement side, double[] origin, double boxW, double boxH, Rectangle2D screen) {
        return switch (side) {
            case TOP -> origin[1] >= screen.getMinY();
            case BOTTOM -> origin[1] + boxH <= screen.getMaxY();
            case LEFT -> origin[0] >= screen.getMinX();
            case RIGHT -> origin[0] + boxW <= screen.getMaxX();
        };
    }

    private static Rectangle2D visualBoundsFor(Bounds t) {
        Rectangle2D area = new Rectangle2D(t.getMinX(), t.getMinY(), Math.max(1, t.getWidth()),
                Math.max(1, t.getHeight()));
        List<Screen> screens = Screen.getScreensForRectangle(area);
        return (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
    }

    /**
     * Puts the arrow's center on the box edge facing the target (Flowbite's {@code -4px}), at the
     * point nearest the target's center that stays clear of the rounded corners.
     */
    private void pointArrow(Placement side, Bounds t, double boxX, double boxY, double boxW, double boxH) {
        double shiftX = 0;
        double shiftY = 0;
        double half = ARROW_SIZE / 2;
        Pos alignment;
        switch (side) {
            case TOP -> {
                alignment = Pos.BOTTOM_CENTER;
                shiftX = slide(t.getMinX() + t.getWidth() / 2 - (boxX + boxW / 2), boxW);
                arrowNode.setTranslateY(half);
                arrowNode.setTranslateX(shiftX);
            }
            case BOTTOM -> {
                alignment = Pos.TOP_CENTER;
                shiftX = slide(t.getMinX() + t.getWidth() / 2 - (boxX + boxW / 2), boxW);
                arrowNode.setTranslateY(-half);
                arrowNode.setTranslateX(shiftX);
            }
            case LEFT -> {
                alignment = Pos.CENTER_RIGHT;
                shiftY = slide(t.getMinY() + t.getHeight() / 2 - (boxY + boxH / 2), boxH);
                arrowNode.setTranslateX(half);
                arrowNode.setTranslateY(shiftY);
            }
            case RIGHT -> {
                alignment = Pos.CENTER_LEFT;
                shiftY = slide(t.getMinY() + t.getHeight() / 2 - (boxY + boxH / 2), boxH);
                arrowNode.setTranslateX(-half);
                arrowNode.setTranslateY(shiftY);
            }
            default -> throw new IllegalStateException("Unknown placement: " + side);
        }
        StackPane.setAlignment(arrowNode, alignment);
    }

    private static double slide(double offset, double extent) {
        double limit = Math.max(0, extent / 2 - ARROW_EDGE_INSET);
        return Math.max(-limit, Math.min(limit, offset));
    }

    // ---- properties
    // -----------------------------------------------------------------------------

    /** @return the node the tooltip is attached to, or {@code null} */
    public Node getTarget() {
        return target.get();
    }

    /**
     * Sets the node the tooltip is attached to (Flowbite's {@code children}). This is the FXML default
     * property.
     *
     * @param target the trigger node, for example an {@code FxButton}
     */
    public void setTarget(Node target) {
        this.target.set(target);
    }

    public ObjectProperty<Node> targetProperty() {
        return target;
    }

    public String getContent() {
        return content.get();
    }

    /**
     * Flowbite's {@code content} as plain text. Long text wraps at 320px. Ignored while
     * {@link #getContentNode()} is set.
     *
     * @param content the tooltip text; {@code null} or blank (with no content node) means the tooltip
     *                never opens
     */
    public void setContent(String content) {
        this.content.set(content);
    }

    public StringProperty contentProperty() {
        return content;
    }

    public Node getContentNode() {
        return contentNode.get();
    }

    /**
     * Rich content (Flowbite's {@code content} can be any React node). When set, it replaces
     * {@link #getContent()} inside the tooltip box.
     *
     * @param contentNode the node to show, or {@code null} to go back to the text
     */
    public void setContentNode(Node contentNode) {
        this.contentNode.set(contentNode);
    }

    public ObjectProperty<Node> contentNodeProperty() {
        return contentNode;
    }

    public Variant getVariant() {
        return variant.get();
    }

    public void setVariant(Variant variant) {
        this.variant.set(variant);
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
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

    /**
     * Changing the trigger closes any open tooltip.
     *
     * @param trigger {@link Trigger#HOVER} or {@link Trigger#CLICK}
     */
    public void setTrigger(Trigger trigger) {
        this.trigger.set(trigger);
    }

    public ObjectProperty<Trigger> triggerProperty() {
        return trigger;
    }

    public boolean isArrow() {
        return arrow.get();
    }

    /**
     * @param arrow {@code false} hides the arrow (Flowbite's {@code arrow={false}})
     */
    public void setArrow(boolean arrow) {
        this.arrow.set(arrow);
    }

    public BooleanProperty arrowProperty() {
        return arrow;
    }

    public Animation getAnimation() {
        return animation.get();
    }

    public void setAnimation(Animation animation) {
        this.animation.set(animation);
    }

    public ObjectProperty<Animation> animationProperty() {
        return animation;
    }

    /** @return whether the tooltip is open (or opening); it is {@code false} as soon as it starts fading out */
    public boolean isShowing() {
        return showing.get();
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }
}
