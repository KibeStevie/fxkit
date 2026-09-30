package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.dropdown.FxDropdown;
import dev.fxkit.core.components.dropdown.FxDropdownDivider;
import dev.fxkit.core.components.dropdown.FxDropdownHeader;
import dev.fxkit.core.components.dropdown.FxDropdownItem;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
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
 * Isolated test harness for {@link FxDropdown}: one section per Flowbite
 * example (default, divider,
 * header, icons, inline, sizes, placement, click handler), a live playground
 * where every property can
 * be flipped at runtime, and edge cases (keyboard navigation, typeahead,
 * disabled items, long lists,
 * programmatic show/hide, text input in a header).
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle with a menu open/closed to check light and
 * dark: the menu is its own
 * window, so it is the part most likely to go wrong.
 */
public class DropdownTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (item clicks, open/close)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live. Also try the keyboard: Tab to the trigger, Down/Up to open, "
                                + "arrows/Home/End to move, Enter/Space to pick, type to search, Esc to close."),
                playground(),

                sectionHeading("Default dropdown",
                        "Flowbite example uses dismissOnClick = false: clicking an item must NOT close the menu."),
                labeled("dismissOnClick = false", defaultDropdown()),

                sectionHeading("Dropdown divider", "A 1px line between entries."),
                labeled("divider before the last item", dividerDropdown()),

                sectionHeading("Dropdown header",
                        "Two-line header with its own divider underneath. Nothing in it should highlight."),
                labeled("header + divider", headerDropdown()),

                sectionHeading("Dropdown items with icon", "16px icon, 8px gap before the label."),
                labeled("icons on every item", iconDropdown()),

                sectionHeading("Inline dropdown",
                        "Plain text with a chevron instead of a button. Menu opens BOTTOM_START by default."),
                labeled("inline in a sentence", inlineDropdown()),

                sectionHeading("Dropdown sizes", "XS to XL, same scale as FxButton. MD is the default."),
                labeled("all five sizes", sizes()),

                sectionHeading("Placement options",
                        "All 12 placements. Near a screen edge the menu flips to the opposite side: drag the "
                                + "window to the bottom/right of your screen and reopen."),
                labeled("placement", placements()),

                sectionHeading("Click event handler",
                        "Each item has its own setOnAction. The handler runs, then the menu closes."),
                labeled("onAction per item", clickDropdown()),

                sectionHeading("Keyboard and typeahead",
                        "Open with the mouse, then type 'ap' (Apple/Apricot), 'b', 'c'. Pause 750ms and the "
                                + "search resets. Disabled items are skipped by the arrows."),
                labeled("30 items, some disabled", longList()),

                sectionHeading("Programmatic control",
                        "show()/hide()/toggle() from code, and the read-only showing property."),
                programmaticSection(),

                sectionHeading("Edge cases",
                        "Disabled dropdown, empty dropdown, no arrow icon, text field in a header."),
                edgeCases(),

                sectionHeading("Event log", "Item clicks and open/close events land here."),
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

        stage.setTitle("FxDropdown test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxDropdown test harness");
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

    // ---- playground
    // -------------------------------------------------------------------------

    private VBox playground() {
        FxDropdown dropdown = new FxDropdown("Dropdown");
        dropdown.showingProperty().addListener((o, was, is) -> log("playground: " + (is ? "opened" : "closed")));

        TextField labelField = new TextField("Dropdown");
        labelField.setPromptText("label");
        dropdown.labelProperty().bind(labelField.textProperty());

        ComboBox<FxDropdown.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxDropdown.Size.values());
        sizeBox.valueProperty().bindBidirectional(dropdown.sizeProperty());

        ComboBox<FxDropdown.Placement> placementBox = new ComboBox<>();
        placementBox.getItems().addAll(FxDropdown.Placement.values());
        placementBox.setPromptText("default");
        placementBox.valueProperty().addListener((o, was, is) -> dropdown.setPlacement(is));
        Button resetPlacement = new Button("Default placement");
        resetPlacement.setOnAction(e -> {
            placementBox.setValue(null);
            dropdown.setPlacement(null);
        });

        CheckBox inline = new CheckBox("inline");
        dropdown.inlineProperty().bind(inline.selectedProperty());
        CheckBox dismiss = new CheckBox("dismissOnClick");
        dismiss.setSelected(true);
        dropdown.dismissOnClickProperty().bind(dismiss.selectedProperty());
        CheckBox typeAhead = new CheckBox("enableTypeAhead");
        typeAhead.setSelected(true);
        dropdown.enableTypeAheadProperty().bind(typeAhead.selectedProperty());
        CheckBox arrow = new CheckBox("arrowIcon");
        arrow.setSelected(true);
        dropdown.arrowIconProperty().bind(arrow.selectedProperty());
        CheckBox disabled = new CheckBox("disabled");
        dropdown.disableProperty().bind(disabled.selectedProperty());

        CheckBox header = new CheckBox("header");
        CheckBox icons = new CheckBox("icons");
        CheckBox divider = new CheckBox("divider");
        CheckBox disabledItem = new CheckBox("disable 'Earnings'");

        Runnable rebuild = () -> {
            dropdown.getItems().clear();
            if (header.isSelected()) {
                dropdown.getItems().add(new FxDropdownHeader("Bonnie Green", "bonnie@flowbite.com"));
            }
            String[] names = { "Dashboard", "Settings", "Earnings", "Sign out" };
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                if (divider.isSelected() && i == names.length - 1) {
                    dropdown.getItems().add(new FxDropdownDivider());
                }
                FxDropdownItem item = new FxDropdownItem(name, icons.isSelected() ? ICON : null);
                item.setDisable(disabledItem.isSelected() && name.equals("Earnings"));
                item.setOnAction(e -> log("playground: clicked " + name));
                dropdown.getItems().add(item);
            }
        };
        for (CheckBox box : new CheckBox[] { header, icons, divider, disabledItem }) {
            box.selectedProperty().addListener((o, was, is) -> rebuild.run());
        }
        rebuild.run();

        Label status = new Label();
        status.getStyleClass().addAll("text-sm", "text-muted");
        status.textProperty().bind(
                Bindings.format("showing = %s", dropdown.showingProperty()));

        HBox row1 = new HBox(10, new Label("Label"), labelField, new Label("Size"), sizeBox,
                new Label("Placement"), placementBox, resetPlacement);
        row1.setAlignment(Pos.CENTER_LEFT);
        FlowPane row2 = new FlowPane(16, 8, inline, dismiss, typeAhead, arrow, disabled);
        FlowPane row3 = new FlowPane(16, 8, header, icons, divider, disabledItem);

        HBox stage = new HBox(16, dropdown, status);
        stage.setAlignment(Pos.CENTER_LEFT);
        stage.setMinHeight(60);

        VBox box = new VBox(12, row1, row2, row3, stage);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- Flowbite examples
    // ----------------------------------------------------------------------

    private FxDropdown defaultDropdown() {
        FxDropdown dropdown = basic(new FxDropdown("Dropdown button"), "default");
        dropdown.setDismissOnClick(false);
        return dropdown;
    }

    private FxDropdown dividerDropdown() {
        FxDropdown dropdown = new FxDropdown("Dropdown button");
        dropdown.getItems().addAll(
                logged("divider", "Dashboard", null),
                logged("divider", "Settings", null),
                logged("divider", "Earnings", null),
                new FxDropdownDivider(),
                logged("divider", "Separated link", null));
        return dropdown;
    }

    private FxDropdown headerDropdown() {
        FxDropdown dropdown = new FxDropdown("Dropdown button");
        dropdown.getItems().addAll(
                new FxDropdownHeader("Bonnie Green", "bonnie@flowbite.com"),
                logged("header", "Dashboard", null),
                logged("header", "Settings", null),
                logged("header", "Earnings", null),
                new FxDropdownDivider(),
                logged("header", "Sign out", null));
        return dropdown;
    }

    private FxDropdown iconDropdown() {
        FxDropdown dropdown = new FxDropdown("Dropdown");
        dropdown.getItems().addAll(
                new FxDropdownHeader("Bonnie Green", "bonnie@flowbite.com"),
                logged("icons", "Dashboard", ICON),
                logged("icons", "Settings", ICON),
                logged("icons", "Earnings", ICON),
                new FxDropdownDivider(),
                logged("icons", "Sign out", ICON));
        return dropdown;
    }

    private HBox inlineDropdown() {
        FxDropdown dropdown = basic(new FxDropdown("Dropdown"), "inline");
        dropdown.setInline(true);
        Label before = new Label("Text before the dropdown");
        Label after = new Label("and text after it.");
        before.getStyleClass().add("text-body");
        after.getStyleClass().add("text-body");
        HBox row = new HBox(8, before, dropdown, after);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox sizes() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        for (FxDropdown.Size size : FxDropdown.Size.values()) {
            FxDropdown dropdown = basic(new FxDropdown(size.name()), "size " + size);
            dropdown.setSize(size);
            row.getChildren().add(dropdown);
        }
        return row;
    }

    private FlowPane placements() {
        FlowPane flow = new FlowPane(16, 16);
        for (FxDropdown.Placement placement : FxDropdown.Placement.values()) {
            FxDropdown dropdown = basic(new FxDropdown(placement.name()), "placement " + placement);
            dropdown.setPlacement(placement);
            flow.getChildren().add(dropdown);
        }
        return flow;
    }

    private FxDropdown clickDropdown() {
        FxDropdown dropdown = new FxDropdown("Dropdown");
        for (String name : new String[] { "Dashboard", "Settings", "Earnings", "Sign out" }) {
            FxDropdownItem item = new FxDropdownItem(name);
            item.setOnAction(e -> log("onAction: " + name + "!"));
            dropdown.getItems().add(item);
        }
        return dropdown;
    }

    // ---- keyboard / typeahead
    // -----------------------------------------------------------------------

    private FxDropdown longList() {
        FxDropdown dropdown = new FxDropdown("Fruit (30)");
        String[] fruit = {
                "Apple", "Apricot", "Avocado", "Banana", "Blackberry", "Blueberry", "Cherry", "Clementine",
                "Coconut", "Cranberry", "Date", "Dragon fruit", "Elderberry", "Fig", "Grape", "Grapefruit",
                "Guava", "Kiwi", "Lemon", "Lime", "Lychee", "Mango", "Melon", "Nectarine", "Orange",
                "Papaya", "Peach", "Pear", "Plum", "Raspberry" };
        for (String name : fruit) {
            FxDropdownItem item = new FxDropdownItem(name);
            item.setDisable(name.equals("Apricot") || name.equals("Fig") || name.equals("Lime"));
            item.setOnAction(e -> log("long list: picked " + name));
            dropdown.getItems().add(item);
        }
        return dropdown;
    }

    // ---- programmatic
    // ---------------------------------------------------------------------------------

    private VBox programmaticSection() {
        FxDropdown target = basic(new FxDropdown("Controlled"), "controlled");
        target.showingProperty().addListener((o, was, is) -> log("controlled: " + (is ? "opened" : "closed")));

        Button show = new Button("show()");
        show.setOnAction(e -> target.show());
        Button hide = new Button("hide()");
        hide.setOnAction(e -> target.hide());
        Button toggle = new Button("toggle()");
        toggle.setOnAction(e -> target.toggle());

        Label status = new Label();
        status.getStyleClass().addAll("text-sm", "text-muted");
        status.textProperty().bind(
                Bindings.format("showing = %s", target.showingProperty()));

        HBox row = new HBox(10, target, show, hide, toggle, status);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(10, row);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases
    // ------------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxDropdown disabled = basic(new FxDropdown("Disabled dropdown"), "disabled");
        disabled.setDisable(true);
        flow.getChildren().add(labeled("disabled: no open, dimmed", disabled));

        flow.getChildren().add(labeled("empty (no items)", new FxDropdown("Empty")));

        FxDropdown noArrow = basic(new FxDropdown("No arrow"), "no arrow");
        noArrow.setArrowIcon(false);
        flow.getChildren().add(labeled("arrowIcon = false", noArrow));

        FxDropdown noLabel = basic(new FxDropdown(), "no label");
        flow.getChildren().add(labeled("no label (chevron only)", noLabel));

        FxDropdown withInput = new FxDropdown("Header with input");
        withInput.setEnableTypeAhead(false);
        TextField filter = new TextField();
        filter.setPromptText("Type here...");
        withInput.getItems().addAll(
                new FxDropdownHeader(filter),
                logged("input header", "Dashboard", null),
                logged("input header", "Settings", null));
        flow.getChildren().add(labeled("TextField in header, enableTypeAhead = false "
                + "(check that typing reaches the field)", withInput));

        FxDropdown custom = new FxDropdown("Custom header");
        custom.getItems().addAll(
                new FxDropdownHeader(new Label("Signed in as"), new Label("a custom node")),
                logged("custom header", "Profile", ICON),
                new FxDropdownDivider(),
                logged("custom header", "Sign out", ICON));
        flow.getChildren().add(labeled("header with custom Labels (they should follow the theme)", custom));
        return flow;
    }

    // ---- helpers
    // -------------------------------------------------------------------------------------------

    private FxDropdown basic(FxDropdown dropdown, String source) {
        for (String name : new String[] { "Dashboard", "Settings", "Earnings", "Sign out" }) {
            dropdown.getItems().add(logged(source, name, null));
        }
        return dropdown;
    }

    private FxDropdownItem logged(String source, String name, Ikon icon) {
        FxDropdownItem item = new FxDropdownItem(name, icon);
        item.setOnAction(e -> log(source + ": clicked " + name));
        return item;
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
        label.setWrapText(true);
        VBox wrapper = new VBox(4, label, content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
