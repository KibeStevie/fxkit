package dev.fxkit.showcase;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.card.FxCard;
import dev.fxkit.core.components.form.FxCheckbox;
import dev.fxkit.core.components.form.FxLabel;
import dev.fxkit.core.components.form.FxTextInput;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxCard}: every Flowbite card example
 * (default/href, CTA button,
 * image, custom image render function, horizontal, user profile, form,
 * e-commerce, app-store CTA,
 * list, pricing, crypto wallet), a live playground, and edge cases (long text,
 * refused or missing
 * picture, link card with a button inside, disabled link card, runtime
 * changes).
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating. Use the
 * header's theme toggle to check that cards restyle correctly in light and
 * dark.
 *
 * <p>
 * Pictures are generated in memory (gradient PNGs passed as {@code data:} URIs,
 * which
 * {@code FxImagePolicy} allows by default), so the harness needs no image files
 * and no network. The
 * white frame and the ring in each picture make cropping and scaling easy to
 * see.
 *
 * <p>
 * Three pieces are stand-ins built from plain JavaFX because they belong to
 * other components:
 * the user card's avatar ({@code FxAvatar}), its menu ({@code FxDropdown}) and
 * the product card's
 * stars ({@code FxRating}). The wallet logos are plain colored dots.
 */
public class CardTestApp extends Application {

    private static final String TITLE = "Noteworthy technology acquisitions 2021";
    private static final String BODY = "Here are the biggest enterprise technology acquisitions of 2021 "
            + "so far, in reverse chronological order.";

    // ---- SVG paths from Flowbite's examples, normalized to explicit separators
    // for JavaFX ----------
    private static final String ARROW_RIGHT = "M 10.293 3.293 a 1 1 0 0 1 1.414 0 l 6 6 a 1 1 0 0 1 0 1.414 l -6 6 a 1 1 0 0 1 -1.414 -1.414 L 14.586 11 H 3 a 1 1 0 1 1 0 -2 h 11.586 l -4.293 -4.293 a 1 1 0 0 1 0 -1.414 Z"; // viewBox
                                                                                                                                                                                                                                // 20x20
    private static final String CHECK_CIRCLE = "M 10 18 a 8 8 0 1 0 0 -16 a 8 8 0 0 0 0 16 Z m 3.707 -9.293 a 1 1 0 0 0 -1.414 -1.414 L 9 10.586 L 7.707 9.293 a 1 1 0 0 0 -1.414 1.414 l 2 2 a 1 1 0 0 0 1.414 0 l 4 -4 Z"; // viewBox
                                                                                                                                                                                                                             // 20x20
    private static final String STAR = "M 9.049 2.927 c 0.3 -0.921 1.603 -0.921 1.902 0 l 1.07 3.292 a 1 1 0 0 0 0.95 0.69 h 3.462 c 0.969 0 1.371 1.24 0.588 1.81 l -2.8 2.034 a 1 1 0 0 0 -0.364 1.118 l 1.07 3.292 c 0.3 0.921 -0.755 1.688 -1.54 1.118 l -2.8 -2.034 a 1 1 0 0 0 -1.175 0 l -2.8 2.034 c -0.784 0.57 -1.838 -0.197 -1.539 -1.118 l 1.07 -3.292 a 1 1 0 0 0 -0.364 -1.118 L 2.98 8.72 c -0.783 -0.57 -0.38 -1.81 0.588 -1.81 h 3.461 a 1 1 0 0 0 0.951 -0.69 l 1.07 -3.292 Z"; // viewBox
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  // 20x20
    private static final String APPLE = "M 318.7 268.7 c -0.2 -36.7 16.4 -64.4 50 -84.8 c -18.8 -26.9 -47.2 -41.7 -84.7 -44.6 c -35.5 -2.8 -74.3 20.7 -88.5 20.7 c -15 0 -49.4 -19.7 -76.4 -19.7 C 63.3 141.2 4 184.8 4 273.5 q 0 39.3 14.4 81.2 c 12.8 36.7 59 126.7 107.2 125.2 c 25.2 -0.6 43 -17.9 75.8 -17.9 c 31.8 0 48.3 17.9 76.4 17.9 c 48.6 -0.7 90.4 -82.5 102.6 -119.3 c -65.2 -30.7 -61.7 -90 -61.7 -91.9 Z m -56.6 -164.2 c 27.3 -32.4 24.8 -61.9 24 -72.5 c -24.1 1.4 -52 16.4 -67.9 34.9 c -17.5 19.8 -27.8 44.3 -25.6 71.9 c 26.1 2 49.9 -11.4 69.5 -34.3 Z"; // viewBox
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               // 384x512
    private static final String GOOGLE_PLAY = "M 325.3 234.3 L 104.6 13 l 280.8 161.2 l -60.1 60.1 Z M 47 0 C 34 6.8 25.3 19.2 25.3 35.3 v 441.3 c 0 16.1 8.7 28.5 21.7 35.3 l 256.6 -256 L 47 0 Z m 425.2 225.6 l -58.9 -34.1 l -65.7 64.5 l 65.7 64.5 l 60.1 -34.1 c 18 -14.3 18 -46.5 -1.2 -60.8 Z M 104.6 499 l 280.8 -161.2 l -60.1 -60.1 L 104.6 499 Z"; // viewBox
                                                                                                                                                                                                                                                                                                                                                               // 512x512
    private static final String QUESTION_CIRCLE = "M 256 8 C 119.043 8 8 119.083 8 256 c 0 136.997 111.043 248 248 248 s 248 -111.003 248 -248 C 504 119.083 392.957 8 256 8 Z m 0 448 c -110.532 0 -200 -89.431 -200 -200 c 0 -110.495 89.472 -200 200 -200 c 110.491 0 200 89.471 200 200 c 0 110.53 -89.431 200 -200 200 Z m 107.244 -255.2 c 0 67.052 -72.421 68.084 -72.421 92.863 V 300 c 0 6.627 -5.373 12 -12 12 h -45.647 c -6.627 0 -12 -5.373 -12 -12 v -8.659 c 0 -35.745 27.1 -50.034 47.579 -61.516 c 17.561 -9.845 28.324 -16.541 28.324 -29.579 c 0 -17.246 -21.999 -28.693 -39.784 -28.693 c -23.189 0 -33.894 10.977 -48.942 29.969 c -4.057 5.12 -11.46 6.071 -16.666 2.124 l -27.824 -21.098 c -5.107 -3.872 -6.251 -11.066 -2.644 -16.363 C 184.846 131.491 214.94 112 261.794 112 c 49.071 0 101.45 38.304 101.45 88.8 Z M 298 368 c 0 23.159 -18.841 42 -42 42 s -42 -18.841 -42 -42 s 18.841 -42 42 -42 s 42 18.841 42 42 Z"; // viewBox
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      // 512x512

