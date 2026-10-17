package dev.fxkit.showcase;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.components.modal.FxModal;
import dev.fxkit.core.components.modal.FxModalBody;
import dev.fxkit.core.components.modal.FxModalFooter;
import dev.fxkit.core.components.modal.FxModalHeader;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Isolated test harness for {@link FxModal}: a live playground, every Size and
 * Position,
 * popup / dismissible / initialFocus, onClose (log, veto, reset), scrolling
 * bodies, missing parts,
 * runtime changes while the modal is open, and programmatic show/close edge
 * cases.
 *
 * <p>
 * Swap this in for {@code ShowcaseApp} as the run configuration's main class
 * while iterating.
 * A modal blocks its owner window, so the theme toggle cannot be used while one
 * is open: switch the
 * theme first, then open the modal and check that it picked up the owner's
 * stylesheets and dark class.
 */
public class FxModalTestApp extends Application {

    private static final String SAMPLE_TITLE = "Terms of Service";

    private static final String LOREM = "With less than a month to go before the European Union enacts new "
            + "consumer privacy laws for its citizens, companies around the world are updating their terms of "
            + "service agreements to comply.";

    private Scene scene;
    private Button themeToggle;
    private TextArea log;

    @Override
    public void start(Stage stage) {
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(6);
        log.setPromptText("Event log (open/close, onClose, actions)...");

        VBox content = new VBox(28,
                sectionHeading("Playground",
                        "Every property live: title, size, position, dismissible, popup, header / close "
                                + "button / body / footer, initial focus and onClose veto. Open it, then use "
                                + "the footer buttons to change size and position while it is showing."),
                playground(),

                sectionHeading("Size (all 10)",
                        "Card max width: SM 384 ... XL7 1280. The card is narrower when the window is "
                                + "(16px gutter each side). Shrink the window and reopen to check."),
                sizeGrid(),

                sectionHeading("Position (all 9)",
                        "Card is placed in the window with a 16px gutter. Backdrop click closes these."),
                positionGrid(),

                sectionHeading("Popup + dismissible",
                        "Popup: header has only the close button, no borders, body has no top padding. "
                                + "Dismissible: a click on the backdrop closes, a click inside the card does not."),
                popupSection(),

                sectionHeading("onClose",
                        "Runs for the X button, Esc and (if dismissible) a backdrop click, then the modal hides "
                                + "unless the event was consumed."),
                onCloseSection(),

                sectionHeading("Initial focus",
                        "With initialFocus the email field is focused on open. Without it the modal itself is "
                                + "focused, so the close button must NOT light up."),
                initialFocusSection(),

                sectionHeading("Scrolling",
                        "Long body scrolls while header and footer stay pinned. Max height is 90% of the window."),
                scrollingSection(),

                sectionHeading("Header variants",
                        "No title (close button only), no close button, very long title."),
                headerVariants(),

                sectionHeading("Missing parts",
                        "Any of header, body and footer can be null. Check each combination lays out cleanly."),
                missingParts(),

                sectionHeading("Runtime changes while open",
                        "Swap body, remove footer and change size or position on a showing modal."),
                runtimeSection(),

                sectionHeading("Programmatic + edge cases",
                        "show() twice, show() on a detached node, close() / requestClose() when closed, "
                                + "close() after a 3s timeout (onClose must NOT fire), repeated open/close."),
                edgeCases(),

                sectionHeading("Event log", "showing changes, onClose calls and button actions land here."),
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

        stage.setTitle("FxModal test");
        stage.setScene(scene);
        stage.show();
    }

    // ---- header / theme toggle
    // --------------------------------------------------------------

    private HBox header() {
        Label title = new Label("FxModal test harness");
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
        FxModal modal = new FxModal();
        track("playground", modal);

        // header
        FxModalHeader header = new FxModalHeader();
        TextField titleField = new TextField(SAMPLE_TITLE);
        titleField.setPromptText("title");
        header.titleProperty().bind(titleField.textProperty());

        // body: an input (for initialFocus) and N paragraphs
        TextField input = new TextField();
        input.setPromptText("initial focus target");
        FxModalBody body = new FxModalBody();
        body.getStyleClass().add("gap-4");
        int[] paragraphs = { 2 };
        Runnable fillBody = () -> {
            List<Node> nodes = new ArrayList<>();
            nodes.add(input);
            for (int i = 1; i <= paragraphs[0]; i++) {
                nodes.add(paragraph(i + ". " + LOREM));
            }
            body.getChildren().setAll(nodes);
        };
        fillBody.run();

        // footer
        Button next = new Button("Next size");
        next.setOnAction(e -> {
            modal.setSize(cycle(FxModal.Size.values(), modal.getSize()));
            log("playground: size -> " + modal.getSize());
        });
        Button nextPos = new Button("Next position");
        nextPos.setOnAction(e -> {
            modal.setPosition(cycle(FxModal.Position.values(), modal.getPosition()));
            log("playground: position -> " + modal.getPosition());
        });
        Button requestClose = new Button("requestClose()");
        requestClose.setOnAction(e -> modal.requestClose());
        Button closeNow = new Button("close()");
        closeNow.setOnAction(e -> modal.close());
        FxModalFooter footer = new FxModalFooter(next, nextPos, requestClose, closeNow);
        footer.setSpacing(8);

        modal.setHeader(header);
        modal.setBody(body);
        modal.setFooter(footer);

        // onClose: always installed, the checkbox decides whether it vetoes
        CheckBox veto = new CheckBox("onClose vetoes (consume)");
        modal.setOnClose(e -> {
            log("playground: onClose fired" + (veto.isSelected() ? " -> consumed, stays open" : ""));
            if (veto.isSelected()) {
                e.consume();
            }
        });

        // controls
        ComboBox<FxModal.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(FxModal.Size.values());
        sizeBox.valueProperty().bindBidirectional(modal.sizeProperty());

        ComboBox<FxModal.Position> positionBox = new ComboBox<>();
        positionBox.getItems().addAll(FxModal.Position.values());
        positionBox.valueProperty().bindBidirectional(modal.positionProperty());

        CheckBox dismissible = new CheckBox("Dismissible");
        dismissible.selectedProperty().bindBidirectional(modal.dismissibleProperty());

        CheckBox popup = new CheckBox("Popup");
        popup.selectedProperty().bindBidirectional(modal.popupProperty());

        CheckBox showHeader = new CheckBox("Header");
        showHeader.setSelected(true);
        showHeader.selectedProperty().addListener((o, was, is) -> modal.setHeader(is ? header : null));

        CheckBox closeButton = new CheckBox("Close button");
        closeButton.setSelected(true);
        header.closeButtonVisibleProperty().bind(closeButton.selectedProperty());

        CheckBox showBody = new CheckBox("Body");
        showBody.setSelected(true);
        showBody.selectedProperty().addListener((o, was, is) -> modal.setBody(is ? body : null));

        CheckBox showFooter = new CheckBox("Footer");
        showFooter.setSelected(true);
        showFooter.selectedProperty().addListener((o, was, is) -> modal.setFooter(is ? footer : null));

        CheckBox focus = new CheckBox("Initial focus on input");
        focus.selectedProperty().addListener((o, was, is) -> modal.setInitialFocus(is ? input : null));

        Button more = new Button("More text");
        more.setOnAction(e -> {
            paragraphs[0]++;
            fillBody.run();
        });
        Button less = new Button("Less text");
        less.setOnAction(e -> {
            paragraphs[0] = Math.max(0, paragraphs[0] - 1);
            fillBody.run();
        });

        Button open = new Button("Open modal");
        open.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-md", "font-semibold");
        open.setOnAction(e -> modal.show(open));

        HBox row1 = new HBox(10, new Label("Title"), titleField, new Label("Size"), sizeBox,
                new Label("Position"), positionBox);
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleField, Priority.ALWAYS);
        FlowPane row2 = new FlowPane(16, 8, dismissible, popup, showHeader, closeButton, showBody, showFooter,
                focus, veto);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox row3 = new HBox(10, more, less, open);
        row3.setAlignment(Pos.CENTER_LEFT);

        return panel(row1, row2, row3);
    }

