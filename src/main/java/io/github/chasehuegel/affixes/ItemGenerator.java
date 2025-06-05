package io.github.chasehuegel.affixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
        MaterialInfo materialInfo = getWeightedRandomMaterialInfo(materialDefinition.materials);
        int rarityLevel = getWeightedRandomRarityLevel();
        Rarity rarity = rarities.get(rarityLevel);

        var material = Material.matchMaterial(materialInfo.name);
        if (material == null) {
            return null;
        }

        NamedTextColor rarityTextColor = NamedTextColor.NAMES.value(rarity.color.toLowerCase());
        if (rarityTextColor == null) {
            return null;
        }

        //  Init the item
        var item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        //  Set model data
        int modelData;
        if (materialInfo.modelMax <= materialInfo.modelMin) {
            modelData = materialInfo.modelMin;
        } else {
            modelData = random.nextInt(materialInfo.modelMin, materialInfo.modelMax + 1);
        }
        meta.setCustomModelData(modelData);

        //  Set base item name
        Component nameComponent = Component.text(itemName)
                .color(rarityTextColor)
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(nameComponent);

        //  Determine number of affixes
        int affixCount;
        if (rarity.maxAffixes <= rarity.minAffixes) {
            affixCount = rarity.minAffixes;
        } else {
            affixCount = random.nextInt(rarity.minAffixes, rarity.maxAffixes + 1);
        }

        //  Apply affixes to random allowed slots
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
            totalWeight += rarity.weight;
        }

        float r = new Random().nextFloat() * totalWeight;
        float runningSum = 0f;

        for (int i = 0; i < rarities.size(); i++) {
            runningSum += rarities.get(i).weight;
            if (r <= runningSum) {
                return i;
            }
        }

        return random.nextInt(rarities.size());
    }

    private MaterialInfo getWeightedRandomMaterialInfo(List<MaterialInfo> materialInfos) {
        float totalWeight = 0f;
        for (MaterialInfo materialInfo : materialInfos) {
            totalWeight += materialInfo.weight;
        }

        float r = new Random().nextFloat() * totalWeight;
        float runningSum = 0f;

        for (MaterialInfo materialInfo : materialInfos) {
            runningSum += materialInfo.weight;
            if (r <= runningSum) {
                return materialInfo;
            }
        }

        return getRandomValue(materialInfos);
    }
}
