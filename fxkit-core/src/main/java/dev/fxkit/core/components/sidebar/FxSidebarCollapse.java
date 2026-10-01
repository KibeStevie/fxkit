package dev.fxkit.core.components.sidebar;

import java.util.function.Function;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * An entry that expands to reveal nested items, mirroring Flowbite React's
 * {@code <SidebarCollapse>} (the "multi-level dropdown").
 *
 * <p>The header reads {@code [icon] label [chevron]}; clicking it, or pressing Enter/Space on it,
 * toggles {@link #openProperty()}. The nested items live in {@link #getContent()} (the FXML default
 * property) and are indented automatically.
 *
 * <h2>Custom chevron</h2>
 * Flowbite offers two levels of customization, and so does this class:
 * <ul>
 *   <li>{@link #chevronIconProperty() chevronIcon} (Flowbite's {@code chevronIcon}) swaps the
 *       arrow for any Ikon. It still rotates 180&deg; when open, like the default.</li>
 *   <li>{@link #chevronFactoryProperty() chevronFactory} (Flowbite's {@code renderChevronIcon})
 *       builds the node itself from the {@code open} state, for cases like plus/minus where the
 *       glyph changes instead of rotating. It wins over {@code chevronIcon}, and the node you
 *       return is not rotated.</li>
 * </ul>
 *
 * <h2>Collapsed sidebar</h2>
 * In a collapsed {@link FxSidebar} the header shows only its icon (or the label's first letter if it
 * has no icon), the chevron is hidden, and a tooltip shows the label. Nested items stay toggleable
 * and show their own collapsed look.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxSidebarCollapse shop = new FxSidebarCollapse("E-commerce", SomeIconPack.SHOPPING_BAG);
 * shop.getContent().addAll(
 *         new FxSidebarItem("Products"),
 *         new FxSidebarItem("Sales"));
 *
 * // plus/minus instead of a rotating arrow
 * shop.setChevronFactory(open -> new FontIcon(open ? SomeIconPack.MINUS : SomeIconPack.PLUS));
 * }</pre>
 */
@DefaultProperty("content")
public class FxSidebarCollapse extends VBox {

    public static final String STYLE_CLASS = "fxk-sidebar-collapse";

    private static final String OPEN_STYLE_CLASS = "fxk-sidebar-collapse-open";
    private static final String COLLAPSED_STYLE_CLASS = "fxk-sidebar-collapse-collapsed";
    private static final String HEADER_STYLE_CLASS = "fxk-sidebar-collapse-button";
    private static final String INITIAL_STYLE_CLASS = "fxk-sidebar-collapse-initial";
    private static final String ICON_BOX_STYLE_CLASS = "fxk-sidebar-icon-box";
    private static final String ICON_STYLE_CLASS = "fxk-sidebar-collapse-icon";
    private static final String LABEL_STYLE_CLASS = "fxk-sidebar-collapse-label";
    private static final String CHEVRON_BOX_STYLE_CLASS = "fxk-sidebar-chevron-box";
    private static final String CHEVRON_ROTATING_STYLE_CLASS = "fxk-sidebar-chevron-rotating";
    private static final String CHEVRON_STYLE_CLASS = "fxk-sidebar-chevron";
    private static final String CHEVRON_ICON_STYLE_CLASS = "fxk-sidebar-chevron-icon";
    private static final String LIST_STYLE_CLASS = "fxk-sidebar-collapse-list";

    /** Heroicons "chevron-down" (outline, 24x24), the same arrow Flowbite uses. */
    private static final String CHEVRON_PATH = "M19 9l-7 7-7-7";

    private final StringProperty label = new SimpleStringProperty(this, "label");
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(this, "icon");
    private final BooleanProperty open = new SimpleBooleanProperty(this, "open", false);
    private final ObjectProperty<Ikon> chevronIcon = new SimpleObjectProperty<>(this, "chevronIcon");
    private final ObjectProperty<Function<Boolean, Node>> chevronFactory =
            new SimpleObjectProperty<>(this, "chevronFactory");
    private final ReadOnlyBooleanWrapper collapsed = new ReadOnlyBooleanWrapper(this, "collapsed", false);

    private final HBox header = new HBox();
    private final StackPane iconBox = new StackPane();
    private final FontIcon iconNode = new FontIcon();
    private final Label labelNode = new Label();
    private final StackPane chevronBox = new StackPane();
    private final VBox list = new VBox();

    private Tooltip tooltip;
    private boolean tooltipInstalled;

    public FxSidebarCollapse() {
        initialize();
    }

    public FxSidebarCollapse(String label) {
        this();
        setLabel(label);
    }

    public FxSidebarCollapse(String label, Ikon icon) {
        this(label);
        setIcon(icon);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);

        header.getStyleClass().add(HEADER_STYLE_CLASS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setFocusTraversable(true);
        header.setAccessibleRole(AccessibleRole.BUTTON);

        iconNode.getStyleClass().add(ICON_STYLE_CLASS);
        iconBox.getStyleClass().add(ICON_BOX_STYLE_CLASS);
        iconBox.getChildren().add(iconNode);

        labelNode.getStyleClass().add(LABEL_STYLE_CLASS);
        labelNode.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(labelNode, Priority.ALWAYS); // flex-1

        chevronBox.getStyleClass().add(CHEVRON_BOX_STYLE_CLASS);

        header.getChildren().addAll(iconBox, labelNode, chevronBox);
        list.getStyleClass().add(LIST_STYLE_CLASS);
        getChildren().addAll(header, list);

        BooleanStyleClassSync.sync(this, OPEN_STYLE_CLASS, open);
        BooleanStyleClassSync.sync(this, COLLAPSED_STYLE_CLASS, collapsed.getReadOnlyProperty());

        label.addListener((observable, oldValue, newValue) -> refreshHeader());
        icon.addListener((observable, oldValue, newValue) -> refreshHeader());
        collapsed.addListener((observable, oldValue, newValue) -> refreshHeader());
        open.addListener((observable, oldValue, newValue) -> {
            refreshList();
            refreshChevron(); // the factory receives the open state
        });
        chevronIcon.addListener((observable, oldValue, newValue) -> refreshChevron());
        chevronFactory.addListener((observable, oldValue, newValue) -> refreshChevron());

        SidebarSupport.onActivate(header, this::toggle);
        SidebarSupport.bindCollapsed(this, collapsed);

        refreshHeader();
        refreshList();
        refreshChevron();
    }

    private void refreshHeader() {
        Ikon currentIcon = icon.get();
        String currentLabel = label.get();
        boolean hasIcon = currentIcon != null;
        boolean hasLabel = currentLabel != null && !currentLabel.isEmpty();
        boolean isCollapsed = collapsed.get();
        boolean initialOnly = isCollapsed && !hasIcon && hasLabel;

        if (hasIcon) {
            iconNode.setIconCode(currentIcon);
        }
        SidebarSupport.setShown(iconBox, hasIcon);

        labelNode.setText(initialOnly ? SidebarSupport.initial(currentLabel) : currentLabel);
        SidebarSupport.setShown(labelNode, hasLabel && (!isCollapsed || !hasIcon));
        SidebarSupport.setShown(chevronBox, !isCollapsed);
        SidebarSupport.setStyleClassPresent(header, INITIAL_STYLE_CLASS, initialOnly);

        header.setAccessibleText(currentLabel);
        updateTooltip(isCollapsed && hasLabel, currentLabel);
    }

    private void refreshList() {
        SidebarSupport.setShown(list, open.get());
    }

    private void refreshChevron() {
        chevronBox.getChildren().clear();
        chevronBox.getStyleClass().remove(CHEVRON_ROTATING_STYLE_CLASS);

        Function<Boolean, Node> factory = chevronFactory.get();
        Ikon customIcon = chevronIcon.get();
        if (factory != null) {
            Node custom = factory.apply(open.get());
            if (custom != null) {
                chevronBox.getChildren().add(custom);
            }
        } else if (customIcon != null) {
            FontIcon chevron = new FontIcon(customIcon);
            chevron.getStyleClass().add(CHEVRON_ICON_STYLE_CLASS);
            chevronBox.getChildren().add(chevron);
            chevronBox.getStyleClass().add(CHEVRON_ROTATING_STYLE_CLASS);
        } else {
            SVGPath chevron = new SVGPath();
            chevron.setContent(CHEVRON_PATH);
            chevron.getStyleClass().add(CHEVRON_STYLE_CLASS);
            chevronBox.getChildren().add(chevron);
            chevronBox.getStyleClass().add(CHEVRON_ROTATING_STYLE_CLASS);
        }
    }

    private void updateTooltip(boolean wanted, String content) {
        if (wanted) {
            if (tooltip == null) {
                tooltip = new Tooltip();
                tooltip.setShowDelay(Duration.millis(300));
            }
            tooltip.setText(content);
            if (!tooltipInstalled) {
                Tooltip.install(header, tooltip);
                tooltipInstalled = true;
            }
        } else if (tooltipInstalled) {
            Tooltip.uninstall(header, tooltip);
            tooltipInstalled = false;
        }
    }

    /** Opens the collapse if it is closed, closes it if it is open. */
    public void toggle() {
        open.set(!open.get());
    }

    /** @return the nested items (the FXML default property); they are indented automatically */
    public ObservableList<Node> getContent() {
        return list.getChildren();
    }

    public String getLabel() { return label.get(); }
    public void setLabel(String label) { this.label.set(label); }
    public StringProperty labelProperty() { return label; }

    public Ikon getIcon() { return icon.get(); }
    public void setIcon(Ikon icon) { this.icon.set(icon); }
    public ObjectProperty<Ikon> iconProperty() { return icon; }

    public boolean isOpen() { return open.get(); }
    public void setOpen(boolean open) { this.open.set(open); }
    public BooleanProperty openProperty() { return open; }

    public Ikon getChevronIcon() { return chevronIcon.get(); }

    /** Replaces the default arrow with {@code chevronIcon}; it still rotates 180&deg; when open. */
    public void setChevronIcon(Ikon chevronIcon) { this.chevronIcon.set(chevronIcon); }
    public ObjectProperty<Ikon> chevronIconProperty() { return chevronIcon; }

    public Function<Boolean, Node> getChevronFactory() { return chevronFactory.get(); }

    /**
     * Flowbite's {@code renderChevronIcon}: builds the chevron node from the {@code open} state,
     * called again every time {@code open} changes. The returned node is not rotated. Overrides
     * {@link #chevronIconProperty()}; {@code null} goes back to it (or the default arrow).
     */
    public void setChevronFactory(Function<Boolean, Node> chevronFactory) {
        this.chevronFactory.set(chevronFactory);
    }
    public ObjectProperty<Function<Boolean, Node>> chevronFactoryProperty() { return chevronFactory; }

    /** @return whether the enclosing {@link FxSidebar} is collapsed ({@code false} outside a sidebar) */
    public boolean isCollapsed() { return collapsed.get(); }
    public ReadOnlyBooleanProperty collapsedProperty() { return collapsed.getReadOnlyProperty(); }
}
