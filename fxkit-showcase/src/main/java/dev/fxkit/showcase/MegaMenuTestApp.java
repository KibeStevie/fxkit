package dev.fxkit.showcase;

import java.net.URL;

import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.megamenu.FxMegaMenu;
import dev.fxkit.core.components.megamenu.FxMegaMenuColumn;
import dev.fxkit.core.components.megamenu.FxMegaMenuCta;
import dev.fxkit.core.components.megamenu.FxMegaMenuDropdown;
import dev.fxkit.core.components.megamenu.FxMegaMenuGrid;
import dev.fxkit.core.components.megamenu.FxMegaMenuImageCard;
import dev.fxkit.core.components.megamenu.FxMegaMenuItem;
import dev.fxkit.core.components.megamenu.FxMegaMenuLink;
import dev.fxkit.core.components.navbar.FxNavbarBrand;
import dev.fxkit.core.components.navbar.FxNavbarLink;
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
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxMegaMenu}: Flowbite React's four mega menu
 * examples (default,
 * with icons, full width, full width with CTA, full width with image), a live
 * playground where the
 * navbar flags, the dropdown look and the content can be flipped at runtime,
 * and a few edge cases.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check the panels in light and dark (the
 * popup is a separate window
 * and re-reads the theme every time it opens), and drag the width sliders below
 * 768px to see the
 * hamburger layout, where every dropdown becomes a full width panel under the
 * navbar.
 *
 * <p>
 * The image example looks for {@code /images/dashboard-overview.png} on the
 * classpath; without it the
 * card is plain gray.
 */
public class MegaMenuTestApp extends Application {

    private static final String SAMPLE_TEXT = "Connect with third-party tools that you're already using.";

