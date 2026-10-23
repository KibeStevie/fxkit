package dev.fxkit.core.components.button;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.FxThreadExtension;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * FxButtonGroup tags each child with its position (start/middle/end/only) and, when told to, restyles
 * its FxButtons as one set, remembering and restoring each button's own look. Runs on the FX thread;
 * skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxButtonGroupTest {

    private FxButton a;
    private FxButton b;
    private FxButton c;

    @BeforeEach
    void createButtons() {
        a = new FxButton("A");
        b = new FxButton("B");
        c = new FxButton("C");
    }

    private static boolean has(javafx.scene.Node node, String styleClass) {
        return node.getStyleClass().contains(styleClass);
    }

    // ---- structure and joining --------------------------------------------------------------

    @Test
    void theGroupHugsItsButtonsAndStartsPlain() {
        FxButtonGroup group = new FxButtonGroup();

        assertTrue(has(group, FxButtonGroup.STYLE_CLASS));
        assertEquals(Region.USE_PREF_SIZE, group.getMaxWidth());
        assertFalse(group.isOutline());
        assertFalse(group.isPill());
        assertEquals(null, group.getColor());
        assertTrue(group.getChildren().isEmpty());
    }

    @Test
    void aSingleButtonIsOnly() {
        new FxButtonGroup(a);

        assertTrue(has(a, "fxk-btn-group-item"));
        assertTrue(has(a, "fxk-btn-group-only"));
        assertFalse(has(a, "fxk-btn-group-start"));
    }

    @Test
    void twoButtonsAreStartAndEnd() {
        new FxButtonGroup(a, b);

        assertTrue(has(a, "fxk-btn-group-start"));
        assertTrue(has(b, "fxk-btn-group-end"));
    }

    @Test
    void threeButtonsAreStartMiddleEnd() {
        new FxButtonGroup(a, b, c);

        assertTrue(has(a, "fxk-btn-group-start"));
        assertTrue(has(b, "fxk-btn-group-middle"));
        assertTrue(has(c, "fxk-btn-group-end"));
        for (FxButton button : new FxButton[] {a, b, c}) {
            assertTrue(has(button, "fxk-btn-group-item"));
        }
    }

    @Test
    void eachButtonHasExactlyOnePositionClass() {
        new FxButtonGroup(a, b, c);

        for (FxButton button : new FxButton[] {a, b, c}) {
            long positions = button.getStyleClass().stream()
                    .filter(s -> s.matches("fxk-btn-group-(start|middle|end|only)")).count();
            assertEquals(1, positions);
        }
    }

    @Test
    void positionsAreRecomputedWhenChildrenChange() {
        FxButtonGroup group = new FxButtonGroup(a, b);

        group.getChildren().add(c);
        assertTrue(has(b, "fxk-btn-group-middle"));
        assertFalse(has(b, "fxk-btn-group-end"));
        assertTrue(has(c, "fxk-btn-group-end"));

        group.getChildren().remove(a);
        assertTrue(has(b, "fxk-btn-group-start"));
        assertFalse(has(b, "fxk-btn-group-middle"));
    }

    @Test
    void aRemovedChildLosesAllItsGroupClasses() {
        FxButtonGroup group = new FxButtonGroup(a, b, c);

        group.getChildren().remove(c);

        assertTrue(c.getStyleClass().stream().noneMatch(s -> s.startsWith("fxk-btn-group-")));
    }

    @Test
    void childrenThatAreNotButtonsAreJoinedToo() {
        Label label = new Label("not a button");

        new FxButtonGroup(a, label);

        assertTrue(has(label, "fxk-btn-group-item"));
        assertTrue(has(label, "fxk-btn-group-end"));
    }

    @Test
    void pillAndOutlineAreStyleClassesOnTheGroup() {
        FxButtonGroup group = new FxButtonGroup(a, b);

        group.setPill(true);
        group.setOutline(true);
        assertTrue(has(group, "fxk-btn-group-pill"));
        assertTrue(has(group, "fxk-btn-group-outline"));

        group.setPill(false);
        group.setOutline(false);
        assertFalse(has(group, "fxk-btn-group-pill"));
        assertFalse(has(group, "fxk-btn-group-outline"));
    }

    // ---- managing the buttons' look ---------------------------------------------------------

    @Test
    void aPlainGroupLeavesTheButtonsAlone() {
        a.setVariant(FxButton.Variant.DANGER);
        a.setColor(FxButton.Color.GREEN);

        new FxButtonGroup(a, b);

        assertEquals(FxButton.Variant.DANGER, a.getVariant());
        assertEquals(FxButton.Color.GREEN, a.getColor());
        assertEquals(FxButton.Variant.PRIMARY, b.getVariant());
    }

    @Test
    void aGroupColorMakesEveryButtonADefaultVariantButtonInThatColor() {
        FxButtonGroup group = new FxButtonGroup(a, b);

        group.setColor(FxButton.Color.CYAN);

        for (FxButton button : new FxButton[] {a, b}) {
            assertEquals(FxButton.Variant.DEFAULT, button.getVariant());
            assertEquals(FxButton.Color.CYAN, button.getColor());
        }
    }

    @Test
    void outlineMakesEveryButtonAnOutlineButtonAndKeepsItsOwnColor() {
        a.setColor(FxButton.Color.GREEN);
        FxButtonGroup group = new FxButtonGroup(a, b);

        group.setOutline(true);

        assertEquals(FxButton.Variant.OUTLINE, a.getVariant());
        assertEquals(FxButton.Color.GREEN, a.getColor());
        assertEquals(FxButton.Variant.OUTLINE, b.getVariant());
    }

    @Test
    void outlineAndColorTogetherGiveOutlineButtonsInThatColor() {
        FxButtonGroup group = new FxButtonGroup(a, b);

        group.setOutline(true);
        group.setColor(FxButton.Color.PURPLE);

        assertEquals(FxButton.Variant.OUTLINE, a.getVariant());
        assertEquals(FxButton.Color.PURPLE, a.getColor());

        group.setOutline(false);
        assertEquals(FxButton.Variant.DEFAULT, a.getVariant());
        assertEquals(FxButton.Color.PURPLE, a.getColor());
    }

    @Test
    void stoppingTheManagementRestoresEachButtonsOwnLook() {
        a.setVariant(FxButton.Variant.DANGER);
        a.setColor(FxButton.Color.GREEN);
        FxButtonGroup group = new FxButtonGroup(a, b);
        group.setColor(FxButton.Color.CYAN);

        group.setColor(null);

        assertEquals(FxButton.Variant.DANGER, a.getVariant());
        assertEquals(FxButton.Color.GREEN, a.getColor());
        assertEquals(FxButton.Variant.PRIMARY, b.getVariant());
        assertEquals(FxButton.Color.DEFAULT, b.getColor());
    }

    @Test
    void theLookRestoredIsTheOneFromBeforeTheFirstOverrideNotAnIntermediateOne() {
        a.setVariant(FxButton.Variant.DANGER);
        a.setColor(FxButton.Color.GREEN);
        FxButtonGroup group = new FxButtonGroup(a);

        group.setColor(FxButton.Color.CYAN);
        group.setColor(FxButton.Color.RED);
        assertEquals(FxButton.Color.RED, a.getColor());

        group.setColor(null);
        assertEquals(FxButton.Variant.DANGER, a.getVariant());
        assertEquals(FxButton.Color.GREEN, a.getColor());
    }

    @Test
    void aButtonRemovedFromAManagedGroupGetsItsOwnLookBack() {
        FxButtonGroup group = new FxButtonGroup(a, b);
        group.setColor(FxButton.Color.CYAN);

        group.getChildren().remove(a);

        assertEquals(FxButton.Variant.PRIMARY, a.getVariant());
        assertEquals(FxButton.Color.DEFAULT, a.getColor());
        assertEquals(FxButton.Color.CYAN, b.getColor(), "the button that stayed is still managed");
    }

    @Test
    void aButtonAddedToAManagedGroupIsStyledAtOnce() {
        FxButtonGroup group = new FxButtonGroup(a);
        group.setColor(FxButton.Color.CYAN);

        group.getChildren().add(b);

        assertEquals(FxButton.Variant.DEFAULT, b.getVariant());
        assertEquals(FxButton.Color.CYAN, b.getColor());
    }

    @Test
    void nonButtonChildrenAreNeverRestyled() {
        Label label = new Label("plain");
        FxButtonGroup group = new FxButtonGroup(a, label);

        group.setColor(FxButton.Color.CYAN);
        group.setOutline(true);
        group.setColor(null);
        group.setOutline(false);

        assertEquals("plain", label.getText());
        assertTrue(has(label, "fxk-btn-group-end"));
    }
}
