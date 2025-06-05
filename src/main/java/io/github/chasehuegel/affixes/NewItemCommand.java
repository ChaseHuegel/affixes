package io.github.chasehuegel.affixes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NewItemCommand implements CommandExecutor {

    private final ItemGenerator itemGenerator;

    public NewItemCommand(ItemGenerator itemGenerator) {
        this.itemGenerator = itemGenerator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (!label.equals("new")) {
            return false;
        }

        ItemStack item = itemGenerator.generate();
        player.getInventory().addItem(item);
        return true;
    }
}
