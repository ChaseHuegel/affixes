package io.github.chasehuegel.affixes;

import net.kyori.adventure.text.format.NamedTextColor;
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
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("new")) {
            player.sendMessage("Usage: /affixes new [amount]");
            return true;
        }

        int amount = 1;
        if (args.length > 1) {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                player.sendMessage("Invalid amount: " + args[1]);
                return true;
            }
        }

        for (int i = 0; i < amount; i++) {
            ItemStack item = itemGenerator.generate();
            player.getInventory().addItem(item);
        }

        player.sendMessage("Generated " + amount + " items with affixes.");
        return true;
    }
}
