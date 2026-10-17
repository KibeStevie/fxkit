package dev.fxkit.core.components.hr;

import dev.fxkit.core.internal.ClassNameStyler;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A short, thick, rounded horizontal rule, centered in the space it is given. Flowbite React's
 * {@code <HRTrimmed />}.
 *
 * <pre>{@code
 * VBox page = new VBox(new Label("First paragraph"), new FxHrTrimmed(), new Label("Second paragraph"));
 * }</pre>
 *
 * <p>The bar is 192px wide and 4px high ({@code w-48 h-1 rounded}); in a narrower parent it shrinks
 * to fit. Vertical spacing is 40px above and below (Flowbite's {@code md:my-10}: a desktop window is
 * always at least Flowbite's {@code md} width).
 *
 * <h2>className</h2>
 * As in Flowbite, {@link #classNameProperty() className} styles the bar itself, so
 * {@code setClassName("bg-blue-500")} recolors it. See {@link ClassNameStyler} for the tokens.
 */
public class FxHrTrimmed extends StackPane {

    private final Region bar = new Region();
    private final StringProperty className = new SimpleStringProperty(this, "className");

    /** Creates a trimmed horizontal rule with default styling. */
    public FxHrTrimmed() {
        getStyleClass().add("fxk-hr-trimmed");
        bar.getStyleClass().add("fxk-hr-trimmed-bar");
        getChildren().add(bar);

        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(bar, newValue));
    }

    /**
     * @param className utility tokens applied to the bar, see {@link #classNameProperty()}
     */
    public FxHrTrimmed(String className) {
        this();
        setClassName(className);
    }

    /** @return the utility tokens applied to the bar; {@code null} for none */
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
