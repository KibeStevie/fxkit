package dev.fxkit.core.components.timeline;

import javafx.scene.control.Label;

/** The date line of a timeline item (Flowbite's {@code <TimelineTime>}). Style class {@code fxk-timeline-time}. */
public class FxTimelineTime extends Label {

    public FxTimelineTime(String text) {
        super(text);
        getStyleClass().add("fxk-timeline-time");
    }
}
