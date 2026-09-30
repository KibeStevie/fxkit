package dev.fxkit.showcase;

import dev.fxkit.core.components.FxAccordion;
import dev.fxkit.core.components.FxAccordionContent;
import dev.fxkit.core.components.FxAccordionPanel;
import dev.fxkit.core.components.FxAccordionTitle;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxAccordion}: default behavior, collapseAll, alwaysOpen, flush,
 * explicit initial state, title graphics, custom content, wrapping, dynamic panel changes
 * (add / remove / reorder), standalone panels, keyboard use, and a live playground.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check the accordion in light and dark.
 */
public class AccordionTestApp extends Application {

    private static final String LOREM =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor "
                    + "incididunt ut labore et dolore magna aliqua.";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;
    private int panelCounter;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(6);
        log.setPromptText("Event log (panel opened / closed)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Flip collapseAll / alwaysOpen / flush live, add, remove and reorder panels, "
                                + "open or close them from code. The state line shows which panels are open."),
                playground(),

                sectionHeading("Default behavior",
                        "First panel starts open. Opening one closes the others. Clicking the open panel "
                                + "closes it, so all can be closed."),
                defaultSection(),

                sectionHeading("collapseAll",
                        "Left: set BEFORE adding panels. Right: set AFTER construction. Both must start fully "
                                + "closed, and panels must still be openable."),
                collapseAllSection(),

                sectionHeading("alwaysOpen",
                        "Panels open independently. Untick it in the playground to check only the first "
                                + "open panel stays open."),
                alwaysOpenSection(),

                sectionHeading("flush",
                        "No side borders or outer rounding, bottom border only."),
                flushSection(),

                sectionHeading("Explicit initial state",
                        "Panel 2 opened before construction: panel 1 must NOT be forced open. Next to it, "
                                + "two panels pre-opened in exclusive mode: only the first of them stays open. "
                                + "Right: alwaysOpen with panels 2 and 3 pre-opened keeps both."),
                initialStateSection(),

                sectionHeading("Title graphic",
                        "setGraphic(node) puts a node before the text. Check alignment, chevron position, "
                                + "and that replacing or clearing the graphic works."),
                graphicSection(),

                sectionHeading("Custom content",
                        "FxAccordionContent built from arbitrary nodes. Clicking inside the content must not "
                                + "toggle the panel."),
                customContentSection(),

                sectionHeading("Wrapping / width",
                        "Long titles and text wrap, the chevron stays pinned to the far end. "
                                + "Drag the slider to narrow the accordion."),
                widthSection(),

                sectionHeading("Dynamic panels",
                        "Accordion starts empty; the initial state (first panel open) is applied when the first "
                                + "panels arrive. Add / remove / reverse via getPanels() and watch the first/last "
                                + "corner and divider styling update."),
                dynamicSection(),

                sectionHeading("Edge cases",
                        "Single panel, empty content, empty title, panel used without an accordion."),
                edgeCases(),

                sectionHeading("Keyboard",
                        "Tab to a title (focus ring), Space / Enter toggles it. Shift+Tab goes back. "
                                + "Try it on each accordion above."),
                new Region(),

                sectionHeading("Event log", "Every open / close from tracked accordions lands here."),
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

        stage.setTitle("FxAccordion test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxAccordion test harness");
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
        FxAccordion accordion = new FxAccordion(newPanel(), newPanel(), newPanel(), newPanel());
        track("playground", accordion);

        CheckBox collapseAll = new CheckBox("collapseAll");
        collapseAll.selectedProperty().bindBidirectional(accordion.collapseAllProperty());
        CheckBox alwaysOpen = new CheckBox("alwaysOpen");
        alwaysOpen.selectedProperty().bindBidirectional(accordion.alwaysOpenProperty());
        CheckBox flush = new CheckBox("flush");
        flush.selectedProperty().bindBidirectional(accordion.flushProperty());

        Button add = new Button("Add panel");
        add.setOnAction(e -> accordion.getPanels().add(newPanel()));

        Button addFirst = new Button("Add at start");
        addFirst.setOnAction(e -> accordion.getPanels().add(0, newPanel()));

