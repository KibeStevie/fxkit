# FXKit: GitHub Setup Guide

How to create the FXKit repository, the Scrum project board, and the Phase 0 backlog on GitHub.

**Two paths:**

- **Path A (recommended): automated.** Run `setup-github.sh` once. It does about 90% of the work in a minute.
- **Path B: manual.** Do everything in the GitHub web UI. It takes about 30 to 40 minutes, but it's a good way to learn how GitHub Projects works.

**Either way, finish with Section 3.** A few things (board columns, sprint iterations, views) can only be configured in the web UI.

---

## 1. Prerequisites (Path A)

| Tool | Why | Check |
|---|---|---|
| **GitHub CLI (`gh`)** | Talks to GitHub from the terminal | `gh --version` |
| **jq** | Reads JSON output from `gh` | `jq --version` |
| **Bash** | Runs the script | macOS/Linux: built in. Windows: use **Git Bash** or **WSL** |
| **Git** | Cloning and pushing | `git --version` |

Install links: https://cli.github.com · https://jqlang.github.io/jq

**Log in and grant Projects permission (one time):**

```bash
gh auth login                 # follow the prompts (choose GitHub.com, HTTPS, browser login)
gh auth refresh -s project    # adds the permission needed to manage Projects (v2)
```

---

## 2. Creating everything

### 2A. Path A: run the script

```bash
# In a folder where you keep your projects (the script will clone the repo here)
chmod +x setup-github.sh
./setup-github.sh
```

**What it does**

| Step | Result |
|---|---|
| 1 | Creates `fxkit` (public, README, MIT license, Java `.gitignore`) |
| 2 | Creates labels: `story`, `spike`, `task`, `bug`, `docs`, `epic:environment`, `epic:repo-build`, `epic:skeleton` |
| 3 | Creates 9 milestones, one per phase (Phase 0 to Phase 8) |
| 4 | Creates the **"FXKit Roadmap"** project, links it to the repo, and adds a numeric **Points** field |
| 5 | Creates the **11 Phase 0 stories** as issues (labels, milestone, acceptance criteria, tasks) and adds them to the board with their points |
| 6 | Clones the repo and pushes issue templates (user story, spike, bug), a PR template with the Definition of Done, and Maven ignores |

**Configuration:** edit the variables at the top of the script (repo name, visibility, license, project title) before running.

**Safe to re-run:** existing repos, labels, projects and issues (matched by title) are skipped or updated instead of duplicated.

### 2B. Path B: manual setup

<details>
<summary>Click to expand the manual steps</summary>

**1. Create the repository**
1. GitHub → **New repository**.
2. Name: `fxkit`. Add a description. Choose Public or Private.
3. Tick **Add a README**, set **.gitignore** to *Java*, and pick a **license** (MIT or Apache-2.0).
4. **Create repository**.

**2. Create labels** (repo → **Issues** → **Labels** → **New label**)

| Label | Color | Description |
|---|---|---|
| `story` | `#0e8a16` | A slice of user-facing value |
| `spike` | `#fbca04` | Time-boxed research |
| `task` | `#c5def5` | Technical task (≤ 1 day) |
| `bug` | `#d73a4a` | Something isn't working |
| `docs` | `#0075ca` | Documentation |
| `epic:environment` | `#1d76db` | Epic: development environment |
| `epic:repo-build` | `#5319e7` | Epic: repository and build |
| `epic:skeleton` | `#b60205` | Epic: project skeleton |

**3. Create milestones** (repo → **Issues** → **Milestones** → **New milestone**)

`Phase 0: Setup` · `Phase 1: Design Foundation` · `Phase 2: First Components` · `Phase 3: Showcase App` · `Phase 4: Inputs & Feedback` · `Phase 5: Composite Components` · `Phase 6: App Shell & Templates` · `Phase 7: Quality, Docs, Tooling & Release` · `Phase 8: Growth & Maintenance`

**4. Create the project board**
1. Your GitHub profile → **Projects** → **New project** → choose the **Board** template → name it `FXKit Roadmap`.
2. In the repo, open the **Projects** tab → **Link a project** → select `FXKit Roadmap`.
3. In the project, switch to the table view. Click **+** at the right of the column headers → **New field** → name `Points`, type **Number**.

