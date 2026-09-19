package dev.fxkit.core.testsupport;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Starts the JavaFX toolkit once per test JVM.
 *
 * <p>Any test that constructs a {@link javafx.scene.control.Control} needs this: {@code Control}'s
 * static initializer calls {@code PlatformImpl.setDefaultPlatformUserAgentStylesheet()}, which needs
 * a running toolkit. Without it the first test fails with
 * {@code ExceptionInInitializerError: Toolkit not initialized} and every later test fails with
 * {@code NoClassDefFoundError: Could not initialize class ...} because the failed class
 * initialization is permanent.
 */
public final class JavaFxToolkit implements BeforeAllCallback {

    private static boolean started;

    @Override
    public void beforeAll(ExtensionContext context) throws InterruptedException {
        start();
    }

    public static synchronized void start() throws InterruptedException {
        if (started) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(ready::countDown);
        } catch (IllegalStateException alreadyRunning) {
            ready.countDown();
        }
        ready.await();
        Platform.setImplicitExit(false); // keep the toolkit alive for the rest of the run
        started = true;
    }
}
