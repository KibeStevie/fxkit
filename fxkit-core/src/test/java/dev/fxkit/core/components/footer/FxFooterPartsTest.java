package dev.fxkit.core.components.footer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import dev.fxkit.core.FxThreadExtension;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The footer's small parts: link, brand, copyright line, title, divider and (without an icon) the icon
 * link. Icons themselves are not covered here: they need an Ikonli icon pack on the test classpath.
 * Runs on the FX thread; skipped without a toolkit.
 */
@ExtendWith(FxThreadExtension.class)
class FxFooterPartsTest {

    // ---- link -------------------------------------------------------------------------------

    @Test
    void aFooterLinkIsAHyperlinkWithTheFooterLinkClass() {
        FxFooterLink link = new FxFooterLink("About");

        assertEquals("About", link.getText());
        assertTrue(link.getStyleClass().contains("fxk-footer-link"));
    }

    @Test
    void theHandlerConstructorWiresTheAction() {
        AtomicInteger clicks = new AtomicInteger();
        EventHandler<ActionEvent> handler = e -> clicks.incrementAndGet();

        FxFooterLink link = new FxFooterLink("About", handler);
        link.fire();

        assertEquals(1, clicks.get());
    }

    // ---- brand ------------------------------------------------------------------------------

    @Test
    void aBrandWithoutALogoIsJustItsName() {
        FxFooterBrand brand = new FxFooterBrand("Flowbite");

        assertEquals("Flowbite", brand.getName());
        assertEquals("Flowbite", brand.getText());
        assertSame(brand.textProperty(), brand.nameProperty());
        assertTrue(brand.getStyleClass().contains("fxk-footer-brand"));
        assertEquals(ContentDisplay.LEFT, brand.getContentDisplay());
        assertNull(brand.getGraphic());
        assertNull(brand.getImage());
    }

    @Test
    void aLogoImageIsScaledTo32PixelsHighKeepingItsAspectRatio() {
        Image logo = new WritableImage(80, 40);

        FxFooterBrand brand = new FxFooterBrand("Flowbite", logo);

        ImageView view = assertInstanceOf(ImageView.class, brand.getGraphic());
        assertSame(logo, view.getImage());
        assertEquals(32, view.getFitHeight(), 1e-9, "h-8");
        assertTrue(view.isPreserveRatio());
        assertTrue(view.isSmooth());
    }

    @Test
    void settingAndClearingTheImageAddsAndRemovesTheLogo() {
        FxFooterBrand brand = new FxFooterBrand("Flowbite");

        brand.setImage(new WritableImage(10, 10));
        assertInstanceOf(ImageView.class, brand.getGraphic());

        brand.setImage(null);
        assertNull(brand.getGraphic());
    }

    @Test
    void theBrandIsClickableLikeAnyHyperlink() {
        AtomicInteger clicks = new AtomicInteger();
        FxFooterBrand brand = new FxFooterBrand("Flowbite");
        brand.setOnAction(e -> clicks.incrementAndGet());

        brand.fire();

        assertEquals(1, clicks.get());
    }

    // ---- copyright --------------------------------------------------------------------------

    private static String text(Node node) {
        return node instanceof Hyperlink link ? link.getText() : ((Label) node).getText();
    }

    @Test
    void aBareCopyrightShowsOnlyTheMark() {
        FxFooterCopyright copyright = new FxFooterCopyright();

        assertTrue(copyright.getStyleClass().contains("fxk-footer-copyright"));
        assertEquals(Pos.CENTER_LEFT, copyright.getAlignment());
        assertEquals(4, copyright.getSpacing(), 1e-9, "ml-1");
        assertEquals(1, copyright.getChildren().size());
        assertEquals("\u00A9", text(copyright.getChildren().get(0)));
    }

    @Test
    void theYearFollowsTheMarkAndTheNameFollowsTheYear() {
        FxFooterCopyright copyright = new FxFooterCopyright("Flowbite\u2122", 2022);

        assertEquals(2, copyright.getChildren().size());
        assertEquals("\u00A9 2022", text(copyright.getChildren().get(0)));
        assertEquals("Flowbite\u2122", text(copyright.getChildren().get(1)));
        assertTrue(copyright.getChildren().get(1).getStyleClass().contains("fxk-footer-copyright-text"));
    }