    // ---- size
    // -------------------------------------------------------------------------------------

    private FlowPane sizeGrid() {
        FlowPane flow = new FlowPane(12, 12);
        for (FxModal.Size size : FxModal.Size.values()) {
            FxModal modal = simple(size.name() + " (" + (int) size.getMaxWidth() + "px)",
                    "Max width " + (int) size.getMaxWidth() + "px. " + LOREM);
            modal.setSize(size);
            modal.setDismissible(true);
            track("size " + size, modal);
            flow.getChildren().add(labeled(size.name(), openButton(size.name(), modal)));
        }
        return flow;
    }

    // ---- position
    // ---------------------------------------------------------------------------------

    private FlowPane positionGrid() {
        FxModal modal = simple("Placement", "Placed by FxModal.position.");
        modal.setSize(FxModal.Size.MD);
        modal.setDismissible(true);
        track("position", modal);

        FlowPane flow = new FlowPane(12, 12);
        for (FxModal.Position p : FxModal.Position.values()) {
            Button b = new Button(p.name().toLowerCase().replace('_', '-'));
            b.setOnAction(e -> {
                modal.setPosition(p);
                modal.show(b);
            });
            flow.getChildren().add(labeled(p.name(), b));
        }
        return flow;
    }

    // ---- popup + dismissible
    // ----------------------------------------------------------------------

