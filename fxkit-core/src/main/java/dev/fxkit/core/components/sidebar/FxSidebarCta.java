package dev.fxkit.core.components.sidebar;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A tinted call-to-action box inside an {@link FxSidebar}, mirroring Flowbite React's
 * {@code <SidebarCTA>}. It is a plain {@link VBox} (12px between children) with a rounded, tinted
 * background that has a 24px gap above it ({@code mt-6}). Put anything in it; the static factories
 * below build the parts Flowbite's example uses, styled for the CTA's colors in both themes:
 *
 * <pre>{@code
 * FxSidebarCta cta = new FxSidebarCta();
 * cta.getChildren().addAll(
 *         FxSidebarCta.header(new FxBadge("Beta", FxBadge.Color.WARNING), () -> sidebar.getContent().remove(cta)),
 *         FxSidebarCta.text("Preview the new navigation! You can turn it off in your profile."),
 *         FxSidebarCta.link("Turn new navigation off", () -> settings.setNewNav(false)));
 * }</pre>
 *
 * <p>The CTA is not hidden when the sidebar is collapsed (Flowbite does not either); remove or hide
 * it yourself if it does not fit in 64px.
 */
public class FxSidebarCta extends VBox {

    public static final String STYLE_CLASS = "fxk-sidebar-cta";

    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-sidebar-cta-color-";

    public static final Color DEFAULT_COLOR = Color.DEFAULT;

    /**
     * The CTA's background tint, styled in {@code colors.css}. {@link #DEFAULT} is Flowbite's neutral
     * gray; the rest are Flowbite's {@code cta.color} map. Flowbite's {@code dark} and {@code light}
     * entries reference palette shades that do not exist, so here they are a mid gray and white
     * (inverted in the dark theme). {@link #BLUE} is cyan, as in Flowbite.
     */
    public enum Color {
        DEFAULT, BLUE, DARK, FAILURE, GRAY, GREEN, LIGHT, RED, PURPLE, SUCCESS, YELLOW, WARNING
    }

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    public FxSidebarCta() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
    }

    public FxSidebarCta(Node... children) {
        this();
        getChildren().addAll(children);
    }

    public Color getColor() { return color.get(); }
    public void setColor(Color color) { this.color.set(color); }
    public ObjectProperty<Color> colorProperty() { return color; }

    // ---- parts ---------------------------------------------------------------------------------

    /**
     * A small square close button with a multiplication-sign glyph (no icon-pack dependency), styled
     * for the CTA. {@code onClose} may be {@code null}.
     */
    public static Button closeButton(Runnable onClose) {
        Button close = new Button("\u00D7");
        close.getStyleClass().add("fxk-sidebar-cta-close");
        close.setAccessibleText("Close");
        close.setAccessibleRole(AccessibleRole.BUTTON);
        if (onClose != null) {
            close.setOnAction(event -> onClose.run());
        }
        return close;
    }

    /**
     * Flowbite's top row: {@code leading} (usually an {@code FxBadge}) at the start and a
     * {@link #closeButton(Runnable) close button} pushed to the far end. Pass a {@code null}
     * {@code onClose} to omit the button.
     */
    public static HBox header(Node leading, Runnable onClose) {
        HBox row = new HBox();
        row.getStyleClass().add("fxk-sidebar-cta-header");
        row.setAlignment(Pos.CENTER_LEFT);
        if (leading != null) {
            row.getChildren().add(leading);
        }
        if (onClose != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(spacer, closeButton(onClose));
        }
        return row;
    }

    /** A wrapping paragraph in the CTA's text color. */
    public static Label text(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("fxk-sidebar-cta-text");
        label.setWrapText(true);
        return label;
    }

    /** An underlined link-style button in the CTA's text color. {@code onAction} may be {@code null}. */
    public static Hyperlink link(String text, Runnable onAction) {
        Hyperlink link = new Hyperlink(text);
        link.getStyleClass().add("fxk-sidebar-cta-link");
        if (onAction != null) {
            link.setOnAction(event -> onAction.run());
        }
        return link;
    }
}
