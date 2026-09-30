package dev.fxkit.showcase;

import java.util.stream.Collectors;

import dev.fxkit.core.components.spinner.FxSpinner;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Isolated test harness for {@link FxSpinner}: default, every Color, every Size, alignment, the
 * light flag in both themes, animation lifecycle (hide/show, detach/attach, many at once), use
 * inside real layouts (button graphic, loading overlay), edge cases, and a live playground.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check the track and arc colors in light and dark.
 */
public class SpinnerTestApp extends Application {

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (lifecycle actions, style classes, exceptions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: color, size, light flag, accessible text, visibility and "
                                + "whether the spinner is in the scene at all."),
                playground(),

                sectionHeading("Default spinner",
                        "new FxSpinner(): medium, default color (theme primary), accessible text \"Loading\"."),
                defaultSection(),

                sectionHeading("Colors (all 16)",
                        "Synonyms (INFO/CYAN, FAILURE/RED, SUCCESS/GREEN, WARNING/YELLOW) should look identical."),
                colorGrid(),

                sectionHeading("Sizing options",
                        "XS 12, SM 16, MD 24, LG 32, XL 40 px. The ring keeps its proportions at every size."),
                sizeSection(),

                sectionHeading("Alignment",
                        "The spinner has a fixed size; alignment comes from the parent container."),
                alignmentSection(),

                sectionHeading("Light flag",
                        "Dark theme only: default spinners get a darker track (and a lighter gray arc); "
                                + "light spinners keep their light-theme colors. Toggle the theme to compare."),
                lightSection(),

                sectionHeading("Animation lifecycle",
                        "The rotation should pause while a spinner is hidden or not in a scene, and resume "
                                + "from where it stopped. Many spinners at once should stay smooth."),
                lifecycleSection(),

                sectionHeading("In context",
                        "Spinner as a button graphic, and as a loading overlay on top of content."),
                inContextSection(),

                sectionHeading("Edge cases",
                        "null via the property, null via the setter, style-class swapping, accessible text."),
                edgeCases(),

                sectionHeading("Event log", "Everything the test buttons report lands here."),
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

        stage.setTitle("FxSpinner test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxSpinner test harness");
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
        FxSpinner spinner = new FxSpinner();
        HBox spinnerHolder = new HBox(spinner);
        spinnerHolder.setAlignment(Pos.CENTER_LEFT);
        spinnerHolder.setMinHeight(48);

