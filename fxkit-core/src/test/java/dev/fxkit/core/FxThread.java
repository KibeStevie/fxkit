package dev.fxkit.core;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;

/**
 * Runs code on the JavaFX application thread and waits for it. Some JavaFX classes insist on that
 * thread: {@code Clipboard.getSystemClipboard()}, animations ({@code Timeline.play()}), and windows
 * such as {@code Popup}. Anything thrown on the FX thread, including failed assertions and skipped
 * assumptions, is rethrown on the calling thread so JUnit reports it normally.
 */
public final class FxThread {

    private static final long TIMEOUT_SECONDS = 30;

    private FxThread() {
        // static utility class, not meant to be instantiated
    }

    /** Like {@link Runnable} but allowed to throw anything, so JUnit's {@code Invocation.proceed} fits. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    /** Runs {@code action} on the FX thread and returns when it has finished. */
    public static void run(Runnable action) {
        try {
            runThrowing(action::run);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /** Runs {@code action} on the FX thread and returns its result. */
    public static <T> T call(Callable<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        run(() -> {
            try {
                result.set(action.call());
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        return result.get();
    }

    /** The general form: rethrows exactly what the action threw. */
    public static void runThrowing(ThrowingRunnable action) throws Throwable {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });
        if (!done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new AssertionError("The JavaFX thread did not finish within " + TIMEOUT_SECONDS + " seconds");
        }
        Throwable thrown = failure.get();
        if (thrown != null) {
            throw thrown;
        }
    }
}