    // ---- generated pictures
    // ------------------------------------------------------------------------
    private static final String BLOG_1 = picture(480, 300, 0x2563EB, 0x9333EA, true);
    private static final String BLOG_2 = picture(480, 300, 0x0891B2, 0x16A34A, true);
    private static final String BLOG_4 = picture(400, 400, 0xDB2777, 0xEAB308, true);
    private static final String WIDE = picture(480, 200, 0x4F46E5, 0x0D9488, true);
    private static final String TALL = picture(200, 480, 0xDC2626, 0x7E22CE, true);
    private static final String PRODUCT = picture(400, 400, 0x374151, 0x06B6D4, true);

    /** What the playground's picture selector offers. */
    private static final Map<String, String> PICTURES = new LinkedHashMap<>();
    static {
        PICTURES.put("(none)", null);
        PICTURES.put("Blog 1 (wide)", BLOG_1);
        PICTURES.put("Blog 4 (square)", BLOG_4);
        PICTURES.put("Tall", TALL);
        PICTURES.put("Refused: file://server/share/a.png", "file://server/share/a.png");
        PICTURES.put("Missing classpath resource", "/images/does-not-exist.png");
    }

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (card activations, buttons)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: horizontal, href, picture, alt, className, renderImage, disable. "
                                + "Tab to the card when href is set: Enter or Space activates it."),
                playground(),

                sectionHeading("Default card (href)",
                        "The whole card is a link: hover color, hand cursor, Tab focus ring, click/Enter/Space "
                                + "fire onAction. It does not open the URL by itself."),
                row(defaultCard()),

                sectionHeading("Card with CTA button",
                        "A button inside the card (full width, arrow icon). No href, so the card is not a link."),
                row(ctaButtonCard()),

                sectionHeading("Card with image",
                        "imgSrc + imgAlt. Picture is as wide as the card, height from its aspect ratio, "
                                + "top corners rounded flush with the border."),
                row(imageCard()),

                sectionHeading("Custom image render function",
                        "renderImage returns your own node (here an ImageView); it is fitted and clipped like the "
                                + "built-in picture."),
                row(renderImageCard()),

                sectionHeading("Horizontal card",
                        "192px picture column, cropped to fill (object-cover), left corners rounded; max width 576. "
                                + "Wide and tall sources must both fill the column without distortion."),
                row(horizontalCard(BLOG_4), horizontalCard(WIDE), horizontalCard(TALL)),

                sectionHeading("User profile card",
                        "Avatar, name, role, two buttons, and a menu (stand-ins, see class doc)."),
                row(profileCard()),

                sectionHeading("Card with form", "FxLabel + FxTextInput + FxCheckbox + FxButton inside a card."),
                row(formCard()),

                sectionHeading("E-commerce card", "Picture, title, rating stars, price and a button."),
                row(productCard()),

                sectionHeading("CTA card", "Two dark FxButtons with an icon and a two-line caption."),
                row(storeCard()),

                sectionHeading("Card with list", "Rows with avatar, text, and a price, separated by 1px dividers."),
                row(listCard()),

                sectionHeading("Pricing card", "Baseline-aligned price, check/strike list, full-width button."),
                row(pricingCard()),

                sectionHeading("Crypto card (web3)", "Rows with a logo, a name and a badge; hover shades the row."),
                row(walletCard()),

                sectionHeading("Edge cases",
                        "Long wrapping text with a button inside a link card, no content, refused/missing picture, "
                                + "disabled link card, runtime changes."),
                edgeCases(),

                sectionHeading("Event log", "Everything the cards and buttons report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1100, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxCard test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // ---------------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxCard test harness");
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
    // --------------------------------------------------------------------------------

    private VBox playground() {
        FxCard card = new FxCard();
        Label title = text(TITLE, "text-xl", "font-bold", "text-body");
        Label paragraph = text(BODY, "text-base", "text-muted");
        card.getContent().addAll(title, paragraph);
        card.setPrefWidth(480);
        card.setOnAction(e -> log("playground: onAction (href=" + card.getHref() + ")"));

        TextField titleField = new TextField(TITLE);
        TextField bodyField = new TextField(BODY);
        HBox.setHgrow(titleField, Priority.SOMETIMES);
        HBox.setHgrow(bodyField, Priority.ALWAYS);
        title.textProperty().bind(titleField.textProperty());
        paragraph.textProperty().bind(bodyField.textProperty());

        CheckBox horizontal = new CheckBox("Horizontal");
        card.horizontalProperty().bind(horizontal.selectedProperty());

        TextField href = new TextField();
        href.setPromptText("href (blank = plain card)");
        card.hrefProperty().bind(href.textProperty());

        ComboBox<String> picture = new ComboBox<>();
        picture.getItems().addAll(PICTURES.keySet());
        picture.getSelectionModel().selectFirst();
        picture.valueProperty().addListener((o, was, is) -> card.setImgSrc(PICTURES.get(is)));

        TextField alt = new TextField();
        alt.setPromptText("imgAlt");
        card.imgAltProperty().bind(alt.textProperty());

        TextField className = new TextField();
        className.setPromptText("className, e.g. bg-yellow-100 border-yellow-300");
        HBox.setHgrow(className, Priority.ALWAYS);
        Button apply = new Button("Apply className");
        apply.setOnAction(e -> card.setClassName(className.getText()));

        CheckBox render = new CheckBox("renderImage (custom ImageView, overrides picture)");
        render.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                card.setRenderImage(c -> customImage(c.isHorizontal() ? BLOG_4 : BLOG_2, "Custom node"));
            } else {
                card.setRenderImage(null);
            }
        });

        CheckBox disable = new CheckBox("Disable");
        card.disableProperty().bind(disable.selectedProperty());

        HBox row1 = new HBox(10, new Label("Title"), titleField, new Label("Text"), bodyField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, horizontal, new Label("href"), href, new Label("Picture"), picture, alt);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(10, className, apply, render, disable);
        row3.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, row3, card);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- Flowbite examples
    // -------------------------------------------------------------------------

    private FxCard defaultCard() {
        FxCard card = new FxCard(text(TITLE, "text-2xl", "font-bold", "text-body"),
                text(BODY, "text-base", "text-muted"));
        card.setHref("#");
        card.setOnAction(e -> log("default card: onAction (href=" + card.getHref() + ")"));
        return width(card, 384);
    }

    private FxCard ctaButtonCard() {
        FxButton read = new FxButton("Read more");
        read.setContentDisplay(ContentDisplay.RIGHT);
        read.setGraphicTextGap(8);
        read.setGraphic(icon(ARROW_RIGHT, 16, 16, read.textFillProperty()));
        read.setMaxWidth(Double.MAX_VALUE);
        read.setOnAction(e -> log("CTA card: Read more"));

        return width(new FxCard(text(TITLE, "text-2xl", "font-bold", "text-body"),
                text(BODY, "text-base", "text-muted"), read), 384);
    }

    private FxCard imageCard() {
        FxCard card = new FxCard(text(TITLE, "text-2xl", "font-bold", "text-body"),
                text(BODY, "text-base", "text-muted"));
        card.setImgSrc(BLOG_1);
        card.setImgAlt("Meaningful alt text for an image that is not purely decorative");
        return width(card, 384);
    }

    private FxCard renderImageCard() {
        FxCard card = new FxCard(text(TITLE, "text-2xl", "font-bold", "text-body"),
                text(BODY, "text-base", "text-muted"));
        card.setRenderImage(c -> customImage(BLOG_2, "image 2"));
        return width(card, 384);
    }

    private FxCard horizontalCard(String picture) {
        FxCard card = new FxCard(text(TITLE, "text-2xl", "font-bold", "text-body"),
                text(BODY, "text-base", "text-muted"));
        card.setHorizontal(true);
        card.setImgSrc(picture);
        card.setPrefWidth(FxCard.HORIZONTAL_MAX_WIDTH);
        return card;
    }

    private FxCard profileCard() {
        // Stand-in for FxDropdown (inline). Its popup is a plain JavaFX menu, so it
        // stays light in dark theme.
        MenuButton menu = new MenuButton();
        menu.setStyle("-fx-background-color: transparent; -fx-mark-color: -fxk-text-muted;");
        for (String name : new String[] { "Edit", "Export Data", "Delete" }) {
            MenuItem item = new MenuItem(name);
            item.setOnAction(e -> log("profile menu: " + name));
            menu.getItems().add(item);
        }
        HBox top = new HBox(menu);
        top.setAlignment(Pos.CENTER_RIGHT);
        top.setPadding(new Insets(16, 16, 0, 16));

        ImageView avatar = avatar(picture(96, 96, 0xDB2777, 0x7E22CE, false), 96);
        avatar.getStyleClass().add("shadow-lg");

        FxButton add = new FxButton("Add friend");
        add.setVariant(FxButton.Variant.DEFAULT);
        add.setColor(FxButton.Color.CYAN);
        add.setOnAction(e -> log("profile: Add friend"));
        FxButton message = new FxButton("Message");
        message.setVariant(FxButton.Variant.SECONDARY);
        message.setOnAction(e -> log("profile: Message"));
        HBox buttons = new HBox(12, add, message);
        buttons.setAlignment(Pos.CENTER);

        VBox person = new VBox(4, avatar,
                text("Bonnie Green", "text-xl", "font-medium", "text-body"),
                text("Visual Designer", "text-sm", "text-muted"),
                buttons);
        person.setAlignment(Pos.TOP_CENTER);
        person.setPadding(new Insets(0, 0, 40, 0));
        VBox.setMargin(avatar, new Insets(0, 0, 8, 0));
        VBox.setMargin(buttons, new Insets(20, 0, 0, 0));

        return width(new FxCard(top, person), 384);
    }

    private FxCard formCard() {
        FxTextInput email = new FxTextInput();
        email.setPromptText("name@flowbite.com");
        FxTextInput password = new FxTextInput();
        password.setType(FxTextInput.Type.PASSWORD);
        FxCheckbox remember = new FxCheckbox("Remember me");
        FxButton submit = new FxButton("Submit");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setOnAction(e -> log("form: submit (email=\"" + email.getText() + "\", remember="
                + remember.isSelected() + ")"));

        VBox form = new VBox(16, field("Your email", email), field("Your password", password), remember, submit);
        return width(new FxCard(form), 384);
    }

    private FxCard productCard() {
        Label title = text("Apple Watch Series 7 GPS, Aluminium Case, Starlight Sport",
                "text-xl", "font-semibold", "text-body");

        HBox stars = new HBox();
        stars.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            stars.getChildren().add(icon(STAR, 20, 20, "bg-yellow-300"));
        }
        Label score = text("5.0", "text-xs", "font-semibold", "bg-cyan-100", "text-cyan-800", "rounded");
        score.setPadding(new Insets(2, 10, 2, 10));
        score.setWrapText(false);
        HBox.setMargin(score, new Insets(0, 8, 0, 12));
        stars.getChildren().add(score);
        VBox.setMargin(stars, new Insets(10, 0, 20, 0));

        Label price = text("$599", "text-3xl", "font-bold", "text-body");
        FxButton add = new FxButton("Add to cart");
        add.setOnAction(e -> log("product: Add to cart"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(price, spacer, add);
        bottom.setAlignment(Pos.CENTER_LEFT);

        FxCard card = new FxCard(title, stars, bottom);
        card.setImgSrc(PRODUCT);
        card.setImgAlt("Apple Watch Series 7 in colors pink, silver, and black");
        return width(card, 384);
    }

    private FxCard storeCard() {
        HBox stores = new HBox(16,
                storeButton(APPLE, 21, 28, "Download on the", "Mac App Store"),
                storeButton(GOOGLE_PLAY, 28, 28, "Get in on", "Google Play"));
        stores.setAlignment(Pos.CENTER);

        Label heading = text("Work fast from anywhere", "text-3xl", "font-bold", "text-body");
        Label paragraph = text("Stay up to date and move work forward with Flowbite on iOS & Android. Download "
                + "the app today.", "text-base", "text-muted");
        // Flowbite's mb-2 / mb-5 come on top of the card's gap-4
        VBox.setMargin(heading, new Insets(0, 0, 8, 0));
        VBox.setMargin(paragraph, new Insets(0, 0, 20, 0));
        return width(new FxCard(heading, paragraph, stores), 384);
    }

    private FxButton storeButton(String path, double iconW, double iconH, String small, String big) {
        FxButton button = new FxButton();
        button.setVariant(FxButton.Variant.DEFAULT);
        button.setColor(FxButton.Color.DARK);
        button.setSize(FxButton.Size.LG);

        Label top = new Label(small);
        top.getStyleClass().add("text-xs");
        Label bottom = new Label(big);
        bottom.getStyleClass().addAll("text-base", "font-semibold");
        // Labels do not inherit the button's text color: follow it, so hover and both
        // themes just work.
        top.textFillProperty().bind(button.textFillProperty());
        bottom.textFillProperty().bind(button.textFillProperty());
        VBox caption = new VBox(top, bottom);

        HBox graphic = new HBox(12, icon(path, iconW, iconH, button.textFillProperty()), caption);
        graphic.setAlignment(Pos.CENTER_LEFT);
        button.setGraphic(graphic);
        button.setOnAction(e -> log("store: " + big));
        return button;
    }

    private FxCard listCard() {
        Label heading = text("Latest Customers", "text-xl", "font-bold", "text-body");
        Hyperlink viewAll = new Hyperlink("View all");
        viewAll.getStyleClass().addAll("text-sm", "font-medium", "text-primary");
        viewAll.setPadding(Insets.EMPTY);
        viewAll.setOnAction(e -> log("list: View all"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(heading, spacer, viewAll);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(0, 0, 16, 0)); // mb-4, on top of the card's gap-4

        String[][] customers = {
                { "Neil Sims", "$320", "0x2563EB", "0x06B6D4" },
                { "Bonnie Green", "$3467", "0xDB2777", "0xEAB308" },
                { "Michael Gough", "$67", "0x16A34A", "0x0D9488" },
                { "Lana Byrd", "$367", "0x9333EA", "0x4F46E5" },
                { "Thomes Lean", "$2367", "0xDC2626", "0xEA580C" } };
        VBox list = new VBox();
        for (int i = 0; i < customers.length; i++) {
            if (i > 0) {
                list.getChildren().add(divider());
            }
            list.getChildren().add(customerRow(customers[i], i == customers.length - 1));
        }
        return width(new FxCard(header, list), 384);
    }

    private static HBox customerRow(String[] customer, boolean last) {
        ImageView avatar = avatar(picture(64, 64, Integer.decode(customer[2]), Integer.decode(customer[3]), false), 32);
        Label name = text(customer[0], "text-sm", "font-medium", "text-body");
        Label email = text("email@windster.com", "text-sm", "text-muted");
        name.setWrapText(false);
        email.setWrapText(false);
        name.setMinWidth(0);
        email.setMinWidth(0);
        VBox who = new VBox(name, email);
        HBox.setHgrow(who, Priority.ALWAYS);
        who.setMinWidth(0);
        Label amount = text(customer[1], "text-base", "font-semibold", "text-body");
        amount.setWrapText(false);

        HBox row = new HBox(16, avatar, who, amount);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 0, last ? 0 : 16, 0));
        return row;
    }

    private FxCard pricingCard() {
        Label dollar = text("$", "text-3xl", "font-semibold", "text-body");
        Label amount = text("49", "font-bold", "text-body");
        amount.setStyle("-fx-font-size: 48px;"); // Flowbite text-5xl: bigger than the utility scale's largest
        Label month = text("/month", "text-xl", "font-normal", "text-muted");
        HBox.setMargin(month, new Insets(0, 0, 0, 4));
        HBox price = new HBox(dollar, amount, month);
        price.setAlignment(Pos.BASELINE_LEFT);

        VBox features = new VBox(20,
                feature("2 team members", true),
                feature("20GB Cloud storage", true),
                feature("Integration help", true),
                feature("Sketch Files", false),
                feature("API Access", false),
                feature("Complete documentation", false),
                feature("24x7 phone & email support", false));
        VBox.setMargin(features, new Insets(28, 0, 28, 0)); // my-7, on top of the card's gap-4

        FxButton choose = new FxButton("Choose plan");
        choose.setMaxWidth(Double.MAX_VALUE);
        choose.setOnAction(e -> log("pricing: Choose plan"));

        Label plan = text("Standard plan", "text-xl", "font-medium", "text-muted");
        VBox.setMargin(plan, new Insets(0, 0, 16, 0)); // mb-4
        return width(new FxCard(plan, price, features, choose), 384);
    }

    private static HBox feature(String label, boolean included) {
        Region check = icon(CHECK_CIRCLE, 20, 20, included ? "bg-primary" : "bg-gray-400");
        Node caption;
        if (included) {
            caption = text(label, "text-base", "text-muted");
        } else {
            Text struck = new Text(label); // Label cannot strike text through
            struck.setStrikethrough(true);
            struck.getStyleClass().addAll("text-base", "text-muted");
            caption = struck;
        }
        HBox row = new HBox(12, check, caption);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private FxCard walletCard() {
        VBox wallets = new VBox(12,
                walletRow("MetaMask", "#E17726", true),
                walletRow("Coinbase Wallet", "#3259A5", false),
                walletRow("Opera Wallet", "#FF1B2D", false),
                walletRow("WalletConnect", "#3B99FC", false),
                walletRow("Fortmatic", "#617BFF", false));
        VBox.setMargin(wallets, new Insets(16, 0, 16, 0)); // my-4, on top of the card's gap-4

        Region help = icon(QUESTION_CIRCLE, 12, 12, "bg-text-muted");
        Hyperlink why = new Hyperlink("Why do I need to connect with my wallet?", help);
        why.getStyleClass().addAll("text-xs", "text-muted");
        why.setPadding(Insets.EMPTY);
        why.setGraphicTextGap(8);
        why.setOnAction(e -> log("wallet: why connect"));

        Label heading = text("Connect wallet", "text-lg", "font-semibold", "text-body");
        VBox.setMargin(heading, new Insets(0, 0, 12, 0)); // mb-3
        return width(new FxCard(heading,
                text("Connect with one of our available wallet providers or create a new one.",
                        "text-sm", "text-muted"),
                wallets, why), 384);
    }

    private HBox walletRow(String name, String brand, boolean popular) {
        HBox row = new HBox(12, new Circle(8, Color.web(brand)), text(name, "text-base", "font-bold", "text-body"));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.getStyleClass().addAll("bg-surface-alt", "rounded-lg");
        // hover:bg-gray-100 dark:hover:bg-gray-500: one step stronger than the resting
        // color, in both themes
        row.hoverProperty().addListener((o, was, is) -> {
            row.getStyleClass().removeAll("bg-surface-alt", "bg-border-strong");
            row.getStyleClass().add(is ? "bg-border-strong" : "bg-surface-alt");
        });
        row.setOnMouseClicked(e -> log("wallet: " + name));
        if (popular) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label badge = text("Popular", "text-xs", "font-medium", "bg-border", "text-muted", "rounded");
            badge.setPadding(new Insets(2, 8, 2, 8));
            badge.setWrapText(false);
            row.getChildren().addAll(spacer, badge);
        }
        return row;
    }

    // ---- edge cases
    // --------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(24, 24);

        // A link card with a button inside: a click on the button must not also
        // activate the card.
        FxButton inner = new FxButton("Inner action");
        inner.setOnAction(e -> log("long-text card: inner button (the card must NOT also fire)"));
        FxCard longText = new FxCard(text("Long text, link card, button inside", "text-xl", "font-bold", "text-body"),
                text("This is a deliberately long paragraph that should wrap inside the card instead of "
                        + "overflowing, and the card should grow taller to fit it. Click the text: the card fires. "
                        + "Click the button: only the button does.", "text-base", "text-muted"),
                inner);
        longText.setHref("#");
        longText.setOnAction(e -> log("long-text card: onAction"));
        flow.getChildren().add(captioned("long text wraps, button does not activate the card", width(longText, 384)));

        FxCard empty = new FxCard();
        empty.setPrefWidth(384);
        flow.getChildren().add(captioned("empty card (no content, no picture)", empty));

        FxCard refused = new FxCard(text("Refused picture", "text-xl", "font-bold", "text-body"),
                text("imgSrc is file://server/share/a.png: FxImagePolicy refuses it, so this is a card without "
                        + "a picture (see the console for the warning).", "text-base", "text-muted"));
        refused.setImgSrc("file://server/share/a.png");
        flow.getChildren().add(captioned("picture refused by FxImagePolicy", width(refused, 384)));

        FxCard missing = new FxCard(text("Missing picture", "text-xl", "font-bold", "text-body"),
                text("The classpath resource does not exist: no picture, no exception.", "text-base", "text-muted"));
        missing.setImgSrc("/images/does-not-exist.png");
        flow.getChildren().add(captioned("missing classpath resource", width(missing, 384)));

        FxCard disabled = new FxCard(text("Disabled link card", "text-xl", "font-bold", "text-body"),
                text("href is set but the card is disabled: no hover, no focus, nothing in the log.",
                        "text-base", "text-muted"));
        disabled.setHref("#");
        disabled.setOnAction(e -> log("DISABLED CARD FIRED - this is a bug"));
        disabled.setDisable(true);
        disabled.setOpacity(0.5);
        Button fireDisabled = new Button("fire() on the disabled card (nothing should be logged)");
        fireDisabled.setOnAction(e -> {
            disabled.fire();
            log("fire() on disabled card returned");
        });
        flow.getChildren().add(captioned("disabled link card", new VBox(6, width(disabled, 384), fireDisabled)));

        flow.getChildren().add(captioned("runtime changes", runtimeCard()));
        return flow;
    }

    private VBox runtimeCard() {
        FxCard card = new FxCard(text("Runtime changes", "text-xl", "font-bold", "text-body"),
                text("Flip every property with the buttons: layout, link, picture, className.",
                        "text-base", "text-muted"));
        card.setImgSrc(BLOG_1);
        card.setPrefWidth(480);
        card.setOnAction(e -> log("runtime card: onAction"));

        Button horizontal = new Button("Toggle horizontal");
        horizontal.setOnAction(e -> card.setHorizontal(!card.isHorizontal()));
        Button href = new Button("Toggle href");
        href.setOnAction(e -> card.setHref(card.getHref() == null ? "#" : null));
        Button swap = new Button("Swap picture");
        swap.setOnAction(e -> card.setImgSrc(BLOG_1.equals(card.getImgSrc()) ? BLOG_4 : BLOG_1));
        Button clear = new Button("Remove picture");
        clear.setOnAction(e -> card.setImgSrc(null));
        Button style = new Button("className on/off");
        style.setOnAction(e -> card.setClassName(card.getClassName().isEmpty()
                ? "bg-yellow-100 border-yellow-300 hover:bg-yellow-200"
                : ""));
        Button add = new Button("Add paragraph");
        add.setOnAction(
                e -> card.getContent().add(text("Another paragraph added at runtime.", "text-base", "text-muted")));

        FlowPane buttons = new FlowPane(8, 8, horizontal, href, swap, clear, style, add);
        buttons.setPrefWrapLength(480);
        return new VBox(8, card, buttons);
    }

    // ---- helpers
    // -----------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    /** Flowbite's {@code className="max-w-sm"}: a card exactly this wide. */
    private static FxCard width(FxCard card, double width) {
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        return card;
    }

    private static Label text(String text, String... styleClasses) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private static VBox field(String caption, Control input) {
        return new VBox(8, new FxLabel(caption, input), input);
    }

    private static Region divider() {
        Region line = new Region();
        line.setMinHeight(1);
        line.setPrefHeight(1);
        line.setMaxHeight(1);
        line.getStyleClass().add("bg-border");
        return line;
    }

    private static ImageView avatar(String dataUri, double size) {
        ImageView view = new ImageView(new Image(dataUri, size, size, true, true));
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setClip(new Circle(size / 2, size / 2, size / 2));
        return view;
    }

    /**
     * What a Next.js {@code <Image>} would be in Flowbite's {@code renderImage}
     * example.
     */
    private static ImageView customImage(String dataUri, String alt) {
        ImageView view = new ImageView(new Image(dataUri, 500, 500, true, true));
        view.setAccessibleText(alt);
        return view;
    }

    /**
     * An icon painted with a utility class (follows the theme or is a fixed palette
     * color).
     */
    private static Region icon(String path, double width, double height, String backgroundClass) {
        Region region = shaped(path, width, height);
        region.getStyleClass().add(backgroundClass);
        return region;
    }

    /** An icon painted with a changing color, e.g. a button's text fill. */
    private static Region icon(String path, double width, double height, ObservableValue<? extends Paint> fill) {
        Region region = shaped(path, width, height);
        region.backgroundProperty().bind(Bindings.createObjectBinding(
                () -> new Background(new BackgroundFill(fill.getValue(), null, null)), fill));
        return region;
    }

    private static Region shaped(String path, double width, double height) {
        SVGPath shape = new SVGPath();
        shape.setContent(path);
        shape.setFillRule(FillRule.EVEN_ODD);
        Region region = new Region();
        region.setShape(shape);
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        region.setMaxSize(width, height);
        return region;
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox captioned(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        return new VBox(6, label, content);
    }

    private static FlowPane row(Node... cards) {
        FlowPane flow = new FlowPane(24, 24);
        flow.getChildren().addAll(cards);
        return flow;
    }

    // ---- generated pictures
    // ------------------------------------------------------------------------

    /**
     * A PNG of a diagonal gradient as a {@code data:} URI. With {@code marks}, a
     * white 3px frame and a
     * ring are drawn: the frame shows what a crop cuts off, the ring shows
     * distortion.
     */
    private static String picture(int w, int h, int from, int to, boolean marks) {
        byte[] raw = new byte[(w * 3 + 1) * h];
        int p = 0;
        for (int y = 0; y < h; y++) {
            raw[p++] = 0; // PNG filter type: none
            for (int x = 0; x < w; x++) {
                double t = (x / (double) w + y / (double) h) / 2;
                int rgb = mix(from, to, t);
                if (marks) {
                    double dx = x - w / 2.0;
                    double dy = y - h / 2.0;
                    double ring = Math.abs(Math.sqrt(dx * dx + dy * dy) - Math.min(w, h) * 0.3);
                    if (ring < 2.5 || x < 3 || y < 3 || x >= w - 3 || y >= h - 3) {
                        rgb = 0xFFFFFF;
                    }
                }
                raw[p++] = (byte) (rgb >> 16);
                raw[p++] = (byte) (rgb >> 8);
                raw[p++] = (byte) rgb;
            }
        }

        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(raw);
        deflater.finish();
        ByteArrayOutputStream idat = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        while (!deflater.finished()) {
            idat.write(buffer, 0, deflater.deflate(buffer));
        }
        deflater.end();

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        png.writeBytes(new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A });
        chunk(png, "IHDR", ByteBuffer.allocate(13).putInt(w).putInt(h)
                .put((byte) 8).put((byte) 2).put((byte) 0).put((byte) 0).put((byte) 0).array());
        chunk(png, "IDAT", idat.toByteArray());
        chunk(png, "IEND", new byte[0]);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png.toByteArray());
    }

    private static int mix(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = (int) Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = (int) Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static void chunk(ByteArrayOutputStream out, String type, byte[] data) {
        byte[] name = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(name);
        crc.update(data);
        out.writeBytes(ByteBuffer.allocate(4).putInt(data.length).array());
        out.writeBytes(name);
        out.writeBytes(data);
        out.writeBytes(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
