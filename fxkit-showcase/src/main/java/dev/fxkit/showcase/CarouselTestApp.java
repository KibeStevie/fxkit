package dev.fxkit.showcase;

import dev.fxkit.core.components.carousel.FxCarousel;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Isolated test harness for {@link FxCarousel}: a live playground, then every
 * Flowbite example
 * (default, static, interval, custom controls, indicators, pause on hover,
 * slide content,
 * onSlideChange), the four sizes, programmatic navigation, and edge cases (no
 * slides, one slide,
 * 12 slides, runtime add/remove, size = null, cropped images, narrow container,
 * a Slider inside a
 * slide, detaching from the scene).
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check the controls and dots in light and
 * dark.
 *
 * <p>
 * Image slides are generated placeholders (Flowbite's SVG examples can't be
 * loaded by
 * {@code ImageView}), so the harness needs no files.
 */
public class CarouselTestApp extends Application {

    private static final Color[][] PALETTE = {
            { Color.web("#3b82f6"), Color.web("#1e3a8a") },
            { Color.web("#10b981"), Color.web("#064e3b") },
            { Color.web("#f59e0b"), Color.web("#78350f") },
            { Color.web("#ec4899"), Color.web("#831843") },
            { Color.web("#8b5cf6"), Color.web("#4c1d95") },
    };
    private static final String[] BACKGROUNDS = { "bg-blue-500", "bg-green-500", "bg-red-500", "bg-primary" };

    private enum SlideKind {
        IMAGES, TEXT, MIXED
    }

    private Scene scene;
    private Button themeToggle;
    private TextArea log;
    private int playgroundCounter;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (onSlideChange, button clicks, exceptions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: size, slide, interval, indicators, pause on hover, controls, "
                                + "and the slide list itself. Watch the carousel react in place."),
                playground(),

                sectionHeading("Default carousel",
                        "Slides every 3 s and wraps. Try: arrows, dots, mouse drag (stops at the ends)."),
                labeled("slide = true, interval 3000, indicators on", new FxCarousel(imageSlides())),

                sectionHeading("Static carousel",
                        "slide = false: no automatic sliding, but controls and dots still work."),
                labeled("slide = false", staticCarousel()),

                sectionHeading("Sliding interval", "slideInterval = 1000 ms next to 5000 ms."),
                intervalRow(),

                sectionHeading("Custom controls",
                        "A control node replaces the whole circle + chevron; the button still spans the "
                                + "full edge height. Hover and click the empty part of the edge too."),
                customControlsSection(),

                sectionHeading("Indicators", "Default next to indicators = false."),
                indicatorsRow(),

                sectionHeading("Pause on hover",
                        "pauseOnHover = true. Rest the mouse on it: sliding stops; leave: it resumes "
                                + "with a fresh interval. The left one has it off."),
                pauseRow(),

                sectionHeading("Slide content",
                        "Any node: text panes, a button (click it: must log, not drag), a non-resizable "
                                + "node (centered), a Slider (known conflict: dragging it also swipes)."),
                labeled("mixed content", new FxCarousel(mixedSlides())),

                sectionHeading("onSlideChange / selectedIndex",
                        "The callback fires for auto-slide, controls, dots, drag and select(). "
                                + "selectedIndex is a read-only property."),
                onSlideChangeSection(),

                sectionHeading("Sizes",
                        "SM 224, MD 256 (default), LG 320, XL 384 px. Heights come from CSS."),
                sizesSection(),

                sectionHeading("Programmatic navigation",
                        "next() / previous() wrap; select(i) throws IndexOutOfBoundsException when out of range."),
                navigationSection(),

                sectionHeading("Edge cases",
                        "No slides, one slide, 12 slides, size = null, cropped and small images, narrow "
                                + "container, detached from the scene."),
                edgeCases(),

                sectionHeading("Event log", "Everything the callbacks report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1000, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("FxCarousel test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxCarousel test harness");
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
        FxCarousel carousel = new FxCarousel();
        carousel.setOnSlideChange(i -> log("playground: slide -> " + i));
        ComboBox<SlideKind> kindBox = new ComboBox<>();
        kindBox.getItems().addAll(SlideKind.values());
        kindBox.setValue(SlideKind.IMAGES);
        for (int i = 0; i < 3; i++) {
            carousel.getSlides().add(playgroundSlide(kindBox.getValue()));
        }

        ComboBox<FxCarousel.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxCarousel.Size.values());
        sizeBox.valueProperty().bindBidirectional(carousel.sizeProperty());

