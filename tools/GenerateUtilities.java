import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates fxkit-core's utilities.css from two inputs:
 * <ul>
 *   <li>{@code tokens.css}: color scales, semantic colors and shadow tokens are discovered
 *       automatically, so adding a new scale (e.g. "purple") there is enough;</li>
 *   <li>{@code tools/scales.txt}: spacing, radius, font-size and font-weight scales.</li>
 * </ul>
 *
 * <p>Run from the repository root (Java's single-file launcher, no build step):
 * <pre>
 *   java tools/GenerateUtilities.java            # (re)write utilities.css
 *   java tools/GenerateUtilities.java --check    # exit 1 if utilities.css is out of date
 * </pre>
 *
 * <p>Rule order matters in JavaFX CSS: when two single-class rules set the same property,
 * the one written LATER wins. That is why border widths come before border colors, and
 * why every family here is emitted in one fixed order.
 */
public class GenerateUtilities {

    static final Path TOKENS = Path.of("fxkit-core/src/main/resources/dev/fxkit/core/tokens.css");
    static final Path SCALES = Path.of("tools/scales.txt");
    static final Path OUTPUT = Path.of("fxkit-core/src/main/resources/dev/fxkit/core/utilities.css");

    /** Class suffix -> semantic token (without the -fxk- prefix). */
    static final Map<String, String> TEXT_SEMANTIC = new LinkedHashMap<>();
    static final Map<String, String> BORDER_SEMANTIC = new LinkedHashMap<>();
    static {
        TEXT_SEMANTIC.put("body", "text");
        TEXT_SEMANTIC.put("muted", "text-muted");
        for (String t : new String[] {"primary", "danger", "success", "warning",
                "on-primary", "on-danger", "on-success", "on-warning"}) {
            TEXT_SEMANTIC.put(t, t);
        }
        BORDER_SEMANTIC.put("default", "border");
        BORDER_SEMANTIC.put("strong", "border-strong");
        BORDER_SEMANTIC.put("focus", "focus-ring");
        for (String t : new String[] {"primary", "danger", "success", "warning"}) {
            BORDER_SEMANTIC.put(t, t);
        }
    }

    // ---- data read from the inputs ------------------------------------------------------
    /** scale name -> steps in file order, e.g. blue -> [50, 100, ...]. */
    static final Map<String, List<String>> colorScales = new LinkedHashMap<>();
    /** raw colors without steps: white, black. */
    static final List<String> baseColors = new ArrayList<>();
    /** semantic color tokens (name without prefix), e.g. primary, surface-alt. */
    static final List<String> semanticColors = new ArrayList<>();
    /** shadow sizes, e.g. sm, md, lg. */
    static final List<String> shadows = new ArrayList<>();
    /** family -> (name -> value). */
    static final Map<String, Map<String, String>> scales = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        boolean check = args.length > 0 && args[0].equals("--check");
        if (!Files.exists(TOKENS)) {
            fail("Cannot find " + TOKENS + ". Run this from the repository root.");
        }
        readTokens(Files.readString(TOKENS));
        readScales(Files.readAllLines(SCALES));
        requireTokens();

        String css = generate();
        if (check) {
            String current = Files.exists(OUTPUT) ? Files.readString(OUTPUT).replace("\r\n", "\n") : "";
            if (!current.equals(css)) {
                fail(OUTPUT + " is out of date. Run: java tools/GenerateUtilities.java");
            }
            System.out.println("utilities.css is up to date.");
        } else {
            Files.writeString(OUTPUT, css);
            System.out.println("Wrote " + OUTPUT + " (" + css.lines().filter(l -> l.startsWith(".")).count() + " rules)");
        }
    }

    // ---- reading ------------------------------------------------------------------------
    static void readTokens(String css) {
        css = css.replaceAll("(?s)/\\*.*?\\*/", "");                    // drop comments
        // First ".root { ... }" only. ".root.dark" has no whitespace after ".root", so it never matches.
        Matcher root = Pattern.compile("\\.root\\s*\\{([^}]*)\\}").matcher(css);
        if (!root.find()) {
            fail("No .root block found in " + TOKENS);
        }
        Matcher decl = Pattern.compile("-fxk-([a-z0-9-]+)\\s*:\\s*([^;]+);").matcher(root.group(1));
        Pattern scaleName = Pattern.compile("([a-z]+)-(\\d+)");
        while (decl.find()) {
            String name = decl.group(1);
            String value = decl.group(2).trim();
            Matcher scale = scaleName.matcher(name);
            if (scale.matches()) {
                colorScales.computeIfAbsent(scale.group(1), k -> new ArrayList<>()).add(scale.group(2));
            } else if (name.startsWith("shadow-")) {
                shadows.add(name.substring("shadow-".length()));
            } else if (value.startsWith("#")) {
                baseColors.add(name);                                   // white, black
            } else if (value.startsWith("-fxk-")) {
                semanticColors.add(name);                               // primary, surface, ...
            }
        }
    }

    static void readScales(List<String> lines) {
        for (String line : lines) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split("\\s+");
            if (p.length != 3) {
                fail("Bad line in " + SCALES + ": " + line);
            }
            scales.computeIfAbsent(p[0], k -> new LinkedHashMap<>()).put(p[1], p[2]);
        }
        for (String family : new String[] {"space", "radius", "font", "weight"}) {
            if (!scales.containsKey(family)) {
                fail("Family '" + family + "' missing from " + SCALES);
            }
        }
    }

    /** The class maps above must only point at tokens that exist: catches drift early. */
    static void requireTokens() {
        List<String> missing = new ArrayList<>();
        for (String t : TEXT_SEMANTIC.values()) {
            if (!semanticColors.contains(t)) missing.add(t);
        }
        for (String t : BORDER_SEMANTIC.values()) {
            if (!semanticColors.contains(t)) missing.add(t);
        }
        if (!missing.isEmpty()) {
            fail("Semantic tokens missing from tokens.css: " + missing);
        }
    }

    // ---- generating ---------------------------------------------------------------------
    static String generate() {
        StringBuilder o = new StringBuilder();
        o.append("/* =============================================================================\n");
        o.append(" * FXKit utility classes\n");
        o.append(" *\n");
        o.append(" * GENERATED FILE. Do not edit by hand: your changes will be overwritten.\n");
        o.append(" * Sources:    tokens.css (colors, shadows) and tools/scales.txt (spacing, radius, type)\n");
        o.append(" * Regenerate: java tools/GenerateUtilities.java     (from the repository root)\n");
        o.append(" * Reference:  docs/utility-classes.md\n");
        o.append(" *\n");
        o.append(" * Palette classes (.bg-blue-500) use raw scale tokens and look the same in both themes.\n");
        o.append(" * Semantic classes (.bg-surface) use semantic tokens and follow the light/dark theme.\n");
        o.append(" * ========================================================================== */\n");

        // -- background ----------------------------------------------------------------
        section(o, "Background color");
        o.append(".bg-transparent { -fx-background-color: transparent; }\n");
        for (String c : baseColors) bg(o, c, c);
        for (var e : colorScales.entrySet()) {
            for (String step : e.getValue()) bg(o, e.getKey() + "-" + step, e.getKey() + "-" + step);
        }
        section(o, "Background color (semantic, follows the theme)");
        for (String t : semanticColors) bg(o, t, t);

        // -- text ------------------------------------------------------------------------
        // -fx-text-fill colors Labels and controls; -fx-fill colors javafx.scene.text.Text nodes.
        section(o, "Text color");
        for (String c : baseColors) text(o, c, c);
        for (var e : colorScales.entrySet()) {
            for (String step : e.getValue()) text(o, e.getKey() + "-" + step, e.getKey() + "-" + step);
        }
        section(o, "Text color (semantic, follows the theme)");
        TEXT_SEMANTIC.forEach((cls, token) -> text(o, cls, token));

        // -- border: widths FIRST, so the color classes below can override the default color ----
        section(o, "Border width (also sets the default border color)");
        o.append(".border-0 { -fx-border-width: 0px; }\n");
        o.append(".border { -fx-border-width: 1px; -fx-border-color: -fxk-border; }\n");
        o.append(".border-2 { -fx-border-width: 2px; -fx-border-color: -fxk-border; }\n");
        o.append(".border-4 { -fx-border-width: 4px; -fx-border-color: -fxk-border; }\n");
        section(o, "Border color");
        o.append(".border-transparent { -fx-border-color: transparent; }\n");
        for (String c : baseColors) border(o, c, c);
        for (var e : colorScales.entrySet()) {
            for (String step : e.getValue()) border(o, e.getKey() + "-" + step, e.getKey() + "-" + step);
        }
        section(o, "Border color (semantic, follows the theme)");
        BORDER_SEMANTIC.forEach((cls, token) -> border(o, cls, token));

        // -- radius (background AND border, or a rounded border leaves square background corners) --
        section(o, "Border radius");
        scales.get("radius").forEach((name, px) -> {
            String cls = name.equals("base") ? "rounded" : "rounded-" + name;
            o.append('.').append(cls).append(" { -fx-background-radius: ").append(px)
                    .append("px; -fx-border-radius: ").append(px).append("px; }\n");
        });

        // -- padding: JavaFX has ONE -fx-padding property, so only ONE padding class per node applies --
        section(o, "Padding (one padding class per node: they all set the same -fx-padding property)");
        scales.get("space").forEach((name, px) -> {
            String v = px + "px";
            pad(o, "p-" + name, v + " " + v + " " + v + " " + v);
            pad(o, "px-" + name, "0 " + v + " 0 " + v);
            pad(o, "py-" + name, v + " 0 " + v + " 0");
            pad(o, "pt-" + name, v + " 0 0 0");
            pad(o, "pr-" + name, "0 " + v + " 0 0");
            pad(o, "pb-" + name, "0 0 " + v + " 0");
            pad(o, "pl-" + name, "0 0 0 " + v);
        });

        // -- gap: -fx-spacing (HBox, VBox), -fx-hgap/-fx-vgap (FlowPane, GridPane, TilePane). See ADR-001. --
        section(o, "Gap (ADR-001): spacing for HBox/VBox, hgap/vgap for FlowPane/GridPane/TilePane");
        scales.get("space").forEach((name, px) -> o.append(".gap-").append(name)
                .append(" { -fx-spacing: ").append(px).append("px; -fx-hgap: ").append(px)
                .append("px; -fx-vgap: ").append(px).append("px; }\n"));
        scales.get("space").forEach((name, px) -> o.append(".gap-x-").append(name)
                .append(" { -fx-hgap: ").append(px).append("px; }\n"));
        scales.get("space").forEach((name, px) -> o.append(".gap-y-").append(name)
                .append(" { -fx-vgap: ").append(px).append("px; }\n"));

        // -- typography -------------------------------------------------------------------
        section(o, "Font size");
        scales.get("font").forEach((name, px) -> o.append(".text-").append(name)
                .append(" { -fx-font-size: ").append(px).append("px; }\n"));
        section(o, "Font weight");
        scales.get("weight").forEach((name, v) -> o.append(".font-").append(name)
                .append(" { -fx-font-weight: ").append(v).append("; }\n"));

        // -- shadow -----------------------------------------------------------------------
        section(o, "Shadow (elevation tokens)");
        o.append(".shadow-none { -fx-effect: null; }\n");
        for (String s : shadows) {
            o.append(".shadow-").append(s).append(" { -fx-effect: -fxk-shadow-").append(s).append("; }\n");
        }
        return o.toString();
    }

    static void section(StringBuilder o, String title) {
        o.append("\n/* ---- ").append(title).append(" ---- */\n");
    }

    static void bg(StringBuilder o, String cls, String token) {
        o.append(".bg-").append(cls).append(" { -fx-background-color: -fxk-").append(token).append("; }\n");
    }

    static void text(StringBuilder o, String cls, String token) {
        o.append(".text-").append(cls).append(" { -fx-text-fill: -fxk-").append(token)
                .append("; -fx-fill: -fxk-").append(token).append("; }\n");
    }

    static void border(StringBuilder o, String cls, String token) {
        o.append(".border-").append(cls).append(" { -fx-border-color: -fxk-").append(token).append("; }\n");
    }

    static void pad(StringBuilder o, String cls, String value) {
        o.append('.').append(cls).append(" { -fx-padding: ").append(value).append("; }\n");
    }

    static void fail(String message) {
        System.err.println("Error: " + message);
        System.exit(1);
    }
}
