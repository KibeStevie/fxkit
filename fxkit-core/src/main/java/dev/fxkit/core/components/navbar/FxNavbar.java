package dev.fxkit.core.components.navbar;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A top navigation bar: brand, links, and optional actions. Port of Flowbite React's
 * {@code <Navbar>}.
 *
 * <pre>
 * FxNavbar (VBox, .fxk-navbar)
 * └─ inner (VBox, .fxk-navbar-inner)          centered; max width = Tailwind "container" unless fluid
 *     ├─ bar (HBox, .fxk-navbar-bar)
 *     │   ├─ brand
 *     │   ├─ links (HBox, .fxk-navbar-links)       only while wide
 *     │   └─ actions (HBox, .fxk-navbar-actions)   your actions, then the toggle while compact
 *     └─ stacked links (VBox, .fxk-navbar-links-compact)   only while compact AND expanded
 * </pre>
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * &lt;NavbarBrand&gt;      brand            (an {@link FxNavbarBrand}, or any node)
 * &lt;NavbarLink&gt;       getLinks()       ({@link FxNavbarLink}s, or any node)
 * &lt;NavbarToggle&gt;     built in         ({@link #getToggle()}), shown only while compact
 * md:order-2 div     getActions()     (CTA button, avatar dropdown, ...)
 * fluid / rounded / bordered   properties of the same name
 * </pre>
 *
 * <h2>Responsive behavior</h2>
 * JavaFX has no media queries, so the navbar measures <em>its own width</em>:
 * <ul>
 *   <li>below {@value #MD_BREAKPOINT}px it is {@link #compactProperty() compact}: the links are hidden
 *       behind the hamburger toggle and stack below the bar when {@link #expandedProperty() expanded}
 *       (Flowbite's {@code md:} breakpoint);</li>
 *   <li>below {@value #SM_BREAKPOINT}px side padding shrinks from 16px to 8px (Flowbite's {@code sm:});</li>
 *   <li>unless {@link #fluidProperty() fluid}, the content is centered with Tailwind's
 *       {@code container} max widths (640, 768, 1024, 1280, 1536).</li>
 * </ul>
 * Give the navbar a width from its parent (for example the {@code top} of a {@code BorderPane}, or a
 * {@code VBox} child). A parent that sizes itself <em>from</em> the navbar's preferred width can make
 * the navbar flip between the two layouts.
 *
 * <h2>Default navbar</h2>
 * <pre>{@code
 * FxNavbar navbar = new FxNavbar(new FxNavbarBrand("Flowbite React", logo));
 * navbar.setFluid(true);
 * navbar.setRounded(true);
 * navbar.getLinks().addAll(
 *         new FxNavbarLink("Home", true),
 *         new FxNavbarLink("About"),
 *         new FxNavbarLink("Services"),
 *         new FxNavbarLink("Pricing"),
 *         new FxNavbarLink("Contact"));
 * }</pre>
 *
 * <h2>With a CTA button</h2>
 * <pre>{@code
 * navbar.getActions().add(new FxButton("Get started"));
 * }</pre>
 *
 * <h2>With an avatar dropdown</h2>
 * <pre>{@code
 * FxDropdown user = new FxDropdown();
 * user.setInline(true);
 * user.setArrowIcon(false);                      // set this BEFORE the graphic: it owns the graphic
 * user.setGraphic(new FxAvatar("/images/people/profile-picture-5.jpg"));
 * user.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
 * user.getItems().addAll(
 *         new FxDropdownHeader("Bonnie Green", "name@flowbite.com"),
 *         new FxDropdownItem("Dashboard"),
 *         new FxDropdownItem("Settings"),
 *         new FxDropdownItem("Earnings"),
 *         new FxDropdownDivider(),
 *         new FxDropdownItem("Sign out"));
 * navbar.getActions().add(user);
 * }</pre>
 *
 * <p>The looks live in {@code components.css} (the FxNavbar block).
 */
@DefaultProperty("links")
public class FxNavbar extends VBox {

    /** Width below which the navbar is {@link #compactProperty() compact} (Tailwind {@code md}). */
    public static final double MD_BREAKPOINT = 768;

    /** Width below which the side padding shrinks (Tailwind {@code sm}). */
    public static final double SM_BREAKPOINT = 640;

    /** Tailwind's {@code container} max widths: sm, md, lg, xl, 2xl. */
    private static final double[] CONTAINER_WIDTHS = {640, 768, 1024, 1280, 1536};

    // ---- properties ----------------------------------------------------------------------------

    private final ObjectProperty<Node> brand = new SimpleObjectProperty<>(this, "brand");
    private final ObservableList<Node> links = FXCollections.observableArrayList();
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    private final BooleanProperty fluid = new SimpleBooleanProperty(this, "fluid", false);
    private final BooleanProperty rounded = new SimpleBooleanProperty(this, "rounded", false);
    private final BooleanProperty bordered = new SimpleBooleanProperty(this, "bordered", false);
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);
    private final ReadOnlyBooleanWrapper compact = new ReadOnlyBooleanWrapper(this, "compact", false);
    private final ReadOnlyBooleanWrapper narrow = new ReadOnlyBooleanWrapper(this, "narrow", false);

    // ---- parts ---------------------------------------------------------------------------------

    private final VBox inner = new VBox();
    private final HBox bar = new HBox();
    private final HBox wideLinks = new HBox();
    private final VBox compactLinks = new VBox();
    private final HBox actionsBox = new HBox();
    private final Region gapBefore = new Region();
    private final Region gapAfter = new Region();
    private final FxNavbarToggle toggle = new FxNavbarToggle();

    public FxNavbar() {
        this(null);
    }

    /**
     * @param brand the brand shown at the start, usually an {@link FxNavbarBrand}; may be {@code null}
     */
    public FxNavbar(Node brand) {
        getStyleClass().add("fxk-navbar");
        setAlignment(Pos.TOP_CENTER); // centers the max-width "container" inside
        inner.getStyleClass().add("fxk-navbar-inner");
        bar.getStyleClass().add("fxk-navbar-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        wideLinks.getStyleClass().add("fxk-navbar-links");
        compactLinks.getStyleClass().add("fxk-navbar-links-compact");
        actionsBox.getStyleClass().add("fxk-navbar-actions");

        // justify-between: the gaps share the leftover width equally.
        for (Region gap : new Region[] {gapBefore, gapAfter}) {
            gap.setMinWidth(0);
            HBox.setHgrow(gap, Priority.ALWAYS);
        }
        getChildren().add(inner);

        BooleanStyleClassSync.sync(this, "fxk-navbar-compact", compact);
        BooleanStyleClassSync.sync(this, "fxk-navbar-narrow", narrow);
        BooleanStyleClassSync.sync(this, "fxk-navbar-rounded", rounded);
        BooleanStyleClassSync.sync(this, "fxk-navbar-bordered", bordered);

        ListChangeListener<Node> rebuild = change -> refresh();
        links.addListener(rebuild);
        actions.addListener(rebuild);
        this.brand.addListener((observable, oldBrand, newBrand) -> refresh());
        compact.addListener((observable, wasCompact, isCompact) -> refresh());
        expanded.addListener((observable, wasExpanded, isExpanded) -> refresh());
        fluid.addListener((observable, wasFluid, isFluid) -> updateContainerWidth());
        widthProperty().addListener((observable, oldWidth, newWidth) -> updateBreakpoints());
        toggle.setOnAction(event -> setExpanded(!isExpanded()));

        setBrand(brand);
        refresh();
    }

    // ---- layout --------------------------------------------------------------------------------

    /** Rebuilds which nodes sit where for the current mode. Cheap: a handful of nodes. */
    private void refresh() {
        boolean isCompact = isCompact();
        boolean hasLinks = !links.isEmpty();
        boolean showWide = !isCompact && hasLinks;
        boolean showStacked = isCompact && hasLinks && isExpanded();
        boolean showToggle = isCompact && hasLinks; // a toggle with nothing to toggle is just noise

        // A node has one parent: empty both link containers before filling the right one.
        wideLinks.getChildren().clear();
        compactLinks.getChildren().clear();
        if (showWide) {
            wideLinks.getChildren().setAll(links);
        }
        if (showStacked) {
            compactLinks.getChildren().setAll(links);
        }

        List<Node> actionNodes = new ArrayList<>(actions);
        if (showToggle) {
            actionNodes.add(toggle);
        }
        actionsBox.getChildren().setAll(actionNodes);

        List<Node> barNodes = new ArrayList<>();
        if (getBrand() != null) {
            barNodes.add(getBrand());
        }
        barNodes.add(gapBefore);
        if (showWide) {
            barNodes.add(wideLinks);
        }
        if (showWide && !actionNodes.isEmpty()) {
            barNodes.add(gapAfter);
        }
        if (!actionNodes.isEmpty()) {
            barNodes.add(actionsBox);
        }
        bar.getChildren().setAll(barNodes);

        if (showStacked) {
            inner.getChildren().setAll(bar, compactLinks);
        } else {
            inner.getChildren().setAll(bar);
        }
    }

    private void updateBreakpoints() {
        double width = getWidth();
        if (width <= 0) {
            return; // not laid out yet: keep the wide layout rather than flashing the compact one
        }
        compact.set(width < MD_BREAKPOINT);
        narrow.set(width < SM_BREAKPOINT);
        updateContainerWidth();
    }

    /** Tailwind's {@code container}: the widest breakpoint that fits, or full width below the first. */
    private void updateContainerWidth() {
        double max = Double.MAX_VALUE;
        if (!isFluid()) {
            double width = getWidth();
            for (double breakpoint : CONTAINER_WIDTHS) {
                if (width >= breakpoint) {
                    max = breakpoint;
                }
            }
        }
        inner.setMaxWidth(max);
    }

    // ---- properties ----------------------------------------------------------------------------

    /**
     * @return the brand at the start of the bar (Flowbite's {@code <NavbarBrand>}); may be {@code null}
     */
    public final ObjectProperty<Node> brandProperty() {
        return brand;
    }

    public final Node getBrand() {
        return brand.get();
    }

    public final void setBrand(Node brand) {
        this.brand.set(brand);
    }

    /**
     * @return the live list of links (Flowbite's {@code <NavbarLink>}s), shown inline while wide and
     *         stacked behind the toggle while compact
     */
    public final ObservableList<Node> getLinks() {
        return links;
    }

    /**
     * @return the live list of actions at the end of the bar (a CTA button, a user dropdown, ...);
     *         they stay visible in both layouts, the toggle is added after them while compact
     */
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /**
     * @return the built-in hamburger button, for example to change its accessible text
     */
    public final FxNavbarToggle getToggle() {
        return toggle;
    }

    /**
     * @return {@code true} to let the content span the full width; the default {@code false} centers it
     *         with Tailwind's {@code container} max widths
     */
    public final BooleanProperty fluidProperty() {
        return fluid;
    }

    public final boolean isFluid() {
        return fluid.get();
    }

    public final void setFluid(boolean fluid) {
        this.fluid.set(fluid);
    }

    /**
     * @return whether the navbar has rounded corners (4px)
     */
    public final BooleanProperty roundedProperty() {
        return rounded;
    }

    public final boolean isRounded() {
        return rounded.get();
    }

    public final void setRounded(boolean rounded) {
        this.rounded.set(rounded);
    }

    /**
     * @return whether the navbar has a 1px border
     */
    public final BooleanProperty borderedProperty() {
        return bordered;
    }

    public final boolean isBordered() {
        return bordered.get();
    }

    public final void setBordered(boolean bordered) {
        this.bordered.set(bordered);
    }

    /**
     * @return whether the stacked links are open while {@link #compactProperty() compact}; toggled by
     *         the hamburger button. It has no visible effect while wide, and keeps its value when the
     *         navbar changes layout.
     */
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    public final boolean isExpanded() {
        return expanded.get();
    }

    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /**
     * @return whether the navbar is narrower than {@value #MD_BREAKPOINT}px and therefore shows the
     *         hamburger toggle instead of inline links
     */
    public final ReadOnlyBooleanProperty compactProperty() {
        return compact.getReadOnlyProperty();
    }

    public final boolean isCompact() {
        return compact.get();
    }
}