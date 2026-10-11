# Elite Champions (NeoForge 1.21.1)

Elite champion mobs built on vanilla mechanics. Four ranks (Uncommon, Rare, Epic, Legendary), scaled stats,
per-mob abilities, special gear, loot, glow/announcements/fireworks. Everything is configurable.

## Build
This project has no Gradle wrapper. Either copy `gradlew`, `gradlew.bat` and `gradle/` from the official
NeoForge MDK (1.21.1), or run `gradle wrapper --gradle-version 8.10.2` with a local Gradle. Needs JDK 21.

    ./gradlew runClient     # dev client
    ./gradlew build         # jar in build/libs

## Config
`config/elitechampions-common.toml`, or in-game: Mods -> Elite Champions -> Config.
Per-rank sections hold weights, stat levels (0-10, default 5), ability count, armor/weapon, special gear chance,
trial-effect chance, essence and bonus loot. Disable any ability with `general.disabledAbilities`.

## Abilities (ids)
Creeper: creeper_volatile, creeper_quick_fuse, creeper_persistent, creeper_last_stand
Skeleton: skeleton_sniper, skeleton_rapid_fire, skeleton_piercing, skeleton_flame_archer, skeleton_deadeye
Zombie: zombie_reinforced, zombie_sprinter, zombie_infectious, zombie_reinforcements, zombie_berserker
Spider: spider_pouncing, spider_venomous, spider_climber, spider_web_weaver, spider_night_hunter
Other: enderman_blink, witch_barrage, slime_giant, drowned_trident, pillager_arbalest, blaze_barrage,
       husk_starving, phantom_frenzy, vindicator_rage, breeze_gale

## How it works
- `FinalizeSpawnEvent` rolls the rank (weights, spawn-type exclusions, whitelist). Spawner spawns are excluded by default.
- On the mob's first tick (after vanilla finalizeSpawn) stats, abilities, gear and effects are applied.
- State is stored in the entity's persistent data (`elitechampions` compound), so it survives save/load.
