package dev.fxkit.core.components.timeline;

import java.util.Objects;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A list of events on a line, modelled on Flowbite React's {@code <Timeline>}.
 *
 * <pre>{@code
 * FxTimeline timeline = new FxTimeline(
 *     new FxTimelineItem(
 *         new FxTimelinePoint(),
 *         new FxTimelineContent(
 *             new FxTimelineTime("February 2022"),
 *             new FxTimelineTitle("Application UI code in Tailwind CSS"),
 *             new FxTimelineBody("Get access to over 20+ pages ..."))));
 *
 * timeline.setHorizontal(true);   // optional: lay the items out side by side
 * }</pre>
 *
 * <p>Vertical (the default): the timeline draws the line as its left border; every item is indented
 * and its point sits centered on that line. Because the point straddles the line, give the timeline
 * some left padding (for example the {@code pl-6} utility class), or an icon point's ring is cut off
 * by whatever contains it.
 *
 * <p>Horizontal: items share the width equally and each draws its own line segment next to its point.
 * The timeline therefore needs a width from its parent (a {@code VBox}, a {@code BorderPane} center,
 * a {@code ScrollPane} with {@code fitToWidth}); on its own it would size to zero.
 *
 * <p>Style classes: {@code fxk-timeline}, plus {@code fxk-timeline-vertical} or
 * {@code fxk-timeline-horizontal}. Colors are the looked-up colors defined in {@code colors.css}.
 */
public class FxTimeline extends StackPane {

    private final BooleanProperty horizontal = new SimpleBooleanProperty(this, "horizontal", false);
    // Held in a field on purpose: a binding that is only referenced by a listener can be garbage collected.
    private final BooleanBinding vertical = horizontal.not();
    private final ObservableList<FxTimelineItem> items = FXCollections.observableArrayList();

    private Pane container;

    public FxTimeline() {
        getStyleClass().add("fxk-timeline");
        setAlignment(Pos.TOP_LEFT);

        BooleanStyleClassSync.sync(this, "fxk-timeline-horizontal", horizontal);
        BooleanStyleClassSync.sync(this, "fxk-timeline-vertical", vertical);

        items.addListener((ListChangeListener<FxTimelineItem>) change -> {
            while (change.next()) {
                for (FxTimelineItem removed : change.getRemoved()) {
                    removed.horizontalProperty().unbind();
                }
                for (FxTimelineItem added : change.getAddedSubList()) {
                    added.horizontalProperty().bind(horizontal);
                }
            }
            populateContainer();
        });
        horizontal.addListener((observable, oldValue, newValue) -> rebuildContainer());

        rebuildContainer();
    }

    public FxTimeline(FxTimelineItem... items) {
        this();
        this.items.addAll(Objects.requireNonNull(items, "items"));
    }

    /** The items, in order. Add to or remove from this list; the timeline re-lays itself out. */
    public ObservableList<FxTimelineItem> getItems() {
        return items;
    }

    /** {@code true} lays the items out side by side; {@code false} (default) stacks them. */
    public BooleanProperty horizontalProperty() {
        return horizontal;
    }

    public boolean isHorizontal() {
        return horizontal.get();
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal.set(horizontal);
    }

    /** Swaps the VBox/HBox that holds the items; the items themselves follow {@code horizontal} by binding. */
    private void rebuildContainer() {
        if (container != null) {
            container.getChildren().clear(); // an item can only have one parent
        }
        container = isHorizontal() ? new HBox() : new VBox();
        container.getStyleClass().add("fxk-timeline-container");
        getChildren().setAll(container);
        populateContainer();
    }

    private void populateContainer() {
        container.getChildren().setAll(items);
        if (isHorizontal()) {
            for (FxTimelineItem item : items) {
                HBox.setHgrow(item, Priority.ALWAYS);
            }
        }
    }
}
