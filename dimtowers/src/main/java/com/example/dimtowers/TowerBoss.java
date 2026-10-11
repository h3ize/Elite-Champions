package com.example.dimtowers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class TowerBoss extends WitherSkeleton {
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    public TowerBoss(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
        this.xpReward = BossKit.XP;
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return BossKit.boost(WitherSkeleton.createAttributes(), BossKit.DMG_WITHER);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType type, @Nullable SpawnGroupData data) {
        SpawnGroupData d = super.finalizeSpawn(level, diff, type, data);
        BossKit.setup(this, BossKit.Weapon.SWORD, BossKit.DMG_WITHER);
        return d;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new BossKit.SignatureGoal(this, BossKit.Move.WITHER_SKULLS));
    }

    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bossEvent.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bossEvent.removePlayer(p); }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        BossKit.setup(this, BossKit.Weapon.SWORD, BossKit.DMG_WITHER);
        bossEvent.setProgress(getHealth() / getMaxHealth());
        BossKit.tick(this);
    }
    @Override public void setCustomName(@Nullable Component n) {
        super.setCustomName(n);
        bossEvent.setName(getDisplayName());
    }
}
