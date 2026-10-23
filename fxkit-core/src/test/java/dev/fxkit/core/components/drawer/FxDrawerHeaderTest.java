package dev.fxkit.core.components.drawer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.FxThreadExtension;
import dev.fxkit.core.TestNodes;
import javafx.beans.DefaultProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The drawer header (title, close button, optional whole-header click), the body container, and the
 * two small lookup helpers the header uses. Icons are not covered here: they need an Ikonli icon pack
 * on the test classpath. Runs on the FX thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxDrawerHeaderTest {

    private FxDrawerHeader header;

    @BeforeEach
    void createHeader() {
        header = new FxDrawerHeader("Add widget");
    }

    private Button closeButton() {
        return TestNodes.require(header, "fxk-drawer-close", Button.class);
    }

    private static void click(Node target, MouseButton button) {
        Event.fireEvent(target, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, 1,
                false, false, false, false, button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY,
                false, false, true, null));
    }

    // ---- title and close button -------------------------------------------------------------

    @Test
    void theHeaderShowsItsTitleAndAnAccessibleCloseButton() {
        assertTrue(header.getStyleClass().contains("fxk-drawer-header"));
        assertEquals("Add widget", header.getTitle());
        assertEquals("Add widget", TestNodes.require(header, "fxk-drawer-title", Label.class).getText());
        assertEquals("Close menu", closeButton().getAccessibleText());
        assertNull(header.getTitleIcon());
        assertNull(header.getCloseIcon());
    }

    @Test
    void theNoArgumentHeaderHasAnEmptyTitle() {
        assertEquals("", new FxDrawerHeader().getTitle());
    }

    @Test
    void changingTheTitleUpdatesTheLabel() {
        header.setTitle("Renamed");

        assertEquals("Renamed", TestNodes.require(header, "fxk-drawer-title", Label.class).getText());
    }

    @Test
    void withoutATitleIconTheTitleRowHoldsOnlyTheLabel() {
        HBox row = TestNodes.require(header, "fxk-drawer-header-row", HBox.class);

        assertEquals(1, row.getChildren().size());
    }

    @Test
    void theDefaultCloseGlyphIsADrawnX() {
        assertTrue(TestNodes.has(closeButton().getGraphic(), "fxk-drawer-close-shape"));
    }

    // ---- closing the drawer -----------------------------------------------------------------

    @Test
    void theCloseButtonClosesTheEnclosingDrawer() {
        FxDrawer drawer = new FxDrawer();
        drawer.setTransitionDuration(Duration.ZERO);
        // Added as a direct child on purpose: the drawer's real content sits in a ScrollPane, which only
        // becomes an ancestor of it once the ScrollPane has a skin.
        drawer.getChildren().add(header);
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);
        drawer.setOpen(true);

        closeButton().fire();

        assertFalse(drawer.isOpen());
        assertEquals(1, closes.get());
    }

    @Test
    void theCloseButtonOutsideAnyDrawerDoesNothingAndDoesNotThrow() {
        closeButton().fire();

        assertNull(header.getOnClick());
    }

    // ---- onClick ----------------------------------------------------------------------------

    @Test
    void anOnClickHandlerMakesTheHeaderClickableAndReplacesClosing() {
        FxDrawer drawer = new FxDrawer();
        drawer.setTransitionDuration(Duration.ZERO);
        drawer.getChildren().add(header);
        drawer.setOpen(true);
        AtomicInteger clicks = new AtomicInteger();
        assertFalse(header.getStyleClass().contains("fxk-drawer-header-clickable"));

        header.setOnClick(clicks::incrementAndGet);
        closeButton().fire();

        assertTrue(header.getStyleClass().contains("fxk-drawer-header-clickable"));
        assertEquals(1, clicks.get());
        assertTrue(drawer.isOpen(), "with onClick set, the close button runs it instead of closing");
    }

    @Test
    void clearingOnClickRemovesTheClickableLook() {
        header.setOnClick(() -> { });

        header.setOnClick(null);

        assertFalse(header.getStyleClass().contains("fxk-drawer-header-clickable"));
    }

    @Test
    void aPrimaryClickAnywhereOnTheHeaderRunsOnClick() {
        AtomicInteger clicks = new AtomicInteger();
        header.setOnClick(clicks::incrementAndGet);

        click(header, MouseButton.PRIMARY);
        assertEquals(1, clicks.get());

        click(header, MouseButton.SECONDARY);
        click(header, MouseButton.MIDDLE);
        assertEquals(1, clicks.get(), "only a primary click counts");
    }

    @Test
    void aClickOnTheCloseButtonRunsOnClickOnlyOnceNotAlsoViaTheHeader() {
        AtomicInteger clicks = new AtomicInteger();
        header.setOnClick(clicks::incrementAndGet);

        click(closeButton(), MouseButton.PRIMARY); // bubbles to the header, which must skip it
        assertEquals(0, clicks.get());

        closeButton().fire(); // the button's own action is what runs the handler
        assertEquals(1, clicks.get());
    }

    @Test
    void withoutOnClickAHeaderClickDoesNothing() {
        FxDrawer drawer = new FxDrawer();
        drawer.setTransitionDuration(Duration.ZERO);
        drawer.getChildren().add(header);
        drawer.setOpen(true);

        click(header, MouseButton.PRIMARY);

        assertTrue(drawer.isOpen());
    }

    // ---- lookup helpers ---------------------------------------------------------------------

    @Test
    void isWithinIsTrueForTheNodeItselfAndItsDescendantsOnly() {
        Region leaf = new Region();
        StackPane parent = new StackPane(leaf);
        StackPane other = new StackPane();

        assertTrue(FxDrawer.isWithin(parent, parent));
        assertTrue(FxDrawer.isWithin(parent, leaf));
        assertFalse(FxDrawer.isWithin(leaf, parent));
        assertFalse(FxDrawer.isWithin(other, leaf));
        assertFalse(FxDrawer.isWithin(parent, null));
        assertFalse(FxDrawer.isWithin(parent, "not a node"));
    }

    @Test
    void findDrawerReturnsTheNearestDrawerAboveANodeOrNull() {
        FxDrawer outer = new FxDrawer();
        FxDrawer inner = new FxDrawer();
        Region leaf = new Region();
        inner.getChildren().add(leaf);
        outer.getChildren().add(inner);

        assertSame(inner, FxDrawer.findDrawer(leaf));
        assertSame(outer, FxDrawer.findDrawer(inner));
        assertNull(FxDrawer.findDrawer(outer));
        assertNull(FxDrawer.findDrawer(new Region()));
    }

    // ---- items container --------------------------------------------------------------------

    @Test
    void drawerItemsIsAnUnstyledVerticalContainerForItsChildren() {
        Label one = new Label("one");
        Label two = new Label("two");

        FxDrawerItems items = new FxDrawerItems(one, two);

        assertTrue(items.getStyleClass().contains("fxk-drawer-items"));
        assertSame(one, items.getChildren().get(0));
        assertSame(two, items.getChildren().get(1));
        assertTrue(new FxDrawerItems().getChildren().isEmpty());
    }

    @Test
    void drawerItemsTakesFxmlChildrenAsItsDefaultProperty() {
        DefaultProperty annotation = FxDrawerItems.class.getAnnotation(DefaultProperty.class);

        assertEquals("children", annotation.value());
    }
}
