# Mob Ascension

**Minecraft 1.21.1 · NeoForge · JDK 21**

Mob Ascension turns ordinary hostile mobs into **champions**. Every time a monster spawns there is a chance it ascends into one of four ranks. Each rank is stronger, more dangerous and pays out better than the last, so a night in the open can suddenly turn into a real fight worth having.

## The four ranks

| Rank | Colour | Stat level | Spawn odds | Levels on kill |
|---|---|---|---|---|
| **Veteran** | Green | 3 - 5 | ~17% | 1 - 4 |
| **Elite** | Aqua | 14 - 20 | ~9% | 8 - 13 |
| **Mythic** | Purple | 7 - 13 | ~3.2% | 13 - 18 |
| **Apex** | Gold | 25 - 30 | ~0.8% | 18 - 26 |

About 70% of mobs stay normal. The Warden, Ender Dragon and Wither never ascend. Mobs from spawners, trial spawners, breeding, buckets and conversions are excluded by default.

## What makes a champion different

- **Random power.** Every champion rolls its own stat level inside its rank's range, so no two are exactly alike. Level 5 equals the base values in the config, level 10 is double, and so on.
- **Stronger stats.** Extra health, armor, toughness, speed (capped at +100%), attack damage, knockback resistance, health regeneration, safe fall distance and body size. The damage bonus also applies to arrows, tridents and fireballs. Champions are immune to fire and lava.
- **Visible effects.** Champions can carry the permanent Weaving, Infested or Oozing effects, with higher ranks more likely to. Every effect is visible with its icon and particles.
- **Looks and sound-off.** A rank-coloured glow on spawn, a rank-coloured particle aura, and a name tag showing the rank. Apex spawns are announced to nearby players. Mythic and Apex deaths set off fireworks.
- **Special abilities.** Higher ranks roll more of them:
  - Vampiric heals on hit
  - Thorned reflects melee damage
  - Frostbite slows and freezes
  - Venomous poisons
  - Berserker enrages at low health
  - Aegis gives a regenerating shield
  - Blinker teleports to you
  - Volatile explodes on death
  - Splitting spawns minions on death
  - Stormcaller calls lightning
- **Gear.** Armor and weapons scale with rank, from leather and chainmail up to netherite. Gear can carry vanilla armor trims and vanilla enchantments, capped per rank.

## Apex champions

Apex mobs are the real threat of the mod.

- **Strength II and Resistance I**, permanently.
- **Two extra random buffs** from Speed, Jump Boost, Fire Resistance, Water Breathing, Slow Falling, Haste and Absorption. Regeneration, Poison, Wither and instant health or damage are never rolled.
- **Ranged attacks.** Only Apex shoot, using wither skulls and large fireballs.
- **They hunt you.** An Apex targets the nearest survival player within 30 blocks without needing to be provoked.
- **Passive mobs.** Off by default. If you enable `includePassiveMobs`, an Apex cow or villager hunts players too.

## Rewards

Champions only drop rewards when a **player** gets the kill.

- **Gear:** 1 to 2 pieces of armor and weapons, with randomly worn durability.
- **Guaranteed loot:** experience bottles for every rank. Mythic also drops diamonds. **Apex always drops 2-4 Bottles o' Enchanting, 1-2 netherite ingots and an enchanted golden apple.**
- **Apex weapon drop:** either a netherite tool or a **bow with Power 7+** (Unbreaking 3, Mending). Maces do not drop.
- **Reward roll:** one roll from a weighted pool per rank. At Apex that pool includes diamonds, netherite, golden apples, totems of Undying, a netherite sword or pickaxe, and a lot of **enchanted books** (Mending, Protection 4, Sharpness 5, Unbreaking 3, Efficiency 5, Fortune 3, Looting 3, Power 5, Feather Falling 4, Thorns 3).
- **Bonus loot:** small independent chances at extras, such as a netherite ingot, totem, nether star, or another book.
- **Experience levels:** given straight to the killer, on top of normal XP orbs (0.5x / 0.6x / 0.75x / 1x by rank).

Minions from the Splitting ability drop nothing by default.

## Configuration

Everything is configurable in `config/mobascension-common.toml` or in-game under **Mods > Mob Ascension > Config**: spawn weights, stat levels, every stat, effects, abilities, gear lists, enchant caps, trims, ranged chances and cooldowns, loot tables, XP and levels, whitelists and blacklists, excluded spawn types and the Apex aggro radius.

Loot lines use the format `item|chance_or_weight|min|max|enchant=level,enchant=level`. Config values are saved on first launch, so after updating the mod, **delete the old config file once** to pick up new defaults.

## Command

`/mobascension summon <rank> [entity]` (operators only) spawns a champion of that rank for testing.

## Building

```
gradle wrapper        # once, if there is no gradlew yet (needs Gradle 8.8+)
./gradlew build       # jar ends up in build/libs/
./gradlew runClient   # test in dev
```
