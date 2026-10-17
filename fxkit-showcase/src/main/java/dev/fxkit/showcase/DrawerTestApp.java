package dev.fxkit.showcase;

import java.util.ArrayList;
import java.util.List;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.drawer.FxDrawer;
import dev.fxkit.core.components.drawer.FxDrawerHeader;
import dev.fxkit.core.components.drawer.FxDrawerItems;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Isolated test harness for {@link FxDrawer}, one section per example in Flowbite React's drawer docs:
 * default drawer, navigation, contact form, form elements, placement (left, right, top, bottom), body
 * scrolling (disabled, enabled), backdrop (enabled, disabled) and the swipeable edge. On top of those:
 * a live playground where every property can be flipped at runtime, and edge cases (long content,
 * rapid toggling, no animation, Escape disabled).
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating. Use
 * the header's theme toggle to check the drawer in light and dark.
 *
 * <p>Plain JavaFX controls stand in for Flowbite's buttons, inputs and sidebar so the harness only
 * depends on the drawer itself. {@link #navigationContent()} is the one place to swap in
 * {@code FxSidebar}.
 */
public class DrawerTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String PITCH = "Supercharge your hiring by taking advantage of our limited-time sale "
            + "for Flowbite Docs + Job Board. Unlimited access to over 190K top-ranked candidates and the "
            + "#1 design job board.";

    private final StackPane stack = new StackPane();
    /** Every drawer except the swipeable-edge one, which is added on demand. Added over the page in start(). */
    private final List<FxDrawer> drawers = new ArrayList<>();

    private Scene scene;
    private Button themeToggle;
    private TextArea log;
    private FxDrawer edgeDrawer;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (onClose callbacks, clicks)...");

        edgeDrawer = buildEdgeDrawer();

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: position, backdrop, body scrolling, edge, padding, panel width, "
                                + "animation. Open it, then flip things while it is open."),
                playground(),

                sectionHeading("Default drawer", "Header + items. Close with the X, a click on the backdrop, or Escape."),
                row(open("Show drawer", defaultDrawer("default", FxDrawer.Position.LEFT))),

                sectionHeading("Drawer navigation", "A navigation list inside the drawer. Header without an icon."),
                row(open("Show navigation", navigationDrawer("navigation"))),

                sectionHeading("Contact form", "Form controls in the items area; the title icon is set."),
                row(open("Show contact form", contactDrawer())),

                sectionHeading("Form elements", "Text fields, a date picker, an inline button and an avatar stack."),
                row(open("Show form elements", formElementsDrawer())),

                sectionHeading("Placement",
                        "position = LEFT (default), RIGHT, TOP, BOTTOM. Left/right are 320px wide and full height; "
                                + "top/bottom are full width and as tall as their content."),
                row(open("Show left drawer", defaultDrawer("left", FxDrawer.Position.LEFT)),
                        open("Show right drawer", defaultDrawer("right", FxDrawer.Position.RIGHT)),
                        open("Show top drawer", defaultDrawer("top", FxDrawer.Position.TOP)),
                        open("Show bottom drawer", defaultDrawer("bottom", FxDrawer.Position.BOTTOM))),

                sectionHeading("Body scrolling",
                        "Open one, then use the mouse wheel over the page. Disabled (default): the page stays put. "
                                + "Enabled: the page scrolls behind the drawer, backdrop or not."),
                row(open("Body scrolling disabled", bodyScrollingDrawer(false)),
                        open("Body scrolling enabled", bodyScrollingDrawer(true))),

                sectionHeading("Backdrop",
                        "Enabled (default): the page is dimmed and blocked, a click on it closes the drawer. "
                                + "Disabled: the page stays clickable; close with the X or Escape."),
                row(open("Backdrop enabled", backdropDrawer(true)),
                        open("Backdrop disabled", backdropDrawer(false))),

                sectionHeading("Swipeable edge",
                        "edge = true keeps the header strip visible when closed; the header's onClick toggles the "
                                + "drawer. Click the strip (or its X) to open and close. Remove takes it off the page again."),
                swipeableSection(),

                sectionHeading("Edge cases",
                        "Long content must scroll inside the panel (a bottom drawer stops at the window height); "
                                + "rapid toggling interrupts the animation cleanly; no-animation snaps; Escape can be off."),
                edgeCases(),

                sectionHeading("Event log", "Everything the onClose callbacks and buttons report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane page = new BorderPane(scroller);
        page.setTop(header());
        page.getStyleClass().add("bg-background");

        stack.getStyleClass().add("bg-background");
        stack.getChildren().add(page);
        stack.getChildren().addAll(drawers); // drawers are overlays: they must come after the page

        scene = new Scene(stack, 1000, 760);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxDrawer test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxDrawer test harness");
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
        FxDrawerHeader header = new FxDrawerHeader("Playground", ICON);
        FxDrawerItems items = new FxDrawerItems(
                paragraph(PITCH),
                paragraph("Move the sliders and toggles below while this is open."));
        items.setSpacing(16);
        FxDrawer drawer = newDrawer("playground", FxDrawer.Position.LEFT, header, items);

        ComboBox<FxDrawer.Position> positionBox = new ComboBox<>();
        positionBox.getItems().addAll(FxDrawer.Position.values());
        positionBox.valueProperty().bindBidirectional(drawer.positionProperty());

        CheckBox animate = new CheckBox("Animate");
        animate.setSelected(true);
        animate.selectedProperty().addListener((o, was, is) ->
                drawer.setTransitionDuration(is ? Duration.millis(150) : Duration.ZERO));

        CheckBox clickableHeader = new CheckBox("Header onClick toggles");
        clickableHeader.selectedProperty().addListener((o, was, is) ->
                header.setOnClick(is ? () -> drawer.setOpen(!drawer.isOpen()) : null));

        Slider width = new Slider(200, 480, drawer.getPanelWidth());
        drawer.panelWidthProperty().bind(width.valueProperty());
        Label widthLabel = new Label();
        widthLabel.textProperty().bind(width.valueProperty().asString("panelWidth %.0f"));

        Slider edgeSize = new Slider(24, 160, drawer.getEdgeSize());
        drawer.edgeSizeProperty().bind(edgeSize.valueProperty());
        Label edgeLabel = new Label();
        edgeLabel.textProperty().bind(edgeSize.valueProperty().asString("edgeSize %.0f"));

        Button openButton = new Button("setOpen(true)");
        openButton.setOnAction(e -> drawer.setOpen(true));
        Button toggleButton = new Button("Toggle");
        toggleButton.setOnAction(e -> drawer.setOpen(!drawer.isOpen()));
        Button requestClose = new Button("requestClose()");
        requestClose.setOnAction(e -> drawer.requestClose());

        FlowPane row1 = new FlowPane(16, 8, new Label("Position"), positionBox,
                bound("Backdrop", drawer.backdropProperty()),
                bound("Body scrolling", drawer.bodyScrollingProperty()),
                bound("Edge", drawer.edgeProperty()),
                bound("Padded", drawer.paddedProperty()),
                bound("Close on Escape", drawer.closeOnEscapeProperty()),
                animate, clickableHeader);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(12, widthLabel, width, edgeLabel, edgeSize);
        row2.setAlignment(Pos.CENTER_LEFT);
        FlowPane row3 = new FlowPane(10, 10, openButton, toggleButton, requestClose);

        return card(row1, row2, row3);
    }

    private static CheckBox bound(String text, BooleanProperty property) {
        CheckBox box = new CheckBox(text);
        box.selectedProperty().bindBidirectional(property);
        return box;
    }

    // ---- Flowbite examples ------------------------------------------------------------------

    /** Flowbite's "Default drawer"; also the content of the placement examples. */
    private FxDrawer defaultDrawer(String name, FxDrawer.Position position) {
        HBox actions = new HBox(16, secondaryButton("Learn more"), primaryButton("Get access"));
        equalWidth(actions);
        FxDrawerItems items = new FxDrawerItems(paragraph(PITCH), actions);
        items.setSpacing(24); // mb-6
        return newDrawer(name, position, new FxDrawerHeader("Drawer"), items);
    }

    private FxDrawer navigationDrawer(String name) {
        FxDrawerItems items = new FxDrawerItems(navigationContent());
        return newDrawer(name, FxDrawer.Position.LEFT, new FxDrawerHeader("MENU"), items);
    }

    /** The navigation list. Replace with an FxSidebar to match Flowbite's example one-to-one. */
    private Node navigationContent() {
        VBox nav = new VBox(4,
                navItem("Dashboard"), navItem("Products"), navItem("Users list"),
                navItem("Sign in"), navItem("Sign up"),
                new Separator(),
                navItem("Docs"), navItem("Components"), navItem("Help"));
        nav.setPadding(new Insets(8, 0, 8, 0));
        return nav;
    }

    private Button navItem(String text) {
        Button item = new Button(text);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 8 12 8 12;");
        item.getStyleClass().addAll("bg-transparent", "text-body", "rounded-lg", "font-medium");
        item.hoverProperty().addListener((o, was, hovered) -> {
            item.getStyleClass().removeAll("bg-transparent", "bg-surface-alt");
            item.getStyleClass().add(hovered ? "bg-surface-alt" : "bg-transparent");
        });
        item.setOnAction(e -> log("navigation: " + text));
        return item;
    }

    private FxDrawer contactDrawer() {
        FxDrawerHeader header = new FxDrawerHeader("CONTACT US", ICON);
        TextField email = new TextField();
        email.setPromptText("name@company.com");
        TextField subject = new TextField();
        subject.setPromptText("Let us know how we can help you");
        TextArea message = new TextArea();
        message.setPromptText("Your message...");
        message.setPrefRowCount(4);
        message.setWrapText(true);
        Button send = primaryButton("Send message");
        send.setOnAction(e -> log("contact form: send clicked"));

        FxDrawerItems items = new FxDrawerItems(
                field("Your email", email),
                field("Subject", subject),
                field("Your message", message),
                send,
                new VBox(8, link("info@company.com"), link("212-456-7890")));
        items.setSpacing(24);
        return newDrawer("contact", FxDrawer.Position.LEFT, header, items);
    }

    private FxDrawer formElementsDrawer() {
        FxDrawerHeader header = new FxDrawerHeader("NEW EVENT", ICON);
        TextField title = new TextField();
        title.setPromptText("Apple Keynote");
        TextArea description = new TextArea();
        description.setPromptText("Write event description...");
        description.setPrefRowCount(4);
        description.setWrapText(true);
        DatePicker date = new DatePicker();
        date.setMaxWidth(Double.MAX_VALUE);

        TextField guests = new TextField();
        guests.setPromptText("Add guest email");
        HBox.setHgrow(guests, Priority.ALWAYS);
        Button add = primaryButton("Add");
        add.setMaxWidth(Region.USE_PREF_SIZE);
        add.setOnAction(e -> log("form elements: add guest '" + guests.getText() + "'"));
        HBox guestRow = new HBox(8, guests, add);

        Button create = primaryButton("Create event");
        create.setOnAction(e -> log("form elements: create event"));

        FxDrawerItems items = new FxDrawerItems(
                field("Title", title), field("Description", description), date, guestRow,
                avatarStack(), create);
        items.setSpacing(24);
        return newDrawer("form elements", FxDrawer.Position.LEFT, header, items);
    }

    private FxDrawer bodyScrollingDrawer(boolean enabled) {
        FxDrawer drawer = navigationDrawer("body scrolling " + (enabled ? "enabled" : "disabled"));
        drawer.setBodyScrolling(enabled);
        return drawer;
    }

    private FxDrawer backdropDrawer(boolean enabled) {
        FxDrawer drawer = navigationDrawer("backdrop " + (enabled ? "enabled" : "disabled"));
        drawer.setBackdrop(enabled);
        return drawer;
    }

    // ---- swipeable edge ---------------------------------------------------------------------

    private FxDrawer buildEdgeDrawer() {
        FxDrawerHeader header = new FxDrawerHeader("Add widget", ICON);
        header.setPadding(new Insets(16, 16, 0, 16)); // px-4 pt-4

        String[] names = { "Chart", "Table", "Ticket", "List", "Price", "Users", "Task", "Custom" };
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(16));
        for (int c = 0; c < 4; c++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            grid.getColumnConstraints().add(column);
        }
        for (int i = 0; i < names.length; i++) {
            grid.add(widgetTile(names[i]), i % 4, i / 4);
        }

        FxDrawer drawer = new FxDrawer(header, new FxDrawerItems(grid));
        drawer.setPosition(FxDrawer.Position.BOTTOM);
        drawer.setEdge(true);
        drawer.setPadded(false); // Flowbite: className="p-0"
        header.setOnClick(() -> drawer.setOpen(!drawer.isOpen()));
        drawer.setOnClose(() -> log("edge drawer: onClose fired"));
        return drawer;
    }

    private VBox widgetTile(String name) {
        Label letter = new Label(name.substring(0, 1));
        letter.getStyleClass().addAll("text-body", "font-semibold");
        StackPane badge = new StackPane(letter);
        badge.setMinSize(48, 48);
        badge.setPrefSize(48, 48);
        badge.setMaxSize(48, 48);
        badge.getStyleClass().addAll("bg-border-strong", "rounded-full");

        Label label = new Label(name);
        label.getStyleClass().addAll("text-sm", "font-medium", "text-muted");

        VBox tile = new VBox(8, badge, label);
        tile.setAlignment(Pos.CENTER);
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setPadding(new Insets(16));
        tile.getStyleClass().addAll("bg-surface-alt", "rounded-lg");
        tile.setOnMouseClicked(e -> log("widget tile: " + name));
        return tile;
    }

    private VBox swipeableSection() {
        Button show = new Button("Show swipeable drawer");
        show.setOnAction(e -> {
            if (!stack.getChildren().contains(edgeDrawer)) {
                stack.getChildren().add(edgeDrawer);
                stack.applyCss();
                stack.layout(); // measure the panel first, so it opens from its real edge position
            }
            edgeDrawer.setOpen(true);
        });
        Button remove = new Button("Remove from page");
        remove.setOnAction(e -> {
            stack.getChildren().remove(edgeDrawer);
            edgeDrawer.setOpen(false);
        });
        return card(row(show, remove));
    }

    // ---- edge cases -------------------------------------------------------------------------

    private VBox edgeCases() {
        FxDrawer longLeft = newDrawer("long content (left)", FxDrawer.Position.LEFT,
                new FxDrawerHeader("Long content"), longItems());
        FxDrawer longBottom = newDrawer("long content (bottom)", FxDrawer.Position.BOTTOM,
                new FxDrawerHeader("Long content"), longItems());

        FxDrawer rapid = defaultDrawer("rapid toggle", FxDrawer.Position.RIGHT);
        rapid.setBackdrop(false);
        Button toggle = new Button("Toggle (click fast)");
        toggle.setOnAction(e -> rapid.setOpen(!rapid.isOpen()));

        FxDrawer instant = defaultDrawer("no animation", FxDrawer.Position.LEFT);
        instant.setTransitionDuration(Duration.ZERO);

        FxDrawer slow = defaultDrawer("slow (1.5s)", FxDrawer.Position.LEFT);
        slow.setTransitionDuration(Duration.seconds(1.5));

        FxDrawer noEscape = defaultDrawer("Escape disabled", FxDrawer.Position.LEFT);
        noEscape.setCloseOnEscape(false);

        FxDrawer wide = defaultDrawer("wide panel", FxDrawer.Position.LEFT);
        wide.setPanelWidth(460);

        return card(row(
                open("Long content (left)", longLeft),
                open("Long content (bottom)", longBottom),
                toggle,
                open("No animation", instant),
                open("Slow animation", slow),
                open("Escape disabled", noEscape),
                open("Wide panel (460)", wide)));
    }

    private static FxDrawerItems longItems() {
        FxDrawerItems items = new FxDrawerItems();
        items.setSpacing(12);
        for (int i = 1; i <= 40; i++) {
            items.getChildren().add(paragraph("Item " + i + ": the panel scrolls when its content is taller than "
                    + "the window."));
        }
        return items;
    }

    // ---- helpers ----------------------------------------------------------------------------

    /** Creates a drawer, logs its onClose, and queues it to be added over the page. */
    private FxDrawer newDrawer(String name, FxDrawer.Position position, Node... children) {
        FxDrawer drawer = new FxDrawer(children);
        drawer.setPosition(position);
        drawer.setOnClose(() -> log(name + ": onClose fired"));
        drawers.add(drawer);
        return drawer;
    }

    private Button open(String caption, FxDrawer drawer) {
        Button button = new Button(caption);
        button.setOnAction(e -> {
            drawer.setOpen(true);
            log("opened: " + caption);
        });
        return button;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static Label paragraph(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().addAll("text-sm", "text-muted");
        return label;
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-padding: 8 16 8 16;");
        button.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-lg", "text-sm", "font-medium");
        return button;
    }

    private static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-padding: 8 16 8 16;");
        button.getStyleClass().addAll("bg-surface", "text-body", "border", "border-default", "rounded-lg",
                "text-sm", "font-medium");
        return button;
    }

    private static void equalWidth(HBox box) {
        for (Node child : box.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
            ((Region) child).setMaxWidth(Double.MAX_VALUE);
        }
    }

    private static VBox field(String caption, Node input) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-sm", "font-medium", "text-body");
        return new VBox(8, label, input);
    }

    private Hyperlink link(String text) {
        Hyperlink link = new Hyperlink(text);
        link.setOnAction(e -> log("link: " + text));
        return link;
    }

    private static HBox avatarStack() {
        String[] initials = { "AB", "CD", "EF", "GH" };
        String[] colors = { "bg-blue-500", "bg-green-500", "bg-purple-500", "bg-pink-500" };
        HBox stackRow = new HBox(-8);
        for (int i = 0; i < initials.length; i++) {
            Label avatar = new Label(initials[i]);
            avatar.setMinSize(32, 32);
            avatar.setPrefSize(32, 32);
            avatar.setMaxSize(32, 32);
            avatar.setAlignment(Pos.CENTER);
            avatar.setStyle("-fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; "
                    + "-fx-border-radius: 16;");
            avatar.getStyleClass().addAll("text-xs", "font-semibold", "rounded-full", colors[i]);
            stackRow.getChildren().add(avatar);
        }
        return stackRow;
    }

    private static FlowPane row(Node... children) {
        return new FlowPane(10, 10, children);
    }

    private static VBox card(Node... children) {
        VBox box = new VBox(12, children);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
