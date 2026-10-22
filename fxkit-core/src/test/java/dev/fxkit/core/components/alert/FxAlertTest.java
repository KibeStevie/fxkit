package dev.fxkit.core.components.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.JavaFxToolkit;
import dev.fxkit.core.TestNodes;
import dev.fxkit.core.TestStyles;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * FxAlert's body (title and message, or custom content), color and accent classes, and the dismiss
 * behavior. Icons are not covered here: they need an Ikonli icon pack on the test classpath.
 * Needs the toolkit; skipped without one.
 */
class FxAlertTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    /** The HBox that holds icon, body and close button: the alert's only child. */
    private static HBox wrapper(FxAlert alert) {
        assertEquals(1, alert.getChildren().size());
        return assertInstanceOf(HBox.class, alert.getChildren().get(0));
    }

    /** The title/message TextFlow, which is the first node in the wrapper when there is no icon. */
    private static TextFlow flow(FxAlert alert) {
        return assertInstanceOf(TextFlow.class, wrapper(alert).getChildren().get(0));
    }

    private static List<String> texts(TextFlow flow) {
        List<String> out = new ArrayList<>();
        for (Node n : flow.getChildren()) {
            out.add(((Text) n).getText());
        }
        return out;
    }

    private static Button closeButton(FxAlert alert) {
        return TestNodes.find(alert, "fxk-alert-close", Button.class);
    }

    // ---- defaults and constructors ----------------------------------------------------------

    @Test
    void aNewAlertHasTheDefaultColorAndNoAccent() {
        FxAlert alert = new FxAlert();

        assertEquals(FxAlert.DEFAULT_COLOR, alert.getColor());
        assertEquals(FxAlert.Color.INFO, alert.getColor());
        assertTrue(alert.getStyleClass().contains(FxAlert.STYLE_CLASS));
        assertTrue(alert.getStyleClass().contains("fxk-alert-color-info"));
        assertFalse(alert.isBorderAccent());
        assertFalse(alert.getStyleClass().contains("fxk-alert-border-accent"));
    }

    @Test
    void theConvenienceConstructorsSetTheTexts() {
        FxAlert messageOnly = new FxAlert("Saved");
        assertEquals("Saved", messageOnly.getMessage());
        assertNull(messageOnly.getTitle());

        FxAlert both = new FxAlert("Heads up", "Disk almost full");
        assertEquals("Heads up", both.getTitle());
        assertEquals("Disk almost full", both.getMessage());
    }

    // ---- body -------------------------------------------------------------------------------

    @Test
    void anEmptyAlertHasAnEmptyBody() {
        assertTrue(flow(new FxAlert()).getChildren().isEmpty());
    }

    @Test
    void aMessageAloneIsOneTextWithoutTheTitleClass() {
        FxAlert alert = new FxAlert("Saved");

        TextFlow flow = flow(alert);
        assertEquals(List.of("Saved"), texts(flow));
        Text only = (Text) flow.getChildren().get(0);
        assertTrue(only.getStyleClass().contains("fxk-alert-text"));
        assertFalse(only.getStyleClass().contains("fxk-alert-title"));
    }

    @Test
    void aTitleAndMessageAreTwoTextsWithTheMessageSeparatedByASpace() {
        FxAlert alert = new FxAlert("Heads up", "Disk almost full");

        TextFlow flow = flow(alert);
        assertEquals(List.of("Heads up", " Disk almost full"), texts(flow));
        assertTrue(flow.getChildren().get(0).getStyleClass().contains("fxk-alert-title"));
        assertFalse(flow.getChildren().get(1).getStyleClass().contains("fxk-alert-title"));
    }

    @Test
    void aTitleAloneIsShownAsTheTitle() {
        FxAlert alert = new FxAlert();
        alert.setTitle("Heads up");

        assertEquals(List.of("Heads up"), texts(flow(alert)));
        assertTrue(flow(alert).getChildren().get(0).getStyleClass().contains("fxk-alert-title"));
    }

    @Test
    void aBlankTitleIsTreatedAsNoTitle() {
        FxAlert alert = new FxAlert("   ", "Only the message");

        assertEquals(List.of("Only the message"), texts(flow(alert)));
    }

    @Test
    void aBlankMessageNextToATitleIsLeftOut() {
        FxAlert alert = new FxAlert("Title", "  ");

        assertEquals(List.of("Title"), texts(flow(alert)));
    }

    @Test
    void changingTheTextsRebuildsTheBody() {
        FxAlert alert = new FxAlert("Before");

        alert.setMessage("After");
        assertEquals(List.of("After"), texts(flow(alert)));

        alert.setTitle("T");
        assertEquals(List.of("T", " After"), texts(flow(alert)));

        alert.setTitle(null);
        alert.setMessage(null);
        assertTrue(flow(alert).getChildren().isEmpty());
    }

    @Test
    void theBodyFillsTheRowSoTheCloseButtonSitsAtTheEnd() {
        FxAlert alert = new FxAlert("Saved");

        assertEquals(Priority.ALWAYS, HBox.getHgrow(wrapper(alert).getChildren().get(0)));
    }

    // ---- custom content ---------------------------------------------------------------------

    @Test
    void customContentReplacesTheTitleAndMessage() {
        FxAlert alert = new FxAlert("Ignored title", "Ignored message");
        Label custom = new Label("Custom body");

        alert.setContent(custom);

        assertSame(custom, alert.getContent());
        assertEquals(1, wrapper(alert).getChildren().size());
        assertSame(custom, wrapper(alert).getChildren().get(0));
    }

    @Test
    void clearingTheContentBringsTheTitleAndMessageBack() {
        FxAlert alert = new FxAlert("Title", "Message");
        alert.setContent(new Label("Custom"));

        alert.setContent(null);

        assertNull(alert.getContent());
        assertEquals(List.of("Title", " Message"), texts(flow(alert)));
    }

    // ---- color and accent -------------------------------------------------------------------

    @Test
    void changingTheColorSwapsTheStyleClass() {
        FxAlert alert = new FxAlert();

        alert.setColor(FxAlert.Color.FAILURE);

        assertTrue(alert.getStyleClass().contains("fxk-alert-color-failure"));
        assertFalse(alert.getStyleClass().contains("fxk-alert-color-info"));
    }

    @Test
    void aNullColorLeavesNoColorClassAndDoesNotThrow() {
        FxAlert alert = new FxAlert();

        alert.setColor(null);

        assertTrue(alert.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-alert-color-")));
    }

    @Test
    void borderAccentIsAStyleClassThatFollowsTheProperty() {
        FxAlert alert = new FxAlert();

        alert.setBorderAccent(true);
        assertTrue(alert.getStyleClass().contains("fxk-alert-border-accent"));

        alert.setBorderAccent(false);
        assertFalse(alert.getStyleClass().contains("fxk-alert-border-accent"));
    }

    @ParameterizedTest
    @EnumSource(FxAlert.Color.class)
    void everyColorGetsExactlyOneColorClass(FxAlert.Color color) {
        FxAlert alert = new FxAlert();

        alert.setColor(color);

        long colorClasses = alert.getStyleClass().stream().filter(c -> c.startsWith("fxk-alert-color-")).count();
        assertEquals(1, colorClasses);
        assertTrue(alert.getStyleClass().contains("fxk-alert-color-" + TestStyles.styleName(color)));
    }

    // ---- dismiss ----------------------------------------------------------------------------

    @Test
    void thereIsNoCloseButtonUntilADismissHandlerIsSet() {
        FxAlert alert = new FxAlert("Saved");

        assertNull(closeButton(alert));
        assertNull(alert.getOnDismiss());
    }

    @Test
    void settingADismissHandlerAddsTheCloseButtonAfterTheBody() {
        FxAlert alert = new FxAlert("Saved");

        alert.setOnDismiss(() -> { });

        Button close = closeButton(alert);
        assertEquals("\u00D7", close.getText());
        List<Node> children = wrapper(alert).getChildren();
        assertSame(close, children.get(children.size() - 1));
    }

    @Test
    void clearingTheDismissHandlerRemovesTheCloseButton() {
        FxAlert alert = new FxAlert("Saved");
        alert.setOnDismiss(() -> { });

        alert.setOnDismiss(null);

        assertNull(closeButton(alert));
    }

    @Test
    void clickingTheCloseButtonRunsTheHandlerAndRemovesTheAlertFromItsParent() {
        FxAlert alert = new FxAlert("Saved");
        VBox parent = new VBox(alert);
        AtomicInteger calls = new AtomicInteger();
        alert.setOnDismiss(calls::incrementAndGet);

        closeButton(alert).fire();

        assertEquals(1, calls.get());
        assertFalse(parent.getChildren().contains(alert));
    }

    @Test
    void theHandlerRunsBeforeTheAlertLeavesItsParent() {
        FxAlert alert = new FxAlert("Saved");
        VBox parent = new VBox(alert);
        AtomicBoolean stillAttached = new AtomicBoolean();
        alert.setOnDismiss(() -> stillAttached.set(alert.getParent() == parent));

        alert.dismiss();

        assertTrue(stillAttached.get(), "the callback must see the alert still in its parent");
        assertFalse(parent.getChildren().contains(alert));
    }

    @Test
    void dismissWorksWithoutAHandlerAndWithoutAParent() {
        FxAlert free = new FxAlert("Saved");
        free.dismiss(); // nothing to run, nothing to remove from: must simply not throw

        FxAlert attached = new FxAlert("Saved");
        VBox parent = new VBox(attached);
        attached.dismiss();
        assertFalse(parent.getChildren().contains(attached));
    }
}
