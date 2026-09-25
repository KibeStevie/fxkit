package dev.fxkit.showcase;

import dev.fxkit.core.components.FxButton;
import dev.fxkit.core.components.FxCard;
import dev.fxkit.core.components.FxCard.Elevation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The {@code FxCard} showcase page: one card per {@link Elevation} level, plus a card that only fills
 * some of its slots, to demonstrate "empty slots take no space" (#31, #32).
 *
 * <p>Nothing here sets a color, border, radius or shadow by hand: switching the theme (the button in the
 * header) restyles every card on this page, which is the manual check for "looks right in both themes"
 * (#32's acceptance criterion).
 */
final class CardPage {

    private CardPage() {
        // static factory only
    }

    /** Builds the page. Put it in the same scroll content as the other showcase sections. */
    static Node create() {
        VBox page = new VBox(24, heading(), elevationRow(), partialSlotsRow());
        page.getStyleClass().addAll("bg-background", "p-8");
        return page;
    }

    private static Node heading() {
        return new VBox(2,
                text("FxCard", "text-xl", "font-semibold", "text-body"),
                text("Header/body/footer slots (#31) and elevation levels (#32).",
                        "text-sm", "text-muted"));
    }

    /** #32 acceptance criterion: an elevation property with levels mapped to the shadow scale. */
    private static Node elevationRow() {
        HBox row = new HBox(16);
        row.getStyleClass().add("gap-4");
        for (Elevation elevation : Elevation.values()) {
            row.getChildren().add(fullCard(elevation));
        }
        return new VBox(12,
                heading("Elevation", "NONE, SM, MD (default) and LG, each mapped to a shadow token."),
                row);
    }

    /** #31 acceptance criterion: "Empty slots take no space." Only the body slot is filled here. */
    private static Node partialSlotsRow() {
        FxCard bodyOnly = new FxCard();
        bodyOnly.setBody(text("A card with only a body - no header or footer divider appears.",
                "text-sm", "text-body"));

        FxCard headerAndBody = new FxCard();
        headerAndBody.setHeader(text("No footer", "text-lg", "font-semibold", "text-body"));
        headerAndBody.setBody(text("This card has a header and a body, but no footer slot.",
                "text-sm", "text-muted"));

        HBox row = new HBox(16, bodyOnly, headerAndBody);
        row.getStyleClass().add("gap-4");
        return new VBox(12,
                heading("Partial slots", "Any subset of header/body/footer can be set independently."),
                row);
    }

    private static FxCard fullCard(Elevation elevation) {
        FxCard card = new FxCard();
        card.setElevation(elevation);
        card.setHeader(text(elevation.name(), "text-lg", "font-semibold", "text-body"));
        card.setBody(text("This can't be undone.", "text-sm", "text-muted"));
        FxButton deleteButton = new FxButton("Delete");
        deleteButton.setVariant(FxButton.Variant.DANGER);
        card.setFooter(deleteButton);
        return card;
    }

    private static Node heading(String title, String subtitle) {
        return new VBox(2,
                text(title, "text-lg", "font-semibold", "text-body"),
                text(subtitle, "text-sm", "text-muted"));
    }

    private static Label text(String value, String... classes) {
        Label label = new Label(value);
        label.getStyleClass().addAll(classes);
        return label;
    }
}
