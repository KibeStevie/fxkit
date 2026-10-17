package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.avatar.FxAvatar;
import dev.fxkit.core.components.list.FxList;
import dev.fxkit.core.components.list.FxListItem;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxList} / {@link FxListItem}: every Flowbite
 * example (default,
 * icons, nested, unstyled, ordered, advanced layout, horizontal), runtime
 * changes (add, remove,
 * reorder, flag flips), edge cases, and a live playground where every property
 * can be flipped.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check that lists restyle correctly in light
 * and dark.
 */
public class ListTestApp extends Application {

    /**
     * Change to any icon from the Ikonli pack you have on the classpath (Flowbite
     * uses a check-circle).
     */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String[] SAMPLE = {
            "At least 10 characters (and up to 100 characters)",
            "At least one lowercase character",
            "Inclusion of at least one special character, e.g., ! @ # ?"
    };

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (add / remove / reorder actions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: edit the items (one per line), flip ordered / unstyled / "
                                + "horizontal / divided / nested, set a list icon, add a nested sub-list."),
                playground(),

                sectionHeading("Default list",
                        "Bulleted, muted text, 4px between items. Flowbite: <List> with <ListItem>s."),
                labeled("default", sized(FxList.of(SAMPLE), 360)),

                sectionHeading("Icons",
                        "Icon replaces the bullet. Set once on the list, or per item (an item's own icon "
                                + "wins). Items with an icon are vertically centered."),
                iconSection(),

                sectionHeading("Nested",
                        "An ordered, nested FxList as the content of an item. Each nested list restarts "
                                + "at 1 and is indented 20px with 8px above."),
                labeled("unordered parent, ordered children", nestedList()),

                sectionHeading("Unstyled",
                        "No bullets or numbers; the text keeps the same muted color."),
                labeled("unstyled", unstyledList()),

                sectionHeading("Ordered list", "Numbers instead of bullets."),
                labeled("ordered", orderedList()),

                sectionHeading("Advanced layout",
                        "Unstyled + divided, with a custom row (avatar, name, e-mail, price) as each item's "
                                + "content. No images are bundled, so avatars fall back to initials."),
                labeled("divided, custom content", advancedList()),

                sectionHeading("Horizontal list",
                        "One centered, wrapping row, 16px apart, no markers. Resize the window to watch it wrap."),
                labeled("horizontal", horizontalList()),

                sectionHeading("Dynamic items",
                        "Numbers must stay correct after every change: add, prepend, remove, reverse, clear."),
                dynamicSection(),

                sectionHeading("Flags at runtime",
                        "Flip a flag on a live list: markers, layout and dividers update in place."),
                flagSection(),

                sectionHeading("Edge cases",
                        "Empty list, wrapping text, 10+ numbers, single divided item, content without text, "
                                + "icon + ordered, horizontal + ordered, an item outside any list."),
                edgeCases(),

                sectionHeading("Event log", "Everything the dynamic sections report lands here."),
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

        stage.setTitle("FxList test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxList test harness");
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
        FxList list = new FxList();
        VBox listHolder = new VBox(list);
        listHolder.getStyleClass().addAll("bg-background", "p-4", "rounded-lg", "border", "border-default");

        TextArea itemsArea = new TextArea(String.join("\n", SAMPLE));
        itemsArea.setPrefRowCount(4);
        itemsArea.setPromptText("one item per line");

        CheckBox orderedCheck = new CheckBox("Ordered");
        CheckBox unstyledCheck = new CheckBox("Unstyled");
        CheckBox horizontalCheck = new CheckBox("Horizontal");
        CheckBox dividedCheck = new CheckBox("Divided");
        CheckBox nestedFlagCheck = new CheckBox("Nested flag (indent the list itself)");
        CheckBox iconCheck = new CheckBox("List icon");
        CheckBox subListCheck = new CheckBox("Nested sub-list under item 1");

        list.orderedProperty().bind(orderedCheck.selectedProperty());
        list.unstyledProperty().bind(unstyledCheck.selectedProperty());
        list.horizontalProperty().bind(horizontalCheck.selectedProperty());
        list.dividedProperty().bind(dividedCheck.selectedProperty());
        list.nestedProperty().bind(nestedFlagCheck.selectedProperty());
        iconCheck.selectedProperty().addListener((o, was, is) -> list.setIcon(is ? ICON : null));

        Runnable rebuild = () -> {
            list.getItems().setAll(itemsArea.getText().lines()
                    .filter(line -> !line.isBlank())
                    .map(FxListItem::new)
                    .toList());
            if (subListCheck.isSelected() && !list.getItems().isEmpty()) {
                FxList sub = FxList.of("Nested one", "Nested two", "Nested three");
                sub.setOrdered(true);
                sub.setNested(true);
                list.getItems().get(0).setContent(sub);
            }
        };
        itemsArea.textProperty().addListener((o, was, is) -> rebuild.run());
        subListCheck.selectedProperty().addListener((o, was, is) -> rebuild.run());
        rebuild.run();

