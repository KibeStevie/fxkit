package dev.fxkit.core.components.timeline;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/** The description of a timeline item (Flowbite's {@code <TimelineBody>}). Style class {@code fxk-timeline-body}. */
public class FxTimelineBody extends Label {

    public FxTimelineBody(String text) {
        super(text);
        getStyleClass().add("fxk-timeline-body");
        setWrapText(true);
        // A wrapped Label in a box can otherwise be given too little height and show "...".
        setMinHeight(Region.USE_PREF_SIZE);
    }
}
