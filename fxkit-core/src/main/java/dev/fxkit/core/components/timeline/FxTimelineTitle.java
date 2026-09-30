package dev.fxkit.core.components.timeline;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/** The heading of a timeline item (Flowbite's {@code <TimelineTitle>}). Style class {@code fxk-timeline-title}. */
public class FxTimelineTitle extends Label {

    public FxTimelineTitle(String text) {
        super(text);
        getStyleClass().add("fxk-timeline-title");
        setWrapText(true);
        // A wrapped Label in a box can otherwise be given too little height and show "...".
        setMinHeight(Region.USE_PREF_SIZE);
    }
}
