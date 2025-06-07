package io.github.chasehuegel.affixes.util;

import io.github.chasehuegel.affixes.models.ArmorStats;
import io.github.chasehuegel.affixes.models.ToolStats;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Map;

public final class VanillaStats {
    
    public static final Map<Material, ToolStats> TOOLS = Map.ofEntries(
        // Swords
        Map.entry(Material.WOODEN_SWORD,     new ToolStats(4.0, -2.4)),
        Map.entry(Material.STONE_SWORD,      new ToolStats(5.0, -2.4)),
        Map.entry(Material.IRON_SWORD,       new ToolStats(6.0, -2.4)),
        Map.entry(Material.GOLDEN_SWORD,     new ToolStats(4.0, -2.4)),
        Map.entry(Material.DIAMOND_SWORD,    new ToolStats(7.0, -2.4)),
        Map.entry(Material.NETHERITE_SWORD,  new ToolStats(8.0, -2.4)),

        // Axes
        Map.entry(Material.WOODEN_AXE,       new ToolStats(7.0, -3.2)),
        Map.entry(Material.STONE_AXE,        new ToolStats(9.0, -3.2)),
        Map.entry(Material.IRON_AXE,         new ToolStats(9.0, -3.1)),
        Map.entry(Material.GOLDEN_AXE,       new ToolStats(7.0, -3.0)),
        Map.entry(Material.DIAMOND_AXE,      new ToolStats(9.0, -3.0)),
        Map.entry(Material.NETHERITE_AXE,    new ToolStats(10.0, -3.0)),

        // Pickaxes
        Map.entry(Material.WOODEN_PICKAXE,   new ToolStats(2.0, -2.8)),
        Map.entry(Material.STONE_PICKAXE,    new ToolStats(3.0, -2.8)),
        Map.entry(Material.IRON_PICKAXE,     new ToolStats(4.0, -2.8)),
        Map.entry(Material.GOLDEN_PICKAXE,   new ToolStats(2.0, -2.8)),
        Map.entry(Material.DIAMOND_PICKAXE,  new ToolStats(5.0, -2.8)),
        Map.entry(Material.NETHERITE_PICKAXE,new ToolStats(6.0, -2.8)),

        // Shovels
        Map.entry(Material.WOODEN_SHOVEL,    new ToolStats(1.5, -3.0)),
        Map.entry(Material.STONE_SHOVEL,     new ToolStats(2.5, -3.0)),
        Map.entry(Material.IRON_SHOVEL,      new ToolStats(3.5, -3.0)),
        Map.entry(Material.GOLDEN_SHOVEL,    new ToolStats(1.5, -3.0)),
        Map.entry(Material.DIAMOND_SHOVEL,   new ToolStats(4.5, -3.0)),
        Map.entry(Material.NETHERITE_SHOVEL, new ToolStats(5.5, -3.0)),

        // Hoes
        Map.entry(Material.WOODEN_HOE,       new ToolStats(1.0, -3.0)),
        Map.entry(Material.STONE_HOE,        new ToolStats(1.0, -2.0)),
        Map.entry(Material.IRON_HOE,         new ToolStats(1.0, -1.0)),
        Map.entry(Material.GOLDEN_HOE,       new ToolStats(1.0, -3.0)),
        Map.entry(Material.DIAMOND_HOE,      new ToolStats(1.0, 0.0)),
        Map.entry(Material.NETHERITE_HOE,    new ToolStats(1.0, 0.0)),

        // Other
        Map.entry(Material.TRIDENT, new ToolStats(8.0, -2.9)),
        Map.entry(Material.MACE,    new ToolStats(6.0, -3.4))
    );

    public static final Map<Material, ArmorStats> ARMOR = Map.ofEntries(
        // Leather Armor
        Map.entry(Material.LEATHER_HELMET,     new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.LEATHER_CHESTPLATE, new ArmorStats(3.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.LEATHER_LEGGINGS,   new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.LEATHER_BOOTS,      new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Chainmail Armor
        Map.entry(Material.CHAINMAIL_HELMET,     new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.CHAINMAIL_CHESTPLATE, new ArmorStats(5.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.CHAINMAIL_LEGGINGS,   new ArmorStats(4.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.CHAINMAIL_BOOTS,      new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Iron Armor
        Map.entry(Material.IRON_HELMET,     new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.IRON_CHESTPLATE, new ArmorStats(6.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.IRON_LEGGINGS,   new ArmorStats(5.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.IRON_BOOTS,      new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Gold Armor
        Map.entry(Material.GOLDEN_HELMET,     new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.GOLDEN_CHESTPLATE, new ArmorStats(5.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.GOLDEN_LEGGINGS,   new ArmorStats(3.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.GOLDEN_BOOTS,      new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Diamond Armor
        Map.entry(Material.DIAMOND_HELMET,     new ArmorStats(3.0, 2.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.DIAMOND_CHESTPLATE, new ArmorStats(8.0, 2.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.DIAMOND_LEGGINGS,   new ArmorStats(6.0, 2.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.DIAMOND_BOOTS,      new ArmorStats(3.0, 2.0, 0.0, EquipmentSlotGroup.FEET)),

        // Netherite Armor
        Map.entry(Material.NETHERITE_HELMET,     new ArmorStats(3.0, 3.0, 0.1, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.NETHERITE_CHESTPLATE, new ArmorStats(8.0, 3.0, 0.1,  EquipmentSlotGroup.CHEST)),
        Map.entry(Material.NETHERITE_LEGGINGS,   new ArmorStats(6.0, 3.0, 0.1,  EquipmentSlotGroup.LEGS)),
        Map.entry(Material.NETHERITE_BOOTS,      new ArmorStats(3.0, 3.0, 0.1,  EquipmentSlotGroup.FEET)),

        // Other
        Map.entry(Material.TURTLE_HELMET, new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD))
    );
}
