package dev.fxkit.core.components.blockquote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import dev.fxkit.core.TestNodes;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FxBlockquoteCaption shows an author's name and role, and only the divider between them when both
 * exist. Needs the toolkit; skipped without one.
 */
class FxBlockquoteCaptionTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    private static Label name(FxBlockquoteCaption caption) {
        return TestNodes.require(caption, "fxk-blockquote-cite", Label.class);
    }

    private static Label role(FxBlockquoteCaption caption) {
        return TestNodes.require(caption, "fxk-blockquote-cite-role", Label.class);
    }

    private static javafx.scene.layout.Region divider(FxBlockquoteCaption caption) {
        return TestNodes.require(caption, "fxk-blockquote-cite-divider", javafx.scene.layout.Region.class);
    }

    private static void assertShown(javafx.scene.Node node, boolean shown) {
        assertEquals(shown, node.isVisible(), "visible");
        assertEquals(shown, node.isManaged(), "managed: a hidden part must not take up space");
    }

    // ---- name, role, divider ----------------------------------------------------------------

    @Test
    void nameAndRoleAreShownWithADividerBetweenThem() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption("Micheal Gough", "CEO at Google");

        assertEquals("Micheal Gough", name(caption).getText());
        assertEquals("CEO at Google", role(caption).getText());
        assertShown(name(caption), true);
        assertShown(role(caption), true);
        assertShown(divider(caption), true);
    }

    @Test
    void withoutARoleThereIsNoDivider() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption("Micheal Gough", null);

        assertShown(name(caption), true);
        assertShown(role(caption), false);
        assertShown(divider(caption), false);
    }

    @Test
    void withoutANameThereIsNoDivider() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption("", "CEO at Google");

        assertShown(name(caption), false);
        assertShown(role(caption), true);
        assertShown(divider(caption), false);
    }

    @Test
    void blankTextCountsAsMissing() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption("   ", "   ");

        assertShown(name(caption), false);
        assertShown(role(caption), false);
        assertShown(divider(caption), false);
    }

    @Test
    void theVisibilityFollowsLaterChanges() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption();
        assertShown(divider(caption), false);

        caption.setName("Jane");
        caption.setRole("Engineer");
        assertShown(divider(caption), true);
        assertEquals("Jane", name(caption).getText());

        caption.setRole(null);
        assertShown(role(caption), false);
        assertShown(divider(caption), false);
    }

    // ---- avatar -----------------------------------------------------------------------------

    @Test
    void theCaptionIsJustTheCitesUntilAnAvatarIsSet() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption("Jane", "Engineer");

        assertEquals(1, caption.getChildren().size());
        HBox cites = (HBox) caption.getChildren().get(0);
        assertTrue(cites.getStyleClass().contains("fxk-blockquote-cites"));
    }

    @Test
    void anAvatarComesBeforeTheCitesAndCanBeRemoved() {
        Label avatar = new Label("av");
        FxBlockquoteCaption caption = new FxBlockquoteCaption(avatar, "Jane", "Engineer");

        assertEquals(2, caption.getChildren().size());
        assertSame(avatar, caption.getChildren().get(0));
        assertTrue(caption.getChildren().get(1).getStyleClass().contains("fxk-blockquote-cites"));

        caption.setAvatar(null);
        assertEquals(1, caption.getChildren().size());
        assertTrue(caption.getChildren().get(0).getStyleClass().contains("fxk-blockquote-cites"));
    }

    // ---- style classes ----------------------------------------------------------------------

    @Test
    void centeredAndStrongDividerAreStyleClassesThatFollowTheirProperties() {
        FxBlockquoteCaption caption = new FxBlockquoteCaption("Jane", "Engineer");
        assertTrue(caption.getStyleClass().contains("fxk-blockquote-caption"));
        assertFalse(caption.getStyleClass().contains("fxk-blockquote-caption-centered"));
        assertFalse(caption.getStyleClass().contains("fxk-blockquote-caption-strong"));

        caption.setCentered(true);
        caption.setStrongDivider(true);
        assertTrue(caption.getStyleClass().contains("fxk-blockquote-caption-centered"));
        assertTrue(caption.getStyleClass().contains("fxk-blockquote-caption-strong"));

        caption.setCentered(false);
        caption.setStrongDivider(false);
        assertFalse(caption.getStyleClass().contains("fxk-blockquote-caption-centered"));
        assertFalse(caption.getStyleClass().contains("fxk-blockquote-caption-strong"));
    }
}
