package dev.fxkit.core.components.popover;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The content layout Flowbite's popover examples build by hand: a shaded title bar with a bottom
 * border, then a padded body in muted text.
 *
 * <pre>{@code
 * <div className="w-64 text-sm text-gray-500 dark:text-gray-400">
 *   <div className="border-b border-gray-200 bg-gray-100 px-3 py-2 ..."><h3 className="font-semibold ...">Popover title</h3></div>
 *   <div className="px-3 py-2"><p>...</p></div>
 * </div>
 * }</pre>
 *
 * <p>Use it as an {@link FxPopover}'s content. It is optional: any node works as content. The default
 * width is 256px ({@code w-64}); change it with {@link #setPrefWidth(double)}. The title bar is only
 * shown while {@link #titleProperty() title} is not empty.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * new FxPopoverPanel("Popover title", "And here's some amazing content. It's very engaging. Right?");
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxPopoverPanel title="Popover title" text="And here's some amazing content."/>
 * }</pre>
 */
@DefaultProperty("body")
public class FxPopoverPanel extends VBox {

    public static final String STYLE_CLASS = "fxk-popover-panel";

    private static final String HEADER_STYLE_CLASS = "fxk-popover-panel-header";
    private static final String TITLE_STYLE_CLASS = "fxk-popover-panel-title";
    private static final String BODY_STYLE_CLASS = "fxk-popover-panel-body";
    private static final String TEXT_STYLE_CLASS = "fxk-popover-panel-text";

    /** Flowbite's {@code w-64}. */
    public static final double DEFAULT_WIDTH = 256;

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty text = new SimpleStringProperty(this, "text");
    private final ObjectProperty<Node> body = new SimpleObjectProperty<>(this, "body");

    private final StackPane header = new StackPane();
    private final Label titleLabel = new Label();
    private final StackPane bodyHolder = new StackPane();
    private final Label textLabel = new Label();

    public FxPopoverPanel() {
        getStyleClass().add(STYLE_CLASS);
        setPrefWidth(DEFAULT_WIDTH);
        setMinWidth(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);

        header.getStyleClass().add(HEADER_STYLE_CLASS);
        titleLabel.getStyleClass().add(TITLE_STYLE_CLASS);
        titleLabel.setWrapText(true);
        titleLabel.textProperty().bind(title);
        header.getChildren().add(titleLabel);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        bodyHolder.getStyleClass().add(BODY_STYLE_CLASS);
        bodyHolder.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        textLabel.getStyleClass().add(TEXT_STYLE_CLASS);
        textLabel.setWrapText(true);
        textLabel.textProperty().bind(text);

        title.addListener((obs, oldValue, newValue) -> refresh());
        text.addListener((obs, oldValue, newValue) -> refresh());
        body.addListener((obs, oldValue, newValue) -> refresh());
        refresh();
    }

    public FxPopoverPanel(String title, String text) {
        this();
        setTitle(title);
        setText(text);
    }

    public FxPopoverPanel(String title, Node body) {
        this();
        setTitle(title);
        setBody(body);
    }

    private void refresh() {
        boolean hasTitle = title.get() != null && !title.get().isEmpty();
        Node custom = body.get();
        boolean hasText = text.get() != null && !text.get().isEmpty();

        getChildren().clear();
        if (hasTitle) {
            getChildren().add(header);
        }
        if (custom != null) {
            bodyHolder.getChildren().setAll(custom);
            getChildren().add(bodyHolder);
        } else if (hasText) {
            bodyHolder.getChildren().setAll(textLabel);
            getChildren().add(bodyHolder);
        }
    }

    public String getTitle() {
        return title.get();
    }

    /** The heading in the shaded title bar; {@code null} or empty hides the bar. */
    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getText() {
        return text.get();
    }

    /** Wrapped body text in the muted color. Ignored while a {@link #bodyProperty() body} node is set. */
    public void setText(String text) {
        this.text.set(text);
    }

    public StringProperty textProperty() {
        return text;
    }

    public Node getBody() {
        return body.get();
    }

    /** A node to show as the body instead of {@link #textProperty() text}. */
    public void setBody(Node body) {
        this.body.set(body);
    }

    public ObjectProperty<Node> bodyProperty() {
        return body;
    }
}
