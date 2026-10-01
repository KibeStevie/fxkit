package dev.fxkit.core.components.footer;

import java.util.List;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Flowbite's {@code <FooterLinkGroup>}: a list of links.
 *
 * <ul>
 *   <li>default: a wrapping row, 24px between links ({@code me-4 ... md:mr-6}), 8px between wrapped
 *       lines;</li>
 *   <li>{@link #colProperty() col}: one link per line, 16px apart ({@code flex-col space-y-4}).</li>
 * </ul>
 *
 * <pre>{@code
 * new FxFooterLinkGroup(new FxFooterLink("About"), new FxFooterLink("Contact"));  // row
 * new FxFooterLinkGroup(true, new FxFooterLink("About"), new FxFooterLink("Contact")); // column
 * }</pre>
 *
 * <p>Style classes: {@code fxk-footer-link-group}, {@code fxk-footer-link-group-col}. Any node can be
 * a child; {@link FxFooterLink} is the usual one.
 */
public class FxFooterLinkGroup extends Pane {

    private static final double GAP_X = 24;   // md:mr-6
    private static final double GAP_Y = 8;    // between wrapped lines of a row
    private static final double COL_GAP = 16; // space-y-4

    private final BooleanProperty col = new SimpleBooleanProperty(this, "col", false);

    public FxFooterLinkGroup(Node... items) {
        this(false, items);
    }

    public FxFooterLinkGroup(boolean col, Node... items) {
        getStyleClass().add("fxk-footer-link-group");
        BooleanStyleClassSync.sync(this, "fxk-footer-link-group-col", this.col);
        this.col.addListener((o, a, b) -> requestLayout());
        setCol(col);
        getChildren().addAll(items);
    }

    /** Flowbite's {@code col} prop: stack the links vertically instead of in a wrapping row. */
    public final BooleanProperty colProperty() { return col; }
    public final boolean isCol() { return col.get(); }
    public final void setCol(boolean value) { col.set(value); }

    // ---- sizing and layout ------------------------------------------------------------------

    @Override
    public Orientation getContentBias() {
        return isCol() ? null : Orientation.HORIZONTAL;
    }

    @Override
    protected double computeMinWidth(double height) {
        double max = 0;
        for (Node kid : getManagedChildren()) {
            max = Math.max(max, kid.minWidth(-1));
        }
        return snappedLeftInset() + snapSizeX(max) + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        List<Node> kids = getManagedChildren();
        double value = 0;
        for (Node kid : kids) {
            value = isCol() ? Math.max(value, kid.prefWidth(-1)) : value + kid.prefWidth(-1);
        }
        if (!isCol() && kids.size() > 1) {
            value += GAP_X * (kids.size() - 1);
        }
        return snappedLeftInset() + snapSizeX(value) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        double available = width < 0 ? -1 : width - snappedLeftInset() - snappedRightInset();
        return snappedTopInset() + arrange(available, false) + snappedBottomInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        arrange(getWidth() - snappedLeftInset() - snappedRightInset(), true);
    }

    /**
     * Flows the children (wrapping row, or single column) and returns the content height. With
     * {@code place} set it also positions them. {@code available < 0} means "no width limit": a row
     * then stays on one line.
     */
    private double arrange(double available, boolean place) {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        double x = 0;
        double y = 0;
        double lineHeight = 0;
        double total = 0;
        boolean first = true;

        for (Node kid : getManagedChildren()) {
            double pw = snapSizeX(kid.prefWidth(-1));
            if (available >= 0) {
                pw = Math.min(pw, Math.max(0, available));
            }
            double ph = snapSizeY(kid.prefHeight(pw));

            if (isCol()) {
                if (!first) {
                    y += COL_GAP;
                }
                if (place) {
                    kid.resizeRelocate(left, top + y, pw, ph);
                }
                y += ph;
                total = y;
            } else {
                if (available >= 0 && x > 0 && x + pw > available) {
                    y += lineHeight + GAP_Y;
                    x = 0;
                    lineHeight = 0;
                }
                if (place) {
                    kid.resizeRelocate(left + x, top + y, pw, ph);
                }
                x += pw + GAP_X;
                lineHeight = Math.max(lineHeight, ph);
                total = y + lineHeight;
            }
            first = false;
        }
        return total;
    }
}
