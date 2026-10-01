package dev.fxkit.showcase;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.devicons.Devicons;

import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.form.FxCheckbox;
import dev.fxkit.core.components.form.FxFileInput;
import dev.fxkit.core.components.form.FxHelperText;
import dev.fxkit.core.components.form.FxLabel;
import dev.fxkit.core.components.form.FxRadio;
import dev.fxkit.core.components.form.FxRangeSlider;
import dev.fxkit.core.components.form.FxTextArea;
import dev.fxkit.core.components.form.FxTextInput;
import dev.fxkit.core.components.form.FxToggleSwitch;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Isolated test harness for FXKit's form components: {@link FxTextInput} (and its skin: addon, icons,
 * password mask), {@link FxLabel}, {@link FxHelperText}, {@link FxTextArea}, {@link FxCheckbox},
 * {@link FxRadio}, {@link FxToggleSwitch}, {@link FxRangeSlider} and {@link FxFileInput}. It ends with
 * a validated sign-up form that uses all of them together.
 *
 * <p>Swap this in for {@code ShowcaseApp} as the run configuration's main class while iterating.
 * Use the header's theme toggle to check every control in light and dark. The controls that drive the
 * playground and the runtime sections are stock JavaFX controls on purpose, so a bug in a component
 * under test cannot break the test rig.
 */
public class FormTestApp extends Application {

    /** Change to any icon from the Ikonli pack you have on the classpath. */
    private static final Ikon ICON = Devicons.JAVA;

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(6);
        log.setPromptText("Event log (value changes, clicks, form submits)...");
        log.setMinWidth(0);

        VBox content = new VBox(28,
                sectionHeading("Playground: TextInput + Label + HelperText",
                        "One field with every property live. The label and helper text follow the field's "
                                + "color, as in Flowbite's validation examples."),
                playground(),

                sectionHeading("FxTextInput: Size",
                        "SM 34px / MD 42px / LG 58px tall, with matching padding and font size."),
                textInputSizes(),

                sectionHeading("FxTextInput: Color",
                        "GRAY is neutral; INFO, FAILURE, WARNING, SUCCESS tint border, background and text. "
                                + "Click into each: the focus ring should keep the color."),
                textInputColors(),

                sectionHeading("FxTextInput: icons, addon, shadow (skin)",
                        "Icons live in a 40px strip inside the padding; the addon is a box that pushes the "
                                + "text. All of it must stay INSIDE the 1px border and let clicks through."),
                textInputDecorations(),

                sectionHeading("FxTextInput: password and states",
                        "PASSWORD masks with bullets and blocks copy/cut: select all, Ctrl+C, then paste into "
                                + "the probe field - it must stay empty. Read-only, disabled and long text too."),
                textInputPasswordAndStates(),

                sectionHeading("FxTextInput: runtime changes",
                        "Skin overlays and the type switch rebuild while the field is showing. Type something "
                                + "first - text and caret must survive."),
                textInputRuntime(),

                sectionHeading("FxLabel",
                        "text-sm font-medium. Colors are the validation colors. Clicking a label focuses its "
                                + "target and activates checkbox / radio / toggle / file input."),
                labelSection(),

                sectionHeading("FxHelperText",
                        "Wraps to the parent's width, holds inline nodes, and setText replaces all children."),
                helperTextSection(),

                sectionHeading("Label + input + helper, per validation color",
                        "Flowbite's validation pattern: set the color on all three."),
                validationTriples(),

                sectionHeading("FxTextArea",
                        "4 rows by default, wraps, shares the field colors and shadow with FxTextInput."),
                textAreaSection(),

                sectionHeading("FxCheckbox",
                        "All 18 checked colors, plus unchecked, indeterminate and disabled states, and a "
                                + "label with a link."),
                checkboxSection(),

                sectionHeading("FxRadio",
                        "Toggle groups allow one choice (try arrow keys). Colors shown selected."),
                radioSection(),

                sectionHeading("FxToggleSwitch",
                        "Sizes, colors, bare switch, disabled. The knob jumps (no CSS transitions)."),
                toggleSection(),

                sectionHeading("FxRangeSlider",
                        "One thumb, 4 / 8 / 12px track. Tick marks and snapping still work."),
                sliderSection(),

                sectionHeading("FxFileInput",
                        "Click, Enter/Space or drop files. Sizes, colors, multiple, filters, custom texts."),
                fileSection(),

                sectionHeading("Everything together: sign-up form",
                        "Submit empty to see failures, fix the fields to see successes. Reset clears it."),
                signupForm(),

                sectionHeading("Event log", "Everything the controls report lands here."),
                log,
                new Region());
        content.getStyleClass().addAll("bg-background", "p-8");

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // The content must be allowed to be as narrow as the window: without this, the widest
        // unwrappable row sets the content's minimum width and the scroll pane grows a horizontal bar.
        content.setMinWidth(0);
        scroller.getStyleClass().add("bg-background");

        BorderPane root = new BorderPane(scroller);
        root.setTop(header());
        root.getStyleClass().add("bg-background");

