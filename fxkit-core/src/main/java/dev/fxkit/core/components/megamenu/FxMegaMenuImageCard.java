package dev.fxkit.core.components.megamenu;

import dev.fxkit.core.internal.ActionSupport;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * The image call-to-action of Flowbite's "Full width with image" example: a rounded card with a
 * background picture, a bold line of text and an outlined "Get started" button.
 *
 * <p>The whole card is one click target, like Flowbite's wrapping {@code <a>}; the button is only
 * its visual. Flowbite's {@code bg-blend-multiply} (and {@code hover:bg-blend-soft-light}) is
 * approximated with a gray overlay over the picture that fades on hover, because JavaFX CSS has no
 * blend modes. Without an image the card is the plain gray of Flowbite's {@code bg-gray-500}.
 *
 * <pre>{@code
 * FxMegaMenuImageCard card = new FxMegaMenuImageCard(
 *         "Preview the new Flowbite dashboard navigation.", "Get started");
 * card.setImageUrl(getClass().getResource("/images/dashboard-overview.png").toExternalForm());
 * card.setOnAction(e -> router.go("/dashboard"));
 * }</pre>
 */
public class FxMegaMenuImageCard extends StackPane {

    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final StringProperty actionText = new SimpleStringProperty(this, "actionText");
    private final StringProperty imageUrl = new SimpleStringProperty(this, "imageUrl");

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }

        @Override
        public Object getBean() {
            return FxMegaMenuImageCard.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    private final Label textLabel = new Label();
    private final Label actionLabel = new Label();

    public FxMegaMenuImageCard() {
        this(null, null);
    }

    /**
     * @param text       the bold line
     * @param actionText the label of the outlined button
     */
    public FxMegaMenuImageCard(String text, String actionText) {
        getStyleClass().add("fxk-megamenu-image-card");
        setAlignment(Pos.TOP_LEFT);

        Region overlay = new Region();
        overlay.getStyleClass().add("fxk-megamenu-image-card-overlay");
        overlay.setMouseTransparent(true);

        textLabel.getStyleClass().add("fxk-megamenu-image-card-text");
        textLabel.setWrapText(true);
        textLabel.setMinHeight(Region.USE_PREF_SIZE);
        textLabel.setMaxWidth(360); // max-w-xl, scaled to the card

        actionLabel.getStyleClass().add("fxk-megamenu-image-card-button");
        actionLabel.setContentDisplay(ContentDisplay.RIGHT);
        actionLabel.setGraphic(FxMegaMenuIcons.arrowRight());

        VBox body = new VBox(20, textLabel, actionLabel); // mb-5
        body.setAlignment(Pos.TOP_LEFT);
        body.setPickOnBounds(false);
        body.setMouseTransparent(true);
        getChildren().addAll(overlay, body);

        // background-image is not clipped by JavaFX's background radius, so round the whole card.
        Rectangle clip = new Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        textLabel.textProperty().bind(this.text);
        actionLabel.textProperty().bind(this.actionText);
        actionLabel.visibleProperty().bind(this.actionText.isNotEmpty());
        actionLabel.managedProperty().bind(this.actionText.isNotEmpty());
        imageUrl.addListener((observable, oldUrl, newUrl) -> applyImage(newUrl));

        ActionSupport.install(this, this::fire);
        accessibleTextProperty().bind(this.text);

        setText(text);
        setActionText(actionText);
    }

    private void applyImage(String url) {
        if (url == null || url.isBlank()) {
            setStyle(null);
        } else {
            setStyle("-fx-background-image: url('" + url.replace("'", "%27") + "');");
        }
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

    public final StringProperty actionTextProperty() {
        return actionText;
    }

    public final String getActionText() {
        return actionText.get();
    }

    public final void setActionText(String actionText) {
        this.actionText.set(actionText);
    }

    /**
     * @return the URL of the background picture (for example from
     *         {@code getResource(...).toExternalForm()}); {@code null} for a plain gray card
     */
    public final StringProperty imageUrlProperty() {
        return imageUrl;
    }

    public final String getImageUrl() {
        return imageUrl.get();
    }

    public final void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
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