        FlowPane flags = new FlowPane(16, 8, orderedCheck, unstyledCheck, horizontalCheck, dividedCheck,
                nestedFlagCheck, iconCheck, subListCheck);

        VBox box = new VBox(12, itemsArea, flags, listHolder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- flowbite examples
    // ------------------------------------------------------------------

    private static VBox iconSection() {
        FxList listIcon = FxList.of(SAMPLE);
        listIcon.setIcon(ICON);

        FxList mixed = FxList.of(SAMPLE);
        mixed.getItems().get(0).setIcon(ICON);
        mixed.getItems().get(2).setIcon(ICON);

        FxList unstyledIcons = FxList.of(SAMPLE);
        unstyledIcons.setUnstyled(true);
        unstyledIcons.setIcon(ICON);

        VBox box = new VBox(12,
                labeled("icon set on the list", sized(listIcon, 360)),
                labeled("icon on items 1 and 3 only (item 2 keeps its bullet)", sized(mixed, 360)),
                labeled("unstyled + icon (icons still show)", sized(unstyledIcons, 360)));
        return box;
    }

    private static FxList nestedList() {
        FxList list = new FxList();
        String[] parents = { "List item one", "List item two", "List item three" };
        String[][] children = {
                { "You might feel like you are being really \"organized\" o",
                        "Nested navigation in UIs is a bad idea too, keep things as flat as possible.",
                        "Nesting tons of folders in your source code is also not helpful." },
                { "I'm not sure if we'll bother styling more than two levels deep.",
                        "Two is already too much, three is guaranteed to be a bad idea.",
                        "If you nest four levels deep you belong in prison." },
                { "Again please don't nest lists if you want",
                        "Nobody wants to look at this.",
                        "I'm upset that we even have to bother styling this." }
        };
        for (int i = 0; i < parents.length; i++) {
            FxList nested = FxList.of(children[i]);
            nested.setOrdered(true);
            nested.setNested(true);
            list.getItems().add(new FxListItem(parents[i], nested));
        }
        return sized(list, 560);
    }

    private static FxList unstyledList() {
        FxList list = FxList.of(SAMPLE);
        list.setUnstyled(true);
        return sized(list, 360);
    }

    private static FxList orderedList() {
        FxList list = FxList.of(SAMPLE);
        list.setOrdered(true);
        return sized(list, 360);
    }

    private static FxList advancedList() {
        FxList list = new FxList();
        list.setUnstyled(true);
        list.setDivided(true);
        String[][] people = {
                { "Neil Sims", "NS", "$320" },
                { "Bonnie Green", "BG", "$3467" },
                { "Michael Gough", "MG", "$67" },
                { "Thomas Lean", "TL", "$2367" },
                { "Lana Byrd", "LB", "$367" }
        };
        for (String[] p : people) {
            list.getItems().add(new FxListItem(null, personRow(p[0], p[1], p[2])));
        }
        return sized(list, 440);
    }

    private static HBox personRow(String name, String initials, String price) {
        FxAvatar avatar = new FxAvatar();
        avatar.setSize(FxAvatar.Size.SM);
        avatar.setRounded(true);
        avatar.setPlaceholderInitials(initials);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().addAll("text-sm", "font-medium", "text-body");
        Label emailLabel = new Label("email@flowbite.com");
        emailLabel.getStyleClass().addAll("text-sm", "text-muted");
        VBox texts = new VBox(2, nameLabel, emailLabel);
        texts.setMinWidth(0);
        HBox.setHgrow(texts, Priority.ALWAYS);

        Label priceLabel = new Label(price);
        priceLabel.getStyleClass().addAll("text-base", "font-semibold", "text-body");

        HBox row = new HBox(16, avatar, texts, priceLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static FxList horizontalList() {
        FxList list = FxList.of("About", "Premium", "Campaigns", "Blog", "Affiliate Program", "FAQs", "Contact");
        list.setHorizontal(true);
        return list;
    }

    // ---- dynamic items
    // ----------------------------------------------------------------------

    private VBox dynamicSection() {
        FxList list = FxList.of("First", "Second", "Third");
        list.setOrdered(true);
        int[] counter = { 3 };

        Button append = new Button("Append");
        append.setOnAction(e -> {
            list.addItem("Item " + (++counter[0]));
            log("append -> " + list.getItems().size() + " items");
        });

        Button prepend = new Button("Prepend");
        prepend.setOnAction(e -> {
            list.getItems().add(0, new FxListItem("Item " + (++counter[0])));
            log("prepend -> numbers should have shifted down");
        });

        Button insertMiddle = new Button("Insert in middle");
        insertMiddle.setOnAction(e -> {
            list.getItems().add(list.getItems().size() / 2, new FxListItem("Item " + (++counter[0])));
            log("insert in middle");
        });

        Button removeFirst = new Button("Remove first");
        removeFirst.setOnAction(e -> {
            if (!list.getItems().isEmpty()) {
                list.getItems().remove(0);
                log("remove first -> " + list.getItems().size() + " items");
            }
        });

        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!list.getItems().isEmpty()) {
                list.getItems().remove(list.getItems().size() - 1);
                log("remove last -> " + list.getItems().size() + " items");
            }
        });

        Button reverse = new Button("Reverse");
        reverse.setOnAction(e -> {
            FXCollections.reverse(list.getItems());
            log("reverse -> numbering follows position, not item identity");
        });

        Button clear = new Button("Clear");
        clear.setOnAction(e -> {
            list.getItems().clear();
            log("clear -> list is empty, should collapse to nothing");
        });

        FlowPane buttons = new FlowPane(10, 8, append, prepend, insertMiddle, removeFirst, removeLast, reverse, clear);
        VBox box = new VBox(10, buttons, sized(list, 360));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- flags at runtime
    // -------------------------------------------------------------------

    private VBox flagSection() {
        FxList list = FxList.of(SAMPLE);

        Button ordered = new Button("Toggle ordered");
        ordered.setOnAction(e -> {
            list.setOrdered(!list.isOrdered());
            log("ordered = " + list.isOrdered());
        });
        Button unstyled = new Button("Toggle unstyled");
        unstyled.setOnAction(e -> {
            list.setUnstyled(!list.isUnstyled());
            log("unstyled = " + list.isUnstyled());
        });
        Button horizontal = new Button("Toggle horizontal");
        horizontal.setOnAction(e -> {
            list.setHorizontal(!list.isHorizontal());
            log("horizontal = " + list.isHorizontal() + " (items move between column and row)");
        });
        Button divided = new Button("Toggle divided");
        divided.setOnAction(e -> {
            list.setDivided(!list.isDivided());
            log("divided = " + list.isDivided());
        });
        Button icon = new Button("Toggle list icon");
        icon.setOnAction(e -> {
            list.setIcon(list.getIcon() == null ? ICON : null);
            log("list icon = " + (list.getIcon() != null));
        });
        Button itemIcon = new Button("Toggle icon on item 2");
        itemIcon.setOnAction(e -> {
            FxListItem second = list.getItems().get(1);
            second.setIcon(second.getIcon() == null ? ICON : null);
            log("item 2 own icon = " + (second.getIcon() != null));
        });
        Button retext = new Button("Change item 1 text");
        retext.setOnAction(e -> list.getItems().get(0).setText("Changed " + System.nanoTime() % 1000));

        FlowPane buttons = new FlowPane(10, 8, ordered, unstyled, horizontal, divided, icon, itemIcon, retext);
        VBox box = new VBox(10, buttons, sized(list, 420));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases
    // -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxList empty = new FxList();
        empty.setPrefWidth(200);
        flow.getChildren().add(labeled("empty list (no items)", empty));

        FxList longText = FxList.of(
                "This is a deliberately long item that should wrap inside the list rather than "
                        + "overflowing, with its continuation lines aligned under the first line of text.",
                "Short item.",
                "Another long one: the number or bullet must stay on the first line only, never repeated.");
        longText.setOrdered(true);
        flow.getChildren().add(labeled("long text wraps (ordered, 260px)", sized(longText, 260)));

        FxList many = new FxList();
        for (int i = 1; i <= 12; i++) {
            many.addItem("Item " + i);
        }
        many.setOrdered(true);
        flow.getChildren().add(labeled("12 items: \"9.\" and \"10.\" should end in the same column", many));

        FxList single = FxList.of("Only item");
        single.setDivided(true);
        single.setUnstyled(true);
        flow.getChildren().add(labeled("single divided item (no rule, no padding)", sized(single, 260)));

        FxListItem contentOnly = new FxListItem(null, new Label("Content only, no text label"));
        FxListItem blankText = new FxListItem("   ", new Label("Blank text is treated as no text"));
        FxList contentList = new FxList();
        contentList.getItems().addAll(contentOnly, blankText);
        flow.getChildren().add(labeled("content without text", sized(contentList, 260)));

        FxList orderedIcon = FxList.of(SAMPLE);
        orderedIcon.setOrdered(true);
        orderedIcon.setIcon(ICON);
        flow.getChildren().add(labeled("ordered + icon (icon wins, no numbers)", sized(orderedIcon, 300)));

        FxList horizontalOrdered = FxList.of("One", "Two", "Three");
        horizontalOrdered.setOrdered(true);
        horizontalOrdered.setHorizontal(true);
        flow.getChildren().add(labeled("horizontal + ordered (no numbers)", horizontalOrdered));

        FxListItem lone = new FxListItem("An item that was never added to a list");
        FxListItem loneIcon = new FxListItem("Same, with its own icon");
        loneIcon.setIcon(ICON);
        flow.getChildren().add(labeled("items outside a list (no bullet, icon only)", new VBox(6, lone, loneIcon)));

        return flow;
    }

    // ---- helpers
    // ----------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxList sized(FxList list, double width) {
        list.setPrefWidth(width);
        list.setMaxWidth(width);
        return list;
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
