package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.alert.FxAlert;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
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
 * Isolated test harness for {@link FxAlert}: every Color, title/message combinations, icon,
 * border accent, dismissing (button, programmatic, auto-timeout, restore), custom content, edge
 * cases, and a live playground where every property can be flipped at runtime.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check that alerts restyle correctly in light and dark.
 */
public class AlertTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String SAMPLE_TITLE = "Info alert!";
    private static final String SAMPLE_MESSAGE = "Change a few things up and try submitting again.";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (dismiss callbacks, actions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: edit title/message, switch color, toggle icon, accent, "
                                + "close button and custom content. Watch the alert rebuild in place."),
                playground(),

                sectionHeading("Color (all 17)",
                        "Synonyms (INFO/CYAN, FAILURE/RED, SUCCESS/GREEN, WARNING/YELLOW) should look identical."),
                colorGrid(),

                sectionHeading("Body: title + message combinations",
                        "Title is bold (font-medium); message follows after a space. Either can be omitted."),
                bodyGrid(),

                sectionHeading("Icon",
                        "Icon sits before the body, 20px. Check it is readable against each color."),
                iconGrid(),

                sectionHeading("Border accent",
                        "4px top border in the alert's color; toggling the flag alone shows or hides it."),
                accentGrid(),

                sectionHeading("Dismissing",
                        "Close button appears only once onDismiss is set. Click x: callback fires, then the "
                                + "alert leaves its parent. Restore re-adds them."),
                dismissSection(),

                sectionHeading("Programmatic dismiss",
                        "dismiss() called directly: after a 3s timeout, with and without a callback."),
                programmaticSection(),

                sectionHeading("Custom content",
                        "setContent(node) overrides title/message entirely; setContent(null) goes back."),
                customContentSection(),

                sectionHeading("Edge cases",
                        "Long wrapping text, empty alert, blank title, dismiss() while detached."),
                edgeCases(),

                sectionHeading("Event log", "Everything the dismiss callbacks report lands here."),
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

        stage.setTitle("FxAlert test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxAlert test harness");
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
        FxAlert alert = new FxAlert();
        VBox alertHolder = new VBox(alert);

        TextField titleField = new TextField(SAMPLE_TITLE);
        TextField messageField = new TextField(SAMPLE_MESSAGE);
        titleField.setPromptText("title");
        messageField.setPromptText("message");
        HBox.setHgrow(titleField, Priority.SOMETIMES);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        alert.titleProperty().bind(titleField.textProperty());
        alert.messageProperty().bind(messageField.textProperty());

        ComboBox<FxAlert.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxAlert.Color.values());
        colorBox.valueProperty().bindBidirectional(alert.colorProperty());

        CheckBox iconCheck = new CheckBox("Icon");
        iconCheck.selectedProperty().addListener((o, was, is) -> alert.setIcon(is ? ICON : null));

        CheckBox accentCheck = new CheckBox("Border accent");
        alert.borderAccentProperty().bind(accentCheck.selectedProperty());

        CheckBox dismissCheck = new CheckBox("Dismissible");
        dismissCheck.selectedProperty().addListener((o, was, is) ->
                alert.setOnDismiss(is ? () -> log("playground: onDismiss fired") : null));

        CheckBox customCheck = new CheckBox("Custom content");
        customCheck.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                Label custom = new Label("Custom node - title/message are ignored while this is set.");
                custom.setWrapText(true);
                alert.setContent(custom);
            } else {
                alert.setContent(null);
            }
        });

        Button restore = new Button("Restore alert");
        restore.setOnAction(e -> {
            if (!alertHolder.getChildren().contains(alert)) {
                alertHolder.getChildren().add(alert);
                log("playground: alert restored");
            }
        });

        HBox row1 = new HBox(10, new Label("Title"), titleField, new Label("Message"), messageField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, new Label("Color"), colorBox, iconCheck, accentCheck, dismissCheck,
                customCheck, restore);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, alertHolder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- colors -----------------------------------------------------------------------------

    private static FlowPane colorGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAlert.Color c : FxAlert.Color.values()) {
            FxAlert alert = new FxAlert(c.name() + " alert!", "Something happened.");
            alert.setColor(c);
            alert.setPrefWidth(300);
            flow.getChildren().add(labeled(c.name(), alert));
        }
        return flow;
    }

    // ---- body combinations --------------------------------------------------------------------

    private static FlowPane bodyGrid() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("title + message", sized(new FxAlert(SAMPLE_TITLE, SAMPLE_MESSAGE))));
        flow.getChildren().add(labeled("message only", sized(new FxAlert(SAMPLE_MESSAGE))));

        FxAlert titleOnly = new FxAlert();
        titleOnly.setTitle("Title only, no message");
        flow.getChildren().add(labeled("title only", sized(titleOnly)));

        flow.getChildren().add(labeled("blank title + message (treated as message only)",
                sized(new FxAlert("   ", SAMPLE_MESSAGE))));
        return flow;
    }

    // ---- icon -----------------------------------------------------------------------------------

    private static FlowPane iconGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAlert.Color c : new FxAlert.Color[] {
                FxAlert.Color.INFO, FxAlert.Color.FAILURE, FxAlert.Color.SUCCESS,
                FxAlert.Color.WARNING, FxAlert.Color.DARK, FxAlert.Color.LIGHT }) {
            FxAlert alert = new FxAlert(c.name(), "with an icon.");
            alert.setColor(c);
            alert.setIcon(ICON);
            flow.getChildren().add(labeled(c.name() + " + icon", sized(alert)));
        }

        FxAlert removable = new FxAlert("Icon toggles", "Click the button to add/remove the icon at runtime.");
        removable.setColor(FxAlert.Color.PURPLE);
        Button toggle = new Button("Toggle icon");
        toggle.setOnAction(e -> removable.setIcon(removable.getIcon() == null ? ICON : null));
        flow.getChildren().add(labeled("runtime icon toggle", new VBox(6, sized(removable), toggle)));
        return flow;
    }

    // ---- border accent -----------------------------------------------------------------------

    private static FlowPane accentGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAlert.Color c : new FxAlert.Color[] {
                FxAlert.Color.INFO, FxAlert.Color.FAILURE, FxAlert.Color.SUCCESS,
                FxAlert.Color.WARNING, FxAlert.Color.GRAY, FxAlert.Color.INDIGO }) {
            FxAlert alert = new FxAlert(c.name(), "with a top accent.");
            alert.setColor(c);
            alert.setBorderAccent(true);
            flow.getChildren().add(labeled(c.name() + " + accent", sized(alert)));
        }

        FxAlert combo = new FxAlert("Everything on", "Icon + accent + close button.");
        combo.setColor(FxAlert.Color.TEAL);
        combo.setIcon(ICON);
        combo.setBorderAccent(true);
        combo.setOnDismiss(() -> {});
        flow.getChildren().add(labeled("icon + accent + dismiss", sized(combo)));

        FxAlert toggling = new FxAlert("Accent toggles", "Click the button to flip the flag.");
        toggling.setColor(FxAlert.Color.LIME);
        Button toggle = new Button("Toggle accent");
        toggle.setOnAction(e -> toggling.setBorderAccent(!toggling.isBorderAccent()));
        flow.getChildren().add(labeled("runtime accent toggle", new VBox(6, sized(toggling), toggle)));
        return flow;
    }

    // ---- dismissing -------------------------------------------------------------------------------

    private VBox dismissSection() {
        VBox holder = new VBox(10);
        FxAlert[] alerts = new FxAlert[3];
        FxAlert.Color[] colors = { FxAlert.Color.SUCCESS, FxAlert.Color.FAILURE, FxAlert.Color.WARNING };
        for (int i = 0; i < alerts.length; i++) {
            String name = colors[i].name();
            FxAlert alert = new FxAlert(name + " dismissible", "Click x to dismiss me.");
            alert.setColor(colors[i]);
            alert.setIcon(ICON);
            alert.setOnDismiss(() -> log("dismissed: " + name));
            alerts[i] = alert;
            holder.getChildren().add(alert);
        }

        Button restore = new Button("Restore all");
        restore.setOnAction(e -> {
            for (FxAlert a : alerts) {
                if (!holder.getChildren().contains(a)) {
                    holder.getChildren().add(a);
                }
            }
            log("restored all dismissible alerts");
        });

        Button hideClose = new Button("Remove onDismiss (close button should vanish)");
        hideClose.setOnAction(e -> {
            for (FxAlert a : alerts) {
                a.setOnDismiss(null);
            }
            log("setOnDismiss(null) on all");
        });

        HBox buttons = new HBox(10, restore, hideClose);
        VBox box = new VBox(10, holder, buttons);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- programmatic dismiss ---------------------------------------------------------------

    private VBox programmaticSection() {
        VBox holder = new VBox(10);

        Button withCallback = new Button("Add alert (auto-dismiss 3s, with callback)");
        withCallback.setOnAction(e -> {
            FxAlert alert = new FxAlert("Auto-dismiss", "I disappear in 3 seconds and log a callback.");
            alert.setColor(FxAlert.Color.INFO);
            alert.setOnDismiss(() -> log("auto-dismiss: callback ran"));
            holder.getChildren().add(alert);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> alert.dismiss());
            pause.play();
        });

        Button noCallback = new Button("Add alert (auto-dismiss 3s, no callback / no close button)");
        noCallback.setOnAction(e -> {
            FxAlert alert = new FxAlert("No callback", "dismiss() should still remove me, silently.");
            alert.setColor(FxAlert.Color.GRAY);
            holder.getChildren().add(alert);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                alert.dismiss();
                log("no-callback alert: dismiss() called");
            });
            pause.play();
        });

        VBox box = new VBox(10, new HBox(10, withCallback, noCallback), holder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- custom content ---------------------------------------------------------------------------

    private VBox customContentSection() {
        FxAlert alert = new FxAlert("Default body", "Click 'Set custom content' to replace me.");
        alert.setColor(FxAlert.Color.INDIGO);
        alert.setIcon(ICON);
        alert.setOnDismiss(() -> log("custom-content alert dismissed"));

        Button setCustom = new Button("Set custom content");
        setCustom.setOnAction(e -> {
            Label heading = new Label("Custom body");
            heading.getStyleClass().add("font-bold");
            Label text = new Label("A VBox with a label and a button, replacing title/message.");
            text.setWrapText(true);
            Button action = new Button("Inner action");
            action.setOnAction(ev -> log("inner button clicked"));
            alert.setContent(new VBox(6, heading, text, action));
        });

        Button clear = new Button("Clear custom content (back to default)");
        clear.setOnAction(e -> alert.setContent(null));

        Button retitle = new Button("Change title while custom is set (should NOT change anything)");
        retitle.setOnAction(e -> alert.setTitle("Title " + System.nanoTime() % 1000));

        VBox box = new VBox(10, alert, new HBox(10, setCustom, clear, retitle));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxAlert longText = new FxAlert("Long text",
                "This is a deliberately long message that should wrap inside the alert rather than "
                        + "overflowing, and the close button should stay pinned to the far end of the row "
                        + "while the icon keeps its size and alignment on the left.");
        longText.setColor(FxAlert.Color.WARNING);
        longText.setIcon(ICON);
        longText.setBorderAccent(true);
        longText.setOnDismiss(() -> {});
        longText.setPrefWidth(420);
        flow.getChildren().add(labeled("long text wraps, close pinned right", longText));

        FxAlert empty = new FxAlert();
        empty.setPrefWidth(300);
        flow.getChildren().add(labeled("empty alert (no title/message)", empty));

        FxAlert detached = new FxAlert("Never added to a parent", "dismiss() should run its callback safely.");
        detached.setOnDismiss(() -> log("detached alert: callback ran, no exception"));
        Button detach = new Button("dismiss() on detached alert");
        detach.setOnAction(e -> detached.dismiss());
        flow.getChildren().add(labeled("dismiss() with no parent", new VBox(6, detach)));

        FxAlert rapid = new FxAlert("Rapid updates", "Color, title and message change every click.");
        Button cycle = new Button("Cycle color + text");
        int[] n = {0};
        cycle.setOnAction(e -> {
            FxAlert.Color[] all = FxAlert.Color.values();
            n[0]++;
            rapid.setColor(all[n[0] % all.length]);
            rapid.setTitle(rapid.getColor().name() + " #" + n[0]);
            rapid.setMessage("Only one color class should be present at a time.");
        });
        flow.getChildren().add(labeled("style class swap", new VBox(6, sized(rapid), cycle)));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxAlert sized(FxAlert alert) {
        alert.setPrefWidth(300);
        return alert;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox labeled(String caption, javafx.scene.Node content) {
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
