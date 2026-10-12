# Mob Ascension

**Mob Ascension** (NeoForge 1.21.1) turns ordinary monsters into **champions**. Whenever a hostile mob spawns, there is a chance it ascends into one of four ranks, each tougher, deadlier and more rewarding than the last:

| Rank | Colour | Stat level (random) | Spawn odds |
|---|---|---|---|
| Veteran | Green | 3 - 5 | ~17% |
| Elite | Aqua | 14 - 20 | ~9% |
| Mythic | Purple | 7 - 13 | ~3.2% |
| Apex | Gold | 25 - 30 | ~0.8% |

The rest (~70%) stay normal mobs. The Warden, Ender Dragon and Wither can never ascend. Mobs from spawners, trial spawners, breeding, buckets and conversions are excluded by default.

## How champions work

**Random power.** Every champion rolls its own stat level inside its rank's range. Level 5 equals the base values in the config, level 10 is double, and so on, so no two champions are exactly alike. A weak Apex is still far stronger than a strong Elite.

**Stats.** Champions gain extra health, armor, toughness, movement speed (capped at +100%), attack damage, knockback resistance, health regeneration, safe fall distance and body size, and are immune to fire and lava. The damage bonus also applies to arrows, tridents and fireballs.

**Effects.** Champions can spawn with the permanent Weaving, Infested or Oozing effects (higher ranks are more likely to). All effects are visible. Every champion glows with a rank-coloured outline for a few seconds on spawn and is surrounded by a rank-coloured particle aura. Apex spawns are announced to nearby players, and Mythic and Apex deaths set off fireworks.

**Abilities.** Champions roll random special abilities (higher ranks roll more):
Vampiric (heals on hit), Thorned (reflects melee damage), Frostbite (slows and freezes), Venomous (poisons), Berserker (enraged at low health), Aegis (regenerating shield), Blinker (teleports to you), Volatile (explodes on death), Splitting (spawns minions on death) and Stormcaller (calls lightning). Each ability has its own minimum rank, power and cooldown in the config.

**Gear.** Champions are armoured and armed according to their rank (leather and chainmail up to netherite and mace). Gear can carry random vanilla armor trims and vanilla enchantments, never above the vanilla maximum and capped per rank.

## Apex champions

Apex are the true threat of the mod:
* **Only Apex use ranged attacks.** They shoot **wither skulls and large fireballs** at their target. No other rank ever shoots.
* **They hunt you.** An Apex automatically targets the nearest survival player within **30 blocks** (`apexAggroRadius`), without needing to be provoked.
* **Passive mobs.** `includePassiveMobs` is **off by default**. If you turn it on, passive mobs (cows, pigs, villagers...) can ascend too, and an Apex passive mob hunts players and attacks them with wither skulls and fireballs. Lower ranks of passive mobs only get the stat boosts.

## Rewards

Rewards are intentionally modest and grow with rank. Every champion kill by a player gives:
* **1-2 pieces of gear** (armor and/or weapons) with randomly worn durability.
* **Guaranteed loot:** experience bottles, plus diamonds from Mythic upward.
* **Reward rolls** from a weighted pool: Veteran and Elite 0-1 rolls, Mythic and Apex 1 roll. The pool contains useful items for each tier; Apex can roll things like netherite, golden apples, totems of undying, and enchanted gear.
* **Bonus loot:** small extra chances at better items (Apex can very rarely drop a netherite ingot or even a nether star).
* **Experience levels:** Veteran 0-1, Elite 1-2, Mythic 2-4, Apex 8-20, plus normal XP orbs at reduced multipliers (0.5x / 0.6x / 0.75x / 1x).

Minions created by Splitting drop nothing by default.

## Configuration

Everything is configurable, either in `config/mobascension-common.toml` or in-game under **Mods > Mob Ascension > Config**: spawn weights, stat levels (min and max), every stat, effects, abilities, gear lists, enchant caps, trims, ranged chances and cooldowns, loot tables, XP, whitelists and blacklists, which spawn types are excluded, and the Apex aggro radius.

Config values are saved on first launch, so **delete `config/mobascension-common.toml` once** after updating the mod to pick up new defaults.

Loot line format: `item|chance_or_weight|min|max|enchant=level,enchant=level` (enchant levels are capped at the vanilla maximum, incompatible enchants are skipped).

## Command

`/mobascension summon <rank> [entity]` (operators only) spawns a champion of the given rank for testing.

## Building

```
gradle wrapper        # once, if there is no gradlew yet (needs Gradle 8.8+)
./gradlew build       # jar ends up in build/libs/
./gradlew runClient   # test in dev
```

Requires JDK 21. If `neo_version` in `gradle.properties` isn't found, use any 21.1.x version from https://projects.neoforged.net/neoforged/neoforge
