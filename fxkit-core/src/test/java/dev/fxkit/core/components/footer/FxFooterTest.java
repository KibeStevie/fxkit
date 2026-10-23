package dev.fxkit.core.components.footer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.FxThreadExtension;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * FxFooterRow is a "justify-between" row (one child fills it, several are spread edge to edge with at
 * least 16px between them), and FxFooter is that row plus two style flags. Layout is checked by giving
 * the row a size and asking it to lay out, with plain Regions of known size as children. Runs on the FX
 * thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxFooterTest {

    private Region wide;   // preferred 100 x 20, minimum 10 x 10
    private Region narrow; // preferred 60 x 20, minimum 10 x 10

    @BeforeEach
    void createChildren() {
        wide = box(100, 20);
        narrow = box(60, 20);
    }

    private static Region box(double width, double height) {
        Region region = new Region();
        region.setPrefSize(width, height);
        region.setMinSize(10, 10);
        return region;
    }

    private static void layOut(FxFooterRow row, double width, double height) {
        row.resize(width, height);
        row.requestLayout();
        row.layout();
    }

    // ---- footer -----------------------------------------------------------------------------

    @Test
    void aFooterIsARowWithTheFooterClassAndNoFlagsSet() {
        FxFooter footer = new FxFooter();

        assertTrue(footer instanceof FxFooterRow);
        assertTrue(footer.getStyleClass().containsAll(java.util.List.of("fxk-footer", "fxk-footer-row")));
        assertFalse(footer.isContainer());
        assertFalse(footer.isBgDark());
        assertFalse(footer.getStyleClass().contains("fxk-footer-container"));
        assertFalse(footer.getStyleClass().contains("fxk-footer-bg-dark"));
    }

    @Test
    void theConstructorTakesItsChildren() {
        FxFooter footer = new FxFooter(wide, narrow);

        assertSame(wide, footer.getChildren().get(0));
        assertSame(narrow, footer.getChildren().get(1));
    }

    @Test
    void containerAndBgDarkAreStyleClassesThatFollowTheirProperties() {
        FxFooter footer = new FxFooter();

        footer.setContainer(true);
        footer.setBgDark(true);
        assertTrue(footer.getStyleClass().contains("fxk-footer-container"));
        assertTrue(footer.getStyleClass().contains("fxk-footer-bg-dark"));

        footer.setContainer(false);
        footer.setBgDark(false);
        assertFalse(footer.getStyleClass().contains("fxk-footer-container"));
        assertFalse(footer.getStyleClass().contains("fxk-footer-bg-dark"));
    }

    // ---- row sizing -------------------------------------------------------------------------

    @Test
    void theRowsPreferredWidthIsTheChildrenPlusAMinimumGapBetweenThem() {
        FxFooterRow row = new FxFooterRow(wide, narrow);

        assertEquals(100 + 60 + FxFooterRow.MIN_GAP, row.prefWidth(-1), 1e-9);
        assertEquals(10 + 10 + FxFooterRow.MIN_GAP, row.minWidth(-1), 1e-9);
        assertEquals(20, row.prefHeight(-1), 1e-9);
    }

    @Test
    void aSingleChildNeedsNoGap() {
        FxFooterRow row = new FxFooterRow(wide);

        assertEquals(100, row.prefWidth(-1), 1e-9);
    }

    @Test
    void anEmptyRowHasNoSize() {
        FxFooterRow row = new FxFooterRow();

        assertEquals(0, row.prefWidth(-1), 1e-9);
        assertEquals(0, row.prefHeight(-1), 1e-9);
        layOut(row, 100, 20); // must not throw
    }

    @Test
    void unmanagedChildrenDoNotCount() {
        narrow.setManaged(false);
        FxFooterRow row = new FxFooterRow(wide, narrow);

        assertEquals(100, row.prefWidth(-1), 1e-9);
    }

    @Test
    void theRowIsWidthBiasedOnlyWhenAChildIs() {
        assertNull(new FxFooterRow(wide, narrow).getContentBias());

        FxFooterLinkGroup wrapping = new FxFooterLinkGroup(wide);
        assertEquals(Orientation.HORIZONTAL, new FxFooterRow(narrow, wrapping).getContentBias());
    }

    // ---- row layout -------------------------------------------------------------------------

    @Test
    void twoChildrenGoToTheTwoEdgesAndAreCenteredVertically() {
        FxFooterRow row = new FxFooterRow(wide, narrow);

        layOut(row, 400, 40);

        assertEquals(0, wide.getLayoutX(), 1e-9);
        assertEquals(100, wide.getWidth(), 1e-9);
        assertEquals(10, wide.getLayoutY(), 1e-9, "20px high in a 40px row: centered");
        assertEquals(340, narrow.getLayoutX(), 1e-9);
        assertEquals(60, narrow.getWidth(), 1e-9);
        assertEquals(400, narrow.getLayoutX() + narrow.getWidth(), 1e-9, "flush with the right edge");
    }

    @Test
    void threeChildrenShareTheFreeSpaceEvenly() {
        Region third = box(40, 20);
        FxFooterRow row = new FxFooterRow(wide, narrow, third);

        layOut(row, 400, 20);

        double gapOne = narrow.getLayoutX() - (wide.getLayoutX() + wide.getWidth());
        double gapTwo = third.getLayoutX() - (narrow.getLayoutX() + narrow.getWidth());
        assertEquals(gapOne, gapTwo, 1.0);
        assertEquals(400, third.getLayoutX() + third.getWidth(), 1e-9);
        assertEquals((400 - 200) / 2.0, gapOne, 1.0);
    }

    @Test
    void aSingleChildFillsTheWholeRow() {
        FxFooterRow row = new FxFooterRow(wide);

        layOut(row, 400, 40);

        assertEquals(0, wide.getLayoutX(), 1e-9);
        assertEquals(400, wide.getWidth(), 1e-9);
        assertEquals(40, wide.getHeight(), 1e-9);
    }

    @Test
    void topAlignmentPutsShorterChildrenAtTheTop() {
        FxFooterRow row = new FxFooterRow(wide, narrow);
        row.setVerticalAlignment(VPos.TOP);

        layOut(row, 400, 40);

        assertEquals(0, wide.getLayoutY(), 1e-9);
        assertEquals(0, narrow.getLayoutY(), 1e-9);
    }

    @Test
    void aNullVerticalAlignmentMeansCenter() {
        FxFooterRow row = new FxFooterRow();
        assertEquals(VPos.CENTER, row.getVerticalAlignment());

        row.setVerticalAlignment(VPos.BOTTOM);
        row.setVerticalAlignment(null);

        assertEquals(VPos.CENTER, row.getVerticalAlignment());
    }

    @Test
    void aNarrowRowShrinksTheChildrenButKeepsTheMinimumGapBetweenThem() {
        FxFooterRow row = new FxFooterRow(wide, narrow);

        layOut(row, 100, 20);

        assertTrue(wide.getWidth() < 100, "shrunk below its preferred width");
        assertTrue(narrow.getWidth() < 60, "shrunk below its preferred width");
        double gap = narrow.getLayoutX() - (wide.getLayoutX() + wide.getWidth());
        assertTrue(gap >= FxFooterRow.MIN_GAP - 1e-9, "gap was " + gap);
        assertTrue(narrow.getLayoutX() + narrow.getWidth() <= 100 + 1e-9, "must still fit in the row");
    }

    @Test
    void childrenNeverShrinkBelowTheirMinimumWidth() {
        FxFooterRow row = new FxFooterRow(wide, narrow);

        layOut(row, 30, 20);

        assertTrue(wide.getWidth() >= 10 - 1e-9);
        assertTrue(narrow.getWidth() >= 10 - 1e-9);
    }
}
