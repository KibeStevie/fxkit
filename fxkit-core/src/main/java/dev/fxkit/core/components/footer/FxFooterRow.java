package dev.fxkit.core.components.footer;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Flowbite's footer row: {@code flex items-center justify-between}.
 *
 * <p>JavaFX has no "space between" container, so this is one. It lays its children out like this:
 * <ul>
 *   <li><b>one child</b> - fills the row's whole width (Flowbite's {@code <div className="w-full">}
 *       wrapper inside {@code <Footer>});</li>
 *   <li><b>two or more</b> - each child at its preferred width, the first at the left edge, the last
 *       at the right edge, the free space split evenly between them. There is always at least
 *       {@value #MIN_GAP}px between neighbours; when the row gets narrower than that allows, the
 *       children are shrunk (down to their minimum width).</li>
 * </ul>
 * Children are centered vertically by default; see {@link #verticalAlignmentProperty()}.
 *
 * <p>{@link FxFooter} extends this class, and it can be used on its own for the rows <em>inside</em> a
 * footer (the brand + links row, the copyright + icons row).
 *
 * <p>Flowbite's {@code sm:flex} / {@code md:flex} breakpoints are not modelled: the row is always the
 * wide (desktop) layout.
 */
public class FxFooterRow extends Pane {

    /** Smallest gap kept between two neighbouring children, in px. */
    public static final double MIN_GAP = 16;

    private final ObjectProperty<VPos> verticalAlignment =
            new SimpleObjectProperty<>(this, "verticalAlignment", VPos.CENTER);

    public FxFooterRow() {
        getStyleClass().add("fxk-footer-row");
        verticalAlignment.addListener((o, a, b) -> requestLayout());
    }

    public FxFooterRow(Node... items) {
        this();
        getChildren().addAll(items);
    }

    /** Vertical placement of children that are shorter than the row; {@link VPos#CENTER} by default
     *  ({@code items-center}), {@link VPos#TOP} for Flowbite rows without {@code items-center}. */
    public final ObjectProperty<VPos> verticalAlignmentProperty() { return verticalAlignment; }
    public final VPos getVerticalAlignment() { return verticalAlignment.get(); }
    public final void setVerticalAlignment(VPos value) { verticalAlignment.set(value == null ? VPos.CENTER : value); }

    // ---- sizing ----------------------------------------------------------------------------

    @Override
    public Orientation getContentBias() {
        for (Node child : getManagedChildren()) {
            if (child.getContentBias() == Orientation.HORIZONTAL) {
                return Orientation.HORIZONTAL;
            }
        }
        return null;
    }

    @Override
    protected double computeMinWidth(double height) {
        List<Node> kids = getManagedChildren();
        double sum = 0;
        for (Node kid : kids) {
            sum += kid.minWidth(-1);
        }
        return snappedLeftInset() + snapSizeX(sum + gaps(kids.size())) + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        List<Node> kids = getManagedChildren();
        double sum = 0;
        for (Node kid : kids) {
            sum += kid.prefWidth(-1);
        }
        return snappedLeftInset() + snapSizeX(sum + gaps(kids.size())) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        List<Node> kids = getManagedChildren();
        double available = width < 0 ? -1 : width - snappedLeftInset() - snappedRightInset();
        double[] widths = childWidths(kids, available);
        double max = 0;
        for (int i = 0; i < kids.size(); i++) {
            max = Math.max(max, kids.get(i).prefHeight(widths[i]));
        }
        return snappedTopInset() + snapSizeY(max) + snappedBottomInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    // ---- layout ----------------------------------------------------------------------------

    @Override
    protected void layoutChildren() {
        List<Node> kids = getManagedChildren();
        int n = kids.size();
        if (n == 0) {
            return;
        }
        double left = snappedLeftInset();
        double top = snappedTopInset();
        double w = getWidth() - left - snappedRightInset();
        double h = getHeight() - top - snappedBottomInset();
        VPos vpos = getVerticalAlignment();

        double[] widths = childWidths(kids, w);
        if (n == 1) {
            layoutInArea(kids.get(0), left, top, widths[0], h, 0, Insets.EMPTY, true, true, HPos.LEFT, vpos);
            return;
        }

        double used = 0;
        for (double width : widths) {
            used += width;
        }
        double gap = Math.max(MIN_GAP, (w - used) / (n - 1));
        double x = left;
        for (int i = 0; i < n; i++) {
            layoutInArea(kids.get(i), x, top, widths[i], h, 0, Insets.EMPTY, true, false, HPos.LEFT, vpos);
            x += widths[i] + gap;
        }
    }

    private static double gaps(int count) {
        return count > 1 ? MIN_GAP * (count - 1) : 0;
    }

    /** Width for each child given the room available ({@code < 0}: unknown, widths are all -1). */
    private double[] childWidths(List<Node> kids, double available) {
        int n = kids.size();
        double[] widths = new double[n];
        if (available < 0) {
            java.util.Arrays.fill(widths, -1);
            return widths;
        }
        if (n == 1) {
            widths[0] = available;
            return widths;
        }
        double sum = 0;
        for (int i = 0; i < n; i++) {
            widths[i] = snapSizeX(kids.get(i).prefWidth(-1));
            sum += widths[i];
        }
        double gaps = gaps(n);
        if (sum + gaps > available) {
            double scale = sum > 0 ? Math.max(0, available - gaps) / sum : 1;
            for (int i = 0; i < n; i++) {
                double min = snapSizeX(kids.get(i).minWidth(-1));
                widths[i] = Math.max(min, Math.floor(widths[i] * scale));
            }
        }
        return widths;
    }
}
