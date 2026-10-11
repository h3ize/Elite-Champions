# Mob Ascension (NeoForge 1.21.1)

Any mob can "ascend" into a champion. Excluded always: **Warden, Ender Dragon, Wither** (hard-coded + blacklist default).
Everything else is affected: hostile and passive mobs alike (toggle `includePassiveMobs`).

## Ranks
Veteran (green) > Elite (aqua) > Mythic (purple) > Apex (gold).
Default spawn odds: ~70% normal, 17% Veteran, 9% Elite, 3.2% Mythic, 0.8% Apex. Spawner / trial-spawner / breeding spawns are excluded by default.

## Stats (each rank has statLevel 1-10, default 5)
Health, armor, toughness, speed, damage (also arrows/fireballs), knockback resistance, regeneration, fall resistance, body size, XP multiplier, fire immunity.

## Effects
Wind Charged, Weaving, Infested, Oozing (chance per rank); Mythic/Apex glow on spawn with a rank-coloured outline;
Apex spawns are announced to nearby players; particle aura; fireworks when Mythic/Apex are defeated.

## Abilities (new)
Vampiric, Thorned, Frostbite, Venomous, Berserker, Aegis, Blinker, Volatile, Splitting, Stormcaller.
Each has its own enabled / minRank / power / cooldown in the config. Higher ranks roll more abilities.

## Gear & loot
* Champions are armoured and armed per rank (lists are configurable).
* Gear may be a **Relic**: unique name, over-leveled enchantments, bonus attributes. Relics can drop.
* Bonus loot lists per rank (`item|chance|min|max`) and a chance of a bonus Relic.

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
