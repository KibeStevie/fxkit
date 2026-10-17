package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.listgroup.FxListGroup;
import dev.fxkit.core.components.listgroup.FxListGroupItem;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
 * Isolated test harness for {@link FxListGroup} / {@link FxListGroupItem}: the
 * four Flowbite examples
 * (default, links, buttons, icons), every item state (active, disabled, hover,
 * keyboard focus), a
 * single-select menu, edge cases for the first/last position tags, and a live
 * playground where items
 * can be added, removed, replaced and reordered at runtime.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check that the list group restyles correctly
 * in light and dark.
 * Tab through a group to see the keyboard focus ring.
 */
public class ListGroupTestApp extends Application {

    /**
     * Change these to icons from whichever Ikonli pack you have on the classpath.
     * Flowbite's own
     * examples use user-circle / adjustments / inbox / cloud-download.
     */
    private static final Ikon ICON_PROFILE = Devicons.JAVA;
    private static final Ikon ICON_SETTINGS = Devicons.JAVA;
    private static final Ikon ICON_MESSAGES = Devicons.JAVA;
    private static final Ikon ICON_DOWNLOAD = Devicons.JAVA;

    /** Flowbite's examples use {@code w-48}. */
    private static final double W48 = 192;

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (item actions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Add, remove, replace and reorder items live. The readout under the list shows which "
                                + "items currently carry the first/last position tags."),
                playground(),

                sectionHeading("Default list group",
                        "Plain items. Download is disabled: gray row, no hover, not clickable."),
                defaultExample(),

                sectionHeading("List items as links",
                        "Flowbite's href: here an onAction handler, since href and onClick look the same. "
                                + "Profile starts active. Clicks are logged."),
                linksExample(),

                sectionHeading("List group with buttons",
                        "onAction on an item. Profile is active and logs 'Profile clicked!'."),
                buttonsExample(),

                sectionHeading("List group with icons",
                        "The icon follows the text color: normal, hover (primary), active (on-primary), disabled."),
                iconsExample(),

                sectionHeading("States",
                        "Resting, active, disabled, active + disabled. Hover each one, and Tab to check the "
                                + "focus ring (it must not shift the text)."),
                statesExample(),

                sectionHeading("Single-select menu",
                        "Active is not managed by the group; this one is wired up so a click makes that item the "
                                + "only active one."),
                singleSelectExample(),

                sectionHeading("Position tags: edge cases",
                        "Empty group, one item (all four corners round, no divider), two items, and an item "
                                + "moved between groups (its tags must follow it)."),
                edgeCases(),

                sectionHeading("Text and width",
                        "Long text in a narrow group is clipped with an ellipsis; a wide group stretches every "
                                + "row to full width."),
                widthExamples(),

                sectionHeading("Event log", "Everything the item actions report lands here."),
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

        stage.setTitle("FxListGroup test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxListGroup test harness");
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
        FxListGroup group = group(W48,
                new FxListGroupItem("Profile"),
                new FxListGroupItem("Settings"),
                new FxListGroupItem("Messages"));

        int[] counter = { 0 };
        Label tags = new Label();
        tags.getStyleClass().addAll("text-xs", "text-muted");
        Runnable refreshTags = () -> {
            StringBuilder sb = new StringBuilder();
            for (FxListGroupItem item : group.getItems()) {
                boolean first = item.getStyleClass().contains("fxk-list-group-item-first");
                boolean last = item.getStyleClass().contains("fxk-list-group-item-last");
                if (first || last) {
                    sb.append(item.getText()).append(": ")
                            .append(first ? "first" : "").append(first && last ? "+" : "")
                            .append(last ? "last" : "").append("   ");
                }
            }
            tags.setText("items: " + group.getItems().size() + "   tags -> "
                    + (sb.length() == 0 ? "(none)" : sb.toString().trim()));
        };
        group.getItems().addListener((javafx.collections.ListChangeListener<FxListGroupItem>) c -> refreshTags.run());

        TextField nameField = new TextField();
        nameField.setPromptText("new item text (blank = 'Item N')");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        CheckBox iconsCheck = new CheckBox("Icons");
        CheckBox selectCheck = new CheckBox("Click sets active");

        java.util.function.Supplier<FxListGroupItem> factory = () -> {
            int n = ++counter[0];
            String text = nameField.getText().isBlank() ? "Item " + n : nameField.getText().trim();
            FxListGroupItem item = new FxListGroupItem(text);
            if (iconsCheck.isSelected()) {
                item.setIcon(ICON_PROFILE);
            }
            item.setOnAction(e -> {
                log("playground: clicked '" + item.getText() + "'");
                if (selectCheck.isSelected()) {
                    group.getItems().forEach(i -> i.setActive(i == item));
                }
            });
            return item;
        };

        // Seed items behave like new ones.
        for (FxListGroupItem seed : group.getItems()) {
            seed.setOnAction(e -> {
                log("playground: clicked '" + seed.getText() + "'");
                if (selectCheck.isSelected()) {
                    group.getItems().forEach(i -> i.setActive(i == seed));
                }
            });
        }

