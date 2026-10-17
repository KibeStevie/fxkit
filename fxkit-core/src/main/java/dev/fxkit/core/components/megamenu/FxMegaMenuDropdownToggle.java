package dev.fxkit.core.components.megamenu;

import dev.fxkit.core.internal.ActionSupport;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * The navbar entry that opens a {@link FxMegaMenuDropdown} (Flowbite's
 * {@code <MegaMenuDropdownToggle>}): the label and a chevron.
 *
 * <p>
 * You normally get one from {@link FxMegaMenuDropdown#getToggle()} and add it
 * to
 * {@link FxMegaMenu#getLinks()} next to the {@code FxNavbarLink}s. It looks
 * like a link while the
 * navbar is wide, and like a stacked row with the chevron at the far end
 * ({@code justify-between})
 * while the navbar is compact. While its dropdown is open the {@code :open}
 * pseudo-class is set and
 * the chevron turns upside down.
 *
 * <p>
 * It is a plain {@link HBox} rather than a {@code Button} so the chevron can be
 * pushed to the far
 * end of a stacked row; {@code ActionSupport} gives it focus, click and Enter /
 * Space handling.
 * Its colors come from the {@code -fxk-navbar-link-*} looked-up colors of the
 * surrounding
 * {@link dev.fxkit.core.components.navbar.FxNavbar}, so it has to live inside
 * one.
 */
public class FxMegaMenuDropdownToggle extends HBox {

    private static final PseudoClass OPEN = PseudoClass.getPseudoClass("open");

    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final ReadOnlyBooleanWrapper open = new ReadOnlyBooleanWrapper(this, "open", false) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(OPEN, get());
        }
    };

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }

        @Override
        public Object getBean() {
            return FxMegaMenuDropdownToggle.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    public FxMegaMenuDropdownToggle() {
        this(null);
    }

    /**
     * @param text the label, for example {@code "Company"}
     */
    public FxMegaMenuDropdownToggle(String text) {
        getStyleClass().add("fxk-megamenu-toggle");
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(Double.MAX_VALUE); // stacked rows fill the navbar's width

        Label label = new Label();
        label.getStyleClass().add("fxk-megamenu-toggle-label");
        label.textProperty().bind(this.text);

        Region spacer = new Region(); // justify-between: pushes the chevron to the end of a stacked row
        spacer.setMinWidth(0);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane chevron = FxMegaMenuIcons.chevronDown();
        HBox.setMargin(chevron, new Insets(0, 0, 0, 8)); // ml-2
        getChildren().addAll(label, spacer, chevron);

        ActionSupport.install(this, this::fire);
        accessibleTextProperty().bind(this.text);
        setText(text);
    }

    /** Fires the action, as a click does. */
    public void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    /**
     * @return whether the dropdown this toggle belongs to is showing; owned by
     *         {@link FxMegaMenuDropdown}
     */
    public final ReadOnlyBooleanProperty openProperty() {
        return open.getReadOnlyProperty();
    }

    public final boolean isOpen() {
        return open.get();
    }

    /** Package-private: the dropdown reports its state here. */
    final void setOpen(boolean open) {
        this.open.set(open);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public final void setOnAction(EventHandler<ActionEvent> handler) {
        onAction.set(handler);
    }
}
