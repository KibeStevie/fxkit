package dev.fxkit.core.components.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * choose() must apply the same rules as a drop. The real dialog blocks, so a subclass replaces
 * {@code showDialog()} with a canned answer. Needs the JavaFX toolkit; skipped (not failed) where it
 * cannot start, for example on a CI machine without a display.
 */
class FxFileInputChooseTest {

    private static boolean toolkitAvailable;

    @TempDir
    Path dir;

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyRunning) {
            started.countDown();
        } catch (RuntimeException | Error noDisplay) {
            toolkitAvailable = false;
            return;
        }
        toolkitAvailable = started.await(10, TimeUnit.SECONDS);
    }

    /** An FxFileInput whose dialog answers with {@code answer} instead of opening. */
    private static final class CannedDialog extends FxFileInput {
        List<File> answer;
        int opened;

        @Override
        List<File> showDialog() {
            opened++;
            return answer;
        }
    }

    private File file(String name) throws IOException {
        return Files.writeString(dir.resolve(name), "x").toFile();
    }

    private static CannedDialog images() {
        CannedDialog input = new CannedDialog();
        input.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        return input;
    }

    @Test
    void aFileTheFiltersAcceptIsSelected() throws IOException {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        File png = file("a.png");
        input.answer = List.of(png);

        input.choose();

        assertEquals(png, input.getFile());
    }

    @Test
    void aFileTheDialogLetThroughButTheFiltersRefuseIsRejectedAndReported() throws IOException {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        List<File> rejected = new ArrayList<>();
        input.setOnRejected(rejected::addAll);
        File exe = file("setup.exe");
        input.answer = List.of(exe);

        input.choose();

        assertNull(input.getFile());
        assertEquals(List.of(exe), rejected);
    }

    @Test
    void aRefusedChoiceKeepsThePreviousSelection() throws IOException {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        File png = file("a.png");
        input.answer = List.of(png);
        input.choose();

        input.answer = List.of(file("notes.txt"));
        input.choose();

        assertEquals(png, input.getFile());
    }

    @Test
    void cancellingKeepsThePreviousSelectionAndReportsNothing() throws IOException {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        List<File> rejected = new ArrayList<>();
        input.setOnRejected(rejected::addAll);
        File png = file("a.png");
        input.answer = List.of(png);
        input.choose();

        input.answer = null; // the user cancelled
        input.choose();

        assertEquals(png, input.getFile());
        assertTrue(rejected.isEmpty());
    }

    @Test
    void singleModeKeepsOnlyTheFirstFile() throws IOException {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        File first = file("a.png");
        input.answer = List.of(first, file("b.png"));

        input.choose();

        assertEquals(List.of(first), List.copyOf(input.getFiles()));
    }

    @Test
    void multipleModeKeepsEveryAcceptedFileAndReportsTheRest() throws IOException {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        input.setMultiple(true);
        List<File> rejected = new ArrayList<>();
        input.setOnRejected(rejected::addAll);
        File a = file("a.png");
        File b = file("b.jpg");
        File bad = file("c.gif");
        input.answer = List.of(a, bad, b);

        input.choose();

        assertEquals(List.of(a, b), List.copyOf(input.getFiles()));
        assertEquals(List.of(bad), rejected);
    }

    @Test
    void aDisabledFieldDoesNotOpenTheDialog() {
        assumeTrue(toolkitAvailable);
        CannedDialog input = images();
        input.setDisable(true);

        input.choose();

        assertEquals(0, input.opened);
    }
}
