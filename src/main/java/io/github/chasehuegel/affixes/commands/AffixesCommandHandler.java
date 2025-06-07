package io.github.chasehuegel.affixes.commands;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.generators.ItemGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AffixesCommandHandler implements CommandExecutor, TabCompleter {

    private final AffixesPlugin plugin;
    private final ItemGenerator itemGenerator;

    public AffixesCommandHandler(AffixesPlugin plugin, ItemGenerator itemGenerator) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("Usage: /affixes <give|reload|help>");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            return onGiveCommand(sender, command, label, args);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return onReloadCommand(sender, command, label, args);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            var options = new ArrayList<String>();
            options.add("give");
            options.add("reload");
            options.add("help");
            return options;
        }

        if (args[0].equalsIgnoreCase("give")) {
            return onGiveTabComplete(sender, command, label, args);
        }

        return List.of();
    }

    private boolean onGiveCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("affixes.commands.give")) {
            sender.sendMessage(Component.text("Insufficient permissions.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Give a player a number of generated items.");
            sender.sendMessage("Usage: /affixes give <player> [amount]");
            return true;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(Component.text("Player " + args[1] + " not found.").color(NamedTextColor.RED));
            sender.sendMessage("Usage: /affixes give <player> [amount]");
            return true;
        }

        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                amount = 0;
            }
        }

        if (amount <= 0) {
            sender.sendMessage(Component.text("Amount must be greater than zero.").color(NamedTextColor.RED));
            sender.sendMessage("Usage: /affixes give <player> [amount]");
            return true;
        }

        int itemsGenerated = 0;
        for (int i = 0; i < amount; i++) {
            ItemStack item = itemGenerator.generate();
            if (item == null) {
                sender.sendMessage(Component.text("Failed generate an item to give " + player.getName()).color(NamedTextColor.RED));
                continue;
            }

            player.getInventory().addItem(item);
            itemsGenerated++;
        }

        sender.sendMessage("Gave " + player.getName() + " " + itemsGenerated + " items with affixes.");
        return true;
    }

    private @Nullable List<String> onGiveTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 2) {
            return null;
        }

        if (args.length == 3) {
            return List.of("1", "8", "16", "32");
        }

        return List.of();
    }

    private boolean onReloadCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("affixes.commands.reload")) {
            sender.sendMessage(Component.text("Insufficient permissions.").color(NamedTextColor.RED));
            return true;
        }

        sender.sendMessage("Reloading Affixes...");
        plugin.reload();
        sender.sendMessage("Reloaded Affixes.");
        return true;
    }
}
