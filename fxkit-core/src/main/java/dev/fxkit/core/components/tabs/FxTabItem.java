package dev.fxkit.core.components.tabs;

import org.kordamp.ikonli.Ikon;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/**
 * One tab of an {@link FxTabs}, modelled on Flowbite React's {@code <TabItem>}: a title (with an
 * optional icon) shown in the tab strip, and the content shown while the tab is active.
 *
 * <pre>{@code
 * FxTabItem profile = new FxTabItem("Profile", Devicons.JAVA, new Label("Profile content"));
 * FxTabItem disabled = new FxTabItem("Disabled");
 * disabled.setDisabled(true);
 *
 * FxTabs tabs = new FxTabs();
 * tabs.getItems().addAll(profile, disabled);
 * }</pre>
 *
 * <p>An {@code FxTabItem} is a plain model object, not a {@link Node}: {@link FxTabs} builds the
 * tab button and the panel from it and keeps them in sync with its properties, so every property
 * can be changed at runtime. An item belongs to at most one {@code FxTabs}, and appears in it once.
 *
 * <h2>Flowbite mapping</h2>
 * <table>
 * <caption>React prop to FxTabItem property</caption>
 * <tr><td>{@code title}</td><td>{@link #titleProperty()}</td></tr>
 * <tr><td>{@code icon}</td><td>{@link #iconProperty()}, an Ikonli {@link Ikon}</td></tr>
 * <tr><td>{@code children}</td><td>{@link #contentProperty()}, any {@link Node}</td></tr>
 * <tr><td>{@code active}</td><td>{@link #activeProperty()}</td></tr>
 * <tr><td>{@code disabled}</td><td>{@link #disabledProperty()}</td></tr>
 * </table>
 */
public class FxTabItem {

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", false);
    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    public FxTabItem() {
        this(null, null, null);
    }

    /**
     * @param title the text of the tab
     */
    public FxTabItem(String title) {
        this(title, null, null);
    }

    /**
     * @param title   the text of the tab
     * @param content what is shown while the tab is active
     */
    public FxTabItem(String title, Node content) {
        this(title, null, content);
    }

    /**
     * @param title   the text of the tab
     * @param icon    the icon before the title, or {@code null}
     * @param content what is shown while the tab is active
     */
    public FxTabItem(String title, Ikon icon, Node content) {
        setTitle(title);
        setIcon(icon);
        setContent(content);
    }

    /** The text of the tab. */
    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String text) {
        title.set(text);
    }

    /** The icon shown before the title (20px), or {@code null} for none. */
    public ObjectProperty<Ikon> iconProperty() {
        return icon;
    }

    public Ikon getIcon() {
        return icon.get();
    }

    public void setIcon(Ikon value) {
        icon.set(value);
    }

    /**
     * What is shown below the tab strip while this tab is active; {@code null} for an empty panel.
     * Replacing it while the tab is active swaps the panel immediately.
     */
    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node node) {
        content.set(node);
    }

    /**
     * Whether this is the active tab. Set it to {@code true} <em>before</em> adding the item to make
     * it the tab shown first (Flowbite's {@code active} prop); once the item is in an
     * {@link FxTabs}, this property simply mirrors the selection, and setting it to {@code true}
     * selects the tab. The first {@code active} item added wins; setting it to {@code false} on the
     * selected tab has no effect (select another tab instead).
     */
    public BooleanProperty activeProperty() {
        return active;
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean value) {
        active.set(value);
    }

    /** A disabled tab is dimmed and cannot be clicked or reached with the arrow keys. */
    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public void setDisabled(boolean value) {
        disabled.set(value);
    }
}
