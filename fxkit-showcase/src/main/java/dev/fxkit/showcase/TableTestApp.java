package dev.fxkit.showcase;

import java.util.ArrayList;
import java.util.List;

import dev.fxkit.core.components.table.FxTable;
import dev.fxkit.core.components.table.FxTableCell;
import dev.fxkit.core.components.table.FxTableHeadCell;
import dev.fxkit.core.components.table.FxTableRow;
import dev.fxkit.core.theme.Theme;
import dev.fxkit.core.theme.ThemeManager;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Manual test harness for {@link FxTable}: the four Flowbite examples (default,
 * striped rows, hover
 * state, checkboxes), then a table with column dividers, filters, draggable
 * columns and a shadow, and
 * a table whose colors and shadow you pick yourself. The button flips light /
 * dark.
 */
public class TableTestApp extends Application {

    private record Product(String name, String color, String category, String price) {
    }

    private static final List<Product> THREE = List.of(
            new Product("Apple MacBook Pro 17\"", "Sliver", "Laptop", "$2999"),
            new Product("Microsoft Surface Pro", "White", "Laptop PC", "$1999"),
            new Product("Magic Mouse 2", "Black", "Accessories", "$99"));

    private static final List<Product> FIVE = List.of(
            THREE.get(0), THREE.get(1), THREE.get(2),
            new Product("Google Pixel Phone", "Gray", "Phone", "$799"),
            new Product("Apple Watch 5", "Red", "Wearables", "$999"));

    @Override
    public void start(Stage stage) {
        Button themeButton = new Button("Toggle theme");

        VBox page = new VBox(32,
                themeButton,
                section("Default table", productTable(THREE, false, false)),
                section("Striped rows", productTable(FIVE, true, false)),
                section("Table hover state", productTable(THREE, false, true)),
                section("Table with checkboxes", checkboxTable(THREE, false)),
                section("Dividers, filters and draggable columns (funnel button; drag a header)",
                        checkboxTable(FIVE, true)),
                section("Your own colors and shadow", colorsDemo()));
        page.setStyle("-fx-padding: 32px; -fx-background-color: -fxk-background;");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: -fxk-background; -fx-background-color: -fxk-background;");

        Scene scene = new Scene(scroll, 900, 900);
        ThemeManager.apply(scene, Theme.LIGHT);
        themeButton.setOnAction(event -> ThemeManager.toggle(scene));

        stage.setTitle("FxTable");
        stage.setScene(scene);
        stage.show();
    }

    private static Node section(String title, Node content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("text-lg");
        heading.setStyle("-fx-text-fill: -fxk-text; -fx-font-weight: bold;");
        return new VBox(12, heading, content);
    }

    /**
     * Default / striped / hoverable: Flowbite's first three examples differ only in
     * the two flags.
     */
    private static FxTable productTable(List<Product> products, boolean striped, boolean hoverable) {
        FxTable table = new FxTable();
        table.setStriped(striped);
        table.setHoverable(hoverable);
        table.setHeaders("Product name", "Color", "Category", "Price", ""); // last: sr-only "Edit"
        for (Product product : products) {
            FxTableRow row = table.addRow(product.name(), product.color(), product.category(),
                    product.price(), FxTable.link("Edit", () -> System.out.println("Edit " + product.name())));
            row.getCells().get(0).setStrong(true);
        }
        return table;
    }

