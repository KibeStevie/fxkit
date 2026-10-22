package dev.fxkit.core.components.breadcrumb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.JavaFxToolkit;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A breadcrumb item is a link only when it has an action; the last "current page" item must neither
 * take focus nor swallow clicks. Icons are not covered here: they need an Ikonli icon pack on the test
 * classpath. Needs the toolkit; skipped without one.
 */
class FxBreadcrumbItemTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    // ---- without an action (the current page) -----------------------------------------------

    @Test
    void anItemWithoutAnActionIsPlainTextThatIgnoresTheMouseAndKeyboardFocus() {
        FxBreadcrumbItem item = new FxBreadcrumbItem("Flowbite");

        assertEquals("Flowbite", item.getText());
        assertFalse(item.isInteractive());
        assertTrue(item.getStyleClass().contains("fxk-breadcrumb-item"));
        assertFalse(item.getStyleClass().contains("fxk-breadcrumb-item-link"));
        assertTrue(item.isMouseTransparent(), "a current-page item must not swallow clicks");
        assertFalse(item.isFocusTraversable(), "a current-page item must not take focus");
        assertNull(item.getIcon());
    }

    @Test
    void theNoArgumentConstructorMakesAnEmptyNonInteractiveItem() {
        FxBreadcrumbItem item = new FxBreadcrumbItem();

        assertFalse(item.isInteractive());
        assertTrue(item.isMouseTransparent());
    }

    // ---- with an action (a link) ------------------------------------------------------------

    @Test
    void anItemWithAnActionIsALinkThatTakesFocusAndClicks() {
        EventHandler<ActionEvent> handler = e -> { };

        FxBreadcrumbItem item = new FxBreadcrumbItem("Home", handler);

        assertTrue(item.isInteractive());
        assertTrue(item.getStyleClass().contains("fxk-breadcrumb-item-link"));
        assertFalse(item.isMouseTransparent());
        assertTrue(item.isFocusTraversable());
    }

    @Test
    void settingAnActionLaterTurnsAPlainItemIntoALink() {
        FxBreadcrumbItem item = new FxBreadcrumbItem("Home");

        item.setOnAction(e -> { });

        assertTrue(item.isInteractive());
        assertTrue(item.getStyleClass().contains("fxk-breadcrumb-item-link"));
        assertFalse(item.isMouseTransparent());
        assertTrue(item.isFocusTraversable());
    }

    @Test
    void clearingTheActionTurnsALinkBackIntoPlainText() {
        FxBreadcrumbItem item = new FxBreadcrumbItem("Home");
        item.setOnAction(e -> { });

        item.setOnAction(null);

        assertFalse(item.isInteractive());
        assertFalse(item.getStyleClass().contains("fxk-breadcrumb-item-link"));
        assertTrue(item.isMouseTransparent());
        assertFalse(item.isFocusTraversable());
    }

    @Test
    void firingALinkRunsItsHandlerOncePerActivation() {
        AtomicInteger calls = new AtomicInteger();
        EventHandler<ActionEvent> handler = e -> calls.incrementAndGet();
        FxBreadcrumbItem item = new FxBreadcrumbItem("Home", handler);

        item.fire();
        item.fire();

        assertEquals(2, calls.get());
    }

    @Test
    void theLinkClassSurvivesManyToggles() {
        // the style class follows a binding that the item keeps a strong reference to; this guards
        // against it silently stopping to follow onAction (see the comment in FxBreadcrumbItem)
        FxBreadcrumbItem item = new FxBreadcrumbItem("Home");
        for (int i = 0; i < 20; i++) {
            item.setOnAction(e -> { });
            assertTrue(item.getStyleClass().contains("fxk-breadcrumb-item-link"), "round " + i);
            item.setOnAction(null);
            assertFalse(item.getStyleClass().contains("fxk-breadcrumb-item-link"), "round " + i);
        }
        System.gc();
        item.setOnAction(e -> { });
        assertTrue(item.getStyleClass().contains("fxk-breadcrumb-item-link"));
    }

    @Test
    void theLinkClassIsAddedOnlyOnceNoMatterHowOftenTheActionChanges() {
        FxBreadcrumbItem item = new FxBreadcrumbItem("Home");

        item.setOnAction(e -> { });
        item.setOnAction(e -> { });
        item.setOnAction(e -> { });

        long count = item.getStyleClass().stream().filter("fxk-breadcrumb-item-link"::equals).count();
        assertEquals(1, count);
    }
}
