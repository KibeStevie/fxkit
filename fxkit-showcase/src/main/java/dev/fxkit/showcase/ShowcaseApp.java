package dev.fxkit.showcase;

import dev.fxkit.core.FxKit;
import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The FXKit showcase window.
 *
 * <p>JavaFX lifecycle in short: {@code main} calls {@link #launch(String...)},
 * JavaFX starts its UI thread and then calls {@link #start(Stage)} with the
 * primary {@link Stage} (the window). A Stage shows one {@link Scene}, and the
 * Scene holds a tree of nodes.
 *
 * <p>Styling now comes from FXKit's design tokens: the token stylesheet from
 * {@code fxkit-core} plus this module's {@code showcase.css}.
 */
public class ShowcaseApp extends Application {

    private static final int[] STEPS = {50, 100, 200, 300, 400, 500, 600, 700, 800, 900};

    @Override
    public void start(Stage stage) {
        Label title = new Label("Hello, FXKit!");
        title.getStyleClass().add("showcase-title");

        // Uses a class from fxkit-core, proving the module dependency works.
        Label subtitle = new Label("Core library version " + FxKit.version());
        subtitle.getStyleClass().add("showcase-subtitle");

        // TEMPORARY: token spot-check rows. They are replaced by the real
        // color palette page in Sprint 2. Hover a swatch to see its token name.
        VBox scaleRows = new VBox(6);
        scaleRows.setAlignment(Pos.CENTER);
        for (String scale : new String[] {"gray", "blue", "red", "green", "yellow"}) {
            HBox row = new HBox(4);
            row.setAlignment(Pos.CENTER);
            for (int step : STEPS) {
                row.getChildren().add(swatch("-fxk-" + scale + "-" + step));
            }
            scaleRows.getChildren().add(row);
        }

        Label semanticCaption = new Label("primary | danger | success | warning");
        semanticCaption.getStyleClass().add("showcase-caption");
        HBox semanticRow = new HBox(4,
                swatch("-fxk-primary"), swatch("-fxk-danger"),
                swatch("-fxk-success"), swatch("-fxk-warning"));
        semanticRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(12, title, subtitle, scaleRows, semanticCaption, semanticRow);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("showcase-root");

        Scene scene = new Scene(root, 640, 400);
        scene.getStylesheets().addAll(
                FxKit.tokensStylesheet(),
                stylesheet("showcase.css"));

        stage.setTitle("FXKit Showcase");
        stage.setScene(scene);
        stage.show();
    }

    /** A small colored square whose color is a token, with the token name as tooltip. */
    private static Region swatch(String token) {
        Region box = new Region();
        box.setPrefSize(40, 24);
        box.setMinSize(40, 24);
        box.setStyle("-fx-background-color: " + token + ";"
                + " -fx-background-radius: 4;"
                + " -fx-border-color: -fxk-border; -fx-border-radius: 4;");
        Tooltip.install(box, new Tooltip(token));
        return box;
    }

    /** Resolves a stylesheet that lives in this module, next to this class. */
    private static String stylesheet(String name) {
        return Objects.requireNonNull(ShowcaseApp.class.getResource(name),
                name + " not found next to ShowcaseApp").toExternalForm();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
