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
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

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
        VBox page = new VBox(24, heading(), sizeAndVariantGrid(), disabledRow(), iconRow());
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

    /**
     * #36 (stretch) acceptance criteria: an icon from Ikonli's typed {@code Ikon}, colored to match the
     * variant (no color set here - {@code components.css} does that from each variant's own token), and
     * an icon-only button that stays accessible via {@link FxButton#setIcon(Ikon, String)}.
     */
    private static Node iconRow() {
        Ikon javaIcon = resolveIcon("di-java"); // DevIcons' Java logo, ships with the showcase (#36)

        FxButton withIcon = new FxButton("Java");
        withIcon.setVariant(Variant.PRIMARY);
        withIcon.setIcon(javaIcon);

        FxButton iconOnly = new FxButton();
        iconOnly.setVariant(Variant.SECONDARY);
        iconOnly.setIcon(javaIcon, "Java"); // sets the icon AND accessible text + a tooltip in one call

        HBox row = new HBox(12, withIcon, iconOnly);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("gap-3");
        return new VBox(12,
                heading("Icons", "setIcon(Ikon) (left) colors the icon from the variant's own token; "
                        + "the icon-only button (right, Tab to it) uses setIcon(Ikon, String) for its "
                        + "tooltip and accessible text."),
                row);
    }

    /**
     * Resolves a raw icon-literal string (e.g. {@code "di-java"}) to a real {@link Ikon} instance,
     * without this class needing to know which enum constant of which icon-pack class it maps to -
     * exactly the string used in {@code fxml-demo.fxml}'s {@code iconLiteral} attribute.
     */
    private static Ikon resolveIcon(String iconLiteral) {
        FontIcon probe = new FontIcon();
        probe.setIconLiteral(iconLiteral);
        return probe.getIconCode();
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
