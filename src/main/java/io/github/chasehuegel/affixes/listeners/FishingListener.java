package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class FishingListener implements Listener {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final Random random = new Random();

    public FishingListener(AffixesPlugin plugin, ItemGenerator itemGenerator) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Entity caught = event.getCaught();
        if (!(caught instanceof Item itemEntity) || event.getExpToDrop() <= 0) {
            return;
        }

        double roll = random.nextDouble();

        FileConfiguration config = plugin.getConfig();
        double baseChance = config.getDouble("sources.fishing.chance");
        double luckOfTheSeaBonus = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA) * config.getDouble("sources.fishing.luckOfTheSeaBonusChance");
        double luckBonus = Utils.getPlayerLuck(player) * plugin.getConfig().getDouble("sources.fishing.luckBonusChance");
        double chance = baseChance + luckOfTheSeaBonus + luckBonus;

        if (plugin.inDev) {
            player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " luckOfTheSea: " + luckOfTheSeaBonus + " luck: " + luckBonus + ")");
        }

        if (roll > chance) {
            return;
        }

        ItemStack item = itemGenerator.generate();
        itemEntity.setItemStack(item);
    }
}
