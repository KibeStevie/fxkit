package dev.fxkit.core.components.sidebar;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * The brand block at the top of an {@link FxSidebar}, mirroring Flowbite React's
 * {@code <SidebarLogo>}: an {@link #imageProperty() image} (28px tall, aspect ratio kept) followed
 * by the {@link #textProperty() text} in large semibold type.
 *
 * <p>Flowbite's {@code href} becomes {@link #onActionProperty() onAction}; the logo is only
 * focusable and clickable when one is set. {@link #imageAltProperty() imageAlt} is the image's
 * accessible text ({@code imgAlt}).
 *
 * <p>In a collapsed sidebar the text is hidden and the image stays, as in Flowbite.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxSidebarLogo logo = new FxSidebarLogo("Flowbite");
 * logo.setImage(new Image(getClass().getResource("/logo.png").toExternalForm()));
 * logo.setImageAlt("Flowbite logo");
 * logo.setOnAction(e -> goHome());
 * }</pre>
 */
public class FxSidebarLogo extends HBox {

    public static final String STYLE_CLASS = "fxk-sidebar-logo";

    private static final String LINK_STYLE_CLASS = "fxk-sidebar-logo-link";
    private static final String IMAGE_STYLE_CLASS = "fxk-sidebar-logo-image";
    private static final String TEXT_STYLE_CLASS = "fxk-sidebar-logo-text";

    /** sm:h-7 */
    private static final double IMAGE_HEIGHT = 28;

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");
    private final StringProperty imageAlt = new SimpleStringProperty(this, "imageAlt");
    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction");
    private final ReadOnlyBooleanWrapper collapsed = new ReadOnlyBooleanWrapper(this, "collapsed", false);

    private final ImageView imageNode = new ImageView();
    private final Label textNode = new Label();

    public FxSidebarLogo() {
        initialize();
    }

    public FxSidebarLogo(String text) {
        this();
        setText(text);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);

        imageNode.getStyleClass().add(IMAGE_STYLE_CLASS);
        imageNode.setPreserveRatio(true);
        imageNode.setSmooth(true);
        imageNode.setFitHeight(IMAGE_HEIGHT);
        imageNode.imageProperty().bind(image);
        imageNode.accessibleTextProperty().bind(imageAlt);

        textNode.getStyleClass().add(TEXT_STYLE_CLASS);
        textNode.textProperty().bind(text);

        getChildren().addAll(imageNode, textNode);

        image.addListener((observable, oldValue, newValue) -> refresh());
        text.addListener((observable, oldValue, newValue) -> refresh());
        collapsed.addListener((observable, oldValue, newValue) -> refresh());

        onAction.addListener((observable, oldHandler, newHandler) -> {
            if (oldHandler != null) {
                removeEventHandler(ActionEvent.ACTION, oldHandler);
            }
            if (newHandler != null) {
                addEventHandler(ActionEvent.ACTION, newHandler);
            }
            boolean clickable = newHandler != null;
            setFocusTraversable(clickable);
            setAccessibleRole(clickable ? AccessibleRole.BUTTON : AccessibleRole.NODE);
            SidebarSupport.setStyleClassPresent(this, LINK_STYLE_CLASS, clickable);
        });

        SidebarSupport.onActivate(this, this::fire);
        SidebarSupport.bindCollapsed(this, collapsed);
        refresh();
    }

    private void refresh() {
        SidebarSupport.setShown(imageNode, image.get() != null);
        String currentText = text.get();
        SidebarSupport.setShown(textNode,
                currentText != null && !currentText.isEmpty() && !collapsed.get());
    }

    /** Fires an {@link ActionEvent} if an action handler is set. */
    public void fire() {
        if (onAction.get() != null && !isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    public Image getImage() { return image.get(); }
    public void setImage(Image image) { this.image.set(image); }
    public ObjectProperty<Image> imageProperty() { return image; }

    public String getImageAlt() { return imageAlt.get(); }
    public void setImageAlt(String imageAlt) { this.imageAlt.set(imageAlt); }
    public StringProperty imageAltProperty() { return imageAlt; }

    public String getText() { return text.get(); }
    public void setText(String text) { this.text.set(text); }
    public StringProperty textProperty() { return text; }

    public EventHandler<ActionEvent> getOnAction() { return onAction.get(); }
    public void setOnAction(EventHandler<ActionEvent> onAction) { this.onAction.set(onAction); }
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() { return onAction; }

    /** @return whether the enclosing {@link FxSidebar} is collapsed ({@code false} outside a sidebar) */
    public boolean isCollapsed() { return collapsed.get(); }
    public ReadOnlyBooleanProperty collapsedProperty() { return collapsed.getReadOnlyProperty(); }
}
