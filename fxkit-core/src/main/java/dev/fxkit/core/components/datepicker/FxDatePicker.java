package dev.fxkit.core.components.datepicker;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.BiPredicate;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.theme.ThemeManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;

/**
 * A date picker, modelled on Flowbite React's {@code <Datepicker>}: a text
 * input with a calendar icon
 * that opens a calendar popup with day, month, year and decade views.
 *
 * <pre>{@code
 * FxDatePicker picker = new FxDatePicker();
 * picker.valueProperty().addListener((o, was, is) -> System.out.println(is));
 *
 * // Filter dates: weekdays only (the filter also sees MONTHS / YEARS / DECADES
 * // lookups)
 * picker.setFilterDate((date, view) -> view != FxDatePicker.View.DAYS || date.getDayOfWeek().getValue() <= 5);
 *
 * // Limit the date
 * picker.setMinDate(LocalDate.of(2023, 1, 1));
 * picker.setMaxDate(LocalDate.of(2023, 4, 30));
 *
 * picker.setWeekStart(DayOfWeek.MONDAY);
 * picker.setAutoHide(false);
 * picker.setTitle("Flowbite Datepicker");
 * picker.setInline(true); // calendar only, no input
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <table>
 * <caption>React prop to FxDatePicker property</caption>
 * <tr>
 * <td>{@code value} / {@code onChange}</td>
 * <td>{@link #valueProperty()} (a {@link LocalDate}, {@code null} = empty)</td>
 * </tr>
 * <tr>
 * <td>{@code filterDate}</td>
 * <td>{@link #filterDateProperty()}: {@code (date, view) -> boolean}</td>
 * </tr>
 * <tr>
 * <td>{@code minDate} / {@code maxDate}</td>
 * <td>{@link #minDateProperty()} / {@link #maxDateProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code weekStart} (0 = Sunday)</td>
 * <td>{@link #weekStartProperty()}, a {@link DayOfWeek}, default
 * {@code SUNDAY}</td>
 * </tr>
 * <tr>
 * <td>{@code autoHide}</td>
 * <td>{@link #autoHideProperty()}, default {@code true}</td>
 * </tr>
 * <tr>
 * <td>{@code title}</td>
 * <td>{@link #titleProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code inline}</td>
 * <td>{@link #inlineProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code language}</td>
 * <td>{@link #localeProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code labelTodayButton} / {@code labelClearButton}</td>
 * <td>{@link #labelTodayButtonProperty()} /
 * {@link #labelClearButtonProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code showTodayButton} / {@code showClearButton}</td>
 * <td>{@link #showTodayButtonProperty()} /
 * {@link #showClearButtonProperty()}</td>
 * </tr>
 * <tr>
 * <td>{@code Views} enum</td>
 * <td>{@link View}</td>
 * </tr>
 * </table>
 *
 * <h2>Behavior</h2>
 * <ul>
 * <li>The popup opens when the input is clicked, or with {@code Down} /
 * {@code F4}; {@code Escape} closes it.
 * The input is editable: type a date in the displayed format and press
 * {@code Enter} (a typed date that
 * is outside {@code minDate}/{@code maxDate} or rejected by the filter reverts
 * to the old value).</li>
 * <li>Choosing a month, year or decade drills down to the next view, as in
 * Flowbite. Days of the neighboring
 * months are shown muted and can be picked.</li>
 * <li>With no value, the calendar opens on today's month, or on the nearest
 * bound if today is outside
 * {@code minDate}..{@code maxDate}.</li>
 * <li>Setting {@link #valueProperty()} from code is not validated against the
 * limits or the filter, so an
 * application can show any date; only user input is.</li>
 * </ul>
 *
 * <h2>Structure</h2>
 * 
 * <pre>
 * .fxk-datepicker                   VBox: either the input box or, when inline, the card
 *   .fxk-dp-input-box               StackPane: .fxk-dp-input (TextField) + .fxk-dp-icon
 *   .fxk-dp-card                    the calendar (also the content of the popup)
 *     .fxk-dp-title
 *     .fxk-dp-selectors             .fxk-dp-selector x3 (previous, view button, next)
 *     .fxk-dp-view                  .fxk-dp-weekdays + .fxk-dp-grid of .fxk-dp-item buttons
 *     .fxk-dp-footer                .fxk-dp-today, .fxk-dp-clear
 * </pre>
 * 
 * The popup is its own window, so {@code show()} copies the owner scene's
 * stylesheets onto it and applies
 * the owner's {@code Theme}, which is how the card follows light and dark. See
 * the FxDatePicker block in
 * {@code components.css}.
 */
