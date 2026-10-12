package com.mobascension;

import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.List;

/** Everything in Mob Ascension is configurable here (file: mobascension-common.toml, or the in-game config GUI). */
public final class AscensionConfig {
    private static final EnumMap<Rank, RankSettings> RANKS = new EnumMap<>(Rank.class);
    private static final EnumMap<Ability, AbilitySettings> ABILITIES = new EnumMap<>(Ability.class);

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue INCLUDE_PASSIVE;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_GEAR;
    public static final ModConfigSpec.BooleanValue REQUIRE_PLAYER_KILL;
    public static final ModConfigSpec.BooleanValue VANILLA_ONLY;
    public static final ModConfigSpec.BooleanValue PARTICLES;
    public static final ModConfigSpec.BooleanValue GLOW_COLORS;
    public static final ModConfigSpec.BooleanValue SCALE_RANGED_DAMAGE;
    public static final ModConfigSpec.BooleanValue MINIONS_DROP_LOOT;
    public static final ModConfigSpec.BooleanValue RANK_IN_NAME;
    public static final ModConfigSpec.BooleanValue NAME_ALWAYS_VISIBLE;
    public static final ModConfigSpec.DoubleValue COMMON_WEIGHT;
    public static final ModConfigSpec.IntValue ANNOUNCE_RADIUS;
    public static final ModConfigSpec.IntValue APEX_AGGRO_RADIUS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_SPAWN_TYPES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        ENABLED = b.comment("Master switch.").define("enabled", true);
        INCLUDE_PASSIVE = b.comment("If true, passive mobs (animals, villagers, etc.) can ascend too.",
                "If false (default), only hostile mobs (Zombies, Creepers, Skeletons...) can.",
                "If true, passive mobs can ascend too; an Apex passive mob hunts players and attacks them with",
                "wither skulls and fireballs.")
                .define("includePassiveMobs", false);
        VANILLA_ONLY = b.comment("Only mobs from vanilla Minecraft can ascend (mobs added by other mods never do).")
                .define("vanillaMobsOnly", true);
        BLACKLIST = b.comment("Entity types that can never become champions.",
                "Warden, Ender Dragon and Wither are ALWAYS excluded in code as well.")
                .defineListAllowEmpty("blacklist",
                        List.of("minecraft:warden", "minecraft:ender_dragon", "minecraft:wither"),
                        () -> "minecraft:warden", o -> o instanceof String);
        WHITELIST = b.comment("If not empty, ONLY these entity types can become champions. Leave empty for all mobs.")
                .defineListAllowEmpty("whitelist", List.of(), () -> "minecraft:zombie", o -> o instanceof String);
        EXCLUDED_SPAWN_TYPES = b.comment("Spawn types that never roll champions.",
                "Valid: NATURAL, CHUNK_GENERATION, STRUCTURE, BREEDING, MOB_SUMMONED, JOCKEY, EVENT, CONVERSION,",
                "REINFORCEMENT, TRIGGERED, BUCKET, SPAWN_EGG, COMMAND, DISPENSER, PATROL, SPAWNER, TRIAL_SPAWNER")
                .defineListAllowEmpty("excludedSpawnTypes",
                        List.of("SPAWNER", "TRIAL_SPAWNER", "BREEDING", "BUCKET", "CONVERSION"),
                        () -> "SPAWNER", o -> o instanceof String);
        COMMON_WEIGHT = b.comment("Spawn weight of normal (non-champion) mobs. Compare against the rank weights.",
                "Defaults give about 70% normal, 17% Veteran, 9% Elite, 3.2% Mythic, 0.8% Apex.")
                .defineInRange("normalWeight", 70.0, 0.0, 100000.0);
        REPLACE_VANILLA_GEAR = b.comment("Replace armor/weapons mobs spawned with by vanilla.")
                .define("replaceVanillaGear", true);
        REQUIRE_PLAYER_KILL = b.comment("Gear and bonus loot only drop if a player gets the kill.")
                .define("requirePlayerKill", true);
        PARTICLES = b.comment("Rank-coloured particle aura around champions.").define("particles", true);
        GLOW_COLORS = b.comment("Put champions on a coloured scoreboard team so the Glowing outline matches their rank.")
                .define("coloredGlowOutline", true);
        SCALE_RANGED_DAMAGE = b.comment("Also apply the rank damage bonus to arrows, tridents, fireballs, etc.")
                .define("scaleRangedDamage", true);
        MINIONS_DROP_LOOT = b.comment("Whether minions from the Splitting ability drop loot/xp.")
                .define("minionsDropLoot", false);
        RANK_IN_NAME = b.comment("Put the champion's rank in its name, e.g. \"Veteran Zombie\".")
                .define("rankInName", true);
        NAME_ALWAYS_VISIBLE = b.comment("Show the rank name above champions all the time (visible through walls).",
                "If false, the name only shows while you look directly at the mob.")
                .define("nameAlwaysVisible", true);
        ANNOUNCE_RADIUS = b.comment("Radius (blocks) in which players are told about announced champions.")
                .defineInRange("announceRadius", 96, 8, 1024);
        APEX_AGGRO_RADIUS = b.comment("Apex champions automatically target the nearest player within this many blocks (0 = off).")
                .defineInRange("apexAggroRadius", 30, 0, 128);
        b.pop();

