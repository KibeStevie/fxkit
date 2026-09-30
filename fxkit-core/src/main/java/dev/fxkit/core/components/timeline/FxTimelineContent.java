package dev.fxkit.core.components.timeline;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * The text block of a timeline item (Flowbite's {@code <TimelineContent>}): typically an
 * {@link FxTimelineTime}, an {@link FxTimelineTitle}, an {@link FxTimelineBody} and optionally a
 * button, stacked top to bottom. Any node can be added.
 *
 * <p>Buttons keep their own width (JavaFX buttons do not stretch inside a {@code VBox}), so a
 * "Learn More" button sits at the left under the body text, as in Flowbite.
 *
 * <p>Style class: {@code fxk-timeline-content}. Its padding depends on the item's orientation and is
 * set in {@code components.css}.
 */
public class FxTimelineContent extends VBox {

    public FxTimelineContent(Node... children) {
        getStyleClass().add("fxk-timeline-content");
        getChildren().addAll(children);
    }
}
