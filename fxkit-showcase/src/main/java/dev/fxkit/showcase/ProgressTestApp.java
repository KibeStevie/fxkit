package dev.fxkit.showcase;

import dev.fxkit.core.components.progress.FxProgress;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Isolated test harness for {@link FxProgress}: one section per category of
 * Flowbite React's
 * Progress docs (default, labels, label positioning, sizing, colors), then
 * value edge cases,
 * layout behavior, and a live playground where every property can be flipped at
 * runtime.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check that bars restyle correctly in light
 * and dark.
 */
public class ProgressTestApp extends Application {

    private static final FxProgress.LabelPosition INSIDE = FxProgress.LabelPosition.INSIDE;
    private static final FxProgress.LabelPosition OUTSIDE = FxProgress.LabelPosition.OUTSIDE;

    private Scene scene;
    private Button themeToggle;

    @Override
    public void start(Stage stage) {
        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: drag the slider, switch size and color, toggle labels and "
                                + "move each one inside or outside the bar."),
                playground(),

                sectionHeading("Default",
                        "FxProgress(45): MD, default color, no labels. The default color follows the "
                                + "theme's primary token."),
                defaultSection(),

                sectionHeading("Labels",
                        "textLabel + labelText shows the text, labelProgress shows the percentage "
                                + "(both inside the bar by default; use LG or XL so the text fits)."),
                labelsSection(),

                sectionHeading("Label positioning",
                        "All four combinations of textLabelPosition x progressLabelPosition, then each "
                                + "label on its own. A single outside label stays at the start of the row."),
                positioningGrid(),

                sectionHeading("Sizing",
                        "SM / MD / LG / XL = 6 / 10 / 16 / 24px. Bottom row: inside labels on every size "
                                + "(they are only legible on LG and XL, and are clipped on SM and MD)."),
                sizingGrid(),

                sectionHeading("Colors (all 13)",
                        "Fill colors follow Flowbite, including its dark: variants. Toggle the theme: DARK, "
                                + "RED, GREEN, INDIGO and PURPLE change, the rest stay put. Check the inside "
                                + "percentage is readable on every one, YELLOW especially."),
                colorGrid(),

                sectionHeading("Values and edge cases",
                        "0 and 100, tiny and huge fills, fractions, out-of-range values (clamped) and NaN "
                                + "(treated as 0). The inside label must never spill onto the empty track."),
                valueGrid(),

                sectionHeading("Layout",
                        "w-full: the bar fills what its parent offers. Narrow parent, fixed pref width, "
                                + "and a row where the bar takes the leftover space."),
                layoutSection(),

                sectionHeading("Live updates",
                        "progress is an ordinary property: set it from a Timeline, bind it, whatever. "
                                + "The color flips to GREEN when it reaches 100."),
                liveSection(),

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

        stage.setTitle("FxProgress test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxProgress test harness");
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
        FxProgress bar = new FxProgress(45);
        bar.setTextLabel("Flowbite");
        bar.setSize(FxProgress.Size.LG);
        bar.setLabelText(true);
        bar.setLabelProgress(true);

        Slider slider = new Slider(0, 100, 45);
        slider.setPrefWidth(260);
        bar.progressProperty().bind(slider.valueProperty());
        Label value = new Label();
        value.getStyleClass().addAll("text-sm", "text-muted");
        value.textProperty().bind(slider.valueProperty().asString("progress = %.1f"));

