package dev.fxkit.core.components.accordion;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The body of an {@link FxAccordionPanel} (Flowbite's {@code AccordionContent}): a padded
 * {@link VBox} that is shown only while its panel is open.
 *
 * <pre>{@code
 * new FxAccordionContent("First paragraph.", "Second paragraph.");     // wrapping muted text
 * new FxAccordionContent(FxAccordionContent.paragraph("Intro"), myNode); // or any nodes
 * }</pre>
 *
 * <p>Styling lives in {@code components.css} ({@code .fxk-accordion-content}).
 */
public class FxAccordionContent extends VBox {

    public FxAccordionContent() {
        getStyleClass().add("fxk-accordion-content");
    }

    /** Content made of the given nodes, stacked with an 8px gap (Flowbite's {@code mb-2}). */
    public FxAccordionContent(Node... children) {
        this();
        getChildren().addAll(children);
    }

    /** Content made of one wrapping, muted paragraph per string. */
    public FxAccordionContent(String... paragraphs) {
        this();
        for (String text : paragraphs) {
            getChildren().add(paragraph(text));
        }
    }

    /**
     * @param text the paragraph text
     * @return a wrapping label styled as accordion body text
     */
    public static Label paragraph(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("fxk-accordion-text");
        return label;
    }
}
