package io.github.chasehuegel.affixes.listeners;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import io.github.chasehuegel.affixes.util.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContainerListener implements Listener {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final Random random = new Random();

    public ContainerListener(AffixesPlugin plugin, ItemGenerator itemGenerator) {
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
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST && block.getType() != Material.BARREL) {
            return;
        }

        Container container = (Container) block.getState();
        if (player.getWorld().getBlockAt(container.getLocation().add(0, 1, 0)).getType().isOccluding()) {
            return;
        }

        //  Only add loot to natural containers that haven't been looted, unless dev testing
        Lootable lootable = (Lootable) block.getState();
        if (!plugin.inDev && lootable.getLootTable() == null) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, new ContainerLootRunnable(player, container));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block.getType() != Material.DECORATED_POT) {
            return;
        }

        //  Only drop loot from natural pots that haven't been looted, unless dev testing
        Lootable lootable = (Lootable) block.getState();
        if (!plugin.inDev && lootable.getLootTable() == null) {
            return;
        }

        double roll = random.nextDouble();

        double baseChance = plugin.getConfig().getDouble("sources.containers.chance");
        double luckBonus = Utils.getPlayerLuck(player) * plugin.getConfig().getDouble("sources.containers.luckBonusChance");
        double chance = baseChance + luckBonus;

        if (plugin.inDev) {
            player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " luck: " + luckBonus + ")");
        }

        if (roll > chance) {
            return;
        }

        ItemStack item = itemGenerator.generate();
        player.getWorld().dropItemNaturally(block.getLocation(), item);

        for (int i = 1; i < plugin.getConfig().getInt("sources.drops.max"); i++) {
            roll = random.nextDouble();
            if (plugin.inDev) {
                player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " luck: " + luckBonus + ")");
            }

            if (roll > chance) {
                break;
            }

            item = itemGenerator.generate();
            player.getWorld().dropItemNaturally(block.getLocation(), item);
        }
    }

    private class ContainerLootRunnable implements Runnable {
        private final Player player;
        private final Container container;

        public ContainerLootRunnable(Player player, Container container) {
            this.player = player;
            this.container = container;
        }

        @Override
        public void run() {
            double roll = random.nextDouble();

            double baseChance = plugin.getConfig().getDouble("sources.containers.chance");
            double luckBonus = Utils.getPlayerLuck(player) * plugin.getConfig().getDouble("sources.containers.luckBonusChance");
            double chance = baseChance + luckBonus;

            if (plugin.inDev) {
                player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " luck: " + luckBonus + ")");
            }

            if (roll > chance) {
                return;
            }

            List<Integer> emptySlots = new ArrayList<>();
            ItemStack[] contents = container.getInventory().getContents();
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
            container.getInventory().setItem(slot, item);

            for (int i = 1; i < totalEmptySlots; i++) {
                roll = random.nextDouble();
                if (plugin.inDev) {
                    player.sendMessage("Roll: " + roll + " Chance: " + chance + " (base: " + baseChance + " luck: " + luckBonus + ")");
                }

                if (roll > chance) {
                    break;
                }

                slot = takeRandomSlot(emptySlots);
                item = itemGenerator.generate();
                container.getInventory().setItem(slot, item);
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
