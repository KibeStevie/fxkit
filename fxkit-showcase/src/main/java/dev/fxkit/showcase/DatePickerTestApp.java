package dev.fxkit.showcase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiPredicate;

import dev.fxkit.core.components.datepicker.FxDatePicker;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxDatePicker}: a live playground where every property can be
 * flipped at runtime, plus focused sections for value, min/max limits, filterDate (all four
 * views), week start, locale, date format, inline mode, button labels/visibility, popup behavior
 * (autoHide, editable, show/hide, view) and edge cases.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check that the input, popup card and inline calendars restyle
 * correctly in light and dark (the popup is its own window, so open it AFTER toggling too, and
 * toggle while it is closed and open).
 */
public class DatePickerTestApp extends Application {

    private static final double INPUT_WIDTH = 280;
    private static final LocalDate TODAY = LocalDate.now();

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(6);
        log.setPromptText("Event log (value changes, show/hide, checks)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live. Min/Max use FxDatePickers themselves: pick a date, or Clear "
                                + "to remove the limit. Open the popup and watch it react in place."),
                playground(),

                sectionHeading("Value",
                        "Empty, preset, today, and a far-past date. Selected day is highlighted; reopening "
                                + "the popup lands on the selected month."),
                valueGrid(),

                sectionHeading("Min / max limits",
                        "Dates outside the range are disabled in every view. Typing an out-of-range date "
                                + "and pressing Enter reverts. Today is outside the first range (opens on nearest bound)."),
                limitsGrid(),

                sectionHeading("filterDate",
                        "(date, view) -> boolean. Called for days, and with the FIRST date of the "
                                + "month/year/decade in the other views. Drill into months/years to check."),
                filterGrid(),

                sectionHeading("Week start",
                        "Inline calendars so the weekday header is visible without opening a popup. "
                                + "Default is SUNDAY."),
                weekStartGrid(),

                sectionHeading("Locale",
                        "Month/weekday names and the default input format follow the locale."),
                localeGrid(),

                sectionHeading("Date format",
                        "dateFormatter formats the input text and parses what you type. null = locale long date."),
                formatGrid(),

                sectionHeading("Inline",
                        "Calendar only, no input and no popup. With title, with value, and with limits."),
                inlineGrid(),

                sectionHeading("Buttons and labels",
                        "Today / Clear visibility and custom labels. Today is disabled when it is not selectable."),
                buttonsGrid(),

                sectionHeading("Popup behavior",
                        "autoHide, editable, programmatic show()/hide(), and setView() while open."),
                behaviorSection(),

                sectionHeading("Programmatic value",
                        "setValue() is NOT validated against limits or filter (only user input is). "
                                + "isDateSelectable() reports what a user could pick."),
                programmaticSection(),

                sectionHeading("Edge cases",
                        "min == max, today outside range, min > max, filter rejecting everything, "
                                + "long title, narrow width, many pickers."),
                edgeCases(),

                sectionHeading("Event log", "Value changes and popup events land here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1100, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxDatePicker test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxDatePicker test harness");
        title.getStyleClass().addAll("text-xl", "font-bold", "text-body");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        themeToggle = new Button();
        themeToggle.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-md", "p-2", "font-semibold");
        themeToggle.setOnAction(e -> themeToggle.setText(toggleCaption(ThemeManager.toggle(scene))));

        HBox header = new HBox(12, title, spacer, themeToggle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().addAll("bg-surface", "p-4");
        return header;
    }

    private static String toggleCaption(Theme current) {
        return current == Theme.LIGHT ? "Switch to dark theme" : "Switch to light theme";
    }

    // ---- playground -------------------------------------------------------------------------

