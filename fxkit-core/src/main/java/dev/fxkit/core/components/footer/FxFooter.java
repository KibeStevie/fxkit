package dev.fxkit.core.components.footer;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;

/**
 * Flowbite React's {@code <Footer>}: a rounded, shadowed surface that holds footer content.
 *
 * <pre>{@code
 * FxFooter footer = new FxFooter(
 *         new FxFooterCopyright("Flowbite™", 2022),
 *         new FxFooterLinkGroup(new FxFooterLink("About"), new FxFooterLink("Contact")));
 * footer.setContainer(true);
 * }</pre>
 *
 * <p>Children are laid out by {@link FxFooterRow}: a single child fills the footer, several are spread
 * {@code justify-between}. Put the parts ({@link FxFooterCopyright}, {@link FxFooterLinkGroup},
 * {@link FxFooterBrand}, {@link FxFooterTitle}, {@link FxFooterDivider}, {@link FxFooterIcon}) inside a
 * footer: they take their colors from the footer and are unstyled elsewhere.
 *
 * <h2>Flowbite mapping (root theme)</h2>
 * <pre>
 *   root.base       w-full rounded-lg bg-white shadow dark:bg-gray-800  -> .fxk-footer
 *   root.container  w-full p-6                                          -> container  (24px padding)
 *   root.bgDark     bg-gray-800                                         -> bgDark     (colors.css)
 * </pre>
 *
 * <p>Style classes: {@code fxk-footer}, {@code fxk-footer-container}, {@code fxk-footer-bg-dark}.
 */
public class FxFooter extends FxFooterRow {

    private final BooleanProperty container = new SimpleBooleanProperty(this, "container", false);
    private final BooleanProperty bgDark = new SimpleBooleanProperty(this, "bgDark", false);

    public FxFooter() {
        getStyleClass().add("fxk-footer");
        BooleanStyleClassSync.sync(this, "fxk-footer-container", container);
        BooleanStyleClassSync.sync(this, "fxk-footer-bg-dark", bgDark);
    }

    public FxFooter(Node... items) {
        this();
        getChildren().addAll(items);
    }

    /** Flowbite's {@code container} prop: adds 24px of padding on every side ({@code p-6}). */
    public final BooleanProperty containerProperty() { return container; }
    public final boolean isContainer() { return container.get(); }
    public final void setContainer(boolean value) { container.set(value); }

    /**
     * Flowbite's {@code bgDark} prop: a dark ({@code gray-800}) footer in <em>both</em> themes, with
     * light text on it. Flowbite keeps the light-theme text colors here (gray-500 on gray-800, about
     * 3.3 : 1); FxFooter uses its dark-theme text colors instead so the footer stays readable.
     */
    public final BooleanProperty bgDarkProperty() { return bgDark; }
    public final boolean isBgDark() { return bgDark.get(); }
    public final void setBgDark(boolean value) { bgDark.set(value); }
}
