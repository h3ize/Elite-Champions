package com.example.dimtowers;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class TowerPiglinRenderer extends PiglinRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin_brute.png");

    public TowerPiglinRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE_INNER_ARMOR,
                ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false);
    }

    @Override
    public ResourceLocation getTextureLocation(Mob entity) {
        return TEXTURE;
    }
}