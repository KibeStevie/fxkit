package dev.fxkit.core.components.table;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.StackPane;

/**
 * One cell of an {@link FxTableRow} (Flowbite's {@code <TableCell>}).
 *
 * <p>
 * A cell holds a single piece of content: a text (shown in a {@link Label}) or
 * any node, such as
 * a checkbox or a link. The content is centered vertically and aligned to the
 * left.
 *
 * <ul>
 * <li>{@link #strongProperty() strong}: darker text, medium weight. Flowbite's
 * {@code whitespace-nowrap font-medium text-gray-900}, usually set on the first
 * cell of a row.</li>
 * <li>{@link #compactProperty() compact}: 16px padding on every side instead of
 * 24 x 16.
 * Flowbite's {@code p-4}, used for the narrow checkbox column.</li>
 * </ul>
 *
 * <pre>{@code
 * FxTableCell name = new FxTableCell("Apple MacBook Pro 17\"");
 * name.setStrong(true);
 * }</pre>
 */
public class FxTableCell extends StackPane {

    private final BooleanProperty strong = new SimpleBooleanProperty(this, "strong", false);
    private final BooleanProperty compact = new SimpleBooleanProperty(this, "compact", false);
    private String filterValue;

    /** Creates an empty cell. */
    public FxTableCell() {
        getStyleClass().add("fxk-table-cell");
        setAlignment(Pos.CENTER_LEFT);
        BooleanStyleClassSync.sync(this, "fxk-table-cell-strong", strong);
        BooleanStyleClassSync.sync(this, "fxk-table-cell-compact", compact);
    }

    /**
     * Creates a cell showing {@code text}.
     *
     * @param text the text, {@code null} for an empty cell
     */
    public FxTableCell(String text) {
        this();
        if (text != null && !text.isEmpty()) {
            getChildren().add(new Label(text));
        }
    }

    /**
     * Creates a cell showing {@code content}.
     *
     * @param content the node to show, {@code null} for an empty cell
     */
    public FxTableCell(Node content) {
        this();
        if (content != null) {
            getChildren().add(content);
        }
    }

    /**
     * Converts whatever a caller passes for a cell into a cell: an
     * {@code FxTableCell} is returned
     * as is, a {@link Node} is wrapped, anything else is shown as its
     * {@code toString()}.
     *
     * @param item a cell, a node, a string or any other object; {@code null} gives
     *             an empty cell
     * @return the cell
     */
    public static FxTableCell of(Object item) {
        if (item instanceof FxTableCell cell) {
            return cell;
        }
        if (item instanceof Node node) {
            return new FxTableCell(node);
        }
        return new FxTableCell(item == null ? null : item.toString());
    }

    // ---- strong
    // ------------------------------------------------------------------------------

    /** @return whether the text is emphasized (darker, medium weight) */
    public final boolean isStrong() {
        return strong.get();
    }

    /** @param value {@code true} to emphasize the text */
    public final void setStrong(boolean value) {
        strong.set(value);
    }

    /** @return the {@code strong} property */
    public final BooleanProperty strongProperty() {
        return strong;
    }

    // ---- compact
    // -----------------------------------------------------------------------------

    /** @return whether the cell uses the smaller, uniform padding */
    public final boolean isCompact() {
        return compact.get();
    }

    /** @param value {@code true} for 16px padding on every side */
    public final void setCompact(boolean value) {
        compact.set(value);
    }

    /** @return the {@code compact} property */
    public final BooleanProperty compactProperty() {
        return compact;
    }

    // ---- text used by column filters
    // -----------------------------------------------------------

    /**
     * @return the text a column filter matches against: the value set with
     *         {@link #setFilterValue(String)} if any, otherwise the text of the
     *         first label, link or
     *         button found in the cell's content, otherwise an empty string
     */
    public String textValue() {
        return filterValue != null ? filterValue : findText(this);
    }

    /**
     * Overrides the text a column filter sees for this cell, for cells whose
     * content is not plain
     * text (for example a status badge).
     *
     * @param value the text to match against, {@code null} to go back to the
     *              automatic text
     */
    public final void setFilterValue(String value) {
        this.filterValue = value;
    }

    /**
     * @return the explicit filter text, or {@code null} if the cell's own text is
     *         used
     */
    public final String getFilterValue() {
        return filterValue;
    }

    private static String findText(Node node) {
        if (node instanceof Labeled labeled && labeled.getText() != null && !labeled.getText().isEmpty()) {
            return labeled.getText();
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                String text = findText(child);
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return "";
    }
}