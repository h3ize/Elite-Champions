# Mob Ascension

**Mob Ascension** (NeoForge 1.21.1) turns ordinary monsters into **champions**. Whenever a hostile mob spawns, there is a chance it ascends into one of four ranks, each tougher, deadlier and more rewarding than the last:

| Rank | Colour | Stat level (random) | Spawn odds |
|---|---|---|---|
| Veteran | Green | 3 - 5 | ~3.7% |
| Elite | Aqua | 14 - 20 | ~0.95% |
| Mythic | Purple | 7 - 13 | ~0.33% |
| Apex | Gold | 25 - 30 | ~0.07% |

The rest (~95%) stay normal mobs. The Warden, Ender Dragon and Wither can never ascend. Mobs from spawners, trial spawners, breeding, buckets and conversions are excluded by default.

## How champions work

**Random power.** Every champion rolls its own stat level inside its rank's range. Level 5 equals the base values in the config, level 10 is double, and so on, so no two champions are exactly alike. A weak Apex is still far stronger than a strong Elite.

**Stats.** Champions gain extra health, armor, toughness, movement speed (capped at +100%), attack damage, knockback resistance, safe fall distance and body size, and are immune to fire and lava. The damage bonus also applies to arrows, tridents and fireballs.

**Effects.** Champions can spawn with the permanent Weaving, Infested or Oozing effects (higher ranks are more likely to). All effects are visible. Champions are surrounded by a rank-coloured particle aura, and Apex also glow with a rank-coloured outline for a few seconds on spawn. Apex spawns are announced to nearby players, and Mythic and Apex deaths set off fireworks.

**Abilities.** Champions roll random special abilities (higher ranks roll more):
Vampiric (heals on hit), Thorned (reflects melee damage), Frostbite (slows and freezes), Venomous (poisons), Berserker (enraged at low health), Blinker (teleports to you), Volatile (explodes on death), Splitting (spawns minions on death) and Stormcaller (calls lightning). Each ability has its own minimum rank, power and cooldown in the config.

**Gear.** Champions are armoured and armed according to their rank (leather and chainmail up to netherite). Gear can carry random vanilla armor trims and vanilla enchantments, never above the vanilla maximum and capped per rank.

## Apex champions

Apex are the true threat of the mod:
* **Only Apex use ranged attacks.** They shoot **wither skulls and large fireballs** at their target. No other rank ever shoots.
* **They hunt you.** An Apex automatically targets the nearest survival player within **30 blocks** (`apexAggroRadius`), without needing to be provoked.
* **Passive mobs.** `includePassiveMobs` is **off by default**. If you turn it on, passive mobs (cows, pigs, villagers...) can ascend too, and an Apex passive mob hunts players and attacks them with wither skulls and fireballs. Lower ranks of passive mobs only get the stat boosts.

## Rewards

Rewards grow with rank. A champion kill by a player gives:
* **Name:** every champion shows its rank in its name, e.g. "Veteran Zombie" (`rankInName`; `nameAlwaysVisible` is off, so the name only shows while you look at the mob, and never through walls: a client-side check hides champion names without a clear line of sight).
* **Gear (what the mob was wearing):** Veteran and Elite 1 piece, Mythic 1-2, Apex 1-2. A drop is armor and/or a single weapon, never two weapons, so Apex gives either 2 armor pieces or a weapon and an armor piece. Pieces have randomly worn durability. Apex weapon drops can be a bow with Power 5-6 (Unbreaking 3, Mending).
* **Guaranteed loot:**
  * Veteran: 1-2 experience bottles
  * Elite: 2-3 experience bottles, 1 diamond
  * Mythic: 2-4 experience bottles, 2-3 diamonds, 1 golden apple
  * **Apex: 8-14 diamonds, 1-2 diamond blocks, 2 netherite ingots, 1-2 netherite scrap, 2-4 ender pearls, a Mending book, 2-3 enchanted golden apples, 4-6 golden apples, 10-18 emeralds, 8-14 experience bottles, 8-16 lapis, 4-8 gold ingots**
* **Random enchanted books:** every kill can drop different random positive (non-curse) enchantment books whose level scales with rank: Veteran 1 (top level about 1/3 of vanilla max, e.g. Looting I), Elite 1-2 (about 70%), Mythic 1-3 (up to vanilla max), **Apex 3-4 (up to 2.5x vanilla max, e.g. Protection X)**. The level is random between 40% of that top and the top, so higher ranks are better but not always maxed. Configurable with `randomBooksMin/Max`, `bookLevelMultiplier` and `bookMinLevelFraction`.
* **Advancement:** killing an Apex earns the **Apex Slayer** goal (Adventure tab) plus 300 XP.
* **Reward rolls:** Veteran always gets 1 roll from a weighted pool, Elite 1-2, Mythic 2-3; Apex gets 3-5 rolls from its own pool (totems, enchanted books, shields, netherite scrap, netherite gear, golden apples).
* **Bonus loot:** Veteran iron 35%; Elite diamond 15%, emerald 35%, golden apple 10%; Mythic netherite scrap 12%, golden apple 20%, ender pearl 35%; Apex beacon 10%, nether star 12%, elytra 1%, totem of undying 45%, Unbreaking III book 50%, netherite scrap 45%, diamond blocks 40%.
* **Apex armor:** a full netherite set. Protection-type enchantments are level 2.
* **Enchant caps:** armor Veteran 1, Elite 2, Mythic 2, Apex 3; weapons Veteran 1, Elite 2, Mythic 3, Apex 5 (Apex Sharpness-type enchantments roll 3-5).
* **Experience levels:** Veteran 1-3, Elite 2-5, Mythic 5-8, Apex 20-28, plus normal XP orbs at 0.35x / 0.4x / 0.5x / 1.5x.

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
