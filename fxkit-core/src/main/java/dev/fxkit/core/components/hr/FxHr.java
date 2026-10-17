package dev.fxkit.core.components.hr;

import dev.fxkit.core.internal.ClassNameStyler;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.Region;

/**
 * A horizontal rule that separates blocks of content. Flowbite React's {@code <HR />}.
 *
 * <pre>{@code
 * VBox page = new VBox(new Label("First paragraph"), new FxHr(), new Label("Second paragraph"));
 * }</pre>
 *
 * <p>The node is a {@link Region} that fills the width it is given: a 1px line in the theme's border
 * color, with 32px of space above and below (Flowbite's {@code my-8 h-px}). The line color follows
 * the light/dark theme with no extra code.
 *
 * <p>The other Flowbite variants are separate components, as in Flowbite itself:
 * {@link FxHrTrimmed}, {@link FxHrIcon} and {@link FxHrText}.
 *
 * <h2>className</h2>
 * Like Flowbite's {@code className}, {@link #classNameProperty() className} takes utility tokens that
 * are resolved by {@link ClassNameStyler}. {@code bg-*} recolors the line, for example
 * {@code setClassName("bg-red-500")} or {@code "bg-primary"}.
 */
public class FxHr extends Region {

    private final StringProperty className = new SimpleStringProperty(this, "className");

    /** Creates a horizontal rule with default styling. */
    public FxHr() {
        getStyleClass().add("fxk-hr");
        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(this, newValue));
    }

    /**
     * @param className utility tokens applied on top of the default styling, see {@link #classNameProperty()}
     */
    public FxHr(String className) {
        this();
        setClassName(className);
    }

    /** @return the utility tokens applied on top of the default styling; {@code null} for none */
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
