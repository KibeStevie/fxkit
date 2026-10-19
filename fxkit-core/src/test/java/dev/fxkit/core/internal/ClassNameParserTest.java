package dev.fxkit.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.fxkit.core.internal.ClassNameParser.Declaration;
import dev.fxkit.core.internal.ClassNameParser.Scope;
import org.junit.jupiter.api.Test;

/** Pure-logic tests: no JavaFX toolkit needed. Uses the vocabulary read from the shipped stylesheets. */
class ClassNameParserTest {

    private final ClassNameVocabulary vocab = ClassNameVocabulary.shipped();
    private final List<String> warnings = new ArrayList<>();

    private ClassNameParser.Result parse(String className) {
        return ClassNameParser.parse(className, vocab, warnings::add);
    }

    private static List<Declaration> base(ClassNameParser.Result r) {
        return r.declarations().getOrDefault(Scope.NONE, List.of());
    }

    private static Set<String> baseClasses(ClassNameParser.Result r) {
        return r.classes().getOrDefault(Scope.NONE, Set.of());
    }

    // ---- issue 1: no guessed colors ---------------------------------------------------------

    @Test
    void unknownTextTokenIsNotMistakenForAColor() {
        // there is no .text-center in utilities.css; it must warn, not become "-fxk-center"
        var r = parse("text-center border-solid bg-cover");
        assertTrue(r.isEmpty());
        assertEquals(3, warnings.size());
    }

    @Test
    void semanticClassWithoutMatchingTokenFallsBackToTheClass() {
        // "-fxk-muted" does not exist (the token is -fxk-text-muted), but .text-muted does
        var r = parse("text-muted");
        assertEquals(List.of(), base(r));
        assertEquals(Set.of("text-muted"), baseClasses(r));
    }

    @Test
    void typoedColorIsRejectedWithAWarning() {
        var r = parse("bg-bleu-500 bg-bleu");
        assertTrue(r.isEmpty());
        assertEquals(2, warnings.size());
    }

    @Test
    void knownColorsBecomeInlineDeclarations() {
        var r = parse("bg-primary text-blue-500 border-gray-200 bg-transparent");
        assertTrue(base(r).contains(new Declaration("-fx-text-fill", "-fxk-blue-500")));
        assertTrue(base(r).contains(new Declaration("-fx-fill", "-fxk-blue-500")));
        assertTrue(base(r).contains(new Declaration("-fx-border-color", "-fxk-gray-200")));
        assertTrue(base(r).contains(new Declaration("-fx-background-color", "transparent")));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void sizesAndSpacingComeFromUtilitiesCss() {
        var r = parse("text-lg p-4 rounded-lg border-2 font-bold");
        assertEquals(Set.of("text-lg", "p-4", "rounded-lg", "border-2", "font-bold"), baseClasses(r));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void shadowTokensAreNotColors() {
        var r = parse("bg-shadow-md");
        assertTrue(r.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void unknownScopePrefixWarns() {
        var r = parse("dark:bg-gray-900");
        assertTrue(r.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void ringTokensAreSilentNoOps() {
        var r = parse("ring-4 ring-blue-300 ring-offset-2");
        assertTrue(r.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    // ---- issue 2: gradients -------------------------------------------------------------------

    @Test
    void namedGradientClassThatDoesNotExistWarnsInsteadOfBecomingAColor() {
        var r = parse("bg-gradient-blue");
        assertTrue(r.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void gradientStopsAreOrderedFromViaToRegardlessOfWrittenOrder() {
        var r = parse("bg-gradient-to-r to-pink-500 via-purple-500 from-blue-500");
        String value = base(r).get(base(r).size() - 1).value();
        assertEquals("linear-gradient(from 0% 0% to 100% 0%, -fxk-blue-500 0%, -fxk-purple-500 50%, -fxk-pink-500 100%)",
                value);
    }

    @Test
    void gradientWinsOverPlainBackgroundInTheSameScope() {
        var r = parse("bg-gradient-to-r from-blue-500 to-pink-500 bg-red-500");
        List<Declaration> decls = base(r);
        assertEquals("-fx-background-color", decls.get(decls.size() - 1).property());
        assertTrue(decls.get(decls.size() - 1).value().startsWith("linear-gradient("));
    }

    @Test
    void hoverCanChangeOnlyTheDirectionAndReuseTheGlobalStops() {
        var r = parse("bg-gradient-to-r from-blue-500 to-pink-500 hover:bg-gradient-to-br");
        assertEquals(1, r.declarations().get(Scope.HOVER).size());
        assertTrue(r.declarations().get(Scope.HOVER).get(0).value().contains("-fxk-blue-500 0%, -fxk-pink-500 100%"));
    }

    @Test
    void gradientWithoutTwoStopsWarns() {
        var r = parse("bg-gradient-to-r from-blue-500");
        assertTrue(r.isEmpty());
        assertEquals(1, warnings.size());
    }

    // ---- vocabulary ---------------------------------------------------------------------------

    @Test
    void selectorScannerIgnoresDeclarationsAndComments() {
        Set<String> found = ClassNameVocabulary.selectorsOutsideBlocks(
                "/* .not-a-class */ .a-b { -fx-padding: 0.5em; } .c:hover, .d { x: y; }");
        assertEquals(Set.of("a-b", "c", "d"), found);
        assertFalse(found.contains("5em"));
    }
}
