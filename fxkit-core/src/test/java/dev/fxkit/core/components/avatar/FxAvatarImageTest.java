package dev.fxkit.core.components.avatar;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import org.junit.jupiter.api.Test;

/** A bad image URL must leave the avatar in its placeholder state, never throw. Needs the toolkit; skipped without one. */
class FxAvatarImageTest {

    @Test
    void aMissingResourceShowsThePlaceholderInsteadOfThrowing() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        FxAvatar avatar = new FxAvatar("/definitely/not/here.png");
        assertNull(avatar.getImage());
    }

    @Test
    void urlsRefusedByThePolicyAreNotLoaded() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        FxAvatar avatar = new FxAvatar();

        avatar.setImage("ftp://example.com/a.png");
        assertNull(avatar.getImage());

        avatar.setImage("file://attacker/share/a.png");
        assertNull(avatar.getImage());
    }

    @Test
    void nullAndBlankClearTheImage() {
        assumeTrue(JavaFxToolkit.ensureStarted());
        FxAvatar avatar = new FxAvatar();

        avatar.setImage((String) null);
        assertNull(avatar.getImage());

        avatar.setImage("   ");
        assertNull(avatar.getImage());
    }
}
