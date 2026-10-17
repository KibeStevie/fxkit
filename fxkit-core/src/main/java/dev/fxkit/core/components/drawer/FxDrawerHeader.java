package dev.fxkit.core.components.drawer;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * The header of an {@link FxDrawer} (Flowbite's {@code DrawerHeader}): a title with an optional icon,
 * and a close button pinned to the top-right corner.
 *
 * <pre>{@code
 * FxDrawerHeader header = new FxDrawerHeader("Add widget");
 * header.setTitleIcon(MyIcons.PLUS);
 * }</pre>
 *
 * <h2>The close button</h2>
 * It sits 10px from the top and right edge of a padded drawer (Flowbite's {@code end-2.5 top-2.5})
 * and asks the enclosing {@code FxDrawer} to close. Replace its X with any icon through
 * {@link #closeIconProperty()}.
 *
 * <h2>Making the whole header clickable</h2>
 * Set {@link #onClickProperty() onClick} and a click anywhere on the header, <em>including</em> the
 * close button, runs it instead of closing the drawer; the header also gets a pointer cursor and a
 * hover background. This is Flowbite's {@code <DrawerHeader onClick=...>}, used for the swipeable-edge
 * pattern where the header toggles the drawer:
 *
 * <pre>{@code
 * header.setOnClick(() -> drawer.setOpen(!drawer.isOpen()));
 * }</pre>
 *
 * <p>The title icon is off by default (Flowbite's examples switch it off as often as they use it); set
 * {@link #titleIconProperty()} to show one. This class sets no padding of its own, so you can give it
 * any with {@link #setPadding(Insets)}.
 */
public class FxDrawerHeader extends StackPane {

    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final ObjectProperty<Ikon> titleIcon = new SimpleObjectProperty<>(this, "titleIcon");
    private final ObjectProperty<Ikon> closeIcon = new SimpleObjectProperty<>(this, "closeIcon");
    private final ObjectProperty<Runnable> onClick = new SimpleObjectProperty<>(this, "onClick");

    private final HBox titleRow = new HBox();
    private final Label titleLabel = new Label();
    private final Button closeButton = new Button();

    public FxDrawerHeader() {
        getStyleClass().add("fxk-drawer-header");

        titleRow.getStyleClass().add("fxk-drawer-header-row");
        titleLabel.getStyleClass().add("fxk-drawer-title");
        StackPane.setAlignment(titleRow, Pos.TOP_LEFT);

        closeButton.getStyleClass().add("fxk-drawer-close");
        closeButton.setAccessibleText("Close menu");
        closeButton.setOnAction(e -> activate());
        // end-2.5 top-2.5 is measured from the drawer's edge, which is 16px (p-4) outside the header.
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(-6, -6, 0, 0));

        getChildren().addAll(titleRow, closeButton);

        // The close button already ran onClick itself (see activate()), so skip clicks that began on it.
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Runnable click = getOnClick();
            if (click != null && e.getButton() == MouseButton.PRIMARY
                    && !FxDrawer.isWithin(closeButton, e.getTarget())) {
                click.run();
            }
        });

        BooleanStyleClassSync.sync(this, "fxk-drawer-header-clickable", onClick.isNotNull());

        title.addListener((o, was, is) -> refreshTitle());
        titleIcon.addListener((o, was, is) -> refreshTitle());
        closeIcon.addListener((o, was, is) -> refreshCloseIcon());
        refreshTitle();
        refreshCloseIcon();
    }

    /** @param title the title text */
    public FxDrawerHeader(String title) {
        this();
        setTitle(title);
    }

    /**
     * @param title     the title text
     * @param titleIcon the icon shown before the title
     */
    public FxDrawerHeader(String title, Ikon titleIcon) {
        this(title);
        setTitleIcon(titleIcon);
    }

    /** The title text. Flowbite's {@code title}. */
    public final StringProperty titleProperty() {
        return title;
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String value) {
        title.set(value);
    }

    /** The icon before the title, 16px. Flowbite's {@code titleIcon}; {@code null} (default) shows none. */
    public final ObjectProperty<Ikon> titleIconProperty() {
        return titleIcon;
    }

    public final Ikon getTitleIcon() {
        return titleIcon.get();
    }

    public final void setTitleIcon(Ikon value) {
        titleIcon.set(value);
    }

    /** The icon in the close button, 16px. Flowbite's {@code closeIcon}; {@code null} (default) draws an X. */
    public final ObjectProperty<Ikon> closeIconProperty() {
        return closeIcon;
    }

    public final Ikon getCloseIcon() {
        return closeIcon.get();
    }

    public final void setCloseIcon(Ikon value) {
        closeIcon.set(value);
    }

    /**
     * Runs on a click anywhere on the header, including the close button, in place of the close button's
     * normal "close the drawer". Flowbite's {@code onClick}. {@code null} (default) leaves the header inert
     * and the close button closing the drawer.
     */
    public final ObjectProperty<Runnable> onClickProperty() {
        return onClick;
    }

    public final Runnable getOnClick() {
        return onClick.get();
    }

    public final void setOnClick(Runnable value) {
        onClick.set(value);
    }

    /** The close button was activated by mouse or keyboard. */
    private void activate() {
        Runnable click = getOnClick();
        if (click != null) {
            click.run();
            return;
        }
        FxDrawer drawer = FxDrawer.findDrawer(this);
        if (drawer != null) {
            drawer.requestClose();
        }
    }

    private void refreshTitle() {
        titleRow.getChildren().clear();
        Ikon icon = getTitleIcon();
        if (icon != null) {
            FontIcon glyph = new FontIcon(icon);
            glyph.getStyleClass().add("fxk-drawer-title-icon");
            titleRow.getChildren().add(glyph);
        }
        titleLabel.setText(getTitle());
        titleRow.getChildren().add(titleLabel);
    }

    private void refreshCloseIcon() {
        Node glyph;
        Ikon icon = getCloseIcon();
        if (icon != null) {
            FontIcon font = new FontIcon(icon);
            font.getStyleClass().add("fxk-drawer-close-icon");
            glyph = font;
        } else {
            SVGPath x = new SVGPath();
            x.setContent("M1.5 1.5L14.5 14.5M14.5 1.5L1.5 14.5");
            x.getStyleClass().add("fxk-drawer-close-shape");
            glyph = x;
        }
        StackPane box = new StackPane(glyph);
        box.getStyleClass().add("fxk-drawer-close-icon-box");
        closeButton.setGraphic(box);
    }
}
