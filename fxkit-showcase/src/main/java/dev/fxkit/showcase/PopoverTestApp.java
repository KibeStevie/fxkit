package dev.fxkit.showcase;

import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.form.FxCheckbox;
import dev.fxkit.core.components.form.FxLabel;
import dev.fxkit.core.components.form.FxTextInput;
import dev.fxkit.core.components.popover.FxPopover;
import dev.fxkit.core.components.popover.FxPopoverPanel;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Isolated showcase for {@link FxPopover}: one section per example in Flowbite
 * React's Popover docs
 * (default, company profile, image, password strength, controlled, placement,
 * trigger type, disable
 * arrow), with a button to flip between the light and dark theme.
 *
 * <p>
 * Run it with the module path set up like the other showcases, e.g.
 * {@code mvn javafx:run -Djavafx.mainClass=dev.fxkit.showcase.PopoverTestApp}.
 */
public class PopoverTestApp extends Application {

    private static final String SAMPLE_TEXT = "And here's some amazing content. It's very engaging. Right?";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        VBox page = new VBox(24);
        page.getStyleClass().add("bg-background");
        page.setPadding(new Insets(24));

        page.getChildren().addAll(
                header(),
                defaultPopover(),
                companyProfile(),
                imagePopover(),
                passwordStrength(),
                controlled(),
                placement(),
                triggerType(),
                disableArrow());

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        BorderPane root = new BorderPane(scroll);
        root.getStyleClass().add("bg-background");

        Scene scene = new Scene(root, 960, 760);
        ThemeManager.apply(scene, Theme.LIGHT);

