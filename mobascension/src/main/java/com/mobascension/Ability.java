package com.mobascension;

/**
 * Special abilities a champion can roll. Each is individually configurable
 * (enabled, minimum rank, power and cooldown) in the config file / GUI.
 */
public enum Ability {
    VAMPIRIC("vampiric", "Vampiric", 1, 0.35, 0),     // power = fraction of damage healed
    THORNED("thorned", "Thorned", 1, 0.30, 0),        // power = fraction of melee damage reflected
    FROSTBITE("frostbite", "Frostbite", 1, 5.0, 0),   // power = seconds of slowness/freeze
    VENOMOUS("venomous", "Venomous", 1, 4.0, 0),      // power = seconds of poison
    BERSERKER("berserker", "Berserker", 2, 0.5, 0),   // power = health fraction that triggers rage
    AEGIS("aegis", "Aegis", 2, 8.0, 100),             // power = max shield HP, cooldown = recharge ticks
    BLINKER("blinker", "Blinker", 2, 16.0, 160),      // power = teleport range, cooldown = ticks
    VOLATILE("volatile", "Volatile", 3, 2.5, 0),      // power = explosion size on death
    SPLITTING("splitting", "Splitting", 3, 2.0, 0),   // power = minions spawned on death
    STORMCALLER("stormcaller", "Stormcaller", 4, 5.0, 400); // power = bolt damage, cooldown = ticks

    public final String id;
    public final String displayName;
    public final int defaultMinRank;
    public final double defaultPower;
    public final int defaultCooldown;

    Ability(String id, String displayName, int defaultMinRank, double defaultPower, int defaultCooldown) {
        this.id = id;
        this.displayName = displayName;
        this.defaultMinRank = defaultMinRank;
        this.defaultPower = defaultPower;
        this.defaultCooldown = defaultCooldown;
    }

    public static Ability byId(String id) {
        for (Ability a : values()) {
            if (a.id.equals(id)) return a;
        }
        return null;
    }
}
