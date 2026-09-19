# Sprint 0 Build Guide: Stories 5 to 9

Covers these stories:

| # | Story | Points |
|---|---|---|
| 5 | Create the parent POM | 3 |
| 6 | Create the `fxkit-core` and `fxkit-showcase` modules | 3 |
| 7 | Add JavaFX dependencies and the `javafx-maven-plugin` | 3 |
| 8 | Hello JavaFX: minimal Application opens a window | 2 |
| 9 | Add `module-info.java` to both modules | 3 |

---

## 1. Add the files to your repo

Unzip `fxkit-skeleton.zip` **into the root of your cloned repo** (for example `~/dev/fxkit`). It only adds new files, so your `README.md`, `LICENSE`, `.gitignore` and `docs/` are untouched.

```
fxkit/                                   ← repo root
├── pom.xml                              ← Story 5: parent POM
├── run.sh                               ← build + run helper
├── fxkit-core/                          ← Story 6: the library
│   ├── pom.xml                          ← Story 7: JavaFX dependencies
│   └── src/main/java/
│       ├── module-info.java             ← Story 9
│       └── dev/fxkit/core/FxKit.java
└── fxkit-showcase/                      ← Story 6: the demo app
    ├── pom.xml                          ← Story 7: javafx-maven-plugin
    └── src/main/java/
        ├── module-info.java             ← Story 9
        └── dev/fxkit/showcase/ShowcaseApp.java   ← Story 8
```

Open the **repo root folder** in VS Code (not a sub-folder), so it recognizes the multi-module project.

## 2. Build and run

From the repo root, in Git Bash:

```bash
mvn clean install
```

You should end with a summary like this:

```
[INFO] Reactor Summary for FXKit 0.1.0-SNAPSHOT:
[INFO] FXKit .............................................. SUCCESS
[INFO] FXKit Core ......................................... SUCCESS
[INFO] FXKit Showcase ..................................... SUCCESS
[INFO] BUILD SUCCESS
```

Then start the app:

```bash
./run.sh
```

which is shorthand for:

```bash
mvn -q -DskipTests install
mvn -q -pl fxkit-showcase javafx:run
```

**Expected result:** a 640×400 window titled **FXKit Showcase** showing *"Hello, FXKit!"* and *"Core library version 0.1.0-SNAPSHOT"*. The second line comes from a class in `fxkit-core`, which proves the two modules are wired together.

> **Why `install` before `javafx:run`?** With Java modules, the showcase needs the *built* `fxkit-core` jar. `install` puts it in your local Maven repo (`~/.m2`). Whenever you change `fxkit-core`, re-run `install` first. That's what `run.sh` does.

---

## 3. What each story delivers (and what to learn from it)

### Story 5: Parent POM (`pom.xml`)

| Acceptance criterion | Where |
|---|---|
| `<packaging>pom</packaging>` and lists the modules | `<packaging>` and `<modules>` |
| Java 25, JavaFX 25.x, UTF-8 | `<properties>`: `maven.compiler.release=25`, `javafx.version=25.0.1` |
| JavaFX versions managed in one place | `<dependencyManagement>` |
| `mvn validate` passes | run it from the root |

**Concepts**
- **Parent/aggregator:** one POM that lists modules and holds shared settings. It produces no jar.
- **`dependencyManagement` vs `dependencies`:** management says *which version to use if a module asks for it*. It adds nothing by itself. The modules then list dependencies **without** versions.
- **`pluginManagement`:** the same idea for plugins.
- **`maven.compiler.release=25`:** tells `javac` to compile for Java 25 and use Java 25's API. It fails fast if the JDK is older.

### Story 6: The two modules

- `fxkit-core` is the **library**. Other developers will add this to their projects.
- `fxkit-showcase` is the **demo app**. It depends on `fxkit-core` the way an outsider would.
- The dependency direction is one-way: **showcase → core**, never the reverse.

Both use Maven's standard layout (`src/main/java`, later `src/main/resources` and `src/test/java`).

### Story 7: JavaFX dependencies and the plugin

- `javafx-controls` and `javafx-fxml` are declared in the modules. **Versions are inherited** from the parent.
- The JavaFX Maven artifacts pick the right native libraries for your OS (Windows, macOS, Linux) automatically.
- `javafx-maven-plugin` (version `0.0.8`) is configured in the showcase with:

```xml
<mainClass>dev.fxkit.showcase/dev.fxkit.showcase.ShowcaseApp</mainClass>
```

The format for a modular app is `moduleName/fully.qualified.MainClass`.

### Story 8: Hello JavaFX (`ShowcaseApp.java`)

The JavaFX lifecycle:

```
main() → launch() → JavaFX starts its UI thread → start(Stage)
                                                    └─ Stage (window)
                                                        └─ Scene
                                                            └─ VBox (layout)
                                                                ├─ Label "Hello, FXKit!"
                                                                └─ Label "Core library version …"
```

The inline `setStyle(...)` is temporary. From Phase 1 it will be replaced by FXKit's CSS tokens and utility classes.