    private FlowPane popupSection() {
        FlowPane flow = new FlowPane(12, 12);

        // confirmation popup
        FxModal confirm = new FxModal();
        confirm.setPopup(true);
        confirm.setSize(FxModal.Size.MD);
        confirm.setHeader(new FxModalHeader());
        Label question = new Label("Are you sure you want to delete this product?");
        question.setWrapText(true);
        question.getStyleClass().addAll("text-lg", "text-muted");
        Button yes = new Button("Yes, I'm sure");
        yes.setOnAction(e -> {
            log("popup: confirmed");
            confirm.requestClose();
        });
        Button no = new Button("No, cancel");
        no.setOnAction(e -> {
            log("popup: cancelled");
            confirm.requestClose();
        });
        HBox buttons = new HBox(16, yes, no);
        buttons.setAlignment(Pos.CENTER);
        VBox inner = new VBox(16, question, buttons);
        inner.setAlignment(Pos.CENTER);
        confirm.setBody(new FxModalBody(inner));
        track("popup confirm", confirm);
        flow.getChildren().add(labeled("popup (header only has X)", openButton("Open popup", confirm)));

        // popup with footer: footer top border must disappear
        FxModal popupFooter = simple("Popup + footer", "In a popup the footer loses its top border.");
        popupFooter.setPopup(true);
        popupFooter.setSize(FxModal.Size.MD);
        track("popup footer", popupFooter);
        flow.getChildren().add(labeled("popup + titled header + footer", openButton("Open", popupFooter)));

        // dismissible
        FxModal dismissible = simple("Dismissible", "Click anywhere on the backdrop to close this modal.");
        dismissible.setDismissible(true);
        track("dismissible", dismissible);
        flow.getChildren().add(labeled("dismissible = true", openButton("Open", dismissible)));

        // not dismissible
        FxModal fixed = simple("Not dismissible", "Backdrop clicks must do nothing. Use X, Esc or the button.");
        track("not dismissible", fixed);
        flow.getChildren().add(labeled("dismissible = false (default)", openButton("Open", fixed)));

        return flow;
    }

    // ---- onClose
    // ----------------------------------------------------------------------------------

