package dev.fxkit.showcase;

import dev.fxkit.core.components.avatar.FxAvatar;
import dev.fxkit.core.components.blockquote.FxBlockquote;
import dev.fxkit.core.components.blockquote.FxBlockquoteCaption;
import dev.fxkit.core.components.rating.FxRating;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxBlockquote} and {@link FxBlockquoteCaption}: every example from
 * Flowbite React's Blockquote page (default, solid background, icon, paragraph context, user
 * testimonial, user review, alignment, sizes), edge cases, and a live playground where every
 * property can be flipped at runtime.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check that blockquotes restyle correctly in light and dark; the
 * solid background and the quote glyph are the parts most worth checking in dark.
 */
public class BlockquoteTestApp extends Application {

    private static final String QUOTE = "\"Flowbite is just awesome. It contains tons of predesigned components and "
            + "pages starting from login screen to complex dashboard. Perfect choice for your next SaaS "
            + "application.\"";

    private static final String USABILITY = "Does your user know how to exit out of screens? Can they follow your "
            + "intended user journey and buy something from the site you've designed? By running a usability "
            + "test, you'll be able to see how users will interact with your design once it's live.";

    private static final String FLOWBITE = "First of all you need to understand how Flowbite works. This library "
            + "is not another framework. Rather, it is a set of components based on Tailwind CSS that you can "
            + "just copy-paste from the documentation.";

    private static final String JIRA = "Track work across the enterprise through an open, collaborative platform. "
            + "Link issues across Jira and ingest data from other software development tools, so your IT "
            + "support and operations teams have richer contextual information to rapidly respond to "
            + "requests, incidents, and changes.";

    private static final String ITSM = "Deliver great service experiences fast - without the complexity of "
            + "traditional ITSM solutions. Accelerate critical development work, eliminate toil, and deploy "
            + "changes with ease, with a complete audit trail for every change.";

    private Scene scene;
    private Button themeToggle;

    @Override
    public void start(Stage stage) {
        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: edit the text, switch size, alignment and weight, toggle solid, "
                                + "icon and custom content."),
                playground(),

                sectionHeading("Default blockquote",
                        "text-xl font-semibold italic, theme text color. Wraps inside its parent."),
                labeled("default", new FxBlockquote(QUOTE)),

                sectionHeading("Solid background",
                        "solid = true: 4px left border, 16px padding, alt-surface background. "
                                + "Check it stays visible on the card in BOTH themes."),
                solidSection(),

                sectionHeading("Blockquote icon",
                        "iconVisible = true puts the 32px quote glyph above the text; it follows the alignment."),
                iconSection(),

                sectionHeading("Paragraph context",
                        "A blockquote between paragraphs, in a two-column grid (Flowbite's md:grid-cols-2)."),
                paragraphContext(),

                sectionHeading("User testimonial",
                        "Glyph, centered LG/MEDIUM blockquote and a centered caption with the strong divider."),
                labeled("figure, max 768px", centered(testimonial())),

                sectionHeading("User review",
                        "Rating stars, LG/SEMIBOLD blockquote and a left-aligned caption with the light divider."),
                labeled("figure, max 768px", review()),

                sectionHeading("Alignment", "TextAlign LEFT, CENTER, RIGHT."),
                alignmentSection(),

                sectionHeading("Sizes", "Size SM (16px), MD (18px, default), LG (24px)."),
                sizeSection(),

                sectionHeading("Edge cases",
                        "Empty text, narrow wrapping, custom content, null enums, blank caption parts, "
                                + "runtime changes."),
                edgeCases(),
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

        stage.setTitle("FxBlockquote test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxBlockquote test harness");
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
        FxBlockquote quote = new FxBlockquote();

        TextField textField = new TextField(QUOTE);
        textField.setPromptText("text");
        HBox.setHgrow(textField, Priority.ALWAYS);
        quote.textProperty().bind(textField.textProperty());

