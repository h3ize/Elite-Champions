package com.mobascension;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Builds "Relics": uniquely named gear with enchantments (never above vanilla max) and bonus attributes. */
public final class RelicFactory {
    public static final String RELIC_TAG = "mobascension_relic";

    private static final String[] PREFIXES = {
            "Gravebound", "Stormforged", "Emberheart", "Voidtouched", "Ashen", "Frostbitten",
            "Wraithwoven", "Sunscarred", "Hollowed", "Thornwrought", "Duskbound", "Bloodsworn"};

    private static final List<ResourceKey<Enchantment>> ARMOR_POOL = List.of(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION, Enchantments.THORNS, Enchantments.UNBREAKING, Enchantments.MENDING);
    private static final List<ResourceKey<Enchantment>> HEAD_POOL = List.of(
            Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY);
    private static final List<ResourceKey<Enchantment>> LEGS_POOL = List.of(Enchantments.SWIFT_SNEAK);
    private static final List<ResourceKey<Enchantment>> FEET_POOL = List.of(
            Enchantments.FEATHER_FALLING, Enchantments.DEPTH_STRIDER, Enchantments.SOUL_SPEED);
    private static final List<ResourceKey<Enchantment>> WEAPON_POOL = List.of(
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.FIRE_ASPECT,
            Enchantments.KNOCKBACK, Enchantments.LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.UNBREAKING,
            Enchantments.MENDING);

    private record AttrDef(Holder<Attribute> attribute, double amount, AttributeModifier.Operation op) {}

    private RelicFactory() {}

    public static boolean isRelic(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains(RELIC_TAG);
    }

    /** Turns the given base item into a Relic for the given rank. {@code slot} MAINHAND means weapon. */
    public static ItemStack create(ItemStack stack, Rank rank, EquipmentSlot slot, RegistryAccess access, RandomSource random) {
        AscensionConfig.RankSettings cfg = AscensionConfig.rank(rank);
        boolean weapon = slot == EquipmentSlot.MAINHAND;

        // ---- enchantments (capped at each enchantment's vanilla max level) ----
        List<ResourceKey<Enchantment>> pool = new ArrayList<>(weapon ? WEAPON_POOL : ARMOR_POOL);
        switch (slot) {
            case HEAD -> pool.addAll(HEAD_POOL);
            case LEGS -> pool.addAll(LEGS_POOL);
            case FEET -> pool.addAll(FEET_POOL);
            default -> { }
        }
        Collections.shuffle(pool, new Random(random.nextLong()));
        Registry<Enchantment> registry = access.registryOrThrow(Registries.ENCHANTMENT);
        int count = Math.min(cfg.relicEnchants.get(), pool.size());
        double[] fraction = {0.5, 0.7, 0.85, 1.0}; // share of the vanilla max level, by rank
        for (int i = 0; i < count; i++) {
            Optional<Holder.Reference<Enchantment>> holder = registry.getHolder(pool.get(i));
            if (holder.isEmpty()) continue;
            int max = holder.get().value().getMaxLevel();
            int level = Math.max(1, Math.min(max, (int) Math.round(max * fraction[rank.ordinal()])));
            stack.enchant(holder.get(), level);
        }

        // ---- bonus attributes ----
        double power = cfg.relicPower.get();
        List<AttrDef> defs = new ArrayList<>();
        if (weapon) {
            defs.add(new AttrDef(Attributes.ATTACK_DAMAGE, 1.5 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.ATTACK_SPEED, 0.25 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.MOVEMENT_SPEED, 0.04 * power, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            defs.add(new AttrDef(Attributes.MAX_HEALTH, 2.0 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.ENTITY_INTERACTION_RANGE, 0.5 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.LUCK, 1.0 * power, AttributeModifier.Operation.ADD_VALUE));
        } else {
            defs.add(new AttrDef(Attributes.MAX_HEALTH, 2.0 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.ARMOR, 1.0 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.ARMOR_TOUGHNESS, 1.0 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.KNOCKBACK_RESISTANCE, 0.05 * power, AttributeModifier.Operation.ADD_VALUE));
            defs.add(new AttrDef(Attributes.MOVEMENT_SPEED, 0.04 * power, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (slot == EquipmentSlot.FEET) {
                defs.add(new AttrDef(Attributes.SAFE_FALL_DISTANCE, 3.0 * power, AttributeModifier.Operation.ADD_VALUE));
            }
            if (slot == EquipmentSlot.HEAD) {
                defs.add(new AttrDef(Attributes.OXYGEN_BONUS, 1.0 * power, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        Collections.shuffle(defs, new Random(random.nextLong()));

        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(existing.modifiers());
        EquipmentSlotGroup group = weapon ? EquipmentSlotGroup.MAINHAND : EquipmentSlotGroup.bySlot(slot);
        int attrCount = Math.min(cfg.relicAttributes.get(), defs.size());
        for (int i = 0; i < attrCount; i++) {
            AttrDef d = defs.get(i);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MobAscension.MODID,
                    "relic_" + Long.toHexString(random.nextLong() & Long.MAX_VALUE));
            entries.add(new ItemAttributeModifiers.Entry(d.attribute(), new AttributeModifier(id, d.amount(), d.op()), group));
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(entries, existing.showInTooltip()));

        // ---- presentation ----
        String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
        Component name = Component.literal(prefix + " ").append(stack.getHoverName())
                .withStyle(Style.EMPTY.withColor(rank.format).withItalic(false));
        stack.set(DataComponents.CUSTOM_NAME, name);
        stack.set(DataComponents.RARITY, rank.rarity);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Relic of the " + cfg.displayName.get() + " champions")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)))));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(RELIC_TAG, true));
        return stack;
    }
}
