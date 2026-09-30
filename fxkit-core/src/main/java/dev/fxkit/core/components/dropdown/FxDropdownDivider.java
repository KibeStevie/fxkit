package dev.fxkit.core.components.dropdown;

import javafx.scene.layout.Region;

/**
 * A thin separator line between the entries of an {@link FxDropdown} (Flowbite's
 * {@code <DropdownDivider />}).
 *
 * <p>The line itself is painted by {@code components.css}: the region is 9px tall and the 1px line sits
 * in its middle, which reproduces Flowbite's {@code my-1 h-px} (4px margin, 1px line, 4px margin)
 * without any margin handling in Java.
 *
 * <pre>{@code
 * dropdown.getItems().addAll(
 *         new FxDropdownItem("Earnings"),
 *         new FxDropdownDivider(),
 *         new FxDropdownItem("Separated link"));
 * }</pre>
 */
public class FxDropdownDivider extends Region {

    public FxDropdownDivider() {
        getStyleClass().add("fxk-dropdown-divider");
    }
}
