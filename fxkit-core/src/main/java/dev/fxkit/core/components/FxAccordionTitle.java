package dev.fxkit.core.components;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * The clickable header row of an {@link FxAccordionPanel}: an optional leading graphic, the title
 * text, and a chevron that rotates 180 degrees while the panel is open (Flowbite's
 * {@code AccordionTitle}).
 *
 * <p>It is an {@link HBox} rather than a {@code Button} so the text can wrap and the chevron can be
 * pushed to the far end without fighting Modena's button skin. It stays keyboard accessible: it is
 * focus-traversable, Space/Enter toggle it, and it reports the BUTTON role and its expanded state to
 * assistive technology.
 *
 * <pre>{@code
 * FxAccordionTitle title = new FxAccordionTitle("What is FXKit?");
 * title.setGraphic(myIcon);   // optional leading icon
 * }</pre>
 *
 * <p>Styling lives in {@code components.css} ({@code .fxk-accordion-title}).
 */
public class FxAccordionTitle extends HBox {

    /** A 24x24 chevron-down, stroked by CSS (same approach as FxBreadcrumb's chevron). */
    private static final String CHEVRON = "M6 9 L12 15 L18 9";

    private final Label label = new Label();
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    /** Set by the owning panel; runs when the user clicks or presses Space/Enter. */
    private Runnable activationHandler = () -> { };

    public FxAccordionTitle() {
        this("");
    }

    public FxAccordionTitle(String text) {
        getStyleClass().add("fxk-accordion-title");
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.BUTTON);

        label.getStyleClass().add("fxk-accordion-title-label");
        label.setText(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        SVGPath chevron = new SVGPath();
        chevron.setContent(CHEVRON);
        chevron.getStyleClass().add("fxk-accordion-arrow");
        StackPane arrowBox = new StackPane(chevron);
        arrowBox.getStyleClass().add("fxk-accordion-arrow-box");

        getChildren().addAll(label, arrowBox);

        graphic.addListener((obs, old, node) -> {
            if (old != null) {
                getChildren().remove(old);
            }
            if (node != null) {
                getChildren().add(0, node);
            }
        });

        BooleanStyleClassSync.sync(this, "fxk-accordion-title-open", expanded);

        // Clicking moves focus here (HBox does not do that by itself) so the focus ring and
        // keyboard toggling work right after a click, like a Button.
        setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                requestFocus();
            }
        });
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                activationHandler.run();
            }
        });
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                activationHandler.run();
                e.consume();
            }
        });
    }

    /** The title text. */
    public final StringProperty textProperty() {
        return label.textProperty();
    }

    public final String getText() {
        return label.getText();
    }

    public final void setText(String text) {
        label.setText(text);
    }

    /** Optional node shown before the text (Flowbite's title icon). {@code null} for none. */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final Node getGraphic() {
        return graphic.get();
    }

    public final void setGraphic(Node node) {
        graphic.set(node);
    }

    /** Whether the owning panel is open. Driven by the panel, so read-only for callers. */
    public final boolean isExpanded() {
        return expanded.get();
    }

    /** Package-private: {@link FxAccordionPanel} binds this to its own {@code open} property. */
    final BooleanProperty expandedProperty() {
        return expanded;
    }

    /** Package-private: the panel registers what a click or Space/Enter should do. */
    final void setActivationHandler(Runnable handler) {
        this.activationHandler = handler == null ? () -> { } : handler;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> label.getText();
            case EXPANDED -> isExpanded();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }
}
