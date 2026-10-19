package dev.fxkit.core.components.megamenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import org.junit.jupiter.api.Test;

/**
 * The picture used to be written into the node's inline style as {@code url('...')}, which replaced
 * any style the caller had set and only escaped quotes. It is an ImageView now. Needs the toolkit;
 * skipped without one.
 */
class FxMegaMenuImageCardTest {

    private static final String CALLERS_STYLE = "-fx-padding: 5; -fx-opacity: 0.9";

    @Test
    void settingAnImageUrlNeverTouchesTheCallersInlineStyle() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        FxMegaMenuImageCard card = new FxMegaMenuImageCard("Text", "Go");
        card.setStyle(CALLERS_STYLE);

        card.setImageUrl("/definitely/not/here.png"); // missing: logged, not thrown
        assertEquals(CALLERS_STYLE, card.getStyle());

        card.setImageUrl("ftp://example.com/a.png");   // refused by the policy
        assertEquals(CALLERS_STYLE, card.getStyle());

        // what a CSS injection attempt would look like; refused by scheme, so nothing is fetched
        card.setImageUrl("ftp://example.com/a.png') ; -fx-background-color: red; (");
        assertEquals(CALLERS_STYLE, card.getStyle());

        card.setImageUrl(null);
        assertEquals(CALLERS_STYLE, card.getStyle());
    }
}
