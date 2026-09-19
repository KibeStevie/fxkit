package dev.fxkit.showcase;

import dev.fxkit.core.components.FxButton;
import dev.fxkit.core.components.FxButton.Size;
import dev.fxkit.core.components.FxButton.Variant;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The {@code FxButton} showcase page: every {@link Variant} against every {@link Size}, plus a disabled
 * twin of each variant, styled only through {@code FxButton}'s own properties and FXKit's stylesheets.
 *
 * <p>Nothing here sets a color, padding or font size by hand: switching the theme (the button in the
 * header) restyles every button on this page, which is the manual check for "looks right in light and
 * dark themes" (#27, #28, #29). {@code FxButton} is a plain {@code Button}, so its usual keyboard
 * behavior is unchanged: Tab onto any button below to see the focus ring (#29), then Space or Enter to
 * activate it.
 */
final class ButtonPage {

    private ButtonPage() {
        // static factory only
    }

    /** Builds the page. Put it in the same scroll content as the other showcase sections. */
    static Node create() {
        VBox page = new VBox(24, heading(), sizeAndVariantGrid(), disabledRow());
        page.getStyleClass().addAll("bg-background", "p-8");
        return page;
    }

    private static Node heading() {
        return new VBox(2,
                text("FxButton", "text-xl", "font-semibold", "text-body"),
                text("Every variant x size (#28), plus disabled (#29). "
                        + "Tab to a button to see the keyboard focus ring.", "text-sm", "text-muted"));
    }

    /** #28 task: "Check all 6 variants x 3 sizes in the showcase." */
    private static Node sizeAndVariantGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().addAll("gap-x-6", "gap-y-3");

        String[] headers = {"Variant", "SM", "MD", "LG"};
        for (int col = 0; col < headers.length; col++) {
            grid.add(text(headers[col], "text-xs", "font-semibold", "text-muted"), col, 0);
        }

        int row = 1;
        for (Variant variant : Variant.values()) {
            grid.add(text(variant.name(), "text-sm", "text-body", "mono"), 0, row);
            int col = 1;
            for (Size size : Size.values()) {
                grid.add(button(variant, size, "Button", false), col, row);
                col++;
            }
            row++;
        }
        return new VBox(12,
                heading("Variant x size", "Any Size combines with any Variant."),
                grid);
    }

    /** #29 acceptance criterion: "Distinct ... disabled styles for every variant." */
    private static Node disabledRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("gap-3");
        for (Variant variant : Variant.values()) {
            row.getChildren().add(button(variant, Size.MD, variant.name(), true));
        }
        return new VBox(12,
                heading("Disabled", "Each variant keeps its own resting colors, dimmed."),
                row);
    }

    private static FxButton button(Variant variant, Size size, String caption, boolean disabled) {
        FxButton button = new FxButton(caption);
        button.setVariant(variant);
        button.setSize(size);
        button.setDisable(disabled);
        return button;
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
