package dev.fxkit.core.components.modal;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * The top row of an {@link FxModal}: a title and a close button. Port of Flowbite React's
 * {@code <ModalHeader>}.
 *
 * <p>Leave the title empty for the pop-up look ({@code <ModalHeader />}): only the close button shows.
 * {@link FxModal#setHeader(javafx.scene.Node)} wires the close button to the modal; the same header
 * can be styled by {@link FxModal#popupProperty()} without knowing about it, because the look comes
 * from CSS ({@code .fxk-modal-popup .fxk-modal-header}).
 *
 * <pre>{@code
 * modal.setHeader(new FxModalHeader("Terms of Service"));
 * modal.setHeader(new FxModalHeader());   // pop-up look
 * }</pre>
 */
public class FxModalHeader extends HBox {

    public static final String STYLE_CLASS = "fxk-modal-header";

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final BooleanProperty closeButtonVisible = new SimpleBooleanProperty(this, "closeButtonVisible", true);

    private final Label titleLabel = new Label();
    private final Region spacer = new Region();
    private final Button closeButton = new Button();

    /** Set by {@link FxModal}; what the close button does. */
    private Runnable closeAction;

    public FxModalHeader() {
        this(null);
    }

    /**
     * @param title the title text (Flowbite's children); {@code null} or empty shows no title
     */
    public FxModalHeader(String title) {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.TOP_LEFT);

        titleLabel.getStyleClass().add("fxk-modal-title");
        titleLabel.textProperty().bind(this.title);
        titleLabel.visibleProperty().bind(Bindings.isNotEmpty(this.title));
        titleLabel.managedProperty().bind(titleLabel.visibleProperty());
        titleLabel.setMinWidth(Region.USE_PREF_SIZE);

        HBox.setHgrow(spacer, Priority.ALWAYS); // ms-auto

        SVGPath cross = new SVGPath();
        cross.setContent("M5 5 L15 15 M15 5 L5 15");
        cross.setFill(Color.TRANSPARENT);
        cross.setStrokeWidth(2);
        cross.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        // The stroke follows the button's text color, so hover and the dark theme restyle it for free.
        cross.strokeProperty().bind(closeButton.textFillProperty());
        StackPane box = new StackPane(cross);
        box.getStyleClass().add("fxk-modal-close-icon-box");
        box.setMouseTransparent(true);

        closeButton.getStyleClass().add("fxk-modal-close");
        closeButton.setGraphic(box);
        closeButton.setAccessibleText("Close modal");
        closeButton.setOnAction(event -> {
            if (closeAction != null) {
                closeAction.run();
            }
        });
        closeButton.visibleProperty().bind(closeButtonVisible);
        closeButton.managedProperty().bind(closeButtonVisible);

        getChildren().addAll(titleLabel, spacer, closeButton);
        this.title.set(title);
    }

    void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
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

    /** @return whether the X button shows, default {@code true} */
    public final BooleanProperty closeButtonVisibleProperty() {
        return closeButtonVisible;
    }

    public final boolean isCloseButtonVisible() {
        return closeButtonVisible.get();
    }

    public final void setCloseButtonVisible(boolean visible) {
        closeButtonVisible.set(visible);
    }
}
