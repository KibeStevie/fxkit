package dev.fxkit.showcase;

import dev.fxkit.core.components.rating.FxRating;
import dev.fxkit.core.components.rating.FxRatingAdvanced;
import dev.fxkit.core.components.rating.FxRatingStar;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxRating}, {@link FxRatingStar} and {@link FxRatingAdvanced}:
 * the Flowbite examples (default, with text, count, sizes, advanced breakdown), a live playground,
 * click-to-rate, per-star toggling, custom colors, and edge cases.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check the stars and bars in light and dark.
 */
public class RatingTestApp extends Application {

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (clicks, exceptions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Change the number of stars, how many are filled, the size and the text next to them."),
                playground(),

                sectionHeading("Default rating",
                        "FxRating.of(filled, total): yellow stars filled from the left, grey for the rest."),
                defaultGrid(),

                sectionHeading("Rating with text",
                        "Stars followed by FxRating.text(...) (muted, medium weight)."),
                withTextGrid(),

                sectionHeading("Rating count",
                        "Stars + score() + dot() + link(). The link is a Hyperlink: set its action yourself."),
                countSection(),

                sectionHeading("Star sizing",
                        "Size.SM (20px, default), MD (28px), LG (40px). Text stays 12px."),
                sizeSection(),

                sectionHeading("Advanced rating (breakdown)",
                        "One FxRatingAdvanced row per score. Bars line up thanks to the minimum label widths."),
                advancedSection(),

                sectionHeading("Advanced: live percent",
                        "Drag the slider: the bar and the percentage label update together."),
                livePercentSection(),

                sectionHeading("Click to rate",
                        "Plain FxRatingStar nodes with a click handler: the star you click and all before it fill."),
                clickToRateSection(),

                sectionHeading("Star toggling",
                        "setFilled(...) on single stars at runtime (click a star to flip only that one)."),
                toggleSection(),

                sectionHeading("Custom colors",
                        "Redefine -fxk-rating-filled / -fxk-rating-empty on the node via setStyle(...)."),
                customColors(),

                sectionHeading("Edge cases",
                        "0 stars, 0 filled, 10 stars, invalid arguments, clamped and NaN percentages, long text."),
                edgeCases(),

                sectionHeading("Event log", "Clicks and caught exceptions land here."),
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

        stage.setTitle("FxRating test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxRating test harness");
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
        VBox ratingHolder = new VBox();
        Spinner<Integer> totalSpinner = new Spinner<>(0, 10, 5);
        Spinner<Integer> filledSpinner = new Spinner<>(0, 10, 4);
        totalSpinner.setPrefWidth(80);
        filledSpinner.setPrefWidth(80);

        ComboBox<FxRating.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxRating.Size.values());
        sizeBox.setValue(FxRating.Size.SM);

        CheckBox textCheck = new CheckBox("Text");
        CheckBox scoreCheck = new CheckBox("Score + dot + link");

        Runnable rebuild = () -> {
            int total = totalSpinner.getValue();
            int filled = Math.min(filledSpinner.getValue(), total);
            FxRating rating = FxRating.of(filled, total);
            rating.setSize(sizeBox.getValue());
            if (textCheck.isSelected()) {
                rating.getChildren().add(FxRating.text(filled + " out of " + total));
            }
            if (scoreCheck.isSelected()) {
                Hyperlink reviews = FxRating.link("73 reviews");
                reviews.setOnAction(e -> log("playground: reviews link clicked"));
                rating.getChildren().addAll(
                        FxRating.score(String.valueOf(filled)), FxRating.dot(), reviews);
            }
            ratingHolder.getChildren().setAll(rating);
        };

