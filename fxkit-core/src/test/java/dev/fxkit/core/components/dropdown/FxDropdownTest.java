package dev.fxkit.core.components.dropdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.FxThreadExtension;
import dev.fxkit.core.TestNodes;
import dev.fxkit.core.TestReflection;
import javafx.beans.DefaultProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * FxDropdown's trigger-button look, its item list, and its keyboard and mouse highlighting. Showing
 * the real popup needs a visible window, so the highlighting is driven by firing key and mouse events
 * at the popup's scene, which is where the dropdown listens. Placement geometry is in
 * {@code FxDropdownPlacementTest}. Runs on the FX thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxDropdownTest {

    private FxDropdown dropdown;
    private FxDropdownItem apple;
    private FxDropdownItem banana;
    private FxDropdownItem berry;
    private FxDropdownItem cherry;
    private List<String> fired;

    @BeforeEach
    void createDropdown() {
        fired = new ArrayList<>();
        dropdown = new FxDropdown("Menu");
        apple = item("Apple");
        banana = item("Banana");
        banana.setDisable(true);
        berry = item("Berry");
        cherry = item("Cherry");
        dropdown.getItems().addAll(
                new FxDropdownHeader("Signed in as", "bonnie@example.com"),
                apple, banana, new FxDropdownDivider(), berry, cherry);
    }

    private FxDropdownItem item(String text) {
        FxDropdownItem item = new FxDropdownItem(text);
        item.setOnAction(e -> fired.add(text));
        return item;
    }

    private Scene popupScene() {
        return TestReflection.field(dropdown, "popup", Popup.class).getScene();
    }

    private void press(KeyCode code) {
        Event.fireEvent(popupScene(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private void type(String character) {
        Event.fireEvent(popupScene(),
                new KeyEvent(KeyEvent.KEY_TYPED, character, "", KeyCode.UNDEFINED, false, false, false, false));
    }

    private static void mouse(Node target, javafx.event.EventType<MouseEvent> type) {
        Event.fireEvent(target, new MouseEvent(type, 0, 0, 0, 0, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, true, null));
    }

    private static boolean isActive(FxDropdownItem item) {
        return item.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("active"));
    }

    /** The one highlighted item, or null when none is; fails if several are. */
    private FxDropdownItem active() {
        List<FxDropdownItem> highlighted = new ArrayList<>();
        for (FxDropdownItem candidate : List.of(apple, banana, berry, cherry)) {
            if (isActive(candidate)) {
                highlighted.add(candidate);
            }
        }
        assertTrue(highlighted.size() <= 1, "more than one item highlighted: " + highlighted);
        return highlighted.isEmpty() ? null : highlighted.get(0);
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewDropdownIsAMediumSolidButtonWithAnArrow() {
        assertEquals("Menu", dropdown.getLabel());
        assertEquals("Menu", dropdown.getText());
        assertEquals(FxDropdown.Size.MD, dropdown.getSize());
        assertFalse(dropdown.isInline());
        assertTrue(dropdown.isDismissOnClick());
        assertTrue(dropdown.isEnableTypeAhead());
        assertTrue(dropdown.isArrowIcon());
        assertNull(dropdown.getPlacement());
        assertFalse(dropdown.isShowing());
        assertFalse(dropdown.isMnemonicParsing());
        assertEquals(ContentDisplay.RIGHT, dropdown.getContentDisplay());
        assertTrue(dropdown.getStyleClass().containsAll(List.of(
                "fxk-dropdown", "fxk-btn", "fxk-btn-default", "fxk-btn-color-default", "fxk-btn-size-md")));
    }

    @Test
    void labelIsJustAnotherNameForTheButtonText() {
        dropdown.setLabel("Account");

        assertEquals("Account", dropdown.getText());
        assertSame(dropdown.textProperty(), dropdown.labelProperty());
    }

    @Test
    void itemsAreTheFxmlDefaultProperty() {
        assertEquals("items", FxDropdown.class.getAnnotation(DefaultProperty.class).value());
    }

    // ---- inline and size --------------------------------------------------------------------

    @Test
    void inlineDropsTheSolidButtonLookAndAddsTheInlineClass() {
        dropdown.setInline(true);

        assertTrue(dropdown.getStyleClass().contains("fxk-dropdown-inline"));
        for (String solid : List.of("fxk-btn", "fxk-btn-default", "fxk-btn-color-default", "fxk-btn-size-md")) {
            assertFalse(dropdown.getStyleClass().contains(solid), solid);
        }
    }

    @Test
    void goingBackFromInlineRestoresTheSolidLook() {
        dropdown.setInline(true);

        dropdown.setInline(false);

        assertFalse(dropdown.getStyleClass().contains("fxk-dropdown-inline"));
        assertTrue(dropdown.getStyleClass().containsAll(List.of(
                "fxk-btn", "fxk-btn-default", "fxk-btn-color-default", "fxk-btn-size-md")));
    }

    @Test
    void theSizeSwapsTheButtonSizeClass() {
        dropdown.setSize(FxDropdown.Size.LG);

        assertTrue(dropdown.getStyleClass().contains("fxk-btn-size-lg"));
        assertFalse(dropdown.getStyleClass().contains("fxk-btn-size-md"));
    }

    @Test
    void aNullSizeIsTreatedAsMedium() {
        dropdown.setSize(FxDropdown.Size.XL);

        dropdown.setSize(null);

        assertTrue(dropdown.getStyleClass().contains("fxk-btn-size-md"));
    }

    @Test
    void anInlineDropdownHasNoSizeClassEvenWhenASizeIsSet() {
        dropdown.setInline(true);

        dropdown.setSize(FxDropdown.Size.SM);
        assertTrue(dropdown.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-btn-size-")));

        dropdown.setInline(false);
        assertTrue(dropdown.getStyleClass().contains("fxk-btn-size-sm"));
    }

    // ---- arrow ------------------------------------------------------------------------------

    @Test
    void theArrowIsTheButtonGraphicAndCanBeSwitchedOffAndOn() {
        Node arrow = dropdown.getGraphic();
        assertTrue(TestNodes.has(arrow, "fxk-dropdown-arrow-box"));

        dropdown.setArrowIcon(false);
        assertNull(dropdown.getGraphic());

        dropdown.setArrowIcon(true);
        assertSame(arrow, dropdown.getGraphic());
    }

    @Test
    void theArrowDoesNotInterceptMouseEvents() {
        assertTrue(dropdown.getGraphic().isMouseTransparent());
    }

    // ---- activation and showing -------------------------------------------------------------

    @Test
    void firingRunsTheActionHandler() {
        dropdown.setOnAction(e -> fired.add("trigger"));

        dropdown.fire();

        assertEquals(List.of("trigger"), fired);
    }

    @Test
    void aDisabledDropdownDoesNothingWhenFired() {
        dropdown.setOnAction(e -> fired.add("trigger"));
        dropdown.setDisable(true);

        dropdown.fire();

        assertTrue(fired.isEmpty());
    }

    @Test
    void withoutAShownWindowTheMenuStaysClosedAndNothingThrows() {
        dropdown.show();
        assertFalse(dropdown.isShowing());

        dropdown.toggle();
        assertFalse(dropdown.isShowing());

        dropdown.hide();
        assertFalse(dropdown.isShowing());
    }

    @Test
    void theMenuListsTheItemsInOrder() {
        VBox menu = TestReflection.field(dropdown, "menu", VBox.class);

        assertEquals(List.copyOf(dropdown.getItems()), List.copyOf(menu.getChildren()));

        FxDropdownItem later = item("Later");
        dropdown.getItems().add(later);
        assertSame(later, menu.getChildren().get(menu.getChildren().size() - 1));
    }

    // ---- keyboard highlighting --------------------------------------------------------------

    @Test
    void downHighlightsTheFirstEnabledItemAndSkipsDisabledOnesAndDecoration() {
        press(KeyCode.DOWN);
        assertSame(apple, active());

        press(KeyCode.DOWN);
        assertSame(berry, active(), "banana is disabled, the divider is not an item");

        press(KeyCode.DOWN);
        assertSame(cherry, active());
    }

    @Test
    void downStopsAtTheLastItemInsteadOfWrapping() {
        press(KeyCode.END);

        press(KeyCode.DOWN);

        assertSame(cherry, active());
    }

    @Test
    void upFromNothingHighlightsTheLastItemAndStopsAtTheFirst() {
        press(KeyCode.UP);
        assertSame(cherry, active());

        press(KeyCode.UP);
        press(KeyCode.UP);
        assertSame(apple, active());

        press(KeyCode.UP);
        assertSame(apple, active());
    }

    @Test
    void homeAndEndJumpToTheFirstAndLastEnabledItems() {
        press(KeyCode.END);
        assertSame(cherry, active());

        press(KeyCode.HOME);
        assertSame(apple, active());
    }

    @Test
    void enterFiresTheHighlightedItem() {
        press(KeyCode.DOWN);

        press(KeyCode.ENTER);

        assertEquals(List.of("Apple"), fired);
    }

    @Test
    void spaceFiresTheHighlightedItemToo() {
        press(KeyCode.END);

        press(KeyCode.SPACE);

        assertEquals(List.of("Cherry"), fired);
    }

    @Test
    void enterWithNothingHighlightedFiresNothing() {
        press(KeyCode.ENTER);
        press(KeyCode.SPACE);

        assertTrue(fired.isEmpty());
    }

    @Test
    void aDisabledItemIsNeverHighlightedOrFired() {
        press(KeyCode.DOWN);
        press(KeyCode.DOWN);
        press(KeyCode.ENTER);

        assertFalse(isActive(banana));
        assertEquals(List.of("Berry"), fired);
    }

    @Test
    void tabClosesTheMenuWithoutHighlightingAnything() {
        press(KeyCode.TAB);

        assertFalse(dropdown.isShowing());
        assertNull(active());
    }

    @Test
    void justOneItemIsHighlightedAtATime() {
        press(KeyCode.DOWN);
        press(KeyCode.DOWN);
        press(KeyCode.UP);

        assertSame(apple, active());
        assertFalse(isActive(berry));
    }

    // ---- typeahead --------------------------------------------------------------------------

    @Test
    void typingALetterHighlightsTheFirstEnabledItemStartingWithIt() {
        type("b");

        assertSame(berry, active(), "Banana also starts with b but is disabled");
    }

    @Test
    void typeaheadIgnoresCase() {
        type("C");

        assertSame(cherry, active());
    }

    @Test
    void typingSeveralLettersQuicklyNarrowsTheMatch() {
        type("a");
        type("p");

        assertSame(apple, active());
    }

    @Test
    void aLetterThatMatchesNothingChangesNothing() {
        press(KeyCode.DOWN);

        type("z");

        assertSame(apple, active());
    }

    @Test
    void typeaheadCanBeSwitchedOff() {
        dropdown.setEnableTypeAhead(false);

        type("c");

        assertNull(active());
    }

    @Test
    void aSpaceInsideAWordIsNotTreatedAsActivate() {
        type("b");
        assertSame(berry, active());

        press(KeyCode.SPACE);
        assertTrue(fired.isEmpty(), "a space while typing a word must not fire the item");

        press(KeyCode.ENTER);
        assertEquals(List.of("Berry"), fired);
    }

    @Test
    void controlCharactersAreNotTypeahead() {
        type("\t");
        type("\r");

        assertNull(active());
    }

    // ---- mouse highlighting -----------------------------------------------------------------

    @Test
    void movingTheMouseOverAnItemHighlightsIt() {
        mouse(berry, MouseEvent.MOUSE_MOVED);

        assertSame(berry, active());

        mouse(cherry, MouseEvent.MOUSE_MOVED);
        assertSame(cherry, active());
    }

    @Test
    void movingOverADisabledItemClearsTheHighlight() {
        mouse(berry, MouseEvent.MOUSE_MOVED);

        mouse(banana, MouseEvent.MOUSE_MOVED);

        assertNull(active());
    }

    @Test
    void theMouseLeavingTheMenuClearsTheHighlight() {
        mouse(berry, MouseEvent.MOUSE_MOVED);
        assertSame(berry, active());

        mouse(TestReflection.field(dropdown, "menu", VBox.class), MouseEvent.MOUSE_EXITED);

        assertNull(active());
    }

    @Test
    void theMouseAndKeyboardShareOneHighlight() {
        mouse(cherry, MouseEvent.MOUSE_MOVED);

        press(KeyCode.UP);

        assertSame(berry, active());
    }
}
