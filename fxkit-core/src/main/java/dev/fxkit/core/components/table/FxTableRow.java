package dev.fxkit.core.components.table;

import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;

/**
 * One row of an {@link FxTableSection} (Flowbite's {@code <TableRow>}).
 *
 * <p>
 * A row does not decide its own column widths: the owning {@link FxTable}
 * measures every row of
 * the head and the body, and hands the shared widths down, so the columns line
 * up. That is also why
 * the row, not the individual cell, carries the background: striping and the
 * hover highlight
 * paint the whole row.
 *
 * <p>
 * The row tags its last cell with {@code fxk-table-cell-last} (JavaFX CSS has
 * no
 * {@code :last-child}), which the column dividers use to skip the final edge.
 *
 * <pre>{@code
 * FxTableRow row = FxTableRow.of("Magic Mouse 2", "Black", "Accessories", "$99");
 * row.getCells().get(0).setStrong(true);
 * }</pre>
 */
public class FxTableRow extends Region {

    private static final String LAST_CELL = "fxk-table-cell-last";

    private final ObservableList<FxTableCell> cells = FXCollections.observableArrayList();
    private double[] columnWidths = new double[0];
    private boolean filteredOut;

    /** Creates an empty row. */
    public FxTableRow() {
        getStyleClass().add("fxk-table-row");
        cells.addListener((ListChangeListener<FxTableCell>) change -> {
            while (change.next()) {
                for (FxTableCell removed : change.getRemoved()) {
                    removed.getStyleClass().remove(LAST_CELL);
                }
            }
            syncChildren();
            if (!cells.isEmpty()) {
                FxTableCell last = cells.get(cells.size() - 1);
                cells.forEach(cell -> cell.getStyleClass().remove(LAST_CELL));
                last.getStyleClass().add(LAST_CELL);
            }
            requestLayout();
        });
    }

    /**
     * Creates a row with the given cells.
     *
     * @param cells the cells, left to right
     */
    public FxTableRow(FxTableCell... cells) {
        this();
        this.cells.addAll(cells);
    }

    /**
     * Creates a row from loose items: each one becomes a cell through
     * {@link FxTableCell#of(Object)}.
     *
     * @param items strings, nodes or cells, left to right
     * @return the new row
     */
    public static FxTableRow of(Object... items) {
        FxTableRow row = new FxTableRow();
        for (Object item : items) {
            row.cells.add(FxTableCell.of(item));
        }
        return row;
    }

    /**
     * @return the cells of this row, left to right; modify the list to add or
     *         remove cells
     */
    public final ObservableList<FxTableCell> getCells() {
        return cells;
    }

    /** @return whether a column filter currently hides this row */
    public final boolean isFilteredOut() {
        return filteredOut;
    }

    // ---- driven by FxTable / FxTableSection
    // ------------------------------------------------------

    void setFilteredOut(boolean value) {
        if (filteredOut != value) {
            filteredOut = value;
            setVisible(!value);
            setManaged(!value);
        }
    }

    /**
     * Restores the child order to the cell order (dragging brings a cell to the
     * front).
     */
    void syncChildren() {
        getChildren().setAll(cells);
    }

    int cellCount() {
        return cells.size();
    }

    /**
     * Raises {@code widths[i]} to this row's preferred width for column i, where it
     * is larger.
     */
    void accumulatePrefWidths(double[] widths) {
        for (int i = 0; i < cells.size() && i < widths.length; i++) {
            widths[i] = Math.max(widths[i], cells.get(i).prefWidth(-1));
        }
    }

    /** The height this row needs when its columns have the given widths. */
    double heightFor(double[] widths) {
        double tallest = 0;
        for (int i = 0; i < cells.size(); i++) {
            double width = i < widths.length ? widths[i] : -1;
            tallest = Math.max(tallest, cells.get(i).prefHeight(width));
        }
        Insets insets = getInsets();
        return snapSizeY(Math.ceil(tallest + insets.getTop() + insets.getBottom()));
    }

    void setColumnWidths(double[] widths) {
        if (!Arrays.equals(columnWidths, widths)) {
            columnWidths = widths.clone();
            requestLayout();
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        double[] widths = new double[cells.size()];
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
        double x = insets.getLeft();
        double y = insets.getTop();
        double height = getHeight() - insets.getTop() - insets.getBottom();
        for (int i = 0; i < cells.size(); i++) {
            double width = i < columnWidths.length ? columnWidths[i] : 0;
            cells.get(i).resizeRelocate(x, y, width, height);
            x += width;
        }
    }
}