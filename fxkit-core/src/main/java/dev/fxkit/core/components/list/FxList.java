package dev.fxkit.core.components.list;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;

/**
 * A list of {@link FxListItem}s: bulleted, numbered, with icons, unstyled, horizontal, nested or
 * divided. Port of Flowbite React's {@code <List>}.
 *
 * <pre>
 * FxList (VBox, .fxk-list)
 * ├─ FxListItem ...                    vertical (the default): items are direct children
 * └─ row (FlowPane, .fxk-list-row)     horizontal only: the single child, holds all items
 *     └─ FxListItem ...
 * </pre>
 *
 * <p>JavaFX has no list markers, so this class draws them: it tells each item whether to show a
 * bullet, a number (restarting at 1 for every list, so nested lists count on their own) or nothing,
 * and renumbers whenever items are added, removed, moved or the flags change. Add items through
 * {@link #getItems()}, not {@code getChildren()}.
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * &lt;List&gt;                  FxList                   (4px between items, muted text: space-y-1 text-gray-500)
 * ordered                  ordered                  numbers instead of bullets
 * unstyled                 unstyled                 no bullets or numbers (icons still show)
 * horizontal               horizontal               one centered, wrapping row, 16px apart; no markers
 * nested                   nested                   8px above, 20px indent (mt-2 ps-5)
 * &lt;ListItem icon=..&gt;       FxListItem.icon          or FxList.icon for every item at once
 * className="divide-y .."  divided                  1px rule between items, with padding either side
 * </pre>
 *
 * <p>Differences from the browser: markers hang to the left of the text (wrapped lines stay aligned
 * with the first line) instead of Flowbite's {@code list-inside}, and nested bullets are always
 * discs rather than switching to circles and squares.
 *
 * <p>No colors.css entry is needed: the list only uses semantic tokens, so it follows the theme.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * FxList list = FxList.of(
 *         "At least 10 characters (and up to 100 characters)",
 *         "At least one lowercase character",
 *         "Inclusion of at least one special character, e.g., ! @ # ?");
 * list.setOrdered(true);
 * }</pre>
 */
@DefaultProperty("items")
public class FxList extends VBox {

    private final BooleanProperty ordered = new SimpleBooleanProperty(this, "ordered", false);
    private final BooleanProperty unstyled = new SimpleBooleanProperty(this, "unstyled", false);
    private final BooleanProperty horizontal = new SimpleBooleanProperty(this, "horizontal", false);
    private final BooleanProperty nested = new SimpleBooleanProperty(this, "nested", false);
    private final BooleanProperty divided = new SimpleBooleanProperty(this, "divided", false);
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final ObservableList<FxListItem> items = FXCollections.observableArrayList();
    private final FlowPane row = new FlowPane();

    public FxList() {
        getStyleClass().add("fxk-list");
        row.getStyleClass().add("fxk-list-row");

        BooleanStyleClassSync.sync(this, "fxk-list-ordered", ordered);
        BooleanStyleClassSync.sync(this, "fxk-list-unstyled", unstyled);
        BooleanStyleClassSync.sync(this, "fxk-list-horizontal", horizontal);
        BooleanStyleClassSync.sync(this, "fxk-list-nested", nested);
        BooleanStyleClassSync.sync(this, "fxk-list-divided", divided);

        InvalidationListener refresh = o -> refresh();
        ordered.addListener(refresh);
        unstyled.addListener(refresh);
        horizontal.addListener(refresh);
        icon.addListener(refresh);
        items.addListener((javafx.collections.ListChangeListener<FxListItem>) change -> refresh());

        refresh();
    }

    /**
     * Convenience for the common case of plain text items.
     *
     * @param texts one item per entry
     * @return a new list containing those items
     */
    public static FxList of(String... texts) {
        FxList list = new FxList();
        for (String text : texts) {
            list.addItem(text);
        }
        return list;
    }

    /**
     * Appends a plain text item.
     *
     * @param text the item's text
     * @return the new item, so it can be given an icon or content
     */
    public FxListItem addItem(String text) {
        FxListItem item = new FxListItem(text);
        items.add(item);
        return item;
    }

    // ---- internals ------------------------------------------------------------------------

    /** Re-decides every item's marker and position, and (re)places the items for the current orientation. */
    private void refresh() {
        FxListItem.Marker marker;
        if (isHorizontal() || isUnstyled()) {
            marker = FxListItem.Marker.NONE;
        } else {
            marker = isOrdered() ? FxListItem.Marker.NUMBER : FxListItem.Marker.BULLET;
        }

        int count = items.size();
        for (int i = 0; i < count; i++) {
            items.get(i).configure(marker, i + 1, getIcon(), i == 0, i == count - 1);
        }

        // Clear both parents first so an item is never in two places while it is being moved.
        getChildren().clear();
        row.getChildren().clear();
        if (isHorizontal()) {
            row.getChildren().setAll(items);
            getChildren().add(row);
        } else {
            getChildren().setAll(items);
        }
    }

    // ---- properties -----------------------------------------------------------------------

    /** Numbers (1. 2. 3.) instead of bullets. Each list counts from 1, so a nested list restarts. */
    public final BooleanProperty orderedProperty() { return ordered; }
    public final boolean isOrdered() { return ordered.get(); }
    public final void setOrdered(boolean value) { ordered.set(value); }

    /** No bullets or numbers. Icons are still shown, as in Flowbite. */
    public final BooleanProperty unstyledProperty() { return unstyled; }
    public final boolean isUnstyled() { return unstyled.get(); }
    public final void setUnstyled(boolean value) { unstyled.set(value); }

    /** Lays the items out in one centered, wrapping row. Horizontal lists never show bullets or numbers. */
    public final BooleanProperty horizontalProperty() { return horizontal; }
    public final boolean isHorizontal() { return horizontal.get(); }
    public final void setHorizontal(boolean value) { horizontal.set(value); }

    /** Indents the list and adds space above it; set it on a list placed inside an {@link FxListItem}. */
    public final BooleanProperty nestedProperty() { return nested; }
    public final boolean isNested() { return nested.get(); }
    public final void setNested(boolean value) { nested.set(value); }

    /** Draws a 1px rule between items, with padding either side (Flowbite's {@code divide-y}). Vertical lists only. */
    public final BooleanProperty dividedProperty() { return divided; }
    public final boolean isDivided() { return divided.get(); }
    public final void setDivided(boolean value) { divided.set(value); }

    /** Icon for every item that does not set its own {@link FxListItem#iconProperty() icon}; {@code null} for none. */
    public final ObjectProperty<Ikon> iconProperty() { return icon; }
    public final Ikon getIcon() { return icon.get(); }
    public final void setIcon(Ikon value) { icon.set(value); }

    /** The items, in order. Modify this list, not {@code getChildren()}. */
    public final ObservableList<FxListItem> getItems() { return items; }
}
