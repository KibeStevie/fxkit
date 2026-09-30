package dev.fxkit.core.components.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * A breadcrumb trail: a row of {@link FxBreadcrumbItem}s separated by chevrons, showing where the
 * user is in a page hierarchy. Port of Flowbite React's {@code Breadcrumb}.
 *
 * <p>Put the items in {@link #getItems()} and the breadcrumb lays them out, inserting a chevron
 * between each pair. (Flowbite renders a chevron in every item and hides the first one with
 * {@code group-first:hidden}; here the separators are simply never created before the first item.)
 *
 * <h2>Default breadcrumb</h2>
 * <pre>{@code
 * FxBreadcrumb crumbs = new FxBreadcrumb(
 *         new FxBreadcrumbItem("Home", FontAwesomeSolid.HOME, e -> showHome()),
 *         new FxBreadcrumbItem("Projects", e -> showProjects()),
 *         new FxBreadcrumbItem("FXKit"));
 * }</pre>
 *
 * <h2>Solid background</h2>
 * <pre>{@code
 * crumbs.setSolidBackground(true); // Flowbite: className="bg-gray-50 px-5 py-3 dark:bg-gray-800"
 * }</pre>
 *
 * <h2>FXML</h2>
 * {@code items} is the default property, so items nest directly:
 * <pre>{@code
 * <FxBreadcrumb solidBackground="true">
 *     <FxBreadcrumbItem text="Home" onAction="#showHome"/>
 *     <FxBreadcrumbItem text="Projects" onAction="#showProjects"/>
 *     <FxBreadcrumbItem text="FXKit"/>
 * </FxBreadcrumb>
 * }</pre>
 * (The {@code icon} property takes an {@code Ikon}, which FXML cannot build from a string; set it
 * from the controller.)
 */
@DefaultProperty("items")
public class FxBreadcrumb extends HBox {

    // Chevron-right, drawn as a stroked path in a 16x16 box (Flowbite: h-4 w-4).
    private static final String CHEVRON_PATH = "M6 4 L10 8 L6 12";

    private final ObservableList<FxBreadcrumbItem> items = FXCollections.observableArrayList();
    private final BooleanProperty solidBackground = new SimpleBooleanProperty(this, "solidBackground", false);

    public FxBreadcrumb() {
        getStyleClass().add("fxk-breadcrumb");
        setAlignment(Pos.CENTER_LEFT); // items-center
        // Flowbite's aria-label prop. Override with setAccessibleText(...) for a more specific label.
        setAccessibleText("Breadcrumb");

        BooleanStyleClassSync.sync(this, "fxk-breadcrumb-solid", solidBackground);

        items.addListener((ListChangeListener<FxBreadcrumbItem>) change -> rebuild());
    }

    public FxBreadcrumb(FxBreadcrumbItem... initialItems) {
        this();
        items.addAll(initialItems);
    }

    /**
     * @return the live list of items, in display order (first = root, last = current page)
     */
    public final ObservableList<FxBreadcrumbItem> getItems() {
        return items;
    }

    /**
     * @return whether the breadcrumb has a filled, padded background (Flowbite's "background color"
     *         example)
     */
    public final BooleanProperty solidBackgroundProperty() {
        return solidBackground;
    }

    public final boolean isSolidBackground() {
        return solidBackground.get();
    }

    public final void setSolidBackground(boolean value) {
        solidBackground.set(value);
    }

    /** Lays out item, chevron, item, chevron, item ... Simple and cheap: breadcrumbs are short. */
    private void rebuild() {
        List<Node> nodes = new ArrayList<>(items.size() * 2);
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                nodes.add(createSeparator());
            }
            nodes.add(items.get(i));
        }
        getChildren().setAll(nodes);
    }

    private static Node createSeparator() {
        SVGPath chevron = new SVGPath();
        chevron.setContent(CHEVRON_PATH);
        chevron.getStyleClass().add("fxk-breadcrumb-chevron");

        StackPane separator = new StackPane(chevron);
        separator.getStyleClass().add("fxk-breadcrumb-separator");
        separator.setMouseTransparent(true);
        return separator;
    }
}