### Story 9: `module-info.java`

| Keyword | Meaning | Where we used it |
|---|---|---|
| `requires` | "I need this module" | Core requires `javafx.controls`, `javafx.fxml`. Showcase requires `dev.fxkit.core`. |
| `requires transitive` | "Anyone who requires me also gets this" | Core → `javafx.controls`, because our components will extend JavaFX classes |
| `exports` | "Other modules may use the public classes in this package" | Core exports `dev.fxkit.core` |
| `exports … to X` | Same, but only for module X | Showcase exports its package only to `javafx.graphics`, whose launcher instantiates `ShowcaseApp` by reflection |
| `opens … to X` | Allows *reflection* (private members) for X | Showcase opens its package to `javafx.fxml`, so FXML controllers work |

**A note on the `opens` line that is commented out in core:** in Phase 2 you'll create `dev.fxkit.core.components`. At that point uncomment `opens dev.fxkit.core.components to javafx.fxml;` so FXML can create `FxButton` and `FxCard` by reflection. Opening a package that doesn't exist yet only produces a warning, so it's commented out for now.

---

## 4. VS Code tips

- Open the **repo root** folder. Wait for the Java extension to finish importing the Maven project (the status bar shows progress).
- Make sure VS Code uses JDK 25 (`Java: Configure Java Runtime` or the `java.configuration.runtimes` setting).
- If you see phantom red errors after adding files: Command Palette → **Java: Clean Java Language Server Workspace** → Reload.
- **Run from VS Code the reliable way:** the *Maven* side panel → `fxkit-showcase` → *Plugins* → `javafx` → `javafx:run` (after running `install` once).
- Pressing the ▶ **Run** button above `main` should also work, because the project is modular. If it reports *"JavaFX runtime components are missing"*, use the Maven route above.

---

## 5. Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| `release version 25 not supported` | Maven is using an older JDK. Run `mvn -v` and check the "Java version" line. Fix `JAVA_HOME` and restart Git Bash. |
| `Could not find artifact org.openjfx:javafx-controls:jar:25.0.1` | Check your internet connection or proxy. If a newer patch (25.0.2, …) is what you want, change `javafx.version` in the parent POM. |
| `package dev.fxkit.core is not visible` / `module dev.fxkit.core not found` | Core hasn't been installed. Run `mvn clean install` from the root, then retry. |
| `No plugin found for prefix 'javafx'` | Run it as `mvn -pl fxkit-showcase javafx:run` from the root, or from inside `fxkit-showcase/`. |
| `Module javafx.controls not found` | Maven didn't import JavaFX yet. Run `mvn clean install`, then reload the project in VS Code. |
| `Error: JavaFX runtime components are missing` | The app was started on the classpath (not the module path). Start it through `javafx:run`. |
| `Unsupported class file major version 69` from a plugin | An old plugin can't read Java 25 classes. The compiler plugin is pinned to 3.14.1 for this reason; check that the parent POM was not modified. |
| Random file-lock errors while building | Keep the repo **outside OneDrive** (e.g. `~/dev/fxkit`). |

---

## 6. Finish the stories on your board

Check each acceptance criterion in the issue, then commit. Suggested flow (one branch for the whole skeleton is fine in Sprint 0):

```bash
git checkout -b feature/maven-skeleton
git add pom.xml run.sh fxkit-core fxkit-showcase
git commit -m "build: add Maven multi-module skeleton with Hello JavaFX and module-info"
git push -u origin feature/maven-skeleton
```

Then open a pull request. In its description, close the stories (check the issue numbers on your board; they are probably #5 to #9):

```
Closes #5, closes #6, closes #7, closes #8, closes #9
```

The PR template's Definition of Done checklist applies. For this sprint, the important items are: `mvn clean verify` passes and acceptance criteria are met. Merging closes the issues and moves them to **Done**.

---

## 7. Check your understanding

Try answering these in your own words (write them in your Sprint 0 retrospective under *"What did I learn?"*):

1. What is the difference between `<dependencyManagement>` and `<dependencies>`?
2. Why does `fxkit-core` use `requires transitive javafx.controls`?
3. What would go wrong if `ShowcaseApp`'s package were not exported to `javafx.graphics`?
4. Why do we run `mvn install` before `javafx:run`?

---

## 8. What was verified

I checked that the three POMs are well-formed XML, that the Java sources and both `module-info.java` files compile together without warnings (against stub versions of the JavaFX modules), and that the versions used exist on Maven Central: `javafx-controls` 25.0.1, `javafx-maven-plugin` 0.0.8, and `maven-compiler-plugin` 3.14.1.

I could **not** run a real Maven + JavaFX 25 build here, so your first `mvn clean install` is the real test. If anything fails, send me the error output.

---

## Next up

With Story 4 done and Stories 5 to 9 finished, Sprint 0 has two stories left: **10** (project board setup, finished in the GitHub UI) and **11** (write the initial Product Backlog for Phases 1 and 2). After that, Sprint 1 starts: design tokens and the first utility classes.
