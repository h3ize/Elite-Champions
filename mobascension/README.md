# Mob Ascension (NeoForge 1.21.1)

Any mob can "ascend" into a champion. Excluded always: **Warden, Ender Dragon, Wither** (hard-coded + blacklist default).
Only hostile vanilla mobs ascend (Zombies, Creepers, Skeletons...). Toggle `includePassiveMobs` / `vanillaMobsOnly` in the config.

## Ranks
Veteran (green) > Elite (aqua) > Mythic (purple) > Apex (gold).
Default spawn odds: ~70% normal, 17% Veteran, 9% Elite, 3.2% Mythic, 0.8% Apex. Spawner / trial-spawner / breeding spawns are excluded by default.

## Stats (each rank has statLevel 1-10, default 5)
Health, armor, toughness, speed, damage (also arrows/fireballs), knockback resistance, regeneration, fall resistance, body size, XP multiplier, fire immunity.

## Effects
All effects are visible. Wind Charged, Weaving, Infested, Oozing (chance per rank); every rank glows for 8 seconds on spawn with a rank-coloured outline (configurable, -1 = permanent);
Apex spawns are announced to nearby players; particle aura; fireworks when Mythic/Apex are defeated.

## Abilities (new)
Vampiric, Thorned, Frostbite, Venomous, Berserker, Aegis, Blinker, Volatile, Splitting, Stormcaller.
Each has its own enabled / minRank / power / cooldown in the config. Higher ranks roll more abilities.

## Gear & loot
* Champions are armoured and armed per rank (lists are configurable).
* Armor can spawn with random **vanilla-only** trims.
* Gear can carry plain vanilla enchantments. Levels never exceed the vanilla max, and each rank has a cap (Veteran 2, Elite 3, Mythic 4, Apex 5).
* **Guaranteed gear drop:** every champion kill drops 1-2 random pieces (weapon and/or armor) with randomly worn durability. If the mob wasn't wearing that slot, a piece is generated.
* Bonus loot lists per rank (`item|chance|min|max`).
* Kill rewards (XP levels): Veteran 5, Elite 15, Mythic 30, Apex 60.
* Champions have no name tag.

## Ranged attacks
Champions can shoot at their target: Veteran/Elite small fireballs, Mythic large fireballs, Apex wither skulls (chance and cooldown per rank).

## Config
`config/mobascension-common.toml`, or in-game: Mods > Mob Ascension > Config.

## Command
`/mobascension summon <rank> [entity]` (op) - spawns a champion for testing.

## Build
```
gradle wrapper        # once, if there is no gradlew yet (needs Gradle 8.8+)
./gradlew build       # jar ends up in build/libs/
./gradlew runClient   # test in dev
```
Requires JDK 21. If `neo_version` in gradle.properties isn't found, use any 21.1.x version from
https://projects.neoforged.net/neoforged/neoforge
