package com.example.dimtowers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TowerPiece extends StructurePiece {
    /** Distance between floors. */
    static final int H = 12;
    /** Spawner floors below the boss arena. */
    static final int FLOORS = 5;
    /** Outer half-width of the shell (tower is 31 wide, ~35 with buttresses). */
    static final int R = 15;
    /** Half-width of the usable interior (shell is two blocks thick). */
    static final int I = R - 2;
    /** Offset of the four interior pillars from the centre. */
    static final int PIL = 7;
    /** Window / wall-bay centres along each face. */
    static final int[] WIN = {-12, -6, 0, 6, 12};
    /** Interior and exterior pilaster positions along each face (between the windows). */
    static final int[] PILASTER = {-9, -3, 3, 9};
    /** Prop spots on every floor (x, z): clear of the stairs, chests, pillars and vault alcove. */
    static final int[][] SPOTS = {{11, 4}, {11, -4}, {-11, 4}, {-11, -4}, {11, 7}, {11, -7}, {-11, 7}, {-11, -7}};
    /** Every floor is a Trial Spawner room (clear it for a key). */
    static final int[] TRIAL_TOTAL = {6, 8, 10, 12, 6};
    static final int[] TRIAL_SIMUL = {2, 3, 4, 5, 3};
    /** Cooldown after a trial spawner is beaten, in ticks (40 = 2 seconds). */
    static final int SPAWNER_COOLDOWN = 40;
    /** The boss spawner stays dormant for a long time after the boss dies (1200000 ticks = ~16 hours). */
    static final int BOSS_COOLDOWN = 1200000;
    static final String[] ARMOR_TIER = {"", "chainmail", "iron", "diamond", "netherite"};
    static final String[] TOOL_TIER = {"wooden", "stone", "iron", "diamond", "netherite"};
    /** Armour-stand gear: no netherite, ever. Duplicates make the common sets more likely. */
    static final String[] STAND_ARMOR = {"leather", "chainmail", "golden", "golden", "iron", "iron", "iron", "diamond", "diamond"};
    static final String[] ARMOR_SLOTS = {"boots", "leggings", "chestplate", "helmet"};
    static final String[] STAND_TOOLS = {"iron_sword", "iron_axe", "iron_pickaxe", "iron_shovel", "iron_hoe", "stone_sword",
            "stone_axe", "stone_pickaxe", "golden_sword", "golden_axe", "golden_pickaxe", "diamond_sword", "diamond_axe",
            "diamond_pickaxe", "diamond_shovel", "trident", "bow"};
    static boolean has(int[] arr, int v) { for (int a : arr) if (a == v) return true; return false; }
    static final Set<EntityType<?>> ARMORED = Set.<EntityType<?>>of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.WITHER_SKELETON, EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN, EntityType.PILLAGER, EntityType.VINDICATOR);

    private final String themeId;
    private final int cx, cz, s0;

    public TowerPiece(String themeId, int cx, int cz, int s0) {
        super(DimensionalTowers.TOWER_PIECE.get(), 0,
                new BoundingBox(cx - (R + 4), s0 - 60, cz - (R + 4), cx + (R + 4), s0 + FLOORS * H + 24, cz + (R + 4)));
        this.themeId = themeId; this.cx = cx; this.cz = cz; this.s0 = s0;
    }

    public TowerPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(DimensionalTowers.TOWER_PIECE.get(), tag);
        this.themeId = tag.getString("Theme");
        this.cx = tag.getInt("CX");
        this.cz = tag.getInt("CZ");
        this.s0 = tag.getInt("S0");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putString("Theme", themeId);
        tag.putInt("CX", cx);
        tag.putInt("CZ", cz);
        tag.putInt("S0", s0);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator gen, RandomSource rnd,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        new Builder(level, rnd, box, Theme.byName(themeId)).build();
    }

    private static final List<String> COMMON_LOOT = List.of(
            "chests/desert_pyramid", "chests/jungle_temple", "chests/simple_dungeon", "chests/abandoned_mineshaft",
            "chests/stronghold_corridor", "chests/stronghold_crossing", "chests/stronghold_library",
            "chests/village/village_weaponsmith", "chests/village/village_armorer", "chests/village/village_toolsmith",
            "chests/woodland_mansion", "chests/pillager_outpost", "chests/igloo_chest", "chests/shipwreck_treasure",
            "chests/underwater_ruin_big", "chests/buried_treasure", "chests/trial_chambers/reward");
    private static final List<String> OVERWORLD_LOOT = List.of("chests/ancient_city", "chests/ancient_city");
    private static final List<String> NETHER_LOOT = List.of("chests/bastion_treasure", "chests/bastion_other",
            "chests/bastion_bridge", "chests/bastion_hoglin_stable", "chests/nether_bridge", "chests/ruined_portal");
    private static final List<String> ILLAGER_LOOT = List.of("chests/pillager_outpost", "chests/pillager_outpost",
            "chests/woodland_mansion", "chests/village/village_weaponsmith", "chests/village/village_armorer");
    private static final List<String> END_LOOT = List.of("chests/ancient_city", "chests/woodland_mansion",
            "chests/bastion_treasure", "chests/stronghold_library");

    private final class Builder {
        final WorldGenLevel level;
        final RandomSource rnd;
        final BoundingBox box;
        final Theme t;
        final BlockState air = Blocks.AIR.defaultBlockState();
        final BlockState chain = Blocks.CHAIN.defaultBlockState();
        final BlockState bookshelf = Blocks.BOOKSHELF.defaultBlockState();
        final BlockState barrel = Blocks.BARREL.defaultBlockState();
        final BlockState candle = Blocks.CANDLE.defaultBlockState()
                .setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, Boolean.TRUE);
        final BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        final int yMax;

        Builder(WorldGenLevel level, RandomSource rnd, BoundingBox box, Theme t) {
            this.level = level; this.rnd = rnd; this.box = box; this.t = t;
            this.yMax = level.getMinBuildHeight() + level.dimensionType().logicalHeight() - 2;
        }

        int s(int k) { return s0 + k * H; }
        int abs(int v) { return Math.abs(v); }

        void set(int dx, int y, int dz, BlockState st) {
            int x = cx + dx, z = cz + dz;
            if (y > yMax || !box.isInside(x, y, z)) return;
            level.setBlock(m.set(x, y, z), st, 2);
        }
        void clr(int dx, int y, int dz) { set(dx, y, dz, air); }
        BlockState at(int dx, int y, int dz) { return level.getBlockState(new BlockPos(cx + dx, y, cz + dz)); }
        boolean colInside(int dx, int dz) { return box.isInside(cx + dx, box.minY(), cz + dz); }

        /** Face-relative set. face 0=south(+z) 1=north(-z) 2=east(+x) 3=west(-x); tt = along the wall, r = distance from centre. */
        void fs(int face, int tt, int r, int y, BlockState st) {
            switch (face) {
                case 0 -> set(tt, y, r, st);
                case 1 -> set(tt, y, -r, st);
                case 2 -> set(r, y, tt, st);
                default -> set(-r, y, tt, st);
            }
        }

        BlockState wallBlock() { return rnd.nextInt(7) == 0 ? t.wallAlt : t.wall; }

        /** Compass-rose floor with a diamond lattice and a trim border against the walls. */
        BlockState floorAt(int dx, int dz) {
            int r = Math.max(abs(dx), abs(dz));
            int man = abs(dx) + abs(dz);
            if (r == I) return t.trim;
            if (dx == 0 || dz == 0) return t.floorAlt;
            if (man % 6 == 0) return t.floorAlt;
            return t.floor;
        }

        BlockState carpet() {
            return switch (t) {
                case NETHER, DEEP_DARK -> Blocks.BLACK_CARPET.defaultBlockState();
                case END, DARK_FOREST -> Blocks.PURPLE_CARPET.defaultBlockState();
                case CHERRY -> Blocks.PINK_CARPET.defaultBlockState();
                case OCEAN, WARPED -> Blocks.CYAN_CARPET.defaultBlockState();
                case CRIMSON -> Blocks.RED_CARPET.defaultBlockState();
                case OAK, JUNGLE, SWAMP -> Blocks.GREEN_CARPET.defaultBlockState();
                case PILLAGER -> Blocks.GRAY_CARPET.defaultBlockState();
                case TAIGA, BADLANDS, SAVANNA -> Blocks.ORANGE_CARPET.defaultBlockState();
                default -> Blocks.RED_CARPET.defaultBlockState();
            };
        }

        /** Second carpet colour, used for rug borders and runner edges. */
        BlockState carpetAlt() {
            return switch (t) {
                case NETHER, DEEP_DARK -> Blocks.GRAY_CARPET.defaultBlockState();
                case END, DARK_FOREST -> Blocks.MAGENTA_CARPET.defaultBlockState();
                case CHERRY -> Blocks.WHITE_CARPET.defaultBlockState();
                case OCEAN -> Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
                case WARPED -> Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
                case CRIMSON -> Blocks.BLACK_CARPET.defaultBlockState();
                case OAK, JUNGLE, SWAMP -> Blocks.LIME_CARPET.defaultBlockState();
                case PILLAGER -> Blocks.BLACK_CARPET.defaultBlockState();
                case TAIGA, BADLANDS, SAVANNA -> Blocks.YELLOW_CARPET.defaultBlockState();
                default -> Blocks.WHITE_CARPET.defaultBlockState();
            };
        }

        BlockState stairBlock(Direction facing, boolean upsideDown) {
            BlockState st = t.stairs.defaultBlockState().setValue(StairBlock.FACING, facing);
            return upsideDown ? st.setValue(BlockStateProperties.HALF, Half.TOP) : st;
        }
        BlockState slabTop() { return t.slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP); }
        Direction dirOf(int ddx, int ddz) {
            if (ddx != 0) return ddx > 0 ? Direction.EAST : Direction.WEST;
            return ddz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        /** Direction pointing from an outer cell toward the nearest wall face (used for corbels). */
        Direction towardWall(int dx, int dz) {
            return abs(dx) >= abs(dz) ? (dx > 0 ? Direction.WEST : Direction.EAST) : (dz > 0 ? Direction.NORTH : Direction.SOUTH);
        }

        ResourceKey<LootTable> loot() {
            ArrayList<String> pool = new ArrayList<>(COMMON_LOOT);
            switch (t) {
                case NETHER, PIGLIN, CRIMSON, WARPED -> pool.addAll(NETHER_LOOT);
                case END -> pool.addAll(END_LOOT);
                case PILLAGER, DARK_FOREST -> pool.addAll(ILLAGER_LOOT);
                default -> pool.addAll(OVERWORLD_LOOT);
            }
            return key(pool.get(rnd.nextInt(pool.size())));
        }
        /** Theme loot for the treasury floor, the four top-floor chests. */
        ResourceKey<LootTable> treasureKey() {
            return ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(DimensionalTowers.MOD_ID, "chests/treasure_" + t.id));
        }
        ResourceKey<LootTable> barrelKey() {
            return ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(DimensionalTowers.MOD_ID, "chests/barrel"));
        }
        ResourceKey<LootTable> key(String p) {
            return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace(p));
        }

        /** A single chest with the given loot table, facing the room. */
        void chestAt(BlockPos p, Direction f, ResourceKey<LootTable> lootKey) {
            if (p.getY() > yMax || !box.isInside(p)) return;
            level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, f), 2);
            RandomizableContainer.setBlockEntityLootTable(level, rnd, p, lootKey);
        }

        /** Wall-centre chest shrine: pillars either side, a trim lintel and floor lanterns. */
        void chestShrine(int face, int b, ResourceKey<LootTable> lootKey) {
            for (int y = b + 1; y <= b + 3; y++) { fs(face, -1, I, y, t.pillar); fs(face, 1, I, y, t.pillar); }
            fs(face, 0, I, b + 3, t.trim);
            fs(face, -1, I - 1, b + 1, t.lanternStanding);
            fs(face, 1, I - 1, b + 1, t.lanternStanding);
            chestAt(pfs(face, 0, I, b + 1), inward(face), lootKey);
        }

        /** Barrel in a wall nook, lid facing the room, filled from the early-game barrel loot table. */
        void barrelAt(int face, int tt, int y) {
            BlockPos p = pfs(face, tt, I, y);
            if (y > yMax || !box.isInside(p)) return;
            level.setBlock(p, barrel.setValue(BlockStateProperties.FACING, inward(face)), 2);
            RandomizableContainer.setBlockEntityLootTable(level, rnd, p, barrelKey());
        }

        void blockEntityNbt(BlockPos pos, String snbt) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return;
            try {
                be.loadWithComponents(TagParser.parseTag(snbt), level.registryAccess());
                be.setChanged();
            } catch (Exception ignored) { }
        }

        // ------------------------------------------------------------------ spawners

        String weaponFor(EntityType<?> type, int k) {
            if (type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED) return "bow";
            if (type == EntityType.PILLAGER) return "crossbow";
            if (type == EntityType.VINDICATOR) return TOOL_TIER[k] + "_axe";
            if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED
                    || type == EntityType.WITHER_SKELETON) return TOOL_TIER[k] + "_sword";
            if (type == EntityType.ZOMBIFIED_PIGLIN || type == EntityType.PIGLIN) return "golden_sword";
            if (type == EntityType.PIGLIN_BRUTE) return "golden_axe";
            return null;
        }

        /**
         * Spawn-data entity for a floor mob. Gear, potion effects and (on the top floor) extra health scale with height.
         * Because this is more than a bare {id:...}, the spawner will NOT call finalizeSpawn, so weapons are set here.
         */
        String mobNbt(EntityType<?> type, int k) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            StringBuilder sb = new StringBuilder("{id:'" + id + "',Tags:['dt_floor']");
            if (k >= 1 && ARMORED.contains(type)) {
                String a = "minecraft:" + ARMOR_TIER[k];
                sb.append(",ArmorItems:[{id:'").append(a).append("_boots',count:1},{id:'").append(a)
                        .append("_leggings',count:1},{id:'").append(a).append("_chestplate',count:1},{id:'").append(a)
                        .append("_helmet',count:1}],ArmorDropChances:[0.0f,0.0f,0.0f,0.0f]");
            }
            String w = weaponFor(type, k);
            if (w != null) sb.append(",HandItems:[{id:'minecraft:").append(w).append("',count:1},{}],HandDropChances:[0.0f,0.0f]");
            List<String> fx = new ArrayList<>();
            if (k >= 3) fx.add("{id:'minecraft:speed',amplifier:0b,duration:-1,show_particles:0b}");
            if (k >= 4) {
                fx.add("{id:'minecraft:strength',amplifier:1b,duration:-1,show_particles:0b}");
                fx.add("{id:'minecraft:resistance',amplifier:0b,duration:-1,show_particles:0b}");
            }
            if (!fx.isEmpty()) sb.append(",active_effects:[").append(String.join(",", fx)).append("]");
            if (k >= 4) sb.append(",attributes:[{id:'minecraft:generic.max_health',base:80.0d}],Health:80.0f");
            return sb.append("}").toString();
        }

        /**
         * Clearable floor: a Trial Spawner that spawns ONE mob type (picked per spawner from the floor's list).
         * Kill the wave and it ejects a regular trial key, which opens this floor's vault. Counts, armour and effects
         * scale with the floor number. The ominous config is identical so an omen can't swap in vanilla mobs.
         */
        void trialFloorSpawner(int dx, int y, int dz, int k) {
            BlockPos p = new BlockPos(cx + dx, y, cz + dz);
            if (y > yMax || !box.isInside(p)) return;
            level.setBlock(p, Blocks.TRIAL_SPAWNER.defaultBlockState(), 2);
            List<EntityType<?>> choices = t.mobsForFloor(k);
            EntityType<?> type = choices.get(rnd.nextInt(choices.size()));
            String config = "{spawn_range:6,total_mobs:" + TRIAL_TOTAL[k] + ".0f,simultaneous_mobs:" + TRIAL_SIMUL[k]
                    + ".0f,total_mobs_added_per_player:3.0f,simultaneous_mobs_added_per_player:1.0f,ticks_between_spawn:30,"
                    + "spawn_potentials:[{weight:1,data:{entity:" + mobNbt(type, k) + "}}],"
                    + "loot_tables_to_eject:[{weight:1,data:'minecraft:spawners/trial_chamber/key'}]}";
            blockEntityNbt(p, "{normal_config:" + config + ",ominous_config:" + config
                    + ",required_player_range:14,target_cooldown_length:" + SPAWNER_COOLDOWN + "}");
        }

        /** Boss Trial Spawner: exactly one boss type for this tower (picked at generation), then it ejects the ominous trial key. */
        void bossTrialSpawner(int dx, int y, int dz) {
            BlockPos p = new BlockPos(cx + dx, y, cz + dz);
            if (y > yMax || !box.isInside(p)) return;
            level.setBlock(p, Blocks.TRIAL_SPAWNER.defaultBlockState(), 2);
            String id = t.bosses[rnd.nextInt(t.bosses.length)];
            // the theme tag is how the boss knows which tower it belongs to (minion waves, abilities)
            String pot = "{weight:1,data:{entity:{id:'" + id + "',Tags:['" + t.bossTag() + "']}}}";
            String config = "{spawn_range:5,total_mobs:1.0f,simultaneous_mobs:1.0f,"
                    + "total_mobs_added_per_player:0.0f,simultaneous_mobs_added_per_player:0.0f,ticks_between_spawn:40,"
                    + "spawn_potentials:[" + pot + "],"
                    + "loot_tables_to_eject:[{weight:1,data:'dimtowers:spawners/boss_key'}]}";
            blockEntityNbt(p, "{normal_config:" + config + ",ominous_config:" + config
                    + ",required_player_range:12,target_cooldown_length:" + BOSS_COOLDOWN + "}");
        }

        // ------------------------------------------------------------------ helpers

        BlockPos pfs(int face, int tt, int r, int y) {
            return switch (face) {
                case 0 -> new BlockPos(cx + tt, y, cz + r);
                case 1 -> new BlockPos(cx + tt, y, cz - r);
                case 2 -> new BlockPos(cx + r, y, cz + tt);
                default -> new BlockPos(cx - r, y, cz + tt);
            };
        }
        /** Direction facing into the room from a given wall face. */
        Direction inward(int face) {
            return switch (face) { case 0 -> Direction.NORTH; case 1 -> Direction.SOUTH; case 2 -> Direction.WEST; default -> Direction.EAST; };
        }

        /** Places a block (and optional block-entity NBT) only when it is inside this chunk's box. */
        void setBe(BlockPos p, BlockState st, String nbt) {
            if (p.getY() > yMax || !box.isInside(p)) return;
            level.setBlock(p, st, 2);
            if (nbt != null) blockEntityNbt(p, nbt);
        }

        /** Loads an entity from SNBT at the given position, only when that position is inside this chunk's box. */
        void entity(String snbt, double x, double y, double z) {
            if (y > yMax || !box.isInside(BlockPos.containing(x, y, z))) return;
            try {
                CompoundTag tag = TagParser.parseTag(snbt);
                ListTag pos = new ListTag();
                pos.add(DoubleTag.valueOf(x)); pos.add(DoubleTag.valueOf(y)); pos.add(DoubleTag.valueOf(z));
                tag.put("Pos", pos);
                Optional<Entity> e = EntityType.create(tag, level.getLevel());
                e.ifPresent(level::addFreshEntity);
            } catch (Exception ignored) { }
        }

        /**
         * Armour stand with random gear: usually one full set, sometimes a mix of two materials (e.g. diamond with iron
         * boots), occasionally a missing piece, plus a random tool. Never netherite, no trims.
         */
        void armorStand(int dx, int y, int dz) {
            String base = STAND_ARMOR[rnd.nextInt(STAND_ARMOR.length)];
            String other = STAND_ARMOR[rnd.nextInt(STAND_ARMOR.length)];
            boolean mixed = !other.equals(base) && rnd.nextInt(10) < 5;
            StringBuilder armor = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i > 0) armor.append(',');
                if (rnd.nextInt(12) == 0) { armor.append("{}"); continue; }
                String mat = mixed && rnd.nextBoolean() ? other : base;
                armor.append("{id:'minecraft:").append(mat).append('_').append(ARMOR_SLOTS[i]).append("',count:1}");
            }
            String tool = STAND_TOOLS[rnd.nextInt(STAND_TOOLS.length)];
            float yaw = dx > 0 ? 90f : 270f;
            entity("{id:'minecraft:armor_stand',ShowArms:1b,NoBasePlate:1b,Invulnerable:1b,DisabledSlots:4144959,Rotation:[" + yaw + "f,0f],"
                    + "ArmorItems:[" + armor + "],HandItems:[{id:'minecraft:" + tool + "',count:1},{}]}", cx + dx + 0.5, y, cz + dz + 0.5);
        }

        /** 1x1 painting, so it never needs more than the one wall cell. */
        void painting(int dx, int y, int dz, Direction dir, String variant) {
            int x = cx + dx, z = cz + dz;
            entity("{id:'minecraft:painting',variant:'minecraft:" + variant + "',facing:" + dir.get2DDataValue() + "b,TileX:" + x
                    + ",TileY:" + y + ",TileZ:" + z + "}", x + 0.5, y + 0.5, z + 0.5);
        }

        BlockState wallBanner(Direction dir) {
            Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(t.bannerColor + "_wall_banner"));
            return b.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, dir);
        }
        String bannerNbt() {
            return "{patterns:[{pattern:'minecraft:" + t.bannerPattern + "',color:'white'},"
                    + "{pattern:'minecraft:border',color:'black'}]}";
        }

        void decoratedPot(int dx, int y, int dz, String contents) {
            String sh = "'minecraft:" + t.sherd + "_pottery_sherd'";
            String plain = "'minecraft:brick'";
            String sherds = rnd.nextBoolean() ? sh + "," + plain + "," + sh + "," + plain : plain + "," + sh + "," + plain + "," + sh;
            setBe(new BlockPos(cx + dx, y, cz + dz), Blocks.DECORATED_POT.defaultBlockState(),
                    "{sherds:[" + sherds + "],item:{id:'" + contents + "',count:" + (2 + rnd.nextInt(5)) + "}}");
        }

        // ------------------------------------------------------------------ floor identities

        /** Floor 1 (k=0): armory. Armour stands with random gear, anvils, grindstones, smithing and fletching tables. */
        void armory(int b) {
            for (int i = 0; i < SPOTS.length; i++) {
                int x = SPOTS[i][0], z = SPOTS[i][1];
                switch (i % 4) {
                    case 0, 2 -> armorStand(x, b + 1, z);
                    case 1 -> set(x, b + 1, z, i < 4 ? Blocks.ANVIL.defaultBlockState() : Blocks.GRINDSTONE.defaultBlockState());
                    default -> set(x, b + 1, z, i < 4 ? Blocks.SMITHING_TABLE.defaultBlockState() : Blocks.FLETCHING_TABLE.defaultBlockState());
                }
            }
        }

        /** Floor 2 (k=1): library. Reading nooks with lecterns, desks lit by candles, bookshelf stacks, chiseled shelves. */
        void library(int b) {
            for (int i = 0; i < SPOTS.length; i++) {
                int x = SPOTS[i][0], z = SPOTS[i][1];
                int side = x > 0 ? 1 : -1;
                Direction toRoom = side > 0 ? Direction.WEST : Direction.EAST;
                switch (i % 4) {
                    case 0 -> {
                        set(x, b + 1, z, Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, toRoom));
                        for (int y = b + 1; y <= b + 3; y++) set(side * I, y, z, bookshelf);
                    }
                    case 1 -> { set(x, b + 1, z, slabTop()); set(x, b + 2, z, candle); }
                    case 2 -> { for (int y = b + 1; y <= b + 3; y++) set(x, y, z, bookshelf); }
                    default -> {
                        BlockState cb = Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, toRoom);
                        set(x, b + 1, z, cb);
                        set(x, b + 2, z, cb);
                        set(x, b + 3, z, candle);
                    }
                }
            }
        }

        /**
         * Floor 3 (k=2): trap floor. Each hidden pressure plate sits next to a dispenser built into the wall (a plate
         * powers the dispenser beside it). Mobs trigger them too.
         */
        void trapFloor(int b) {
            for (int f = 0; f < 4; f++) for (int tt : new int[]{-4, 4}) {
                if (f == 1 && tt == -4) continue; // k=2 is an even floor: the stairs fill this spot, the dispenser would shoot into stone
                String arrows = "{Items:[{Slot:0b,id:'minecraft:arrow',count:64},{Slot:1b,id:'minecraft:arrow',count:64},"
                        + "{Slot:2b,id:'minecraft:tipped_arrow',count:32,components:{'minecraft:potion_contents':{potion:'minecraft:poison'}}},"
                        + "{Slot:3b,id:'minecraft:tipped_arrow',count:32,components:{'minecraft:potion_contents':{potion:'minecraft:slowness'}}},"
                        + "{Slot:4b,id:'minecraft:arrow',count:64}]}";
                setBe(pfs(f, tt, R - 1, b + 1), Blocks.DISPENSER.defaultBlockState().setValue(BlockStateProperties.FACING, inward(f)), arrows);
                fs(f, tt, I, b + 1, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
            }
        }

        /** Floor 4 (k=3): treasury. Decorated pots on gold plinths, with the theme's loot in the chests. */
        void treasury(int b) {
            for (int i = 0; i < SPOTS.length; i++) {
                int x = SPOTS[i][0], z = SPOTS[i][1];
                String[] loot = {"minecraft:emerald", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:amethyst_shard"};
                if (i % 2 == 0) {
                    decoratedPot(x, b + 1, z, loot[(i / 2) % 4]);
                } else {
                    set(x, b + 1, z, Blocks.GOLD_BLOCK.defaultBlockState());
                    decoratedPot(x, b + 2, z, loot[(i / 2) % 4]);
                }
            }
        }

        /** Floor 5 (k=4): champion hall. Armour stands with random gear (never netherite) between lantern braziers. */
        void championHall(int b) {
            for (int i = 0; i < SPOTS.length; i++) {
                int x = SPOTS[i][0], z = SPOTS[i][1];
                if (i % 2 == 0) {
                    armorStand(x, b + 1, z);
                } else {
                    set(x, b + 1, z, t.trim);
                    set(x, b + 2, z, t.pillar);
                    set(x, b + 3, z, t.lanternStanding);
                }
            }
        }

        /** Banners on the pilasters, plus a painting (odd floors) or a candle shelf (even floors) below each. */
        void dress(int k, int b) {
            String[] art = {"kebab", "aztec", "alban", "bomb", "plant", "wasteland", "aztec2"};
            // the staircase fills the cells in front of one pilaster on the stair wall, so skip that one
            final int stairFace = k % 2 == 0 ? 1 : 0;
            final int stairTt = k % 2 == 0 ? -3 : 3;
            for (int f = 0; f < 4; f++) for (int tt : new int[]{-3, 3}) {
                if (f == stairFace && tt == stairTt) continue;
                Direction in = inward(f);
                setBe(pfs(f, tt, I - 1, b + 8), wallBanner(in), bannerNbt());
                BlockPos fp = pfs(f, tt, I - 1, b + 4);
                int ddx = fp.getX() - cx, ddz = fp.getZ() - cz;
                if (k % 2 == 1) {
                    painting(ddx, b + 4, ddz, in, art[rnd.nextInt(art.length)]);
                } else {
                    set(ddx, b + 4, ddz, slabTop());
                    set(ddx, b + 5, ddz, candle);
                }
            }
        }

        /** Floors 2-5: this floor's vault (opens with the key the floor's Trial Spawner ejects), framed like the boss vault. */
        void floorVault(int k, int b, int vf) {
            for (int y = b + 1; y <= b + 3; y++) { fs(vf, -1, I, y, t.pillar); fs(vf, 1, I, y, t.pillar); }
            fs(vf, 0, I, b + 2, t.trim);
            fs(vf, 0, I, b + 3, t.trim);
            setBe(pfs(vf, 0, I, b + 1), Blocks.VAULT.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, inward(vf)),
                    "{config:{key_item:{id:'minecraft:trial_key',count:1},loot_table:'dimtowers:vault/floor_" + t.id + "'}}");
        }

        /** Interior pilasters between the windows on all four walls, from y0 up to y1. */
        void interiorPilasters(int y0, int y1) {
            for (int f = 0; f < 4; f++) for (int tt : PILASTER) {
                fs(f, tt, I, y0, t.trim);
                for (int y = y0 + 1; y < y1; y++) fs(f, tt, I, y, t.pillar);
                fs(f, tt, I, y1, t.trim);
            }
        }

        /** Corner brazier posts in the four inside corners. */
        void cornerPosts(int b) {
            for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) {
                set(sx * I, b + 1, sz * I, t.trim);
                set(sx * I, b + 2, sz * I, t.pillar);
                set(sx * I, b + 3, sz * I, t.lanternStanding);
            }
        }

        /** Runners, a patterned ring round the dais and bordered rugs under each group of props. */
        void carpets(int b, int k) {
            BlockState main = carpet(), alt = carpetAlt();
            // chequered ring round the spawner dais
            for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
                if (Math.max(abs(dx), abs(dz)) == 4) set(dx, b + 1, dz, ((dx + dz) & 1) == 0 ? main : alt);
            }
            // bordered rugs in the four corners, under the prop spots
            for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) {
                for (int x = 10; x <= 12; x++) for (int z = 3; z <= 8; z++) {
                    boolean edge = x == 10 || x == 12 || z == 3 || z == 8;
                    set(sx * x, b + 1, sz * z, edge ? alt : main);
                }
            }
            // north-south runner (on the ground floor it leads in from the front door)
            int zMax = k == 0 ? R : R - 5;
            for (int dz = -(R - 5); dz <= zMax; dz++) {
                if (abs(dz) <= 3) continue;
                for (int dx = -1; dx <= 1; dx++) set(dx, b + 1, dz, dx == 0 ? main : alt);
            }
            // east-west runner out to the chest shrines
            for (int dx = -(I - 1); dx <= I - 1; dx++) {
                if (abs(dx) <= 3) continue;
                for (int dz = -1; dz <= 1; dz++) set(dx, b + 1, dz, dz == 0 ? main : alt);
            }
        }

        void build() {
            final int sTop = s(FLOORS);
            final int top = Math.min(sTop + 22, yMax);
            final BlockState slabTop = t.slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            final BlockState slabBot = t.slab.defaultBlockState();
            final BlockState wallPiece = t.wallPiece.defaultBlockState();

            // 1. clear the whole volume
            for (int dx = -(R + 3); dx <= R + 3; dx++) for (int dz = -(R + 3); dz <= R + 3; dz++) {
                if (!colInside(dx, dz)) continue;
                for (int y = s0; y <= top; y++) clr(dx, y, dz);
            }

            // 2. tapered foundation down to the ground
            for (int dx = -(R + 2); dx <= R + 2; dx++) for (int dz = -(R + 2); dz <= R + 2; dz++) {
                if (!colInside(dx, dz)) continue;
                int depth = 56 - 3 * Math.max(abs(dx), abs(dz));
                for (int i = 0; i <= depth; i++) {
                    int y = s0 - i;
                    if (y < level.getMinBuildHeight()) break;
                    BlockState st = at(dx, y, dz);
                    if (i > 0 && !(st.isAir() || !st.getFluidState().isEmpty() || st.canBeReplaced())) break;
                    set(dx, y, dz, t.plinth);
                }
            }

            // 3. floors (compass-rose + diamond pattern); k == FLOORS is the boss arena
            for (int k = 0; k <= FLOORS; k++)
                for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++)
                    set(dx, s(k), dz, floorAt(dx, dz));

            // 4. double-thick walls with a plinth course, string courses and a crenellated parapet
            for (int y = s0 + 1; y <= sTop + 5; y++)
                for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
                    if (Math.max(abs(dx), abs(dz)) < R - 1) continue;
                    set(dx, y, dz, y == s0 + 1 ? t.plinth : wallBlock());
                }
            for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
                if (Math.max(abs(dx), abs(dz)) != R) continue;
                for (int k = 1; k <= FLOORS; k++) set(dx, s(k), dz, t.trim);
                if (((dx + dz) & 1) == 0) set(dx, sTop + 6, dz, t.wall);
            }

            // 5. cornices (one per floor), a low base ledge and an outer apron step
            for (int dx = -(R + 2); dx <= R + 2; dx++) for (int dz = -(R + 2); dz <= R + 2; dz++) {
                int r = Math.max(abs(dx), abs(dz));
                if (r == R + 1) {
                    for (int k = 1; k <= FLOORS; k++) {
                        set(dx, s(k), dz, slabTop);
                        set(dx, s(k) - 1, dz, stairBlock(towardWall(dx, dz), true)); // corbel under each cornice
                    }
                    if (!(dz == R + 1 && abs(dx) <= 4)) set(dx, s0 + 1, dz, slabBot);
                } else if (r == R + 2) {
                    if (!(dz == R + 2 && abs(dx) <= 4)) set(dx, s0 + 1, dz, slabBot);
                }
            }

            // 6. pilasters on every face
            for (int f = 0; f < 4; f++) for (int tt : PILASTER) {
                for (int y = s0 + 1; y <= sTop + 5; y++) fs(f, tt, R + 1, y, t.pillar);
                fs(f, tt, R + 1, sTop + 6, wallPiece);
            }

            // 6b. banners hung on the outer pilasters (ground, third and top floors)
            for (int k = 0; k < FLOORS; k += 2) for (int f = 0; f < 4; f++) for (int tt : PILASTER) {
                if (k == 0 && f == 0 && abs(tt) == 3) continue; // entrance pillars
                setBe(pfs(f, tt, R + 2, s(k) + 6), wallBanner(inward(f).getOpposite()), bannerNbt());
            }

            // 7. corner turrets with tall lantern spires
            for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) {
                for (int ax = R - 1; ax <= R + 1; ax++) for (int az = R - 1; az <= R + 1; az++) {
                    boolean corner = ax == R + 1 && az == R + 1;
                    for (int y = s0 + 1; y <= sTop + 8; y++) set(sx * ax, y, sz * az, corner ? t.pillar : wallBlock());
                    set(sx * ax, sTop + 9, sz * az, t.trim);
                    if (!(ax == R && az == R)) set(sx * ax, sTop + 10, sz * az, slabBot);
                }
                for (int y = sTop + 10; y <= sTop + 13; y++) set(sx * R, y, sz * R, t.pillar);
                set(sx * R, sTop + 14, sz * R, wallPiece);
                set(sx * R, sTop + 15, sz * R, t.light);
                set(sx * R, sTop + 16, sz * R, Blocks.BELL.defaultBlockState());
            }

            // 8. tall arched windows (five per face per floor)
            for (int k = 0; k < FLOORS; k++) for (int f = 0; f < 4; f++) for (int tt : WIN) {
                if (k == 0 && f == 0 && tt == 0) continue; // the main door
                int b = s(k);
                for (int w = -1; w <= 1; w++) {
                    int c = tt + w;
                    for (int y = b + 3; y <= b + 8; y++) { fs(f, c, R, y, t.glass); fs(f, c, R - 1, y, air); }
                    if (w == 0) { fs(f, c, R, b + 9, t.glass); fs(f, c, R - 1, b + 9, air); }
                    else fs(f, c, R, b + 9, t.trim);
                    fs(f, c, R, b + 10, t.trim);
                    fs(f, c, R, b + 2, t.trim);
                }
                for (int y = b + 3; y <= b + 9; y++) {
                    if (abs(tt - 2) < R - 1) fs(f, tt - 2, R, y, t.pillar);
                    if (abs(tt + 2) < R - 1) fs(f, tt + 2, R, y, t.pillar);
                }
            }

            // 9. grand entrance on the south face
            for (int dx = -2; dx <= 2; dx++) for (int y = s0 + 1; y <= s0 + 7; y++) { clr(dx, y, R - 1); clr(dx, y, R); }
            for (int dx = -1; dx <= 1; dx++) { clr(dx, s0 + 8, R - 1); clr(dx, s0 + 8, R); }
            for (int z = R - 1; z <= R + 1; z++) {
                set(-2, s0 + 8, z, t.trim); set(2, s0 + 8, z, t.trim);
                for (int dx = -2; dx <= 2; dx++) set(dx, s0 + 9, z, t.trim);
                for (int y = s0 + 1; y <= s0 + 9; y++) { set(-3, y, z, t.pillar); set(3, y, z, t.pillar); }
            }
            set(-3, s0 + 10, R + 1, t.light); set(3, s0 + 10, R + 1, t.light);

            // 9b. paved forecourt in front of the door, with lantern braziers at its corners
            for (int dx = -4; dx <= 4; dx++) for (int dz = R + 1; dz <= R + 4; dz++) {
                if (!colInside(dx, dz)) continue;
                for (int i = 0; i <= 8; i++) {
                    int y = s0 - i;
                    if (y < level.getMinBuildHeight()) break;
                    if (i > 0) {
                        BlockState st = at(dx, y, dz);
                        if (!(st.isAir() || !st.getFluidState().isEmpty() || st.canBeReplaced())) break;
                    }
                    set(dx, y, dz, i > 0 ? t.plinth : (abs(dx) + dz) % 2 == 0 ? t.floor : t.floorAlt);
                }
                if (dz > R + 2) for (int y = s0 + 1; y <= s0 + 7; y++) clr(dx, y, dz);
            }
            for (int sx = -4; sx <= 4; sx += 8) {
                set(sx, s0 + 1, R + 4, t.trim);
                set(sx, s0 + 2, R + 4, t.pillar);
                set(sx, s0 + 3, R + 4, t.lanternStanding);
            }

            // 10. interior floors
            for (int k = 0; k < FLOORS; k++) {
                int b = s(k);
                // stairs hug the north wall on even floors and the south wall on odd ones, so the vault takes the other side
                final int vf = k % 2 == 1 ? 1 : 0;

                // ceiling beams: a square through the pillars plus a cross through the middle
                for (int i = -PIL; i <= PIL; i++) {
                    set(i, b + H - 1, PIL, t.trim); set(i, b + H - 1, -PIL, t.trim);
                    set(PIL, b + H - 1, i, t.trim); set(-PIL, b + H - 1, i, t.trim);
                    set(i, b + H - 1, 0, t.trim);   set(0, b + H - 1, i, t.trim);
                }

                // pillars with a trim base and glowing capitals
                for (int sx = -PIL; sx <= PIL; sx += 2 * PIL) for (int sz = -PIL; sz <= PIL; sz += 2 * PIL) {
                    set(sx, b + 1, sz, t.trim);
                    for (int y = b + 2; y <= b + H - 2; y++) set(sx, y, sz, t.pillar);
                    set(sx, b + H - 1, sz, t.trim);
                    set(sx, b + H, sz, t.light);
                    // stair-block plinth at the foot and capital under the beams
                    for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        Direction toPillar = dirOf(-d[0], -d[1]);
                        set(sx + d[0], b + 1, sz + d[1], stairBlock(toPillar, false));
                        set(sx + d[0], b + H - 2, sz + d[1], stairBlock(toPillar, true));
                    }
                }

                // chandeliers on the beam crossings, plus a big one over the spawner
                int[][] ch = {{0, PIL}, {0, -PIL}, {PIL, 0}, {-PIL, 0}};
                for (int[] c : ch) {
                    set(c[0], b + H - 2, c[1], chain);
                    set(c[0], b + H - 3, c[1], chain);
                    set(c[0], b + H - 4, c[1], t.lanternHanging);
                }
                for (int[] c : new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}}) {
                    set(c[0], b + H - 2, c[1], chain);
                    set(c[0], b + H - 3, c[1], t.lanternHanging);
                }
                for (int y = b + H - 2; y >= b + H - 5; y--) set(0, y, 0, chain);
                set(0, b + H - 6, 0, t.lanternHanging);

                carpets(b, k);

                // spawner dais: layered floor medallion, slab collar and four lantern posts
                for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                    int r = Math.max(abs(dx), abs(dz));
                    set(dx, b, dz, r <= 1 ? t.accent : r == 2 ? t.floorAlt : t.trim);
                    if (r == 1 && (dx != 0 || dz != 0)) set(dx, b + 1, dz, slabBot);
                }
                for (int sx = -3; sx <= 3; sx += 6) for (int sz = -3; sz <= 3; sz += 6) {
                    set(sx, b + 1, sz, t.trim);
                    set(sx, b + 2, sz, t.pillar);
                    set(sx, b + 3, sz, t.lanternStanding);
                }
                trialFloorSpawner(0, b + 1, 0, k);

                // lantern posts flanking the runner
                for (int sx = -3; sx <= 3; sx += 6) for (int sz = -6; sz <= 6; sz += 12) {
                    set(sx, b + 1, sz, t.trim);
                    set(sx, b + 2, sz, t.lanternStanding);
                }

                // skirting course and ceiling cornice of stair blocks all round the room
                for (int f = 0; f < 4; f++) {
                    Direction outward = inward(f).getOpposite();
                    for (int tt = -I; tt <= I; tt++) {
                        fs(f, tt, I, b + H - 1, stairBlock(outward, true));
                        if (has(WIN, tt) || has(PILASTER, tt)) continue;
                        if (k == 0 && f == 0 && abs(tt) <= 2) continue; // front door
                        fs(f, tt, I, b + 1, stairBlock(outward, false));
                    }
                }

                // wall pilasters, corner braziers
                interiorPilasters(b + 1, b + H - 1);
                cornerPosts(b);

                // library nooks and barrels under each window, topped with candles
                for (int f = 0; f < 4; f++) for (int tt : WIN) {
                    if (k == 0 && f == 0 && tt == 0) continue;  // front door
                    if (f >= 2 && tt == 0) continue;            // the chest shrines sit on the east/west centre bays
                    if (k >= 1 && tt == 0 && f == vf) continue; // this floor's vault goes here
                    if (k == 2 && rnd.nextInt(5) == 0) {
                        fs(f, tt, I, b + 1, Blocks.INFESTED_STONE_BRICKS.defaultBlockState()); // silverfish
                        fs(f, tt, I, b + 2, Blocks.INFESTED_STONE_BRICKS.defaultBlockState());
                    } else if (rnd.nextInt(3) == 0) {
                        barrelAt(f, tt, b + 1);
                        barrelAt(f, tt, b + 2);
                    } else {
                        fs(f, tt, I, b + 1, bookshelf);
                        fs(f, tt, I, b + 2, bookshelf);
                    }
                    fs(f, tt, I, b + 3, candle);
                }

                // two-wide switchback staircase: solid-backed steps, a stair-block stringer, a balustrade with a
                // slab handrail and a lantern every few posts, and a newel post at the foot
                boolean even = k % 2 == 0;
                int zRow = even ? -(R - 3) : (R - 3), zIn = even ? -(R - 4) : (R - 4);
                int zRail = even ? zIn + 1 : zIn - 1;
                Direction dir = even ? Direction.EAST : Direction.WEST;
                int startX = even ? -(H - 1) : (H - 1), step = even ? 1 : -1;
                BlockState stair = stairBlock(dir, false);
                for (int i = 0; i < H; i++) {
                    int x = startX + step * i;
                    int y = b + 1 + i;
                    for (int z : new int[]{zRow, zIn}) {
                        for (int yy = b + 1; yy < y; yy++) set(x, yy, z, t.floorAlt);
                        set(x, y, z, stair);
                        if (i < H - 1) clr(x, b + H, z);
                    }
                    for (int yy = b + 1; yy < y; yy++) set(x, yy, zRail, t.trim);
                    set(x, y, zRail, stair);
                    if (i < H - 1) {
                        set(x, y + 1, zRail, wallPiece);
                        set(x, y + 2, zRail, i % 4 == 2 ? t.lanternStanding : slabBot);
                    }
                }
                int footX = startX - step;
                set(footX, b + 1, zRail, t.trim);
                set(footX, b + 2, zRail, t.pillar);
                set(footX, b + 3, zRail, t.lanternStanding);
                set(footX, b + 1, zRow, carpetAlt());
                set(footX, b + 1, zIn, carpetAlt());

                // chests: the treasury (k=3) holds the theme's own loot, other floors roll vanilla tables
                chestShrine(2, b, k == 3 ? treasureKey() : loot());
                chestShrine(3, b, k == 3 ? treasureKey() : loot());

                // floor identity, dressing, vault and secrets
                switch (k) {
                    case 0 -> armory(b);
                    case 1 -> library(b);
                    case 2 -> trapFloor(b);
                    case 3 -> treasury(b);
                    default -> championHall(b);
                }
                dress(k, b);
                if (k >= 1) floorVault(k, b, vf);
            }

            // 11. boss arena (open to the sky)
            for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
                int r = Math.max(abs(dx), abs(dz));
                set(dx, sTop, dz, r <= 1 ? t.accent : r == 2 ? t.floorAlt : r == 3 ? t.trim : r == 4 ? t.floorAlt : t.trim);
            }
            bossTrialSpawner(0, sTop + 1, 0);
            for (int sx = -6; sx <= 6; sx += 12) for (int sz = -6; sz <= 6; sz += 12) {
                for (int y = sTop + 1; y <= sTop + 3; y++) set(sx, y, sz, t.pillar);
                set(sx, sTop + 4, sz, wallPiece);
                set(sx, sTop + 5, sz, t.light);
            }
            // braziers at the four compass points of the dais
            int[][] br = {{6, 0}, {-6, 0}, {0, 6}, {0, -6}};
            for (int[] c : br) { set(c[0], sTop + 1, c[1], t.trim); set(c[0], sTop + 2, c[1], t.lanternStanding); }
            // wall lights, pilasters and corner posts
            for (int f = 0; f < 4; f++) for (int tt : WIN) fs(f, tt, R - 1, sTop + 3, t.light);
            interiorPilasters(sTop + 1, sTop + 4);
            cornerPosts(sTop);

            // banners on the arena pilasters
            for (int f = 0; f < 4; f++) for (int tt : new int[]{-3, 3}) {
                setBe(pfs(f, tt, I - 1, sTop + 3), wallBanner(inward(f)), bannerNbt());
            }

            // vault on the south side, framed
            for (int y = sTop + 1; y <= sTop + 3; y++) { set(-1, y, I, t.pillar); set(1, y, I, t.pillar); }
            set(0, sTop + 2, I, t.trim);
            set(0, sTop + 3, I, t.trim);
            BlockPos vp = new BlockPos(cx, sTop + 1, cz + I);
            if (sTop + 1 <= yMax && box.isInside(vp)) {
                level.setBlock(vp, Blocks.VAULT.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                        .setValue(BlockStateProperties.OMINOUS, true), 2);
                blockEntityNbt(vp, "{config:{key_item:{id:\"minecraft:ominous_trial_key\",count:1},"
                        + "loot_table:\"dimtowers:vault/reward_" + t.id + "\"}}");
            }

            // treasure chests
            int[][] cp = {{R - 3, R - 3, 0}, {-(R - 3), R - 3, 1}, {I, -6, 0}, {-I, -6, 1}};
            for (int[] c : cp) {
                BlockPos p = new BlockPos(cx + c[0], sTop + 1, cz + c[1]);
                if (sTop + 1 > yMax || !box.isInside(p)) continue;
                level.setBlock(p, Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, c[2] == 0 ? Direction.WEST : Direction.EAST), 2);
                RandomizableContainer.setBlockEntityLootTable(level, rnd, p, treasureKey());
            }
        }
    }
}
