package dev.fxkit.core.components.accordion;

import java.util.Objects;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.VBox;

/**
 * One collapsible section of an {@link FxAccordion}: a title plus the content it reveals
 * (Flowbite's {@code AccordionPanel}).
 *
 * <pre>{@code
 * FxAccordionPanel panel = new FxAccordionPanel("What is FXKit?", "A JavaFX component library.");
 * panel.setOpen(true);
 * }</pre>
 *
 * <p>A panel can be used on its own, but the "only one open at a time" rule, the first-panel-open
 * default and the divider/corner styling all come from the surrounding {@link FxAccordion}.
 */
public class FxAccordionPanel extends VBox {

    private final BooleanProperty open = new SimpleBooleanProperty(this, "open", false);
    private final FxAccordionTitle title;
    private final FxAccordionContent content;

    /** A panel with a plain-text title and one wrapping paragraph per string. */
    public FxAccordionPanel(String title, String... paragraphs) {
        this(new FxAccordionTitle(title), new FxAccordionContent(paragraphs));
    }

    /** A panel with a plain-text title and arbitrary content. */
    public FxAccordionPanel(String title, FxAccordionContent content) {
        this(new FxAccordionTitle(title), content);
    }

    /** A panel from a fully built title (for example one with a graphic) and content. */
    public FxAccordionPanel(FxAccordionTitle title, FxAccordionContent content) {
        this.title = Objects.requireNonNull(title, "title");
        this.content = Objects.requireNonNull(content, "content");

        getStyleClass().add("fxk-accordion-panel");
        getChildren().addAll(title, content);

        content.visibleProperty().bind(open);
        content.managedProperty().bind(open);
        title.expandedProperty().bind(open);
        title.setActivationHandler(this::toggle);

        BooleanStyleClassSync.sync(this, "fxk-accordion-open", open);
    }

    /** Whether the content is shown. Defaults to {@code false}; see {@link FxAccordion} for the accordion-level defaults. */
    public final BooleanProperty openProperty() {
        return open;
    }

    public final boolean isOpen() {
        return open.get();
    }

    public final void setOpen(boolean value) {
        open.set(value);
    }

    /** Opens the panel if closed, closes it if open (what a click on the title does). */
    public final void toggle() {
        open.set(!open.get());
    }

    public final FxAccordionTitle getTitle() {
        return title;
    }

    public final FxAccordionContent getContent() {
        return content;
    }
}
