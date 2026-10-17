package dev.fxkit.core.components.form;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

/**
 * Flowbite React's dropzone {@code <FileInput>}: a large dashed area with an upload icon, a
 * "Click to upload or drag and drop" prompt and an optional hint line. Clicking anywhere on it (or
 * pressing Enter / Space while focused) opens a {@link FileChooser}; files can be dropped onto it.
 *
 * <p>Like Flowbite's version, which hides a real {@code <FileInput>} inside a {@code <Label>}, this
 * wraps a hidden {@link FxFileInput} and delegates the value API to it: {@link #fileProperty()},
 * {@link #getFiles()}, {@link #multipleProperty()}, {@link #getExtensionFilters()},
 * {@link #setOnRejected(Consumer)}, {@link #clear()}, {@link #pick(List)} and {@link #choose()} behave
 * exactly as on {@code FxFileInput}. Flowbite's dropzone shows nothing after a pick; as a convenience a
 * small line with the file name (or the file count) appears under the hint once there is a selection.
 *
 * <h2>Look</h2>
 * Height 256px ({@code h-64}), dashed 2px border, {@code rounded-lg}. The fill, border and their hover
 * colors are looked-up colors ({@code -fxk-dz-bg}, {@code -fxk-dz-bg-hover}, {@code -fxk-dz-border},
 * {@code -fxk-dz-border-hover}) defined per theme in {@code colors.css}. While files are dragged over
 * it, the {@code :drag-over} pseudo-class is set and the border takes the primary color.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxFileDropzone zone = new FxFileDropzone();
 * zone.setHint("SVG, PNG, JPG or GIF (MAX. 800x400px)");
 * zone.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif", "*.svg"));
 * zone.setOnRejected(rejected -> System.out.println("Not an image: " + rejected));
 * zone.fileProperty().addListener((obs, was, now) -> preview(now));
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxFileDropzone hint="SVG, PNG, JPG or GIF" multiple="true"/>
 * }</pre>
 */
public class FxFileDropzone extends VBox {

    public static final String STYLE_CLASS = "fxk-dropzone";

    private static final PseudoClass DRAG_OVER = PseudoClass.getPseudoClass("drag-over");

    /** Flowbite's cloud-upload icon (viewBox 0 0 20 16), with arcs and relative commands spelled out. */
    private static final String CLOUD_UPLOAD_PATH = "M13 13h3a3 3 0 0 0 0-6h-0.025A5.56 5.56 0 0 0 16 6.5"
            + "A5.5 5.5 0 0 0 5.207 5.021C5.137 5.017 5.071 5 5 5a4 4 0 0 0 0 8h2.167"
            + "M10 15V6M10 6L8 8M10 6L12 8";
    private static final double ICON_BOX = 32; // h-8 w-8
    private static final double ICON_SCALE = ICON_BOX / 20; // viewBox is 20 wide

    private final FxFileInput input = new FxFileInput();

