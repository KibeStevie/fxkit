package dev.fxkit.core.components.form;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import dev.fxkit.core.internal.BooleanStyleClassSync;
import dev.fxkit.core.internal.EnumStyleClassSync;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * A file picker field: Flowbite React's {@code <FileInput>}. A "Choose file" button, then the name of
 * the picked file ("No file chosen" until there is one), inside the same bordered box as
 * {@link FxTextInput}.
 *
 * <p>Clicking anywhere on the field, or pressing Enter / Space while it is focused, opens a
 * {@link FileChooser}; files can also be dropped onto it. Unlike the other form controls this is NOT
 * a subclass of a stock control, because JavaFX has no file field: it is an {@link HBox} with two
 * labels, so its value is read through {@link #getFile()} / {@link #getFiles()} instead.
 *
 * <h2>Value</h2>
 * {@link #fileProperty()} holds the first picked file (or {@code null}); {@link #getFiles()} is every
 * picked file when {@link #multipleProperty()} is on. Both are read-only: use {@link #clear()} to
 * reset. Cancelling the dialog keeps the previous selection. {@link #getExtensionFilters()} filters
 * both the dialog and dropped files.
 *
 * <h2>Dropzone</h2>
 * Flowbite's dropzone variant (a big dashed click-or-drop area) is {@link FxFileDropzone}; it wraps one
 * of these and exposes the same value API.
 *
 * <h2>Programmatic selection and rejection</h2>
 * {@link #pick(List)} sets the selection as if the files had been dropped (filters, single-file limit).
 * Dropped or picked files that fail the extension filters, or are directories, are reported to
 * {@link #setOnRejected(Consumer)}.
 *
 * <h2>Size and Color</h2>
 * {@link Size} and {@link Color} behave exactly as on {@link FxTextInput}.
 *
 * <h2>Java</h2>
 * <pre>{@code
 * FxFileInput upload = new FxFileInput();
 * upload.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
 * upload.fileProperty().addListener((obs, was, now) -> preview(now));
 *
 * FxFileInput many = new FxFileInput();
 * many.setMultiple(true);
 * many.setButtonText("Browse...");
 * }</pre>
 *
 * <h2>FXML</h2>
 * <pre>{@code
 * <FxFileInput multiple="true" size="SM"/>
 * }</pre>
 */
public class FxFileInput extends HBox {

    public static final String STYLE_CLASS = "fxk-file";

    private static final String SIZE_STYLE_CLASS_PREFIX = "fxk-file-size-";
    private static final String COLOR_STYLE_CLASS_PREFIX = "fxk-field-color-";
    private static final String SHADOW_STYLE_CLASS = "fxk-field-shadow";
    private static final PseudoClass DRAG_OVER = PseudoClass.getPseudoClass("drag-over");

    public static final Size DEFAULT_SIZE = Size.MD;
    public static final Color DEFAULT_COLOR = Color.GRAY;

    /** Flowbite's {@code sizing}: SM {@code text-xs}, MD {@code text-sm}, LG {@code text-lg}. */
    public enum Size {
        SM, MD, LG
    }

    /** Flowbite's {@code color}. GRAY is the neutral default; the rest signal a validation state. */
    public enum Color {
        GRAY, INFO, FAILURE, WARNING, SUCCESS
    }

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE);

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);

    private final BooleanProperty shadow = new SimpleBooleanProperty(this, "shadow", false);

    private final BooleanProperty multiple = new SimpleBooleanProperty(this, "multiple", false);

    private final StringProperty buttonText = new SimpleStringProperty(this, "buttonText", "Choose file");

    private final StringProperty placeholder = new SimpleStringProperty(this, "placeholder", "No file chosen");

    private final ObservableList<File> files = FXCollections.observableArrayList();
    private final ObservableList<File> readOnlyFiles = FXCollections.unmodifiableObservableList(files);
    private final ReadOnlyObjectWrapper<File> file = new ReadOnlyObjectWrapper<>(this, "file");
    private final ObservableList<FileChooser.ExtensionFilter> extensionFilters = FXCollections.observableArrayList();
    private final ObjectProperty<Consumer<List<File>>> onRejected = new SimpleObjectProperty<>(this, "onRejected");

    private final Label button = new Label();
    private final Label name = new Label();

    public FxFileInput() {
        getStyleClass().addAll(FxTextInput.FIELD_STYLE_CLASS, STYLE_CLASS);
        EnumStyleClassSync.sync(this, SIZE_STYLE_CLASS_PREFIX, size);
        EnumStyleClassSync.sync(this, COLOR_STYLE_CLASS_PREFIX, color);
        BooleanStyleClassSync.sync(this, SHADOW_STYLE_CLASS, shadow);

        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);

        button.getStyleClass().add("fxk-file-button");
        name.getStyleClass().add("fxk-file-name");
        // Labels do not grow on their own: the button fills the field's height, the name its width.
        button.setMaxHeight(Double.MAX_VALUE);
        name.setMaxHeight(Double.MAX_VALUE);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        getChildren().addAll(button, name);

        button.textProperty().bind(buttonText);
        name.textProperty().bind(Bindings.createStringBinding(this::describe, files, placeholder));
        files.addListener((ListChangeListener<File>) change -> file.set(files.isEmpty() ? null : files.get(0)));
        // Turning multiple off keeps only the first file, so getFiles() never contradicts the flag.
        multiple.addListener((observable, was, is) -> {
            if (!is && files.size() > 1) {
                files.setAll(files.get(0));
            }
        });

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
            boolean accepted = !isDisabled() && event.getDragboard().hasFiles() && pick(event.getDragboard().getFiles());
            event.setDropCompleted(accepted);
            event.consume();
        });
    }

    private String describe() {
        if (files.isEmpty()) {
            return getPlaceholder();
        }
        return files.size() == 1 ? files.get(0).getName() : files.size() + " files";
    }

    /** Matches a file against the extension filters ({@code "*.png"}, {@code "*.*"}); no filters accept everything. */
    private boolean accepts(File candidate) {
        if (extensionFilters.isEmpty()) {
            return true;
        }
        String fileName = candidate.getName().toLowerCase(Locale.ROOT);
        for (FileChooser.ExtensionFilter filter : extensionFilters) {
            for (String pattern : filter.getExtensions()) {
                String p = pattern.toLowerCase(Locale.ROOT);
                if (p.equals("*") || p.equals("*.*") || fileName.endsWith(p.startsWith("*") ? p.substring(1) : p)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Opens the file dialog, exactly as clicking the field does. Does nothing while disabled. */
    public void choose() {
        if (isDisabled()) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().setAll(extensionFilters);
        Window owner = getScene() == null ? null : getScene().getWindow();

        List<File> picked;
        if (isMultiple()) {
            picked = chooser.showOpenMultipleDialog(owner);
        } else {
            File single = chooser.showOpenDialog(owner);
            picked = single == null ? null : List.of(single);
        }
        if (picked != null && !picked.isEmpty()) {
            files.setAll(picked);
        }
    }

    /**
     * Sets the selection as if these files had been dropped: directories and files rejected by
     * {@link #getExtensionFilters()} are skipped (and reported to {@link #getOnRejected()}), and only the
     * first accepted file is kept unless {@link #isMultiple()}. The previous selection is replaced only
     * when at least one file is accepted. Does nothing while disabled.
     *
     * @param candidates the files to offer; may be {@code null}
     * @return whether at least one file was accepted
     */
    public boolean pick(List<File> candidates) {
        if (isDisabled() || candidates == null || candidates.isEmpty()) {
            return false;
        }
        List<File> usable = candidates.stream().filter(f -> f.isFile() && accepts(f)).toList();
        List<File> rejected = candidates.stream().filter(f -> !usable.contains(f)).toList();
        if (!usable.isEmpty()) {
            files.setAll(isMultiple() ? usable : usable.subList(0, 1));
        }
        Consumer<List<File>> handler = getOnRejected();
        if (!rejected.isEmpty() && handler != null) {
            handler.accept(rejected);
        }
        return !usable.isEmpty();
    }

    /** Forgets the picked file(s). */
    public void clear() {
        files.clear();
    }

    // ---- value
    // ---------------------------------------------------------------------------------------

    /** @return the first picked file, or {@code null} */
    public File getFile() {
        return file.get();
    }

    public ReadOnlyObjectProperty<File> fileProperty() {
        return file.getReadOnlyProperty();
    }

    /** @return every picked file (one at most unless {@link #isMultiple()}); never modifiable */
    public ObservableList<File> getFiles() {
        return readOnlyFiles;
    }

    /** The filters offered by the dialog and enforced on dropped files. */
    public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    /** @return the callback for rejected files, or {@code null} */
    public Consumer<List<File>> getOnRejected() {
        return onRejected.get();
    }

    /** Called with the files that {@link #pick(List)} (so: a drop) refused; never called with an empty list. */
    public void setOnRejected(Consumer<List<File>> onRejected) {
        this.onRejected.set(onRejected);
    }

    public ObjectProperty<Consumer<List<File>>> onRejectedProperty() {
        return onRejected;
    }

    // ---- properties
    // -----------------------------------------------------------------------------

    public Size getSize() {
        return size.get();
    }

    public void setSize(Size size) {
        this.size.set(size);
    }

    public ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    /** @return whether the field has a drop shadow (Flowbite's {@code shadow}) */
    public boolean isShadow() {
        return shadow.get();
    }

    public void setShadow(boolean shadow) {
        this.shadow.set(shadow);
    }

    public BooleanProperty shadowProperty() {
        return shadow;
    }

    /** @return whether several files can be picked (Flowbite's {@code multiple}) */
    public boolean isMultiple() {
        return multiple.get();
    }

    public void setMultiple(boolean multiple) {
        this.multiple.set(multiple);
    }

    public BooleanProperty multipleProperty() {
        return multiple;
    }

    public String getButtonText() {
        return buttonText.get();
    }

    /** The text of the button part; "Choose file" by default. */
    public void setButtonText(String buttonText) {
        this.buttonText.set(buttonText);
    }

    public StringProperty buttonTextProperty() {
        return buttonText;
    }

    public String getPlaceholder() {
        return placeholder.get();
    }

    /** The text shown while no file is picked; "No file chosen" by default. */
    public void setPlaceholder(String placeholder) {
        this.placeholder.set(placeholder);
    }

    public StringProperty placeholderProperty() {
        return placeholder;
    }
}
