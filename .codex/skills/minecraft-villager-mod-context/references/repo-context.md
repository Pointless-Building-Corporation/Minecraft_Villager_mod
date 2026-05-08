# Project Snapshot

## Identity

- Repo: `Minecraft_Villager_mod`
- Main package: `com.deepan.bettervillagers`
- Mod id: `bettervillagers`
- Type: NeoForge mod for Minecraft `1.21.1`

## Important Runtime Structure

- `BetterVillagers.java`
  - common mod bootstrap
  - item registration
  - event bus registration
  - analyzer interaction suppression
- `BetterVillagersClient.java`
  - client bootstrap
  - renderer registration
- `VillagerNameManager.java`
  - villager naming lifecycle
  - baby naming logic
  - hobo prefix handling
  - hooks that should stay lightweight
- `VillagerGenealogySavedData.java`
  - persistent full genealogy registry
  - relation/path resolution
  - DNA analyzer payload building
- `DnaAnalyzerPayload.java`
  - client payload DTO for analyzer graph
- `DnaAnalyzerScreen.java`
  - client DNA analyzer graph UI
- `DnaAnalyzerItem.java`
  - right-click scanner item

## Current Functional Goals

### Villager Naming

- Assign biome-themed cultural names
- Prefix villagers without `HOME` memory as `Hobo`
- Let player renames persist while still supporting hobo-state updates
- Give babies lineage-aware names with multiple naming patterns

### Genealogy

- Maintain a persistent full registry
- Survive unloaded and historical villagers
- Register parent-child links at villager creation
- Keep updates event-driven, not tick-heavy

### DNA Analyzer

- Handheld craftable scanner item
- Right-click villager to inspect family tree
- Open a custom client screen
- Show exact family relations
- Keep UI graph bounded and deterministic

## Performance Rules

- Avoid global villager scans
- Avoid background genealogy recomputation
- Compute family trees lazily on analyzer use
- Keep payloads compact and client-bound only
- Prefer bounded graph/node counts
- Preserve event-driven updates

## Known Build/Workflow Notes

- `compileJava` and `build` work reliably with `--no-build-cache`
- Windows file locks in `build/` can break normal clean/cache behavior
- Do not treat those lock failures as source failures without retrying `--no-build-cache`

## Editing Guidance

- Preserve current villager naming and genealogy systems unless the task explicitly replaces them
- If touching analyzer UI, preserve the non-blurred custom backdrop
- If touching genealogy logic, keep server-side classification and compact payloads
- If touching villager naming, keep baby-parent linkage compatible with genealogy registration
