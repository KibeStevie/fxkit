package dev.fxkit.showcase;

import dev.fxkit.core.FxKit;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * "Hello JavaFX": the smallest possible FXKit showcase window.
 *
 * <p>JavaFX lifecycle in short: {@code main} calls {@link #launch(String...)},
 * JavaFX starts its UI thread and then calls {@link #start(Stage)} with the
 * primary {@link Stage} (the window). A Stage shows one {@link Scene}, and the
 * Scene holds a tree of nodes (here: a VBox containing two Labels).
 */
public class ShowcaseApp extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Hello, FXKit!");
        // Temporary inline style. From Phase 1 on this will come from FXKit's CSS tokens.
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Uses a class from fxkit-core, proving the module dependency works.
        Label subtitle = new Label("Core library version " + FxKit.version());

        VBox root = new VBox(12, title, subtitle);
        root.setAlignment(Pos.CENTER);

        stage.setTitle("FXKit Showcase");
        stage.setScene(new Scene(root, 640, 400));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
