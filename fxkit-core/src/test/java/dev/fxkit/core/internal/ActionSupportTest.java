package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.JavaFxToolkit;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

/** Real events through real nodes, so the bubbling is JavaFX's own. Needs the toolkit; skipped without one. */
class ActionSupportTest {

    private final AtomicInteger activations = new AtomicInteger();
    private final Label label = new Label("Title");
    private final Button closeButton = new Button("x");
    private final TextField field = new TextField();
    private StackPane item;

    private void install() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        item = new StackPane(label, closeButton, field);
        ActionSupport.install(item, activations::incrementAndGet);
    }

    private static void key(Node target, javafx.event.EventType<KeyEvent> type, KeyCode code) {
        Event.fireEvent(target, new KeyEvent(type, "", "", code, false, false, false, false));
    }

    private static void click(Node target, MouseButton button) {
        Event.fireEvent(target, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, 1,
                false, false, false, false, button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY,
                false, false, true, null));
    }

    // ---- keyboard: repeat -----------------------------------------------------------------

    @Test
    void holdingEnterActivatesOnceNotOncePerRepeat() {
        install();

        key(item, KeyEvent.KEY_PRESSED, KeyCode.ENTER);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.ENTER);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.ENTER);

        assertEquals(1, activations.get());
    }

    @Test
    void releasingTheKeyAllowsTheNextActivation() {
        install();

        key(item, KeyEvent.KEY_PRESSED, KeyCode.SPACE);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.SPACE);
        key(item, KeyEvent.KEY_RELEASED, KeyCode.SPACE);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.SPACE);

        assertEquals(2, activations.get());
    }

    @Test
    void otherKeysDoNothing() {
        install();

        key(item, KeyEvent.KEY_PRESSED, KeyCode.A);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.TAB);

        assertEquals(0, activations.get());
    }

    // ---- keyboard: bubbling ---------------------------------------------------------------

    @Test
    void spaceOrEnterTypedInAFocusableChildDoesNotActivateTheParent() {
        install();

        key(field, KeyEvent.KEY_PRESSED, KeyCode.SPACE);
        key(field, KeyEvent.KEY_PRESSED, KeyCode.ENTER);
        key(closeButton, KeyEvent.KEY_PRESSED, KeyCode.ENTER);

        assertEquals(0, activations.get());
    }

    @Test
    void aBubbledKeyDoesNotUseUpTheNodesOwnFirstPress() {
        install();

        key(field, KeyEvent.KEY_PRESSED, KeyCode.SPACE);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.SPACE);

        assertEquals(1, activations.get());
    }

    // ---- mouse ----------------------------------------------------------------------------

    @Test
    void aPrimaryClickOnTheNodeOrItsLabelActivates() {
        install();

        click(item, MouseButton.PRIMARY);
        click(label, MouseButton.PRIMARY);

        assertEquals(2, activations.get());
    }

    @Test
    void aClickOnAFocusableChildBelongsToThatChild() {
        install();

        click(closeButton, MouseButton.PRIMARY);
        click(field, MouseButton.PRIMARY);

        assertEquals(0, activations.get());
    }

    @Test
    void otherMouseButtonsDoNotActivate() {
        install();

        click(item, MouseButton.SECONDARY);

        assertEquals(0, activations.get());
    }

    @Test
    void aDisabledNodeDoesNotActivate() {
        install();
        item.setDisable(true);

        click(item, MouseButton.PRIMARY);
        key(item, KeyEvent.KEY_PRESSED, KeyCode.ENTER);

        assertEquals(0, activations.get());
    }
}
