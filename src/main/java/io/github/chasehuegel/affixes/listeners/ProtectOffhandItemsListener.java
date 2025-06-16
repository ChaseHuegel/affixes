package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.util.AffixesMeta;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ProtectOffhandItemsListener implements Listener {

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (allowedToPlace(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        //  OK if the block is interactable
        //noinspection deprecation
        if (clickedBlock.getType().isInteractable()) {
            return;
        }

        if (allowedToPlace(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
    }

    private boolean allowedToPlace(@NotNull Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean mainHandHasAffixes = AffixesMeta.hasAnyAffixes(mainHand);
        boolean offHandHasAffixes = AffixesMeta.hasAnyAffixes(offHand);

        //  Nothing to protect if the player isn't holding affixed items
        if (!mainHandHasAffixes && !offHandHasAffixes) {
            return true;
        }

        //  If the main hand is a block and not an affixed item,
        //  then the offhand should be safe from placement.
        if (!mainHandHasAffixes && mainHand.getType() != Material.AIR && mainHand.getType().isBlock()) {
            return true;
        }

        return false;
    }
}
