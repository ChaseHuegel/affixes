package io.github.chasehuegel.affixes.util;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utils {

    public static double getPlayerLuck(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.LUCK);
        if (attribute == null) {
            return 0;
        }

        return attribute.getValue();
    }

    public static int getHeldShieldEnchantLevel(Player player, Enchantment enchantment) {
        ItemStack mainHandItem = player.getInventory().getItemInOffHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (mainHandItem.getType() != Material.SHIELD && offHandItem.getType() != Material.SHIELD) {
            return 0;
        }

        int enchantLevel = 0;
        if (mainHandItem.getType() == Material.SHIELD && mainHandItem.containsEnchantment(enchantment)) {
            enchantLevel = mainHandItem.getEnchantmentLevel(enchantment);
        }

        if (offHandItem.getType() == Material.SHIELD && offHandItem.containsEnchantment(enchantment)) {
            int level = offHandItem.getEnchantmentLevel(enchantment);
            if (level > enchantLevel) {
                enchantLevel = level;
            }
        }

        return enchantLevel;
    }

    public static int getArmorEnchantLevel(Player player, Enchantment enchantment) {
        int enchantLevel = 0;

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (!item.containsEnchantment(enchantment)) {
                continue;
            }

            enchantLevel = Math.max(item.getEnchantmentLevel(enchantment), enchantLevel);
        }

        return enchantLevel;
    }
}