        Button removeFirst = new Button("Remove first");
        removeFirst.setOnAction(e -> {
            if (!accordion.getPanels().isEmpty()) {
                accordion.getPanels().remove(0);
            }
        });

        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!accordion.getPanels().isEmpty()) {
                accordion.getPanels().remove(accordion.getPanels().size() - 1);
            }
        });

        Button reverse = new Button("Reverse order");
        reverse.setOnAction(e -> FXCollections.reverse(accordion.getPanels()));

        Button openAll = new Button("Open all (from code)");
        openAll.setOnAction(e -> accordion.getPanels().forEach(p -> p.setOpen(true)));

        Button closeAll = new Button("Close all (from code)");
        closeAll.setOnAction(e -> accordion.getPanels().forEach(p -> p.setOpen(false)));

        Button toggleFirst = new Button("toggle() first");
        toggleFirst.setOnAction(e -> {
            if (!accordion.getPanels().isEmpty()) {
                accordion.getPanels().get(0).toggle();
            }
        });

        HBox flags = row(16, new Label("Flags"), collapseAll, alwaysOpen, flush);
        FlowPane actions = new FlowPane(10, 10, add, addFirst, removeFirst, removeLast, reverse,
                openAll, closeAll, toggleFirst);

        VBox box = new VBox(12, flags, actions, openState(accordion), accordion);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- default ----------------------------------------------------------------------------------

    private VBox defaultSection() {
        FxAccordion accordion = new FxAccordion(
                new FxAccordionPanel("What is FXKit?", "A JavaFX component library."),
                new FxAccordionPanel("Is there a dark theme?", "Yes: ThemeManager.apply(scene, Theme.DARK)."),
                new FxAccordionPanel("Can I customize it?", "Yes, through semantic tokens in tokens.css."));
        track("default", accordion);
        return labeled("default: first open, exclusive", accordion, openState(accordion));
    }

    // ---- collapseAll ------------------------------------------------------------------------------

    private FlowPane collapseAllSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxAccordion before = new FxAccordion();
        before.setCollapseAll(true);
        before.getPanels().addAll(newPanel(), newPanel(), newPanel());
        track("collapseAll-before", before);
        flow.getChildren().add(sizedLabeled("collapseAll set before adding panels", before, 420));

        FxAccordion after = new FxAccordion(newPanel(), newPanel(), newPanel());
        after.setCollapseAll(true);
        track("collapseAll-after", after);
        flow.getChildren().add(sizedLabeled("collapseAll set after construction", after, 420));

        FxAccordion toggling = new FxAccordion(newPanel(), newPanel(), newPanel());
        track("collapseAll-toggle", toggling);
        Button collapse = new Button("setCollapseAll(true)");
        collapse.setOnAction(e -> {
            // Property only fires on change, so reset first to make the button repeatable.
            toggling.setCollapseAll(false);
            toggling.setCollapseAll(true);
        });
        flow.getChildren().add(sizedLabeled("runtime collapse (click panels open first)",
                new VBox(8, toggling, collapse), 420));
        return flow;
    }

    // ---- alwaysOpen -------------------------------------------------------------------------------

    private VBox alwaysOpenSection() {
        FxAccordion accordion = new FxAccordion(newPanel(), newPanel(), newPanel());
        accordion.setAlwaysOpen(true);
        track("alwaysOpen", accordion);

        CheckBox toggle = new CheckBox("alwaysOpen");
        toggle.selectedProperty().bindBidirectional(accordion.alwaysOpenProperty());

        return labeled("alwaysOpen: open several, then untick", toggle, openState(accordion), accordion);
    }

    // ---- flush ------------------------------------------------------------------------------------

    private FlowPane flushSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxAccordion normal = new FxAccordion(newPanel(), newPanel(), newPanel());
        flow.getChildren().add(sizedLabeled("normal", normal, 420));

        FxAccordion flush = new FxAccordion(newPanel(), newPanel(), newPanel());
        flush.setFlush(true);
        flow.getChildren().add(sizedLabeled("flush", flush, 420));

        FxAccordion runtime = new FxAccordion(newPanel(), newPanel(), newPanel());
        Button toggle = new Button("Toggle flush");
        toggle.setOnAction(e -> runtime.setFlush(!runtime.isFlush()));
        flow.getChildren().add(sizedLabeled("runtime flush toggle", new VBox(8, runtime, toggle), 420));
        return flow;
    }

    // ---- explicit initial state ------------------------------------------------------------------

    private FlowPane initialStateSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxAccordionPanel a1 = newPanel();
        FxAccordionPanel a2 = newPanel();
        FxAccordionPanel a3 = newPanel();
        a2.setOpen(true);
        FxAccordion explicit = new FxAccordion(a1, a2, a3);
        track("initial-explicit", explicit);
        flow.getChildren().add(sizedLabeled("panel 2 pre-opened (expect only 2 open)", explicit, 420));

        FxAccordionPanel b1 = newPanel();
        FxAccordionPanel b2 = newPanel();
        FxAccordionPanel b3 = newPanel();
        b2.setOpen(true);
        b3.setOpen(true);
        FxAccordion exclusive = new FxAccordion(b1, b2, b3);
        track("initial-exclusive", exclusive);
        flow.getChildren().add(sizedLabeled("panels 2 + 3 pre-opened, exclusive (expect only 2)", exclusive, 420));

        FxAccordionPanel c1 = newPanel();
        FxAccordionPanel c2 = newPanel();
        FxAccordionPanel c3 = newPanel();
        c2.setOpen(true);
        c3.setOpen(true);
        FxAccordion always = new FxAccordion();
        always.setAlwaysOpen(true);
        always.getPanels().addAll(c1, c2, c3);
        track("initial-alwaysOpen", always);
        flow.getChildren().add(sizedLabeled("alwaysOpen, 2 + 3 pre-opened (expect 2 and 3)", always, 420));
        return flow;
    }

    // ---- title graphic ----------------------------------------------------------------------------

    private FlowPane graphicSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxAccordionTitle t1 = new FxAccordionTitle("Title with a leading icon");
        t1.setGraphic(dot(Color.DODGERBLUE));
        FxAccordionTitle t2 = new FxAccordionTitle("Another icon title");
        t2.setGraphic(dot(Color.SEAGREEN));
        FxAccordionTitle t3 = new FxAccordionTitle("No icon for comparison");

        FxAccordion accordion = new FxAccordion(
                new FxAccordionPanel(t1, new FxAccordionContent("Content under an icon title.")),
                new FxAccordionPanel(t2, new FxAccordionContent("More content.")),
                new FxAccordionPanel(t3, new FxAccordionContent("Plain title.")));
        track("graphic", accordion);
        flow.getChildren().add(sizedLabeled("graphics before text", accordion, 420));

        FxAccordionTitle live = new FxAccordionTitle("Runtime graphic");
        FxAccordion runtime = new FxAccordion(new FxAccordionPanel(live,
                new FxAccordionContent("Use the buttons to set, replace, or clear the graphic.")));
        Button set = new Button("Set graphic");
        set.setOnAction(e -> live.setGraphic(dot(Color.ORANGE)));
        Button replace = new Button("Replace graphic");
        replace.setOnAction(e -> live.setGraphic(dot(Color.CRIMSON)));
        Button clear = new Button("Clear graphic");
        clear.setOnAction(e -> live.setGraphic(null));
        Button retitle = new Button("Change text");
        retitle.setOnAction(e -> live.setText("Runtime title " + System.nanoTime() % 1000));
        flow.getChildren().add(sizedLabeled("runtime graphic / text",
                new VBox(8, runtime, new HBox(8, set, replace, clear, retitle)), 420));
        return flow;
    }

    // ---- custom content ---------------------------------------------------------------------------

    private VBox customContentSection() {
        Button inner = new Button("Inner button");
        inner.setOnAction(e -> log("custom content: inner button clicked"));
        TextField field = new TextField();
        field.setPromptText("Click / type here, the panel must stay open");

        FxAccordionContent nodes = new FxAccordionContent(
                FxAccordionContent.paragraph("Mixed content: a paragraph, a text field and a button."),
                field, inner);

        FxAccordionContent multi = new FxAccordionContent(
                "First paragraph of several.", "Second paragraph.", "Third paragraph.");

        FxAccordion accordion = new FxAccordion(
                new FxAccordionPanel("Nodes inside", nodes),
                new FxAccordionPanel("Several paragraphs", multi),
                new FxAccordionPanel("Plain string content", "One wrapping muted paragraph."));
        accordion.setAlwaysOpen(true);
        track("custom", accordion);
        return labeled("custom nodes (alwaysOpen so you can compare)", accordion);
    }

    // ---- wrapping / width -------------------------------------------------------------------------

    private VBox widthSection() {
        FxAccordion accordion = new FxAccordion(
                new FxAccordionPanel(
                        "A deliberately long accordion title that should wrap onto several lines instead "
                                + "of overflowing while the chevron stays on the right",
                        LOREM + " " + LOREM),
                new FxAccordionPanel("Short title", LOREM),
                new FxAccordionPanel("Second long title to check wrapping in a closed panel as well as in an "
                        + "open one, at any width", LOREM));
        accordion.setAlwaysOpen(true);
        accordion.getPanels().forEach(p -> p.setOpen(true));

        Slider slider = new Slider(180, 700, 420);
        slider.setPrefWidth(300);
        accordion.setMaxWidth(slider.getValue());
        slider.valueProperty().addListener((o, was, is) -> accordion.setMaxWidth(is.doubleValue()));

        Label width = new Label();
        width.getStyleClass().addAll("text-xs", "text-muted");
        width.textProperty().bind(slider.valueProperty().asString("max width: %.0f px"));

        VBox holder = new VBox(accordion);
        holder.setAlignment(Pos.TOP_LEFT);
        return labeled("narrow me", row(10, new Label("Width"), slider, width), holder);
    }

    // ---- dynamic ----------------------------------------------------------------------------------

    private VBox dynamicSection() {
        FxAccordion accordion = new FxAccordion();
        track("dynamic", accordion);

        Button addThree = new Button("Add 3 panels");
        addThree.setOnAction(e -> accordion.getPanels().addAll(newPanel(), newPanel(), newPanel()));
        Button clear = new Button("Clear all panels");
        clear.setOnAction(e -> accordion.getPanels().clear());
        Button replace = new Button("setAll (replace with 2 new)");
        replace.setOnAction(e -> accordion.getPanels().setAll(newPanel(), newPanel()));
        Button swap = new Button("Move last to front");
        swap.setOnAction(e -> {
            if (accordion.getPanels().size() > 1) {
                FxAccordionPanel last = accordion.getPanels().remove(accordion.getPanels().size() - 1);
                accordion.getPanels().add(0, last);
            }
        });

        return labeled("starts empty", new FlowPane(10, 10, addThree, clear, replace, swap),
                openState(accordion), accordion);
    }

    // ---- edge cases -------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxAccordion single = new FxAccordion(new FxAccordionPanel("Only panel", "First and last at once."));
        track("edge-single", single);
        flow.getChildren().add(sizedLabeled("single panel (first + last corners)", single, 420));

        FxAccordion emptyContent = new FxAccordion(
                new FxAccordionPanel(new FxAccordionTitle("Empty content"), new FxAccordionContent()),
                new FxAccordionPanel(new FxAccordionTitle(""), new FxAccordionContent("Panel with empty title.")));
        flow.getChildren().add(sizedLabeled("empty content / empty title", emptyContent, 420));

        FxAccordionPanel standalone = new FxAccordionPanel("Standalone panel (no accordion)",
                "No exclusivity, no first/last styling, but click and keyboard toggle still work.");
        standalone.openProperty().addListener((o, was, is) -> log("standalone: " + (is ? "opened" : "closed")));
        Button toggle = new Button("toggle() from code");
        toggle.setOnAction(e -> standalone.toggle());
        flow.getChildren().add(sizedLabeled("panel used alone", new VBox(8, standalone, toggle), 420));

        FxAccordion emptyAccordion = new FxAccordion();
        Button addLater = new Button("Add a panel");
        addLater.setOnAction(e -> emptyAccordion.getPanels().add(newPanel()));
        flow.getChildren().add(sizedLabeled("empty accordion, then add",
                new VBox(8, emptyAccordion, addLater), 420));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private FxAccordionPanel newPanel() {
        int n = ++panelCounter;
        return new FxAccordionPanel("Panel " + n, "Body of panel " + n + ". " + LOREM);
    }

    private static Circle dot(Color color) {
        return new Circle(6, color);
    }

    /** Logs every open/close of panels in the accordion, including panels added later. */
    private void track(String name, FxAccordion accordion) {
        accordion.getPanels().forEach(p -> attachTracker(name, p));
        accordion.getPanels().addListener((ListChangeListener<FxAccordionPanel>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(p -> attachTracker(name, p));
            }
        });
    }

    private void attachTracker(String name, FxAccordionPanel panel) {
        String key = "tracked-" + name;
        if (panel.getProperties().putIfAbsent(key, Boolean.TRUE) != null) {
            return;
        }
        panel.openProperty().addListener((o, was, is) ->
                log(name + ": \"" + panel.getTitle().getText() + "\" " + (is ? "opened" : "closed")));
    }

    /** A live "Open: 1, 3" line for the accordion. */
    private static Label openState(FxAccordion accordion) {
        Label label = new Label();
        label.getStyleClass().addAll("text-xs", "text-muted");

        InvalidationListener update = o -> {
            StringBuilder open = new StringBuilder();
            for (int i = 0; i < accordion.getPanels().size(); i++) {
                if (accordion.getPanels().get(i).isOpen()) {
                    open.append(open.isEmpty() ? "" : ", ").append(i + 1);
                }
            }
            label.setText("Panels: " + accordion.getPanels().size()
                    + "   Open: " + (open.isEmpty() ? "none" : open));
        };

        accordion.getPanels().forEach(p -> p.openProperty().addListener(update));
        accordion.getPanels().addListener((ListChangeListener<FxAccordionPanel>) change -> {
            while (change.next()) {
                change.getRemoved().forEach(p -> p.openProperty().removeListener(update));
                change.getAddedSubList().forEach(p -> p.openProperty().addListener(update));
            }
            update.invalidated(null);
        });
        update.invalidated(null);
        return label;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static HBox row(double spacing, Node... nodes) {
        HBox box = new HBox(spacing, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        subtitleLabel.setWrapText(true);
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox labeled(String caption, Node... content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        VBox wrapper = new VBox(8);
        wrapper.getChildren().add(label);
        wrapper.getChildren().addAll(content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    private static VBox sizedLabeled(String caption, Node content, double width) {
        VBox box = labeled(caption, content);
        box.setPrefWidth(width);
        box.setMaxWidth(width);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
