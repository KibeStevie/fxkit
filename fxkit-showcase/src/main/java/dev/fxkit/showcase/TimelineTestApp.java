package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.timeline.FxTimeline;
import dev.fxkit.core.components.timeline.FxTimelineBody;
import dev.fxkit.core.components.timeline.FxTimelineContent;
import dev.fxkit.core.components.timeline.FxTimelineItem;
import dev.fxkit.core.components.timeline.FxTimelinePoint;
import dev.fxkit.core.components.timeline.FxTimelineTime;
import dev.fxkit.core.components.timeline.FxTimelineTitle;
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
 * Isolated test harness for {@link FxTimeline}: the three Flowbite examples (default, vertical with
 * icons, horizontal), point variants, content variants, runtime changes (orientation, items, icons) and
 * edge cases (single item, empty timeline, long text, narrow width), plus a live playground.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check the timelines in light and dark.
 *
 * <p>Two things worth knowing while you look at it:
 * <ul>
 *   <li>Padding goes AROUND a timeline, never on it. The vertical line is the timeline's own border and
 *       the point is centered on the item's left edge, so padding on the timeline itself would push the
 *       points away from the line. {@link #padded(FxTimeline)} wraps it for that reason: the point and its
 *       ring stick out to the left of the line.</li>
 *   <li>Every timeline here sits on a {@code bg-surface} card, so each one overrides
 *       {@code -fxk-timeline-ring} with {@code -fxk-surface} (see {@link #onSurface(FxTimeline)}).
 *       The default ring is {@code -fxk-background} in dark, which would show as a halo on a card.</li>
 * </ul>
 */
public class TimelineTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String BODY_1 = "Get access to over 20+ pages including a dashboard layout, charts, "
            + "kanban board, calendar, and pre-order E-commerce & Marketing pages.";
    private static final String BODY_2 = "All of the pages and components are first designed in Figma and we "
            + "keep a parity between the two versions even as we update the project.";
    private static final String BODY_3 = "Get started with dozens of web components and interactive elements "
            + "built on top of Tailwind CSS.";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (button clicks inside the timelines)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Flip orientation and icons, add or remove items, add a button to the last item. "
                                + "The timeline rebuilds in place."),
                playground(),

                sectionHeading("Default timeline",
                        "Flowbite's first example: plain dots, date, title, description and a button."),
                card("vertical, no icons", padded(sample(false, false))),

                sectionHeading("Vertical timeline",
                        "Same content with an icon in every point: 24px tinted circle with an 8px ring "
                                + "that cuts a gap into the line."),
                card("vertical, icons", padded(sample(false, true))),

                sectionHeading("Horizontal timeline",
                        "setHorizontal(true): equal-width columns, each point draws its own line segment."),
                card("horizontal, icons", padded(sample(true, true))),
                card("horizontal, no icons", padded(sample(true, false))),

                sectionHeading("Point variants",
                        "Icon and plain points can be mixed in one timeline."),
                card("vertical, mixed", padded(mixedPoints(false))),
                card("horizontal, mixed", padded(mixedPoints(true))),

                sectionHeading("Content variants",
                        "Every part is optional. Content accepts any node, not just the three label types."),
                card("time + title only / title + body only / body only", padded(contentVariants(false))),
                card("the same, horizontal", padded(contentVariants(true))),
                card("custom nodes: several buttons and a label row", padded(customNodes())),

                sectionHeading("Runtime changes",
                        "Orientation flip, replacing every item, clearing, toggling icons on existing points."),
                runtimeSection(),

                sectionHeading("Edge cases",
                        "Single item, empty timeline, long text, a narrow width, many items."),
                edgeCases(),

                sectionHeading("Event log", "Everything the buttons above report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1100, 820);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxTimeline test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxTimeline test harness");
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
        FxTimeline timeline = sample(false, false);

        CheckBox horizontalCheck = new CheckBox("Horizontal");
        timeline.horizontalProperty().bind(horizontalCheck.selectedProperty());

        CheckBox iconCheck = new CheckBox("Icons");
        iconCheck.selectedProperty().addListener((o, was, is) -> setIcons(timeline, is ? ICON : null));

        int[] counter = {3};
        Button add = new Button("Add item");
        add.setOnAction(e -> {
            counter[0]++;
            timeline.getItems().add(item(iconCheck.isSelected() ? ICON : null,
                    "Item " + counter[0], "Added at runtime #" + counter[0], BODY_3));
        });

        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!timeline.getItems().isEmpty()) {
                timeline.getItems().remove(timeline.getItems().size() - 1);
            }
        });

        Button addButton = new Button("Add button to last item");
        addButton.setOnAction(e -> {
            if (!timeline.getItems().isEmpty()) {
                timeline.getItems().get(timeline.getItems().size() - 1)
                        .getContent().getChildren().add(learnMore());
            }
        });

        HBox controls = new HBox(16, horizontalCheck, iconCheck, add, removeLast, addButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, controls, padded(timeline));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- the three Flowbite examples, parametrized ---------------------------------------------

    private FxTimeline sample(boolean horizontal, boolean icons) {
        Ikon icon = icons ? ICON : null;
        FxTimeline timeline = new FxTimeline(
                item(icon, "February 2022", "Application UI code in Tailwind CSS", BODY_1, learnMore()),
                item(icon, "March 2022", "Marketing UI design in Figma", BODY_2),
                item(icon, "April 2022", "E-Commerce UI code in Tailwind CSS", BODY_3));
        timeline.setHorizontal(horizontal);
        return onSurface(timeline);
    }

    // ---- point variants ------------------------------------------------------------------------

    private FxTimeline mixedPoints(boolean horizontal) {
        FxTimeline timeline = new FxTimeline(
                item(ICON, "January 2022", "Icon point", BODY_3),
                item(null, "February 2022", "Plain point", BODY_3),
                item(ICON, "March 2022", "Icon point again", BODY_3));
        timeline.setHorizontal(horizontal);
        return onSurface(timeline);
    }

    // ---- content variants ----------------------------------------------------------------------

    private FxTimeline contentVariants(boolean horizontal) {
        FxTimeline timeline = new FxTimeline(
                item(null, "Only time and title", "No description here", null),
                item(null, null, "Only title and description", BODY_3),
                item(null, null, null, "Only a description: " + BODY_3));
        timeline.setHorizontal(horizontal);
        return onSurface(timeline);
    }

    private FxTimeline customNodes() {
        HBox buttons = new HBox(8, learnMore(), secondary("Dismiss"));
        Label meta = new Label("Custom node: a plain Label between the text and the buttons.");
        meta.getStyleClass().addAll("text-xs", "text-muted");
        meta.setPadding(new javafx.geometry.Insets(0, 0, 8, 0));

        FxTimelineContent content = new FxTimelineContent(
                new FxTimelineTime("May 2022"), new FxTimelineTitle("Content takes any node"),
                new FxTimelineBody(BODY_2), meta, buttons);

        FxTimeline timeline = new FxTimeline(
                new FxTimelineItem(new FxTimelinePoint(ICON), content),
                item(ICON, "June 2022", "A normal item after it", BODY_3));
        return onSurface(timeline);
    }

    // ---- runtime changes -----------------------------------------------------------------------

    private VBox runtimeSection() {
        FxTimeline timeline = sample(false, true);

        Button flip = new Button("Flip orientation");
        flip.setOnAction(e -> {
            timeline.setHorizontal(!timeline.isHorizontal());
            log("orientation: " + (timeline.isHorizontal() ? "horizontal" : "vertical"));
        });

        Button replace = new Button("Replace all items (setAll)");
        replace.setOnAction(e -> {
            timeline.getItems().setAll(
                    item(ICON, "Replaced 1", "First replacement", BODY_3),
                    item(ICON, "Replaced 2", "Second replacement", BODY_3));
            log("setAll: 2 new items");
        });

        Button clear = new Button("Clear");
        clear.setOnAction(e -> {
            timeline.getItems().clear();
            log("cleared");
        });

        Button reset = new Button("Reset to sample");
        reset.setOnAction(e -> {
            timeline.getItems().setAll(
                    item(ICON, "February 2022", "Application UI code in Tailwind CSS", BODY_1, learnMore()),
                    item(ICON, "March 2022", "Marketing UI design in Figma", BODY_2),
                    item(ICON, "April 2022", "E-Commerce UI code in Tailwind CSS", BODY_3));
            log("reset");
        });

        Button iconsOn = new Button("Icons on");
        iconsOn.setOnAction(e -> setIcons(timeline, ICON));
        Button iconsOff = new Button("Icons off");
        iconsOff.setOnAction(e -> setIcons(timeline, null));

        HBox buttons = new HBox(10, flip, replace, clear, reset, iconsOn, iconsOff);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, buttons, padded(timeline));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases ------------------------------------------------------------------------------

    private VBox edgeCases() {
        FxTimeline single = onSurface(new FxTimeline(
                item(ICON, "Only one", "A timeline with a single item", BODY_3)));
        FxTimeline singleHorizontal = onSurface(new FxTimeline(
                item(ICON, "Only one", "A horizontal timeline with a single item", BODY_3)));
        singleHorizontal.setHorizontal(true);

        FxTimeline empty = onSurface(new FxTimeline());

        FxTimeline longText = onSurface(new FxTimeline(
                item(ICON, "A very long date line that is not meant to wrap because it is a label",
                        "A deliberately long title that has to wrap onto several lines without being cut off "
                                + "by an ellipsis or overlapping the next item",
                        BODY_1 + " " + BODY_2 + " " + BODY_3, learnMore()),
                item(ICON, "Next", "Short one after a long one", "Short.")));

        FxTimeline longHorizontal = onSurface(new FxTimeline(
                item(ICON, "February 2022", "A deliberately long title that has to wrap onto several lines",
                        BODY_1 + " " + BODY_2, learnMore()),
                item(ICON, "March 2022", "Short", "Short."),
                item(ICON, "April 2022", "Another long title to check that the columns stay equal",
                        BODY_3)));
        longHorizontal.setHorizontal(true);

        FxTimeline many = new FxTimeline();
        for (int i = 1; i <= 6; i++) {
            many.getItems().add(item(i % 2 == 0 ? ICON : null, "Step " + i, "Milestone number " + i,
                    "Six columns in a narrow card: each one should stay readable and equal in width."));
        }
        many.setHorizontal(true);
        onSurface(many);

        VBox narrow = card("horizontal, six items in a 560px card", padded(many));
        narrow.setMaxWidth(560);

        VBox box = new VBox(12,
                card("single item, vertical", padded(single)),
                card("single item, horizontal", padded(singleHorizontal)),
                card("empty timeline (draws nothing, must not throw)", padded(empty)),
                card("long text, vertical", padded(longText)),
                card("long text, horizontal (button in the first column)", padded(longHorizontal)),
                narrow);
        return box;
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private static FxTimelineItem item(Ikon icon, String time, String title, String body, Node... extras) {
        FxTimelinePoint point = icon == null ? new FxTimelinePoint() : new FxTimelinePoint(icon);
        FxTimelineContent content = new FxTimelineContent();
        if (time != null) {
            content.getChildren().add(new FxTimelineTime(time));
        }
        if (title != null) {
            content.getChildren().add(new FxTimelineTitle(title));
        }
        if (body != null) {
            content.getChildren().add(new FxTimelineBody(body));
        }
        content.getChildren().addAll(extras);
        return new FxTimelineItem(point, content);
    }

    private static void setIcons(FxTimeline timeline, Ikon icon) {
        for (FxTimelineItem item : timeline.getItems()) {
            item.getPoint().setIcon(icon);
        }
    }

    /** FxButton's DEFAULT / GRAY / MD classes on a plain Button, like Flowbite's gray "Learn More". */
    private Button learnMore() {
        Button button = new Button("Learn More");
        button.getStyleClass().addAll("fxk-btn", "fxk-btn-default", "fxk-btn-color-gray", "fxk-btn-size-md");
        button.setOnAction(e -> log("Learn More clicked"));
        return button;
    }

    private Button secondary(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("fxk-btn", "fxk-btn-default", "fxk-btn-color-alternative", "fxk-btn-size-md");
        button.setOnAction(e -> log(text + " clicked"));
        return button;
    }

    /** The timelines sit on a bg-surface card, so the ring that hides the line must be the surface color. */
    private static FxTimeline onSurface(FxTimeline timeline) {
        timeline.setStyle("-fxk-timeline-ring: -fxk-surface;");
        return timeline;
    }

    /**
     * Room for the points that stick out to the left of the line (up to 20px for an icon plus its ring).
     * Must wrap the timeline: padding ON the timeline would move the items away from the line.
     */
    private static VBox padded(FxTimeline timeline) {
        VBox wrapper = new VBox(timeline);
        wrapper.getStyleClass().addAll("pl-6", "pr-2");
        return wrapper;
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

    private static VBox card(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        VBox wrapper = new VBox(8, label, content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
