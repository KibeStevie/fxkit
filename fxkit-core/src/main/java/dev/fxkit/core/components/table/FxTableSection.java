package dev.fxkit.core.components.table;

import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;

/**
 * A vertical stack of {@link FxTableRow}s: the common base of
 * {@link FxTableHead} and
 * {@link FxTableBody}.
 *
 * <p>
 * JavaFX CSS has no {@code :first-child}, {@code :last-child} or
 * {@code :nth-child}, so the section
 * tags each <em>visible</em> row with {@code fxk-table-row-first},
 * {@code fxk-table-row-last} and
 * {@code fxk-table-row-odd} / {@code fxk-table-row-even} (counting from 1, like
 * CSS), and keeps the
 * tags right as rows come and go or a column filter hides them. The divider
 * lines, the rounded last
 * row and the striping in {@code components.css} are all built on those tags.
 *
 * <p>
 * Column widths are measured over <em>all</em> rows, including filtered-out
 * ones, so the columns
 * do not jump around while the user types a filter.
 */
public abstract class FxTableSection extends Region {

    private static final String[] ROW_TAGS = {
            "fxk-table-row-first", "fxk-table-row-last", "fxk-table-row-odd", "fxk-table-row-even"
    };

    private final ObservableList<FxTableRow> rows = FXCollections.observableArrayList();
    private double[] columnWidths = new double[0];

    FxTableSection(String styleClass) {
        getStyleClass().add(styleClass);
        rows.addListener((ListChangeListener<FxTableRow>) change -> {
            getChildren().setAll(rows);
            refresh();
        });
    }

    /**
     * @return the rows of this section, top to bottom; modify the list to add or
     *         remove rows
     */
    public final ObservableList<FxTableRow> getRows() {
        return rows;
    }

    /**
     * Re-tags the visible rows and lays the section out again (after rows came,
     * went or were filtered).
     */
    void refresh() {
        for (FxTableRow row : rows) {
            row.getStyleClass().removeAll(ROW_TAGS);
        }
        List<FxTableRow> visible = visibleRows();
        for (int i = 0; i < visible.size(); i++) {
            var classes = visible.get(i).getStyleClass();
            if (i == 0) {
                classes.add("fxk-table-row-first");
            }
            if (i == visible.size() - 1) {
                classes.add("fxk-table-row-last");
            }
            classes.add(i % 2 == 0 ? "fxk-table-row-odd" : "fxk-table-row-even");
        }
        requestLayout();
    }

    private List<FxTableRow> visibleRows() {
        return rows.stream().filter(row -> !row.isFilteredOut()).toList();
    }

    // ---- sizing, driven by FxTable
    // -------------------------------------------------------------

    int columnCount() {
        return rows.stream().mapToInt(FxTableRow::cellCount).max().orElse(0);
    }

    void accumulatePrefWidths(double[] widths) {
        for (FxTableRow row : rows) {
            row.accumulatePrefWidths(widths);
        }
    }

    double heightFor(double[] widths) {
        Insets insets = getInsets();
        double height = insets.getTop() + insets.getBottom();
        for (FxTableRow row : visibleRows()) {
            height += row.heightFor(widths);
        }
        return height;
    }

    void setColumnWidths(double[] widths) {
        if (!Arrays.equals(columnWidths, widths)) {
            columnWidths = widths.clone();
            requestLayout();
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        double[] widths = new double[columnCount()];
        accumulatePrefWidths(widths);
        Insets insets = getInsets();
        return Arrays.stream(widths).sum() + insets.getLeft() + insets.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        return heightFor(columnWidths);
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        double width = getWidth() - insets.getLeft() - insets.getRight();
        double y = insets.getTop();
        for (FxTableRow row : visibleRows()) {
            double height = row.heightFor(columnWidths);
            row.setColumnWidths(columnWidths);
            row.resizeRelocate(insets.getLeft(), y, width, height);
            y += height;
        }
    }
}