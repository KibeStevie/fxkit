package dev.fxkit.showcase;

import java.util.function.Supplier;
import javafx.scene.Node;

/**
 * One entry in the showcase's navigation shell (#39): a name shown in the sidebar, plus how to build
 * that page's content.
 *
 * <p>{@code content} is a {@link Supplier} rather than a built {@link Node} so that {@link ShowcaseShell}
 * can build each page lazily - only the page selected on launch is built up front, and every other page
 * is built the first time it's actually selected (see #39's task: "pages are only built when first
 * shown").
 */
record ShowcasePage(String name, Supplier<Node> content) {
}
