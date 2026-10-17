package dev.fxkit.showcase;

import dev.fxkit.core.components.clipboard.FxClipboard;
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
import javafx.scene.input.Clipboard;
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
 * Isolated test harness for {@link FxClipboard}: the three Flowbite examples
 * (default, input with
 * icon button, icon + text), every variant's copied state, custom labels and
 * timing, verifying what
 * really landed on the system clipboard, edge cases, and a live playground
 * where every property can
 * be flipped at runtime.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check light and dark, including the icon
 * variant's tooltip.
 */
public class ClipboardTestApp extends Application {

    private static final String SAMPLE = "npm install flowbite-react";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (onCopy callbacks, clipboard reads)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: value, labels, tooltip texts, variant, reset delay, disabled. "
                                + "Click the button, then paste into the field below to check the clipboard."),
                playground(),

                sectionHeading("Default copy to clipboard",
                        "Flowbite's <Clipboard>: full-width button beside a read-only input; the label "
                                + "changes to \"Copied\" for 2 seconds."),
                defaultExample(),

                sectionHeading("Input with copy button",
                        "Flowbite's <ClipboardWithIcon>: icon button inside the input. Hover for the "
                                + "tooltip; after a click the icon becomes a check and the tooltip says Copied."),
                iconExample(),

                sectionHeading("Copy button with text",
                        "Flowbite's <ClipboardWithIconText>: outlined icon + label inside the input; "
                                + "icon and label both turn blue when copied."),
                iconTextExample(),

                sectionHeading("Labels and timing",
                        "Custom label/copiedLabel, a short reset delay, and an indefinite one that only "
                                + "reset() clears."),
                labelsAndTiming(),

                sectionHeading("Verify the clipboard",
                        "Copy, then read the system clipboard back into the log, or paste with Ctrl+V."),
                verifySection(),

                sectionHeading("Edge cases",
                        "null/empty value, long value, disabled, rapid clicks, programmatic copy(), and "
                                + "removing the button from the scene while its tooltip is showing."),
                edgeCases(),

                sectionHeading("Event log", "Everything the onCopy callbacks report lands here."),
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

        stage.setTitle("FxClipboard test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxClipboard test harness");
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
        FxClipboard clip = new FxClipboard(SAMPLE, "Copy");
        clip.setOnCopy(() -> log("playground: onCopy fired"));
        VBox holder = new VBox(clip);
        holder.setPrefWidth(220);
        holder.setMaxWidth(220);

        TextField valueField = new TextField(SAMPLE);
        valueField.setPromptText("valueToCopy");
        clip.valueToCopyProperty().bind(valueField.textProperty());

        TextField labelField = new TextField("Copy");
        clip.labelProperty().bind(labelField.textProperty());

        TextField copiedField = new TextField("Copied");
        clip.copiedLabelProperty().bind(copiedField.textProperty());

        TextField tipField = new TextField("Copy to clipboard");
        clip.tooltipTextProperty().bind(tipField.textProperty());

        TextField copiedTipField = new TextField("Copied");
        clip.copiedTooltipTextProperty().bind(copiedTipField.textProperty());

        ComboBox<FxClipboard.Variant> variantBox = new ComboBox<>();
        variantBox.getItems().addAll(FxClipboard.Variant.values());
        variantBox.valueProperty().bindBidirectional(clip.variantProperty());

        ComboBox<Integer> delayBox = new ComboBox<>();
        delayBox.getItems().addAll(500, 2000, 5000);
        delayBox.setValue(2000);
        delayBox.valueProperty().addListener((o, was, is) -> clip.setResetDelay(Duration.millis(is)));

        CheckBox disabledCheck = new CheckBox("Disabled");
        clip.disableProperty().bind(disabledCheck.selectedProperty());

        Label state = new Label();
        state.getStyleClass().addAll("text-xs", "text-muted");
        state.textProperty().bind(Bindings.concat("copied = ", clip.copiedProperty().asString()));

        Button resetButton = new Button("reset()");
        resetButton.setOnAction(e -> clip.reset());

        TextField pasteTarget = new TextField();
        pasteTarget.setPromptText("Paste here (Ctrl+V) to check what was copied");

        FlowPane controls = new FlowPane(16, 12,
                field("variant", variantBox),
                field("resetDelay (ms)", delayBox),
                field("valueToCopy", valueField),
                field("label", labelField),
                field("copiedLabel", copiedField),
                field("tooltipText (ICON)", tipField),
                field("copiedTooltipText (ICON)", copiedTipField),
                disabledCheck);

        HBox live = new HBox(16, holder, state, resetButton);
        live.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, controls, live, pasteTarget);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the three Flowbite examples
    // --------------------------------------------------------

    /** grid-cols-8: input col-span-6, button col-span-2, max-w-[23rem]. */
    private VBox defaultExample() {
        TextField field = readOnlyField(SAMPLE, "md");
        FxClipboard clip = new FxClipboard(SAMPLE, "Copy");
        clip.setOnCopy(() -> log("default: copied"));

        GridPane grid = new GridPane();
        grid.setHgap(8); // gap-2
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setPercentWidth(75);
        ColumnConstraints buttonColumn = new ColumnConstraints();
        buttonColumn.setPercentWidth(25);
        grid.getColumnConstraints().addAll(inputColumn, buttonColumn);
        grid.add(field, 0, 0);
        grid.add(clip, 1, 0);
        grid.setMaxWidth(368);

        return labeled("default, Variant.DEFAULT", grid);
    }

    /** max-w-64, icon at end-2. */
    private VBox iconExample() {
        TextField field = readOnlyField(SAMPLE, "md");
        FxClipboard clip = new FxClipboard(SAMPLE);
        clip.setVariant(FxClipboard.Variant.ICON);
        clip.setOnCopy(() -> log("icon: copied"));

        StackPane host = FxClipboard.overlay(field, clip);
        host.setMaxWidth(256);

        return labeled("icon inside input, Variant.ICON (hover me, then click)", host);
    }

    /** max-w-80, taller input (py-4), icon + text at end-2.5. */
    private VBox iconTextExample() {
        TextField field = readOnlyField(SAMPLE, "lg");
        FxClipboard clip = new FxClipboard(SAMPLE, "Copy");
        clip.setVariant(FxClipboard.Variant.ICON_TEXT);
        clip.setOnCopy(() -> log("icon-text: copied"));

        StackPane host = FxClipboard.overlay(field, clip);
        host.setMaxWidth(320);

        return labeled("icon + text inside input, Variant.ICON_TEXT", host);
    }

    // ---- labels and timing
    // ------------------------------------------------------------------

    private FlowPane labelsAndTiming() {
        FlowPane flow = new FlowPane(12, 12);

        FxClipboard custom = new FxClipboard("https://fxkit.dev", "Copy link");
        custom.setCopiedLabel("Link copied!");
        flow.getChildren().add(labeled("custom labels", wide(custom, 160)));

        FxClipboard quick = new FxClipboard(SAMPLE, "Copy (0.5 s)");
        quick.setCopiedLabel("Copied");
        quick.setResetDelay(Duration.millis(500));
        flow.getChildren().add(labeled("resetDelay = 500 ms", wide(quick, 160)));

        FxClipboard sticky = new FxClipboard(SAMPLE, "Copy (sticky)");
        sticky.setResetDelay(Duration.INDEFINITE);
        Button reset = new Button("reset()");
        reset.setOnAction(e -> sticky.reset());
        flow.getChildren().add(labeled("resetDelay = INDEFINITE", new VBox(6, wide(sticky, 160), reset)));

        FxClipboard iconText = new FxClipboard(SAMPLE, "Kopieren");
        iconText.setVariant(FxClipboard.Variant.ICON_TEXT);
        iconText.setCopiedLabel("Kopiert");
        flow.getChildren().add(labeled("ICON_TEXT, custom labels", iconText));

        FxClipboard iconTips = new FxClipboard(SAMPLE);
        iconTips.setVariant(FxClipboard.Variant.ICON);
        iconTips.setTooltipText("Click to copy the install command");
        iconTips.setCopiedTooltipText("Done!");
        flow.getChildren().add(labeled("ICON, custom tooltip texts", iconTips));

        FxClipboard iconNoTip = new FxClipboard(SAMPLE);
        iconNoTip.setVariant(FxClipboard.Variant.ICON);
        iconNoTip.setTooltipText(null);
        iconNoTip.setCopiedTooltipText(null);
        flow.getChildren().add(labeled("ICON, tooltips off (null)", iconNoTip));
        return flow;
    }

    // ---- verify
    // -----------------------------------------------------------------------------

    private VBox verifySection() {
        TextField value = new TextField("clipboard check " + System.nanoTime() % 100000);
        FxClipboard clip = new FxClipboard();
        clip.valueToCopyProperty().bind(value.textProperty());
        clip.setOnCopy(() -> log("verify: copied \"" + value.getText() + "\""));

        Button read = new Button("Read system clipboard into log");
        read.setOnAction(e -> log("system clipboard now: \"" + Clipboard.getSystemClipboard().getString() + "\""));

        TextField paste = new TextField();
        paste.setPromptText("Paste here (Ctrl+V)");

        VBox box = new VBox(10, field("value to copy (editable)", value),
                new HBox(10, wide(clip, 120), read), paste);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases
    // -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxClipboard nullValue = new FxClipboard(null, "Copy null");
        nullValue.setOnCopy(() -> log("null value: copied (clipboard should hold an empty string)"));
        flow.getChildren().add(labeled("valueToCopy = null", wide(nullValue, 140)));

        String longText = "0123456789".repeat(30);
        FxClipboard longValue = new FxClipboard(longText, "Copy 300 chars");
        longValue.setOnCopy(() -> log("long value: copied " + longText.length() + " chars"));
        flow.getChildren().add(labeled("300-character value", wide(longValue, 160)));

        FxClipboard disabledDefault = new FxClipboard(SAMPLE, "Copy");
        disabledDefault.setDisable(true);
        FxClipboard disabledIcon = new FxClipboard(SAMPLE);
        disabledIcon.setVariant(FxClipboard.Variant.ICON);
        disabledIcon.setDisable(true);
        FxClipboard disabledIconText = new FxClipboard(SAMPLE, "Copy");
        disabledIconText.setVariant(FxClipboard.Variant.ICON_TEXT);
        disabledIconText.setDisable(true);
        flow.getChildren().add(labeled("disabled (all three; click and hover do nothing)",
                new HBox(10, wide(disabledDefault, 100), disabledIcon, disabledIconText)));

        FxClipboard rapid = new FxClipboard(SAMPLE, "Click fast");
        int[] clicks = { 0 };
        rapid.setOnCopy(() -> log("rapid: copy #" + (++clicks[0])));
        flow.getChildren().add(labeled("rapid clicks restart the 2 s timer", wide(rapid, 140)));

        FxClipboard programmatic = new FxClipboard("copied from code", "Copy");
        Button trigger = new Button("Call copy()");
        trigger.setOnAction(e -> programmatic.copy());
        flow.getChildren().add(labeled("programmatic copy()", new VBox(6, wide(programmatic, 140), trigger)));

        FxClipboard removable = new FxClipboard(SAMPLE);
        removable.setVariant(FxClipboard.Variant.ICON);
        HBox removableHolder = new HBox(removable);
        removableHolder.setMinHeight(40);
        Button copyThenRemove = new Button("copy() then remove from scene");
        copyThenRemove.setOnAction(e -> {
            removable.copy();
            removableHolder.getChildren().remove(removable);
            log("removable: removed while copied (tooltip should vanish)");
        });
        Button restore = new Button("Restore");
        restore.setOnAction(e -> {
            if (!removableHolder.getChildren().contains(removable)) {
                removableHolder.getChildren().add(removable);
            }
        });
        flow.getChildren().add(labeled("tooltip hides when the node leaves the scene",
                new VBox(6, removableHolder, new HBox(8, copyThenRemove, restore))));
        return flow;
    }

    // ---- helpers
    // ----------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    /**
     * A read-only input styled with FXKit's field classes (md = p-2.5, lg = p-4).
     */
    private static TextField readOnlyField(String value, String size) {
        TextField field = new TextField(value);
        field.setEditable(false);
        field.getStyleClass().addAll("fxk-field", "fxk-input", "fxk-input-size-" + size);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    /**
     * DEFAULT-variant buttons fill their parent; this pins one to a width for
     * FlowPane layouts.
     */
    private static FxClipboard wide(FxClipboard clip, double width) {
        clip.setPrefWidth(width);
        clip.setMaxWidth(width);
        return clip;
    }

    private static VBox field(String caption, Node control) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        return new VBox(2, label, control);
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
