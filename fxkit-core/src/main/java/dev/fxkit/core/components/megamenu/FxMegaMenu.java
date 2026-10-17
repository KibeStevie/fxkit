package dev.fxkit.core.components.megamenu;

import dev.fxkit.core.components.navbar.FxNavbar;
import javafx.beans.DefaultProperty;
import javafx.scene.Node;

/**
 * A navbar whose entries can open large panels of links: port of Flowbite React's
 * {@code <MegaMenu>}.
 *
 * <p>It <em>is</em> an {@link FxNavbar} (brand, links, actions, {@code fluid} / {@code rounded} /
 * {@code bordered}, the hamburger below 768px), plus two things: only one dropdown is open at a
 * time, and an open dropdown closes when the navbar changes layout or width, because its panel is
 * positioned for the old one.
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * &lt;MegaMenu&gt;                    FxMegaMenu
 * &lt;NavbarBrand&gt;/&lt;NavbarLink&gt;   FxNavbarBrand / FxNavbarLink (unchanged)
 * &lt;MegaMenuDropdownToggle&gt;    FxMegaMenuDropdown#getToggle(), added to getLinks()
 * &lt;MegaMenuDropdown&gt;          FxMegaMenuDropdown (content + fullWidth)
 * &lt;ul className="grid ..."&gt;   FxMegaMenuGrid
 * &lt;div className="space-y-4"&gt; FxMegaMenuColumn
 * &lt;a href&gt; (+ icon)           FxMegaMenuLink
 * title + description block   FxMegaMenuItem
 * CTA text / CTA with image   FxMegaMenuCta / FxMegaMenuImageCard
 * </pre>
 *
 * <h2>Default mega menu</h2>
 * <pre>{@code
 * FxMegaMenuDropdown company = new FxMegaMenuDropdown("Company", new FxMegaMenuGrid(3,
 *         new FxMegaMenuColumn(new FxMegaMenuLink("About Us"), new FxMegaMenuLink("Library")),
 *         new FxMegaMenuColumn(new FxMegaMenuLink("Contact Us"), new FxMegaMenuLink("Terms")),
 *         new FxMegaMenuColumn(new FxMegaMenuLink("Newsletter"))));
 *
 * FxMegaMenu menu = new FxMegaMenu(new FxNavbarBrand("Flowbite"));
 * menu.getLinks().addAll(new FxNavbarLink("Home", true), company.getToggle(), new FxNavbarLink("Team"));
 * menu.getActions().add(new FxButton("Sign up"));
 * }</pre>
 *
 * <h2>Full width</h2>
 * <pre>{@code
 * company.setFullWidth(true);
 * company.setContent(new FxMegaMenuGrid(3, itemA, itemB, itemC));   // FxMegaMenuItems
 * }</pre>
 *
 * <p>The looks live in {@code components.css} (the FxMegaMenu block).
 */
@DefaultProperty("links")
public class FxMegaMenu extends FxNavbar {

    private FxMegaMenuDropdown openDropdown;

    public FxMegaMenu() {
        this(null);
    }

    /**
     * @param brand the brand shown at the start, usually an {@code FxNavbarBrand}; may be {@code null}
     */
    public FxMegaMenu(Node brand) {
        super(brand);
        getStyleClass().add("fxk-megamenu");
        widthProperty().addListener(observable -> closeDropdown());
        compactProperty().addListener(observable -> closeDropdown());
    }

    /**
     * Adds the dropdown's toggle to {@link #getLinks()}. Shorthand for
     * {@code getLinks().add(dropdown.getToggle())}.
     *
     * @param dropdown the dropdown to add
     */
    public void addDropdown(FxMegaMenuDropdown dropdown) {
        getLinks().add(dropdown.getToggle());
    }

    /**
     * Closes the open dropdown, if any.
     */
    public void closeDropdown() {
        FxMegaMenuDropdown current = openDropdown;
        if (current != null) {
            current.hide();
        }
    }

    /**
     * @return the dropdown that is showing, or {@code null}
     */
    public final FxMegaMenuDropdown getOpenDropdown() {
        return openDropdown;
    }

    /** Called by a dropdown just before it opens: a sibling that is still open closes. */
    void dropdownOpened(FxMegaMenuDropdown dropdown) {
        if (openDropdown != null && openDropdown != dropdown) {
            openDropdown.hide();
        }
        openDropdown = dropdown;
    }

    /** Called by a dropdown after it closed. */
    void dropdownClosed(FxMegaMenuDropdown dropdown) {
        if (openDropdown == dropdown) {
            openDropdown = null;
        }
    }
}
