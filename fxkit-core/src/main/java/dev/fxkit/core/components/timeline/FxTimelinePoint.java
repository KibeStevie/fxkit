package dev.fxkit.core.components.timeline;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The dot (or icon) that marks an item on the line (Flowbite's {@code <TimelinePoint>}).
 *
 * <ul>
 *   <li>No icon: a 12px dot with a thin border in the ring color.</li>
 *   <li>With {@link #setIcon(Ikon)}: a 24px tinted circle with the icon inside, surrounded by an 8px
 *       ring in the surface color that cuts a gap into the line (Flowbite's {@code ring-8}).</li>
 * </ul>
 *
 * <p>In a horizontal timeline the point is also the row that holds the line segment running to the
 * next item; in a vertical timeline it is just the marker.
 *
 * <p>Style classes: {@code fxk-timeline-point}, {@code fxk-timeline-point-horizontal} /
 * {@code -vertical}; parts: {@code fxk-timeline-marker} (plus {@code fxk-timeline-marker-icon} when an
 * icon is set), {@code fxk-timeline-line}, {@code fxk-timeline-icon}.
 */
public class FxTimelinePoint extends HBox {

    private final BooleanProperty horizontal = new SimpleBooleanProperty(this, "horizontal", false);
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    // Fields on purpose: bindings only referenced by listeners can be garbage collected.
    private final BooleanBinding vertical = horizontal.not();
    private final BooleanBinding hasIcon = icon.isNotNull();

    private final StackPane marker = new StackPane();
    private final Region line = new Region();

    public FxTimelinePoint() {
        getStyleClass().add("fxk-timeline-point");
        setAlignment(Pos.CENTER_LEFT);
        BooleanStyleClassSync.sync(this, "fxk-timeline-point-horizontal", horizontal);
        BooleanStyleClassSync.sync(this, "fxk-timeline-point-vertical", vertical);

        // The line only exists in a horizontal timeline (vertical uses the timeline's own left border).
        line.getStyleClass().add("fxk-timeline-line");
        line.visibleProperty().bind(horizontal);
        line.managedProperty().bind(horizontal);
        HBox.setHgrow(line, Priority.ALWAYS);

        // Unmanaged: positioned in layoutChildren and offset by CSS so it straddles the line.
        marker.getStyleClass().add("fxk-timeline-marker");
        marker.setManaged(false);
        BooleanStyleClassSync.sync(marker, "fxk-timeline-marker-icon", hasIcon);
        icon.addListener((observable, oldValue, newValue) -> refreshIcon());

        getChildren().addAll(line, marker);
    }

    public FxTimelinePoint(Ikon icon) {
        this();
        setIcon(icon);
    }

    /** Set by the owning {@link FxTimelineItem}. */
    public BooleanProperty horizontalProperty() {
        return horizontal;
    }

    public boolean isHorizontal() {
        return horizontal.get();
    }

    /** The icon shown inside the marker; {@code null} (default) shows the plain dot. */
    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public Ikon getIcon() {
        return icon.get();
    }

    public void setIcon(Ikon icon) {
        this.icon.set(icon);
    }

    private void refreshIcon() {
        Ikon ikon = icon.get();
        if (ikon == null) {
            marker.getChildren().clear();
        } else {
            FontIcon glyph = new FontIcon(ikon);
            glyph.getStyleClass().add("fxk-timeline-icon");
            marker.getChildren().setAll(glyph);
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        marker.autosize();
        marker.relocate(0, 0);
    }
}
