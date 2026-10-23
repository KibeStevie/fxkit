package dev.fxkit.core.components.dropdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.FxThreadExtension;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The popup placement maths: where a 40 x 50 menu goes around a trigger, which placement it flips to
 * when there is no room, and what counts as "no room". Pure geometry, no windows.
 */
@ExtendWith(FxThreadExtension.class)
class FxDropdownPlacementTest {

    /** x 100..180, y 200..230. */
    private static final Bounds TRIGGER = new BoundingBox(100, 200, 80, 30);
    private static final double WIDTH = 40;
    private static final double HEIGHT = 50;
    private static final double GAP = 8;

    @ParameterizedTest
    @CsvSource({
            "TOP,120,142", "TOP_START,100,142", "TOP_END,140,142",
            "BOTTOM,120,238", "BOTTOM_START,100,238", "BOTTOM_END,140,238",
            "LEFT,52,190", "LEFT_START,52,200", "LEFT_END,52,180",
            "RIGHT,188,190", "RIGHT_START,188,200", "RIGHT_END,188,180"})
    void everyPlacementPutsTheMenuWhereFlowbiteWouldPutIt(
            FxDropdown.Placement placement, double x, double y) {
        Point2D position = FxDropdown.position(placement, TRIGGER, WIDTH, HEIGHT, GAP);

        assertEquals(x, position.getX(), 1e-9, "x");
        assertEquals(y, position.getY(), 1e-9, "y");
    }

    @Test
    void theGapIsKeptBetweenTheTriggerAndTheMenu() {
        Point2D below = FxDropdown.position(FxDropdown.Placement.BOTTOM, TRIGGER, WIDTH, HEIGHT, 20);
        Point2D above = FxDropdown.position(FxDropdown.Placement.TOP, TRIGGER, WIDTH, HEIGHT, 20);

        assertEquals(TRIGGER.getMaxY() + 20, below.getY(), 1e-9);
        assertEquals(TRIGGER.getMinY() - 20 - HEIGHT, above.getY(), 1e-9);
    }

    @ParameterizedTest
    @CsvSource({
            "TOP,BOTTOM", "TOP_START,BOTTOM_START", "TOP_END,BOTTOM_END",
            "BOTTOM,TOP", "BOTTOM_START,TOP_START", "BOTTOM_END,TOP_END",
            "LEFT,RIGHT", "LEFT_START,RIGHT_START", "LEFT_END,RIGHT_END",
            "RIGHT,LEFT", "RIGHT_START,LEFT_START", "RIGHT_END,LEFT_END"})
    void flippingGoesToTheOppositeSideAndKeepsTheAlignment(
            FxDropdown.Placement placement, FxDropdown.Placement flipped) {
        assertEquals(flipped, placement.flip());
    }

    @ParameterizedTest
    @EnumSource(FxDropdown.Placement.class)
    void flippingTwiceComesBackToTheStart(FxDropdown.Placement placement) {
        assertEquals(placement, placement.flip().flip());
    }

    // ---- overflow ---------------------------------------------------------------------------

    private static final Rectangle2D SCREEN = new Rectangle2D(0, 0, 1000, 800);

    @Test
    void belowOverflowsWhenTheMenuWouldEndPastTheBottomEdge() {
        assertTrue(FxDropdown.overflowsMainAxis(FxDropdown.Placement.BOTTOM, new Point2D(10, 760), 40, 50, SCREEN));
        assertFalse(FxDropdown.overflowsMainAxis(FxDropdown.Placement.BOTTOM, new Point2D(10, 750), 40, 50, SCREEN),
                "ending exactly on the edge still fits");
    }

    @Test
    void aboveOverflowsWhenTheMenuWouldStartBeforeTheTopEdge() {
        assertTrue(FxDropdown.overflowsMainAxis(FxDropdown.Placement.TOP, new Point2D(10, -1), 40, 50, SCREEN));
        assertFalse(FxDropdown.overflowsMainAxis(FxDropdown.Placement.TOP, new Point2D(10, 0), 40, 50, SCREEN));
    }

    @Test
    void leftOverflowsWhenTheMenuWouldStartBeforeTheLeftEdge() {
        assertTrue(FxDropdown.overflowsMainAxis(FxDropdown.Placement.LEFT, new Point2D(-1, 10), 40, 50, SCREEN));
        assertFalse(FxDropdown.overflowsMainAxis(FxDropdown.Placement.LEFT, new Point2D(0, 10), 40, 50, SCREEN));
    }

    @Test
    void rightOverflowsWhenTheMenuWouldEndPastTheRightEdge() {
        assertTrue(FxDropdown.overflowsMainAxis(FxDropdown.Placement.RIGHT, new Point2D(961, 10), 40, 50, SCREEN));
        assertFalse(FxDropdown.overflowsMainAxis(FxDropdown.Placement.RIGHT, new Point2D(960, 10), 40, 50, SCREEN));
    }

    @Test
    void onlyTheDirectionOfTheMenuMattersNotTheSideways() {
        // BOTTOM_END far off to the left is still "fine" on the main axis; clamping handles the rest
        assertFalse(FxDropdown.overflowsMainAxis(
                FxDropdown.Placement.BOTTOM_END, new Point2D(-500, 100), 40, 50, SCREEN));
        assertFalse(FxDropdown.overflowsMainAxis(
                FxDropdown.Placement.LEFT_START, new Point2D(100, -500), 40, 50, SCREEN));
    }
}
