package dev.fxkit.showcase;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.components.pagination.FxPagination;
import dev.fxkit.core.components.pagination.FxPagination.Layout;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
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
 * Isolated test harness for {@link FxPagination}: the seven Flowbite examples, a live playground,
 * the page-number window at its edges, table-layout counts, programmatic changes (out-of-range
 * values, shrinking totals, two paginations sharing one page), a data-driven list, and a disabled
 * control.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check light and dark. Tab through the controls to check the
 * focus ring and that a focused page button keeps focus after it is activated.
 */
public class PaginationTestApp extends Application {

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (onPageChange callbacks)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live. Page spinner accepts out-of-range values on purpose: "
                                + "the control should show the nearest valid page."),
                playground(),

                sectionHeading("Flowbite examples",
                        "The seven examples from the Flowbite React docs. Click around: each logs onPageChange."),
                flowbiteExamples(),

                sectionHeading("Page window",
                        "Current page +/- 2, clamped to the ends. Previous disabled on page 1, next on the last page."),
                windowGrid(),

                sectionHeading("Table layout counts",
                        "\"Showing x to y of z Entries\": last page is partial, empty list shows 0 to 0."),
                tableGrid(),

                sectionHeading("Labels and icons",
                        "Custom text, icon only (empty labels), and long labels."),
                labelGrid(),

                sectionHeading("Keyboard focus",
                        "Tab to a page number, press Space or Enter repeatedly: focus should stay on the same "
                                + "button while the window slides. The ring should be fully visible, even where "
                                + "neighbouring borders overlap."),
                keyboardSection(),

                sectionHeading("Programmatic changes",
                        "setCurrentPage(...) from code must NOT call onPageChange (nothing in the log). "
                                + "The property keeps the raw value, the display clamps it."),
                programmaticSection(),

                sectionHeading("Shrinking total",
                        "Lowering totalPages below currentPage must show the new last page without errors."),
                shrinkSection(),

                sectionHeading("Shared page",
                        "Two paginations with currentPage bound bidirectionally; either one moves the other."),
                sharedSection(),

                sectionHeading("Data-driven list",
                        "47 items, 5 per page. The list reacts to the currentPage property, so it also follows "
                                + "programmatic changes."),
                dataSection(),

                sectionHeading("Disabled",
                        "setDisable(true): every button dims and ignores clicks, active page stays highlighted."),
                disabledSection(),

                sectionHeading("Event log", "Everything onPageChange reports lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1000, 760);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxPagination test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxPagination test harness");
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
        FxPagination pagination = new FxPagination(1, 20);
        pagination.setItemsPerPage(10);
        pagination.setTotalItems(200);
        pagination.setOnPageChange(page -> log("playground: onPageChange(" + page + ")"));

        ComboBox<Layout> layoutBox = new ComboBox<>();
        layoutBox.getItems().addAll(Layout.values());
        layoutBox.valueProperty().bindBidirectional(pagination.layoutProperty());

        CheckBox iconsCheck = new CheckBox("Show icons");
        iconsCheck.selectedProperty().bindBidirectional(pagination.showIconsProperty());

        CheckBox disabledCheck = new CheckBox("Disabled");
        pagination.disableProperty().bind(disabledCheck.selectedProperty());

        TextField previousField = new TextField();
        previousField.textProperty().bindBidirectional(pagination.previousLabelProperty());
        TextField nextField = new TextField();
        nextField.textProperty().bindBidirectional(pagination.nextLabelProperty());
        previousField.setPrefColumnCount(8);
        nextField.setPrefColumnCount(8);

        HBox row1 = new HBox(16,
                new Label("Layout"), layoutBox, iconsCheck, disabledCheck,
                new Label("Previous"), previousField, new Label("Next"), nextField);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(16,
                new Label("currentPage"), spinner(pagination.currentPageProperty(), -5, 100_000),
                new Label("totalPages"), spinner(pagination.totalPagesProperty(), -5, 100_000),
                new Label("itemsPerPage"), spinner(pagination.itemsPerPageProperty(), -5, 1_000),
                new Label("totalItems"), spinner(pagination.totalItemsProperty(), -5, 1_000_000));
        row2.setAlignment(Pos.CENTER_LEFT);

        Label readout = new Label();
        readout.getStyleClass().addAll("text-sm", "text-muted");
        readout.textProperty().bind(Bindings.concat(
                "currentPage=", pagination.currentPageProperty(),
                "  totalPages=", pagination.totalPagesProperty(),
                "  itemsPerPage=", pagination.itemsPerPageProperty(),
                "  totalItems=", pagination.totalItemsProperty()));

        VBox box = new VBox(12, row1, row2, pagination, readout);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the seven Flowbite examples -----------------------------------------------------------

    private FlowPane flowbiteExamples() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("Default pagination",
                tracked("default", new FxPagination(1, 100))));

        FxPagination icons = new FxPagination(1, 100);
        icons.setShowIcons(true);
        flow.getChildren().add(labeled("Pagination with icons", tracked("icons", icons)));

        FxPagination navigation = new FxPagination(1, 100);
        navigation.setLayout(Layout.NAVIGATION);
        flow.getChildren().add(labeled("Previous and next", tracked("navigation", navigation)));

        FxPagination navigationIcons = new FxPagination(1, 100);
        navigationIcons.setLayout(Layout.NAVIGATION);
        navigationIcons.setShowIcons(true);
        flow.getChildren().add(labeled("Control button icons", tracked("navigation+icons", navigationIcons)));

        flow.getChildren().add(labeled("Table data navigation", tracked("table", table(1, 10, 100, false))));
        flow.getChildren().add(labeled("Table data navigation with icons",
                tracked("table+icons", table(1, 10, 100, true))));

        FxPagination text = new FxPagination(1, 1000);
        text.setPreviousLabel("Go back");
        text.setNextLabel("Go forward");
        text.setShowIcons(true);
        flow.getChildren().add(labeled("Control button text", tracked("text", text)));
        return flow;
    }

    // ---- page window ---------------------------------------------------------------------------

    private FlowPane windowGrid() {
        FlowPane flow = new FlowPane(12, 12);
        int[][] cases = {
                {1, 1}, {1, 2}, {2, 2}, {1, 5}, {3, 5}, {1, 100}, {2, 100},
                {3, 100}, {50, 100}, {99, 100}, {100, 100}, {54321, 100000}
        };
        for (int[] c : cases) {
            String caption = "page " + c[0] + " of " + c[1]
                    + (c[1] == 100000 ? " (5-digit numbers, buttons should grow)" : "");
            flow.getChildren().add(labeled(caption, tracked("window " + c[0] + "/" + c[1], new FxPagination(c[0], c[1]))));
        }
        return flow;
    }

    // ---- table layout --------------------------------------------------------------------------

    private FlowPane tableGrid() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("page 1, 10 per page, 95 items", tracked("t1", table(1, 10, 95, true))));
        flow.getChildren().add(labeled("page 10 (partial: 91 to 95)", tracked("t2", table(10, 10, 95, true))));
        flow.getChildren().add(labeled("page 10 of exactly 100 items", tracked("t3", table(10, 10, 100, true))));
        flow.getChildren().add(labeled("1 item", tracked("t4", table(1, 10, 1, true))));
        flow.getChildren().add(labeled("0 items (both buttons disabled)", tracked("t5", table(1, 10, 0, true))));
        flow.getChildren().add(labeled("page 99 of 100 items, 10 per page (clamps to 10)",
                tracked("t6", table(99, 10, 100, true))));
        return flow;
    }

    // ---- labels / icons ----------------------------------------------------------------------

    private FlowPane labelGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxPagination iconOnly = new FxPagination(3, 10);
        iconOnly.setShowIcons(true);
        iconOnly.setPreviousLabel("");
        iconOnly.setNextLabel("");
        flow.getChildren().add(labeled("icon only (empty labels)", tracked("icon-only", iconOnly)));

        FxPagination noIconsNoText = new FxPagination(3, 10);
        noIconsNoText.setPreviousLabel("<");
        noIconsNoText.setNextLabel(">");
        flow.getChildren().add(labeled("symbols, no icons", tracked("symbols", noIconsNoText)));

        FxPagination longLabels = new FxPagination(3, 10);
        longLabels.setPreviousLabel("Go to the previous page");
        longLabels.setNextLabel("Go to the next page");
        longLabels.setShowIcons(true);
        flow.getChildren().add(labeled("long labels", tracked("long", longLabels)));

        FxPagination nullLabels = new FxPagination(3, 10);
        nullLabels.setPreviousLabel(null);
        nullLabels.setNextLabel(null);
        flow.getChildren().add(labeled("null labels (should not throw)", tracked("null-labels", nullLabels)));
        return flow;
    }

    // ---- keyboard -------------------------------------------------------------------------------

    private VBox keyboardSection() {
        FxPagination pagination = new FxPagination(10, 100);
        pagination.setShowIcons(true);
        tracked("keyboard", pagination);

        TextField before = new TextField();
        before.setPromptText("Tab starts here");
        VBox box = new VBox(10, before, pagination);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- programmatic --------------------------------------------------------------------------

    private VBox programmaticSection() {
        FxPagination pagination = new FxPagination(1, 10);
        pagination.setShowIcons(true);
        tracked("programmatic", pagination);

        Label raw = new Label();
        raw.getStyleClass().addAll("text-sm", "text-muted");
        raw.textProperty().bind(Bindings.concat("currentPage property = ", pagination.currentPageProperty()));

        FlowPane buttons = new FlowPane(8, 8);
        for (int value : new int[] {1, 5, 10, 0, -5, 999}) {
            Button b = new Button("setCurrentPage(" + value + ")");
            b.setOnAction(e -> pagination.setCurrentPage(value));
            buttons.getChildren().add(b);
        }

        VBox box = new VBox(10, pagination, raw, buttons);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- shrinking total ---------------------------------------------------------------------

    private VBox shrinkSection() {
        FxPagination pagination = new FxPagination(15, 20);
        tracked("shrink", pagination);

        Button shrink = new Button("totalPages = 5");
        shrink.setOnAction(e -> pagination.setTotalPages(5));
        Button grow = new Button("totalPages = 20");
        grow.setOnAction(e -> pagination.setTotalPages(20));
        Button zero = new Button("totalPages = 0 (treated as 1)");
        zero.setOnAction(e -> pagination.setTotalPages(0));

        Button toTable = new Button("Layout -> TABLE (0 items)");
        toTable.setOnAction(e -> pagination.setLayout(Layout.TABLE));
        Button toPagination = new Button("Layout -> PAGINATION");
        toPagination.setOnAction(e -> pagination.setLayout(Layout.PAGINATION));

        VBox box = new VBox(10, pagination, new HBox(8, shrink, grow, zero), new HBox(8, toTable, toPagination));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- shared page -----------------------------------------------------------------------------

    private VBox sharedSection() {
        FxPagination numbers = new FxPagination(1, 10);
        numbers.setShowIcons(true);
        FxPagination table = table(1, 10, 100, true);
        table.currentPageProperty().bindBidirectional(numbers.currentPageProperty());
        tracked("shared numbers", numbers);
        tracked("shared table", table);

        VBox box = new VBox(12, numbers, table);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- data-driven ---------------------------------------------------------------------------

    private VBox dataSection() {
        final int perPage = 5;
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 47; i++) {
            items.add("Item " + i);
        }

        FxPagination pagination = table(1, perPage, items.size(), true);
        tracked("data", pagination);

        VBox list = new VBox(4);
        Runnable render = () -> {
            int page = Math.max(1, Math.min(pagination.getCurrentPage(), (items.size() + perPage - 1) / perPage));
            list.getChildren().clear();
            for (int i = (page - 1) * perPage; i < Math.min(page * perPage, items.size()); i++) {
                Label label = new Label(items.get(i));
                label.getStyleClass().add("text-body");
                list.getChildren().add(label);
            }
        };
        pagination.currentPageProperty().addListener((o, was, is) -> render.run());
        render.run();

        Button last = new Button("Jump to last page");
        last.setOnAction(e -> pagination.setCurrentPage(10));

        VBox box = new VBox(10, list, pagination, last);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- disabled ------------------------------------------------------------------------------------

    private FlowPane disabledSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxPagination numbers = new FxPagination(3, 10);
        numbers.setShowIcons(true);
        numbers.setDisable(true);
        flow.getChildren().add(labeled("pagination, disabled", tracked("disabled-pagination", numbers)));

        FxPagination table = table(2, 10, 100, true);
        table.setDisable(true);
        flow.getChildren().add(labeled("table, disabled", tracked("disabled-table", table)));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private static FxPagination table(int page, int perPage, int total, boolean icons) {
        FxPagination pagination = new FxPagination();
        pagination.setLayout(Layout.TABLE);
        pagination.setItemsPerPage(perPage);
        pagination.setTotalItems(total);
        pagination.setCurrentPage(page);
        pagination.setShowIcons(icons);
        return pagination;
    }

    /** Logs every user-initiated page change of {@code pagination} under {@code name}. */
    private FxPagination tracked(String name, FxPagination pagination) {
        pagination.setOnPageChange(page -> log(name + ": onPageChange(" + page + ")"));
        return pagination;
    }

    private static Spinner<Integer> spinner(IntegerProperty property, int min, int max) {
        Spinner<Integer> spinner = new Spinner<>(min, max, property.get());
        spinner.setEditable(true);
        spinner.setPrefWidth(110);
        spinner.valueProperty().addListener((o, was, is) -> {
            if (is != null) {
                property.set(is);
            }
        });
        property.addListener((o, was, is) -> {
            if (spinner.getValueFactory().getValue() != is.intValue()) {
                spinner.getValueFactory().setValue(is.intValue());
            }
        });
        return spinner;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        subtitleLabel.setWrapText(true);
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
