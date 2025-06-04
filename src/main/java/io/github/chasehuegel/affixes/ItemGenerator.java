package io.github.chasehuegel.affixes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class ItemGenerator {

    private final Random random = new Random();
    private final AffixGenerator affixGenerator;
    private final List<MaterialDefinition> definitions;

    public ItemGenerator(AffixGenerator affixGenerator, List<MaterialDefinition> definitions) {
        this.affixGenerator = affixGenerator;
        this.definitions = definitions;
    }

    public ItemStack generate() {
        return ItemStack.of(Material.AIR);
    }
}
