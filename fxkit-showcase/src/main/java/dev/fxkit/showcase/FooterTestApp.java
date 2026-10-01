package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.components.footer.FxFooter;
import dev.fxkit.core.components.footer.FxFooterBrand;
import dev.fxkit.core.components.footer.FxFooterCopyright;
import dev.fxkit.core.components.footer.FxFooterDivider;
import dev.fxkit.core.components.footer.FxFooterIcon;
import dev.fxkit.core.components.footer.FxFooterLink;
import dev.fxkit.core.components.footer.FxFooterLinkGroup;
import dev.fxkit.core.components.footer.FxFooterRow;
import dev.fxkit.core.components.footer.FxFooterTitle;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Isolated test harness for the FxFooter family: the four Flowbite examples, a live playground, the
 * root flags (container / bgDark), FxFooterRow layout (one, two, three children, vertical alignment,
 * squeezing), link groups (row, column, wrapping), copyright variants, brand (text, icon, raster
 * logo, click), icons, titles, dividers, and edge cases.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check light and dark; use the sliders to check shrinking and
 * wrapping. Needs {@code ikonli-fontawesome5-pack} on the classpath (swap the icons for another pack
 * if you prefer).
 */
public class FooterTestApp extends Application {

    private enum Preset { DEFAULT, LOGO, SOCIAL, SITEMAP }

