# ClassNameStyler fixes (issues 1-4)

Drop-in: copy `fxkit-core/src` over your project. Public API (`ClassNameStyler.apply(Node, String)`)
is unchanged, so FxButton / FxHr* need no edits.

Files
- main/internal/ClassNameStyler.java      REPLACED  (stateful, node-facing)
- main/internal/ClassNameParser.java      NEW       (pure parsing, no JavaFX)
- main/internal/ClassNameVocabulary.java  NEW       (valid colors/classes read from tokens.css + utilities.css)
- test/internal/ClassNameParserTest.java  NEW
- test/internal/ClassNameStylerTest.java  NEW

pom.xml: add test dependencies (versions to match your parent's dependencyManagement)
  org.junit.jupiter:junit-jupiter  (scope test)
  maven-surefire-plugin >= 3.0 in the parent build

Behavior changes to be aware of
- Tokens not defined in utilities.css (e.g. `shadow`, `border-b`, `text-primary-600`) now log a
  warning and are ignored. Before, some became a guessed class or an undefined looked-up color.
- Gradient stops are ordered from, via..., to regardless of the order written.
- In one scope a gradient now wins over a plain bg-* color (as in Tailwind).
