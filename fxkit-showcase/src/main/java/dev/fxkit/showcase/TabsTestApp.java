package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.tabs.FxTabItem;
import dev.fxkit.core.components.tabs.FxTabs;
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
 * Isolated test harness for {@link FxTabs}: the Flowbite examples (default, underline, icons, pills,
 * full width), active-by-default and disabled tabs, a live playground where tabs can be added,
 * removed, retitled, re-iconed and disabled at runtime, selection from code, the change callback,
 * keyboard navigation, and edge cases (wrapping, empty, single tab, nesting, content swapping).
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check every variant in light and dark.
 */
public class TabsTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final double WIDTH = 620;

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (tab changes)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: variant, add/remove tabs, retitle, toggle icons and disabled, "
                                + "select from code, swap content. Watch the strip rebuild in place."),
                playground(),

                sectionHeading("Default tabs",
                        "Gray-100 background on the active tab, primary text, hover tint. The Disabled tab is "
                                + "dimmed and ignores clicks."),
                labeled("variant = DEFAULT", flowbite(FxTabs.Variant.DEFAULT, true, "Default tabs")),

                sectionHeading("Tabs with underline",
                        "2px primary line under the active tab, drawn over the strip's 1px border."),
                labeled("variant = UNDERLINE", flowbite(FxTabs.Variant.UNDERLINE, true, "Tabs with underline")),

                sectionHeading("Tabs with icons", "Icon sits before the title, 20px, same color as the text."),
                iconsGrid(),

                sectionHeading("Tabs with pills", "Rounded pills, filled primary when active, no strip border."),
                labeled("variant = PILLS", flowbite(FxTabs.Variant.PILLS, false, "Pills")),

                sectionHeading("Full width tabs",
                        "Tabs share the whole width, 1px dividers, shadow. Resize the window to check it stretches."),
                fullWidthSection(),

                sectionHeading("Active by default and disabled",
                        "setActive(true) before adding makes a tab the first one shown. Disabled tabs are skipped "
                                + "by the arrow keys; selecting one from code still works."),
                activeAndDisabled(),

                sectionHeading("Selection from code and callback",
                        "setActiveTab(i), setSelectedItem(item), setActiveTab(-1) (hides the panel). "
                                + "onActiveTabChange reports the new index."),
                selectionSection(),

                sectionHeading("Keyboard",
                        "Tab into the strip (one tab is reachable), Left/Right move focus between enabled tabs "
                                + "without wrapping, Home/End jump, Space/Enter selects."),
                keyboardSection(),

                sectionHeading("Edge cases",
                        "Wrapping in a narrow container, one tab, no tabs, icon-only and text-only tabs, long "
                                + "titles, nested tabs, content that changes while active."),
                edgeCases(),

                sectionHeading("Event log", "Everything the callbacks report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1000, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxTabs test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxTabs test harness");
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
        FxTabs tabs = sized(new FxTabs());
        int[] counter = { 0 };
        for (int i = 0; i < 3; i++) {
            counter[0]++;
            tabs.getItems().add(new FxTabItem("Tab " + counter[0], ICON, text("Content of tab " + counter[0])));
        }
        FxTabItem disabled = new FxTabItem("Disabled", text("Disabled content"));
        disabled.setDisabled(true);
        tabs.getItems().add(disabled);
        tabs.setOnActiveTabChange(i -> log("playground: active tab -> " + i));

        ComboBox<FxTabs.Variant> variantBox = new ComboBox<>();
        variantBox.getItems().addAll(FxTabs.Variant.values());
        variantBox.valueProperty().bindBidirectional(tabs.variantProperty());

        Label state = new Label();
        state.getStyleClass().addAll("text-sm", "text-muted");
        Runnable refreshState = () -> {
            FxTabItem sel = tabs.getSelectedItem();
            state.setText("activeTab = " + tabs.getActiveTab() + ", selected = "
                    + (sel == null ? "null" : sel.getTitle()) + ", tabs = " + tabs.getItems().size());
        };
        tabs.activeTabProperty().addListener((o, was, is) -> refreshState.run());
        tabs.getItems().addListener((javafx.collections.ListChangeListener<FxTabItem>) c -> refreshState.run());
        refreshState.run();

        Button add = new Button("Add tab");
        add.setOnAction(e -> {
            counter[0]++;
            tabs.getItems().add(new FxTabItem("Tab " + counter[0], text("Content of tab " + counter[0])));
        });
        Button addActive = new Button("Add tab with active = true");
        addActive.setOnAction(e -> {
            counter[0]++;
            FxTabItem item = new FxTabItem("Active " + counter[0], text("Added as the active tab"));
            item.setActive(true);
            tabs.getItems().add(item);
        });
        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!tabs.getItems().isEmpty()) {
                tabs.getItems().remove(tabs.getItems().size() - 1);
            }
        });
        Button removeSelected = new Button("Remove selected");
        removeSelected.setOnAction(e -> {
            if (tabs.getSelectedItem() != null) {
                tabs.getItems().remove(tabs.getSelectedItem());
            }
        });
        Button clear = new Button("Clear all");
        clear.setOnAction(e -> tabs.getItems().clear());
        Button reverse = new Button("Reverse order");
        reverse.setOnAction(e -> javafx.collections.FXCollections.reverse(tabs.getItems()));

        Button next = new Button("Select next");
        next.setOnAction(e -> {
            int n = tabs.getItems().size();
            if (n > 0) {
                tabs.setActiveTab((tabs.getActiveTab() + 1) % n);
            }
        });
        Button none = new Button("setActiveTab(-1)");
        none.setOnAction(e -> tabs.setActiveTab(-1));

        TextField retitleField = new TextField("Renamed");
        Button retitle = new Button("Retitle selected");
        retitle.setOnAction(e -> {
            if (tabs.getSelectedItem() != null) {
                tabs.getSelectedItem().setTitle(retitleField.getText());
            }
        });
        Button toggleIcons = new Button("Toggle icons on all");
        toggleIcons.setOnAction(e -> {
            for (FxTabItem item : tabs.getItems()) {
                item.setIcon(item.getIcon() == null ? ICON : null);
            }
        });
        Button toggleDisabled = new Button("Toggle disabled on selected");
        toggleDisabled.setOnAction(e -> {
            FxTabItem sel = tabs.getSelectedItem();
            if (sel != null) {
                sel.setDisabled(!sel.isDisabled());
            }
        });
        Button swap = new Button("Swap content of selected");
        swap.setOnAction(e -> {
            FxTabItem sel = tabs.getSelectedItem();
            if (sel != null) {
                sel.setContent(text("Swapped content #" + System.nanoTime() % 1000));
            }
        });

        VBox box = new VBox(12,
                row(10, new Label("Variant"), variantBox, state),
                row(8, add, addActive, removeLast, removeSelected, clear, reverse),
                row(8, next, none, retitleField, retitle),
                row(8, toggleIcons, toggleDisabled, swap),
                tabs);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the Flowbite examples ----------------------------------------------------------------------

    /** Profile / Dashboard / Settings / Contacts / Disabled, as in the Flowbite docs. */
    private FxTabs flowbite(FxTabs.Variant variant, boolean icons, String accessibleText) {
        FxTabs tabs = sized(new FxTabs(variant));
        tabs.setAccessibleText(accessibleText);
        String[] titles = icons
                ? new String[] { "Profile", "Dashboard", "Settings", "Contacts" }
                : new String[] { "Tab 1", "Tab 2", "Tab 3", "Tab 4" };
        for (int i = 0; i < titles.length; i++) {
            FxTabItem item = new FxTabItem(titles[i], icons ? ICON : null, associated(titles[i]));
            tabs.getItems().add(item);
        }
        tabs.getItems().get(0).setActive(true);
        FxTabItem disabled = new FxTabItem(icons ? "Disabled" : "Tab 5", text("Disabled content"));
        disabled.setDisabled(true);
        tabs.getItems().add(disabled);
        tabs.setOnActiveTabChange(i -> log(accessibleText + ": active tab -> " + i));
        return tabs;
    }

    private FlowPane iconsGrid() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("UNDERLINE + icons", flowbite(FxTabs.Variant.UNDERLINE, true, "Tabs with icons")));
        flow.getChildren().add(labeled("DEFAULT + icons", flowbite(FxTabs.Variant.DEFAULT, true, "Default icons")));
        flow.getChildren().add(labeled("PILLS + icons", flowbite(FxTabs.Variant.PILLS, true, "Pills icons")));
        return flow;
    }

    private VBox fullWidthSection() {
        FxTabs tabs = new FxTabs(FxTabs.Variant.FULL_WIDTH);
        tabs.setAccessibleText("Full width tabs");
        String[] titles = { "Profile", "Dashboard", "Settings", "Contacts" };
        for (String title : titles) {
            tabs.getItems().add(new FxTabItem(title, ICON, associated(title)));
        }
        FxTabItem disabled = new FxTabItem("Disabled", text("Disabled content"));
        disabled.setDisabled(true);
        tabs.getItems().add(disabled);
        tabs.setOnActiveTabChange(i -> log("full width: active tab -> " + i));

        VBox box = new VBox(10, tabs);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- active / disabled -----------------------------------------------------------------------------

    private FlowPane activeAndDisabled() {
        FlowPane flow = new FlowPane(12, 12);

        FxTabs third = sized(new FxTabs(FxTabs.Variant.UNDERLINE));
        third.getItems().add(new FxTabItem("First", text("First content")));
        third.getItems().add(new FxTabItem("Second", text("Second content")));
        FxTabItem active = new FxTabItem("Third (active)", text("Third content, active before adding"));
        active.setActive(true);
        third.getItems().add(active);
        flow.getChildren().add(labeled("third tab has active = true before adding", third));

        FxTabs twoActive = sized(new FxTabs(FxTabs.Variant.PILLS));
        FxTabItem a = new FxTabItem("A (active)", text("A"));
        FxTabItem b = new FxTabItem("B (active)", text("B: the first active item wins, B's flag is cleared"));
        a.setActive(true);
        b.setActive(true);
        twoActive.getItems().addAll(b, a);
        flow.getChildren().add(labeled("two items flagged active: the first one added wins", twoActive));

        FxTabs firstDisabled = sized(new FxTabs(FxTabs.Variant.DEFAULT));
        FxTabItem d1 = new FxTabItem("Disabled first", text("Shown first, like Flowbite (index 0)"));
        d1.setDisabled(true);
        firstDisabled.getItems().addAll(d1, new FxTabItem("Enabled", text("Enabled content")));
        flow.getChildren().add(labeled("first tab disabled (still shown first)", firstDisabled));

        FxTabs allDisabled = sized(new FxTabs(FxTabs.Variant.PILLS));
        for (int i = 1; i <= 3; i++) {
            FxTabItem item = new FxTabItem("Tab " + i, text("Content " + i));
            item.setDisabled(true);
            allDisabled.getItems().add(item);
        }
        flow.getChildren().add(labeled("all disabled (nothing reachable with Tab)", allDisabled));
        return flow;
    }

    // ---- selection / callback -----------------------------------------------------------------------------

    private VBox selectionSection() {
        FxTabs tabs = sized(new FxTabs(FxTabs.Variant.UNDERLINE));
        FxTabItem[] items = new FxTabItem[4];
        for (int i = 0; i < items.length; i++) {
            items[i] = new FxTabItem("Tab " + (i + 1), text("Content " + (i + 1)));
            tabs.getItems().add(items[i]);
        }
        items[2].setDisabled(true);
        tabs.setOnActiveTabChange(i -> log("selection: active tab -> " + i));
        tabs.selectedItemProperty().addListener((o, was, is) -> log("selection: selectedItem "
                + (was == null ? "null" : was.getTitle()) + " -> " + (is == null ? "null" : is.getTitle())));

        Button first = new Button("setActiveTab(0)");
        first.setOnAction(e -> tabs.setActiveTab(0));
        Button third = new Button("setActiveTab(2) (disabled: allowed from code)");
        third.setOnAction(e -> tabs.setActiveTab(2));
        Button byItem = new Button("setSelectedItem(Tab 4)");
        byItem.setOnAction(e -> tabs.setSelectedItem(items[3]));
        Button viaFlag = new Button("Tab 2.setActive(true)");
        viaFlag.setOnAction(e -> items[1].setActive(true));
        Button clearFlag = new Button("selected.setActive(false) (stays active)");
        clearFlag.setOnAction(e -> {
            if (tabs.getSelectedItem() != null) {
                tabs.getSelectedItem().setActive(false);
                log("selection: active flag of selected after setActive(false) = " + tabs.getSelectedItem().isActive());
            }
        });
        Button bad = new Button("setActiveTab(9) (throws)");
        bad.setOnAction(e -> {
            try {
                tabs.setActiveTab(9);
            } catch (IndexOutOfBoundsException ex) {
                log("selection: IndexOutOfBoundsException, as documented");
            }
        });

        VBox box = new VBox(10, tabs, row(8, first, third, byItem), row(8, viaFlag, clearFlag, bad));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- keyboard ------------------------------------------------------------------------------------------

    private VBox keyboardSection() {
        FxTabs tabs = sized(new FxTabs(FxTabs.Variant.DEFAULT));
        for (int i = 1; i <= 5; i++) {
            FxTabItem item = new FxTabItem("Tab " + i, text("Content " + i));
            item.setDisabled(i == 3);
            tabs.getItems().add(item);
        }
        TextField before = new TextField();
        before.setPromptText("focus starts here, press Tab");
        TextField after = new TextField();
        after.setPromptText("Tab out of the strip lands here");

        VBox box = new VBox(10, before, tabs, after,
                new Label("Tab 3 is disabled: arrows skip it. The focus ring is the 2px outline."));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases -----------------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        // wrapping: a narrow container, the strip breaks onto several rows
        for (FxTabs.Variant v : new FxTabs.Variant[] { FxTabs.Variant.DEFAULT, FxTabs.Variant.UNDERLINE,
                FxTabs.Variant.PILLS }) {
            FxTabs wrapped = new FxTabs(v);
            wrapped.setPrefWidth(260);
            wrapped.setMaxWidth(260);
            for (String title : new String[] { "Profile", "Dashboard", "Settings", "Contacts", "Billing", "Team" }) {
                wrapped.getItems().add(new FxTabItem(title, text(title + " content")));
            }
            flow.getChildren().add(labeled(v + ": 260px wide, strip wraps", wrapped));
        }

        FxTabs full = new FxTabs(FxTabs.Variant.FULL_WIDTH);
        full.setPrefWidth(260);
        full.setMaxWidth(260);
        for (String title : new String[] { "One", "Two", "Three" }) {
            full.getItems().add(new FxTabItem(title, text(title + " content")));
        }
        flow.getChildren().add(labeled("FULL_WIDTH: 260px wide", full));

        FxTabs single = sized(new FxTabs(FxTabs.Variant.UNDERLINE));
        single.getItems().add(new FxTabItem("Only tab", text("A single tab")));
        flow.getChildren().add(labeled("a single tab", single));

        FxTabs empty = sized(new FxTabs(FxTabs.Variant.DEFAULT));
        flow.getChildren().add(labeled("no tabs (empty strip, no panel, must not throw)", empty));

        FxTabs iconOnly = sized(new FxTabs(FxTabs.Variant.PILLS));
        for (int i = 1; i <= 3; i++) {
            iconOnly.getItems().add(new FxTabItem(null, ICON, text("Icon-only tab " + i)));
        }
        iconOnly.getItems().add(new FxTabItem("Text only", text("Text-only tab")));
        flow.getChildren().add(labeled("icon-only (null title) and text-only", iconOnly));

        FxTabs longTitles = sized(new FxTabs(FxTabs.Variant.DEFAULT));
        longTitles.getItems().add(new FxTabItem("A deliberately very long tab title", ICON, text("Long")));
        longTitles.getItems().add(new FxTabItem("Another quite long title here", text("Long 2")));
        flow.getChildren().add(labeled("long titles", longTitles));

        FxTabs underscore = sized(new FxTabs(FxTabs.Variant.PILLS));
        underscore.getItems().add(new FxTabItem("_Underscore", text("No mnemonic: the _ is literal")));
        underscore.getItems().add(new FxTabItem("snake_case", text("snake_case")));
        flow.getChildren().add(labeled("underscores in titles are literal", underscore));

        // nested: an outer underline strip with a pills strip inside, they must not affect each other
        FxTabs inner = new FxTabs(FxTabs.Variant.PILLS);
        inner.getItems().add(new FxTabItem("Inner 1", text("Inner content 1")));
        inner.getItems().add(new FxTabItem("Inner 2", text("Inner content 2")));
        FxTabs outer = sized(new FxTabs(FxTabs.Variant.UNDERLINE));
        outer.getItems().add(new FxTabItem("Outer 1", inner));
        outer.getItems().add(new FxTabItem("Outer 2", text("Outer content 2")));
        flow.getChildren().add(labeled("nested: pills inside underline", outer));

        // state is kept: the field keeps its text while another tab is shown
        TextField field = new TextField();
        field.setPromptText("type here, switch tab, come back");
        FxTabs keeps = sized(new FxTabs(FxTabs.Variant.UNDERLINE));
        keeps.getItems().add(new FxTabItem("Form", field));
        keeps.getItems().add(new FxTabItem("Other", text("Switch back: the text field keeps its text and focus")));
        flow.getChildren().add(labeled("inactive content keeps its state", keeps));

        // content changes while active
        FxTabs live = sized(new FxTabs(FxTabs.Variant.DEFAULT));
        FxTabItem liveItem = new FxTabItem("Live", text("Original content"));
        live.getItems().add(liveItem);
        live.getItems().add(new FxTabItem("Other", text("Other")));
        Button swap = new Button("Swap content of the Live tab");
        swap.setOnAction(e -> liveItem.setContent(text("Swapped #" + System.nanoTime() % 1000)));
        CheckBox disable = new CheckBox("Live tab disabled");
        disable.selectedProperty().bindBidirectional(liveItem.disabledProperty());
        flow.getChildren().add(labeled("content/disabled changed at runtime", new VBox(6, live, row(8, swap, disable))));
        return flow;
    }

    // ---- helpers --------------------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxTabs sized(FxTabs tabs) {
        tabs.setPrefWidth(WIDTH);
        tabs.setMaxWidth(WIDTH);
        return tabs;
    }

    private static Label text(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().addAll("text-sm", "text-muted");
        return label;
    }

    /** The Flowbite docs' sample paragraph. */
    private static Label associated(String title) {
        return text("This is " + title + " tab's associated content. Clicking another tab will toggle the "
                + "visibility of this one for the next. The tab JavaScript swaps classes to control the "
                + "content visibility and styling.");
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