    private static final Ikon LOGO_ICON = FontAwesomeSolid.CUBE;

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (link, brand and icon clicks)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Pick a layout, flip container / bgDark, edit the copyright, and drag the width "
                                + "slider to watch the row shrink and the links wrap."),
                playground(),

                sectionHeading("The four Flowbite examples",
                        "Default, with logo, social media icons, sitemap links (bgDark). Compare with the docs."),
                flowbiteExamples(),

                sectionHeading("Root flags: container and bgDark",
                        "container adds 24px padding. bgDark is gray-800 with light text in BOTH themes."),
                rootFlags(),

                sectionHeading("FxFooterRow layout",
                        "One child fills the width; two or more are spread edge to edge, min 16px apart."),
                rowLayouts(),

                sectionHeading("Squeeze test",
                        "Drag the slider: the row shrinks its children (never below their minimum) "
                                + "and keeps at least 16px between them. The link group wraps."),
                squeezeTest(),

                sectionHeading("Link groups",
                        "Row: 24px apart, wraps. Column: 16px apart. Hover underlines, Tab shows a focus ring."),
                linkGroups(),

                sectionHeading("Copyright",
                        "Year optional, 'by' plain or a link (when onAction is set), left or centered."),
                copyrights(),

                sectionHeading("Brand",
                        "Text only, FontIcon logo, raster logo (32px high), clickable. SVG is not supported."),
                brands(),

                sectionHeading("Icons",
                        "20px, muted. Light theme: no hover change. Dark theme (and bgDark): brighter on hover."),
                icons(),

                sectionHeading("Title and divider",
                        "Title is upper-cased from the string you set. Divider is 65px tall, 1px line."),
                titlesAndDividers(),

                sectionHeading("Edge cases",
                        "Empty footer, no container, very long text, runtime changes, parts outside a footer."),
                edgeCases(),

                sectionHeading("Event log", "Everything clicked lands here."),
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

        stage.setTitle("FxFooter test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxFooter test harness");
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
        FxFooter footer = new FxFooter();
        VBox holder = new VBox(footer);
        holder.setAlignment(Pos.TOP_LEFT);

        ComboBox<Preset> presetBox = new ComboBox<>();
        presetBox.getItems().addAll(Preset.values());
        presetBox.setValue(Preset.DEFAULT);

        CheckBox containerCheck = new CheckBox("container");
        containerCheck.setSelected(true);
        footer.containerProperty().bind(containerCheck.selectedProperty());

        CheckBox bgDarkCheck = new CheckBox("bgDark");
        footer.bgDarkProperty().bind(bgDarkCheck.selectedProperty());

        TextField byField = new TextField("Flowbite™");
        TextField yearField = new TextField("2022");
        yearField.setPrefColumnCount(5);
        yearField.setPromptText("year");
        byField.setPromptText("by");
        CheckBox linkCheck = new CheckBox("copyright is a link");

        Slider width = new Slider(240, 1000, 1000);
        width.setPrefWidth(220);
        footer.maxWidthProperty().bind(width.valueProperty());

        Runnable rebuild = () -> {
            FxFooterCopyright copyright = copyright(byField.getText(), yearField.getText(), linkCheck.isSelected());
            footer.getChildren().setAll(preset(presetBox.getValue(), copyright));
        };
        presetBox.valueProperty().addListener((o, a, b) -> rebuild.run());
        byField.textProperty().addListener((o, a, b) -> rebuild.run());
        yearField.textProperty().addListener((o, a, b) -> rebuild.run());
        linkCheck.selectedProperty().addListener((o, a, b) -> rebuild.run());
        rebuild.run();

        HBox row1 = new HBox(16, new Label("Layout"), presetBox, containerCheck, bgDarkCheck,
                new Label("Width"), width);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(10, new Label("by"), byField, new Label("year"), yearField, linkCheck);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, holder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    /** The children for one of the four Flowbite layouts. */
    private Node[] preset(Preset preset, FxFooterCopyright copyright) {
        return switch (preset) {
            case DEFAULT -> new Node[] {
                    copyright,
                    links(false, "About", "Privacy Policy", "Licensing", "Contact")
            };
            case LOGO -> {
                copyright.setAlignment(Pos.CENTER);
                yield new Node[] {
                        new VBox(
                                new FxFooterRow(brand(), links(false, "About", "Privacy Policy", "Licensing", "Contact")),
                                new FxFooterDivider(),
                                copyright)
                };
            }
            case SOCIAL -> {
                FxFooterRow top = new FxFooterRow(brand(), new HBox(24,
                        column("about", "Flowbite", "Tailwind CSS"),
                        column("Follow us", "Github", "Discord"),
                        column("Legal", "Privacy Policy", "Terms & Conditions")));
                top.setVerticalAlignment(VPos.TOP);
                yield new Node[] {
                        new VBox(top, new FxFooterDivider(), new FxFooterRow(copyright, socialIcons()))
                };
            }
            case SITEMAP -> {
                GridPane grid = new GridPane();
                grid.setHgap(32);
                grid.setPadding(new Insets(32, 24, 32, 24));
                Node[] cols = {
                        column("Company", "About", "Careers", "Brand Center", "Blog"),
                        column("help center", "Discord Server", "Twitter", "Facebook", "Contact Us"),
                        column("legal", "Privacy Policy", "Licensing", "Terms & Conditions"),
                        column("download", "iOS", "Android", "Windows", "MacOS")
                };
                for (int i = 0; i < cols.length; i++) {
                    ColumnConstraints cc = new ColumnConstraints();
                    cc.setPercentWidth(25);
                    grid.getColumnConstraints().add(cc);
                    grid.add(cols[i], i, 0);
                }
                FxFooterRow bar = new FxFooterRow(copyright, socialIcons());
                // Inline: .px-4 / .py-6 both set -fx-padding, so they can't be combined.
                bar.setStyle("-fx-background-color: -fxk-gray-700; -fx-padding: 24px 16px 24px 16px;");
                yield new Node[] { new VBox(grid, bar) };
            }
        };
    }

    private FxFooterCopyright copyright(String by, String year, boolean link) {
        Integer y = null;
        try {
            y = year == null || year.isBlank() ? null : Integer.valueOf(year.trim());
        } catch (NumberFormatException ignored) {
            // leave null: an unparsable year is simply omitted
        }
        FxFooterCopyright c = new FxFooterCopyright(by, y);
        if (link) {
            c.setOnAction(e -> log("copyright link clicked"));
        }
        return c;
    }

    // ---- the four Flowbite examples -----------------------------------------------------------

    private VBox flowbiteExamples() {
        VBox box = new VBox(20);
        for (Preset p : Preset.values()) {
            FxFooter footer = new FxFooter(preset(p, new FxFooterCopyright("Flowbite™", 2022)));
            footer.setContainer(p != Preset.SITEMAP);
            footer.setBgDark(p == Preset.SITEMAP);
            box.getChildren().add(labeled(p.name().toLowerCase() + (p == Preset.SITEMAP ? " (bgDark)" : " (container)"), footer));
        }
        return box;
    }

    // ---- root flags ---------------------------------------------------------------------------

    private FlowPane rootFlags() {
        FlowPane flow = new FlowPane(12, 12);
        for (boolean container : new boolean[] { false, true }) {
            for (boolean bgDark : new boolean[] { false, true }) {
                FxFooter f = new FxFooter(
                        new FxFooterCopyright("Flowbite™", 2022),
                        links(false, "About", "Contact"));
                f.setContainer(container);
                f.setBgDark(bgDark);
                f.setPrefWidth(400);
                flow.getChildren().add(labeled("container=" + container + ", bgDark=" + bgDark, f));
            }
        }

        FxFooter toggling = new FxFooter(
                new FxFooterCopyright("Flowbite™", 2022),
                links(false, "About", "Contact"));
        toggling.setPrefWidth(400);
        Button c = new Button("Toggle container");
        c.setOnAction(e -> toggling.setContainer(!toggling.isContainer()));
        Button d = new Button("Toggle bgDark");
        d.setOnAction(e -> toggling.setBgDark(!toggling.isBgDark()));
        flow.getChildren().add(labeled("runtime flags", new VBox(6, toggling, new HBox(8, c, d))));
        return flow;
    }

    // ---- FxFooterRow layout -------------------------------------------------------------------

    private VBox rowLayouts() {
        VBox box = new VBox(12);

        box.getChildren().add(labeled("one child: fills the width (centered text proves it)",
                footerOf(new Node[] { centeredLabel("I fill the whole footer") })));

        box.getChildren().add(labeled("two children: space between",
                footerOf(new Node[] { new Label("left"), new Label("right") })));

        box.getChildren().add(labeled("three children: left, middle evenly spaced, right",
                footerOf(new Node[] { new Label("left"), new Label("middle"), new Label("right") })));

        // tall + short child: CENTER vs TOP
        for (VPos v : new VPos[] { VPos.CENTER, VPos.TOP }) {
            Label tall = new Label("tall\nchild\nhere");
            FxFooter f = footerOf(new Node[] { tall, new Label("short (" + v + ")") });
            f.setVerticalAlignment(v);
            box.getChildren().add(labeled("verticalAlignment = " + v, f));
        }
        return box;
    }

    private static FxFooter footerOf(Node[] items) {
        FxFooter f = new FxFooter(items);
        f.setContainer(true);
        return f;
    }

    // ---- squeeze test -------------------------------------------------------------------------

    private VBox squeezeTest() {
        FxFooter footer = new FxFooter(
                brand(),
                links(false, "About", "Privacy Policy", "Licensing", "Contact", "Careers", "Blog"),
                new FxFooterCopyright("Flowbite™", 2022));
        footer.setContainer(true);

        Slider slider = new Slider(200, 1000, 1000);
        slider.setPrefWidth(300);
        footer.maxWidthProperty().bind(slider.valueProperty());
        Label readout = new Label();
        readout.textProperty().bind(slider.valueProperty().asString("width: %.0f px"));
        readout.getStyleClass().addAll("text-sm", "text-muted");

        VBox holder = new VBox(footer);
        holder.setAlignment(Pos.TOP_LEFT);
        VBox box = new VBox(10, new HBox(12, slider, readout), holder);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- link groups --------------------------------------------------------------------------

    private FlowPane linkGroups() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("row", inFooter(links(false, "About", "Privacy Policy", "Licensing", "Contact"))));
        flow.getChildren().add(labeled("col", inFooter(links(true, "About", "Privacy Policy", "Licensing", "Contact"))));

        FxFooterLinkGroup many = links(false, "One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten", "Eleven", "Twelve");
        FxFooter narrow = inFooter(many);
        narrow.setPrefWidth(300);
        narrow.setMaxWidth(300);
        flow.getChildren().add(labeled("12 links in 300px: row wraps", narrow));

        FxFooterLinkGroup toggling = links(false, "About", "Careers", "Blog");
        Button toggle = new Button("Toggle col");
        toggle.setOnAction(e -> toggling.setCol(!toggling.isCol()));
        flow.getChildren().add(labeled("runtime col toggle", new VBox(6, inFooter(toggling), toggle)));

        FxFooterLinkGroup growing = links(false, "About");
        Button add = new Button("Add link");
        int[] n = {1};
        add.setOnAction(e -> growing.getChildren().add(new FxFooterLink("Link " + (++n[0]))));
        Button remove = new Button("Remove last");
        remove.setOnAction(e -> {
            if (!growing.getChildren().isEmpty()) {
                growing.getChildren().remove(growing.getChildren().size() - 1);
            }
        });
        flow.getChildren().add(labeled("add / remove at runtime",
                new VBox(6, inFooter(growing), new HBox(8, add, remove))));

        FxFooterLinkGroup empty = new FxFooterLinkGroup();
        flow.getChildren().add(labeled("empty group (no exception)", inFooter(empty)));
        return flow;
    }

    private static FxFooter inFooter(Node node) {
        FxFooter f = new FxFooter(node);
        f.setContainer(true);
        return f;
    }

    // ---- copyright ----------------------------------------------------------------------------

    private FlowPane copyrights() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("by + year", inFooter(new FxFooterCopyright("Flowbite™", 2022))));
        flow.getChildren().add(labeled("by only (no year)", inFooter(new FxFooterCopyright("Flowbite™"))));
        flow.getChildren().add(labeled("nothing set (just the mark)", inFooter(new FxFooterCopyright())));

        FxFooterCopyright link = new FxFooterCopyright("Flowbite™", 2022);
        link.setOnAction(e -> log("copyright 'by' link clicked"));
        flow.getChildren().add(labeled("by is a link (hover underlines)", inFooter(link)));

        FxFooterCopyright centered = new FxFooterCopyright("Flowbite™", 2022);
        centered.setAlignment(Pos.CENTER);
        FxFooter centeredFooter = inFooter(centered);
        centeredFooter.setPrefWidth(300);
        flow.getChildren().add(labeled("setAlignment(CENTER)", centeredFooter));

        FxFooterCopyright live = new FxFooterCopyright("Flowbite™", 2022);
        Button year = new Button("Year++");
        year.setOnAction(e -> live.setYear(live.getYear() == null ? 2022 : live.getYear() + 1));
        Button clearYear = new Button("Clear year");
        clearYear.setOnAction(e -> live.setYear(null));
        Button asLink = new Button("Toggle link");
        asLink.setOnAction(e -> live.setOnAction(live.getOnAction() == null ? ev -> log("live link clicked") : null));
        flow.getChildren().add(labeled("runtime changes",
                new VBox(6, inFooter(live), new HBox(8, year, clearYear, asLink))));
        return flow;
    }

    // ---- brand --------------------------------------------------------------------------------

    private FlowPane brands() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("text only", inFooter(new FxFooterBrand("Flowbite"))));
        flow.getChildren().add(labeled("FontIcon logo (setGraphic)", inFooter(brand())));

        FxFooterBrand raster = new FxFooterBrand("Raster logo", logoImage());
        flow.getChildren().add(labeled("raster logo (setImage, 32px high)", inFooter(raster)));

        FxFooterBrand clickable = brand();
        clickable.setOnAction(e -> log("brand clicked"));
        flow.getChildren().add(labeled("clickable (Tab, Enter, click)", inFooter(clickable)));

        FxFooterBrand live = new FxFooterBrand("Swap me");
        Button image = new Button("Toggle raster logo");
        image.setOnAction(e -> live.setImage(live.getImage() == null ? logoImage() : null));
        Button rename = new Button("Rename");
        rename.setOnAction(e -> live.setName("Name " + System.nanoTime() % 1000));
        flow.getChildren().add(labeled("runtime image / name",
                new VBox(6, inFooter(live), new HBox(8, image, rename))));

        FxFooterBrand wide = new FxFooterBrand("A very long brand name that goes on and on");
        FxFooter wideFooter = inFooter(wide);
        wideFooter.setPrefWidth(300);
        wideFooter.setMaxWidth(300);
        flow.getChildren().add(labeled("long name in 300px", wideFooter));
        return flow;
    }

    // ---- icons --------------------------------------------------------------------------------

    private FlowPane icons() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("social icons", inFooter(socialIcons())));

        FxFooter dark = inFooter(socialIcons());
        dark.setBgDark(true);
        flow.getChildren().add(labeled("same, bgDark", dark));

        FxFooterIcon swap = icon(FontAwesomeBrands.GITHUB, "GitHub");
        Button change = new Button("Swap icon");
        change.setOnAction(e -> swap.setIcon(swap.getIcon() == FontAwesomeBrands.GITHUB
                ? FontAwesomeBrands.TWITTER : FontAwesomeBrands.GITHUB));
        Button clear = new Button("setIcon(null)");
        clear.setOnAction(e -> swap.setIcon(null));
        flow.getChildren().add(labeled("runtime icon swap",
                new VBox(6, inFooter(swap), new HBox(8, change, clear))));
        return flow;
    }

    // ---- title and divider --------------------------------------------------------------------

    private FlowPane titlesAndDividers() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("titles (mixed case in, upper case out)", inFooter(new HBox(32,
                column("about", "Flowbite", "Tailwind CSS"),
                column("Follow Us", "Github", "Discord"),
                column("LEGAL", "Privacy Policy", "Terms & Conditions")))));

        FxFooterTitle live = new FxFooterTitle("initial");
        Button retitle = new Button("Change title");
        retitle.setOnAction(e -> live.setTitle("title " + System.nanoTime() % 1000));
        flow.getChildren().add(labeled("runtime title",
                new VBox(6, inFooter(new VBox(live, links(true, "A link"))), retitle)));

        FxFooter dividers = inFooter(new VBox(
                new Label("above"), new FxFooterDivider(), new Label("between"),
                new FxFooterDivider(), new Label("below")));
        dividers.setPrefWidth(300);
        flow.getChildren().add(labeled("dividers", dividers));
        return flow;
    }

    // ---- edge cases ---------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxFooter empty = new FxFooter();
        empty.setContainer(true);
        empty.setPrefWidth(300);
        flow.getChildren().add(labeled("empty footer (container)", empty));

        FxFooter bare = new FxFooter();
        bare.setPrefWidth(300);
        flow.getChildren().add(labeled("empty footer (no container: collapses to zero height)", bare));

        FxFooter longText = footerOf(new Node[] {
                new FxFooterCopyright("A deliberately long company name Incorporated and Sons Limited", 2022),
                links(false, "About", "Privacy Policy", "Licensing", "Contact") });
        longText.setPrefWidth(420);
        longText.setMaxWidth(420);
        flow.getChildren().add(labeled("long copyright + links in 420px", longText));

        FxFooter replace = footerOf(new Node[] { new Label("original child") });
        Button swap = new Button("Replace children");
        int[] n = {0};
        swap.setOnAction(e -> replace.getChildren().setAll(++n[0] % 2 == 0
                ? new Node[] { new Label("original child") }
                : new Node[] { new FxFooterCopyright("Flowbite™", 2022), links(false, "A", "B") }));
        flow.getChildren().add(labeled("children replaced at runtime", new VBox(6, replace, swap)));

        // Parts need an FxFooter ancestor for their colors (looked-up colors are inherited).
        VBox outside = new VBox(6, new FxFooterLink("link outside any footer"),
                new FxFooterCopyright("Outside", 2022));
        flow.getChildren().add(labeled("parts OUTSIDE a footer (unstyled by design; check no exception)", outside));

        FxFooter nested = footerOf(new Node[] { new VBox(8,
                new Label("nested footer:"),
                footerOf(new Node[] { new FxFooterCopyright("Inner", 2022), links(false, "A", "B") })) });
        flow.getChildren().add(labeled("footer inside footer", nested));
        return flow;
    }

    // ---- shared builders ----------------------------------------------------------------------

    private FxFooterBrand brand() {
        FxFooterBrand brand = new FxFooterBrand("Flowbite");
        brand.setGraphic(new FontIcon(LOGO_ICON));
        return brand;
    }

    private FxFooterLinkGroup links(boolean col, String... labels) {
        FxFooterLinkGroup group = new FxFooterLinkGroup(col);
        for (String label : labels) {
            group.getChildren().add(new FxFooterLink(label, e -> log("link clicked: " + label)));
        }
        return group;
    }

    private VBox column(String title, String... labels) {
        return new VBox(new FxFooterTitle(title), links(true, labels));
    }

    private HBox socialIcons() {
        HBox icons = new HBox(24,
                icon(FontAwesomeBrands.FACEBOOK, "Facebook"),
                icon(FontAwesomeBrands.INSTAGRAM, "Instagram"),
                icon(FontAwesomeBrands.TWITTER, "Twitter"),
                icon(FontAwesomeBrands.GITHUB, "GitHub"),
                icon(FontAwesomeBrands.DRIBBBLE, "Dribbble"));
        icons.setAlignment(Pos.CENTER);
        return icons;
    }

    private FxFooterIcon icon(Ikon ikon, String label) {
        FxFooterIcon icon = new FxFooterIcon(ikon);
        icon.setAccessibleText(label);
        icon.setOnAction(e -> log("icon clicked: " + label));
        return icon;
    }

    /** A 64x64 rounded-square PNG-like logo drawn in code, so the test needs no image file. */
    private static Image logoImage() {
        int size = 64;
        WritableImage image = new WritableImage(size, size);
        PixelWriter pw = image.getPixelWriter();
        Color fill = Color.web("#2563eb");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = Math.max(0, Math.abs(x - size / 2.0 + 0.5) - (size / 2.0 - 12));
                double dy = Math.max(0, Math.abs(y - size / 2.0 + 0.5) - (size / 2.0 - 12));
                pw.setColor(x, y, Math.hypot(dx, dy) <= 12 ? fill : Color.TRANSPARENT);
            }
        }
        return image;
    }

    private static Label centeredLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
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