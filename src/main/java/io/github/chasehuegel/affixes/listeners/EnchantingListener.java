package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import io.github.chasehuegel.affixes.util.AffixesInspector;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

public class EnchantingListener implements Listener {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final Random random = new Random();

    public EnchantingListener(AffixesPlugin plugin, ItemGenerator itemGenerator) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        ItemStack item = event.getItem();

        double roll = random.nextDouble();
        double chance = plugin.getConfig().getDouble("sources.enchanting.chance");

        int enchantmentLevelTotal = 0;
        int rarityLevel = 0;
        if (roll <= chance && !AffixesInspector.hasAnyAffixes(item)) {
            enchantmentLevelTotal = getEnchantmentLevelTotal(event.getEnchantsToAdd());
            rarityLevel = itemGenerator.getWeightedRandomRarityLevel(enchantmentLevelTotal);
            itemGenerator.generateFrom(item, rarityLevel);
        }

        if (plugin.inDev) {
            player.sendMessage("Roll: " + roll + " Chance: " + chance + " Enchant value: " + enchantmentLevelTotal + " Rarity level: " + rarityLevel);
        }
    }

    private int getEnchantmentLevelTotal(Map<Enchantment, Integer> enchantments) {
        float totalLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            totalLevels += (float) entry.getValue() / entry.getKey().getMaxLevel();
        }

        return Math.round(totalLevels);
    }
}
