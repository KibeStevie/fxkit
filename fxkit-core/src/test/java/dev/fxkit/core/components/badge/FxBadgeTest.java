package dev.fxkit.core.components.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.JavaFxToolkit;
import dev.fxkit.core.TestNodes;
import dev.fxkit.core.TestStyles;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * FxBadge's text, color and size classes, and the dismiss behavior. Icons are not covered here: they
 * need an Ikonli icon pack on the test classpath. Needs the toolkit; skipped without one.
 */
class FxBadgeTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    private static Button closeButton(FxBadge badge) {
        return TestNodes.find(badge, "fxk-badge-close", Button.class);
    }

    // ---- defaults and constructors ----------------------------------------------------------

    @Test
    void aNewBadgeHasTheDefaultColorAndSizeAndNoChildren() {
        FxBadge badge = new FxBadge();

        assertEquals(FxBadge.DEFAULT_COLOR, badge.getColor());
        assertEquals(FxBadge.DEFAULT_SIZE, badge.getSize());
        assertTrue(badge.getStyleClass().contains(FxBadge.STYLE_CLASS));
        assertTrue(badge.getStyleClass().contains("fxk-badge-color-info"));
        assertTrue(badge.getStyleClass().contains("fxk-badge-size-xs"));
        assertTrue(badge.getChildren().isEmpty());
    }

    @Test
    void theConvenienceConstructorsSetTextAndColor() {
        FxBadge plain = new FxBadge("New");
        assertEquals("New", plain.getText());
        assertEquals(FxBadge.Color.INFO, plain.getColor());

        FxBadge colored = new FxBadge("Failed", FxBadge.Color.FAILURE);
        assertEquals("Failed", colored.getText());
        assertTrue(colored.getStyleClass().contains("fxk-badge-color-failure"));
        assertFalse(colored.getStyleClass().contains("fxk-badge-color-info"));
    }

    @Test
    void aBadgeHugsItsContentInsteadOfStretching() {
        FxBadge badge = new FxBadge("New");

        assertEquals(Region.USE_PREF_SIZE, badge.getMaxWidth());
        assertEquals(Region.USE_PREF_SIZE, badge.getMaxHeight());
    }

    // ---- text -------------------------------------------------------------------------------

    @Test
    void textIsShownInALabel() {
        FxBadge badge = new FxBadge("New");

        assertEquals(1, badge.getChildren().size());
        Label label = TestNodes.require(badge, "fxk-badge-text", Label.class);
        assertEquals("New", label.getText());
    }

    @Test
    void changingTheTextUpdatesTheLabel() {
        FxBadge badge = new FxBadge("Before");

        badge.setText("After");

        assertEquals("After", TestNodes.require(badge, "fxk-badge-text", Label.class).getText());
        assertEquals(1, badge.getChildren().size());
    }

    @Test
    void blankOrNullTextShowsNoLabel() {
        FxBadge blank = new FxBadge("   ");
        assertTrue(blank.getChildren().isEmpty());

        FxBadge cleared = new FxBadge("New");
        cleared.setText(null);
        assertTrue(cleared.getChildren().isEmpty());
        assertNull(TestNodes.find(cleared, "fxk-badge-text", Label.class));
    }

    // ---- color and size ---------------------------------------------------------------------

    @Test
    void changingTheSizeSwapsTheStyleClass() {
        FxBadge badge = new FxBadge("New");

        badge.setSize(FxBadge.Size.SM);

        assertTrue(badge.getStyleClass().contains("fxk-badge-size-sm"));
        assertFalse(badge.getStyleClass().contains("fxk-badge-size-xs"));
    }

    @Test
    void aNullColorOrSizeLeavesNoClassAndDoesNotThrow() {
        FxBadge badge = new FxBadge("New");

        badge.setColor(null);
        badge.setSize(null);

        assertTrue(badge.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-badge-color-")));
        assertTrue(badge.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-badge-size-")));
    }

    @ParameterizedTest
    @EnumSource(FxBadge.Color.class)
    void everyColorGetsExactlyOneColorClass(FxBadge.Color color) {
        FxBadge badge = new FxBadge("x");

        badge.setColor(color);

        long colorClasses = badge.getStyleClass().stream().filter(c -> c.startsWith("fxk-badge-color-")).count();
        assertEquals(1, colorClasses);
        assertTrue(badge.getStyleClass().contains("fxk-badge-color-" + TestStyles.styleName(color)));
    }

    // ---- dismiss ----------------------------------------------------------------------------

    @Test
    void thereIsNoCloseButtonUntilADismissHandlerIsSet() {
        FxBadge badge = new FxBadge("New");

        assertNull(closeButton(badge));
        assertNull(badge.getOnDismiss());
        assertFalse(badge.getStyleClass().contains("fxk-badge-dismissible"));
    }

    @Test
    void aDismissHandlerAddsTheCloseButtonAfterTheTextAndMarksTheBadgeDismissible() {
        FxBadge badge = new FxBadge("New");

        badge.setOnDismiss(() -> { });

        Button close = closeButton(badge);
        assertEquals("\u00D7", close.getText());
        assertEquals(2, badge.getChildren().size());
        assertSame(close, badge.getChildren().get(1));
        assertTrue(badge.getStyleClass().contains("fxk-badge-dismissible"));
    }

    @Test
    void clearingTheDismissHandlerRemovesTheButtonAndTheClass() {
        FxBadge badge = new FxBadge("New");
        badge.setOnDismiss(() -> { });

        badge.setOnDismiss(null);

        assertNull(closeButton(badge));
        assertFalse(badge.getStyleClass().contains("fxk-badge-dismissible"));
        assertEquals(1, badge.getChildren().size());
    }

    @Test
    void clickingTheCloseButtonRunsTheHandlerAndRemovesTheBadgeFromItsParent() {
        FxBadge badge = new FxBadge("New");
        HBox parent = new HBox(badge);
        AtomicInteger calls = new AtomicInteger();
        badge.setOnDismiss(calls::incrementAndGet);

        closeButton(badge).fire();

        assertEquals(1, calls.get());
        assertFalse(parent.getChildren().contains(badge));
    }

    @Test
    void theHandlerRunsBeforeTheBadgeLeavesItsParent() {
        FxBadge badge = new FxBadge("New");
        VBox parent = new VBox(badge);
        AtomicBoolean stillAttached = new AtomicBoolean();
        badge.setOnDismiss(() -> stillAttached.set(badge.getParent() == parent));

        badge.dismiss();

        assertTrue(stillAttached.get(), "the callback must see the badge still in its parent");
        assertFalse(parent.getChildren().contains(badge));
    }

    @Test
    void dismissWorksWithoutAHandlerAndWithoutAParent() {
        new FxBadge("New").dismiss(); // nothing to run, nothing to remove from: must simply not throw

        FxBadge attached = new FxBadge("New");
        VBox parent = new VBox(attached);
        attached.dismiss();
        assertFalse(parent.getChildren().contains(attached));
    }

    @Test
    void aDismissibleBadgeWithoutTextStillShowsOnlyTheCloseButton() {
        FxBadge badge = new FxBadge();

        badge.setOnDismiss(() -> { });

        assertEquals(1, badge.getChildren().size());
        assertSame(closeButton(badge), badge.getChildren().get(0));
    }
}
