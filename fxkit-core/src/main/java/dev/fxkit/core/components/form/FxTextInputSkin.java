package dev.fxkit.core.components.form;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.StackPane;

/**
 * The skin of {@link FxTextInput}: a stock {@link TextFieldSkin} plus three overlay nodes - the addon,
 * the left icon and the right icon - that sit INSIDE the field's 1px border.
 *
 * <p>Icons live in the field's padding: {@code form-components.css} gives the field 40px of padding
 * on the icon side (Flowbite's {@code pl-10} / {@code pr-10}), and this skin places a 40px strip there.
 * The addon has no fixed width, so it is the one thing that pushes the text: the text area is shifted
 * by the addon's width and the preferred width grows by it.
 *
 * <p>The password mode is {@link #maskText(String)}: {@code TextFieldSkin} only masks for a real
 * {@code PasswordField}, so the {@link FxTextInput.Type} is checked here instead.
 *
 * <p>Not part of FXKit's public API.
 */
final class FxTextInputSkin extends TextFieldSkin {

    /** The border the field draws; the overlay nodes sit inside it. Keep in sync with the CSS. */
    private static final double BORDER = 1;

    /** Flowbite's pl-10 / pr-10, the strip an icon sits in. Keep in sync with the CSS padding. */
    private static final double ICON_ZONE = 40;

    // Null while the super constructor runs (it may already call the overridden methods below).
    private final Label addonLabel = new Label();
    private final StackPane leftIcon = new StackPane();
    private final StackPane rightIcon = new StackPane();

    FxTextInputSkin(FxTextInput input) {
        super(input);

        addonLabel.getStyleClass().add("fxk-field-addon");
        leftIcon.getStyleClass().addAll("fxk-field-icon-pane", "fxk-field-icon-pane-left");
        rightIcon.getStyleClass().addAll("fxk-field-icon-pane", "fxk-field-icon-pane-right");
        // pointer-events-none: a click on an icon or the addon reaches the field
        addonLabel.setMouseTransparent(true);
        leftIcon.setMouseTransparent(true);
        rightIcon.setMouseTransparent(true);
        getChildren().addAll(addonLabel, leftIcon, rightIcon);

        registerChangeListener(input.iconProperty(), e -> refresh());
        registerChangeListener(input.rightIconProperty(), e -> refresh());
        registerChangeListener(input.addonProperty(), e -> refresh());
        refresh();
    }

    private void refresh() {
        FxTextInput input = (FxTextInput) getSkinnable();
        showIcon(leftIcon, input.getIcon());
        showIcon(rightIcon, input.getRightIcon());

        String addon = input.getAddon();
        boolean hasAddon = addon != null && !addon.isEmpty();
        addonLabel.setText(hasAddon ? addon : "");
        addonLabel.setVisible(hasAddon);

        input.requestLayout();
    }

    private static void showIcon(StackPane pane, Ikon ikon) {
        if (ikon == null) {
            pane.getChildren().clear();
            pane.setVisible(false);
            return;
        }
        FontIcon fontIcon = new FontIcon(ikon);
        fontIcon.getStyleClass().add("fxk-field-icon");
        pane.getChildren().setAll(fontIcon);
        pane.setVisible(true);
    }

    @Override
    protected String maskText(String text) {
        if (getSkinnable() instanceof FxTextInput input && input.getType() == FxTextInput.Type.PASSWORD) {
            return "\u2022".repeat(text.length());
        }
        return super.maskText(text);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        if (addonLabel == null) {
            super.layoutChildren(x, y, w, h);
            return;
        }
        TextField field = getSkinnable();
        double innerHeight = field.getHeight() - 2 * BORDER;
        double addonWidth = addonWidth(innerHeight);

        // The text starts after the addon; the icon strips are already inside the padding.
        super.layoutChildren(x + addonWidth, y, w - addonWidth, h);

        double left = BORDER;
        if (addonLabel.isVisible()) {
            addonLabel.resizeRelocate(left, BORDER, addonWidth, innerHeight);
            left += addonWidth;
        }
        if (leftIcon.isVisible()) {
            leftIcon.resizeRelocate(left, BORDER, ICON_ZONE, innerHeight);
        }
        if (rightIcon.isVisible()) {
            rightIcon.resizeRelocate(field.getWidth() - BORDER - ICON_ZONE, BORDER, ICON_ZONE, innerHeight);
        }
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset,
            double leftInset) {
        double width = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        return addonLabel == null ? width : width + addonWidth(height - topInset - bottomInset);
    }

    private double addonWidth(double height) {
        return addonLabel.isVisible() ? snapSizeX(addonLabel.prefWidth(height)) : 0;
    }
}
