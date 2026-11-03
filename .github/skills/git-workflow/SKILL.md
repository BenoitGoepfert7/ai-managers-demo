---
name: git-workflow
description: Git branching, commit and pull request conventions for this repository. Use when creating a branch, committing, pushing or opening a pull request.
---

# Git workflow

## Branches

- Never commit directly to `main`.
- Name: `<type>/<JIRA-KEY>-<short-kebab-title>`, e.g. `feature/DEMO-3-validate-subscriber`. Types: `feature`, `fix`, `chore`, `docs`, `test`.
- Create from an up-to-date `main`.

## Commits

- Conventional Commits: `<type>(<scope>): <KEY> <imperative summary>` (e.g. `fix(access): DEMO-3 reject blank channel`), summary under 72 chars.
- One logical change per commit; code and its tests go together.
- Run `mvn -q verify` before committing; do not commit failing tests.
- Never commit secrets, `target/`, or IDE files (`.idea/`).
- Review with `git --no-pager diff --staged` before committing.
- Append the trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>` to AI-assisted commits.

## Pull requests

- Title follows the commit format. Description: what changed, why, Jira key, how it was tested.
- Keep PRs small and focused; no unrelated refactors.
- Use `gh pr create` and check CI results with `gh pr checks`.

## Safety

- No force-push to shared branches; no history rewriting after a push.
- Resolve conflicts by rebasing on `main`, then re-run the tests.