        CheckBox nullSize = new CheckBox("size = null (prefHeight 180)");
        nullSize.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                carousel.setSize(null);
                carousel.setPrefHeight(180);
            } else {
                carousel.setSize(FxCarousel.Size.MD);
            }
            sizeBox.setDisable(is);
        });

        CheckBox slideCheck = new CheckBox("slide (auto)");
        slideCheck.setSelected(carousel.isSlide());
        carousel.slideProperty().bind(slideCheck.selectedProperty());

        CheckBox indicatorsCheck = new CheckBox("Indicators");
        indicatorsCheck.setSelected(carousel.isIndicators());
        carousel.indicatorsProperty().bind(indicatorsCheck.selectedProperty());

        CheckBox hoverCheck = new CheckBox("Pause on hover");
        carousel.pauseOnHoverProperty().bind(hoverCheck.selectedProperty());

        CheckBox controlsCheck = new CheckBox("Custom controls");
        controlsCheck.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                carousel.setLeftControlText("left");
                carousel.setRightControlText("right");
            } else {
                carousel.setLeftControl(null);
                carousel.setRightControl(null);
            }
        });

        Spinner<Integer> interval = new Spinner<>(250, 10000, 3000, 250);
        interval.setEditable(true);
        interval.valueProperty().addListener((o, was, is) -> {
            if (is != null) {
                carousel.setSlideInterval(is);
            }
        });

        Button add = new Button("Add slide");
        add.setOnAction(e -> carousel.getSlides().add(playgroundSlide(kindBox.getValue())));
        Button insertFirst = new Button("Insert at 0");
        insertFirst.setOnAction(e -> carousel.getSlides().add(0, playgroundSlide(kindBox.getValue())));
        Button removeLast = new Button("Remove last");
        removeLast.setOnAction(e -> {
            if (!carousel.getSlides().isEmpty()) {
                carousel.getSlides().remove(carousel.getSlides().size() - 1);
            }
        });
        Button removeSelected = new Button("Remove selected");
        removeSelected.setOnAction(e -> {
            if (!carousel.getSlides().isEmpty()) {
                carousel.getSlides().remove(carousel.getSelectedIndex());
            }
        });
        Button clear = new Button("Clear");
        clear.setOnAction(e -> carousel.getSlides().clear());

        Button prev = new Button("previous()");
        prev.setOnAction(e -> carousel.previous());
        Button next = new Button("next()");
        next.setOnAction(e -> carousel.next());

        Label state = new Label();
        state.getStyleClass().addAll("text-sm", "text-muted");
        state.textProperty().bind(carousel.selectedIndexProperty().asString("selectedIndex = %d"));

        HBox row1 = new HBox(16, new Label("Size"), sizeBox, nullSize, slideCheck, new Label("Interval ms"),
                interval);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, indicatorsCheck, hoverCheck, controlsCheck, new Label("New slides"), kindBox);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(10, add, insertFirst, removeLast, removeSelected, clear, prev, next, state);
        row3.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, row3, carousel);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    private Node playgroundSlide(SlideKind kind) {
        int n = ++playgroundCounter;
        return switch (kind) {
            case IMAGES -> placeholder(n, PALETTE[n % PALETTE.length], 1200, 600);
            case TEXT -> textSlide("Slide " + n, BACKGROUNDS[n % BACKGROUNDS.length]);
            case MIXED -> n % 2 == 0
                    ? placeholder(n, PALETTE[n % PALETTE.length], 1200, 600)
                    : textSlide("Slide " + n, BACKGROUNDS[n % BACKGROUNDS.length]);
        };
    }

    // ---- Flowbite examples
    // ------------------------------------------------------------------

    private FxCarousel staticCarousel() {
        FxCarousel carousel = new FxCarousel(imageSlides());
        carousel.setSlide(false);
        return carousel;
    }

    private VBox intervalRow() {
        FxCarousel fast = new FxCarousel(imageSlides());
        fast.setSlideInterval(1000);
        FxCarousel slow = new FxCarousel(imageSlides());
        slow.setSlideInterval(5000);
        return sideBySide(labeled("slideInterval = 1000", fast), labeled("slideInterval = 5000", slow));
    }

    private VBox customControlsSection() {
        FxCarousel text = new FxCarousel(imageSlides());
        text.setLeftControlText("left");
        text.setRightControlText("right");

        // Any node works: here a plain label with a utility-class pill instead of the
        // default circle.
        Label prev = new Label("< Prev");
        prev.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-md", "p-2", "font-semibold");
        Label next = new Label("Next >");
        next.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-md", "p-2", "font-semibold");
        FxCarousel nodes = new FxCarousel(imageSlides());
        nodes.setLeftControl(prev);
        nodes.setRightControl(next);

        Button reset = new Button("Reset both to default (setLeftControl(null))");
        reset.setOnAction(e -> {
            text.setLeftControl(null);
            text.setRightControl(null);
            nodes.setLeftControl(null);
            nodes.setRightControl(null);
        });

        VBox box = sideBySide(labeled("setLeftControlText(\"left\") / right", text),
                labeled("custom nodes", nodes));
        box.getChildren().add(reset);
        return box;
    }

    private VBox indicatorsRow() {
        FxCarousel with = new FxCarousel(imageSlides());
        FxCarousel without = new FxCarousel(imageSlides());
        without.setIndicators(false);
        return sideBySide(labeled("indicators = true", with), labeled("indicators = false", without));
    }

    private VBox pauseRow() {
        FxCarousel off = new FxCarousel(imageSlides());
        off.setSlideInterval(1500);
        FxCarousel on = new FxCarousel(imageSlides());
        on.setSlideInterval(1500);
        on.setPauseOnHover(true);
        return sideBySide(labeled("pauseOnHover = false", off), labeled("pauseOnHover = true", on));
    }

    private VBox onSlideChangeSection() {
        FxCarousel carousel = new FxCarousel(
                textSlide("Slide 1", "bg-blue-500"), textSlide("Slide 2", "bg-green-500"),
                textSlide("Slide 3", "bg-red-500"));
        Label status = new Label();
        status.getStyleClass().addAll("text-sm", "text-body");
        status.textProperty().bind(carousel.selectedIndexProperty().asString("selectedIndex = %d"));
        carousel.setOnSlideChange(i -> log("onSlideChange(" + i + ")"));
        return labeled("callback logs below; label is bound to selectedIndexProperty()",
                carousel, status);
    }

    // ---- sizes / navigation
    // -----------------------------------------------------------------

    private VBox sizesSection() {
        VBox box = new VBox(12);
        for (FxCarousel.Size size : FxCarousel.Size.values()) {
            FxCarousel carousel = new FxCarousel(imageSlides());
            carousel.setSize(size);
            carousel.setSlide(false);
            box.getChildren().add(labeled("Size." + size.name(), carousel));
        }
        return box;
    }

    private VBox navigationSection() {
        FxCarousel carousel = new FxCarousel(
                textSlide("Slide 0", "bg-blue-500"), textSlide("Slide 1", "bg-green-500"),
                textSlide("Slide 2", "bg-red-500"), textSlide("Slide 3", "bg-primary"));
        carousel.setSlide(false);
        carousel.setSize(FxCarousel.Size.SM);

        Button prev = new Button("previous() (wraps from 0 to last)");
        prev.setOnAction(e -> carousel.previous());
        Button next = new Button("next() (wraps from last to 0)");
        next.setOnAction(e -> carousel.next());
        Button first = new Button("select(0)");
        first.setOnAction(e -> carousel.select(0));
        Button last = new Button("select(last)");
        last.setOnAction(e -> carousel.select(carousel.getSlides().size() - 1));
        Button bad = new Button("select(99) (expect IndexOutOfBoundsException)");
        bad.setOnAction(e -> {
            try {
                carousel.select(99);
                log("select(99): NO exception - bug");
            } catch (IndexOutOfBoundsException ex) {
                log("select(99): IndexOutOfBoundsException, as expected");
            }
        });

        return labeled("static carousel driven from code", carousel,
                new HBox(10, prev, next, first, last, bad));
    }

    // ---- edge cases
    // -------------------------------------------------------------------------

    private VBox edgeCases() {
        // No slides
        FxCarousel empty = new FxCarousel();
        empty.setSize(FxCarousel.Size.SM);
        Button fill = new Button("Add a slide");
        fill.setOnAction(e -> empty.getSlides().add(textSlide("Now there is one",
                BACKGROUNDS[empty.getSlides().size() % BACKGROUNDS.length])));
        VBox emptyBox = labeled("no slides: must not throw; add one at runtime", empty, fill);

        // One slide
        FxCarousel single = new FxCarousel(textSlide("Only slide", "bg-blue-500"));
        single.setSize(FxCarousel.Size.SM);
        single.setOnSlideChange(i -> log("single: onSlideChange(" + i + ") - should never appear"));
        VBox singleBox = labeled("one slide: no auto-slide; arrows stay on slide 0", single);

        // Many slides
        FxCarousel[] manyHolder = new FxCarousel[1];
        Node[] many = new Node[12];
        for (int i = 0; i < many.length; i++) {
            many[i] = textSlide("Slide " + (i + 1) + " of 12", BACKGROUNDS[i % BACKGROUNDS.length]);
        }
        manyHolder[0] = new FxCarousel(many);
        manyHolder[0].setSize(FxCarousel.Size.SM);
        manyHolder[0].setSlideInterval(800);
        VBox manyBox = labeled("12 slides at 800 ms: dots wrap neatly, wraparound scrolls back across all",
                manyHolder[0]);

        // size = null with prefHeight
        FxCarousel custom = new FxCarousel(textSlide("size = null, prefHeight 120", "bg-green-500"),
                textSlide("second", "bg-red-500"));
        custom.setSize(null);
        custom.setPrefHeight(120);
        VBox customBox = labeled("size = null + setPrefHeight(120): code height must apply", custom);

        // Images of different shapes
        FxCarousel shapes = new FxCarousel(
                placeholder(1, PALETTE[0], 1200, 600),
                placeholder(2, PALETTE[1], 400, 900),
                placeholder(3, PALETTE[2], 200, 100));
        shapes.setSize(FxCarousel.Size.SM);
        VBox shapesBox = labeled("landscape / tall portrait (cropped, centered) / tiny (scaled to width)", shapes);

        // Narrow container
        FxCarousel narrow = new FxCarousel(imageSlides());
        narrow.setSize(FxCarousel.Size.SM);
        VBox narrowHolder = new VBox(narrow);
        narrowHolder.setMaxWidth(220);
        VBox narrowBox = labeled("220 px wide: controls and dots must still fit", narrowHolder);

        // Detach / reattach
        FxCarousel detachable = new FxCarousel(
                textSlide("Slide 1", "bg-blue-500"), textSlide("Slide 2", "bg-green-500"),
                textSlide("Slide 3", "bg-red-500"));
        detachable.setSize(FxCarousel.Size.SM);
        detachable.setSlideInterval(1000);
        detachable.setOnSlideChange(i -> log("detachable: slide -> " + i));
        VBox holder = new VBox(detachable);
        Button detach = new Button("Remove from scene (log must go quiet)");
        detach.setOnAction(e -> holder.getChildren().remove(detachable));
        Button attach = new Button("Add back (auto-slide resumes)");
        attach.setOnAction(e -> {
            if (!holder.getChildren().contains(detachable)) {
                holder.getChildren().add(detachable);
            }
        });
        VBox detachBox = labeled("timer stops when detached, restarts when re-added", holder,
                new HBox(10, detach, attach));

        VBox box = new VBox(16, emptyBox, singleBox, manyBox, customBox, shapesBox, narrowBox, detachBox);
        return box;
    }

    // ---- slide factories
    // --------------------------------------------------------------------

    private static Node[] imageSlides() {
        Node[] slides = new Node[PALETTE.length];
        for (int i = 0; i < slides.length; i++) {
            slides[i] = placeholder(i + 1, PALETTE[i], 1200, 600);
        }
        return slides;
    }

    private Node[] mixedSlides() {
        Button click = new Button("Click me (must log, not swipe)");
        click.setOnAction(e -> log("slide button clicked"));
        VBox buttonPane = new VBox(8, new Label("A button inside a slide"), click);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.getStyleClass().add("bg-surface-alt");

        Slider slider = new Slider(0, 100, 50);
        slider.setMaxWidth(300);
        StackPane sliderPane = new StackPane(slider);
        sliderPane.getStyleClass().add("bg-surface-alt");

        Circle circle = new Circle(48, Color.web("#f59e0b")); // not resizable: centered at natural size

        return new Node[] {
                textSlide("Text slide", "bg-blue-500"),
                placeholder(2, PALETTE[1], 1200, 600),
                buttonPane,
                circle,
                sliderPane
        };
    }

    /** A slide with text on a palette background. */
    private static Node textSlide(String text, String backgroundClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("text-white", "text-2xl", "font-bold");
        StackPane pane = new StackPane(label);
        pane.getStyleClass().add(backgroundClass);
        return pane;
    }

    private static ImageView placeholder(int number, Color[] colors, int width, int height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, colors[0]), new Stop(1, colors[1])));
        g.fillRect(0, 0, width, height);
        g.setFill(Color.color(1, 1, 1, 0.9));
        g.setFont(Font.font("System", FontWeight.BOLD, Math.min(width, height) / 5.0));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText("Slide " + number, width / 2.0, height / 2.0);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return FxCarousel.image(canvas.snapshot(parameters, null));
    }

    // ---- helpers
    // ----------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static VBox sideBySide(Node left, Node right) {
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox row = new HBox(16, left, right);
        return new VBox(row);
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        subtitleLabel.setWrapText(true);
        return new VBox(2, titleLabel, subtitleLabel);
    }

    private static VBox labeled(String caption, Node... content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        VBox wrapper = new VBox(8, label);
        wrapper.getChildren().addAll(content);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