    private FlowPane onCloseSection() {
        FlowPane flow = new FlowPane(12, 12);

        // no handler
        FxModal none = simple("No onClose", "With no handler the modal simply hides.");
        track("onClose none", none);
        flow.getChildren().add(labeled("no handler", openButton("Open", none)));

        // logging handler
        FxModal logging = simple("Logging onClose", "X, Esc and the button all run the handler, then hide.");
        logging.setDismissible(true);
        logging.setOnClose(e -> log("onClose(logging): handler ran, modal will hide"));
        track("onClose logging", logging);
        flow.getChildren().add(labeled("handler runs, then hides", openButton("Open", logging)));

        // veto when dirty
        FxModal veto = new FxModal();
        veto.setSize(FxModal.Size.MD);
        veto.setHeader(new FxModalHeader("Unsaved changes"));
        TextField field = new TextField();
        field.setPromptText("Type something to make the form dirty");
        Label hint = paragraph("While the field has text, X / Esc / backdrop are vetoed.");
        FxModalBody vetoBody = new FxModalBody(hint, field);
        vetoBody.getStyleClass().add("gap-4");
        veto.setBody(vetoBody);
        veto.setDismissible(true);
        Button discard = new Button("Discard and close");
        discard.setOnAction(e -> {
            field.clear();
            veto.requestClose();
        });
        Button force = new Button("close() (skips onClose)");
        force.setOnAction(e -> veto.close());
        FxModalFooter vetoFooter = new FxModalFooter(discard, force);
        vetoFooter.setSpacing(8);
        veto.setFooter(vetoFooter);
        veto.setOnClose(e -> {
            if (!field.getText().isEmpty()) {
                e.consume();
                log("onClose(veto): consumed, form is dirty");
            } else {
                log("onClose(veto): clean, closing");
            }
        });
        track("onClose veto", veto);
        flow.getChildren().add(labeled("veto when dirty", openButton("Open", veto)));

        // reset on close (Flowbite form example)
        FxModal form = new FxModal();
        form.setPopup(true);
        form.setSize(FxModal.Size.MD);
        form.setHeader(new FxModalHeader());
        Label formTitle = new Label("Sign in to our platform");
        formTitle.getStyleClass().addAll("text-xl", "font-medium", "text-body");
        TextField email = new TextField();
        email.setPromptText("name@company.com");
        PasswordField password = new PasswordField();
        password.setPromptText("password");
        CheckBox remember = new CheckBox("Remember me");
        Button login = new Button("Log in to your account");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> {
            log("form: login clicked for '" + email.getText() + "'");
            form.requestClose();
        });
        VBox formBox = new VBox(16, formTitle, new VBox(6, new Label("Your email"), email),
                new VBox(6, new Label("Your password"), password), remember, login);
        form.setBody(new FxModalBody(formBox));
        form.setOnClose(e -> {
            email.clear();
            log("onClose(form): email reset");
        });
        track("form", form);
        flow.getChildren().add(labeled("form: onClose clears email", openButton("Open", form)));