    private final StringProperty actionText = new SimpleStringProperty(this, "actionText", "Click to upload");
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "or drag and drop");
    private final StringProperty hint = new SimpleStringProperty(this, "hint", "");

    private final Text action = new Text();
    private final Text prompt = new Text();
    private final Label hintLabel = new Label();
    private final Label selection = new Label();

    public FxFileDropzone() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER);
        setFocusTraversable(true);
        setMaxWidth(Double.MAX_VALUE);

        // The real input stays in the tree (so it has a scene and inherits :disabled) but is never shown.
        input.setVisible(false);
        input.setManaged(false);
        input.setFocusTraversable(false);

        SVGPath cloud = new SVGPath();
        cloud.setContent(CLOUD_UPLOAD_PATH);
        cloud.getStyleClass().add("fxk-dropzone-icon");
        cloud.setScaleX(ICON_SCALE);
        cloud.setScaleY(ICON_SCALE);
        StackPane iconBox = new StackPane(cloud);
        iconBox.setMinSize(ICON_BOX, ICON_BOX);
        iconBox.setPrefSize(ICON_BOX, ICON_BOX);
        iconBox.setMaxSize(ICON_BOX, ICON_BOX);
        VBox.setMargin(iconBox, new Insets(0, 0, 16, 0)); // mb-4

        action.getStyleClass().add("fxk-dropzone-action");
        TextFlow promptFlow = new TextFlow(action, prompt);
        promptFlow.getStyleClass().add("fxk-dropzone-prompt");
        promptFlow.setTextAlignment(TextAlignment.CENTER);
        promptFlow.setMaxWidth(Region.USE_PREF_SIZE);
        VBox.setMargin(promptFlow, new Insets(0, 0, 8, 0)); // mb-2

        hintLabel.getStyleClass().add("fxk-dropzone-hint");
        hintLabel.setTextAlignment(TextAlignment.CENTER);
        hintLabel.setWrapText(true);

        selection.getStyleClass().add("fxk-dropzone-selection");
        selection.setTextAlignment(TextAlignment.CENTER);

        getChildren().addAll(input, iconBox, promptFlow, hintLabel, selection);

        action.textProperty().bind(actionText);
        prompt.textProperty().bind(Bindings.createStringBinding(() -> {
            String text = getPromptText();
            return text == null || text.isEmpty() ? "" : " " + text;
        }, promptText));
        hintLabel.textProperty().bind(hint);
        hintLabel.visibleProperty().bind(hint.isNotEmpty());
        hintLabel.managedProperty().bind(hintLabel.visibleProperty());
        selection.textProperty().bind(Bindings.createStringBinding(this::describe, input.getFiles()));
        selection.visibleProperty().bind(Bindings.isNotEmpty(input.getFiles()));
        selection.managedProperty().bind(selection.visibleProperty());

        addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                requestFocus();
                choose();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                choose();
                event.consume();
            }
        });
        setOnDragEntered(event -> {
            if (!isDisabled() && event.getDragboard().hasFiles()) {
                pseudoClassStateChanged(DRAG_OVER, true);
            }
        });
        setOnDragExited(event -> pseudoClassStateChanged(DRAG_OVER, false));
        setOnDragOver(event -> {
            if (!isDisabled() && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        setOnDragDropped(event -> {
            boolean accepted = !isDisabled() && event.getDragboard().hasFiles() && input.pick(event.getDragboard().getFiles());
            event.setDropCompleted(accepted);
            event.consume();
        });
    }

    private String describe() {
        List<File> files = input.getFiles();
        if (files.isEmpty()) {
            return "";
        }
        return files.size() == 1 ? files.get(0).getName() : files.size() + " files";
    }

    /** Opens the file dialog, exactly as clicking the zone does. Does nothing while disabled. */
    public void choose() {
        input.choose();
    }

    /** Forgets the picked file(s). */
    public void clear() {
        input.clear();
    }

    /** See {@link FxFileInput#pick(List)}. */
    public boolean pick(List<File> candidates) {
        return input.pick(candidates);
    }

    // ---- value (delegated to the hidden FxFileInput)
    // ---------------------------------------------------------

    /** @return the first picked file, or {@code null} */
    public File getFile() {
        return input.getFile();
    }

    public ReadOnlyObjectProperty<File> fileProperty() {
        return input.fileProperty();
    }

    /** @return every picked file (one at most unless {@link #isMultiple()}); never modifiable */
    public ObservableList<File> getFiles() {
        return input.getFiles();
    }

    /** The filters offered by the dialog and enforced on dropped files. */
    public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
        return input.getExtensionFilters();
    }

    public Consumer<List<File>> getOnRejected() {
        return input.getOnRejected();
    }

    /** Called with the dropped files that were refused (wrong type, directories). */
    public void setOnRejected(Consumer<List<File>> onRejected) {
        input.setOnRejected(onRejected);
    }

    public ObjectProperty<Consumer<List<File>>> onRejectedProperty() {
        return input.onRejectedProperty();
    }

    public boolean isMultiple() {
        return input.isMultiple();
    }

    public void setMultiple(boolean multiple) {
        input.setMultiple(multiple);
    }

    public BooleanProperty multipleProperty() {
        return input.multipleProperty();
    }

    // ---- texts
    // ----------------------------------------------------------------------------------------

    public String getActionText() {
        return actionText.get();
    }

    /** The bold lead of the prompt; "Click to upload" by default. */
    public void setActionText(String actionText) {
        this.actionText.set(actionText);
    }

    public StringProperty actionTextProperty() {
        return actionText;
    }

    public String getPromptText() {
        return promptText.get();
    }

    /** The regular part after the action; "or drag and drop" by default. Empty hides it. */
    public void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    public StringProperty promptTextProperty() {
        return promptText;
    }

    public String getHint() {
        return hint.get();
    }

    /** The small line under the prompt, e.g. the allowed types and sizes; empty (the default) hides it. */
    public void setHint(String hint) {
        this.hint.set(hint);
    }

    public StringProperty hintProperty() {
        return hint;
    }
}
