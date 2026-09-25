package dev.fxkit.core.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.components.FxCard.Elevation;
import dev.fxkit.core.testsupport.JavaFxToolkit;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link FxCard}'s slot and elevation logic (#31, #32).
 *
 * <p>Every test here needs the JavaFX toolkit running (see {@link JavaFxToolkit}): filling more than one
 * slot makes {@code FxCard} construct a {@link Separator}, and {@code Separator} is a {@code Control}
 * like {@code FxButton}/{@code Button} (see {@code FxButtonTest}'s Javadoc), so the same toolkit
 * requirement applies even though these tests otherwise only build a card and read its children/getters.
 */
@ExtendWith(JavaFxToolkit.class)
class FxCardTest {

    @Test
    void emptyCardHasBaseStyleClassAndNoChildren() {
        FxCard card = new FxCard();

        assertTrue(card.getStyleClass().contains(FxCard.STYLE_CLASS));
        assertTrue(card.getChildren().isEmpty(), "a card with no slots filled should have no children");
    }

    @Test
    void defaultElevationIsMd() {
        FxCard card = new FxCard();

        assertEquals(Elevation.MD, card.getElevation());
        assertEquals(FxCard.DEFAULT_ELEVATION, card.getElevation());
        assertTrue(card.getStyleClass().contains("fxk-card-elevation-md"));
    }

    @Test
    void settingOnlyBodyTakesNoSpaceForHeaderOrFooter() {
        FxCard card = new FxCard();

        card.setBody(new Label("Body"));

        assertEquals(1, card.getChildren().size(), "only the body wrapper should be present");
        assertEquals(0, dividerCount(card), "a single slot should never produce a divider");
        assertNull(card.getHeader());
        assertNull(card.getFooter());
    }

    @Test
    void twoAdjacentSlotsGetExactlyOneDivider() {
        FxCard card = new FxCard();

        card.setHeader(new Label("Header"));
        card.setBody(new Label("Body"));

        assertEquals(3, card.getChildren().size(), "header wrapper + divider + body wrapper");
        assertEquals(1, dividerCount(card));
    }

    @Test
    void allThreeSlotsGetTwoDividers() {
        FxCard card = new FxCard();

        card.setHeader(new Label("Header"));
        card.setBody(new Label("Body"));
        card.setFooter(new Label("Footer"));

        assertEquals(5, card.getChildren().size(), "3 wrappers + 2 dividers");
        assertEquals(2, dividerCount(card));
    }

    @Test
    void clearingASlotRemovesItsWrapperAndAnyNowExtraDivider() {
        FxCard card = new FxCard();
        card.setHeader(new Label("Header"));
        card.setBody(new Label("Body"));

        card.setHeader(null);

        assertEquals(1, card.getChildren().size(), "clearing the header should leave only the body");
        assertEquals(0, dividerCount(card));
        assertNull(card.getHeader());
    }

    @Test
    void gettersReflectWhatWasSet() {
        FxCard card = new FxCard();
        Label header = new Label("Header");
        Label body = new Label("Body");
        Label footer = new Label("Footer");

        card.setHeader(header);
        card.setBody(body);
        card.setFooter(footer);

        assertEquals(header, card.getHeader());
        assertEquals(body, card.getBody());
        assertEquals(footer, card.getFooter());
    }

    @Test
    void changingElevationUpdatesStyleClasses() {
        FxCard card = new FxCard();

        card.setElevation(Elevation.LG);

        assertEquals(Elevation.LG, card.getElevation());
        assertFalse(card.getStyleClass().contains("fxk-card-elevation-md"), "old elevation class should be removed");
        assertTrue(card.getStyleClass().contains("fxk-card-elevation-lg"), "new elevation class should be added");
    }

    @Test
    void everyElevationProducesExactlyOneElevationStyleClass() {
        FxCard card = new FxCard();

        for (Elevation elevation : Elevation.values()) {
            card.setElevation(elevation);

            long elevationClasses = card.getStyleClass().stream().filter(c -> c.startsWith("fxk-card-elevation-")).count();
            assertEquals(1, elevationClasses, "exactly one elevation style class for " + elevation);
            assertTrue(card.getStyleClass().contains("fxk-card-elevation-" + elevation.name().toLowerCase()));
        }
    }

    @Test
    void elevationPropertyReflectsSetElevation() {
        FxCard card = new FxCard();

        card.elevationProperty().set(Elevation.NONE);

        assertEquals(Elevation.NONE, card.getElevation());
        assertTrue(card.getStyleClass().contains("fxk-card-elevation-none"));
    }

    private static long dividerCount(FxCard card) {
        return card.getChildren().stream().filter(Separator.class::isInstance).count();
    }
}
