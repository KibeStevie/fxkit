package dev.fxkit.showcase;

import dev.fxkit.core.components.FxButton;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@code FxButton.className}, {@code Variant.DEFAULT} + {@code Color}, and
 * the two working together - without the rest of the showcase in the way.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating; switch
 * back once satisfied. The header's theme toggle lets you flip light/dark and watch every section
 * restyle - semantic-token-based sections (Color) should shift; palette/gradient sections (raw scale
 * tokens, per {@code tokens.css}'s own contract) should look identical in both themes.
 */
public class ClassNameTestApp extends Application {

    private Scene scene;
    private Button themeToggle;

    @Override
    public void start(Stage stage) {
        VBox content = new VBox(28,
                sectionHeading("Color (Variant.DEFAULT x every Color)",
                        "Only Variant.DEFAULT reacts to Color; every other variant ignores it."),
                colorGrid(),

                sectionHeading("className - gradients",
                        "Directional gradient syntax parsed at runtime; from-/via-/to- stops are "
                                + "collected once and reused by hover:'s direction swap."),
                gradientGrid(),

                sectionHeading("className - overriding Variant and Color",
                        "className always wins on shared CSS properties without touching the "
                                + "Variant/Color style classes underneath."),
                overrideGrid(),

                sectionHeading("className - pseudo-class scoping",
                        "hover: / pressed: / focus: / disabled: prefixes, recomputed live from the "
                                + "button's own state."),
                pseudoClassGrid(),

                sectionHeading("Sanity checks",
                        "Reapplying className must fully replace the previous value."),
                sanityGrid(),

                sectionHeading("Size (all five, same Variant)",
                        "Fixed height (min-height + pref-height) plus horizontal-only padding, matching Flowbite's h-*/px-* scale."),
                sizeGrid(),
                new Region() // spacer at the bottom so the last row isn't flush with the window edge   
        );
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1000, 720);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxButton className + Color test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxButton test harness");
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

    // ---- Color section ------------------------------------------------------------------------

    private static FlowPane colorGrid() {
        FlowPane flow = new FlowPane(10, 10);
        for (FxButton.Color c : FxButton.Color.values()) {
            FxButton button = new FxButton(c.name());
            button.setVariant(FxButton.Variant.DEFAULT);
            button.setColor(c);
            flow.getChildren().add(labeled(c.name(), box(button)));
        }
        return flow;
    }

    // ---- className: gradients ------------------------------------------------------------------

    private static FlowPane gradientGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("3-stop, hover swaps direction", box(plainGradient())),
                labeled("4-stop (2x via-)", box(fourStopGradient())),
                labeled("hover: direction only, stops reused", box(hoverDirectionSwap())),
                labeled("pressed: swaps a gradient", box(pressedGradient()))
        );
        return flow;
    }

    private static FxButton plainGradient() {
        FxButton button = new FxButton("Blue");
        button.setClassName(
                "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 "
              + "text-white hover:bg-gradient-to-br focus:ring-blue-300");
        return button;
    }

    private static FxButton fourStopGradient() {
        FxButton button = new FxButton("4-stop");
        button.setClassName(
                "bg-gradient-to-r from-purple-500 via-purple-600 via-pink-500 to-blue-500 "
              + "text-white hover:bg-gradient-to-br rounded-full");
        return button;
    }

    private static FxButton hoverDirectionSwap() {
        FxButton button = new FxButton("Hover: direction");
        button.setClassName(
                "bg-gradient-to-r from-cyan-500 via-cyan-600 to-blue-600 "
              + "text-white hover:bg-gradient-to-br");
        return button;
    }

    private static FxButton pressedGradient() {
        FxButton button = new FxButton("Press and hold");
        button.setClassName(
                "bg-gradient-to-r from-cyan-500 to-blue-500 text-white "
              + "pressed:bg-gradient-to-l");
        return button;
    }

    // ---- className overriding Variant / Color ----------------------------------------------------

    private static FlowPane overrideGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("className overrides Variant.PRIMARY", box(overridePrimary())),
                labeled("className overrides Color.RED (still Variant.DEFAULT)", box(overrideColor())),
                labeled("className gradient on top of Color.PURPLE", box(colorThenGradient()))
        );
        return flow;
    }

    private static FxButton overridePrimary() {
        FxButton button = new FxButton("Was PRIMARY");
        button.setVariant(FxButton.Variant.PRIMARY);
        button.setClassName("bg-green-600 rounded-full");
        return button;
    }

    private static FxButton overrideColor() {
        FxButton button = new FxButton("Was Color.RED");
        button.setVariant(FxButton.Variant.DEFAULT);
        button.setColor(FxButton.Color.RED);
        button.setClassName("bg-indigo-600"); // className should win over Color.RED's fill
        return button;
    }

    private static FxButton colorThenGradient() {
        FxButton button = new FxButton("Color -> gradient");
        button.setVariant(FxButton.Variant.DEFAULT);
        button.setColor(FxButton.Color.PURPLE); // fxk-btn-color-purple class stays present...
        button.setClassName(
                "bg-gradient-to-r from-purple-500 to-pink-500 text-white hover:bg-gradient-to-l");
        // ...but this inline gradient wins visually, proving className > Color without removing it.
        return button;
    }

    // ---- className: pseudo-class scoping -----------------------------------------------------

    private static FlowPane pseudoClassGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("hover: swaps a plain background", box(hoverPlainColor())),
                labeled("focus: (ring is a recognized no-op today)", box(focusRing())),
                labeled("disabled: overrides look while disabled", box(disabledOverride()))
        );
        return flow;
    }

    private static FxButton hoverPlainColor() {
        FxButton button = new FxButton("Hover: swap color");
        button.setVariant(FxButton.Variant.SECONDARY);
        button.setClassName("hover:bg-green-100 hover:border-green-500");
        return button;
    }

    private static FxButton focusRing() {
        FxButton button = new FxButton("Tab to me");
        button.setVariant(FxButton.Variant.OUTLINE);
        button.setClassName("focus:bg-blue-50 focus:ring-blue-300");
        return button;
    }

    private static FxButton disabledOverride() {
        FxButton button = new FxButton("Disabled look");
        button.setVariant(FxButton.Variant.DANGER);
        button.setClassName("disabled:bg-gray-300 disabled:text-gray-500");
        button.setDisable(true);
        return button;
    }

    // ---- sanity checks --------------------------------------------------------------------------

    private static FlowPane sanityGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().add(labeled("Reassigning className replaces the old one", box(reassignable())));
        return flow;
    }

    private static FxButton reassignable() {
        FxButton button = new FxButton("Reassign x3");
        button.setClassName("bg-red-600 text-white");
        button.setClassName("bg-green-600 text-white");
        button.setClassName("bg-blue-600 text-white rounded-full");
        return button;
    }

    private static FlowPane sizeGrid() {
        FlowPane flow = new FlowPane(10, 10);
        flow.setAlignment(Pos.CENTER_LEFT);
        for (FxButton.Size s : FxButton.Size.values()) {
            FxButton button = new FxButton(s.name());
            button.setVariant(FxButton.Variant.PRIMARY);
            button.setSize(s);
            flow.getChildren().add(button);
        }
        return flow;
    }

    // ---- layout helpers -----------------------------------------------------------------------

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox box(FxButton button) {
        VBox box = new VBox(6, button);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static VBox labeled(String caption, VBox content) {
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