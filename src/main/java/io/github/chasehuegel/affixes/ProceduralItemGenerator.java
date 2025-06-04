package io.github.chasehuegel.affixes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ProceduralItemGenerator {
    private final ProceduralMaterialDefinition[] definitions;

    public ProceduralItemGenerator(ProceduralMaterialDefinition[] definitions) {
        this.definitions = definitions;
    }

    public ItemStack Generate() {
        return ItemStack.of(Material.AIR);
    }
}
