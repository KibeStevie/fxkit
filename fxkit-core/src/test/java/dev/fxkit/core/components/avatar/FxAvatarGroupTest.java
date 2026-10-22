package dev.fxkit.core.components.avatar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The avatar group container and its "+N" counter. Needs the toolkit; skipped without one. */
class FxAvatarGroupTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    @Test
    void theGroupHoldsItsAvatarsInOrder() {
        FxAvatar first = new FxAvatar();
        FxAvatar second = new FxAvatar();
        FxAvatarGroupCounter counter = new FxAvatarGroupCounter(5);

        FxAvatarGroup group = new FxAvatarGroup(first, second, counter);

        assertTrue(group.getStyleClass().contains("fxk-avatar-group"));
        assertEquals(3, group.getChildren().size());
        assertSame(first, group.getChildren().get(0));
        assertSame(second, group.getChildren().get(1));
        assertSame(counter, group.getChildren().get(2));
    }

    @Test
    void anEmptyGroupHasNoChildren() {
        assertTrue(new FxAvatarGroup().getChildren().isEmpty());
    }

    @Test
    void theCounterShowsAPlusAndTheTotal() {
        FxAvatarGroupCounter counter = new FxAvatarGroupCounter(120);

        assertEquals("+120", counter.getText());
        assertEquals(120, counter.getTotal());
        assertTrue(counter.getStyleClass().contains("fxk-avatar-counter"));
    }

    @Test
    void aCounterWithNoArgumentsShowsPlusZero() {
        assertEquals("+0", new FxAvatarGroupCounter().getText());
    }

    @Test
    void changingTheTotalUpdatesTheText() {
        FxAvatarGroupCounter counter = new FxAvatarGroupCounter(1);

        counter.setTotal(7);

        assertEquals("+7", counter.getText());
        assertEquals(7, counter.totalProperty().get());
    }
}
