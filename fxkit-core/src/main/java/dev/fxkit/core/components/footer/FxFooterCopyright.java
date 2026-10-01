package dev.fxkit.core.components.footer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Flowbite's {@code <FooterCopyright by="..." year={...} href="...">}: "© 2022 Flowbite™".
 *
 * <ul>
 *   <li>{@code year}: omitted when {@code null} (Flowbite parity);</li>
 *   <li>{@code by}: plain text, or - when an {@link #onActionProperty() onAction} handler is set - a
 *       link that underlines on hover (Flowbite's {@code href}).</li>
 * </ul>
 *
 * <p>Alignment (left by default) is a plain {@link HBox} property, so a centered copyright
 * ({@code sm:text-center}) is {@code setAlignment(Pos.CENTER)}. Style classes:
 * {@code fxk-footer-copyright}, {@code fxk-footer-copyright-text}, {@code fxk-footer-copyright-link}.
 */
public class FxFooterCopyright extends HBox {

    private final StringProperty by = new SimpleStringProperty(this, "by");
    private final ObjectProperty<Integer> year = new SimpleObjectProperty<>(this, "year");
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>(this, "onAction");

    public FxFooterCopyright() {
        getStyleClass().add("fxk-footer-copyright");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(4); // ml-1
        InvalidationListener rebuild = o -> refresh();
        by.addListener(rebuild);
        year.addListener(rebuild);
        onAction.addListener(rebuild);
        refresh();
    }

    public FxFooterCopyright(String by) {
        this();
        setBy(by);
    }

    public FxFooterCopyright(String by, Integer year) {
        this();
        setBy(by);
        setYear(year);
    }

    public final StringProperty byProperty() { return by; }
    public final String getBy() { return by.get(); }
    public final void setBy(String value) { by.set(value); }

    public final ObjectProperty<Integer> yearProperty() { return year; }
    public final Integer getYear() { return year.get(); }
    public final void setYear(Integer value) { year.set(value); }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() { return onAction; }
    public final EventHandler<ActionEvent> getOnAction() { return onAction.get(); }
    public final void setOnAction(EventHandler<ActionEvent> value) { onAction.set(value); }

    private void refresh() {
        getChildren().clear();

        Label mark = new Label(getYear() == null ? "©" : "© " + getYear());
        mark.getStyleClass().add("fxk-footer-copyright-text");
        getChildren().add(mark);

        String name = getBy();
        if (name == null || name.isEmpty()) {
            return;
        }
        if (getOnAction() != null) {
            Hyperlink link = new Hyperlink(name);
            link.getStyleClass().add("fxk-footer-copyright-link");
            link.setOnAction(getOnAction());
            getChildren().add(link);
        } else {
            Label text = new Label(name);
            text.getStyleClass().add("fxk-footer-copyright-text");
            getChildren().add(text);
        }
    }
}