        ComboBox<FxSpinner.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxSpinner.Color.values());
        colorBox.valueProperty().bindBidirectional(spinner.colorProperty());

        ComboBox<FxSpinner.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxSpinner.Size.values());
        sizeBox.valueProperty().bindBidirectional(spinner.sizeProperty());

        CheckBox lightCheck = new CheckBox("Light");
        spinner.lightProperty().bind(lightCheck.selectedProperty());

        CheckBox visibleCheck = new CheckBox("Visible");
        visibleCheck.setSelected(true);
        spinner.visibleProperty().bind(visibleCheck.selectedProperty());

        CheckBox inSceneCheck = new CheckBox("In scene");
        inSceneCheck.setSelected(true);
        inSceneCheck.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                spinnerHolder.getChildren().add(spinner);
            } else {
                spinnerHolder.getChildren().remove(spinner);
            }
        });

        TextField labelField = new TextField();
        labelField.setPromptText("accessible text");
        labelField.textProperty().addListener((o, was, is) -> spinner.setAccessibleText(is));
        HBox.setHgrow(labelField, Priority.ALWAYS);

        Label readout = new Label();
        readout.getStyleClass().addAll("text-xs", "text-muted");
        readout.setText("accessibleText = \"" + spinner.getAccessibleText() + "\"");
        spinner.accessibleTextProperty().addListener((o, was, is) ->
                readout.setText("accessibleText = \"" + is + "\""));

        HBox row1 = new HBox(10, new Label("Color"), colorBox, new Label("Size"), sizeBox,
                lightCheck, visibleCheck, inSceneCheck);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(10, new Label("Accessible text"), labelField);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, readout, spinnerHolder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- default ----------------------------------------------------------------------------

    private static Node defaultSection() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("new FxSpinner()", new FxSpinner()));
        flow.getChildren().add(labeled("new FxSpinner(\"Default status example\")",
                new FxSpinner("Default status example")));
        return flow;
    }

    // ---- colors -----------------------------------------------------------------------------

    private static FlowPane colorGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxSpinner.Color c : FxSpinner.Color.values()) {
            flow.getChildren().add(labeled(c.name(), colored(c, FxSpinner.Size.LG)));
        }
        return flow;
    }

    // ---- sizes --------------------------------------------------------------------------------

    private static Node sizeSection() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT); // items-center
        for (FxSpinner.Size size : FxSpinner.Size.values()) {
            FxSpinner spinner = new FxSpinner(size + " spinner example");
            spinner.setSize(size);
            row.getChildren().add(labeled(size.name() + " (" + (int) size.pixels() + "px)", spinner));
        }
        return row;
    }

    // ---- alignment ----------------------------------------------------------------------------

    private static Node alignmentSection() {
        VBox box = new VBox(8,
                aligned(Pos.CENTER_LEFT, "Left-aligned spinner example"),
                aligned(Pos.CENTER, "Center-aligned spinner example"),
                aligned(Pos.CENTER_RIGHT, "Right-aligned spinner example"));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private static Node aligned(Pos pos, String accessibleText) {
        HBox row = new HBox(new FxSpinner(accessibleText));
        row.setAlignment(pos);
        row.getStyleClass().addAll("bg-surface-alt", "p-2", "rounded-md");
        return row;
    }

    // ---- light flag ---------------------------------------------------------------------------

    private static FlowPane lightSection() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("default, light off", colored(FxSpinner.Color.DEFAULT, FxSpinner.Size.LG)));
        flow.getChildren().add(labeled("default, light on", lit(FxSpinner.Color.DEFAULT)));
        flow.getChildren().add(labeled("gray, light off", colored(FxSpinner.Color.GRAY, FxSpinner.Size.LG)));
        flow.getChildren().add(labeled("gray, light on", lit(FxSpinner.Color.GRAY)));
        flow.getChildren().add(labeled("success, light off", colored(FxSpinner.Color.SUCCESS, FxSpinner.Size.LG)));
        flow.getChildren().add(labeled("success, light on", lit(FxSpinner.Color.SUCCESS)));
        return flow;
    }

    private static FxSpinner lit(FxSpinner.Color color) {
        FxSpinner spinner = colored(color, FxSpinner.Size.LG);
        spinner.setLight(true);
        return spinner;
    }

    // ---- lifecycle ----------------------------------------------------------------------------

    private VBox lifecycleSection() {
        // hide / show
        FxSpinner hideable = colored(FxSpinner.Color.INFO, FxSpinner.Size.XL);
        Button hide = new Button("Toggle visible");
        hide.setOnAction(e -> {
            hideable.setVisible(!hideable.isVisible());
            log("hideable visible = " + hideable.isVisible() + " (rotation should pause/resume)");
        });
        HBox hideRow = new HBox(12, hideable, hide);
        hideRow.setAlignment(Pos.CENTER_LEFT);

        // detach / attach
        FxSpinner detachable = colored(FxSpinner.Color.PURPLE, FxSpinner.Size.XL);
        HBox detachHolder = new HBox(detachable);
        detachHolder.setMinWidth(40);
        Button detach = new Button("Detach / attach");
        detach.setOnAction(e -> {
            if (detachHolder.getChildren().contains(detachable)) {
                detachHolder.getChildren().remove(detachable);
            } else {
                detachHolder.getChildren().add(detachable);
            }
            log("detachable in scene = " + (detachable.getScene() != null));
        });
        HBox detachRow = new HBox(12, detachHolder, detach);
        detachRow.setAlignment(Pos.CENTER_LEFT);

        // many at once
        FlowPane many = new FlowPane(6, 6);
        Button add = new Button("Add 100 spinners");
        add.setOnAction(e -> {
            FxSpinner.Color[] colors = FxSpinner.Color.values();
            for (int i = 0; i < 100; i++) {
                many.getChildren().add(colored(colors[i % colors.length], FxSpinner.Size.SM));
            }
            log("spinners on screen: " + many.getChildren().size());
        });
        Button clear = new Button("Clear");
        clear.setOnAction(e -> {
            many.getChildren().clear();
            log("cleared spinners");
        });

        VBox box = new VBox(12, hideRow, detachRow, new HBox(10, add, clear), many);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- in context ---------------------------------------------------------------------------

    private VBox inContextSection() {
        // spinner as a button graphic, the usual "saving..." pattern
        Button save = new Button("Save changes");
        save.setOnAction(e -> {
            FxSpinner busy = new FxSpinner("Saving");
            busy.setSize(FxSpinner.Size.SM);
            busy.setColor(FxSpinner.Color.GRAY);
            save.setGraphic(busy);
            save.setText("Saving...");
            save.setDisable(true);
            log("save: started");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> {
                save.setGraphic(null);
                save.setText("Save changes");
                save.setDisable(false);
                log("save: finished, graphic removed");
            });
            pause.play();
        });

        // loading overlay over content
        Label body = new Label("Some content that is being refreshed. The overlay dims it and centers a spinner.");
        body.setWrapText(true);
        body.getStyleClass().add("text-body");
        VBox card = new VBox(body);
        card.getStyleClass().addAll("bg-surface-alt", "p-4", "rounded-lg");
        card.setPrefHeight(90);

        Region dim = new Region();
        dim.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35); -fx-background-radius: 8px;");
        FxSpinner overlaySpinner = new FxSpinner("Refreshing content");
        overlaySpinner.setSize(FxSpinner.Size.LG);
        StackPane overlay = new StackPane(dim, overlaySpinner);
        overlay.setVisible(false);

        StackPane stack = new StackPane(card, overlay);
        Button toggleOverlay = new Button("Toggle overlay");
        toggleOverlay.setOnAction(e -> {
            overlay.setVisible(!overlay.isVisible());
            log("overlay visible = " + overlay.isVisible());
        });

        VBox box = new VBox(12, new HBox(10, save), stack, toggleOverlay);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxSpinner nullSize = colored(FxSpinner.Color.SUCCESS, FxSpinner.Size.XL);
        Button clearSize = new Button("sizeProperty().set(null)");
        clearSize.setOnAction(e -> {
            nullSize.sizeProperty().set(null);
            log("null size -> classes: " + spinnerClasses(nullSize) + " (expect MD geometry, no size class)");
        });
        flow.getChildren().add(labeled("null size via property falls back to MD",
                new VBox(6, nullSize, clearSize)));

        FxSpinner nullColor = colored(FxSpinner.Color.PINK, FxSpinner.Size.XL);
        Button clearColor = new Button("colorProperty().set(null)");
        clearColor.setOnAction(e -> {
            nullColor.colorProperty().set(null);
            log("null color -> classes: " + spinnerClasses(nullColor) + " (expect theme-primary fallback arc)");
        });
        flow.getChildren().add(labeled("null color via property falls back to primary",
                new VBox(6, nullColor, clearColor)));

        FxSpinner guarded = new FxSpinner();
        Button setterNull = new Button("setSize(null) / setColor(null)");
        setterNull.setOnAction(e -> {
            try {
                guarded.setSize(null);
            } catch (NullPointerException ex) {
                log("setSize(null) threw NPE: " + ex.getMessage());
            }
            try {
                guarded.setColor(null);
            } catch (NullPointerException ex) {
                log("setColor(null) threw NPE: " + ex.getMessage());
            }
        });
        flow.getChildren().add(labeled("setters reject null", new VBox(6, guarded, setterNull)));

        FxSpinner cycling = new FxSpinner("Cycling spinner");
        Button cycle = new Button("Cycle color + size");
        int[] n = {0};
        cycle.setOnAction(e -> {
            FxSpinner.Color[] colors = FxSpinner.Color.values();
            FxSpinner.Size[] sizes = FxSpinner.Size.values();
            n[0]++;
            cycling.setColor(colors[n[0] % colors.length]);
            cycling.setSize(sizes[n[0] % sizes.length]);
            log("cycle #" + n[0] + " -> " + spinnerClasses(cycling) + " (exactly one size and one color)");
        });
        HBox cycleBox = new HBox(12, cycling, cycle);
        cycleBox.setAlignment(Pos.CENTER_LEFT);
        flow.getChildren().add(labeled("rapid style-class swapping (size change while spinning)", cycleBox));

        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static String spinnerClasses(FxSpinner spinner) {
        return spinner.getStyleClass().stream()
                .filter(c -> c.startsWith("fxk-spinner"))
                .collect(Collectors.joining(", "));
    }

    private static FxSpinner colored(FxSpinner.Color color, FxSpinner.Size size) {
        FxSpinner spinner = new FxSpinner(color.name().toLowerCase() + " spinner example");
        spinner.setColor(color);
        spinner.setSize(size);
        return spinner;
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
