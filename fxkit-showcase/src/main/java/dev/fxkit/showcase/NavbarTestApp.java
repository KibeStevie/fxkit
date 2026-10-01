package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.components.avatar.FxAvatar;
import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.dropdown.FxDropdown;
import dev.fxkit.core.components.dropdown.FxDropdownDivider;
import dev.fxkit.core.components.dropdown.FxDropdownHeader;
import dev.fxkit.core.components.dropdown.FxDropdownItem;
import dev.fxkit.core.components.navbar.FxNavbar;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxNavbar}: Flowbite's three examples, the responsive breakpoints
 * (wide, compact, narrow, expanded), the container max width, rounded/bordered, runtime changes,
 * edge cases, and a live playground with a width slider so you can drag through the 768px and 640px
 * breakpoints.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check that the navbar restyles correctly in light and dark.
 */
public class NavbarTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon LOGO = Devicons.JAVA;

    private static final String AVATAR_URL = "https://flowbite.com/docs/images/people/profile-picture-5.jpg";
    private static final String[] LINK_NAMES = {"Home", "About", "Services", "Pricing", "Contact"};

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (link clicks, brand clicks, toggle, actions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Drag the width slider through 768px (links collapse into the hamburger) and 640px "
                                + "(side padding shrinks). Flip every flag live."),
                playground(),

                sectionHeading("Flowbite examples",
                        "Default navbar, navbar with CTA button, navbar with dropdown. All fluid + rounded, "
                                + "as in the Flowbite docs."),
                flowbiteExamples(),

                sectionHeading("Breakpoints",
                        "Fixed widths. 900: wide. 700: compact, 16px padding. 500: compact, 8px padding. "
                                + "Last one starts expanded: links stack under the bar."),
                breakpoints(),

                sectionHeading("Container (fluid = false)",
                        "Content is centered with Tailwind's container max width (768 at 900px wide, 640 "
                                + "at 700px). The background still spans the full width."),
                containerSection(),

                sectionHeading("Rounded and bordered",
                        "Flags are independent: neither, rounded, bordered, both."),
                flagsSection(),

                sectionHeading("Links: active and disabled",
                        "Click a link to make it the current page. 'Pricing' is disabled and must not "
                                + "fire, hover, or become active."),
                linkStatesSection(),

                sectionHeading("Runtime changes",
                        "Everything rebuilds in place. Try them while compact AND expanded too."),
                runtimeSection(),

                sectionHeading("Edge cases",
                        "No links (no toggle), no brand, links but no actions, long brand, many actions."),
                edgeCases(),

                sectionHeading("Event log", "Everything the navbars report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1040, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxNavbar test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxNavbar test harness");
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
        FxNavbarBrand brand = brand("Flowbite React");
        FxNavbar navbar = new FxNavbar(brand);
        FxNavbarLink pricing = null;
        for (String name : LINK_NAMES) {
            FxNavbarLink link = link(navbar, name, name.equals("Home"));
            if (name.equals("Pricing")) {
                pricing = link;
            }
            navbar.getLinks().add(link);
        }
        FxButton cta = new FxButton("Get started");
        cta.setOnAction(e -> log("playground: CTA clicked"));
        FxDropdown user = userDropdown();

        navbar.expandedProperty().addListener((o, was, is) -> log("playground: expanded = " + is));
        navbar.compactProperty().addListener((o, was, is) -> log("playground: compact = " + is));

        VBox navHolder = new VBox(navbar);

        Slider width = new Slider(320, 1000, 900);
        width.setPrefWidth(260);
        navHolder.maxWidthProperty().bind(width.valueProperty());

        Label readout = new Label();
        readout.getStyleClass().addAll("text-xs", "text-muted");
        Runnable refreshReadout = () -> readout.setText(String.format(
                "slider %.0fpx | navbar %.0fpx | compact: %s | expanded: %s",
                width.getValue(), navbar.getWidth(), navbar.isCompact(), navbar.isExpanded()));
        navbar.widthProperty().addListener(o -> refreshReadout.run());
        navbar.compactProperty().addListener(o -> refreshReadout.run());
        navbar.expandedProperty().addListener(o -> refreshReadout.run());
        width.valueProperty().addListener(o -> refreshReadout.run());
        refreshReadout.run();

        CheckBox fluid = new CheckBox("Fluid");
        fluid.selectedProperty().bindBidirectional(navbar.fluidProperty());
        CheckBox rounded = new CheckBox("Rounded");
        rounded.selectedProperty().bindBidirectional(navbar.roundedProperty());
        CheckBox bordered = new CheckBox("Bordered");
        bordered.selectedProperty().bindBidirectional(navbar.borderedProperty());
        CheckBox expanded = new CheckBox("Expanded");
        expanded.selectedProperty().bindBidirectional(navbar.expandedProperty());

        CheckBox brandCheck = new CheckBox("Brand");
        brandCheck.setSelected(true);
        brandCheck.selectedProperty().addListener((o, was, is) -> navbar.setBrand(is ? brand : null));
        CheckBox ctaCheck = new CheckBox("CTA button");
        ctaCheck.selectedProperty().addListener((o, was, is) -> toggleAction(navbar, cta, is));
        CheckBox userCheck = new CheckBox("Avatar dropdown");
        userCheck.selectedProperty().addListener((o, was, is) -> toggleAction(navbar, user, is));

        FxNavbarLink pricingLink = pricing;
        CheckBox disablePricing = new CheckBox("Disable 'Pricing'");
        disablePricing.selectedProperty().addListener((o, was, is) -> pricingLink.setDisable(is));

        TextField brandText = new TextField(brand.getText());
        brandText.setPrefColumnCount(12);
        brand.textProperty().bind(brandText.textProperty());

        int[] extra = {0};
        Button addLink = new Button("Add link");
        addLink.setOnAction(e -> navbar.getLinks().add(link(navbar, "Link " + (++extra[0]), false)));
        Button removeLink = new Button("Remove last link");
        removeLink.setOnAction(e -> {
            if (!navbar.getLinks().isEmpty()) {
                navbar.getLinks().remove(navbar.getLinks().size() - 1);
            }
        });

        HBox row1 = row(new Label("Width"), width, readout);
        HBox row2 = row(fluid, rounded, bordered, expanded);
        HBox row3 = row(brandCheck, new Label("Name"), brandText, ctaCheck, userCheck, disablePricing);
        HBox row4 = row(addLink, removeLink);

        VBox box = new VBox(12, row1, row2, row3, row4, navHolder);
        box.getStyleClass().addAll("bg-surface-alt", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private static void toggleAction(FxNavbar navbar, Node action, boolean on) {
        if (on && !navbar.getActions().contains(action)) {
            navbar.getActions().add(action);
        } else if (!on) {
            navbar.getActions().remove(action);
        }
    }

    // ---- Flowbite examples --------------------------------------------------------------------

    private VBox flowbiteExamples() {
        FxNavbar plain = fluidRounded(simpleNavbar("Flowbite React"));

        FxNavbar withCta = fluidRounded(simpleNavbar("Flowbite React"));
        FxButton cta = new FxButton("Get started");
        cta.setOnAction(e -> log("CTA example: Get started clicked"));
        withCta.getActions().add(cta);

        FxNavbar withDropdown = fluidRounded(simpleNavbar("Flowbite React"));
        withDropdown.getActions().add(userDropdown());

        VBox box = new VBox(16,
                labeled("default navbar", plain),
                labeled("navbar with CTA button", withCta),
                labeled("navbar with dropdown", withDropdown));
        return box;
    }

    // ---- breakpoints --------------------------------------------------------------------------

    private VBox breakpoints() {
        FxNavbar expandedNav = fluidRounded(simpleNavbar("Flowbite React"));
        expandedNav.getActions().add(new FxButton("Get started"));
        expandedNav.setExpanded(true);

        return new VBox(16,
                labeled("900px (wide)", fixed(fluidRounded(simpleNavbar("Flowbite React")), 900)),
                labeled("700px (compact)", fixed(fluidRounded(simpleNavbar("Flowbite React")), 700)),
                labeled("500px (compact, narrow padding, with CTA)", fixed(withCta(), 500)),
                labeled("500px (compact, expanded)", fixed(expandedNav, 500)));
    }

    private FxNavbar withCta() {
        FxNavbar navbar = fluidRounded(simpleNavbar("Flowbite React"));
        navbar.getActions().add(new FxButton("Get started"));
        return navbar;
    }

    // ---- container ----------------------------------------------------------------------------

    private VBox containerSection() {
        FxNavbar wide = simpleNavbar("Container 900");
        wide.getActions().add(new FxButton("Get started"));
        FxNavbar medium = simpleNavbar("Container 700");
        medium.setBordered(true);
        return new VBox(16,
                labeled("fluid = false at 900px", fixed(wide, 900)),
                labeled("fluid = false at 700px (compact)", fixed(medium, 700)));
    }

    // ---- flags --------------------------------------------------------------------------------

    private VBox flagsSection() {
        FxNavbar neither = fluidRounded(simpleNavbar("Neither"));
        neither.setRounded(false);
        FxNavbar roundedOnly = fluidRounded(simpleNavbar("Rounded"));
        FxNavbar borderedOnly = fluidRounded(simpleNavbar("Bordered"));
        borderedOnly.setRounded(false);
        borderedOnly.setBordered(true);
        FxNavbar both = fluidRounded(simpleNavbar("Rounded + bordered"));
        both.setBordered(true);
        return new VBox(16,
                labeled("neither", neither),
                labeled("rounded", roundedOnly),
                labeled("bordered", borderedOnly),
                labeled("rounded + bordered", both));
    }

    // ---- link states --------------------------------------------------------------------------

    private VBox linkStatesSection() {
        FxNavbar wide = fluidRounded(simpleNavbar("Wide"));
        disablePricing(wide);
        FxNavbar compact = fluidRounded(simpleNavbar("Compact"));
        compact.setExpanded(true);
        disablePricing(compact);
        return new VBox(16,
                labeled("wide: active link is primary-colored, disabled is muted", wide),
                labeled("compact + expanded: active link is a filled row, others have separators",
                        fixed(compact, 500)));
    }

    private static void disablePricing(FxNavbar navbar) {
        for (Node node : navbar.getLinks()) {
            if (node instanceof FxNavbarLink link && "Pricing".equals(link.getText())) {
                link.setDisable(true);
            }
        }
    }

    // ---- runtime changes ----------------------------------------------------------------------

    private VBox runtimeSection() {
        FxNavbarBrand brand = brand("Runtime");
        FxNavbar navbar = fluidRounded(new FxNavbar(brand));
        for (String name : LINK_NAMES) {
            navbar.getLinks().add(link(navbar, name, name.equals("Home")));
        }
        var savedLinks = new java.util.ArrayList<>(navbar.getLinks());

        Button toggleBrand = new Button("Toggle brand");
        toggleBrand.setOnAction(e -> navbar.setBrand(navbar.getBrand() == null ? brand : null));

        Button clearLinks = new Button("Clear links");
        clearLinks.setOnAction(e -> navbar.getLinks().clear());

        Button restoreLinks = new Button("Restore links");
        restoreLinks.setOnAction(e -> navbar.getLinks().setAll(savedLinks));

        Button reverseLinks = new Button("Reverse links");
        reverseLinks.setOnAction(e -> {
            var reversed = new java.util.ArrayList<>(navbar.getLinks());
            java.util.Collections.reverse(reversed);
            navbar.getLinks().setAll(reversed);
        });

        FxButton cta = new FxButton("Get started");
        Button toggleCta = new Button("Toggle CTA");
        toggleCta.setOnAction(e -> toggleAction(navbar, cta, !navbar.getActions().contains(cta)));

        Button toggleExpanded = new Button("Toggle expanded");
        toggleExpanded.setOnAction(e -> navbar.setExpanded(!navbar.isExpanded()));

        Button toggleFluid = new Button("Toggle fluid");
        toggleFluid.setOnAction(e -> navbar.setFluid(!navbar.isFluid()));

        VBox holder = new VBox(navbar);
        Slider width = new Slider(320, 900, 500);
        holder.maxWidthProperty().bind(width.valueProperty());

        VBox box = new VBox(10,
                row(new Label("Width"), width),
                row(toggleBrand, clearLinks, restoreLinks, reverseLinks),
                row(toggleCta, toggleExpanded, toggleFluid),
                holder);
        box.getStyleClass().addAll("bg-surface-alt", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ---------------------------------------------------------------------------

    private VBox edgeCases() {
        FxNavbar noLinks = fluidRounded(new FxNavbar(brand("No links")));
        noLinks.getActions().add(new FxButton("Get started"));

        FxNavbar noLinksCompact = fluidRounded(new FxNavbar(brand("No links, compact")));
        noLinksCompact.getActions().add(new FxButton("Get started"));

        FxNavbar noBrand = fluidRounded(new FxNavbar());
        for (String name : LINK_NAMES) {
            noBrand.getLinks().add(link(noBrand, name, name.equals("Home")));
        }

        FxNavbar noActions = fluidRounded(simpleNavbar("Links only"));

        FxNavbar longBrand = fluidRounded(simpleNavbar("A deliberately very long brand name that competes for room"));
        longBrand.getActions().add(new FxButton("Get started"));

        FxNavbar manyActions = fluidRounded(simpleNavbar("Many actions"));
        manyActions.getActions().addAll(new FxButton("One"), new FxButton("Two"), userDropdown());

        FxNavbar brandOnly = fluidRounded(new FxNavbar(brand("Brand only")));

        FxNavbar empty = fluidRounded(new FxNavbar());

        return new VBox(16,
                labeled("brand + CTA, no links (wide: no toggle)", noLinks),
                labeled("same at 400px (compact, but still no toggle)", fixed(noLinksCompact, 400)),
                labeled("links only, no brand", noBrand),
                labeled("links, no actions", noActions),
                labeled("long brand at 760px, wide", fixed(longBrand, 760)),
                labeled("many actions at 560px (compact, toggle after the actions)", fixed(manyActions, 560)),
                labeled("brand only", brandOnly),
                labeled("completely empty navbar", empty));
    }

    // ---- builders -----------------------------------------------------------------------------

    private FxNavbarBrand brand(String text) {
        FxNavbarBrand brand = new FxNavbarBrand(text, new FontIcon(LOGO));
        brand.setOnAction(e -> log("brand clicked: " + brand.getText()));
        return brand;
    }

    /** A link that logs, and becomes the only active link of its navbar when clicked. */
    private FxNavbarLink link(FxNavbar navbar, String text, boolean active) {
        FxNavbarLink link = new FxNavbarLink(text, active);
        link.setOnAction(e -> {
            for (Node node : navbar.getLinks()) {
                if (node instanceof FxNavbarLink other) {
                    other.setActive(other == link);
                }
            }
            log("link clicked: " + text);
        });
        return link;
    }

    private FxNavbar simpleNavbar(String brandText) {
        FxNavbar navbar = new FxNavbar(brand(brandText));
        for (String name : LINK_NAMES) {
            navbar.getLinks().add(link(navbar, name, name.equals("Home")));
        }
        return navbar;
    }

    private static FxNavbar fluidRounded(FxNavbar navbar) {
        navbar.setFluid(true);
        navbar.setRounded(true);
        return navbar;
    }

    /** The avatar-as-trigger dropdown from Flowbite's "Navbar with dropdown". */
    private FxDropdown userDropdown() {
        FxAvatar avatar = new FxAvatar(AVATAR_URL);
        avatar.setRounded(true);
        avatar.setPlaceholderInitials("BG");
        avatar.setAlt("User settings");

        FxDropdown user = new FxDropdown();
        user.setInline(true);
        user.setArrowIcon(false); // before setGraphic: FxDropdown owns the graphic while the arrow is on
        user.setGraphic(avatar);
        user.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        FxDropdownItem signOut = new FxDropdownItem("Sign out");
        signOut.setOnAction(e -> log("user menu: Sign out"));
        FxDropdownItem dashboard = new FxDropdownItem("Dashboard");
        dashboard.setOnAction(e -> log("user menu: Dashboard"));
        user.getItems().addAll(
                new FxDropdownHeader("Bonnie Green", "name@flowbite.com"),
                dashboard,
                new FxDropdownItem("Settings"),
                new FxDropdownItem("Earnings"),
                new FxDropdownDivider(),
                signOut);
        return user;
    }

    // ---- helpers ------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    /** Pins the navbar's width by wrapping it in a holder of exactly that width. */
    private static VBox fixed(FxNavbar navbar, double width) {
        VBox holder = new VBox(navbar);
        holder.setMinWidth(0);
        holder.setPrefWidth(width);
        holder.setMaxWidth(width);
        return holder;
    }

    private static HBox row(Node... nodes) {
        HBox row = new HBox(16, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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
        wrapper.getStyleClass().addAll("bg-surface-alt", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}