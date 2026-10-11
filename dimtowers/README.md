# Dimensional Towers (NeoForge 1.21.1)

Huge themed boss towers: five floors, each with its own identity, then a boss arena at the top with a
trial-spawner boss, an ominous-key vault and theme loot. Vanilla blocks, mobs, projectiles, loot and data only.

## Towers (19)
| Tower | Where | Boss pool |
|---|---|---|
| Overworld | any overworld biome | Rotting Colossus, Bone Archon, Spider Queen |
| Nether | nether | Ashen Tyrant, Blaze King |
| End | end islands | Void Stalker, Ashen Tyrant |
| Piglin | nether | Gilded Warlord, Blaze King |
| Crimson | crimson forest | Gilded Warlord, Blaze King |
| Warped | warped forest | Void Stalker, Ashen Tyrant |
| Frozen | snowy biomes | Bone Archon, Axe Warlord |
| Desert | desert | Rotting Colossus, Spider Queen |
| Oak | forest, flower forest | Axe Warlord, Rotting Colossus |
| Dark Forest | dark forest | Axe Warlord, Spider Queen |
| Jungle | jungle biomes | Spider Queen, Slime King |
| Swamp | swamp, mangrove swamp | Slime King, Tidal Tyrant |
| Taiga | taiga biomes | Rotting Colossus, Bone Archon |
| Badlands | badlands biomes | Rotting Colossus, Ashen Tyrant |
| Pillager | pillager outpost biomes | Axe Warlord |
| Ocean (prismarine) | beaches, stony shores | Tidal Tyrant |
| Deep Dark (sculk) | windswept hills, stony peaks | Void Stalker, Ashen Tyrant |
| Cherry | cherry grove, meadow | Axe Warlord, Slime King |
| Savanna (acacia) | savanna biomes | Rotting Colossus, Bone Archon |

## Bosses
| Boss | Base mob | Move |
|---|---|---|
| Ashen Tyrant | wither skeleton | wither-skull fan (centre skull is the blue one) |
| Rotting Colossus | zombie | telegraphed ground-slam shockwave |
| Bone Archon | skeleton | arrow fan, plus arrows raining from the sky when enraged |
| Gilded Warlord | piglin brute | calls in hoglins |
| Axe Warlord | vindicator | telegraphed leap with a landing shockwave |
| Blaze King | blaze | large-fireball barrage and a ring of small fireballs |
| Spider Queen | spider | temporary cobwebs on the target, cave-spider brood |
| Void Stalker | enderman | blinks next to you, dragon's-breath clouds (vanilla arrow immunity and block stealing removed) |
| Slime King | slime | ground slam, sheds small slimes when hit, splits on death |
| Tidal Tyrant | drowned | thrown-trident fan |

Phases: minion waves at 75 / 40 / 15 % health, enrage at 50 % (speed II, strength II, lightning every 7 s, stronger
moves). Waves draw from **that tower's own mob list** (Stray in Frozen, Husk in Desert...). A boss learns its tower
from a `dt_theme_<id>` tag in the boss spawner's spawn data. Because that makes the spawn data more than a bare
`{id:...}`, the spawner skips `finalizeSpawn`, so boss setup also runs on the first AI tick.

## Floors
| Floor | Identity | Spawner |
|---|---|---|
| 1 | Armory: trimmed armour stands, anvils, grindstones, smithing tables | classic spawner, weak half of the mob list |
| 2 | Library: enchanting tables, lecterns, bookshelves, secret closet | Trial Spawner, 8 mobs |
| 3 | Trap floor: hidden pressure plates under carpets firing wall dispensers (poison/slowness arrows), infested bricks | Trial Spawner, 10 mobs |
| 4 | Treasury: decorated pots, theme-loot chests, secret closet | Trial Spawner, 12 mobs, strong half of the list |
| 5 | Champion hall: netherite armour stands, boss-head trophies | Trial Spawner, 6 champions (top two mobs, 80 HP, strength II) |

Mobs wear armour that scales with height (chainmail, iron, diamond, netherite) and get potion effects on upper floors.
Clearing a Trial Spawner ejects a trial key that opens the vault on that floor. Floors 2-5 each have a vault.
Secret closets (floors 2 and 4) sit behind a cracked-brick door in the north-east quadrant.
Dressing: wall banners with theme patterns, paintings, trophy item frames, bells on the spires.

## Loot
- Boss vault (ominous key): the original table plus a guaranteed trim template and a named, enchanted weapon and chestplate per theme.
- Floor vaults: smaller per-theme table with a chance of the theme's trim template.
- Top-floor chests and the treasury: `end_city_treasure` plus the theme's trim template, named gear and a Tower Map.
- Tower Maps use the `dimtowers:towers` structure tag (also usable by any datapack loot table).
- Bosses drop heads (where vanilla has one) and a music disc.

## Advancements
Towering Ambition, Tower Slayer, Master of the Towers (all ten boss types), Tower Cartographer (visit all 19 tower types),
Keyring (open a vault with a trial key and an ominous trial key).


## Build
```
gradle wrapper
./gradlew build
./gradlew runClient
```
Requires JDK 21. This update was written without a Minecraft toolchain, so it has not been compiled yet.
