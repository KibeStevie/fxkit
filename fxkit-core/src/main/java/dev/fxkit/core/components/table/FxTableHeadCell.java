package dev.fxkit.core.components.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Screen;

/**
 * A cell of the table head (Flowbite's {@code <TableHeadCell>}): small, bold,
 * upper-case text.
 *
 * <p>
 * JavaFX CSS has no {@code text-transform}, so the text passed to
 * {@link #FxTableHeadCell(String)}
 * is upper-cased here. A head cell can also hold a node (for example a "select
 * all" checkbox), or
 * nothing at all, which is Flowbite's
 * {@code <span className="sr-only">Edit</span>} column.
 *
 * <h2>Filtering</h2>
 * With {@link #filterableProperty() filterable} on, a funnel button appears at
 * the right of the cell.
 * It opens a small popup with a text field; the table then shows only the rows
 * whose cell in this
 * column <em>contains</em> that text (case-insensitive). The funnel is filled
 * while a filter is active.
 * Filters of several columns combine with AND. The filter belongs to the
 * column, so it follows it when
 * the user drags the column somewhere else.
 *
 * <h2>Moving</h2>
 * With the table's {@code reorderable} on, a head cell can be dragged to a new
 * position. Set
 * {@link #movableProperty() movable} to {@code false} to pin it (for example
 * the checkbox column).
 * Nothing can be dropped onto a pinned column either.
 */
public class FxTableHeadCell extends FxTableCell {

    private static final String PINNED = "fxk-table-head-cell-pinned";
    private static final double POPUP_MARGIN = 12;

    private final BooleanProperty filterable = new SimpleBooleanProperty(this, "filterable", false);
    private final BooleanProperty movable = new SimpleBooleanProperty(this, "movable", true);
    private final StringProperty filterText = new SimpleStringProperty(this, "filterText", "");

    private final HBox content = new HBox(8);
    private Button filterButton;
    private Popup popup;
    private StackPane popupHost;

    /** Creates an empty head cell. */
    public FxTableHeadCell() {
        super();
        init();
    }

    /**
     * Creates a head cell showing {@code text} in upper case.
     *
     * @param text the header text, {@code null} for an empty cell
     */
    public FxTableHeadCell(String text) {
        super(text == null ? null : text.toUpperCase(Locale.ROOT));
        init();
    }

    /**
     * Creates a head cell showing {@code content}.
     *
     * @param content the node to show, {@code null} for an empty cell
     */
    public FxTableHeadCell(Node content) {
        super(content);
        init();
    }

    private void init() {
        getStyleClass().add("fxk-table-head-cell");

        // Wrap whatever the cell holds in a row, so the filter button can sit to its
        // right.
        List<Node> existing = new ArrayList<>(getChildren());
        getChildren().clear();
        content.getStyleClass().add("fxk-table-head-content");
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().setAll(existing);
        for (Node node : existing) {
            if (node instanceof Label label) {
                label.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(label, Priority.ALWAYS);
            }
        }
        getChildren().add(content);

        filterable.addListener((obs, was, is) -> {
            if (is) {
                content.getChildren().add(createFilterButton());
            } else {
                removeFilterButton();
                filterText.set("");
            }
            notifyTable();
        });
        filterText.addListener((obs, was, is) -> {
            if (filterButton != null) {
                boolean active = is != null && !is.isBlank();
                filterButton.getStyleClass().remove("fxk-table-filter-active");
                if (active) {
                    filterButton.getStyleClass().add("fxk-table-filter-active");
                }
            }
            notifyTable();
        });
        movable.addListener((obs, was, is) -> {
            getStyleClass().remove(PINNED);
            if (!is) {
                getStyleClass().add(PINNED);
            }
        });
    }

    private void notifyTable() {
        FxTable table = FxTable.ownerOf(this);
        if (table != null) {
            table.applyFilters();
        }
    }

    // ---- filter button and popup
    // ---------------------------------------------------------------

    private Button createFilterButton() {
        SVGPath funnel = new SVGPath();
        funnel.setContent("M1.5 2.5h9L7 6.75V10.5L5 9.5V6.75Z");
        funnel.getStyleClass().add("fxk-table-filter-icon");

        filterButton = new Button();
        filterButton.setGraphic(funnel);
        filterButton.getStyleClass().add("fxk-table-filter-button");
        filterButton.setOnAction(event -> toggleFilterPopup());
        if (filterText.get() != null && !filterText.get().isBlank()) {
            filterButton.getStyleClass().add("fxk-table-filter-active");
        }
        return filterButton;
    }