        iconsCheck.selectedProperty()
                .addListener((o, was, is) -> group.getItems().forEach(i -> i.setIcon(is ? ICON_PROFILE : null)));

        Button addEnd = new Button("Add at end");
        addEnd.setOnAction(e -> group.getItems().add(factory.get()));

        Button addStart = new Button("Add at start");
        addStart.setOnAction(e -> group.getItems().add(0, factory.get()));

        Button removeFirst = new Button("Remove first");
        removeFirst.setOnAction(e -> {
            if (!group.getItems().isEmpty()) {
                group.getItems().remove(0);
            }
        });

        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!group.getItems().isEmpty()) {
                group.getItems().remove(group.getItems().size() - 1);
            }
        });

        Button replaceFirst = new Button("Replace first");
        replaceFirst.setOnAction(e -> {
            if (!group.getItems().isEmpty()) {
                group.getItems().set(0, factory.get());
            }
        });

        Button reverse = new Button("Reverse");
        reverse.setOnAction(e -> FXCollections.reverse(group.getItems()));

        Button clear = new Button("Clear");
        clear.setOnAction(e -> group.getItems().clear());

        Button toggleActive = new Button("Toggle active on first");
        toggleActive.setOnAction(e -> {
            if (!group.getItems().isEmpty()) {
                FxListGroupItem first = group.getItems().get(0);
                first.setActive(!first.isActive());
            }
        });

        Button toggleDisabled = new Button("Toggle disabled on first");
        toggleDisabled.setOnAction(e -> {
            if (!group.getItems().isEmpty()) {
                FxListGroupItem first = group.getItems().get(0);
                first.setDisable(!first.isDisable());
            }
        });

        HBox row1 = new HBox(10, new Label("Text"), nameField, iconsCheck, selectCheck);
        row1.setAlignment(Pos.CENTER_LEFT);
        FlowPane row2 = new FlowPane(10, 10, addEnd, addStart, removeFirst, removeLast, replaceFirst, reverse, clear,
                toggleActive, toggleDisabled);

        refreshTags.run();
        VBox box = new VBox(12, row1, row2, group, tags);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- Flowbite examples
    // ------------------------------------------------------------------

    private FlowPane defaultExample() {
        FxListGroupItem download = new FxListGroupItem("Download");
        download.setDisable(true);
        FxListGroup group = group(W48,
                new FxListGroupItem("Profile"),
                new FxListGroupItem("Settings"),
                new FxListGroupItem("Messages"),
                download);
        return flow(labeled("default", group));
    }

    private FlowPane linksExample() {
        FxListGroupItem profile = new FxListGroupItem("Profile");
        profile.setActive(true);
        FxListGroup group = group(W48,
                profile,
                new FxListGroupItem("Settings"),
                new FxListGroupItem("Messages"),
                new FxListGroupItem("Download"));
        for (FxListGroupItem item : group.getItems()) {
            item.setOnAction(e -> log("link: " + item.getText()));
        }
        return flow(labeled("onAction on every item", group));
    }

    private FlowPane buttonsExample() {
        FxListGroupItem profile = new FxListGroupItem("Profile");
        profile.setActive(true);
        profile.setOnAction(e -> log("Profile clicked!"));
        FxListGroup group = group(W48,
                profile,
                new FxListGroupItem("Settings"),
                new FxListGroupItem("Messages"),
                new FxListGroupItem("Download"));
        return flow(labeled("only Profile has a handler", group));
    }

    private FlowPane iconsExample() {
        FxListGroupItem profile = new FxListGroupItem("Profile", ICON_PROFILE);
        profile.setActive(true);
        FxListGroupItem download = new FxListGroupItem("Download", ICON_DOWNLOAD);
        download.setDisable(true);
        FxListGroup group = group(W48,
                profile,
                new FxListGroupItem("Settings", ICON_SETTINGS),
                new FxListGroupItem("Messages", ICON_MESSAGES),
                download);

        // Runtime icon toggle: icon must appear and disappear without touching the
        // text.
        FxListGroup toggling = group(W48,
                new FxListGroupItem("Icon toggles", ICON_PROFILE),
                new FxListGroupItem("Second item"));
        Button toggle = new Button("Toggle icon on first");
        toggle.setOnAction(e -> {
            FxListGroupItem first = toggling.getItems().get(0);
            first.setIcon(first.getIcon() == null ? ICON_PROFILE : null);
        });

        return flow(
                labeled("icons, Profile active, Download disabled", group),
                labeled("runtime icon toggle", new VBox(6, toggling, toggle)));
    }

    // ---- states
    // -------------------------------------------------------------------------------

    private FlowPane statesExample() {
        FxListGroupItem active = new FxListGroupItem("active");
        active.setActive(true);
        FxListGroupItem disabled = new FxListGroupItem("disabled");
        disabled.setDisable(true);
        FxListGroupItem activeDisabled = new FxListGroupItem("active + disabled");
        activeDisabled.setActive(true);
        activeDisabled.setDisable(true);
        FxListGroup group = group(W48, new FxListGroupItem("resting"), active, disabled, activeDisabled);
        for (FxListGroupItem item : group.getItems()) {
            item.setOnAction(e -> log("state item clicked: " + item.getText()));
        }

        FxListGroup withIcons = group(W48,
                new FxListGroupItem("resting", ICON_PROFILE),
                activeCopy("active", ICON_PROFILE),
                disabledCopy("disabled", ICON_PROFILE));

        return flow(
                labeled("text only (disabled items never log)", group),
                labeled("with icons (icon color tracks the text)", withIcons));
    }

    private static FxListGroupItem activeCopy(String text, Ikon icon) {
        FxListGroupItem item = new FxListGroupItem(text, icon);
        item.setActive(true);
        return item;
    }

    private static FxListGroupItem disabledCopy(String text, Ikon icon) {
        FxListGroupItem item = new FxListGroupItem(text, icon);
        item.setDisable(true);
        return item;
    }

    // ---- single select
    // ------------------------------------------------------------------------

    private FlowPane singleSelectExample() {
        FxListGroup group = group(W48,
                new FxListGroupItem("Profile", ICON_PROFILE),
                new FxListGroupItem("Settings", ICON_SETTINGS),
                new FxListGroupItem("Messages", ICON_MESSAGES),
                new FxListGroupItem("Download", ICON_DOWNLOAD));
        group.getItems().get(0).setActive(true);
        for (FxListGroupItem item : group.getItems()) {
            item.setOnAction(e -> {
                group.getItems().forEach(i -> i.setActive(i == item));
                log("selected: " + item.getText());
            });
        }
        return flow(labeled("click or Tab + Space", group));
    }

    // ---- edge cases
    // ---------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FxListGroup empty = group(W48);

        FxListGroup single = group(W48, new FxListGroupItem("Only item"));
        FxListGroup singleActive = group(W48, activeCopy("Only item, active", ICON_PROFILE));

        FxListGroup two = group(W48, new FxListGroupItem("First"), new FxListGroupItem("Second"));

        // Moving an item: it must lose its tags in the source group and pick up the
        // right ones
        // in the target. Remove from one list before adding to the other.
        FxListGroup source = group(W48,
                new FxListGroupItem("A1"), new FxListGroupItem("A2"), new FxListGroupItem("A3"));
        FxListGroup target = group(W48, new FxListGroupItem("B1"), new FxListGroupItem("B2"));
        Button move = new Button("Move A's first to end of B");
        move.setOnAction(e -> {
            if (!source.getItems().isEmpty()) {
                FxListGroupItem moved = source.getItems().remove(0);
                target.getItems().add(moved);
                log("moved '" + moved.getText() + "' from A to B");
            }
        });
        Button back = new Button("Move B's last to start of A");
        back.setOnAction(e -> {
            if (!target.getItems().isEmpty()) {
                FxListGroupItem moved = target.getItems().remove(target.getItems().size() - 1);
                source.getItems().add(0, moved);
                log("moved '" + moved.getText() + "' from B to A");
            }
        });

        return flow(
                labeled("empty group", empty),
                labeled("one item (all corners round, no divider)", single),
                labeled("one item, active + icon", singleActive),
                labeled("two items", two),
                labeled("group A", source),
                labeled("group B", target),
                labeled("move between groups", new VBox(6, move, back)));
    }

    // ---- text and width
    // -----------------------------------------------------------------------

    private FlowPane widthExamples() {
        FxListGroup narrow = group(120,
                new FxListGroupItem("A deliberately long label that cannot fit"),
                new FxListGroupItem("Short"),
                activeCopy("An equally long active label", ICON_PROFILE));

        FxListGroup wide = new FxListGroup(
                new FxListGroupItem("Profile", ICON_PROFILE),
                new FxListGroupItem("Settings", ICON_SETTINGS),
                activeCopy("Messages", ICON_MESSAGES));
        wide.setPrefWidth(460);
        wide.setMaxWidth(Region.USE_PREF_SIZE);

        FxListGroup unsized = new FxListGroup(
                new FxListGroupItem("No preferred width set"),
                new FxListGroupItem("Sized by its widest row"),
                new FxListGroupItem("OK"));
        unsized.setMaxWidth(Region.USE_PREF_SIZE);

        return flow(
                labeled("120px wide: ellipsis", narrow),
                labeled("460px wide: rows stretch", wide),
                labeled("no width set", unsized));
    }

    // ---- helpers
    // ------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxListGroup group(double width, FxListGroupItem... items) {
        FxListGroup group = new FxListGroup(items);
        group.setPrefWidth(width);
        group.setMaxWidth(Region.USE_PREF_SIZE);
        return group;
    }

    private static FlowPane flow(Node... children) {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().addAll(children);
        return flow;
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
