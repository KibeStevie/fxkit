package dev.fxkit.core.components.clipboard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.Callable;

import dev.fxkit.core.FxThread;
import dev.fxkit.core.JavaFxToolkit;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The timed part of FxClipboard: the copied state ends by itself after the reset delay. This class runs
 * on the ordinary test thread and hops to the FX thread only where needed, because a test that sits ON
 * the FX thread could never wait for the animation that does the resetting. Like
 * {@code FxClipboardTest} it writes to the real clipboard and puts the previous text back.
 */
class FxClipboardResetTimerTest {

    private static final long WAIT_MILLIS = 5_000;

    private boolean clipboardAvailable;
    private String savedText;

    @BeforeEach
    void rememberTheClipboard() {
        assumeTrue(JavaFxToolkit.ensureStarted(), "no JavaFX toolkit available (no display?)");
        try {
            savedText = FxThread.call(() -> Clipboard.getSystemClipboard().getString());
            clipboardAvailable = true;
        } catch (RuntimeException noClipboard) {
            clipboardAvailable = false;
        }
        assumeTrue(clipboardAvailable, "no system clipboard available here");
    }

    @AfterEach
    void putTheClipboardBack() {
        if (clipboardAvailable && savedText != null) {
            FxThread.run(() -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(savedText);
                Clipboard.getSystemClipboard().setContent(content);
            });
        }
    }

    private static void await(Callable<Boolean> condition) throws Exception {
        long deadline = System.currentTimeMillis() + WAIT_MILLIS;
        while (!condition.call()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("condition not met within " + WAIT_MILLIS + " ms");
            }
            Thread.sleep(20);
        }
    }

    @Test
    void theCopiedStateEndsByItselfAfterTheResetDelay() throws Exception {
        FxClipboard button = FxThread.call(() -> new FxClipboard("value"));

        FxThread.run(() -> {
            button.setResetDelay(Duration.millis(200));
            button.copy();
            assertTrue(button.isCopied());
        });

        await(() -> !FxThread.call(button::isCopied));
        assertTrue(FxThread.call(() -> "Copy".equals(button.getText())));
    }

    @Test
    void copyingAgainWhileCopiedStillEndsInAReset() throws Exception {
        FxClipboard button = FxThread.call(() -> new FxClipboard("value"));
        FxThread.run(() -> {
            button.setResetDelay(Duration.millis(300));
            button.copy();
        });

        FxThread.run(button::copy);

        await(() -> !FxThread.call(button::isCopied));
    }

    @Test
    void anIndefiniteDelayNeverResetsOnItsOwn() throws Exception {
        FxClipboard button = FxThread.call(() -> new FxClipboard("value"));
        FxThread.run(() -> {
            button.setResetDelay(Duration.INDEFINITE);
            button.copy();
        });

        Thread.sleep(500);

        assertTrue(FxThread.call(button::isCopied));
        FxThread.run(button::reset);
        assertFalse(FxThread.call(button::isCopied));
    }
}