        scene = new Scene(root, 1080, 800);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));

        stage.setTitle("Form components test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("Form components test harness");
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
        FxTextInput input = input("name@flowbite.com");
        input.setPrefWidth(360);
        FxLabel label = new FxLabel("Your email", input);
        FxHelperText help = new FxHelperText("Helper text goes here.");

        ComboBox<FxTextInput.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxTextInput.Size.values());
        sizeBox.valueProperty().bindBidirectional(input.sizeProperty());

        ComboBox<FxTextInput.Color> colorBox = new ComboBox<>();
        colorBox.getItems().addAll(FxTextInput.Color.values());
        colorBox.valueProperty().bindBidirectional(input.colorProperty());
        input.colorProperty().addListener((o, was, is) -> {
            label.setColor(labelColor(is));
            help.setColor(helperColor(is));
        });

        ComboBox<FxTextInput.Type> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(FxTextInput.Type.values());
        typeBox.valueProperty().bindBidirectional(input.typeProperty());

        CheckBox shadow = new CheckBox("Shadow");
        shadow.selectedProperty().bindBidirectional(input.shadowProperty());
        CheckBox leftIcon = new CheckBox("Icon");
        leftIcon.selectedProperty().addListener((o, was, is) -> input.setIcon(is ? ICON : null));
        CheckBox rightIcon = new CheckBox("Right icon");
        rightIcon.selectedProperty().addListener((o, was, is) -> input.setRightIcon(is ? ICON : null));

        TextField addonField = new TextField("@");
        addonField.setPrefColumnCount(6);
        CheckBox addon = new CheckBox("Addon");
        addon.selectedProperty().addListener((o, was, is) -> input.setAddon(is ? addonField.getText() : null));
        addonField.textProperty().addListener((o, was, is) -> {
            if (addon.isSelected()) {
                input.setAddon(is);
            }
        });

        CheckBox readOnly = new CheckBox("Read-only");
        input.editableProperty().bind(readOnly.selectedProperty().not());
        CheckBox disabled = new CheckBox("Disabled");
        input.disableProperty().bind(disabled.selectedProperty());
        label.disableProperty().bind(disabled.selectedProperty());

        TextField helpText = new TextField(help.getText());
        help.textProperty().bind(helpText.textProperty());

        input.textProperty().addListener((o, was, is) -> log("playground: text = '" + is + "'"));

        FlowPane row1 = row(new Label("Size"), sizeBox, new Label("Color"), colorBox, new Label("Type"), typeBox);
        FlowPane row2 = row(shadow, leftIcon, rightIcon, addon, addonField, readOnly, disabled);
        HBox row3 = new HBox(12, new Label("Helper text"), helpText);
        row3.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(helpText, Priority.ALWAYS);

        VBox field = new VBox(8, label, input, help);
        field.setMaxWidth(420);

        VBox box = new VBox(12, row1, row2, row3, field);
        styleCard(box);
        return box;
    }

    // ---- text input -------------------------------------------------------------------------

    private FlowPane textInputSizes() {
        FlowPane flow = flow();
        for (FxTextInput.Size s : FxTextInput.Size.values()) {
            FxTextInput in = input("Size " + s);
            in.setSize(s);
            flow.getChildren().add(labeled(s.name(), in));
        }
        FxTextInput filled = input("");
        filled.setText("With text, MD");
        flow.getChildren().add(labeled("with text", filled));
        return flow;
    }

    private FlowPane textInputColors() {
        FlowPane flow = flow();
        for (FxTextInput.Color c : FxTextInput.Color.values()) {
            FxTextInput empty = input("Prompt text");
            empty.setColor(c);
            flow.getChildren().add(labeled(c.name() + " (empty)", empty));

            FxTextInput filled = input("");
            filled.setText("Typed text");
            filled.setColor(c);
            flow.getChildren().add(labeled(c.name() + " (filled)", filled));
        }
        return flow;
    }

    private FlowPane textInputDecorations() {
        FlowPane flow = flow();

        FxTextInput left = input("name@flowbite.com");
        left.setIcon(ICON);
        flow.getChildren().add(labeled("icon", left));

        FxTextInput right = input("Search");
        right.setRightIcon(ICON);
        flow.getChildren().add(labeled("rightIcon", right));

        FxTextInput both = input("Both icons");
        both.setIcon(ICON);
        both.setRightIcon(ICON);
        flow.getChildren().add(labeled("icon + rightIcon", both));

        FxTextInput addon = input("username");
        addon.setAddon("@");
        flow.getChildren().add(labeled("addon \"@\"", addon));

        FxTextInput wideAddon = input("example.com");
        wideAddon.setAddon("https://");
        flow.getChildren().add(labeled("wide addon \"https://\"", wideAddon));

        FxTextInput addonIcon = input("username");
        addonIcon.setAddon("@");
        addonIcon.setIcon(ICON);
        addonIcon.setRightIcon(ICON);
        flow.getChildren().add(labeled("addon + both icons", addonIcon));

        for (FxTextInput.Size s : FxTextInput.Size.values()) {
            FxTextInput sized = input("Size " + s);
            sized.setSize(s);
            sized.setAddon("@");
            sized.setIcon(ICON);
            flow.getChildren().add(labeled(s.name() + " + addon + icon", sized));
        }

        FxTextInput shadow = input("shadow = true");
        shadow.setShadow(true);
        flow.getChildren().add(labeled("shadow", shadow));

        FxTextInput failure = input("Validation color + decorations");
        failure.setColor(FxTextInput.Color.FAILURE);
        failure.setAddon("@");
        failure.setRightIcon(ICON);
        flow.getChildren().add(labeled("FAILURE + addon + rightIcon", failure));
        return flow;
    }

    private FlowPane textInputPasswordAndStates() {
        FlowPane flow = flow();

        FxTextInput password = input("Password");
        password.setType(FxTextInput.Type.PASSWORD);
        password.setText("secret123");
        TextField probe = new TextField();
        probe.setPromptText("paste probe: must stay empty");
        flow.getChildren().add(labeled("PASSWORD (copy/cut blocked)", new VBox(8, password, probe)));

        FxTextInput passwordIcon = input("Password with icons");
        passwordIcon.setType(FxTextInput.Type.PASSWORD);
        passwordIcon.setIcon(ICON);
        passwordIcon.setRightIcon(ICON);
        flow.getChildren().add(labeled("PASSWORD + icons", passwordIcon));

        FxTextInput readOnly = input("");
        readOnly.setText("Read-only text, still selectable");
        readOnly.setEditable(false);
        flow.getChildren().add(labeled("read-only", readOnly));

        FxTextInput disabled = input("");
        disabled.setText("Disabled with text");
        disabled.setDisable(true);
        flow.getChildren().add(labeled("disabled (filled)", disabled));

        FxTextInput disabledEmpty = input("Disabled prompt");
        disabledEmpty.setDisable(true);
        disabledEmpty.setIcon(ICON);
        flow.getChildren().add(labeled("disabled (empty) + icon", disabledEmpty));

        FxTextInput longText = input("");
        longText.setText("A deliberately long value that is wider than the field and has to scroll sideways");
        longText.setIcon(ICON);
        longText.setRightIcon(ICON);
        flow.getChildren().add(labeled("long text between two icons", longText));
        return flow;
    }

    private VBox textInputRuntime() {
        FxTextInput in = input("Type here, then press the buttons");
        in.setPrefWidth(420);
        in.setText("Hello");

        Button toggleLeft = new Button("Toggle icon");
        toggleLeft.setOnAction(e -> in.setIcon(in.getIcon() == null ? ICON : null));
        Button toggleRight = new Button("Toggle rightIcon");
        toggleRight.setOnAction(e -> in.setRightIcon(in.getRightIcon() == null ? ICON : null));

        String[] addons = {null, "@", "https://", "$"};
        int[] addonIndex = {0};
        Button cycleAddon = new Button("Cycle addon");
        cycleAddon.setOnAction(e -> {
            addonIndex[0] = (addonIndex[0] + 1) % addons.length;
            in.setAddon(addons[addonIndex[0]]);
        });

        Button cycleSize = new Button("Cycle size");
        cycleSize.setOnAction(e -> in.setSize(next(FxTextInput.Size.values(), in.getSize())));
        Button cycleColor = new Button("Cycle color");
        cycleColor.setOnAction(e -> in.setColor(next(FxTextInput.Color.values(), in.getColor())));
        Button toggleType = new Button("Toggle PASSWORD");
        toggleType.setOnAction(e -> in.setType(
                in.getType() == FxTextInput.Type.TEXT ? FxTextInput.Type.PASSWORD : FxTextInput.Type.TEXT));
        Button toggleShadow = new Button("Toggle shadow");
        toggleShadow.setOnAction(e -> in.setShadow(!in.isShadow()));

        VBox box = new VBox(10, in,
                buttons(toggleLeft, toggleRight, cycleAddon),
                buttons(cycleSize, cycleColor, toggleType, toggleShadow));
        styleCard(box);
        return box;
    }

    // ---- label ------------------------------------------------------------------------------

    private FlowPane labelSection() {
        FlowPane flow = flow();

        VBox colors = new VBox(6);
        for (FxLabel.Color c : FxLabel.Color.values()) {
            FxLabel colored = new FxLabel("Label color " + c.name());
            colored.setColor(c);
            colors.getChildren().add(colored);
        }
        flow.getChildren().add(labeled("colors", colors));

        FxTextInput target = input("Click the label above");
        FxLabel forInput = new FxLabel("Click me: focuses the input", target);
        flow.getChildren().add(labeled("labelFor: text input", new VBox(6, forInput, target)));

        FxCheckbox checkbox = new FxCheckbox();
        FxLabel forCheckbox = new FxLabel("Click me: toggles the checkbox", checkbox);
        checkbox.selectedProperty().addListener((o, was, is) -> log("label->checkbox: selected = " + is));
        flow.getChildren().add(labeled("labelFor: checkbox (empty text)", row8(checkbox, forCheckbox)));

        FxToggleSwitch toggle = new FxToggleSwitch();
        FxLabel forToggle = new FxLabel("Click me: flips the switch", toggle);
        toggle.selectedProperty().addListener((o, was, is) -> log("label->toggle: selected = " + is));
        flow.getChildren().add(labeled("labelFor: toggle switch", row8(toggle, forToggle)));

        FxFileInput file = new FxFileInput();
        file.setPrefWidth(280);
        FxLabel forFile = new FxLabel("Click me: opens the file dialog", file);
        flow.getChildren().add(labeled("labelFor: file input", new VBox(6, forFile, file)));

        FxLabel disabled = new FxLabel("Disabled label");
        disabled.setDisable(true);
        flow.getChildren().add(labeled("disabled", disabled));
        return flow;
    }

    // ---- helper text ------------------------------------------------------------------------

    private FlowPane helperTextSection() {
        FlowPane flow = flow();

        VBox colors = new VBox(4);
        for (FxHelperText.Color c : FxHelperText.Color.values()) {
            FxHelperText help = new FxHelperText("Helper text color " + c.name());
            help.setColor(c);
            colors.getChildren().add(help);
        }
        flow.getChildren().add(labeled("colors", colors));

        FxHelperText rich = new FxHelperText();
        rich.setColor(FxHelperText.Color.FAILURE);
        rich.getChildren().addAll(FxHelperText.strong("Oops!"), new Text(" Username already taken! "),
                new Hyperlink("Pick another"));
        rich.setPrefWidth(300);
        flow.getChildren().add(labeled("rich: strong lead-in + hyperlink", rich));

        FxHelperText wrapping = new FxHelperText("A deliberately long helper text that has to wrap onto several "
                + "lines inside a narrow parent instead of running off to the right.");
        wrapping.setPrefWidth(260);
        flow.getChildren().add(labeled("wraps at 260px", wrapping));

        FxHelperText swap = new FxHelperText("Click the button to replace my text");
        int[] n = {0};
        Button replace = new Button("setText");
        replace.setOnAction(e -> swap.setText("Replaced " + (++n[0]) + " times"));
        Button clear = new Button("setText(null)");
        clear.setOnAction(e -> swap.setText(null));
        Button recolor = new Button("Cycle color");
        recolor.setOnAction(e -> swap.setColor(next(FxHelperText.Color.values(), swap.getColor())));
        flow.getChildren().add(labeled("runtime text and color",
                new VBox(6, swap, buttons(replace, clear, recolor))));
        return flow;
    }

    private FlowPane validationTriples() {
        FlowPane flow = flow();
        String[] hints = {
                "We'll never share your email.",
                "Heads up: this domain looks unusual.",
                "Oops! That address is already registered.",
                "Alright! Username available!",
                "Some neutral information."
        };
        FxTextInput.Color[] colors = {
                FxTextInput.Color.INFO, FxTextInput.Color.WARNING, FxTextInput.Color.FAILURE,
                FxTextInput.Color.SUCCESS, FxTextInput.Color.GRAY };
        for (int i = 0; i < colors.length; i++) {
            FxTextInput in = input("name@flowbite.com");
            in.setColor(colors[i]);
            in.setIcon(ICON);
            FxLabel label = new FxLabel("Your email (" + colors[i] + ")", in);
            label.setColor(labelColor(colors[i]));
            FxHelperText help = new FxHelperText(hints[i]);
            help.setColor(helperColor(colors[i]));
            flow.getChildren().add(labeled(colors[i].name(), new VBox(8, label, in, help)));
        }
        return flow;
    }

    // ---- text area --------------------------------------------------------------------------

    private FlowPane textAreaSection() {
        FlowPane flow = flow();

        flow.getChildren().add(labeled("default (4 rows)", area("Leave a comment...", 4)));
        flow.getChildren().add(labeled("prefRowCount = 8", area("Eight rows", 8)));
        flow.getChildren().add(labeled("prefRowCount = 2", area("Two rows", 2)));

        for (FxTextArea.Color c : FxTextArea.Color.values()) {
            FxTextArea a = area("Color " + c, 3);
            a.setColor(c);
            a.setText("Typed text in " + c);
            flow.getChildren().add(labeled(c.name(), a));
        }

        FxTextArea shadow = area("shadow = true", 3);
        shadow.setShadow(true);
        flow.getChildren().add(labeled("shadow", shadow));

        FxTextArea wrap = area("", 3);
        wrap.setText("This long text has no line breaks and must wrap at the edge of the textarea instead of "
                + "scrolling sideways, like a browser's textarea does.");
        flow.getChildren().add(labeled("wraps by default", wrap));

        FxTextArea readOnly = area("", 3);
        readOnly.setText("Read-only");
        readOnly.setEditable(false);
        flow.getChildren().add(labeled("read-only", readOnly));

        FxTextArea disabled = area("Disabled", 3);
        disabled.setDisable(true);
        flow.getChildren().add(labeled("disabled", disabled));
        return flow;
    }

    private static FxTextArea area(String prompt, int rows) {
        FxTextArea a = new FxTextArea();
        a.setPromptText(prompt);
        a.setPrefRowCount(rows);
        a.setPrefWidth(280);
        return a;
    }

    // ---- checkbox ---------------------------------------------------------------------------

    private VBox checkboxSection() {
        FlowPane colors = new FlowPane(20, 10);
        for (FxCheckbox.Color c : FxCheckbox.Color.values()) {
            FxCheckbox cb = new FxCheckbox(c.name());
            cb.setColor(c);
            cb.setSelected(true);
            colors.getChildren().add(cb);
        }

        FxCheckbox unchecked = new FxCheckbox("Unchecked");
        FxCheckbox checked = new FxCheckbox("Checked");
        checked.setSelected(true);
        FxCheckbox indeterminate = new FxCheckbox("Indeterminate (3 states)");
        indeterminate.setAllowIndeterminate(true);
        indeterminate.setIndeterminate(true);
        FxCheckbox disabled = new FxCheckbox("Disabled");
        disabled.setDisable(true);
        FxCheckbox disabledChecked = new FxCheckbox("Disabled + checked");
        disabledChecked.setDisable(true);
        disabledChecked.setSelected(true);
        FxCheckbox disabledIndeterminate = new FxCheckbox("Disabled + indeterminate");
        disabledIndeterminate.setDisable(true);
        disabledIndeterminate.setAllowIndeterminate(true);
        disabledIndeterminate.setIndeterminate(true);
        for (FxCheckbox cb : new FxCheckbox[] {unchecked, checked, indeterminate}) {
            cb.setOnAction(e -> log("checkbox '" + cb.getText() + "': selected=" + cb.isSelected()
                    + ", indeterminate=" + cb.isIndeterminate()));
        }
        FlowPane states = new FlowPane(20, 10, unchecked, checked, indeterminate, disabled, disabledChecked,
                disabledIndeterminate);

        FxCheckbox agree = new FxCheckbox();
        FxLabel agreeLabel = new FxLabel("I agree with the ", agree);
        Hyperlink terms = new Hyperlink("terms and conditions");
        terms.setOnAction(e -> log("terms link clicked"));
        HBox withLink = row8(agree, new HBox(0, agreeLabel, terms));
        withLink.setAlignment(Pos.CENTER_LEFT);

        FxCheckbox twoLine = new FxCheckbox();
        FxLabel twoLineLabel = new FxLabel("Free shipping via Flowbite", twoLine);
        FxHelperText twoLineHelp = new FxHelperText("For orders including only digital items.");
        HBox twoLineRow = new HBox(8, twoLine, new VBox(0, twoLineLabel, twoLineHelp));
        twoLineRow.setAlignment(Pos.TOP_LEFT);

        VBox box = new VBox(14,
                labeled("checked, all 18 colors", colors),
                labeled("states", states),
                labeled("FxLabel with a link (click the text before the link)", withLink),
                labeled("FxLabel + helper text beside a checkbox", twoLineRow));
        return box;
    }

    // ---- radio ------------------------------------------------------------------------------

    private VBox radioSection() {
        ToggleGroup countries = new ToggleGroup();
        VBox countryBox = new VBox(8);
        String[][] options = {{"United States", "USA"}, {"Germany", "Germany"}, {"Spain", "Spain"},
                {"United Kingdom", "UK"}, {"China (disabled)", "China"}};
        for (String[] option : options) {
            FxRadio r = new FxRadio(option[0]);
            r.setToggleGroup(countries);
            r.setUserData(option[1]);
            if (option[1].equals("China")) {
                r.setDisable(true);
            }
            countryBox.getChildren().add(r);
        }
        ((FxRadio) countryBox.getChildren().get(0)).setSelected(true);
        Label readout = new Label();
        readout.getStyleClass().addAll("text-xs", "text-muted");
        countries.selectedToggleProperty().addListener((o, was, is) -> {
            readout.setText("selected userData: " + (is == null ? "none" : is.getUserData()));
            log("radio group: " + readout.getText());
        });
        readout.setText("selected userData: USA");
        countryBox.getChildren().add(readout);

        FlowPane colors = new FlowPane(20, 10);
        for (FxRadio.Color c : FxRadio.Color.values()) {
            FxRadio r = new FxRadio(c.name());
            r.setColor(c);
            r.setToggleGroup(new ToggleGroup()); // its own group, so every color shows selected
            r.setSelected(true);
            colors.getChildren().add(r);
        }

        FxRadio selectedDisabled = new FxRadio("Disabled + selected");
        selectedDisabled.setSelected(true);
        selectedDisabled.setDisable(true);
        FxRadio plain = new FxRadio("No group, unselected");
        FlowPane states = new FlowPane(20, 10, plain, selectedDisabled);

        return new VBox(14,
                labeled("group (one choice, arrow keys move)", countryBox),
                labeled("selected, all 18 colors", colors),
                labeled("states", states));
    }

    // ---- toggle switch ----------------------------------------------------------------------

    private VBox toggleSection() {
        FlowPane sizes = new FlowPane(24, 12);
        for (FxToggleSwitch.Size s : FxToggleSwitch.Size.values()) {
            FxToggleSwitch off = new FxToggleSwitch("Size " + s + " (off)");
            off.setSize(s);
            FxToggleSwitch on = new FxToggleSwitch("Size " + s + " (on)");
            on.setSize(s);
            on.setSelected(true);
            off.selectedProperty().addListener((o, was, is) -> log("toggle " + s + ": " + is));
            sizes.getChildren().addAll(off, on);
        }

        FlowPane colors = new FlowPane(24, 12);
        for (FxToggleSwitch.Color c : FxToggleSwitch.Color.values()) {
            FxToggleSwitch t = new FxToggleSwitch(c.name());
            t.setColor(c);
            t.setSelected(true);
            colors.getChildren().add(t);
        }

        FxToggleSwitch bare = new FxToggleSwitch();
        FxToggleSwitch bareOn = new FxToggleSwitch();
        bareOn.setSelected(true);
        FxToggleSwitch disabledOff = new FxToggleSwitch("Disabled (off)");
        disabledOff.setDisable(true);
        FxToggleSwitch disabledOn = new FxToggleSwitch("Disabled (on)");
        disabledOn.setDisable(true);
        disabledOn.setSelected(true);
        FlowPane states = new FlowPane(24, 12, bare, bareOn, disabledOff, disabledOn);

        return new VBox(14,
                labeled("sizes", sizes),
                labeled("on, all 18 colors", colors),
                labeled("bare (no text), disabled", states));
    }

    // ---- range slider -----------------------------------------------------------------------

    private VBox sliderSection() {
        VBox sizes = new VBox(14);
        for (FxRangeSlider.Size s : FxRangeSlider.Size.values()) {
            FxRangeSlider slider = new FxRangeSlider(0, 100, 40);
            slider.setSize(s);
            slider.setPrefWidth(320);
            Label readout = new Label();
            readout.getStyleClass().addAll("text-xs", "text-muted");
            readout.textProperty().bind(slider.valueProperty().asString(s + " value: %.0f"));
            sizes.getChildren().add(new VBox(4, slider, readout));
        }

        FxRangeSlider disabled = new FxRangeSlider(0, 100, 60);
        disabled.setDisable(true);
        disabled.setPrefWidth(320);

        FxRangeSlider ticks = new FxRangeSlider(0, 100, 50);
        ticks.setShowTickMarks(true);
        ticks.setShowTickLabels(true);
        ticks.setMajorTickUnit(25);
        ticks.setMinorTickCount(4);
        ticks.setSnapToTicks(true);
        ticks.setPrefWidth(320);
        ticks.valueProperty().addListener((o, was, is) -> log("snapping slider: " + is.intValue()));

        FxRangeSlider small = new FxRangeSlider(0, 10, 3);
        small.setBlockIncrement(1);
        small.setPrefWidth(320);
        Label smallReadout = new Label();
        smallReadout.getStyleClass().addAll("text-xs", "text-muted");
        smallReadout.textProperty().bind(small.valueProperty().asString("0..10 range, value: %.2f"));

        Button reset = new Button("Reset all to 0 / min");
        reset.setOnAction(e -> {
            for (Node n : sizes.getChildren()) {
                ((FxRangeSlider) ((VBox) n).getChildren().get(0)).setValue(0);
            }
        });

        return new VBox(14,
                labeled("sizes SM / MD / LG", new VBox(10, sizes, reset)),
                labeled("disabled", disabled),
                labeled("tick marks + labels + snap", ticks),
                labeled("custom range", new VBox(4, small, smallReadout)));
    }

    // ---- file input -------------------------------------------------------------------------

    private VBox fileSection() {
        FlowPane sizes = flow();
        for (FxFileInput.Size s : FxFileInput.Size.values()) {
            FxFileInput f = new FxFileInput();
            f.setSize(s);
            f.setPrefWidth(300);
            sizes.getChildren().add(labeled(s.name(), f));
        }

        FlowPane colors = flow();
        for (FxFileInput.Color c : FxFileInput.Color.values()) {
            FxFileInput f = new FxFileInput();
            f.setColor(c);
            f.setPrefWidth(300);
            colors.getChildren().add(labeled(c.name(), f));
        }

        FxFileInput shadow = new FxFileInput();
        shadow.setShadow(true);
        shadow.setPrefWidth(300);

        FxFileInput custom = new FxFileInput();
        custom.setButtonText("Browse...");
        custom.setPlaceholder("Nothing selected yet");
        custom.setPrefWidth(300);

        FxFileInput disabled = new FxFileInput();
        disabled.setDisable(true);
        disabled.setPrefWidth(300);

        FlowPane variants = flow();
        variants.getChildren().addAll(
                labeled("shadow", shadow),
                labeled("custom button text and placeholder", custom),
                labeled("disabled (click and drop must do nothing)", disabled));

        FxFileInput single = new FxFileInput();
        single.setPrefWidth(300);
        FxFileInput multiple = new FxFileInput();
        multiple.setMultiple(true);
        multiple.setPrefWidth(300);
        FxFileInput images = new FxFileInput();
        images.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        images.setPrefWidth(300);

        Label singleReadout = fileReadout(single, "single");
        Label multipleReadout = fileReadout(multiple, "multiple");
        Label imagesReadout = fileReadout(images, "images only");

        Button clearAll = new Button("clear() all three");
        clearAll.setOnAction(e -> {
            single.clear();
            multiple.clear();
            images.clear();
            log("cleared the three file inputs");
        });

        FlowPane behaviour = flow();
        behaviour.getChildren().addAll(
                labeled("single file", new VBox(6, single, singleReadout)),
                labeled("multiple = true (\"N files\" when several)", new VBox(6, multiple, multipleReadout)),
                labeled("filter: png/jpg only, also applies to dropped files", new VBox(6, images, imagesReadout)),
                labeled("reset", clearAll));

        return new VBox(14,
                labeled("sizes", sizes),
                labeled("colors", colors),
                variants,
                behaviour);
    }

    private Label fileReadout(FxFileInput input, String name) {
        Label readout = new Label();
        readout.getStyleClass().addAll("text-xs", "text-muted");
        readout.textProperty().bind(Bindings.createStringBinding(
                () -> "getFile(): " + (input.getFile() == null ? "null" : input.getFile().getName())
                        + " | getFiles(): " + input.getFiles().size(),
                input.fileProperty(), input.getFiles()));
        input.fileProperty().addListener((o, was, is) ->
                log(name + ": file = " + (is == null ? "null" : is.getName())));
        return readout;
    }

    // ---- everything together ----------------------------------------------------------------

    private VBox signupForm() {
        FxTextInput username = input("bonnie");
        username.setAddon("@");
        FxLabel usernameLabel = new FxLabel("Username", username);
        FxHelperText usernameHelp = new FxHelperText();

        FxTextInput email = input("name@flowbite.com");
        email.setIcon(ICON);
        FxLabel emailLabel = new FxLabel("Your email", email);
        FxHelperText emailHelp = new FxHelperText();

        FxTextInput password = input("At least 8 characters");
        password.setType(FxTextInput.Type.PASSWORD);
        password.setRightIcon(ICON);
        FxLabel passwordLabel = new FxLabel("Password", password);
        FxHelperText passwordHelp = new FxHelperText();

        FxTextInput confirm = input("Repeat the password");
        confirm.setType(FxTextInput.Type.PASSWORD);
        FxLabel confirmLabel = new FxLabel("Confirm password", confirm);
        FxHelperText confirmHelp = new FxHelperText();

        FxTextArea bio = area("Tell us about yourself...", 3);
        bio.setPrefWidth(420);
        FxLabel bioLabel = new FxLabel("Bio", bio);

        ToggleGroup plan = new ToggleGroup();
        FxRadio free = plan("Free", "free", plan);
        FxRadio pro = plan("Pro", "pro", plan);
        FxRadio team = plan("Team", "team", plan);
        free.setSelected(true);
        HBox plans = new HBox(20, free, pro, team);

        FxToggleSwitch notifications = new FxToggleSwitch("Email me product news");
        FxRangeSlider volume = new FxRangeSlider(0, 100, 50);
        volume.setPrefWidth(420);
        Label volumeLabel = new Label();
        volumeLabel.textProperty().bind(volume.valueProperty().asString("Notification volume: %.0f"));
        volumeLabel.getStyleClass().addAll("text-sm", "font-medium", "text-body");

        FxFileInput avatar = new FxFileInput();
        avatar.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        avatar.setPrefWidth(420);
        FxLabel avatarLabel = new FxLabel("Avatar", avatar);

        FxCheckbox terms = new FxCheckbox("I agree with the terms and conditions");
        FxHelperText termsHelp = new FxHelperText();

        FxButton submit = new FxButton("Register new account");
        FxButton reset = new FxButton("Reset");
        reset.setVariant(FxButton.Variant.SECONDARY);

        submit.setOnAction(e -> {
            boolean ok = true;
            ok &= check(usernameLabel, username, usernameHelp, !username.getText().isBlank(),
                    "Username is required.", "Alright! Username available!");
            ok &= check(emailLabel, email, emailHelp, email.getText().contains("@"),
                    "That does not look like an email address.", "Looks good!");
            ok &= check(passwordLabel, password, passwordHelp, password.getText().length() >= 8,
                    "Use at least 8 characters.", "Strong enough.");
            ok &= check(confirmLabel, confirm, confirmHelp,
                    !confirm.getText().isEmpty() && confirm.getText().equals(password.getText()),
                    "The passwords do not match.", "Passwords match.");
            boolean accepted = terms.isSelected();
            termsHelp.setColor(accepted ? FxHelperText.Color.SUCCESS : FxHelperText.Color.FAILURE);
            termsHelp.setText(accepted ? "" : "You must accept the terms.");
            ok &= accepted;

            if (ok) {
                log("form submitted: user=" + username.getText() + ", email=" + email.getText()
                        + ", plan=" + plan.getSelectedToggle().getUserData()
                        + ", news=" + notifications.isSelected()
                        + ", volume=" + (int) volume.getValue()
                        + ", avatar=" + (avatar.getFile() == null ? "none" : avatar.getFile().getName())
                        + ", bio chars=" + bio.getText().length());
            } else {
                log("form invalid");
            }
        });

        reset.setOnAction(e -> {
            for (FxTextInput f : new FxTextInput[] {username, email, password, confirm}) {
                f.clear();
                f.setColor(FxTextInput.Color.GRAY);
            }
            for (FxLabel l : new FxLabel[] {usernameLabel, emailLabel, passwordLabel, confirmLabel}) {
                l.setColor(FxLabel.Color.DEFAULT);
            }
            for (FxHelperText h : new FxHelperText[] {usernameHelp, emailHelp, passwordHelp, confirmHelp,
                    termsHelp}) {
                h.setColor(FxHelperText.Color.DEFAULT);
                h.setText("");
            }
            bio.clear();
            free.setSelected(true);
            notifications.setSelected(false);
            volume.setValue(50);
            avatar.clear();
            terms.setSelected(false);
            log("form reset");
        });

        VBox form = new VBox(16,
                group(usernameLabel, username, usernameHelp),
                group(emailLabel, email, emailHelp),
                group(passwordLabel, password, passwordHelp),
                group(confirmLabel, confirm, confirmHelp),
                group(bioLabel, bio, null),
                new VBox(8, new FxLabel("Plan"), plans),
                notifications,
                new VBox(8, volumeLabel, volume),
                group(avatarLabel, avatar, null),
                new VBox(4, terms, termsHelp),
                new HBox(8, submit, reset));
        form.setMaxWidth(460);
        styleCard(form);
        return form;
    }

    private static FxRadio plan(String text, String value, ToggleGroup group) {
        FxRadio r = new FxRadio(text);
        r.setToggleGroup(group);
        r.setUserData(value);
        return r;
    }

    private static VBox group(FxLabel label, Node field, FxHelperText help) {
        VBox box = new VBox(8, label, field);
        if (help != null) {
            box.getChildren().add(help);
        }
        return box;
    }

    /** Sets label, field and helper text to success or failure; returns {@code ok}. */
    private static boolean check(FxLabel label, FxTextInput field, FxHelperText help, boolean ok,
            String error, String success) {
        label.setColor(ok ? FxLabel.Color.SUCCESS : FxLabel.Color.FAILURE);
        field.setColor(ok ? FxTextInput.Color.SUCCESS : FxTextInput.Color.FAILURE);
        help.setColor(ok ? FxHelperText.Color.SUCCESS : FxHelperText.Color.FAILURE);
        help.setText(ok ? success : error);
        return ok;
    }

    // ---- helpers ----------------------------------------------------------------------------

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static FxTextInput input(String prompt) {
        FxTextInput in = new FxTextInput();
        in.setPromptText(prompt);
        in.setPrefWidth(280);
        return in;
    }

    private static FxLabel.Color labelColor(FxTextInput.Color c) {
        return c == FxTextInput.Color.GRAY ? FxLabel.Color.DEFAULT : FxLabel.Color.valueOf(c.name());
    }

    private static FxHelperText.Color helperColor(FxTextInput.Color c) {
        return c == FxTextInput.Color.GRAY ? FxHelperText.Color.DEFAULT : FxHelperText.Color.valueOf(c.name());
    }

    /** The constant after {@code current}, wrapping around. */
    private static <E extends Enum<E>> E next(E[] values, E current) {
        return values[(current.ordinal() + 1) % values.length];
    }

    /** A row of controls that wraps onto further lines when the window is narrow. */
    private static FlowPane row(Node... nodes) {
        FlowPane row = new FlowPane(12, 8, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setRowValignment(javafx.geometry.VPos.CENTER);
        row.setMinWidth(0);
        return row;
    }

    /** A row of buttons that wraps onto further lines when the window is narrow. */
    private static FlowPane buttons(Node... nodes) {
        FlowPane row = new FlowPane(8, 8, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinWidth(0);
        return row;
    }

    private static HBox row8(Node... nodes) {
        HBox row = new HBox(8, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static FlowPane flow() {
        FlowPane flow = new FlowPane(12, 12);
        flow.setMinWidth(0); // wrap onto more lines instead of forcing the page wider
        return flow;
    }

    private static void styleCard(VBox box) {
        box.setMinWidth(0);
        box.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
    }

    private static VBox sectionHeading(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("text-lg", "font-semibold", "text-body");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("text-sm", "text-muted");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMinWidth(0);
        VBox heading = new VBox(2, titleLabel, subtitleLabel);
        heading.setMinWidth(0);
        return heading;
    }

    private static VBox labeled(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().addAll("text-xs", "text-muted");
        label.setWrapText(true);
        label.setMinWidth(0);
        if (content instanceof Region region) {
            region.setMinWidth(0); // may shrink (and wrap, for text flows) instead of widening the page
        }
        VBox wrapper = new VBox(6, label, content);
        wrapper.setMinWidth(0);
        wrapper.getStyleClass().addAll("bg-surface", "p-4", "rounded-lg", "border", "border-default");
        return wrapper;
    }

    public static void main(String[] args) {
        launch(args);
    }
}