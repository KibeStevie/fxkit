package dev.fxkit.core.components.megamenu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import dev.fxkit.core.internal.ActionSupport;

/**
 * A clickable block with a bold title and a muted description: the
 * {@code <a className="block rounded-lg p-3 hover:bg-gray-50">} of Flowbite's "Full width
 * dropdown" example.
 *
 * <p>The whole block is the target: click, or Enter / Space while focused, fires the action. It is
 * laid out with a {@link VBox}, so a long description wraps inside the width its grid cell gives it.
 *
 * <pre>{@code
 * FxMegaMenuItem item = new FxMegaMenuItem("Online Stores",
 *         "Connect with third-party tools that you're already using.");
 * item.setOnAction(e -> router.go("/stores"));
 * }</pre>
 */
public class FxMegaMenuItem extends VBox {

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty description = new SimpleStringProperty(this, "description");
    private final Label titleLabel = new Label();
    private final Label descriptionLabel = new Label();

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }

        @Override
        public Object getBean() {
            return FxMegaMenuItem.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    public FxMegaMenuItem() {
        this(null, null);
    }

    /**
     * @param title       the bold first line
     * @param description the muted text under it; may be {@code null}
     */
    public FxMegaMenuItem(String title, String description) {
        getStyleClass().add("fxk-megamenu-item");
        titleLabel.getStyleClass().add("fxk-megamenu-item-title");
        descriptionLabel.getStyleClass().add("fxk-megamenu-item-description");
        titleLabel.setWrapText(true);
        descriptionLabel.setWrapText(true);
        titleLabel.setMinHeight(Label.USE_PREF_SIZE);
        descriptionLabel.setMinHeight(Label.USE_PREF_SIZE);

        titleLabel.textProperty().bind(this.title);
        descriptionLabel.textProperty().bind(this.description);
        descriptionLabel.visibleProperty().bind(this.description.isNotEmpty());
        descriptionLabel.managedProperty().bind(this.description.isNotEmpty());
        getChildren().addAll(titleLabel, descriptionLabel);

        ActionSupport.install(this, this::fire);
        accessibleTextProperty().bind(this.title);

        setTitle(title);
        setDescription(description);
    }

    /** Fires the action, as a click does. */
    public void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    public final StringProperty titleProperty() {
        return title;
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String title) {
        this.title.set(title);
    }

    public final StringProperty descriptionProperty() {
        return description;
    }

    public final String getDescription() {
        return description.get();
    }

    public final void setDescription(String description) {
        this.description.set(description);
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
