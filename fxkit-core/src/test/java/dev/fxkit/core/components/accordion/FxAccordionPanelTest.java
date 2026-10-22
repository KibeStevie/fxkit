package dev.fxkit.core.components.accordion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import javafx.event.Event;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A single panel with its title and content: toggling, mouse and keyboard activation, what the title
 * tells assistive technology, and the content helpers. Needs the toolkit; skipped without one.
 */
class FxAccordionPanelTest {

    private FxAccordionPanel panel;

    @BeforeEach
    void createPanel() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        panel = new FxAccordionPanel("Title", "First paragraph", "Second paragraph");
    }

    private static void click(Node target, MouseButton button) {
        Event.fireEvent(target, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, 1,
                false, false, false, false, button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY,
                false, false, true, null));
    }

    private static void key(Node target, KeyCode code) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    // ---- open state -------------------------------------------------------------------------

    @Test
    void aNewPanelIsClosedAndHidesItsContent() {
        assertFalse(panel.isOpen());
        assertFalse(panel.getContent().isVisible());
        assertFalse(panel.getContent().isManaged(), "closed content must not take up space");
        assertFalse(panel.getTitle().isExpanded());
    }

    @Test
    void openingShowsTheContentAndMarksTheTitleExpanded() {
        panel.setOpen(true);

        assertTrue(panel.getContent().isVisible());
        assertTrue(panel.getContent().isManaged());
        assertTrue(panel.getTitle().isExpanded());
    }

    @Test
    void toggleFlipsTheState() {
        panel.toggle();
        assertTrue(panel.isOpen());

        panel.toggle();
        assertFalse(panel.isOpen());
    }

    @Test
    void openIsAStyleClassOnThePanelAndOnTheTitle() {
        assertFalse(panel.getStyleClass().contains("fxk-accordion-open"));
        assertFalse(panel.getTitle().getStyleClass().contains("fxk-accordion-title-open"));

        panel.setOpen(true);
        assertTrue(panel.getStyleClass().contains("fxk-accordion-open"));
        assertTrue(panel.getTitle().getStyleClass().contains("fxk-accordion-title-open"));

        panel.setOpen(false);
        assertFalse(panel.getStyleClass().contains("fxk-accordion-open"));
        assertFalse(panel.getTitle().getStyleClass().contains("fxk-accordion-title-open"));
    }

    // ---- activation -------------------------------------------------------------------------

    @Test
    void aPrimaryClickOnTheTitleToggles() {
        click(panel.getTitle(), MouseButton.PRIMARY);
        assertTrue(panel.isOpen());

        click(panel.getTitle(), MouseButton.PRIMARY);
        assertFalse(panel.isOpen());
    }

    @Test
    void otherMouseButtonsDoNotToggle() {
        click(panel.getTitle(), MouseButton.SECONDARY);
        click(panel.getTitle(), MouseButton.MIDDLE);

        assertFalse(panel.isOpen());
    }

    @Test
    void enterAndSpaceToggleTheFocusedTitle() {
        key(panel.getTitle(), KeyCode.ENTER);
        assertTrue(panel.isOpen());

        key(panel.getTitle(), KeyCode.SPACE);
        assertFalse(panel.isOpen());
    }

    @Test
    void otherKeysDoNotToggle() {
        key(panel.getTitle(), KeyCode.A);
        key(panel.getTitle(), KeyCode.TAB);
        key(panel.getTitle(), KeyCode.ESCAPE);

        assertFalse(panel.isOpen());
    }

    // ---- title ------------------------------------------------------------------------------

    @Test
    void theTitleIsFocusableAndAnnouncedAsAButton() {
        FxAccordionTitle title = panel.getTitle();

        assertTrue(title.isFocusTraversable());
        assertEquals(AccessibleRole.BUTTON, title.getAccessibleRole());
    }

    @Test
    void theTitleReportsItsTextAndExpandedStateToAssistiveTechnology() {
        FxAccordionTitle title = panel.getTitle();

        assertEquals("Title", title.queryAccessibleAttribute(AccessibleAttribute.TEXT));
        assertEquals(Boolean.FALSE, title.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));

        panel.setOpen(true);
        assertEquals(Boolean.TRUE, title.queryAccessibleAttribute(AccessibleAttribute.EXPANDED));

        title.setText("Renamed");
        assertEquals("Renamed", title.queryAccessibleAttribute(AccessibleAttribute.TEXT));
    }

    @Test
    void titleTextIsReadableAndWritable() {
        assertEquals("Title", panel.getTitle().getText());

        panel.getTitle().setText("Other");

        assertEquals("Other", panel.getTitle().getText());
        assertEquals("Other", panel.getTitle().textProperty().get());
    }

    @Test
    void aGraphicIsShownFirstAndReplacedOrRemovedCleanly() {
        FxAccordionTitle title = new FxAccordionTitle("T");
        int withoutGraphic = title.getChildren().size();
        Label icon1 = new Label("1");
        Label icon2 = new Label("2");

        title.setGraphic(icon1);
        assertEquals(withoutGraphic + 1, title.getChildren().size());
        assertSame(icon1, title.getChildren().get(0));

        title.setGraphic(icon2);
        assertEquals(withoutGraphic + 1, title.getChildren().size());
        assertSame(icon2, title.getChildren().get(0));

        title.setGraphic(null);
        assertEquals(withoutGraphic, title.getChildren().size());
        assertFalse(title.getChildren().contains(icon2));
    }

    @Test
    void aStandaloneTitleDoesNothingWhenActivated() {
        FxAccordionTitle title = new FxAccordionTitle("Alone");

        click(title, MouseButton.PRIMARY);
        key(title, KeyCode.ENTER);

        assertFalse(title.isExpanded());
    }

    // ---- construction -----------------------------------------------------------------------

    @Test
    void theStringConstructorBuildsATitleAndOneParagraphPerString() {
        assertEquals("Title", panel.getTitle().getText());
        assertEquals(2, panel.getContent().getChildren().size());
        assertTrue(panel.getStyleClass().contains("fxk-accordion-panel"));
    }

    @Test
    void thePanelHoldsItsTitleAboveItsContent() {
        assertEquals(2, panel.getChildren().size());
        assertSame(panel.getTitle(), panel.getChildren().get(0));
        assertSame(panel.getContent(), panel.getChildren().get(1));
    }

    @Test
    void aMissingTitleOrContentIsAProgrammingError() {
        assertThrows(NullPointerException.class,
                () -> new FxAccordionPanel((FxAccordionTitle) null, new FxAccordionContent()));
        assertThrows(NullPointerException.class,
                () -> new FxAccordionPanel(new FxAccordionTitle("t"), (FxAccordionContent) null));
    }

    // ---- content ----------------------------------------------------------------------------

    @Test
    void paragraphsWrapAndGetTheBodyTextClass() {
        Label paragraph = FxAccordionContent.paragraph("Hello");

        assertEquals("Hello", paragraph.getText());
        assertTrue(paragraph.isWrapText());
        assertTrue(paragraph.getStyleClass().contains("fxk-accordion-text"));
    }

    @Test
    void contentBuiltFromStringsHasOneParagraphEach() {
        FxAccordionContent content = new FxAccordionContent("one", "two", "three");

        assertEquals(3, content.getChildren().size());
        assertEquals("two", ((Label) content.getChildren().get(1)).getText());
        assertTrue(content.getStyleClass().contains("fxk-accordion-content"));
    }

    @Test
    void contentBuiltFromNodesKeepsThoseNodes() {
        Region first = new Region();
        Region second = new Region();

        FxAccordionContent content = new FxAccordionContent(first, second);

        assertSame(first, content.getChildren().get(0));
        assertSame(second, content.getChildren().get(1));
    }

    @Test
    void emptyContentHasNoChildren() {
        assertTrue(new FxAccordionContent().getChildren().isEmpty());
    }
}
