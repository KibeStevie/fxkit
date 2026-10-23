package dev.fxkit.core.components.drawer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.FxThreadExtension;
import dev.fxkit.core.TestNodes;
import dev.fxkit.core.TestReflection;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * FxDrawer's open/close state, backdrop, panel placement and sizing, edge mode, and Escape handling.
 * Without a scene the drawer skips its slide animation and jumps straight to the end state, which is
 * what most of these tests use. Scrolling behavior is not covered. Runs on the FX thread; skipped
 * without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxDrawerTest {

    private FxDrawer drawer;

    @BeforeEach
    void createDrawer() {
        drawer = new FxDrawer();
    }

    private ScrollPane panel() {
        return TestNodes.require(drawer, "fxk-drawer-panel", ScrollPane.class);
    }

    private Region backdrop() {
        return TestNodes.require(drawer, "fxk-drawer-backdrop", Region.class);
    }

    /** The VBox inside the panel. It is not in the scene graph until the ScrollPane has a skin. */
    private VBox contentBox() {
        return TestReflection.field(drawer, "content", VBox.class);
    }

    private static void click(javafx.scene.Node target, MouseButton button) {
        Event.fireEvent(target, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, 1,
                false, false, false, false, button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY,
                false, false, true, null));
    }

    private static void press(javafx.scene.Node target, KeyCode code) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewDrawerIsClosedOnTheLeftWithTheDocumentedDefaults() {
        assertFalse(drawer.isOpen());
        assertEquals(FxDrawer.Position.LEFT, drawer.getPosition());
        assertTrue(drawer.isBackdrop());
        assertFalse(drawer.isBodyScrolling());
        assertFalse(drawer.isEdge());
        assertTrue(drawer.isPadded());
        assertTrue(drawer.isCloseOnEscape());
        assertEquals(FxDrawer.DEFAULT_PANEL_WIDTH, drawer.getPanelWidth(), 1e-9);
        assertEquals(FxDrawer.DEFAULT_EDGE_SIZE, drawer.getEdgeSize(), 1e-9);
        assertEquals(Duration.millis(150), drawer.getTransitionDuration());
        assertNull(drawer.getOnClose());
        assertTrue(drawer.getStyleClass().contains("fxk-drawer"));
        assertTrue(drawer.getStyleClass().contains("fxk-drawer-position-left"));
    }

    @Test
    void theEmptyLayerNeverCatchesClicksAndTheDrawerIsClippedToItsOwnSize() {
        assertFalse(drawer.isPickOnBounds());

        drawer.resize(300, 200);

        Rectangle clip = assertInstanceOf(Rectangle.class, drawer.getClip());
        assertEquals(300, clip.getWidth(), 1e-9);
        assertEquals(200, clip.getHeight(), 1e-9);
    }

    @Test
    void aClosedDrawerShowsNeitherThePanelNorTheBackdrop() {
        assertFalse(panel().isVisible());
        assertFalse(backdrop().isVisible());
        assertEquals(0, backdrop().getOpacity(), 1e-9);
    }

    // ---- content ----------------------------------------------------------------------------

    @Test
    void theConstructorPutsItsChildrenInTheContent() {
        Label first = new Label("first");
        Label second = new Label("second");

        FxDrawer withContent = new FxDrawer(first, second);

        assertEquals(List.of(first, second), List.copyOf(withContent.getContent()));
    }

    @Test
    void theContentListIsLive() {
        Label added = new Label("added");

        drawer.getContent().add(added);

        assertSame(added, contentBox().getChildren().get(0));
    }

    // ---- open and close ---------------------------------------------------------------------

    @Test
    void openingShowsThePanelAndTheBackdropAtFullStrength() {
        drawer.setOpen(true);

        assertTrue(panel().isVisible());
        assertTrue(backdrop().isVisible());
        assertEquals(1, backdrop().getOpacity(), 1e-9);
    }

    @Test
    void closingHidesThemAgain() {
        drawer.setOpen(true);

        drawer.setOpen(false);

        assertFalse(panel().isVisible());
        assertFalse(backdrop().isVisible());
        assertEquals(0, backdrop().getOpacity(), 1e-9);
    }

    @Test
    void withoutABackdropOnlyThePanelAppears() {
        drawer.setBackdrop(false);

        drawer.setOpen(true);

        assertTrue(panel().isVisible());
        assertFalse(backdrop().isVisible());
    }

    @Test
    void theBackdropCanBeSwitchedOffAndOnWhileOpen() {
        drawer.setOpen(true);

        drawer.setBackdrop(false);
        assertFalse(backdrop().isVisible());

        drawer.setBackdrop(true);
        assertTrue(backdrop().isVisible());
    }

    @Test
    void requestCloseClosesAnOpenDrawerAndRunsTheCloseHandlerOnce() {
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);
        drawer.setOpen(true);

        drawer.requestClose();

        assertFalse(drawer.isOpen());
        assertEquals(1, closes.get());
    }

    @Test
    void requestCloseOnAClosedDrawerDoesNothing() {
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);

        drawer.requestClose();

        assertEquals(0, closes.get());
    }

    @Test
    void closingInCodeDoesNotRunTheCloseHandler() {
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);
        drawer.setOpen(true);

        drawer.setOpen(false);

        assertEquals(0, closes.get(), "onClose is for the user asking to close, not for setOpen(false)");
    }

    @Test
    void clickingTheBackdropAsksTheDrawerToClose() {
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);
        drawer.setOpen(true);

        click(backdrop(), MouseButton.PRIMARY);

        assertFalse(drawer.isOpen());
        assertEquals(1, closes.get());
    }

    @Test
    void onlyAPrimaryClickOnTheBackdropCloses() {
        drawer.setOpen(true);

        click(backdrop(), MouseButton.SECONDARY);
        click(backdrop(), MouseButton.MIDDLE);

        assertTrue(drawer.isOpen());
    }

    // ---- panel size and placement -----------------------------------------------------------

    @Test
    void aSideDrawerPinsThePanelToItsWidth() {
        assertEquals(320, panel().getMinWidth(), 1e-9);
        assertEquals(320, panel().getPrefWidth(), 1e-9);
        assertEquals(320, panel().getMaxWidth(), 1e-9);
        assertEquals(Double.MAX_VALUE, panel().getMaxHeight(), 0);

        drawer.setPanelWidth(480);

        assertEquals(480, panel().getMinWidth(), 1e-9);
        assertEquals(480, panel().getPrefWidth(), 1e-9);
        assertEquals(480, panel().getMaxWidth(), 1e-9);
    }

    @Test
    void aTopOrBottomDrawerSpansTheWidthAndHugsItsContentHeight() {
        drawer.setPosition(FxDrawer.Position.TOP);

        assertEquals(Region.USE_COMPUTED_SIZE, panel().getMinWidth(), 0);
        assertEquals(Region.USE_COMPUTED_SIZE, panel().getPrefWidth(), 0);
        assertEquals(Double.MAX_VALUE, panel().getMaxWidth(), 0);
        assertEquals(Region.USE_PREF_SIZE, panel().getMaxHeight(), 0);
    }

    @ParameterizedTest
    @CsvSource({"LEFT,CENTER_LEFT", "RIGHT,CENTER_RIGHT", "TOP,TOP_CENTER", "BOTTOM,BOTTOM_CENTER"})
    void thePositionAlignsThePanelAndSwapsTheStyleClass(FxDrawer.Position position, Pos alignment) {
        drawer.setPosition(position);

        assertEquals(alignment, StackPane.getAlignment(panel()));
        assertTrue(drawer.getStyleClass().contains(
                "fxk-drawer-position-" + position.name().toLowerCase(java.util.Locale.ROOT)));
        assertEquals(1, drawer.getStyleClass().stream()
                .filter(c -> c.startsWith("fxk-drawer-position-")).count());
    }

    @Test
    void aNullPositionFallsBackToLeft() {
        drawer.setPosition(FxDrawer.Position.BOTTOM);

        drawer.setPosition(null);

        assertEquals(FxDrawer.Position.LEFT, drawer.getPosition());
    }

    @ParameterizedTest
    @CsvSource({"LEFT,-320,0", "RIGHT,320,0", "TOP,0,-240", "BOTTOM,0,240"})
    void aClosedPanelIsPushedOffItsEdgeAndSlidesBackWhenOpen(
            FxDrawer.Position position, double closedX, double closedY) {
        drawer.setPosition(position);
        panel().resize(320, 240);

        assertEquals(closedX, panel().getTranslateX(), 1e-9);
        assertEquals(closedY, panel().getTranslateY(), 1e-9);

        drawer.setOpen(true);

        assertEquals(0, panel().getTranslateX(), 1e-9);
        assertEquals(0, panel().getTranslateY(), 1e-9);
    }

    // ---- edge mode --------------------------------------------------------------------------

    @Test
    void edgeModeKeepsASliverOfTheClosedPanelVisible() {
        panel().resize(320, 240);

        drawer.setEdge(true);

        assertTrue(drawer.getStyleClass().contains("fxk-drawer-edge"));
        assertTrue(panel().isVisible(), "the edge must be visible while closed");
        assertEquals(-(320 - 64), panel().getTranslateX(), 1e-9);
    }

    @Test
    void theEdgeSizeControlsHowMuchStaysVisible() {
        panel().resize(320, 240);
        drawer.setEdge(true);

        drawer.setEdgeSize(100);

        assertEquals(-(320 - 100), panel().getTranslateX(), 1e-9);
    }

    @Test
    void anEdgeLargerThanThePanelHidesNothing() {
        panel().resize(320, 240);
        drawer.setEdge(true);

        drawer.setEdgeSize(1000);

        assertEquals(0, panel().getTranslateX(), 1e-9);
    }

    @Test
    void turningEdgeOffHidesAClosedPanelAgain() {
        drawer.setEdge(true);

        drawer.setEdge(false);

        assertFalse(panel().isVisible());
        assertFalse(drawer.getStyleClass().contains("fxk-drawer-edge"));
    }

    // ---- padding ----------------------------------------------------------------------------

    @Test
    void paddedIsAStyleClassOnTheContentThatFollowsTheProperty() {
        assertTrue(contentBox().getStyleClass().contains("fxk-drawer-padded"));

        drawer.setPadded(false);
        assertFalse(contentBox().getStyleClass().contains("fxk-drawer-padded"));

        drawer.setPadded(true);
        assertTrue(contentBox().getStyleClass().contains("fxk-drawer-padded"));
    }

    // ---- Escape and scene handling ----------------------------------------------------------

    private Scene sceneWithInstantDrawer() {
        drawer.setTransitionDuration(Duration.ZERO);
        return new Scene(drawer, 400, 300);
    }

    @Test
    void escapeClosesAnOpenDrawerInAScene() {
        sceneWithInstantDrawer();
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);
        drawer.setOpen(true);

        press(drawer, KeyCode.ESCAPE);

        assertFalse(drawer.isOpen());
        assertEquals(1, closes.get());
    }

    @Test
    void otherKeysDoNotCloseTheDrawer() {
        sceneWithInstantDrawer();
        drawer.setOpen(true);

        press(drawer, KeyCode.ENTER);
        press(drawer, KeyCode.A);

        assertTrue(drawer.isOpen());
    }

    @Test
    void escapeIsIgnoredWhenCloseOnEscapeIsOff() {
        sceneWithInstantDrawer();
        drawer.setCloseOnEscape(false);
        drawer.setOpen(true);

        press(drawer, KeyCode.ESCAPE);

        assertTrue(drawer.isOpen());
    }

    @Test
    void escapeOnAClosedDrawerDoesNotRunTheCloseHandler() {
        sceneWithInstantDrawer();
        AtomicInteger closes = new AtomicInteger();
        drawer.setOnClose(closes::incrementAndGet);

        press(drawer, KeyCode.ESCAPE);

        assertEquals(0, closes.get());
    }

    @Test
    void aDrawerRemovedFromItsSceneNoLongerReactsToEscape() {
        StackPane root = new StackPane(drawer);
        new Scene(root, 400, 300);
        drawer.setTransitionDuration(Duration.ZERO);
        drawer.setOpen(true);

        root.getChildren().remove(drawer);
        // the removed drawer must have dropped its scene filters: an ESC in that scene leaves it open
        press(root, KeyCode.ESCAPE);

        assertTrue(drawer.isOpen());
        assertNull(drawer.getScene());
    }

    @Test
    void openingInASceneWithAnAnimationShowsThePanelFromTheFirstFrame() {
        new Scene(drawer, 400, 300);

        drawer.setOpen(true); // 150 ms slide: the panel must not wait for it to become visible

        assertTrue(panel().isVisible());
        assertFalse(backdrop().isVisible(), "the backdrop fades in from zero");

        drawer.setTransitionDuration(Duration.ZERO); // also stops the running animation
        drawer.setOpen(false);
        assertFalse(panel().isVisible());
    }

    @Test
    void anOpenDrawerWithAZeroOrMissingTransitionJumpsStraightToTheEnd() {
        new Scene(drawer, 400, 300);

        drawer.setTransitionDuration(null);
        drawer.setOpen(true);
        assertEquals(1, backdrop().getOpacity(), 1e-9);

        drawer.setTransitionDuration(Duration.ZERO);
        drawer.setOpen(false);
        assertEquals(0, backdrop().getOpacity(), 1e-9);
    }
}
