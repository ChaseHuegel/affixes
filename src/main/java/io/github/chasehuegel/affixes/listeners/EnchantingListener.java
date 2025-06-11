package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import io.github.chasehuegel.affixes.models.Rarity;
import io.github.chasehuegel.affixes.util.AffixesMeta;
import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EnchantingListener implements Listener {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final List<Rarity> rarities;
    private final Random random = new Random();

    public EnchantingListener(AffixesPlugin plugin, ItemGenerator itemGenerator, List<Rarity> rarities) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;
        this.rarities = rarities;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        ItemStack item = event.getItem();

        if (AffixesMeta.hasAnyAffixes(item)) {
            return;
        }

        double roll = random.nextDouble();

        double baseChance = plugin.getConfig().getDouble("sources.enchanting.chance");
        double luckBonus = Utils.getPlayerLuck(player) * plugin.getConfig().getDouble("sources.enchanting.luckBonusChance");
        double chance = baseChance + luckBonus;

        int enchantmentLevelTotal = 0;
        int rarityLevel = 0;
        if (roll <= chance && !AffixesMeta.hasAnyAffixes(item)) {
            enchantmentLevelTotal = getEnchantmentLevelTotal(event.getEnchantsToAdd());
            rarityLevel = itemGenerator.getWeightedRandomRarityLevel(enchantmentLevelTotal, rarities.size() - 2);
            itemGenerator.generateFrom(item, rarityLevel);
        }

        if (plugin.inDev) {
            player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " luck: " + luckBonus + ") Enchant value: " + enchantmentLevelTotal + " Rarity level: " + rarityLevel);
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
