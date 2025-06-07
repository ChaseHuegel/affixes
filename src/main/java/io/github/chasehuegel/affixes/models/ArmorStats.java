package io.github.chasehuegel.affixes.models;

import org.bukkit.inventory.EquipmentSlotGroup;

public record ArmorStats(
    double armor,
    double toughness,
    double knockbackResistance,
    EquipmentSlotGroup slot
) {}
