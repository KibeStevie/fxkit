package dev.fxkit.core.components.code;

import java.util.Collection;
import java.util.List;

/**
 * Style class names for a code editor, so an app does not have to write CSS for syntax colouring.
 *
 * <p>
 * FXKit does not depend on an editor library. Use these with one that draws text as JavaFX {@code Text}
 * nodes and accepts style classes, such as RichTextFX's {@code CodeArea}:
 *
 * <pre>{@code
 * CodeArea area = new CodeArea();
 * area.getStyleClass().add(FxCode.EDITOR);                       // surface, mono font, caret, selection, gutter
 * area.setParagraphGraphicFactory(LineNumberFactory.get(area));  // gutter is styled by FxCode.EDITOR too
 *
 * builder.add(FxCode.classes(FxCode.KEYWORD), 6);                // a coloured span
 * area.setStyle(from, to, FxCode.classes(FxCode.BRACKET_MATCH)); // mark a bracket
 * }</pre>
 *
 * The colours are the {@code -fxk-code-*} tokens in {@code tokens.css} (light and dark), and the rules
 * are the "Code editor" block of {@code components.css}. The current line is highlighted by the editor
 * itself (the paragraph that holds the caret).
 */
public final class FxCode {

    /** Style class for the editor node itself. */
    public static final String EDITOR = "fxk-code";

    public static final String KEYWORD = "fxk-code-keyword";
    public static final String TYPE = "fxk-code-type";
    public static final String FUNCTION = "fxk-code-function";
    public static final String STRING = "fxk-code-string";
    public static final String NUMBER = "fxk-code-number";
    /** A bind parameter such as {@code $1}; coloured like a number. */
    public static final String PARAM = "fxk-code-param";
    /** A quoted identifier or other name worth marking. */
    public static final String IDENT = "fxk-code-ident";
    public static final String COMMENT = "fxk-code-comment";

    /** The bracket next to the caret and its partner. */
    public static final String BRACKET_MATCH = "fxk-code-bracket-match";
    /** A bracket that has no partner. */
    public static final String BRACKET_BAD = "fxk-code-bracket-bad";

    private FxCode() {
    }

    /** One style class as the {@code Collection<String>} that RichTextFX style spans take. */
    public static Collection<String> classes(String styleClass) {
        return List.of(styleClass);
    }
}
