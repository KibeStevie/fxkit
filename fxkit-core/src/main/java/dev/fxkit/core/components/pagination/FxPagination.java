package dev.fxkit.core.components.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A pagination control, modelled on Flowbite React's {@code <Pagination>}.
 *
 * <pre>{@code
 * FxPagination pagination = new FxPagination(1, 100);
 * pagination.setShowIcons(true);
 * pagination.setOnPageChange(page -> loadPage(page));
 * }</pre>
 *
 * <h2>Layouts</h2>
 * <ul>
 *   <li>{@link Layout#PAGINATION} (default): previous button, a window of page numbers, next button.
 *       Like Flowbite, the window is the current page plus two on each side, so near the start or
 *       end it shows fewer than five numbers.</li>
 *   <li>{@link Layout#NAVIGATION}: only the previous and next buttons.</li>
 *   <li>{@link Layout#TABLE}: a "Showing 1 to 10 of 100 Entries" line above previous and next
 *       buttons. The page count is derived from {@link #totalItemsProperty() totalItems} and
 *       {@link #itemsPerPageProperty() itemsPerPage}; {@code totalPages} is ignored.</li>
 * </ul>
 *
 * <h2>Page changes</h2>
 * {@link #currentPageProperty() currentPage} is a normal JavaFX property, so it can be bound or
 * observed. Clicking a button sets it and then calls {@link #setOnPageChange(IntConsumer) onPageChange}
 * with the new page. Setting {@code currentPage} from code does not call {@code onPageChange}.
 * A value outside {@code 1..pageCount} is displayed as the nearest valid page (the property itself is
 * left as set).
 *
 * <h2>Styling</h2>
 * Root class {@code fxk-pagination} plus {@code fxk-pagination-layout-<layout>}; buttons carry
 * {@code fxk-pagination-btn} and one of {@code -prev}, {@code -next}, {@code -page}; the current page
 * also has {@code fxk-pagination-active}. All rules are in {@code components.css}.
 */
public class FxPagination extends VBox {

    /** Which controls are shown. */
    public enum Layout {
        /** Previous, page numbers, next. */
        PAGINATION,
        /** Previous and next only. */
        NAVIGATION,
        /** "Showing x to y of z Entries" above previous and next. */
        TABLE
    }

    /** Page numbers shown on each side of the current page (Flowbite shows 2). */
    private static final int SIBLINGS = 2;

    private static final String BTN = "fxk-pagination-btn";
    private static final String ACTIVE = "fxk-pagination-active";

    private final ObjectProperty<Layout> layout = new SimpleObjectProperty<>(this, "layout", Layout.PAGINATION);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(this, "currentPage", 1);
    private final IntegerProperty totalPages = new SimpleIntegerProperty(this, "totalPages", 1);
    private final IntegerProperty itemsPerPage = new SimpleIntegerProperty(this, "itemsPerPage", 10);
    private final IntegerProperty totalItems = new SimpleIntegerProperty(this, "totalItems", 0);
    private final BooleanProperty showIcons = new SimpleBooleanProperty(this, "showIcons", false);
    private final StringProperty previousLabel = new SimpleStringProperty(this, "previousLabel", "Previous");
    private final StringProperty nextLabel = new SimpleStringProperty(this, "nextLabel", "Next");
    private final ObjectProperty<IntConsumer> onPageChange = new SimpleObjectProperty<>(this, "onPageChange");

    private final TextFlow tableText = new TextFlow();
    private final HBox pagesRow = new HBox();
    private final Button previousButton = new Button();
    private final Button nextButton = new Button();
    /** Reused between refreshes so a focused page button keeps focus when the window moves. */
    private final List<PageButton> pageButtons = new ArrayList<>();

    public FxPagination() {
        getStyleClass().add("fxk-pagination");
        EnumStyleClassSync.sync(this, "fxk-pagination-layout-", layout);
        setAccessibleText("Pagination");

        tableText.getStyleClass().add("fxk-pagination-table");
        pagesRow.getStyleClass().add("fxk-pagination-pages");
        pagesRow.setAlignment(Pos.CENTER_LEFT);

        setUpButton(previousButton, "fxk-pagination-prev");
        previousButton.setContentDisplay(ContentDisplay.LEFT);
        previousButton.textProperty().bind(previousLabel);
        previousButton.setOnAction(e -> goTo(effectivePage() - 1));

        setUpButton(nextButton, "fxk-pagination-next");
        nextButton.setContentDisplay(ContentDisplay.RIGHT);
        nextButton.textProperty().bind(nextLabel);
        nextButton.setOnAction(e -> goTo(effectivePage() + 1));

        InvalidationListener refresh = observable -> refresh();
        layout.addListener(refresh);
        currentPage.addListener(refresh);
        totalPages.addListener(refresh);
        itemsPerPage.addListener(refresh);
        totalItems.addListener(refresh);
        showIcons.addListener(refresh);

        refresh();
    }

    /**
     * @param currentPage the page shown as current, 1-based
     * @param totalPages  how many pages there are
     */
    public FxPagination(int currentPage, int totalPages) {
        this();
        setCurrentPage(currentPage);
        setTotalPages(totalPages);
    }

    // ---- behaviour ---------------------------------------------------------------------------

    private void goTo(int target) {
        int count = pageCount();
        int page = Math.max(1, Math.min(target, count));
        if (page == effectivePage()) {
            return;
        }
        setCurrentPage(page);
        IntConsumer handler = getOnPageChange();
        if (handler != null) {
            handler.accept(page);
        }
    }

    private Layout effectiveLayout() {
        return getLayout() == null ? Layout.PAGINATION : getLayout();
    }

    private int pageCount() {
        if (effectiveLayout() == Layout.TABLE) {
            int per = Math.max(1, getItemsPerPage());
            int total = Math.max(0, getTotalItems());
            return Math.max(1, (total + per - 1) / per);
        }
        return Math.max(1, getTotalPages());
    }

    private int effectivePage() {
        return Math.min(Math.max(1, getCurrentPage()), pageCount());
    }

    // ---- rendering ---------------------------------------------------------------------------

    private void refresh() {
        Layout mode = effectiveLayout();
        int count = pageCount();
        int page = effectivePage();

        previousButton.setDisable(page <= 1);
        nextButton.setDisable(page >= count);

        boolean icons = isShowIcons();
        if (icons != (previousButton.getGraphic() != null)) {
            previousButton.setGraphic(icons ? chevron(true) : null);
            nextButton.setGraphic(icons ? chevron(false) : null);
        }

        List<Node> row = new ArrayList<>();
        row.add(previousButton);
        if (mode == Layout.PAGINATION) {
            int first = Math.max(1, page - SIBLINGS);
            int last = Math.min(page + SIBLINGS, count);
            int shown = last - first + 1;
            while (pageButtons.size() < shown) {
                pageButtons.add(new PageButton());
            }
            for (int i = 0; i < shown; i++) {
                PageButton button = pageButtons.get(i);
                button.show(first + i, first + i == page);
                row.add(button);
            }
        }
        row.add(nextButton);
        if (!pagesRow.getChildren().equals(row)) {
            pagesRow.getChildren().setAll(row);
        }

        List<Node> children = new ArrayList<>();
        if (mode == Layout.TABLE) {
            refreshTableText(page);
            children.add(tableText);
        }
        children.add(pagesRow);
        if (!getChildren().equals(children)) {
            getChildren().setAll(children);
        }
    }

    /** "Showing <b>1</b> to <b>10</b> of <b>100</b> Entries" */
    private void refreshTableText(int page) {
        int per = Math.max(1, getItemsPerPage());
        int total = Math.max(0, getTotalItems());
        int first = total == 0 ? 0 : (page - 1) * per + 1;
        int last = Math.min(page * per, total);
        tableText.getChildren().setAll(
                plain("Showing "), strong(first), plain(" to "), strong(last),
                plain(" of "), strong(total), plain(" Entries"));
    }

    private static Text plain(String value) {
        Text text = new Text(value);
        text.getStyleClass().add("fxk-pagination-table-text");
        return text;
    }

    private static Text strong(int value) {
        Text text = new Text(String.valueOf(value));
        text.getStyleClass().add("fxk-pagination-table-strong");
        return text;
    }

    private static Node chevron(boolean pointsLeft) {
        SVGPath path = new SVGPath();
        path.setContent(pointsLeft ? "M12.5 4.5 L7 10 L12.5 15.5" : "M7.5 4.5 L13 10 L7.5 15.5");
        path.getStyleClass().add("fxk-pagination-icon");
        StackPane box = new StackPane(path);
        box.getStyleClass().add("fxk-pagination-icon-box");
        return box;
    }

    /**
     * Neighbouring buttons overlap by 1px (Flowbite's -space-x-px), so a focused button is drawn on
     * top of its neighbours (view order only, the layout order is untouched) to keep its whole ring
     * visible.
     */
    private static void setUpButton(Button button, String... styleClasses) {
        button.getStyleClass().add(BTN);
        button.getStyleClass().addAll(styleClasses);
        button.focusedProperty().addListener((observable, was, now) -> button.setViewOrder(now ? -1 : 0));
    }

    private final class PageButton extends Button {
        private int page;

        PageButton() {
            setUpButton(this, "fxk-pagination-page");
            setOnAction(e -> goTo(page));
        }

        void show(int page, boolean active) {
            this.page = page;
            setText(String.valueOf(page));
            setAccessibleText(active ? "Page " + page + ", current page" : "Page " + page);
            if (active != getStyleClass().contains(ACTIVE)) {
                if (active) {
                    getStyleClass().add(ACTIVE);
                } else {
                    getStyleClass().remove(ACTIVE);
                }
            }
        }
    }

    // ---- properties --------------------------------------------------------------------------

    /** Which controls are shown. Default {@link Layout#PAGINATION}. */
    public ObjectProperty<Layout> layoutProperty() { return layout; }
    public Layout getLayout() { return layout.get(); }
    public void setLayout(Layout value) { layout.set(value); }

    /** The current page, 1-based. Default 1. */
    public IntegerProperty currentPageProperty() { return currentPage; }
    public int getCurrentPage() { return currentPage.get(); }
    public void setCurrentPage(int value) { currentPage.set(value); }

    /** Total number of pages for PAGINATION and NAVIGATION layouts. Default 1. */
    public IntegerProperty totalPagesProperty() { return totalPages; }
    public int getTotalPages() { return totalPages.get(); }
    public void setTotalPages(int value) { totalPages.set(value); }

    /** Items on one page, for the TABLE layout. Default 10. */
    public IntegerProperty itemsPerPageProperty() { return itemsPerPage; }
    public int getItemsPerPage() { return itemsPerPage.get(); }
    public void setItemsPerPage(int value) { itemsPerPage.set(value); }

    /** Total number of items, for the TABLE layout. Default 0. */
    public IntegerProperty totalItemsProperty() { return totalItems; }
    public int getTotalItems() { return totalItems.get(); }
    public void setTotalItems(int value) { totalItems.set(value); }

    /** Whether the previous and next buttons show a chevron. Default {@code false}. */
    public BooleanProperty showIconsProperty() { return showIcons; }
    public boolean isShowIcons() { return showIcons.get(); }
    public void setShowIcons(boolean value) { showIcons.set(value); }

    /** Text of the previous button. Default "Previous". */
    public StringProperty previousLabelProperty() { return previousLabel; }
    public String getPreviousLabel() { return previousLabel.get(); }
    public void setPreviousLabel(String value) { previousLabel.set(value); }

    /** Text of the next button. Default "Next". */
    public StringProperty nextLabelProperty() { return nextLabel; }
    public String getNextLabel() { return nextLabel.get(); }
    public void setNextLabel(String value) { nextLabel.set(value); }

    /** Called with the new page after the user changes it. Not called when {@code currentPage} is set from code. */
    public ObjectProperty<IntConsumer> onPageChangeProperty() { return onPageChange; }
    public IntConsumer getOnPageChange() { return onPageChange.get(); }
    public void setOnPageChange(IntConsumer value) { onPageChange.set(value); }
}
