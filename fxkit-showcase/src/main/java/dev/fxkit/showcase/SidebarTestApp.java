package dev.fxkit.showcase;

import java.util.List;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.badge.FxBadge;
import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.sidebar.FxSidebar;
import dev.fxkit.core.components.sidebar.FxSidebarCollapse;
import dev.fxkit.core.components.sidebar.FxSidebarCta;
import dev.fxkit.core.components.sidebar.FxSidebarItem;
import dev.fxkit.core.components.sidebar.FxSidebarItemGroup;
import dev.fxkit.core.components.sidebar.FxSidebarLogo;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxSidebar} and its parts ({@link FxSidebarItem},
 * {@link FxSidebarCollapse}, {@link FxSidebarItemGroup}, {@link FxSidebarCta},
 * {@link FxSidebarLogo}): a live playground, the six Flowbite examples, expanded vs collapsed,
 * item states and all 17 label colors, all CTA colors, logo variants, runtime add/remove, and
 * edge cases (scrolling, ellipsis, moving an item between sidebars, replacing handlers).
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check light and dark. Tab through a sidebar to check focus
 * rings; Enter/Space activate the focused row.
 */
public class SidebarTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final double SIDEBAR_HEIGHT = 480;

    private Scene scene;
    private FxButton themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (item actions, logo clicks, handler tests)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: collapse the sidebar, toggle logo / second group / CTA, "
                                + "recolor the CTA, and edit the probe item and the collapse."),
                playground(),

                sectionHeading("Flowbite examples",
                        "The six examples from the Flowbite React docs, in order. Default sidebar: click an "
                                + "item to make it the active one."),
                flowbiteExamples(),

                sectionHeading("Expanded vs collapsed",
                        "Collapsed: 64px rail, icon only, no badges, no chevrons, tooltip on hover, "
                                + "icon-less rows show their first letter in bold, logo keeps only its image."),
                expandedVsCollapsed(),

                sectionHeading("Item states",
                        "Normal, active, disabled, disabled + active, label, no icon, icon only, long text. "
                                + "Disabled rows must not fire or show a hover fill."),
                itemStates(),

                sectionHeading("Item label colors (all 17)",
                        "FxBadge colors on the item label. 30+ rows also check that the sidebar scrolls."),
                labelColors(),

                sectionHeading("CTA colors (all 12)",
                        "Each CTA sits in a sidebar to check it against the real background. DARK and LIGHT "
                                + "are FXKit's own (Flowbite's reference missing shades)."),
                ctaColors(),

                sectionHeading("Logo",
                        "Text only, image + text, image only, clickable (focusable, hand cursor), long text, "
                                + "and the same logo in a collapsed sidebar."),
                logoVariants(),

                sectionHeading("Runtime changes",
                        "Add / remove items and groups. The first group must never show a divider: after "
                                + "'Remove first group' the next one loses its line, after 'Prepend group' the "
                                + "old first one gains it."),
                dynamicSection(),

                sectionHeading("Edge cases",
                        "Empty sidebar, scrolling, ellipsis, an item moved between sidebars (collapsed state "
                                + "must follow), a standalone item, replacing an onAction handler."),
                edgeCases(),

                sectionHeading("Event log", "Everything the actions report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1180, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxSidebar test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxSidebar test harness");
        title.getStyleClass().addAll("text-xl", "font-bold", "text-body");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        themeToggle = new FxButton();
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

    private HBox playground() {
        FxSidebarLogo logo = new FxSidebarLogo("Playground");
        logo.setImage(logoImage(Color.web("#3b82f6")));
        logo.setImageAlt("Playground logo");
        logo.setOnAction(e -> log("playground: logo clicked"));

        FxSidebarItem probe = new FxSidebarItem("Probe item", ICON);
        probe.setLabel("3");
        probe.setOnAction(e -> log("playground: probe fired"));

        FxSidebarCollapse collapse = new FxSidebarCollapse("Collapse", ICON);
        collapse.getContent().addAll(item("Nested one"), item("Nested two"), item("Nested three"));

        FxSidebarItemGroup group1 = new FxSidebarItemGroup(probe, item("Inbox", ICON), collapse);
        FxSidebarItemGroup group2 = new FxSidebarItemGroup(item("Help", ICON), item("Documentation", ICON));

        FxSidebarCta cta = new FxSidebarCta();
        cta.getChildren().addAll(
                FxSidebarCta.header(new FxBadge("Beta", FxBadge.Color.WARNING), () -> log("playground: CTA closed")),
                FxSidebarCta.text("Preview the new navigation! You can turn it off in your profile."),
                FxSidebarCta.link("Turn new navigation off", () -> log("playground: CTA link")));

        FxSidebar sidebar = sidebar("Playground sidebar", logo, group1, group2, cta);

        // -- sidebar-level controls
        CheckBox collapsedCheck = new CheckBox("Collapsed");
        collapsedCheck.selectedProperty().bindBidirectional(sidebar.collapsedProperty());
        CheckBox logoCheck = new CheckBox("Logo");
        CheckBox groupCheck = new CheckBox("Second group (divider)");
        CheckBox ctaCheck = new CheckBox("CTA");
        for (CheckBox c : List.of(logoCheck, groupCheck, ctaCheck)) {
            c.setSelected(true);
        }
        Runnable rebuild = () -> {
            sidebar.getContent().clear();
            if (logoCheck.isSelected()) {
                sidebar.getContent().add(logo);
            }
            sidebar.getContent().add(group1);
            if (groupCheck.isSelected()) {
                sidebar.getContent().add(group2);
            }
            if (ctaCheck.isSelected()) {
                sidebar.getContent().add(cta);
            }
        };
        for (CheckBox c : List.of(logoCheck, groupCheck, ctaCheck)) {
            c.selectedProperty().addListener((o, was, is) -> rebuild.run());
        }

        TextField logoText = new TextField(logo.getText());
        logo.textProperty().bind(logoText.textProperty());

        ComboBox<FxSidebarCta.Color> ctaColor = new ComboBox<>();
        ctaColor.getItems().addAll(FxSidebarCta.Color.values());
        ctaColor.valueProperty().bindBidirectional(cta.colorProperty());

        // -- probe item controls
        TextField probeText = new TextField(probe.getText());
        probe.textProperty().bind(probeText.textProperty());
        TextField probeLabel = new TextField(probe.getLabel());
        probe.labelProperty().bind(probeLabel.textProperty());

        ComboBox<FxBadge.Color> labelColor = new ComboBox<>();
        labelColor.getItems().addAll(FxBadge.Color.values());
        labelColor.valueProperty().bindBidirectional(probe.labelColorProperty());

        CheckBox iconCheck = new CheckBox("Icon");
        iconCheck.setSelected(true);
        iconCheck.selectedProperty().addListener((o, was, is) -> probe.setIcon(is ? ICON : null));
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.selectedProperty().bindBidirectional(probe.activeProperty());
        CheckBox disabledCheck = new CheckBox("Disabled");
        disabledCheck.selectedProperty().bindBidirectional(probe.disableProperty());

        // -- collapse controls
        CheckBox openCheck = new CheckBox("Open");
        openCheck.selectedProperty().bindBidirectional(collapse.openProperty());
        ComboBox<String> chevronMode = new ComboBox<>();
        chevronMode.getItems().addAll("Default arrow", "Custom icon (rotates)", "Factory (+ / -)");
        chevronMode.getSelectionModel().selectFirst();
        chevronMode.valueProperty().addListener((o, was, mode) -> {
            collapse.setChevronIcon(null);
            collapse.setChevronFactory(null);
            if ("Custom icon (rotates)".equals(mode)) {
                collapse.setChevronIcon(ICON);
            } else if ("Factory (+ / -)".equals(mode)) {
                collapse.setChevronFactory(open -> new Label(open ? "\u2212" : "+"));
            }
        });

        VBox controls = new VBox(12,
                controlRow("Sidebar", collapsedCheck, logoCheck, groupCheck, ctaCheck),
                controlRow("Logo text", logoText, new Label("CTA color"), ctaColor),
                controlRow("Probe", new Label("text"), probeText, new Label("label"), probeLabel),
                controlRow("Probe", new Label("labelColor"), labelColor, iconCheck, activeCheck, disabledCheck),
                controlRow("Collapse", openCheck, new Label("chevron"), chevronMode));
        controls.setPrefWidth(560);
        HBox.setHgrow(controls, Priority.ALWAYS);

        HBox box = new HBox(24, controls, sidebar);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the six Flowbite examples ----------------------------------------------------------

    private FlowPane flowbiteExamples() {
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("Default sidebar", defaultExample()),
                labeled("Multi-level dropdown", dropdownExample(false)),
                labeled("Custom chevron (+ / -)", dropdownExample(true)),
                labeled("Content separator", separatorExample()),
                labeled("Sidebar with button", ctaExample()),
                labeled("Sidebar with logo", logoExample()));
        return flow;
    }

    private FxSidebar defaultExample() {
        FxSidebarItem dashboard = item("Dashboard", ICON);
        FxSidebarItem kanban = item("Kanban", ICON);
        kanban.setLabel("Pro");
        kanban.setLabelColor(FxBadge.Color.DARK);
        FxSidebarItem inbox = item("Inbox", ICON);
        inbox.setLabel("3");
        List<FxSidebarItem> items = List.of(dashboard, kanban, inbox, item("Users", ICON),
                item("Products", ICON), item("Sign In", ICON), item("Sign Up", ICON));
        for (FxSidebarItem it : items) {
            it.setOnAction(e -> {
                items.forEach(other -> other.setActive(other == it));
                log("active: " + it.getText());
            });
        }
        dashboard.setActive(true);
        return sidebar("Default sidebar example", new FxSidebarItemGroup(items.toArray(FxSidebarItem[]::new)));
    }

    private FxSidebar dropdownExample(boolean plusMinus) {
        FxSidebarCollapse ecommerce = ecommerce();
        if (plusMinus) {
            ecommerce.setChevronFactory(open -> new Label(open ? "\u2212" : "+"));
        }
        return sidebar(plusMinus ? "Custom chevron example" : "Multi-level dropdown example",
                new FxSidebarItemGroup(item("Dashboard", ICON), ecommerce, item("Inbox", ICON),
                        item("Users", ICON), item("Products", ICON), item("Sign In", ICON),
                        item("Sign Up", ICON)));
    }

    private FxSidebar separatorExample() {
        return sidebar("Content separator example",
                new FxSidebarItemGroup(item("Dashboard", ICON), item("Kanban", ICON), item("Inbox", ICON),
                        item("Users", ICON), item("Products", ICON), item("Sign In", ICON),
                        item("Sign Up", ICON)),
                new FxSidebarItemGroup(item("Upgrade to Pro", ICON), item("Documentation", ICON),
                        item("Help", ICON)));
    }

    private FxSidebar ctaExample() {
        FxSidebar sidebar = sidebar("Call to action example",
                new FxSidebarItemGroup(item("Dashboard", ICON), item("Kanban", ICON), item("Inbox", ICON),
                        item("Users", ICON), item("Products", ICON), item("Sign In", ICON),
                        item("Sign Up", ICON)));
        FxSidebarCta cta = betaCta(() -> log("beta CTA closed"));
        sidebar.getContent().add(cta);
        return sidebar;
    }

    private FxSidebar logoExample() {
        FxSidebarLogo logo = new FxSidebarLogo("Flowbite");
        logo.setImage(logoImage(Color.web("#06b6d4")));
        logo.setImageAlt("Flowbite logo");
        logo.setOnAction(e -> log("logo clicked"));
        return sidebar("Logo branding example", logo,
                new FxSidebarItemGroup(item("Dashboard", ICON), item("Kanban", ICON), item("Inbox", ICON),
                        item("Users", ICON), item("Products", ICON), item("Sign In", ICON),
                        item("Sign Up", ICON)));
    }

    // ---- expanded vs collapsed --------------------------------------------------------------

    private FlowPane expandedVsCollapsed() {
        FxSidebar expanded = richSidebar();
        FxSidebar collapsed = richSidebar();
        collapsed.setCollapsed(true);

        FxSidebar bound = richSidebar();
        FxButton toggle = button("Toggle collapsed", bound::toggleCollapsed);

        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().addAll(
                labeled("expanded", expanded),
                labeled("collapsed", collapsed),
                labeled("toggled live (nested collapse stays toggleable)", new VBox(8, bound, toggle)));
        return flow;
    }

    /** Logo, icon items with labels, icon-less items, an open collapse, and a CTA. */
    private FxSidebar richSidebar() {
        FxSidebarLogo logo = new FxSidebarLogo("Acme");
        logo.setImage(logoImage(Color.web("#a855f7")));

        FxSidebarItem inbox = item("Inbox", ICON);
        inbox.setLabel("3");
        FxSidebarItem kanban = item("Kanban", ICON);
        kanban.setLabel("Pro");
        kanban.setLabelColor(FxBadge.Color.DARK);

        FxSidebarCollapse shop = ecommerce();
        shop.setOpen(true);

        FxSidebarCollapse noIcon = new FxSidebarCollapse("Reports");
        noIcon.getContent().addAll(item("Weekly"), item("Monthly"));

        FxSidebarCta cta = new FxSidebarCta();
        cta.getChildren().addAll(FxSidebarCta.text("A CTA is not hidden when the sidebar collapses."));

        return sidebar("Rich sidebar", logo,
                new FxSidebarItemGroup(item("Dashboard", ICON), kanban, inbox, shop),
                new FxSidebarItemGroup(item("Settings"), item("Profile"), noIcon),
                cta);
    }

    // ---- item states ------------------------------------------------------------------------

    private FlowPane itemStates() {
        FxSidebarItem active = item("Active", ICON);
        active.setActive(true);
        FxSidebarItem disabled = item("Disabled (must not fire)", ICON);
        disabled.setDisable(true);
        FxSidebarItem disabledActive = item("Disabled + active", ICON);
        disabledActive.setActive(true);
        disabledActive.setDisable(true);
        FxSidebarItem withLabel = item("With label", ICON);
        withLabel.setLabel("New");
        withLabel.setLabelColor(FxBadge.Color.SUCCESS);
        FxSidebarItem iconOnly = new FxSidebarItem(null, ICON);
        iconOnly.setOnAction(e -> log("icon-only item fired"));
        FxSidebarItem longText = item("A deliberately long item text that must not push the badge off", ICON);
        longText.setLabel("99+");

        FxSidebar expanded = sidebar("Item states", new FxSidebarItemGroup(
                item("Normal", ICON), active, disabled, disabledActive, withLabel,
                item("No icon at all"), iconOnly, longText));

        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().add(labeled("expanded", expanded));
        return flow;
    }

    // ---- label colors -----------------------------------------------------------------------

    private FlowPane labelColors() {
        FxSidebarItemGroup group = new FxSidebarItemGroup();
        for (FxBadge.Color c : FxBadge.Color.values()) {
            FxSidebarItem it = item(c.name(), ICON);
            it.setLabel(c.name().toLowerCase());
            it.setLabelColor(c);
            group.getChildren().add(it);
        }
        FlowPane flow = new FlowPane(16, 16);
        flow.getChildren().add(labeled("17 label colors (scrolls)", sidebar("Label colors", group)));
        return flow;
    }

    // ---- CTA colors -------------------------------------------------------------------------

    private FlowPane ctaColors() {
        FlowPane flow = new FlowPane(16, 16);
        for (FxSidebarCta.Color c : FxSidebarCta.Color.values()) {
            FxSidebarCta cta = betaCta(null);
            cta.setColor(c);
            FxSidebar host = sidebar(c.name() + " CTA", cta);
            host.setPrefHeight(230);
            flow.getChildren().add(labeled(c.name(), host));
        }
        return flow;
    }

    // ---- logo -------------------------------------------------------------------------------

    private FlowPane logoVariants() {
        FxSidebarLogo textOnly = new FxSidebarLogo("Text only");

        FxSidebarLogo both = new FxSidebarLogo("Image + text");
        both.setImage(logoImage(Color.web("#22c55e")));
        both.setImageAlt("Green dot");

        FxSidebarLogo imageOnly = new FxSidebarLogo();
        imageOnly.setImage(logoImage(Color.web("#ef4444")));

        FxSidebarLogo clickable = new FxSidebarLogo("Clickable");
        clickable.setImage(logoImage(Color.web("#eab308")));
        clickable.setOnAction(e -> log("clickable logo fired"));

        FxSidebarLogo longText = new FxSidebarLogo("A very long company name indeed");
        longText.setImage(logoImage(Color.web("#6366f1")));

        FxSidebarLogo inCollapsed = new FxSidebarLogo("Collapsed");
        inCollapsed.setImage(logoImage(Color.web("#ec4899")));
        FxSidebar collapsedHost = sidebar("Collapsed logo", inCollapsed);
        collapsedHost.setCollapsed(true);

        FlowPane flow = new FlowPane(16, 16);
        for (Node n : List.of(textOnly, both, imageOnly, clickable, longText)) {
            FxSidebar host = sidebar("Logo variant", n);
            host.setPrefHeight(100);
            flow.getChildren().add(labeled(((FxSidebarLogo) n).getText() == null ? "image only"
                    : ((FxSidebarLogo) n).getText(), host));
        }
        collapsedHost.setPrefHeight(100);
        flow.getChildren().add(labeled("in a collapsed sidebar", collapsedHost));
        return flow;
    }

    // ---- runtime changes --------------------------------------------------------------------

    private VBox dynamicSection() {
        FxSidebarItemGroup first = new FxSidebarItemGroup(item("Alpha", ICON), item("Beta", ICON));
        FxSidebar sidebar = sidebar("Dynamic sidebar", first);
        sidebar.setPrefHeight(420);

        int[] counter = {0};
        FlowPane buttons = new FlowPane(8, 8,
                button("Add item to first group", () ->
                        first.getChildren().add(item("Item " + (++counter[0]), ICON))),
                button("Remove last item", () -> {
                    if (!first.getChildren().isEmpty()) {
                        first.getChildren().remove(first.getChildren().size() - 1);
                    }
                }),
                button("Add collapse to first group", () -> {
                    FxSidebarCollapse c = new FxSidebarCollapse("Collapse " + (++counter[0]), ICON);
                    c.getContent().addAll(item("Child A"), item("Child B"));
                    first.getChildren().add(c);
                }),
                button("Append group", () -> sidebar.getContent().add(newGroup(++counter[0]))),
                button("Prepend group", () -> sidebar.getContent().add(0, newGroup(++counter[0]))),
                button("Remove first group", () -> sidebar.getContent().stream()
                        .filter(n -> n instanceof FxSidebarItemGroup).findFirst()
                        .ifPresent(n -> sidebar.getContent().remove(n))),
                button("Toggle collapsed", sidebar::toggleCollapsed));
        buttons.setPrefWrapLength(420);

        HBox box = new HBox(24, sidebar, buttons);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return new VBox(box);
    }

    private FxSidebarItemGroup newGroup(int n) {
        return new FxSidebarItemGroup(item("Group " + n + " - one", ICON), item("Group " + n + " - two", ICON));
    }

    // ---- edge cases -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(16, 16);

        // empty
        FxSidebar empty = sidebar("Empty sidebar");
        empty.setPrefHeight(120);
        flow.getChildren().add(labeled("empty sidebar", empty));

        // scrolling
        FxSidebarItemGroup many = new FxSidebarItemGroup();
        for (int i = 1; i <= 30; i++) {
            many.getChildren().add(item("Row " + i, ICON));
        }
        FxSidebar scrolling = sidebar("Scrolling sidebar", new FxSidebarLogo("Scroll me"), many);
        scrolling.setPrefHeight(300);
        flow.getChildren().add(labeled("30 rows scroll, no horizontal bar", scrolling));

        // moving an item between sidebars
        FxSidebarItem probe = item("Move me", ICON);
        probe.setLabel("new");
        FxSidebarItemGroup groupA = new FxSidebarItemGroup(item("A1", ICON), probe);
        FxSidebarItemGroup groupB = new FxSidebarItemGroup(item("B1", ICON));
        FxSidebar a = sidebar("Sidebar A (expanded)", groupA);
        FxSidebar b = sidebar("Sidebar B (collapsed)", groupB);
        b.setCollapsed(true);
        a.setPrefHeight(260);
        b.setPrefHeight(260);
        Label state = new Label();
        state.getStyleClass().addAll("text-xs", "text-muted");
        state.textProperty().bind(Bindings.when(probe.collapsedProperty())
                .then("probe.collapsed = true").otherwise("probe.collapsed = false"));
        FxButton move = button("Move probe to the other sidebar", () ->
                (probe.getParent() == groupA ? groupB : groupA).getChildren().add(probe));
        flow.getChildren().add(labeled("move an item: collapsed state must follow",
                new VBox(8, new HBox(12, a, b), state, move)));

        // standalone
        FxSidebarItem standalone = item("Standalone item (no sidebar)", ICON);
        standalone.setLabel("ok");
        VBox standaloneBox = new VBox(standalone);
        standaloneBox.setPrefWidth(256);
        flow.getChildren().add(labeled("outside any sidebar: looks expanded", standaloneBox));

        // handler replacement
        FxSidebarItem swap = new FxSidebarItem("Click me", ICON);
        swap.setOnAction(e -> log("handler v0"));
        int[] gen = {0};
        FxSidebar swapHost = sidebar("Handler test", new FxSidebarItemGroup(swap));
        swapHost.setPrefHeight(100);
        VBox handlerBox = new VBox(8, swapHost,
                button("Replace handler", () -> {
                    String name = "handler v" + (++gen[0]);
                    swap.setOnAction(e -> log(name));
                    log("replaced -> " + name + " (a click must log only this one)");
                }),
                button("Clear handler", () -> swap.setOnAction(null)),
                button("fire() programmatically", swap::fire));
        flow.getChildren().add(labeled("onAction replacement", handlerBox));

        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------

    private FxSidebarCollapse ecommerce() {
        FxSidebarCollapse c = new FxSidebarCollapse("E-commerce", ICON);
        c.getContent().addAll(item("Products"), item("Sales"), item("Refunds"), item("Shipping"));
        return c;
    }

    private FxSidebarCta betaCta(Runnable onClose) {
        FxSidebarCta cta = new FxSidebarCta();
        cta.getChildren().addAll(
                FxSidebarCta.header(new FxBadge("Beta", FxBadge.Color.WARNING), onClose),
                FxSidebarCta.text("Preview the new dashboard navigation! You can turn it off in your profile."),
                FxSidebarCta.link("Turn new navigation off", () -> log("CTA link clicked")));
        return cta;
    }

    private FxSidebarItem item(String text) {
        return item(text, null);
    }

    /** An item that logs when fired. */
    private FxSidebarItem item(String text, Ikon icon) {
        FxSidebarItem it = new FxSidebarItem(text, icon);
        it.setOnAction(e -> log("fired: " + text));
        return it;
    }

    private FxSidebar sidebar(String accessibleText, Node... content) {
        FxSidebar sidebar = new FxSidebar(content);
        sidebar.setAccessibleText(accessibleText);
        sidebar.setPrefHeight(SIDEBAR_HEIGHT);
        return sidebar;
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxButton button(String text, Runnable action) {
        FxButton b = new FxButton(text);
        b.setVariant(FxButton.Variant.SECONDARY);
        b.setSize(FxButton.Size.SM);
        b.setOnAction(e -> action.run());
        return b;
    }

    private static HBox controlRow(String caption, Node... nodes) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        label.setMinWidth(64);
        HBox row = new HBox(10, label);
        row.getChildren().addAll(nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Image logoImage(Color color) {
        WritableImage image = new WritableImage(32, 32);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                double dx = x - 15.5;
                double dy = y - 15.5;
                writer.setColor(x, y, dx * dx + dy * dy <= 15 * 15 ? color : Color.TRANSPARENT);
            }
        }
        return image;
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
