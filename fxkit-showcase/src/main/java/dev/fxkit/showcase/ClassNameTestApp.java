package dev.fxkit.showcase;

import java.util.ArrayList;
import java.util.List;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.util.Duration;

/**
 * Isolated test harness for {@code FxButton.className}, {@code Variant.DEFAULT}
 * + {@code Color},
 * {@code FxButton.processing} (the spinner state), and how they work together -
 * without the rest of
 * the showcase in the way.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating; switch
 * back once satisfied. The header's theme toggle lets you flip light/dark and
 * watch every section
 * restyle - semantic-token-based sections (Color) should shift;
 * palette/gradient sections (raw scale
 * tokens, per {@code tokens.css}'s own contract) should look identical in both
 * themes. The header's
 * "Toggle all processing" button flips every button in the processing sections
 * at once.
 */
public class ClassNameTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private Scene scene;
    private Button themeToggle;

    /**
     * Every button in the "Processing" sections, so the header can flip them all
     * together.
     */
    private final List<FxButton> processingButtons = new ArrayList<>();

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

                sectionHeading("Processing - every Variant",
                        "processing = true: spinner replaces the icon, text becomes \"Loading...\". The spinner "
                                + "uses the button's text color, so it must be visible on every variant."),
                processingVariantGrid(),

                sectionHeading("Processing - Variant.DEFAULT and OUTLINE x every Color",
                        "Top row solid, bottom row outline. Hover an outline button: its text flips to white "
                                + "and the spinner must flip with it."),
                processingColorGrid(),

                sectionHeading("Processing - Size",
                        "Spinner is 12px on XS, 16px on SM/MD/LG, 24px on XL. Height must not change vs. idle."),
                processingSizeGrid(),

                sectionHeading("Processing - label and icon",
                        "Custom label, blank label (keeps own text), icon replaced by the spinner, "
                                + "icon-only and empty buttons."),
                processingLabelGrid(),

                sectionHeading("Processing - interactive",
                        "Click the first button: it processes for 2s. The counter only goes up for clicks that "
                                + "actually reach onAction - spam-click while it spins and it must not change."),
                interactiveSection(),

                sectionHeading("Processing - runtime changes",
                        "Change text, icon, size, variant and label while processing. Nothing should be lost: "
                                + "turn processing off and the latest text and icon come back."),
                runtimeChangesSection(),

                sectionHeading("Processing - combined with className and disabled",
                        "Gradient + processing (white spinner), hover-scoped className, disabled + processing, pill."),
                processingCombinedGrid(),
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

        stage.setTitle("FxButton className + Color + processing test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxButton test harness");
        title.getStyleClass().addAll("text-xl", "font-bold", "text-body");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button toggleAll = new Button("Toggle all processing");
        toggleAll.getStyleClass().addAll("bg-surface-alt", "text-body", "rounded-md", "p-2", "font-semibold");
        toggleAll.setOnAction(e -> {
            boolean next = !processingButtons.stream().allMatch(FxButton::isProcessing);
            processingButtons.forEach(b -> b.setProcessing(next));
        });

        themeToggle = new Button();
        themeToggle.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-md", "p-2", "font-semibold");
        themeToggle.setOnAction(e -> themeToggle.setText(toggleCaption(ThemeManager.toggle(scene))));

        HBox header = new HBox(12, title, spacer, toggleAll, themeToggle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().addAll("bg-surface", "p-4");
        return header;
    }

    private static String toggleCaption(Theme current) {
        return current == Theme.LIGHT ? "Switch to dark theme" : "Switch to light theme";
    }

    // ---- Color section
    // ------------------------------------------------------------------------

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

    // ---- className: gradients
    // ------------------------------------------------------------------

    private static FlowPane gradientGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("3-stop, hover swaps direction", box(plainGradient())),
                labeled("4-stop (2x via-)", box(fourStopGradient())),
                labeled("hover: direction only, stops reused", box(hoverDirectionSwap())),
                labeled("pressed: swaps a gradient", box(pressedGradient())));
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

    // ---- className overriding Variant / Color
    // ----------------------------------------------------

    private static FlowPane overrideGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("className overrides Variant.PRIMARY", box(overridePrimary())),
                labeled("className overrides Color.RED (still Variant.DEFAULT)", box(overrideColor())),
                labeled("className gradient on top of Color.PURPLE", box(colorThenGradient())));
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
        // ...but this inline gradient wins visually, proving className > Color without
        // removing it.
        return button;
    }

    // ---- className: pseudo-class scoping
    // -----------------------------------------------------

    private static FlowPane pseudoClassGrid() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("hover: swaps a plain background", box(hoverPlainColor())),
                labeled("focus: (ring is a recognized no-op today)", box(focusRing())),
                labeled("disabled: overrides look while disabled", box(disabledOverride())));
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

    // ---- sanity checks
    // --------------------------------------------------------------------------

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
            button.setVariant(FxButton.Variant.OUTLINE);
            button.setColor(FxButton.Color.RED);
            button.setSize(s);
            button.setPill(true);
            flow.getChildren().add(button);
        }
        return flow;
    }

    // ---- processing: every variant
    // -----------------------------------------------------------

    private FlowPane processingVariantGrid() {
        FlowPane flow = new FlowPane(10, 10);
        for (FxButton.Variant v : FxButton.Variant.values()) {
            FxButton button = new FxButton(v.name());
            button.setVariant(v);
            flow.getChildren().add(labeled(v.name(), box(processing(button))));
        }
        return flow;
    }

    // ---- processing: every color (solid + outline)
    // ---------------------------------------------

    private VBox processingColorGrid() {
        FlowPane solid = new FlowPane(10, 10);
        FlowPane outline = new FlowPane(10, 10);
        for (FxButton.Color c : FxButton.Color.values()) {
            FxButton filled = new FxButton(c.name());
            filled.setVariant(FxButton.Variant.DEFAULT);
            filled.setColor(c);
            solid.getChildren().add(labeled("DEFAULT " + c.name(), box(processing(filled))));

            FxButton outlined = new FxButton(c.name());
            outlined.setVariant(FxButton.Variant.OUTLINE);
            outlined.setColor(c);
            outline.getChildren().add(labeled("OUTLINE " + c.name(), box(processing(outlined))));
        }
        return new VBox(16, solid, outline);
    }

    // ---- processing: sizes
    // ---------------------------------------------------------------------

    private FlowPane processingSizeGrid() {
        FlowPane flow = new FlowPane(10, 10);
        flow.setAlignment(Pos.CENTER_LEFT);
        for (FxButton.Size s : FxButton.Size.values()) {
            FxButton button = new FxButton(s.name());
            button.setSize(s);
            flow.getChildren().add(labeled(s.name(), box(processing(button))));
        }
        FxButton pill = new FxButton("PILL");
        pill.setPill(true);
        flow.getChildren().add(labeled("pill", box(processing(pill))));
        return flow;
    }

    // ---- processing: label and icon
    // ------------------------------------------------------------

    private FlowPane processingLabelGrid() {
        FlowPane flow = new FlowPane(16, 16);

        FxButton defaultLabel = new FxButton("Save");
        flow.getChildren().add(labeled("default label (\"Loading...\")", box(processing(defaultLabel))));

        FxButton customLabel = new FxButton("Save");
        customLabel.setProcessingLabel("Saving...");
        flow.getChildren().add(labeled("custom label", box(processing(customLabel))));

        FxButton blankLabel = new FxButton("Save");
        blankLabel.setProcessingLabel("");
        flow.getChildren().add(labeled("blank label keeps own text", box(processing(blankLabel))));

        FxButton nullLabel = new FxButton("Save");
        nullLabel.setProcessingLabel(null);
        flow.getChildren().add(labeled("null label keeps own text", box(processing(nullLabel))));

        FxButton withIcon = new FxButton("Run");
        withIcon.setIcon(ICON);
        flow.getChildren().add(labeled("icon + text: spinner replaces icon", box(processing(withIcon))));

        FxButton iconOnly = new FxButton();
        iconOnly.setIcon(ICON, "Run");
        flow.getChildren().add(labeled("icon only: stays icon-only, spinner only", box(processing(iconOnly))));

        FxButton empty = new FxButton();
        flow.getChildren().add(labeled("no text, no icon", box(processing(empty))));

        return flow;
    }

    // ---- processing: interactive
    // --------------------------------------------------------------

    private VBox interactiveSection() {
        int[] reached = { 0 };
        Label counter = new Label("onAction reached: 0");
        counter.getStyleClass().addAll("text-sm", "text-body");

        FxButton save = new FxButton("Save changes");
        save.setProcessingLabel("Saving...");
        save.setOnAction(e -> {
            reached[0]++;
            counter.setText("onAction reached: " + reached[0]);
            save.setProcessing(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> save.setProcessing(false));
            pause.play();
        });

        FxButton outlined = new FxButton("Outline, click me");
        outlined.setVariant(FxButton.Variant.OUTLINE);
        outlined.setColor(FxButton.Color.GREEN);
        outlined.setPill(true);
        outlined.setOnAction(e -> {
            outlined.setProcessing(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> outlined.setProcessing(false));
            pause.play();
        });

        FxButton danger = new FxButton("Delete");
        danger.setVariant(FxButton.Variant.DANGER);
        danger.setIcon(ICON);
        danger.setProcessingLabel("Deleting...");
        danger.setOnAction(e -> {
            danger.setProcessing(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> danger.setProcessing(false));
            pause.play();
        });

        HBox buttons = new HBox(12, save, outlined, danger);
        buttons.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(12, buttons, counter);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- processing: runtime changes
    // -------------------------------------------------------------

    private VBox runtimeChangesSection() {
        FxButton target = new FxButton("Target");
        target.setIcon(ICON);
        target.setProcessingLabel("Working...");

        int[] textN = { 0 };
        int[] labelN = { 0 };
        String[] labels = { "Working...", "Saving...", "", null };
        FxButton.Size[] sizes = FxButton.Size.values();
        FxButton.Variant[] variants = FxButton.Variant.values();
        int[] sizeN = { 0 };
        int[] variantN = { 0 };

        Button toggle = new Button("Toggle processing");
        toggle.setOnAction(e -> target.setProcessing(!target.isProcessing()));

        Button text = new Button("setText(new)");
        text.setOnAction(e -> target.setText("Text " + (++textN[0])));

        Button icon = new Button("Toggle icon");
        icon.setOnAction(e -> target.setIcon(target.getIcon() == null ? ICON : null));

        Button size = new Button("Next size");
        size.setOnAction(e -> target.setSize(sizes[++sizeN[0] % sizes.length]));

        Button variant = new Button("Next variant");
        variant.setOnAction(e -> target.setVariant(variants[++variantN[0] % variants.length]));

        Button label = new Button("Next label");
        label.setOnAction(e -> target.setProcessingLabel(labels[++labelN[0] % labels.length]));

        Button disable = new Button("Toggle disable");
        disable.setOnAction(e -> target.setDisable(!target.isDisabled()));

        FlowPane controls = new FlowPane(8, 8, toggle, text, icon, size, variant, label, disable);
        VBox box = new VBox(12, target, controls);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- processing: combined with className / disabled
    // ---------------------------------------------

    private FlowPane processingCombinedGrid() {
        FlowPane flow = new FlowPane(16, 16);

        FxButton gradient = new FxButton("Gradient");
        gradient.setClassName(
                "bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 "
                        + "text-white hover:bg-gradient-to-br");
        flow.getChildren().add(labeled("gradient + text-white", box(processing(gradient))));

        FxButton textOverride = new FxButton("Text override");
        textOverride.setVariant(FxButton.Variant.SECONDARY);
        textOverride.setClassName("text-purple-700 hover:text-pink-600");
        flow.getChildren().add(labeled("className text color (spinner follows it)", box(processing(textOverride))));

        FxButton disabled = new FxButton("Disabled");
        disabled.setDisable(true);
        flow.getChildren().add(labeled("disabled + processing", box(processing(disabled))));

        FxButton pillOutline = new FxButton("Pill outline");
        pillOutline.setVariant(FxButton.Variant.OUTLINE);
        pillOutline.setColor(FxButton.Color.PURPLE);
        pillOutline.setPill(true);
        flow.getChildren().add(labeled("pill + outline", box(processing(pillOutline))));

        return flow;
    }

    // ---- layout helpers
    // -----------------------------------------------------------------------

    /**
     * Puts {@code button} into processing state and registers it with the header's
     * "toggle all" button.
     */
    private FxButton processing(FxButton button) {
        button.setProcessing(true);
        processingButtons.add(button);
        return button;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox box(Node node) {
        VBox box = new VBox(6, node);
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