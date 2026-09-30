package dev.fxkit.showcase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import dev.fxkit.core.FxKit;
import dev.fxkit.core.components.avatar.FxAvatar;
import dev.fxkit.core.components.avatar.FxAvatarGroup;
import dev.fxkit.core.components.avatar.FxAvatarGroupCounter;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
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
 * Isolated test harness for {@link FxAvatar}, {@link FxAvatarGroup} and {@link FxAvatarGroupCounter}:
 * every Size, Color (ring), rounded/bordered/stacked flag, Status and StatusPosition, the three
 * faces (image / initials / placeholder) and their fallback order, non-square image cropping,
 * content beside the avatar, groups with a counter, edge cases, and a live playground.
 *
 * <p>No image assets are needed: test photos are generated in code. In the generated non-square
 * images, the part that a correct center-crop removes is painted <b>red</b>, so red visible inside
 * an avatar means the crop is wrong.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check that rings, status dots and text restyle in light and dark.
 */
public class AvatarTestApp extends Application {

    private static final String SAMPLE_NAME = "Jese Leos";
    private static final String SAMPLE_SUBTITLE = "Joined in August 2014";

    /** Well-formed URL that fails to load, used to test the fallback when an image errors. */
    private static final String BROKEN_URL = "file:///this/path/does/not/exist.png";

    private static Image url(String u) { return new Image(u, true); } // true = load in background

    private static final Image PHOTO_SQUARE = url("https://i.pravatar.cc/200?img=12");
    private static final Image PHOTO_WIDE   = url("https://picsum.photos/id/1025/400/200");
    private static final Image PHOTO_TALL   = url("https://picsum.photos/id/237/200/400");

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setPromptText("Event log (counter clicks, ...)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: size, ring color, flags, status + position, face source, "
                                + "initials, text beside the avatar, and a group with a counter."),
                playground(),

                sectionHeading("Size (XS 24 / SM 32 / MD 40 / LG 80 / XL 144)",
                        "Same three faces at every size. Initials font scales with the avatar; "
                                + "the placeholder silhouette scales with the face."),
                sizeGrid(),

                sectionHeading("Rounded vs square",
                        "Square has a 4px radius, rounded is a full circle. The image must be clipped to "
                                + "the same shape, including the non-square photos (wide / tall)."),
                shapeGrid(),

                sectionHeading("Face fallback order",
                        "image -> initials -> placeholder. A broken image URL must fall back automatically; "
                                + "blank initials must fall through to the placeholder."),
                faceGrid(),

                sectionHeading("Image cropping",
                        "Wide and tall photos are center-cropped to a square, not stretched. "
                                + "No red should be visible: red marks the area the crop removes."),
                cropGrid(),

                sectionHeading("Bordered: color (all 17)",
                        "Ring is drawn outside the avatar and must not shift layout. INFO/CYAN, FAILURE/RED, "
                                + "SUCCESS/GREEN, WARNING/YELLOW are synonyms and should look identical. "
                                + "Toggle the theme: ring shades change in dark."),
                colorGrid(),

                sectionHeading("Bordered: size x shape",
                        "Padding 4px + 2px ring at every size; circle ring for rounded, 6px radius otherwise."),
                borderedGrid(),

                sectionHeading("Status: dot color",
                        "ONLINE success, BUSY danger, AWAY warning, OFFLINE muted. Dot has a surface-colored "
                                + "border, so it should look cut out of the avatar in both themes."),
                statusGrid(),

                sectionHeading("Status: position (all 9)",
                        "Default is TOP_LEFT. Every non-center edge pushes the dot 4px outside the avatar. "
                                + "CENTER sits inside the face."),
                statusPositionGrid(),

                sectionHeading("Status: sizes and combinations",
                        "14px dot is fixed, so it is proportionally large on XS/SM. Also combined with a ring."),
                statusComboGrid(),

                sectionHeading("Content beside the avatar",
                        "setTexts(title, subtitle) puts a name and a muted line to the right, 16px gap. "
                                + "Subtitle is optional; setContent(node) replaces it; setContent(null) removes it."),
                contentSection(),

                sectionHeading("Group + counter",
                        "Stacked avatars overlap by 16px, later on top, each with a neutral 2px ring. "
                                + "The counter is a Hyperlink: click it and check the log."),
                groupSection(),

                sectionHeading("Edge cases",
                        "Null status/position, runtime image swap, rapid property changes, long text, "
                                + "stacked + bordered together."),
                edgeCases(),

