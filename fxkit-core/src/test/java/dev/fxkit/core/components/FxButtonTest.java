package dev.fxkit.core.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.components.FxButton.Size;
import dev.fxkit.core.components.FxButton.Variant;
import dev.fxkit.core.testsupport.JavaFxToolkit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link FxButton}'s property-to-style-class logic (issue #30).
 *
 * <p><b>On running controls in a unit test</b> (see the issue's "work out how to run controls" task):
 * these tests never build a {@code Scene}, call {@code show()}, or force CSS/layout to resolve — they
 * only construct an {@code FxButton} and read its {@link javafx.scene.Node#getStyleClass()} list and
 * its own getters. Even so, the JavaFX toolkit has to be running: {@code Control}'s static
 * initializer calls {@code PlatformImpl.setDefaultPlatformUserAgentStylesheet()}, which throws
 * {@code IllegalStateException: Toolkit not initialized} without it — and once that class
 * initialization fails it stays failed, so every later test reports
 * {@code NoClassDefFoundError: Could not initialize class FxButton}. {@link JavaFxToolkit} starts the
 * toolkit once per test JVM to avoid that.
 */
@ExtendWith(JavaFxToolkit.class)
class FxButtonTest {

    @Test
    void defaultVariantIsPrimary() {
        FxButton button = new FxButton();

        assertEquals(Variant.PRIMARY, button.getVariant());
        assertEquals(FxButton.DEFAULT_VARIANT, button.getVariant());
        assertTrue(button.getStyleClass().contains("fxk-btn-primary"));
    }

    @Test
    void defaultSizeIsMd() {
        FxButton button = new FxButton();

        assertEquals(Size.MD, button.getSize());
        assertEquals(FxButton.DEFAULT_SIZE, button.getSize());
        assertTrue(button.getStyleClass().contains("fxk-btn-size-md"));
    }

    @Test
    void baseStyleClassIsAlwaysPresent() {
        FxButton button = new FxButton();

        assertTrue(button.getStyleClass().contains(FxButton.STYLE_CLASS));
    }

    @Test
    void textConstructorSetsTextAndKeepsDefaults() {
        FxButton button = new FxButton("Save");

        assertEquals("Save", button.getText());
        assertEquals(Variant.PRIMARY, button.getVariant());
        assertEquals(Size.MD, button.getSize());
    }

    @Test
    void changingVariantUpdatesStyleClasses() {
        FxButton button = new FxButton();

        button.setVariant(Variant.DANGER);

        assertEquals(Variant.DANGER, button.getVariant());
        assertFalse(button.getStyleClass().contains("fxk-btn-primary"), "old variant class should be removed");
        assertTrue(button.getStyleClass().contains("fxk-btn-danger"), "new variant class should be added");
    }

    @Test
    void changingSizeUpdatesStyleClasses() {
        FxButton button = new FxButton();

        button.setSize(Size.LG);

        assertEquals(Size.LG, button.getSize());
        assertFalse(button.getStyleClass().contains("fxk-btn-size-md"), "old size class should be removed");
        assertTrue(button.getStyleClass().contains("fxk-btn-size-lg"), "new size class should be added");
    }

    @Test
    void everyVariantProducesExactlyOneVariantStyleClass() {
        FxButton button = new FxButton();

        for (Variant variant : Variant.values()) {
            button.setVariant(variant);

            long variantClasses = button.getStyleClass().stream().filter(c -> c.startsWith("fxk-btn-")
                    && !c.startsWith("fxk-btn-size-")).count();
            assertEquals(1, variantClasses, "exactly one variant style class for " + variant);
            assertTrue(button.getStyleClass().contains("fxk-btn-" + variant.name().toLowerCase()));
        }
    }

    @Test
    void everySizeProducesExactlyOneSizeStyleClass() {
        FxButton button = new FxButton();

        for (Size size : Size.values()) {
            button.setSize(size);

            long sizeClasses = button.getStyleClass().stream().filter(c -> c.startsWith("fxk-btn-size-")).count();
            assertEquals(1, sizeClasses, "exactly one size style class for " + size);
            assertTrue(button.getStyleClass().contains("fxk-btn-size-" + size.name().toLowerCase()));
        }
    }

    /** #28 acceptance criterion: "Size and variant can be combined freely." */
    @Test
    void variantAndSizeChangeIndependently() {
        FxButton button = new FxButton();

        button.setVariant(Variant.OUTLINE);
        button.setSize(Size.SM);

        assertTrue(button.getStyleClass().contains("fxk-btn-outline"));
        assertTrue(button.getStyleClass().contains("fxk-btn-size-sm"));

        button.setSize(Size.LG);

        // changing size must not disturb the variant class, and vice versa
        assertTrue(button.getStyleClass().contains("fxk-btn-outline"));
        assertTrue(button.getStyleClass().contains("fxk-btn-size-lg"));
        assertFalse(button.getStyleClass().contains("fxk-btn-size-sm"));
    }

    @Test
    void variantPropertyReflectsSetVariant() {
        FxButton button = new FxButton();

        button.variantProperty().set(Variant.SUCCESS);

        assertEquals(Variant.SUCCESS, button.getVariant());
        assertTrue(button.getStyleClass().contains("fxk-btn-success"));
    }

    @Test
    void sizePropertyReflectsSetSize() {
        FxButton button = new FxButton();

        button.sizeProperty().set(Size.SM);

        assertEquals(Size.SM, button.getSize());
        assertTrue(button.getStyleClass().contains("fxk-btn-size-sm"));
    }
}
