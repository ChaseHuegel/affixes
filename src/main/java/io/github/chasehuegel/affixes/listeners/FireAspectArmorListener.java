package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FireAspectArmorListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }

        int fireAspectLevel = Utils.getArmorEnchantLevel(player, Enchantment.FIRE_ASPECT);
        if (fireAspectLevel == 0) {
            return;
        }

        attacker.setFireTicks(80 * fireAspectLevel);
    }
}
