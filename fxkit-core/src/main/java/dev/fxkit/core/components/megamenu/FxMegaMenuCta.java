package dev.fxkit.core.components.megamenu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The text call-to-action of Flowbite's "Full width with CTA" example: a heading, a short paragraph
 * and a link with an arrow ({@code <h2>}, {@code <p>}, {@code <a>} + {@code HiArrowRight}).
 *
 * <pre>{@code
 * FxMegaMenuCta cta = new FxMegaMenuCta("Our brands",
 *         "At Flowbite, we have a portfolio of brands that cater to a variety of preferences.",
 *         "Explore our brands");
 * cta.setOnAction(e -> router.go("/brands"));
 * }</pre>
 */
public class FxMegaMenuCta extends VBox {

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final StringProperty linkText = new SimpleStringProperty(this, "linkText");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");

    private final Label titleLabel = new Label();
    private final Label textLabel = new Label();
    private final Hyperlink link = new Hyperlink();

    public FxMegaMenuCta() {
        this(null, null, null);
    }

    /**
     * @param title    the bold heading
     * @param text     the muted paragraph
     * @param linkText the label of the link that carries the action
     */
    public FxMegaMenuCta(String title, String text, String linkText) {
        getStyleClass().add("fxk-megamenu-cta");
        setAlignment(Pos.TOP_LEFT);

        titleLabel.getStyleClass().add("fxk-megamenu-cta-title");
        textLabel.getStyleClass().add("fxk-megamenu-cta-text");
        textLabel.setWrapText(true);
        textLabel.setMinHeight(Region.USE_PREF_SIZE);

        link.getStyleClass().add("fxk-megamenu-cta-link");
        link.setMnemonicParsing(false);
        link.setContentDisplay(ContentDisplay.RIGHT);
        link.setGraphic(FxMegaMenuIcons.arrowRight());
        link.setMaxWidth(Region.USE_PREF_SIZE); // only the label and arrow are clickable
        link.setOnAction(event -> {
            link.setVisited(false);
            EventHandler<ActionEvent> handler = getOnAction();
            if (handler != null) {
                handler.handle(event);
            }
        });

        titleLabel.textProperty().bind(this.title);
        textLabel.textProperty().bind(this.text);
        link.textProperty().bind(this.linkText);
        getChildren().addAll(titleLabel, textLabel, link);

        setTitle(title);
        setText(text);
        setLinkText(linkText);
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

    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    public final StringProperty linkTextProperty() {
        return linkText;
    }

    public final String getLinkText() {
        return linkText.get();
    }

    public final void setLinkText(String linkText) {
        this.linkText.set(linkText);
    }

    /**
     * @return the handler run when the link is clicked
     */
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
