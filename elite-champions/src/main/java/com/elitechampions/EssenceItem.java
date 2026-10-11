package com.elitechampions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Unobtainable-by-normal-means trophy item dropped by champions. */
public class EssenceItem extends Item {
    public EssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
