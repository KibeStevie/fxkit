package dev.fxkit.core.components;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A vertical stack of collapsible {@link FxAccordionPanel}s (Flowbite's {@code Accordion}).
 *
 * <pre>{@code
 * FxAccordion accordion = new FxAccordion(
 *         new FxAccordionPanel("What is FXKit?", "A JavaFX component library."),
 *         new FxAccordionPanel("Is there a dark theme?", "Yes: ThemeManager.apply(scene, Theme.DARK)."));
 *
 * accordion.setCollapseAll(true);   // start with every panel closed
 * accordion.setAlwaysOpen(true);    // opening a panel keeps the others open
 * accordion.setFlush(true);         // no side borders or outer rounding
 * }</pre>
 *
 * <h2>Behavior (matches Flowbite React)</h2>
 * <ul>
 *   <li><b>Default:</b> the first panel starts open (unless a panel was opened explicitly), and
 *       opening a panel closes the others. Clicking the open panel closes it, so all can be closed.</li>
 *   <li>{@link #collapseAllProperty() collapseAll}: closes every panel; set it before adding panels
 *       or after, either way all panels end up closed. Panels can be opened again afterwards.</li>
 *   <li>{@link #alwaysOpenProperty() alwaysOpen}: panels open independently. Turning it off again
 *       leaves only the first open panel open.</li>
 *   <li>{@link #flushProperty() flush}: removes the outer side borders and rounding (bottom border
 *       only).</li>
 * </ul>
 *
 * <p>Add and remove panels through {@link #getPanels()}, not {@code getChildren()}: the accordion
 * keeps its children in sync with that list and tags the first and last panel for styling.
 * Styling lives in {@code components.css} ({@code .fxk-accordion}).
 */
public class FxAccordion extends VBox {

    private static final String FIRST = "fxk-accordion-first";
    private static final String LAST = "fxk-accordion-last";

    private final ObservableList<FxAccordionPanel> panels = FXCollections.observableArrayList();
    private final BooleanProperty collapseAll = new SimpleBooleanProperty(this, "collapseAll", false);
    private final BooleanProperty alwaysOpen = new SimpleBooleanProperty(this, "alwaysOpen", false);
    private final BooleanProperty flush = new SimpleBooleanProperty(this, "flush", false);

    /** Whether the one-time initial state (first panel open, or all closed) has been applied. */
    private boolean initialStateApplied;

    /** Shared by every panel's {@code open} property; the property's bean is the panel itself. */
    private final ChangeListener<Boolean> panelOpenListener = (observable, wasOpen, isOpen) -> {
        if (Boolean.TRUE.equals(isOpen) && !isAlwaysOpen()) {
            closeOthers((FxAccordionPanel) ((ReadOnlyProperty<?>) observable).getBean());
        }
    };

    public FxAccordion(FxAccordionPanel... initialPanels) {
        getStyleClass().add("fxk-accordion");

        BooleanStyleClassSync.sync(this, "fxk-accordion-flush", flush);

        panels.addListener((ListChangeListener<FxAccordionPanel>) change -> {
            while (change.next()) {
                for (FxAccordionPanel removed : change.getRemoved()) {
                    removed.openProperty().removeListener(panelOpenListener);
                    removed.getStyleClass().removeAll(FIRST, LAST);
                }
                for (FxAccordionPanel added : change.getAddedSubList()) {
                    added.openProperty().addListener(panelOpenListener);
                }
            }
            getChildren().setAll(panels);
            updatePositionClasses();
            applyInitialState();
        });

        collapseAll.addListener((observable, was, is) -> {
            if (is) {
                panels.forEach(panel -> panel.setOpen(false));
            }
        });
        alwaysOpen.addListener((observable, was, is) -> {
            if (!is) {
                keepOnlyFirstOpen();
            }
        });

        panels.addAll(initialPanels);
    }

    /** The panels, in display order. Modify this list to add, remove or reorder panels. */
    public final ObservableList<FxAccordionPanel> getPanels() {
        return panels;
    }

    /** When {@code true}, every panel is closed (Flowbite's {@code collapseAll}). */
    public final BooleanProperty collapseAllProperty() {
        return collapseAll;
    }

    public final boolean isCollapseAll() {
        return collapseAll.get();
    }

    public final void setCollapseAll(boolean value) {
        collapseAll.set(value);
    }

    /** When {@code true}, opening a panel does not close the others (Flowbite's {@code alwaysOpen}). */
    public final BooleanProperty alwaysOpenProperty() {
        return alwaysOpen;
    }

    public final boolean isAlwaysOpen() {
        return alwaysOpen.get();
    }

    public final void setAlwaysOpen(boolean value) {
        alwaysOpen.set(value);
    }

    /** When {@code true}, drops the side borders and outer rounding (Flowbite's {@code flush}). */
    public final BooleanProperty flushProperty() {
        return flush;
    }

    public final boolean isFlush() {
        return flush.get();
    }

    public final void setFlush(boolean value) {
        flush.set(value);
    }

    // ---- internals ------------------------------------------------------------------------

    /** Runs once, when the first panels arrive: honor collapseAll, else open the first panel. */
    private void applyInitialState() {
        if (initialStateApplied || panels.isEmpty()) {
            return;
        }
        initialStateApplied = true;

        if (isCollapseAll()) {
            panels.forEach(panel -> panel.setOpen(false));
            return;
        }
        if (panels.stream().noneMatch(FxAccordionPanel::isOpen)) {
            panels.get(0).setOpen(true);
        }
        if (!isAlwaysOpen()) {
            keepOnlyFirstOpen();
        }
    }

    private void closeOthers(FxAccordionPanel opened) {
        for (FxAccordionPanel other : panels) {
            if (other != opened && other.isOpen()) {
                other.setOpen(false);
            }
        }
    }

    private void keepOnlyFirstOpen() {
        panels.stream()
                .filter(FxAccordionPanel::isOpen)
                .findFirst()
                .ifPresent(this::closeOthers);
    }

    private void updatePositionClasses() {
        int last = panels.size() - 1;
        for (int i = 0; i <= last; i++) {
            setStyleClass(panels.get(i), FIRST, i == 0);
            setStyleClass(panels.get(i), LAST, i == last);
        }
    }

    private static void setStyleClass(Node node, String styleClass, boolean present) {
        if (present) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        } else {
            node.getStyleClass().remove(styleClass);
        }
    }
}
