package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Pure-logic tests (no JavaFX toolkit): the rules every file field applies to dropped and chosen files. */
class FileSelectionTest {

    @TempDir
    Path dir;

    private File file(String name) throws IOException {
        return Files.writeString(dir.resolve(name), "x").toFile();
    }

    private static final List<String> PNG = List.of("*.png");

    @Test
    void noPatternsAcceptsAnyRegularFile() throws IOException {
        File a = file("a.txt");
        var r = FileSelection.select(List.of(a), List.of(), true);
        assertEquals(List.of(a), r.accepted());
        assertTrue(r.rejected().isEmpty());
    }

    @Test
    void filesOutsideThePatternsAreRejectedAndReportedInOrder() throws IOException {
        File good = file("a.png");
        File bad1 = file("b.exe");
        File bad2 = file("c.txt");
        var r = FileSelection.select(List.of(bad1, good, bad2), PNG, true);
        assertEquals(List.of(good), r.accepted());
        assertEquals(List.of(bad1, bad2), r.rejected());
    }

    @Test
    void matchingIsCaseInsensitive() throws IOException {
        File upper = file("PHOTO.PNG");
        assertEquals(List.of(upper), FileSelection.select(List.of(upper), PNG, true).accepted());
        assertEquals(List.of(upper), FileSelection.select(List.of(upper), List.of("*.Png"), true).accepted());
    }

    @Test
    void wildcardPatternsAcceptEverything() throws IOException {
        File any = file("README");
        assertEquals(List.of(any), FileSelection.select(List.of(any), List.of("*.*"), true).accepted());
        assertEquals(List.of(any), FileSelection.select(List.of(any), List.of("*"), true).accepted());
    }

    @Test
    void multiPartExtensionsMatchTheWholeSuffix() throws IOException {
        File tgz = file("backup.tar.gz");
        File gz = file("backup.gz");
        var r = FileSelection.select(List.of(tgz, gz), List.of("*.tar.gz"), true);
        assertEquals(List.of(tgz), r.accepted());
        assertEquals(List.of(gz), r.rejected());
    }

    @Test
    void anyOfSeveralPatternsMatches() throws IOException {
        File jpg = file("a.jpg");
        var r = FileSelection.select(List.of(jpg), List.of("*.png", "*.jpg"), true);
        assertEquals(List.of(jpg), r.accepted());
    }

    @Test
    void directoriesAndMissingFilesAreRejected() throws IOException {
        File folder = Files.createDirectory(dir.resolve("folder.png")).toFile();
        File missing = dir.resolve("ghost.png").toFile();
        var r = FileSelection.select(List.of(folder, missing), PNG, true);
        assertTrue(r.accepted().isEmpty());
        assertEquals(List.of(folder, missing), r.rejected());
    }

    @Test
    void singleModeKeepsOnlyTheFirstAcceptedAndDoesNotReportTheRest() throws IOException {
        File first = file("a.png");
        File second = file("b.png");
        var r = FileSelection.select(List.of(first, second), PNG, false);
        assertEquals(List.of(first), r.accepted());
        assertTrue(r.rejected().isEmpty());
    }

    @Test
    void multipleModeKeepsEveryAcceptedFile() throws IOException {
        File first = file("a.png");
        File second = file("b.png");
        var r = FileSelection.select(List.of(first, second), PNG, true);
        assertEquals(List.of(first, second), r.accepted());
    }

    @Test
    void nullElementsAreIgnored() throws IOException {
        File good = file("a.png");
        var r = FileSelection.select(java.util.Arrays.asList(null, good), PNG, true);
        assertEquals(List.of(good), r.accepted());
        assertTrue(r.rejected().isEmpty());
    }

    @Test
    void resultListsAreImmutable() throws IOException {
        var r = FileSelection.select(List.of(file("a.png")), PNG, true);
        boolean threw = false;
        try {
            r.accepted().add(new File("x"));
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        assertTrue(threw);
        assertFalse(r.accepted().isEmpty());
    }
}
