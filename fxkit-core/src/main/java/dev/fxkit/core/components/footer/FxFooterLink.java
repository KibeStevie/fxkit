package dev.fxkit.core.components.footer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;

/**
 * Flowbite's {@code <FooterLink>}: a text link with an underline on hover.
 *
 * <p>Flowbite's {@code href} becomes {@link #setOnAction}: a JavaFX node cannot navigate on its own.
 * Open a URL from the handler with {@code HostServices.showDocument(...)} or switch your own view.
 *
 * <p>Style class {@code fxk-footer-link}. Meant to live in an {@link FxFooterLinkGroup} inside an
 * {@link FxFooter}.
 */
public class FxFooterLink extends Hyperlink {

    public FxFooterLink(String text) {
        super(text);
        getStyleClass().add("fxk-footer-link");
    }

    public FxFooterLink(String text, EventHandler<ActionEvent> onAction) {
        this(text);
        setOnAction(onAction);
    }
}
