package dev.fxkit.core.internal;

import javafx.scene.input.KeyCode;

/**
 * Decides what a key press means for a node made activatable by {@link ActionSupport}, and remembers
 * which key is currently held so that keyboard auto-repeat does not activate it again and again.
 *
 * <p>JavaFX gives {@code KeyEvent} no "is repeat" flag: holding Enter delivers a stream of
 * {@code KEY_PRESSED} events with nothing to tell the first from the rest. The only way to tell is to
 * remember that the key went down and has not come up yet, which is all this class does. It has no
 * JavaFX node or event types, so the rules can be tested without a toolkit.
 *
 * <p>Not part of FXKit's public API. Use on the JavaFX Application Thread only.
 */
final class KeyActivation {

    /** What the caller should do with a key-pressed event. */
    enum Outcome {
        /** Not ours: leave the event alone so it keeps bubbling. */
        IGNORE,
        /** Ours but already handled (auto-repeat of the held key): swallow it, do not run the action. */
        CONSUME,
        /** Ours and new: swallow it and run the action. */
        FIRE
    }

    private KeyCode held;

    /**
     * @param code    the pressed key
     * @param onNode  whether the node itself has the focus, i.e. the event was not bubbled up from a
     *                focused child (a text field inside the item must keep its own Space and Enter)
     * @param enabled whether the node is enabled
     */
    Outcome pressed(KeyCode code, boolean onNode, boolean enabled) {
        if (!onNode || !enabled || !isActivationKey(code)) {
            return Outcome.IGNORE;
        }
        if (code == held) {
            return Outcome.CONSUME; // auto-repeat: the key never came up since it last fired
        }
        held = code;
        return Outcome.FIRE;
    }

    /** The key came up: the next press of it counts as a new activation. */
    void released(KeyCode code) {
        if (code == held) {
            held = null;
        }
    }

    /**
     * Forgets the held key. Call when the node loses focus or is disabled: the matching key-released
     * event would go to whoever has the focus now, and a stale "held" would swallow the next real press.
     */
    void reset() {
        held = null;
    }

    private static boolean isActivationKey(KeyCode code) {
        return code == KeyCode.ENTER || code == KeyCode.SPACE;
    }
}
