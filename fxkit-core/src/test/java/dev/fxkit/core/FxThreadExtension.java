package dev.fxkit.core;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * Put {@code @ExtendWith(FxThreadExtension.class)} on a test class and every {@code @BeforeEach},
 * {@code @AfterEach}, {@code @Test} and parameterized test in it runs on the JavaFX application
 * thread. It also starts the toolkit first and skips the test (rather than failing it) where no
 * toolkit can start, so the class needs no {@code assumeTrue(JavaFxToolkit.ensureStarted())}.
 *
 * <p>Because the test body occupies the FX thread, it must not wait for an animation or a
 * {@code Platform.runLater}: they could never run. Tests that need to wait use {@link FxThread}
 * from an ordinary test thread instead.
 */
public final class FxThreadExtension implements BeforeEachCallback, InvocationInterceptor {

    @Override
    public void beforeEach(ExtensionContext context) {
        assumeTrue(JavaFxToolkit.ensureStarted(), "no JavaFX toolkit available (no display?)");
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        FxThread.runThrowing(invocation::proceed);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        FxThread.runThrowing(invocation::proceed);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        FxThread.runThrowing(invocation::proceed);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
            throws Throwable {
        FxThread.runThrowing(invocation::proceed);
    }
}
