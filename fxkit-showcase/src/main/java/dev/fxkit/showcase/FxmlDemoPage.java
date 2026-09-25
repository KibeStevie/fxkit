package dev.fxkit.showcase;

import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Demonstrates that {@code FxButton} and {@code FxCard} both work when built declaratively from FXML,
 * loaded from this module ({@code dev.fxkit.showcase}) rather than the module that defines them
 * ({@code dev.fxkit.core}) - which is exactly what proves {@code fxkit-core}'s {@code module-info.java}
 * {@code exports}/{@code opens} FxButton and FxCard correctly (#33).
 *
 * <p>{@code fxml-demo.fxml} needed no {@code fx:controller} and no extra module-info changes:
 * {@code opens dev.fxkit.core.components to javafx.fxml;} was already added for {@code FxButton} in
 * Phase 2 (#27) and covers {@code FxCard} too, since both live in the same package. See
 * {@code docs/fxml-notes.md} for the one workaround that FXML loading did need.
 */
final class FxmlDemoPage {

    private FxmlDemoPage() {
        // static factory only
    }

    /** Builds the page. Put it in the same scroll content as the other showcase sections. */
    static Node create() {
        VBox page = new VBox(24, heading(), load());
        page.getStyleClass().addAll("bg-background", "p-8");
        return page;
    }

    private static Node heading() {
        return new VBox(2,
                text("FXML", "text-xl", "font-semibold", "text-body"),
                text("The buttons and card below are built from fxml-demo.fxml, not Java (#33).",
                        "text-sm", "text-muted"));
    }

    private static Node load() {
        try {
            return FXMLLoader.load(FxmlDemoPage.class.getResource("fxml-demo.fxml"));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load fxml-demo.fxml", e);
        }
    }

    private static Label text(String value, String... classes) {
        Label label = new Label(value);
        label.getStyleClass().addAll(classes);
        return label;
    }
}
