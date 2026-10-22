package dev.fxkit.core.components.avatar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.fxkit.core.JavaFxToolkit;
import dev.fxkit.core.TestNodes;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * FxAvatar's size, status dot, face (image, initials or placeholder), accessibility and text content.
 * Loading from a URL is covered by {@code FxAvatarImageTest}. Needs the toolkit; skipped without one.
 */
class FxAvatarTest {

    @BeforeEach
    void requireToolkit() {
        assumeTrue(JavaFxToolkit.ensureStarted());
    }

    private static StackPane face(FxAvatar avatar) {
        return TestNodes.require(avatar, "fxk-avatar-face", StackPane.class);
    }

    private static Region dot(FxAvatar avatar) {
        return TestNodes.require(avatar, "fxk-avatar-status", Region.class);
    }

    private static StackPane inner(FxAvatar avatar) {
        return TestNodes.require(avatar, "fxk-avatar-inner", StackPane.class);
    }

    /** An image the toolkit has flagged as failed, or the calling test is skipped. */
    private static Image brokenImage() {
        Image broken = null;
        try {
            broken = new Image("data:image/png;base64,AAAA");
        } catch (RuntimeException ignored) {
            // handled by the assumption below
        }
        assumeTrue(broken != null && broken.isError(), "this JavaFX build did not flag the bad image as an error");
        return broken;
    }

    // ---- defaults ---------------------------------------------------------------------------

    @Test
    void aNewAvatarIsMediumLightAndPlain() {
        FxAvatar avatar = new FxAvatar();

        assertEquals(FxAvatar.Size.MD, avatar.getSize());
        assertEquals(FxAvatar.Color.LIGHT, avatar.getColor());
        assertTrue(avatar.getStyleClass().contains("fxk-avatar"));
        assertTrue(avatar.getStyleClass().contains("fxk-avatar-size-md"));
        assertTrue(avatar.getStyleClass().contains("fxk-avatar-color-light"));
        assertFalse(avatar.isRounded());
        assertFalse(avatar.isBordered());
        assertFalse(avatar.isStacked());
        assertNull(avatar.getStatus());
        assertNull(avatar.getImage());
        assertNull(avatar.getContent());
    }

    @Test
    void roundedBorderedAndStackedAreStyleClassesThatFollowTheirProperties() {
        FxAvatar avatar = new FxAvatar();

        avatar.setRounded(true);
        avatar.setBordered(true);
        avatar.setStacked(true);
        assertTrue(avatar.getStyleClass().containsAll(
                java.util.List.of("fxk-avatar-rounded", "fxk-avatar-bordered", "fxk-avatar-stacked")));

        avatar.setRounded(false);
        avatar.setBordered(false);
        avatar.setStacked(false);
        assertFalse(avatar.getStyleClass().contains("fxk-avatar-rounded"));
        assertFalse(avatar.getStyleClass().contains("fxk-avatar-bordered"));
        assertFalse(avatar.getStyleClass().contains("fxk-avatar-stacked"));
    }

    @Test
    void changingTheColorSwapsTheStyleClass() {
        FxAvatar avatar = new FxAvatar();

        avatar.setColor(FxAvatar.Color.PURPLE);

        assertTrue(avatar.getStyleClass().contains("fxk-avatar-color-purple"));
        assertFalse(avatar.getStyleClass().contains("fxk-avatar-color-light"));
    }

    // ---- size -------------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"XS,24", "SM,32", "MD,40", "LG,80", "XL,144"})
    void theInnerBoxIsPinnedToTheSizeInPixels(FxAvatar.Size size, double px) {
        FxAvatar avatar = new FxAvatar();

        avatar.setSize(size);

        StackPane inner = inner(avatar);
        assertEquals(px, inner.getMinWidth(), 1e-9);
        assertEquals(px, inner.getPrefWidth(), 1e-9);
        assertEquals(px, inner.getMaxWidth(), 1e-9);
        assertEquals(px, inner.getMinHeight(), 1e-9);
        assertEquals(px, inner.getPrefHeight(), 1e-9);
        assertEquals(px, inner.getMaxHeight(), 1e-9);
        assertTrue(avatar.getStyleClass().contains("fxk-avatar-size-" + size.name().toLowerCase(java.util.Locale.ROOT)));
    }

