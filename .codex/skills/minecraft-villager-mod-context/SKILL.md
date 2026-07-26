---
name: minecraft-villager-mod-context
description: Repo-specific context and workflow for the Minecraft_Villager_mod NeoForge project at D:\Deepan\Minecraft_Villager_mod. Use whenever working in this repository, especially for villager naming, genealogy, DNA Analyzer UI, client/server NeoForge wiring, assets, screens, packets, and performance-sensitive family-tree features. Prefer this skill to reduce token usage by loading only the listed files and references instead of rediscovering repo structure.
---

# Repo Workflow

Use this skill whenever the current workspace is `D:\Deepan\Minecraft_Villager_mod`.

Start with these files only unless the task clearly requires more:
- `src/main/java/com/deepan/bettervillagers/BetterVillagers.java`
- `src/main/java/com/deepan/bettervillagers/BetterVillagersClient.java`
- `src/main/java/com/deepan/bettervillagers/villager/VillagerNameManager.java`
- For DNA analyzer and genealogy work, also read:
  - `src/main/java/com/deepan/bettervillagers/villager/VillagerGenealogySavedData.java`
  - `src/main/java/com/deepan/bettervillagers/villager/VillagerGenealogyRecord.java`
  - `src/main/java/com/deepan/bettervillagers/network/DnaAnalyzerPayload.java`
  - `src/main/java/com/deepan/bettervillagers/client/screen/DnaAnalyzerScreen.java`
  - `src/main/java/com/deepan/bettervillagers/item/DnaAnalyzerItem.java`

Read `references/repo-context.md` before making architectural changes.

# Token Discipline

Avoid loading the whole repo.

Prefer this order:
1. Read `references/repo-context.md`
2. Read only the exact feature entrypoints
3. Search narrowly for touched symbols
4. Compile targeted tasks before full builds when possible

Do not reread large generated or build directories.

# Build / Verify

Target stack:
- Minecraft `1.21.1`
- NeoForge `21.1.227`
- Java `21`

Preferred verification:
- Use `.\gradlew.bat --no-build-cache compileJava --stacktrace` for fast code validation
- Use `.\gradlew.bat --no-build-cache build --stacktrace` for full verification

The repo has hit Windows file-lock issues in `build/` with normal cache/clean flows. Prefer `--no-build-cache` before assuming the code is broken.

# Git & Auto-Commit Discipline

All agents working in this project must actively maintain clean git history and trigger atomic commits at relevant milestones (see `.codex/skills/git-auto-commit/SKILL.md`).
- Use `.\scripts\auto_commit.ps1 -Type "feat|fix|chore" -Scope "<area>" -Message "<summary>" -Files <paths>` after completing logical checkpoints (e.g., asset creation, bug fixes, license/setup tasks).
- Never leave completed, working changes uncommitted at the end of a session or task step.

# Current Focus

Assume the active long-term goal is to improve villager family simulation and inspection:
- culturally themed villager naming
- baby lineage-aware naming
- persistent genealogy registry
- DNA Analyzer item
- graph-based family tree UI
- exact kinship relations on the analyzer
- performance-safe lazy computation

If the task is related to villagers, genealogy, naming, analyzer UI, or performance, preserve and extend the current system instead of redesigning from scratch.
