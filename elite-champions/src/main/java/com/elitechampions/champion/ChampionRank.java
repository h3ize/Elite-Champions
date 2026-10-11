package com.elitechampions.champion;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Champion ranks. The "base" values are what you get at stat level 5 (the config default);
 * level 10 doubles them, level 0 disables the stat.
 */
public enum ChampionRank {
    COMMON("common", ChatFormatting.WHITE, 0, 0, 0, 0, 0, 0, null),
    UNCOMMON("uncommon", ChatFormatting.GREEN, 0.5, 2, 0.05, 0.25, 0.10, 0.0, () -> ParticleTypes.HAPPY_VILLAGER),
    RARE("rare", ChatFormatting.AQUA, 1.0, 4, 0.10, 0.50, 0.25, 0.1, () -> ParticleTypes.ENCHANT),
    EPIC("epic", ChatFormatting.LIGHT_PURPLE, 2.0, 6, 0.15, 1.00, 0.50, 0.25, () -> ParticleTypes.WITCH),
    LEGENDARY("legendary", ChatFormatting.GOLD, 4.0, 10, 0.20, 1.50, 0.80, 0.5, () -> ParticleTypes.FLAME);

    private final String id;
    private final ChatFormatting color;
    private final double baseHealth;     // +% of base max health (1.0 = +100%)
    private final double baseArmor;      // flat armor points
    private final double baseSpeed;      // +% movement speed
    private final double baseDamage;     // +% attack damage
    private final double baseKnockback;  // knockback resistance (0..1)
    private final double baseRegen;      // HP per second
    private final Supplier<ParticleOptions> particle;

    ChampionRank(String id, ChatFormatting color, double baseHealth, double baseArmor, double baseSpeed,
                 double baseDamage, double baseKnockback, double baseRegen, Supplier<ParticleOptions> particle) {
        this.id = id;
        this.color = color;
        this.baseHealth = baseHealth;
        this.baseArmor = baseArmor;
        this.baseSpeed = baseSpeed;
        this.baseDamage = baseDamage;
        this.baseKnockback = baseKnockback;
        this.baseRegen = baseRegen;
        this.particle = particle;
    }

    public String id() { return id; }
    public ChatFormatting color() { return color; }
    public double baseHealth() { return baseHealth; }
    public double baseArmor() { return baseArmor; }
    public double baseSpeed() { return baseSpeed; }
    public double baseDamage() { return baseDamage; }
    public double baseKnockback() { return baseKnockback; }
    public double baseRegen() { return baseRegen; }
    public ParticleOptions particle() { return particle == null ? null : particle.get(); }
    public String translationKey() { return "elitechampions.rank." + id; }

    public Rarity itemRarity() {
        return switch (this) {
            case UNCOMMON -> Rarity.UNCOMMON;
            case RARE -> Rarity.RARE;
            case EPIC, LEGENDARY -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
    }

    public static List<ChampionRank> champions() {
        return List.of(UNCOMMON, RARE, EPIC, LEGENDARY);
    }

    public static ChampionRank byId(String id) {
        for (ChampionRank r : values()) {
            if (r.id.equals(id.toLowerCase(Locale.ROOT))) return r;
        }
        return COMMON;
    }
}
