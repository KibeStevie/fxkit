package dev.fxkit.core.components.blockquote;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * The author line under a quote: an optional avatar, a name and a role, with a vertical divider
 * between name and role (Flowbite's {@code <figcaption>} with two {@code <cite>} elements, used by the
 * "User testimonial" and "User review" examples).
 *
 * <pre>
 * FxBlockquoteCaption (HBox, .fxk-blockquote-caption)
 * ├─ avatar (optional Node, usually an FxAvatar of size XS)
 * └─ cites (HBox, .fxk-blockquote-cites)
 *     ├─ name (Label, .fxk-blockquote-cite)
 *     ├─ divider (Region, .fxk-blockquote-cite-divider)   only when name AND role are shown
 *     └─ role (Label, .fxk-blockquote-cite-role)
 * </pre>
 *
 * <h2>Flowbite mapping</h2>
 * <pre>
 * figcaption   mt-6 flex items-center space-x-3            12px gap; the 24px top margin is layout
 *              [+ justify-center]                          centered = true
 * divide-x-2 divide-gray-300 / divide-gray-500             strongDivider = false / true
 *              (dark: divide-gray-700 for both)
 * cite name    pr-3 font-medium text-gray-900              .fxk-blockquote-cite
 * cite role    pl-3 text-sm text-gray-500                  .fxk-blockquote-cite-role
 * </pre>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * FxAvatar avatar = new FxAvatar();
 * avatar.setSize(FxAvatar.Size.XS);
 * avatar.setRounded(true);
 * avatar.setPlaceholderInitials("BG");
 *
 * FxBlockquoteCaption caption = new FxBlockquoteCaption(avatar, "Bonnie Green", "CTO at Flowbite");
 * VBox.setMargin(caption, new Insets(24, 0, 0, 0)); // mt-6
 * }</pre>
 *
 * <p>A blank name or role is simply left out (and the divider with it).
 */
public class FxBlockquoteCaption extends HBox {

    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final StringProperty role = new SimpleStringProperty(this, "role", "");
    private final ObjectProperty<Node> avatar = new SimpleObjectProperty<>(this, "avatar");
    private final BooleanProperty centered = new SimpleBooleanProperty(this, "centered", false);
    private final BooleanProperty strongDivider = new SimpleBooleanProperty(this, "strongDivider", false);

    private final HBox cites = new HBox();
    private final Label nameLabel = new Label();
    private final Label roleLabel = new Label();
    private final Region divider = new Region();

    /** Creates an empty caption; set {@link #setName(String) name}, {@link #setRole(String) role} and avatar. */
    public FxBlockquoteCaption() {
        getStyleClass().add("fxk-blockquote-caption");
        cites.getStyleClass().add("fxk-blockquote-cites");
        nameLabel.getStyleClass().add("fxk-blockquote-cite");
        roleLabel.getStyleClass().add("fxk-blockquote-cite-role");
        divider.getStyleClass().add("fxk-blockquote-cite-divider");

        nameLabel.textProperty().bind(name);
        roleLabel.textProperty().bind(role);
        show(nameLabel, Bindings.createBooleanBinding(() -> !isBlank(getName()), name));
        show(roleLabel, Bindings.createBooleanBinding(() -> !isBlank(getRole()), role));
        show(divider, Bindings.createBooleanBinding(() -> !isBlank(getName()) && !isBlank(getRole()), name, role));

        BooleanStyleClassSync.sync(this, "fxk-blockquote-caption-centered", centered);
        BooleanStyleClassSync.sync(this, "fxk-blockquote-caption-strong", strongDivider);

        cites.getChildren().addAll(nameLabel, divider, roleLabel);
        avatar.addListener(o -> refresh());
        refresh();
    }

    /**
     * @param name the author, for example {@code "Micheal Gough"}
     * @param role the author's role, for example {@code "CEO at Google"}; may be {@code null}
     */
    public FxBlockquoteCaption(String name, String role) {
        this();
        setName(name);
        setRole(role);
    }

    /**
     * @param avatar the node before the name, usually an {@code FxAvatar} of size {@code XS}
     * @param name   the author
     * @param role   the author's role; may be {@code null}
     */
    public FxBlockquoteCaption(Node avatar, String name, String role) {
        this(name, role);
        setAvatar(avatar);
    }

    private void refresh() {
        if (getAvatar() != null) {
            getChildren().setAll(getAvatar(), cites);
        } else {
            getChildren().setAll(cites);
        }
    }

    private static void show(Node node, ObservableValue<Boolean> visible) {
        node.visibleProperty().bind(visible);
        node.managedProperty().bind(node.visibleProperty());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ---- properties -------------------------------------------------------------------------

    /** The author's name (the bold cite). */
    public final StringProperty nameProperty() { return name; }
    public final String getName() { return name.get(); }
    public final void setName(String value) { name.set(value); }

    /** The author's role (the small muted cite after the divider). */
    public final StringProperty roleProperty() { return role; }
    public final String getRole() { return role.get(); }
    public final void setRole(String value) { role.set(value); }

    /** Node shown before the name, usually an {@code FxAvatar}; {@code null} for none. */
    public final ObjectProperty<Node> avatarProperty() { return avatar; }
    public final Node getAvatar() { return avatar.get(); }
    public final void setAvatar(Node value) { avatar.set(value); }

    /** Centers the caption in its row (Flowbite's {@code justify-center}). Default {@code false}. */
    public final BooleanProperty centeredProperty() { return centered; }
    public final boolean isCentered() { return centered.get(); }
    public final void setCentered(boolean value) { centered.set(value); }

    /**
     * Uses the darker divider of the testimonial example ({@code divide-gray-500}) instead of the
     * lighter one of the review example ({@code divide-gray-300}). Default {@code false}.
     */
    public final BooleanProperty strongDividerProperty() { return strongDivider; }
    public final boolean isStrongDivider() { return strongDivider.get(); }
    public final void setStrongDivider(boolean value) { strongDivider.set(value); }
}
