package com.elitechampions.champion;

import com.elitechampions.EliteChampions;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

/** Builds standard and "special" (named, enchanted, attribute-boosted) champion gear. */
public final class ChampionGear {
    private static final String[] ARMOR_SUFFIX = {"_helmet", "_chestplate", "_leggings", "_boots"};

    private ChampionGear() {}

    public static ItemStack standardArmor(String material, EquipmentSlot slot) {
        int idx = switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> -1;
        };
        if (idx < 0 || material == null || material.isBlank()) return ItemStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(material);
        if (rl == null) return ItemStack.EMPTY;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath() + ARMOR_SUFFIX[idx]);
        return BuiltInRegistries.ITEM.getOptional(id).map(item -> new ItemStack(item)).orElse(ItemStack.EMPTY);
    }

    public static ItemStack itemById(String id) {
        if (id == null || id.isBlank()) return ItemStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return ItemStack.EMPTY;
        return BuiltInRegistries.ITEM.getOptional(rl).map(item -> new ItemStack(item)).orElse(ItemStack.EMPTY);
    }

    private static void enchant(RegistryAccess access, ItemStack stack, ResourceKey<Enchantment> key, int level) {
        access.lookupOrThrow(Registries.ENCHANTMENT).get(key).ifPresent(holder -> stack.enchant(holder, level));
    }

    /** Turns a plain item into a rank-flavoured special item (in place). */
    public static void decorate(RegistryAccess access, ChampionRank rank, ItemStack stack, EquipmentSlot slot, RandomSource rand) {
        int t = rank.ordinal(); // 1..4
        double attrAmount;
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr;
        AttributeModifier.Operation op = AttributeModifier.Operation.ADD_VALUE;

        if (slot != EquipmentSlot.MAINHAND) {
            enchant(access, stack, Enchantments.PROTECTION, Math.min(5, t + 1));
            enchant(access, stack, Enchantments.UNBREAKING, 3);
            List<ResourceKey<Enchantment>> extras = List.of(Enchantments.THORNS, Enchantments.PROJECTILE_PROTECTION,
                    Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION);
            enchant(access, stack, extras.get(rand.nextInt(extras.size())), Math.min(3, t));
            if (slot == EquipmentSlot.FEET) {
                enchant(access, stack, Enchantments.FEATHER_FALLING, 4);
                attr = Attributes.MOVEMENT_SPEED;
                attrAmount = 0.02 * t;
                op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            } else {
                attr = Attributes.MAX_HEALTH;
                attrAmount = 2.0 * t;
            }
        } else {
            if (stack.getItem() instanceof BowItem) {
                enchant(access, stack, Enchantments.POWER, Math.min(7, t + 2));
                enchant(access, stack, Enchantments.PUNCH, Math.min(2, t));
            } else if (stack.getItem() instanceof CrossbowItem) {
                enchant(access, stack, Enchantments.QUICK_CHARGE, Math.min(3, t));
                enchant(access, stack, Enchantments.PIERCING, Math.min(4, t));
                if (rank == ChampionRank.LEGENDARY) enchant(access, stack, Enchantments.MULTISHOT, 1);
            } else {
                enchant(access, stack, Enchantments.SHARPNESS, Math.min(7, t + 2));
                enchant(access, stack, Enchantments.FIRE_ASPECT, Math.min(2, t));
                enchant(access, stack, Enchantments.KNOCKBACK, Math.min(2, t));
            }
            enchant(access, stack, Enchantments.UNBREAKING, 3);
            attr = Attributes.ATTACK_DAMAGE;
            attrAmount = 1.5 * t;
        }

        ItemAttributeModifiers mods = stack.getAttributeModifiers();
        mods = mods.withModifierAdded(attr,
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(EliteChampions.MODID, "special_gear_" + slot.getName()), attrAmount, op),
                EquipmentSlotGroup.bySlot(slot));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, mods);

        stack.set(DataComponents.CUSTOM_NAME,
                Component.translatable("elitechampions.gear.name", Component.translatable(rank.translationKey()), stack.getItem().getDescription())
                        .withStyle(rank.color())
                        .withStyle(style -> style.withItalic(false)));
    }
}
