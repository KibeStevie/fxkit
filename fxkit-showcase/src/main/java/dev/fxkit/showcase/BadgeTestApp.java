package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.badge.FxBadge;
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
 * Isolated test harness for {@link FxBadge}: every Color (light and dark), both Sizes, text /
 * icon / icon-only modes, dismissible chips, programmatic dismiss, hug-content sizing, and a live
 * playground where every property can be flipped at runtime.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle: badges should switch to Flowbite's {@code dark:} shades, and
 * hovering any badge should darken its background one step.
 */
public class BadgeTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    /** The eight colors Flowbite's docs show in "Default badges". */
    private static final FxBadge.Color[] DOCS_COLORS = {
            FxBadge.Color.INFO, FxBadge.Color.GRAY, FxBadge.Color.FAILURE, FxBadge.Color.SUCCESS,
            FxBadge.Color.WARNING, FxBadge.Color.INDIGO, FxBadge.Color.PURPLE, FxBadge.Color.PINK };

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
                        "Every property live: text, color, size, icon, dismissible. Clear the text with "
                                + "an icon set to get the icon-only (circle) badge."),
                playground(),

                sectionHeading("Default badges (Flowbite docs set)",
                        "The eight colors from the docs page. Hover each: the background darkens one step."),
                defaultBadges(),

                sectionHeading("Color (all 17)",
                        "Synonyms (FAILURE/RED, SUCCESS/GREEN, WARNING/YELLOW) look identical; INFO/CYAN "
                                + "differ only in dark theme text."),
                allColors(),

                sectionHeading("Badge with icon",
                        "Icon before the text, gap-1. Check the icon color follows the badge text color."),
                iconBadges(),

                sectionHeading("Icon only",
                        "No text: fully rounded, uniform padding. Both colors and both sizes."),
                iconOnlyBadges(),

                sectionHeading("Sizes",
                        "XS (11px) vs SM (12px) text; SM icons are 14px. Each row is one size."),
                sizeRows(),

                sectionHeading("Dismissible badges (chips)",
                        "Close button + 1px border only once onDismiss is set. Click x: callback fires, "
                                + "then the badge leaves its parent. Restore re-adds them."),
                dismissSection(),

                sectionHeading("Programmatic dismiss",
                        "dismiss() called directly: after a 3s timeout, with and without a callback."),
                programmaticSection(),

                sectionHeading("Edge cases",
                        "Hug-content sizing in a VBox, empty badge, long text, dismiss() while detached, "
                                + "icon-only + dismiss, rapid style swaps."),
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

        stage.setTitle("FxBadge test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxBadge test harness");
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
        FxBadge badge = new FxBadge("Playground");
        HBox badgeHolder = new HBox(badge);
        badgeHolder.setMinHeight(40);
        badgeHolder.setAlignment(Pos.CENTER_LEFT);

        TextField textField = new TextField("Playground");
        textField.setPromptText("text (empty + icon = icon-only)");
        HBox.setHgrow(textField, Priority.ALWAYS);
        badge.textProperty().bind(textField.textProperty());

        ComboBox<FxBadge.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxBadge.Color.values());
        colorBox.valueProperty().bindBidirectional(badge.colorProperty());

        ComboBox<FxBadge.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxBadge.Size.values());
        sizeBox.valueProperty().bindBidirectional(badge.sizeProperty());

        CheckBox iconCheck = new CheckBox("Icon");
        iconCheck.selectedProperty().addListener((o, was, is) -> badge.setIcon(is ? ICON : null));

        CheckBox dismissCheck = new CheckBox("Dismissible");
        dismissCheck.selectedProperty().addListener((o, was, is) ->
                badge.setOnDismiss(is ? () -> log("playground: onDismiss fired") : null));

        Button restore = new Button("Restore badge");
        restore.setOnAction(e -> {
            if (!badgeHolder.getChildren().contains(badge)) {
                badgeHolder.getChildren().add(badge);
                log("playground: badge restored");
            }
        });

        HBox row1 = new HBox(10, new Label("Text"), textField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, new Label("Color"), colorBox, new Label("Size"), sizeBox,
                iconCheck, dismissCheck, restore);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, badgeHolder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- default badges -----------------------------------------------------------------------

    private static FlowPane defaultBadges() {
        FlowPane flow = new FlowPane(8, 8);
        for (FxBadge.Color c : DOCS_COLORS) {
            flow.getChildren().add(new FxBadge(displayName(c), c));
        }
        return card(flow);
    }

    // ---- all colors ---------------------------------------------------------------------------

    private static FlowPane allColors() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxBadge.Color c : FxBadge.Color.values()) {
            flow.getChildren().add(labeled(c.name(), new FxBadge(displayName(c), c)));
        }
        return flow;
    }

    // ---- icon ---------------------------------------------------------------------------------

    private static FlowPane iconBadges() {
        FlowPane flow = new FlowPane(8, 8);

        FxBadge minutes = new FxBadge("2 minutes ago");
        minutes.setIcon(ICON);

        FxBadge days = new FxBadge("3 days ago", FxBadge.Color.GRAY);
        days.setIcon(ICON);

        FxBadge smallSuccess = new FxBadge("Deployed", FxBadge.Color.SUCCESS);
        smallSuccess.setSize(FxBadge.Size.SM);
        smallSuccess.setIcon(ICON);

        FxBadge failure = new FxBadge("Failed", FxBadge.Color.FAILURE);
        failure.setIcon(ICON);

        FxBadge dark = new FxBadge("Dark + icon", FxBadge.Color.DARK);
        dark.setIcon(ICON);

        FxBadge light = new FxBadge("Light + icon", FxBadge.Color.LIGHT);
        light.setIcon(ICON);

        flow.getChildren().addAll(minutes, days, smallSuccess, failure, dark, light);
        return card(flow);
    }

    // ---- icon only ------------------------------------------------------------------------------

    private static FlowPane iconOnlyBadges() {
        FlowPane flow = new FlowPane(8, 8);
        flow.setAlignment(Pos.CENTER_LEFT);

        flow.getChildren().add(iconOnly(FxBadge.Color.INFO, FxBadge.Size.XS));
        flow.getChildren().add(iconOnly(FxBadge.Color.GRAY, FxBadge.Size.XS));
        flow.getChildren().add(iconOnly(FxBadge.Color.INFO, FxBadge.Size.SM));
        flow.getChildren().add(iconOnly(FxBadge.Color.GRAY, FxBadge.Size.SM));
        flow.getChildren().add(iconOnly(FxBadge.Color.SUCCESS, FxBadge.Size.XS));
        flow.getChildren().add(iconOnly(FxBadge.Color.FAILURE, FxBadge.Size.SM));
        flow.getChildren().add(iconOnly(FxBadge.Color.PURPLE, FxBadge.Size.XS));
        return card(flow);
    }

    private static FxBadge iconOnly(FxBadge.Color color, FxBadge.Size size) {
        FxBadge badge = new FxBadge();
        badge.setColor(color);
        badge.setSize(size);
        badge.setIcon(ICON);
        return badge;
    }

    // ---- sizes ----------------------------------------------------------------------------------

    private static VBox sizeRows() {
        VBox rows = new VBox(12);
        for (FxBadge.Size size : FxBadge.Size.values()) {
            FlowPane row = new FlowPane(8, 8);
            for (FxBadge.Color c : DOCS_COLORS) {
                FxBadge badge = new FxBadge(displayName(c), c);
                badge.setSize(size);
                row.getChildren().add(badge);
            }
            rows.getChildren().add(labeled("Size." + size.name(), row));
        }
        return rows;
    }

    // ---- dismissible chips ----------------------------------------------------------------------

    private VBox dismissSection() {
        FlowPane holder = new FlowPane(8, 8);
        FxBadge.Color[] colors = {
                FxBadge.Color.BLUE, FxBadge.Color.GRAY, FxBadge.Color.FAILURE,
                FxBadge.Color.SUCCESS, FxBadge.Color.WARNING, FxBadge.Color.DARK };
        FxBadge[] chips = new FxBadge[colors.length];
        for (int i = 0; i < colors.length; i++) {
            String name = displayName(colors[i]);
            FxBadge chip = new FxBadge(name, colors[i]);
            chip.setOnDismiss(() -> log("dismissed: " + name));
            chips[i] = chip;
            holder.getChildren().add(chip);
        }

        Button restore = new Button("Restore all");
        restore.setOnAction(e -> {
            for (FxBadge chip : chips) {
                if (!holder.getChildren().contains(chip)) {
                    holder.getChildren().add(chip);
                }
            }
            log("restored all chips");
        });

        Button hideClose = new Button("Remove onDismiss (close button + border should vanish)");
        hideClose.setOnAction(e -> {
            for (FxBadge chip : chips) {
                chip.setOnDismiss(null);
            }
            log("setOnDismiss(null) on all chips");
        });

        Button smallToggle = new Button("Toggle SM size on all");
        smallToggle.setOnAction(e -> {
            for (FxBadge chip : chips) {
                chip.setSize(chip.getSize() == FxBadge.Size.XS ? FxBadge.Size.SM : FxBadge.Size.XS);
            }
        });

        VBox box = new VBox(10, holder, new HBox(10, restore, hideClose, smallToggle));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- programmatic dismiss ---------------------------------------------------------------

    private VBox programmaticSection() {
        FlowPane holder = new FlowPane(8, 8);
        holder.setMinHeight(30);

        Button withCallback = new Button("Add badge (auto-dismiss 3s, with callback)");
        withCallback.setOnAction(e -> {
            FxBadge badge = new FxBadge("Auto-dismiss", FxBadge.Color.INFO);
            badge.setOnDismiss(() -> log("auto-dismiss: callback ran"));
            holder.getChildren().add(badge);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> badge.dismiss());
            pause.play();
        });

        Button noCallback = new Button("Add badge (auto-dismiss 3s, no callback / no close button)");
        noCallback.setOnAction(e -> {
            FxBadge badge = new FxBadge("No callback", FxBadge.Color.GRAY);
            holder.getChildren().add(badge);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                badge.dismiss();
                log("no-callback badge: dismiss() called");
            });
            pause.play();
        });

        VBox box = new VBox(10, new HBox(10, withCallback, noCallback), holder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        // A badge dropped in a VBox must hug its content, not stretch to the column width.
        VBox stretchTest = new VBox(6, new FxBadge("Hugs content", FxBadge.Color.SUCCESS),
                new FxBadge("Also hugs", FxBadge.Color.WARNING));
        stretchTest.setPrefWidth(260);
        flow.getChildren().add(labeled("in a VBox: should NOT stretch", stretchTest));

        FxBadge empty = new FxBadge();
        flow.getChildren().add(labeled("empty (no text, no icon)", empty));

        FxBadge longText = new FxBadge("A deliberately long badge label that should stay on one line",
                FxBadge.Color.PURPLE);
        longText.setIcon(ICON);
        flow.getChildren().add(labeled("long text + icon", longText));

        FxBadge iconOnlyDismiss = new FxBadge();
        iconOnlyDismiss.setColor(FxBadge.Color.TEAL);
        iconOnlyDismiss.setIcon(ICON);
        iconOnlyDismiss.setOnDismiss(() -> log("icon-only + dismiss: removed"));
        flow.getChildren().add(labeled("icon only + dismiss (chip layout, not circle)", iconOnlyDismiss));

        FxBadge detached = new FxBadge("Never added to a parent", FxBadge.Color.INDIGO);
        detached.setOnDismiss(() -> log("detached badge: callback ran, no exception"));
        Button detach = new Button("dismiss() on detached badge");
        detach.setOnAction(e -> detached.dismiss());
        flow.getChildren().add(labeled("dismiss() with no parent", new VBox(6, detach)));

        FxBadge toggling = new FxBadge("Icon toggles", FxBadge.Color.PINK);
        Button toggleIcon = new Button("Toggle icon (text stays)");
        toggleIcon.setOnAction(e -> toggling.setIcon(toggling.getIcon() == null ? ICON : null));
        Button toggleText = new Button("Toggle text (icon-only <-> icon+text)");
        toggleText.setOnAction(e -> toggling.setText(toggling.getText() == null ? "Icon toggles" : null));
        flow.getChildren().add(labeled("runtime icon/text toggles", new VBox(6, toggling, toggleIcon, toggleText)));

        FxBadge rapid = new FxBadge("Rapid updates");
        Button cycle = new Button("Cycle color + size + text");
        int[] n = {0};
        cycle.setOnAction(e -> {
            FxBadge.Color[] all = FxBadge.Color.values();
            n[0]++;
            rapid.setColor(all[n[0] % all.length]);
            rapid.setSize(n[0] % 2 == 0 ? FxBadge.Size.XS : FxBadge.Size.SM);
            rapid.setText(rapid.getColor().name() + " #" + n[0]);
        });
        flow.getChildren().add(labeled("style class swap (one color + one size class at a time)",
                new VBox(6, rapid, cycle)));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static String displayName(FxBadge.Color c) {
        String n = c.name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }

    private static FlowPane card(FlowPane flow) {
        flow.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return flow;
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