        return flow;
    }

    // ---- initial focus
    // ----------------------------------------------------------------------------

    private FlowPane initialFocusSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxModal focused = new FxModal();
        focused.setSize(FxModal.Size.MD);
        focused.setHeader(new FxModalHeader("Sign in"));
        TextField email = new TextField();
        email.setPromptText("name@company.com");
        PasswordField password = new PasswordField();
        password.setPromptText("password");
        FxModalBody focusBody = new FxModalBody(new Label("Your email"), email, new Label("Your password"),
                password);
        focusBody.getStyleClass().add("gap-2");
        focused.setBody(focusBody);
        focused.setInitialFocus(email);
        track("initialFocus", focused);
        flow.getChildren().add(labeled("initialFocus = email field", openButton("Open", focused)));

        FxModal unfocused = new FxModal();
        unfocused.setSize(FxModal.Size.MD);
        unfocused.setHeader(new FxModalHeader("No initial focus"));
        unfocused.setBody(new FxModalBody(paragraph("Nothing is focused; the close button must not be highlighted.")));
        track("no initialFocus", unfocused);
        flow.getChildren().add(labeled("initialFocus = null", openButton("Open", unfocused)));

        // focus target that is not in the modal's scene: falls back to the modal
        FxModal stray = new FxModal();
        stray.setSize(FxModal.Size.MD);
        stray.setHeader(new FxModalHeader("Stray focus target"));
        stray.setBody(new FxModalBody(paragraph("initialFocus is a node that is not in this modal.")));
        stray.setInitialFocus(new TextField());
        track("stray initialFocus", stray);
        flow.getChildren().add(labeled("initialFocus not in scene (falls back)", openButton("Open", stray)));

        return flow;
    }

    // ---- scrolling
    // --------------------------------------------------------------------------------

    private FlowPane scrollingSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxModal tall = new FxModal();
        tall.setHeader(new FxModalHeader("Long body"));
        FxModalBody tallBody = new FxModalBody();
        tallBody.getStyleClass().add("gap-4");
        for (int i = 1; i <= 40; i++) {
            tallBody.getChildren().add(paragraph(i + ". " + LOREM));
        }
        tall.setBody(tallBody);
        Button accept = new Button("I accept");
        accept.setOnAction(e -> tall.requestClose());
        tall.setFooter(new FxModalFooter(accept));
        tall.setDismissible(true);
        track("scroll tall", tall);
        flow.getChildren().add(labeled("40 paragraphs, footer stays pinned", openButton("Open", tall)));

        FxModal wide = new FxModal();
        wide.setSize(FxModal.Size.SM);
        wide.setHeader(new FxModalHeader("Wrapping in a narrow card"));
        FxModalBody wideBody = new FxModalBody(paragraph(LOREM + " " + LOREM + " " + LOREM));
        wide.setBody(wideBody);
        wide.setFooter(new FxModalFooter(new Button("OK")));
        track("scroll wrap", wide);
        flow.getChildren().add(labeled("SM card, wrapped text (height must fit text)", openButton("Open", wide)));

        FxModal bottom = new FxModal();
        bottom.setPosition(FxModal.Position.BOTTOM_RIGHT);
        bottom.setSize(FxModal.Size.MD);
        bottom.setHeader(new FxModalHeader("Bottom-right + tall"));
        FxModalBody bottomBody = new FxModalBody();
        bottomBody.getStyleClass().add("gap-4");
        for (int i = 1; i <= 12; i++) {
            bottomBody.getChildren().add(paragraph(i + ". " + LOREM));
        }
        bottom.setBody(bottomBody);
        bottom.setDismissible(true);
        track("scroll bottom", bottom);
        flow.getChildren().add(labeled("anchored bottom-right, overflows", openButton("Open", bottom)));

        return flow;
    }

    // ---- header variants
    // --------------------------------------------------------------------------

    private FlowPane headerVariants() {
        FlowPane flow = new FlowPane(12, 12);

        FxModal noTitle = new FxModal();
        noTitle.setSize(FxModal.Size.MD);
        noTitle.setHeader(new FxModalHeader());
        noTitle.setBody(new FxModalBody(paragraph("Header with no title: only the close button shows.")));
        track("header no title", noTitle);
        flow.getChildren().add(labeled("no title", openButton("Open", noTitle)));

        FxModal noClose = new FxModal();
        noClose.setSize(FxModal.Size.MD);
        FxModalHeader hidden = new FxModalHeader("No close button");
        hidden.setCloseButtonVisible(false);
        noClose.setHeader(hidden);
        noClose.setBody(new FxModalBody(paragraph("Close with Esc or the button below.")));
        Button done = new Button("Done");
        done.setOnAction(e -> noClose.requestClose());
        noClose.setFooter(new FxModalFooter(done));
        track("header no close", noClose);
        flow.getChildren().add(labeled("closeButtonVisible = false", openButton("Open", noClose)));

        FxModal longTitle = new FxModal();
        longTitle.setSize(FxModal.Size.SM);
        longTitle.setHeader(new FxModalHeader("A deliberately very long modal title that should not squash the "
                + "close button out of the card"));
        longTitle.setBody(
                new FxModalBody(paragraph("Title is not shrinkable, so check the close button stays visible.")));
        track("header long title", longTitle);
        flow.getChildren().add(labeled("very long title in SM", openButton("Open", longTitle)));

        FxModal setTitle = new FxModal();
        setTitle.setTitle("Created with setTitle(String)");
        setTitle.setBody(new FxModalBody(paragraph("Shortcut for setHeader(new FxModalHeader(title)).")));
        track("setTitle", setTitle);
        flow.getChildren().add(labeled("modal.setTitle(...) shortcut", openButton("Open", setTitle)));

        FxModal plainHeader = new FxModal();
        plainHeader.setHeader(new Label("A plain Label as header (not FxModalHeader)"));
        plainHeader.setBody(new FxModalBody(paragraph("No close button is wired, so use Esc.")));
        track("plain header", plainHeader);
        flow.getChildren().add(labeled("non-FxModalHeader header", openButton("Open", plainHeader)));

        return flow;
    }

    // ---- missing parts
    // ----------------------------------------------------------------------------

    private FlowPane missingParts() {
        FlowPane flow = new FlowPane(12, 12);

        FxModal headerOnly = new FxModal();
        headerOnly.setSize(FxModal.Size.MD);
        headerOnly.setHeader(new FxModalHeader("Header only"));
        track("header only", headerOnly);
        flow.getChildren().add(labeled("header only", openButton("Open", headerOnly)));

        FxModal bodyOnly = new FxModal();
        bodyOnly.setSize(FxModal.Size.MD);
        bodyOnly.setBody(new FxModalBody(paragraph("Body only. Close with Esc.")));
        track("body only", bodyOnly);
        flow.getChildren().add(labeled("body only", openButton("Open", bodyOnly)));

        FxModal footerOnly = new FxModal();
        footerOnly.setSize(FxModal.Size.MD);
        Button close = new Button("Close");
        close.setOnAction(e -> footerOnly.requestClose());
        footerOnly.setFooter(new FxModalFooter(close));
        track("footer only", footerOnly);
        flow.getChildren().add(labeled("footer only", openButton("Open", footerOnly)));

        FxModal noFooter = new FxModal();
        noFooter.setSize(FxModal.Size.MD);
        noFooter.setHeader(new FxModalHeader("Header + body"));
        noFooter.setBody(new FxModalBody(paragraph("No footer.")));
        track("no footer", noFooter);
        flow.getChildren().add(labeled("no footer", openButton("Open", noFooter)));

        FxModal empty = new FxModal();
        empty.setSize(FxModal.Size.MD);
        empty.setDismissible(true);
        track("empty", empty);
        flow.getChildren().add(labeled("empty modal (click backdrop)", openButton("Open", empty)));

        return flow;
    }

    // ---- runtime changes
    // --------------------------------------------------------------------------

    private FlowPane runtimeSection() {
        FlowPane flow = new FlowPane(12, 12);

        FxModal modal = new FxModal();
        modal.setSize(FxModal.Size.MD);
        modal.setHeader(new FxModalHeader("Runtime changes"));
        FxModalBody first = new FxModalBody(paragraph("Original body."));
        FxModalBody second = new FxModalBody(paragraph("Swapped body. " + LOREM));
        modal.setBody(first);

        Button swap = new Button("Swap body");
        swap.setOnAction(e -> modal.setBody(modal.getBody() == first ? second : first));
        Button size = new Button("Next size");
        size.setOnAction(e -> modal.setSize(cycle(FxModal.Size.values(), modal.getSize())));
        Button pos = new Button("Next position");
        pos.setOnAction(e -> modal.setPosition(cycle(FxModal.Position.values(), modal.getPosition())));
        Button popup = new Button("Toggle popup");
        popup.setOnAction(e -> modal.setPopup(!modal.isPopup()));
        Button dismiss = new Button("Toggle dismissible");
        dismiss.setOnAction(e -> {
            modal.setDismissible(!modal.isDismissible());
            log("runtime: dismissible = " + modal.isDismissible());
        });
        Button title = new Button("New title");
        title.setOnAction(e -> modal.setTitle("Title " + System.nanoTime() % 1000));
        Button removeFooter = new Button("Remove footer");
        removeFooter.setOnAction(e -> modal.setFooter(null));
        Button close = new Button("Close");
        close.setOnAction(e -> modal.requestClose());

        FxModalFooter footer = new FxModalFooter(swap, size, pos, popup, dismiss, title, removeFooter, close);
        footer.setSpacing(8);
        modal.setFooter(footer);
        modal.setSize(FxModal.Size.XL2);
        track("runtime", modal);

        flow.getChildren().add(labeled("change everything while showing", openButton("Open", modal)));
        return flow;
    }

    // ---- programmatic / edge cases
    // ----------------------------------------------------------------

    private FlowPane edgeCases() {
        FlowPane flow = new FlowPane(12, 12);

        // show twice
        FxModal twice = simple("show() twice", "The second show() is a no-op: only one stage may exist.");
        track("show twice", twice);
        Button twiceBtn = new Button("show() twice in a row");
        twiceBtn.setOnAction(e -> {
            twice.show(twiceBtn);
            twice.show(twiceBtn);
            log("show twice: isShowing = " + twice.isShowing());
        });
        flow.getChildren().add(labeled("second show() ignored", twiceBtn));

        // detached anchor
        FxModal detached = simple("Never shown", "You should not see this.");
        Button detachedBtn = new Button("show() on detached node");
        detachedBtn.setOnAction(e -> {
            try {
                detached.show(new Button("not in a scene"));
                log("detached: NO exception (unexpected)");
            } catch (IllegalStateException ex) {
                log("detached: IllegalStateException - " + ex.getMessage());
            }
        });
        flow.getChildren().add(labeled("expects IllegalStateException", detachedBtn));

        // close / requestClose when closed
        FxModal closed = simple("Closed", "Never shown.");
        closed.setOnClose(e -> log("closed modal: onClose fired (unexpected)"));
        Button closedBtn = new Button("close() + requestClose() when closed");
        closedBtn.setOnAction(e -> {
            closed.close();
            closed.requestClose();
            log("closed modal: close() and requestClose() did nothing, no exception, isShowing = "
                    + closed.isShowing());
        });
        flow.getChildren().add(labeled("no-ops when not showing", closedBtn));

        // timed close()
        FxModal timed = simple("Auto close", "close() is called in 3 seconds. onClose must NOT fire.");
        timed.setOnClose(e -> log("timed: onClose fired (only if you closed it yourself)"));
        track("timed", timed);
        Button timedBtn = new Button("Open, close() after 3s");
        timedBtn.setOnAction(e -> {
            timed.show(timedBtn);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                timed.close();
                log("timed: close() called");
            });
            pause.play();
        });
        flow.getChildren().add(labeled("programmatic close()", timedBtn));

        // show(Window)
        FxModal byWindow = simple("show(Window)", "Opened with show(Window) instead of show(Node).");
        track("show(Window)", byWindow);
        Button windowBtn = new Button("show(Window)");
        windowBtn.setOnAction(e -> byWindow.show(windowBtn.getScene().getWindow()));
        flow.getChildren().add(labeled("window overload", windowBtn));

        // reuse many times
        FxModal reuse = simple("Reuse", "Open and close this one repeatedly: a fresh Stage per opening.");
        reuse.setDismissible(true);
        int[] count = { 0 };
        reuse.showingProperty().addListener((o, was, is) -> {
            if (is) {
                log("reuse: opening #" + (++count[0]));
            }
        });
        flow.getChildren().add(labeled("repeated open/close", openButton("Open repeatedly", reuse)));

        // move / resize the owner while open
        FxModal tracking = simple("Tracks the owner", "Drag or resize the main window: this card must follow it.");
        tracking.setDismissible(true);
        track("tracking", tracking);
        flow.getChildren().add(labeled("owner move / resize", openButton("Open, then move window", tracking)));

        // owner scene theme
        FxModal theme = simple("Theme check", "Colors here must match the current owner theme. Toggle "
                + "the theme, reopen, compare.");
        theme.setDismissible(true);
        track("theme", theme);
        flow.getChildren().add(labeled("theme sync on show", openButton("Open", theme)));

        return flow;
    }

    // ---- helpers
    // ----------------------------------------------------------------------------------

    /**
     * Title + one paragraph + an "I accept" footer button that goes through
     * requestClose().
     */
    private FxModal simple(String title, String text) {
        FxModal modal = new FxModal();
        modal.setHeader(new FxModalHeader(title));
        modal.setBody(new FxModalBody(paragraph(text)));
        Button accept = new Button("I accept");
        accept.setOnAction(e -> modal.requestClose());
        Button decline = new Button("Decline");
        decline.setOnAction(e -> modal.requestClose());
        FxModalFooter footer = new FxModalFooter(accept, decline);
        footer.setSpacing(8);
        modal.setFooter(footer);
        return modal;
    }

    private Button openButton(String caption, FxModal modal) {
        Button button = new Button(caption);
        button.setOnAction(e -> modal.show(button));
        return button;
    }

    /** Logs every open/close of the modal. */
    private void track(String name, FxModal modal) {
        modal.showingProperty().addListener((o, was, is) -> log(name + ": " + (is ? "opened" : "closed")));
    }

    private void log(String line) {
        log.appendText(line + System.lineSeparator());
    }

    private static <T> T cycle(T[] values, T current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    private static Label paragraph(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().addAll("text-base", "text-muted");
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
