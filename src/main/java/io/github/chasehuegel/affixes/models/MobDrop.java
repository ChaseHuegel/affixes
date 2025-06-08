package io.github.chasehuegel.affixes.models;

import java.util.Map;

import org.bukkit.entity.EntityType;

public record MobDrop(
        EntityType type,
        double chance,
        double lootingBonusChance
) {

    public static MobDrop fromMap(Map<?, ?> map) {
        EntityType type = EntityType.valueOf(((String) map.get("type")).toUpperCase());

        double chance = map.get("chance") instanceof Number
                ? ((Number) map.get("chance")).doubleValue()
                : 0.025;

        double lootingBonusChance = map.get("lootingBonusChance") instanceof Number
                ? ((Number) map.get("lootingBonusChance")).doubleValue()
                : 0.01;

        return new MobDrop(type, chance, lootingBonusChance);
    }
}