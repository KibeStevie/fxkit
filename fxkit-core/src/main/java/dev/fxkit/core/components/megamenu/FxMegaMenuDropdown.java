package dev.fxkit.core.components.megamenu;

import dev.fxkit.core.components.navbar.FxNavbar;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;

/**
 * The panel that opens under a navbar entry (Flowbite's {@code <MegaMenuDropdown>}), together with
 * the {@link #getToggle() toggle} that opens it.
 *
 * <p>Two looks, chosen with {@link #fullWidthProperty() fullWidth}:
 * <ul>
 *   <li><b>floating</b> (default): a rounded, bordered card that hangs under its toggle, sized by its
 *       content. Flowbite's "Default mega menu" and "Mega menu with icons".</li>
 *   <li><b>full width</b>: a panel as wide as the navbar, flush under it, with a border above and
 *       below; the content is centered in Tailwind's {@code max-w-screen-xl} (1280px). Flowbite's
 *       "Full width dropdown", "... with CTA" and "... with image". A navbar that is
 *       {@linkplain FxNavbar#isCompact() compact} always uses this look, because a floating card
 *       under a stacked row makes no sense.</li>
 * </ul>
 *
 * <p>The panel is a {@link Popup}, i.e. a separate window, so it can overlap whatever is under the
 * navbar. It copies the owner scene's stylesheets and theme every time it opens, so tokens resolve
 * exactly as in the main scene. It closes on a click outside, on Escape, when a link inside fires
 * ({@link #closeOnActionProperty()}), and whenever the window moves or resizes or the navbar moves
 * (for example when its {@code ScrollPane} scrolls) because the popup would otherwise be left behind.
 *
 * <pre>{@code
 * FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", new FxMegaMenuGrid(3,
 *         new FxMegaMenuColumn(new FxMegaMenuLink("About Us"), new FxMegaMenuLink("Library")),
 *         new FxMegaMenuColumn(new FxMegaMenuLink("Contact Us"), new FxMegaMenuLink("Terms")),
 *         new FxMegaMenuColumn(new FxMegaMenuLink("Newsletter"))));
 *
 * FxMegaMenu menu = new FxMegaMenu(new FxNavbarBrand("Flowbite"));
 * menu.getLinks().addAll(new FxNavbarLink("Home", true), company.getToggle(), new FxNavbarLink("Team"));
 * }</pre>
 */
@DefaultProperty("content")
public class FxMegaMenuDropdown {

    /** Tailwind's {@code max-w-screen-xl}: the widest the content of a full width panel gets. */
    public static final double CONTAINER_MAX_WIDTH = 1280;

    /** Transparent room around a floating card so its shadow is not clipped by the popup window. */
    private static final double SHADOW_PAD = 16;

    /** The floating card hangs this far under the toggle (the dropdown theme's {@code mt-2}). */
    private static final double FLOATING_OFFSET = 8;

    /** Ignore a toggle click this soon after an auto-hide: it is the click that closed the popup. */
    private static final long REOPEN_GUARD_NANOS = 250_000_000L;

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty fullWidth = new SimpleBooleanProperty(this, "fullWidth", false);
    private final BooleanProperty closeOnAction = new SimpleBooleanProperty(this, "closeOnAction", true);
    private final ReadOnlyBooleanWrapper open = new ReadOnlyBooleanWrapper(this, "open", false);

    private final FxMegaMenuDropdownToggle toggle;
    private final Popup popup = new Popup();
    private final StackPane host = new StackPane();
    private final StackPane panel = new StackPane();
    private final StackPane contentBox = new StackPane();

    private final InvalidationListener guard = observable -> hide();
    private Window guardedWindow;
    private Scene guardedScene;
    private Node guardedAnchor;
    private FxMegaMenu shownIn;
    private long lastHiddenNanos;

    public FxMegaMenuDropdown() {
        this(null, null);
    }

    /**
     * @param text the toggle's label
     */
    public FxMegaMenuDropdown(String text) {
        this(text, null);
    }

