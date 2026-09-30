package dev.fxkit.core.components.dropdown;

import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A non-interactive header at the top of an {@link FxDropdown} (Flowbite's {@code <DropdownHeader>}),
 * for example a user's name and e-mail address.
 *
 * <p>Like Flowbite's header, it draws its own divider underneath, so you do not add an
 * {@link FxDropdownDivider} after it. It can hold any nodes; if they include a text input, set
 * {@link FxDropdown#enableTypeAheadProperty() enableTypeAhead} to {@code false} on the dropdown so
 * keystrokes are not treated as item searches.
 *
 * <pre>{@code
 * // two-line header, like Flowbite's Bonnie Green example
 * new FxDropdownHeader("Bonnie Green", "bonnie@flowbite.com");
 *
 * // or with your own content
 * new FxDropdownHeader(new Label("Signed in as"), myCustomNode);
 * }</pre>
 */
@DefaultProperty("content")
public class FxDropdownHeader extends VBox {

    private final VBox content = new VBox();

    public FxDropdownHeader() {
        getStyleClass().add("fxk-dropdown-header");
        content.getStyleClass().add("fxk-dropdown-header-content");
        getChildren().addAll(content, new FxDropdownDivider());
    }

    /**
     * @param nodes the nodes to show in the header, top to bottom
     */
    public FxDropdownHeader(Node... nodes) {
        this();
        content.getChildren().addAll(nodes);
    }

    /**
     * Two-line header: a regular line and a medium-weight line below it (Flowbite's name + e-mail).
     *
     * @param primary   first line, for example a name
     * @param secondary second line, for example an e-mail address
     */
    public FxDropdownHeader(String primary, String secondary) {
        this();
        Label first = new Label(primary);
        first.getStyleClass().add("fxk-dropdown-header-title");
        Label second = new Label(secondary);
        second.getStyleClass().add("fxk-dropdown-header-subtitle");
        content.getChildren().addAll(first, second);
    }

    /**
     * @return the live list of nodes shown in the header (the divider below is not part of it)
     */
    public final ObservableList<Node> getContent() {
        return content.getChildren();
    }
}
