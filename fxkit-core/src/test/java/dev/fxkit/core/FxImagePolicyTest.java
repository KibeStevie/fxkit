package dev.fxkit.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Pure-logic tests (no JavaFX toolkit). The policy is process-wide, so every test restores the default. */
class FxImagePolicyTest {

    @AfterEach
    void restoreDefault() {
        FxImagePolicy.resetAllowedSchemes();
    }

    private static void assertAllowed(String url) {
        assertTrue(FxImagePolicy.isAllowed(url), "should be allowed: " + url);
    }

    private static void assertRefused(String url) {
        assertFalse(FxImagePolicy.isAllowed(url), "should be refused: " + url);
    }

    @Test
    void classpathResourcesAndDefaultSchemesAreAllowed() {
        assertAllowed("/images/people/profile-picture-1.jpg");
        assertAllowed("images/a.png");
        assertAllowed("https://example.com/a.png");
        assertAllowed("HTTP://example.com/a.png");
        assertAllowed("file:/C:/Users/me/a.png");
        assertAllowed("file:///C:/Users/me/a.png");
        assertAllowed("file://localhost/C:/Users/me/a.png");
        assertAllowed("jar:file:/app/lib/app.jar!/images/a.png");
        assertAllowed("data:image/png;base64,iVBORw0KGgo=");
    }

    @Test
    void nullBlankAndOtherSchemesAreRefused() {
        assertRefused(null);
        assertRefused("");
        assertRefused("   ");
        assertRefused("ftp://example.com/a.png");
        assertRefused("javascript:alert(1)");
        assertRefused("gopher://example.com/a");
    }

    @Test
    void controlCharactersAreRefused() {
        assertRefused("https://example.com/a.png\nhttps://evil.example/b.png");
        assertRefused("https://example.com/a\u0000.png");
        assertRefused("/images/\r\na.png");
    }

    @Test
    void surroundingWhitespaceIsTrimmedNotRefused() {
        // components load the trimmed text, so this is the same URL as "/images/a.png"
        assertAllowed("  /images/a.png \n");
    }

    @Test
    void fileUrlsPointingAtAnotherMachineAreRefused() {
        assertRefused("file://attacker/share/a.png");
        assertRefused("file://attacker.example.com/a.png");
        assertRefused("file:////attacker/share/a.png");
        assertRefused("file:\\\\attacker\\share\\a.png");
        assertRefused("file:/%2F%2Fattacker/share/a.png");
        assertRefused("file:/%5C%5Cattacker/share/a.png");
        assertRefused("FILE://attacker/share/a.png");
        assertRefused("file:/%zz");
    }

    @Test
    void jarUrlsAreJudgedByTheirInnerUrl() {
        assertRefused("jar:file://attacker/share/app.jar!/a.png");
        assertRefused("jar:ftp://example.com/app.jar!/a.png");
        assertAllowed("jar:https://example.com/app.jar!/a.png"); // https is allowed by default
        assertRefused("jar:jar:jar:jar:jar:jar:file:/x.jar!/a!/b!/c!/d!/e!/f.png");
    }

    @Test
    void narrowingTheAllowListAppliesToEveryComponent() {
        FxImagePolicy.setAllowedSchemes(Set.of("file", "jar"));

        assertAllowed("/images/a.png");
        assertAllowed("file:/C:/a.png");
        assertRefused("https://example.com/a.png");
        assertRefused("data:image/png;base64,AAAA");
        assertRefused("jar:https://example.com/app.jar!/a.png");
    }

    @Test
    void anEmptyAllowListLeavesOnlyClasspathResources() {
        FxImagePolicy.setAllowedSchemes(Set.of());

        assertAllowed("/images/a.png");
        assertRefused("file:/C:/a.png");
        assertRefused("https://example.com/a.png");
    }

    @Test
    void schemeNamesAreNormalized() {
        FxImagePolicy.setAllowedSchemes(Set.of(" HTTPS "));

        assertEquals(Set.of("https"), FxImagePolicy.getAllowedSchemes());
        assertAllowed("https://example.com/a.png");
    }

    @Test
    void resetRestoresTheDefaults() {
        FxImagePolicy.setAllowedSchemes(Set.of());
        FxImagePolicy.resetAllowedSchemes();

        assertEquals(Set.of("file", "jar", "http", "https", "data"), FxImagePolicy.getAllowedSchemes());
    }

    @Test
    void aWindowsDriveLetterIsNotMistakenForAScheme() {
        FxImagePolicy.setAllowedSchemes(Set.of());
        assertAllowed("C:\\Users\\me\\a.png"); // not a URL; JavaFX will fail to resolve it and the component logs that
    }
}
