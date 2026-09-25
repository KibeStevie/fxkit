package dev.fxkit.core.components;

import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A surface for grouping related content into header, body and footer sections, styled by FXKit's
 * design tokens with a typed {@link Elevation} property.
 *
 * <p>{@code FxCard} is a plain subclass of {@link VBox} (see {@code docs/adr/ADR-003-card-base-class.md}
 * for why): the three slots are each held in their own wrapper so that CSS can target
 * {@code .fxk-card-header}/{@code .fxk-card-body}/{@code .fxk-card-footer} independently of whatever
 * content they hold, and a thin divider ({@code .fxk-card-divider}) is inserted between whichever slots
 * are actually present. A slot that is never set, or is set back to {@code null}, contributes nothing to
 * the layout: no wrapper, no padding and no divider for it.
 *
 * <p>Requires FXKit's stylesheets to be installed on the scene (see
 * {@code dev.fxkit.core.theme.ThemeManager#apply}); without them the card falls back to a plain,
 * unstyled {@code VBox}.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxCard card = new FxCard();
 * card.setHeader(new Label("Delete account"));
 * card.setBody(new Label("This can't be undone."));
 * card.setFooter(new FxButton("Delete", FxButton.Variant.DANGER));
 * card.setElevation(FxCard.Elevation.LG);
 * }</pre>
 *
 * <h2>FXML</h2>
 * The {@link #bodyProperty() body} slot is this class's {@linkplain DefaultProperty default property},
 * so a single nested element with no name sets it; {@link #headerProperty() header} and
 * {@link #footerProperty() footer} need to be named explicitly.
 * <pre>{@code
 * <FxCard elevation="MD">
 *     <header><Label text="Delete account"/></header>
 *     <body><Label text="This can't be undone." wrapText="true"/></body>
 *     <footer><FxButton text="Delete" variant="DANGER"/></footer>
 * </FxCard>
 * }</pre>
 */
@DefaultProperty("body")
public class FxCard extends VBox {

    /** Base style class every {@code FxCard} carries, regardless of which slots are filled. */
    public static final String STYLE_CLASS = "fxk-card";

    /** Style class on the wrapper around whatever node is set as {@link #headerProperty()}. */
    public static final String HEADER_STYLE_CLASS = "fxk-card-header";

    /** Style class on the wrapper around whatever node is set as {@link #bodyProperty()}. */
    public static final String BODY_STYLE_CLASS = "fxk-card-body";

    /** Style class on the wrapper around whatever node is set as {@link #footerProperty()}. */
    public static final String FOOTER_STYLE_CLASS = "fxk-card-footer";

    /** Style class on the {@link Separator} placed between two adjacent present slots. */
    public static final String DIVIDER_STYLE_CLASS = "fxk-card-divider";

    /** Prefix used for the elevation style class, e.g. {@code fxk-card-elevation-lg} for {@link Elevation#LG}. */
    private static final String ELEVATION_STYLE_CLASS_PREFIX = "fxk-card-elevation-";

    /** The default elevation a new {@code FxCard} is created with. Matches the default a hand-styled
     *  card would use (see {@code docs/utility-classes.md}'s {@code .shadow-md} example). */
    public static final Elevation DEFAULT_ELEVATION = Elevation.MD;

    /**
     * How far a card appears to sit above the surface behind it. Each level maps to one of FXKit's
     * shadow tokens ({@code docs/design-tokens.md}), so elevation stays consistent with every other
     * component and follows the light/dark theme automatically.
     */
    public enum Elevation {

        /** No shadow: the card reads as flush with the surface behind it, relying on its border alone. */
        NONE,

        /** A subtle lift, using {@code -fxk-shadow-sm}. For cards inside an already-elevated container. */
        SM,

        /** The default lift, using {@code -fxk-shadow-md}. Suits most standalone cards. */
        MD,

        /** A pronounced lift, using {@code -fxk-shadow-lg}. For a card meant to draw the eye, e.g. a modal-like panel. */
        LG
    }

    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");
    private final ObjectProperty<Node> body = new SimpleObjectProperty<>(this, "body");
    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(this, "footer");

    private final ObjectProperty<Elevation> elevation =
            new SimpleObjectProperty<>(this, "elevation", DEFAULT_ELEVATION);

    private final StackPane headerSlot = slot(HEADER_STYLE_CLASS);
    private final StackPane bodySlot = slot(BODY_STYLE_CLASS);
    private final StackPane footerSlot = slot(FOOTER_STYLE_CLASS);

    /** Creates an empty {@code FxCard}: no header, body or footer, and the {@linkplain #DEFAULT_ELEVATION
     *  default elevation}. Fill it with {@link #setHeader}, {@link #setBody} and/or {@link #setFooter}. */
    public FxCard() {
        getStyleClass().add(STYLE_CLASS);
        EnumStyleClassSync.sync(this, ELEVATION_STYLE_CLASS_PREFIX, elevation);

        header.addListener((observable, oldValue, newValue) -> fillSlot(headerSlot, newValue));
        body.addListener((observable, oldValue, newValue) -> fillSlot(bodySlot, newValue));
        footer.addListener((observable, oldValue, newValue) -> fillSlot(footerSlot, newValue));
    }

    private static StackPane slot(String styleClass) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add(styleClass);
        return pane;
    }

    /** Puts (or clears) the slot's content, then rebuilds which wrappers/dividers are actually shown. */
    private void fillSlot(StackPane slot, Node content) {
        if (content == null) {
            slot.getChildren().clear();
        } else {
            slot.getChildren().setAll(content);
        }
        rebuildChildren();
    }

    /**
     * Rebuilds this card's children from scratch: one entry per non-empty slot, in header/body/footer
     * order, with a {@link Separator} inserted between any two adjacent entries. An empty slot is left
     * out entirely, so it takes no space and never produces a stray divider.
     */
    private void rebuildChildren() {
        getChildren().clear();
        for (StackPane slot : new StackPane[] {headerSlot, bodySlot, footerSlot}) {
            if (slot.getChildren().isEmpty()) {
                continue;
            }
            if (!getChildren().isEmpty()) {
                Separator divider = new Separator();
                divider.getStyleClass().add(DIVIDER_STYLE_CLASS);
                getChildren().add(divider);
            }
            getChildren().add(slot);
        }
    }

    /**
     * @return the card's current header, or {@code null} if none is set
     */
    public Node getHeader() {
        return header.get();
    }

    /**
     * Sets the card's header. Pass {@code null} to remove it; an empty header takes no space and leaves
     * no divider behind.
     *
     * @param header the header node, or {@code null} to clear it
     */
    public void setHeader(Node header) {
        this.header.set(header);
    }

    /**
     * @return the header property, for binding, listening, or use as the {@code <header>} FXML element
     */
    public ObjectProperty<Node> headerProperty() {
        return header;
    }

    /**
     * @return the card's current body, or {@code null} if none is set
     */
    public Node getBody() {
        return body.get();
    }

    /**
     * Sets the card's body. Pass {@code null} to remove it; an empty body takes no space and leaves no
     * divider behind.
     *
     * @param body the body node, or {@code null} to clear it
     */
    public void setBody(Node body) {
        this.body.set(body);
    }

    /**
     * @return the body property, for binding, listening, or use as this class's
     *         {@linkplain DefaultProperty default FXML property} (a single unnamed nested element)
     */
    public ObjectProperty<Node> bodyProperty() {
        return body;
    }

    /**
     * @return the card's current footer, or {@code null} if none is set
     */
    public Node getFooter() {
        return footer.get();
    }

    /**
     * Sets the card's footer. Pass {@code null} to remove it; an empty footer takes no space and leaves
     * no divider behind.
     *
     * @param footer the footer node, or {@code null} to clear it
     */
    public void setFooter(Node footer) {
        this.footer.set(footer);
    }

    /**
     * @return the footer property, for binding, listening, or use as the {@code <footer>} FXML element
     */
    public ObjectProperty<Node> footerProperty() {
        return footer;
    }

    /**
     * @return the card's current elevation
     */
    public Elevation getElevation() {
        return elevation.get();
    }

    /**
     * Sets the card's elevation. Applies the new elevation's shadow immediately and removes the previous
     * elevation's style.
     *
     * @param elevation the elevation to apply; must not be {@code null}
     */
    public void setElevation(Elevation elevation) {
        this.elevation.set(elevation);
    }

    /**
     * @return the elevation property, for binding or listening
     */
    public ObjectProperty<Elevation> elevationProperty() {
        return elevation;
    }
}
