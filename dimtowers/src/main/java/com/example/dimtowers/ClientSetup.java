package com.example.dimtowers;

import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = DimensionalTowers.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(DimensionalTowers.TOWER_BOSS.get(), WitherSkeletonRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_ZOMBIE.get(), ZombieRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_SKELETON.get(), SkeletonRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_PIGLIN.get(), TowerPiglinRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_VINDICATOR.get(), VindicatorRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_BLAZE.get(), BlazeRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_SPIDER.get(), SpiderRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_ENDER.get(), EndermanRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_SLIME.get(), SlimeRenderer::new);
        e.registerEntityRenderer(DimensionalTowers.TOWER_DROWNED.get(), DrownedRenderer::new);
    }
}