        ComboBox<FxProgress.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxProgress.Size.values());
        sizeBox.valueProperty().bindBidirectional(bar.sizeProperty());

        ComboBox<FxProgress.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxProgress.Color.values());
        colorBox.valueProperty().bindBidirectional(bar.colorProperty());

        TextField textField = new TextField("Flowbite");
        textField.setPromptText("textLabel");
        bar.textLabelProperty().bind(textField.textProperty());

        CheckBox labelText = new CheckBox("labelText");
        labelText.setSelected(true);
        bar.labelTextProperty().bind(labelText.selectedProperty());

        CheckBox labelProgress = new CheckBox("labelProgress");
        labelProgress.setSelected(true);
        bar.labelProgressProperty().bind(labelProgress.selectedProperty());

        ComboBox<FxProgress.LabelPosition> textPos = new ComboBox<>();
        textPos.getItems().addAll(FxProgress.LabelPosition.values());
        textPos.valueProperty().bindBidirectional(bar.textLabelPositionProperty());

        ComboBox<FxProgress.LabelPosition> progressPos = new ComboBox<>();
        progressPos.getItems().addAll(FxProgress.LabelPosition.values());
        progressPos.valueProperty().bindBidirectional(bar.progressLabelPositionProperty());

        HBox row1 = row(new Label("Progress"), slider, value);
        HBox row2 = row(new Label("Size"), sizeBox, new Label("Color"), colorBox,
                new Label("textLabel"), textField);
        HBox row3 = row(labelText, new Label("text position"), textPos,
                labelProgress, new Label("progress position"), progressPos);

        VBox box = new VBox(12, row1, row2, row3, bar);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- default / labels
    // -------------------------------------------------------------------------

    private static VBox defaultSection() {
        return card("FxProgress(45)", new FxProgress(45));
    }

    private static FlowPane labelsSection() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("text + progress, LG, inside",
                sized(bar(50, FxProgress.Size.LG, "Flowbite", true, true, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("progress only, LG, inside",
                sized(bar(50, FxProgress.Size.LG, "Flowbite", false, true, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("text only, XL, inside",
                sized(bar(50, FxProgress.Size.XL, "Flowbite", true, false, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("labelText on, textLabel empty (label must not render)",
                sized(bar(50, FxProgress.Size.LG, "", true, true, INSIDE, INSIDE))));
        return flow;
    }

    // ---- positioning
    // --------------------------------------------------------------------------------

    private static FlowPane positioningGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxProgress.LabelPosition t : FxProgress.LabelPosition.values()) {
            for (FxProgress.LabelPosition p : FxProgress.LabelPosition.values()) {
                flow.getChildren().add(labeled("text " + t + ", progress " + p,
                        sized(bar(45, FxProgress.Size.LG, "Flowbite", true, true, t, p))));
            }
        }
        flow.getChildren().add(labeled("text OUTSIDE only",
                sized(bar(45, FxProgress.Size.LG, "Flowbite", true, false, OUTSIDE, INSIDE))));
        flow.getChildren().add(labeled("progress OUTSIDE only (stays at the start, like justify-between)",
                sized(bar(45, FxProgress.Size.LG, "Flowbite", false, true, INSIDE, OUTSIDE))));
        flow.getChildren().add(labeled("progress OUTSIDE on SM (no inside text needed)",
                sized(bar(45, FxProgress.Size.SM, "Flowbite", true, true, OUTSIDE, OUTSIDE))));
        return flow;
    }

    // ---- sizing
    // -----------------------------------------------------------------------------------

    private static FlowPane sizingGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxProgress.Size size : FxProgress.Size.values()) {
            FxProgress bar = new FxProgress(45);
            bar.setSize(size);
            flow.getChildren().add(labeled(size.name(), sized(bar)));
        }
        for (FxProgress.Size size : FxProgress.Size.values()) {
            flow.getChildren().add(labeled(size.name() + " + inside labels",
                    sized(bar(45, size, "Flowbite", true, true, INSIDE, INSIDE))));
        }
        return flow;
    }

    // ---- colors
    // -----------------------------------------------------------------------------------

    private static FlowPane colorGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxProgress.Color color : FxProgress.Color.values()) {
            FxProgress bar = bar(45, FxProgress.Size.LG, "Flowbite", false, true, INSIDE, INSIDE);
            bar.setColor(color);
            flow.getChildren().add(labeled(color.name(), sized(bar)));
        }
        return flow;
    }

    // ---- values / edge cases
    // ----------------------------------------------------------------------

    private static FlowPane valueGrid() {
        FlowPane flow = new FlowPane(12, 12);
        double[] values = { 0, 1, 5, 25, 99.5, 100 };
        for (double v : values) {
            flow.getChildren().add(labeled("progress = " + v,
                    sized(bar(v, FxProgress.Size.XL, "Flowbite", false, true, INSIDE, INSIDE))));
        }
        flow.getChildren().add(labeled("45.5 (shows 45.5%, not 46%)",
                sized(bar(45.5, FxProgress.Size.XL, "Flowbite", false, true, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("-20 (clamped to 0)",
                sized(bar(-20, FxProgress.Size.XL, "Flowbite", false, true, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("150 (clamped to 100)",
                sized(bar(150, FxProgress.Size.XL, "Flowbite", false, true, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("NaN (treated as 0)",
                sized(bar(Double.NaN, FxProgress.Size.XL, "Flowbite", false, true, OUTSIDE, INSIDE))));
        flow.getChildren().add(labeled("long text label, small fill (clipped to the fill, no ellipsis)",
                sized(bar(15, FxProgress.Size.XL, "A deliberately long label", true, true, INSIDE, INSIDE))));
        flow.getChildren().add(labeled("long text label OUTSIDE (row stays on one line)",
                sized(bar(15, FxProgress.Size.LG, "A deliberately long label", true, true, OUTSIDE, OUTSIDE))));
        return flow;
    }

    // ---- layout
    // -----------------------------------------------------------------------------------

    private static VBox layoutSection() {
        VBox narrow = new VBox(sizedTo(bar(60, FxProgress.Size.LG, "Narrow", true, true, OUTSIDE, INSIDE), 160));
        narrow.setMaxWidth(160);

        // No width set: in an HBox (which never stretches children) the bar keeps the
        // track's 240px pref width
        FxProgress defaultWidth = new FxProgress(60);

        // In an HBox with Hgrow the bar takes whatever the label leaves over
        FxProgress grow = new FxProgress(60);
        grow.setSize(FxProgress.Size.LG);
        HBox.setHgrow(grow, Priority.ALWAYS);
        Label before = new Label("Download");
        before.getStyleClass().addAll("text-sm", "text-body");
        Label after = new Label("60%");
        after.getStyleClass().addAll("text-sm", "text-muted");
        HBox inRow = new HBox(12, before, grow, after);
        inRow.setAlignment(Pos.CENTER_LEFT);

        // Stretches to the full width of the card
        FxProgress full = new FxProgress(60);
        full.setSize(FxProgress.Size.XL);
        full.setLabelProgress(true);

        VBox box = new VBox(16,
                labeled("160px parent", narrow),
                labeled("no width set, in an HBox (default 240px)", new HBox(defaultWidth)),
                labeled("HBox with Hgrow.ALWAYS", inRow),
                labeled("full width of the card", full));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- live
    // -------------------------------------------------------------------------------------

    private VBox liveSection() {
        FxProgress live = new FxProgress(0);
        live.setSize(FxProgress.Size.LG);
        live.setTextLabel("Uploading report.pdf");
        live.setLabelText(true);
        live.setLabelProgress(true);
        live.setTextLabelPosition(OUTSIDE);
        live.setProgressLabelPosition(OUTSIDE);
        live.progressProperty().addListener((o, was, is) -> live
                .setColor(is.doubleValue() >= 100 ? FxProgress.Color.GREEN : FxProgress.Color.DEFAULT));

        FxProgress inside = new FxProgress(0);
        inside.setSize(FxProgress.Size.XL);
        inside.setColor(FxProgress.Color.PURPLE);
        inside.setLabelProgress(true);
        inside.progressProperty().bind(live.progressProperty());

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            double next = live.getProgress() + 0.5;
            live.setProgress(Math.min(100, next));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        live.progressProperty().addListener((o, was, is) -> {
            if (is.doubleValue() >= 100) {
                timeline.stop();
            }
        });

        Button start = new Button("Start / resume");
        start.setOnAction(e -> {
            if (live.getProgress() >= 100) {
                live.setProgress(0);
            }
            timeline.play();
        });
        Button pause = new Button("Pause");
        pause.setOnAction(e -> timeline.pause());
        Button reset = new Button("Reset");
        reset.setOnAction(e -> {
            timeline.stop();
            live.setProgress(0);
        });

        VBox box = new VBox(12, new HBox(10, start, pause, reset), live, inside);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- helpers
    // ----------------------------------------------------------------------------------

    private static FxProgress bar(double progress, FxProgress.Size size, String text, boolean labelText,
            boolean labelProgress, FxProgress.LabelPosition textPos,
            FxProgress.LabelPosition progressPos) {
        FxProgress bar = new FxProgress(progress);
        bar.setSize(size);
        bar.setTextLabel(text);
        bar.setLabelText(labelText);
        bar.setLabelProgress(labelProgress);
        bar.setTextLabelPosition(textPos);
        bar.setProgressLabelPosition(progressPos);
        return bar;
    }

    private static FxProgress sized(FxProgress bar) {
        return sizedTo(bar, 300);
    }

    private static FxProgress sizedTo(FxProgress bar, double width) {
        bar.setPrefWidth(width);
        return bar;
    }

    private static HBox row(Node... nodes) {
        HBox row = new HBox(12, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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
        label.setMaxWidth(300);
        VBox wrapper = new VBox(8, label, content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    private static VBox card(String caption, Node content) {
        VBox box = labeled(caption, content);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
