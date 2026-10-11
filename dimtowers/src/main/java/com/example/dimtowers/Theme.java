package com.example.dimtowers;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

public enum Theme {
    OVERWORLD("overworld",
            Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES, Blocks.CHISELED_DEEPSLATE, Blocks.CHISELED_TUFF_BRICKS,
            Blocks.POLISHED_DEEPSLATE, Blocks.TUFF_BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.OXIDIZED_CUT_COPPER,
            Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILE_WALL,
            Blocks.COPPER_BULB.defaultBlockState().setValue(BlockStateProperties.LIT, true),
            Blocks.TINTED_GLASS, Blocks.SOUL_LANTERN, b("zombie", "skeleton", "spider"),
            List.of(EntityType.ZOMBIE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                    EntityType.CREEPER, EntityType.WITCH, EntityType.BOGGED, EntityType.BREEZE),
            "ward", "gray", "skull", "skull"),
    NETHER("nether",
            Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS,
            Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRYING_OBSIDIAN,
            Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICK_WALL,
            Blocks.SHROOMLIGHT.defaultBlockState(),
            Blocks.RED_STAINED_GLASS, Blocks.LANTERN, b("boss", "blaze"),
            List.of(EntityType.ZOMBIFIED_PIGLIN, EntityType.SKELETON, EntityType.MAGMA_CUBE, EntityType.BLAZE,
                    EntityType.WITHER_SKELETON),
            "rib", "red", "piglin", "burn"),
    END("end",
            Blocks.END_STONE_BRICKS, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_BLOCK,
            Blocks.END_STONE_BRICKS, Blocks.PURPUR_BLOCK, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN,
            Blocks.END_STONE_BRICK_STAIRS, Blocks.END_STONE_BRICK_SLAB, Blocks.END_STONE_BRICK_WALL,
            Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(),
            Blocks.PURPLE_STAINED_GLASS, Blocks.SOUL_LANTERN, b("ender", "boss"),
            List.of(EntityType.ENDERMITE, EntityType.ENDERMITE, EntityType.SHULKER, EntityType.ENDERMAN, EntityType.ENDERMAN),
            "spire", "purple", "globe", "plenty"),
    PIGLIN("piglin",
            Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE,
            Blocks.GILDED_BLACKSTONE, Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE, Blocks.GOLD_BLOCK,
            Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_WALL,
            Blocks.SHROOMLIGHT.defaultBlockState(),
            Blocks.ORANGE_STAINED_GLASS, Blocks.LANTERN, b("piglin", "blaze"),
            List.of(EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.BLAZE, EntityType.HOGLIN, EntityType.PIGLIN_BRUTE),
            "snout", "yellow", "piglin", "heart"),
    FROZEN("frozen",
            Blocks.QUARTZ_BRICKS, Blocks.PACKED_ICE, Blocks.QUARTZ_PILLAR, Blocks.CHISELED_QUARTZ_BLOCK,
            Blocks.SMOOTH_QUARTZ, Blocks.PACKED_ICE, Blocks.STONE_BRICKS, Blocks.BLUE_ICE,
            Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_SLAB, Blocks.DIORITE_WALL,
            Blocks.SEA_LANTERN.defaultBlockState(),
            Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.SOUL_LANTERN, b("skeleton", "vindicator"),
            List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.STRAY, EntityType.STRAY, EntityType.WITCH),
            "coast", "light_blue", "globe", "snort"),
    DESERT("desert",
            Blocks.CUT_SANDSTONE, Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE,
            Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SANDSTONE, Blocks.GOLD_BLOCK,
            Blocks.SANDSTONE_STAIRS, Blocks.SANDSTONE_SLAB, Blocks.SANDSTONE_WALL,
            Blocks.GLOWSTONE.defaultBlockState(),
            Blocks.YELLOW_STAINED_GLASS, Blocks.LANTERN, b("zombie", "spider"),
            List.of(EntityType.HUSK, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                    EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.WITCH),
            "dune", "yellow", "creeper", "miner"),
    OAK("oak",
            Blocks.OAK_PLANKS, Blocks.STRIPPED_OAK_LOG, Blocks.OAK_LOG, Blocks.DARK_OAK_PLANKS,
            Blocks.SPRUCE_PLANKS, Blocks.OAK_PLANKS, Blocks.COBBLESTONE, Blocks.MOSS_BLOCK,
            Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.OAK_FENCE,
            Blocks.OCHRE_FROGLIGHT.defaultBlockState(),
            Blocks.GREEN_STAINED_GLASS, Blocks.LANTERN, b("vindicator", "zombie"),
            List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER,
                    EntityType.WITCH),
            "raiser", "green", "flower", "plenty"),
    PILLAGER("pillager",
            Blocks.DARK_OAK_PLANKS, Blocks.COBBLESTONE, Blocks.DARK_OAK_LOG, Blocks.SPRUCE_PLANKS,
            Blocks.COBBLESTONE, Blocks.DARK_OAK_PLANKS, Blocks.STONE_BRICKS, Blocks.IRON_BLOCK,
            Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_FENCE,
            Blocks.JACK_O_LANTERN.defaultBlockState(),
            Blocks.GRAY_STAINED_GLASS, Blocks.SOUL_LANTERN, b("vindicator"),
            List.of(EntityType.PILLAGER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.WITCH,
                    EntityType.VEX, EntityType.EVOKER, EntityType.RAVAGER),
            "sentry", "gray", "skull", "arms_up"),
    DARK_FOREST("dark_forest",
            Blocks.DARK_OAK_PLANKS, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_LOG, Blocks.SPRUCE_PLANKS,
            Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.COBBLESTONE, Blocks.RED_WOOL,
            Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_FENCE,
            Blocks.SHROOMLIGHT.defaultBlockState(),
            Blocks.PURPLE_STAINED_GLASS, Blocks.SOUL_LANTERN, b("vindicator", "spider"),
            List.of(EntityType.SILVERFISH, EntityType.SPIDER, EntityType.VINDICATOR, EntityType.PILLAGER,
                    EntityType.WITCH, EntityType.VEX, EntityType.EVOKER),
            "vex", "purple", "skull", "howl"),
    JUNGLE("jungle",
            Blocks.MOSSY_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.JUNGLE_LOG, Blocks.JUNGLE_PLANKS,
            Blocks.MOSSY_COBBLESTONE, Blocks.JUNGLE_PLANKS, Blocks.MOSSY_COBBLESTONE, Blocks.EMERALD_BLOCK,
            Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.MOSSY_COBBLESTONE_WALL,
            Blocks.VERDANT_FROGLIGHT.defaultBlockState(),
            Blocks.LIME_STAINED_GLASS, Blocks.LANTERN, b("spider", "slime"),
            List.of(EntityType.ZOMBIE, EntityType.SLIME, EntityType.SKELETON, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                    EntityType.CREEPER),
            "wild", "lime", "creeper", "brewer"),
    SWAMP("swamp",
            Blocks.MUD_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.MANGROVE_LOG, Blocks.MANGROVE_PLANKS,
            Blocks.MUD_BRICKS, Blocks.MANGROVE_PLANKS, Blocks.PACKED_MUD, Blocks.MOSS_BLOCK,
            Blocks.MUD_BRICK_STAIRS, Blocks.MUD_BRICK_SLAB, Blocks.MUD_BRICK_WALL,
            Blocks.VERDANT_FROGLIGHT.defaultBlockState(),
            Blocks.GREEN_STAINED_GLASS, Blocks.SOUL_LANTERN, b("slime", "drowned"),
            List.of(EntityType.SLIME, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITCH, EntityType.BOGGED,
                    EntityType.DROWNED),
            "host", "green", "flower", "mourner"),
    TAIGA("taiga",
            Blocks.SPRUCE_PLANKS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_LOG, Blocks.DARK_OAK_PLANKS,
            Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.COBBLESTONE, Blocks.GOLD_BLOCK,
            Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE,
            Blocks.SHROOMLIGHT.defaultBlockState(),
            Blocks.ORANGE_STAINED_GLASS, Blocks.LANTERN, b("zombie", "skeleton"),
            List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.STRAY, EntityType.SPIDER,
                    EntityType.WITCH),
            "shaper", "orange", "globe", "shelter"),
    BADLANDS("badlands",
            Blocks.TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
            Blocks.SMOOTH_RED_SANDSTONE, Blocks.BROWN_TERRACOTTA, Blocks.RED_SANDSTONE, Blocks.GOLD_BLOCK,
            Blocks.RED_SANDSTONE_STAIRS, Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE_WALL,
            Blocks.GLOWSTONE.defaultBlockState(),
            Blocks.ORANGE_STAINED_GLASS, Blocks.LANTERN, b("zombie", "boss"),
            List.of(EntityType.HUSK, EntityType.SKELETON, EntityType.CAVE_SPIDER, EntityType.SPIDER,
                    EntityType.CREEPER, EntityType.WITCH),
            "bolt", "orange", "skull", "angler"),
    OCEAN("ocean",
            Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE, Blocks.DARK_PRISMARINE, Blocks.DARK_PRISMARINE,
            Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.PRISMARINE, Blocks.SEA_LANTERN,
            Blocks.PRISMARINE_BRICK_STAIRS, Blocks.PRISMARINE_BRICK_SLAB, Blocks.PRISMARINE_WALL,
            Blocks.SEA_LANTERN.defaultBlockState(),
            Blocks.CYAN_STAINED_GLASS, Blocks.SOUL_LANTERN, b("drowned"),
            List.of(EntityType.DROWNED, EntityType.ZOMBIE, EntityType.DROWNED, EntityType.SKELETON, EntityType.WITCH,
                    EntityType.DROWNED),
            "tide", "cyan", "globe", "angler"),
    DEEP_DARK("deep_dark",
            Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES,
            Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE, Blocks.SCULK,
            Blocks.DEEPSLATE_BRICK_STAIRS, Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICK_WALL,
            Blocks.VERDANT_FROGLIGHT.defaultBlockState(),
            Blocks.TINTED_GLASS, Blocks.SOUL_LANTERN, b("ender", "boss"),
            List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.WITCH,
                    EntityType.BREEZE, EntityType.WITHER_SKELETON),
            "silence", "black", "skull", "howl"),
    CHERRY("cherry",
            Blocks.CHERRY_PLANKS, Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD,
            Blocks.CHERRY_PLANKS, Blocks.PINK_TERRACOTTA, Blocks.POLISHED_ANDESITE, Blocks.PINK_CONCRETE,
            Blocks.CHERRY_STAIRS, Blocks.CHERRY_SLAB, Blocks.CHERRY_FENCE,
            Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(),
            Blocks.PINK_STAINED_GLASS, Blocks.LANTERN, b("vindicator", "slime"),
            List.of(EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SKELETON, EntityType.WITCH, EntityType.VINDICATOR),
            "flow", "pink", "flower", "heart"),
    SAVANNA("savanna",
            Blocks.ACACIA_PLANKS, Blocks.ORANGE_TERRACOTTA, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG,
            Blocks.ACACIA_PLANKS, Blocks.TERRACOTTA, Blocks.COBBLESTONE, Blocks.GOLD_BLOCK,
            Blocks.ACACIA_STAIRS, Blocks.ACACIA_SLAB, Blocks.ACACIA_FENCE,
            Blocks.OCHRE_FROGLIGHT.defaultBlockState(),
            Blocks.ORANGE_STAINED_GLASS, Blocks.LANTERN, b("zombie", "skeleton"),
            List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER,
                    EntityType.WITCH),
            "dune", "orange", "creeper", "sheaf"),
    CRIMSON("crimson",
            Blocks.CRIMSON_PLANKS, Blocks.NETHER_BRICKS, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_STEM,
            Blocks.CRIMSON_PLANKS, Blocks.RED_NETHER_BRICKS, Blocks.BLACKSTONE, Blocks.NETHER_WART_BLOCK,
            Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_FENCE,
            Blocks.SHROOMLIGHT.defaultBlockState(),
            Blocks.RED_STAINED_GLASS, Blocks.LANTERN, b("piglin", "blaze"),
            List.of(EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.MAGMA_CUBE, EntityType.HOGLIN,
                    EntityType.PIGLIN_BRUTE),
            "eye", "red", "piglin", "burn"),
    WARPED("warped",
            Blocks.WARPED_PLANKS, Blocks.STRIPPED_WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_STEM,
            Blocks.WARPED_PLANKS, Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE, Blocks.WARPED_WART_BLOCK,
            Blocks.WARPED_STAIRS, Blocks.WARPED_SLAB, Blocks.WARPED_FENCE,
            Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(),
            Blocks.CYAN_STAINED_GLASS, Blocks.SOUL_LANTERN, b("ender", "boss"),
            List.of(EntityType.ENDERMITE, EntityType.ZOMBIFIED_PIGLIN, EntityType.SKELETON, EntityType.ENDERMAN,
                    EntityType.ENDERMAN),
            "wayfinder", "cyan", "skull", "scrape");

    public final String id;
    public final BlockState wall, wallAlt, pillar, trim, floor, floorAlt, plinth, accent, light, glass,
            lanternHanging, lanternStanding;
    public final Block stairs, slab, wallPiece;
    /** Entity type ids the boss Trial Spawner can pick from. */
    public final String[] bosses;
    /** Entity types the per-floor monster spawners can pick from. */
    public final List<EntityType<?>> mobs;
    /** Smithing-trim pattern id (also names the template item), banner colour/pattern and pottery sherd. */
    public final String trimPattern, bannerColor, bannerPattern, sherd;
    /** Nether-style themes search for ground inside the cavern layer instead of on the surface. */
    public final boolean nether;

    Theme(String id, Block wall, Block wallAlt, Block pillar, Block trim, Block floor, Block floorAlt,
          Block plinth, Block accent, Block stairs, Block slab, Block wallPiece, BlockState light,
          Block glass, Block lantern, String[] bosses, List<EntityType<?>> mobs,
          String trimPattern, String bannerColor, String bannerPattern, String sherd) {
        this.id = id;
        this.wall = wall.defaultBlockState();
        this.wallAlt = wallAlt.defaultBlockState();
        this.pillar = pillar.defaultBlockState();
        this.trim = trim.defaultBlockState();
        this.floor = floor.defaultBlockState();
        this.floorAlt = floorAlt.defaultBlockState();
        this.plinth = plinth.defaultBlockState();
        this.accent = accent.defaultBlockState();
        this.stairs = stairs;
        this.slab = slab;
        this.wallPiece = wallPiece;
        this.light = light;
        this.glass = glass.defaultBlockState();
        this.lanternStanding = lantern.defaultBlockState();
        this.lanternHanging = lantern.defaultBlockState().setValue(LanternBlock.HANGING, true);
        this.bosses = bosses;
        this.mobs = mobs;
        this.trimPattern = trimPattern;
        this.bannerColor = bannerColor;
        this.bannerPattern = bannerPattern;
        this.sherd = sherd;
        this.nether = id.equals("nether") || id.equals("piglin") || id.equals("crimson") || id.equals("warped");
    }

    /** Boss pool helper: b("zombie") -> dimtowers:tower_zombie. */
    private static String[] b(String... names) {
        String[] out = new String[names.length];
        for (int i = 0; i < names.length; i++) out[i] = "dimtowers:tower_" + names[i];
        return out;
    }

    /** Tag a boss carries so its minion waves and abilities know which tower it came from. */
    public String bossTag() { return "dt_theme_" + id; }

    /** Mob list ordered weak to strong. Floor 0 uses the weak half, floors 1-2 nearly everything, floor 3 the strong half, floor 4 the top two. */
    public List<EntityType<?>> mobsForFloor(int k) {
        int n = mobs.size();
        int half = (n + 1) / 2;
        return switch (k) {
            case 0 -> mobs.subList(0, Math.max(2, half));
            case 1 -> mobs.subList(0, Math.min(n, half + 1));
            case 2 -> mobs;
            case 3 -> mobs.subList(n - Math.max(2, half), n);
            default -> mobs.subList(n - 2, n);
        };
    }

    /** Mobs a boss may summon in a wave: the tower's own list, minus things that make bad summons. */
    public List<EntityType<?>> waveMobs() {
        List<EntityType<?>> out = new ArrayList<>();
        for (EntityType<?> t : mobs) if (t != EntityType.SHULKER && t != EntityType.RAVAGER) out.add(t);
        return out;
    }

    public static Theme byName(String n) {
        for (Theme t : values()) if (t.id.equals(n)) return t;
        return OVERWORLD;
    }
}
