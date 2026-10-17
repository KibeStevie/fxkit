package dev.fxkit.showcase;

import dev.fxkit.core.components.form.FxFloatingLabel;
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

import static dev.fxkit.core.components.form.FxFloatingLabel.Color;
import static dev.fxkit.core.components.form.FxFloatingLabel.Size;
import static dev.fxkit.core.components.form.FxFloatingLabel.Variant;

/**
 * Isolated test harness for {@link FxFloatingLabel}: the three variants, disabled state, success and
 * error validation, both sizes, a live playground, initial and programmatic text, runtime
 * property changes and edge cases.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check light and dark. Things to look for in every section:
 * the label rests in the field while empty and unfocused, floats over 300ms on focus or when text
 * is present, and drops back when the field is emptied and loses focus.
 */
public class FloatingLabelTestApp extends Application {

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (text changes, Enter pressed)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: edit the label, switch variant/size/color, disable, "
                                + "pre-fill text. The readout shows the text property."),
                playground(),

                sectionHeading("Floating label example",
                        "The three variants. Click a field, or type: the label floats up and takes the "
                                + "primary color while focused."),
                panel(row(
                        new FxFloatingLabel(Variant.FILLED, "Label"),
                        new FxFloatingLabel(Variant.OUTLINED, "Label"),
                        new FxFloatingLabel(Variant.STANDARD, "Label"))),

                sectionHeading("Disabled state",
                        "Dimmed to 50%, no focus, no typing. The third one carries text, so its label "
                                + "starts floated."),
                disabledSection(),

                sectionHeading("Validation",
                        "SUCCESS is green and ERROR is red in every variant; the color stays on focus."),
                validationSection(),

                sectionHeading("Sizes",
                        "SM (text-xs, shorter) above MD (text-sm, the default)."),
                sizesSection(),

                sectionHeading("Initial and programmatic text",
                        "A field created with text starts floated (no animation). Fill and Clear "
                                + "animate the label."),
                textSection(),

                sectionHeading("Runtime changes",
                        "One field, restyled while it holds text and focus: the old style classes "
                                + "must be swapped out, never stacked."),
                runtimeSection(),

                sectionHeading("Edge cases",
                        "Long labels in narrow fields, label changes while floated, whitespace-only "
                                + "text, fixed width next to growing width."),
                edgeCases(),

                sectionHeading("Event log", "Text changes and Enter presses land here."),
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

        stage.setTitle("FxFloatingLabel test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxFloatingLabel test harness");
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
        FxFloatingLabel field = new FxFloatingLabel(Variant.OUTLINED, "Playground label");
        field.setMaxWidth(360);

        TextField labelField = new TextField("Playground label");
        labelField.setPromptText("label");
        field.labelProperty().bind(labelField.textProperty());

        ComboBox<Variant> variantBox = new ComboBox<>();
        variantBox.getItems().addAll(Variant.values());
        variantBox.valueProperty().bindBidirectional(field.variantProperty());

        ComboBox<Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(Size.values());
        sizeBox.valueProperty().bindBidirectional(field.sizeProperty());

        ComboBox<Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(Color.values());
        colorBox.valueProperty().bindBidirectional(field.colorProperty());

        CheckBox disabledCheck = new CheckBox("Disabled");
        field.disableProperty().bind(disabledCheck.selectedProperty());

        Button fill = new Button("Set text");
        fill.setOnAction(e -> field.setText("Set from code"));
        Button clear = new Button("Clear text");
        clear.setOnAction(e -> field.setText(""));

        Label readout = new Label();
        readout.getStyleClass().addAll("text-sm", "text-muted");
        readout.textProperty().bind(Bindings.concat("text = \"", field.textProperty(), "\""));

        HBox row1 = new HBox(10, new Label("Label"), labelField, new Label("Variant"), variantBox,
                new Label("Size"), sizeBox, new Label("Color"), colorBox);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, disabledCheck, fill, clear);
        row2.setAlignment(Pos.CENTER_LEFT);

        return panel(row1, row2, field, readout);
    }

    // ---- disabled -----------------------------------------------------------------------------

    private VBox disabledSection() {
        FxFloatingLabel filled = new FxFloatingLabel(Variant.FILLED, "Label");
        FxFloatingLabel outlined = new FxFloatingLabel(Variant.OUTLINED, "Label");
        FxFloatingLabel standard = new FxFloatingLabel(Variant.STANDARD, "Label");
        standard.setText("Disabled with text");
        FxFloatingLabel[] all = { filled, outlined, standard };
        for (FxFloatingLabel f : all) {
            f.setDisable(true);
        }

        Button toggle = new Button("Toggle disabled");
        toggle.setOnAction(e -> {
            for (FxFloatingLabel f : all) {
                f.setDisable(!f.isDisable());
            }
        });
        return panel(row(filled, outlined, standard), toggle);
    }

    // ---- validation ---------------------------------------------------------------------------

    private VBox validationSection() {
        return panel(
                labeledRow("success",
                        colored(Variant.FILLED, "Filled Success", Color.SUCCESS),
                        colored(Variant.OUTLINED, "Outlined Success", Color.SUCCESS),
                        colored(Variant.STANDARD, "Standard Success", Color.SUCCESS)),
                labeledRow("error",
                        colored(Variant.FILLED, "Filled Error", Color.ERROR),
                        colored(Variant.OUTLINED, "Outlined Error", Color.ERROR),
                        colored(Variant.STANDARD, "Standard Error", Color.ERROR)),
                labeledRow("success / error with text (floated from the start)",
                        withText(colored(Variant.FILLED, "Valid", Color.SUCCESS), "hello@example.com"),
                        withText(colored(Variant.OUTLINED, "Invalid", Color.ERROR), "not-an-email"),
                        withText(colored(Variant.STANDARD, "Invalid", Color.ERROR), "x")));
    }

    // ---- sizes --------------------------------------------------------------------------------

    private VBox sizesSection() {
        return panel(
                labeledRow("SM",
                        sized(Variant.FILLED, "Small Filled", Size.SM),
                        sized(Variant.OUTLINED, "Small Outlined", Size.SM),
                        sized(Variant.STANDARD, "Small Standard", Size.SM)),
                labeledRow("MD (default)",
                        new FxFloatingLabel(Variant.FILLED, "Default Filled"),
                        new FxFloatingLabel(Variant.OUTLINED, "Default Outlined"),
                        new FxFloatingLabel(Variant.STANDARD, "Default Standard")));
    }

    // ---- initial / programmatic text ----------------------------------------------------------

    private VBox textSection() {
        FxFloatingLabel prefilled = new FxFloatingLabel(Variant.OUTLINED, "Pre-filled");
        prefilled.setText("Created with text");
        prefilled.getInput().setOnAction(e -> log("pre-filled: Enter pressed"));
        prefilled.textProperty().addListener((o, was, is) -> log("pre-filled text: \"" + is + "\""));

        FxFloatingLabel target = new FxFloatingLabel(Variant.FILLED, "Target");
        Button fill = new Button("Fill");
        fill.setOnAction(e -> target.setText("Filled from code"));
        Button clear = new Button("Clear");
        clear.setOnAction(e -> target.setText(""));
        Button focus = new Button("requestFocus()");
        focus.setOnAction(e -> target.requestFocus());

        return panel(row(prefilled, target), new HBox(10, fill, clear, focus));
    }

    // ---- runtime changes ----------------------------------------------------------------------

    private VBox runtimeSection() {
        FxFloatingLabel field = new FxFloatingLabel(Variant.FILLED, "Restyle me");
        field.setText("Typed text");
        field.setMaxWidth(320);

        Button variant = new Button("Cycle variant");
        variant.setOnAction(e -> field.setVariant(next(Variant.values(), field.getVariant())));
        Button size = new Button("Cycle size");
        size.setOnAction(e -> field.setSize(next(Size.values(), field.getSize())));
        Button color = new Button("Cycle color");
        color.setOnAction(e -> field.setColor(next(Color.values(), field.getColor())));
        Button nullColor = new Button("color = null");
        nullColor.setOnAction(e -> field.setColor(null));
        Button relabel = new Button("Change label");
        int[] n = { 0 };
        relabel.setOnAction(e -> field.setLabel("Label #" + (++n[0])));

        return panel(field, new HBox(10, variant, size, color, nullColor, relabel));
    }

    private static <E extends Enum<E>> E next(E[] values, E current) {
        return values[current == null ? 0 : (current.ordinal() + 1) % values.length];
    }

    // ---- edge cases ---------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxFloatingLabel narrow = new FxFloatingLabel(Variant.OUTLINED,
                "A deliberately very long label that cannot fit");
        narrow.setPrefWidth(180);
        narrow.setMaxWidth(180);
        flow.getChildren().add(labeled("long label, 180px wide (should clip, not overflow)", narrow));

        FxFloatingLabel blank = new FxFloatingLabel(Variant.STANDARD, "Whitespace-only text");
        blank.setText("   ");
        flow.getChildren().add(labeled("text = \"   \" (counts as text, label floated)", blank));

        flow.getChildren().add(labeled("empty label", new FxFloatingLabel(Variant.FILLED, "")));
        flow.getChildren().add(labeled("null label (treated as empty)", new FxFloatingLabel(Variant.OUTLINED, null)));

        FxFloatingLabel fixed = new FxFloatingLabel(Variant.FILLED, "Fixed 140px");
        fixed.setPrefWidth(140);
        fixed.setMaxWidth(140);
        FxFloatingLabel grow = new FxFloatingLabel(Variant.FILLED, "Grows");
        HBox mixed = new HBox(12, fixed, grow);
        mixed.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(grow, Priority.ALWAYS);
        mixed.setPrefWidth(420);
        flow.getChildren().add(labeled("fixed + growing in one row", mixed));

        FxFloatingLabel viaInput = new FxFloatingLabel(Variant.OUTLINED, "Edited via getInput()");
        viaInput.getInput().setEditable(false);
        viaInput.setText("Read-only (setEditable(false))");
        flow.getChildren().add(labeled("getInput().setEditable(false)", viaInput));

        return flow;
    }

    // ---- helpers ------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxFloatingLabel colored(Variant variant, String label, Color color) {
        FxFloatingLabel f = new FxFloatingLabel(variant, label);
        f.setColor(color);
        return f;
    }

    private static FxFloatingLabel sized(Variant variant, String label, Size size) {
        FxFloatingLabel f = new FxFloatingLabel(variant, label);
        f.setSize(size);
        return f;
    }

    private static FxFloatingLabel withText(FxFloatingLabel f, String text) {
        f.setText(text);
        return f;
    }

    /** Equal-width columns, bottoms aligned (the OUTLINED/STANDARD variants reserve a strip on top). */
    private static HBox row(Node... fields) {
        HBox row = new HBox(16, fields);
        row.setAlignment(Pos.BOTTOM_LEFT);
        for (Node field : fields) {
            HBox.setHgrow(field, Priority.ALWAYS);
        }
        return row;
    }

    private static VBox labeledRow(String caption, Node... fields) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        return new VBox(4, label, row(fields));
    }

    private static VBox panel(Node... children) {
        VBox box = new VBox(12, children);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
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
