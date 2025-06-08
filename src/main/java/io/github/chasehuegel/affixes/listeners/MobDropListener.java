package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import io.github.chasehuegel.affixes.models.MobDrop;
import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobDropListener implements Listener {

    private final NamespacedKey fromSpawnerKey = new NamespacedKey(AffixesPlugin.NAMESPACE, "from_spawner");
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

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            LivingEntity entity = event.getEntity();
            PersistentDataContainer data = entity.getPersistentDataContainer();
            data.set(fromSpawnerKey, PersistentDataType.BOOLEAN, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity victim = event.getEntity();
        Entity attacker = event.getDamageSource().getCausingEntity();
        if (!(attacker instanceof Player player)) {
            return;
        }

        if (plugin.getConfig().getBoolean("sources.drops.blockSpawners")) {
            PersistentDataContainer data = victim.getPersistentDataContainer();
            Boolean value = data.get(fromSpawnerKey, PersistentDataType.BOOLEAN);
            if (value != null && value) {
                return;
            }
        }

        MobDrop mobDrop = mobDrops.get(victim.getType());
        if (mobDrop == null) {
            return;
        }

        double roll = random.nextDouble();

        double baseChance = mobDrop.chance();
        double lootingBonus = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING) * mobDrop.lootingBonusChance();
        double luckBonus = Utils.getPlayerLuck(player) * plugin.getConfig().getDouble("sources.drops.luckBonusChance");
        double chance = baseChance + lootingBonus + luckBonus;

        if (plugin.inDev) {
            player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " looting: " + lootingBonus + " luck: " + luckBonus + ")");
        }

        if (roll > chance) {
            return;
        }

        ItemStack item = itemGenerator.generate();
        victim.getWorld().dropItemNaturally(victim.getLocation(), item);

        for (int i = 1; i < plugin.getConfig().getInt("sources.drops.max"); i++) {
            roll = random.nextDouble();
            if (plugin.inDev) {
                player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " looting: " + lootingBonus + " luck: " + luckBonus + ")");
            }

            if (roll > chance) {
                break;
            }

            item = itemGenerator.generate();
            victim.getWorld().dropItemNaturally(victim.getLocation(), item);
        }
    }
}
