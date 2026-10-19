package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.ref.WeakReference;

import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;

/** Node-facing behavior. Uses a plain Region (no toolkit start needed); disabled: is driven by setDisable. */
class ClassNameStylerTest {

    // ---- issue 3: ownership -------------------------------------------------------------------

    @Test
    void callersInlineDeclarationsSurviveApplyReapplyAndClear() {
        Region node = new Region();
        node.setStyle("-fx-background-color: red; -fx-padding: 4");

        ClassNameStyler.apply(node, "bg-blue-500");
        assertTrue(node.getStyle().contains("-fx-padding: 4"));
        assertTrue(node.getStyle().contains("-fx-background-color: -fxk-blue-500;"));

        ClassNameStyler.apply(node, "bg-red-500");
        assertTrue(node.getStyle().contains("-fx-padding: 4"));
        assertFalse(node.getStyle().contains("-fxk-blue-500"));

        ClassNameStyler.apply(node, null);
        assertEquals("-fx-background-color: red; -fx-padding: 4", node.getStyle());
    }

    @Test
    void semicolonsInsideUrlsDoNotConfuseOwnership() {
        Region node = new Region();
        String user = "-fx-background-image: url('data:image/png;base64,AAAA')";
        node.setStyle(user);
        ClassNameStyler.apply(node, "text-primary");
        ClassNameStyler.apply(node, "");
        assertEquals(user, node.getStyle());
    }

    @Test
    void clearingLeavesNoStyleBehindWhenThereWasNone() {
        Region node = new Region();
        ClassNameStyler.apply(node, "bg-blue-500");
        ClassNameStyler.apply(node, null);
        assertTrue(isEmpty(node.getStyle()), "style should be empty but was <" + node.getStyle() + ">");
    }

    @Test
    void callersStyleClassIsNeverRemoved() {
        Region node = new Region();
        node.getStyleClass().add("p-4");
        ClassNameStyler.apply(node, "p-4 text-lg");
        ClassNameStyler.apply(node, null);
        assertTrue(node.getStyleClass().contains("p-4"));
        assertFalse(node.getStyleClass().contains("text-lg"));
    }

    @Test
    void reapplyingTheSameStringIsIdempotent() {
        Region node = new Region();
        ClassNameStyler.apply(node, "bg-blue-500 text-lg");
        String style = node.getStyle();
        int classCount = node.getStyleClass().size();
        ClassNameStyler.apply(node, "bg-blue-500 text-lg");
        assertEquals(style, node.getStyle());
        assertEquals(classCount, node.getStyleClass().size());
    }

    @Test
    void disabledScopeLayersOnTopAndComesOffAgain() {
        Region node = new Region();
        ClassNameStyler.apply(node, "bg-blue-500 disabled:bg-gray-300 disabled:text-sm");
        assertTrue(node.getStyle().contains("-fxk-blue-500"));

        node.setDisable(true);
        assertTrue(node.getStyle().contains("-fxk-gray-300"));
        assertFalse(node.getStyle().contains("-fxk-blue-500"));
        assertTrue(node.getStyleClass().contains("text-sm"));

        node.setDisable(false);
        assertTrue(node.getStyle().contains("-fxk-blue-500"));
        assertFalse(node.getStyle().contains("-fxk-gray-300"));
        assertFalse(node.getStyleClass().contains("text-sm"));
    }

    @Test
    void clearingDetachesStateListeners() {
        Region node = new Region();
        ClassNameStyler.apply(node, "disabled:bg-gray-300");
        ClassNameStyler.apply(node, null);
        assertTrue(isEmpty(node.getStyle()));

        // if a listener had been left behind, this state change would write the style again
        node.setDisable(true);
        assertTrue(isEmpty(node.getStyle()), "style should be empty but was <" + node.getStyle() + ">");
        assertFalse(node.getStyleClass().contains("bg-gray-300"));
    }

    /** JavaFX reports "no inline style" as "" (never null); accept both so the intent is what's tested. */
    private static boolean isEmpty(String style) {
        return style == null || style.isEmpty();
    }

    // ---- issue 4: no static registry ----------------------------------------------------------

    @Test
    void aStyledNodeCanBeGarbageCollected() throws InterruptedException {
        WeakReference<Region> ref = styledNode();
        for (int i = 0; i < 40 && ref.get() != null; i++) {
            System.gc();
            Thread.sleep(25);
        }
        assertNull(ref.get());
    }

    private static WeakReference<Region> styledNode() {
        Region node = new Region();
        ClassNameStyler.apply(node, "hover:bg-red-500 focus:bg-blue-500 disabled:text-sm");
        return new WeakReference<>(node);
    }
}
