package com.elitechampions.champion;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.breeze.Breeze;

import java.util.Locale;

/** Every ability is built on vanilla mechanics (attributes, vanilla effects, vanilla projectiles). */
public enum ChampionAbility {
    // Creeper
    CREEPER_VOLATILE(Creeper.class),
    CREEPER_QUICK_FUSE(Creeper.class),
    CREEPER_PERSISTENT(Creeper.class),
    CREEPER_LAST_STAND(Creeper.class),
    // Skeleton (skeleton, stray, bogged, wither skeleton)
    SKELETON_SNIPER(AbstractSkeleton.class),
    SKELETON_RAPID_FIRE(AbstractSkeleton.class),
    SKELETON_PIERCING(AbstractSkeleton.class),
    SKELETON_FLAME_ARCHER(AbstractSkeleton.class),
    SKELETON_DEADEYE(AbstractSkeleton.class),
    // Zombie (zombie, husk, drowned, ...)
    ZOMBIE_REINFORCED(Zombie.class),
    ZOMBIE_SPRINTER(Zombie.class),
    ZOMBIE_INFECTIOUS(Zombie.class),
    ZOMBIE_REINFORCEMENTS(Zombie.class),
    ZOMBIE_BERSERKER(Zombie.class),
    // Spider (spider, cave spider)
    SPIDER_POUNCING(Spider.class),
    SPIDER_VENOMOUS(Spider.class),
    SPIDER_CLIMBER(Spider.class),
    SPIDER_WEB_WEAVER(Spider.class),
    SPIDER_NIGHT_HUNTER(Spider.class),
    // Others
    ENDERMAN_BLINK(EnderMan.class),
    WITCH_BARRAGE(Witch.class),
    SLIME_GIANT(Slime.class),
    DROWNED_TRIDENT(Drowned.class),
    PILLAGER_ARBALEST(Pillager.class),
    BLAZE_BARRAGE(Blaze.class),
    HUSK_STARVING(Husk.class),
    PHANTOM_FRENZY(Phantom.class),
    VINDICATOR_RAGE(Vindicator.class),
    BREEZE_GALE(Breeze.class);

    private final Class<? extends Mob> type;

    ChampionAbility(Class<? extends Mob> type) {
        this.type = type;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "elitechampions.ability." + id();
    }

    public boolean appliesTo(Mob mob) {
        return type.isInstance(mob);
    }

    public static ChampionAbility byId(String id) {
        try {
            return valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