**5. Create the Phase 0 stories**
1. Repo → **Issues** → **New issue**. Use the table in the [Appendix](#appendix-phase-0-stories) for titles, points and epic labels.
2. For each: add labels (`story` + the epic label), set the milestone to **Phase 0: Setup**, and in the sidebar under **Projects** choose `FXKit Roadmap`.
3. Put the acceptance criteria and tasks in the body as checkboxes (`- [ ] ...`), and set the **Points** value on the board.

**6. Templates (optional but recommended)**
Create `.github/ISSUE_TEMPLATE/` (user-story, spike, bug) and `.github/pull_request_template.md` in the repo. You can copy the text from the bottom section of `setup-github.sh`.

</details>

---

## 3. Finish in the web UI (both paths)

Open your project: **https://github.com/users/YOUR_USERNAME/projects/N** (the script prints the link).

### 3.1 Status columns (Scrum flow)
Project **⋯** menu (top right) → **Settings** → **Status** field. Set the options to:

`Backlog` → `Sprint Backlog` → `In Progress` → `In Review` → `Done`

> Tip: **rename** the existing *Todo* option to *Backlog* instead of deleting it. Items already on the board keep their status.

### 3.2 Sprint iterations
Project **Settings** → **+ New field** → type **Iteration**.
- Name: `Sprint`
- Duration: **2 weeks** (or 1 week if you're full-time)
- Start date: the first day of Sprint 0

GitHub automatically generates the following sprints. Rename them "Sprint 0", "Sprint 1", and so on if you like.

### 3.3 Views
Create three views (the tabs at the top of the project):

| View | Layout | Setup |
|---|---|---|
| **Board** | Board | Group by **Status**. Show *Points* and *Milestone* on cards. |
| **Backlog** | Table | Filter: `status:Backlog`. Sort by manual order (drag to prioritize). Show *Points*, *Milestone*, *Labels*. |
| **Current Sprint** | Board | Filter: `iteration:@current`. Group by **Status**. Use the view menu → **Field sum** → *Points*, so each column shows its total points. |

### 3.4 Built-in automation
Project **⋯** → **Workflows**. Enable:

- **Item added to project** → set Status = `Backlog`
- **Item closed** → set Status = `Done`
- **Pull request merged** → set Status = `Done`
- **Auto-add to project** (optional) → automatically add new issues from the `fxkit` repo. Availability can depend on your plan.

### 3.5 Load Sprint 0
1. Open the **Backlog** view.
2. Select all Phase 0 items → set **Sprint** = *Sprint 0*, **Status** = `Sprint Backlog`.
3. Check the total. Phase 0 comes to **26 points**, which is a lot for a first sprint, but since it's a setup sprint, treat it as your calibration sprint. If it feels heavy, leave the last two stories (project board setup and initial backlog) as "work in progress" while you do the rest.

---

## 4. Running a sprint on the board

| When | What to do on GitHub |
|---|---|
| **Sprint Planning** | Write the Sprint Goal in the iteration description or in a pinned issue. Move the chosen stories to **Sprint Backlog** and assign the current **Sprint** iteration. |
| **Daily** | Drag the card you're working on to **In Progress**. Add a short comment on the issue (your daily log). |
| **Working** | Branch name: `feature/12-parent-pom` (issue number + short name). Commit with Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`). |
| **Finishing** | Open a PR with `Closes #12` in the description (the PR template already includes it). Tick the Definition of Done checklist. Merging closes the issue and moves it to **Done**. |
| **Sprint Review** | Filter to the current sprint's **Done** column. Demo it and note feedback as new backlog issues. |
| **Retrospective** | Add a short note in `docs/retros/sprint-N.md` (went well / didn't / change / learned). |
| **Refinement** | Keep the top ~2 sprints of the Backlog **Ready** (clear criteria, ≤ 8 points). Split anything bigger. |

**Closing keywords:** `Closes #12`, `Fixes #12` or `Resolves #12` in a PR description automatically close the issue on merge.

---

## 5. Troubleshooting

| Problem | Fix |
|---|---|
| `gh: command not found` | Install GitHub CLI and reopen your terminal. |
| `Missing 'project' permission` / `insufficient scopes` | Run `gh auth refresh -s project`. |
| `unknown command "project"` | Your `gh` is too old. Update it. |
| Script won't run on Windows | Use **Git Bash** or **WSL**, not PowerShell or CMD. |
| `jq: command not found` | Install jq (`winget install jqlang.jq`, `brew install jq` or `sudo apt install jq`). |
| Project created under the wrong account | The script uses your personal account (`@me`). For an organization, change `--owner "@me"` to `--owner YOUR_ORG` and set `OWNER` and `REPO` accordingly. |
| Points didn't get set on some cards | Set them in the table view (the *Points* column). It's a one-time cost. |
| A command fails half-way | Fix the cause and re-run. Existing items are skipped. If an issue is duplicated, close one. |
| `git push` fails at the end | Check `git config user.name` and `user.email`. Then commit and push the `.github/` files manually. |

> The script was syntax-checked but I couldn't run it against a real GitHub account. If any command fails, copy the error output and I'll help you fix it.

---

## Appendix: Phase 0 Stories

Milestone for all: **Phase 0: Setup**. Labels: `story` + epic label.

| # | Story | Points | Epic label |
|---|---|---|---|
| 1 | Install JDK 25 and verify the toolchain | 2 | `epic:environment` |
| 2 | Configure VS Code for Java and Maven | 2 | `epic:environment` |
| 3 | Install Scene Builder | 1 | `epic:environment` |
| 4 | Initialize repository files (.gitignore, README, LICENSE, docs) | 2 | `epic:repo-build` |
| 5 | Create the parent POM | 3 | `epic:repo-build` |
| 6 | Create the fxkit-core and fxkit-showcase modules | 3 | `epic:repo-build` |
| 7 | Add JavaFX dependencies and the javafx-maven-plugin | 3 | `epic:repo-build` |
| 8 | Hello JavaFX: minimal Application opens a window | 2 | `epic:skeleton` |
| 9 | Add module-info.java to both modules | 3 | `epic:skeleton` |
| 10 | Set up the project board, labels and sprint fields | 2 | `epic:skeleton` |
| 11 | Write the initial Product Backlog | 3 | `epic:skeleton` |
| | **Total** | **26** | |

**Suggested order of work:** 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11. Story 10 is largely done by the script, and you finish it in Section 3.

---

## Next: Sprint 1 backlog

Once Phase 0 is done, add the **Phase 1** stories from `FXKIT_DEVELOPMENT_PHASES.md` (color tokens, spacing/radius/typography, utility classes, dark theme, `ThemeManager`) to the board. Story 11 covers exactly that. If you'd like, I can generate a second script for the Phase 1 and Phase 2 issues so it's a single run.