public class FxDatePicker extends VBox {

    /** The calendar views, as in Flowbite's {@code Views} enum. */
    public enum View {
        DAYS, MONTHS, YEARS, DECADES
    }

    /**
     * Transparent room around the card in the popup window so its shadow is not
     * clipped.
     */
    private static final double SHADOW_PAD = 16;
    /** The popup's pt-2 gap between the input and the card. */
    private static final double POPUP_GAP = 8;

    /**
     * HiCalendar (20x20, even-odd), arcs written with spaced flags for JavaFX's
     * path parser.
     */
    private static final String CALENDAR_PATH = "M6 2 a1 1 0 0 0 -1 1 v1 H4 a2 2 0 0 0 -2 2 v10 a2 2 0 0 0 2 2 h12 a2 2 0 0 0 2 -2 V6 "
            + "a2 2 0 0 0 -2 -2 h-1 V3 a1 1 0 1 0 -2 0 v1 H7 V3 a1 1 0 0 0 -1 -1 z "
            + "M6 7 a1 1 0 0 0 0 2 h8 a1 1 0 1 0 0 -2 H6 z";
    private static final String ARROW_LEFT_PATH = "M13 8 H3 M7 4 L3 8 L7 12";
    private static final String ARROW_RIGHT_PATH = "M3 8 H13 M9 4 L13 8 L9 12";

    // ---- properties
    // ---------------------------------------------------------------------------

