package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.models.ToolStats;
import io.github.chasehuegel.affixes.util.VanillaStats;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ArrowAttackDamageListener implements Listener {

    private static final NamespacedKey BONUS_ARROW_DAMAGE_KEY = new NamespacedKey(AffixesPlugin.NAMESPACE, "bonus_arrow_damage");

    private final AffixesPlugin plugin;

    public ArrowAttackDamageListener(AffixesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getProjectile() instanceof AbstractArrow arrow)) {
            return;
        }

        AttributeInstance attackAttribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttribute == null) {
            return;
        }

        double bonusDamage = attackAttribute.getValue() - 1; //  Subtract 1 for fist damage

        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ToolStats toolStats = VanillaStats.TOOLS.get(mainHandItem.getType());
        if (toolStats != null) {
            //  Subtract tool/weapon damage. This may subtract too much if there are negative modifiers present,
            //  but this exists to prevent cheesing by putting a bow/crossbow in your offhand for extra damage.
            bonusDamage -= toolStats.attackDamage();
        }

        if (bonusDamage == 0) {
            return;
        }

        arrow.getPersistentDataContainer().set(BONUS_ARROW_DAMAGE_KEY, PersistentDataType.DOUBLE, bonusDamage);
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }

        Double bonusDamage = arrow.getPersistentDataContainer().get(BONUS_ARROW_DAMAGE_KEY, PersistentDataType.DOUBLE);
        if (bonusDamage == null) {
            return;
        }

        event.setDamage(event.getDamage() + bonusDamage);

        if (plugin.inDev && arrow.getShooter() instanceof Player player) {
            player.sendMessage("Bonus arrow damage: " + bonusDamage);
        }
    }
}
