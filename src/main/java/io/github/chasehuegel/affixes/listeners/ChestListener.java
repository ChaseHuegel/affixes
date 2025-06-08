package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChestListener implements Listener {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final Random random = new Random();

    public ChestListener(AffixesPlugin plugin, ItemGenerator itemGenerator) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        assert block != null;
        if (block.getType() != Material.CHEST) {
            return;
        }

        Chest chest = (Chest) block.getState();
        if (player.getWorld().getBlockAt(chest.getLocation().add(0, 1, 0)).getType().isOccluding()) {
            return;
        }

        //  Only add loot to natural chests that haven't been looted, unless dev testing
        if (!plugin.inDev && chest.getLootTable() == null) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, new ChestLootRunnable(player, chest));
    }

    private class ChestLootRunnable implements Runnable {
        private final Player player;
        private final Chest chest;

        public ChestLootRunnable(Player player, Chest chest) {
            this.player = player;
            this.chest = chest;
        }

        @Override
        public void run() {
            double roll = random.nextDouble();
            double chance = plugin.getConfig().getDouble("sources.chests.chance");
            if (plugin.inDev) {
                player.sendMessage("Roll: " + roll + " Chance: " + chance);
            }

            if (roll > chance) {
                return;
            }

            List<Integer> emptySlots = new ArrayList<>();
            ItemStack[] contents = chest.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    continue;
                }

                emptySlots.add(i);
            }

            int totalEmptySlots = emptySlots.size();
            if (emptySlots.isEmpty()) {
                return;
            }

            int slot = takeRandomSlot(emptySlots);
            ItemStack item = itemGenerator.generate();
            chest.getInventory().setItem(slot, item);

            for (int i = 1; i < totalEmptySlots; i++) {
                roll = random.nextDouble();
                if (plugin.inDev) {
                    player.sendMessage("Roll: " + roll + " Chance: " + chance);
                }

                if (roll > chance) {
                    break;
                }

                slot = takeRandomSlot(emptySlots);
                item = itemGenerator.generate();
                chest.getInventory().setItem(slot, item);
            }
        }

        private int takeRandomSlot(List<Integer> choices) {
            int index = random.nextInt(choices.size());
            int slot = choices.get(index);
            choices.remove(index);
            return slot;
        }
    }
}
