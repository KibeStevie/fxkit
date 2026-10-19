package dev.fxkit.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Starts the JavaFX toolkit once for tests that create real controls. Tests call
 * {@code assumeTrue(JavaFxToolkit.ensureStarted())} so that they are skipped, not failed, where no
 * toolkit can start (for example a CI machine without a display).
 */
public final class JavaFxToolkit {

    private static Boolean available;

    private JavaFxToolkit() {
        // static utility class, not meant to be instantiated
    }

    public static synchronized boolean ensureStarted() {
        if (available == null) {
            available = start();
        }
        return available;
    }

    private static boolean start() {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyRunning) {
            return true;
        } catch (RuntimeException | Error noDisplay) {
            return false;
        }
        try {
            return started.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
