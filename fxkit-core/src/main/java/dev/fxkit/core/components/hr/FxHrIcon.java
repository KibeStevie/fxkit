package dev.fxkit.core.components.hr;

import dev.fxkit.core.internal.ClassNameStyler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A horizontal rule with an icon in the middle. Flowbite React's {@code <HRIcon />}.
 *
 * <pre>{@code
 * new FxHrIcon();                                // Flowbite's quotation-mark icon
 * new FxHrIcon(FontAwesomeSolid.STAR);           // any Ikonli icon
 * new FxHrIcon(myNode);                          // any node, e.g. an SVGPath or ImageView
 * }</pre>
 *
 * <p>Two 4px rounded bars, with the icon between them. The whole thing is at most 256px wide
 * ({@code w-64}), centered, with 32px above and below ({@code my-8}).
 *
 * <p>Flowbite hides the middle of the line behind an icon that has a page-colored background. Here
 * the line is simply two bars with the icon laid out between them, so it needs no background color
 * and looks right on any surface, in either theme.
 *
 * <h2>Custom graphics</h2>
 * The default icon and {@link FontIcon}s take the theme's text color and a 16px size. Any other
 * node is shown as it is, so color it yourself.
 *
 * <h2>className</h2>
 * As in Flowbite, {@link #classNameProperty() className} goes on the outer wrapper. See
 * {@link ClassNameStyler}.
 */
public class FxHrIcon extends StackPane {

    /** Flowbite's quotation-mark icon, 18 x 14. */
    private static final String QUOTE_PATH =
            "M6 0H2a2 2 0 0 0-2 2v4a2 2 0 0 0 2 2h4v1a3 3 0 0 1-3 3H2a1 1 0 0 0 0 2h1a5.006 5.006 0 0 0 5-5V2a2 2 0 0 0-2-2Z"
          + "M16 0h-4a2 2 0 0 0-2 2v4a2 2 0 0 0 2 2h4v1a3 3 0 0 1-3 3h-1a1 1 0 0 0 0 2h1a5.006 5.006 0 0 0 5-5V2a2 2 0 0 0-2-2Z";

    private final StackPane iconHolder = new StackPane();
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");
    private final StringProperty className = new SimpleStringProperty(this, "className");

    /** Creates a rule with Flowbite's default quotation-mark icon. */
    public FxHrIcon() {
        this(quoteIcon());
    }

    /**
     * @param graphic the node shown in the middle of the rule; {@code null} for none
     */
    public FxHrIcon(Node graphic) {
        getStyleClass().add("fxk-hr-icon");
        iconHolder.getStyleClass().add("fxk-hr-icon-holder");

        Region left = bar();
        Region right = bar();
        HBox content = new HBox(left, iconHolder, right);
        content.getStyleClass().add("fxk-hr-icon-content");
        getChildren().add(content);

        this.graphic.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                iconHolder.getChildren().clear();
            } else {
                iconHolder.getChildren().setAll(newValue);
            }
        });
        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(this, newValue));

        setGraphic(graphic);
    }

    /**
     * @param icon an Ikonli icon, shown in the middle of the rule at 16px in the theme's text color
     */
    public FxHrIcon(Ikon icon) {
        this(icon == null ? null : new FontIcon(icon));
    }

    private static Region bar() {
        Region bar = new Region();
        bar.getStyleClass().add("fxk-hr-icon-bar");
        HBox.setHgrow(bar, Priority.ALWAYS);
        return bar;
    }

    private static SVGPath quoteIcon() {
        SVGPath path = new SVGPath();
        path.setContent(QUOTE_PATH);
        path.getStyleClass().add("fxk-hr-icon-quote");
        return path;
    }

    /** @return the node shown in the middle of the rule; {@code null} for none */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final Node getGraphic() {
        return graphic.get();
    }

    public final void setGraphic(Node value) {
        graphic.set(value);
    }

    /**
     * Convenience for {@code setGraphic(new FontIcon(icon))}.
     *
     * @param icon an Ikonli icon; {@code null} removes the graphic
     */
    public final void setIcon(Ikon icon) {
        setGraphic(icon == null ? null : new FontIcon(icon));
    }

    /** @return the utility tokens applied to the outer wrapper; {@code null} for none */
    public final StringProperty classNameProperty() {
        return className;
    }

    public final String getClassName() {
        return className.get();
    }

    public final void setClassName(String value) {
        className.set(value);
    }
}
