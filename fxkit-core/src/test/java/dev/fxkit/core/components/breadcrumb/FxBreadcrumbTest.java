package dev.fxkit.core.components.breadcrumb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import dev.fxkit.core.JavaFxToolkit;
import dev.fxkit.core.TestNodes;
import javafx.geometry.Pos;
import javafx.scene.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FxBreadcrumb lays out "item, chevron, item, chevron, item" and rebuilds that whenever its item list
 * changes. Needs the toolkit; skipped without one.
 */
class FxBreadcrumbTest {

    private FxBreadcrumbItem home;
    private FxBreadcrumbItem projects;
    private FxBreadcrumbItem current;

    @BeforeEach
    void createItems() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        home = new FxBreadcrumbItem("Home");
        projects = new FxBreadcrumbItem("Projects");
        current = new FxBreadcrumbItem("Flowbite");
    }

    private static boolean isSeparator(Node node) {
        return node.getStyleClass().contains("fxk-breadcrumb-separator");
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewBreadcrumbIsEmptyAndLeftAlignedWithAnAccessibleLabel() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb();

        assertTrue(breadcrumb.getStyleClass().contains("fxk-breadcrumb"));
        assertEquals(Pos.CENTER_LEFT, breadcrumb.getAlignment());
        assertEquals("Breadcrumb", breadcrumb.getAccessibleText());
        assertTrue(breadcrumb.getItems().isEmpty());
        assertTrue(breadcrumb.getChildren().isEmpty());
        assertFalse(breadcrumb.isSolidBackground());
    }

    @Test
    void theAccessibleLabelCanBeMadeMoreSpecific() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb();

        breadcrumb.setAccessibleText("Project path");

        assertEquals("Project path", breadcrumb.getAccessibleText());
    }

    @Test
    void solidBackgroundIsAStyleClassThatFollowsTheProperty() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb();

        breadcrumb.setSolidBackground(true);
        assertTrue(breadcrumb.getStyleClass().contains("fxk-breadcrumb-solid"));

        breadcrumb.setSolidBackground(false);
        assertFalse(breadcrumb.getStyleClass().contains("fxk-breadcrumb-solid"));
    }

    // ---- layout -----------------------------------------------------------------------------

    @Test
    void itemsAreSeparatedByChevronsAndNeverStartOrEndWithOne() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home, projects, current);

        List<Node> children = breadcrumb.getChildren();
        assertEquals(5, children.size());
        assertSame(home, children.get(0));
        assertTrue(isSeparator(children.get(1)));
        assertSame(projects, children.get(2));
        assertTrue(isSeparator(children.get(3)));
        assertSame(current, children.get(4));
    }

    @Test
    void theConstructorFillsTheItemList() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home, projects);

        assertEquals(List.of(home, projects), List.copyOf(breadcrumb.getItems()));
    }

    @Test
    void aSingleItemHasNoSeparator() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home);

        assertEquals(1, breadcrumb.getChildren().size());
        assertSame(home, breadcrumb.getChildren().get(0));
    }

    @Test
    void separatorsAreDecorationThatDoesNotCatchMouseEventsAndHoldAChevron() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home, projects);

        Node separator = breadcrumb.getChildren().get(1);

        assertTrue(separator.isMouseTransparent());
        assertTrue(TestNodes.has(separator, "fxk-breadcrumb-chevron"));
    }

    // ---- changes ----------------------------------------------------------------------------

    @Test
    void addingAnItemAppendsASeparatorAndTheItem() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home);

        breadcrumb.getItems().add(projects);

        assertEquals(3, breadcrumb.getChildren().size());
        assertTrue(isSeparator(breadcrumb.getChildren().get(1)));
        assertSame(projects, breadcrumb.getChildren().get(2));
    }

    @Test
    void removingTheMiddleItemRemovesOneSeparatorWithIt() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home, projects, current);

        breadcrumb.getItems().remove(projects);

        List<Node> children = breadcrumb.getChildren();
        assertEquals(3, children.size());
        assertSame(home, children.get(0));
        assertTrue(isSeparator(children.get(1)));
        assertSame(current, children.get(2));
    }

    @Test
    void clearingTheItemsClearsTheChildren() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home, projects, current);

        breadcrumb.getItems().clear();

        assertTrue(breadcrumb.getChildren().isEmpty());
    }

    @Test
    void replacingTheItemsFollowsTheNewOrder() {
        FxBreadcrumb breadcrumb = new FxBreadcrumb(home, projects, current);

        breadcrumb.getItems().setAll(current, projects, home);

        List<Node> children = breadcrumb.getChildren();
        assertEquals(5, children.size());
        assertSame(current, children.get(0));
        assertSame(projects, children.get(2));
        assertSame(home, children.get(4));
        assertTrue(isSeparator(children.get(1)));
        assertTrue(isSeparator(children.get(3)));
    }
}