    @Test
    void aMissingYearIsLeftOutNotShownAsNull() {
        FxFooterCopyright copyright = new FxFooterCopyright("Flowbite", null);

        assertEquals("\u00A9", text(copyright.getChildren().get(0)));
    }

    @Test
    void aBlankNameShowsOnlyTheMarkAndYear() {
        FxFooterCopyright copyright = new FxFooterCopyright("", 2022);

        assertEquals(1, copyright.getChildren().size());
        assertEquals("\u00A9 2022", text(copyright.getChildren().get(0)));
    }

    @Test
    void changingTheNameOrYearRebuildsTheLine() {
        FxFooterCopyright copyright = new FxFooterCopyright("Flowbite", 2022);

        copyright.setYear(2023);
        copyright.setBy("Acme");

        assertEquals("\u00A9 2023", text(copyright.getChildren().get(0)));
        assertEquals("Acme", text(copyright.getChildren().get(1)));
    }

    @Test
    void withAnActionHandlerTheNameBecomesALink() {
        AtomicInteger clicks = new AtomicInteger();
        FxFooterCopyright copyright = new FxFooterCopyright("Flowbite", 2022);
        assertInstanceOf(Label.class, copyright.getChildren().get(1));

        copyright.setOnAction(e -> clicks.incrementAndGet());

        Hyperlink link = assertInstanceOf(Hyperlink.class, copyright.getChildren().get(1));
        assertTrue(link.getStyleClass().contains("fxk-footer-copyright-link"));
        link.fire();
        assertEquals(1, clicks.get());
    }

    @Test
    void clearingTheActionHandlerTurnsTheLinkBackIntoPlainText() {
        FxFooterCopyright copyright = new FxFooterCopyright("Flowbite", 2022);
        copyright.setOnAction(e -> { });

        copyright.setOnAction(null);

        assertInstanceOf(Label.class, copyright.getChildren().get(1));
    }

    // ---- title ------------------------------------------------------------------------------

    @Test
    void theTitleIsShownInUpperCase() {
        FxFooterTitle title = new FxFooterTitle("Resources");

        assertEquals("Resources", title.getTitle());
        assertEquals("RESOURCES", title.getText());
        assertTrue(title.getStyleClass().contains("fxk-footer-title"));
    }

    @Test
    void changingTheTitleUpdatesTheShownText() {
        FxFooterTitle title = new FxFooterTitle("Resources");

        title.setTitle("Follow us");

        assertEquals("FOLLOW US", title.getText());
    }

    @Test
    void aNullTitleShowsNothing() {
        FxFooterTitle title = new FxFooterTitle(null);

        assertEquals("", title.getText());
    }

    @Test
    void theShownTextIsBoundSoItCannotBeSetDirectly() {
        FxFooterTitle title = new FxFooterTitle("Resources");

        assertThrows(RuntimeException.class, () -> title.setText("other"));
    }

    @Test
    void upperCasingIgnoresTheMachinesLanguage() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            // with the default locale this would become a dotted capital I: "\u0130STANBUL"
            assertEquals("ISTANBUL", new FxFooterTitle("istanbul").getText());
        } finally {
            Locale.setDefault(original);
        }
    }

    // ---- divider and icon -------------------------------------------------------------------

    @Test
    void theDividerIsAPlainRegionWithItsStyleClass() {
        FxFooterDivider divider = new FxFooterDivider();

        assertTrue(divider.getStyleClass().contains("fxk-footer-divider"));
        assertTrue(divider.getChildrenUnmodifiable().isEmpty());
    }

    @Test
    void anIconLinkWithoutAnIconIsAGraphicOnlyLinkWithNothingToShow() {
        FxFooterIcon icon = new FxFooterIcon(null);

        assertTrue(icon.getStyleClass().contains("fxk-footer-icon"));
        assertEquals(ContentDisplay.GRAPHIC_ONLY, icon.getContentDisplay());
        assertNull(icon.getIcon());
        assertNull(icon.getGraphic());
    }
}
