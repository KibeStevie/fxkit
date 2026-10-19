package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import dev.fxkit.core.FxImagePolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Only the paths that end before any pixel is decoded, so no toolkit and no network are involved. */
class ImageSourcesTest {

    @AfterEach
    void restoreDefault() {
        FxImagePolicy.resetAllowedSchemes();
    }

    @Test
    void nothingToLoadGivesNull() {
        assertNull(ImageSources.load(null, 100, 100));
        assertNull(ImageSources.load("", 100, 100));
        assertNull(ImageSources.load("   ", 100, 100));
    }

    @Test
    void urlsRefusedByThePolicyAreNotLoaded() {
        assertNull(ImageSources.load("ftp://example.com/a.png", 100, 100));
        assertNull(ImageSources.load("file://attacker/share/a.png", 100, 100));

        FxImagePolicy.setAllowedSchemes(Set.of("file", "jar"));
        assertNull(ImageSources.load("https://example.com/a.png", 100, 100));
    }

    @Test
    void aMissingClasspathResourceGivesNullInsteadOfThrowing() {
        assertNull(ImageSources.load("/definitely/not/here.png", 100, 100));
    }

    @Test
    void aNonPositiveBoxIsAProgrammingError() {
        boolean threw = false;
        try {
            ImageSources.load("/a.png", 0, 100);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    void logDescriptionsNeverContainThePathQueryOrCredentials() {
        String described = ImageSources.describe("https://user:secret@images.example.com/private/a.png?token=abc123");
        assertEquals("https: URL on images.example.com", described);
        assertFalse(described.contains("secret"));
        assertFalse(described.contains("abc123"));
        assertEquals("classpath resource", ImageSources.describe("/images/a.png"));
        assertEquals("data: URI", ImageSources.describe("data:image/png;base64,AAAA"));
        assertEquals("file: URL", ImageSources.describe("file:/C:/Users/me/a.png"));
    }

    // ---- cover crop ------------------------------------------------------------------------

    @Test
    void aWideImageIsCroppedAtTheSidesToFillASquare() {
        var crop = ImageSources.coverCrop(400, 200, 100, 100);
        assertEquals(100.0, crop.x());
        assertEquals(0.0, crop.y());
        assertEquals(200.0, crop.width());
        assertEquals(200.0, crop.height());
    }

    @Test
    void aTallImageIsCroppedAtTheTopAndBottom() {
        var crop = ImageSources.coverCrop(200, 400, 100, 100);
        assertEquals(0.0, crop.x());
        assertEquals(100.0, crop.y());
        assertEquals(200.0, crop.width());
        assertEquals(200.0, crop.height());
    }

    @Test
    void theCropAlwaysHasTheBoxAspectRatio() {
        var crop = ImageSources.coverCrop(1920, 1080, 300, 200);
        assertEquals(300.0 / 200.0, crop.width() / crop.height(), 1e-9);
        assertTrue(crop.x() >= 0 && crop.y() >= 0);
        assertTrue(crop.x() + crop.width() <= 1920 + 1e-9 && crop.y() + crop.height() <= 1080 + 1e-9);
    }

    @Test
    void unknownSizesGiveNoCrop() {
        assertNull(ImageSources.coverCrop(0, 100, 100, 100)); // image not loaded yet
        assertNull(ImageSources.coverCrop(100, 100, 0, 100)); // card not laid out yet
        assertNull(ImageSources.coverCrop(Double.NaN, 100, 100, 100));
    }
}
