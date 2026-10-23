package dev.fxkit.core.components.footer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.FxThreadExtension;
import javafx.geometry.Orientation;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * FxFooterLinkGroup lays its children out as a wrapping row (24px between links, 8px between wrapped
 * lines) or as a column (16px apart). Checked with plain 50 x 20 Regions so the numbers are exact.
 * Runs on the FX thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxFooterLinkGroupTest {

    private Region a;
    private Region b;
    private Region c;

    @BeforeEach
    void createChildren() {
        a = box();
        b = box();
        c = box();
    }

    private static Region box() {
        Region region = new Region();
        region.setPrefSize(50, 20);
        region.setMinSize(10, 10);
        return region;
    }

    private static void layOut(FxFooterLinkGroup group, double width, double height) {
        group.resize(width, height);
        group.requestLayout();
        group.layout();
    }

    // ---- construction -----------------------------------------------------------------------

    @Test
    void aGroupIsARowByDefaultAndTheBooleanConstructorMakesAColumn() {
        FxFooterLinkGroup row = new FxFooterLinkGroup(a, b);
        assertFalse(row.isCol());
        assertTrue(row.getStyleClass().contains("fxk-footer-link-group"));
        assertFalse(row.getStyleClass().contains("fxk-footer-link-group-col"));

        FxFooterLinkGroup column = new FxFooterLinkGroup(true, a, b);
        assertTrue(column.isCol());
        assertTrue(column.getStyleClass().contains("fxk-footer-link-group-col"));
    }

    @Test
    void colIsAStyleClassThatFollowsTheProperty() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b);

        group.setCol(true);
        assertTrue(group.getStyleClass().contains("fxk-footer-link-group-col"));

        group.setCol(false);
        assertFalse(group.getStyleClass().contains("fxk-footer-link-group-col"));
    }

    @Test
    void anEmptyGroupHasNoSize() {
        FxFooterLinkGroup group = new FxFooterLinkGroup();

        assertEquals(0, group.prefWidth(-1), 1e-9);
        assertEquals(0, group.prefHeight(-1), 1e-9);
        layOut(group, 100, 20); // must not throw
    }

    // ---- sizing -----------------------------------------------------------------------------

    @Test
    void aRowsPreferredWidthIsAllLinksPlus24PxBetweenThem() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b, c);

        assertEquals(3 * 50 + 2 * 24, group.prefWidth(-1), 1e-9);
        assertEquals(20, group.prefHeight(-1), 1e-9, "one line when the width is unlimited");
    }

    @Test
    void aColumnsPreferredWidthIsTheWidestLinkAndItsHeightAddsThe16PxGaps() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(true, a, b, c);

        assertEquals(50, group.prefWidth(-1), 1e-9);
        assertEquals(3 * 20 + 2 * 16, group.prefHeight(-1), 1e-9);
    }

    @Test
    void theMinimumWidthIsTheWidestMinimumOfTheChildren() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b, c);
        b.setMinWidth(35);

        assertEquals(35, group.minWidth(-1), 1e-9);
    }

    @Test
    void aRowWrapsWhenTheWidthRunsOutAndAddsTheLineGap() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b, c);

        // two links (50 + 24 + 50 = 124) fit in 130; the third wraps, 8px below the first line
        assertEquals(20 + 8 + 20, group.prefHeight(130), 1e-9);
        assertEquals(20, group.prefHeight(400), 1e-9, "all three fit on one line");
    }

    @Test
    void unmanagedChildrenDoNotCount() {
        c.setManaged(false);
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b, c);

        assertEquals(2 * 50 + 24, group.prefWidth(-1), 1e-9);
    }

    @Test
    void aRowIsWidthBiasedBecauseItsHeightDependsOnItsWidthButAColumnIsNot() {
        assertEquals(Orientation.HORIZONTAL, new FxFooterLinkGroup(a, b).getContentBias());
        assertNull(new FxFooterLinkGroup(true, a, b).getContentBias());
    }

    // ---- layout -----------------------------------------------------------------------------

    @Test
    void aRowPlacesLinksLeftToRightAndWrapsTheLastOne() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b, c);

        layOut(group, 130, 48);

        assertEquals(0, a.getLayoutX(), 1e-9);
        assertEquals(0, a.getLayoutY(), 1e-9);
        assertEquals(74, b.getLayoutX(), 1e-9, "50 wide + 24 gap");
        assertEquals(0, b.getLayoutY(), 1e-9);
        assertEquals(0, c.getLayoutX(), 1e-9, "wrapped to the second line");
        assertEquals(28, c.getLayoutY(), 1e-9, "20 high + 8 line gap");
    }

    @Test
    void aColumnStacksLinksWith16PxBetweenThem() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(true, a, b, c);

        layOut(group, 100, 92);

        assertEquals(0, a.getLayoutX(), 1e-9);
        assertEquals(0, a.getLayoutY(), 1e-9);
        assertEquals(36, b.getLayoutY(), 1e-9);
        assertEquals(72, c.getLayoutY(), 1e-9);
        assertEquals(0, b.getLayoutX(), 1e-9);
    }

    @Test
    void aLinkWiderThanTheGroupIsSqueezedToFit() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a);

        layOut(group, 30, 20);

        assertEquals(30, a.getWidth(), 1e-9);
    }

    @Test
    void switchingToAColumnRelaysOutTheLinks() {
        FxFooterLinkGroup group = new FxFooterLinkGroup(a, b);
        layOut(group, 200, 20);
        assertEquals(74, b.getLayoutX(), 1e-9);

        group.setCol(true);
        layOut(group, 200, 56);

        assertEquals(0, b.getLayoutX(), 1e-9);
        assertEquals(36, b.getLayoutY(), 1e-9);
    }
}
