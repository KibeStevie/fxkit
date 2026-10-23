package dev.fxkit.core.components.card;

import java.util.function.Function;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.ClassNameStyler;
import dev.fxkit.core.internal.ImageSources;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A surface that groups content: Flowbite React's {@code <Card>}. A rounded, bordered, shadowed box
 * with an optional picture ({@link #imgSrcProperty()}), a vertical or {@link #horizontalProperty()
 * horizontal} layout, and an optional {@link #hrefProperty() link} behavior.
 *
 * <p>The content goes in {@link #getContent()} (Flowbite's {@code children}): a vertical box with
 * 24px padding and a 16px gap, vertically centered, whose children are as wide as the card. The
 * card draws nothing for the content itself, so any node works: labels, an {@code FxButton}, a form,
 * a list. Titles and paragraphs are ordinary labels styled with utility classes
 * ({@code text-2xl font-bold text-body}).
 *
 * <p>The card has no width of its own, like a {@code <div>}: it is as wide as its content wants. Give
 * it a width the way Flowbite examples use {@code className="max-w-sm"}:
 * {@code card.setMaxWidth(384)} (and {@code setPrefWidth(384)} to make it exactly that wide even when
 * its content is narrower).
 *
 * <h2>Picture</h2>
 * {@link #imgSrcProperty()} is loaded through {@code ImageSources}, so {@code FxImagePolicy} decides
 * which URLs are acceptable. A refused, missing or broken picture is not drawn: the card just has no
 * picture. {@link #imgAltProperty()} is the picture's accessible text.
 *
 * <p>Vertically the picture is as wide as the card and as tall as its aspect ratio says, with rounded
 * top corners. Horizontally it is a 192px column (Flowbite's {@code md:w-48}) as tall as the card,
 * cropped to fill it ({@code object-cover}), with rounded left corners.
 *
 * <p>{@link #renderImageProperty()} replaces the picture with a node of your own (Flowbite's
 * {@code renderImage}). It is called again when {@code horizontal} changes, so it can choose a node
 * for the layout. An {@link ImageView} is fitted like the built-in picture; any other resizable node
 * is resized to fill the picture slot. The slot clips its content to the rounded corners.
 *
 * <h2>Horizontal</h2>
 * Flowbite stacks a horizontal card below the {@code md} breakpoint and puts it in a row above it.
 * A desktop window is always "above md", so {@code FxCard} is always a row when {@code horizontal} is
 * true, and its default maximum width is {@link #HORIZONTAL_MAX_WIDTH} ({@code md:max-w-xl}).
 *
 * <h2>Link</h2>
 * A non-blank {@link #hrefProperty()} (Flowbite's {@code href}) makes the whole card a link: it gets
 * the hover color, a hand cursor, keyboard focus (Tab, then Enter or Space) and a focus ring. Clicking
 * it, or pressing Enter/Space, fires {@link #onActionProperty()}. FXKit has no access to
 * {@code HostServices}, so the card does not open the URL itself:
 *
 * <pre>{@code
 * card.setOnAction(e -> hostServices.showDocument(card.getHref()));
 * }</pre>
 *
 * Clicks that land on a button, text field or combo box inside the card go to that control and do not
 * activate the card. Use {@code href="#"} for a card that only needs the link behavior.
 *
 * <h2>className</h2>
 * Same grammar as {@code FxButton.className} ({@code ClassNameStyler}): Tailwind-like tokens such as
 * {@code "bg-yellow-100 border-yellow-300"}, resolved to inline style, so they override the card's own
 * colors.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxCard card = new FxCard(title, paragraph);
 * card.setHref("#");
 * card.setOnAction(e -> openArticle());
 * card.setMaxWidth(384);
 *
 * FxCard withImage = new FxCard(title, paragraph);
 * withImage.setImgSrc("/images/blog/image-1.jpg");
 * withImage.setImgAlt("A meaningful description");
 *
 * FxCard row = new FxCard(title, paragraph);
 * row.setHorizontal(true);
 * row.setImgSrc("/images/blog/image-4.jpg");
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxCard href="#" maxWidth="384" imgSrc="/images/blog/image-1.jpg" imgAlt="...">
 *     <Label text="Noteworthy technology acquisitions 2021" styleClass="text-2xl, font-bold, text-body"/>
 * </FxCard>
 * }</pre>
 */
@DefaultProperty("content")
public class FxCard extends Region {

    public static final String STYLE_CLASS = "fxk-card";

    /** Flowbite's {@code md:max-w-xl}: the widest a horizontal card gets unless you set a maximum. */
    public static final double HORIZONTAL_MAX_WIDTH = 576;

    private static final String BODY_STYLE_CLASS = "fxk-card-body";
    private static final String HORIZONTAL_STYLE_CLASS = "fxk-card-horizontal";
    private static final String HREF_STYLE_CLASS = "fxk-card-href";

    private final VBox body = new VBox();

    /** The picture slot; {@code null} while the card has no picture. */
    private CardImage frame;

    /** The {@link ImageView} built from {@code imgSrc}, if that is what the slot shows. */
    private ImageView builtView;

    private final StringProperty href = new SimpleStringProperty(this, "href");

    private final BooleanProperty horizontal = new SimpleBooleanProperty(this, "horizontal", false);

    private final StringProperty imgSrc = new SimpleStringProperty(this, "imgSrc");

    private final StringProperty imgAlt = new SimpleStringProperty(this, "imgAlt");

    private final ObjectProperty<Function<FxCard, Node>> renderImage = new SimpleObjectProperty<>(this,
            "renderImage");

    private final StringProperty className = new SimpleStringProperty(this, "className", "");

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>() {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }

        @Override
        public Object getBean() {
            return FxCard.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    public FxCard() {
        initialize();
    }

    /** @param children the card's content, in order */
    public FxCard(Node... children) {
        initialize();
        body.getChildren().addAll(children);
    }

    private void initialize() {
        getStyleClass().add(STYLE_CLASS);
        body.getStyleClass().add(BODY_STYLE_CLASS);
        getChildren().add(body);

        BooleanStyleClassSync.sync(this, HORIZONTAL_STYLE_CLASS, horizontal);

        className.addListener((observable, oldValue, newValue) -> ClassNameStyler.apply(this, newValue));
        href.addListener((observable, oldValue, newValue) -> applyHref());
        imgSrc.addListener((observable, oldValue, newValue) -> refreshImage());
        renderImage.addListener((observable, oldValue, newValue) -> refreshImage());
        imgAlt.addListener((observable, oldValue, newValue) -> {
            if (builtView != null) {
                builtView.setAccessibleText(newValue);
            }
        });
        horizontal.addListener((observable, oldValue, newValue) -> {
            if (getRenderImage() != null) {
                refreshImage(); // the renderer may choose a different node for the other layout
            } else if (frame != null) {
                frame.setHorizontal(newValue);
            }
            requestLayout();
        });

        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
        addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
    }

    // ---- content ---------------------------------------------------------------------------

    /**
     * @return the card's content (Flowbite's {@code children}); the list is live: add, remove or reorder
     *         nodes in it
     */
    public ObservableList<Node> getContent() {
        return body.getChildren();
    }

    // ---- picture ---------------------------------------------------------------------------

    private void refreshImage() {
        Node node = createImageNode();
        if (node == null) {
            removeFrame();
            return;
        }
        if (frame == null) {
            frame = new CardImage();
            getChildren().add(0, frame);
        }
        frame.setHorizontal(isHorizontal());
        frame.setContent(node);
        requestLayout();
    }

    /** @return the node for the picture slot, or {@code null} if the card has no picture */
    private Node createImageNode() {
        builtView = null;

        Function<FxCard, Node> renderer = getRenderImage();
        if (renderer != null) {
            return renderer.apply(this); // renderImage wins over imgSrc, as in Flowbite
        }

        Image image = ImageSources.load(getImgSrc(), ImageSources.CARD_IMAGE_MAX_WIDTH,
                ImageSources.CARD_IMAGE_MAX_HEIGHT);
        if (image == null || image.isError()) {
            return null;
        }
        ImageView view = new ImageView(image);
        view.setSmooth(true);
        view.setAccessibleText(getImgAlt());
        builtView = view;

        // The load runs in the background, so a failure can arrive after the card is already showing
        // the (still empty) picture slot: take the slot away again.
        image.errorProperty().addListener((observable, wasError, isError) -> {
            if (isError && builtView == view) {
                removeFrame();
            }
        });
        return view;
    }

    private void removeFrame() {
        builtView = null;
        if (frame != null) {
            getChildren().remove(frame);
            frame = null;
            requestLayout();
        }
    }

    // ---- link ------------------------------------------------------------------------------

    private boolean isLink() {
        String value = getHref();
        return value != null && !value.isBlank();
    }

    private void applyHref() {
        boolean link = isLink();
        getStyleClass().remove(HREF_STYLE_CLASS);
        if (link) {
            getStyleClass().add(HREF_STYLE_CLASS);
        }
        setFocusTraversable(link);
        setAccessibleRole(link ? AccessibleRole.HYPERLINK : AccessibleRole.NODE);
    }

    /**
     * Activates the card as if it had been clicked: fires an {@link ActionEvent} to
     * {@link #onActionProperty()}. Does nothing unless the card has an {@code href} and is enabled.
     */
    public void fire() {
        if (isLink() && !isDisabled()) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    private void onMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress() && !fromInteractiveChild(event)) {
            fire();
        }
    }

    private void onKeyPressed(KeyEvent event) {
        boolean activate = event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE;
        if (activate && event.getTarget() == this && isLink()) {
            fire();
            event.consume();
        }
    }

    /** A click on a control inside the card belongs to that control, not to the card. */
    private boolean fromInteractiveChild(MouseEvent event) {
        Node node = event.getTarget() instanceof Node target ? target : null;
        while (node != null && node != this) {
            if (node instanceof ButtonBase || node instanceof TextInputControl || node instanceof ComboBoxBase<?>) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        if (action == AccessibleAction.FIRE) {
            fire();
        } else {
            super.executeAccessibleAction(action, parameters);
        }
    }

    // ---- layout ----------------------------------------------------------------------------
    // Vertical: picture on top, content below. Horizontal: picture column on the left, content to its
    // right. Written by hand (not BorderPane) because the picture's height comes from its aspect ratio
    // and, horizontally, from the content's height.

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets in = getInsets();
        double inner = height >= 0 ? height - in.getTop() - in.getBottom() : -1;
        double bodyWidth = body.prefWidth(inner);
        double frameWidth = frame == null ? 0 : frame.prefWidth(-1);
        double content = isHorizontal() ? frameWidth + bodyWidth : Math.max(frameWidth, bodyWidth);
        return in.getLeft() + content + in.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets in = getInsets();
        double inner = width >= 0 ? width - in.getLeft() - in.getRight() : -1;
        double content;
        if (isHorizontal()) {
            double frameWidth = frame == null ? 0 : CardImage.HORIZONTAL_WIDTH;
            double frameHeight = frame == null ? 0 : frame.prefHeight(frameWidth);
            double bodyHeight = body.prefHeight(inner >= 0 ? inner - frameWidth : -1);
            content = Math.max(frameHeight, bodyHeight);
        } else {
            double frameHeight = frame == null ? 0 : frame.prefHeight(inner);
            content = frameHeight + body.prefHeight(inner);
        }
        return in.getTop() + content + in.getBottom();
    }

    @Override
    protected double computeMinWidth(double height) {
        Insets in = getInsets();
        double bodyWidth = body.minWidth(-1);
        double frameWidth = frame == null ? 0 : frame.minWidth(-1);
        double content = isHorizontal() ? frameWidth + bodyWidth : Math.max(frameWidth, bodyWidth);
        return in.getLeft() + content + in.getRight();
    }

    /** Text cannot be squeezed: the card never gets shorter than its content needs at this width. */
    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    /** Flowbite's {@code md:max-w-xl} for a horizontal card; unbounded otherwise. */
    @Override
    protected double computeMaxWidth(double height) {
        return isHorizontal() ? HORIZONTAL_MAX_WIDTH : Double.MAX_VALUE;
    }

    @Override
    protected void layoutChildren() {
        double x = snappedLeftInset();
        double y = snappedTopInset();
        double w = getWidth() - x - snappedRightInset();
        double h = getHeight() - y - snappedBottomInset();

        if (isHorizontal()) {
            double frameWidth = frame == null ? 0 : Math.min(CardImage.HORIZONTAL_WIDTH, w);
            if (frame != null) {
                frame.resizeRelocate(x, y, frameWidth, h);
            }
            body.resizeRelocate(x + frameWidth, y, w - frameWidth, h);
        } else {
            double frameHeight = frame == null ? 0 : Math.min(frame.prefHeight(w), h);
            if (frame != null) {
                frame.resizeRelocate(x, y, w, frameHeight);
            }
            body.resizeRelocate(x, y + frameHeight, w, h - frameHeight);
        }
    }

    // ---- properties ------------------------------------------------------------------------

    public String getHref() {
        return href.get();
    }

    /**
     * Flowbite's {@code href}: a non-blank value makes the whole card a link (hover color, hand cursor,
     * keyboard focus, {@link #fire()}). The card does not open the URL itself; handle
     * {@link #onActionProperty()}.
     *
     * @param href the link target, or {@code null}/blank for a plain card
     */
    public void setHref(String href) {
        this.href.set(href);
    }

    public StringProperty hrefProperty() {
        return href;
    }

    public boolean isHorizontal() {
        return horizontal.get();
    }

    /**
     * Flowbite's {@code horizontal}: the picture becomes a column beside the content instead of a
     * banner above it.
     *
     * @param horizontal {@code true} for the row layout
     */
    public void setHorizontal(boolean horizontal) {
        this.horizontal.set(horizontal);
    }

    public BooleanProperty horizontalProperty() {
        return horizontal;
    }

    public String getImgSrc() {
        return imgSrc.get();
    }

    /**
     * Flowbite's {@code imgSrc}: an image URL or classpath resource, subject to {@code FxImagePolicy}.
     * {@code null} or blank removes the picture.
     *
     * @param imgSrc the picture's location
     */
    public void setImgSrc(String imgSrc) {
        this.imgSrc.set(imgSrc);
    }

    public StringProperty imgSrcProperty() {
        return imgSrc;
    }

    public String getImgAlt() {
        return imgAlt.get();
    }

    /**
     * Flowbite's {@code imgAlt}: the picture's accessible text. Leave it empty for a picture that is
     * purely decorative.
     *
     * @param imgAlt the alternative text
     */
    public void setImgAlt(String imgAlt) {
        this.imgAlt.set(imgAlt);
    }

    public StringProperty imgAltProperty() {
        return imgAlt;
    }

    public Function<FxCard, Node> getRenderImage() {
        return renderImage.get();
    }

    /**
     * Flowbite's {@code renderImage}: builds the picture node yourself. It receives the card, so it can
     * look at {@link #isHorizontal()}; returning {@code null} means no picture. While set, it replaces
     * {@link #imgSrcProperty()}.
     *
     * @param renderImage the factory, or {@code null} to go back to {@code imgSrc}
     */
    public void setRenderImage(Function<FxCard, Node> renderImage) {
        this.renderImage.set(renderImage);
    }

    public ObjectProperty<Function<FxCard, Node>> renderImageProperty() {
        return renderImage;
    }

    public EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /**
     * Called when the card is activated: a click, or Enter/Space while it has focus. Only a card with
     * an {@link #hrefProperty() href} can be activated.
     *
     * @param handler the handler, or {@code null}
     */
    public void setOnAction(EventHandler<ActionEvent> handler) {
        onAction.set(handler);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /** @return the current {@code className} string (never {@code null}; empty if unset) */
    public String getClassName() {
        return className.get();
    }

    /**
     * Arbitrary Tailwind-like utility tokens; see {@code FxButton}'s "className" section. Pass
     * {@code ""} or {@code null} to clear what this property applied before.
     *
     * @param className space-separated utility tokens, optionally pseudo-class-scoped
     */
    public void setClassName(String className) {
        this.className.set(className);
    }

    public StringProperty classNameProperty() {
        return className;
    }
}
