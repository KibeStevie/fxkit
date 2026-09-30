package dev.fxkit.core.components.dropdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * A button that opens a menu of {@link FxDropdownItem}s, {@link FxDropdownHeader}s and
 * {@link FxDropdownDivider}s. Port of Flowbite React's {@code <Dropdown>}.
 *
 * <pre>{@code
 * FxDropdown dropdown = new FxDropdown("Dropdown button");
 * dropdown.getItems().addAll(
 *         new FxDropdownHeader("Bonnie Green", "bonnie@flowbite.com"),
 *         new FxDropdownItem("Dashboard"),
 *         new FxDropdownItem("Settings"),
 *         new FxDropdownDivider(),
 *         new FxDropdownItem("Sign out"));
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <ul>
 *   <li>{@code label} = {@link #labelProperty()} (the button's text)</li>
 *   <li>{@code <DropdownItem>/<DropdownDivider>/<DropdownHeader>} = the three item classes in
 *       {@link #getItems()}</li>
 *   <li>{@code inline}, {@code size}, {@code placement}, {@code dismissOnClick},
 *       {@code enableTypeAhead}, {@code arrowIcon} = properties of the same name</li>
 *   <li>{@code onClick} on an item = {@code setOnAction} on the {@link FxDropdownItem}</li>
 * </ul>
 *
 * <h2>How it works</h2>
 * FxDropdown <em>is</em> the trigger: it extends {@link Button} and reuses FXKit's button classes
 * ({@code fxk-btn fxk-btn-default fxk-btn-color-default fxk-btn-size-*}), or a plain text look when
 * {@link #inlineProperty() inline}. The menu lives in a {@link Popup}. A popup is its own window with
 * its own scene, so on every {@link #show()} the owner scene's stylesheets and FXKit's {@code dark}
 * root class are copied onto it: tokens and the dark theme therefore apply to the menu just as they do
 * to the rest of the scene (the same reason {@link ThemeManager} puts the stylesheets on the scene).
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>Click, Space or Enter toggles the menu; Down/Up open it and highlight the first/last item.</li>
 *   <li>While open: Up/Down/Home/End move the highlight, Enter/Space activates it, typing searches by
 *       item label (see {@link #enableTypeAheadProperty()}), Esc closes, Tab closes and moves on,
 *       and a click outside closes.</li>
 *   <li>The menu flips to the opposite side when the chosen side has no room, and is kept on screen.</li>
 * </ul>
 *
 * <p>Only {@link FxDropdownItem}s placed directly in {@link #getItems()} take part in keyboard
 * navigation and typeahead.
 */
@DefaultProperty("items")
public class FxDropdown extends Button {

    /** Trigger sizes, the same scale as FxButton ({@code fxk-btn-size-*}). Ignored when inline. */
    public enum Size { XS, SM, MD, LG, XL }

    private enum Align { START, CENTER, END }

    /**
     * Where the menu opens relative to the trigger (Flowbite's {@code placement}). The menu flips to
     * the opposite side automatically if it does not fit.
     */
    public enum Placement {
        TOP(Side.TOP, Align.CENTER),
        TOP_START(Side.TOP, Align.START),
        TOP_END(Side.TOP, Align.END),
        RIGHT(Side.RIGHT, Align.CENTER),
        RIGHT_START(Side.RIGHT, Align.START),
        RIGHT_END(Side.RIGHT, Align.END),
        BOTTOM(Side.BOTTOM, Align.CENTER),
        BOTTOM_START(Side.BOTTOM, Align.START),
        BOTTOM_END(Side.BOTTOM, Align.END),
        LEFT(Side.LEFT, Align.CENTER),
        LEFT_START(Side.LEFT, Align.START),
        LEFT_END(Side.LEFT, Align.END);

        private final Side side;
        private final Align align;

        Placement(Side side, Align align) {
            this.side = side;
            this.align = align;
        }

        /** Same alignment, opposite side. */
        Placement flip() {
            return switch (this) {
                case TOP -> BOTTOM;
                case TOP_START -> BOTTOM_START;
                case TOP_END -> BOTTOM_END;
                case BOTTOM -> TOP;
                case BOTTOM_START -> TOP_START;
                case BOTTOM_END -> TOP_END;
                case LEFT -> RIGHT;
                case LEFT_START -> RIGHT_START;
                case LEFT_END -> RIGHT_END;
                case RIGHT -> LEFT;
                case RIGHT_START -> LEFT_START;
                case RIGHT_END -> LEFT_END;
            };
        }
    }

    /**
     * Transparent margin around the visible menu inside the popup window. A window cannot draw
     * outside its own bounds, so without it the drop shadow would be cut off. Must be larger than the
     * shadow's blur radius plus offset ({@code -fxk-shadow-md}: 8 + 3).
     */
    private static final double GUTTER = 12;

    /** Gap between trigger and menu (Flowbite's floating offset of 8px). */
    private static final double OFFSET = 8;

    /** Typeahead forgets what was typed after this pause (Flowbite/floating-ui: 750ms). */
    private static final long TYPEAHEAD_RESET_NANOS = 750_000_000L;

    // ---- properties ----------------------------------------------------------------------------

    private final ObservableList<Node> items = FXCollections.observableArrayList();
    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MD);
    private final ObjectProperty<Placement> placement = new SimpleObjectProperty<>(this, "placement");
    private final BooleanProperty inline = new SimpleBooleanProperty(this, "inline", false);
    private final BooleanProperty dismissOnClick = new SimpleBooleanProperty(this, "dismissOnClick", true);
    private final BooleanProperty enableTypeAhead = new SimpleBooleanProperty(this, "enableTypeAhead", true);
    private final BooleanProperty arrowIcon = new SimpleBooleanProperty(this, "arrowIcon", true);
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    // Kept in fields on purpose: a binding that nothing references is garbage-collected, and its
    // style-class listener would silently stop working.
    private final BooleanBinding solid = inline.not();
    private final ObjectBinding<Size> solidSize =
            Bindings.createObjectBinding(() -> isInline() ? null : sizeOrDefault(), inline, size);

    // ---- trigger parts -------------------------------------------------------------------------

    private final SVGPath arrowPath = new SVGPath();
    private final StackPane arrow = new StackPane(arrowPath);

    // ---- popup parts ---------------------------------------------------------------------------

    private final VBox menu = new VBox();
    private final StackPane surface = new StackPane(menu);
    private final Popup popup = new Popup();
    private final FadeTransition fadeIn = new FadeTransition(Duration.millis(150), surface);

    private FxDropdownItem active;
    private final StringBuilder typeahead = new StringBuilder();
    private long lastTypedAt;

    public FxDropdown() {
        this(null);
    }

    /**
     * @param label the trigger's text (Flowbite's {@code label})
     */
    public FxDropdown(String label) {
        super(label);

        // ---- trigger look: reuse FxButton's classes, or go plain when inline ----
        getStyleClass().add("fxk-dropdown");
        setMnemonicParsing(false);
        setContentDisplay(ContentDisplay.RIGHT);
        BooleanStyleClassSync.sync(this, "fxk-btn", solid);
        BooleanStyleClassSync.sync(this, "fxk-btn-default", solid);
        BooleanStyleClassSync.sync(this, "fxk-btn-color-default", solid);
        EnumStyleClassSync.sync(this, "fxk-btn-size-", solidSize);
        BooleanStyleClassSync.sync(this, "fxk-dropdown-inline", inline);

        // ---- chevron: stroke follows whatever text color the current variant/theme gives the button ----
        arrowPath.setContent("M4 6 L8 10 L12 6");
        arrowPath.setFill(Color.TRANSPARENT);
        arrowPath.getStyleClass().add("fxk-dropdown-arrow");
        arrowPath.strokeProperty().bind(textFillProperty());
        arrow.getStyleClass().add("fxk-dropdown-arrow-box");
        arrow.setMouseTransparent(true);
        arrowIcon.addListener((observable, oldValue, newValue) -> refreshArrow());
        refreshArrow();

        // ---- popup ----
        menu.getStyleClass().add("fxk-dropdown-menu");
        surface.getStyleClass().add("fxk-dropdown-popup");
        surface.setPadding(new Insets(GUTTER));
        surface.setPickOnBounds(false); // the transparent gutter must not swallow clicks
        Bindings.bindContent(menu.getChildren(), items);

        popup.getContent().add(surface);
        popup.setAutoHide(true);                  // click outside closes
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(true);   // the click that closes is not also delivered, so clicking
                                                  // the trigger while open closes instead of re-opening
        popup.setAutoFix(false);                  // reposition() keeps the menu on screen itself
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // A popup's key events are redirected to its own scene by JavaFX, even while the trigger
        // keeps focus, so handling them here covers both cases.
        Scene popupScene = popup.getScene();
        popupScene.addEventHandler(KeyEvent.KEY_PRESSED, this::onPopupKeyPressed);
        popupScene.addEventHandler(KeyEvent.KEY_TYPED, this::onPopupKeyTyped);
        surface.addEventHandler(ActionEvent.ACTION, this::onItemAction);
        menu.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMenuMouseMoved);
        menu.addEventHandler(MouseEvent.MOUSE_EXITED, event -> setActive(null));

        popup.showingProperty().addListener((observable, wasShowing, isShowing) -> {
            showing.set(isShowing);
            if (!isShowing) {
                fadeIn.stop();
                setActive(null);
                typeahead.setLength(0);
            }
        });

        // Down/Up on the closed trigger opens the menu and highlights the first/last item.
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (!popup.isShowing() && (code == KeyCode.DOWN || code == KeyCode.UP)) {
                show();
                if (popup.isShowing()) {
                    moveActive(code == KeyCode.DOWN ? 1 : -1);
                    event.consume();
                }
            }
        });

        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                hide();
            }
        });
        disabledProperty().addListener((observable, wasDisabled, isDisabled) -> {
            if (isDisabled) {
                hide();
            }
        });
    }

    // ---- public API ----------------------------------------------------------------------------

    /**
     * @return the live list of menu entries, top to bottom: {@link FxDropdownItem},
     *         {@link FxDropdownDivider}, {@link FxDropdownHeader}, or any other node
     */
    public final ObservableList<Node> getItems() {
        return items;
    }

    /**
     * @return the trigger's text; an alias of {@link #textProperty()} named after Flowbite's
     *         {@code label} prop
     */
    public final StringProperty labelProperty() {
        return textProperty();
    }

    public final String getLabel() {
        return getText();
    }

    public final void setLabel(String label) {
        setText(label);
    }

    /**
     * @return the trigger size, default {@link Size#MD}; has no effect when {@link #inlineProperty() inline}
     */
    public final ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public final Size getSize() {
        return size.get();
    }

    public final void setSize(Size size) {
        this.size.set(size);
    }

    /**
     * @return where the menu opens. {@code null} (the default) means Flowbite's default:
     *         {@link Placement#BOTTOM}, or {@link Placement#BOTTOM_START} when inline
     */
    public final ObjectProperty<Placement> placementProperty() {
        return placement;
    }

    public final Placement getPlacement() {
        return placement.get();
    }

    public final void setPlacement(Placement placement) {
        this.placement.set(placement);
    }

    /**
     * @return {@code true} to show the trigger as plain text with a chevron instead of a button
     */
    public final BooleanProperty inlineProperty() {
        return inline;
    }

    public final boolean isInline() {
        return inline.get();
    }

    public final void setInline(boolean inline) {
        this.inline.set(inline);
    }

    /**
     * @return whether clicking an {@link FxDropdownItem} closes the menu, default {@code true}
     */
    public final BooleanProperty dismissOnClickProperty() {
        return dismissOnClick;
    }

    public final boolean isDismissOnClick() {
        return dismissOnClick.get();
    }

    public final void setDismissOnClick(boolean dismissOnClick) {
        this.dismissOnClick.set(dismissOnClick);
    }

    /**
     * @return whether typing while the menu is open jumps to the item whose label starts with what was
     *         typed, default {@code true}. Turn it off when a header contains a text input.
     */
    public final BooleanProperty enableTypeAheadProperty() {
        return enableTypeAhead;
    }

    public final boolean isEnableTypeAhead() {
        return enableTypeAhead.get();
    }

    public final void setEnableTypeAhead(boolean enableTypeAhead) {
        this.enableTypeAhead.set(enableTypeAhead);
    }

    /**
     * @return whether the trigger shows a chevron after its label, default {@code true}
     */
    public final BooleanProperty arrowIconProperty() {
        return arrowIcon;
    }

    public final boolean isArrowIcon() {
        return arrowIcon.get();
    }

    public final void setArrowIcon(boolean arrowIcon) {
        this.arrowIcon.set(arrowIcon);
    }

    /**
     * @return whether the menu is currently open
     */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    public final boolean isShowing() {
        return showing.get();
    }

    /** Opens the menu. Does nothing if it is open, the dropdown is disabled or not in a showing window. */
    public void show() {
        Scene scene = getScene();
        if (scene == null || scene.getWindow() == null || !scene.getWindow().isShowing()
                || isDisabled() || popup.isShowing()) {
            return;
        }
        Bounds trigger = localToScreen(getLayoutBounds());
        if (trigger == null) {
            return;
        }

        syncPopupTheme(scene);
        surface.setOpacity(0); // invisible until it is in its final position, then faded in
        popup.getScene().getRoot().applyCss();
        popup.show(this, trigger.getMinX(), trigger.getMaxY());
        reposition(trigger);
        fadeIn.playFromStart();
    }

    /** Closes the menu. */
    public void hide() {
        fadeIn.stop();
        popup.hide();
    }

    /** Opens the menu if closed, closes it if open. */
    public void toggle() {
        if (popup.isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /** Clicking the trigger (or Space/Enter on it) toggles the menu, then runs any {@code onAction}. */
    @Override
    public void fire() {
        if (!isDisabled()) {
            super.fire();
            toggle();
        }
    }

    // ---- trigger helpers -----------------------------------------------------------------------

    private Size sizeOrDefault() {
        Size current = getSize();
        return current == null ? Size.MD : current;
    }

    private void refreshArrow() {
        setGraphic(isArrowIcon() ? arrow : null);
    }

    // ---- popup: theme and position -------------------------------------------------------------

    /**
     * Copies what the owner scene gives FXKit components onto the popup's own scene: its stylesheets
     * (tokens.css etc. plus the application's own) and the {@code dark} root class.
     */
    private void syncPopupTheme(Scene ownerScene) {
        Scene popupScene = popup.getScene();
        popupScene.setUserAgentStylesheet(ownerScene.getUserAgentStylesheet());
        popupScene.getStylesheets().setAll(ownerScene.getStylesheets());

        PopupRootClasses.sync(popupScene, ThemeManager.current(ownerScene) == Theme.DARK);
    }

    private Placement resolvePlacement() {
        Placement chosen = getPlacement();
        if (chosen != null) {
            return chosen;
        }
        return isInline() ? Placement.BOTTOM_START : Placement.BOTTOM;
    }

    /** Runs right after the popup window exists and therefore has its real size. */
    private void reposition(Bounds trigger) {
        double width = popup.getWidth() - 2 * GUTTER;
        double height = popup.getHeight() - 2 * GUTTER;
        Rectangle2D area = visualBoundsAround(trigger);

        Placement wanted = resolvePlacement();
        Point2D position = position(wanted, trigger, width, height, OFFSET);
        if (overflowsMainAxis(wanted, position, width, height, area)) {
            Placement flipped = wanted.flip();
            Point2D alternative = position(flipped, trigger, width, height, OFFSET);
            if (!overflowsMainAxis(flipped, alternative, width, height, area)) {
                position = alternative;
            }
        }

        double x = clamp(position.getX(), area.getMinX(), area.getMaxX() - width);
        double y = clamp(position.getY(), area.getMinY(), area.getMaxY() - height);
        popup.setAnchorX(x - GUTTER);
        popup.setAnchorY(y - GUTTER);
    }

    private static Rectangle2D visualBoundsAround(Bounds trigger) {
        List<Screen> screens = Screen.getScreensForRectangle(
                trigger.getMinX(), trigger.getMinY(),
                Math.max(1, trigger.getWidth()), Math.max(1, trigger.getHeight()));
        Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
        return screen.getVisualBounds();
    }

    /** Top-left corner of the menu (not the popup window) for {@code placement}, in screen coordinates. */
    static Point2D position(Placement placement, Bounds trigger, double width, double height, double gap) {
        double x;
        double y;
        switch (placement.side) {
            case TOP, BOTTOM -> {
                x = switch (placement.align) {
                    case START -> trigger.getMinX();
                    case CENTER -> trigger.getMinX() + (trigger.getWidth() - width) / 2;
                    case END -> trigger.getMaxX() - width;
                };
                y = placement.side == Side.TOP ? trigger.getMinY() - gap - height : trigger.getMaxY() + gap;
            }
            case LEFT, RIGHT -> {
                y = switch (placement.align) {
                    case START -> trigger.getMinY();
                    case CENTER -> trigger.getMinY() + (trigger.getHeight() - height) / 2;
                    case END -> trigger.getMaxY() - height;
                };
                x = placement.side == Side.LEFT ? trigger.getMinX() - gap - width : trigger.getMaxX() + gap;
            }
            default -> throw new IllegalStateException("Unexpected side: " + placement.side);
        }
        return new Point2D(x, y);
    }

    /** Whether the menu sticks out past the screen on the side it opens toward. */
    static boolean overflowsMainAxis(Placement placement, Point2D position, double width, double height,
                                     Rectangle2D area) {
        return switch (placement.side) {
            case TOP -> position.getY() < area.getMinY();
            case BOTTOM -> position.getY() + height > area.getMaxY();
            case LEFT -> position.getX() < area.getMinX();
            case RIGHT -> position.getX() + width > area.getMaxX();
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    // ---- popup: events -------------------------------------------------------------------------

    private void onItemAction(ActionEvent event) {
        // Only real items dismiss; a button inside a custom header must not close the menu.
        if (isDismissOnClick() && event.getTarget() instanceof FxDropdownItem) {
            hide();
        }
    }

    private void onMenuMouseMoved(MouseEvent event) {
        FxDropdownItem item = itemOf(event.getTarget());
        setActive(item != null && !item.isDisabled() ? item : null);
    }

    private void onPopupKeyPressed(KeyEvent event) {
        if (event.isConsumed()) {
            return;
        }
        switch (event.getCode()) {
            case DOWN -> {
                moveActive(1);
                event.consume();
            }
            case UP -> {
                moveActive(-1);
                event.consume();
            }
            case HOME -> {
                List<FxDropdownItem> enabled = enabledItems();
                if (!enabled.isEmpty()) {
                    setActive(enabled.get(0));
                }
                event.consume();
            }
            case END -> {
                List<FxDropdownItem> enabled = enabledItems();
                if (!enabled.isEmpty()) {
                    setActive(enabled.get(enabled.size() - 1));
                }
                event.consume();
            }
            case ENTER, SPACE -> {
                if (event.getCode() == KeyCode.SPACE && isTyping()) {
                    event.consume(); // a space inside a typeahead word, not "activate"
                    return;
                }
                if (active != null) {
                    FxDropdownItem item = active;
                    event.consume();
                    item.fire();
                }
            }
            case TAB -> hide(); // not consumed: focus moves on from the trigger as usual
            default -> { }
        }
    }

    private void onPopupKeyTyped(KeyEvent event) {
        if (event.isConsumed() || !isEnableTypeAhead() || event.isShortcutDown()) {
            return;
        }
        String typed = event.getCharacter();
        if (typed == null || typed.isEmpty() || KeyEvent.CHAR_UNDEFINED.equals(typed)
                || Character.isISOControl(typed.charAt(0))) {
            return;
        }

        long now = System.nanoTime();
        if (now - lastTypedAt > TYPEAHEAD_RESET_NANOS) {
            typeahead.setLength(0);
        }
        if (typed.equals(" ") && typeahead.length() == 0) {
            return; // a lone space is "activate", handled on key pressed
        }
        lastTypedAt = now;
        typeahead.append(typed.toLowerCase(Locale.ROOT));
        selectByPrefix(typeahead.toString());
        event.consume();
    }

    private boolean isTyping() {
        return isEnableTypeAhead() && typeahead.length() > 0
                && System.nanoTime() - lastTypedAt <= TYPEAHEAD_RESET_NANOS;
    }

    // ---- popup: active item --------------------------------------------------------------------

    private List<FxDropdownItem> enabledItems() {
        List<FxDropdownItem> result = new ArrayList<>();
        for (Node node : items) {
            if (node instanceof FxDropdownItem item && !item.isDisabled() && item.isVisible()) {
                result.add(item);
            }
        }
        return result;
    }

    private void setActive(FxDropdownItem item) {
        if (active == item) {
            return;
        }
        if (active != null) {
            active.setActive(false);
        }
        active = item;
        if (active != null) {
            active.setActive(true);
        }
    }

    /** Moves the highlight by one; from "nothing active" Down goes to the first item, Up to the last. */
    private void moveActive(int direction) {
        List<FxDropdownItem> enabled = enabledItems();
        if (enabled.isEmpty()) {
            return;
        }
        int index = enabled.indexOf(active);
        int next;
        if (index < 0) {
            next = direction > 0 ? 0 : enabled.size() - 1;
        } else {
            next = Math.max(0, Math.min(enabled.size() - 1, index + direction));
        }
        setActive(enabled.get(next));
    }

    private void selectByPrefix(String prefix) {
        List<FxDropdownItem> enabled = enabledItems();
        if (enabled.isEmpty()) {
            return;
        }
        int current = enabled.indexOf(active);
        // A single letter moves on to the next match (press "s" again for the next "S..." item);
        // a longer prefix narrows down from the current item.
        int start = prefix.length() == 1 ? current + 1 : Math.max(current, 0);
        for (int offset = 0; offset < enabled.size(); offset++) {
            FxDropdownItem candidate = enabled.get((start + offset) % enabled.size());
            String text = candidate.getText();
            if (text != null && text.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                setActive(candidate);
                return;
            }
        }
    }

    private static FxDropdownItem itemOf(EventTarget target) {
        Node node = target instanceof Node n ? n : null;
        while (node != null && !(node instanceof FxDropdownItem)) {
            node = node.getParent();
        }
        return (FxDropdownItem) node;
    }

    /** Sets the popup scene's root classes: FXKit tokens hang off {@code .root}, the theme off {@code .dark}. */
    private static final class PopupRootClasses {
        private PopupRootClasses() {
        }

        static void sync(Scene popupScene, boolean dark) {
            List<String> rootClasses = popupScene.getRoot().getStyleClass();
            if (!rootClasses.contains("root")) {
                rootClasses.add("root");
            }
            rootClasses.remove(ThemeManager.DARK_CLASS);
            if (dark) {
                rootClasses.add(ThemeManager.DARK_CLASS);
            }
        }
    }
}
