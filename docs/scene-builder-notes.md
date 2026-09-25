# Spike: Load FXKit in Scene Builder (#34)

**Time-box:** 1 day. **Status: partially answered.** This is desk research against Scene Builder's own
documentation and known JPMS/library-JAR mechanics, done from an environment with no display and no way
to install or run a GUI application - so nothing below has actually been clicked through in Scene
Builder itself. Treat every "should" as a prediction to verify, not a confirmed result, and see
"Follow-up" at the end for the hands-on pass this still needs.

## Which Scene Builder

Oracle's original Scene Builder is no longer the actively maintained one. The current tool is
**Gluon Scene Builder** (open source, BSD license, <https://github.com/gluonhq/scenebuilder>), which is
what these notes are about. It bundles its own JavaFX runtime, so a plain FxButton/FxCard demo doesn't
need the person running Scene Builder to have JavaFX installed separately.

## How custom controls get into Scene Builder

Since version 8.2.0, Scene Builder has a **Library Manager**: point it at a JAR (or a folder of them),
and it scans every `.class` file for candidates. To be picked up, a class needs (per Scene Builder's own
docs):
- A **public no-arg constructor** - the Library Manager instantiates a real instance to generate the
  Library's preview thumbnail and Inspector default values.
- A package name that isn't filtered out (Scene Builder excludes JDK/JavaFX/its-own-internal package
  prefixes so it doesn't try to list `java.*`/`javafx.*`/etc. as "custom controls").

Both `FxButton` and `FxCard` already satisfy the constructor requirement (#27, #31) - that was table
stakes for FXML loading anyway (`docs/fxml-notes.md`), not something specific to Scene Builder. Their
package, `dev.fxkit.core.components`, isn't a reserved prefix, so it should pass the filter.

## The JPMS question this spike was actually asked to answer

**Does Scene Builder care that `fxkit-core` is a JPMS module?** As far as the documentation shows: no,
not in the way that would block loading it. The Library Manager scans a JAR's classes directly; it
doesn't launch your application with `--module-path` or otherwise resolve your module graph. A modular
JAR (one with a `module-info.class`) and a plain classpath JAR look the same to its scanner - it just
needs `dev.fxkit.core-<version>.jar`, built with `mvn package` from the `fxkit-core` module specifically
(not the parent reactor POM, which has no jar of its own).

**What likely does matter, and is a real risk given #36 in the same sprint:** `fxkit-core` now has real
runtime dependencies beyond JavaFX itself - `ikonli-core` and `ikonli-javafx` (#36, stretch). Scene
Builder's Library Manager loads only the JAR(s) you give it; it does **not** resolve Maven dependencies
for you. If `FxButton` is imported into Scene Builder without also handing it `ikonli-core-12.4.0.jar`
and `ikonli-javafx-12.4.0.jar` (same versions as `fxkit-core`'s own pom.xml), instantiating `FxButton`
for the Library preview will fail with a `NoClassDefFoundError` for `org.kordamp.ikonli.Ikon` - even
though nobody's using the `icon` property in that particular design. This is a real, easily-hit
limitation, not a hypothetical one, and it gets worse with every dependency a future component adds.

## Expected result (unverified)

- Importing `fxkit-core-0.1.0-SNAPSHOT.jar` plus `ikonli-core` and `ikonli-javafx` alongside it into the
  Library Manager should surface `FxButton` and `FxCard` in a "Custom" library section.
- `variant`/`size`/`elevation` are plain JavaFX enum properties, so the Inspector should show them as
  dropdowns without any extra work on FXKit's side - Scene Builder's Inspector generates its property
  sheet from bean introspection, the same mechanism `FXMLLoader` itself uses (`docs/fxml-notes.md`).
- `FxCard`'s header/body/footer slots are a bigger unknown: Scene Builder's drag-and-drop model is built
  around a component's *children* far more than around arbitrary object properties. Whether it offers any
  drag target for `header`/`body`/`footer` specifically (versus only working if you hand-edit the FXML
  and then reopen it) is genuinely unverified here and is the single biggest open question from this
  spike.

## Known limitations (documented, not all independently confirmed)

- **Dependencies aren't resolved for you** (above) - every runtime dependency `fxkit-core` picks up needs
  its own JAR added to the Library Manager, kept in version lockstep with the pom.xml by hand.
- **FXML-only content, no fx:controller logic runs in the canvas** - normal for any FXML tool, not FXKit-
  specific, but worth remembering when judging what "loads correctly" means here.
- **`FxCard`'s slot properties vs. Scene Builder's children-first model** - see above; needs the hands-on
  pass to actually know.

## Follow-up

Scene Builder support is a known risk (per the story's own framing), and a full integration guide is
already planned for Phase 7 - this spike doesn't try to replace that. Concretely still needed, and out of
this spike's time-box:
1. An actual hands-on session: install Gluon Scene Builder, `mvn package` fxkit-core, import the jar (plus
   `ikonli-core`/`ikonli-javafx`) via the Library Manager, and record what really happens for `FxButton`
   and `FxCard` - especially the header/body/footer question above.
2. If slots don't drag-and-drop cleanly: either document the FXML-hand-edit workaround, or open a
   follow-up story to investigate further.
3. Once dependencies are confirmed as the practical blocker they look like on paper: consider whether
   `fxkit-core` should publish a small "Scene-Builder-ready" fat JAR (bundling `ikonli-core`/
   `ikonli-javafx`) alongside the normal thin JAR, purely for Library Manager convenience - a decision for
   Phase 7's integration guide, not this spike.