    /**
     * A select-all checkbox in the head and one checkbox per row, each in a compact
     * cell.
     * With {@code advanced}: column dividers, filters on the text columns,
     * draggable columns (the
     * checkbox and Edit columns are pinned) and a medium shadow.
     */
    private static FxTable checkboxTable(List<Product> products, boolean advanced) {
        FxTable table = new FxTable();
        table.setHoverable(true);

        CheckBox selectAll = FxTable.checkbox();
        FxTableRow head = new FxTableRow();
        FxTableHeadCell checkHead = new FxTableHeadCell(selectAll);
        checkHead.setCompact(true);
        head.getCells().add(checkHead);
        for (String title : new String[] { "Product name", "Color", "Category", "Price", "" }) {
            head.getCells().add(new FxTableHeadCell(title));
        }
        table.getHead().getRows().setAll(head);

        List<CheckBox> boxes = new ArrayList<>();
        for (Product product : products) {
            CheckBox box = FxTable.checkbox();
            boxes.add(box);
            FxTableCell checkCell = new FxTableCell(box);
            checkCell.setCompact(true);
            FxTableCell name = new FxTableCell(product.name());
            name.setStrong(true);
            table.getBody().getRows().add(new FxTableRow(checkCell, name,
                    new FxTableCell(product.color()), new FxTableCell(product.category()),
                    new FxTableCell(product.price()),
                    new FxTableCell(FxTable.link("Edit", () -> System.out.println("Edit " + product.name())))));
        }

        if (advanced) {
            table.setColumnDividers(true);
            table.setReorderable(true);
            table.setStriped(true);
            table.setShadow(FxTable.Shadow.MD);
            List<FxTableHeadCell> headCells = table.getHeadCells();
            headCells.get(0).setMovable(false); // checkbox column
            headCells.get(headCells.size() - 1).setMovable(false); // Edit column
            for (int i = 1; i < headCells.size() - 1; i++) {
                headCells.get(i).setFilterable(true);
            }
            table.setOnColumnMoved((from, to) -> System.out.println("column " + from + " -> " + to));
        }

        // select-all <-> rows, guarded so the two listeners do not feed each other
        boolean[] syncing = { false };
        selectAll.selectedProperty().addListener((obs, was, selected) -> {
            if (!syncing[0]) {
                syncing[0] = true;
                boxes.forEach(box -> box.setSelected(selected));
                syncing[0] = false;
            }
        });
        boxes.forEach(box -> box.selectedProperty().addListener((obs, was, selected) -> {
            if (!syncing[0]) {
                syncing[0] = true;
                selectAll.setSelected(boxes.stream().allMatch(CheckBox::isSelected));
                syncing[0] = false;
            }
        }));
        return table;
    }

    /**
     * Colors, shadow and dividers chosen live with pickers: what a user of the
     * library would set.
     */
    private static Node colorsDemo() {
        FxTable table = productTable(FIVE, true, true);
        table.setShadow(FxTable.Shadow.LG);

        ColorPicker head = picker(Color.web("#e0e7ff"), table::setHeadBackground);
        ColorPicker stripe = picker(Color.web("#f5f3ff"), table::setStripedBackground);
        ColorPicker hover = picker(Color.web("#c7d2fe"), table::setHoverBackground);
        ColorPicker divider = picker(Color.web("#a5b4fc"), table::setDividerColor);
        table.setHeadBackground(head.getValue());
        table.setStripedBackground(stripe.getValue());
        table.setHoverBackground(hover.getValue());
        table.setDividerColor(divider.getValue());
        table.setColumnDividers(true);

        ComboBox<FxTable.Shadow> shadow = new ComboBox<>();
        shadow.getItems().addAll(FxTable.Shadow.values());
        shadow.setValue(table.getShadow());
        shadow.valueProperty().addListener((obs, was, is) -> table.setShadow(is));

        Button reset = new Button("Theme colors");
        reset.setOnAction(event -> {
            table.setHeadBackground(null);
            table.setStripedBackground(null);
            table.setHoverBackground(null);
            table.setDividerColor(null);
        });

        HBox controls = new HBox(12, new Label("Head"), head, new Label("Stripe"), stripe,
                new Label("Hover"), hover, new Label("Divider"), divider, new Label("Shadow"), shadow, reset);
        controls.setStyle("-fx-alignment: CENTER_LEFT;");
        return new VBox(16, controls, table);
    }

    private static ColorPicker picker(Color initial, java.util.function.Consumer<Color> apply) {
        ColorPicker picker = new ColorPicker(initial);
        picker.valueProperty().addListener((obs, was, is) -> apply.accept(is));
        return picker;
    }

    public static void main(String[] args) {
        launch(args);
    }
}