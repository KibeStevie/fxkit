package dev.fxkit.showcase;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import dev.fxkit.core.components.form.FxFileDropzone;
import dev.fxkit.core.components.form.FxFileInput;
import dev.fxkit.core.components.form.FxHelperText;
import dev.fxkit.core.components.form.FxLabel;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxFileInput} and {@link FxFileDropzone}, following Flowbite React's
 * file-input docs one section at a time (single file, helper text, multiple, sizes, dropzone), plus
 * validation colors, extension filters, programmatic control, edge cases, and a live playground.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check light and dark. To test dropping, drag files from your file
 * manager onto any field; with an extension filter set, wrong types land in the event log as "rejected".
 */
public class FileInputTestApp extends Application {

    private static final double FIELD_WIDTH = 380;
    private static final String HELPER = "SVG, PNG, JPG or GIF (MAX. 800x400px).";

    private static final FileChooser.ExtensionFilter IMAGES = new FileChooser.ExtensionFilter(
            "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg");

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(6);
        log.setPromptText("Event log (picks, drops, rejected files)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live on one field and one dropzone: size, color, shadow, multiple, "
                                + "disabled, image filter, button text and placeholder."),
                playground(),

                sectionHeading("File upload example",
                        "A plain FxFileInput with an FxLabel whose labelFor is the field (Flowbite's htmlFor): "
                                + "clicking the label opens the dialog."),
                singleSection(),

                sectionHeading("Helper text",
                        "FxHelperText under the field, to say which extensions and sizes are allowed."),
                helperSection(),

                sectionHeading("Multiple files",
                        "multiple = true: the dialog allows several files and the name part shows the count."),
                multipleSection(),

                sectionHeading("Sizes",
                        "SM text-xs, MD text-sm (default), LG text-lg. Heights should read 38 / 42 / 50px."),
                sizesSection(),

                sectionHeading("Dropzone",
                        "FxFileDropzone: click anywhere or drop files. Hover, focus (Tab) and drag-over each "
                                + "restyle the border."),
                dropzoneSection(),

                sectionHeading("Colors / validation",
                        "GRAY plus the four validation colors, each with a matching FxLabel and FxHelperText."),
                colorSection(),

                sectionHeading("Extension filters",
                        "Images only: the dialog is filtered and dropped files are checked. Drop a PDF to see it "
                                + "land in the log as rejected."),
                filterSection(),

                sectionHeading("Shadow and disabled",
                        "shadow adds Flowbite's shadow-sm; a disabled field is dimmed and ignores clicks, "
                                + "keys and drops."),
                shadowDisabledSection(),

                sectionHeading("Programmatic control",
                        "choose(), clear(), buttonText and placeholder changed at runtime; "
                                + "turning multiple off trims the selection to the first file."),
                programmaticSection(),

                sectionHeading("Edge cases",
                        "Narrow field with a long placeholder, compact dropzone, empty dropzone texts, "
                                + "rapid size/color swaps."),
                edgeCases(),

                sectionHeading("Event log", "Selections, rejected drops and label clicks all report here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1040, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxFileInput test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxFileInput / FxFileDropzone test harness");
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
        FxFileInput input = newInput("playground input");
        FxLabel label = new FxLabel("Upload file", input);
        FxHelperText helper = new FxHelperText(HELPER);
        FxFileDropzone zone = newZone("playground dropzone");
        zone.setHint(HELPER);
        zone.multipleProperty().bind(input.multipleProperty());

