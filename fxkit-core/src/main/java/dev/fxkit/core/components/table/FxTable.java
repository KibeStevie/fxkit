package dev.fxkit.core.components.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * A static table of rows and cells, ported from Flowbite React's {@code 
 * <Table>
 * }.
 *
 * <p>
 * It is built like the Flowbite original: an {@link FxTableHead} and an
 * {@link FxTableBody},
 * holding {@link FxTableRow}s, holding {@link FxTableCell}s. Columns are sized
 * the way an HTML table
 * sizes them: each column is as wide as its widest cell, and when the table is
 * wider than that, the
 * extra width is shared between the columns in proportion. The table never gets
 * narrower than its
 * content, so put it in a {@code ScrollPane} for Flowbite's
 * {@code overflow-x-auto}.
 *
 * <p>
 * This is a layout of ordinary nodes, not a virtualized data grid. For large or
 * sortable data,
 * use JavaFX's {@code TableView}.
 *
 * <h2>Options</h2>
 * 
 * <pre>{@code
 * FxTable table = new FxTable();
 * table.setStriped(true); // alternate row background
 * table.setHoverable(true); // highlight the row under the mouse
 * table.setColumnDividers(true); // 1px line between columns
 * table.setReorderable(true); // drag head cells to rearrange columns
 * table.setShadow(FxTable.Shadow.MD); // elevation
 * table.setHoverBackground(Color.web("#e0e7ff")); // your own colors (null = theme default)
 *
 * table.setHeaders("Product name", "Color", "Category", "Price", "");
 * table.getHeadCells().get(0).setFilterable(true); // funnel button + filter popup
 * }</pre>
 *
 * <h2>Colors</h2>
 * The five color properties ({@code headBackground}, {@code bodyBackground},
 * {@code stripedBackground},
 * {@code hoverBackground}, {@code dividerColor}) are {@code null} by default,
 * which means "use the
 * theme": they follow light/dark. Setting one pins that color in both themes.
 * They work by setting
 * inline looked-up colors on the table (see {@code components.css}), so they
 * leave the rest of the
 * table's inline style alone.
 *
 * <p>
 * Needs {@code components.css} to be styled. {@code ThemeManager.apply}
 * installs it.
 */
public class FxTable extends Region {

    /** Elevation of the table, from the shadow tokens in {@code tokens.css}. */
    public enum Shadow {
        NONE, SM, MD, LG
    }

    private static final double DRAG_THRESHOLD = 4;

    private static final Set<String> COLOR_TOKENS = Set.of(
            "-fxk-table-head-bg", "-fxk-table-body-bg", "-fxk-table-stripe-bg",
            "-fxk-table-hover-bg", "-fxk-table-divider");

    private final FxTableHead head = new FxTableHead();
    private final FxTableBody body = new FxTableBody();
    private final Region dropIndicator = new Region();

    private final BooleanProperty striped = new SimpleBooleanProperty(this, "striped", false);
    private final BooleanProperty hoverable = new SimpleBooleanProperty(this, "hoverable", false);
    private final BooleanProperty columnDividers = new SimpleBooleanProperty(this, "columnDividers", false);
    private final BooleanProperty reorderable = new SimpleBooleanProperty(this, "reorderable", false);
    private final ObjectProperty<Shadow> shadow = new SimpleObjectProperty<>(this, "shadow", Shadow.NONE);

    private final ObjectProperty<Color> headBackground = colorProperty("headBackground");
    private final ObjectProperty<Color> bodyBackground = colorProperty("bodyBackground");
    private final ObjectProperty<Color> stripedBackground = colorProperty("stripedBackground");
    private final ObjectProperty<Color> hoverBackground = colorProperty("hoverBackground");
    private final ObjectProperty<Color> dividerColor = colorProperty("dividerColor");

    private final ObjectProperty<BiConsumer<Integer, Integer>> onColumnMoved = new SimpleObjectProperty<>(this,
            "onColumnMoved");

    private double[] laidOutWidths = new double[0];

    // drag state
    private FxTableHeadCell dragCell;
    private int dragFrom = -1;
    private int dragTarget = -1;
    private double dragStartSceneX;
    private boolean dragging;