                sectionHeading("Event log", "Everything the counters report lands here."),
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

        stage.setTitle("FxAvatar test");
        stage.setScene(scene);
        stage.show();

        diagnoseStylesheets();
    }

    /**
     * Logs whether the stylesheets FXKit actually loads at runtime contain the avatar rules. If this
     * says MISSING, the app is reading a stale or different components.css / colors.css (e.g. an old
     * copy in target/ or build/ or another resources folder), not the file you edited.
     */
    private void diagnoseStylesheets() {
        String[][] checks = {
                {"components.css", FxKit.componentsStylesheet(), ".fxk-avatar-size-md"},
                {"colors.css", FxKit.colorsStylesheet(), ".fxk-avatar-color-purple"}};
        for (String[] c : checks) {
            try (InputStream in = new URL(c[1]).openStream()) {
                String css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                log("[css] " + c[0] + " loaded from " + c[1] + " -> avatar rules "
                        + (css.contains(c[2]) ? "PRESENT" : "MISSING"));
            } catch (IOException | RuntimeException e) {
                log("[css] " + c[0] + " (" + c[1] + ") could not be read: " + e);
            }
        }
        log("[css] scene stylesheets: " + scene.getStylesheets());
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxAvatar test harness");
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

    private enum Source { PHOTO, PHOTO_WIDE, PHOTO_TALL, RANDOM, BROKEN_URL, NONE }

    private VBox playground() {
        FxAvatar avatar = new FxAvatar();
        avatar.setImage(PHOTO_SQUARE);
        avatar.setAlt("Playground avatar");

        // enum properties
        ComboBox<FxAvatar.Size> sizeBox = new ComboBox<>(FXCollections.observableArrayList(FxAvatar.Size.values()));
        sizeBox.valueProperty().bindBidirectional(avatar.sizeProperty());

        ComboBox<FxAvatar.Color> colorBox =
                new ComboBox<>(FXCollections.observableArrayList(FxAvatar.Color.values()));
        colorBox.valueProperty().bindBidirectional(avatar.colorProperty());

        ComboBox<FxAvatar.Status> statusBox = nullableCombo(FxAvatar.Status.values());
        statusBox.valueProperty().bindBidirectional(avatar.statusProperty());

        ComboBox<FxAvatar.StatusPosition> positionBox =
                new ComboBox<>(FXCollections.observableArrayList(FxAvatar.StatusPosition.values()));
        positionBox.valueProperty().bindBidirectional(avatar.statusPositionProperty());

        ComboBox<Source> sourceBox = new ComboBox<>(FXCollections.observableArrayList(Source.values()));
        sourceBox.setValue(Source.PHOTO);
        sourceBox.valueProperty().addListener((o, was, is) -> avatar.setImage(switch (is) {
            case PHOTO -> PHOTO_SQUARE;
            case PHOTO_WIDE -> PHOTO_WIDE;
            case PHOTO_TALL -> PHOTO_TALL;
            case BROKEN_URL -> new Image(BROKEN_URL, true);
            case RANDOM -> new Image("https://i.pravatar.cc/200?img=" + java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 71), true);
            case NONE -> null;
        }));

        // boolean properties
        CheckBox rounded = new CheckBox("Rounded");
        rounded.selectedProperty().bindBidirectional(avatar.roundedProperty());
        CheckBox bordered = new CheckBox("Bordered");
        bordered.selectedProperty().bindBidirectional(avatar.borderedProperty());
        CheckBox stacked = new CheckBox("Stacked");
        stacked.selectedProperty().bindBidirectional(avatar.stackedProperty());

        // initials
        TextField initials = new TextField();
        initials.setPromptText("initials");
        initials.setPrefColumnCount(6);
        avatar.placeholderInitialsProperty().bind(initials.textProperty());

        // text beside the avatar
        CheckBox texts = new CheckBox("Texts");
        texts.selectedProperty().addListener((o, was, is) -> {
            if (is) {
                avatar.setTexts(SAMPLE_NAME, SAMPLE_SUBTITLE);
            } else {
                avatar.setContent(null);
            }
        });

        HBox row1 = new HBox(16, new Label("Size"), sizeBox, new Label("Color"), colorBox,
                new Label("Source"), sourceBox, new Label("Initials"), initials);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(16, rounded, bordered, stacked, texts,
                new Label("Status"), statusBox, new Label("Position"), positionBox);
        row2.setAlignment(Pos.CENTER_LEFT);

        // avatar stage: extra padding so an overhanging dot is never clipped by the layout
        HBox stage = new HBox(avatar);
        stage.setAlignment(Pos.CENTER_LEFT);
        stage.setMinHeight(190);
        stage.getStyleClass().addAll("bg-surface-alt", "p-8", "rounded-lg");

        // group + counter playground
        FxAvatarGroupCounter counter = new FxAvatarGroupCounter(12);
        counter.setOnAction(e -> log("playground counter clicked (total " + counter.getTotal() + ")"));
        FxAvatarGroup group = new FxAvatarGroup(
                stackedAvatar(PHOTO_SQUARE, true), stackedAvatar(PHOTO_WIDE, true),
                stackedAvatar(PHOTO_TALL, true), counter);

        Spinner<Integer> total = new Spinner<>(0, 999, 12);
        total.setEditable(true);
        total.setPrefWidth(90);
        total.valueProperty().addListener((o, was, is) -> {
            if (is != null) {
                counter.setTotal(is);
            }
        });
        HBox groupRow = new HBox(16, new Label("Counter total"), total, group);
        groupRow.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12, row1, row2, stage, groupRow);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- size -------------------------------------------------------------------------------

    private static FlowPane sizeGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAvatar.Size s : FxAvatar.Size.values()) {
            FxAvatar image = new FxAvatar();
            image.setImage(PHOTO_SQUARE);
            image.setSize(s);

            FxAvatar initials = new FxAvatar();
            initials.setPlaceholderInitials("JL");
            initials.setSize(s);

            FxAvatar placeholder = new FxAvatar();
            placeholder.setSize(s);

            HBox row = new HBox(16, image, initials, placeholder);
            row.setAlignment(Pos.CENTER_LEFT);
            flow.getChildren().add(labeled(s.name() + ": image / initials / placeholder", row));
        }
        return flow;
    }

    // ---- shape ------------------------------------------------------------------------------

    private static FlowPane shapeGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (boolean rounded : new boolean[] {false, true}) {
            String tag = rounded ? "rounded" : "square";
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            for (Image img : new Image[] {PHOTO_SQUARE, PHOTO_WIDE, PHOTO_TALL, null}) {
                FxAvatar a = new FxAvatar();
                a.setSize(FxAvatar.Size.LG);
                a.setRounded(rounded);
                a.setImage(img);
                row.getChildren().add(a);
            }
            flow.getChildren().add(labeled(tag + ": square / wide / tall / placeholder", row));
        }
        return flow;
    }

    // ---- face fallback ---------------------------------------------------------------------

    private static FlowPane faceGrid() {
        FlowPane flow = new FlowPane(12, 12);

        FxAvatar img = new FxAvatar();
        img.setImage(PHOTO_SQUARE);
        img.setPlaceholderInitials("JL"); // image wins over initials
        flow.getChildren().add(labeled("image + initials -> image", sizedLg(img)));

        FxAvatar broken = new FxAvatar();
        broken.setImage(new Image(BROKEN_URL, true));
        broken.setPlaceholderInitials("JL");
        flow.getChildren().add(labeled("broken image + initials -> initials", sizedLg(broken)));

        FxAvatar brokenNoInitials = new FxAvatar();
        brokenNoInitials.setImage(new Image(BROKEN_URL, true));
        flow.getChildren().add(labeled("broken image, no initials -> placeholder", sizedLg(brokenNoInitials)));

        FxAvatar blank = new FxAvatar();
        blank.setPlaceholderInitials("   ");
        flow.getChildren().add(labeled("blank initials -> placeholder", sizedLg(blank)));

        FxAvatar initialsOnly = new FxAvatar();
        initialsOnly.setPlaceholderInitials("AB");
        initialsOnly.setRounded(true);
        flow.getChildren().add(labeled("initials only, rounded", sizedLg(initialsOnly)));

        FxAvatar none = new FxAvatar();
        flow.getChildren().add(labeled("nothing set -> placeholder", sizedLg(none)));

        FxAvatar clearable = new FxAvatar();
        clearable.setImage(PHOTO_SQUARE);
        clearable.setPlaceholderInitials("JL");
        Button clear = new Button("Clear image");
        clear.setOnAction(e -> clearable.setImage((Image) null));
        Button restore = new Button("Restore image");
        restore.setOnAction(e -> clearable.setImage(PHOTO_SQUARE));
        flow.getChildren().add(labeled("runtime: clear image -> initials",
                new VBox(6, sizedLg(clearable), new HBox(6, clear, restore))));
        return flow;
    }

    // ---- cropping --------------------------------------------------------------------------

    private static FlowPane cropGrid() {
        FlowPane flow = new FlowPane(12, 12);
        Object[][] cases = {
                {"square 200x200 (no red expected anyway)", PHOTO_SQUARE},
                {"wide 400x200 (sides cropped)", PHOTO_WIDE},
                {"tall 200x400 (top/bottom cropped)", PHOTO_TALL}};
        for (Object[] c : cases) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            for (FxAvatar.Size s : new FxAvatar.Size[] {FxAvatar.Size.SM, FxAvatar.Size.LG, FxAvatar.Size.XL}) {
                FxAvatar a = new FxAvatar();
                a.setSize(s);
                a.setImage((Image) c[1]);
                row.getChildren().add(a);
            }
            flow.getChildren().add(labeled((String) c[0], row));
        }
        return flow;
    }

    // ---- ring colors ------------------------------------------------------------------------

    private static FlowPane colorGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAvatar.Color c : FxAvatar.Color.values()) {
            FxAvatar a = new FxAvatar();
            a.setImage(PHOTO_SQUARE);
            a.setRounded(true);
            a.setBordered(true);
            a.setColor(c);
            flow.getChildren().add(labeled(c.name(), padded(a)));
        }
        return flow;
    }

    private static FlowPane borderedGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (boolean rounded : new boolean[] {false, true}) {
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            for (FxAvatar.Size s : FxAvatar.Size.values()) {
                FxAvatar a = new FxAvatar();
                a.setImage(PHOTO_SQUARE);
                a.setBordered(true);
                a.setColor(FxAvatar.Color.PURPLE);
                a.setRounded(rounded);
                a.setSize(s);
                row.getChildren().add(a);
            }
            flow.getChildren().add(labeled(rounded ? "rounded, all sizes" : "square, all sizes", padded(row)));
        }

        HBox flags = new HBox(20);
        flags.setAlignment(Pos.CENTER_LEFT);
        FxAvatar plain = new FxAvatar();
        plain.setSize(FxAvatar.Size.LG);
        plain.setPlaceholderInitials("JL");
        FxAvatar ringInitials = new FxAvatar();
        ringInitials.setSize(FxAvatar.Size.LG);
        ringInitials.setPlaceholderInitials("JL");
        ringInitials.setBordered(true);
        ringInitials.setColor(FxAvatar.Color.PINK);
        FxAvatar ringPlaceholder = new FxAvatar();
        ringPlaceholder.setSize(FxAvatar.Size.LG);
        ringPlaceholder.setBordered(true);
        ringPlaceholder.setRounded(true);
        ringPlaceholder.setColor(FxAvatar.Color.TEAL);
        flags.getChildren().addAll(plain, ringInitials, ringPlaceholder);
        flow.getChildren().add(labeled("no ring / ring + initials / ring + placeholder", padded(flags)));

        FxAvatar toggling = new FxAvatar();
        toggling.setImage(PHOTO_SQUARE);
        toggling.setSize(FxAvatar.Size.LG);
        toggling.setColor(FxAvatar.Color.FAILURE);
        Button toggle = new Button("Toggle bordered");
        toggle.setOnAction(e -> toggling.setBordered(!toggling.isBordered()));
        flow.getChildren().add(labeled("runtime bordered toggle (layout must not jump)",
                new VBox(10, padded(toggling), toggle)));
        return flow;
    }

    // ---- status -----------------------------------------------------------------------------

    private static FlowPane statusGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAvatar.Status st : FxAvatar.Status.values()) {
            HBox row = new HBox(24);
            row.setAlignment(Pos.CENTER_LEFT);
            for (boolean rounded : new boolean[] {false, true}) {
                FxAvatar a = new FxAvatar();
                a.setImage(PHOTO_SQUARE);
                a.setSize(FxAvatar.Size.LG);
                a.setRounded(rounded);
                a.setStatus(st);
                row.getChildren().add(a);
            }
            flow.getChildren().add(labeled(st.name() + " (square / rounded)", padded(row)));
        }
        return flow;
    }

    private static FlowPane statusPositionGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxAvatar.StatusPosition p : FxAvatar.StatusPosition.values()) {
            FxAvatar a = new FxAvatar();
            a.setImage(PHOTO_SQUARE);
            a.setSize(FxAvatar.Size.LG);
            a.setRounded(true);
            a.setStatus(FxAvatar.Status.ONLINE);
            a.setStatusPosition(p);
            flow.getChildren().add(labeled(p.name(), padded(a)));
        }
        return flow;
    }

    private static FlowPane statusComboGrid() {
        FlowPane flow = new FlowPane(12, 12);

        HBox sizes = new HBox(20);
        sizes.setAlignment(Pos.CENTER_LEFT);
        for (FxAvatar.Size s : FxAvatar.Size.values()) {
            FxAvatar a = new FxAvatar();
            a.setImage(PHOTO_SQUARE);
            a.setRounded(true);
            a.setSize(s);
            a.setStatus(FxAvatar.Status.BUSY);
            a.setStatusPosition(FxAvatar.StatusPosition.BOTTOM_RIGHT);
            sizes.getChildren().add(a);
        }
        flow.getChildren().add(labeled("BUSY bottom-right at every size", padded(sizes)));

        HBox withRing = new HBox(24);
        withRing.setAlignment(Pos.CENTER_LEFT);
        FxAvatar ring = new FxAvatar();
        ring.setImage(PHOTO_SQUARE);
        ring.setSize(FxAvatar.Size.LG);
        ring.setRounded(true);
        ring.setBordered(true);
        ring.setColor(FxAvatar.Color.SUCCESS);
        ring.setStatus(FxAvatar.Status.ONLINE);
        FxAvatar ringInitials = new FxAvatar();
        ringInitials.setSize(FxAvatar.Size.LG);
        ringInitials.setPlaceholderInitials("JL");
        ringInitials.setBordered(true);
        ringInitials.setColor(FxAvatar.Color.INDIGO);
        ringInitials.setStatus(FxAvatar.Status.AWAY);
        ringInitials.setStatusPosition(FxAvatar.StatusPosition.TOP_RIGHT);
        withRing.getChildren().addAll(ring, ringInitials);
        flow.getChildren().add(labeled("status + ring (dot vs ring overlap)", padded(withRing)));

        FxAvatar toggling = new FxAvatar();
        toggling.setImage(PHOTO_SQUARE);
        toggling.setSize(FxAvatar.Size.LG);
        toggling.setRounded(true);
        Button cycle = new Button("Cycle status (incl. none)");
        int[] n = {0};
        cycle.setOnAction(e -> {
            FxAvatar.Status[] all = FxAvatar.Status.values();
            int i = n[0]++ % (all.length + 1);
            toggling.setStatus(i == all.length ? null : all[i]);
        });
        flow.getChildren().add(labeled("runtime status cycle (dot must be unmanaged when null)",
                new VBox(10, padded(toggling), cycle)));
        return flow;
    }

    // ---- content ----------------------------------------------------------------------------

    private VBox contentSection() {
        VBox holder = new VBox(16);

        FxAvatar full = new FxAvatar();
        full.setImage(PHOTO_SQUARE);
        full.setRounded(true);
        full.setStatus(FxAvatar.Status.ONLINE);
        full.setTexts(SAMPLE_NAME, SAMPLE_SUBTITLE);

        FxAvatar noSubtitle = new FxAvatar();
        noSubtitle.setPlaceholderInitials("JL");
        noSubtitle.setRounded(true);
        noSubtitle.setTexts(SAMPLE_NAME, null);

        FxAvatar blankSubtitle = new FxAvatar();
        blankSubtitle.setSize(FxAvatar.Size.SM);
        blankSubtitle.setTexts("Blank subtitle is skipped", "   ");

        FxAvatar big = new FxAvatar();
        big.setImage(PHOTO_WIDE);
        big.setSize(FxAvatar.Size.LG);
        big.setBordered(true);
        big.setColor(FxAvatar.Color.BLUE);
        big.setTexts("LG avatar", "Text stays vertically centered");

        holder.getChildren().addAll(full, noSubtitle, blankSubtitle, big);

        FxAvatar custom = new FxAvatar();
        custom.setImage(PHOTO_SQUARE);
        custom.setRounded(true);
        custom.setTexts("Custom content demo", "Click the buttons");

        Button setCustom = new Button("setContent(custom node)");
        setCustom.setOnAction(e -> {
            Label heading = new Label("Custom node");
            heading.getStyleClass().add("font-bold");
            Button inner = new Button("Inner action");
            inner.setOnAction(ev -> log("avatar content: inner button clicked"));
            custom.setContent(new VBox(4, heading, inner));
        });
        Button texts = new Button("setTexts again");
        texts.setOnAction(e -> custom.setTexts("Replaced texts", "Old content must be removed, not stacked"));
        Button clear = new Button("setContent(null)");
        clear.setOnAction(e -> custom.setContent(null));

        VBox box = new VBox(16, holder, new VBox(10, custom, new HBox(10, setCustom, texts, clear)));
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- group ------------------------------------------------------------------------------

    private VBox groupSection() {
        FxAvatarGroup plain = new FxAvatarGroup(
                stackedAvatar(PHOTO_SQUARE, true), stackedAvatar(PHOTO_WIDE, true),
                stackedAvatar(PHOTO_TALL, true), stackedAvatar(PHOTO_SQUARE, true));

        FxAvatarGroupCounter counter = new FxAvatarGroupCounter(99);
        counter.setOnAction(e -> log("group counter clicked: +" + counter.getTotal()));
        FxAvatarGroup withCounter = new FxAvatarGroup(
                stackedAvatar(PHOTO_SQUARE, true), stackedAvatar(PHOTO_WIDE, true),
                stackedAvatar(PHOTO_TALL, true), stackedAvatar(PHOTO_SQUARE, true), counter);

        FxAvatarGroup squares = new FxAvatarGroup(
                stackedAvatar(PHOTO_SQUARE, false), stackedAvatar(PHOTO_WIDE, false),
                stackedAvatar(PHOTO_TALL, false));

        FxAvatar initialsA = new FxAvatar();
        initialsA.setPlaceholderInitials("AB");
        initialsA.setRounded(true);
        initialsA.setStacked(true);
        FxAvatar placeholder = new FxAvatar();
        placeholder.setRounded(true);
        placeholder.setStacked(true);
        FxAvatarGroup mixed = new FxAvatarGroup(
                stackedAvatar(PHOTO_SQUARE, true), initialsA, placeholder, new FxAvatarGroupCounter(5));

        FxAvatarGroup small = new FxAvatarGroup();
        for (FxAvatar.Size s : new FxAvatar.Size[] {FxAvatar.Size.SM, FxAvatar.Size.SM, FxAvatar.Size.SM}) {
            FxAvatar a = stackedAvatar(PHOTO_SQUARE, true);
            a.setSize(s);
            small.getChildren().add(a);
        }

        FxAvatarGroupCounter zero = new FxAvatarGroupCounter();
        FxAvatarGroup zeroGroup = new FxAvatarGroup(stackedAvatar(PHOTO_SQUARE, true), zero);

        FlowPane flow = new FlowPane(12, 12);
        flow.getChildren().addAll(
                labeled("4 rounded, stacked", padded(plain)),
                labeled("4 rounded + counter (+99)", padded(withCounter)),
                labeled("square stacked", padded(squares)),
                labeled("image / initials / placeholder + counter", padded(mixed)),
                labeled("SM avatars", padded(small)),
                labeled("counter default total (+0)", padded(zeroGroup)));

        VBox box = new VBox(10, flow);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return box;
    }

    // ---- edge cases -------------------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        FxAvatar nullPos = new FxAvatar();
        nullPos.setImage(PHOTO_SQUARE);
        nullPos.setSize(FxAvatar.Size.LG);
        nullPos.setStatus(FxAvatar.Status.ONLINE);
        nullPos.setStatusPosition(null);
        flow.getChildren().add(labeled("statusPosition = null (falls back to TOP_LEFT)", padded(nullPos)));

        FxAvatar swap = new FxAvatar();
        swap.setSize(FxAvatar.Size.LG);
        swap.setRounded(true);
        swap.setPlaceholderInitials("JL");
        Image[] sources = {PHOTO_SQUARE, PHOTO_WIDE, PHOTO_TALL, new Image(BROKEN_URL, true), null};
        String[] names = {"square", "wide", "tall", "broken", "null"};
        Label current = new Label("current: none");
        current.getStyleClass().addAll("text-xs", "text-muted");
        Button next = new Button("Next image");
        int[] n = {0};
        next.setOnAction(e -> {
            int i = n[0]++ % sources.length;
            swap.setImage(sources[i]);
            current.setText("current: " + names[i]);
        });
        flow.getChildren().add(labeled("swap image at runtime (listeners must not leak or stick)",
                new VBox(8, padded(swap), current, next)));

        FxAvatar rapid = new FxAvatar();
        rapid.setImage(PHOTO_SQUARE);
        rapid.setSize(FxAvatar.Size.LG);
        Button cycle = new Button("Cycle size + color + shape + status");
        int[] k = {0};
        cycle.setOnAction(e -> {
            k[0]++;
            rapid.setSize(FxAvatar.Size.values()[k[0] % FxAvatar.Size.values().length]);
            rapid.setColor(FxAvatar.Color.values()[k[0] % FxAvatar.Color.values().length]);
            rapid.setRounded(k[0] % 2 == 0);
            rapid.setBordered(true);
            rapid.setStatus(FxAvatar.Status.values()[k[0] % FxAvatar.Status.values().length]);
        });
        Label hint = new Label("Only one size-* / color-* / status-* class should be present at a time.");
        hint.getStyleClass().addAll("text-xs", "text-muted");
        flow.getChildren().add(labeled("style class swap", new VBox(8, padded(rapid), hint, cycle)));

        FxAvatar both = new FxAvatar();
        both.setImage(PHOTO_SQUARE);
        both.setSize(FxAvatar.Size.LG);
        both.setRounded(true);
        both.setBordered(true);
        both.setStacked(true);
        both.setColor(FxAvatar.Color.FAILURE);
        flow.getChildren().add(labeled("bordered + stacked (stacked's neutral ring wins)", padded(both)));

        FxAvatar longText = new FxAvatar();
        longText.setImage(PHOTO_SQUARE);
        longText.setRounded(true);
        longText.setStatus(FxAvatar.Status.AWAY);
        longText.setTexts("A rather long display name that keeps going and going",
                "And an equally long subtitle to see how the row behaves without wrapping");
        flow.getChildren().add(labeled("long text (labels do not wrap by default)", padded(longText)));

        FxAvatar accessible = new FxAvatar();
        accessible.setImage(PHOTO_SQUARE);
        accessible.setSize(FxAvatar.Size.LG);
        accessible.setAlt("Portrait of Jese Leos");
        flow.getChildren().add(labeled("alt text set (check with a screen reader / Scenic View)",
                padded(accessible)));
        return flow;
    }

    // ---- helpers ----------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxAvatar stackedAvatar(Image image, boolean rounded) {
        FxAvatar a = new FxAvatar();
        a.setImage(image);
        a.setRounded(rounded);
        a.setStacked(true);
        return a;
    }

    private static FxAvatar sizedLg(FxAvatar avatar) {
        avatar.setSize(FxAvatar.Size.LG);
        return avatar;
    }

    /** Breathing room so rings and overhanging status dots are never mistaken for clipping. */
    private static VBox padded(Node content) {
        VBox box = new VBox(content);
        box.setStyle("-fx-padding: 6px;");
        return box;
    }

    private static <T> ComboBox<T> nullableCombo(T[] values) {
        ComboBox<T> box = new ComboBox<>();
        box.getItems().add(null);
        box.getItems().addAll(values);
        box.setButtonCell(new NullableCell<>());
        box.setCellFactory(lv -> new NullableCell<>());
        return box;
    }

    private static final class NullableCell<T> extends ListCell<T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : (item == null ? "(none)" : item.toString()));
        }
    }

    /**
     * A test "photo": a hue gradient with a white disc in the middle. The strips that a correct
     * center-crop to a square removes are painted red, so red showing in an avatar means the crop
     * is wrong.
     */
    private static Image generated(int w, int h, double hue) {
        WritableImage img = new WritableImage(w, h);
        PixelWriter pw = img.getPixelWriter();
        int side = Math.min(w, h);
        int x0 = (w - side) / 2;
        int y0 = (h - side) / 2;
        double cx = w / 2.0;
        double cy = h / 2.0;
        double r = side * 0.18;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c;
                boolean cropped = x < x0 || x >= x0 + side || y < y0 || y >= y0 + side;
                if (cropped) {
                    c = Color.RED;
                } else if (Math.hypot(x - cx, y - cy) < r) {
                    c = Color.WHITE;
                } else {
                    c = Color.hsb((hue + 70.0 * (x - x0) / side) % 360, 0.65, 0.55 + 0.4 * (y - y0) / side);
                }
                pw.setColor(x, y, c);
            }
        }
        return img;
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