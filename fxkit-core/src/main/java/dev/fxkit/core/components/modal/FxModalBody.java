package dev.fxkit.core.components.modal;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * The scrolling middle of an {@link FxModal}. Port of Flowbite React's {@code <ModalBody>}
 * ({@code flex-1 overflow-auto p-6}).
 *
 * <p>A plain {@link VBox}: put your content in it. The modal scrolls it when it is taller than the
 * space left. Space between your children is yours to set, for example with the {@code gap-6} utility
 * class (Flowbite's {@code space-y-6}).
 *
 * <pre>{@code
 * FxModalBody body = new FxModalBody(paragraph1, paragraph2);
 * body.getStyleClass().add("gap-6");
 * }</pre>
 */
public class FxModalBody extends VBox {

    public static final String STYLE_CLASS = "fxk-modal-body";

    public FxModalBody() {
        getStyleClass().add(STYLE_CLASS);
    }

    public FxModalBody(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