    /** Creates an empty table. */
    public FxTable() {
        getStyleClass().add("fxk-table");

        dropIndicator.getStyleClass().add("fxk-table-drop-indicator");
        dropIndicator.setManaged(false);
        dropIndicator.setMouseTransparent(true);
        dropIndicator.setVisible(false);
        getChildren().addAll(head, body, dropIndicator);

        BooleanStyleClassSync.sync(this, "fxk-table-striped", striped);
        BooleanStyleClassSync.sync(this, "fxk-table-hoverable", hoverable);
        BooleanStyleClassSync.sync(this, "fxk-table-column-dividers", columnDividers);
        BooleanStyleClassSync.sync(this, "fxk-table-reorderable", reorderable);
        EnumStyleClassSync.sync(this, "fxk-table-shadow-", shadow);

        // rows added later must respect filters that are already active
        body.getRows().addListener((javafx.collections.ListChangeListener<FxTableRow>) change -> applyFilters());
        head.getRows().addListener((javafx.collections.ListChangeListener<FxTableRow>) change -> applyFilters());

        head.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onHeadPressed);
        head.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onHeadDragged);
        head.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onHeadReleased);
    }

    // ---- structure
    // ---------------------------------------------------------------------------

    /** @return the head section */
    public final FxTableHead getHead() {
        return head;
    }

    /** @return the body section */
    public final FxTableBody getBody() {
        return body;
    }

    /**
     * @return the {@link FxTableHeadCell}s of the first head row, left to right (a
     *         snapshot; empty if
     *         the head has no row)
     */
    public List<FxTableHeadCell> getHeadCells() {
        if (head.getRows().isEmpty()) {
            return List.of();
        }
        return head.getRows().get(0).getCells().stream()
                .filter(FxTableHeadCell.class::isInstance)
                .map(FxTableHeadCell.class::cast)
                .toList();
    }

    /**
     * Replaces the head with a single row of {@link FxTableHeadCell}s. An empty
     * string or
     * {@code null} gives an empty head cell (Flowbite's {@code sr-only} column).
     *
     * @param headers the column titles, left to right
     */
    public void setHeaders(String... headers) {
        FxTableRow row = new FxTableRow();
        for (String header : headers) {
            row.getCells().add(new FxTableHeadCell(header));
        }
        head.getRows().setAll(row);
    }

    /**
     * Appends a row to the body. Each item becomes a cell through
     * {@link FxTableCell#of(Object)}.
     *
     * @param items strings, nodes or cells, left to right
     * @return the new row, for further tweaks
     */
    public FxTableRow addRow(Object... items) {
        FxTableRow row = FxTableRow.of(items);
        body.getRows().add(row);
        return row;
    }

    // ---- helpers for common cell content
    // -----------------------------------------------------

    /**
     * Creates a checkbox styled for table cells. Put it in a compact cell
     * ({@link FxTableCell#setCompact(boolean)}) to get Flowbite's {@code p-4}
     * checkbox column.
     *
     * @return a new checkbox
     */
    public static CheckBox checkbox() {
        CheckBox box = new CheckBox();
        box.getStyleClass().add("fxk-table-checkbox");
        return box;
    }

    /**
     * Creates a link styled for table cells (Flowbite's
     * {@code font-medium text-primary-600
     * hover:underline}).
     *
     * @param text     the link text
     * @param onAction what to do when it is clicked, may be {@code null}
     * @return a new hyperlink
     */
    public static Hyperlink link(String text, Runnable onAction) {
        Hyperlink link = new Hyperlink(text);
        link.getStyleClass().add("fxk-table-link");
        if (onAction != null) {
            link.setOnAction(event -> onAction.run());
        }
        return link;
    }

    // ---- filters
    // -----------------------------------------------------------------------------

    /** Clears the filter text of every column. */
    public void clearFilters() {
        getHeadCells().forEach(cell -> cell.setFilterText(""));
    }

    /**
     * Shows or hides each body row according to the filter text of the head cells.
     * Called
     * automatically when a filter, a row or a head cell changes.
     */
    void applyFilters() {
        List<FxTableCell> headCells = head.getRows().isEmpty()
                ? List.of()
                : head.getRows().get(0).getCells();
        for (FxTableRow row : body.getRows()) {
            boolean show = true;
            for (int i = 0; i < headCells.size() && show; i++) {
                if (headCells.get(i) instanceof FxTableHeadCell headCell && headCell.isFilterable()) {
                    String query = headCell.getFilterText();
                    if (query != null && !query.isBlank()) {
                        String value = i < row.getCells().size() ? row.getCells().get(i).textValue() : "";
                        show = value.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
                    }
                }
            }
            row.setFilteredOut(!show);
        }
        body.refresh();
    }

    /** The table a node sits in, or {@code null}. */
    static FxTable ownerOf(Node node) {
        for (Node n = node; n != null; n = n.getParent()) {
            if (n instanceof FxTable table) {
                return table;
            }
        }
        return null;
    }

    // ---- moving columns
    // ----------------------------------------------------------------------

    /**
     * Moves a column, in the head and in every body row, so the cells keep lining
     * up. Filters and
     * the {@code movable} flag travel with the column, because they live on its
     * head cell.
     *
     * @param from the current column index
     * @param to   the new column index
     * @throws IllegalArgumentException if an index is outside
     *                                  {@code 0 .. columns - 1}
     */
    public void moveColumn(int from, int to) {
        int columns = columnCount();
        if (from < 0 || from >= columns || to < 0 || to >= columns) {
            throw new IllegalArgumentException("column out of range 0.." + (columns - 1) + ": " + from + " -> " + to);
        }
        if (from == to) {
            return;
        }
        List<FxTableRow> all = new ArrayList<>(head.getRows());
        all.addAll(body.getRows());
        for (FxTableRow row : all) {
            if (row.getCells().size() > Math.max(from, to)) {
                List<FxTableCell> reordered = new ArrayList<>(row.getCells());
                reordered.add(to, reordered.remove(from));
                row.getCells().setAll(reordered);
            }
        }
        BiConsumer<Integer, Integer> handler = onColumnMoved.get();
        if (handler != null) {
            handler.accept(from, to);
        }
    }

    private int columnCount() {
        return Math.max(head.columnCount(), body.columnCount());
    }

    private FxTableHeadCell headCellAt(Object target) {
        for (Node n = target instanceof Node node ? node : null; n != null && n != head; n = n.getParent()) {
            if (n.getStyleClass().contains("fxk-table-filter-button")) {
                return null; // a press on the funnel is a click, not the start of a drag
            }
            if (n instanceof FxTableHeadCell cell) {
                return cell;
            }
        }
        return null;
    }

    private boolean canDropAt(int index) {
        List<FxTableCell> headCells = head.getRows().get(0).getCells();
        return index >= 0 && index < headCells.size()
                && headCells.get(index) instanceof FxTableHeadCell cell && cell.isMovable();
    }

    private static double sum(double[] widths, int from, int to) {
        double total = 0;
        for (int i = from; i < to && i < widths.length; i++) {
            total += widths[i];
        }
        return total;
    }

    private void onHeadPressed(MouseEvent event) {
        dragCell = null;
        dragging = false;
        if (!isReorderable() || event.getButton() != MouseButton.PRIMARY || head.getRows().isEmpty()) {
            return;
        }
        FxTableHeadCell cell = headCellAt(event.getTarget());
        if (cell == null || !cell.isMovable() || !(cell.getParent() instanceof FxTableRow row)) {
            return;
        }
        int index = row.getCells().indexOf(cell);
        if (index < 0) {
            return;
        }
        dragCell = cell;
        dragFrom = index;
        dragTarget = index;
        dragStartSceneX = event.getSceneX();
    }

    private void onHeadDragged(MouseEvent event) {
        if (dragCell == null) {
            return;
        }
        double dx = event.getSceneX() - dragStartSceneX;
        if (!dragging) {
            if (Math.abs(dx) < DRAG_THRESHOLD) {
                return;
            }
            dragging = true;
            dragCell.getStyleClass().add("fxk-table-cell-dragging");
            dragCell.toFront();
        }
        double[] widths = laidOutWidths;
        if (dragFrom >= widths.length) {
            return;
        }
        double left = sum(widths, 0, dragFrom);
        double total = sum(widths, 0, widths.length);
        dx = Math.max(-left, Math.min(dx, total - left - widths[dragFrom]));
        dragCell.setTranslateX(dx);

        // the column the dragged cell's center is over
        double center = left + widths[dragFrom] / 2 + dx;
        int target = widths.length - 1;
        double edge = 0;
        for (int i = 0; i < widths.length; i++) {
            edge += widths[i];
            if (center < edge) {
                target = i;
                break;
            }
        }
        dragTarget = canDropAt(target) ? target : dragFrom;

        if (dragTarget == dragFrom) {
            dropIndicator.setVisible(false);
        } else {
            double x = dragTarget > dragFrom ? sum(widths, 0, dragTarget + 1) : sum(widths, 0, dragTarget);
            dropIndicator.setVisible(true);
            dropIndicator.resizeRelocate(head.getLayoutX() + x - 1, head.getLayoutY(), 2, head.getHeight());
        }
        event.consume();
    }

    private void onHeadReleased(MouseEvent event) {
        if (dragCell == null) {
            return;
        }
        FxTableHeadCell cell = dragCell;
        boolean wasDragging = dragging;
        int from = dragFrom;
        int to = dragTarget;
        dragCell = null;
        dragging = false;
        dragFrom = -1;
        dragTarget = -1;
        dropIndicator.setVisible(false);
        if (!wasDragging) {
            return;
        }
        cell.setTranslateX(0);
        cell.getStyleClass().remove("fxk-table-cell-dragging");
        if (to >= 0 && to != from) {
            moveColumn(from, to);
        } else if (cell.getParent() instanceof FxTableRow row) {
            row.syncChildren(); // undo toFront()
        }
        event.consume();
    }

    // ---- colors
    // ------------------------------------------------------------------------------

    private ObjectProperty<Color> colorProperty(String name) {
        ObjectProperty<Color> property = new SimpleObjectProperty<>(this, name);
        property.addListener(observable -> updateColorStyle());
        return property;
    }

    private void updateColorStyle() {
        StringBuilder ours = new StringBuilder();
        appendColor(ours, "-fxk-table-head-bg", headBackground.get());
        appendColor(ours, "-fxk-table-body-bg", bodyBackground.get());
        appendColor(ours, "-fxk-table-stripe-bg", stripedBackground.get());
        appendColor(ours, "-fxk-table-hover-bg", hoverBackground.get());
        appendColor(ours, "-fxk-table-divider", dividerColor.get());

        String existing = getStyle() == null ? "" : getStyle();
        String kept = Arrays.stream(existing.split(";"))
                .map(String::trim)
                .filter(declaration -> !declaration.isEmpty())
                .filter(declaration -> !COLOR_TOKENS.contains(declaration.split(":", 2)[0].trim()))
                .map(declaration -> declaration + "; ")
                .collect(Collectors.joining());
        setStyle(kept + ours);
    }

    private static void appendColor(StringBuilder out, String token, Color color) {
        if (color != null) {
            out.append(token).append(": ").append(String.format(Locale.ROOT, "rgba(%d, %d, %d, %.3f)",
                    Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255),
                    Math.round(color.getBlue() * 255), color.getOpacity())).append("; ");
        }
    }

    /** @return the head background, {@code null} for the theme's */
    public final Color getHeadBackground() {
        return headBackground.get();
    }

    /** @param value the head background, {@code null} for the theme's */
    public final void setHeadBackground(Color value) {
        headBackground.set(value);
    }

    /** @return the {@code headBackground} property */
    public final ObjectProperty<Color> headBackgroundProperty() {
        return headBackground;
    }

    /** @return the body row background, {@code null} for the theme's */
    public final Color getBodyBackground() {
        return bodyBackground.get();
    }

    /** @param value the body row background, {@code null} for the theme's */
    public final void setBodyBackground(Color value) {
        bodyBackground.set(value);
    }

    /** @return the {@code bodyBackground} property */
    public final ObjectProperty<Color> bodyBackgroundProperty() {
        return bodyBackground;
    }

    /**
     * @return the background of every second row when {@code striped}, {@code null}
     *         for the theme's
     */
    public final Color getStripedBackground() {
        return stripedBackground.get();
    }

    /**
     * @param value the background of every second row when {@code striped},
     *              {@code null} for the theme's
     */
    public final void setStripedBackground(Color value) {
        stripedBackground.set(value);
    }

    /** @return the {@code stripedBackground} property */
    public final ObjectProperty<Color> stripedBackgroundProperty() {
        return stripedBackground;
    }

    /**
     * @return the row background under the mouse when {@code hoverable},
     *         {@code null} for the theme's
     */
    public final Color getHoverBackground() {
        return hoverBackground.get();
    }

    /**
     * @param value the row background under the mouse when {@code hoverable},
     *              {@code null} for the theme's
     */
    public final void setHoverBackground(Color value) {
        hoverBackground.set(value);
    }

    /** @return the {@code hoverBackground} property */
    public final ObjectProperty<Color> hoverBackgroundProperty() {
        return hoverBackground;
    }

    /**
     * @return the color of row and column dividers, {@code null} for the theme's
     */
    public final Color getDividerColor() {
        return dividerColor.get();
    }

    /**
     * @param value the color of row and column dividers, {@code null} for the
     *              theme's
     */
    public final void setDividerColor(Color value) {
        dividerColor.set(value);
    }

    /** @return the {@code dividerColor} property */
    public final ObjectProperty<Color> dividerColorProperty() {
        return dividerColor;
    }

    // ---- flags
    // -------------------------------------------------------------------------------

    /** @return whether every second body row has an alternate background */
    public final boolean isStriped() {
        return striped.get();
    }

    /**
     * @param value {@code true} to alternate the background of every second body
     *              row
     */
    public final void setStriped(boolean value) {
        striped.set(value);
    }

    /** @return the {@code striped} property */
    public final BooleanProperty stripedProperty() {
        return striped;
    }

    /** @return whether a body row is highlighted under the mouse */
    public final boolean isHoverable() {
        return hoverable.get();
    }

    /** @param value {@code true} to highlight the body row under the mouse */
    public final void setHoverable(boolean value) {
        hoverable.set(value);
    }

    /** @return the {@code hoverable} property */
    public final BooleanProperty hoverableProperty() {
        return hoverable;
    }

    /** @return whether a 1px line separates the columns */
    public final boolean isColumnDividers() {
        return columnDividers.get();
    }

    /**
     * @param value {@code true} to draw a 1px line between columns, in the head and
     *              the body
     */
    public final void setColumnDividers(boolean value) {
        columnDividers.set(value);
    }

    /** @return the {@code columnDividers} property */
    public final BooleanProperty columnDividersProperty() {
        return columnDividers;
    }

    /** @return whether the user can rearrange columns by dragging head cells */
    public final boolean isReorderable() {
        return reorderable.get();
    }

    /**
     * @param value {@code true} to let the user drag head cells; pin single columns
     *              with {@code setMovable(false)}
     */
    public final void setReorderable(boolean value) {
        reorderable.set(value);
    }

    /** @return the {@code reorderable} property */
    public final BooleanProperty reorderableProperty() {
        return reorderable;
    }

    /** @return the table's elevation */
    public final Shadow getShadow() {
        return shadow.get();
    }

    /**
     * @param value the table's elevation; {@code null} counts as
     *              {@link Shadow#NONE}
     */
    public final void setShadow(Shadow value) {
        shadow.set(value == null ? Shadow.NONE : value);
    }

    /** @return the {@code shadow} property */
    public final ObjectProperty<Shadow> shadowProperty() {
        return shadow;
    }

    /**
     * @return the handler called with (from, to) after the user (or
     *         {@link #moveColumn}) moved a column
     */
    public final BiConsumer<Integer, Integer> getOnColumnMoved() {
        return onColumnMoved.get();
    }

    /**
     * @param handler called with (from, to) after a column moved, may be
     *                {@code null}
     */
    public final void setOnColumnMoved(BiConsumer<Integer, Integer> handler) {
        onColumnMoved.set(handler);
    }

    /** @return the {@code onColumnMoved} property */
    public final ObjectProperty<BiConsumer<Integer, Integer>> onColumnMovedProperty() {
        return onColumnMoved;
    }

    // ---- layout
    // ------------------------------------------------------------------------------

    /**
     * Column widths for a table that is {@code width} wide (or as narrow as
     * possible when
     * {@code width} is not positive): the widest cell of each column, scaled up to
     * fill the width.
     */
    private double[] resolveWidths(double width) {
        double[] widths = new double[columnCount()];
        head.accumulatePrefWidths(widths);
        body.accumulatePrefWidths(widths);
        double total = Arrays.stream(widths).sum();
        if (total > 0 && width > total) {
            double factor = width / total;
            for (int i = 0; i < widths.length; i++) {
                widths[i] *= factor;
            }
        }
        return widths;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets insets = getInsets();
        return Arrays.stream(resolveWidths(-1)).sum() + insets.getLeft() + insets.getRight();
    }

    @Override
    protected double computeMinWidth(double height) {
        return computePrefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets insets = getInsets();
        double[] widths = resolveWidths(width - insets.getLeft() - insets.getRight());
        return insets.getTop() + head.heightFor(widths) + body.heightFor(widths) + insets.getBottom();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        double width = getWidth() - insets.getLeft() - insets.getRight();
        double[] widths = resolveWidths(width);
        laidOutWidths = widths;
        double headHeight = head.heightFor(widths);
        double bodyHeight = body.heightFor(widths);

        head.setColumnWidths(widths);
        body.setColumnWidths(widths);
        head.resizeRelocate(insets.getLeft(), insets.getTop(), width, headHeight);
        body.resizeRelocate(insets.getLeft(), insets.getTop() + headHeight, width, bodyHeight);
    }
}