#!/usr/bin/env bash
# =============================================================================
# FXKit: GitHub setup script
#
# Creates (safe to re-run; existing items are skipped where possible):
#   1. The GitHub repository (README, MIT license, Java .gitignore)
#   2. Labels (story / spike / task / bug / epic:*)
#   3. Milestones (one per phase, Phase 0 to Phase 8)
#   4. A GitHub Project (v2) board, linked to the repo, with a "Points" field
#   5. The 11 Phase 0 stories as issues, added to the board with their points
#   6. (Optional) issue templates + PR template + Maven ignores, pushed to main
#
# Prerequisites:
#   - GitHub CLI:  https://cli.github.com   (gh --version)
#   - jq:          https://jqlang.github.io/jq
#   - gh auth login
#   - gh auth refresh -s project        <- needed for Projects (v2)
#
# Usage:
#   chmod +x setup-github.sh
#   ./setup-github.sh
# =============================================================================
set -euo pipefail

# ------------------------------ CONFIG ---------------------------------------
REPO_NAME="fxkit"
REPO_DESC="Tailwind-inspired UI component library for JavaFX desktop apps"
VISIBILITY="public"                 # public | private
LICENSE_KEY="mit"                   # mit | apache-2.0 | ...
PROJECT_TITLE="FXKit Roadmap"
CLONE_AND_SCAFFOLD="yes"            # yes | no  (clone repo + add templates)
# -----------------------------------------------------------------------------

