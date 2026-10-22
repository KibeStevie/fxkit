package dev.fxkit.core.components.blockquote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import dev.fxkit.core.TestNodes;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FxBlockquote's text and content, its size/alignment/weight classes, the optional quote glyph and the
 * glyph factory. Needs the toolkit; skipped without one.
 */
class FxBlockquoteTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    private static Label textLabel(FxBlockquote quote) {
        return TestNodes.require(quote, "fxk-blockquote-text", Label.class);
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewBlockquoteHasTheDocumentedDefaults() {
        FxBlockquote quote = new FxBlockquote();

        assertEquals("", quote.getText());
        assertEquals(FxBlockquote.Size.MD, quote.getSize());
        assertEquals(FxBlockquote.TextAlign.LEFT, quote.getTextAlignment());
        assertEquals(FxBlockquote.Weight.SEMIBOLD, quote.getWeight());
        assertFalse(quote.isSolid());
        assertFalse(quote.isIconVisible());
        assertNull(quote.getContent());
        assertTrue(quote.getStyleClass().containsAll(java.util.List.of(
                "fxk-blockquote", "fxk-blockquote-size-md", "fxk-blockquote-align-left",
                "fxk-blockquote-weight-semibold")));
        assertFalse(quote.getStyleClass().contains("fxk-blockquote-solid"));
    }

    // ---- text -------------------------------------------------------------------------------

    @Test
    void theTextIsShownInAWrappingLabelAndFollowsTheProperty() {
        FxBlockquote quote = new FxBlockquote("To be or not to be");

        assertEquals(1, quote.getChildren().size());
        assertSame(textLabel(quote), quote.getChildren().get(0));
        assertEquals("To be or not to be", textLabel(quote).getText());
        assertTrue(textLabel(quote).isWrapText());

        quote.setText("Changed");
        assertEquals("Changed", textLabel(quote).getText());
        assertEquals("Changed", quote.textProperty().get());
    }

    // ---- content ----------------------------------------------------------------------------

    @Test
    void contentIsShownInsteadOfTheText() {
        Label custom = new Label("custom");
        FxBlockquote quote = new FxBlockquote("ignored");

        quote.setContent(custom);

        assertSame(custom, quote.getContent());
        assertEquals(1, quote.getChildren().size());
        assertSame(custom, quote.getChildren().get(0));
    }

    @Test
    void theNodeConstructorStartsWithThatContent() {
        Label custom = new Label("custom");

        FxBlockquote quote = new FxBlockquote(custom);

        assertSame(custom, quote.getChildren().get(0));
    }

    @Test
    void clearingTheContentGoesBackToTheText() {
        FxBlockquote quote = new FxBlockquote("the text");
        quote.setContent(new Label("custom"));

        quote.setContent(null);

        assertSame(textLabel(quote), quote.getChildren().get(0));
        assertEquals("the text", textLabel(quote).getText());
    }

    // ---- icon -------------------------------------------------------------------------------

    @Test
    void theQuoteGlyphIsAboveTheBodyOnlyWhileItIsVisible() {
        FxBlockquote quote = new FxBlockquote("text");

        quote.setIconVisible(true);
        assertEquals(2, quote.getChildren().size());
        assertTrue(quote.getChildren().get(0).getStyleClass().contains("fxk-blockquote-icon"));
        assertSame(textLabel(quote), quote.getChildren().get(1));

        quote.setIconVisible(false);
        assertEquals(1, quote.getChildren().size());
        assertSame(textLabel(quote), quote.getChildren().get(0));
    }

    @Test
    void theGlyphStaysAboveCustomContentToo() {
        Label custom = new Label("custom");
        FxBlockquote quote = new FxBlockquote(custom);

        quote.setIconVisible(true);

        assertEquals(2, quote.getChildren().size());
        assertSame(custom, quote.getChildren().get(1));
    }

    @Test
    void quoteIconBuildsAStyledGlyphForEachSize() {
        Region medium = FxBlockquote.quoteIcon(FxBlockquote.IconSize.MD);
        Region large = FxBlockquote.quoteIcon(FxBlockquote.IconSize.LG);

        assertTrue(medium.getStyleClass().containsAll(java.util.List.of("fxk-blockquote-icon", "fxk-blockquote-icon-md")));
        assertTrue(large.getStyleClass().containsAll(java.util.List.of("fxk-blockquote-icon", "fxk-blockquote-icon-lg")));
    }

    @Test
    void theGlyphIsDecorationNotAControl() {
        Region glyph = FxBlockquote.quoteIcon(FxBlockquote.IconSize.MD);

        assertTrue(glyph.isMouseTransparent());
        assertFalse(glyph.isFocusTraversable());
    }

    @Test
    void aMissingGlyphSizeIsAProgrammingError() {
        assertThrows(NullPointerException.class, () -> FxBlockquote.quoteIcon(null));
    }

    // ---- style classes ----------------------------------------------------------------------

    @Test
    void sizeAlignmentAndWeightSwapTheirStyleClasses() {
        FxBlockquote quote = new FxBlockquote("x");

        quote.setSize(FxBlockquote.Size.LG);
        quote.setTextAlignment(FxBlockquote.TextAlign.CENTER);
        quote.setWeight(FxBlockquote.Weight.MEDIUM);

        assertTrue(quote.getStyleClass().containsAll(java.util.List.of(
                "fxk-blockquote-size-lg", "fxk-blockquote-align-center", "fxk-blockquote-weight-medium")));
        assertFalse(quote.getStyleClass().contains("fxk-blockquote-size-md"));
        assertFalse(quote.getStyleClass().contains("fxk-blockquote-align-left"));
        assertFalse(quote.getStyleClass().contains("fxk-blockquote-weight-semibold"));
    }

    @Test
    void aNullSizeAlignmentOrWeightLeavesNoClassAndDoesNotThrow() {
        FxBlockquote quote = new FxBlockquote("x");

        quote.setSize(null);
        quote.setTextAlignment(null);
        quote.setWeight(null);

        assertTrue(quote.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-blockquote-size-")));
        assertTrue(quote.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-blockquote-align-")));
        assertTrue(quote.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-blockquote-weight-")));
    }

    @Test
    void solidIsAStyleClassThatFollowsTheProperty() {
        FxBlockquote quote = new FxBlockquote("x");

        quote.setSolid(true);
        assertTrue(quote.getStyleClass().contains("fxk-blockquote-solid"));

        quote.setSolid(false);
        assertFalse(quote.getStyleClass().contains("fxk-blockquote-solid"));
    }
}
