package dev.fxkit.core.components.sidebar;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A vertical navigation sidebar styled by FXKit's design tokens, mirroring Flowbite React's
 * {@code <Sidebar>}. It is the container; what goes inside are the other sidebar components:
 *
 * <ul>
 *   <li>{@link FxSidebarLogo} - branding at the top</li>
 *   <li>{@link FxSidebarItemGroup} - a group of entries; consecutive groups get a divider line</li>
 *   <li>{@link FxSidebarItem} - one navigation entry (icon, text, optional badge)</li>
 *   <li>{@link FxSidebarCollapse} - an entry that expands to show nested items</li>
 *   <li>{@link FxSidebarCta} - a call-to-action box</li>
 * </ul>
 *
 * <p>Flowbite's {@code <SidebarItems>} wrapper has no styling of its own, so it does not exist here:
 * put {@link FxSidebarItemGroup}s directly into {@link #getContent()}.
 *
 * <h2>Collapsed</h2>
 * {@link #collapsedProperty()} switches the sidebar from 256px to a 64px icon rail. Descendants find
 * out on their own: items and collapses hide their text (an item without an icon shows its first
 * letter in bold), badges and chevrons disappear, the logo drops its text, and each entry gets a
 * tooltip with its text. Nothing needs to be wired up by hand.
 *
 * <h2>Scrolling</h2>
 * The content sits in a vertical {@link ScrollPane} (Flowbite's {@code overflow-y-auto}), so a tall
 * menu scrolls instead of being clipped. The sidebar fills the height its parent gives it
 * ({@code h-full}).
 *
 * <p>Set {@link #setAccessibleText(String)} to describe the sidebar to screen readers (Flowbite's
 * {@code aria-label}).
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxSidebarItem dashboard = new FxSidebarItem("Dashboard", SomeIconPack.CHART_PIE);
 * FxSidebarItem inbox = new FxSidebarItem("Inbox", SomeIconPack.INBOX);
 * inbox.setLabel("3");
 *
 * FxSidebar sidebar = new FxSidebar(
 *         new FxSidebarLogo("Acme"),
 *         new FxSidebarItemGroup(dashboard, inbox));
 * sidebar.setAccessibleText("Main navigation");
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxSidebar accessibleText="Main navigation">
 *     <FxSidebarItemGroup>
 *         <FxSidebarItem text="Dashboard"/>
 *         <FxSidebarItem text="Inbox" label="3"/>
 *     </FxSidebarItemGroup>
 * </FxSidebar>
 * }</pre>
 */
@DefaultProperty("content")
public class FxSidebar extends VBox {

    public static final String STYLE_CLASS = "fxk-sidebar";

    private static final String COLLAPSED_STYLE_CLASS = "fxk-sidebar-collapsed";
    private static final String INNER_STYLE_CLASS = "fxk-sidebar-inner";
    private static final String SCROLL_STYLE_CLASS = "fxk-sidebar-scroll";
    private static final String FIRST_GROUP_STYLE_CLASS = "fxk-sidebar-group-first";

    private final BooleanProperty collapsed = new SimpleBooleanProperty(this, "collapsed", false);

    private final VBox inner = new VBox();
    private final ScrollPane scroll = new ScrollPane(inner);

    public FxSidebar() {
        initialize();
    }

    /** Convenience constructor adding {@code content} in order. */
    public FxSidebar(Node... content) {
        this();
        getContent().addAll(content);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        inner.getStyleClass().add(INNER_STYLE_CLASS);
        scroll.getStyleClass().add(SCROLL_STYLE_CLASS);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);

        // h-full: take whatever height the parent offers; width is fixed by CSS (w-64 / w-16).
        setMaxHeight(Double.MAX_VALUE);

        BooleanStyleClassSync.sync(this, COLLAPSED_STYLE_CLASS, collapsed);

        inner.getChildren().addListener((ListChangeListener<Node>) change -> markFirstGroup());
        markFirstGroup();
    }

    /**
     * The first group has no divider and no top gap (Flowbite's {@code first:mt-0 first:border-t-0
     * first:pt-0}); JavaFX CSS has no {@code :first-child}, so the class is kept here.
     */
    private void markFirstGroup() {
        boolean seenGroup = false;
        for (Node child : inner.getChildren()) {
            if (child instanceof FxSidebarItemGroup) {
                SidebarSupport.setStyleClassPresent(child, FIRST_GROUP_STYLE_CLASS, !seenGroup);
                seenGroup = true;
            }
        }
    }

    /**
     * @return the sidebar's children in order (logo, groups, CTA, ...); this is also the FXML default
     *         property. {@link FxSidebarItemGroup}s must be direct children to get their dividers.
     */
    public ObservableList<Node> getContent() {
        return inner.getChildren();
    }

    public boolean isCollapsed() { return collapsed.get(); }

    /**
     * Collapses the sidebar to a 64px icon rail ({@code true}) or expands it to 256px ({@code false}).
     * Items, collapses and the logo react automatically.
     */
    public void setCollapsed(boolean collapsed) { this.collapsed.set(collapsed); }

    public BooleanProperty collapsedProperty() { return collapsed; }

    /** Flips {@link #collapsedProperty()}; convenient for a hamburger button's action. */
    public void toggleCollapsed() { collapsed.set(!collapsed.get()); }
}
