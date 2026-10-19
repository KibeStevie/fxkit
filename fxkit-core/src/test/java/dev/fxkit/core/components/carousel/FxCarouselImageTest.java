package dev.fxkit.core.components.carousel;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;

/** FxCarousel.image(...) hands back an empty view for a URL it will not or cannot load. Needs the toolkit; skipped without one. */
class FxCarouselImageTest {

    @Test
    void aMissingResourceGivesAnEmptyViewInsteadOfThrowing() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        ImageView view = FxCarousel.image("/definitely/not/here.png");
        assertNull(view.getImage());
    }

    @Test
    void urlsRefusedByThePolicyGiveAnEmptyView() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        assertNull(FxCarousel.image("ftp://example.com/a.png").getImage());
        assertNull(FxCarousel.image("file:////attacker/share/a.png").getImage());
    }

    @Test
    void aNullUrlIsStillAProgrammingError() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        boolean threw = false;
        try {
            FxCarousel.image((String) null);
        } catch (NullPointerException expected) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    void aNonPositiveDecodeLimitIsAProgrammingError() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        boolean threw = false;
        try {
            FxCarousel.image("/images/a.png", 0, 100);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        assertTrue(threw);
    }
}
