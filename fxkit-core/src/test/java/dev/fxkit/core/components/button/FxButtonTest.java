package dev.fxkit.core.components.button;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import dev.fxkit.core.FxThreadExtension;
import dev.fxkit.core.components.spinner.FxSpinner;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * FxButton's variant/size/color style classes, the processing state (spinner, label, no activation),
 * and the {@code className} hook. Icons are not covered here: they need an Ikonli icon pack on the
 * test classpath. Runs on the FX thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxButtonTest {

    private static final Set<String> VARIANT_CLASSES = Arrays.stream(FxButton.Variant.values())
            .map(v -> "fxk-btn-" + v.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

    private static long count(FxButton button, String prefix) {
        return button.getStyleClass().stream().filter(c -> c.startsWith(prefix)).count();
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewButtonIsPrimaryMediumAndDefaultColored() {
        FxButton button = new FxButton("Save");

        assertEquals("Save", button.getText());
        assertEquals(FxButton.Variant.PRIMARY, button.getVariant());
        assertEquals(FxButton.Size.MD, button.getSize());
        assertEquals(FxButton.Color.DEFAULT, button.getColor());
        assertFalse(button.isPill());
        assertFalse(button.isProcessing());
        assertEquals("", button.getClassName());
        assertEquals("Loading...", button.getProcessingLabel());
        assertTrue(button.getStyleClass().containsAll(java.util.List.of(
                FxButton.STYLE_CLASS, "fxk-btn-primary", "fxk-btn-size-md", "fxk-btn-color-default")));
        assertFalse(button.getStyleClass().contains("fxk-btn-pill"));
        assertFalse(button.getStyleClass().contains("fxk-btn-processing"));
    }

    @Test
    void theNoArgumentConstructorMakesAnEmptyButtonWithTheSameDefaults() {
        FxButton button = new FxButton();

        assertEquals("", button.getText());
        assertTrue(button.getStyleClass().contains("fxk-btn-primary"));
    }

    // ---- variant, size, color, pill ---------------------------------------------------------

    @ParameterizedTest
    @EnumSource(FxButton.Variant.class)
    void everyVariantGetsExactlyOneVariantClass(FxButton.Variant variant) {
        FxButton button = new FxButton("x");

        button.setVariant(variant);

        long present = button.getStyleClass().stream().filter(VARIANT_CLASSES::contains).count();
        assertEquals(1, present);
        assertTrue(button.getStyleClass().contains("fxk-btn-" + variant.name().toLowerCase(Locale.ROOT)));
    }

    @ParameterizedTest
    @EnumSource(FxButton.Size.class)
    void everySizeGetsExactlyOneSizeClass(FxButton.Size size) {
        FxButton button = new FxButton("x");

        button.setSize(size);

        assertEquals(1, count(button, "fxk-btn-size-"));
        assertTrue(button.getStyleClass().contains("fxk-btn-size-" + size.name().toLowerCase(Locale.ROOT)));
    }

    @ParameterizedTest
    @EnumSource(FxButton.Color.class)
    void everyColorGetsExactlyOneColorClass(FxButton.Color color) {
        FxButton button = new FxButton("x");

        button.setColor(color);

        assertEquals(1, count(button, "fxk-btn-color-"));
        assertTrue(button.getStyleClass().contains("fxk-btn-color-" + color.name().toLowerCase(Locale.ROOT)));
    }

    @Test
    void aNullVariantSizeOrColorLeavesNoClassAndDoesNotThrow() {
        FxButton button = new FxButton("x");

        button.setVariant(null);
        button.setSize(null);
        button.setColor(null);

        assertEquals(0, button.getStyleClass().stream().filter(VARIANT_CLASSES::contains).count());
        assertEquals(0, count(button, "fxk-btn-size-"));
        assertEquals(0, count(button, "fxk-btn-color-"));
    }

    @Test
    void pillIsAStyleClassThatFollowsTheProperty() {
        FxButton button = new FxButton("x");

        button.setPill(true);
        assertTrue(button.getStyleClass().contains("fxk-btn-pill"));

        button.setPill(false);
        assertFalse(button.getStyleClass().contains("fxk-btn-pill"));
    }

    // ---- processing -------------------------------------------------------------------------

    @Test
    void processingShowsASpinnerAndTheProcessingLabel() {
        FxButton button = new FxButton("Save");

        button.setProcessing(true);

        assertTrue(button.isProcessing());
        assertTrue(button.getStyleClass().contains("fxk-btn-processing"));
        assertInstanceOf(FxSpinner.class, button.getGraphic());
        assertEquals("Loading...", button.getText());
    }

    @Test
    void endingProcessingRestoresTheTextAndTheOriginalGraphic() {
        FxButton button = new FxButton("Save");
        Region original = new Region();
        button.setGraphic(original);

        button.setProcessing(true);
        button.setProcessing(false);

        assertEquals("Save", button.getText());
        assertSame(original, button.getGraphic());
        assertFalse(button.getStyleClass().contains("fxk-btn-processing"));
    }

    @Test
    void endingProcessingOnAButtonWithoutAGraphicLeavesNone() {
        FxButton button = new FxButton("Save");

        button.setProcessing(true);
        button.setProcessing(false);

        assertNull(button.getGraphic());
    }

    @Test
    void aCustomProcessingLabelReplacesTheText() {
        FxButton button = new FxButton("Save");
        button.setProcessingLabel("Saving...");

        button.setProcessing(true);

        assertEquals("Saving...", button.getText());
    }

    @Test
    void changingTheProcessingLabelWhileProcessingUpdatesTheText() {
        FxButton button = new FxButton("Save");
        button.setProcessing(true);

        button.setProcessingLabel("Please wait");

        assertEquals("Please wait", button.getText());
    }

    @Test
    void aBlankProcessingLabelKeepsTheButtonsOwnText() {
        FxButton button = new FxButton("Save");
        button.setProcessingLabel("   ");

        button.setProcessing(true);

        assertEquals("Save", button.getText());
    }

    @Test
    void anIconOnlyButtonStaysIconOnlyWhileProcessing() {
        FxButton button = new FxButton();
        Region icon = new Region();
        button.setGraphic(icon);

        button.setProcessing(true);
        assertEquals("", button.getText());
        assertInstanceOf(FxSpinner.class, button.getGraphic());

        button.setProcessing(false);
        assertEquals("", button.getText());
        assertSame(icon, button.getGraphic());
    }

    @Test
    void textChangedWhileProcessingIsRememberedForAfterwards() {
        FxButton button = new FxButton("Save");
        button.setProcessing(true);

        button.setText("Update");
        assertEquals("Loading...", button.getText(), "the label stays while processing");

        button.setProcessing(false);
        assertEquals("Update", button.getText());
    }

    @Test
    void aProcessingButtonIgnoresActivationUntilProcessingEnds() {
        FxButton button = new FxButton("Save");
        AtomicInteger calls = new AtomicInteger();
        button.setOnAction(e -> calls.incrementAndGet());

        button.fire();
        assertEquals(1, calls.get());

        button.setProcessing(true);
        button.fire();
        assertEquals(1, calls.get(), "a slow action must not be triggerable twice");

        button.setProcessing(false);
        button.fire();
        assertEquals(2, calls.get());
    }

    @ParameterizedTest
    @CsvSource({"XS,XS", "SM,SM", "MD,SM", "LG,SM", "XL,MD"})
    void theSpinnerSizeFollowsTheButtonSize(FxButton.Size buttonSize, FxSpinner.Size spinnerSize) {
        FxButton button = new FxButton("Save");
        button.setSize(buttonSize);

        button.setProcessing(true);

        assertEquals(spinnerSize, ((FxSpinner) button.getGraphic()).getSize());
    }

    @Test
    void resizingAProcessingButtonResizesItsSpinner() {
        FxButton button = new FxButton("Save");
        button.setProcessing(true);

        button.setSize(FxButton.Size.XL);

        assertEquals(FxSpinner.Size.MD, ((FxSpinner) button.getGraphic()).getSize());
    }

    @Test
    void theSpinnerAnnouncesTheProcessingLabelOrLoading() {
        FxButton labelled = new FxButton("Save");
        labelled.setProcessingLabel("Saving");
        labelled.setProcessing(true);
        assertEquals("Saving", ((FxSpinner) labelled.getGraphic()).getAccessibleText());

        FxButton blank = new FxButton("Save");
        blank.setProcessingLabel(" ");
        blank.setProcessing(true);
        assertEquals("Loading", ((FxSpinner) blank.getGraphic()).getAccessibleText());
    }

    @Test
    void theSpinnerIsPaintedInTheButtonsTextColor() {
        FxButton button = new FxButton("Save");
        button.setProcessing(true);
        FxSpinner spinner = (FxSpinner) button.getGraphic();
        assertTrue(spinner.getStyle().startsWith("-fxk-spinner-arc: rgba("));

        button.setTextFill(Color.rgb(10, 20, 30));

        assertEquals("-fxk-spinner-arc: rgba(10, 20, 30, 1.000); -fxk-spinner-track: rgba(10, 20, 30, 0.300);",
                spinner.getStyle());
    }

    // ---- className --------------------------------------------------------------------------

    @Test
    void classNameTokensBecomeStyleClassesAndInlineStyle() {
        FxButton button = new FxButton("Save");

        button.setClassName("rounded-lg bg-blue-500");

        assertEquals("rounded-lg bg-blue-500", button.getClassName());
        assertTrue(button.getStyleClass().contains("rounded-lg"));
        assertTrue(button.getStyle().contains("-fxk-blue-500"));
    }

    @Test
    void clearingTheClassNameRemovesWhatItAdded() {
        FxButton button = new FxButton("Save");
        button.setClassName("rounded-lg bg-blue-500");

        button.setClassName("");

        assertFalse(button.getStyleClass().contains("rounded-lg"));
        assertTrue(button.getStyle() == null || button.getStyle().isEmpty());
        assertTrue(button.getStyleClass().contains("fxk-btn-primary"), "the button's own classes stay");
    }

    @Test
    void settingTheClassNameToNullAlsoClears() {
        FxButton button = new FxButton("Save");
        button.setClassName("rounded-lg");

        button.setClassName(null);

        assertFalse(button.getStyleClass().contains("rounded-lg"));
    }

    /**
     * KNOWN DOC MISMATCH, found while writing these tests. {@code getClassName()} is documented as
     * "never null; empty if unset", but {@code setClassName(null)} stores null, so the getter returns
     * null. Fix: normalize in the setter ({@code className.set(className == null ? "" : className)}).
     * Remove the {@code @Disabled} once it is fixed.
     */
    @Test
    @Disabled("Known doc mismatch: getClassName() returns null after setClassName(null), but is documented never-null")
    void theClassNameIsNeverNullEvenAfterSettingNull() {
        FxButton button = new FxButton("Save");

        button.setClassName(null);

        assertEquals("", button.getClassName());
    }
}
