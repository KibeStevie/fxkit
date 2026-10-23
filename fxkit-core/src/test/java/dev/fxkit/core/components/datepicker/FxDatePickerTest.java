package dev.fxkit.core.components.datepicker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dev.fxkit.core.FxThreadExtension;
import dev.fxkit.core.TestNodes;
import dev.fxkit.core.TestTree;
import dev.fxkit.core.components.datepicker.FxDatePicker.View;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * FxDatePicker's input field (typing, formatting, limits) and its calendar card in all four views.
 * The card is only reachable without showing a popup in inline mode, so most tests use that; the popup
 * itself needs a visible window and is not covered. The picker is pinned to the US locale so month and
 * weekday names do not depend on the machine running the tests. Runs on the FX thread; skipped without
 * a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxDatePickerTest {

    private static final LocalDate MARCH_15 = LocalDate.of(2024, 3, 15); // March 1st 2024 was a Friday

    private FxDatePicker picker;

    @BeforeEach
    void createPicker() {
        picker = new FxDatePicker();
        picker.setLocale(Locale.US);
    }

    private FxDatePicker inline(LocalDate value) {
        picker.setInline(true);
        picker.setValue(value);
        return picker;
    }

    private TextField input() {
        return TestNodes.require(picker, "fxk-dp-input", TextField.class);
    }

    private static void commit(TextField field, String text) {
        field.setText(text);
        Event.fireEvent(field, new ActionEvent());
    }

    private String header() {
        return TestNodes.require(picker, "fxk-dp-selector-view", Button.class).getText();
    }

    private Button viewButton() {
        return TestNodes.require(picker, "fxk-dp-selector-view", Button.class);
    }

    private Button previous() {
        List<Button> selectors = TestTree.all(picker, "fxk-dp-selector", Button.class);
        return selectors.get(0);
    }

    private Button next() {
        List<Button> selectors = TestTree.all(picker, "fxk-dp-selector", Button.class);
        return selectors.get(selectors.size() - 1);
    }

    private List<Button> cells() {
        return TestTree.all(picker, "fxk-dp-item", Button.class);
    }

    private List<String> cellTexts() {
        return cells().stream().map(Button::getText).toList();
    }

    private Button cell(String text) {
        return cells().stream().filter(c -> c.getText().equals(text)).findFirst()
                .orElseThrow(() -> new AssertionError("no cell with text " + text));
    }

    private long count(String styleClass) {
        return TestTree.all(picker, styleClass, Button.class).size();
    }

    private long disabledCells() {
        return cells().stream().filter(Button::isDisabled).count();
    }

    // ---- defaults and the input field -------------------------------------------------------

    @Test
    void aNewPickerIsAnEmptyInputThatStretchesToItsContainer() {
        assertNull(picker.getValue());
        assertEquals(1, picker.getChildren().size());
        assertTrue(picker.getStyleClass().contains("fxk-datepicker"));
        assertFalse(picker.getStyleClass().contains("fxk-datepicker-inline"));
        assertEquals(Double.MAX_VALUE, picker.getMaxWidth(), 0);
        assertEquals("", input().getText());
        assertEquals("Select date", input().getPromptText());
        assertTrue(input().isEditable());
        assertEquals(View.DAYS, picker.getView());
        assertEquals(DayOfWeek.SUNDAY, picker.getWeekStart());
        assertTrue(picker.isAutoHide());
        assertTrue(picker.isShowTodayButton());
        assertTrue(picker.isShowClearButton());
        assertEquals("Today", picker.getLabelTodayButton());
        assertEquals("Clear", picker.getLabelClearButton());
        assertNull(picker.getMinDate());
        assertNull(picker.getMaxDate());
        assertNull(picker.getFilterDate());
    }

    @Test
    void theCalendarIconIsDecorationThatDoesNotCatchClicks() {
        SVGPath icon = TestNodes.require(picker, "fxk-dp-icon", SVGPath.class);

        assertTrue(icon.isMouseTransparent());
    }

    @Test
    void promptTextAndEditableAreHandedToTheInput() {
        picker.setPromptText("Pick a day");
        picker.setEditable(false);

        assertEquals("Pick a day", input().getPromptText());
        assertFalse(input().isEditable());
    }

    @Test
    void theValueConstructorStartsWithThatDateShownInTheInput() {
        FxDatePicker withValue = new FxDatePicker(MARCH_15);

        assertEquals(MARCH_15, withValue.getValue());
        String expected = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                .withLocale(Locale.getDefault()).format(MARCH_15);
        assertEquals(expected, TestNodes.require(withValue, "fxk-dp-input", TextField.class).getText());
    }

    // ---- text formatting --------------------------------------------------------------------

    @Test
    void theValueIsShownWithTheLongLocalizedFormatByDefault() {
        picker.setValue(MARCH_15);

        String expected = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.US).format(MARCH_15);
        assertEquals(expected, input().getText());
    }

    @Test
    void aCustomFormatterIsUsedForShowingTheValue() {
        picker.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        picker.setValue(MARCH_15);

        assertEquals("2024-03-15", input().getText());
    }

    @Test
    void changingTheFormatterOrLocaleUpdatesTheText() {
        picker.setValue(MARCH_15);
        String before = input().getText();

        picker.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        assertEquals("2024-03-15", input().getText());

        picker.setDateFormatter(null);
        picker.setLocale(Locale.GERMANY);
        assertFalse(input().getText().equals(before), "German long format differs from US long format");
    }

    @Test
    void clearingTheValueClearsTheText() {
        picker.setValue(MARCH_15);

        picker.setValue(null);

        assertEquals("", input().getText());
    }

    // ---- typing a date ----------------------------------------------------------------------

    @Test
    void aTypedDateIsCommittedOnEnter() {
        picker.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);

        commit(input(), "2024-05-01");

        assertEquals(LocalDate.of(2024, 5, 1), picker.getValue());
        assertEquals("2024-05-01", input().getText());
    }

    @Test
    void anEmptiedInputClearsTheValue() {
        picker.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        picker.setValue(MARCH_15);

        commit(input(), "   ");

        assertNull(picker.getValue());
    }

    @Test
    void textThatIsNotADateKeepsTheValueAndRestoresTheText() {
        picker.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        picker.setValue(MARCH_15);

        commit(input(), "garbage");

        assertEquals(MARCH_15, picker.getValue());
        assertEquals("2024-03-15", input().getText());
    }

    @Test
    void aTypedDateOutsideTheLimitsIsRejected() {
        picker.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        picker.setMinDate(LocalDate.of(2024, 4, 1));

        commit(input(), "2024-03-01");

        assertNull(picker.getValue());
        assertEquals("", input().getText());
    }

    // ---- limits and filter ------------------------------------------------------------------

    @Test
    void aDateIsSelectableUnlessALimitOrTheFilterSaysNo() {
        assertFalse(picker.isDateSelectable(null));
        assertTrue(picker.isDateSelectable(MARCH_15));

        picker.setMinDate(LocalDate.of(2024, 3, 10));
        picker.setMaxDate(LocalDate.of(2024, 3, 20));
        assertFalse(picker.isDateSelectable(LocalDate.of(2024, 3, 9)));
        assertTrue(picker.isDateSelectable(LocalDate.of(2024, 3, 10)), "the minimum itself is allowed");
        assertTrue(picker.isDateSelectable(LocalDate.of(2024, 3, 20)), "the maximum itself is allowed");
        assertFalse(picker.isDateSelectable(LocalDate.of(2024, 3, 21)));

        picker.setMinDate(null);
        picker.setMaxDate(null);
        picker.setFilterDate((date, view) -> date.getDayOfWeek() != DayOfWeek.SATURDAY);
        assertFalse(picker.isDateSelectable(LocalDate.of(2024, 3, 16))); // a Saturday
        assertTrue(picker.isDateSelectable(MARCH_15)); // a Friday
    }

    // ---- inline versus popup ----------------------------------------------------------------

    @Test
    void inlineShowsTheCalendarCardInsteadOfTheInput() {
        picker.setInline(true);

        assertEquals(1, picker.getChildren().size());
        assertTrue(picker.getChildren().get(0).getStyleClass().contains("fxk-dp-card"));
        assertTrue(picker.getStyleClass().contains("fxk-datepicker-inline"));
    }

    @Test
    void goingBackFromInlineShowsTheInputAgain() {
        picker.setInline(true);

        picker.setInline(false);

        assertTrue(TestNodes.has(picker, "fxk-dp-input"));
        assertFalse(TestNodes.has(picker, "fxk-dp-card"), "the card now lives in the popup");
        assertFalse(picker.getStyleClass().contains("fxk-datepicker-inline"));
    }

    @Test
    void withoutAShownWindowThePopupNeverOpensAndNothingThrows() {
        picker.show();
        assertFalse(picker.isShowing());
        assertNotNull(picker.showingProperty());

        picker.hide();
        assertFalse(picker.isShowing());
    }

    @Test
    void showDoesNothingInInlineMode() {
        inline(MARCH_15).show();

        assertFalse(picker.isShowing());
    }

    // ---- the days view ----------------------------------------------------------------------

    @Test
    void theDaysViewHasSixWeeksStartingOnTheWeekStartDay() {
        inline(MARCH_15);

        assertEquals("March 2024", header());
        assertEquals(42, cells().size());
        assertEquals("25", cells().get(0).getText(), "Sunday February 25th starts the grid");
        assertEquals("1", cells().get(5).getText(), "Friday March 1st is the sixth cell");
    }

    @Test
    void weekdayNamesStartOnTheWeekStartDay() {
        inline(MARCH_15);
        assertEquals(List.of("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"), weekdayNames());

        picker.setWeekStart(DayOfWeek.MONDAY);
        assertEquals(List.of("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"), weekdayNames());
        assertEquals("26", cells().get(0).getText(), "Monday February 26th starts the grid");
    }

    private List<String> weekdayNames() {
        return TestTree.all(picker, "fxk-dp-weekday", Label.class).stream().map(Label::getText).toList();
    }

    @Test
    void aNullWeekStartMeansSunday() {
        inline(MARCH_15);

        picker.setWeekStart(null);

        assertEquals("25", cells().get(0).getText());
    }

    @Test
    void daysOutsideTheMonthAreMarkedAndTheSelectedDayIsMarked() {
        inline(MARCH_15);

        assertEquals(11, count("fxk-dp-item-outside"), "five February days and six April days");
        List<Button> selected = TestTree.all(picker, "fxk-dp-item-selected", Button.class);
        assertEquals(1, selected.size());
        assertEquals("15", selected.get(0).getText());
    }

    @Test
    void aFebruaryInALeapYearHasTwentyNineDaysInTheGrid() {
        inline(LocalDate.of(2024, 2, 10));

        assertEquals("February 2024", header());
        assertEquals(13, count("fxk-dp-item-outside"), "42 cells minus 29 days");
    }

    @Test
    void todayIsMarkedWhenItsMonthIsShown() {
        LocalDate today = LocalDate.now();
        inline(today);

        List<Button> marked = TestTree.all(picker, "fxk-dp-item-today", Button.class);
        assertEquals(1, marked.size());
        assertEquals(Integer.toString(today.getDayOfMonth()), marked.get(0).getText());
    }

    @Test
    void clickingADaySelectsIt() {
        inline(MARCH_15);

        cell("20").fire();

        assertEquals(LocalDate.of(2024, 3, 20), picker.getValue());
        assertEquals("20", TestTree.all(picker, "fxk-dp-item-selected", Button.class).get(0).getText());
    }

    @Test
    void clickingADayOfTheNeighbouringMonthSelectsItAndShowsThatMonth() {
        inline(MARCH_15);

        cells().get(0).fire(); // the grey 25 of February

        assertEquals(LocalDate.of(2024, 2, 25), picker.getValue());
        assertEquals("February 2024", header());
    }

    @Test
    void daysBeforeTheMinimumAndAfterTheMaximumAreDisabled() {
        inline(MARCH_15);

        picker.setMinDate(LocalDate.of(2024, 3, 10));
        assertEquals(14, disabledCells(), "February 25th to March 9th");

        picker.setMaxDate(LocalDate.of(2024, 3, 20));
        assertEquals(31, disabledCells(), "everything except March 10th to 20th");
    }

    @Test
    void aDisabledDayCannotBeClicked() {
        inline(MARCH_15);
        picker.setMinDate(LocalDate.of(2024, 3, 10));

        cells().get(0).fire();

        assertEquals(MARCH_15, picker.getValue());
    }

    @Test
    void theFilterDisablesDaysAndSeesTheViewItIsAskedAbout() {
        inline(MARCH_15);
        Set<View> seen = EnumSet.noneOf(View.class);
        picker.setFilterDate((date, view) -> {
            seen.add(view);
            return date.getDayOfWeek() != DayOfWeek.SUNDAY;
        });

        assertEquals(6, disabledCells(), "six Sundays in the 42-day grid");
        assertTrue(seen.contains(View.DAYS));

        picker.setView(View.MONTHS);
        assertTrue(seen.contains(View.MONTHS));
        picker.setView(View.YEARS);
        assertTrue(seen.contains(View.YEARS));
        picker.setView(View.DECADES);
        assertTrue(seen.contains(View.DECADES));
    }

    // ---- title and footer -------------------------------------------------------------------

    @Test
    void theTitleIsHiddenUntilSet() {
        inline(MARCH_15);
        Label title = TestNodes.require(picker, "fxk-dp-title", Label.class);
        assertFalse(title.isVisible());
        assertFalse(title.isManaged());

        picker.setTitle("Pick a date");
        assertTrue(title.isVisible());
        assertTrue(title.isManaged());
        assertEquals("Pick a date", title.getText());

        picker.setTitle("  ");
        assertFalse(title.isVisible());
    }

    @Test
    void theFooterHasTodayAndClearButtonsThatCanBeRelabelledAndHidden() {
        inline(MARCH_15);
        Button today = TestNodes.require(picker, "fxk-dp-today", Button.class);
        Button clear = TestNodes.require(picker, "fxk-dp-clear", Button.class);
        VBox card = TestNodes.require(picker, "fxk-dp-card", VBox.class);
        assertEquals("Today", today.getText());
        assertEquals("Clear", clear.getText());

        picker.setLabelTodayButton("Hoy");
        picker.setLabelClearButton("Borrar");
        assertEquals("Hoy", today.getText());
        assertEquals("Borrar", clear.getText());

        picker.setShowTodayButton(false);
        assertFalse(today.isVisible());
        assertFalse(today.isManaged());
        assertTrue(TestNodes.require(card, "fxk-dp-footer", javafx.scene.layout.HBox.class).isVisible(),
                "the footer stays while one button is left");

        picker.setShowClearButton(false);
        assertFalse(TestNodes.require(card, "fxk-dp-footer", javafx.scene.layout.HBox.class).isVisible());
    }

    @Test
    void theTodayButtonSelectsTodayAndReturnsToTheDaysView() {
        inline(MARCH_15);
        picker.setView(View.YEARS);

        TestNodes.require(picker, "fxk-dp-today", Button.class).fire();

        assertEquals(LocalDate.now(), picker.getValue());
        assertEquals(View.DAYS, picker.getView());
    }

    @Test
    void theTodayButtonIsDisabledWhenTodayCannotBePicked() {
        inline(MARCH_15);

        picker.setMaxDate(LocalDate.now().minusDays(1));

        Button today = TestNodes.require(picker, "fxk-dp-today", Button.class);
        assertTrue(today.isDisabled());
        today.fire();
        assertEquals(MARCH_15, picker.getValue());
    }

    @Test
    void theClearButtonClearsTheValue() {
        inline(MARCH_15);

        TestNodes.require(picker, "fxk-dp-clear", Button.class).fire();

        assertNull(picker.getValue());
        assertTrue(TestTree.all(picker, "fxk-dp-item-selected", Button.class).isEmpty());
    }

    // ---- navigation -------------------------------------------------------------------------

    @Test
    void thePreviousAndNextButtonsStepOneMonthInTheDaysView() {
        inline(MARCH_15);

        next().fire();
        assertEquals("April 2024", header());

        previous().fire();
        previous().fire();
        assertEquals("February 2024", header());
    }

    @Test
    void steppingPastDecemberRollsIntoTheNextYear() {
        inline(LocalDate.of(2024, 12, 5));

        next().fire();

        assertEquals("January 2025", header());
    }

    @Test
    void theArrowButtonsHaveAccessibleNames() {
        inline(MARCH_15);

        assertEquals("Previous", previous().getAccessibleText());
        assertEquals("Next", next().getAccessibleText());
    }

    @Test
    void clickingTheHeaderDrillsOutThroughMonthsYearsAndDecades() {
        inline(MARCH_15);

        viewButton().fire();
        assertEquals(View.MONTHS, picker.getView());
        assertEquals("2024", header());

        viewButton().fire();
        assertEquals(View.YEARS, picker.getView());
        assertEquals("2020-2029", header());

        viewButton().fire();
        assertEquals(View.DECADES, picker.getView());
        assertEquals("2000-2090", header());
        assertTrue(viewButton().isDisabled(), "there is nothing above the decades");
    }

    @Test
    void theStepSizeGrowsWithTheView() {
        inline(MARCH_15);

        picker.setView(View.MONTHS);
        next().fire();
        assertEquals("2025", header());

        picker.setView(View.YEARS);
        assertEquals("2020-2029", header());
        next().fire();
        assertEquals("2030-2039", header());

        picker.setView(View.DECADES);
        next().fire();
        assertEquals("2100-2190", header());
    }

    @Test
    void aNullViewIsTreatedAsTheDaysView() {
        inline(MARCH_15);
        picker.setView(View.MONTHS);

        picker.setView(null);

        assertEquals("March 2024", header());
        assertEquals(42, cells().size());
    }

    // ---- months, years and decades views ----------------------------------------------------

    @Test
    void theMonthsViewHasTwelveShortMonthNamesAndMarksTheSelectedOne() {
        inline(MARCH_15);

        picker.setView(View.MONTHS);

        assertEquals(12, cells().size());
        assertEquals("Jan", cells().get(0).getText());
        assertEquals("Mar", cells().get(2).getText());
        assertEquals("Dec", cells().get(11).getText());
        List<Button> selected = TestTree.all(picker, "fxk-dp-item-selected", Button.class);
        assertEquals(1, selected.size());
        assertEquals("Mar", selected.get(0).getText());
    }

    @Test
    void pickingAMonthGoesBackToTheDaysOfThatMonth() {
        inline(MARCH_15);
        picker.setView(View.MONTHS);

        cell("Jun").fire();

        assertEquals(View.DAYS, picker.getView());
        assertEquals("June 2024", header());
        assertEquals(MARCH_15, picker.getValue(), "browsing does not change the selected date");
    }

    @Test
    void monthsOutsideTheLimitsAreDisabled() {
        inline(MARCH_15);
        picker.setMinDate(LocalDate.of(2024, 3, 10));
        picker.setMaxDate(LocalDate.of(2024, 3, 20));

        picker.setView(View.MONTHS);

        List<Button> months = cells();
        assertTrue(months.get(0).isDisabled(), "January");
        assertTrue(months.get(1).isDisabled(), "February");
        assertFalse(months.get(2).isDisabled(), "March overlaps the allowed range");
        assertTrue(months.get(3).isDisabled(), "April");
        assertEquals(11, disabledCells());
    }

    @Test
    void theYearsViewShowsTwelveYearsWithTheNeighbouringDecadeFadedOut() {
        inline(MARCH_15);

        picker.setView(View.YEARS);

        assertEquals(12, cells().size());
        assertEquals("2019", cells().get(0).getText());
        assertEquals("2030", cells().get(11).getText());
        assertTrue(cells().get(0).getStyleClass().contains("fxk-dp-item-outside"));
        assertTrue(cells().get(11).getStyleClass().contains("fxk-dp-item-outside"));
        assertEquals("2024", TestTree.all(picker, "fxk-dp-item-selected", Button.class).get(0).getText());
    }

    @Test
    void pickingAYearGoesToThatYearsMonths() {
        inline(MARCH_15);
        picker.setView(View.YEARS);

        cell("2026").fire();

        assertEquals(View.MONTHS, picker.getView());
        assertEquals("2026", header());
    }

    @Test
    void yearsOutsideTheLimitsAreDisabled() {
        inline(MARCH_15);
        picker.setMinDate(LocalDate.of(2024, 3, 10));

        picker.setView(View.YEARS);

        for (int i = 0; i < 5; i++) {
            assertTrue(cells().get(i).isDisabled(), cells().get(i).getText());
        }
        assertFalse(cell("2024").isDisabled());
    }

    @Test
    void theDecadesViewShowsTwelveDecadesAroundTheCentury() {
        inline(MARCH_15);

        picker.setView(View.DECADES);

        assertEquals(12, cells().size());
        assertEquals("1990", cells().get(0).getText());
        assertEquals("2100", cells().get(11).getText());
        assertTrue(cells().get(0).getStyleClass().contains("fxk-dp-item-outside"));
        assertEquals("2020", TestTree.all(picker, "fxk-dp-item-selected", Button.class).get(0).getText());
    }

    @Test
    void pickingADecadeGoesToThatDecadesYears() {
        inline(MARCH_15);
        picker.setView(View.DECADES);

        cell("2010").fire();

        assertEquals(View.YEARS, picker.getView());
        assertEquals("2010-2019", header());
    }

    // ---- locale -----------------------------------------------------------------------------

    @Test
    void theHeaderUsesTheLocalesMonthName() {
        inline(MARCH_15);

        picker.setLocale(Locale.GERMANY);

        assertEquals("M\u00E4rz 2024", header());
    }

    @Test
    void aNullLocaleFallsBackToTheDefaultWithoutThrowing() {
        inline(MARCH_15);

        picker.setLocale(null);

        assertFalse(header().isBlank());
        assertNull(picker.getLocale());
    }

    @Test
    void theMonthNamesInTheMonthsViewFollowTheLocale() {
        inline(MARCH_15);
        picker.setView(View.MONTHS);
        List<String> english = new ArrayList<>(cellTexts());

        picker.setLocale(Locale.GERMANY);

        assertFalse(english.equals(cellTexts()), "German abbreviations differ from English ones");
        assertEquals(12, cells().size());
    }
}
