package dev.fxkit.showcase;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The color palette page: every scale, every semantic token, and a card built only from utility classes.
 *
 * <p>Nothing here hard-codes a color, a padding or a font size. Swatches are plain Regions carrying
 * utility classes such as {@code bg-blue-500}, so editing a token in {@code tokens.css} changes this page,
 * and switching the theme re-colors every swatch that uses a semantic token.
 *
 * <p>The hex value next to each swatch is read back from the color JavaFX actually painted, so it also
 * shows what a semantic token resolves to in the current theme.
 */
final class PalettePage {

    private static final String[] SCALES = {"gray", "blue", "red", "green", "yellow"};
    private static final int[] STEPS = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900};

    /** A semantic token (name without the -fxk- prefix) and what it is for. */
    private record Semantic(String name, String purpose) { }

    private static final List<Semantic> SEMANTIC = List.of(
            new Semantic("background", "App / window background"),
            new Semantic("surface", "Cards, panels, inputs"),
            new Semantic("surface-alt", "Subtle contrast areas, table stripes"),
            new Semantic("text", "Body text"),
            new Semantic("text-muted", "Secondary text, placeholders"),
            new Semantic("border", "Default borders, dividers"),
            new Semantic("border-strong", "Input borders, emphasis"),
            new Semantic("focus-ring", "Keyboard focus outline"),
            new Semantic("primary", "Main action / brand"),
            new Semantic("primary-hover", "Primary, hovered"),
            new Semantic("primary-pressed", "Primary, pressed"),
            new Semantic("on-primary", "Text/icon on a primary background"),
            new Semantic("danger", "Destructive actions, errors"),
            new Semantic("danger-hover", "Danger, hovered"),
            new Semantic("on-danger", "Text on danger"),
            new Semantic("success", "Success states"),
            new Semantic("success-hover", "Success, hovered"),
            new Semantic("on-success", "Text on success"),
            new Semantic("warning", "Warnings"),
            new Semantic("warning-hover", "Warning, hovered"),
            new Semantic("on-warning", "Text on warning"));

    private PalettePage() {
        // static factory only
    }

    /** Builds the page. Put it in a ScrollPane. */
    static Node create() {
        VBox page = new VBox(32,
                scaleSection(),
                semanticSection(),
                pairsSection(),
                utilityCardSection());
        page.getStyleClass().addAll("bg-background", "p-8");
        return page;
    }

    // ---- sections -----------------------------------------------------------------------------

    private static Node scaleSection() {
        VBox section = new VBox(12, heading("Color scales",
                "Palette utilities such as .bg-blue-500 use these. They look the same in both themes."));
        for (String scale : SCALES) {
            HBox row = new HBox(6);
            row.setAlignment(Pos.TOP_LEFT);

            Label name = text(scale, "text-sm", "font-semibold", "text-muted");
            name.setMinWidth(56);
            row.getChildren().add(name);

            for (int step : STEPS) {
                String token = scale + "-" + step;
                Region swatch = swatch(64, 36, "bg-" + token);
                Tooltip tip = new Tooltip("-fxk-" + token);
                Tooltip.install(swatch, tip);
                onColor(swatch, hex -> tip.setText("-fxk-" + token + "   " + hex));

                VBox cell = new VBox(4, swatch, text(String.valueOf(step), "text-xs", "text-muted"));
                cell.setAlignment(Pos.TOP_CENTER);
                row.getChildren().add(cell);
            }
            section.getChildren().add(row);
        }
        return section;
    }

    private static Node semanticSection() {
        GridPane grid = new GridPane();
        grid.getStyleClass().addAll("gap-x-4", "gap-y-2");

        String[] headers = {"", "Token", "Value now", "Use for"};
        for (int col = 0; col < headers.length; col++) {
            grid.add(text(headers[col], "text-xs", "font-semibold", "text-muted"), col, 0);
        }

        int row = 1;
        for (Semantic s : SEMANTIC) {
            Region swatch = swatch(48, 26, "bg-" + s.name());
            Label hexLabel = text("", "text-sm", "text-muted", "mono");
            onColor(swatch, hexLabel::setText);

            grid.add(swatch, 0, row);
            grid.add(text("-fxk-" + s.name(), "text-sm", "text-body", "mono"), 1, row);
            grid.add(hexLabel, 2, row);
            grid.add(text(s.purpose(), "text-sm", "text-body"), 3, row);
            row++;
        }
        return new VBox(12,
                heading("Semantic tokens",
                        "Components use these. Switch the theme to watch the values change (click the button above)."),
                grid);
    }

    /** Fill + "on" color pairs: the quickest way to eyeball contrast in each theme. */
    private static Node pairsSection() {
        HBox row = new HBox(12);
        for (String role : new String[] {"primary", "danger", "success", "warning"}) {
            Label chip = text(role + "  Aa", "text-base", "font-semibold", "bg-" + role, "text-on-" + role,
                    "rounded-md", "p-4");
            chip.setMinWidth(140);
            chip.setAlignment(Pos.CENTER);
            row.getChildren().add(chip);
        }
        return new VBox(12,
                heading("Fill and text pairs", "Each fill with its matching on-color text."),
                row);
    }

    /** #20 acceptance criterion: a node styled ONLY with utility classes (no CSS, no inline style). */
    private static Node utilityCardSection() {
        Label cardTitle = text("A card made only of utility classes", "text-xl", "font-bold", "text-body");
        Label cardBody = text("No stylesheet of its own and no inline styles: just style classes "
                + "from utilities.css, read from Java.", "text-sm", "text-muted");
        cardBody.setWrapText(true);

        HBox badges = new HBox(8,
                badge("New", "bg-primary", "text-on-primary"),
                badge("Stable", "bg-success", "text-on-success"),
                badge("Beta", "bg-warning", "text-on-warning"),
                badge("Removed", "bg-danger", "text-on-danger"));
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getStyleClass().add("gap-2");

        VBox card = new VBox(cardTitle, cardBody, badges);
        card.getStyleClass().addAll("bg-surface", "border", "rounded-lg", "p-6", "gap-3", "shadow-md");
        card.setMaxWidth(420);

        Label classes = text("bg-surface  border  rounded-lg  p-6  gap-3  shadow-md",
                "text-xs", "text-muted", "mono");

        return new VBox(12,
                heading("Utility classes", "Change the theme and the card follows: it only uses semantic classes."),
                card,
                classes);
    }

    // ---- small helpers ------------------------------------------------------------------------

    private static Node heading(String title, String subtitle) {
        VBox box = new VBox(2,
                text(title, "text-xl", "font-semibold", "text-body"),
                text(subtitle, "text-sm", "text-muted"));
        return box;
    }

    private static Label badge(String caption, String... classes) {
        Label badge = text(caption, "text-xs", "font-semibold", "rounded-full", "p-1");
        badge.getStyleClass().addAll(classes);
        badge.setMinWidth(Region.USE_PREF_SIZE);
        return badge;
    }

    private static Label text(String value, String... classes) {
        Label label = new Label(value);
        label.getStyleClass().addAll(classes);
        return label;
    }

    private static Region swatch(double width, double height, String colorClass) {
        Region box = new Region();
        box.setMinSize(width, height);
        box.setPrefSize(width, height);
        box.getStyleClass().addAll(colorClass, "rounded-md", "border");
        return box;
    }

    /**
     * Calls {@code consumer} with the hex of the color JavaFX paints in {@code swatch}, now and every time
     * the styling changes (first layout, theme switch, a token edit that is reloaded).
     */
    private static void onColor(Region swatch, Consumer<String> consumer) {
        Runnable read = () -> {
            var background = swatch.getBackground();
            if (background != null && !background.getFills().isEmpty()
                    && background.getFills().get(0).getFill() instanceof Color c) {
                consumer.accept(hex(c));
            }
        };
        swatch.backgroundProperty().addListener((obs, old, now) -> read.run());
        read.run();
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
    }
}