    private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(this, "value");
    private final ObjectProperty<LocalDate> minDate = new SimpleObjectProperty<>(this, "minDate");
    private final ObjectProperty<LocalDate> maxDate = new SimpleObjectProperty<>(this, "maxDate");
    private final ObjectProperty<BiPredicate<LocalDate, View>> filterDate = new SimpleObjectProperty<>(this,
            "filterDate");
    private final ObjectProperty<DayOfWeek> weekStart = new SimpleObjectProperty<>(this, "weekStart", DayOfWeek.SUNDAY);
    private final BooleanProperty autoHide = new SimpleBooleanProperty(this, "autoHide", true);
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final BooleanProperty inline = new SimpleBooleanProperty(this, "inline", false);
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "Select date");
    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);
    private final BooleanProperty showTodayButton = new SimpleBooleanProperty(this, "showTodayButton", true);
    private final BooleanProperty showClearButton = new SimpleBooleanProperty(this, "showClearButton", true);
    private final StringProperty labelTodayButton = new SimpleStringProperty(this, "labelTodayButton", "Today");
    private final StringProperty labelClearButton = new SimpleStringProperty(this, "labelClearButton", "Clear");
    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", Locale.getDefault());
    private final ObjectProperty<DateTimeFormatter> dateFormatter = new SimpleObjectProperty<>(this, "dateFormatter");
    private final ObjectProperty<View> view = new SimpleObjectProperty<>(this, "view", View.DAYS);

    // ---- nodes
    // --------------------------------------------------------------------------------

    private final TextField input = new TextField();
    private final SVGPath calendarIcon = new SVGPath();
    private final StackPane inputBox = new StackPane(input, calendarIcon);

    private final Label titleLabel = new Label();
    private final Button prevButton = arrowButton(ARROW_LEFT_PATH, "Previous");
    private final Button viewButton = new Button();
    private final Button nextButton = arrowButton(ARROW_RIGHT_PATH, "Next");
    private final HBox selectors;
    private final VBox body = new VBox();
    private final Button todayButton = new Button();
    private final Button clearButton = new Button();
    private final HBox footer = new HBox(todayButton, clearButton);
    private final VBox card;

    private final Popup popup = new Popup();
    private final StackPane popupHost = new StackPane();

    /**
     * First day of the month the calendar is anchored on; every view derives from
     * it.
     */
    private LocalDate anchor = LocalDate.now().withDayOfMonth(1);

    public FxDatePicker() {
        this(null);
    }

    /**
     * @param value the initial date, or {@code null} for an empty picker
     */
    public FxDatePicker(LocalDate value) {
        getStyleClass().add("fxk-datepicker");
        BooleanStyleClassSync.sync(this, "fxk-datepicker-inline", inline);
        setMaxWidth(Double.MAX_VALUE);

        buildInput();

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);
        selectors = new HBox(prevButton, spacerLeft, viewButton, spacerRight, nextButton);
        selectors.getStyleClass().add("fxk-dp-selectors");
        viewButton.getStyleClass().addAll("fxk-dp-selector", "fxk-dp-selector-view");
        viewButton.setMnemonicParsing(false);

        titleLabel.getStyleClass().add("fxk-dp-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        body.getStyleClass().add("fxk-dp-view");

        todayButton.getStyleClass().addAll("fxk-dp-footer-btn", "fxk-dp-today");
        clearButton.getStyleClass().addAll("fxk-dp-footer-btn", "fxk-dp-clear");
        todayButton.setMnemonicParsing(false);
        clearButton.setMnemonicParsing(false);
        todayButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(todayButton, Priority.ALWAYS);
        HBox.setHgrow(clearButton, Priority.ALWAYS);
        footer.getStyleClass().add("fxk-dp-footer");

        card = new VBox(titleLabel, selectors, body, footer);
        card.getStyleClass().add("fxk-dp-card");
        card.setMaxWidth(Region.USE_PREF_SIZE); // inline-block: the card hugs its content

        prevButton.setOnAction(e -> step(-1));
        nextButton.setOnAction(e -> step(1));
        viewButton.setOnAction(e -> drillOut());
        todayButton.setOnAction(e -> pickToday());
        clearButton.setOnAction(e -> select(null, true));

        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(true);
        popupHost.getStyleClass().add("fxk-dp-popup-host");
        popup.getContent().add(popupHost);

        wireListeners();

        resetAnchor();
        this.value.set(value);
        applyMode();
    }

    private void buildInput() {
        input.getStyleClass().add("fxk-dp-input");
        input.promptTextProperty().bind(promptText);
        input.editableProperty().bind(editable);

        calendarIcon.setContent(CALENDAR_PATH);
        calendarIcon.setFillRule(FillRule.EVEN_ODD);
        calendarIcon.getStyleClass().add("fxk-dp-icon");
        calendarIcon.setMouseTransparent(true);
        StackPane.setAlignment(calendarIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(calendarIcon, new Insets(0, 0, 0, 12)); // icon container's pl-3

        inputBox.getStyleClass().add("fxk-dp-input-box");

        input.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> show());
        input.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if ((e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.F4) && !popup.isShowing()) {
                show();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE && popup.isShowing()) {
                hide();
                e.consume();
            }
        });
        input.setOnAction(e -> {
            commitText();
            if (isAutoHide()) {
                hide();
            }
        });
        input.focusedProperty().addListener((o, was, is) -> {
            if (!is) {
                commitText();
            }
        });
    }

    private void wireListeners() {
        value.addListener((o, was, is) -> {
            LocalDate v = getValue();
            if (v != null) {
                setAnchor(v);
            }
            syncText();
            requestRebuild();
        });
        minDate.addListener((o, was, is) -> limitsChanged());
        maxDate.addListener((o, was, is) -> limitsChanged());
        filterDate.addListener((o, was, is) -> requestRebuild());
        weekStart.addListener((o, was, is) -> requestRebuild());
        title.addListener((o, was, is) -> requestRebuild());
        showTodayButton.addListener((o, was, is) -> requestRebuild());
        showClearButton.addListener((o, was, is) -> requestRebuild());
        labelTodayButton.addListener((o, was, is) -> requestRebuild());
        labelClearButton.addListener((o, was, is) -> requestRebuild());
        view.addListener((o, was, is) -> requestRebuild());
        locale.addListener((o, was, is) -> {
            syncText();
            requestRebuild();
        });
        dateFormatter.addListener((o, was, is) -> syncText());
        inline.addListener((o, was, is) -> applyMode());
        disabledProperty().addListener((o, was, is) -> {
            if (is) {
                hide();
            }
        });
        sceneProperty().addListener((o, was, is) -> {
            if (is == null) {
                hide();
            }
        });
    }

    // ---- public actions
    // -------------------------------------------------------------------------

    /**
     * Opens the calendar popup (does nothing when {@link #inlineProperty() inline},
     * disabled or not in a window).
     */
    public void show() {
        Scene owner = getScene();
        if (isInline() || isDisabled() || popup.isShowing() || owner == null || owner.getWindow() == null) {
            return;
        }
        resetAnchor();
        view.set(View.DAYS);
        rebuild();

        // The popup is a separate window: give it the owner's stylesheets and theme
        // (see class doc).
        Scene popupScene = popup.getScene();
        popupScene.getStylesheets().setAll(owner.getStylesheets());
        ThemeManager.apply(popupScene, ThemeManager.current(owner));

        Bounds bounds = input.localToScreen(input.getBoundsInLocal());
        if (bounds == null) {
            return;
        }
        popup.show(input, bounds.getMinX() - SHADOW_PAD, bounds.getMaxY() + POPUP_GAP - SHADOW_PAD);
    }

    /** Closes the calendar popup. */
    public void hide() {
        popup.hide();
    }

    public boolean isShowing() {
        return popup.isShowing();
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return popup.showingProperty();
    }

    /**
     * @return whether the user could pick {@code date} in the days view: inside
     *         {@code minDate}..{@code maxDate}
     *         and accepted by the filter
     */
    public boolean isDateSelectable(LocalDate date) {
        return date != null && selectable(date, date, View.DAYS);
    }

    // ---- mode: input + popup, or inline
    // -----------------------------------------------------------

    private void applyMode() {
        hide();
        if (isInline()) {
            popupHost.getChildren().clear();
            getChildren().setAll(card);
            rebuild();
        } else {
            getChildren().setAll(inputBox);
            popupHost.getChildren().setAll(card);
        }
    }

    private void limitsChanged() {
        if (getValue() == null) {
            resetAnchor();
        }
        requestRebuild();
    }

    private void requestRebuild() {
        if (isInline() || popup.isShowing()) {
            rebuild();
        }
    }

    // ---- text input
    // -----------------------------------------------------------------------------

    private DateTimeFormatter formatter() {
        DateTimeFormatter custom = getDateFormatter();
        return custom != null ? custom
                : DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(effectiveLocale());
    }

    private Locale effectiveLocale() {
        Locale l = getLocale();
        return l != null ? l : Locale.getDefault();
    }

    private void syncText() {
        LocalDate v = getValue();
        input.setText(v == null ? "" : formatter().format(v));
    }

    /**
     * Parses what the user typed; anything invalid or not selectable reverts to the
     * current value.
     */
    private void commitText() {
        String text = input.getText();
        if (text == null || text.isBlank()) {
            setValue(null);
            return;
        }
        try {
            LocalDate parsed = LocalDate.parse(text.trim(), formatter());
            if (isDateSelectable(parsed)) {
                setValue(parsed);
            }
        } catch (DateTimeParseException ignored) {
            // fall through to the revert below
        }
        syncText();
    }

    // ---- selection and navigation
    // -------------------------------------------------------------------

    private void select(LocalDate date, boolean allowAutoHide) {
        setValue(date);
        if (date != null) {
            setAnchor(date);
        }
        requestRebuild();
        if (allowAutoHide && isAutoHide() && !isInline()) {
            hide();
        }
    }

    private void pickToday() {
        LocalDate today = LocalDate.now();
        if (isDateSelectable(today)) {
            view.set(View.DAYS);
            select(today, true);
        }
    }

    private void setAnchor(LocalDate date) {
        anchor = date.withDayOfMonth(1);
    }

    private void resetAnchor() {
        LocalDate base = getValue() != null ? getValue() : clamp(LocalDate.now());
        setAnchor(base);
    }

    private LocalDate clamp(LocalDate date) {
        LocalDate min = getMinDate();
        LocalDate max = getMaxDate();
        if (min != null && date.isBefore(min)) {
            return min;
        }
        if (max != null && date.isAfter(max)) {
            return max;
        }
        return date;
    }

    private void step(int direction) {
        switch (currentView()) {
            case DAYS -> anchor = anchor.plusMonths(direction);
            case MONTHS -> anchor = anchor.plusYears(direction);
            case YEARS -> anchor = anchor.plusYears(10L * direction);
            case DECADES -> anchor = anchor.plusYears(100L * direction);
        }
        rebuild();
    }

    private void drillOut() {
        switch (currentView()) {
            case DAYS -> view.set(View.MONTHS);
            case MONTHS -> view.set(View.YEARS);
            case YEARS -> view.set(View.DECADES);
            case DECADES -> {
                /* the widest view: nothing above it */ }
        }
    }

    private View currentView() {
        View v = getView();
        return v != null ? v : View.DAYS;
    }

    // ---- limits and filter
    // -----------------------------------------------------------------------

    /**
     * Whether a period (a day, month, year or decade) has any selectable part: it
     * must overlap
     * {@code minDate}..{@code maxDate}, and the filter gets the period's first
     * date.
     */
    private boolean selectable(LocalDate start, LocalDate end, View forView) {
        LocalDate min = getMinDate();
        LocalDate max = getMaxDate();
        if (min != null && end.isBefore(min)) {
            return false;
        }
        if (max != null && start.isAfter(max)) {
            return false;
        }
        BiPredicate<LocalDate, View> filter = getFilterDate();
        return filter == null || filter.test(start, forView);
    }

    // ---- building the calendar
    // ----------------------------------------------------------------------

    private void rebuild() {
        View v = currentView();

        String titleText = getTitle();
        boolean hasTitle = titleText != null && !titleText.isBlank();
        titleLabel.setText(titleText);
        reveal(titleLabel, hasTitle);

        viewButton.setText(headerText(v));
        viewButton.setDisable(v == View.DECADES);

        body.getChildren().clear();
        switch (v) {
            case DAYS -> buildDays();
            case MONTHS -> buildMonths();
            case YEARS -> buildYears();
            case DECADES -> buildDecades();
        }

        todayButton.setText(getLabelTodayButton());
        clearButton.setText(getLabelClearButton());
        todayButton.setDisable(!isDateSelectable(LocalDate.now()));
        reveal(todayButton, isShowTodayButton());
        reveal(clearButton, isShowClearButton());
        reveal(footer, isShowTodayButton() || isShowClearButton());
    }

    private String headerText(View v) {
        int year = anchor.getYear();
        return switch (v) {
            case DAYS -> anchor.getMonth().getDisplayName(TextStyle.FULL, effectiveLocale()) + " " + year;
            case MONTHS -> String.valueOf(year);
            case YEARS -> {
                int decade = Math.floorDiv(year, 10) * 10;
                yield decade + "-" + (decade + 9);
            }
            case DECADES -> {
                int century = Math.floorDiv(year, 100) * 100;
                yield century + "-" + (century + 90);
            }
        };
    }

    /** 7 weekday names over a 6 x 7 grid: always 42 cells, as in Flowbite. */
    private void buildDays() {
        DayOfWeek first = getWeekStart() != null ? getWeekStart() : DayOfWeek.SUNDAY;

        GridPane names = grid(7);
        names.getStyleClass().add("fxk-dp-weekdays");
        for (int i = 0; i < 7; i++) {
            Label name = new Label(shortWeekday(first.plus(i)));
            name.getStyleClass().add("fxk-dp-weekday");
            name.setMinWidth(0);
            name.setMaxWidth(Double.MAX_VALUE);
            name.setAlignment(Pos.CENTER);
            names.add(name, i, 0);
        }

        GridPane items = grid(7);
        LocalDate firstOfMonth = anchor.withDayOfMonth(1);
        int lead = Math.floorMod(firstOfMonth.getDayOfWeek().getValue() - first.getValue(), 7);
        LocalDate start = firstOfMonth.minusDays(lead);
        LocalDate today = LocalDate.now();
        LocalDate selected = getValue();

        for (int i = 0; i < 42; i++) {
            LocalDate date = start.plusDays(i);
            Button cell = item(Integer.toString(date.getDayOfMonth()));
            if (date.getMonthValue() != firstOfMonth.getMonthValue()) {
                cell.getStyleClass().add("fxk-dp-item-outside");
            }
            if (date.equals(today)) {
                cell.getStyleClass().add("fxk-dp-item-today");
            }
            if (date.equals(selected)) {
                cell.getStyleClass().add("fxk-dp-item-selected");
            }
            cell.setDisable(!selectable(date, date, View.DAYS));
            cell.setOnAction(e -> select(date, true));
            items.add(cell, i % 7, i / 7);
        }
        body.getChildren().addAll(names, items);
    }

    private void buildMonths() {
        GridPane items = grid(4);
        int year = anchor.getYear();
        LocalDate selected = getValue();
        for (int month = 1; month <= 12; month++) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = YearMonth.of(year, month).atEndOfMonth();
            Button cell = item(Month.of(month).getDisplayName(TextStyle.SHORT, effectiveLocale()));
            if (selected != null && selected.getYear() == year && selected.getMonthValue() == month) {
                cell.getStyleClass().add("fxk-dp-item-selected");
            }
            cell.setDisable(!selectable(start, end, View.MONTHS));
            cell.setOnAction(e -> {
                setAnchor(start);
                view.set(View.DAYS);
            });
            items.add(cell, (month - 1) % 4, (month - 1) / 4);
        }
        body.getChildren().add(items);
    }

    /** 12 cells: the decade's ten years, with the year before and after muted. */
    private void buildYears() {
        GridPane items = grid(4);
        int decade = Math.floorDiv(anchor.getYear(), 10) * 10;
        LocalDate selected = getValue();
        for (int i = 0; i < 12; i++) {
            int year = decade - 1 + i;
            Button cell = item(Integer.toString(year));
            if (i == 0 || i == 11) {
                cell.getStyleClass().add("fxk-dp-item-outside");
            }
            if (selected != null && selected.getYear() == year) {
                cell.getStyleClass().add("fxk-dp-item-selected");
            }
            cell.setDisable(!selectable(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), View.YEARS));
            cell.setOnAction(e -> {
                setAnchor(LocalDate.of(year, anchor.getMonthValue(), 1));
                view.set(View.MONTHS);
            });
            items.add(cell, i % 4, i / 4);
        }
        body.getChildren().add(items);
    }

    /**
     * 12 cells: the century's ten decades, with the decade before and after muted.
     */
    private void buildDecades() {
        GridPane items = grid(4);
        int century = Math.floorDiv(anchor.getYear(), 100) * 100;
        LocalDate selected = getValue();
        for (int i = 0; i < 12; i++) {
            int decade = century - 10 + i * 10;
            Button cell = item(Integer.toString(decade));
            if (i == 0 || i == 11) {
                cell.getStyleClass().add("fxk-dp-item-outside");
            }
            if (selected != null && Math.floorDiv(selected.getYear(), 10) * 10 == decade) {
                cell.getStyleClass().add("fxk-dp-item-selected");
            }
            cell.setDisable(!selectable(LocalDate.of(decade, 1, 1), LocalDate.of(decade + 9, 12, 31), View.DECADES));
            cell.setOnAction(e -> {
                setAnchor(LocalDate.of(decade, anchor.getMonthValue(), 1));
                view.set(View.YEARS);
            });
            items.add(cell, i % 4, i / 4);
        }
        body.getChildren().add(items);
    }

    // ---- node helpers
    // -----------------------------------------------------------------------------------

    private static GridPane grid(int columns) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("fxk-dp-grid");
        for (int i = 0; i < columns; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / columns);
            grid.getColumnConstraints().add(column);
        }
        return grid;
    }

    private static Button item(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("fxk-dp-item");
        button.setMnemonicParsing(false);
        button.setMinWidth(0);
        button.setMaxWidth(Double.MAX_VALUE);
        // Cells are reached by mouse; the input, selectors and footer stay in the tab
        // order.
        button.setFocusTraversable(false);
        return button;
    }

    private static Button arrowButton(String path, String accessibleText) {
        SVGPath arrow = new SVGPath();
        arrow.setContent(path);
        arrow.getStyleClass().add("fxk-dp-arrow");
        StackPane box = new StackPane(arrow);
        box.getStyleClass().add("fxk-dp-arrow-box");

        Button button = new Button();
        button.setGraphic(box);
        button.setAccessibleText(accessibleText);
        button.getStyleClass().add("fxk-dp-selector");
        return button;
    }

    /** Two letters at most ("Su", "Mo"), whatever the locale's SHORT form is. */
    private String shortWeekday(DayOfWeek day) {
        String name = day.getDisplayName(TextStyle.SHORT, effectiveLocale());
        int end = name.offsetByCodePoints(0, Math.min(2, name.codePointCount(0, name.length())));
        return name.substring(0, end);
    }

    private static void reveal(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // ---- property accessors
    // ---------------------------------------------------------------------------------

    /** The selected date, or {@code null}. */
    public ObjectProperty<LocalDate> valueProperty() {
        return value;
    }

    public LocalDate getValue() {
        return value.get();
    }

    public void setValue(LocalDate date) {
        value.set(date);
    }

    /** Earliest selectable date (inclusive), or {@code null} for no limit. */
    public ObjectProperty<LocalDate> minDateProperty() {
        return minDate;
    }

    public LocalDate getMinDate() {
        return minDate.get();
    }

    public void setMinDate(LocalDate date) {
        minDate.set(date);
    }

    /** Latest selectable date (inclusive), or {@code null} for no limit. */
    public ObjectProperty<LocalDate> maxDateProperty() {
        return maxDate;
    }

    public LocalDate getMaxDate() {
        return maxDate.get();
    }

    public void setMaxDate(LocalDate date) {
        maxDate.set(date);
    }

    /**
     * Decides which dates can be picked. Called with the day for {@link View#DAYS},
     * and with the first
     * date of the month, year or decade for the other views. Return {@code false}
     * to disable that cell.
     */
    public ObjectProperty<BiPredicate<LocalDate, View>> filterDateProperty() {
        return filterDate;
    }

    public BiPredicate<LocalDate, View> getFilterDate() {
        return filterDate.get();
    }

    public void setFilterDate(BiPredicate<LocalDate, View> filter) {
        filterDate.set(filter);
    }

    /**
     * First day of the week in the days view. Default {@code SUNDAY}, like
     * Flowbite.
     */
    public ObjectProperty<DayOfWeek> weekStartProperty() {
        return weekStart;
    }

    public DayOfWeek getWeekStart() {
        return weekStart.get();
    }

    public void setWeekStart(DayOfWeek day) {
        weekStart.set(day);
    }

    /**
     * Close the popup after a day, Today or Clear is chosen. Default {@code true}.
     */
    public BooleanProperty autoHideProperty() {
        return autoHide;
    }

    public boolean isAutoHide() {
        return autoHide.get();
    }

    public void setAutoHide(boolean value) {
        autoHide.set(value);
    }

    /**
     * A heading shown at the top of the calendar; {@code null} or blank for none.
     */
    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String text) {
        title.set(text);
    }

    /** Show the calendar directly, without the input and popup. */
    public BooleanProperty inlineProperty() {
        return inline;
    }

    public boolean isInline() {
        return inline.get();
    }

    public void setInline(boolean value) {
        inline.set(value);
    }

    /** Placeholder of the input. Default {@code "Select date"}. */
    public StringProperty promptTextProperty() {
        return promptText;
    }

    public String getPromptText() {
        return promptText.get();
    }

    public void setPromptText(String text) {
        promptText.set(text);
    }

    /** Whether a date can be typed into the input. Default {@code true}. */
    public BooleanProperty editableProperty() {
        return editable;
    }

    public boolean isEditable() {
        return editable.get();
    }

    public void setEditable(boolean value) {
        editable.set(value);
    }

    public BooleanProperty showTodayButtonProperty() {
        return showTodayButton;
    }

    public boolean isShowTodayButton() {
        return showTodayButton.get();
    }

    public void setShowTodayButton(boolean value) {
        showTodayButton.set(value);
    }

    public BooleanProperty showClearButtonProperty() {
        return showClearButton;
    }

    public boolean isShowClearButton() {
        return showClearButton.get();
    }

    public void setShowClearButton(boolean value) {
        showClearButton.set(value);
    }

    public StringProperty labelTodayButtonProperty() {
        return labelTodayButton;
    }

    public String getLabelTodayButton() {
        return labelTodayButton.get();
    }

    public void setLabelTodayButton(String text) {
        labelTodayButton.set(text);
    }

    public StringProperty labelClearButtonProperty() {
        return labelClearButton;
    }

    public String getLabelClearButton() {
        return labelClearButton.get();
    }

    public void setLabelClearButton(String text) {
        labelClearButton.set(text);
    }

    /**
     * Language of month and weekday names and of the default date format. Default:
     * the JVM's locale.
     */
    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public Locale getLocale() {
        return locale.get();
    }

    public void setLocale(Locale value) {
        locale.set(value);
    }

    /**
     * Format of the input text. {@code null} (default) means the locale's long
     * date, e.g. "October 1, 2026".
     */
    public ObjectProperty<DateTimeFormatter> dateFormatterProperty() {
        return dateFormatter;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter.get();
    }

    public void setDateFormatter(DateTimeFormatter formatter) {
        dateFormatter.set(formatter);
    }

    /**
     * The view being shown. Reset to {@link View#DAYS} every time the popup opens.
     */
    public ObjectProperty<View> viewProperty() {
        return view;
    }

    public View getView() {
        return view.get();
    }

    public void setView(View value) {
        view.set(value);
    }
}
