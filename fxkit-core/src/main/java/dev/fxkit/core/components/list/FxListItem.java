package dev.fxkit.core.components.list;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * One entry of an {@link FxList}. Port of Flowbite React's {@code <ListItem>}.
 *
 * <pre>
 * FxListItem (HBox, .fxk-list-item)
 * ├─ marker  bullet / number (Label, .fxk-list-marker)  OR  icon (FontIcon, .fxk-list-icon)
 * └─ body (VBox, .fxk-list-item-body)
 *     ├─ text label (.fxk-list-item-text)   only when {@link #textProperty() text} is not blank
 *     └─ content (any Node)                 only when {@link #contentProperty() content} is set
 * </pre>
 *
 * <p>{@code text} and {@code content} are <b>additive</b>, not alternatives: the text is shown first and
 * the content sits below it. That is what makes the Flowbite "nested" example work (text
 * {@code "List item one"} plus a nested {@link FxList} as content), and an item with no text at all
 * and a custom row as content is the "advanced layout" example.
 *
 * <p>Which marker is shown is decided by the owning {@link FxList}, never by the item itself:
 * an icon (the item's own, else the list's) always wins; otherwise the list shows a bullet, a number
 * or nothing, depending on its {@code ordered}, {@code unstyled} and {@code horizontal} flags. An item
 * that is not inside a list has no bullet or number, only an icon if it has one.
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * icon (IconType)     icon                 (an Ikonli {@link Ikon}; 14px, {@code h-3.5 w-3.5})
 * children            text / content
 * item.withIcon.on    "flex items-center"  -> .fxk-list-item-with-icon centers marker and body
 * item.icon           "me-2"               -> 8px gap between marker and body
 * </pre>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * FxListItem plain = new FxListItem("At least one lowercase character");
 *
 * FxListItem withIcon = new FxListItem("Inclusion of at least one special character");
 * withIcon.setIcon(MaterialDesignC.CHECK_CIRCLE);
 *
 * FxList nested = FxList.of("A", "B");
 * nested.setNested(true);
 * FxListItem parent = new FxListItem("List item one", nested);
 * }</pre>
 */
@DefaultProperty("content")
public class FxListItem extends HBox {

    /** What the owning list wants shown in front of the body when the item has no icon. */
    enum Marker { NONE, BULLET, NUMBER }

    private static final String BULLET = "\u2022";

    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");

    private final VBox body = new VBox();
    private final Label textLabel = new Label();
    private final Label markerLabel = new Label();
    private final FontIcon iconView = new FontIcon();

    // Set by the owning FxList through configure(); defaults mean "not in a list".
    private Marker marker = Marker.NONE;
    private int number = 1;
    private Ikon inheritedIcon;

    public FxListItem() {
        getStyleClass().add("fxk-list-item");
        body.getStyleClass().add("fxk-list-item-body");
        textLabel.getStyleClass().add("fxk-list-item-text");
        markerLabel.getStyleClass().add("fxk-list-marker");
        iconView.getStyleClass().add("fxk-list-icon");

        textLabel.setWrapText(true);
        body.setMinWidth(0); // lets a wrapping label shrink instead of forcing the row wider
        HBox.setHgrow(body, Priority.ALWAYS);

        text.addListener(o -> refreshBody());
        content.addListener(o -> refreshBody());
        icon.addListener(o -> refreshMarker());

        refreshBody();
        refreshMarker();
    }

    /** @param text the item's text; may be {@code null} when only {@link #setContent(Node) content} is used */
    public FxListItem(String text) {
        this();
        setText(text);
    }

    /**
     * @param text    the item's text; may be {@code null}
     * @param content shown below the text (for example a nested {@link FxList}); may be {@code null}
     */
    public FxListItem(String text, Node content) {
        this(text);
        setContent(content);
    }

    // ---- called by FxList -----------------------------------------------------------------

    /**
     * Applies the owning list's current state to this item. Package-private: only {@link FxList}
     * decides markers, numbering and position, so an item never needs to know its list.
     */
    void configure(Marker marker, int number, Ikon inheritedIcon, boolean first, boolean last) {
        this.marker = marker;
        this.number = number;
        this.inheritedIcon = inheritedIcon;
        toggleClass(this, "fxk-list-item-first", first);
        toggleClass(this, "fxk-list-item-last", last);
        refreshMarker();
    }

    // ---- internals ------------------------------------------------------------------------

    private void refreshBody() {
        body.getChildren().clear();
        String t = getText();
        if (t != null && !t.isBlank()) {
            textLabel.setText(t);
            body.getChildren().add(textLabel);
        }
        Node c = getContent();
        if (c != null) {
            body.getChildren().add(c);
        }
    }

    private void refreshMarker() {
        Ikon effective = getIcon() != null ? getIcon() : inheritedIcon;

        Node shown = null;
        if (effective != null) {
            iconView.setIconCode(effective);
            shown = iconView;
        } else if (marker == Marker.BULLET) {
            markerLabel.setText(BULLET);
            shown = markerLabel;
        } else if (marker == Marker.NUMBER) {
            markerLabel.setText(number + ".");
            shown = markerLabel;
        }

        toggleClass(markerLabel, "fxk-list-marker-bullet", shown == markerLabel && marker == Marker.BULLET);
        toggleClass(markerLabel, "fxk-list-marker-number", shown == markerLabel && marker == Marker.NUMBER);
        toggleClass(this, "fxk-list-item-with-icon", effective != null);

        if (shown == null) {
            getChildren().setAll(body);
        } else {
            getChildren().setAll(shown, body);
        }
    }

    /** Adds or removes {@code styleClass} without ever duplicating it. */
    static void toggleClass(Node node, String styleClass, boolean on) {
        var classes = node.getStyleClass();
        if (on) {
            if (!classes.contains(styleClass)) {
                classes.add(styleClass);
            }
        } else {
            classes.remove(styleClass);
        }
    }

    // ---- properties -----------------------------------------------------------------------

    /** The item's text. Blank or {@code null} means no text label at all (so only {@link #contentProperty() content} shows). */
    public final StringProperty textProperty() { return text; }
    public final String getText() { return text.get(); }
    public final void setText(String value) { text.set(value); }

    /**
     * Node shown below the text, for example a nested {@link FxList} or a custom row. Unlike
     * {@code FxAlert}'s content it does not replace the text, it adds to it.
     */
    public final ObjectProperty<Node> contentProperty() { return content; }
    public final Node getContent() { return content.get(); }
    public final void setContent(Node value) { content.set(value); }

    /**
     * This item's own icon, shown instead of a bullet or number (Flowbite's {@code icon}). When
     * {@code null}, the owning {@link FxList#iconProperty() list icon} is used, if any.
     */
    public final ObjectProperty<Ikon> iconProperty() { return icon; }
    public final Ikon getIcon() { return icon.get(); }
    public final void setIcon(Ikon value) { icon.set(value); }
}
