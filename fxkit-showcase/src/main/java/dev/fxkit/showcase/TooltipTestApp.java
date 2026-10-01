package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.tooltip.FxTooltip;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Isolated test harness for {@link FxTooltip}: every Variant, Placement, Trigger and Animation, the
 * arrow flag, different kinds of target, rich content, screen-edge flipping, edge cases, and a live
 * playground where every property can be flipped at runtime.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check that tooltips restyle correctly in light and dark (the popup
 * copies the theme each time it opens, so toggle first, then hover).
 */
public class TooltipTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String SAMPLE = "Tooltip content";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (showing changes, clicks)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: edit the text, switch variant / placement / trigger / "
                                + "animation, toggle the arrow, show and hide from code."),
                playground(),

                sectionHeading("Default tooltip",
                        "Wrap the trigger, pass content. Hover the button: LIGHT, TOP, arrow, 300ms fade."),
                defaultSection(),

                sectionHeading("Variant",
                        "LIGHT is white with a gray border. DARK has no border. AUTO follows the theme: "
                                + "toggle the theme and compare it with LIGHT and DARK."),
                variantGrid(),

                sectionHeading("Placement",
                        "TOP, RIGHT, BOTTOM, LEFT: an 8px gap from the target, arrow centered on the "
                                + "target."),
                placementGrid(),

                sectionHeading("Trigger",
                        "HOVER opens while the pointer is over the target. CLICK toggles; click anywhere "
                                + "else or press Escape to close."),
                triggerGrid(),

                sectionHeading("Arrow", "arrow=false removes it; the box keeps its 8px gap."),
                arrowGrid(),

                sectionHeading("Animation",
                        "Fade duration: NONE, 150, 300, 500, 1000 ms. Leave the target mid-fade and re-enter: "
                                + "it must not flicker or get stuck."),
                animationGrid(),

                sectionHeading("Targets",
                        "Anything can be the target: labels, icon-only buttons, text fields, a whole card."),
                targetGrid(),

                sectionHeading("Rich content",
                        "setContentNode(node) replaces the text; setContentNode(null) goes back to it."),
                richContentSection(),

                sectionHeading("Screen edges",
                        "Drag the window so a button touches an edge of the screen: the tooltip should flip "
                                + "to the other side (or slide along the edge) and the arrow should still "
                                + "point at the target."),
                edgeSection(),

                sectionHeading("Edge cases",
                        "Empty content, long text, content changing while open, programmatic show/hide, "
                                + "disabled target, target removed while open."),
                edgeCases(),

                sectionHeading("Event log", "showing changes and button clicks land here."),
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

        stage.setTitle("FxTooltip test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxTooltip test harness");
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
        FxTooltip tip = new FxTooltip(SAMPLE, new FxButton("Playground target"));
        tip.showingProperty().addListener((o, was, is) -> log("playground: showing=" + is));

        TextField contentField = new TextField(SAMPLE);
        contentField.setPromptText("content");
        HBox.setHgrow(contentField, Priority.ALWAYS);
        tip.contentProperty().bind(contentField.textProperty());

        ComboBox<FxTooltip.Variant> variantBox = combo(FxTooltip.Variant.values());
        variantBox.valueProperty().bindBidirectional(tip.variantProperty());

        ComboBox<FxTooltip.Placement> placementBox = combo(FxTooltip.Placement.values());
        placementBox.valueProperty().bindBidirectional(tip.placementProperty());

        ComboBox<FxTooltip.Trigger> triggerBox = combo(FxTooltip.Trigger.values());
        triggerBox.valueProperty().bindBidirectional(tip.triggerProperty());

        ComboBox<FxTooltip.Animation> animationBox = combo(FxTooltip.Animation.values());
        animationBox.valueProperty().bindBidirectional(tip.animationProperty());

        CheckBox arrowCheck = new CheckBox("Arrow");
        arrowCheck.selectedProperty().bindBidirectional(tip.arrowProperty());

        Button show = new Button("show()");
        show.setOnAction(e -> tip.show());
        Button hide = new Button("hide()");
        hide.setOnAction(e -> tip.hide());

        HBox row1 = new HBox(10, new Label("Content"), contentField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16,
                new Label("Variant"), variantBox, new Label("Placement"), placementBox,
                new Label("Trigger"), triggerBox, new Label("Animation"), animationBox, arrowCheck);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(10, show, hide);
        row3.setAlignment(Pos.CENTER_LEFT);

        // Room around the target so every placement has space in the window.
        VBox stage = new VBox(tip);
        stage.setAlignment(Pos.CENTER);
        stage.setMinHeight(120);

        VBox box = new VBox(12, row1, row2, row3, stage);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- default ----------------------------------------------------------------------------

    private FlowPane defaultSection() {
        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().add(labeled("default", tip(SAMPLE, new FxButton("Default tooltip"))));
        return flow;
    }

    // ---- variant ----------------------------------------------------------------------------

    private FlowPane variantGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxTooltip.Variant v : FxTooltip.Variant.values()) {
            FxTooltip tip = tip(SAMPLE, new FxButton(v.name() + " tooltip"));
            tip.setVariant(v);
            flow.getChildren().add(labeled(v.name(), tip));
        }
        return flow;
    }

    // ---- placement --------------------------------------------------------------------------

    private FlowPane placementGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxTooltip.Placement p : FxTooltip.Placement.values()) {
            FxTooltip tip = tip(SAMPLE, new FxButton("Tooltip " + p.name().toLowerCase()));
            tip.setPlacement(p);
            flow.getChildren().add(labeled(p.name(), padded(tip)));
        }
        return flow;
    }

    // ---- trigger ----------------------------------------------------------------------------

    private FlowPane triggerGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxTooltip.Trigger t : FxTooltip.Trigger.values()) {
            FxTooltip tip = tip(SAMPLE, new FxButton("Tooltip " + t.name().toLowerCase()));
            tip.setTrigger(t);
            flow.getChildren().add(labeled(t.name(), tip));
        }

        FxTooltip switching = tip("Trigger flips on every click of the button below.",
                new FxButton("Switchable trigger"));
        Button flip = new Button("Toggle HOVER / CLICK");
        flip.setOnAction(e -> {
            switching.setTrigger(switching.getTrigger() == FxTooltip.Trigger.HOVER
                    ? FxTooltip.Trigger.CLICK : FxTooltip.Trigger.HOVER);
            log("trigger is now " + switching.getTrigger() + " (any open tooltip closes)");
        });
        flow.getChildren().add(labeled("runtime trigger change", new VBox(6, padded(switching), flip)));
        return flow;
    }

    // ---- arrow ------------------------------------------------------------------------------

    private FlowPane arrowGrid() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("arrow = true", tip(SAMPLE, new FxButton("With arrow"))));

        FxTooltip noArrow = tip(SAMPLE, new FxButton("Without arrow"));
        noArrow.setArrow(false);
        flow.getChildren().add(labeled("arrow = false", noArrow));

        FxTooltip toggling = tip("Arrow flag flips at runtime.", new FxButton("Runtime arrow"));
        Button toggle = new Button("Toggle arrow");
        toggle.setOnAction(e -> toggling.setArrow(!toggling.isArrow()));
        flow.getChildren().add(labeled("runtime toggle", new VBox(6, padded(toggling), toggle)));
        return flow;
    }

    // ---- animation --------------------------------------------------------------------------

    private FlowPane animationGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxTooltip.Animation a : FxTooltip.Animation.values()) {
            String caption = a == FxTooltip.Animation.NONE ? "Not animated" : a.name().replace("DURATION_", "") + " ms";
            FxTooltip tip = tip(SAMPLE, new FxButton(caption));
            tip.setAnimation(a);
            flow.getChildren().add(labeled(a.name(), tip));
        }
        return flow;
    }

    // ---- targets ----------------------------------------------------------------------------

    private FlowPane targetGrid() {
        FlowPane flow = new FlowPane(12, 12);

        Label label = new Label("Hover this label");
        label.getStyleClass().addAll("text-base", "text-body");
        flow.getChildren().add(labeled("Label", tip("Labels work too", label)));

        FxButton iconOnly = new FxButton();
        iconOnly.setIcon(ICON);
        flow.getChildren().add(labeled("icon-only FxButton", tip("Icon-only button", iconOnly)));

        TextField field = new TextField();
        field.setPromptText("Hover or type here");
        FxTooltip fieldTip = tip("Text field with a tooltip", field);
        fieldTip.setPlacement(FxTooltip.Placement.RIGHT);
        flow.getChildren().add(labeled("TextField (RIGHT)", fieldTip));

        VBox card = new VBox(4, new Label("A whole card"), new Label("The tooltip hugs the card's bounds."));
        card.getStyleClass().addAll("bg-surface-alt", "p-4", "rounded-lg");
        flow.getChildren().add(labeled("Region as target", padded(tip("Tooltip on a card", card))));

        FxTooltip nested = tip("Outer tooltip", new HBox(8,
                new FxButton("Left"), tip("Inner tooltip", new FxButton("Right (nested)"))));
        flow.getChildren().add(labeled("nested tooltips (both can open)", padded(nested)));
        return flow;
    }

    // ---- rich content -----------------------------------------------------------------------

    private VBox richContentSection() {
        FxTooltip tip = tip("Plain text content.", new FxButton("Hover me"));
        tip.setPlacement(FxTooltip.Placement.BOTTOM);

        Button setRich = new Button("Set rich content");
        setRich.setOnAction(e -> {
            Label heading = new Label("Rich content");
            heading.getStyleClass().add("font-bold");
            Label body = new Label("An icon and two labels, replacing the text.");
            body.setWrapText(true);
            VBox box = new VBox(2, heading, body);
            HBox row = new HBox(10, new FontIcon(ICON), box);
            row.setAlignment(Pos.CENTER_LEFT);
            tip.setContentNode(row);
        });

        Button clear = new Button("Clear (back to text)");
        clear.setOnAction(e -> tip.setContentNode(null));

        Button retext = new Button("Change text while node is set (should NOT change anything)");
        retext.setOnAction(e -> tip.setContent("Text " + System.nanoTime() % 1000));

        VBox box = new VBox(10, padded(tip), new HBox(10, setRich, clear, retext));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- screen edges -----------------------------------------------------------------------

    private HBox edgeSection() {
        HBox row = new HBox(12);
        for (FxTooltip.Placement p : FxTooltip.Placement.values()) {
            FxTooltip tip = tip("Wants to go " + p.name().toLowerCase() + ", flips if there is no room.",
                    new FxButton("Prefer " + p.name().toLowerCase()));
            tip.setPlacement(p);
            row.getChildren().add(tip);
        }
        row.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return row;
    }

    // ---- edge cases -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxTooltip longText = tip("This is a deliberately long tooltip that should wrap at about 320px "
                + "instead of becoming one very wide line, and the arrow should stay centered on the target.",
                new FxButton("Long text"));
        flow.getChildren().add(labeled("long text wraps", padded(longText)));

        FxTooltip empty = new FxTooltip("", new FxButton("Empty content"));
        flow.getChildren().add(labeled("empty content (never opens)", empty));

        FxTooltip changing = tip("Original text", new FxButton("Hover, then wait 2s"));
        Button change = new Button("Change text in 2s");
        change.setOnAction(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> changing.setContent(
                    "Changed while open - now a much longer line of text than before"));
            pause.play();
            log("hover the target now; text changes in 2s");
        });
        Button reset = new Button("Reset text");
        reset.setOnAction(e -> changing.setContent("Original text"));
        flow.getChildren().add(labeled("content changes while open (box resizes, stays anchored)",
                new VBox(6, padded(changing), new HBox(8, change, reset))));

        FxTooltip programmatic = tip("Opened from code", new FxButton("Programmatic"));
        programmatic.showingProperty().addListener((o, was, is) -> log("programmatic: showing=" + is));
        Button show = new Button("show()");
        show.setOnAction(e -> programmatic.show());
        Button hide = new Button("hide()");
        hide.setOnAction(e -> programmatic.hide());
        flow.getChildren().add(labeled("show() / hide()", new VBox(6, padded(programmatic), new HBox(8, show, hide))));

        FxButton disabledButton = new FxButton("Disabled target");
        disabledButton.setDisable(true);
        flow.getChildren().add(labeled("disabled target (does hover still reach the tooltip?)",
                tip("Tooltip on a disabled button", disabledButton)));

        HBox slot = new HBox();
        FxTooltip removable = tip("Remove the target while I am open", new FxButton("Target"));
        slot.getChildren().add(removable);
        Button remove = new Button("Remove from scene");
        remove.setOnAction(e -> {
            slot.getChildren().remove(removable);
            log("removed from scene (popup must close, no exception)");
        });
        Button add = new Button("Add back");
        add.setOnAction(e -> {
            if (!slot.getChildren().contains(removable)) {
                slot.getChildren().add(removable);
            }
        });
        flow.getChildren().add(labeled("removed from scene", new VBox(6, padded(slot), new HBox(8, remove, add))));

        FxTooltip clickLog = tip("Click this button: the button action and the tooltip both fire.",
                new FxButton("Click trigger + action"));
        clickLog.setTrigger(FxTooltip.Trigger.CLICK);
        ((FxButton) clickLog.getTarget()).setOnAction(e -> log("target action fired"));
        flow.getChildren().add(labeled("CLICK trigger does not swallow the target's action", padded(clickLog)));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------

    private FxTooltip tip(String content, Node target) {
        FxTooltip tip = new FxTooltip(content, target);
        tip.showingProperty().addListener((o, was, is) -> {
            if (is) {
                log("open: " + content);
            }
        });
        return tip;
    }

    /** Extra space so TOP/LEFT tooltips have room inside the labeled box. */
    private static Node padded(Node node) {
        VBox box = new VBox(node);
        box.setStyle("-fx-padding: 24 16 24 16;");
        return box;
    }

    private static <T> ComboBox<T> combo(T[] values) {
        ComboBox<T> box = new ComboBox<>();
        box.getItems().addAll(values);
        return box;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
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
