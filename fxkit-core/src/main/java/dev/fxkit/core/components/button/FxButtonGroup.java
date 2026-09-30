package dev.fxkit.core.components.button;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * A row of {@link FxButton}s fused into one segmented control, modelled on Flowbite React's
 * {@code <ButtonGroup>} (theme base: {@code inline-flex rounded-md shadow-sm}).
 *
 * <p>{@code FxButtonGroup} is a plain {@link HBox}: add buttons with {@code getChildren()} or the
 * varargs constructor. The group only decides <em>how the buttons join up</em>; each button keeps
 * its own text, icon, size and handlers.
 *
 * <h2>Joining</h2>
 * Every child gets the style classes {@code fxk-btn-group-item} plus exactly one of
 * {@code fxk-btn-group-start}, {@code -middle}, {@code -end} or {@code -only}, recomputed whenever
 * the children change (JavaFX CSS has no {@code :first-child}/{@code :last-child}). {@code components.css}
 * uses them to square off the inner corners and to drop the inner borders so neighbours share one
 * divider line - Flowbite's {@code positionInGroup}.
 *
 * <h2>Group-level outline and color</h2>
 * Flowbite passes {@code outline} and {@code color} down to each {@code Button}. The same is done
 * here, with one twist: in FXKit, "outline" is a {@link FxButton.Variant}, and {@code Color} only
 * has an effect under {@link FxButton.Variant#DEFAULT} or {@link FxButton.Variant#OUTLINE}. So while
 * the group manages its buttons:
 * <ul>
 *   <li>{@link #outlineProperty()} {@code true} sets each {@code FxButton}'s variant to
 *       {@code OUTLINE};</li>
 *   <li>{@link #colorProperty()} non-null sets each {@code FxButton}'s color, and its variant to
 *       {@code DEFAULT} unless the group is outlined;</li>
 *   <li>with {@code outline == false} and {@code color == null} the group manages nothing, and a
 *       button's own variant and color are left exactly as you set them.</li>
 * </ul>
 * The first time the group overrides a button it remembers that button's own variant and color, and
 * restores them when the group stops managing it (property reset, or the button is removed). Children
 * that are not {@code FxButton}s still get the joining classes but are never restyled.
 *
 * <h2>Pill</h2>
 * {@link #pillProperty()} rounds the <em>outer</em> ends of the group fully. A button's own
 * {@code pill} flag is ignored inside a group.
 *
 * <h2>Shadow</h2>
 * Solid groups get {@code -fxk-shadow-sm} (Flowbite's {@code shadow-sm}). Outline groups do not: in
 * JavaFX an effect shadows the visible glyphs of a transparent node, which would put a faint shadow
 * under the text and borders.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * // Default: alternative-colored buttons
 * FxButtonGroup group = new FxButtonGroup(
 *         new FxButton("Profile"), new FxButton("Settings"), new FxButton("Messages"));
 * group.setColor(FxButton.Color.ALTERNATIVE);
 *
 * // Outline
 * FxButtonGroup outlined = new FxButtonGroup(a, b, c);
 * outlined.setOutline(true);
 *
 * // Color options
 * FxButtonGroup cyan = new FxButtonGroup(a, b, c);
 * cyan.setColor(FxButton.Color.CYAN);
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxButtonGroup color="CYAN" pill="true">
 *     <FxButton text="Profile"/>
 *     <FxButton text="Settings"/>
 * </FxButtonGroup>
 * }</pre>
 */
public class FxButtonGroup extends HBox {

    public static final String STYLE_CLASS = "fxk-btn-group";

    private static final String ITEM_STYLE_CLASS = "fxk-btn-group-item";
    private static final String START_STYLE_CLASS = "fxk-btn-group-start";
    private static final String MIDDLE_STYLE_CLASS = "fxk-btn-group-middle";
    private static final String END_STYLE_CLASS = "fxk-btn-group-end";
    private static final String ONLY_STYLE_CLASS = "fxk-btn-group-only";
    private static final String PILL_STYLE_CLASS = "fxk-btn-group-pill";
    private static final String OUTLINE_STYLE_CLASS = "fxk-btn-group-outline";

    private static final List<String> ITEM_CLASSES = List.of(
            ITEM_STYLE_CLASS, START_STYLE_CLASS, MIDDLE_STYLE_CLASS, END_STYLE_CLASS, ONLY_STYLE_CLASS);

    /** A button's own look, saved before the group first overrides it. */
    private record Original(FxButton.Variant variant, FxButton.Color color) { }

    private final BooleanProperty outline = new SimpleBooleanProperty(this, "outline", false);
    private final BooleanProperty pill = new SimpleBooleanProperty(this, "pill", false);
    private final ObjectProperty<FxButton.Color> color = new SimpleObjectProperty<>(this, "color");

    /** Children the group has styled, so removed ones can be released. Identity-based. */
    private final Set<Node> tracked = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<FxButton, Original> originals = new IdentityHashMap<>();

    public FxButtonGroup() {
        this(new Node[0]);
    }

    public FxButtonGroup(Node... items) {
        getStyleClass().add(STYLE_CLASS);
        setMaxWidth(USE_PREF_SIZE); // inline-flex: hug the buttons instead of stretching in a VBox

        BooleanStyleClassSync.sync(this, PILL_STYLE_CLASS, pill);
        BooleanStyleClassSync.sync(this, OUTLINE_STYLE_CLASS, outline);
        outline.addListener((observable, oldValue, newValue) -> applyManagedLook());
        color.addListener((observable, oldValue, newValue) -> applyManagedLook());
        getChildren().addListener((ListChangeListener<Node>) change -> refresh());

        getChildren().addAll(items);
    }

    /** Re-tags every child with its position and releases children that were removed. */
    private void refresh() {
        List<Node> items = getChildren();

        tracked.removeIf(node -> {
            if (items.contains(node)) {
                return false;
            }
            release(node);
            return true;
        });

        int last = items.size() - 1;
        for (int index = 0; index <= last; index++) {
            Node node = items.get(index);
            tracked.add(node);
            applyPosition(node, index, last);
            if (node instanceof FxButton button) {
                applyManagedLook(button);
            }
        }
    }

    private static void applyPosition(Node node, int index, int last) {
        String position;
        if (last == 0) {
            position = ONLY_STYLE_CLASS;
        } else if (index == 0) {
            position = START_STYLE_CLASS;
        } else if (index == last) {
            position = END_STYLE_CLASS;
        } else {
            position = MIDDLE_STYLE_CLASS;
        }
        node.getStyleClass().removeAll(ITEM_CLASSES);
        node.getStyleClass().add(ITEM_STYLE_CLASS);
        node.getStyleClass().add(position);
    }

    private void applyManagedLook() {
        for (Node node : getChildren()) {
            if (node instanceof FxButton button) {
                applyManagedLook(button);
            }
        }
    }

    private void applyManagedLook(FxButton button) {
        FxButton.Color groupColor = color.get();
        if (!outline.get() && groupColor == null) {
            restore(button);
            return;
        }
        Original original = originals.computeIfAbsent(
                button, b -> new Original(b.getVariant(), b.getColor()));
        button.setVariant(outline.get() ? FxButton.Variant.OUTLINE : FxButton.Variant.DEFAULT);
        button.setColor(groupColor != null ? groupColor : original.color());
    }

    private void restore(FxButton button) {
        Original original = originals.remove(button);
        if (original != null) {
            button.setVariant(original.variant());
            button.setColor(original.color());
        }
    }

    /** Called for a child that just left the group: drop its group classes and look. */
    private void release(Node node) {
        node.getStyleClass().removeAll(ITEM_CLASSES);
        if (node instanceof FxButton button) {
            restore(button);
        }
    }

    /**
     * @return whether the group renders its buttons as {@link FxButton.Variant#OUTLINE}
     */
    public boolean isOutline() { return outline.get(); }

    /**
     * Makes every {@code FxButton} in the group an outline button (Flowbite's
     * {@code <ButtonGroup outline>}). Set {@link #setColor(FxButton.Color)} as well to pick the color.
     *
     * @param outline {@code true} for outline buttons; {@code false} restores the buttons' own variants
     */
    public void setOutline(boolean outline) { this.outline.set(outline); }
    public BooleanProperty outlineProperty() { return outline; }

    /**
     * @return whether the outer ends of the group are fully rounded
     */
    public boolean isPill() { return pill.get(); }

    /**
     * Fully rounds the outer ends of the group (Tailwind's {@code rounded-full}).
     *
     * @param pill {@code true} for fully rounded outer corners
     */
    public void setPill(boolean pill) { this.pill.set(pill); }
    public BooleanProperty pillProperty() { return pill; }

    /**
     * @return the color applied to every button, or {@code null} if the buttons keep their own
     */
    public FxButton.Color getColor() { return color.get(); }

    /**
     * Applies one named color to every {@code FxButton} in the group: solid under the default
     * look, border + text when {@link #isOutline()}. Pass {@code null} to let each button keep its
     * own color.
     *
     * @param color the named color, or {@code null}
     */
    public void setColor(FxButton.Color color) { this.color.set(color); }
    public ObjectProperty<FxButton.Color> colorProperty() { return color; }
}