    private VBox playground() {
        FxDatePicker picker = new FxDatePicker();
        picker.setPrefWidth(INPUT_WIDTH);
        picker.setMaxWidth(INPUT_WIDTH);
        VBox pickerHolder = new VBox(picker);

        Label valueLabel = new Label("value: null");
        valueLabel.getStyleClass().addAll("text-sm", "text-muted");
        picker.valueProperty().addListener((o, was, is) -> {
            valueLabel.setText("value: " + is);
            log("playground: value " + was + " -> " + is);
        });

        // text
        TextField titleField = new TextField();
        titleField.setPromptText("title");
        picker.titleProperty().bind(titleField.textProperty());

        TextField promptField = new TextField("Select date");
        promptField.setPromptText("prompt text");
        picker.promptTextProperty().bind(promptField.textProperty());

        TextField todayLabel = new TextField("Today");
        picker.labelTodayButtonProperty().bind(todayLabel.textProperty());
        TextField clearLabel = new TextField("Clear");
        picker.labelClearButtonProperty().bind(clearLabel.textProperty());

        // limits (FxDatePickers dogfooding FxDatePicker)
        FxDatePicker minPicker = new FxDatePicker();
        minPicker.setPromptText("No min");
        minPicker.setPrefWidth(180);
        picker.minDateProperty().bind(minPicker.valueProperty());
        FxDatePicker maxPicker = new FxDatePicker();
        maxPicker.setPromptText("No max");
        maxPicker.setPrefWidth(180);
        picker.maxDateProperty().bind(maxPicker.valueProperty());

        // combos
        ComboBox<DayOfWeek> weekStartBox = new ComboBox<>();
        weekStartBox.getItems().addAll(DayOfWeek.values());
        weekStartBox.valueProperty().bindBidirectional(picker.weekStartProperty());

        ComboBox<Locale> localeBox = new ComboBox<>();
        localeBox.getItems().addAll(Locale.ENGLISH, Locale.US, Locale.UK, Locale.FRENCH, Locale.GERMAN,
                Locale.ITALIAN, Locale.JAPANESE, Locale.KOREAN, Locale.CHINESE,
                Locale.of("es"), Locale.of("pt", "BR"), Locale.of("ar"), Locale.of("sw"));
        localeBox.valueProperty().bindBidirectional(picker.localeProperty());

        ComboBox<FxDatePicker.View> viewBox = new ComboBox<>();
        viewBox.getItems().addAll(FxDatePicker.View.values());
        viewBox.valueProperty().bindBidirectional(picker.viewProperty());

        Map<String, BiPredicate<LocalDate, FxDatePicker.View>> filters = filterPresets();
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll(filters.keySet());
        filterBox.setValue("(none)");
        filterBox.valueProperty().addListener((o, was, is) -> picker.setFilterDate(filters.get(is)));

        Map<String, DateTimeFormatter> formats = formatPresets();
        ComboBox<String> formatBox = new ComboBox<>();
        formatBox.getItems().addAll(formats.keySet());
        formatBox.setValue("(default: locale long)");
        formatBox.valueProperty().addListener((o, was, is) -> picker.setDateFormatter(formats.get(is)));

        // flags
        CheckBox inlineCheck = new CheckBox("Inline");
        inlineCheck.selectedProperty().bindBidirectional(picker.inlineProperty());
        CheckBox autoHideCheck = new CheckBox("autoHide");
        autoHideCheck.selectedProperty().bindBidirectional(picker.autoHideProperty());
        CheckBox editableCheck = new CheckBox("Editable");
        editableCheck.selectedProperty().bindBidirectional(picker.editableProperty());
        CheckBox showTodayCheck = new CheckBox("Show Today");
        showTodayCheck.selectedProperty().bindBidirectional(picker.showTodayButtonProperty());
        CheckBox showClearCheck = new CheckBox("Show Clear");
        showClearCheck.selectedProperty().bindBidirectional(picker.showClearButtonProperty());

        // actions
        Button todayBtn = new Button("setValue(today)");
        todayBtn.setOnAction(e -> picker.setValue(LocalDate.now()));
        Button nullBtn = new Button("setValue(null)");
        nullBtn.setOnAction(e -> picker.setValue(null));
        Button showBtn = new Button("show()");
        showBtn.setOnAction(e -> picker.show());
        Button hideBtn = new Button("hide()");
        hideBtn.setOnAction(e -> picker.hide());

        HBox row1 = row(10, new Label("Title"), titleField, new Label("Prompt"), promptField,
                new Label("Today label"), todayLabel, new Label("Clear label"), clearLabel);
        HBox row2 = row(10, new Label("Min"), minPicker, new Label("Max"), maxPicker,
                new Label("Week start"), weekStartBox, new Label("Locale"), localeBox);
        HBox row3 = row(10, new Label("Filter"), filterBox, new Label("Format"), formatBox,
                new Label("View"), viewBox);
        HBox row4 = row(16, inlineCheck, autoHideCheck, editableCheck, showTodayCheck, showClearCheck);
        HBox row5 = row(10, todayBtn, nullBtn, showBtn, hideBtn);

        VBox box = new VBox(12, row1, row2, row3, row4, row5, pickerHolder, valueLabel);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private static Map<String, BiPredicate<LocalDate, FxDatePicker.View>> filterPresets() {
        Map<String, BiPredicate<LocalDate, FxDatePicker.View>> m = new LinkedHashMap<>();
        m.put("(none)", null);
        m.put("weekdays only", (d, v) -> v != FxDatePicker.View.DAYS || d.getDayOfWeek().getValue() <= 5);
        m.put("no weekdays (weekends only)",
                (d, v) -> v != FxDatePicker.View.DAYS || d.getDayOfWeek().getValue() >= 6);
        m.put("even days only", (d, v) -> v != FxDatePicker.View.DAYS || d.getDayOfMonth() % 2 == 0);
        m.put("no past dates", (d, v) -> !beforeToday(d, v));
        m.put("block current month (MONTHS view)",
                (d, v) -> !(v == FxDatePicker.View.MONTHS && d.getMonth() == TODAY.getMonth()
                        && d.getYear() == TODAY.getYear()));
        m.put("block even years (YEARS view)", (d, v) -> v != FxDatePicker.View.YEARS || d.getYear() % 2 != 0);
        m.put("reject everything", (d, v) -> false);
        return m;
    }

    private static Map<String, DateTimeFormatter> formatPresets() {
        Map<String, DateTimeFormatter> m = new LinkedHashMap<>();
        m.put("(default: locale long)", null);
        m.put("yyyy-MM-dd (ISO)", DateTimeFormatter.ISO_LOCAL_DATE);
        m.put("dd/MM/yyyy", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        m.put("MM/dd/yyyy", DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        m.put("d MMM yyyy", DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
        m.put("EEE, MMM d, yyyy", DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH));
        return m;
    }

    /** True for days before today; the MONTHS/YEARS/DECADES lookups only block fully-past periods. */
    private static boolean beforeToday(LocalDate d, FxDatePicker.View v) {
        return switch (v) {
            case DAYS -> d.isBefore(TODAY);
            case MONTHS -> d.plusMonths(1).isBefore(TODAY.withDayOfMonth(1).plusDays(1));
            case YEARS -> d.getYear() < TODAY.getYear();
            case DECADES -> d.getYear() + 9 < TODAY.getYear();
        };
    }

    // ---- value --------------------------------------------------------------------------------

    private FlowPane valueGrid() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("empty (prompt text shows)", tracked("empty", sized(new FxDatePicker()))));
        flow.getChildren().add(labeled("preset: today",
                tracked("preset today", sized(new FxDatePicker(LocalDate.now())))));
        flow.getChildren().add(labeled("preset: 2023-04-15",
                tracked("preset 2023-04-15", sized(new FxDatePicker(LocalDate.of(2023, 4, 15))))));
        flow.getChildren().add(labeled("preset: leap day 2024-02-29",
                tracked("leap day", sized(new FxDatePicker(LocalDate.of(2024, 2, 29))))));
        flow.getChildren().add(labeled("preset: far past 1987-12-31",
                tracked("1987", sized(new FxDatePicker(LocalDate.of(1987, 12, 31))))));

        FxDatePicker prompt = sized(new FxDatePicker());
        prompt.setPromptText("Pick your birthday...");
        flow.getChildren().add(labeled("custom prompt text", tracked("custom prompt", prompt)));
        return flow;
    }

    // ---- min / max ----------------------------------------------------------------------------

    private FlowPane limitsGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxDatePicker fixed = sized(new FxDatePicker());
        fixed.setMinDate(LocalDate.of(2023, 1, 1));
        fixed.setMaxDate(LocalDate.of(2023, 4, 30));
        flow.getChildren().add(labeled("2023-01-01 .. 2023-04-30 (today outside)", tracked("fixed range", fixed)));

        FxDatePicker minOnly = sized(new FxDatePicker());
        minOnly.setMinDate(TODAY);
        flow.getChildren().add(labeled("min = today (no past)", tracked("min today", minOnly)));

        FxDatePicker maxOnly = sized(new FxDatePicker());
        maxOnly.setMaxDate(TODAY);
        flow.getChildren().add(labeled("max = today (no future)", tracked("max today", maxOnly)));

        FxDatePicker window = sized(new FxDatePicker());
        window.setMinDate(TODAY.minusDays(7));
        window.setMaxDate(TODAY.plusDays(7));
        flow.getChildren().add(labeled("today -7d .. +7d", tracked("window", window)));

        FxDatePicker valueOutside = sized(new FxDatePicker(TODAY.plusYears(1)));
        valueOutside.setMinDate(TODAY.minusDays(7));
        valueOutside.setMaxDate(TODAY.plusDays(7));
        flow.getChildren().add(labeled("preset value outside the range (allowed from code)",
                tracked("value outside", valueOutside)));
        return flow;
    }

