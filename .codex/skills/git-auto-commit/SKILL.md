---
name: git-auto-commit
description: Maintains git commit discipline and automated milestone hooks for the Minecraft_Villager_mod repository. Use this skill whenever implementing features, modifying assets, refactoring code, fixing bugs, or completing task milestones to ensure frequent, atomic, well-documented commits.
---

# Git Auto-Commit Workflow & Milestone Hooks

This skill establishes the automated committing protocol and milestone hooks for the `Minecraft_Villager_mod` project. All automated agents and developers working in this repository MUST act as an active commit hook, regularly staging and committing changes at logical checkpoints rather than leaving large uncommitted diffs in the working tree.

## 1. Core Commit Discipline

- **Atomic Commits**: Group related changes into focused, atomic commits. Do not bundle unrelated bug fixes, feature additions, and formatting changes into a single monolithic commit.
- **Always Verify First**: Never commit code with syntax errors or broken resource references. For Java code changes, run a fast verification check (e.g., `.\gradlew.bat --no-build-cache compileJava`) before staging when possible. For asset JSON changes, ensure valid JSON formatting.
- **No Orphaned State**: Do not leave completed tasks uncommitted at the end of a session or conversational turn. If a feature or fix is working, commit it immediately.

## 2. Automated Milestone Hooks (When to Commit)

The agent must trigger an auto-commit whenever any of the following milestone hooks are reached:

1. **Post-Asset Creation / Modification (`[Asset Hook]`)**:
   - Trigger: A 3D model (`.json`), texture (`.png`), recipe, tag, or localization string (`en_us.json`) is created or significantly updated.
   - Action: Stage the specific asset files and commit immediately.
2. **Post-Bug Fix (`[Fix Hook]`)**:
   - Trigger: A root cause is identified and patched in source code (e.g., fixing NBT serialization tag types).
   - Action: Verify compilation/functionality, stage the modified files, and commit immediately.
3. **Task Checklist Completion (`[Task Hook]`)**:
   - Trigger: Marking a major TODO item as `[x]` in `task.md` or completing a section of `implementation_plan.md`.
   - Action: Commit the code corresponding to that completed task.
4. **Infrastructure & Setup (`[Chore Hook]`)**:
   - Trigger: Adding license files, updating build configurations (`build.gradle`, `gradle.properties`), or modifying workspace rules/skills.
   - Action: Stage and commit boilerplate and config updates separately from feature code.

## 3. Commit Message Conventions

We adhere to standard Conventional Commits formatting:
```
<type>(<scope>): <short imperative description>
```

### Types:
- `feat`: A new feature, item, block, entity, or asset design (e.g., `feat(analyzer): add futuristic 3d sci-fi scanner model`)
- `fix`: A bug fix (e.g., `fix(genealogy): fix nbt list tag type mismatch on world reload`)
- `docs`: Documentation or walkthrough updates (e.g., `docs: update walkthrough for 3d model redesign`)
- `style`: Formatting, missing semicolons, etc., no code change
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `perf`: Code change that improves performance (e.g., caching analyzer tree queries)
- `test`: Adding or correcting tests
- `chore`: Maintenance, build scripts, licensing, or skill file updates (e.g., `chore: add MIT license and copyright attribution`)

### Scopes:
Common repository scopes include: `analyzer`, `genealogy`, `naming`, `villager`, `client`, `network`, `assets`, `build`, `skills`.

## 4. Helper Script Usage

A PowerShell helper script is available at `scripts/auto_commit.ps1` and `.codex/skills/git-auto-commit/scripts/auto_commit.ps1` to facilitate quick, structured committing:

```powershell
# Example usage:
.\scripts\auto_commit.ps1 -Type "feat" -Scope "analyzer" -Message "add futuristic 3d sci-fi scanner model" -Files "src/main/resources/assets/bettervillagers/models/item/dna_analyzer.json"
```

If committing manually via git CLI, always ensure clean staging:
```powershell
git add <specific-files>
git commit -m "type(scope): message"
```
