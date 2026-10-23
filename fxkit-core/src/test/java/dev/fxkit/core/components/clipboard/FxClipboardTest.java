package dev.fxkit.core.components.clipboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.FxThreadExtension;
import dev.fxkit.core.TestNodes;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * FxClipboard's three variants, what copy() and fire() do, the copied state, and the overlay helper.
 * These tests WRITE to the real system clipboard; the text that was on it beforehand is put back
 * afterwards (plain text only: an image or file on the clipboard is not restored). The timed reset is
 * covered by {@code FxClipboardResetTimerTest}. Runs on the FX thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxClipboardTest {

    private boolean clipboardAvailable;
    private String savedText;

    @BeforeEach
    void rememberTheClipboard() {
        try {
            savedText = Clipboard.getSystemClipboard().getString();
            clipboardAvailable = true;
        } catch (RuntimeException noClipboard) {
            clipboardAvailable = false;
        }
    }

    @AfterEach
    void putTheClipboardBack() {
        if (clipboardAvailable && savedText != null) {
            ClipboardContent content = new ClipboardContent();
            content.putString(savedText);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private void requireClipboard() {
        assumeTrue(clipboardAvailable, "no system clipboard available here");
    }

    private static String clipboardText() {
        return Clipboard.getSystemClipboard().getString();
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewClipboardButtonIsAFullWidthTextButton() {
        FxClipboard button = new FxClipboard("hello");

        assertEquals("hello", button.getValueToCopy());
        assertEquals("Copy", button.getText());
        assertEquals(FxClipboard.Variant.DEFAULT, button.getVariant());
        assertEquals(ContentDisplay.TEXT_ONLY, button.getContentDisplay());
        assertNull(button.getGraphic());
        assertFalse(button.isCopied());
        assertTrue(button.getStyleClass().contains("fxk-clip"));
        assertTrue(button.getStyleClass().contains("fxk-clip-default"));
        assertFalse(button.getStyleClass().contains("fxk-clip-copied"));
        assertEquals(Double.MAX_VALUE, button.getMaxWidth(), 0, "w-full");
        assertEquals(Region.USE_PREF_SIZE, button.getMinWidth(), 0);
    }

    @Test
    void theDefaultsMatchTheDocumentedConstants() {
        FxClipboard button = new FxClipboard();

        assertEquals("", button.getValueToCopy());
        assertEquals(FxClipboard.DEFAULT_LABEL, button.getLabel());
        assertEquals(FxClipboard.DEFAULT_COPIED_LABEL, button.getCopiedLabel());
        assertEquals(FxClipboard.DEFAULT_TOOLTIP_TEXT, button.getTooltipText());
        assertEquals(FxClipboard.DEFAULT_COPIED_TOOLTIP_TEXT, button.getCopiedTooltipText());
        assertEquals(FxClipboard.DEFAULT_RESET_DELAY, button.getResetDelay());
        assertEquals(Duration.seconds(2), FxClipboard.DEFAULT_RESET_DELAY);
        assertEquals("Copy", FxClipboard.DEFAULT_LABEL);
        assertEquals("Copied", FxClipboard.DEFAULT_COPIED_LABEL);
        assertEquals("Copy to clipboard", FxClipboard.DEFAULT_TOOLTIP_TEXT);
        assertNull(button.getOnCopy());
    }

    @Test
    void theTwoArgumentConstructorSetsTheLabel() {
        FxClipboard button = new FxClipboard("hello", "Copy link");

        assertEquals("Copy link", button.getText());
        assertEquals("Copy link", button.getLabel());
    }

    // ---- copying ----------------------------------------------------------------------------

    @Test
    void copyPutsTheValueOnTheSystemClipboardAndEntersTheCopiedState() {
        requireClipboard();
        FxClipboard button = new FxClipboard("npm install flowbite-react");

        button.copy();

        assertEquals("npm install flowbite-react", clipboardText());
        assertTrue(button.isCopied());
        assertEquals("Copied", button.getText());
        assertTrue(button.getStyleClass().contains("fxk-clip-copied"));
    }

    @Test
    void copyRunsTheOnCopyCallbackAfterWritingTheClipboard() {
        requireClipboard();
        FxClipboard button = new FxClipboard("payload");
        List<String> seenOnClipboard = new ArrayList<>();
        button.setOnCopy(() -> seenOnClipboard.add(clipboardText()));

        button.copy();

        assertEquals(List.of("payload"), seenOnClipboard);
    }

    @Test
    void aNullValueCopiesAnEmptyStringInsteadOfFailing() {
        requireClipboard();
        FxClipboard button = new FxClipboard("something");
        button.setValueToCopy(null);

        button.copy();

        assertTrue(button.isCopied());
    }

    @Test
    void firingCopiesFirstAndThenRunsTheActionHandler() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        List<String> clipboardInsideHandler = new ArrayList<>();
        button.setOnAction(e -> clipboardInsideHandler.add(clipboardText()));

        button.fire();

        assertEquals(List.of("value"), clipboardInsideHandler);
        assertTrue(button.isCopied());
    }

    @Test
    void aDisabledButtonDoesNotCopyOrRunItsHandlerWhenFired() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        List<String> calls = new ArrayList<>();
        button.setOnAction(e -> calls.add("action"));
        button.setOnCopy(() -> calls.add("copy"));
        button.setDisable(true);

        button.fire();

        assertTrue(calls.isEmpty());
        assertFalse(button.isCopied());
    }

    @Test
    void copyWorksInCodeEvenWhileTheButtonIsDisabled() {
        requireClipboard();
        FxClipboard button = new FxClipboard("from code");
        button.setDisable(true);

        button.copy();

        assertEquals("from code", clipboardText());
        assertTrue(button.isCopied());
    }

    @Test
    void resetLeavesTheCopiedStateAtOnce() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        button.copy();

        button.reset();

        assertFalse(button.isCopied());
        assertEquals("Copy", button.getText());
        assertFalse(button.getStyleClass().contains("fxk-clip-copied"));
    }

    @Test
    void copiedLabelsFollowTheirProperties() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        button.setLabel("Copy link");
        button.setCopiedLabel("Link copied");
        assertEquals("Copy link", button.getText());

        button.copy();
        assertEquals("Link copied", button.getText());

        button.setCopiedLabel("Done");
        assertEquals("Done", button.getText());
    }

    @Test
    void anIndefiniteResetDelayKeepsTheCopiedStateUntilReset() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        button.setResetDelay(Duration.INDEFINITE);

        button.copy();
        assertTrue(button.isCopied());

        button.reset();
        assertFalse(button.isCopied());
    }

    @Test
    void aMaxSizeSetByTheCallerSurvivesCopying() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        button.setMaxWidth(200);

        button.copy();
        button.reset();

        assertEquals(200, button.getMaxWidth(), 1e-9);
    }

    // ---- variants ---------------------------------------------------------------------------

    @Test
    void theIconVariantIsAGraphicOnlyButtonThatHugsItsIcon() {
        FxClipboard button = new FxClipboard("value");

        button.setVariant(FxClipboard.Variant.ICON);

        assertTrue(button.getStyleClass().contains("fxk-clip-icon"));
        assertFalse(button.getStyleClass().contains("fxk-clip-default"));
        assertNull(button.getText());
        assertNotNull(button.getGraphic());
        assertEquals(ContentDisplay.GRAPHIC_ONLY, button.getContentDisplay());
        assertEquals(Region.USE_PREF_SIZE, button.getMaxWidth(), 0);
        assertEquals(Region.USE_PREF_SIZE, button.getMaxHeight(), 0);
    }

    @Test
    void theIconVariantAnnouncesItsTooltipTextBecauseItHasNoLabel() {
        FxClipboard button = new FxClipboard("value");
        button.setVariant(FxClipboard.Variant.ICON);
        assertEquals("Copy to clipboard", button.getAccessibleText());

        button.setTooltipText("Copy the command");
        assertEquals("Copy the command", button.getAccessibleText());
    }

    @Test
    void theIconVariantAnnouncesTheCopiedTextAfterCopying() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        button.setVariant(FxClipboard.Variant.ICON);

        button.copy();

        assertEquals("Copied", button.getAccessibleText());
    }

    @Test
    void theIconAndTextVariantShowsBoth() {
        FxClipboard button = new FxClipboard("value");

        button.setVariant(FxClipboard.Variant.ICON_TEXT);

        assertTrue(button.getStyleClass().contains("fxk-clip-icon-text"));
        assertEquals("Copy", button.getText());
        assertNotNull(button.getGraphic());
        assertEquals(ContentDisplay.LEFT, button.getContentDisplay());
        assertNull(button.getAccessibleText(), "the visible text already names it");
    }

    @Test
    void theGlyphSwitchesFromTheClipboardToACheckMarkWhenCopied() {
        requireClipboard();
        FxClipboard button = new FxClipboard("value");
        button.setVariant(FxClipboard.Variant.ICON);
        assertTrue(TestNodes.has(button.getGraphic(), "fxk-clip-glyph-default"));

        button.copy();

        assertTrue(TestNodes.has(button.getGraphic(), "fxk-clip-glyph-success"));
        assertFalse(TestNodes.has(button.getGraphic(), "fxk-clip-glyph-default"));
    }

    @Test
    void switchingBackToTheDefaultVariantRestoresTheFullWidthTextButton() {
        FxClipboard button = new FxClipboard("value");
        button.setVariant(FxClipboard.Variant.ICON);

        button.setVariant(FxClipboard.Variant.DEFAULT);

        assertEquals("Copy", button.getText());
        assertNull(button.getGraphic());
        assertEquals(Double.MAX_VALUE, button.getMaxWidth(), 0);
        assertNull(button.getAccessibleText());
    }

    @Test
    void aNullVariantIsTreatedAsTheDefaultOne() {
        FxClipboard button = new FxClipboard("value");
        button.setVariant(FxClipboard.Variant.ICON);

        button.setVariant(null);

        assertEquals("Copy", button.getText());
        assertEquals(ContentDisplay.TEXT_ONLY, button.getContentDisplay());
    }

    @ParameterizedTest
    @EnumSource(FxClipboard.Variant.class)
    void everyVariantGetsExactlyOneVariantClass(FxClipboard.Variant variant) {
        FxClipboard button = new FxClipboard("value");

        button.setVariant(variant);

        long classes = button.getStyleClass().stream()
                .filter(c -> c.equals("fxk-clip-default") || c.equals("fxk-clip-icon")
                        || c.equals("fxk-clip-icon-text"))
                .count();
        assertEquals(1, classes);
    }

    // ---- overlay ----------------------------------------------------------------------------

    @Test
    void overlayStacksTheButtonOnTheRightEndOfTheField() {
        Region field = new Region();
        FxClipboard button = new FxClipboard("value");
        button.setVariant(FxClipboard.Variant.ICON);

        StackPane stack = FxClipboard.overlay(field, button);

        assertEquals(List.of(field, button), List.copyOf(stack.getChildren()));
        assertEquals(Pos.CENTER_RIGHT, StackPane.getAlignment(button));
        assertEquals(new Insets(0, 8, 0, 0), StackPane.getMargin(button));
    }

    @Test
    void overlayInsetsTheIconAndTextVariantBy10AndFollowsVariantChanges() {
        FxClipboard button = new FxClipboard("value");
        FxClipboard.overlay(new Region(), button);

        button.setVariant(FxClipboard.Variant.ICON_TEXT);
        assertEquals(new Insets(0, 10, 0, 0), StackPane.getMargin(button));

        button.setVariant(FxClipboard.Variant.ICON);
        assertEquals(new Insets(0, 8, 0, 0), StackPane.getMargin(button));
    }
}
