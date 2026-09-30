package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.button.FxButtonGroup;
import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxButtonGroup}: the five Flowbite React examples (default, with
 * icons, outline, outline with icons, color options), every Color, pill, sizes, unmanaged mixed
 * variants, restore-on-reset behaviour, dynamic children (add, remove, reverse), and edge cases,
 * plus a live playground where every property can be flipped at runtime.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check the groups in light and dark, and press Tab to check the
 * focus ring on every position (start, middle, end).
 */
public class ButtonGroupTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String[] NAMES = { "Profile", "Settings", "Messages" };

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (button actions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: color, outline, pill, size, icons, disabled, and "
                                + "adding/removing buttons. Watch the joined corners update in place."),
                playground(),

                sectionHeading("Flowbite examples",
                        "The five examples from the Flowbite React docs, in order."),
                flowbiteExamples(),

                sectionHeading("Color (all 15) - solid",
                        "group.setColor(c): children become Variant.DEFAULT with that color."),
                colorGrid(false),

                sectionHeading("Color (all 15) - outline",
                        "setOutline(true) + setColor(c): border + text, solid on hover."),
                colorGrid(true),

                sectionHeading("Pill",
                        "Only the OUTER ends go fully round; the inner joints stay square."),
                pillGrid(),

                sectionHeading("Sizes",
                        "Size is per button, so it works with the group as usual. XS to XL."),
                sizeGrid(),

                sectionHeading("Unmanaged: mixed variants",
                        "No group color/outline set, so every button keeps its own variant. The group "
                                + "only joins them."),
                unmanagedSection(),

                sectionHeading("Restore on reset",
                        "Each button has its own DEFAULT color. Outline/Color override them; turning "
                                + "both off must bring back RED / GREEN / BLUE exactly."),
                restoreSection(),

                sectionHeading("Dynamic children",
                        "Positions (start/middle/end/only) are recomputed on every change. Removed "
                                + "buttons should stop looking grouped."),
                dynamicSection(),

                sectionHeading("Edge cases",
                        "Single button, empty group, non-FxButton child, disabled button inside a group."),
                edgeCases(),

                sectionHeading("Event log", "Every button click lands here."),
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

        stage.setTitle("FxButtonGroup test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxButtonGroup test harness");
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
        FxButtonGroup group = makeGroup(3, false);
        int[] count = {3};

        ComboBox<FxButton.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxButton.Color.values());
        colorBox.setPromptText("(none)");
        colorBox.valueProperty().bindBidirectional(group.colorProperty());

        Button clearColor = new Button("Clear color");
        clearColor.setOnAction(e -> group.setColor(null));

        ComboBox<FxButton.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxButton.Size.values());
        sizeBox.setValue(FxButton.DEFAULT_SIZE);
        sizeBox.valueProperty().addListener((o, was, is) -> {
            for (Node n : group.getChildren()) {
                if (n instanceof FxButton b) {
                    b.setSize(is);
                }
            }
        });

        CheckBox outlineCheck = new CheckBox("Outline");
        group.outlineProperty().bind(outlineCheck.selectedProperty());

        CheckBox pillCheck = new CheckBox("Pill");
        group.pillProperty().bind(pillCheck.selectedProperty());

        CheckBox iconCheck = new CheckBox("Icons");
        iconCheck.selectedProperty().addListener((o, was, is) -> {
            for (Node n : group.getChildren()) {
                if (n instanceof FxButton b) {
                    b.setIcon(is ? ICON : null);
                }
            }
        });

        CheckBox disabledCheck = new CheckBox("Disabled");
        group.disableProperty().bind(disabledCheck.selectedProperty());

        Button add = new Button("Add button");
        add.setOnAction(e -> {
            count[0]++;
            FxButton b = button("Button " + count[0], iconCheck.isSelected() ? ICON : null);
            b.setSize(sizeBox.getValue());
            group.getChildren().add(b);
        });

        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!group.getChildren().isEmpty()) {
                group.getChildren().remove(group.getChildren().size() - 1);
            }
        });

        HBox row1 = new HBox(16, new Label("Color"), colorBox, clearColor, new Label("Size"), sizeBox);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, outlineCheck, pillCheck, iconCheck, disabledCheck, add, removeLast);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, group);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the five Flowbite examples -----------------------------------------------------------

    private FlowPane flowbiteExamples() {
        FlowPane flow = new FlowPane(12, 12);

        FxButtonGroup defaultGroup = makeGroup(3, false);
        defaultGroup.setColor(FxButton.Color.ALTERNATIVE);
        flow.getChildren().add(labeled("1. default (color=alternative)", defaultGroup));

        FxButtonGroup iconGroup = makeGroup(3, true);
        iconGroup.setColor(FxButton.Color.ALTERNATIVE);
        flow.getChildren().add(labeled("2. with icons", iconGroup));

        FxButtonGroup outlineGroup = makeGroup(3, false);
        outlineGroup.setOutline(true);
        flow.getChildren().add(labeled("3. outline", outlineGroup));

        FxButtonGroup outlineIconGroup = makeGroup(3, true);
        outlineIconGroup.setOutline(true);
        flow.getChildren().add(labeled("4. outline with icons", outlineIconGroup));

        FxButtonGroup cyanGroup = makeGroup(3, false);
        cyanGroup.setColor(FxButton.Color.CYAN);
        flow.getChildren().add(labeled("5. color options (cyan)", cyanGroup));

        return flow;
    }

    // ---- colors -------------------------------------------------------------------------------

    private FlowPane colorGrid(boolean outline) {
        FlowPane flow = new FlowPane(12, 12);
        for (FxButton.Color c : FxButton.Color.values()) {
            FxButtonGroup group = makeGroup(3, false);
            group.setOutline(outline);
            group.setColor(c);
            flow.getChildren().add(labeled(c.name(), group));
        }
        return flow;
    }

    // ---- pill ---------------------------------------------------------------------------------

    private FlowPane pillGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxButtonGroup solid = makeGroup(3, false);
        solid.setColor(FxButton.Color.ALTERNATIVE);
        solid.setPill(true);
        flow.getChildren().add(labeled("pill, alternative", solid));

        FxButtonGroup outlined = makeGroup(3, true);
        outlined.setOutline(true);
        outlined.setColor(FxButton.Color.GREEN);
        outlined.setPill(true);
        flow.getChildren().add(labeled("pill, outline green, icons", outlined));

        FxButtonGroup single = makeGroup(1, false);
        single.setColor(FxButton.Color.PURPLE);
        single.setPill(true);
        flow.getChildren().add(labeled("pill, single button (only)", single));

        FxButtonGroup toggling = makeGroup(3, false);
        toggling.setColor(FxButton.Color.INDIGO);
        Button toggle = new Button("Toggle pill");
        toggle.setOnAction(e -> toggling.setPill(!toggling.isPill()));
        flow.getChildren().add(labeled("runtime pill toggle", new VBox(6, toggling, toggle)));
        return flow;
    }

    // ---- sizes --------------------------------------------------------------------------------

    private FlowPane sizeGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxButton.Size size : FxButton.Size.values()) {
            FxButtonGroup group = makeGroup(3, true);
            group.setColor(FxButton.Color.ALTERNATIVE);
            for (Node n : group.getChildren()) {
                ((FxButton) n).setSize(size);
            }
            flow.getChildren().add(labeled(size.name(), group));
        }
        return flow;
    }

    // ---- unmanaged mixed variants ---------------------------------------------------------------

    private FlowPane unmanagedSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxButtonGroup variants = new FxButtonGroup(
                variantButton("Primary", FxButton.Variant.PRIMARY),
                variantButton("Secondary", FxButton.Variant.SECONDARY),
                variantButton("Danger", FxButton.Variant.DANGER),
                variantButton("Success", FxButton.Variant.SUCCESS),
                variantButton("Ghost", FxButton.Variant.GHOST));
        flow.getChildren().add(labeled("PRIMARY / SECONDARY / DANGER / SUCCESS / GHOST", variants));

        FxButtonGroup primary = makeGroup(3, false);
        flow.getChildren().add(labeled("all default PRIMARY (no group color)", primary));
        return flow;
    }

    // ---- restore on reset -------------------------------------------------------------------------

    private VBox restoreSection() {
        FxButtonGroup group = new FxButtonGroup(
                coloredButton("Red", FxButton.Color.RED),
                coloredButton("Green", FxButton.Color.GREEN),
                coloredButton("Blue", FxButton.Color.BLUE));

        CheckBox outline = new CheckBox("Group outline");
        outline.selectedProperty().addListener((o, was, is) -> {
            group.setOutline(is);
            log("restore test: outline=" + is);
        });

        Button cyan = new Button("Group color CYAN");
        cyan.setOnAction(e -> {
            group.setColor(FxButton.Color.CYAN);
            log("restore test: group color CYAN");
        });

        Button clear = new Button("Group color null");
        clear.setOnAction(e -> {
            group.setColor(null);
            log("restore test: group color null");
        });

        Button removeMiddle = new Button("Remove middle (should restore GREEN), then re-add");
        removeMiddle.setOnAction(e -> {
            if (group.getChildren().size() == 3) {
                Node middle = group.getChildren().remove(1);
                middle.setUserData("removed");
                log("restore test: middle removed; its own look should be back");
                group.getChildren().add(1, middle);
                log("restore test: middle re-added; group look should apply again");
            }
        });

        VBox box = new VBox(10, group, new HBox(12, outline, cyan, clear, removeMiddle));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- dynamic children -------------------------------------------------------------------------

    private VBox dynamicSection() {
        FxButtonGroup group = makeGroup(3, false);
        group.setColor(FxButton.Color.ALTERNATIVE);
        int[] count = {3};

        Button addStart = new Button("Add at start");
        addStart.setOnAction(e -> group.getChildren().add(0, button("New " + (++count[0]), null)));

        Button addEnd = new Button("Add at end");
        addEnd.setOnAction(e -> group.getChildren().add(button("New " + (++count[0]), null)));

        Button removeFirst = new Button("Remove first");
        removeFirst.setOnAction(e -> {
            if (!group.getChildren().isEmpty()) {
                group.getChildren().remove(0);
            }
        });

        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!group.getChildren().isEmpty()) {
                group.getChildren().remove(group.getChildren().size() - 1);
            }
        });

        Button reverse = new Button("Reverse order");
        reverse.setOnAction(e -> FXCollections.reverse(group.getChildren()));

        Button clearAll = new Button("Remove all");
        clearAll.setOnAction(e -> group.getChildren().clear());

        FlowPane buttons = new FlowPane(10, 10, addStart, addEnd, removeFirst, removeLast, reverse, clearAll);
        VBox box = new VBox(10, group, buttons);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxButtonGroup single = makeGroup(1, true);
        single.setColor(FxButton.Color.TEAL);
        flow.getChildren().add(labeled("single button (all four corners round)", single));

        FxButtonGroup empty = new FxButtonGroup();
        flow.getChildren().add(labeled("empty group (renders nothing, no exception)", empty));

        FxButtonGroup mixed = new FxButtonGroup(button("FxButton", null), new Button("Plain Button"),
                button("FxButton", null));
        mixed.setColor(FxButton.Color.ALTERNATIVE);
        flow.getChildren().add(labeled("non-FxButton child (tagged, never restyled)", mixed));

        FxButtonGroup disabledMiddle = makeGroup(3, false);
        disabledMiddle.setColor(FxButton.Color.GREEN);
        disabledMiddle.getChildren().get(1).setDisable(true);
        flow.getChildren().add(labeled("disabled middle button", disabledMiddle));

        FxButtonGroup hugging = makeGroup(3, false);
        hugging.setColor(FxButton.Color.PINK);
        VBox stretchParent = new VBox(hugging);
        stretchParent.setPrefWidth(420);
        flow.getChildren().add(labeled("inside a wide VBox (group must hug its buttons, not stretch)", stretchParent));

        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private FxButtonGroup makeGroup(int count, boolean icons) {
        FxButtonGroup group = new FxButtonGroup();
        for (int i = 0; i < count; i++) {
            String name = i < NAMES.length ? NAMES[i] : "Button " + (i + 1);
            group.getChildren().add(button(name, icons ? ICON : null));
        }
        return group;
    }

    private FxButton button(String text, Ikon icon) {
        FxButton button = new FxButton(text);
        if (icon != null) {
            button.setIcon(icon);
        }
        button.setOnAction(e -> log("clicked: " + text));
        return button;
    }

    private FxButton variantButton(String text, FxButton.Variant variant) {
        FxButton button = button(text, null);
        button.setVariant(variant);
        return button;
    }

    private FxButton coloredButton(String text, FxButton.Color color) {
        FxButton button = button(text, null);
        button.setVariant(FxButton.Variant.DEFAULT);
        button.setColor(color);
        return button;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
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