        totalSpinner.valueProperty().addListener((o, a, b) -> rebuild.run());
        filledSpinner.valueProperty().addListener((o, a, b) -> rebuild.run());
        sizeBox.valueProperty().addListener((o, a, b) -> rebuild.run());
        textCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());
        scoreCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());
        rebuild.run();

        HBox controls = new HBox(16, new Label("Total"), totalSpinner, new Label("Filled"), filledSpinner,
                new Label("Size"), sizeBox, textCheck, scoreCheck);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, controls, ratingHolder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- default ----------------------------------------------------------------------------

    private static FlowPane defaultGrid() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("4 of 5", FxRating.of(4, 5)));
        flow.getChildren().add(labeled("5 of 5", FxRating.of(5, 5)));
        flow.getChildren().add(labeled("1 of 5", FxRating.of(1, 5)));
        flow.getChildren().add(labeled("0 of 5", FxRating.of(0, 5)));
        flow.getChildren().add(labeled("built by hand",
                new FxRating(new FxRatingStar(), new FxRatingStar(), new FxRatingStar(false))));
        return flow;
    }

    // ---- with text --------------------------------------------------------------------------

    private static FlowPane withTextGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxRating a = FxRating.of(4, 5);
        a.getChildren().add(FxRating.text("4.95 out of 5"));
        flow.getChildren().add(labeled("text after the stars", a));

        FxRating b = FxRating.of(3, 5);
        b.getChildren().add(FxRating.score("3.0"));
        flow.getChildren().add(labeled("bold score", b));

        FxRating c = new FxRating(FxRating.text("Rated"), new FxRatingStar(), new FxRatingStar(), new FxRatingStar(false));
        flow.getChildren().add(labeled("text before the stars", c));
        return flow;
    }

    // ---- count ------------------------------------------------------------------------------

    private VBox countSection() {
        Hyperlink reviews = FxRating.link("73 reviews");
        reviews.setOnAction(e -> log("count: '73 reviews' clicked"));
        FxRating count = new FxRating(new FxRatingStar(), FxRating.score("4.95"), FxRating.dot(), reviews);

        FxRating full = FxRating.of(4, 5);
        Hyperlink more = FxRating.link("1,204 reviews");
        more.setOnAction(e -> log("count: '1,204 reviews' clicked"));
        full.getChildren().addAll(FxRating.score("4.95"), FxRating.text("out of 5"), FxRating.dot(), more);

        FlowPane flow = new FlowPane(12, 12,
                labeled("one star + score + dot + link", count),
                labeled("five stars + score + text + dot + link", full));
        VBox box = new VBox(flow);
        return box;
    }

    // ---- sizes ------------------------------------------------------------------------------

    private static FlowPane sizeSection() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxRating.Size size : FxRating.Size.values()) {
            FxRating rating = FxRating.of(4, 5);
            rating.setSize(size);
            rating.getChildren().add(FxRating.text(size.name()));
            flow.getChildren().add(labeled(size.name(), rating));
        }

        FxRating ctor = new FxRating(FxRating.Size.LG, new FxRatingStar(), new FxRatingStar(false));
        flow.getChildren().add(labeled("constructor FxRating(Size, Node...)", ctor));

        FxRating swap = FxRating.of(3, 5);
        Button cycle = new Button("Cycle size");
        cycle.setOnAction(e -> {
            FxRating.Size[] all = FxRating.Size.values();
            swap.setSize(all[(swap.getSize().ordinal() + 1) % all.length]);
        });
        flow.getChildren().add(labeled("runtime size swap", new VBox(6, swap, cycle)));
        return flow;
    }

    // ---- advanced ---------------------------------------------------------------------------

    private static VBox advancedSection() {
        FxRating average = FxRating.of(4, 5);
        average.getChildren().addAll(FxRating.score("4.95"), FxRating.text("out of 5"));
        Label total = new Label("1,745 global ratings");
        total.getStyleClass().addAll("text-sm", "text-muted");

        VBox breakdown = new VBox(8,
                new FxRatingAdvanced("5 star", 70),
                new FxRatingAdvanced("4 star", 17),
                new FxRatingAdvanced("3 star", 8),
                new FxRatingAdvanced("2 star", 4),
                new FxRatingAdvanced("1 star", 1));

        VBox box = new VBox(10, average, total, breakdown);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private static VBox livePercentSection() {
        FxRatingAdvanced row = new FxRatingAdvanced("Live", 40);
        Slider slider = new Slider(0, 100, 40);
        slider.setPrefWidth(300);
        row.percentFilledProperty().bind(slider.valueProperty());

        Slider width = new Slider(100, 500, 240);
        width.setPrefWidth(300);
        width.valueProperty().addListener((o, a, b) -> row.setBarWidth(b.doubleValue()));

        VBox box = new VBox(10, row,
                new HBox(10, new Label("Percent"), slider),
                new HBox(10, new Label("Bar width"), width));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- click to rate ----------------------------------------------------------------------

    private VBox clickToRateSection() {
        FxRating rating = FxRating.of(0, 5);
        rating.setSize(FxRating.Size.MD);
        Label value = new Label("No rating yet");
        value.getStyleClass().addAll("text-sm", "text-muted");

        for (int i = 0; i < rating.getChildren().size(); i++) {
            final int index = i;
            Node star = rating.getChildren().get(i);
            star.setStyle("-fx-cursor: hand;");
            star.setOnMouseClicked(e -> {
                for (int j = 0; j < rating.getChildren().size(); j++) {
                    ((FxRatingStar) rating.getChildren().get(j)).setFilled(j <= index);
                }
                value.setText("You rated " + (index + 1) + " out of 5");
                log("click-to-rate: " + (index + 1));
            });
        }

        VBox box = new VBox(8, rating, value);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- toggle single stars ----------------------------------------------------------------

    private VBox toggleSection() {
        FxRating rating = FxRating.of(2, 5);
        for (Node n : rating.getChildren()) {
            FxRatingStar star = (FxRatingStar) n;
            star.setStyle("-fx-cursor: hand;");
            star.setOnMouseClicked(e -> {
                star.setFilled(!star.isFilled());
                log("star toggled, filled=" + star.isFilled());
            });
        }
        VBox box = new VBox(8, rating);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- custom colors ----------------------------------------------------------------------

    private static FlowPane customColors() {
        FlowPane flow = new FlowPane(12, 12);

        FxRating red = FxRating.of(3, 5);
        red.setStyle("-fxk-rating-filled: -fxk-red-500; -fxk-rating-empty: -fxk-red-200;");
        flow.getChildren().add(labeled("red", red));

        FxRating green = FxRating.of(4, 5);
        green.setStyle("-fxk-rating-filled: -fxk-green-500;");
        flow.getChildren().add(labeled("green filled only", green));

        FxRating purple = FxRating.of(2, 5);
        purple.setSize(FxRating.Size.MD);
        purple.setStyle("-fxk-rating-filled: -fxk-purple-500; -fxk-rating-empty: -fxk-purple-100;");
        flow.getChildren().add(labeled("purple, MD", purple));

        FxRatingAdvanced bar = new FxRatingAdvanced("Teal", 65);
        bar.setStyle("-fxk-rating-filled: -fxk-teal-500;");
        flow.getChildren().add(labeled("advanced bar, teal fill", bar));
        return flow;
    }

    // ---- edge cases -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("of(0, 0): no stars", FxRating.of(0, 0)));
        flow.getChildren().add(labeled("of(10, 10)", FxRating.of(10, 10)));
        flow.getChildren().add(labeled("empty FxRating()", new FxRating()));

        Button bad = new Button("of(6, 5) -> IllegalArgumentException");
        bad.setOnAction(e -> tryOf(6, 5));
        Button neg = new Button("of(-1, 5) -> IllegalArgumentException");
        neg.setOnAction(e -> tryOf(-1, 5));
        Button badTotal = new Button("of(0, -1) -> IllegalArgumentException");
        badTotal.setOnAction(e -> tryOf(0, -1));
        flow.getChildren().add(labeled("invalid arguments", new VBox(6, bad, neg, badTotal)));

        FxRating accessible = FxRating.of(4, 5);
        Label accessibleText = new Label("accessibleText: " + accessible.getAccessibleText());
        accessibleText.getStyleClass().addAll("text-xs", "text-muted");
        flow.getChildren().add(labeled("accessible text (of)", new VBox(6, accessible, accessibleText)));

        VBox clamped = new VBox(8,
                new FxRatingAdvanced("150%", 150),
                new FxRatingAdvanced("-20%", -20),
                new FxRatingAdvanced("NaN", Double.NaN),
                new FxRatingAdvanced("8.5", 8.5),
                new FxRatingAdvanced("100", 100),
                new FxRatingAdvanced());
        flow.getChildren().add(labeled("percent clamped to 0..100, NaN -> 0, empty row", clamped));

        FxRating longText = FxRating.of(4, 5);
        longText.getChildren().add(FxRating.text("A deliberately long caption that sits next to the stars"));
        flow.getChildren().add(labeled("long text", longText));
        return flow;
    }

    private void tryOf(int filled, int total) {
        try {
            FxRating.of(filled, total);
            log("of(" + filled + ", " + total + ") did NOT throw (unexpected)");
        } catch (IllegalArgumentException ex) {
            log("of(" + filled + ", " + total + ") threw: " + ex.getMessage());
        }
    }

    // ---- helpers ----------------------------------------------------------------------------

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
