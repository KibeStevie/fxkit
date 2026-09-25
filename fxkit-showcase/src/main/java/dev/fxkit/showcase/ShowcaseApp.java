package dev.fxkit.showcase;

import dev.fxkit.core.FxKit;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
 * <p>Styling comes from FXKit itself: {@link ThemeManager#apply} installs the token and
 * utility stylesheets and picks the theme, and the nodes below carry utility classes.
 * {@code showcase.css} only holds the few rules utilities cannot express.
 *
 * <p>For now the window shows every page stacked in one scroll: the color palette, then a page per
 * component ({@code FxButton}'s is {@link ButtonPage}, {@code FxCard}'s is {@link CardPage}), then
 * {@link FxmlDemoPage} proving both work from FXML. The navigation shell that hosts one page at a time
 * arrives in Phase 3.
 */
public class ShowcaseApp extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("FXKit Showcase");
        title.getStyleClass().addAll("text-2xl", "font-bold", "text-body");

        // Uses a class from fxkit-core, proving the module dependency works.
        Label version = new Label("core " + FxKit.version());
        version.getStyleClass().addAll("text-sm", "text-muted");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeToggle = new Button();
        themeToggle.getStyleClass().addAll("bg-primary", "text-on-primary", "rounded-md", "p-2", "font-semibold");

        HBox header = new HBox(12, title, version, spacer, themeToggle);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().addAll("bg-surface", "p-4");

        VBox pages = new VBox(PalettePage.create(), ButtonPage.create(), CardPage.create(), FxmlDemoPage.create());

        ScrollPane scroll = new ScrollPane(pages);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("showcase-scroll");

        BorderPane root = new BorderPane(scroll);
        root.setTop(header);
        root.getStyleClass().add("bg-background");

        Scene scene = new Scene(root, 960, 720);
        ThemeManager.apply(scene, Theme.LIGHT);          // installs tokens.css + utilities.css
        scene.getStylesheets().add(stylesheet("showcase.css"));

        themeToggle.setText(toggleCaption(ThemeManager.current(scene)));
        themeToggle.setOnAction(e -> themeToggle.setText(toggleCaption(ThemeManager.toggle(scene))));

        stage.setTitle("FXKit Showcase");
        stage.setScene(scene);
        stage.show();
    }

    /** The button offers the theme you would switch TO. */
    private static String toggleCaption(Theme current) {
        return current == Theme.LIGHT ? "Switch to dark theme" : "Switch to light theme";
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
