package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.fxkit.core.internal.KeyActivation.Outcome;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

/** Pure-logic tests (no JavaFX toolkit): when does a key press activate, repeat, or not concern the node. */
class KeyActivationTest {

    private final KeyActivation keys = new KeyActivation();

    private Outcome press(KeyCode code) {
        return keys.pressed(code, true, true);
    }

    @Test
    void theFirstPressFiresAndAutoRepeatIsSwallowed() {
        assertEquals(Outcome.FIRE, press(KeyCode.ENTER));
        assertEquals(Outcome.CONSUME, press(KeyCode.ENTER));
        assertEquals(Outcome.CONSUME, press(KeyCode.ENTER));
    }

    @Test
    void releasingTheKeyReArmsIt() {
        assertEquals(Outcome.FIRE, press(KeyCode.SPACE));
        assertEquals(Outcome.CONSUME, press(KeyCode.SPACE));

        keys.released(KeyCode.SPACE);

        assertEquals(Outcome.FIRE, press(KeyCode.SPACE));
    }

    @Test
    void aDifferentActivationKeyIsANewActivation() {
        assertEquals(Outcome.FIRE, press(KeyCode.ENTER));
        assertEquals(Outcome.FIRE, press(KeyCode.SPACE));
    }

    @Test
    void releasingAnotherKeyDoesNotReArm() {
        press(KeyCode.ENTER);
        keys.released(KeyCode.A);
        assertEquals(Outcome.CONSUME, press(KeyCode.ENTER));
    }

    @Test
    void otherKeysAreLeftAlone() {
        assertEquals(Outcome.IGNORE, press(KeyCode.A));
        assertEquals(Outcome.IGNORE, press(KeyCode.TAB));
    }

    @Test
    void aKeyBubbledFromAFocusedChildIsLeftAloneAndDoesNotArm() {
        assertEquals(Outcome.IGNORE, keys.pressed(KeyCode.SPACE, false, true));
        assertEquals(Outcome.IGNORE, keys.pressed(KeyCode.SPACE, false, true));

        // the node itself getting the focus afterwards still activates on its first press
        assertEquals(Outcome.FIRE, keys.pressed(KeyCode.SPACE, true, true));
    }

    @Test
    void aDisabledNodeIgnoresKeys() {
        assertEquals(Outcome.IGNORE, keys.pressed(KeyCode.ENTER, true, false));
        assertEquals(Outcome.FIRE, keys.pressed(KeyCode.ENTER, true, true));
    }

    @Test
    void resetForgetsTheHeldKeySoALostReleaseCannotSwallowTheNextPress() {
        press(KeyCode.ENTER); // focus then moves away; the key-released goes to someone else
        keys.reset();

        assertEquals(Outcome.FIRE, press(KeyCode.ENTER));
    }
}
