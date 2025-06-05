package io.github.chasehuegel.affixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class ItemGenerator {

    private final Random random = new Random();
    private final AffixGenerator affixGenerator;
    private final List<MaterialDefinition> materialDefinitions;
    private final List<Rarity> rarities;

    public ItemGenerator(AffixGenerator affixGenerator, List<MaterialDefinition> materialDefinitions, List<Rarity> rarities) {
        this.affixGenerator = affixGenerator;
        this.materialDefinitions = materialDefinitions;
        this.rarities = rarities;
    }

    public ItemStack generate(MaterialDefinition materialDefinition) {
        String itemName = getRandomValue(materialDefinition.names);
        String materialName = getRandomValue(materialDefinition.materials);
        int rarityLevel = getWeightedRandomRarityLevel();
        Rarity rarity = rarities.get(rarityLevel);

        var material = Material.matchMaterial(materialName);
        if (material == null) {
            return null;
        }

        NamedTextColor rarityTextColor = NamedTextColor.NAMES.value(rarity.color.toLowerCase());
        if (rarityTextColor == null) {
            return null;
        }

        var item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        Component nameComponent = Component.text(itemName).color(rarityTextColor);
        meta.displayName(nameComponent);

        int maxAffixes = Math.max(1, rarityLevel);
        int affixCount = maxAffixes > 1 ? random.nextInt(1, maxAffixes) : 1;
        boolean appliedAnyAffixes = false;
        for (int i = 0; i < affixCount; i++) {
            String slotName = getRandomValue(materialDefinition.slots);
            appliedAnyAffixes |= affixGenerator.generateAffix(meta, slotName, rarityLevel);
        }

        if (!appliedAnyAffixes) {
            return null;
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack generate() {
        MaterialDefinition materialDefinition = getRandomValue(materialDefinitions);
        return generate(materialDefinition);
    }

    private <T> T getRandomValue(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private int getWeightedRandomRarityLevel() {
        float totalWeight = 0f;
        for (Rarity rarity : rarities) {
            totalWeight += rarity.chance;
        }

        float r = new Random().nextFloat() * totalWeight;
        float runningSum = 0f;

        for (int i = 0; i < rarities.size(); i++) {
            runningSum += rarities.get(i).chance;
            if (r <= runningSum) {
                return i;
            }
        }

        return random.nextInt(rarities.size());
    }
}
