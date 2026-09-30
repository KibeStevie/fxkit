package dev.fxkit.core.components.timeline;

import java.util.Objects;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.VBox;

/**
 * One entry of an {@link FxTimeline}: a {@link FxTimelinePoint} and its {@link FxTimelineContent}
 * (Flowbite's {@code <TimelineItem>}).
 *
 * <p>The orientation is owned by the parent {@link FxTimeline}, which binds {@link #horizontalProperty()}
 * when the item is added to it. An item used outside a timeline is vertical.
 *
 * <ul>
 *   <li>Vertical: the content is indented (24px) and the point is drawn over the timeline's line, at the
 *       item's left edge. The point does not take part in layout.</li>
 *   <li>Horizontal: the point row (marker and line) sits on top, the content below it.</li>
 * </ul>
 *
 * <p>Style classes: {@code fxk-timeline-item}, plus {@code fxk-timeline-item-horizontal} or
 * {@code fxk-timeline-item-vertical}.
 */
public class FxTimelineItem extends VBox {

    private final BooleanProperty horizontal = new SimpleBooleanProperty(this, "horizontal", false);
    private final BooleanBinding vertical = horizontal.not(); // field: see FxTimeline

    private final FxTimelinePoint point;
    private final FxTimelineContent content;

    public FxTimelineItem(FxTimelinePoint point, FxTimelineContent content) {
        this.point = Objects.requireNonNull(point, "point");
        this.content = Objects.requireNonNull(content, "content");

        getStyleClass().add("fxk-timeline-item");
        BooleanStyleClassSync.sync(this, "fxk-timeline-item-horizontal", horizontal);
        BooleanStyleClassSync.sync(this, "fxk-timeline-item-vertical", vertical);

        point.horizontalProperty().bind(horizontal);
        horizontal.addListener((observable, oldValue, newValue) -> arrange());
        arrange();
    }

    public FxTimelinePoint getPoint() {
        return point;
    }

    public FxTimelineContent getContent() {
        return content;
    }

    /** Set by the parent {@link FxTimeline}; bound while the item is in one. */
    public BooleanProperty horizontalProperty() {
        return horizontal;
    }

    public boolean isHorizontal() {
        return horizontal.get();
    }

    /**
     * Horizontal: the point is a normal first row. Vertical: it is an unmanaged overlay, listed after the
     * content so it is painted on top (Flowbite's absolutely positioned marker).
     */
    private void arrange() {
        boolean isHorizontal = isHorizontal();
        point.setManaged(isHorizontal);
        if (isHorizontal) {
            getChildren().setAll(point, content);
        } else {
            getChildren().setAll(content, point);
        }
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (!isHorizontal()) {
            // Unmanaged, so VBox neither sizes nor places it. Pin it to the item's top-left corner;
            // fxk-timeline-marker's translate-x/-y (components.css) then centers it on the line.
            point.autosize();
            point.relocate(0, 0);
        }
    }
}
