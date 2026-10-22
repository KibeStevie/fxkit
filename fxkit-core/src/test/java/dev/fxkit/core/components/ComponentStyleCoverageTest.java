package dev.fxkit.core.components;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.fxkit.core.TestStyles;
import dev.fxkit.core.components.alert.FxAlert;
import dev.fxkit.core.components.avatar.FxAvatar;
import dev.fxkit.core.components.badge.FxBadge;
import dev.fxkit.core.components.blockquote.FxBlockquote;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Guards against drift between Java and CSS. A component sets a style class from an enum constant;
 * if nobody wrote the rule, JavaFX silently ignores the class and the component renders unstyled.
 * These tests only read the shipped stylesheets, so they need no toolkit and never skip.
 */
class ComponentStyleCoverageTest {

    private static void assertStyled(String prefix, Enum<?> value) {
        String styleClass = prefix + TestStyles.styleName(value);
        assertTrue(TestStyles.hasRuleFor(styleClass), "no rule for ." + styleClass + " in the shipped CSS");
    }

    @ParameterizedTest
    @EnumSource(FxAlert.Color.class)
    void everyAlertColorHasARule(FxAlert.Color value) {
        assertStyled("fxk-alert-color-", value);
    }

    @ParameterizedTest
    @EnumSource(FxBadge.Color.class)
    void everyBadgeColorHasARule(FxBadge.Color value) {
        assertStyled("fxk-badge-color-", value);
    }

    @ParameterizedTest
    @EnumSource(FxBadge.Size.class)
    void everyBadgeSizeHasARule(FxBadge.Size value) {
        assertStyled("fxk-badge-size-", value);
    }

    @ParameterizedTest
    @EnumSource(FxAvatar.Color.class)
    void everyAvatarColorHasARule(FxAvatar.Color value) {
        assertStyled("fxk-avatar-color-", value);
    }

    @ParameterizedTest
    @EnumSource(FxAvatar.Size.class)
    void everyAvatarSizeHasARule(FxAvatar.Size value) {
        assertStyled("fxk-avatar-size-", value);
    }

    @ParameterizedTest
    @EnumSource(FxAvatar.Status.class)
    void everyAvatarStatusHasARule(FxAvatar.Status value) {
        assertStyled("fxk-avatar-status-", value);
    }

    @ParameterizedTest
    @EnumSource(FxBlockquote.Size.class)
    void everyBlockquoteSizeHasARule(FxBlockquote.Size value) {
        assertStyled("fxk-blockquote-size-", value);
    }

    @ParameterizedTest
    @EnumSource(FxBlockquote.TextAlign.class)
    void everyBlockquoteAlignmentHasARule(FxBlockquote.TextAlign value) {
        assertStyled("fxk-blockquote-align-", value);
    }

    @ParameterizedTest
    @EnumSource(FxBlockquote.Weight.class)
    void everyBlockquoteWeightHasARule(FxBlockquote.Weight value) {
        assertStyled("fxk-blockquote-weight-", value);
    }

    @ParameterizedTest
    @EnumSource(FxBlockquote.IconSize.class)
    void everyBlockquoteIconSizeHasARule(FxBlockquote.IconSize value) {
        assertStyled("fxk-blockquote-icon-", value);
    }

    /** The fixed style classes these components add themselves, as of today. */
    @ParameterizedTest
    @ValueSource(strings = {
            "fxk-accordion", "fxk-accordion-panel", "fxk-accordion-title", "fxk-accordion-content",
            "fxk-accordion-open", "fxk-accordion-title-open", "fxk-accordion-first", "fxk-accordion-last",
            "fxk-accordion-flush", "fxk-accordion-text",
            "fxk-alert", "fxk-alert-title", "fxk-alert-text", "fxk-alert-close", "fxk-alert-border-accent",
            "fxk-avatar", "fxk-avatar-inner", "fxk-avatar-face", "fxk-avatar-placeholder",
            "fxk-avatar-initials", "fxk-avatar-status", "fxk-avatar-group", "fxk-avatar-counter",
            "fxk-avatar-rounded", "fxk-avatar-bordered", "fxk-avatar-stacked",
            "fxk-avatar-texts", "fxk-avatar-title", "fxk-avatar-subtitle",
            "fxk-badge", "fxk-badge-text", "fxk-badge-close", "fxk-badge-dismissible",
            "fxk-blockquote", "fxk-blockquote-text", "fxk-blockquote-icon", "fxk-blockquote-solid",
            "fxk-blockquote-caption", "fxk-blockquote-cites", "fxk-blockquote-cite",
            "fxk-blockquote-cite-role", "fxk-blockquote-cite-divider",
            "fxk-blockquote-caption-centered", "fxk-blockquote-caption-strong",
            "fxk-breadcrumb", "fxk-breadcrumb-solid", "fxk-breadcrumb-separator", "fxk-breadcrumb-chevron",
            "fxk-breadcrumb-item", "fxk-breadcrumb-item-link"})
    void everyFixedStyleClassHasARule(String styleClass) {
        assertTrue(TestStyles.hasRuleFor(styleClass), "no rule for ." + styleClass + " in the shipped CSS");
    }
}
