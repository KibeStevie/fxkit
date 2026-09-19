package dev.fxkit.showcase;

import dev.fxkit.core.components.FxButton;
import dev.fxkit.core.components.FxButton.Variant;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * The {@code FxButton} showcase page: every {@link Variant}, enabled and disabled, styled only through
 * {@code FxButton}'s own {@code variant} property and FXKit's stylesheets.
 *
 * <p>Nothing here sets a color or a size by hand: switching the theme (the button in the header) restyles
 * every button on this page, which is the manual check for the "looks right in light and dark themes"
 * acceptance criterion on #27.
 */
final class ButtonPage {

    private ButtonPage() {
        // static factory only
    }

    /** Builds the page. Put it in the same scroll content as the other showcase sections. */
    static Node create() {
        VBox page = new VBox(24, heading(), variantGrid());
        page.getStyleClass().addAll("bg-background", "p-8");
        return page;
    }

    private static Node heading() {
        return new VBox(2,
                text("FxButton", "text-xl", "font-semibold", "text-body"),
                text("Every variant, enabled and disabled. Style comes from FxButton.setVariant(...) alone.",
                        "text-sm", "text-muted"));
    }

    private static Node variantGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().addAll("gap-x-6", "gap-y-3");

        String[] headers = {"Variant", "Enabled", "Disabled"};
        for (int col = 0; col < headers.length; col++) {
            grid.add(text(headers[col], "text-xs", "font-semibold", "text-muted"), col, 0);
        }

        int row = 1;
        for (Variant variant : Variant.values()) {
            grid.add(text(variant.name(), "text-sm", "text-body", "mono"), 0, row);
            grid.add(button(variant, "Button", false), 1, row);
            grid.add(button(variant, "Button", true), 2, row);
            row++;
        }
        return grid;
    }

    private static FxButton button(Variant variant, String caption, boolean disabled) {
        FxButton button = new FxButton(caption);
        button.setVariant(variant);
        button.setDisable(disabled);
        return button;
    }

    private static Label text(String value, String... classes) {
        Label label = new Label(value);
        label.getStyleClass().addAll(classes);
        return label;
    }
}
