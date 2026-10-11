package com.example.dimtowers;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DimensionalTowers.MOD_ID)
public class DimensionalTowers {
    public static final String MOD_ID = "dimtowers";

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);
    public static final DeferredHolder<StructureType<?>, StructureType<TowerStructure>> TOWER_STRUCTURE =
            STRUCTURE_TYPES.register("tower", () -> (StructureType<TowerStructure>) () -> TowerStructure.CODEC);

    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MOD_ID);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> TOWER_PIECE =
            PIECE_TYPES.register("tower_piece", () -> (StructurePieceType) TowerPiece::new);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<TowerBoss>> TOWER_BOSS =
            ENTITIES.register("tower_boss", () -> EntityType.Builder.<TowerBoss>of(TowerBoss::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.4F).fireImmune().clientTrackingRange(10).build("tower_boss"));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerZombie>> TOWER_ZOMBIE =
            ENTITIES.register("tower_zombie", () -> EntityType.Builder.<TowerZombie>of(TowerZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).fireImmune().clientTrackingRange(10).build("tower_zombie"));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerSkeleton>> TOWER_SKELETON =
            ENTITIES.register("tower_skeleton", () -> EntityType.Builder.<TowerSkeleton>of(TowerSkeleton::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F).fireImmune().clientTrackingRange(10).build("tower_skeleton"));

    public static final DeferredHolder<EntityType<?>, EntityType<TowerPiglin>> TOWER_PIGLIN =
            ENTITIES.register("tower_piglin", () -> EntityType.Builder.<TowerPiglin>of(TowerPiglin::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).fireImmune().clientTrackingRange(10).build("tower_piglin"));

    public static final DeferredHolder<EntityType<?>, EntityType<TowerVindicator>> TOWER_VINDICATOR =
            ENTITIES.register("tower_vindicator", () -> EntityType.Builder.<TowerVindicator>of(TowerVindicator::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).fireImmune().clientTrackingRange(10).build("tower_vindicator"));

    public static final DeferredHolder<EntityType<?>, EntityType<TowerBlaze>> TOWER_BLAZE =
            ENTITIES.register("tower_blaze", () -> EntityType.Builder.<TowerBlaze>of(TowerBlaze::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).fireImmune().clientTrackingRange(10).build("tower_blaze"));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerSpider>> TOWER_SPIDER =
            ENTITIES.register("tower_spider", () -> EntityType.Builder.<TowerSpider>of(TowerSpider::new, MobCategory.MONSTER)
                    .sized(1.4F, 0.9F).fireImmune().clientTrackingRange(10).build("tower_spider"));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerEnder>> TOWER_ENDER =
            ENTITIES.register("tower_ender", () -> EntityType.Builder.<TowerEnder>of(TowerEnder::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.9F).fireImmune().clientTrackingRange(10).build("tower_ender"));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerSlime>> TOWER_SLIME =
            ENTITIES.register("tower_slime", () -> EntityType.Builder.<TowerSlime>of(TowerSlime::new, MobCategory.MONSTER)
                    .sized(0.52F, 0.52F).fireImmune().clientTrackingRange(10).build("tower_slime"));
    public static final DeferredHolder<EntityType<?>, EntityType<TowerDrowned>> TOWER_DROWNED =
            ENTITIES.register("tower_drowned", () -> EntityType.Builder.<TowerDrowned>of(TowerDrowned::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).fireImmune().clientTrackingRange(10).build("tower_drowned"));

    public DimensionalTowers(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        PIECE_TYPES.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(this::attributes);
        NeoForge.EVENT_BUS.addListener(BossKit::onLivingDeath);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(TOWER_BOSS.get(), TowerBoss.createBossAttributes().build());
        e.put(TOWER_ZOMBIE.get(), TowerZombie.createBossAttributes().build());
        e.put(TOWER_SKELETON.get(), TowerSkeleton.createBossAttributes().build());
        e.put(TOWER_PIGLIN.get(), TowerPiglin.createBossAttributes().build());
        e.put(TOWER_VINDICATOR.get(), TowerVindicator.createBossAttributes().build());
        e.put(TOWER_BLAZE.get(), TowerBlaze.createBossAttributes().build());
        e.put(TOWER_SPIDER.get(), TowerSpider.createBossAttributes().build());
        e.put(TOWER_ENDER.get(), TowerEnder.createBossAttributes().build());
        e.put(TOWER_SLIME.get(), TowerSlime.createBossAttributes().build());
        e.put(TOWER_DROWNED.get(), TowerDrowned.createBossAttributes().build());
    }
}