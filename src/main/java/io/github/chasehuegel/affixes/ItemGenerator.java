package io.github.chasehuegel.affixes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemGenerator {
    private final MaterialDefinition[] definitions;

    public ItemGenerator(MaterialDefinition[] definitions) {
        this.definitions = definitions;
    }

    public ItemStack Generate() {
        return ItemStack.of(Material.AIR);
    }
}
