package dev.fxkit.showcase;

import dev.fxkit.core.components.FxButton;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A read-only, monospaced code snippet box (#41), reused by every showcase page that shows a demo's
 * source next to it.
 *
 * <p>The box is styled only with FXKit's existing tokens and utility classes - {@code bg-surface-alt},
 * {@code border}, {@code rounded-md}, the showcase's own {@code .mono} class from {@code showcase.css} -
 * no new hard-coded colors or fonts. Long lines never push the rest of the page off-screen: the snippet
 * scrolls horizontally in its own {@link ScrollPane} (reusing the {@code showcase-scroll} class for a
 * transparent background) instead of wrapping or growing the page.
 */
final class CodeBlock {

    private CodeBlock() {
        // static factory only
    }

    /**
     * Builds a snippet box showing {@code code} verbatim (leading/trailing blank lines stripped), with a
     * small "Copy" button above it.
     */
    static Node create(String code) {
        String source = code.strip();

        Label text = new Label(source);
        text.getStyleClass().addAll("mono", "text-xs", "text-body");

        ScrollPane scroll = new ScrollPane(text);
        scroll.setFitToHeight(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("showcase-scroll");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(spacer, copyButton(source));
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(4, toolbar, scroll);
        box.getStyleClass().addAll("bg-surface-alt", "border", "rounded-md", "p-3");
        return box;
    }

    /** Nice-to-have from #41's tasks: not required, but cheap once {@code FxButton} already exists. */
    private static FxButton copyButton(String source) {
        FxButton copy = new FxButton("Copy");
        copy.setVariant(FxButton.Variant.GHOST);
        copy.setSize(FxButton.Size.SM);
        copy.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(source);
            Clipboard.getSystemClipboard().setContent(content);
        });
        return copy;
    }
}