        ComboBox<FxBlockquote.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxBlockquote.Size.values());
        sizeBox.valueProperty().bindBidirectional(quote.sizeProperty());

        ComboBox<FxBlockquote.TextAlign> alignBox = new ComboBox<>();
        alignBox.getItems().addAll(FxBlockquote.TextAlign.values());
        alignBox.valueProperty().bindBidirectional(quote.textAlignmentProperty());

        ComboBox<FxBlockquote.Weight> weightBox = new ComboBox<>();
        weightBox.getItems().addAll(FxBlockquote.Weight.values());
        weightBox.valueProperty().bindBidirectional(quote.weightProperty());

        CheckBox solidCheck = new CheckBox("Solid");
        quote.solidProperty().bind(solidCheck.selectedProperty());

        CheckBox iconCheck = new CheckBox("Icon");
        quote.iconVisibleProperty().bind(iconCheck.selectedProperty());

        CheckBox customCheck = new CheckBox("Custom content");
        customCheck.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                Label custom = new Label("Custom node - the text field is ignored while this is set. "
                        + "It inherits size, weight and italic from the blockquote.");
                custom.setWrapText(true);
                custom.getStyleClass().add("text-body");
                quote.setContent(custom);
            } else {
                quote.setContent(null);
            }
        });

        HBox row1 = new HBox(10, new Label("Text"), textField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, new Label("Size"), sizeBox, new Label("Align"), alignBox,
                new Label("Weight"), weightBox);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(16, solidCheck, iconCheck, customCheck);
        row3.setAlignment(Pos.CENTER_LEFT);

        return card(new VBox(12, row1, row2, row3, quote));
    }

    // ---- solid background -------------------------------------------------------------------

    private VBox solidSection() {
        FxBlockquote solid = new FxBlockquote(QUOTE);
        solid.setSolid(true);
        VBox.setMargin(solid, new Insets(16, 0, 16, 0)); // my-4

        VBox box = new VBox(paragraph(USABILITY), solid, paragraph(FLOWBITE));
        return card(box);
    }

    // ---- icon ---------------------------------------------------------------------------------

    private VBox iconSection() {
        FxBlockquote left = new FxBlockquote(QUOTE);
        left.setIconVisible(true);

        FxBlockquote center = new FxBlockquote(QUOTE);
        center.setIconVisible(true);
        center.setTextAlignment(FxBlockquote.TextAlign.CENTER);

        FxBlockquote right = new FxBlockquote(QUOTE);
        right.setIconVisible(true);
        right.setTextAlignment(FxBlockquote.TextAlign.RIGHT);

        FxBlockquote solid = new FxBlockquote(QUOTE);
        solid.setIconVisible(true);
        solid.setSolid(true);

        FxBlockquote iconBefore = new FxBlockquote(QUOTE);
        Button toggle = new Button("Toggle icon");
        toggle.setOnAction(e -> iconBefore.setIconVisible(!iconBefore.isIconVisible()));

        return new VBox(12,
                labeled("icon, left", left),
                labeled("icon, center", center),
                labeled("icon, right", right),
                labeled("icon + solid (glyph sits inside the padding)", solid),
                labeled("runtime icon toggle", new VBox(8, iconBefore, toggle)));
    }

    // ---- paragraph context ------------------------------------------------------------------

    private VBox paragraphContext() {
        // Flowbite's example really has a space inside the quote marks here.
        FxBlockquote quote = new FxBlockquote("\" Flowbite is just awesome. It contains tons of predesigned components and pages "
                + "starting from login screen to complex dashboard. Perfect choice for your next SaaS "
                + "application. \"");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        ColumnConstraints other = new ColumnConstraints();
        other.setPercentWidth(50);
        grid.getColumnConstraints().addAll(half, other);
        grid.add(paragraph(JIRA), 0, 0);
        grid.add(quote, 1, 0);
        GridPane.setValignment(quote, javafx.geometry.VPos.TOP);

        return card(new VBox(12, paragraph(JIRA), grid, paragraph(ITSM))); // mb-3 between blocks
    }

    // ---- user testimonial -------------------------------------------------------------------

    private VBox testimonial() {
        FxBlockquote text = new FxBlockquote(QUOTE);
        text.setSize(FxBlockquote.Size.LG);
        text.setWeight(FxBlockquote.Weight.MEDIUM);
        text.setTextAlignment(FxBlockquote.TextAlign.CENTER);

        FxBlockquoteCaption caption = new FxBlockquoteCaption(avatar("MG"), "Micheal Gough", "CEO at Google");
        caption.setCentered(true);
        caption.setStrongDivider(true);

        Region glyph = FxBlockquote.quoteIcon(FxBlockquote.IconSize.LG);
        VBox.setMargin(glyph, new Insets(0, 0, 12, 0)); // mb-3
        VBox.setMargin(caption, new Insets(24, 0, 0, 0)); // mt-6

        VBox figure = new VBox(glyph, text, caption);
        figure.setAlignment(Pos.TOP_CENTER); // mx-auto text-center
        figure.setMaxWidth(768); // max-w-screen-md
        return figure;
    }

    // ---- user review ------------------------------------------------------------------------

    private VBox review() {
        FxRating stars = FxRating.of(5, 5);
        stars.setSize(FxRating.Size.MD);
        VBox.setMargin(stars, new Insets(0, 0, 16, 0)); // mb-4

        FxBlockquote text = new FxBlockquote(QUOTE);
        text.setSize(FxBlockquote.Size.LG);

        FxBlockquoteCaption caption = new FxBlockquoteCaption(avatar("BG"), "Bonnie Green", "CTO at Flowbite");
        VBox.setMargin(caption, new Insets(24, 0, 0, 0)); // mt-6

        VBox figure = new VBox(stars, text, caption);
        figure.setMaxWidth(768); // max-w-screen-md
        return figure;
    }

    // ---- alignment --------------------------------------------------------------------------

    private VBox alignmentSection() {
        VBox box = new VBox(12);
        for (FxBlockquote.TextAlign align : FxBlockquote.TextAlign.values()) {
            FxBlockquote quote = new FxBlockquote(QUOTE);
            quote.setTextAlignment(align);
            box.getChildren().add(labeled(align.name(), quote));
        }
        FxBlockquote shortText = new FxBlockquote("\"Short, single line.\"");
        shortText.setTextAlignment(FxBlockquote.TextAlign.RIGHT);
        box.getChildren().add(labeled("RIGHT, short text (must hug the right edge, not wrap)", shortText));
        return box;
    }

    // ---- sizes ------------------------------------------------------------------------------

    private VBox sizeSection() {
        VBox box = new VBox(12);
        for (FxBlockquote.Size size : FxBlockquote.Size.values()) {
            FxBlockquote quote = new FxBlockquote(QUOTE);
            quote.setSize(size);
            box.getChildren().add(labeled(size.name(), quote));
        }
        return box;
    }

    // ---- edge cases -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxBlockquote empty = new FxBlockquote();
        empty.setSolid(true);
        empty.setPrefWidth(300);
        flow.getChildren().add(labeled("empty solid blockquote (a thin box, no exception)", empty));

        FxBlockquote narrow = new FxBlockquote(QUOTE);
        narrow.setPrefWidth(260);
        narrow.setSolid(true);
        narrow.setIconVisible(true);
        flow.getChildren().add(labeled("narrow: wraps, never ellipsizes", narrow));

        FxBlockquote nulls = new FxBlockquote("\"null size / alignment / weight fall back to the defaults.\"");
        nulls.setPrefWidth(300);
        Button clear = new Button("Set all three to null");
        clear.setOnAction(e -> {
            nulls.setSize(null);
            nulls.setTextAlignment(null);
            nulls.setWeight(null);
        });
        Button restore = new Button("Restore");
        restore.setOnAction(e -> {
            nulls.setSize(FxBlockquote.Size.MD);
            nulls.setTextAlignment(FxBlockquote.TextAlign.LEFT);
            nulls.setWeight(FxBlockquote.Weight.SEMIBOLD);
        });
        flow.getChildren().add(labeled("null enums", new VBox(8, nulls, new HBox(8, clear, restore))));

        FxBlockquote rapid = new FxBlockquote("\"Only one size / align / weight class at a time.\"");
        rapid.setPrefWidth(300);
        int[] n = {0};
        Button cycle = new Button("Cycle size + align + weight + solid");
        cycle.setOnAction(e -> {
            n[0]++;
            FxBlockquote.Size[] sizes = FxBlockquote.Size.values();
            FxBlockquote.TextAlign[] aligns = FxBlockquote.TextAlign.values();
            FxBlockquote.Weight[] weights = FxBlockquote.Weight.values();
            rapid.setSize(sizes[n[0] % sizes.length]);
            rapid.setTextAlignment(aligns[n[0] % aligns.length]);
            rapid.setWeight(weights[n[0] % weights.length]);
            rapid.setSolid(n[0] % 2 == 0);
        });
        flow.getChildren().add(labeled("style class swap", new VBox(8, rapid, cycle)));

        FxBlockquote custom = new FxBlockquote(QUOTE);
        custom.setPrefWidth(320);
        Button set = new Button("Set custom content");
        set.setOnAction(e -> {
            Label heading = new Label("Custom body");
            heading.getStyleClass().addAll("text-body", "font-bold");
            Label body = new Label("A VBox replacing the text: inherits the italic font, color is up to you.");
            body.setWrapText(true);
            body.getStyleClass().add("text-muted");
            custom.setContent(new VBox(4, heading, body));
        });
        Button clearContent = new Button("Clear (back to text)");
        clearContent.setOnAction(e -> custom.setContent(null));
        Button retext = new Button("Change text while custom is set");
        retext.setOnAction(e -> custom.setText("\"Text #" + System.nanoTime() % 1000 + "\""));
        flow.getChildren().add(labeled("custom content overrides text; null restores it",
                new VBox(8, custom, new HBox(8, set, clearContent), retext)));

        FxBlockquoteCaption nameOnly = new FxBlockquoteCaption("Only a name", null);
        FxBlockquoteCaption roleOnly = new FxBlockquoteCaption(null, "Only a role");
        FxBlockquoteCaption neither = new FxBlockquoteCaption(avatar("??"), "", "   ");
        FxBlockquoteCaption noAvatar = new FxBlockquoteCaption("No avatar", "Divider still shows");
        FxBlockquoteCaption both = new FxBlockquoteCaption(avatar("AB"), "Name", "Role");
        flow.getChildren().add(labeled("caption parts (divider only when name AND role)",
                new VBox(10, nameOnly, roleOnly, neither, noAvatar, both)));

        FxBlockquoteCaption live = new FxBlockquoteCaption(avatar("LV"), "Live caption", "Edit me");
        TextField nameField = new TextField("Live caption");
        TextField roleField = new TextField("Edit me");
        live.nameProperty().bind(nameField.textProperty());
        live.roleProperty().bind(roleField.textProperty());
        CheckBox centerCheck = new CheckBox("centered");
        live.centeredProperty().bind(centerCheck.selectedProperty());
        CheckBox strongCheck = new CheckBox("strongDivider");
        live.strongDividerProperty().bind(strongCheck.selectedProperty());
        CheckBox avatarCheck = new CheckBox("avatar");
        avatarCheck.setSelected(true);
        avatarCheck.selectedProperty().addListener((o, was, is) -> live.setAvatar(is ? avatar("LV") : null));
        VBox liveBox = new VBox(8, live, nameField, roleField, new HBox(12, centerCheck, strongCheck, avatarCheck));
        liveBox.setPrefWidth(320);
        flow.getChildren().add(labeled("caption playground", liveBox));

        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------

    /** XS rounded avatar with initials, so the harness needs no image files. */
    private static FxAvatar avatar(String initials) {
        FxAvatar avatar = new FxAvatar();
        avatar.setSize(FxAvatar.Size.XS);
        avatar.setRounded(true);
        avatar.setPlaceholderInitials(initials);
        return avatar;
    }

    /** Muted wrapping body paragraph (Flowbite's text-gray-500 dark:text-gray-400). */
    private static Label paragraph(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().addAll("text-base", "text-muted");
        return label;
    }

    /** Centers a width-limited figure in its row (Flowbite's mx-auto). */
    private static HBox centered(Node figure) {
        HBox row = new HBox(figure);
        row.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(figure, Priority.ALWAYS);
        return row;
    }

    private static VBox card(Node content) {
        VBox box = new VBox(content);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
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