        ComboBox<FxFileInput.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxFileInput.Size.values());
        sizeBox.valueProperty().bindBidirectional(input.sizeProperty());

        ComboBox<FxFileInput.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxFileInput.Color.values());
        colorBox.valueProperty().bindBidirectional(input.colorProperty());
        // Keep the label and helper text in step with the field, as Flowbite's validation look does.
        input.colorProperty().addListener((o, was, is) -> {
            boolean gray = is == null || is == FxFileInput.Color.GRAY;
            label.setColor(gray ? FxLabel.Color.DEFAULT : FxLabel.Color.valueOf(is.name()));
            helper.setColor(gray ? FxHelperText.Color.DEFAULT : FxHelperText.Color.valueOf(is.name()));
        });

        CheckBox shadowCheck = new CheckBox("Shadow");
        input.shadowProperty().bind(shadowCheck.selectedProperty());

        CheckBox multipleCheck = new CheckBox("Multiple");
        input.multipleProperty().bind(multipleCheck.selectedProperty());

        CheckBox filterCheck = new CheckBox("Images only");
        filterCheck.selectedProperty().addListener((o, was, is) -> {
            input.getExtensionFilters().clear();
            zone.getExtensionFilters().clear();
            if (is) {
                input.getExtensionFilters().add(IMAGES);
                zone.getExtensionFilters().add(IMAGES);
            }
        });

        VBox preview = new VBox(0, stack(label, input, helper), spaced(zone, 16));
        CheckBox disabledCheck = new CheckBox("Disabled");
        preview.disableProperty().bind(disabledCheck.selectedProperty());

        TextField buttonField = new TextField();
        buttonField.setPromptText("button text");
        input.buttonTextProperty().bindBidirectional(buttonField.textProperty());
        TextField placeholderField = new TextField();
        placeholderField.setPromptText("placeholder");
        input.placeholderProperty().bindBidirectional(placeholderField.textProperty());
        HBox.setHgrow(buttonField, Priority.SOMETIMES);
        HBox.setHgrow(placeholderField, Priority.ALWAYS);

        Button clear = new Button("Clear");
        clear.setOnAction(e -> {
            input.clear();
            zone.clear();
        });

        HBox row1 = new HBox(10, new Label("Button"), buttonField, new Label("Placeholder"), placeholderField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, new Label("Size"), sizeBox, new Label("Color"), colorBox, shadowCheck,
                multipleCheck, filterCheck, disabledCheck, clear);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, preview);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- Flowbite: file upload example ---------------------------------------------------------

    private VBox singleSection() {
        FxFileInput input = newInput("single");
        FxLabel label = new FxLabel("Upload file", input);
        return card("FxFileInput + FxLabel", stack(label, input, null));
    }

    // ---- Flowbite: helper text -----------------------------------------------------------------

    private VBox helperSection() {
        FxFileInput input = newInput("helper text");
        FxLabel label = new FxLabel("Upload file", input);
        return card("FxFileInput + FxHelperText", stack(label, input, new FxHelperText(HELPER)));
    }

    // ---- Flowbite: multiple files --------------------------------------------------------------

    private VBox multipleSection() {
        FxFileInput input = newInput("multiple");
        input.setMultiple(true);
        FxLabel label = new FxLabel("Upload multiple files", input);
        return card("multiple = true", stack(label, input, null));
    }

    // ---- Flowbite: sizes -----------------------------------------------------------------------

    private VBox sizesSection() {
        FxFileInput small = newInput("small");
        small.setSize(FxFileInput.Size.SM);
        FxFileInput medium = newInput("default");
        FxFileInput large = newInput("large");
        large.setSize(FxFileInput.Size.LG);

        VBox box = new VBox(20,
                stack(new FxLabel("Small file input", small), small, null),
                stack(new FxLabel("Default size file input", medium), medium, null),
                stack(new FxLabel("Large file input", large), large, null));
        return card("SM / MD / LG", box);
    }

    // ---- Flowbite: dropzone --------------------------------------------------------------------

    private FlowPane dropzoneSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxFileDropzone basic = newZone("dropzone");
        basic.setHint("SVG, PNG, JPG or GIF (MAX. 800x400px)");
        flow.getChildren().add(card("default dropzone", basic));

        FxFileDropzone many = newZone("dropzone multiple");
        many.setMultiple(true);
        many.setHint("Any number of files");
        flow.getChildren().add(card("multiple = true", many));

        FxFileDropzone images = newZone("dropzone images");
        images.getExtensionFilters().add(IMAGES);
        images.setHint("Images only: PNG, JPG, GIF or SVG");
        flow.getChildren().add(card("images only (non-images are rejected)", images));

        FxFileDropzone texts = newZone("dropzone custom texts");
        texts.setActionText("Browse your computer");
        texts.setPromptText("or drop a file here");
        flow.getChildren().add(card("custom action / prompt text, no hint", texts));

        FxFileDropzone label = newZone("dropzone via label");
        label.setHint("The label below opens the dialog too");
        FxLabel htmlFor = new FxLabel("Click this label (labelFor = dropzone)", label);
        htmlFor.getStyleClass().add("text-xs");
        VBox withLabel = new VBox(8, label, htmlFor);
        flow.getChildren().add(card("FxLabel.labelFor", withLabel));
        return flow;
    }

    // ---- colors / validation -------------------------------------------------------------------

    private FlowPane colorSection() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxFileInput.Color c : FxFileInput.Color.values()) {
            FxFileInput input = newInput("color " + c.name());
            input.setColor(c);
            boolean gray = c == FxFileInput.Color.GRAY;

            FxLabel label = new FxLabel(gray ? "Gray (default)" : c.name() + " file input", input);
            label.setColor(gray ? FxLabel.Color.DEFAULT : FxLabel.Color.valueOf(c.name()));

            FxHelperText helper = new FxHelperText();
            helper.setColor(gray ? FxHelperText.Color.DEFAULT : FxHelperText.Color.valueOf(c.name()));
            if (gray) {
                helper.setText(HELPER);
            } else {
                helper.getChildren().addAll(FxHelperText.strong(prefix(c)), new Text(" " + message(c)));
            }
            flow.getChildren().add(card(c.name(), stack(label, input, helper)));
        }
        return flow;
    }

    private static String prefix(FxFileInput.Color c) {
        return switch (c) {
            case INFO -> "Heads up!";
            case FAILURE -> "Oops!";
            case WARNING -> "Careful!";
            case SUCCESS -> "Well done!";
            case GRAY -> "";
        };
    }

    private static String message(FxFileInput.Color c) {
        return switch (c) {
            case INFO -> "Large files take longer to upload.";
            case FAILURE -> "That file type is not allowed.";
            case WARNING -> "This will replace your current avatar.";
            case SUCCESS -> "Your file was uploaded.";
            case GRAY -> "";
        };
    }

    // ---- extension filters ---------------------------------------------------------------------

    private FlowPane filterSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxFileInput input = newInput("images-only input");
        input.getExtensionFilters().add(IMAGES);
        flow.getChildren().add(card("FxFileInput, images only",
                stack(new FxLabel("Avatar", input), input, new FxHelperText(HELPER))));

        FxFileInput two = newInput("two filters input");
        two.getExtensionFilters().addAll(IMAGES,
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt"));
        flow.getChildren().add(card("two filters (dialog offers both; drops match either)",
                stack(new FxLabel("Attachment", two), two, null)));

        FxFileInput any = newInput("wildcard input");
        any.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        flow.getChildren().add(card("\"*.*\" accepts everything",
                stack(new FxLabel("Anything", any), any, null)));
        return flow;
    }

    // ---- shadow and disabled -------------------------------------------------------------------

    private FlowPane shadowDisabledSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxFileInput shadowed = newInput("shadow");
        shadowed.setShadow(true);
        flow.getChildren().add(card("shadow = true", stack(new FxLabel("With shadow", shadowed), shadowed, null)));

        FxFileInput disabled = newInput("disabled input");
        disabled.setDisable(true);
        flow.getChildren().add(card("disabled input", stack(new FxLabel("Disabled", disabled), disabled, null)));

        FxFileDropzone zone = newZone("disabled dropzone");
        zone.setHint("Disabled");
        zone.setDisable(true);
        zone.setPrefWidth(FIELD_WIDTH);
        flow.getChildren().add(card("disabled dropzone", zone));

        FxFileInput toggled = newInput("toggled");
        Button toggle = new Button("Toggle disabled");
        toggle.setOnAction(e -> toggled.setDisable(!toggled.isDisabled()));
        flow.getChildren().add(card("runtime disable", new VBox(8, toggled, toggle)));
        return flow;
    }

    // ---- programmatic --------------------------------------------------------------------------

    private VBox programmaticSection() {
        FxFileInput input = newInput("programmatic");
        input.setMultiple(true);

        Button choose = new Button("choose()");
        choose.setOnAction(e -> input.choose());
        Button clear = new Button("clear()");
        clear.setOnAction(e -> input.clear());

        Button rename = new Button("Swap button text");
        rename.setOnAction(e -> input.setButtonText("Choose file".equals(input.getButtonText()) ? "Browse..." : "Choose file"));

        Button placeholder = new Button("Swap placeholder");
        placeholder.setOnAction(e -> input.setPlaceholder(
                "No file chosen".equals(input.getPlaceholder()) ? "Nothing selected yet" : "No file chosen"));

        CheckBox multiple = new CheckBox("multiple (pick 2+ files, then untick: only the first stays)");
        multiple.setSelected(true);
        input.multipleProperty().bind(multiple.selectedProperty());

        Label selected = new Label();
        selected.getStyleClass().addAll("text-xs", "text-muted");
        selected.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                () -> "getFile() = " + (input.getFile() == null ? "null" : input.getFile().getName())
                        + ", getFiles().size() = " + input.getFiles().size(),
                input.getFiles()));

        HBox buttons = new HBox(10, choose, clear, rename, placeholder);
        VBox box = new VBox(10, input, buttons, multiple, selected);
        return card("programmatic API", box);
    }

    // ---- edge cases ----------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxFileInput narrow = newInput("narrow");
        narrow.setPrefWidth(190);
        narrow.setPlaceholder("A very long placeholder that has to be cut off with an ellipsis");
        flow.getChildren().add(card("narrow field, long placeholder (ellipsis, button stays whole)", narrow));

        FxFileDropzone compact = newZone("compact dropzone");
        compact.setStyle("-fx-min-height: 120px; -fx-pref-height: 120px;");
        compact.setHint("Inline style overrides the 256px height");
        compact.setPrefWidth(FIELD_WIDTH);
        flow.getChildren().add(card("compact dropzone (120px)", compact));

        FxFileDropzone bare = newZone("bare dropzone");
        bare.setActionText("Drop here");
        bare.setPromptText("");
        flow.getChildren().add(card("empty prompt text and hint: only the action shows", bare));

        FxFileInput rapid = newInput("rapid");
        Button cycle = new Button("Cycle size + color");
        int[] n = {0};
        cycle.setOnAction(e -> {
            n[0]++;
            FxFileInput.Size[] sizes = FxFileInput.Size.values();
            FxFileInput.Color[] colors = FxFileInput.Color.values();
            rapid.setSize(sizes[n[0] % sizes.length]);
            rapid.setColor(colors[n[0] % colors.length]);
            log("rapid -> " + rapid.getSize() + " / " + rapid.getColor()
                    + ", style classes: " + rapid.getStyleClass());
        });
        flow.getChildren().add(card("style class swap (exactly one size and one color class)",
                new VBox(8, rapid, cycle)));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private FxFileInput newInput(String name) {
        FxFileInput input = new FxFileInput();
        input.setPrefWidth(FIELD_WIDTH);
        input.getFiles().addListener((ListChangeListener<File>) c -> log(name + ": " + summary(input.getFiles())));
        input.setOnRejected(rejected -> log(name + ": rejected " + summary(rejected)));
        return input;
    }

    private FxFileDropzone newZone(String name) {
        FxFileDropzone zone = new FxFileDropzone();
        zone.setPrefWidth(FIELD_WIDTH);
        zone.getFiles().addListener((ListChangeListener<File>) c -> log(name + ": " + summary(zone.getFiles())));
        zone.setOnRejected(rejected -> log(name + ": rejected " + summary(rejected)));
        return zone;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static String summary(List<File> files) {
        return files.isEmpty() ? "(none)" : files.stream().map(File::getName).collect(Collectors.joining(", "));
    }

    /** Label, control, optional helper text: Flowbite's {@code mb-2 block} label above the control. */
    private static VBox stack(FxLabel label, Node control, FxHelperText helper) {
        VBox box = new VBox(0, label, control);
        VBox.setMargin(label, new Insets(0, 0, 8, 0));
        if (helper != null) {
            box.getChildren().add(helper);
        }
        return box;
    }

    private static Node spaced(Node node, double top) {
        VBox wrapper = new VBox(node);
        wrapper.setPadding(new Insets(top, 0, 0, 0));
        return wrapper;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox card(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        VBox wrapper = new VBox(8, label, content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
