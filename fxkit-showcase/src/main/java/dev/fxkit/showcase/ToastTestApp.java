package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.toast.FxToast;
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
 * Isolated test harness for {@link FxToast}: the three Flowbite examples, every
 * Color in both
 * Variants, body combinations, dismissing (button, callback, restore),
 * auto-dismiss with hover
 * pause, programmatic dismiss, custom content, edge cases, and a live
 * playground.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check that toasts restyle correctly in light
 * and dark.
 *
 * <p>
 * Note: a dismissed FxToast is single-use (like Flowbite's, which unmounts), so
 * every
 * "restore" here builds fresh toasts instead of re-adding the old ones.
 */
public class ToastTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String SAMPLE_MESSAGE = "Set yourself free.";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (onDismissed callbacks)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Edit the message, switch color/variant, toggle icon, close button, auto-dismiss "
                                + "and custom content. The toast is rebuilt on every change, which also "
                                + "restores it after a dismiss."),
                playground(),

                sectionHeading("Flowbite examples",
                        "Default toast, toast colors, feedback toast - the three reference examples."),
                flowbiteExamples(),

                sectionHeading("Color x Variant (all 4 colors)",
                        "Left: DEFAULT (colored icon box). Right: FEEDBACK (bare icon in the color's shade)."),
                colorGrid(),

                sectionHeading("Body combinations",
                        "Text only, icon only, icon + text, and a custom content node."),
                bodyGrid(),

                sectionHeading("Dismissing",
                        "Close button appears only when dismissible. Click x: 300ms fade, then the callback "
                                + "fires and the toast leaves its parent. Restore builds new ones."),
                dismissSection(),

                sectionHeading("Auto-dismiss and programmatic dismiss",
                        "autoDismiss counts down once the toast is in a scene and pauses while the pointer "
                                + "is over it. dismiss() can also be called directly."),
                autoSection(),

                sectionHeading("Custom content",
                        "setContent(node) replaces the text label; setContent(null) goes back."),
                customContentSection(),

                sectionHeading("Edge cases",
                        "Long wrapping text, empty toast, null color/variant, double dismiss, dismiss while "
                                + "detached, rapid style changes."),
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

        stage.setTitle("FxToast test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxToast test harness");
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
        VBox toastHolder = new VBox();
        toastHolder.setPadding(new javafx.geometry.Insets(8)); // room for the shadow

        TextField messageField = new TextField(SAMPLE_MESSAGE);
        messageField.setPromptText("message");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        ComboBox<FxToast.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxToast.Color.values());
        colorBox.setValue(FxToast.Color.INFO);

        ComboBox<FxToast.Variant> variantBox = new ComboBox<>();
        variantBox.getItems().addAll(FxToast.Variant.values());
        variantBox.setValue(FxToast.Variant.DEFAULT);

        CheckBox iconCheck = new CheckBox("Icon");
        iconCheck.setSelected(true);
        CheckBox dismissCheck = new CheckBox("Dismissible");
        dismissCheck.setSelected(true);
        CheckBox autoCheck = new CheckBox("Auto-dismiss (4s)");
        CheckBox customCheck = new CheckBox("Custom content");

        Runnable rebuild = () -> {
            FxToast toast = new FxToast(iconCheck.isSelected() ? ICON : null, messageField.getText());
            toast.setColor(colorBox.getValue());
            toast.setVariant(variantBox.getValue());
            toast.setDismissible(dismissCheck.isSelected());
            if (autoCheck.isSelected()) {
                toast.setAutoDismiss(Duration.seconds(4));
            }
            if (customCheck.isSelected()) {
                Label custom = new Label("Custom node - text is ignored while this is set.");
                custom.setWrapText(true);
                toast.setContent(custom);
            }
            toast.setOnDismissed(e -> {
                log("playground: onDismissed fired");
                toastHolder.getChildren().remove(toast);
            });
            toastHolder.getChildren().setAll(toast);
        };

        messageField.textProperty().addListener((o, a, b) -> rebuild.run());
        colorBox.valueProperty().addListener((o, a, b) -> rebuild.run());
        variantBox.valueProperty().addListener((o, a, b) -> rebuild.run());
        iconCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());
        dismissCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());
        autoCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());
        customCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());

        Button restore = new Button("Restore toast");
        restore.setOnAction(e -> {
            rebuild.run();
            log("playground: toast restored");
        });

        HBox row1 = new HBox(10, new Label("Message"), messageField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, new Label("Color"), colorBox, new Label("Variant"), variantBox);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(16, iconCheck, dismissCheck, autoCheck, customCheck, restore);
        row3.setAlignment(Pos.CENTER_LEFT);

        rebuild.run();

        VBox box = new VBox(12, row1, row2, row3, toastHolder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- Flowbite examples
    // -----------------------------------------------------------------

    private FlowPane flowbiteExamples() {
        FlowPane flow = new FlowPane(12, 12);

        FxToast fire = toast(FxToast.Color.INFO, "Set yourself free.", true);
        flow.getChildren().add(labeled("Default toast", fire));

        FxToast moved = toast(FxToast.Color.SUCCESS, "Item moved successfully.", true);
        FxToast deleted = toast(FxToast.Color.FAILURE, "Item has been deleted.", true);
        FxToast password = toast(FxToast.Color.WARNING, "Improve password difficulty.", true);
        flow.getChildren().add(labeled("Toast colors", new VBox(16, moved, deleted, password)));

        FxToast sent = new FxToast(ICON, "Message sent successfully.");
        sent.setVariant(FxToast.Variant.FEEDBACK);
        flow.getChildren().add(labeled("Feedback toast", sent));
        return flow;
    }

    // ---- colors
    // -----------------------------------------------------------------------------

    private FlowPane colorGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxToast.Color c : FxToast.Color.values()) {
            FxToast box = toast(c, c.name() + " toast.", true);

            FxToast bare = new FxToast(ICON, c.name() + " feedback.");
            bare.setColor(c);
            bare.setVariant(FxToast.Variant.FEEDBACK);

            flow.getChildren().add(labeled(c.name(), new VBox(16, box, bare)));
        }
        return flow;
    }

    // ---- body combinations
    // ------------------------------------------------------------------

    private FlowPane bodyGrid() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("icon + text", toast(FxToast.Color.INFO, "Icon and text.", false)));

        FxToast textOnly = new FxToast("Text only, no icon.");
        textOnly.setDismissible(true);
        flow.getChildren().add(labeled("text only", textOnly));

        FxToast iconOnly = new FxToast(ICON, "");
        iconOnly.setColor(FxToast.Color.SUCCESS);
        flow.getChildren().add(labeled("icon only (empty text)", iconOnly));

        FxToast custom = new FxToast(ICON, "");
        custom.setColor(FxToast.Color.WARNING);
        custom.setContent(customBody(custom));
        custom.setDismissible(true);
        flow.getChildren().add(labeled("custom content node", custom));
        return flow;
    }

    // ---- dismissing
    // -------------------------------------------------------------------------

    private VBox dismissSection() {
        VBox holder = new VBox(10);
        holder.setPadding(new javafx.geometry.Insets(8));
        Runnable fill = () -> {
            holder.getChildren().clear();
            for (FxToast.Color c : new FxToast.Color[] {
                    FxToast.Color.SUCCESS, FxToast.Color.FAILURE, FxToast.Color.WARNING }) {
                FxToast t = toast(c, c.name() + " dismissible - click x.", true);
                t.setOnDismissed(e -> {
                    log("dismissed: " + c.name() + " (closed=" + t.isClosed() + ")");
                    holder.getChildren().remove(t);
                });
                holder.getChildren().add(t);
            }
        };
        fill.run();

        Button restore = new Button("Restore all");
        restore.setOnAction(e -> {
            fill.run();
            log("restored all dismissible toasts");
        });

        Button hideClose = new Button("setDismissible(false) on all (close button should vanish)");
        hideClose.setOnAction(e -> {
            holder.getChildren().forEach(n -> ((FxToast) n).setDismissible(false));
            log("setDismissible(false) on all");
        });

        Button showClose = new Button("setDismissible(true) on all");
        showClose.setOnAction(e -> {
            holder.getChildren().forEach(n -> ((FxToast) n).setDismissible(true));
            log("setDismissible(true) on all");
        });

        VBox box = new VBox(10, holder, new HBox(10, restore, hideClose, showClose));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- auto-dismiss / programmatic
    // --------------------------------------------------------

    private VBox autoSection() {
        VBox holder = new VBox(10);
        holder.setPadding(new javafx.geometry.Insets(8));

        Button auto = new Button("Add toast (autoDismiss 3s)");
        auto.setOnAction(e -> {
            FxToast t = toast(FxToast.Color.INFO, "I disappear in 3 seconds.", false);
            t.setAutoDismiss(Duration.seconds(3));
            removeWhenDismissed(holder, t, "autoDismiss 3s");
            holder.getChildren().add(t);
        });

        Button hover = new Button("Add toast (autoDismiss 6s - hover to pause)");
        hover.setOnAction(e -> {
            FxToast t = toast(FxToast.Color.WARNING, "Hover me: the countdown pauses.", true);
            t.setAutoDismiss(Duration.seconds(6));
            removeWhenDismissed(holder, t, "autoDismiss 6s with hover");
            holder.getChildren().add(t);
        });

        Button programmatic = new Button("Add toast (dismiss() after 3s, no close button)");
        programmatic.setOnAction(e -> {
            FxToast t = toast(FxToast.Color.FAILURE, "dismiss() will be called on me.", false);
            removeWhenDismissed(holder, t, "programmatic dismiss()");
            holder.getChildren().add(t);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> t.dismiss());
            pause.play();
        });

        Button silent = new Button("Add toast (dismiss(), no callback)");
        silent.setOnAction(e -> {
            FxToast t = toast(FxToast.Color.SUCCESS, "No callback: I hide silently (stay in the list).", false);
            holder.getChildren().add(t);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> {
                t.dismiss();
                log("no-callback toast: dismiss() called (hidden + unmanaged, still a child)");
            });
            pause.play();
        });

        VBox box = new VBox(10, new HBox(10, auto, hover), new HBox(10, programmatic, silent), holder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private void removeWhenDismissed(VBox holder, FxToast t, String what) {
        t.setOnDismissed(e -> {
            log("dismissed: " + what);
            holder.getChildren().remove(t);
        });
    }

    // ---- custom content
    // ---------------------------------------------------------------------

    private VBox customContentSection() {
        FxToast toast = toast(FxToast.Color.INFO, "Default body - click 'Set custom content'.", true);
        toast.setOnDismissed(e -> log("custom-content toast dismissed"));

        Button setCustom = new Button("Set custom content");
        setCustom.setOnAction(e -> toast.setContent(customBody(toast)));

        Button clear = new Button("Clear custom content (back to text)");
        clear.setOnAction(e -> toast.setContent(null));

        Button retext = new Button("Change text while custom is set (should NOT change anything)");
        retext.setOnAction(e -> toast.setText("Text " + System.nanoTime() % 1000));

        VBox box = new VBox(10, toast, new HBox(10, setCustom, clear, retext));
        box.setPadding(new javafx.geometry.Insets(8));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private Node customBody(FxToast owner) {
        Label heading = new Label("Custom body");
        heading.getStyleClass().add("font-bold");
        Label text = new Label("A VBox with a label and a button.");
        text.setWrapText(true);
        Button action = new Button("Inner action");
        action.setOnAction(e -> log("inner button clicked"));
        return new VBox(6, heading, text, action);
    }

    // ---- edge cases
    // -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxToast longText = toast(FxToast.Color.WARNING,
                "This is a deliberately long message that should wrap inside the toast rather than "
                        + "overflowing, while the close button stays pinned to the far end of the row "
                        + "and the icon keeps its 32px box.",
                true);
        flow.getChildren().add(labeled("long text wraps, close pinned right", longText));

        flow.getChildren().add(labeled("empty toast (no icon, no text)", new FxToast()));

        FxToast nulls = toast(FxToast.Color.INFO, "Color and variant are null.", true);
        nulls.setColor(null);
        nulls.setVariant(null);
        flow.getChildren().add(labeled("null color/variant (neutral fallback)", nulls));

        FxToast twice = toast(FxToast.Color.FAILURE, "Double-click x fast: one callback only.", true);
        twice.setOnDismissed(e -> log("double dismiss: callback ran (should appear once)"));
        flow.getChildren().add(labeled("double dismiss", twice));

        FxToast detached = new FxToast(ICON, "Never added to a parent.");
        detached.setOnDismissed(e -> log("detached toast: callback ran, no exception"));
        Button detach = new Button("dismiss() on detached toast");
        detach.setOnAction(e -> detached.dismiss());
        flow.getChildren().add(labeled("dismiss() with no parent", detach));

        FxToast rapid = toast(FxToast.Color.INFO, "Color and variant change every click.", true);
        Button cycle = new Button("Cycle color + variant");
        int[] n = { 0 };
        cycle.setOnAction(e -> {
            FxToast.Color[] colors = FxToast.Color.values();
            FxToast.Variant[] variants = FxToast.Variant.values();
            n[0]++;
            rapid.setColor(colors[n[0] % colors.length]);
            rapid.setVariant(variants[(n[0] / colors.length) % variants.length]);
            rapid.setText(rapid.getColor() + " / " + rapid.getVariant() + " #" + n[0]);
        });
        flow.getChildren().add(labeled("style class swap (one color class at a time)",
                new VBox(6, rapid, cycle)));

        FxToast iconSwap = new FxToast("Icon toggles at runtime.");
        iconSwap.setColor(FxToast.Color.SUCCESS);
        Button toggleIcon = new Button("Toggle icon");
        toggleIcon.setOnAction(e -> iconSwap.setIcon(iconSwap.getIcon() == null ? ICON : null));
        flow.getChildren().add(labeled("runtime icon toggle", new VBox(6, iconSwap, toggleIcon)));
        return flow;
    }

    // ---- helpers
    // ----------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxToast toast(FxToast.Color color, String text, boolean dismissible) {
        FxToast toast = new FxToast(ICON, text);
        toast.setColor(color);
        toast.setDismissible(dismissible);
        return toast;
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
