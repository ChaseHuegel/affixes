package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import io.github.chasehuegel.affixes.models.MobDrop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobDropListener implements Listener {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final Map<EntityType, MobDrop> mobDrops;
    private final Random random = new Random();

    public MobDropListener(AffixesPlugin plugin, ItemGenerator itemGenerator, List<MobDrop> mobDrops) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;

        this.mobDrops = new HashMap<>();
        for (MobDrop mobDrop : mobDrops) {
            this.mobDrops.put(mobDrop.type(), mobDrop);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity attacker = event.getDamageSource().getCausingEntity();
        if (!(attacker instanceof Player player)) {
            return;
        }

        Entity victim = event.getEntity();
        MobDrop mobDrop = mobDrops.get(victim.getType());
        if (mobDrop == null) {
            return;
        }

        double roll = random.nextDouble();

        double baseChance = mobDrop.chance();
        double bonusChance = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING) * mobDrop.lootingBonusChance();
        double chance = baseChance + bonusChance;

        if (plugin.inDev) {
            player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " bonus: " + bonusChance + ")");
        }

        if (roll > chance) {
            return;
        }

        ItemStack item = itemGenerator.generate();
        victim.getWorld().dropItemNaturally(victim.getLocation(), item);

        for (int i = 1; i < plugin.getConfig().getInt("sources.drops.max"); i++) {
            roll = random.nextDouble();
            if (plugin.inDev) {
                player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " bonus: " + bonusChance + ")");
            }

            if (roll > chance) {
                break;
            }

            item = itemGenerator.generate();
            victim.getWorld().dropItemNaturally(victim.getLocation(), item);
        }
    }
}
