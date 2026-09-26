package dev.fxkit.showcase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * The showcase's navigation shell (#39): a sidebar (left) listing every {@link ShowcasePage}, and a
 * content area (center) that swaps to whichever one is selected. {@link ShowcaseApp} puts its header
 * (title, version, theme toggle) above this shell, so the header stays visible across every page and
 * still restyles whichever page is currently showing when the theme is toggled - nothing here touches
 * the header or the theme.
 *
 * <p>Pages are looked up from a small registry (a {@link ShowcasePage} per sidebar entry, see #40 for how
 * {@link ShowcaseApp} fills it) and built lazily: a page's content {@link java.util.function.Supplier} is
 * only invoked the first time that page is selected, then cached, so switching back to it later doesn't
 * rebuild it.
 *
 * <p>The sidebar and its selected entry are styled only with FXKit's existing utility classes
 * ({@code bg-surface-alt}, {@code bg-primary}/{@code text-on-primary} for the selected entry, {@code p-*},
 * {@code rounded-md}, ...) - no new hard-coded colors. The content area reuses the same
 * {@code showcase-scroll} approach {@link ShowcaseApp} used before this shell existed, so a tall page
 * still scrolls independently of the sidebar.
 */
final class ShowcaseShell {

    private static final double SIDEBAR_WIDTH = 220;

    /** Utility classes for a sidebar entry at rest. */
    private static final String[] RESTING_CLASSES = {"text-body", "font-medium"};

    /** Utility classes for the currently selected sidebar entry. */
    private static final String[] SELECTED_CLASSES = {"bg-primary", "text-on-primary", "font-semibold"};

    private ShowcaseShell() {
        // static factory only
    }

    /**
     * Builds the shell. The first entry in {@code pages} is shown on launch.
     *
     * @param pages every page the sidebar should list, in order; must not be empty
     */
    static Node create(List<ShowcasePage> pages) {
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("ShowcaseShell needs at least one page");
        }

        Map<ShowcasePage, Node> built = new HashMap<>();
        ScrollPane content = new ScrollPane();
        content.setFitToWidth(true);
        content.getStyleClass().add("showcase-scroll");

        ToggleGroup group = new ToggleGroup();
        VBox sidebar = new VBox();
        sidebar.getStyleClass().addAll("bg-surface-alt", "p-4", "gap-2");
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);

        for (ShowcasePage page : pages) {
            ToggleButton entry = navEntry(page.name(), group);
            entry.setOnAction(e -> content.setContent(
                    built.computeIfAbsent(page, p -> Objects.requireNonNull(p.content().get(),
                            p.name() + "'s content supplier returned null"))));
            sidebar.getChildren().add(entry);
        }

        // Default page (#39's "a sensible default page is shown on launch"): select the first entry
        // and fire it so the same onAction handler above also loads its content into the ScrollPane.
        ToggleButton first = (ToggleButton) sidebar.getChildren().get(0);
        first.setSelected(true);
        first.fire();

        BorderPane shell = new BorderPane(content);
        shell.setLeft(sidebar);
        return shell;
    }

    private static ToggleButton navEntry(String name, ToggleGroup group) {
        ToggleButton entry = new ToggleButton(name);
        entry.setToggleGroup(group);
        entry.setMaxWidth(Double.MAX_VALUE);
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.getStyleClass().addAll("rounded-md", "p-3", "text-sm");
        entry.getStyleClass().addAll(RESTING_CLASSES);
        entry.selectedProperty().addListener((obs, wasSelected, isSelected) -> restyle(entry, isSelected));
        return entry;
    }

    /** Swaps an entry between {@link #RESTING_CLASSES} and {@link #SELECTED_CLASSES} - no inline color. */
    private static void restyle(ToggleButton entry, boolean selected) {
        entry.getStyleClass().removeAll(RESTING_CLASSES);
        entry.getStyleClass().removeAll(SELECTED_CLASSES);
        entry.getStyleClass().addAll(selected ? SELECTED_CLASSES : RESTING_CLASSES);
    }
}
