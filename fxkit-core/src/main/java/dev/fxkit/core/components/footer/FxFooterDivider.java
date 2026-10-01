package dev.fxkit.core.components.footer;

import javafx.scene.layout.Region;

/**
 * Flowbite's {@code <FooterDivider>}: a full-width 1px line with generous space above and below
 * ({@code my-6 ... lg:my-8}, the larger value is used).
 *
 * <p>The node is 65px tall and paints only its middle pixel (the same trick as {@code FxDropdownDivider}),
 * so the spacing needs no margins and works in any container. Style class {@code fxk-footer-divider}.
 */
public class FxFooterDivider extends Region {

    public FxFooterDivider() {
        getStyleClass().add("fxk-footer-divider");
    }
}
