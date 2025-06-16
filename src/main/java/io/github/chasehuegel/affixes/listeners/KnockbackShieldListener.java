package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class KnockbackShieldListener implements Listener {

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

        int knockbackLevel = Utils.getHeldShieldEnchantLevel(player, Enchantment.KNOCKBACK);
        if (knockbackLevel == 0) {
            return;
        }

        Vector playerVector = player.getLocation().toVector();
        Vector attackerVector = attacker.getLocation().toVector();
        Vector knockbackVector = attackerVector.subtract(playerVector)
                .normalize()
                .multiply(knockbackLevel * 0.6)
                .setY(0.35);

        attacker.setVelocity(knockbackVector);
        player.getWorld().playSound(player.getLocation(), "entity.player.attack.knockback", 1.0f, 1.0f);
    }
}
