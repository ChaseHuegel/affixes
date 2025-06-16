package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class ThornsShieldListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }

        if (!player.isBlocking()) {
            return;
        }

        int thornsLevel = Utils.getHeldShieldEnchantLevel(player, Enchantment.THORNS);
        if (thornsLevel == 0) {
            return;
        }

        if (random.nextFloat() > thornsLevel * 0.15) {
            return;
        }

        attacker.damage(random.nextInt(1, 5), player);
    }
}