    private void removeFilterButton() {
        if (popup != null) {
            popup.hide();
        }
        if (filterButton != null) {
            content.getChildren().remove(filterButton);
            filterButton = null;
        }
    }

    private void toggleFilterPopup() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
            return;
        }
        Scene scene = filterButton.getScene();
        if (scene == null) {
            return;
        }
        if (popup == null) {
            popup = buildPopup();
        }
        // The popup is its own window: it does not inherit the stylesheets or the theme
        // of the table's
        // scene, so copy both. "root" makes the popup content a token scope of its own
        // (tokens.css).
        popup.getScene().getStylesheets().setAll(scene.getStylesheets());
        popupHost.getStyleClass().remove(ThemeManager.DARK_CLASS);
        if (ThemeManager.current(scene) == Theme.DARK) {
            popupHost.getStyleClass().add(ThemeManager.DARK_CLASS);
        }
        Bounds bounds = filterButton.localToScreen(filterButton.getBoundsInLocal());
        double y = bounds.getMaxY() + 4 - POPUP_MARGIN;
        popup.show(filterButton, bounds.getMinX(), y);

        // The popup only knows its width once it is showing: line its right edge up
        // with the funnel's
        // right edge, so it opens below the icon and extends to the left. Keep it on
        // the screen.
        Screen screen = Screen.getScreensForRectangle(bounds.getMinX(), bounds.getMinY(), 1, 1)
                .stream().findFirst().orElse(Screen.getPrimary());
        double x = bounds.getMaxX() + POPUP_MARGIN - popup.getWidth();
        popup.setX(Math.max(x, screen.getVisualBounds().getMinX()));
    }

    private Popup buildPopup() {
        TextField field = new TextField();
        field.setPromptText("Filter\u2026");
        field.setPrefColumnCount(14);
        field.getStyleClass().add("fxk-table-filter-input");
        field.textProperty().bindBidirectional(filterText);

        Hyperlink clear = new Hyperlink("Clear");
        clear.getStyleClass().add("fxk-table-link");
        clear.setOnAction(event -> filterText.set(""));

        HBox footer = new HBox(clear);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(field, footer);
        card.getStyleClass().add("fxk-table-filter-popup");

        popupHost = new StackPane(card); // transparent margin, so the card's shadow is not clipped
        popupHost.getStyleClass().addAll("root", "fxk-table-filter-host");

        Popup result = new Popup();
        result.setAutoHide(true);
        result.setHideOnEscape(true);
        result.getContent().add(popupHost);
        result.setOnShown(event -> {
            field.requestFocus();
            field.selectAll();
        });
        field.setOnAction(event -> result.hide());
        return result;
    }

    // ---- filterable
    // ----------------------------------------------------------------------------

    /** @return whether this column shows a filter button */
    public final boolean isFilterable() {
        return filterable.get();
    }

    /**
     * @param value {@code true} to show the filter button; {@code false} also
     *              clears the filter
     */
    public final void setFilterable(boolean value) {
        filterable.set(value);
    }

    /** @return the {@code filterable} property */
    public final BooleanProperty filterableProperty() {
        return filterable;
    }

    // ---- filter text
    // ---------------------------------------------------------------------------

    /** @return the text rows must contain in this column, empty for no filter */
    public final String getFilterText() {
        return filterText.get();
    }

    /**
     * @param value the text rows must contain in this column, {@code null} or blank
     *              for no filter
     */
    public final void setFilterText(String value) {
        filterText.set(value == null ? "" : value);
    }

    /**
     * @return the {@code filterText} property (also bound to the popup's text
     *         field)
     */
    public final StringProperty filterTextProperty() {
        return filterText;
    }

    // ---- movable
    // -------------------------------------------------------------------------------

    /**
     * @return whether the user may drag this column (only has an effect if the
     *         table is reorderable)
     */
    public final boolean isMovable() {
        return movable.get();
    }

    /** @param value {@code false} to pin this column in place */
    public final void setMovable(boolean value) {
        movable.set(value);
    }

    /** @return the {@code movable} property */
    public final BooleanProperty movableProperty() {
        return movable;
    }
}