    @Test
    void aNullSizeFallsBackToMediumWithoutThrowing() {
        FxAvatar avatar = new FxAvatar();
        avatar.setSize(FxAvatar.Size.XL);

        avatar.setSize(null);

        assertEquals(40, inner(avatar).getPrefWidth(), 1e-9);
        assertTrue(avatar.getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-avatar-size-")));
    }

    // ---- status dot -------------------------------------------------------------------------

    @Test
    void theStatusDotIsHiddenAndTakesNoSpaceUntilAStatusIsSet() {
        FxAvatar avatar = new FxAvatar();

        assertFalse(dot(avatar).isVisible());
        assertFalse(dot(avatar).isManaged());
    }

    @Test
    void settingAStatusShowsTheDotWithItsStyleClass() {
        FxAvatar avatar = new FxAvatar();

        avatar.setStatus(FxAvatar.Status.ONLINE);

        assertTrue(dot(avatar).isVisible());
        assertTrue(dot(avatar).isManaged());
        assertTrue(dot(avatar).getStyleClass().contains("fxk-avatar-status-online"));
    }

    @Test
    void changingTheStatusSwapsTheClassAndClearingItHidesTheDot() {
        FxAvatar avatar = new FxAvatar();
        avatar.setStatus(FxAvatar.Status.ONLINE);

        avatar.setStatus(FxAvatar.Status.BUSY);
        assertTrue(dot(avatar).getStyleClass().contains("fxk-avatar-status-busy"));
        assertFalse(dot(avatar).getStyleClass().contains("fxk-avatar-status-online"));

        avatar.setStatus(null);
        assertFalse(dot(avatar).isVisible());
        assertTrue(dot(avatar).getStyleClass().stream().noneMatch(c -> c.startsWith("fxk-avatar-status-")));
    }

    @Test
    void theStatusDotDoesNotInterceptMouseEvents() {
        assertTrue(dot(new FxAvatar()).isMouseTransparent());
    }

    @ParameterizedTest
    @CsvSource({
            "TOP_LEFT,-4,-4", "TOP_CENTER,0,-4", "TOP_RIGHT,4,-4",
            "CENTER_LEFT,-4,0", "CENTER,0,0", "CENTER_RIGHT,4,0",
            "BOTTOM_LEFT,-4,4", "BOTTOM_CENTER,0,4", "BOTTOM_RIGHT,4,4"})
    void everyPositionAlignsTheDotAndPushesEdgesFourPixelsOutside(
            FxAvatar.StatusPosition position, double dx, double dy) {
        FxAvatar avatar = new FxAvatar();

        avatar.setStatusPosition(position);

        assertEquals(Pos.valueOf(position.name()), StackPane.getAlignment(dot(avatar)));
        assertEquals(dx, dot(avatar).getTranslateX(), 1e-9);
        assertEquals(dy, dot(avatar).getTranslateY(), 1e-9);
    }

    @Test
    void theDotStartsTopLeftAndANullPositionFallsBackToIt() {
        FxAvatar avatar = new FxAvatar();
        assertEquals(FxAvatar.StatusPosition.TOP_LEFT, avatar.getStatusPosition());
        assertEquals(-4, dot(avatar).getTranslateX(), 1e-9);

        avatar.setStatusPosition(FxAvatar.StatusPosition.BOTTOM_RIGHT);
        avatar.setStatusPosition(null);

        assertEquals(Pos.TOP_LEFT, StackPane.getAlignment(dot(avatar)));
        assertEquals(-4, dot(avatar).getTranslateX(), 1e-9);
        assertEquals(-4, dot(avatar).getTranslateY(), 1e-9);
    }

    // ---- face -------------------------------------------------------------------------------

    @Test
    void withoutAnImageOrInitialsTheFaceShowsThePlaceholderGlyph() {
        FxAvatar avatar = new FxAvatar();

        assertTrue(TestNodes.has(avatar, "fxk-avatar-placeholder"));
        assertFalse(TestNodes.has(avatar, "fxk-avatar-initials"));
    }

    @Test
    void initialsReplaceThePlaceholder() {
        FxAvatar avatar = new FxAvatar();

        avatar.setPlaceholderInitials("JD");

        assertEquals("JD", TestNodes.require(avatar, "fxk-avatar-initials", Label.class).getText());
        assertFalse(TestNodes.has(avatar, "fxk-avatar-placeholder"));
    }

    @Test
    void blankOrClearedInitialsGoBackToThePlaceholder() {
        FxAvatar avatar = new FxAvatar();

        avatar.setPlaceholderInitials("   ");
        assertTrue(TestNodes.has(avatar, "fxk-avatar-placeholder"));

        avatar.setPlaceholderInitials("JD");
        avatar.setPlaceholderInitials(null);
        assertTrue(TestNodes.has(avatar, "fxk-avatar-placeholder"));
        assertFalse(TestNodes.has(avatar, "fxk-avatar-initials"));
    }

    @Test
    void anImageIsShownInPreferenceToInitials() {
        FxAvatar avatar = new FxAvatar();
        avatar.setPlaceholderInitials("JD");

        avatar.setImage(new WritableImage(50, 50));

        assertEquals(1, face(avatar).getChildren().size());
        assertInstanceOf(ImageView.class, face(avatar).getChildren().get(0));
        assertFalse(TestNodes.has(avatar, "fxk-avatar-initials"));
    }

    @Test
    void removingTheImageBringsTheInitialsBack() {
        FxAvatar avatar = new FxAvatar();
        avatar.setPlaceholderInitials("JD");
        avatar.setImage(new WritableImage(50, 50));

        avatar.setImage((Image) null);

        assertTrue(TestNodes.has(avatar, "fxk-avatar-initials"));
    }

    @Test
    void anImageThatFailedToLoadFallsBackToInitialsThenPlaceholder() {
        FxAvatar avatar = new FxAvatar();
        avatar.setPlaceholderInitials("JD");

        avatar.setImage(brokenImage());
        assertTrue(TestNodes.has(avatar, "fxk-avatar-initials"));

        avatar.setPlaceholderInitials(null);
        assertTrue(TestNodes.has(avatar, "fxk-avatar-placeholder"));
    }

    @ParameterizedTest
    @CsvSource({"200,100,50,0,100,100", "100,200,0,50,100,100", "80,80,0,0,80,80"})
    void aNonSquareImageIsCenterCroppedToASquare(
            int width, int height, double x, double y, double w, double h) {
        FxAvatar avatar = new FxAvatar();

        avatar.setImage(new WritableImage(width, height));

        ImageView view = assertInstanceOf(ImageView.class, face(avatar).getChildren().get(0));
        assertEquals(new Rectangle2D(x, y, w, h), view.getViewport());
    }

    @Test
    void theCropIsClearedWhenTheImageIsRemoved() {
        FxAvatar avatar = new FxAvatar();
        avatar.setImage(new WritableImage(200, 100));
        ImageView view = assertInstanceOf(ImageView.class, face(avatar).getChildren().get(0));

        avatar.setImage((Image) null);

        assertNull(view.getViewport());
        assertNull(view.getImage());
    }

    // ---- accessibility ----------------------------------------------------------------------

    @Test
    void theAvatarIsAnImageForAssistiveTechnologyAndAltBecomesItsText() {
        FxAvatar avatar = new FxAvatar();
        StackPane inner = inner(avatar);

        assertEquals(AccessibleRole.IMAGE_VIEW, inner.getAccessibleRole());
        assertNull(avatar.getAlt());

        avatar.setAlt("Jane Doe");

        assertEquals("Jane Doe", avatar.getAlt());
        assertEquals("Jane Doe", inner.getAccessibleText());
    }

    // ---- content and texts ------------------------------------------------------------------

    @Test
    void contentSitsNextToTheAvatarAndCanBeReplacedOrRemoved() {
        FxAvatar avatar = new FxAvatar();
        Label first = new Label("first");
        Label second = new Label("second");

        avatar.setContent(first);
        assertEquals(2, avatar.getChildren().size());
        assertSame(first, avatar.getChildren().get(1));

        avatar.setContent(second);
        assertEquals(2, avatar.getChildren().size());
        assertSame(second, avatar.getChildren().get(1));

        avatar.setContent(null);
        assertEquals(1, avatar.getChildren().size());
    }

    @Test
    void setTextsShowsATitleAndASubtitle() {
        FxAvatar avatar = new FxAvatar();

        avatar.setTexts("Jane Doe", "Joined in 2014");

        VBox box = assertInstanceOf(VBox.class, avatar.getContent());
        assertTrue(box.getStyleClass().contains("fxk-avatar-texts"));
        assertEquals("Jane Doe", TestNodes.require(avatar, "fxk-avatar-title", Label.class).getText());
        assertEquals("Joined in 2014", TestNodes.require(avatar, "fxk-avatar-subtitle", Label.class).getText());
    }

    @Test
    void aNullOrBlankSubtitleLeavesOnlyTheTitle() {
        FxAvatar avatar = new FxAvatar();

        avatar.setTexts("Jane Doe", null);
        assertFalse(TestNodes.has(avatar, "fxk-avatar-subtitle"));

        avatar.setTexts("Jane Doe", "   ");
        assertFalse(TestNodes.has(avatar, "fxk-avatar-subtitle"));
    }

    @Test
    void callingSetTextsAgainReplacesTheEarlierTexts() {
        FxAvatar avatar = new FxAvatar();
        avatar.setTexts("One", "uno");
        Label oldTitle = TestNodes.require(avatar, "fxk-avatar-title", Label.class);

        avatar.setTexts("Two", "dos");

        assertEquals(2, avatar.getChildren().size());
        Label newTitle = TestNodes.require(avatar, "fxk-avatar-title", Label.class);
        assertEquals("Two", newTitle.getText());
        assertFalse(oldTitle == newTitle);
    }
}
