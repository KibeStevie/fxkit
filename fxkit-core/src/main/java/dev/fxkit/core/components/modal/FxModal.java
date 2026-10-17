package dev.fxkit.core.components.modal;

import java.util.List;
import java.util.Objects;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * A dialog with a header, body and footer. Port of Flowbite React's {@code <Modal>}.
 *
 * <pre>{@code
 * FxModal modal = new FxModal();
 * modal.setHeader(new FxModalHeader("Terms of Service"));
 * modal.setBody(new FxModalBody(new Label("...")));
 * modal.setFooter(new FxModalFooter(accept, decline));
 * modal.show(anyNodeInTheScene);
 * }</pre>
 *
 * <h2>Flowbite mapping</h2>
 * <ul>
 *   <li>{@code show} = {@link #show(Node)} / {@link #close()} / {@link #showingProperty()}.
 *       Flowbite's modal is controlled by React state; a JavaFX dialog is opened and closed by
 *       calls, and {@code showing} is the read-only state.</li>
 *   <li>{@code onClose} = {@link #onCloseProperty()}: fired for the header's close button, Esc and
 *       (when {@link #dismissibleProperty() dismissible}) a click on the backdrop.</li>
 *   <li>{@code dismissible}, {@code popup}, {@code size}, {@code position}, {@code initialFocus} =
 *       properties of the same name.</li>
 *   <li>{@code <ModalHeader>/<ModalBody>/<ModalFooter>} = {@link FxModalHeader},
 *       {@link FxModalBody}, {@link FxModalFooter}, set with {@link #setHeader(Node)} etc.</li>
 * </ul>
 *
 * <h2>onClose</h2>
 * The handler runs first; afterwards the modal hides, unless the handler consumed the event. With no
 * handler the modal simply hides. (In Flowbite the handler <em>is</em> what hides the modal, because
 * React state owns visibility; consuming the event is the JavaFX way to veto a close, for example on
 * unsaved changes.)
 *
 * <h2>How it works</h2>
 * The modal is a transparent, undecorated, owner-bound {@link Stage} ({@link Modality#WINDOW_MODAL})
 * that tracks the owner scene's bounds. Its root paints the backdrop and lays the card out by
 * {@link Position} and {@link Size}. Like {@code FxDropdown}'s popup, it is a separate window, so on
 * every {@link #show(Window)} the owner scene's stylesheets and FXKit's {@code dark} root class are
 * copied onto it.
 *
 * <p>Transparent stages need platform support ({@code Platform.isSupported(ConditionalFeature.TRANSPARENT_WINDOW)}).
 *
 * <p>Call all methods on the JavaFX Application Thread.
 */
public class FxModal {

    public static final String STYLE_CLASS = "fxk-modal-card";

    /** Flowbite's {@code size}: the max width of the card ({@code max-w-sm} ... {@code max-w-7xl}). */
    public enum Size {
        SM(384), MD(448), LG(512), XL(576),
        XL2(672), XL3(768), XL4(896), XL5(1024), XL6(1152), XL7(1280);

        private final double maxWidth;

        Size(double maxWidth) {
            this.maxWidth = maxWidth;
        }

        /** @return the widest the card gets, in px; it is narrower when the window is */
        public double getMaxWidth() {
            return maxWidth;
        }
    }

    /** Flowbite's {@code position}: where the card sits in the window. */
    public enum Position {
        TOP_LEFT(Pos.TOP_LEFT), TOP_CENTER(Pos.TOP_CENTER), TOP_RIGHT(Pos.TOP_RIGHT),
        CENTER_LEFT(Pos.CENTER_LEFT), CENTER(Pos.CENTER), CENTER_RIGHT(Pos.CENTER_RIGHT),
        BOTTOM_LEFT(Pos.BOTTOM_LEFT), BOTTOM_CENTER(Pos.BOTTOM_CENTER), BOTTOM_RIGHT(Pos.BOTTOM_RIGHT);

        private final Pos pos;

        Position(Pos pos) {
            this.pos = pos;
        }
    }

    public static final Size DEFAULT_SIZE = Size.XL2;
    public static final Position DEFAULT_POSITION = Position.CENTER;

    /** Flowbite's content wrapper {@code p-4}: the card never touches the window edge. */
    private static final double GUTTER = 16;

    /** {@code max-h-[90dvh]} */
    private static final double MAX_HEIGHT_RATIO = 0.9;

    // ---- properties ----------------------------------------------------------------------------

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);
    private final ObjectProperty<Position> position = new SimpleObjectProperty<>(this, "position", DEFAULT_POSITION);
    private final BooleanProperty dismissible = new SimpleBooleanProperty(this, "dismissible", false);
    private final BooleanProperty popup = new SimpleBooleanProperty(this, "popup", false);
    private final ObjectProperty<Node> initialFocus = new SimpleObjectProperty<>(this, "initialFocus");
    private final ObjectProperty<EventHandler<ActionEvent>> onClose = new SimpleObjectProperty<>(this, "onClose");
    private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header");
    private final ObjectProperty<Node> body = new SimpleObjectProperty<>(this, "body");
    private final ObjectProperty<Node> footer = new SimpleObjectProperty<>(this, "footer");
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    // ---- parts (card must exist before the layer, which adds it) ---------------------------------

    private final VBox card = new VBox();
    private final BodyScroll bodyScroll = new BodyScroll();
    private final ModalLayer layer = new ModalLayer();
    private final FadeTransition fadeIn = new FadeTransition(Duration.millis(150), layer);

    // ---- state while showing ---------------------------------------------------------------------

    private Stage stage;
    private Window owner;
    private Scene ownerScene;
    private final InvalidationListener boundsListener = observable -> syncBounds();
    private final ChangeListener<Boolean> ownerShowingListener = (observable, wasShowing, isShowing) -> {
        if (!isShowing) {
            close();
        }
    };

    public FxModal() {
        card.getStyleClass().add(STYLE_CLASS);
        BooleanStyleClassSync.sync(card, "fxk-modal-popup", popup);

        bodyScroll.getStyleClass().add("fxk-modal-scroll");
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        bodyScroll.setFocusTraversable(false);

        header.addListener((observable, oldValue, newValue) -> refresh());
        body.addListener((observable, oldValue, newValue) -> refresh());
        footer.addListener((observable, oldValue, newValue) -> refresh());
        size.addListener((observable, oldValue, newValue) -> layer.requestLayout());
        position.addListener((observable, oldValue, newValue) -> layer.requestLayout());

        layer.setFocusTraversable(true); // so a modal without initialFocus does not light up its close button
        layer.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            // Only the backdrop itself: clicks inside the card have a descendant of the card as target.
            if (isDismissible() && event.getTarget() == layer) {
                requestClose();
            }
        });

        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        refresh();
    }

    // ---- showing ---------------------------------------------------------------------------------

    /**
     * Opens the modal over the window that contains {@code anchor}.
     *
     * @throws IllegalStateException if {@code anchor} is not in a scene that is in a window
     */
    public void show(Node anchor) {
        Objects.requireNonNull(anchor, "anchor");
        Scene scene = anchor.getScene();
        if (scene == null || scene.getWindow() == null) {
            throw new IllegalStateException("anchor is not in a showing window");
        }
        show(scene.getWindow());
    }

    /**
     * Opens the modal over {@code owner}, blocking input to it. Does nothing if already open.
     *
     * @throws IllegalStateException if the owner is not showing or has no scene
     */
    public void show(Window owner) {
        Objects.requireNonNull(owner, "owner");
        if (stage != null) {
            return;
        }
        if (!owner.isShowing() || owner.getScene() == null) {
            throw new IllegalStateException("owner is not a showing window with a scene");
        }

        this.owner = owner;
        this.ownerScene = owner.getScene();

        // initOwner/initStyle only work before a stage's first show, so each opening gets a new Stage.
        Stage st = new Stage(StageStyle.TRANSPARENT);
        st.initOwner(owner);
        st.initModality(Modality.WINDOW_MODAL);
        Scene scene = new Scene(layer);
        scene.setFill(Color.TRANSPARENT);
        syncTheme(scene, ownerScene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && !event.isConsumed()) {
                event.consume();
                requestClose();
            }
        });
        st.setScene(scene);
        st.setOnHidden(event -> cleanup());
        stage = st;

        owner.xProperty().addListener(boundsListener);
        owner.yProperty().addListener(boundsListener);
        owner.widthProperty().addListener(boundsListener);
        owner.heightProperty().addListener(boundsListener);
        ownerScene.widthProperty().addListener(boundsListener);
        ownerScene.heightProperty().addListener(boundsListener);
        owner.showingProperty().addListener(ownerShowingListener);

        syncBounds();
        layer.setOpacity(0); // invisible until sized, then faded in
        st.show();
        syncBounds();
        showing.set(true);
        fadeIn.playFromStart();

        Platform.runLater(() -> {
            Node target = getInitialFocus();
            if (target != null && target.getScene() == scene) {
                target.requestFocus();
            } else {
                layer.requestFocus();
            }
        });
    }

    /** Hides the modal without running {@link #onCloseProperty() onClose}. */
    public void close() {
        Stage st = stage;
        if (st != null) {
            st.hide();
            cleanup();
        }
    }

    /**
     * What the close button, Esc and a backdrop click call: runs {@link #onCloseProperty() onClose},
     * then hides the modal unless the handler consumed the event.
     */
    public void requestClose() {
        if (stage == null) {
            return;
        }
        ActionEvent event = new ActionEvent(this, layer);
        EventHandler<ActionEvent> handler = getOnClose();
        if (handler != null) {
            handler.handle(event);
        }
        if (!event.isConsumed()) {
            close();
        }
    }

    private void cleanup() {
        Stage st = stage;
        if (st == null) {
            return;
        }
        stage = null;
        fadeIn.stop();

        if (owner != null) {
            owner.xProperty().removeListener(boundsListener);
            owner.yProperty().removeListener(boundsListener);
            owner.widthProperty().removeListener(boundsListener);
            owner.heightProperty().removeListener(boundsListener);
            owner.showingProperty().removeListener(ownerShowingListener);
        }
        if (ownerScene != null) {
            ownerScene.widthProperty().removeListener(boundsListener);
            ownerScene.heightProperty().removeListener(boundsListener);
        }
        owner = null;
        ownerScene = null;

        // A node can be the root of one scene only; free the layer for the next opening.
        Scene scene = st.getScene();
        if (scene != null) {
            scene.setRoot(new Region());
        }
        showing.set(false);
    }

    /** The stage covers the owner scene's content area exactly. */
    private void syncBounds() {
        Stage st = stage;
        Window o = owner;
        if (st == null || o == null || o.getScene() == null) {
            return;
        }
        Scene s = o.getScene();
        st.setX(o.getX() + s.getX());
        st.setY(o.getY() + s.getY());
        st.setWidth(s.getWidth());
        st.setHeight(s.getHeight());
    }

    /** Same as FxDropdown: stylesheets and the {@code dark} root class do not cross a window boundary. */
    private void syncTheme(Scene modalScene, Scene ownerScene) {
        modalScene.setUserAgentStylesheet(ownerScene.getUserAgentStylesheet());
        modalScene.getStylesheets().setAll(ownerScene.getStylesheets());

        List<String> rootClasses = layer.getStyleClass();
        if (!rootClasses.contains("root")) {
            rootClasses.add("root");
        }
        rootClasses.remove(ThemeManager.DARK_CLASS);
        if (ThemeManager.current(ownerScene) == Theme.DARK) {
            rootClasses.add(ThemeManager.DARK_CLASS);
        }
    }

    // ---- content ---------------------------------------------------------------------------------

    private void refresh() {
        card.getChildren().clear();

        Node headerNode = getHeader();
        if (headerNode != null) {
            if (headerNode instanceof FxModalHeader modalHeader) {
                modalHeader.setCloseAction(this::requestClose);
            }
            card.getChildren().add(headerNode);
        }

        Node bodyNode = getBody();
        bodyScroll.setContent(bodyNode);
        if (bodyNode != null) {
            card.getChildren().add(bodyScroll);
            VBox.setVgrow(bodyScroll, javafx.scene.layout.Priority.ALWAYS);
        }

        Node footerNode = getFooter();
        if (footerNode != null) {
            card.getChildren().add(footerNode);
        }
        layer.requestLayout();
    }

    /**
     * Body wrapper: Flowbite's {@code flex-1 overflow-auto}. A ScrollPane reports the pref height of
     * its content at an unknown width, which is wrong for wrapped text, so the pref height is asked of
     * the content at the width it will really get.
     */
    private static final class BodyScroll extends ScrollPane {
        @Override
        protected double computePrefHeight(double width) {
            Node content = getContent();
            if (content == null) {
                return super.computePrefHeight(width);
            }
            double inner = width < 0 ? -1 : Math.max(0, width - snappedLeftInset() - snappedRightInset());
            return content.prefHeight(inner) + snappedTopInset() + snappedBottomInset();
        }
    }

    /** Backdrop + positioned card. Is the scene root of the modal stage. */
    private final class ModalLayer extends Pane {
        ModalLayer() {
            getStyleClass().add("fxk-modal-layer");
            getChildren().add(card);
        }

        @Override
        protected void layoutChildren() {
            double areaW = getWidth();
            double areaH = getHeight();
            Size s = getSize() == null ? DEFAULT_SIZE : getSize();
            Position p = getPosition() == null ? DEFAULT_POSITION : getPosition();

            double w = Math.min(s.getMaxWidth(), Math.max(0, areaW - 2 * GUTTER));
            double maxH = Math.max(0, Math.min(areaH * MAX_HEIGHT_RATIO, areaH - 2 * GUTTER));
            double h = Math.min(card.prefHeight(w), maxH);

            HPos hpos = p.pos.getHpos();
            VPos vpos = p.pos.getVpos();
            double x = switch (hpos) {
                case LEFT -> GUTTER;
                case RIGHT -> areaW - GUTTER - w;
                default -> (areaW - w) / 2;
            };
            double y = switch (vpos) {
                case TOP -> GUTTER;
                case BOTTOM -> areaH - GUTTER - h;
                default -> (areaH - h) / 2;
            };
            card.resizeRelocate(snapPositionX(x), snapPositionY(y), snapSizeX(w), snapSizeY(h));
        }
    }

    // ---- properties API --------------------------------------------------------------------------

    /** @return the card's max width, default {@link Size#XL2} (Flowbite's {@code 2xl}) */
    public final ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public final Size getSize() {
        return size.get();
    }

    public final void setSize(Size size) {
        this.size.set(size);
    }

    /** @return where the card sits, default {@link Position#CENTER} */
    public final ObjectProperty<Position> positionProperty() {
        return position;
    }

    public final Position getPosition() {
        return position.get();
    }

    public final void setPosition(Position position) {
        this.position.set(position);
    }

    /** @return whether a click on the backdrop closes the modal, default {@code false} */
    public final BooleanProperty dismissibleProperty() {
        return dismissible;
    }

    public final boolean isDismissible() {
        return dismissible.get();
    }

    public final void setDismissible(boolean dismissible) {
        this.dismissible.set(dismissible);
    }

    /**
     * @return Flowbite's {@code popup}: a compact confirmation look. The header loses its border and
     *         most of its padding, the body loses its top padding and the footer its top border.
     */
    public final BooleanProperty popupProperty() {
        return popup;
    }

    public final boolean isPopup() {
        return popup.get();
    }

    public final void setPopup(boolean popup) {
        this.popup.set(popup);
    }

    /** @return the node that gets focus when the modal opens; {@code null} focuses the modal itself */
    public final ObjectProperty<Node> initialFocusProperty() {
        return initialFocus;
    }

    public final Node getInitialFocus() {
        return initialFocus.get();
    }

    public final void setInitialFocus(Node initialFocus) {
        this.initialFocus.set(initialFocus);
    }

    /** @return the close handler, see the class comment */
    public final ObjectProperty<EventHandler<ActionEvent>> onCloseProperty() {
        return onClose;
    }

    public final EventHandler<ActionEvent> getOnClose() {
        return onClose.get();
    }

    public final void setOnClose(EventHandler<ActionEvent> onClose) {
        this.onClose.set(onClose);
    }

    public final ObjectProperty<Node> headerProperty() {
        return header;
    }

    public final Node getHeader() {
        return header.get();
    }

    /** Usually an {@link FxModalHeader}, which gets its close button wired to this modal. */
    public final void setHeader(Node header) {
        this.header.set(header);
    }

    /** Shortcut for {@code setHeader(new FxModalHeader(title))}. */
    public final void setTitle(String title) {
        setHeader(new FxModalHeader(title));
    }

    public final ObjectProperty<Node> bodyProperty() {
        return body;
    }

    public final Node getBody() {
        return body.get();
    }

    /** Usually an {@link FxModalBody}. Scrolls when taller than the space left in the card. */
    public final void setBody(Node body) {
        this.body.set(body);
    }

    public final ObjectProperty<Node> footerProperty() {
        return footer;
    }

    public final Node getFooter() {
        return footer.get();
    }

    /** Usually an {@link FxModalFooter}. */
    public final void setFooter(Node footer) {
        this.footer.set(footer);
    }

    /** @return whether the modal is currently open */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    public final boolean isShowing() {
        return showing.get();
    }
}
