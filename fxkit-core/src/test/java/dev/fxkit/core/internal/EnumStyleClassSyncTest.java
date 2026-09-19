package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;

class EnumStyleClassSyncTest {

    private enum Variant {
        PRIMARY,
        DANGER,
        ON_PRIMARY // exercises the underscore-to-hyphen naming rule
    }

    private static final String PREFIX = "fxk-btn-";

    @Test
    void addsTheStyleClassForTheInitialValueAtCreation() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(Variant.PRIMARY);

        EnumStyleClassSync.sync(node, PREFIX, variant);

        assertEquals(1, countFxkClasses(node));
        assertTrue(node.getStyleClass().contains("fxk-btn-primary"));
    }

    @Test
    void changingTheValueSwapsTheStyleClass() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(Variant.PRIMARY);
        EnumStyleClassSync.sync(node, PREFIX, variant);

        variant.set(Variant.DANGER);

        assertFalse(node.getStyleClass().contains("fxk-btn-primary"), "old style class should be removed");
        assertTrue(node.getStyleClass().contains("fxk-btn-danger"), "new style class should be added");
        assertEquals(1, countFxkClasses(node), "exactly one matching style class should be present");
    }

    @Test
    void underscoresInTheEnumNameBecomeHyphens() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(Variant.ON_PRIMARY);

        EnumStyleClassSync.sync(node, PREFIX, variant);

        assertTrue(node.getStyleClass().contains("fxk-btn-on-primary"));
    }

    @Test
    void aNullInitialValueAddsNoStyleClass() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(null);

        EnumStyleClassSync.sync(node, PREFIX, variant);

        assertEquals(0, countFxkClasses(node));
    }

    @Test
    void settingTheValueToNullRemovesTheStyleClassAndAddsNoReplacement() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(Variant.PRIMARY);
        EnumStyleClassSync.sync(node, PREFIX, variant);

        variant.set(null);

        assertEquals(0, countFxkClasses(node));
    }

    @Test
    void settingAValueAfterNullAddsTheStyleClass() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(null);
        EnumStyleClassSync.sync(node, PREFIX, variant);

        variant.set(Variant.DANGER);

        assertEquals(1, countFxkClasses(node));
        assertTrue(node.getStyleClass().contains("fxk-btn-danger"));
    }

    @Test
    void rejectsNullArguments() {
        Node node = new Region();
        ObjectProperty<Variant> variant = new SimpleObjectProperty<>(Variant.PRIMARY);

        assertThrows(NullPointerException.class, () -> EnumStyleClassSync.sync(null, PREFIX, variant));
        assertThrows(NullPointerException.class, () -> EnumStyleClassSync.sync(node, null, variant));
        assertThrows(NullPointerException.class, () -> EnumStyleClassSync.sync(node, PREFIX, null));
    }

    private static long countFxkClasses(Node node) {
        return node.getStyleClass().stream().filter(c -> c.startsWith(PREFIX)).count();
    }
}
