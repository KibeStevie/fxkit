package dev.fxkit.core.components.dropdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.FxThreadExtension;
import javafx.beans.DefaultProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The small dropdown parts: the clickable item, the two-line header and the divider. Icons are not
 * covered here: they need an Ikonli icon pack on the test classpath. Runs on the FX thread; skipped
 * without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxDropdownPartsTest {

    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");

    // ---- item -------------------------------------------------------------------------------

    @Test
    void anItemIsAFillingButtonThatTabAndMnemonicsLeaveAlone() {
        FxDropdownItem item = new FxDropdownItem("Sign_out");

        assertEquals("Sign_out", item.getText());
        assertTrue(item.getStyleClass().contains("fxk-dropdown-item"));
        assertFalse(item.isMnemonicParsing(), "an underscore must show as an underscore");
        assertFalse(item.isFocusTraversable(), "the dropdown drives highlighting; Tab must not land here");
        assertEquals(Double.MAX_VALUE, item.getMaxWidth(), 0, "the whole row is clickable");
        assertNull(item.getIcon());
        assertNull(item.getGraphic());
    }

    @Test
    void anItemRunsItsHandlerUnlessDisabled() {
        FxDropdownItem item = new FxDropdownItem("Settings");
        AtomicInteger calls = new AtomicInteger();
        item.setOnAction(e -> calls.incrementAndGet());

        item.fire();
        assertEquals(1, calls.get());

        item.setDisable(true);
        item.fire();
        assertEquals(1, calls.get());
    }

    @Test
    void theActiveHighlightIsAPseudoClassTheDropdownTurnsOnAndOff() {
        FxDropdownItem item = new FxDropdownItem("Settings");
        assertFalse(item.getPseudoClassStates().contains(ACTIVE));

        item.setActive(true);
        assertTrue(item.getPseudoClassStates().contains(ACTIVE));

        item.setActive(false);
        assertFalse(item.getPseudoClassStates().contains(ACTIVE));
    }

    // ---- header -----------------------------------------------------------------------------

    @Test
    void aTwoLineHeaderShowsAPrimaryAndASecondaryLabel() {
        FxDropdownHeader header = new FxDropdownHeader("Bonnie Green", "bonnie@flowbite.com");

        assertTrue(header.getStyleClass().contains("fxk-dropdown-header"));
        assertEquals(2, header.getContent().size());
        Label primary = assertInstanceOf(Label.class, header.getContent().get(0));
        Label secondary = assertInstanceOf(Label.class, header.getContent().get(1));
        assertEquals("Bonnie Green", primary.getText());
        assertTrue(primary.getStyleClass().contains("fxk-dropdown-header-title"));
        assertEquals("bonnie@flowbite.com", secondary.getText());
        assertTrue(secondary.getStyleClass().contains("fxk-dropdown-header-subtitle"));
    }

    @Test
    void aHeaderDrawsItsOwnDividerBelowItsContent() {
        FxDropdownHeader header = new FxDropdownHeader("a", "b");

        assertEquals(2, header.getChildren().size());
        VBox content = assertInstanceOf(VBox.class, header.getChildren().get(0));
        assertTrue(content.getStyleClass().contains("fxk-dropdown-header-content"));
        assertInstanceOf(FxDropdownDivider.class, header.getChildren().get(1));
        assertEquals(2, header.getContent().size(), "the divider is not part of the content list");
    }

    @Test
    void aHeaderCanHoldAnyNodes() {
        Region first = new Region();
        Region second = new Region();

        FxDropdownHeader header = new FxDropdownHeader(first, second);

        assertSame(first, header.getContent().get(0));
        assertSame(second, header.getContent().get(1));
    }

    @Test
    void theHeaderContentListIsLiveAndStartsEmpty() {
        FxDropdownHeader header = new FxDropdownHeader();
        assertTrue(header.getContent().isEmpty());

        Label added = new Label("later");
        header.getContent().add(added);

        assertSame(added, header.getContent().get(0));
    }

    @Test
    void headerContentIsTheFxmlDefaultProperty() {
        assertEquals("content", FxDropdownHeader.class.getAnnotation(DefaultProperty.class).value());
    }

    // ---- divider ----------------------------------------------------------------------------

    @Test
    void theDividerIsAPlainRegionWithItsStyleClass() {
        FxDropdownDivider divider = new FxDropdownDivider();

        assertTrue(divider.getStyleClass().contains("fxk-dropdown-divider"));
        assertTrue(divider.getChildrenUnmodifiable().isEmpty());
    }
}
