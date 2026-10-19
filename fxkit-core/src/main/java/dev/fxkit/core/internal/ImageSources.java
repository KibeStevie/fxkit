package dev.fxkit.core.internal;

import java.net.URI;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.fxkit.core.FxImagePolicy;
import javafx.scene.image.Image;

/**
 * How FXKit components turn an image URL into an {@link Image}: policy check first, a bounded decode
 * size, and failures that degrade to "no image" instead of throwing or leaking the URL into logs.
 *
 * <h2>Why bounded</h2>
 * {@code new Image(url, true)} decodes the file at its natural size, so a 20000 x 20000 picture is
 * about 1.6 GB of pixels. Every load here asks JavaFX to fit the image in a maximum box instead
 * (aspect ratio kept, smooth scaling), so memory per image has a hard ceiling. The boxes below are
 * about twice what each component can display, so pictures stay sharp on high-DPI screens. A smaller
 * source is scaled up to the box, which is cheap at these sizes.
 *
 * <p>Not part of FXKit's public API.
 */
public final class ImageSources {

    /** Twice the largest {@code FxAvatar.Size} (144 px), for high-DPI screens. */
    public static final double AVATAR_MAX_PX = 288;

    /** Largest decoded carousel slide. */
    public static final double SLIDE_MAX_WIDTH = 2560;
    public static final double SLIDE_MAX_HEIGHT = 1440;

    /** Largest decoded card background. */
    public static final double BACKDROP_MAX_WIDTH = 1920;
    public static final double BACKDROP_MAX_HEIGHT = 1080;

    private static final Logger LOG = Logger.getLogger("dev.fxkit.image");
    private static final Pattern SCHEME = Pattern.compile("^([A-Za-z][A-Za-z0-9+.\\-]+):");

    private ImageSources() {
        // static utility class, not meant to be instantiated
    }

    /**
     * Starts loading an image in the background.
     *
     * @param url       image URL or classpath resource; {@code null} or blank means "no image"
     * @param maxWidth  decode box width in pixels, greater than 0
     * @param maxHeight decode box height in pixels, greater than 0
     * @return the image (still loading, and possibly failing later: watch {@code errorProperty()}), or
     *         {@code null} if there is nothing to load, the URL is refused by {@link FxImagePolicy}, or
     *         it is invalid or names a resource that does not exist
     * @throws IllegalArgumentException if {@code maxWidth} or {@code maxHeight} is not positive
     */
    public static Image load(String url, double maxWidth, double maxHeight) {
        if (!(maxWidth > 0) || !(maxHeight > 0)) {
            throw new IllegalArgumentException("maxWidth and maxHeight must be positive");
        }
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.strip();
        if (!FxImagePolicy.isAllowed(trimmed)) {
            LOG.warning("Image not loaded: " + describe(trimmed) + " is not allowed by FxImagePolicy");
            return null;
        }

        Image image;
        try {
            image = new Image(trimmed, maxWidth, maxHeight, true, true, true);
        } catch (IllegalArgumentException invalidUrlOrMissingResource) {
            // JavaFX throws here (instead of reporting an error on the image) for a malformed URL or a
            // classpath resource that is not there. The URL stays out of the log: it may carry a token.
            LOG.warning("Image not loaded: " + describe(trimmed) + " is invalid or does not exist");
            return null;
        }

        image.errorProperty().addListener((observable, wasError, isError) -> {
            if (isError) {
                logFailure(image, trimmed);
            }
        });
        if (image.isError()) {
            logFailure(image, trimmed);
        }
        return image;
    }

    private static void logFailure(Image image, String url) {
        Exception cause = image.getException();
        LOG.log(Level.WARNING, "Image failed to load: " + describe(url)
                + (cause == null ? "" : " (" + cause.getClass().getSimpleName() + ")"));
    }

    /** A description safe to log: the kind of source and the host, never the path, query or credentials. */
    static String describe(String url) {
        Matcher matcher = SCHEME.matcher(url);
        if (!matcher.find()) {
            return "classpath resource";
        }
        String scheme = matcher.group(1).toLowerCase(Locale.ROOT);
        if (scheme.equals("data")) {
            return "data: URI";
        }
        try {
            String host = URI.create(url).getHost();
            return scheme + ": URL" + (host == null ? "" : " on " + host);
        } catch (IllegalArgumentException notAUri) {
            return scheme + ": URL";
        }
    }

    /** The part of an image to show so that it covers a box completely, like CSS {@code background-size: cover}. */
    public record Crop(double x, double y, double width, double height) {
    }

    /**
     * @return the centered region of the image that covers a {@code boxWidth x boxHeight} area with the
     *         same aspect ratio as the box (so it can be shown without distortion), or {@code null} if
     *         any size is unknown or not positive
     */
    public static Crop coverCrop(double imageWidth, double imageHeight, double boxWidth, double boxHeight) {
        if (!(imageWidth > 0) || !(imageHeight > 0) || !(boxWidth > 0) || !(boxHeight > 0)) {
            return null;
        }
        double scale = Math.max(boxWidth / imageWidth, boxHeight / imageHeight);
        double width = boxWidth / scale;
        double height = boxHeight / scale;
        return new Crop((imageWidth - width) / 2, (imageHeight - height) / 2, width, height);
    }
}