    // ---- filterDate ---------------------------------------------------------------------------

    private FlowPane filterGrid() {
        FlowPane flow = new FlowPane(12, 12);
        Map<String, BiPredicate<LocalDate, FxDatePicker.View>> presets = filterPresets();
        for (Map.Entry<String, BiPredicate<LocalDate, FxDatePicker.View>> e : presets.entrySet()) {
            if (e.getValue() == null || e.getKey().equals("reject everything")) {
                continue;
            }
            FxDatePicker p = sized(new FxDatePicker());
            p.setFilterDate(e.getValue());
            flow.getChildren().add(labeled(e.getKey(), tracked("filter: " + e.getKey(), p)));
        }

        FxDatePicker combo = sized(new FxDatePicker());
        combo.setMinDate(TODAY.minusMonths(2));
        combo.setMaxDate(TODAY.plusMonths(2));
        combo.setFilterDate((d, v) -> v != FxDatePicker.View.DAYS || d.getDayOfWeek().getValue() <= 5);
        flow.getChildren().add(labeled("weekdays only + min/max (+-2 months)", tracked("filter + limits", combo)));
        return flow;
    }

    // ---- week start -----------------------------------------------------------------------------

    private FlowPane weekStartGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (DayOfWeek start : new DayOfWeek[] { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.SATURDAY }) {
            FxDatePicker p = new FxDatePicker(TODAY);
            p.setInline(true);
            p.setWeekStart(start);
            p.setPrefWidth(300);
            flow.getChildren().add(labeled("weekStart = " + start, tracked("week start " + start, p)));
        }
        return flow;
    }

    // ---- locale ---------------------------------------------------------------------------------

    private FlowPane localeGrid() {
        FlowPane flow = new FlowPane(12, 12);
        Locale[] locales = { Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.JAPANESE,
                Locale.of("es"), Locale.of("pt", "BR"), Locale.of("ar"), Locale.of("sw") };
        for (Locale l : locales) {
            FxDatePicker p = sized(new FxDatePicker(TODAY));
            p.setLocale(l);
            flow.getChildren().add(labeled(l.toLanguageTag() + " (" + l.getDisplayLanguage(Locale.ENGLISH) + ")",
                    tracked("locale " + l.toLanguageTag(), p)));
        }

        FxDatePicker switching = sized(new FxDatePicker(TODAY));
        Button cycle = new Button("Cycle locale");
        int[] n = { 0 };
        cycle.setOnAction(e -> {
            n[0]++;
            switching.setLocale(locales[n[0] % locales.length]);
            log("locale -> " + switching.getLocale().toLanguageTag());
        });
        flow.getChildren().add(labeled("runtime locale change (input text + names update)",
                new VBox(6, tracked("locale cycle", switching), cycle)));
        return flow;
    }

    // ---- format -----------------------------------------------------------------------------------

    private FlowPane formatGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (Map.Entry<String, DateTimeFormatter> e : formatPresets().entrySet()) {
            FxDatePicker p = sized(new FxDatePicker(TODAY));
            p.setDateFormatter(e.getValue());
            flow.getChildren().add(labeled(e.getKey() + " - type a date in this format + Enter",
                    tracked("format " + e.getKey(), p)));
        }

        FxDatePicker swapping = sized(new FxDatePicker(TODAY));
        Button swap = new Button("Toggle ISO / default");
        swap.setOnAction(e -> swapping.setDateFormatter(
                swapping.getDateFormatter() == null ? DateTimeFormatter.ISO_LOCAL_DATE : null));
        flow.getChildren().add(labeled("runtime formatter swap",
                new VBox(6, tracked("format swap", swapping), swap)));
        return flow;
    }

    // ---- inline -----------------------------------------------------------------------------------

    private FlowPane inlineGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxDatePicker plain = inline(null, null);
        flow.getChildren().add(labeled("plain", tracked("inline plain", plain)));

        FxDatePicker titled = inline("Flowbite Datepicker", TODAY);
        flow.getChildren().add(labeled("title + value", tracked("inline titled", titled)));

        FxDatePicker limited = inline("Weekdays in range", null);
        limited.setMinDate(TODAY.minusDays(14));
        limited.setMaxDate(TODAY.plusDays(14));
        limited.setFilterDate((d, v) -> v != FxDatePicker.View.DAYS || d.getDayOfWeek().getValue() <= 5);
        flow.getChildren().add(labeled("limits + filter", tracked("inline limited", limited)));

        FxDatePicker noButtons = inline(null, TODAY);
        noButtons.setShowTodayButton(false);
        noButtons.setShowClearButton(false);
        flow.getChildren().add(labeled("no footer buttons", tracked("inline no buttons", noButtons)));

        FxDatePicker toggled = sized(new FxDatePicker(TODAY));
        Button toggle = new Button("Toggle inline");
        toggle.setOnAction(e -> toggled.setInline(!toggled.isInline()));
        flow.getChildren().add(labeled("runtime inline toggle", new VBox(6, tracked("inline toggle", toggled), toggle)));
        return flow;
    }

    private static FxDatePicker inline(String title, LocalDate value) {
        FxDatePicker p = new FxDatePicker(value);
        p.setInline(true);
        p.setTitle(title);
        p.setPrefWidth(300);
        return p;
    }

    // ---- buttons / labels -------------------------------------------------------------------------

    private FlowPane buttonsGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxDatePicker both = sized(new FxDatePicker(TODAY));
        flow.getChildren().add(labeled("Today + Clear (defaults)", tracked("buttons both", both)));

        FxDatePicker todayOnly = sized(new FxDatePicker(TODAY));
        todayOnly.setShowClearButton(false);
        flow.getChildren().add(labeled("Today only", tracked("today only", todayOnly)));

        FxDatePicker clearOnly = sized(new FxDatePicker(TODAY));
        clearOnly.setShowTodayButton(false);
        flow.getChildren().add(labeled("Clear only", tracked("clear only", clearOnly)));

        FxDatePicker none = sized(new FxDatePicker(TODAY));
        none.setShowTodayButton(false);
        none.setShowClearButton(false);
        flow.getChildren().add(labeled("no footer at all", tracked("no footer", none)));

        FxDatePicker custom = sized(new FxDatePicker(TODAY));
        custom.setLabelTodayButton("Hoy");
        custom.setLabelClearButton("Borrar");
        custom.setLocale(Locale.of("es"));
        flow.getChildren().add(labeled("custom labels (Hoy / Borrar)", tracked("custom labels", custom)));

        FxDatePicker disabledToday = sized(new FxDatePicker());
        disabledToday.setMinDate(TODAY.plusDays(3));
        flow.getChildren().add(labeled("Today not selectable (min = today+3)", tracked("today disabled", disabledToday)));
        return flow;
    }

    // ---- popup behavior -----------------------------------------------------------------------------

    private VBox behaviorSection() {
        FxDatePicker stay = sized(new FxDatePicker());
        stay.setAutoHide(false);
        FxDatePicker readOnly = sized(new FxDatePicker(TODAY));
        readOnly.setEditable(false);

        FxDatePicker driven = sized(new FxDatePicker(TODAY));
        driven.setTitle("Driven from code");

        Button show = new Button("show()");
        show.setOnAction(e -> {
            driven.show();
            log("driven.show() -> isShowing=" + driven.isShowing());
        });
        Button hide = new Button("hide()");
        hide.setOnAction(e -> {
            driven.hide();
            log("driven.hide() -> isShowing=" + driven.isShowing());
        });
        Button months = new Button("setView(MONTHS)");
        months.setOnAction(e -> driven.setView(FxDatePicker.View.MONTHS));
        Button years = new Button("setView(YEARS)");
        years.setOnAction(e -> driven.setView(FxDatePicker.View.YEARS));
        Button decades = new Button("setView(DECADES)");
        decades.setOnAction(e -> driven.setView(FxDatePicker.View.DECADES));
        Button days = new Button("setView(DAYS)");
        days.setOnAction(e -> driven.setView(FxDatePicker.View.DAYS));
        Button state = new Button("log isShowing / view");
        state.setOnAction(e -> log("driven: isShowing=" + driven.isShowing() + ", view=" + driven.getView()));
        driven.viewProperty().addListener((o, was, is) -> log("driven: view " + was + " -> " + is));

        VBox box = new VBox(12,
                row(24,
                        labeled("autoHide = false (stays open after a pick; click outside / Esc closes)",
                                tracked("autoHide false", stay)),
                        labeled("editable = false (click or Down/F4 still opens)",
                                tracked("not editable", readOnly))),
                new Label("Programmatic control (view resets to DAYS each time the popup opens):"),
                tracked("driven", driven),
                row(8, show, hide, days, months, years, decades, state));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- programmatic value -------------------------------------------------------------------------

    private VBox programmaticSection() {
        FxDatePicker picker = sized(new FxDatePicker());
        picker.setMinDate(TODAY.minusDays(7));
        picker.setMaxDate(TODAY.plusDays(7));
        picker.setFilterDate((d, v) -> v != FxDatePicker.View.DAYS || d.getDayOfWeek().getValue() <= 5);
        tracked("programmatic", picker);

        Button inRange = new Button("setValue(today) (selectable if weekday)");
        inRange.setOnAction(e -> picker.setValue(TODAY));
        Button outOfRange = new Button("setValue(today + 1 year) (out of range, allowed)");
        outOfRange.setOnAction(e -> picker.setValue(TODAY.plusYears(1)));
        Button weekend = new Button("setValue(next Saturday) (filtered, allowed)");
        weekend.setOnAction(e -> picker.setValue(TODAY.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))));
        Button clear = new Button("setValue(null)");
        clear.setOnAction(e -> picker.setValue(null));
        Button check = new Button("isDateSelectable(current value)");
        check.setOnAction(e -> log("isDateSelectable(" + picker.getValue() + ") = "
                + (picker.getValue() == null ? "n/a (null)" : picker.isDateSelectable(picker.getValue()))));

        Label hint = new Label("Range: today +-7 days, weekdays only.");
        hint.getStyleClass().addAll("text-sm", "text-muted");

        VBox box = new VBox(10, hint, picker, row(8, inRange, outOfRange, weekend, clear, check));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ---------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxDatePicker single = sized(new FxDatePicker());
        single.setMinDate(TODAY);
        single.setMaxDate(TODAY);
        flow.getChildren().add(labeled("min == max (only today pickable)", tracked("min==max", single)));

        FxDatePicker inverted = sized(new FxDatePicker());
        inverted.setMinDate(TODAY.plusDays(10));
        inverted.setMaxDate(TODAY.minusDays(10));
        flow.getChildren().add(labeled("min > max (nothing selectable, must not throw)", tracked("min>max", inverted)));

        FxDatePicker rejectAll = sized(new FxDatePicker());
        rejectAll.setFilterDate((d, v) -> false);
        flow.getChildren().add(labeled("filter rejects everything", tracked("reject all", rejectAll)));

        FxDatePicker futureOnly = sized(new FxDatePicker());
        futureOnly.setMinDate(LocalDate.of(2100, 1, 1));
        flow.getChildren().add(labeled("min far in the future (opens on nearest bound)", tracked("min 2100", futureOnly)));

        FxDatePicker longTitle = sized(new FxDatePicker());
        longTitle.setTitle("A deliberately long calendar title that should not break the card layout");
        flow.getChildren().add(labeled("long title", tracked("long title", longTitle)));

        FxDatePicker narrow = new FxDatePicker(TODAY);
        narrow.setPrefWidth(160);
        narrow.setMaxWidth(160);
        flow.getChildren().add(labeled("narrow width (160px)", tracked("narrow", narrow)));

        FxDatePicker year1 = sized(new FxDatePicker(LocalDate.of(1, 1, 1)));
        flow.getChildren().add(labeled("year 0001", tracked("year 1", year1)));

        FxDatePicker year9999 = sized(new FxDatePicker(LocalDate.of(9999, 12, 31)));
        flow.getChildren().add(labeled("year 9999 (next must not overflow)", tracked("year 9999", year9999)));

        FxDatePicker mirror = sized(new FxDatePicker());
        FxDatePicker twin = sized(new FxDatePicker());
        twin.valueProperty().bindBidirectional(mirror.valueProperty());
        flow.getChildren().add(labeled("two pickers, bidirectionally bound values",
                new VBox(6, mirror, twin)));
        return flow;
    }

    // ---- helpers ------------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    /** Logs every value change of {@code picker} under {@code name} and returns it. */
    private FxDatePicker tracked(String name, FxDatePicker picker) {
        picker.valueProperty().addListener((o, was, is) -> log(name + ": " + was + " -> " + is));
        return picker;
    }

    private static FxDatePicker sized(FxDatePicker picker) {
        picker.setPrefWidth(INPUT_WIDTH);
        picker.setMaxWidth(INPUT_WIDTH);
        return picker;
    }

    private static HBox row(double spacing, Node... nodes) {
        HBox row = new HBox(spacing, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox labeled(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        VBox wrapper = new VBox(4, label, content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