say()  { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
info() { printf '    %s\n' "$*"; }
die()  { printf '\n\033[31mError:\033[0m %s\n' "$*" >&2; exit 1; }

# ------------------------------ PRE-FLIGHT -----------------------------------
command -v gh >/dev/null 2>&1 || die "GitHub CLI (gh) not found. Install it from https://cli.github.com"
command -v jq >/dev/null 2>&1 || die "jq not found. Install it from https://jqlang.github.io/jq"
gh auth status >/dev/null 2>&1 || die "Not logged in. Run: gh auth login"
gh project list --owner "@me" >/dev/null 2>&1 \
  || die "Missing 'project' permission. Run: gh auth refresh -s project"

OWNER="$(gh api user -q .login)"
REPO="$OWNER/$REPO_NAME"
MS_PHASE0="Phase 0: Setup"
info "Logged in as: $OWNER"
info "Target repo:  $REPO"

# ------------------------------ 1. REPOSITORY --------------------------------
say "1/6  Repository"
if gh repo view "$REPO" >/dev/null 2>&1; then
  info "Repo already exists, skipping creation."
else
  gh repo create "$REPO" "--$VISIBILITY" \
    --description "$REPO_DESC" \
    --add-readme \
    --license "$LICENSE_KEY" \
    --gitignore Java
  info "Created https://github.com/$REPO"
fi

# ------------------------------ 2. LABELS ------------------------------------
say "2/6  Labels"
label() { gh label create "$1" --color "$2" --description "$3" --repo "$REPO" --force >/dev/null && info "label: $1"; }

label "story"            "0e8a16" "A slice of user-facing value"
label "spike"            "fbca04" "Time-boxed research (output is knowledge)"
label "task"             "c5def5" "Technical task (<= 1 day)"
label "bug"              "d73a4a" "Something isn't working"
label "docs"             "0075ca" "Documentation"
label "epic:environment" "1d76db" "Epic: development environment"
label "epic:repo-build"  "5319e7" "Epic: repository and build"
label "epic:skeleton"    "b60205" "Epic: project skeleton"

# ------------------------------ 3. MILESTONES --------------------------------
say "3/6  Milestones (one per phase)"
milestone() {
  gh api -X POST "repos/$REPO/milestones" -f title="$1" -f description="$2" >/dev/null 2>&1 \
    && info "milestone: $1" || info "milestone exists (or failed): $1"
}
milestone "$MS_PHASE0"                                "Sprint 0. Environment, repo, skeleton. Exit: Hello JavaFX runs, backlog exists."
milestone "Phase 1: Design Foundation"                "Sprints 1-2. Tokens (50-900 scales), utility CSS, light/dark theme."
milestone "Phase 2: First Components"                 "Sprints 3-4. FxButton + FxCard. Milestone M1 'First Light'."
milestone "Phase 3: Showcase App"                     "Sprint 5. Demo app with a page per component. Milestone M2."
milestone "Phase 4: Inputs & Feedback"                "Sprints 6-8. Inputs, selection controls, badge/alert/progress/avatar. v0.1.0-alpha."
milestone "Phase 5: Composite Components"             "Sprints 9-13. FormField, Modal, Toast, Tabs, DataTable."
milestone "Phase 6: App Shell & Templates"            "Sprints 14-16. Sidebar, navbar, navigator, templates. v0.1.0-beta."
milestone "Phase 7: Quality, Docs, Tooling & Release" "Sprints 17-19. Tests, CI, docs, archetype, publish v0.1.0."
milestone "Phase 8: Growth & Maintenance"             "Sprint 20+. Feedback-driven improvements and upkeep."

# ------------------------------ 4. PROJECT BOARD -----------------------------
say "4/6  Project board"
PROJECT_NUMBER="$(gh project list --owner "@me" --format json \
  | jq -r --arg t "$PROJECT_TITLE" '.projects[]? | select(.title==$t) | .number' | head -n1)"

if [[ -z "$PROJECT_NUMBER" ]]; then
  PROJECT_NUMBER="$(gh project create --owner "@me" --title "$PROJECT_TITLE" --format json | jq -r .number)"
  info "Created project #$PROJECT_NUMBER"
else
  info "Project '$PROJECT_TITLE' already exists (#$PROJECT_NUMBER)."
fi

PROJECT_JSON="$(gh project view "$PROJECT_NUMBER" --owner "@me" --format json)"
PROJECT_ID="$(jq -r .id <<<"$PROJECT_JSON")"
PROJECT_URL="$(jq -r .url <<<"$PROJECT_JSON")"

gh project edit "$PROJECT_NUMBER" --owner "@me" \
  --description "Product Backlog and sprint board for FXKit (Scrum)" >/dev/null 2>&1 || true
gh project link "$PROJECT_NUMBER" --owner "@me" --repo "$REPO" >/dev/null 2>&1 \
  && info "Linked project to $REPO" || info "Project already linked (or link failed)."

get_field_id() {
  gh project field-list "$PROJECT_NUMBER" --owner "@me" --format json \
    | jq -r --arg n "$1" '.fields[]? | select(.name==$n) | .id' | head -n1
}

POINTS_FIELD_ID="$(get_field_id "Points")"
if [[ -z "$POINTS_FIELD_ID" ]]; then
  gh project field-create "$PROJECT_NUMBER" --owner "@me" --name "Points" --data-type "NUMBER" >/dev/null
  POINTS_FIELD_ID="$(get_field_id "Points")"
  info "Created field: Points"
fi

# ------------------------------ 5. PHASE 0 STORIES ---------------------------
say "5/6  Phase 0 stories -> issues -> board"
EXISTING_TITLES="$(gh issue list --repo "$REPO" --state all --limit 500 --json title -q '.[].title' || true)"

add_to_board() {
  local url="$1" points="$2" item_json item_id
  item_json="$(gh project item-add "$PROJECT_NUMBER" --owner "@me" --url "$url" --format json)"
  item_id="$(jq -r .id <<<"$item_json")"
  if [[ -n "$POINTS_FIELD_ID" && -n "$item_id" && "$item_id" != "null" ]]; then
    gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" \
      --field-id "$POINTS_FIELD_ID" --number "$points" >/dev/null
  fi
}

# story "Title" POINTS "epic-label" "Epic name"   <<'EOF' ...body... EOF
story() {
  local title="$1" points="$2" epic_label="$3" epic_name="$4" body url
  if grep -Fxq -- "$title" <<<"$EXISTING_TITLES"; then
    info "skip (exists): $title"
    return 0
  fi
  body="$(cat)"
  body+=$'\n\n---\n'"**Points:** ${points} · **Epic:** ${epic_name} · **Phase:** 0 (Sprint 0)"
  url="$(gh issue create --repo "$REPO" \
          --title "$title" --body "$body" \
          --label "story,$epic_label" --milestone "$MS_PHASE0")"
  add_to_board "$url" "$points"
  info "[$points pts] $title"
}

# ---- Epic: Environment ------------------------------------------------------
story "Install JDK 25 and verify the toolchain" 2 "epic:environment" "Environment" <<'EOF'
## User story
As a developer, I want JDK 25 and Maven installed and configured, so that I can compile and run the project.

## Acceptance criteria
- [ ] `java -version` reports version 25
- [ ] `mvn -v` shows Maven 3.9+ running on Java 25
- [ ] `JAVA_HOME` is set and `java`/`mvn` work in a fresh terminal

## Tasks
- [ ] Download and install a JDK 25 distribution
- [ ] Set `JAVA_HOME` and update `PATH`
- [ ] Install Maven 3.9+
- [ ] Verify with `java -version` and `mvn -v`
EOF

story "Configure VS Code for Java and Maven" 2 "epic:environment" "Environment" <<'EOF'
## User story
As a developer, I want VS Code set up for Java and Maven, so that I can build, run and debug from the IDE.

## Acceptance criteria
- [ ] *Extension Pack for Java* and *Maven for Java* are installed
- [ ] VS Code uses JDK 25 (`java.configuration.runtimes` if needed)
- [ ] A sample Maven project opens with no red errors
- [ ] I can run and debug a `main` class from VS Code

## Tasks
- [ ] Install *Extension Pack for Java* (includes Maven for Java)
- [ ] Install *XML* (Red Hat) for `pom.xml` and FXML editing
- [ ] Point VS Code at JDK 25
- [ ] Try run/debug on a small test class
EOF

story "Install Scene Builder" 1 "epic:environment" "Environment" <<'EOF'
## User story
As a developer, I want Scene Builder installed, so that I can design and test FXML layouts for the components.

## Acceptance criteria
- [ ] Scene Builder opens and can load an `.fxml` file

## Tasks
- [ ] Install Scene Builder (Gluon)
- [ ] Open a sample FXML file to confirm it works
EOF

# ---- Epic: Repository & build -----------------------------------------------
story "Initialize repository files (.gitignore, README, LICENSE, docs)" 2 "epic:repo-build" "Repository & build" <<'EOF'
## User story
As a maintainer, I want the repository initialized with the standard project files, so that the project is ready for collaboration.

## Acceptance criteria
- [ ] `.gitignore` covers Java, Maven (`target/`) and VS Code/JDT files
- [ ] `README.md` describes the project and links to the overview
- [ ] `LICENSE` is present (license chosen deliberately)
- [ ] `docs/` contains `PROJECT_OVERVIEW.md` and `FXKIT_DEVELOPMENT_PHASES.md`

## Tasks
- [ ] Clone the repo locally
- [ ] Review the `.gitignore` added by the setup script
- [ ] Write a real README (one-liner, status, planned stack)
- [ ] Commit the two planning documents under `docs/`
EOF

story "Create the parent POM" 3 "epic:repo-build" "Repository & build" <<'EOF'
## User story
As a developer, I want a parent POM that centralizes versions and settings, so that all modules build consistently.

## Acceptance criteria
- [ ] Root `pom.xml` has `<packaging>pom</packaging>` and lists the modules
- [ ] Properties: `maven.compiler.release=25`, `javafx.version` (25.x), UTF-8 source encoding
- [ ] JavaFX versions are managed in `<dependencyManagement>`
- [ ] `mvn validate` passes from the root

## Tasks
- [ ] Define groupId / artifactId / version (`0.1.0-SNAPSHOT`)
- [ ] Add properties and dependencyManagement
- [ ] Configure `maven-compiler-plugin` for release 25
EOF

story "Create the fxkit-core and fxkit-showcase modules" 3 "epic:repo-build" "Repository & build" <<'EOF'
## User story
As a developer, I want separate modules for the library and the demo app, so that the library stays independent of the showcase.

## Acceptance criteria
- [ ] `fxkit-core` (library) and `fxkit-showcase` (demo app) each have their own `pom.xml`
- [ ] `fxkit-showcase` depends on `fxkit-core`
- [ ] Standard Maven layout (`src/main/java`, `src/main/resources`, `src/test/java`)
- [ ] `mvn clean install` succeeds from the root

## Tasks
- [ ] Create both module directories and POMs
- [ ] Register modules in the parent POM
- [ ] Add the dependency of showcase on core
EOF

story "Add JavaFX dependencies and the javafx-maven-plugin" 3 "epic:repo-build" "Repository & build" <<'EOF'
## User story
As a developer, I want JavaFX wired into the build, so that I can run the app with one Maven command.

## Acceptance criteria
- [ ] `fxkit-core` depends on `javafx-controls` (and `javafx-fxml`) at version 25.x
- [ ] `fxkit-showcase` configures `org.openjfx:javafx-maven-plugin` with its `mainClass`
- [ ] `mvn javafx:run` (from the showcase module) launches the app

## Tasks
- [ ] Add JavaFX dependencies
- [ ] Add and configure the plugin (latest version)
- [ ] Verify `mvn javafx:run`
EOF

# ---- Epic: Skeleton ---------------------------------------------------------
story "Hello JavaFX: minimal Application opens a window" 2 "epic:skeleton" "Skeleton" <<'EOF'
## User story
As a developer, I want a minimal JavaFX app running, so that I know the whole toolchain works end to end.

## Acceptance criteria
- [ ] An `Application` subclass shows a `Stage` with a `Scene` and a `Label`
- [ ] It runs from the terminal (`mvn javafx:run`)
- [ ] It runs from VS Code

## Tasks
- [ ] Create the `Application` subclass (`start(Stage)`)
- [ ] Run via Maven
- [ ] Run from VS Code (if the run button reports missing JavaFX runtime components, run through Maven or fix the module path settings)
EOF

story "Add module-info.java to both modules" 3 "epic:skeleton" "Skeleton" <<'EOF'
## User story
As a developer, I want proper Java modules, so that the library is ready for JPMS users and FXML reflection works.

## Acceptance criteria
- [ ] `fxkit-core` has a `module-info.java` that `requires` the JavaFX modules it uses and `exports` its public packages
- [ ] Packages that FXML must instantiate are `opens` to `javafx.fxml`
- [ ] `fxkit-showcase` has a `module-info.java` that `requires` the core module
- [ ] The app still builds and runs

## Tasks
- [ ] Choose module names (e.g. `io.github.<username>.fxkit.core`)
- [ ] Write both `module-info.java` files
- [ ] Rebuild and run
EOF

story "Set up the project board, labels and sprint fields" 2 "epic:skeleton" "Skeleton" <<'EOF'
## User story
As the Product Owner, I want a project board configured for Scrum, so that I can plan and track sprints.

## Acceptance criteria
- [ ] Status columns: Backlog, Sprint Backlog, In Progress, In Review, Done
- [ ] `Points` field exists; an `Iteration` field (2 weeks) exists
- [ ] Labels and phase milestones exist
- [ ] Views: Board, Backlog (table), Current Sprint

## Tasks
- [ ] Run `setup-github.sh` (creates labels, milestones, project, Points)
- [ ] Follow `GITHUB_SETUP_GUIDE.md` to finish the UI-only parts (Status options, Iteration, views, workflows)
EOF

story "Write the initial Product Backlog" 3 "epic:skeleton" "Skeleton" <<'EOF'
## User story
As the Product Owner, I want the first phases captured as backlog items, so that Sprint 1 planning can start immediately.

## Acceptance criteria
- [ ] Phase 1 (Sprints 1-2) stories exist as issues with points and the Phase 1 milestone
- [ ] Phase 2 stories exist at least as rough items
- [ ] Later phases are captured as rough items or left in `docs/FXKIT_DEVELOPMENT_PHASES.md`
- [ ] Sprint 1 items meet the Definition of Ready

## Tasks
- [ ] Copy Phase 1 and Phase 2 stories from the phases document
- [ ] Add labels, milestone and Points to each
- [ ] Order the backlog by priority
EOF

# ------------------------------ 6. SCAFFOLDING -------------------------------
if [[ "$CLONE_AND_SCAFFOLD" == "yes" ]]; then
  say "6/6  Clone repo and add templates"
  if [[ ! -d "$REPO_NAME" ]]; then
    gh repo clone "$REPO" "$REPO_NAME" >/dev/null
    info "Cloned into ./$REPO_NAME"
  fi
  cd "$REPO_NAME"

  mkdir -p .github/ISSUE_TEMPLATE docs

  if ! grep -q "Maven / JDT" .gitignore 2>/dev/null; then
    cat >> .gitignore <<'EOF'

# Maven / JDT (added by setup script)
target/
.classpath
.project
.settings/
.factorypath
EOF
  fi

  cat > .github/ISSUE_TEMPLATE/user-story.md <<'EOF'
---
name: User story
about: A slice of user-facing value
title: ""
labels: story
---

## User story
As a <who>, I want <what>, so that <why>.

## Acceptance criteria
- [ ]

## Tasks
- [ ]

**Points:**  ·  **Epic:**  ·  **Phase:**
EOF

  cat > .github/ISSUE_TEMPLATE/spike.md <<'EOF'
---
name: Spike
about: Time-boxed research. The output is knowledge, not code.
title: "Spike: "
labels: spike
---

## Question to answer

## Time-box
1 day (stop when it runs out, even if unfinished)

## Output
- [ ] Findings written up (ADR or note under `docs/`)
- [ ] Follow-up stories created

## Findings
EOF

  cat > .github/ISSUE_TEMPLATE/bug.md <<'EOF'
---
name: Bug report
about: Something isn't working
title: ""
labels: bug
---

## What happened

## Expected behavior

## Steps to reproduce
1.

## Environment
- OS:
- Java / JavaFX version:
- FXKit version:
EOF

  cat > .github/pull_request_template.md <<'EOF'
Closes #

## What changed

## Definition of Done
- [ ] Acceptance criteria met
- [ ] `mvn clean verify` passes
- [ ] Java API + FXML support (components)
- [ ] Uses design tokens; light and dark themes checked (components)
- [ ] Showcase page added/updated (components)
- [ ] Tests + Javadoc added
EOF

  git add -A
  if ! git diff --cached --quiet; then
    git commit -q -m "chore: add issue templates, PR template and Maven ignores"
    git push -q
    info "Pushed templates to $REPO"
  else
    info "Nothing new to commit."
  fi
  cd ..
else
  say "6/6  Skipped (CLONE_AND_SCAFFOLD=no)"
fi

# ------------------------------ DONE -----------------------------------------
say "Done!"
info "Repository:    https://github.com/$REPO"
info "Project board: $PROJECT_URL"
info ""
info "Finish the UI-only steps in GITHUB_SETUP_GUIDE.md, section 3:"
info "  - Status columns, Iteration field, views, built-in workflows"
