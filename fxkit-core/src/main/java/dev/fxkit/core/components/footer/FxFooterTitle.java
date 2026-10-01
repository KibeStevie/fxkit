package dev.fxkit.core.components.footer;

import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

/**
 * Flowbite's {@code <FooterTitle title="...">}: a small, bold, upper-case heading for a group of links.
 *
 * <p>JavaFX CSS has no {@code text-transform}, so the text is upper-cased here: {@code title} is what
 * you set, the label's text is bound to its upper-case form (so {@code setText} is not available).
 * Flowbite's {@code mb-6} is the label's bottom padding (24px).
 *
 * <p>Style class {@code fxk-footer-title}.
 */
public class FxFooterTitle extends Label {

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    public FxFooterTitle(String title) {
        getStyleClass().add("fxk-footer-title");
        textProperty().bind(Bindings.createStringBinding(
                () -> this.title.get() == null ? "" : this.title.get().toUpperCase(Locale.ROOT), this.title));
        setTitle(title);
    }

    public final StringProperty titleProperty() { return title; }
    public final String getTitle() { return title.get(); }
    public final void setTitle(String value) { title.set(value); }
}
