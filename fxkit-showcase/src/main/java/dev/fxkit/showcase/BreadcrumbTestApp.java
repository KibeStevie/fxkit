package dev.fxkit.showcase;

import java.util.ArrayList;
import java.util.List;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.breadcrumb.FxBreadcrumb;
import dev.fxkit.core.components.breadcrumb.FxBreadcrumbItem;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxBreadcrumb} and {@link FxBreadcrumbItem}: the two Flowbite
 * examples (default, solid background), icons, link vs current-page items, keyboard focus, a
 * clickable trail that truncates itself, a live playground, and edge cases.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check the breadcrumbs in light and dark.
 */
public class BreadcrumbTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (item clicks)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Push/pop crumbs, toggle the solid background and the home icon. The previous "
                                + "last crumb becomes a link when a new one is pushed."),
                playground(),

                sectionHeading("Default breadcrumb",
                        "Flowbite example 1: icon on Home, Projects is a link, the last crumb is the "
                                + "current page (muted, not clickable)."),
                labeled("default", defaultBreadcrumb(false)),

                sectionHeading("Background color",
                        "Flowbite example 2: solidBackground = true (py-3 px-5, surface-alt fill)."),
                labeled("solid background", defaultBreadcrumb(true)),

                sectionHeading("Link vs current page",
                        "Hover the links (text turns primary), Tab through them (focus ring, no layout "
                                + "shift). The current page is skipped by Tab and ignores the mouse."),
                linkVsCurrent(),

                sectionHeading("Icons",
                        "Icon follows the item's text color, including on hover. Also: icon only, "
                                + "icon on every item, and a runtime icon toggle."),
                iconSection(),

                sectionHeading("Clickable trail",
                        "Click any crumb to go 'back' to it: the trail truncates and the clicked crumb "
                                + "becomes the current page."),
                trailSection(),

                sectionHeading("Edge cases",
                        "Empty, single item, very long trail in a narrow box, blank text, action "
                                + "toggled at runtime."),
                edgeCases(),

                sectionHeading("Event log", "Everything the item actions report lands here."),
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

        stage.setTitle("FxBreadcrumb test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxBreadcrumb test harness");
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
        FxBreadcrumb crumbs = new FxBreadcrumb(
                new FxBreadcrumbItem("Home", e -> log("playground: Home clicked")),
                new FxBreadcrumbItem("Projects", e -> log("playground: Projects clicked")),
                new FxBreadcrumbItem("Flowbite React"));

        CheckBox solidCheck = new CheckBox("Solid background");
        crumbs.solidBackgroundProperty().bind(solidCheck.selectedProperty());

        CheckBox homeIconCheck = new CheckBox("Icon on first crumb");
        homeIconCheck.selectedProperty().addListener((o, was, is) -> {
            if (!crumbs.getItems().isEmpty()) {
                crumbs.getItems().get(0).setIcon(is ? ICON : null);
            }
        });

        int[] counter = {0};
        Button push = new Button("Push crumb");
        push.setOnAction(e -> {
            List<FxBreadcrumbItem> items = crumbs.getItems();
            if (!items.isEmpty()) {
                FxBreadcrumbItem previousLast = items.get(items.size() - 1);
                String name = previousLast.getText();
                previousLast.setOnAction(ev -> log("playground: " + name + " clicked"));
            }
            counter[0]++;
            items.add(new FxBreadcrumbItem("Level " + counter[0]));
        });

        Button pop = new Button("Pop crumb");
        pop.setOnAction(e -> {
            List<FxBreadcrumbItem> items = crumbs.getItems();
            if (items.isEmpty()) {
                return;
            }
            items.remove(items.size() - 1);
            if (!items.isEmpty()) {
                items.get(items.size() - 1).setOnAction(null); // new last = current page
            }
        });

        Button clear = new Button("Clear");
        clear.setOnAction(e -> crumbs.getItems().clear());

        HBox controls = new HBox(16, solidCheck, homeIconCheck, push, pop, clear);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, controls, crumbs);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- Flowbite examples -------------------------------------------------------------------

    private FxBreadcrumb defaultBreadcrumb(boolean solid) {
        FxBreadcrumb crumbs = new FxBreadcrumb(
                new FxBreadcrumbItem("Home", ICON, e -> log("Home clicked")),
                new FxBreadcrumbItem("Projects", e -> log("Projects clicked")),
                new FxBreadcrumbItem("Flowbite React"));
        crumbs.setAccessibleText(solid ? "Solid background breadcrumb example" : "Default breadcrumb example");
        crumbs.setSolidBackground(solid);
        return crumbs;
    }

    // ---- link vs current ---------------------------------------------------------------------

    private VBox linkVsCurrent() {
        FxBreadcrumb links = new FxBreadcrumb(
                new FxBreadcrumbItem("Link A", e -> log("Link A")),
                new FxBreadcrumbItem("Link B", e -> log("Link B")),
                new FxBreadcrumbItem("Link C", e -> log("Link C")),
                new FxBreadcrumbItem("Current page"));

        FxBreadcrumb allCurrent = new FxBreadcrumb(
                new FxBreadcrumbItem("No"),
                new FxBreadcrumbItem("action"),
                new FxBreadcrumbItem("anywhere"));

        VBox box = new VBox(10,
                labeled("3 links + current page (Tab should stop 3 times)", links),
                labeled("no actions at all (nothing focusable, nothing hoverable)", allCurrent));
        return box;
    }

    // ---- icons -------------------------------------------------------------------------------

    private VBox iconSection() {
        FxBreadcrumb iconOnly = new FxBreadcrumb(
                new FxBreadcrumbItem(null, ICON, e -> log("icon-only Home clicked")),
                new FxBreadcrumbItem("Projects", e -> log("Projects clicked")),
                new FxBreadcrumbItem("Current"));

        FxBreadcrumb everyItem = new FxBreadcrumb(
                new FxBreadcrumbItem("Home", ICON, e -> log("Home clicked")),
                new FxBreadcrumbItem("Projects", ICON, e -> log("Projects clicked")),
                new FxBreadcrumbItem("Current", ICON));

        FxBreadcrumbItem toggling = new FxBreadcrumbItem("Toggle me", e -> log("Toggle me clicked"));
        FxBreadcrumb runtime = new FxBreadcrumb(toggling, new FxBreadcrumbItem("Current"));
        Button toggle = new Button("Toggle icon");
        toggle.setOnAction(e -> toggling.setIcon(toggling.getIcon() == null ? ICON : null));

        return new VBox(10,
                labeled("icon only on the first crumb (no text)", iconOnly),
                labeled("icon on every crumb, including the current page", everyItem),
                labeled("runtime icon toggle", new VBox(6, runtime, toggle)));
    }

    // ---- clickable trail ---------------------------------------------------------------------

    private VBox trailSection() {
        List<String> full = List.of("Home", "Projects", "FXKit", "Components", "Breadcrumb");
        FxBreadcrumb crumbs = new FxBreadcrumb();
        crumbs.setSolidBackground(true);
        renderTrail(crumbs, new ArrayList<>(full));

        Button reset = new Button("Reset trail");
        reset.setOnAction(e -> {
            renderTrail(crumbs, new ArrayList<>(full));
            log("trail: reset");
        });

        return new VBox(10, labeled("click a crumb to go back to it", new VBox(6, crumbs, reset)));
    }

    /** Rebuilds the trail: every crumb but the last is a link that truncates the path to itself. */
    private void renderTrail(FxBreadcrumb crumbs, List<String> path) {
        List<FxBreadcrumbItem> items = new ArrayList<>();
        int last = path.size() - 1;
        for (int i = 0; i < path.size(); i++) {
            String name = path.get(i);
            if (i == last) {
                items.add(new FxBreadcrumbItem(name));
            } else {
                int index = i;
                items.add(new FxBreadcrumbItem(name, e -> {
                    log("trail: navigated to " + name);
                    renderTrail(crumbs, new ArrayList<>(path.subList(0, index + 1)));
                }));
            }
        }
        crumbs.getItems().setAll(items);
    }

    // ---- edge cases --------------------------------------------------------------------------

    private VBox edgeCases() {
        FxBreadcrumb empty = new FxBreadcrumb();
        empty.setSolidBackground(true);

        FxBreadcrumb single = new FxBreadcrumb(new FxBreadcrumbItem("Only one, no chevron"));

        FxBreadcrumb longTrail = new FxBreadcrumb();
        for (int i = 1; i <= 12; i++) {
            longTrail.getItems().add(i == 12
                    ? new FxBreadcrumbItem("Current")
                    : new FxBreadcrumbItem("Level " + i, e -> log("long trail click")));
        }
        VBox narrow = new VBox(longTrail);
        narrow.setMaxWidth(360);
        narrow.setPrefWidth(360);

        FxBreadcrumb blank = new FxBreadcrumb(
                new FxBreadcrumbItem("Home", e -> log("Home clicked")),
                new FxBreadcrumbItem(""),
                new FxBreadcrumbItem("After a blank crumb"));

        FxBreadcrumbItem flip = new FxBreadcrumbItem("Flip me", e -> log("Flip me clicked"));
        FxBreadcrumb runtimeAction = new FxBreadcrumb(new FxBreadcrumbItem("Home", e -> log("Home clicked")), flip);
        Button flipButton = new Button("Toggle action on last crumb");
        flipButton.setOnAction(e -> {
            flip.setOnAction(flip.isInteractive() ? null : ev -> log("Flip me clicked"));
            log("flip: interactive = " + flip.isInteractive());
        });

        return new VBox(10,
                labeled("empty + solid background (should be just the padding)", empty),
                labeled("single item (no chevron)", single),
                labeled("12 crumbs in a 360px box (row clips instead of wrapping - decide if you want a scroll/ellipsis)", narrow),
                labeled("blank text crumb in the middle (chevrons still render)", blank),
                labeled("action toggled at runtime (link <-> current page)", new VBox(6, runtimeAction, flipButton)));
    }

    // ---- helpers -----------------------------------------------------------------------------

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
