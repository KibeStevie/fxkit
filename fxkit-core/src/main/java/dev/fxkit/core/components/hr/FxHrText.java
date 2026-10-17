package dev.fxkit.core.components.hr;

import dev.fxkit.core.internal.ClassNameStyler;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A horizontal rule with text in the middle. Flowbite React's {@code <HRText text="or" />}.
 *
 * <pre>{@code
 * VBox page = new VBox(new Label("Sign in"), new FxHrText("or"), new Label("Create an account"));
 * }</pre>
 *
 * <p>Two 1px bars with the text between them, at most 256px wide ({@code w-64}), centered, with 32px
 * above and below ({@code my-8}). The text is medium weight in the theme's text color, with 12px of
 * space on each side ({@code px-3}). If the text is wider than the rule, the rule grows to fit it and
 * the bars shrink away; the text is never cut off.
 *
 * <p>Flowbite hides the middle of the line behind a page-colored background. Here the line is simply
 * two bars with the text laid out between them, so it needs no background color and looks right on
 * any surface, in either theme.
 *
 * <h2>className</h2>
 * As in Flowbite, {@link #classNameProperty() className} goes on the outer wrapper. See
 * {@link ClassNameStyler}.
 */
public class FxHrText extends StackPane {

    private final Label label = new Label();
    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final StringProperty className = new SimpleStringProperty(this, "className");

    /** Creates a rule with no text. */
    public FxHrText() {
        this("");
    }

    /**
     * @param text the text shown in the middle of the rule
     */
    public FxHrText(String text) {
        getStyleClass().add("fxk-hr-text");
        label.getStyleClass().add("fxk-hr-text-label");
        label.setMinWidth(Region.USE_PREF_SIZE); // never ellipsize: the bars give way instead

        HBox content = new HBox(bar(), label, bar());
        content.getStyleClass().add("fxk-hr-text-content");
        getChildren().add(content);

        label.textProperty().bind(this.text);
        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(this, newValue));

        setText(text);
    }

    private static Region bar() {
        Region bar = new Region();
        bar.getStyleClass().add("fxk-hr-text-bar");
        HBox.setHgrow(bar, Priority.ALWAYS);
        return bar;
    }

    /** @return the text shown in the middle of the rule */
    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String value) {
        text.set(value);
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
