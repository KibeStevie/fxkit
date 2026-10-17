package dev.fxkit.core.components.modal;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * The bottom row of an {@link FxModal}, usually its buttons. Port of Flowbite React's
 * {@code <ModalFooter>} ({@code flex items-center gap-2 p-6}).
 *
 * <p>It has a top border, except in a {@link FxModal#popupProperty() popup} modal.
 *
 * <pre>{@code
 * modal.setFooter(new FxModalFooter(new FxButton("I accept"), declineButton));
 * }</pre>
 */
public class FxModalFooter extends HBox {

    public static final String STYLE_CLASS = "fxk-modal-footer";

    public FxModalFooter() {
        initialize();
    }

    public FxModalFooter(Node... children) {
        super(children);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
    }
}
