package dev.fxkit.core.components.listgroup;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

/**
 * A vertical menu of {@link FxListGroupItem}s inside one rounded, bordered box: Flowbite's
 * {@code ListGroup}.
 *
 * <pre>{@code
 * FxListGroup group = new FxListGroup(
 *         new FxListGroupItem("Profile"),
 *         new FxListGroupItem("Settings"),
 *         new FxListGroupItem("Messages"));
 * group.setPrefWidth(192); // Flowbite example uses w-48
 * }</pre>
 *
 * <p>Add and remove rows through {@link #getItems()}, not {@link #getChildren()}: the group keeps
 * its children in step with that list and tags the first and last row with
 * {@code fxk-list-group-item-first} / {@code -last}. JavaFX CSS has no {@code :first-child}, so
 * those classes are how {@code components.css} rounds the outer corners and drops the last row's
 * divider (Flowbite's {@code first:rounded-t-lg last:rounded-b-lg last:border-b-0}). The tags
 * update live as items are added, removed, replaced or reordered, and are removed from an item when
 * it leaves the group.
 *
 * <p>The group does not manage selection: Flowbite's list group does not either. Each item has its
 * own {@link FxListGroupItem#activeProperty() active} flag, which you drive from your own handlers.
 *
 * <p>An item can be in only one group (like any node in one parent). To move it, remove it from the
 * first group's {@link #getItems()} before adding it to the second; adding the same item twice to
 * one group throws {@link IllegalArgumentException}.
 */
public class FxListGroup extends VBox {

    private static final String ITEM_FIRST = "fxk-list-group-item-first";
    private static final String ITEM_LAST = "fxk-list-group-item-last";

    private final ObservableList<FxListGroupItem> items = FXCollections.observableArrayList();

    /** Creates an empty list group. */
    public FxListGroup() {
        getStyleClass().add("fxk-list-group");
        items.addListener((ListChangeListener<FxListGroupItem>) this::onItemsChanged);
    }

    /**
     * Creates a list group holding {@code items}, in order.
     *
     * @param items the rows to show; must not contain {@code null}
     */
    public FxListGroup(FxListGroupItem... items) {
        this();
        getItems().addAll(items);
    }

    /**
     * The rows, top to bottom. Modify this list to add, remove, replace or reorder rows.
     *
     * @return the live, modifiable list of items
     */
    public final ObservableList<FxListGroupItem> getItems() {
        return items;
    }

    private void onItemsChanged(ListChangeListener.Change<? extends FxListGroupItem> change) {
        // Apply the change to the children incrementally rather than setAll(items): re-adding every
        // child would pull a focused item out of the scene and make it lose keyboard focus.
        while (change.next()) {
            if (change.wasPermutated()) {
                getChildren().setAll(items);
            } else {
                if (change.wasRemoved()) {
                    for (FxListGroupItem removed : change.getRemoved()) {
                        // Skip items that were re-added (replaced by themselves) in the same change.
                        if (!items.contains(removed)) {
                            clearPositionClasses(removed);
                        }
                    }
                    getChildren().remove(change.getFrom(), change.getFrom() + change.getRemovedSize());
                }
                if (change.wasAdded()) {
                    for (FxListGroupItem added : change.getAddedSubList()) {
                        Objects.requireNonNull(added, "items must not contain null");
                    }
                    getChildren().addAll(change.getFrom(), change.getAddedSubList());
                }
            }
        }
        tagPositions();
    }

    private void tagPositions() {
        int last = items.size() - 1;
        for (int i = 0; i <= last; i++) {
            FxListGroupItem item = items.get(i);
            setClass(item, ITEM_FIRST, i == 0);
            setClass(item, ITEM_LAST, i == last);
        }
    }

    private static void clearPositionClasses(FxListGroupItem item) {
        item.getStyleClass().removeAll(ITEM_FIRST, ITEM_LAST);
    }

    private static void setClass(FxListGroupItem item, String styleClass, boolean present) {
        if (present) {
            if (!item.getStyleClass().contains(styleClass)) {
                item.getStyleClass().add(styleClass);
            }
        } else {
            item.getStyleClass().remove(styleClass);
        }
    }
}