    /**
     * @param text    the toggle's label
     * @param content what the panel shows; usually an {@link FxMegaMenuGrid}
     */
    public FxMegaMenuDropdown(String text, Node content) {
        toggle = new FxMegaMenuDropdownToggle(text);
        toggle.setOnAction(event -> {
            if (System.nanoTime() - lastHiddenNanos < REOPEN_GUARD_NANOS) {
                return;
            }
            if (isOpen()) {
                hide();
            } else {
                show();
            }
        });

        // The popup's tree: host (transparent, carries .root / .dark) > panel > contentBox > content.
        host.getStyleClass().addAll("root", "fxk-megamenu-popup-host");
        panel.getStyleClass().add("fxk-megamenu-panel");
        contentBox.getStyleClass().add("fxk-megamenu-panel-content");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getChildren().add(contentBox);
        host.setAlignment(Pos.TOP_LEFT);
        host.getChildren().add(panel);
        panel.addEventHandler(ActionEvent.ACTION, event -> {
            if (isCloseOnAction()) {
                hide();
            }
        });

        popup.getContent().add(host);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(true);
        popup.setOnHidden(event -> onHidden());

        this.content.addListener((observable, oldContent, newContent) -> placeContent(newContent));
        toggle.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                hide(); // the toggle left the scene graph: there is nothing to hang the panel on
            }
        });
        setContent(content);
    }

    // ---- showing and hiding --------------------------------------------------------------------

    /**
     * Opens the panel under the toggle. Does nothing if it is already open, has no content, or the
     * toggle is not in a showing window.
     */
    public void show() {
        if (isOpen() || getContent() == null) {
            return;
        }
        Scene scene = toggle.getScene();
        Window window = scene == null ? null : scene.getWindow();
        if (window == null || !window.isShowing()) {
            return;
        }

        FxNavbar navbar = findNavbar();
        boolean full = isFullWidth() || (navbar != null && navbar.isCompact());

        // The popup is its own window: bring over the stylesheets and the theme of the owner scene.
        popup.getScene().getStylesheets().setAll(scene.getStylesheets());
        setClass(host, ThemeManager.DARK_CLASS, ThemeManager.current(scene) == Theme.DARK);
        setClass(host, "fxk-megamenu-popup-host-full", full);
        setClass(host, "fxk-megamenu-popup-host-floating", !full);
        setClass(panel, "fxk-megamenu-panel-full", full);
        setClass(panel, "fxk-megamenu-panel-floating", !full);
        contentBox.setMaxWidth(full ? CONTAINER_MAX_WIDTH : Region.USE_COMPUTED_SIZE);

        double x;
        double y;
        Node anchor;
        if (full) {
            anchor = navbar != null ? navbar : scene.getRoot();
            Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
            host.setMinWidth(bounds.getWidth());
            host.setPrefWidth(bounds.getWidth());
            host.setMaxWidth(bounds.getWidth());
            x = bounds.getMinX();
            y = bounds.getMaxY();
        } else {
            anchor = toggle;
            Bounds bounds = toggle.localToScreen(toggle.getBoundsInLocal());
            host.setMinWidth(Region.USE_COMPUTED_SIZE);
            host.setPrefWidth(Region.USE_COMPUTED_SIZE);
            host.setMaxWidth(Region.USE_COMPUTED_SIZE);
            x = bounds.getMinX() - SHADOW_PAD;
            y = bounds.getMaxY() + FLOATING_OFFSET - SHADOW_PAD;
        }

        if (navbar instanceof FxMegaMenu) {
            shownIn = (FxMegaMenu) navbar;
            shownIn.dropdownOpened(this); // closes a sibling that is still open
        }
        attachGuards(scene, anchor);
        popup.show(window, x, y);
        open.set(true);
        toggle.setOpen(true);
    }

    /** Closes the panel. Does nothing if it is not open. */
    public void hide() {
        if (popup.isShowing()) {
            popup.hide(); // onHidden() does the bookkeeping
        }
    }

    private void onHidden() {
        detachGuards();
        lastHiddenNanos = System.nanoTime();
        open.set(false);
        toggle.setOpen(false);
        if (shownIn != null) {
            shownIn.dropdownClosed(this);
            shownIn = null;
        }
    }

    private void placeContent(Node node) {
        if (node == null) {
            contentBox.getChildren().clear();
        } else {
            contentBox.getChildren().setAll(node);
        }
    }

    // ---- close when the anchor moves ------------------------------------------------------------

    /** A popup is positioned in screen coordinates once; anything that moves its anchor closes it. */
    private void attachGuards(Scene scene, Node anchor) {
        detachGuards();
        guardedScene = scene;
        guardedWindow = scene.getWindow();
        guardedAnchor = anchor;
        guardedWindow.xProperty().addListener(guard);
        guardedWindow.yProperty().addListener(guard);
        scene.widthProperty().addListener(guard);
        scene.heightProperty().addListener(guard);
        anchor.localToSceneTransformProperty().addListener(guard);
        anchor.getLocalToSceneTransform(); // an invalidation listener only fires once the value was read
        if (anchor instanceof Region) {
            ((Region) anchor).widthProperty().addListener(guard);
        }
    }

    private void detachGuards() {
        if (guardedWindow != null) {
            guardedWindow.xProperty().removeListener(guard);
            guardedWindow.yProperty().removeListener(guard);
            guardedScene.widthProperty().removeListener(guard);
            guardedScene.heightProperty().removeListener(guard);
            guardedAnchor.localToSceneTransformProperty().removeListener(guard);
            if (guardedAnchor instanceof Region) {
                ((Region) guardedAnchor).widthProperty().removeListener(guard);
            }
        }
        guardedWindow = null;
        guardedScene = null;
        guardedAnchor = null;
    }

    // ---- helpers --------------------------------------------------------------------------------

    private FxNavbar findNavbar() {
        Node node = toggle.getParent();
        while (node != null && !(node instanceof FxNavbar)) {
            node = node.getParent();
        }
        return (FxNavbar) node;
    }

    private static void setClass(Node node, String styleClass, boolean on) {
        if (on) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }

    // ---- properties -----------------------------------------------------------------------------

    /**
     * @return the entry to put in {@link FxMegaMenu#getLinks()}; clicking it opens and closes this
     *         dropdown
     */
    public final FxMegaMenuDropdownToggle getToggle() {
        return toggle;
    }

    /**
     * @return what the panel shows (Flowbite's {@code <MegaMenuDropdown>} children)
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return content.get();
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    /**
     * @return {@code true} for a panel as wide as the navbar; the default {@code false} is a card
     *         under the toggle. Read when the panel opens.
     */
    public final BooleanProperty fullWidthProperty() {
        return fullWidth;
    }

    public final boolean isFullWidth() {
        return fullWidth.get();
    }

    public final void setFullWidth(boolean fullWidth) {
        this.fullWidth.set(fullWidth);
    }

    /**
     * @return whether the panel closes after an action fires inside it (a link or item is clicked);
     *         default {@code true}
     */
    public final BooleanProperty closeOnActionProperty() {
        return closeOnAction;
    }

    public final boolean isCloseOnAction() {
        return closeOnAction.get();
    }

    public final void setCloseOnAction(boolean closeOnAction) {
        this.closeOnAction.set(closeOnAction);
    }

    /**
     * @return whether the panel is showing
     */
    public final ReadOnlyBooleanProperty openProperty() {
        return open.getReadOnlyProperty();
    }

    public final boolean isOpen() {
        return open.get();
    }
}
