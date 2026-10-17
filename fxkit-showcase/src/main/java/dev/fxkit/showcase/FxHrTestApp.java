package dev.fxkit.showcase;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.hr.FxHr;
import dev.fxkit.core.components.hr.FxHrIcon;
import dev.fxkit.core.components.hr.FxHrText;
import dev.fxkit.core.components.hr.FxHrTrimmed;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Isolated test harness for the FxHr family ({@link FxHr}, {@link FxHrTrimmed},
 * {@link FxHrIcon},
 * {@link FxHrText}): a live playground, each variant between paragraphs,
 * widths, graphics, text,
 * className (and which node it lands on), surfaces, runtime changes and edge
 * cases.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * Use the header's theme toggle to check that the lines restyle correctly in
 * light and dark.
 */
public class FxHrTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private static final String FIRST = "Track work across the enterprise through an open, collaborative platform. Link issues across "
            + "Jira and ingest data from other software development tools, so your IT support and "
            + "operations teams have richer contextual information to rapidly respond to requests, "
            + "incidents, and changes.";

    private static final String SECOND = "Deliver great service experiences fast - without the complexity of traditional ITSM solutions. "
            + "Accelerate critical development work, eliminate toil, and deploy changes with ease, "
            + "with a complete audit trail for every change.";

    private static final String LONG_TEXT = "a deliberately long label that is wider than 256 pixels";

    private static final List<String> SURFACES = List.of(
            "bg-background", "bg-surface", "bg-surface-alt", "bg-white", "bg-gray-900");

    /**
     * ClassNameStyler logs unrecognized tokens to this logger. LogManager only
     * holds loggers weakly, so
     * keep a reference or the handler below could be garbage collected.
     */
    private static final Logger CLASS_NAME_LOG = Logger.getLogger("dev.fxkit.className");

    private Handler classNameHandler;
    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(5);
        log.setPromptText("Event log (playground rebuilds, runtime changes)...");
        captureClassNameWarnings();

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Pick a variant, then edit className, text, graphic, surface and parent width live. "
                                + "className and text are bound, so typing restyles the same node in place."),
                playground(),

                sectionHeading("Variants between paragraphs",
                        "Like the Flowbite docs page. FxHr: 1px line, 32px above and below. Trimmed: 192 x 4 "
                                + "rounded bar, 40px above and below. Icon: 4px bars around an icon, max 256 wide. "
                                + "Text: 1px bars around medium-weight text, max 256 wide."),
                variantsSection(),

                sectionHeading("Parent width",
                        "Trimmed shrinks below 192px in a narrow parent. Icon and Text are at most 256px and "
                                + "centered. FxHr always fills the width it is given."),
                widthGrid(),

                sectionHeading("Icon graphic",
                        "Default quote icon, Ikonli icon, custom nodes and none. FontIcon and the default take "
                                + "the theme text color at 16px; a custom node is shown as it is."),
                iconSection(),

                sectionHeading("Text",
                        "Short, empty, long and runtime text. Text longer than the rule grows it and the bars "
                                + "shrink away; the text is never cut off."),
                textSection(),

                sectionHeading("className",
                        "FxHr: className recolors the line. Trimmed: it styles the bar. Icon and Text: it goes "
                                + "on the outer wrapper (as in Flowbite), so a bg-* paints the whole wrapper, "
                                + "not just the bars. Palette (bg-red-500) and semantic (bg-primary) tokens both work."),
                classNameGrid(),

                sectionHeading("className at runtime",
                        "One click sets the next token on all four. Null, a swap between two bg-* tokens, and an "
                                + "unknown token should each leave exactly the expected style classes behind. The unknown "
                                + "token must log an 'Unrecognized className token' warning in the event log below."),
                classNameRuntime(),

                sectionHeading("Surfaces",
                        "No variant hides its middle behind a page-colored fill, so the line must look right on "
                                + "every background. Toggle the theme and check all five."),
                surfaceGrid(),

                sectionHeading("Edge cases",
                        "Stacked rules, rules in an HBox, null arguments, unknown tokens, a graphic shared by two "
                                + "rules."),
                edgeCases(),

                sectionHeading("Event log", "Playground rebuilds and runtime changes land here."),
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

        stage.setTitle("FxHr test");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sends ClassNameStyler's warnings to the event log instead of the console. The
     * unknown-token
     * cases below are deliberate, so a warning there is the expected result; any
     * other warning is a
     * real finding and shows up in the same place.
     */
    private void captureClassNameWarnings() {
        classNameHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (!isLoggable(record)) {
                    return;
                }
                String line = "[className " + record.getLevel() + "] " + record.getMessage();
                if (Platform.isFxApplicationThread()) {
                    log(line);
                } else {
                    Platform.runLater(() -> log(line));
                }
            }

            @Override
            public void flush() {
                // nothing buffered
            }

            @Override
            public void close() {
                // nothing to release
            }
        };
        CLASS_NAME_LOG.setUseParentHandlers(false);
        CLASS_NAME_LOG.addHandler(classNameHandler);
    }

    @Override
    public void stop() {
        if (classNameHandler != null) {
            CLASS_NAME_LOG.removeHandler(classNameHandler);
            CLASS_NAME_LOG.setUseParentHandlers(true);
        }
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxHr test harness");
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
        ComboBox<String> variantBox = new ComboBox<>();
        variantBox.getItems().addAll("FxHr", "FxHrTrimmed", "FxHrIcon", "FxHrText");
        variantBox.setValue("FxHr");

        TextField classField = new TextField();
        classField.setPromptText("className, e.g. bg-red-500");
        HBox.setHgrow(classField, Priority.ALWAYS);

        TextField textField = new TextField("or");
        textField.setPromptText("text (FxHrText)");

        ComboBox<String> iconBox = new ComboBox<>();
        iconBox.getItems().addAll("Default quote", "Ikonli icon", "Custom node (circle)", "None");
        iconBox.setValue("Default quote");

        ComboBox<String> surfaceBox = new ComboBox<>();
        surfaceBox.getItems().addAll(SURFACES);
        surfaceBox.setValue("bg-surface");

        Slider width = new Slider(60, 760, 560);
        width.setPrefWidth(220);
        Label widthLabel = new Label();
        widthLabel.textProperty().bind(width.valueProperty().asString("parent width: %.0fpx"));

        VBox holder = new VBox();
        holder.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        holder.prefWidthProperty().bind(width.valueProperty());
        holder.maxWidthProperty().bind(width.valueProperty());

        Runnable rebuild = () -> {
            String variant = variantBox.getValue();
            Node hr = switch (variant) {
                case "FxHrTrimmed" -> {
                    FxHrTrimmed h = new FxHrTrimmed();
                    h.classNameProperty().bind(classField.textProperty());
                    yield h;
                }
                case "FxHrIcon" -> {
                    FxHrIcon h = switch (iconBox.getValue()) {
                        case "Ikonli icon" -> new FxHrIcon(ICON);
                        case "Custom node (circle)" -> new FxHrIcon(circle());
                        case "None" -> new FxHrIcon((Node) null);
                        default -> new FxHrIcon();
                    };
                    h.classNameProperty().bind(classField.textProperty());
                    yield h;
                }
                case "FxHrText" -> {
                    FxHrText h = new FxHrText();
                    h.textProperty().bind(textField.textProperty());
                    h.classNameProperty().bind(classField.textProperty());
                    yield h;
                }
                default -> {
                    FxHr h = new FxHr();
                    h.classNameProperty().bind(classField.textProperty());
                    yield h;
                }
            };
            holder.getChildren().setAll(paragraph(FIRST), hr, paragraph(SECOND));
            log("playground: rebuilt as " + variant);
        };

        variantBox.valueProperty().addListener((o, was, is) -> rebuild.run());
        iconBox.valueProperty().addListener((o, was, is) -> {
            if ("FxHrIcon".equals(variantBox.getValue())) {
                rebuild.run();
            }
        });
        surfaceBox.valueProperty().addListener((o, was, is) -> {
            holder.getStyleClass().removeAll(SURFACES);
            holder.getStyleClass().add(is);
        });
        rebuild.run();

        HBox row1 = new HBox(10, new Label("Variant"), variantBox, new Label("className"), classField);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, new Label("Text"), textField, new Label("Icon"), iconBox,
                new Label("Surface"), surfaceBox);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(10, width, widthLabel);
        row3.setAlignment(Pos.CENTER_LEFT);

        return panel(row1, row2, row3, holder);
    }

    // ---- variants between paragraphs
    // ----------------------------------------------------------

    private VBox variantsSection() {
        VBox box = new VBox(4,
                caption("FxHr"), example(new FxHr()),
                caption("FxHrTrimmed"), example(new FxHrTrimmed()),
                caption("FxHrIcon (default quote)"), example(new FxHrIcon()),
                caption("FxHrText(\"or\")"), example(new FxHrText("or")));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- parent width
    // -----------------------------------------------------------------------------

    private FlowPane widthGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (double w : new double[] { 120, 200, 256, 320, 520 }) {
            VBox stack = new VBox(new FxHr(), new FxHrTrimmed(), new FxHrIcon(), new FxHrText("or"));
            flow.getChildren().add(labeled("parent " + (int) w + "px", constrained(w, stack)));
        }
        return flow;
    }

    // ---- icon
    // -------------------------------------------------------------------------------------

    private FlowPane iconSection() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("default quote icon", sample(new FxHrIcon())));
        flow.getChildren().add(labeled("Ikonli icon (constructor)", sample(new FxHrIcon(ICON))));
        flow.getChildren().add(labeled("custom node: circle", sample(new FxHrIcon(circle()))));
        flow.getChildren().add(labeled("custom node: emoji label", sample(new FxHrIcon(new Label("\u2605")))));
        flow.getChildren().add(labeled("graphic = null (bars only)", sample(new FxHrIcon((Node) null))));

        FxHrIcon runtime = new FxHrIcon();
        Button quote = new Button("quote (setGraphic)");
        quote.setOnAction(e -> {
            runtime.setGraphic(new FxHrIcon().getGraphic());
            log("icon runtime: default quote");
        });
        Button ikonli = new Button("setIcon(Ikonli)");
        ikonli.setOnAction(e -> {
            runtime.setIcon(ICON);
            log("icon runtime: setIcon(" + ICON + ")");
        });
        Button custom = new Button("setGraphic(circle)");
        custom.setOnAction(e -> {
            runtime.setGraphic(circle());
            log("icon runtime: circle");
        });
        Button none = new Button("setIcon(null)");
        none.setOnAction(e -> {
            runtime.setIcon(null);
            log("icon runtime: removed, getGraphic() = " + runtime.getGraphic());
        });
        flow.getChildren().add(labeled("runtime changes",
                new VBox(6, sample(runtime), new HBox(8, quote, ikonli, custom, none))));
        return flow;
    }

    // ---- text
    // -------------------------------------------------------------------------------------

    private FlowPane textSection() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("\"or\"", sample(new FxHrText("or"))));
        flow.getChildren().add(labeled("empty text (no-arg constructor)", sample(new FxHrText())));
        flow.getChildren().add(labeled("long text: rule grows, bars shrink", sample(new FxHrText(LONG_TEXT))));

        FxHrText narrow = new FxHrText(LONG_TEXT);
        flow.getChildren().add(labeled("long text in a 160px parent (must not ellipsize)",
                constrained(160, new VBox(narrow))));

        FxHrText live = new FxHrText("type below");
        TextField field = new TextField("type below");
        field.textProperty().addListener((o, was, is) -> live.setText(is));
        Button clear = new Button("setText(\"\")");
        clear.setOnAction(e -> {
            live.setText("");
            log("text runtime: cleared, getText() = '" + live.getText() + "'");
        });
        flow.getChildren().add(labeled("runtime text",
                new VBox(6, sample(live), new HBox(8, field, clear))));
        return flow;
    }

    // ---- className
    // ---------------------------------------------------------------------------------

    private FlowPane classNameGrid() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("FxHr bg-red-500", sample(new FxHr("bg-red-500"))));
        flow.getChildren().add(labeled("FxHr bg-primary (semantic)", sample(new FxHr("bg-primary"))));
        flow.getChildren().add(labeled("FxHr bg-gray-400", sample(new FxHr("bg-gray-400"))));
        flow.getChildren().add(labeled("FxHrTrimmed bg-blue-500", sample(new FxHrTrimmed("bg-blue-500"))));
        flow.getChildren().add(labeled("FxHrTrimmed bg-success", sample(new FxHrTrimmed("bg-success"))));

        FxHrIcon iconBg = new FxHrIcon();
        iconBg.setClassName("bg-red-500");
        flow.getChildren().add(labeled("FxHrIcon bg-red-500 (wrapper painted)", sample(iconBg)));

        FxHrText textBg = new FxHrText("or");
        textBg.setClassName("bg-red-500");
        flow.getChildren().add(labeled("FxHrText bg-red-500 (wrapper painted)", sample(textBg)));

        FxHrIcon iconPad = new FxHrIcon();
        iconPad.setClassName("py-2");
        flow.getChildren().add(labeled("FxHrIcon py-2 (less spacing)", sample(iconPad)));

        FxHrText textPad = new FxHrText("or");
        textPad.setClassName("py-2");
        flow.getChildren().add(labeled("FxHrText py-2 (less spacing)", sample(textPad)));
        return flow;
    }

    private VBox classNameRuntime() {
        FxHr hr = new FxHr();
        FxHrTrimmed trimmed = new FxHrTrimmed();
        FxHrIcon icon = new FxHrIcon();
        FxHrText text = new FxHrText("or");
        List<StringProperty> props = List.of(hr.classNameProperty(), trimmed.classNameProperty(),
                icon.classNameProperty(), text.classNameProperty());

        String[] tokens = { null, "bg-red-500", "bg-blue-500", "bg-green-500", "bg-red-500 bg-blue-500",
                "not-a-real-token", "bg-primary", null };
        int[] index = { 0 };
        Label current = new Label("className: (none yet)");
        current.getStyleClass().addAll("text-sm", "text-muted");

        Button cycle = new Button("Apply next token");
        cycle.setOnAction(e -> {
            String token = tokens[index[0]++ % tokens.length];
            for (StringProperty p : props) {
                p.set(token);
            }
            current.setText("className: " + (token == null ? "null" : "\"" + token + "\""));
            log("className runtime: " + (token == null ? "null" : token));
        });

        Button reset = new Button("Reset to null");
        reset.setOnAction(e -> {
            for (StringProperty p : props) {
                p.set(null);
            }
            index[0] = 0;
            current.setText("className: null");
            log("className runtime: reset");
        });

        VBox box = new VBox(10, new HBox(10, cycle, reset, current),
                example(hr), example(trimmed), example(icon), example(text));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- surfaces
    // -----------------------------------------------------------------------------------

    private FlowPane surfaceGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (String surface : SURFACES) {
            VBox stack = new VBox(new FxHr(), new FxHrTrimmed(), new FxHrIcon(), new FxHrText("or"));
            stack.getStyleClass().addAll(surface, "p-4", "rounded-lg");
            stack.setPrefWidth(300);
            flow.getChildren().add(labeled(surface, stack));
        }
        return flow;
    }

    // ---- edge cases
    // ------------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        flow.getChildren().add(labeled("two FxHr back to back (32 + 32 + 32 + 32 spacing)",
                constrained(340, new VBox(paragraph("Above"), new FxHr(), new FxHr(), paragraph("Below")))));

        HBox noGrow = new HBox(paragraph("left"), new FxHr(), paragraph("right"));
        flow.getChildren().add(labeled("FxHr in HBox, no hgrow (observe: Region has no pref width)",
                constrained(340, noGrow)));

        FxHr grown = new FxHr();
        HBox.setHgrow(grown, Priority.ALWAYS);
        HBox withGrow = new HBox(paragraph("left"), grown, paragraph("right"));
        withGrow.setAlignment(Pos.CENTER);
        flow.getChildren().add(labeled("FxHr in HBox with hgrow ALWAYS (fills)", constrained(340, withGrow)));

        flow.getChildren().add(labeled("new FxHr((String) null)", sample(new FxHr((String) null))));
        flow.getChildren().add(labeled("new FxHrTrimmed((String) null)", sample(new FxHrTrimmed((String) null))));
        flow.getChildren().add(labeled("new FxHrText((String) null) (observe, no exception)",
                sample(new FxHrText((String) null))));
        flow.getChildren().add(labeled("new FxHrIcon((Ikon) null)", sample(new FxHrIcon((Ikon) null))));

        flow.getChildren().add(labeled("unknown + valid token: logs a warning, still turns red (expected)",
                sample(new FxHr("not-a-real-token bg-red-500"))));
        flow.getChildren().add(labeled("extra whitespace (\"  bg-red-500   \")",
                sample(new FxHr("  bg-red-500   "))));

        Node shared = circle();
        FxHrIcon a = new FxHrIcon(shared);
        FxHrIcon b = new FxHrIcon(shared);
        flow.getChildren().add(labeled("one node used by two FxHrIcons (observe: first one empties)",
                new VBox(sample(a), sample(b))));
        return flow;
    }

    // ---- helpers
    // ----------------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static Circle circle() {
        Circle c = new Circle(7);
        c.setStyle("-fx-fill: -fxk-primary;");
        return c;
    }

    /** Paragraph, the rule under test, paragraph: the Flowbite docs layout. */
    private static VBox example(Node hr) {
        return new VBox(paragraph(FIRST), hr, paragraph(SECOND));
    }

    /** Short version of {@link #example(Node)} for grid cells. */
    private static VBox sample(Node hr) {
        VBox box = new VBox(blurb("Above the rule"), hr, blurb("Below the rule"));
        box.setPrefWidth(340);
        return box;
    }

    /**
     * A parent with a fixed width, to test how a rule behaves in narrow and wide
     * space.
     */
    private static VBox constrained(double width, Node content) {
        VBox box = new VBox(content);
        box.setPrefWidth(width);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        box.getStyleClass().addAll("border", "border-default");
        return box;
    }

    private static Label paragraph(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().addAll("text-base", "text-muted");
        return label;
    }

    private static Label blurb(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().addAll("text-sm", "text-muted");
        return label;
    }

    private static Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll("text-sm", "font-semibold", "text-body");
        label.setStyle("-fx-padding: 12px 0 0 0;");
        return label;
    }

    private static VBox panel(Node... children) {
        VBox box = new VBox(12, children);
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