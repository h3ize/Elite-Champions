package com.elitechampions.config;

import com.elitechampions.champion.ChampionRank;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * All settings. File: config/elitechampions-common.toml. Also editable in-game via the Mods screen.
 */
public final class ChampionConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    // ---------------------------------------------------------------- general
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_SPAWN_TYPES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_ABILITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> TRIAL_EFFECTS;
    public static final ModConfigSpec.DoubleValue COMMON_WEIGHT;
    public static final ModConfigSpec.BooleanValue SHOW_NAMES;
    public static final ModConfigSpec.BooleanValue PARTICLES;
    public static final ModConfigSpec.BooleanValue ANNOUNCE;
    public static final ModConfigSpec.EnumValue<ChampionRank> ANNOUNCE_MIN_RANK;
    public static final ModConfigSpec.IntValue ANNOUNCE_RADIUS;
    public static final ModConfigSpec.IntValue GLOW_SECONDS;
    public static final ModConfigSpec.EnumValue<ChampionRank> GLOW_MIN_RANK;
    public static final ModConfigSpec.BooleanValue FIREWORKS;
    public static final ModConfigSpec.EnumValue<ChampionRank> FIREWORKS_MIN_RANK;
    public static final ModConfigSpec.BooleanValue DROP_STANDARD_GEAR;
    public static final ModConfigSpec.BooleanValue FIRE_RESISTANCE;
    public static final ModConfigSpec.DoubleValue FALL_DAMAGE_REDUCTION;

    // -------------------------------------------------------------- abilities
    public static final ModConfigSpec.IntValue VOLATILE_RADIUS;
    public static final ModConfigSpec.IntValue QUICK_FUSE_TICKS;
    public static final ModConfigSpec.DoubleValue LAST_STAND_THRESHOLD;
    public static final ModConfigSpec.DoubleValue SNIPER_DAMAGE_MULT;
    public static final ModConfigSpec.IntValue RAPID_FIRE_INTERVAL;
    public static final ModConfigSpec.IntValue PIERCING_LEVEL;
    public static final ModConfigSpec.DoubleValue DEADEYE_RANGE;
    public static final ModConfigSpec.DoubleValue SPRINTER_BONUS;
    public static final ModConfigSpec.DoubleValue BERSERKER_THRESHOLD;
    public static final ModConfigSpec.DoubleValue BERSERKER_MULT;
    public static final ModConfigSpec.IntValue INFECTIOUS_SECONDS;
    public static final ModConfigSpec.IntValue VENOM_SECONDS;
    public static final ModConfigSpec.IntValue WEB_SLOW_SECONDS;
    public static final ModConfigSpec.IntValue HUSK_HUNGER_SECONDS;
    public static final ModConfigSpec.DoubleValue POUNCING_BONUS;
    public static final ModConfigSpec.IntValue ENDERMAN_BLINK_INTERVAL;
    public static final ModConfigSpec.IntValue WITCH_INTERVAL;
    public static final ModConfigSpec.IntValue SLIME_SIZE_MULT;
    public static final ModConfigSpec.IntValue SLIME_MAX_SIZE;
    public static final ModConfigSpec.IntValue BLAZE_INTERVAL;
    public static final ModConfigSpec.IntValue BREEZE_INTERVAL;
    public static final ModConfigSpec.IntValue PHANTOM_INTERVAL;
    public static final ModConfigSpec.DoubleValue VINDICATOR_BONUS;

    // ------------------------------------------------------------------ ranks
    public static final Map<ChampionRank, RankSettings> RANKS = new EnumMap<>(ChampionRank.class);

    public static final ModConfigSpec SPEC;

    static {
        B.push("general");
        ENABLED = B.comment("Master switch.").define("enabled", true);
        WHITELIST = B.comment("Entity types that can spawn as champions.")
                .defineListAllowEmpty("whitelist", List.of(
                        "minecraft:creeper", "minecraft:skeleton", "minecraft:stray", "minecraft:bogged",
                        "minecraft:wither_skeleton", "minecraft:zombie", "minecraft:husk", "minecraft:drowned",
                        "minecraft:spider", "minecraft:cave_spider", "minecraft:enderman", "minecraft:witch",
                        "minecraft:slime", "minecraft:pillager", "minecraft:vindicator", "minecraft:blaze",
                        "minecraft:phantom", "minecraft:breeze"),
                        () -> "minecraft:zombie", o -> o instanceof String);
        EXCLUDED_SPAWN_TYPES = B.comment(
                        "MobSpawnType names that can NOT produce champions (case-insensitive).",
                        "Spawners are excluded by default so mob grinders are not clogged.",
                        "Values: NATURAL, CHUNK_GENERATION, SPAWNER, STRUCTURE, BREEDING, MOB_SUMMONED, JOCKEY, EVENT,",
                        "CONVERSION, REINFORCEMENT, TRIGGERED, BUCKET, SPAWN_ITEM, COMMAND, DISPENSER, PATROL, TRIAL_SPAWNER")
                .defineListAllowEmpty("excludedSpawnTypes",
                        List.of("SPAWNER", "TRIAL_SPAWNER", "BREEDING", "BUCKET", "REINFORCEMENT"),
                        () -> "SPAWNER", o -> o instanceof String);
        DISABLED_ABILITIES = B.comment("Ability ids to disable, e.g. \"creeper_volatile\". See README for the full list.")
                .defineListAllowEmpty("disabledAbilities", List.of(), () -> "creeper_volatile", o -> o instanceof String);
        COMMON_WEIGHT = B.comment("Relative weight of a normal (non-champion) mob. Defaults give 69 / 17.2 / 10.3 / 3.1 / 0.7 %.")
                .defineInRange("commonWeight", 69.0, 0.0, 100000.0);
        SHOW_NAMES = B.comment("Show a coloured nameplate such as 'Epic Zombie'.").define("showNames", true);
        PARTICLES = B.comment("Rank-coloured ambient particles.").define("particles", true);
        ANNOUNCE = B.comment("Tell nearby players when a high-rank champion spawns.").define("announce", true);
        ANNOUNCE_MIN_RANK = B.defineEnum("announceMinRank", ChampionRank.LEGENDARY);
        ANNOUNCE_RADIUS = B.defineInRange("announceRadius", 64, 8, 512);
        GLOW_SECONDS = B.comment("Seconds of Glowing on spawn for high ranks (0 disables).")
                .defineInRange("glowSeconds", 10, 0, 600);
        GLOW_MIN_RANK = B.defineEnum("glowMinRank", ChampionRank.LEGENDARY);
        FIREWORKS = B.comment("Celebratory fireworks when a high-rank champion dies.").define("fireworks", true);
        FIREWORKS_MIN_RANK = B.defineEnum("fireworksMinRank", ChampionRank.LEGENDARY);
        DROP_STANDARD_GEAR = B.comment("If true, standard (non-special) champion gear can drop with the vanilla 8.5% chance.")
                .define("dropStandardGear", false);
        FIRE_RESISTANCE = B.comment("Champions are immune to fire damage.").define("fireResistance", true);
        FALL_DAMAGE_REDUCTION = B.comment("Fraction of fall damage removed (0.75 = takes 25%).")
                .defineInRange("fallDamageReduction", 0.75, 0.0, 1.0);
        TRIAL_EFFECTS = B.comment("Pool of vanilla trial-chamber effects a champion can roll (chance set per rank).")
                .defineListAllowEmpty("trialEffects",
                        List.of("minecraft:wind_charged", "minecraft:weaving", "minecraft:infested"),
                        () -> "minecraft:weaving", o -> o instanceof String);
        B.pop();

        B.push("abilities");
        VOLATILE_RADIUS = B.comment("Creeper explosion radius (vanilla 3).").defineInRange("volatileRadius", 6, 1, 30);
        QUICK_FUSE_TICKS = B.comment("Creeper fuse length in ticks (vanilla 30).").defineInRange("quickFuseTicks", 15, 1, 30);
        LAST_STAND_THRESHOLD = B.comment("Health fraction below which Last Stand grants Speed.")
                .defineInRange("lastStandThreshold", 0.3, 0.05, 1.0);
        SNIPER_DAMAGE_MULT = B.defineInRange("sniperDamageMultiplier", 1.5, 1.0, 10.0);
        RAPID_FIRE_INTERVAL = B.comment("Bow attack interval in ticks (vanilla 20-40).").defineInRange("rapidFireInterval", 10, 2, 40);
        PIERCING_LEVEL = B.comment("Arrow pierce level for Piercing skeletons.").defineInRange("piercingLevel", 2, 1, 10);
        DEADEYE_RANGE = B.defineInRange("deadeyeRange", 16.0, 4.0, 64.0);
        SPRINTER_BONUS = B.comment("Extra movement speed fraction for Sprinter zombies.").defineInRange("sprinterBonus", 0.5, 0.0, 3.0);
        BERSERKER_THRESHOLD = B.defineInRange("berserkerThreshold", 0.5, 0.05, 1.0);
        BERSERKER_MULT = B.defineInRange("berserkerDamageMultiplier", 1.5, 1.0, 10.0);
        INFECTIOUS_SECONDS = B.defineInRange("infectiousSeconds", 6, 1, 120);
        VENOM_SECONDS = B.defineInRange("venomSeconds", 6, 1, 120);
        WEB_SLOW_SECONDS = B.defineInRange("webSlownessSeconds", 6, 1, 120);
        HUSK_HUNGER_SECONDS = B.defineInRange("huskHungerSeconds", 30, 1, 600);
        POUNCING_BONUS = B.defineInRange("pouncingSpeedBonus", 0.3, 0.0, 3.0);
        ENDERMAN_BLINK_INTERVAL = B.comment("Ticks between teleport attempts toward the target.").defineInRange("endermanBlinkInterval", 60, 10, 600);
        WITCH_INTERVAL = B.comment("Ticks between extra splash potions.").defineInRange("witchInterval", 40, 10, 600);
        SLIME_SIZE_MULT = B.defineInRange("slimeSizeMultiplier", 2, 1, 8);
        SLIME_MAX_SIZE = B.defineInRange("slimeMaxSize", 8, 1, 127);
        BLAZE_INTERVAL = B.comment("Ticks between extra fireballs.").defineInRange("blazeInterval", 30, 5, 600);
        BREEZE_INTERVAL = B.comment("Ticks between extra wind charges.").defineInRange("breezeInterval", 50, 10, 600);
        PHANTOM_INTERVAL = B.comment("Ticks between extra phantom bites when touching the target.").defineInRange("phantomInterval", 20, 5, 200);
        VINDICATOR_BONUS = B.comment("Extra speed AND damage fraction for Vindicators.").defineInRange("vindicatorBonus", 0.35, 0.0, 3.0);
        B.pop();

        B.push("ranks");
        for (ChampionRank rank : ChampionRank.champions()) {
            RANKS.put(rank, new RankSettings(rank));
        }
        B.pop();

        SPEC = B.build();
    }

    private ChampionConfig() {}

    public static boolean isSpawnTypeExcluded(MobSpawnType type) {
        for (String s : EXCLUDED_SPAWN_TYPES.get()) {
            if (s.equalsIgnoreCase(type.name())) return true;
        }
        return false;
    }

    // ===================================================================================

    public static final class RankSettings {
        private static final double[] WEIGHT = {0, 17.2, 10.3, 3.1, 0.7};
        private static final String[] ARMOR = {"", "minecraft:chainmail", "minecraft:iron", "minecraft:diamond", "minecraft:netherite"};
        private static final String[] WEAPON = {"", "minecraft:stone_sword", "minecraft:iron_sword", "minecraft:diamond_sword", "minecraft:netherite_sword"};
        private static final int[] ABILITIES = {0, 1, 1, 2, 3};
        private static final double[] SPECIAL_GEAR = {0, 0.05, 0.15, 0.30, 0.60};
        private static final double[] SPECIAL_DROP = {0, 0.15, 0.25, 0.35, 0.60};
        private static final double[] TRIAL = {0, 0.05, 0.10, 0.25, 0.40};
        private static final double[] ESSENCE = {0, 0.25, 0.50, 0.75, 1.00};
        private static final int[] ESSENCE_MAX = {0, 1, 1, 2, 3};

        public final ChampionRank rank;
        public final ModConfigSpec.DoubleValue weight;
        public final ModConfigSpec.IntValue health, armor, speed, damage, knockback, regen;
        public final ModConfigSpec.IntValue abilityCount;
        public final ModConfigSpec.ConfigValue<String> armorMaterial, weapon;
        public final ModConfigSpec.DoubleValue specialGearChance, specialDropChance, trialEffectChance, essenceChance;
        public final ModConfigSpec.IntValue essenceMin, essenceMax;
        public final ModConfigSpec.ConfigValue<List<? extends String>> bonusLoot;

        RankSettings(ChampionRank r) {
            this.rank = r;
            int i = r.ordinal();
            B.push(r.id());
            weight = B.comment("Relative spawn weight.").defineInRange("weight", WEIGHT[i], 0.0, 100000.0);
            health = level("healthLevel", "Max health scaling, 0-10 (5 = default, 10 = double, 0 = off).");
            armor = level("armorLevel", "Armor scaling, 0-10.");
            speed = level("speedLevel", "Movement speed scaling, 0-10.");
            damage = level("damageLevel", "Attack damage scaling, 0-10.");
            knockback = level("knockbackResistanceLevel", "Knockback resistance scaling, 0-10.");
            regen = level("regenerationLevel", "Health regeneration scaling, 0-10.");
            abilityCount = B.comment("How many abilities this rank rolls (limited by what the mob type supports).")
                    .defineInRange("abilityCount", ABILITIES[i], 0, 6);
            armorMaterial = B.comment("Standard armor set, as namespace:prefix (e.g. minecraft:iron -> iron_helmet...).")
                    .define("armorMaterial", ARMOR[i]);
            weapon = B.comment("Standard melee weapon item for zombie-type champions. Empty = none.")
                    .define("weapon", WEAPON[i]);
            specialGearChance = B.comment("Chance per equipment slot to roll a SPECIAL (named, enchanted, attribute-boosted) item.")
                    .defineInRange("specialGearChance", SPECIAL_GEAR[i], 0.0, 1.0);
            specialDropChance = B.comment("Drop chance of special gear on player kill.")
                    .defineInRange("specialDropChance", SPECIAL_DROP[i], 0.0, 1.0);
            trialEffectChance = B.comment("Chance to roll one trial-chamber effect (see general.trialEffects).")
                    .defineInRange("trialEffectChance", TRIAL[i], 0.0, 1.0);
            essenceChance = B.comment("Chance to drop Champion Essence on player kill.")
                    .defineInRange("essenceChance", ESSENCE[i], 0.0, 1.0);
            essenceMin = B.defineInRange("essenceMin", 1, 0, 64);
            essenceMax = B.defineInRange("essenceMax", ESSENCE_MAX[i], 0, 64);
            bonusLoot = B.comment("Extra drops on player kill, format \"item_id;chance;min;max\".")
                    .defineListAllowEmpty("bonusLoot", defaultLoot(r), () -> "minecraft:diamond;0.1;1;1", o -> o instanceof String);
            B.pop();
        }

        private static ModConfigSpec.IntValue level(String name, String comment) {
            return B.comment(comment).defineInRange(name, 5, 0, 10);
        }

        private static List<String> defaultLoot(ChampionRank r) {
            return switch (r) {
                case UNCOMMON -> List.of("minecraft:gold_nugget;0.5;1;4");
                case RARE -> List.of("minecraft:emerald;0.4;1;2");
                case EPIC -> List.of("minecraft:diamond;0.3;1;2");
                case LEGENDARY -> List.of("minecraft:netherite_scrap;0.25;1;2", "minecraft:enchanted_golden_apple;0.10;1;1");
                default -> List.of();
            };
        }

        private static double scale(ModConfigSpec.IntValue v) {
            return v.get() / 5.0;
        }

        public double healthBonus() { return rank.baseHealth() * scale(health); }
        public double armorBonus() { return rank.baseArmor() * scale(armor); }
        public double speedBonus() { return rank.baseSpeed() * scale(speed); }
        public double damageBonus() { return rank.baseDamage() * scale(damage); }
        public double knockbackBonus() { return Math.min(1.0, rank.baseKnockback() * scale(knockback)); }
        public double regenPerSecond() { return rank.baseRegen() * scale(regen); }
    }
}
