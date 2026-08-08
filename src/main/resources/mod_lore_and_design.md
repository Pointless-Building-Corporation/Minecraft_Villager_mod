# Better Villagers: Lore and Design Architecture

## Overview
The **Better Villagers** mod is designed to radically improve the depth, interactivity, and cultural richness of Minecraft's Villager system. By decoupling dialogue from hardcoded logic and migrating to a data-driven JSON architecture, the mod supports extensive lore building, regional immersion, and dynamic questing elements without requiring a recompile.

## Core Pillars
1. **Cultural Immersion (Region-Based):** 
   Villagers are no longer generic blockheads. Their dialogue, worldviews, and jokes are heavily influenced by the biome they spawned in.
   - **Plains:** Indian Cultural Flavor (Monsoons, Tigers, Spices, Vedic Mathematics).
   - **Desert:** Arabic/Middle Eastern Flavor (Dunes, Oases, Bazaars, Djinns, Scimitars).
   - **Savanna:** African Flavor (Acacia, Ancestors, Oral Traditions, Warrior Tribes).
   - **Snow:** Nordic/Scandinavian Flavor (Blizzards, Runes, All-Father, Fjords, Longships).
   - **Swamp:** Indonesian/Javanese Flavor (Murky Waters, Shadow Puppets, Kris, Stilts).
   - **Taiga:** Russian/Slavic Flavor (Deep Woods, Baba Yaga, Kvass, Harsh Winters).
   - **Jungle:** Brazilian/Amazonian Flavor (Canopy, Ruins, Parrots, Festivals).

2. **Tone (80% Comedy, 20% Hyper-Serious):**
   The mod embraces Minecraft's inherent silliness by injecting a large degree of meta-comedy (e.g., villagers questioning why players run around with 64 blocks of dirt in their pockets, or trample crops). However, it retains a 20% slice of hyper-serious dialogue to establish genuine worldbuilding, ancient lore, and high-stakes bounties.

3. **Profession Loyalty:**
   Villagers strictly speak lines associated with their profession. A Desert Armorer will talk about lightweight chainmail for the heat, while a Snow Armorer discusses thick fur-lined iron. 

## JSON Dialogue Schema
The entire dialogue pool is managed via `RegionDialogueManager.java`, which extends `SimpleJsonResourceReloadListener`. Server owners and modpack creators can modify or replace the `region_dialogues/*.json` files on the fly.

### Structure Example (`data/bettervillagers/region_dialogues/plains.json`)
```json
{
  "guild_master": {
    "greetings": [
      "Namaste, traveler. The winds are gentle today.",
      "The balance of the plains is disrupted."
    ],
    "reward": "The harvest has been plentiful thanks to you. Accept this Golden Apple!"
  },
  "professions": {
    "farmer": [
      "Our spices and lentils grow well this season.",
      "Have you seen my oxen? Oh wait, this is Minecraft."
    ],
    "librarian": [ ... ],
    "cleric": [ ... ],
    ... (all 15 vanilla professions mapped here)
  },
  "generic": [
    "The weather is strange today.",
    "Watch out for the creepers."
  ]
}
```

## The Bounty System & Dialogue
When a player interacts with a Villager:
1. **Guild Masters:** Manage regional bounties. After a player completes 10 bounties for a specific region, the Guild Master awards a region-specific rare item and outputs their `reward` dialogue.
2. **Standard Villagers:** If a player has accepted a bounty for a specific villager, the dialogue engine intercepts standard greeting lines and dynamically displays bounty-progression text instead. Once the bounty is complete, they return to their localized cultural dialogue.

## UI Engine
The visual component of this dialogue engine relies on a completely bespoke Side-by-Side horizontal UI system.
- **Left Panel:** Standard Vanilla Trading UI, mechanically shifted using a modified `PoseStack`.
- **Right Panel:** The massive, dynamically-scaling Dialogue and Lore window, allowing text to breathe comfortably alongside trades.
- **Hitbox Synchronization:** The mod dynamically intercepts `AbstractContainerScreen` mouse inputs to ensure that the visual shift aligns perfectly with the interactive logic shift for dragging/dropping items.