        b.push("ranks");
        for (Rank rank : Rank.values()) {
            RANKS.put(rank, new RankSettings(b, rank));
        }
        b.pop();

        b.push("abilities");
        for (Ability ability : Ability.values()) {
            ABILITIES.put(ability, new AbilitySettings(b, ability));
        }
        b.pop();

        SPEC = b.build();
    }

    private AscensionConfig() {}

    public static RankSettings rank(Rank r) { return RANKS.get(r); }

    public static AbilitySettings ability(Ability a) { return ABILITIES.get(a); }

    public static final class AbilitySettings {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.IntValue minRank;
        public final ModConfigSpec.DoubleValue power;
        public final ModConfigSpec.IntValue cooldown;

        AbilitySettings(ModConfigSpec.Builder b, Ability a) {
            b.push(a.id);
            enabled = b.define("enabled", true);
            minRank = b.comment("Lowest rank that can roll this ability (1=Veteran, 2=Elite, 3=Mythic, 4=Apex).")
                    .defineInRange("minRank", a.defaultMinRank, 1, 4);
            power = b.comment("Strength of the ability (meaning depends on the ability, see Ability.java).")
                    .defineInRange("power", a.defaultPower, 0.0, 1000.0);
            cooldown = b.comment("Cooldown in ticks (20 = 1 second) for abilities that use one.")
                    .defineInRange("cooldownTicks", a.defaultCooldown, 0, 100000);
            b.pop();
        }
    }

    public static final class RankSettings {
        public final ModConfigSpec.DoubleValue weight;
        public final ModConfigSpec.IntValue statLevel, statLevelMin;
        public final ModConfigSpec.ConfigValue<String> displayName;

        public final ModConfigSpec.DoubleValue health, armor, toughness, speed, damage, knockback,
                regen, fall, size, xp;

        public final ModConfigSpec.IntValue abilityCount;
        public final ModConfigSpec.DoubleValue weaving, infested, oozing;
        public final ModConfigSpec.BooleanValue fireResistant;

        public final ModConfigSpec.IntValue glowSeconds;
        public final ModConfigSpec.BooleanValue announce, fireworks;

        public final ModConfigSpec.DoubleValue gearChance, trim, enchantChance, rangedChance, headChance;
        public final ModConfigSpec.IntValue enchantCount, maxEnchantLevel, maxProtectionLevel, weaponEnchantLevel, gearDropsMin, gearDropsMax, killLevels, killLevelsMax, rangedCooldown,
                rewardRollsMin, rewardRollsMax;
        public final EnumMap<EquipmentSlot, ModConfigSpec.ConfigValue<List<? extends String>>> gear =
                new EnumMap<>(EquipmentSlot.class);
        public final ModConfigSpec.ConfigValue<List<? extends String>> bonusLoot, guaranteedLoot, rewardPool;

        RankSettings(ModConfigSpec.Builder b, Rank rank) {
            int i = rank.ordinal();
            b.push(rank.id);

            weight = b.comment("Relative spawn weight of this rank (0 disables the rank).")
                    .defineInRange("weight", new double[]{17.0, 9.0, 3.2, 0.8}[i], 0.0, 100000.0);
            displayName = b.define("displayName", rank.defaultName);

            b.push("stats");
            statLevelMin = b.comment("MIN stat level for this rank. Every champion rolls a random level between min and max.",
                    "Defaults: Veteran 3, Elite 14, Mythic 7, Apex 25.")
                    .defineInRange("statLevelMin", new int[]{3, 14, 7, 25}[i], 1, 100);
            statLevel = b.comment("MAX stat level for this rank (see statLevelMin).",
                    "Level 5 = the values below, level 10 = double, level 1 = one fifth.",
                    "Defaults: Veteran 5, Elite 20, Mythic 13, Apex 30.")
                    .defineInRange("statLevel", new int[]{5, 20, 13, 30}[i], 1, 100);
            health = b.comment("Extra max health as a fraction (1.0 = +100%) at level 5.")
                    .defineInRange("healthBonus", new double[]{0.5, 1.0, 2.0, 4.0}[i], 0.0, 1000.0);
            armor = b.comment("Extra armor points at level 5.")
                    .defineInRange("armorBonus", new double[]{2, 4, 6, 10}[i], 0.0, 30.0);
            toughness = b.comment("Extra armor toughness at level 5.")
                    .defineInRange("toughnessBonus", new double[]{0, 1, 3, 6}[i], 0.0, 20.0);
            speed = b.comment("Extra movement speed as a fraction at level 5.")
                    .defineInRange("speedBonus", new double[]{0.05, 0.10, 0.15, 0.25}[i], 0.0, 10.0);
            damage = b.comment("Extra attack damage as a fraction at level 5.")
                    .defineInRange("damageBonus", new double[]{0.35, 0.8, 1.5, 2.75}[i], 0.0, 100.0);
            knockback = b.comment("Knockback resistance (0-1) at level 5.")
                    .defineInRange("knockbackResistance", new double[]{0.1, 0.25, 0.5, 0.8}[i], 0.0, 1.0);
            regen = b.comment("Health regenerated per second at level 5.")
                    .defineInRange("regenPerSecond", new double[]{0, 0.25, 0.5, 1.0}[i], 0.0, 1000.0);
            fall = b.comment("Extra blocks of safe fall distance at level 5.")
                    .defineInRange("safeFallBonus", new double[]{5, 10, 20, 40}[i], 0.0, 1000.0);
            size = b.comment("Extra body size as a fraction at level 5 (0.2 = 20% bigger).")
                    .defineInRange("sizeBonus", new double[]{0.0, 0.05, 0.10, 0.20}[i], 0.0, 2.0);
            xp = b.comment("Experience drop multiplier.")
                    .defineInRange("xpMultiplier", new double[]{0.35, 0.4, 0.5, 0.6}[i], 0.0, 1000.0);
            b.pop();

            b.push("effects");
            abilityCount = b.comment("How many special abilities this rank rolls (limited by what's enabled).")
                    .defineInRange("abilityCount", i + 1, 0, 10);
            fireResistant = b.comment("Immune to fire and lava.").define("fireResistant", true);
            weaving = b.comment("Chance to spawn Weaving (infinite).")
                    .defineInRange("weavingChance", new double[]{0.05, 0.10, 0.20, 0.35}[i], 0.0, 1.0);
            infested = b.comment("Chance to spawn Infested (infinite).")
                    .defineInRange("infestedChance", new double[]{0.05, 0.10, 0.20, 0.35}[i], 0.0, 1.0);
            oozing = b.comment("Chance to spawn Oozing (infinite).")
                    .defineInRange("oozingChance", new double[]{0.05, 0.10, 0.20, 0.35}[i], 0.0, 1.0);
            glowSeconds = b.comment("Seconds of Glowing when spawned (outline is coloured by rank). 0 = none, -1 = permanent.")
                    .defineInRange("glowSecondsOnSpawn", new int[]{8, 8, 8, 8}[i], -1, 3600);
            announce = b.comment("Tell nearby players when this rank spawns.")
                    .define("announceSpawn", i == 3);
            fireworks = b.comment("Celebratory fireworks when a player defeats this rank.")
                    .define("fireworksOnDefeat", i >= 2);
            b.pop();

            b.push("gear");
            gearChance = b.comment("Chance per equipment slot to receive gear (only for hostile mobs).")
                    .defineInRange("gearChance", new double[]{0.5, 0.7, 0.9, 1.0}[i], 0.0, 1.0);
            gear.put(EquipmentSlot.HEAD, list(b, "helmets", GEAR_HEAD[i]));
            gear.put(EquipmentSlot.CHEST, list(b, "chestplates", GEAR_CHEST[i]));
            gear.put(EquipmentSlot.LEGS, list(b, "leggings", GEAR_LEGS[i]));
            gear.put(EquipmentSlot.FEET, list(b, "boots", GEAR_FEET[i]));
            gear.put(EquipmentSlot.MAINHAND, list(b, "weapons", GEAR_WEAPON[i]));
            trim = b.comment("Chance per armor piece to have a random VANILLA armor trim.")
                    .defineInRange("armorTrimChance", new double[]{0.10, 0.25, 0.50, 0.80}[i], 0.0, 1.0);
            enchantChance = b.comment("Chance per gear piece to be enchanted (plain vanilla enchantments).")
                    .defineInRange("enchantChance", new double[]{0.3, 0.5, 0.75, 1.0}[i], 0.0, 1.0);
            enchantCount = b.comment("How many enchantments an enchanted piece gets.")
                    .defineInRange("enchantCount", new int[]{1, 2, 3, 4}[i], 0, 8);
            maxEnchantLevel = b.comment("Highest enchantment level on ARMOR (also never above the vanilla max for that enchantment).")
                    .defineInRange("maxEnchantLevel", new int[]{1, 2, 2, 3}[i], 1, 5);
            maxProtectionLevel = b.comment("Highest level of the Protection-type armor enchantments (Protection, Fire, Blast, Projectile).")
                    .defineInRange("maxProtectionLevel", new int[]{1, 2, 2, 2}[i], 1, 4);
            weaponEnchantLevel = b.comment("Highest enchantment level on WEAPONS (also never above the vanilla max for that enchantment).")
                    .defineInRange("weaponEnchantLevel", new int[]{1, 2, 3, 5}[i], 1, 5);
            gearDropsMin = b.comment("Gear pieces dropped on a player kill, minimum. A drop is never two weapons: it is armor and/or one weapon.")
                    .defineInRange("gearDropsMin", new int[]{1, 1, 1, 1}[i], 0, 5);
            gearDropsMax = b.comment("Gear pieces dropped on a player kill, maximum.")
                    .defineInRange("gearDropsMax", new int[]{1, 1, 2, 2}[i], 0, 5);
            b.pop();

            b.push("ranged");
            rangedChance = b.comment("Chance this champion can shoot projectiles at its target.",
                    "ONLY Apex shoots (randomly wither skulls or large fireballs). Other ranks never shoot, whatever this is set to.")
                    .defineInRange("rangedAttackChance", new double[]{0.0, 0.0, 0.0, 0.75}[i], 0.0, 1.0);
            rangedCooldown = b.comment("Ticks between shots (20 = 1 second).")
                    .defineInRange("rangedCooldownTicks", new int[]{160, 120, 100, 100}[i], 5, 100000);
            b.pop();

            killLevels = b.comment("Minimum experience LEVELS given directly to the player who kills this rank.")
                    .defineInRange("killLevels", new int[]{1, 2, 5, 18}[i], 0, 1000);
            killLevelsMax = b.comment("Maximum experience LEVELS given to the killer (a random amount between min and max).")
                    .defineInRange("killLevelsMax", new int[]{3, 5, 8, 25}[i], 0, 1000);

            b.push("rewards");
            rewardRollsMin = b.comment("Guaranteed rolls on rewardPool for EVERY kill (minimum). Each roll drops one item.")
                    .defineInRange("rewardRollsMin", new int[]{1, 1, 1, 0}[i], 0, 64);
            rewardRollsMax = b.comment("Guaranteed rolls on rewardPool for every kill (maximum).")
                    .defineInRange("rewardRollsMax", new int[]{1, 1, 1, 0}[i], 0, 64);
            headChance = b.comment("Chance to drop the champion's own head (only zombie, skeleton, wither skeleton, creeper and piglin have one).")
                    .defineInRange("headDropChance", new double[]{0.0, 0.01, 0.02, 0.05}[i], 0.0, 1.0);
            rewardPool = b.comment("Weighted pool the guaranteed rolls pick from.",
                    "Format: item_id|weight|min|max  or  item_id|weight|min|max|enchant=level,enchant=level",
                    "Example: minecraft:shield|5|1|1|unbreaking=3,mending=1  (levels are capped at the vanilla max)")
                    .defineListAllowEmpty("rewardPool", java.util.Arrays.asList(REWARD_POOL[i]),
                            () -> "minecraft:diamond|5|1|2", o -> o instanceof String);
            guaranteedLoot = b.comment("Items that ALWAYS drop on every kill of this rank (every line drops).",
                    "Format: item_id|1.0|min|max  or  item_id|1.0|min|max|enchant=level,enchant=level")
                    .defineListAllowEmpty("guaranteedLoot", java.util.Arrays.asList(GUARANTEED_LOOT[i]),
                            () -> "minecraft:experience_bottle|1.0|2|4", o -> o instanceof String);
            bonusLoot = b.comment("Extra chance-based drops (on top of the guaranteed ones).",
                    "Format: item_id|chance|min|max  or  item_id|chance|min|max|enchant=level,enchant=level")
                    .defineListAllowEmpty("bonusLoot", java.util.Arrays.asList(BONUS_LOOT[i]),
                            () -> "minecraft:diamond|0.5|1|2", o -> o instanceof String);
            b.pop();

            b.pop();
        }

        private static ModConfigSpec.ConfigValue<List<? extends String>> list(ModConfigSpec.Builder b, String name, String[] def) {
            return b.defineListAllowEmpty(name, java.util.Arrays.asList(def),
                    () -> "minecraft:iron_ingot", o -> o instanceof String);
        }

        private static final String[][] GEAR_HEAD = {
                {"minecraft:leather_helmet", "minecraft:chainmail_helmet"},
                {"minecraft:iron_helmet"},
                {"minecraft:diamond_helmet"},
                {"minecraft:netherite_helmet"}};
        private static final String[][] GEAR_CHEST = {
                {"minecraft:leather_chestplate", "minecraft:chainmail_chestplate"},
                {"minecraft:iron_chestplate"},
                {"minecraft:diamond_chestplate"},
                {"minecraft:netherite_chestplate"}};
        private static final String[][] GEAR_LEGS = {
                {"minecraft:leather_leggings", "minecraft:chainmail_leggings"},
                {"minecraft:iron_leggings"},
                {"minecraft:diamond_leggings"},
                {"minecraft:netherite_leggings"}};
        private static final String[][] GEAR_FEET = {
                {"minecraft:leather_boots", "minecraft:chainmail_boots"},
                {"minecraft:iron_boots"},
                {"minecraft:diamond_boots"},
                {"minecraft:netherite_boots"}};
        private static final String[][] GEAR_WEAPON = {
                {"minecraft:stone_sword", "minecraft:stone_axe", "minecraft:iron_sword"},
                {"minecraft:iron_sword", "minecraft:iron_axe"},
                {"minecraft:diamond_sword", "minecraft:diamond_axe"},
                {"minecraft:netherite_sword", "minecraft:netherite_axe"}};
        private static final String[][] GUARANTEED_LOOT = {
                {"minecraft:experience_bottle|1.0|1|2"},
                {"minecraft:experience_bottle|1.0|2|3",
                        "minecraft:diamond|1.0|1|1"},
                {"minecraft:experience_bottle|1.0|2|4",
                        "minecraft:diamond|1.0|2|3",
                        "minecraft:golden_apple|1.0|1|1"},
                {"minecraft:diamond|1.0|2|6",
                        "minecraft:netherite_ingot|1.0|2|2",
                        "minecraft:enchanted_golden_apple|1.0|1|1",
                        "minecraft:experience_bottle|1.0|2|6"}};
        private static final String[][] REWARD_POOL = {
                {"minecraft:iron_ingot|12|1|2", "minecraft:gold_ingot|8|1|1", "minecraft:emerald|6|1|1",
                        "minecraft:cooked_beef|10|2|4",
                        "minecraft:shield|2|1|1|unbreaking=1", "minecraft:enchanted_book|1|1|1|unbreaking=1"},
                {"minecraft:diamond|3|1|1", "minecraft:emerald|8|1|2", "minecraft:golden_apple|3|1|1",
                        "minecraft:ender_pearl|5|1|1",
                        "minecraft:experience_bottle|6|1|2",
                        "minecraft:shield|3|1|1|unbreaking=2",
                        "minecraft:enchanted_book|1|1|1|unbreaking=2"},
                {"minecraft:diamond|8|1|1", "minecraft:netherite_scrap|1|1|1", "minecraft:golden_apple|5|1|1",
                        "minecraft:ender_pearl|5|1|2", "minecraft:experience_bottle|6|1|3",
                        "minecraft:shield|3|1|1|unbreaking=2,mending=1",
                        "minecraft:enchanted_book|2|1|1|unbreaking=3"},
                {"minecraft:diamond|8|1|3", "minecraft:netherite_scrap|2|1|1", "minecraft:netherite_ingot|1|1|1",
                        "minecraft:golden_apple|6|1|1", "minecraft:enchanted_golden_apple|1|1|1",
                        "minecraft:totem_of_undying|1|1|1",
                        "minecraft:shield|3|1|1|unbreaking=3,mending=1",
                        "minecraft:netherite_sword|1|1|1|sharpness=3,unbreaking=2",
                        "minecraft:netherite_pickaxe|1|1|1|efficiency=3,unbreaking=2",
                        "minecraft:enchanted_book|3|1|1|mending=1", "minecraft:enchanted_book|3|1|1|protection=4",
                        "minecraft:enchanted_book|3|1|1|sharpness=5", "minecraft:enchanted_book|3|1|1|unbreaking=3",
                        "minecraft:enchanted_book|3|1|1|efficiency=5", "minecraft:enchanted_book|3|1|1|fortune=3",
                        "minecraft:enchanted_book|3|1|1|looting=3", "minecraft:enchanted_book|3|1|1|power=5",
                        "minecraft:enchanted_book|3|1|1|feather_falling=4", "minecraft:enchanted_book|3|1|1|thorns=3"}};
        private static final String[][] BONUS_LOOT = {
                {"minecraft:iron_ingot|0.20|1|1"},
                {"minecraft:diamond|0.06|1|1", "minecraft:emerald|0.20|1|1", "minecraft:golden_apple|0.04|1|1"},
                {"minecraft:netherite_scrap|0.06|1|1", "minecraft:golden_apple|0.10|1|1",
                        "minecraft:ender_pearl|0.20|1|1"},
                {"minecraft:beacon|0.02|1|1", "minecraft:nether_star|0.02|1|1", "minecraft:elytra|0.01|1|1"}};
    }
}
