# Better Villagers (Minecraft NeoForge Mod)

A Minecraft NeoForge mod that introduces advanced kinship and genealogy tracking to Villagers.

## Features

### 🧬 Grimoire of Ancestry
Craft the **Grimoire of Ancestry** to inspect the genetic bloodlines of villagers.

**Recipe:**
- 4x Glass Pane
- 4x Spider Eye
- 1x Ender Pearl
*(Arranged in a cross pattern with the Ender Pearl in the center)*

Right-click any Villager with the Grimoire of Ancestry to open the **Grimoire of Ancestry** screen.

### 🌳 Sugiyama Family Tree Layout
The analyzer screen displays a beautiful, mathematically-arranged, infinite-scrolling family tree!
- Computes exact parent-child generational bands.
- Accurately renders complex non-linear genealogies (such as cousins and distant relatives).
- Groups multi-parent families and visually maps siblings under unified horizontal connection buses.
- **Color-coded relations**: Parents (Blue), Children (Green), Siblings/Mates (Pink), Self (Gold), and Distant Relatives (Gray).

### 🧬 Villager Genealogy Tracking
Villagers now persistently track their ancestors, descendants, and mates in their saved NBT data. Relationships are dynamically evaluated at runtime to accurately map the entire genetic lineage of a village!

## Installation
Drop the built `.jar` file into your Prism Launcher or standard Minecraft `mods` folder.

**Building from source:**
```bash
# Compile and run unit tests (including the layout engine mathematical tests)
./gradlew build test
```

## Credits
Built with the [NeoForge Modding API](https://neoforged.net/).

**3D Assets:**
- "Book for minecraft" (https://skfb.ly/6ZPMu) by SebastianFEnriquez is licensed under Creative Commons Attribution (http://creativecommons.org/licenses/by/4.0/). (Used for the Grimoire of Ancestry).
