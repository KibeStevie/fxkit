package dev.fxkit.core.components.megamenu;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Equal-width columns that fill left to right, top to bottom: Flowbite's {@code grid grid-cols-N}.
 * Children are placed in the order they were added, so there is nothing to configure per child.
 *
 * <p>JavaFX has no media queries, so {@code sm:grid-cols-2 md:grid-cols-3} is expressed with
 * {@link #minColumnWidthProperty() minColumnWidth}: when the grid is narrower than
 * {@code columns * minColumnWidth} it uses fewer columns (never fewer than one). The default
 * {@code 0} keeps exactly {@link #columnsProperty() columns}.
 *
 * <pre>{@code
 * FxMegaMenuGrid grid = new FxMegaMenuGrid(3, itemA, itemB, itemC, itemD);
 * grid.setMinColumnWidth(240);   // 3 columns when wide, 2 or 1 as it narrows
 * }</pre>
 */
public class FxMegaMenuGrid extends GridPane {

    private final IntegerProperty columns = new SimpleIntegerProperty(this, "columns", 3);
    private final DoubleProperty minColumnWidth = new SimpleDoubleProperty(this, "minColumnWidth", 0);
    private int appliedColumns = -1;

    public FxMegaMenuGrid() {
        this(3);
    }

    /**
     * @param columns  the number of columns when there is room (at least 1)
     * @param children the cells, in reading order
     */
    public FxMegaMenuGrid(int columns, Node... children) {
        getStyleClass().add("fxk-megamenu-grid");
        this.columns.set(Math.max(1, columns));

        getChildren().addListener((ListChangeListener<Node>) change -> relayout(true));
        this.columns.addListener(observable -> relayout(true));
        minColumnWidth.addListener(observable -> relayout(true));
        widthProperty().addListener(observable -> relayout(false));

        getChildren().addAll(children);
        relayout(true);
    }

    private int effectiveColumns() {
        int max = Math.max(1, getColumns());
        double min = getMinColumnWidth();
        double width = getWidth();
        if (min <= 0 || width <= 0) {
            return max; // not laid out yet, or responsive mode off
        }
        return Math.max(1, Math.min(max, (int) Math.floor(width / min)));
    }

    /** @param force rebuild even if the column count is unchanged (children or settings changed) */
    private void relayout(boolean force) {
        int count = effectiveColumns();
        if (!force && count == appliedColumns) {
            return;
        }
        appliedColumns = count;

        getColumnConstraints().clear();
        for (int i = 0; i < count; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100.0 / count);
            constraints.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(constraints);
        }
        int index = 0;
        for (Node child : getChildren()) {
            GridPane.setConstraints(child, index % count, index / count);
            GridPane.setValignment(child, VPos.TOP);
            index++;
        }
    }

    /**
     * @return the number of columns when there is room (Flowbite's {@code md:grid-cols-N})
     */
    public final IntegerProperty columnsProperty() {
        return columns;
    }

    public final int getColumns() {
        return columns.get();
    }

    public final void setColumns(int columns) {
        this.columns.set(Math.max(1, columns));
    }

    /**
     * @return the narrowest a column may get before the grid drops to fewer columns; {@code 0} turns
     *         this off
     */
    public final DoubleProperty minColumnWidthProperty() {
        return minColumnWidth;
    }

    public final double getMinColumnWidth() {
        return minColumnWidth.get();
    }

    public final void setMinColumnWidth(double minColumnWidth) {
        this.minColumnWidth.set(minColumnWidth);
    }
}