        stage.setTitle("FxPopover showcase");
        stage.setScene(scene);
        stage.show();
    }

    // ---- page chrome
    // ---------------------------------------------------------------------------

    private Node header() {
        Label title = label("FxPopover", "text-2xl", "font-semibold", "text-body");

        FxButton toggle = new FxButton("Toggle dark mode");
        toggle.setVariant(FxButton.Variant.SECONDARY);
        toggle.setOnAction(event -> ThemeManager.toggle(toggle.getScene()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, title, spacer, toggle);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Node section(String title, String description, Node demo) {
        VBox card = new VBox(16, demo);
        card.getStyleClass().addAll("bg-surface", "border", "rounded-lg", "p-6");

        Label heading = label(title, "text-xl", "font-semibold", "text-body");
        Label text = label(description, "text-sm", "text-muted");
        text.setWrapText(true);
        return new VBox(8, heading, text, card);
    }

    private static Label label(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private static FxPopoverPanel samplePanel() {
        return new FxPopoverPanel("Popover title", SAMPLE_TEXT);
    }

    // ---- 1. default
    // ----------------------------------------------------------------------------

    private Node defaultPopover() {
        FxPopover popover = new FxPopover(new FxButton("Default popover"), samplePanel());
        return section("Default popover",
                "Wrap the trigger in an FxPopover and give it content. The popover opens when the trigger is clicked.",
                popover);
    }

    // ---- 2. company profile
    // --------------------------------------------------------------------

    private Node companyProfile() {
        Label avatar = label("JL", "bg-blue-600", "text-white", "font-semibold", "rounded-full");
        avatar.setAlignment(Pos.CENTER);
        avatar.setMinSize(40, 40);
        avatar.setPrefSize(40, 40);
        avatar.setMaxSize(40, 40);

        FxButton follow = new FxButton("Follow");
        follow.setSize(FxButton.Size.XS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(avatar, spacer, follow);
        top.setAlignment(Pos.CENTER_LEFT);

        Label name = label("Jese Leos", "text-base", "font-semibold", "text-body");
        Label handle = label("@jeseleos", "text-sm", "text-muted");

        Text plain = new Text("Open-source contributor. Building ");
        plain.getStyleClass().addAll("text-sm", "text-muted");
        Text link = new Text("flowbite.com");
        link.getStyleClass().addAll("text-sm", "text-blue-600");
        Text dot = new Text(".");
        dot.getStyleClass().addAll("text-sm", "text-muted");
        TextFlow about = new TextFlow(plain, link, dot);

        HBox stats = new HBox(16, stat("799", "Following"), stat("3,758", "Followers"));

        VBox content = new VBox(8, top, new VBox(2, name, handle), about, stats);
        content.setPadding(new Insets(12));
        content.setPrefWidth(256);
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMaxWidth(Region.USE_PREF_SIZE);

        return section("Company profile",
                "Show more information about a profile, company or person when the trigger is clicked.",
                new FxPopover(new FxButton("Company profile"), content));
    }

    private static Node stat(String value, String caption) {
        return new HBox(4,
                label(value, "text-sm", "font-semibold", "text-body"),
                label(caption, "text-sm", "text-muted"));
    }

    // ---- 3. image popover
    // ----------------------------------------------------------------------

    private Node imagePopover() {
        Label readMore = label("Read more \u203A", "text-sm", "font-medium", "text-blue-600");
        Label about = label("About Italy", "text-sm", "font-semibold", "text-body");
        Label body = label("Italy is located in the middle of the Mediterranean Sea, in Southern Europe it is "
                + "also considered part of Western Europe. A unitary parliamentary republic with Rome as its "
                + "capital and largest city.", "text-sm", "text-muted");
        body.setWrapText(true);

        VBox text = new VBox(8, about, body, readMore);
        text.setPadding(new Insets(12));

        // The Flowbite example loads a map image; swap this placeholder for an
        // ImageView.
        Label mapCaption = label("Italy map", "text-sm", "text-blue-800");
        StackPane map = new StackPane(mapCaption);
        map.getStyleClass().add("bg-blue-200");

        GridPane grid = new GridPane();
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(60);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(40);
        grid.getColumnConstraints().addAll(left, right);
        grid.add(text, 0, 0);
        grid.add(map, 1, 0);
        grid.setPrefWidth(384); // w-96
        grid.setMinWidth(Region.USE_PREF_SIZE);
        grid.setMaxWidth(Region.USE_PREF_SIZE);

        Label italy = label("Italy", "text-base", "text-blue-600");
        italy.setUnderline(true);
        FxPopover popover = new FxPopover(italy, grid);
        popover.setTrigger(FxPopover.Trigger.HOVER);

        TextFlow paragraph = new TextFlow(
                flowText("Due to its central geographic location in Southern Europe, "),
                popover,
                flowText(" has historically been home to myriad peoples and cultures. In addition to the "
                        + "various ancient peoples dispersed throughout what is now modern-day Italy, the most "
                        + "predominant being the Indo-European Italic peoples who gave the peninsula its name, "
                        + "beginning from the classical era, Phoenicians and Carthaginians founded colonies "
                        + "mostly in insular Italy"));
        paragraph.setMaxWidth(640);

        return section("Image popover",
                "Hover the highlighted word to show detailed information with an image, like Wikipedia and "
                        + "large news outlets do. The pointer can move into the popover without closing it.",
                paragraph);
    }

    private static Text flowText(String value) {
        Text text = new Text(value);
        text.getStyleClass().addAll("text-base", "text-muted");
        return text;
    }

    // ---- 4. password strength
    // ------------------------------------------------------------------

    private Node passwordStrength() {
        FxTextInput email = new FxTextInput();
        email.setPromptText("name@flowbite.com");

        FxTextInput password = new FxTextInput();
        password.setType(FxTextInput.Type.PASSWORD);

        FxPopover passwordPopover = new FxPopover(password, strengthContent(password.textProperty()));
        passwordPopover.setTrigger(FxPopover.Trigger.HOVER);

        VBox form = new VBox(16,
                new VBox(8, new FxLabel("Your email", email), email),
                new VBox(8, new FxLabel("Your password", password), passwordPopover),
                new FxCheckbox("Remember me"),
                new FxButton("Submit"));
        form.setMaxWidth(448);

        return section("Password strength",
                "Show the password strength next to the input while the user hovers it. The bars and "
                        + "checks update as you type.",
                form);
    }

    private static Node strengthContent(StringProperty password) {
        Label heading = label("Must have at least 6 characters", "text-sm", "font-semibold", "text-body");

        HBox bars = new HBox(8);
        Region[] segments = new Region[4];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Region();
            segments[i].setPrefHeight(4);
            segments[i].setMinHeight(4);
            segments[i].setMaxHeight(4);
            segments[i].setMinWidth(0);
            HBox.setHgrow(segments[i], Priority.ALWAYS);
            segments[i].setMaxWidth(Double.MAX_VALUE);
            bars.getChildren().add(segments[i]);
        }

        Label better = label("It's better to have:", "text-sm", "text-muted");
        Label[] marks = { new Label(), new Label(), new Label() };
        HBox[] rows = {
                requirement(marks[0], "Upper & lower case letters"),
                requirement(marks[1], "A symbol (#$&)"),
                requirement(marks[2], "A longer password (min. 12 chars.)")
        };

        Runnable update = () -> {
            String value = password.get() == null ? "" : password.get();
            boolean[] checks = {
                    value.length() >= 6,
                    !value.equals(value.toLowerCase()) && !value.equals(value.toUpperCase()),
                    value.matches(".*[^A-Za-z0-9].*"),
                    value.length() >= 12
            };
            int score = 0;
            for (boolean check : checks) {
                if (check) {
                    score++;
                }
            }
            for (int i = 0; i < segments.length; i++) {
                segments[i].getStyleClass().removeAll("bg-yellow-400", "bg-border-strong", "bg-success");
                segments[i].getStyleClass().add(i < score ? (score == 4 ? "bg-success" : "bg-yellow-400")
                        : "bg-border-strong");
            }
            for (int i = 0; i < marks.length; i++) {
                boolean met = checks[i + 1];
                marks[i].setText(met ? "\u2713" : "\u2715");
                marks[i].getStyleClass().removeAll("text-sm", "text-success", "text-muted");
                marks[i].getStyleClass().addAll("text-sm", met ? "text-success" : "text-muted");
            }
        };
        password.addListener((obs, oldValue, newValue) -> update.run());
        update.run();

        VBox content = new VBox(8, heading, bars, better, new VBox(4, rows));
        content.setPadding(new Insets(12));
        return content;
    }

    private static HBox requirement(Label mark, String text) {
        mark.setMinWidth(16);
        HBox row = new HBox(8, mark, label(text, "text-sm", "text-muted"));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ---- 5. controlled
    // -------------------------------------------------------------------------

    private Node controlled() {
        FxTextInput min = new FxTextInput();
        FxTextInput max = new FxTextInput();

        FxButton reset = new FxButton("Reset");
        reset.setVariant(FxButton.Variant.DEFAULT);
        reset.setColor(FxButton.Color.GRAY);
        reset.setOnAction(event -> {
            min.clear();
            max.clear();
        });
        FxButton save = new FxButton("Save");
        save.setVariant(FxButton.Variant.SUCCESS);

        VBox content = new VBox(16,
                label("Area (sqft)", "text-base", "text-muted"),
                new VBox(8, new FxLabel("Minimum sqft", min), min),
                new VBox(8, new FxLabel("Maximum sqft", max), max),
                new HBox(8, reset, save));
        content.setPadding(new Insets(16));
        content.setPrefWidth(256); // w-64
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMaxWidth(Region.USE_PREF_SIZE);

        FxPopover popover = new FxPopover(new FxButton("Area \u25BE"), content);
        save.setOnAction(event -> popover.setOpen(false));

        FxButton openFromCode = new FxButton("Open from code");
        openFromCode.setVariant(FxButton.Variant.SECONDARY);
        openFromCode.setOnAction(event -> popover.setOpen(true));

        Label status = label("", "text-sm", "text-muted");
        status.textProperty().bind(Bindings.when(popover.openProperty())
                .then("open = true").otherwise("open = false"));

        HBox row = new HBox(8, popover, openFromCode, status);
        row.setAlignment(Pos.CENTER_LEFT);

        return section("Controlled",
                "openProperty() is Flowbite's open / onOpenChange in one property: read it, bind to it or set "
                        + "it. Save closes the popover from code; clicking outside or pressing Esc closes it "
                        + "from the user's side.",
                row);
    }

    // ---- 6. placement
    // --------------------------------------------------------------------------

    private Node placement() {
        HBox row = new HBox(8);
        for (FxPopover.Placement placement : new FxPopover.Placement[] {
                FxPopover.Placement.TOP, FxPopover.Placement.RIGHT,
                FxPopover.Placement.BOTTOM, FxPopover.Placement.LEFT }) {
            String name = placement.name().charAt(0) + placement.name().substring(1).toLowerCase();
            FxPopover popover = new FxPopover(new FxButton("Popover " + name.toLowerCase()), samplePanel());
            popover.setPlacement(placement);
            row.getChildren().add(popover);
        }
        return section("Placement",
                "Use placement to choose the side. The default is bottom. If there is no room on the "
                        + "chosen side the popover flips to the other one.",
                row);
    }

    // ---- 7. trigger type
    // -----------------------------------------------------------------------

    private Node triggerType() {
        FxPopover hover = new FxPopover(new FxButton("Popover hover"), samplePanel());
        hover.setTrigger(FxPopover.Trigger.HOVER);

        FxPopover click = new FxPopover(new FxButton("Popover click"), samplePanel());
        click.setTrigger(FxPopover.Trigger.CLICK);

        return section("Trigger type",
                "Use trigger to open the popover on hover instead of on click. The default is click.",
                new HBox(8, hover, click));
    }

    // ---- 8. disable arrow
    // ----------------------------------------------------------------------

    private Node disableArrow() {
        FxPopover popover = new FxPopover(new FxButton("No Arrow Popover"), samplePanel());
        popover.setArrow(false);
        return section("Disable arrow",
                "Set arrow to false to remove the arrow pointing at the trigger.",
                popover);
    }
}
