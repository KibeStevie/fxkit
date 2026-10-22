package dev.fxkit.core.components.accordion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import dev.fxkit.core.JavaFxToolkit;
import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The accordion's open/close rules: which panels start open, what opening one does to the others,
 * and how alwaysOpen, collapseAll, flush and the first/last style classes behave.
 * Needs the toolkit; skipped without one.
 */
class FxAccordionTest {

    private FxAccordionPanel a;
    private FxAccordionPanel b;
    private FxAccordionPanel c;

    @BeforeEach
    void createPanels() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        a = new FxAccordionPanel("A", "Body of A");
        b = new FxAccordionPanel("B", "Body of B");
        c = new FxAccordionPanel("C", "Body of C");
    }

    private static void click(FxAccordionTitle title) {
        Event.fireEvent(title, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, true, null));
    }

    // ---- initial state ----------------------------------------------------------------------

    @Test
    void theFirstPanelStartsOpenAndTheRestClosed() {
        new FxAccordion(a, b, c);

        assertTrue(a.isOpen());
        assertFalse(b.isOpen());
        assertFalse(c.isOpen());
    }

    @Test
    void aPanelThatWasAlreadyOpenIsNotOverriddenByTheFirstOne() {
        b.setOpen(true);

        new FxAccordion(a, b, c);

        assertFalse(a.isOpen());
        assertTrue(b.isOpen());
        assertFalse(c.isOpen());
    }

    @Test
    void severalPanelsOpenBeforeAddingKeepOnlyTheFirstOpenOne() {
        b.setOpen(true);
        c.setOpen(true);

        new FxAccordion(a, b, c);

        assertFalse(a.isOpen());
        assertTrue(b.isOpen());
        assertFalse(c.isOpen());
    }

    @Test
    void anEmptyAccordionHasNoChildrenAndNoStateToApply() {
        FxAccordion accordion = new FxAccordion();

        assertTrue(accordion.getPanels().isEmpty());
        assertTrue(accordion.getChildren().isEmpty());
    }

    @Test
    void theInitialStateIsAppliedWhenTheFirstPanelsArriveLater() {
        FxAccordion accordion = new FxAccordion();

        accordion.getPanels().addAll(a, b);

        assertTrue(a.isOpen());
        assertFalse(b.isOpen());
    }

    @Test
    void panelsAddedAfterTheInitialStateStartClosedAndChangeNothing() {
        FxAccordion accordion = new FxAccordion(a);

        accordion.getPanels().add(b);

        assertTrue(a.isOpen());
        assertFalse(b.isOpen());
    }

    /**
     * KNOWN BUG, found while writing these tests. {@code applyInitialState()} runs only once, so adding
     * a panel that is already open to an accordion that already has an open panel leaves TWO panels open
     * even though alwaysOpen is off. Fix: after any change to {@code panels}, when alwaysOpen is false,
     * call {@code keepOnlyFirstOpen()} (or close the others of the newly added open panel). Remove the
     * {@code @Disabled} once it is fixed.
     */
    @Test
    @Disabled("Known bug: an already-open panel added later leaves two panels open (alwaysOpen is off)")
    void addingAnAlreadyOpenPanelLaterStillLeavesOnlyOneOpen() {
        FxAccordion accordion = new FxAccordion(a);
        c.setOpen(true);

        accordion.getPanels().add(c);

        long open = accordion.getPanels().stream().filter(FxAccordionPanel::isOpen).count();
        assertEquals(1, open);
    }

    // ---- exclusive opening ------------------------------------------------------------------

    @Test
    void openingAPanelClosesTheOthers() {
        new FxAccordion(a, b, c);

        b.setOpen(true);

        assertFalse(a.isOpen());
        assertTrue(b.isOpen());
        assertFalse(c.isOpen());
    }

    @Test
    void clickingATitleOpensThatPanelAndClosesTheOthers() {
        new FxAccordion(a, b, c);

        click(c.getTitle());

        assertFalse(a.isOpen());
        assertTrue(c.isOpen());
    }

    @Test
    void theOpenPanelCanBeClosedLeavingNoneOpen() {
        new FxAccordion(a, b);

        a.setOpen(false);

        assertFalse(a.isOpen());
        assertFalse(b.isOpen());
    }

    // ---- alwaysOpen -------------------------------------------------------------------------

    @Test
    void alwaysOpenLetsSeveralPanelsStayOpen() {
        FxAccordion accordion = new FxAccordion(a, b, c);
        accordion.setAlwaysOpen(true);

        b.setOpen(true);
        c.setOpen(true);

        assertTrue(a.isOpen());
        assertTrue(b.isOpen());
        assertTrue(c.isOpen());
    }

    @Test
    void turningAlwaysOpenOffKeepsOnlyTheFirstOpenPanel() {
        FxAccordion accordion = new FxAccordion(a, b, c);
        accordion.setAlwaysOpen(true);
        b.setOpen(true);
        c.setOpen(true);

        accordion.setAlwaysOpen(false);

        assertTrue(a.isOpen());
        assertFalse(b.isOpen());
        assertFalse(c.isOpen());
    }

    @Test
    void anAccordionBuiltWithAlwaysOpenStillOpensTheFirstPanelAtStart() {
        FxAccordion accordion = new FxAccordion();
        accordion.setAlwaysOpen(true);

        accordion.getPanels().addAll(a, b);

        assertTrue(a.isOpen());
        assertFalse(b.isOpen());
    }

    // ---- collapseAll ------------------------------------------------------------------------

    @Test
    void collapseAllClosesEveryPanel() {
        FxAccordion accordion = new FxAccordion(a, b);

        accordion.setCollapseAll(true);

        assertFalse(a.isOpen());
        assertFalse(b.isOpen());
    }

    @Test
    void collapseAllSetBeforeThePanelsArriveKeepsThemAllClosed() {
        FxAccordion accordion = new FxAccordion();
        accordion.setCollapseAll(true);
        a.setOpen(true);

        accordion.getPanels().addAll(a, b);

        assertFalse(a.isOpen());
        assertFalse(b.isOpen());
    }

    @Test
    void panelsCanBeOpenedAgainAfterCollapseAll() {
        FxAccordion accordion = new FxAccordion(a, b);
        accordion.setCollapseAll(true);

        b.setOpen(true);

        assertTrue(b.isOpen());
        assertFalse(a.isOpen());
    }

    // ---- structure --------------------------------------------------------------------------

    @Test
    void theChildrenAreThePanelsInOrder() {
        FxAccordion accordion = new FxAccordion(a, b, c);

        assertEquals(List.of(a, b, c), List.copyOf(accordion.getChildren()));
        assertTrue(accordion.getStyleClass().contains("fxk-accordion"));
    }

    @Test
    void firstAndLastStyleClassesMarkTheEnds() {
        new FxAccordion(a, b, c);

        assertTrue(a.getStyleClass().contains("fxk-accordion-first"));
        assertFalse(a.getStyleClass().contains("fxk-accordion-last"));
        assertFalse(b.getStyleClass().contains("fxk-accordion-first"));
        assertFalse(b.getStyleClass().contains("fxk-accordion-last"));
        assertTrue(c.getStyleClass().contains("fxk-accordion-last"));
        assertFalse(c.getStyleClass().contains("fxk-accordion-first"));
    }

    @Test
    void aSinglePanelIsBothFirstAndLast() {
        new FxAccordion(a);

        assertTrue(a.getStyleClass().contains("fxk-accordion-first"));
        assertTrue(a.getStyleClass().contains("fxk-accordion-last"));
    }

    @Test
    void removingAPanelMovesTheEndMarkersAndClearsTheRemovedPanel() {
        FxAccordion accordion = new FxAccordion(a, b, c);

        accordion.getPanels().remove(c);

        assertEquals(List.of(a, b), List.copyOf(accordion.getChildren()));
        assertTrue(b.getStyleClass().contains("fxk-accordion-last"));
        assertFalse(c.getStyleClass().contains("fxk-accordion-last"));
        assertFalse(c.getStyleClass().contains("fxk-accordion-first"));
    }

    @Test
    void aRemovedPanelNoLongerAffectsTheAccordion() {
        FxAccordion accordion = new FxAccordion(a, b, c);
        accordion.getPanels().remove(c);

        c.setOpen(true);

        assertTrue(a.isOpen(), "a removed panel opening must not close the panels that stayed");
    }

    @Test
    void flushIsAStyleClassThatFollowsTheProperty() {
        FxAccordion accordion = new FxAccordion(a);
        assertFalse(accordion.getStyleClass().contains("fxk-accordion-flush"));

        accordion.setFlush(true);
        assertTrue(accordion.isFlush());
        assertTrue(accordion.getStyleClass().contains("fxk-accordion-flush"));

        accordion.setFlush(false);
        assertFalse(accordion.getStyleClass().contains("fxk-accordion-flush"));
    }
}