    // Icon path data copied from Flowbite's "Mega menu with icons"; the number is
    // the viewBox size.
    private static final String ABOUT = "M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM9.5 4a1.5 1.5 0 1 1 0 3 1.5 1.5 0 0 1 0-3ZM12 15H8a1 1 0 0 1 0-2h1v-3H8a1 1 0 0 1 0-2h2a1 1 0 0 1 1 1v4h1a1 1 0 0 1 0 2Z";
    private static final String LIBRARY = "m1.56 6.245 8 3.924a1 1 0 0 0 .88 0l8-3.924a1 1 0 0 0 0-1.8l-8-3.925a1 1 0 0 0-.88 0l-8 3.925a1 1 0 0 0 0 1.8Z "
            + "M18 8.376a1 1 0 0 0-1 1v.163l-7 3.434-7-3.434v-.163a1 1 0 0 0-2 0v.786a1 1 0 0 0 .56.9l8 3.925a1 1 0 0 0 .88 0l8-3.925a1 1 0 0 0 .56-.9v-.786a1 1 0 0 0-1-1Z "
            + "M17.993 13.191a1 1 0 0 0-1 1v.163l-7 3.435-7-3.435v-.163a1 1 0 1 0-2 0v.787a1 1 0 0 0 .56.9l8 3.925a1 1 0 0 0 .88 0l8-3.925a1 1 0 0 0 .56-.9v-.787a1 1 0 0 0-1-1Z";
    private static final String RESOURCES = "M15.977.783A1 1 0 0 0 15 0H3a1 1 0 0 0-.977.783L.2 9h4.239a2.99 2.99 0 0 1 2.742 1.8 1.977 1.977 0 0 0 3.638 0A2.99 2.99 0 0 1 13.561 9H17.8L15.977.783ZM6 2h6a1 1 0 1 1 0 2H6a1 1 0 0 1 0-2Zm7 5H5a1 1 0 0 1 0-2h8a1 1 0 1 1 0 2Z "
            + "M1 18h16a1 1 0 0 0 1-1v-6h-4.439a.99.99 0 0 0-.908.6 3.978 3.978 0 0 1-7.306 0 .99.99 0 0 0-.908-.6H0v6a1 1 0 0 0 1 1Z";
    private static final String PRO = "m7.164 3.805-4.475.38L.327 6.546a1.114 1.114 0 0 0 .63 1.89l3.2.375 3.007-5.006ZM11.092 15.9l.472 3.14a1.114 1.114 0 0 0 1.89.63l2.36-2.362.38-4.475-5.102 3.067Zm8.617-14.283A1.613 1.613 0 0 0 18.383.291c-1.913-.33-5.811-.736-7.556 1.01-1.98 1.98-6.172 9.491-7.477 11.869a1.1 1.1 0 0 0 .193 1.316l.986.985.985.986a1.1 1.1 0 0 0 1.316.193c2.378-1.3 9.889-5.5 11.869-7.477 1.746-1.745 1.34-5.643 1.01-7.556Zm-3.873 6.268a2.63 2.63 0 1 1-3.72-3.72 2.63 2.63 0 0 1 3.72 3.72Z";
    private static final String BLOG = "M19 4h-1a1 1 0 1 0 0 2v11a1 1 0 0 1-2 0V2a2 2 0 0 0-2-2H2a2 2 0 0 0-2 2v15a3 3 0 0 0 3 3h14a3 3 0 0 0 3-3V5a1 1 0 0 0-1-1ZM3 4a1 1 0 0 1 1-1h3a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4Zm9 13H4a1 1 0 0 1 0-2h8a1 1 0 0 1 0 2Zm0-3H4a1 1 0 0 1 0-2h8a1 1 0 0 1 0 2Zm0-3H4a1 1 0 0 1 0-2h8a1 1 0 1 1 0 2Zm0-3h-2a1 1 0 0 1 0-2h2a1 1 0 1 1 0 2Z "
            + "M6 5H5v1h1V5Z";
    private static final String NEWSLETTER = "m17.418 3.623-.018-.008a6.713 6.713 0 0 0-2.4-.569V2h1a1 1 0 1 0 0-2h-2a1 1 0 0 0-1 1v2H9.89A6.977 6.977 0 0 1 12 8v5h-2V8A5 5 0 1 0 0 8v6a1 1 0 0 0 1 1h8v4a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1v-4h6a1 1 0 0 0 1-1V8a5 5 0 0 0-2.582-4.377ZM6 12H4a1 1 0 0 1 0-2h2a1 1 0 0 1 0 2Z";
    private static final String PLAYGROUND = "M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0Zm10 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286A1.857 1.857 0 0 0 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0Zm-10 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286A1.857 1.857 0 0 0 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10ZM17 13h-2v-2a1 1 0 0 0-2 0v2h-2a1 1 0 0 0 0 2h2v2a1 1 0 0 0 2 0v-2h2a1 1 0 0 0 0-2Z";
    private static final String LICENSE = "M13.383.076a1 1 0 0 0-1.09.217L11 1.586 9.707.293a1 1 0 0 0-1.414 0L7 1.586 5.707.293a1 1 0 0 0-1.414 0L3 1.586 1.707.293A1 1 0 0 0 0 1v18a1 1 0 0 0 1.707.707L3 18.414l1.293 1.293a1 1 0 0 0 1.414 0L7 18.414l1.293 1.293a1 1 0 0 0 1.414 0L11 18.414l1.293 1.293A1 1 0 0 0 14 19V1a1 1 0 0 0-.617-.924ZM10 15H4a1 1 0 1 1 0-2h6a1 1 0 0 1 0 2Zm0-4H4a1 1 0 1 1 0-2h6a1 1 0 1 1 0 2Zm0-4H4a1 1 0 0 1 0-2h6a1 1 0 1 1 0 2Z";
    private static final String CONTACT = "M12 0H2a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2ZM7.5 17.5h-1a1 1 0 0 1 0-2h1a1 1 0 0 1 0 2ZM12 13H2V4h10v9Z";
    private static final String SUPPORT = "m5.4 2.736 3.429 3.429A5.046 5.046 0 0 1 10.134 6c.356.01.71.06 1.056.147l3.41-3.412c.136-.133.287-.248.45-.344A9.889 9.889 0 0 0 10.269 1c-1.87-.041-3.713.44-5.322 1.392a2.3 2.3 0 0 1 .454.344Zm11.45 1.54-.126-.127a.5.5 0 0 0-.706 0l-2.932 2.932c.029.023.049.054.078.077.236.194.454.41.65.645.034.038.078.067.11.107l2.927-2.927a.5.5 0 0 0 0-.707Zm-2.931 9.81c-.024.03-.057.052-.081.082a4.963 4.963 0 0 1-.633.639c-.041.036-.072.083-.115.117l2.927 2.927a.5.5 0 0 0 .707 0l.127-.127a.5.5 0 0 0 0-.707l-2.932-2.931Zm-1.442-4.763a3.036 3.036 0 0 0-1.383-1.1l-.012-.007a2.955 2.955 0 0 0-1-.213H10a2.964 2.964 0 0 0-2.122.893c-.285.29-.509.634-.657 1.013l-.01.016a2.96 2.96 0 0 0-.21 1 2.99 2.99 0 0 0 .489 1.716c.009.014.022.026.032.04a3.04 3.04 0 0 0 1.384 1.1l.012.007c.318.129.657.2 1 .213.392.015.784-.05 1.15-.192.012-.005.02-.013.033-.018a3.011 3.011 0 0 0 1.676-1.7v-.007a2.89 2.89 0 0 0 0-2.207 2.868 2.868 0 0 0-.27-.515c-.007-.012-.02-.025-.03-.039Zm6.137-3.373a2.53 2.53 0 0 1-.35.447L14.84 9.823c.112.428.166.869.16 1.311-.01.356-.06.709-.147 1.054l3.413 3.412c.132.134.249.283.347.444A9.88 9.88 0 0 0 20 11.269a9.912 9.912 0 0 0-1.386-5.319ZM14.6 19.264l-3.421-3.421c-.385.1-.781.152-1.18.157h-.134c-.356-.01-.71-.06-1.056-.147l-3.41 3.412a2.503 2.503 0 0 1-.443.347A9.884 9.884 0 0 0 9.732 21H10a9.9 9.9 0 0 0 5.044-1.388 2.519 2.519 0 0 1-.444-.348ZM1.735 15.6l3.426-3.426a4.608 4.608 0 0 1-.013-2.367L1.735 6.4a2.507 2.507 0 0 1-.35-.447 9.889 9.889 0 0 0 0 10.1c.1-.164.217-.316.35-.453Zm5.101-.758a4.957 4.957 0 0 1-.651-.645c-.033-.038-.077-.067-.11-.107L3.15 17.017a.5.5 0 0 0 0 .707l.127.127a.5.5 0 0 0 .706 0l2.932-2.933c-.03-.018-.05-.053-.078-.076ZM6.08 7.914c.03-.037.07-.063.1-.1.183-.22.384-.423.6-.609.047-.04.082-.092.129-.13L3.983 4.149a.5.5 0 0 0-.707 0l-.127.127a.5.5 0 0 0 0 .707L6.08 7.914Z";
    private static final String TERMS = "M16 1h-3.278A1.992 1.992 0 0 0 11 0H7a1.993 1.993 0 0 0-1.722 1H2a2 2 0 0 0-2 2v15a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2Zm-3 14H5a1 1 0 0 1 0-2h8a1 1 0 0 1 0 2Zm0-4H5a1 1 0 0 1 0-2h8a1 1 0 1 1 0 2Zm0-5H5a1 1 0 0 1 0-2h2V2h4v2h2a1 1 0 1 1 0 2Z";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (link actions, dropdown open/close)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "One mega menu, everything live: pick the content, flip fluid / rounded / bordered, "
                                + "switch the dropdown between floating and full width, and drag the width "
                                + "below 768px to get the hamburger layout."),
                playground(),

                sectionHeading("Default mega menu",
                        "Three columns of links in a floating card under the toggle. Login / Sign up hide "
                                + "while the navbar is compact (Flowbite's hidden md:flex)."),
                labeled("floating, 3 columns", defaultMegaMenu()),

                sectionHeading("Mega menu with icons",
                        "Same card; each link has a 12px icon that turns primary with the text."),
                labeled("floating, 3 columns, icons", iconsMegaMenu()),

                sectionHeading("Full width dropdown",
                        "Panel as wide as the navbar, content centered in 1280px. The grid drops to 2 or 1 "
                                + "column as it narrows (minColumnWidth)."),
                labeled("full width, title + description items", fullWidthMegaMenu()),

                sectionHeading("Full width with CTA",
                        "Two link lists and a text call-to-action; links underline on hover."),
                labeled("full width, CTA", ctaMegaMenu()),

                sectionHeading("Full width with image",
                        "Two link lists and a picture card. The whole card is one click target."),
                labeled("full width, image", imageMegaMenu()),

                sectionHeading("Edge cases",
                        "Two dropdowns in one navbar (opening one closes the other), an empty dropdown "
                                + "(opens nothing), a disabled toggle, and closeOnAction off."),
                labeled("sibling dropdowns / empty / disabled / stays open", edgeCases()),

                sectionHeading("Event log", "Link clicks and dropdown open / close land here."),
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

        stage.setTitle("FxMegaMenu test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxMegaMenu test harness");
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
        FxMegaMenuDropdown dropdown = new FxMegaMenuDropdown("Company", defaultContent());
        watch(dropdown, "playground");
        FxMegaMenu menu = menu(dropdown, "Home", "Team");

        ComboBox<String> contentBox = new ComboBox<>();
        contentBox.getItems().addAll("Links (3 columns)", "Links with icons", "Items (title + description)",
                "Links + CTA", "Links + image", "Nothing (null)");
        contentBox.getSelectionModel().selectFirst();
        contentBox.valueProperty().addListener((o, was, is) -> dropdown.setContent(switch (is) {
            case "Links with icons" -> iconsContent();
            case "Items (title + description)" -> itemsContent();
            case "Links + CTA" -> ctaContent();
            case "Links + image" -> imageContent();
            case "Nothing (null)" -> null;
            default -> defaultContent();
        }));

        CheckBox fullWidth = new CheckBox("Full width dropdown");
        dropdown.fullWidthProperty().bind(fullWidth.selectedProperty());
        CheckBox closeOnAction = new CheckBox("Close on action");
        closeOnAction.setSelected(true);
        dropdown.closeOnActionProperty().bind(closeOnAction.selectedProperty());
        CheckBox fluid = new CheckBox("Fluid");
        menu.fluidProperty().bind(fluid.selectedProperty());
        CheckBox rounded = new CheckBox("Rounded");
        menu.roundedProperty().bind(rounded.selectedProperty());
        CheckBox bordered = new CheckBox("Bordered");
        bordered.setSelected(true);
        menu.borderedProperty().bind(bordered.selectedProperty());

        Slider width = new Slider(320, 1100, 1000);
        width.setPrefWidth(260);
        menu.maxWidthProperty().bind(width.valueProperty());
        Label widthLabel = new Label();
        widthLabel.getStyleClass().addAll("text-xs", "text-muted");
        widthLabel.textProperty().bind(width.valueProperty().asString("navbar width: %.0f px"));

        Button open = new Button("show()");
        open.setOnAction(e -> dropdown.show());
        Button close = new Button("hide()");
        close.setOnAction(e -> dropdown.hide());

        HBox row1 = new HBox(14, new Label("Content"), contentBox, fullWidth, closeOnAction, open, close);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(14, fluid, rounded, bordered, width, widthLabel);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, menu, row1, row2);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the four Flowbite examples
    // ---------------------------------------------------------

    private FxMegaMenu defaultMegaMenu() {
        FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", defaultContent());
        watch(company, "default");
        FxMegaMenu menu = menu(company, "Home", "Team", "Contact");
        addLoginAndSignUp(menu);
        return menu;
    }

    private FxMegaMenu iconsMegaMenu() {
        FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", iconsContent());
        watch(company, "icons");
        FxMegaMenu menu = menu(company, "Home", "Team");
        addLoginAndSignUp(menu);
        return menu;
    }

    private FxMegaMenu fullWidthMegaMenu() {
        FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", itemsContent());
        company.setFullWidth(true);
        watch(company, "full width");
        return menu(company, "Home", "Marketplace", "Resources", "Contact");
    }

    private FxMegaMenu ctaMegaMenu() {
        FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", ctaContent());
        company.setFullWidth(true);
        watch(company, "cta");
        return menu(company, "Home", "Marketplace", "Resources", "Contact");
    }

    private FxMegaMenu imageMegaMenu() {
        FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", imageContent());
        company.setFullWidth(true);
        watch(company, "image");
        return menu(company, "Home", "Marketplace", "Resources", "Contact");
    }

    // ---- dropdown contents
    // ------------------------------------------------------------------

    private Node defaultContent() {
        return new FxMegaMenuGrid(3,
                new FxMegaMenuColumn(link("About Us"), link("Library"), link("Resources"), link("Pro Version")),
                new FxMegaMenuColumn(link("Contact Us"), link("Support Center"), link("Terms"), link("Blog")),
                new FxMegaMenuColumn(link("Newsletter"), link("Playground"), link("License")));
    }

    private Node iconsContent() {
        return new FxMegaMenuGrid(3,
                new FxMegaMenuColumn(
                        link("About Us", ABOUT, 20), link("Library", LIBRARY, 20),
                        link("Resources", RESOURCES, 18), link("Pro Version", PRO, 20)),
                new FxMegaMenuColumn(
                        link("Blog", BLOG, 20), link("Newsletter", NEWSLETTER, 20),
                        link("Playground", PLAYGROUND, 18), link("License", LICENSE, 20)),
                new FxMegaMenuColumn(
                        link("Contact Us", CONTACT, 20), link("Support Center", SUPPORT, 21),
                        link("Terms", TERMS, 20)));
    }

    private Node itemsContent() {
        FxMegaMenuGrid grid = new FxMegaMenuGrid(3,
                item("Online Stores"), item("Segmentation"), item("Marketing CRM"),
                item("Online Stores"), item("Segmentation"), item("Marketing CRM"),
                item("Audience Management"), item("Creative Tools"), item("Marketing Automation"));
        grid.setMinColumnWidth(240); // sm:grid-cols-2 md:grid-cols-3
        return grid;
    }

    private Node ctaContent() {
        FxMegaMenuCta cta = new FxMegaMenuCta("Our brands",
                "At Flowbite, we have a portfolio of brands that cater to a variety of preferences.",
                "Explore our brands");
        cta.setOnAction(e -> log("cta: Explore our brands"));
        FxMegaMenuGrid grid = new FxMegaMenuGrid(3,
                flush(underlined("Online Stores"), underlined("Segmentation"), underlined("Marketing CRM"),
                        underlined("Online Stores")),
                flush(underlined("Our Blog"), underlined("Terms & Conditions"), underlined("License"),
                        underlined("Resources")),
                cta);
        grid.setHgap(16);
        grid.setMinColumnWidth(220);
        return grid;
    }

    private Node imageContent() {
        FxMegaMenuImageCard card = new FxMegaMenuImageCard("Preview the new Flowbite dashboard navigation.",
                "Get started");
        URL image = MegaMenuTestApp.class.getResource("/images/dashboard-overview.png");
        if (image != null) {
            card.setImageUrl(image.toExternalForm());
        }
        card.setOnAction(e -> log("image card: Get started"));
        FxMegaMenuGrid grid = new FxMegaMenuGrid(3,
                flush(underlined("Online Stores"), underlined("Segmentation"), underlined("Marketing CRM"),
                        underlined("Online Stores")),
                flush(underlined("Our Blog"), underlined("Terms & Conditions"), underlined("License"),
                        underlined("Resources")),
                card);
        grid.setHgap(16);
        grid.setMinColumnWidth(220);
        return grid;
    }

    // ---- edge cases
    // -------------------------------------------------------------------------

    private FxMegaMenu edgeCases() {
        FxMegaMenuDropdown first = new FxMegaMenuDropdown("Company", defaultContent());
        watch(first, "sibling A");

        FxMegaMenuDropdown second = new FxMegaMenuDropdown("Products", itemsContent());
        second.setFullWidth(true);
        watch(second, "sibling B");

        FxMegaMenuDropdown empty = new FxMegaMenuDropdown("Empty");
        watch(empty, "empty");

        FxMegaMenuDropdown disabled = new FxMegaMenuDropdown("Disabled", defaultContent());
        disabled.getToggle().setDisable(true);

        FxMegaMenuDropdown stays = new FxMegaMenuDropdown("Stays open", defaultContent());
        stays.setCloseOnAction(false);
        watch(stays, "stays open");

        FxMegaMenu menu = new FxMegaMenu(new FxNavbarBrand("Edge cases"));
        menu.setBordered(true);
        menu.getLinks().addAll(first.getToggle(), second.getToggle(), empty.getToggle(), disabled.getToggle(),
                stays.getToggle());
        return menu;
    }

    // ---- building blocks
    // --------------------------------------------------------------------

    /**
     * A mega menu with the brand, a "Home" (active) link, the dropdown's toggle,
     * then the other links.
     */
    private FxMegaMenu menu(FxMegaMenuDropdown dropdown, String... links) {
        FxMegaMenu menu = new FxMegaMenu(new FxNavbarBrand("Flowbite"));
        menu.setBordered(true);
        menu.setRounded(true);
        FxNavbarLink home = new FxNavbarLink(links[0], true);
        menu.getLinks().addAll(home, dropdown.getToggle());
        for (int i = 1; i < links.length; i++) {
            String text = links[i];
            FxNavbarLink link = new FxNavbarLink(text);
            link.setOnAction(e -> log("navbar link: " + text));
            menu.getLinks().add(link);
        }
        return menu;
    }

    private void addLoginAndSignUp(FxMegaMenu menu) {
        Hyperlink login = new Hyperlink("Login");
        login.getStyleClass().addAll("text-body", "text-sm", "font-medium");
        login.setOnAction(e -> log("Login"));
        FxButton signUp = new FxButton("Sign up");
        signUp.setOnAction(e -> log("Sign up"));
        HBox auth = new HBox(4, login, signUp);
        auth.setAlignment(Pos.CENTER);
        auth.visibleProperty().bind(menu.compactProperty().not()); // hidden md:flex
        auth.managedProperty().bind(menu.compactProperty().not());
        menu.getActions().add(auth);
    }

    private FxMegaMenuLink link(String text) {
        FxMegaMenuLink link = new FxMegaMenuLink(text);
        link.setOnAction(e -> log("link: " + text));
        return link;
    }

    private FxMegaMenuLink link(String text, String iconPath, double viewBox) {
        FxMegaMenuLink link = new FxMegaMenuLink(text, FxMegaMenuLink.svgIcon(iconPath, viewBox));
        link.setOnAction(e -> log("link: " + text));
        return link;
    }

    private FxMegaMenuLink underlined(String text) {
        FxMegaMenuLink link = link(text);
        link.setUnderlineOnHover(true);
        return link;
    }

    private static FxMegaMenuColumn flush(Node... links) {
        FxMegaMenuColumn column = new FxMegaMenuColumn(links);
        column.setFlush(true);
        return column;
    }

    private FxMegaMenuItem item(String title) {
        FxMegaMenuItem item = new FxMegaMenuItem(title, SAMPLE_TEXT);
        item.setOnAction(e -> log("item: " + title));
        return item;
    }

    private void watch(FxMegaMenuDropdown dropdown, String name) {
        dropdown.openProperty().addListener((o, was, is) -> log(name + ": " + (is ? "opened" : "closed")));
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        subtitleLabel.setWrapText(true);
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
