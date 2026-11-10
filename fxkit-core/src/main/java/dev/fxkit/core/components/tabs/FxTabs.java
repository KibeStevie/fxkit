package dev.fxkit.core.components.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A tab strip with one panel below it, modelled on Flowbite React's
 * {@code <Tabs>}: add
 * {@link FxTabItem}s, click a tab, and its content replaces the previous one.
 *
 * <pre>{@code
 * FxTabs tabs = new FxTabs(FxTabs.Variant.UNDERLINE);
 * tabs.getItems().addAll(
 *         new FxTabItem("Profile", Devicons.JAVA, new Label("Profile content")),
 *         new FxTabItem("Dashboard", new Label("Dashboard content")));
 * tabs.setAccessibleText("Tabs with underline"); // aria-label
 * tabs.setOnActiveTabChange(index -> System.out.println(index));
 * tabs.setActiveTab(1); // like the ref's setActiveTab
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <table>
 * <caption>React prop to FxTabs property</caption>
 * <tr>
 * <td>{@code variant}</td>
 * <td>{@link #variantProperty()}: {@link Variant}, default {@code DEFAULT}</td>
 * </tr>
 * <tr>
 * <td>{@code <TabItem>} children</td>
 * <td>{@link #getItems()}</td>
 * </tr>
 * <tr>
 * <td>{@code aria-label}</td>
 * <td>{@code setAccessibleText(...)} (inherited from {@link Node})</td>
 * </tr>
 * <tr>
 * <td>{@code onActiveTabChange}</td>
 * <td>{@link #onActiveTabChangeProperty()}, called with the new index</td>
 * </tr>
 * <tr>
 * <td>ref {@code setActiveTab(i)} / {@code activeTab}</td>
 * <td>{@link #setActiveTab(int)} / {@link #activeTabProperty()}</td>
 * </tr>
 * </table>
 *
 * <h2>Behavior</h2>
 * <ul>
 * <li>With no {@code active} item the first tab is shown. If items are flagged
 * {@link FxTabItem#setActive(boolean) active} when added, the first of them is
 * shown.</li>
 * <li>Clicking a tab (or pressing Space/Enter on it) selects it; disabled tabs
 * ignore input.
 * Selecting from code is not blocked by {@code disabled}, only user input
 * is.</li>
 * <li>Keyboard, as in Flowbite: Tab enters the strip on one tab only;
 * Left/Right move focus to the
 * previous/next enabled tab (no wrap), Home/End to the first/last. Focus moves
 * without
 * selecting; Space/Enter selects.</li>
 * <li>Removing the selected item selects the first remaining one.</li>
 * <li>Only the selected item's content is in the scene graph; the others keep
 * their state and
 * come back as they were.</li>
 * <li>The tab strip wraps onto several rows when it is narrower than its tabs
 * (not for
 * {@code FULL_WIDTH}, where tabs share the width).</li>
 * </ul>
 *
 * <h2>Structure</h2>
 *
 * <pre>
 * .fxk-tabs  .fxk-tabs-&lt;variant&gt;       VBox
 *   .fxk-tabs-listwrap                  StackPane: .fxk-tabs-divider + the list
 *     .fxk-tabs-list                    FlowPane (wrapping variants) or HBox (FULL_WIDTH)
 *       .fxk-tab .fxk-tab-&lt;variant&gt; [.fxk-tab-active]    one Button per item
 *   .fxk-tabs-panel                     StackPane holding the active item's content
 * </pre>
 *
 * See the FxTabs block in {@code components.css}.
 */
public class FxTabs extends VBox {

    /**
     * The tab styles, as in Flowbite's {@code variant} prop ({@code fullWidth} is
     * {@code FULL_WIDTH}).
     */
    public enum Variant {
        DEFAULT, UNDERLINE, PILLS, FULL_WIDTH
    }

    /**
     * Wrap length of the wrapping list: large, so the tab strip's preferred width
     * is "all tabs in
     * one row" and it only wraps when the container is actually narrower
     * (flex-wrap).
     */
    private static final double WRAP_LENGTH = 10_000;

    // ---- properties
    // ---------------------------------------------------------------------------

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.DEFAULT);
    private final ReadOnlyObjectWrapper<FxTabItem> selectedItem = new ReadOnlyObjectWrapper<>(this, "selectedItem");
    private final ReadOnlyIntegerWrapper activeTab = new ReadOnlyIntegerWrapper(this, "activeTab", -1);
    private final ObjectProperty<IntConsumer> onActiveTabChange = new SimpleObjectProperty<>(this,
            "onActiveTabChange");
    private final ObservableList<FxTabItem> items = FXCollections.observableArrayList();
    // FXKit additions for editor-style tabs
    private final ObjectProperty<Consumer<FxTabItem>> onCloseRequest = new SimpleObjectProperty<>(this,
            "onCloseRequest");
    private final BooleanProperty reorderable = new SimpleBooleanProperty(this, "reorderable", false);
    private final ObjectProperty<BiConsumer<Integer, Integer>> onTabMoved = new SimpleObjectProperty<>(this,
            "onTabMoved");
    private final ObjectProperty<Node> trailing = new SimpleObjectProperty<>(this, "trailing");

    // ---- nodes / state
    // --------------------------------------------------------------------------

    private final Map<FxTabItem, Entry> entries = new HashMap<>();
    private final FlowPane flowList = new FlowPane();
    private final HBox gridList = new HBox();
    private final Region divider = new Region();
    private final HBox header = new HBox(); // the tab list, then the optional trailing node
    private final StackPane listWrap = new StackPane();
    private final StackPane panel = new StackPane();

    /**
     * The tab that is reachable with the Tab key (roving tabindex): the last one
     * that had focus.
     */
    private Entry roving;
    /**
     * True while this class itself is writing {@link FxTabItem#activeProperty()}
     * flags.
     */
    private boolean syncing;

    public FxTabs() {
        getStyleClass().add("fxk-tabs");
        EnumStyleClassSync.sync(this, "fxk-tabs-", variant);
        setMaxWidth(Double.MAX_VALUE);
        setAccessibleRole(AccessibleRole.TAB_PANE);

        flowList.getStyleClass().add("fxk-tabs-list");
        flowList.setPrefWrapLength(WRAP_LENGTH);
        flowList.setMaxWidth(Double.MAX_VALUE);

        gridList.getStyleClass().add("fxk-tabs-list");
        gridList.setMaxWidth(Double.MAX_VALUE);

        divider.getStyleClass().add("fxk-tabs-divider");
        divider.setMinHeight(1);
        divider.setPrefHeight(1);
        divider.setMaxHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(divider, Pos.BOTTOM_LEFT);

        header.getStyleClass().add("fxk-tabs-header");
        header.setAlignment(Pos.BOTTOM_LEFT);
        trailing.addListener((o, was, is) -> rebuildList());
        listWrap.getStyleClass().add("fxk-tabs-listwrap");
        panel.getStyleClass().add("fxk-tabs-panel");
        VBox.setVgrow(panel, Priority.ALWAYS);
        getChildren().addAll(listWrap, panel);

        items.addListener((ListChangeListener<FxTabItem>) change -> itemsChanged());
        variant.addListener((o, was, is) -> {
            rebuildList();
            refreshButtons();
        });
        selectedItem.addListener((o, was, is) -> {
            activeTab.set(items.indexOf(is));
            syncActiveFlags();
            refreshButtons();
            updateTraversal();
            showPanel();
            IntConsumer callback = onActiveTabChange.get();
            if (callback != null) {
                callback.accept(activeTab.get());
            }
        });

        rebuildList();
        showPanel();
    }

    /**
     * @param variant the tab style
     */
    public FxTabs(Variant variant) {
        this();
        setVariant(variant);
    }

    /**
     * @param variant the tab style
     * @param tabs    the initial items
     */
    public FxTabs(Variant variant, FxTabItem... tabs) {
        this(variant);
        items.addAll(tabs);
    }

    // ---- items / selection
    // ------------------------------------------------------------------------

    private void itemsChanged() {
        entries.entrySet().removeIf(e -> {
            if (items.contains(e.getKey())) {
                return false;
            }
            e.getValue().dispose();
            if (roving == e.getValue()) {
                roving = null;
            }
            return true;
        });

        FxTabItem flagged = null;
        for (FxTabItem item : items) {
            if (!entries.containsKey(item)) {
                entries.put(item, new Entry(item));
                if (flagged == null && item.isActive()) {
                    flagged = item;
                }
            }
        }

        FxTabItem current = selectedItem.get();
        if (flagged != null) {
            selectedItem.set(flagged);
        } else if (current == null || !items.contains(current)) {
            selectedItem.set(items.isEmpty() ? null : items.get(0));
        }
        activeTab.set(items.indexOf(selectedItem.get()));

        rebuildList();
        refreshButtons();
        updateTraversal();
        showPanel();
    }

    /** Makes every item's {@code active} flag mirror the selection. */
    private void syncActiveFlags() {
        FxTabItem selected = selectedItem.get();
        syncing = true;
        try {
            for (FxTabItem item : items) {
                item.setActive(item == selected);
            }
        } finally {
            syncing = false;
        }
    }

    // ---- building the strip
    // --------------------------------------------------------------------------

    private Variant effectiveVariant() {
        Variant v = variant.get();
        return v == null ? Variant.DEFAULT : v;
    }

    private static String styleName(Variant v) {
        return v.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Puts the tabs into the right list for the variant, in item order. */
    private void rebuildList() {
        Variant v = effectiveVariant();
        boolean full = v == Variant.FULL_WIDTH;
        Pane target = full ? gridList : flowList;

        flowList.getChildren().clear();
        gridList.getChildren().clear();
        for (Entry entry : orderedEntries()) {
            if (!target.getChildren().contains(entry.holder)) {
                target.getChildren().add(entry.holder);
            }
            // grid-flow-col with w-full: tabs share the free space equally
            entry.holder.setMaxWidth(full ? Double.MAX_VALUE : Region.USE_COMPUTED_SIZE);
            HBox.setHgrow(entry.holder, full ? Priority.ALWAYS : null);
        }

        HBox.setHgrow(target, Priority.ALWAYS);
        Node extra = trailing.get();
        if (extra == null) {
            header.getChildren().setAll(target);
        } else {
            header.getChildren().setAll(target, extra);
        }

        boolean hasDivider = v == Variant.DEFAULT || v == Variant.UNDERLINE;
        if (hasDivider) {
            listWrap.getChildren().setAll(divider, header);
        } else {
            listWrap.getChildren().setAll(header);
        }
    }

    /** Re-applies the variant class and the active class to every tab button. */
    private void refreshButtons() {
        String variantClass = "fxk-tab-" + styleName(effectiveVariant());
        FxTabItem selected = selectedItem.get();
        for (Entry entry : entries.values()) {
            List<String> classes = entry.button.getStyleClass();
            classes.removeIf(c -> c.startsWith("fxk-tab-") && !c.equals("fxk-tab-closable"));
            classes.add(variantClass);
            if (entry.item == selected) {
                classes.add("fxk-tab-active");
            }
        }
    }

    private void showPanel() {
        FxTabItem selected = selectedItem.get();
        Node content = selected == null ? null : selected.getContent();
        panel.getChildren().setAll(content == null ? List.<Node>of() : List.of(content));
        boolean shown = selected != null;
        panel.setVisible(shown);
        panel.setManaged(shown);
    }

    private List<Entry> orderedEntries() {
        List<Entry> ordered = new ArrayList<>();
        for (FxTabItem item : items) {
            Entry entry = entries.get(item);
            if (entry != null && !ordered.contains(entry)) {
                ordered.add(entry);
            }
        }
        return ordered;
    }

    // ---- keyboard
    // ------------------------------------------------------------------------------------

    /** Roving tabindex: exactly one enabled tab is reachable with the Tab key. */
    private void updateTraversal() {
        List<Entry> ordered = orderedEntries();
        Entry target = roving != null && ordered.contains(roving) && !roving.item.isDisabled() ? roving : null;
        if (target == null) {
            FxTabItem selected = selectedItem.get();
            Entry selectedEntry = selected == null ? null : entries.get(selected);
            if (selectedEntry != null && !selectedEntry.item.isDisabled()) {
                target = selectedEntry;
            }
        }
        if (target == null) {
            for (Entry entry : ordered) {
                if (!entry.item.isDisabled()) {
                    target = entry;
                    break;
                }
            }
        }
        for (Entry entry : ordered) {
            entry.button.setFocusTraversable(entry == target);
        }
    }

    private void onKeyPressed(Entry from, KeyEvent event) {
        List<Entry> ordered = orderedEntries();
        int index = ordered.indexOf(from);
        int target;
        switch (event.getCode()) {
            case RIGHT -> target = nextEnabled(ordered, index, 1);
            case LEFT -> target = nextEnabled(ordered, index, -1);
            case HOME -> target = nextEnabled(ordered, -1, 1);
            case END -> target = nextEnabled(ordered, ordered.size(), -1);
            default -> {
                return;
            }
        }
        event.consume(); // otherwise JavaFX's own arrow-key focus traversal would jump out of the strip
        if (target >= 0 && target != index) {
            ordered.get(target).button.requestFocus();
        }
    }

    private static int nextEnabled(List<Entry> ordered, int from, int step) {
        for (int i = from + step; i >= 0 && i < ordered.size(); i += step) {
            if (!ordered.get(i).item.isDisabled()) {
                return i;
            }
        }
        return -1;
    }

    // ---- close and reorder (FXKit additions)
    // ---------------------------------------------------------------------------------------

    /** The close button or a middle click: asks {@code onCloseRequest}, or removes the item without one. */
    private void requestClose(FxTabItem item) {
        Consumer<FxTabItem> handler = onCloseRequest.get();
        if (handler != null) {
            handler.accept(item);
        } else {
            items.remove(item);
        }
    }

    /** Drops {@code dragged} where its centre (x in the strip) lies: before the first tab whose middle is further right. */
    private void moveTab(Entry dragged, double centerX) {
        List<Entry> ordered = orderedEntries();
        int from = ordered.indexOf(dragged);
        if (from < 0) {
            return;
        }
        int to = ordered.size() - 1;
        for (int i = 0; i < ordered.size(); i++) {
            Entry e = ordered.get(i);
            if (e != dragged && centerX < e.holder.getLayoutX() + e.holder.getWidth()) {
                to = i;
                break;
            }
        }
        // moving right: the tab ends up after the neighbour it passed
        if (to > from && to < ordered.size() - 1 && centerX < ordered.get(to).holder.getLayoutX()) {
            to--;
        }
        if (to == from) {
            return;
        }
        List<FxTabItem> reordered = new ArrayList<>(items);
        reordered.add(to, reordered.remove(from));
        items.setAll(reordered); // the selection survives: every item is still in the list
        BiConsumer<Integer, Integer> callback = onTabMoved.get();
        if (callback != null) {
            callback.accept(from, to);
        }
    }

    // ---- one tab
    // ---------------------------------------------------------------------------------------

    /**
     * The button of one item, plus the listeners that keep it in sync with the
     * item.
     */
    private final class Entry {
        final FxTabItem item;
        final Button button = new Button();
        final StackPane holder = new StackPane(); // the tab button, with the close button on top of its right end
        private Button closeButton;

        // drag-to-reorder state
        private double pressSceneX;
        private boolean dragging;

        private final InvalidationListener appearance = o -> {
            updateButton();
            updateTraversal();
        };

        private final InvalidationListener contentChanged;

        private final ChangeListener<Boolean> activeChanged = (o, was, is) -> onActiveFlag(is);

        private final ChangeListener<Boolean> focusChanged = (o, was, is) -> {
            if (is) {
                roving = this;
                updateTraversal();
            }
        };

        Entry(FxTabItem item) {
            this.item = item;

            // item is initialized before contentChanged is created
            this.contentChanged = o -> {
                if (selectedItem.get() == this.item) {
                    showPanel();
                }
            };

            button.getStyleClass().add("fxk-tab");
            button.setMnemonicParsing(false);
            button.setAccessibleRole(AccessibleRole.TAB_ITEM);

            button.setOnAction(e -> {
                if (!item.isDisabled()) {
                    selectedItem.set(item);
                }
            });

            button.setOnKeyPressed(e -> onKeyPressed(this, e));
            button.focusedProperty().addListener(focusChanged);

            button.setMaxWidth(Double.MAX_VALUE);
            button.setMaxHeight(Double.MAX_VALUE);
            holder.getStyleClass().add("fxk-tab-holder");
            holder.setAlignment(Pos.CENTER_RIGHT);
            holder.getChildren().add(button);
            button.addEventHandler(MouseEvent.MOUSE_PRESSED, this::dragPressed);
            button.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::dragMoved);
            button.addEventHandler(MouseEvent.MOUSE_RELEASED, this::dragReleased);
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.MIDDLE && item.isClosable() && !item.isDisabled()) {
                    requestClose(item);
                }
            });
            item.closableProperty().addListener(appearance);

            item.titleProperty().addListener(appearance);
            item.iconProperty().addListener(appearance);
            item.disabledProperty().addListener(appearance);
            item.contentProperty().addListener(contentChanged);
            item.activeProperty().addListener(activeChanged);

            updateButton();
        }

        private void updateButton() {
            button.setText(item.getTitle());

            Ikon icon = item.getIcon();
            button.setGraphic(
                    icon == null ? null : new FontIcon(icon));

            button.setDisable(item.isDisabled());

            boolean closable = item.isClosable();
            button.getStyleClass().remove("fxk-tab-closable");
            if (closable) {
                button.getStyleClass().add("fxk-tab-closable"); // room for the close button
                if (closeButton == null) {
                    closeButton = createCloseButton();
                }
                if (!holder.getChildren().contains(closeButton)) {
                    holder.getChildren().add(closeButton);
                }
                closeButton.setDisable(item.isDisabled());
            } else if (closeButton != null) {
                holder.getChildren().remove(closeButton);
            }
        }

        private Button createCloseButton() {
            SVGPath glyph = new SVGPath();
            glyph.setContent("M1 1L9 9M9 1L1 9");
            glyph.getStyleClass().add("fxk-tab-close-glyph");
            Button close = new Button();
            close.setGraphic(glyph);
            close.getStyleClass().add("fxk-tab-close");
            close.setFocusTraversable(false);
            close.setAccessibleText("Close tab");
            close.setOnAction(e -> requestClose(item));
            StackPane.setAlignment(close, Pos.CENTER_RIGHT);
            return close;
        }

        // ---- drag to reorder (only while the tabs are reorderable)

        private void dragPressed(MouseEvent e) {
            dragging = false;
            pressSceneX = e.getSceneX();
        }

        private void dragMoved(MouseEvent e) {
            if (!reorderable.get() || e.getButton() != MouseButton.PRIMARY || item.isDisabled()) {
                return;
            }
            double dx = e.getSceneX() - pressSceneX;
            if (!dragging) {
                if (Math.abs(dx) < 4) {
                    return;
                }
                dragging = true;
                holder.getStyleClass().add("fxk-tab-dragging");
                holder.setViewOrder(-1); // paint above its neighbours without changing the layout order
            }
            holder.setTranslateX(dx);
            e.consume();
        }

        private void dragReleased(MouseEvent e) {
            if (!dragging) {
                return;
            }
            dragging = false;
            holder.getStyleClass().remove("fxk-tab-dragging");
            holder.setViewOrder(0);
            double center = holder.getLayoutX() + holder.getWidth() / 2 + holder.getTranslateX();
            holder.setTranslateX(0);
            moveTab(this, center);
            e.consume();
        }

        /**
         * {@code item.active} was written from outside (or by us, guarded by
         * {@code syncing}).
         */
        private void onActiveFlag(boolean active) {
            if (syncing) {
                return;
            }

            if (active) {
                selectedItem.set(item);
            } else if (selectedItem.get() == item) {
                syncing = true;
                try {
                    item.setActive(true);
                } finally {
                    syncing = false;
                }
            }
        }

        void dispose() {
            item.titleProperty().removeListener(appearance);
            item.iconProperty().removeListener(appearance);
            item.disabledProperty().removeListener(appearance);
            item.contentProperty().removeListener(contentChanged);
            item.activeProperty().removeListener(activeChanged);
            item.closableProperty().removeListener(appearance);
            button.focusedProperty().removeListener(focusChanged);

            button.setOnAction(null);
            button.setOnKeyPressed(null);
        }
    }

    // ---- property accessors
    // --------------------------------------------------------------------------------

    /** The tab style. Default {@link Variant#DEFAULT}. */
    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Variant getVariant() {
        return variant.get();
    }

    public void setVariant(Variant value) {
        variant.set(value);
    }

    /**
     * The tabs, in order. Add, remove and reorder freely; the selection follows
     * (see the class description).
     */
    public ObservableList<FxTabItem> getItems() {
        return items;
    }

    /** The selected tab, or {@code null} when there are no tabs. */
    public ReadOnlyObjectProperty<FxTabItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    public FxTabItem getSelectedItem() {
        return selectedItem.get();
    }

    /**
     * Selects {@code item} (not blocked by {@code disabled}), or clears the
     * selection with
     * {@code null}, which hides the panel.
     *
     * @throws IllegalArgumentException if {@code item} is not one of
     *                                  {@link #getItems()}
     */
    public void setSelectedItem(FxTabItem item) {
        if (item != null && !items.contains(item)) {
            throw new IllegalArgumentException("item is not in this FxTabs");
        }
        selectedItem.set(item);
    }

    /**
     * Index of the selected tab in {@link #getItems()}, or {@code -1} when there is
     * none.
     */
    public ReadOnlyIntegerProperty activeTabProperty() {
        return activeTab.getReadOnlyProperty();
    }

    public int getActiveTab() {
        return activeTab.get();
    }

    /**
     * Selects the tab at {@code index}, or clears the selection with {@code -1}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is not {@code -1} or a
     *                                   valid index
     */
    public void setActiveTab(int index) {
        setSelectedItem(index == -1 ? null : items.get(index));
    }

    /**
     * Called with the new index whenever the selected tab changes, from a click,
     * the keyboard or
     * code. Also called for the very first selection and when the selected item is
     * removed.
     */
    public ObjectProperty<IntConsumer> onActiveTabChangeProperty() {
        return onActiveTabChange;
    }

    public IntConsumer getOnActiveTabChange() {
        return onActiveTabChange.get();
    }

    public void setOnActiveTabChange(IntConsumer callback) {
        onActiveTabChange.set(callback);
    }

    // ---- FXKit additions: closable, reorderable, trailing
    // ---------------------------------------------------------------------------------------

    /**
     * Called with the item when its close button (or a middle click) is used on a
     * {@link FxTabItem#closableProperty() closable} tab. The handler decides: ask to save, then remove the
     * item from {@link #getItems()}. When it is {@code null} the item is simply removed.
     */
    public ObjectProperty<Consumer<FxTabItem>> onCloseRequestProperty() {
        return onCloseRequest;
    }

    public Consumer<FxTabItem> getOnCloseRequest() {
        return onCloseRequest.get();
    }

    public void setOnCloseRequest(Consumer<FxTabItem> handler) {
        onCloseRequest.set(handler);
    }

    /**
     * Lets the user drag tabs sideways to reorder them (single row of tabs; not for
     * {@link Variant#FULL_WIDTH}). Default {@code false}.
     */
    public BooleanProperty reorderableProperty() {
        return reorderable;
    }

    public boolean isReorderable() {
        return reorderable.get();
    }

    public void setReorderable(boolean value) {
        reorderable.set(value);
    }

    /** Called with (from, to) after the user dragged a tab to a new position. */
    public ObjectProperty<BiConsumer<Integer, Integer>> onTabMovedProperty() {
        return onTabMoved;
    }

    public void setOnTabMoved(BiConsumer<Integer, Integer> callback) {
        onTabMoved.set(callback);
    }

    /**
     * A node shown at the right end of the tab strip, beside the tabs (a status text, a button). It sits
     * on the same baseline and the tabs take the remaining width. {@code null} for none.
     */
    public ObjectProperty<Node> trailingProperty() {
        return trailing;
    }

    public Node getTrailing() {
        return trailing.get();
    }

    public void setTrailing(Node node) {
        trailing.set(node);
    }
}
