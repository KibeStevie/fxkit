package dev.fxkit.core.components.sidebar;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.components.badge.FxBadge;
import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * One navigation entry in an {@link FxSidebar}, mirroring Flowbite React's {@code <SidebarItem>}:
 * {@code [icon] text [label badge]}.
 *
 * <h2>href becomes onAction</h2>
 * A desktop app has no URLs, so Flowbite's {@code href} becomes {@link #onActionProperty()
 * onAction}. The item is focusable and fires on a click or on Enter/Space, like a button.
 *
 * <h2>Label</h2>
 * {@link #labelProperty() label} shows a small {@link FxBadge} at the end of the row (Flowbite's
 * {@code label}); {@link #labelColorProperty() labelColor} picks its color (default
 * {@link FxBadge.Color#INFO}, like Flowbite).
 *
 * <h2>Active</h2>
 * {@link #activeProperty()} highlights the item as the current page. Flowbite derives this from the
 * router; here you set it yourself (see the showcase for a one-line way to keep exactly one active).
 *
 * <h2>Collapsed sidebar</h2>
 * Inside a collapsed {@link FxSidebar} the item shows only its icon (an item without an icon shows
 * its first letter in bold), hides the label badge, and gets a tooltip with its text.
 * {@link #collapsedProperty()} is read-only: it follows the sidebar.
 *
 * <h2>Inside a collapse</h2>
 * Put items in {@link FxSidebarCollapse#getContent()} and they indent automatically.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxSidebarItem kanban = new FxSidebarItem("Kanban", SomeIconPack.VIEW_BOARDS);
 * kanban.setLabel("Pro");
 * kanban.setLabelColor(FxBadge.Color.DARK);
 * kanban.setOnAction(e -> showKanban());
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxSidebarItem text="Inbox" label="3" onAction="#openInbox"/>
 * }</pre>
 */
public class FxSidebarItem extends HBox {

    public static final String STYLE_CLASS = "fxk-sidebar-item";

    private static final String ACTIVE_STYLE_CLASS = "fxk-sidebar-item-active";
    private static final String COLLAPSED_STYLE_CLASS = "fxk-sidebar-item-collapsed";
    private static final String INITIAL_STYLE_CLASS = "fxk-sidebar-item-initial";
    private static final String ICON_BOX_STYLE_CLASS = "fxk-sidebar-icon-box";
    private static final String ICON_STYLE_CLASS = "fxk-sidebar-item-icon";
    private static final String TEXT_STYLE_CLASS = "fxk-sidebar-item-text";

    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");
    private final StringProperty label = new SimpleStringProperty(this, "label");
    private final ObjectProperty<FxBadge.Color> labelColor =
            new SimpleObjectProperty<>(this, "labelColor", FxBadge.DEFAULT_COLOR);
    private final BooleanProperty active = new SimpleBooleanProperty(this, "active", false);
    private final ObjectProperty<EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction");
    private final ReadOnlyBooleanWrapper collapsed = new ReadOnlyBooleanWrapper(this, "collapsed", false);

    private final StackPane iconBox = new StackPane();
    private final FontIcon iconNode = new FontIcon();
    private final Label textNode = new Label();
    private final FxBadge badge = new FxBadge();

    private Tooltip tooltip;
    private boolean tooltipInstalled;

    public FxSidebarItem() {
        initialize();
    }

    public FxSidebarItem(String text) {
        this();
        setText(text);
    }

    public FxSidebarItem(String text, Ikon icon) {
        this(text);
        setIcon(icon);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.BUTTON);

        iconNode.getStyleClass().add(ICON_STYLE_CLASS);
        iconBox.getStyleClass().add(ICON_BOX_STYLE_CLASS);
        iconBox.getChildren().add(iconNode);

        textNode.getStyleClass().add(TEXT_STYLE_CLASS);
        textNode.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textNode, Priority.ALWAYS); // flex-1

        badge.textProperty().bind(label);
        badge.colorProperty().bind(labelColor);

        getChildren().addAll(iconBox, textNode, badge);
        setAlignment(Pos.CENTER_LEFT);

        BooleanStyleClassSync.sync(this, ACTIVE_STYLE_CLASS, active);
        BooleanStyleClassSync.sync(this, COLLAPSED_STYLE_CLASS, collapsed.getReadOnlyProperty());

        text.addListener((observable, oldValue, newValue) -> refresh());
        icon.addListener((observable, oldValue, newValue) -> refresh());
        label.addListener((observable, oldValue, newValue) -> refresh());
        collapsed.addListener((observable, oldValue, newValue) -> refresh());

        onAction.addListener((observable, oldHandler, newHandler) -> {
            if (oldHandler != null) {
                removeEventHandler(ActionEvent.ACTION, oldHandler);
            }
            if (newHandler != null) {
                addEventHandler(ActionEvent.ACTION, newHandler);
            }
        });

        SidebarSupport.onActivate(this, this::fire);
        SidebarSupport.bindCollapsed(this, collapsed);
        refresh();
    }

    /** Rebuilds what is visible from the current properties and the sidebar's collapsed state. */
    private void refresh() {
        Ikon currentIcon = icon.get();
        String currentText = text.get();
        boolean hasIcon = currentIcon != null;
        boolean hasText = currentText != null && !currentText.isEmpty();
        boolean isCollapsed = collapsed.get();
        boolean initialOnly = isCollapsed && !hasIcon && hasText;

        if (hasIcon) {
            iconNode.setIconCode(currentIcon);
        }
        SidebarSupport.setShown(iconBox, hasIcon);

        textNode.setText(initialOnly ? SidebarSupport.initial(currentText) : currentText);
        SidebarSupport.setShown(textNode, hasText && (!isCollapsed || !hasIcon));
        SidebarSupport.setStyleClassPresent(this, INITIAL_STYLE_CLASS, initialOnly);

        String currentLabel = label.get();
        SidebarSupport.setShown(badge, currentLabel != null && !currentLabel.isBlank() && !isCollapsed);

        setAccessibleText(currentText);
        updateTooltip(isCollapsed && hasText, currentText);
    }

    private void updateTooltip(boolean wanted, String content) {
        if (wanted) {
            if (tooltip == null) {
                tooltip = new Tooltip();
                tooltip.setShowDelay(Duration.millis(300));
            }
            tooltip.setText(content);
            if (!tooltipInstalled) {
                Tooltip.install(this, tooltip);
                tooltipInstalled = true;
            }
        } else if (tooltipInstalled) {
            Tooltip.uninstall(this, tooltip);
            tooltipInstalled = false;
        }
    }

    /** Fires an {@link ActionEvent}, unless the item is disabled. Called on click and Enter/Space. */
    public void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    public String getText() { return text.get(); }
    public void setText(String text) { this.text.set(text); }
    public StringProperty textProperty() { return text; }

    public Ikon getIcon() { return icon.get(); }
    public void setIcon(Ikon icon) { this.icon.set(icon); }
    public ObjectProperty<Ikon> iconProperty() { return icon; }

    /** @return the badge text shown at the end of the row, or {@code null}/blank for none */
    public String getLabel() { return label.get(); }
    public void setLabel(String label) { this.label.set(label); }
    public StringProperty labelProperty() { return label; }

    public FxBadge.Color getLabelColor() { return labelColor.get(); }
    public void setLabelColor(FxBadge.Color labelColor) { this.labelColor.set(labelColor); }
    public ObjectProperty<FxBadge.Color> labelColorProperty() { return labelColor; }

    public boolean isActive() { return active.get(); }

    /** Highlights the item as the current page. */
    public void setActive(boolean active) { this.active.set(active); }
    public BooleanProperty activeProperty() { return active; }

    public EventHandler<ActionEvent> getOnAction() { return onAction.get(); }
    public void setOnAction(EventHandler<ActionEvent> onAction) { this.onAction.set(onAction); }
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() { return onAction; }

    /** @return whether the enclosing {@link FxSidebar} is collapsed ({@code false} outside a sidebar) */
    public boolean isCollapsed() { return collapsed.get(); }
    public ReadOnlyBooleanProperty collapsedProperty() { return collapsed.getReadOnlyProperty(); }
}
