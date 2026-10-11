# Mob Ascension (NeoForge 1.21.1)

Any mob can "ascend" into a champion. Excluded always: **Warden, Ender Dragon, Wither** (hard-coded + blacklist default).
Only hostile vanilla mobs ascend (Zombies, Creepers, Skeletons...). Toggle `includePassiveMobs` / `vanillaMobsOnly` in the config.

## Ranks
Veteran (green) > Elite (aqua) > Mythic (purple) > Apex (gold).
Default spawn odds: ~70% normal, 17% Veteran, 9% Elite, 3.2% Mythic, 0.8% Apex. Spawner / trial-spawner / breeding spawns are excluded by default.

## Stats (each rank has statLevel 1-10, default 5)
Health, armor, toughness, speed, damage (also arrows/fireballs), knockback resistance, regeneration, fall resistance, body size, XP multiplier, fire immunity.

## Effects
All effects are visible (particles + icon). Weaving, Infested, Oozing (chance per rank); Wind Charged was removed so bosses no longer burst on death; every rank glows for 8 seconds on spawn with a rank-coloured outline (configurable, -1 = permanent);
Apex spawns are announced to nearby players; particle aura; fireworks when Mythic/Apex are defeated.

## Abilities (new)
Vampiric, Thorned, Frostbite, Venomous, Berserker, Aegis, Blinker, Volatile, Splitting, Stormcaller.
Each has its own enabled / minRank / power / cooldown in the config. Higher ranks roll more abilities.

## Gear & loot
* Champions are armoured and armed per rank (lists are configurable).
* Armor can spawn with random **vanilla-only** trims.
* Gear can carry plain vanilla enchantments. Levels never exceed the vanilla max, and each rank has a cap (Veteran 2, Elite 3, Mythic 4, Apex 5).
* **Guaranteed gear drop:** every champion kill drops 1-2 pieces: 1 armor + 1 weapon, 2 armor pieces, or 2 weapons, with randomly worn durability.
* **Guaranteed rewards:** every kill also drops all `guaranteedLoot` lines plus `rewardRollsMin`-`rewardRollsMax` rolls from a weighted `rewardPool` (Veteran 1, Elite 1, Mythic 1-2, Apex 1-2), then chance-based `bonusLoot`.
* Shields with Unbreaking (+ Mending at Elite and above) are a possible roll in the reward pool, not a guaranteed drop.
* Line format: `item|chance_or_weight|min|max|enchant=level,enchant=level` (levels are capped at the vanilla max, incompatible enchants are skipped).
* Kill rewards (XP levels): Veteran 1, Elite 3, Mythic 5-8, Apex 25-60 (random in range); XP orb multipliers are 1x / 1.25x / 1.5x / 2x.
* NOTE: config values are saved in `config/mobascension-common.toml`. Delete that file once so the new defaults apply.
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
