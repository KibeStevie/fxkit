package dev.fxkit.core.components;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.TestStyles;
import dev.fxkit.core.components.button.FxButton;
import dev.fxkit.core.components.clipboard.FxClipboard;
import dev.fxkit.core.components.dropdown.FxDropdown;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Drift guard, like {@code ComponentStyleCoverageTest}, for the button, clipboard, date picker, drawer,
 * dropdown and footer: every enum-driven style class and every fixed class that the stylesheet is
 * expected to style must have a rule. It reads only the shipped CSS, so it needs no toolkit and never
 * skips.
 *
 * <p>Not listed on purpose: classes these components set purely as hooks for application CSS, which the
 * shipped stylesheet does not style because the layout is done in Java ({@code fxk-drawer-position-*},
 * {@code fxk-drawer-edge}, {@code fxk-footer-row}, {@code fxk-footer-link-group}, {@code fxk-dropdown},
 * {@code fxk-datepicker-inline}, {@code fxk-dp-selector-view}, {@code fxk-dropdown-header-title}).
 */
class InteractiveComponentStyleCoverageTest {

    private static void assertStyled(String prefix, Enum<?> value) {
        String styleClass = prefix + TestStyles.styleName(value);
        assertTrue(TestStyles.hasRuleFor(styleClass), "no rule for ." + styleClass + " in the shipped CSS");
    }

    @ParameterizedTest
    @EnumSource(FxButton.Variant.class)
    void everyButtonVariantHasARule(FxButton.Variant value) {
        assertStyled("fxk-btn-", value);
    }

    @ParameterizedTest
    @EnumSource(FxButton.Size.class)
    void everyButtonSizeHasARule(FxButton.Size value) {
        assertStyled("fxk-btn-size-", value);
    }

    @ParameterizedTest
    @EnumSource(FxButton.Color.class)
    void everyButtonColorHasARule(FxButton.Color value) {
        assertStyled("fxk-btn-color-", value);
    }

    @ParameterizedTest
    @EnumSource(FxDropdown.Size.class)
    void everyDropdownSizeHasARuleBecauseItReusesTheButtonSizeClasses(FxDropdown.Size value) {
        assertStyled("fxk-btn-size-", value);
    }

    @ParameterizedTest
    @EnumSource(FxClipboard.Variant.class)
    void everyClipboardVariantHasARule(FxClipboard.Variant value) {
        assertStyled("fxk-clip-", value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "fxk-btn", "fxk-btn-pill", "fxk-btn-processing",
            "fxk-btn-group", "fxk-btn-group-item", "fxk-btn-group-start", "fxk-btn-group-middle",
            "fxk-btn-group-end", "fxk-btn-group-only", "fxk-btn-group-pill", "fxk-btn-group-outline",
            "fxk-clip", "fxk-clip-copied", "fxk-clip-glyph-default", "fxk-clip-glyph-success",
            "fxk-datepicker", "fxk-dp-card", "fxk-dp-input", "fxk-dp-icon", "fxk-dp-title", "fxk-dp-selector",
            "fxk-dp-weekday", "fxk-dp-item", "fxk-dp-item-outside", "fxk-dp-item-today",
            "fxk-dp-item-selected", "fxk-dp-today", "fxk-dp-clear", "fxk-dp-footer",
            "fxk-drawer", "fxk-drawer-panel", "fxk-drawer-backdrop", "fxk-drawer-padded",
            "fxk-drawer-header-clickable", "fxk-drawer-close", "fxk-drawer-title", "fxk-drawer-close-shape",
            "fxk-dropdown-inline", "fxk-dropdown-arrow-box", "fxk-dropdown-menu", "fxk-dropdown-item",
            "fxk-dropdown-divider", "fxk-dropdown-header", "fxk-dropdown-header-content",
            "fxk-dropdown-header-subtitle",
            "fxk-footer", "fxk-footer-container", "fxk-footer-bg-dark", "fxk-footer-link", "fxk-footer-brand",
            "fxk-footer-copyright-text", "fxk-footer-copyright-link", "fxk-footer-title", "fxk-footer-divider",
            "fxk-footer-icon"})
    void everyFixedStyleClassHasARule(String styleClass) {
        assertTrue(TestStyles.hasRuleFor(styleClass), "no rule for ." + styleClass + " in the shipped CSS");
    }
}
