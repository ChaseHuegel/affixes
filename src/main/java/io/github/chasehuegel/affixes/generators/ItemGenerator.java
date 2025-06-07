package io.github.chasehuegel.affixes.generators;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.models.*;
import io.github.chasehuegel.affixes.util.VanillaStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemGenerator {

    private final Random random = new Random();
    private final AffixesPlugin plugin;
    private final AffixGenerator affixGenerator;
    private final List<MaterialDefinition> materialDefinitions;
    private final List<Rarity> rarities;
    private final Map<String, ItemDefinition> itemDefinitions;
    private final Map<String, List<EnchantmentDefinition>> enchantmentDefinitions;
    private final Map<String, List<AttributeDefinition>> attributeDefinitions;
    private final Map<Integer, List<ItemDefinition>> itemDefinitionsByRarityLevel;
    private final Map<String, Integer> rarityLevelsByName;
    private final Map<String, Rarity> raritiesByName;

    public ItemGenerator(
        AffixesPlugin plugin,
        AffixGenerator affixGenerator,
        List<MaterialDefinition> materialDefinitions,
        Map<String, ItemDefinition> itemDefinitions,
        Map<String, List<EnchantmentDefinition>> enchantmentDefinitions,
        Map<String, List<AttributeDefinition>> attributeDefinitions,
        List<Rarity> rarities
    ) {
        this.plugin = plugin;
        this.affixGenerator = affixGenerator;
        this.materialDefinitions = materialDefinitions;
        this.itemDefinitions = itemDefinitions;
        this.enchantmentDefinitions = enchantmentDefinitions;
        this.attributeDefinitions = attributeDefinitions;
        this.rarities = rarities;

        raritiesByName = new HashMap<>();
        rarityLevelsByName = new HashMap<>();
        itemDefinitionsByRarityLevel = new HashMap<>();

        for (int i = 0; i < rarities.size(); i++) {
            Rarity rarity = rarities.get(i);

            rarityLevelsByName.put(rarity.name(), i);
            raritiesByName.put(rarity.name(), rarity);
            itemDefinitionsByRarityLevel.put(i, new ArrayList<>());
        }

        for (ItemDefinition itemDefinition : itemDefinitions.values()) {
            //  If rarity is undefined, it can be randomly generated
            //  so should be available to all rarities
            if (itemDefinition.rarity() == null || itemDefinition.rarity().isEmpty()) {
                for (int i = 0; i < rarities.size(); i++) {
                    itemDefinitionsByRarityLevel.get(i).add(itemDefinition);
                }
                continue;
            }

            int rarityLevel = rarityLevelsByName.get(itemDefinition.rarity());
            itemDefinitionsByRarityLevel.get(rarityLevel).add(itemDefinition);
        }
    }

    public ItemStack generate() {
        //  Allow up to 3 rerolls if generation fails to create an item
        for (int i = 0; i < 3; i++) {
            int rarityLevel = getWeightedRandomRarityLevel();

            List<ItemDefinition> itemDefinitionsForRarity = itemDefinitionsByRarityLevel.get(rarityLevel);

            //  Pick a material or item def with equal weight to all items
            int totalDefinitionChoices = materialDefinitions.size() + itemDefinitionsForRarity.size();
            int definitionIndex = random.nextInt(totalDefinitionChoices);

            //  Within range of material defs
            if (definitionIndex < materialDefinitions.size()) {
                MaterialDefinition materialDefinition = materialDefinitions.get(definitionIndex);
                ItemStack item = generate(materialDefinition, rarityLevel);
                if (item == null) {
                    //  Failed. Try rerolling.
                    continue;
                }

                return item;
            }

            //  Else, within range of item defs
            definitionIndex -= materialDefinitions.size();
            ItemDefinition itemDefinition = itemDefinitionsForRarity.get(definitionIndex);
            ItemStack item = generate(itemDefinition);
            if (item == null) {
                //  Failed. Try rerolling.
                continue;
            }

            return item;
        }

        return null;
    }

    public ItemStack generate(MaterialDefinition materialDefinition) {
        int rarityLevel = getWeightedRandomRarityLevel();
        return generate(materialDefinition, rarityLevel);
    }

    public ItemStack generate(MaterialDefinition materialDefinition, int rarityLevel) {
        String itemName = getRandomValue(materialDefinition.names());
        MaterialInfo materialInfo = getWeightedRandomMaterialInfo(materialDefinition.materials());
        Rarity rarity = rarities.get(rarityLevel);

        var effectOptions = new EffectOptions(true, rarity.minAffixes(), rarity.maxAffixes(), null, null, null);

        return generate(itemName, materialInfo, rarityLevel, rarity, materialDefinition.slots(), effectOptions);
    }

    public ItemStack generate(ItemDefinition itemDefinition) {
        Rarity rarity;
        int rarityLevel;

        //  Get rarity if defined
        if (itemDefinition.rarity() != null && !itemDefinition.rarity().isEmpty()) {
            rarity = raritiesByName.get(itemDefinition.rarity());
            if (rarity == null) {
                plugin.getLogger().warning("Unknown rarity: " + itemDefinition.rarity());
                return null;
            }

            rarityLevel = rarityLevelsByName.get(itemDefinition.rarity());
        } else {
            //  Undefined rarity is randomized
            rarityLevel = getWeightedRandomRarityLevel();
            rarity = rarities.get(rarityLevel);
        }

        //  Collect any enchantment defs by rarity
        List<EnchantmentDefinition> itemDefEnchantments = null;
        if (itemDefinition.enchantments() != null) {
            itemDefEnchantments = new ArrayList<>();
            for (String key : itemDefinition.enchantments()) {
                itemDefEnchantments.add(enchantmentDefinitions.get(key).get(rarityLevel));
            }
        }

        //  Collect any attribute defs by rarity
        List<AttributeDefinition> itemDefAttributes = null;
        if (itemDefinition.attributes() != null) {
            itemDefAttributes = new ArrayList<>();
            for (String key : itemDefinition.attributes()) {
                itemDefAttributes.add(attributeDefinitions.get(key).get(rarityLevel));
            }
        }

        //  negative affix count will use the rarity's options
        int itemDefMinRandomAffixes = 0;
        if (itemDefinition.effectOptions().minRandomAffixes() < 0) {
            itemDefMinRandomAffixes = rarity.minAffixes();
        }

        int itemDefMaxRandomAffixes = 0;
        if (itemDefinition.effectOptions().maxRandomAffixes() < 0) {
            itemDefMaxRandomAffixes = rarity.maxAffixes();
        }

        //  Merge with any options the item def specifies
        var effectOptions = new EffectOptions(false, itemDefMinRandomAffixes, itemDefMaxRandomAffixes, null, itemDefEnchantments, itemDefAttributes);
        effectOptions = effectOptions.merge(itemDefinition.effectOptions());

        return generate(itemDefinition.name(), itemDefinition.material(), rarityLevel, rarity, itemDefinition.slots(), effectOptions);
    }

    public ItemStack generate(String itemName, MaterialInfo materialInfo, int rarityLevel, Rarity rarity, List<String> allowedSlotNames, EffectOptions effectOptions) {
        var material = Material.matchMaterial(materialInfo.name());
        if (material == null) {
            plugin.getLogger().warning("Unknown material: " + materialInfo.name());
            return null;
        }

        NamedTextColor rarityTextColor = NamedTextColor.NAMES.value(rarity.color().toLowerCase());
        if (rarityTextColor == null) {
            plugin.getLogger().warning("Unknown rarity color: " + rarity.color());
            return null;
        }

        //  Init the item
        var item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        //  Set any tool stats
        ToolStats toolStats = VanillaStats.TOOLS.get(item.getType());
        if (toolStats != null) {
            //  Use any custom stats
            if (materialInfo.stats() != null) {
                toolStats = new ToolStats(materialInfo.stats().attackDamage(), materialInfo.stats().attackSpeed());
            }

            //  Set attack damage
            NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
            var modifier = new AttributeModifier(modifierKey, toolStats.attackDamage(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, modifier);

            //  Set attack speed
            modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
            modifier = new AttributeModifier(modifierKey, toolStats.attackSpeed(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, modifier);
        }

        //  Set any armor stats
        ArmorStats armorStats = VanillaStats.ARMOR.get(item.getType());
        if (armorStats != null) {
            //  Use any custom stats
            if (materialInfo.stats() != null) {
                armorStats = new ArmorStats(materialInfo.stats().armor(), materialInfo.stats().toughness(), materialInfo.stats().knockbackResistance(), armorStats.slot());
            }

            //  Set armor
            if (armorStats.armor() > 0) {
                NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
                var modifier = new AttributeModifier(modifierKey, armorStats.armor(), AttributeModifier.Operation.ADD_NUMBER, armorStats.slot());
                meta.addAttributeModifier(Attribute.ARMOR, modifier);
            }

            //  Set toughness
            if (armorStats.toughness() > 0) {
                NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
                var modifier = new AttributeModifier(modifierKey, armorStats.toughness(), AttributeModifier.Operation.ADD_NUMBER, armorStats.slot());
                meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier);
            }

            //  Set knockback resistance
            if (armorStats.knockbackResistance() > 0) {
                NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
                var modifier = new AttributeModifier(modifierKey, armorStats.knockbackResistance(), AttributeModifier.Operation.ADD_NUMBER, armorStats.slot());
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, modifier);
            }
        }

        //  Determine then set model data
        int modelData;
        if (materialInfo.modelMax() <= materialInfo.modelMin()) {
            modelData = materialInfo.modelMin();
        } else {
            modelData = random.nextInt(materialInfo.modelMin(), materialInfo.modelMax() + 1);
        }

        if (modelData > 0) {
            meta.setCustomModelData(modelData);
        }

        //  If the rarity is unbreakable, make the item unbreakable
        if (rarity.unbreakable()) {
            meta.setUnbreakable(true);
            meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        //  Set base item name
        Component nameComponent = Component.text(itemName)
                .color(rarityTextColor)
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(nameComponent);

        boolean appliedAnyEffects = false;

        //  Apply any specified affixes
        if (effectOptions.affixes() != null) {
            for (Affix affix : effectOptions.affixes()) {
                String slotName = getRandomValue(allowedSlotNames);
                appliedAnyEffects |= affixGenerator.applyEffect(item, meta, slotName, affix, rarity, rarityLevel);
            }
        }

        //  Apply any specified enchantments
        if (effectOptions.enchantments() != null) {
            for (EnchantmentDefinition enchantmentDefinition : effectOptions.enchantments()) {
                appliedAnyEffects |= affixGenerator.applyEnchantment(item, meta, enchantmentDefinition);
            }
        }

        //  Apply any specified attributes
        if (effectOptions.attributes() != null) {
            for (AttributeDefinition attributeDefinition : effectOptions.attributes()) {
                String slotName = getRandomValue(allowedSlotNames);
                appliedAnyEffects |= affixGenerator.applyAttribute(meta, slotName, attributeDefinition);
            }
        }

        //  Apply random affixes if enabled
        if (effectOptions.randomAffixes()) {
            //  Determine number of affixes
            int affixCount;
            if (effectOptions.maxRandomAffixes() <= effectOptions.minRandomAffixes()) {
                affixCount = effectOptions.minRandomAffixes();
            } else {
                affixCount = random.nextInt(effectOptions.minRandomAffixes(), effectOptions.maxRandomAffixes() + 1);
            }

            //  Apply affixes to random allowed slots
            for (int i = 0; i < affixCount; i++) {
                String slotName = getRandomValue(allowedSlotNames);
                appliedAnyEffects |= affixGenerator.generateAffix(item, meta, slotName, rarityLevel);
            }
        }

        if (!appliedAnyEffects) {
            plugin.getLogger().warning("Failed to generate an item with any effects.");
            return null;
        }

        item.setItemMeta(meta);
        return item;
    }

    private <T> T getRandomValue(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private int getWeightedRandomRarityLevel() {
        float totalWeight = 0f;
        for (Rarity rarity : rarities) {
            totalWeight += rarity.weight();
        }

        if (totalWeight == 0f) {
            return random.nextInt(rarities.size());
        }

        float roll = random.nextFloat() * totalWeight;

        float runningSum = 0f;
        for (int i = 0; i < rarities.size(); i++) {
            runningSum += rarities.get(i).weight();
            if (roll <= runningSum) {
                return i;
            }
        }

        return random.nextInt(rarities.size());
    }

    private MaterialInfo getWeightedRandomMaterialInfo(List<MaterialInfo> materialInfos) {
        float totalWeight = 0f;
        for (MaterialInfo materialInfo : materialInfos) {
            totalWeight += materialInfo.weight();
        }

        if (totalWeight == 0f) {
            return getRandomValue(materialInfos);
        }

        float roll = random.nextFloat() * totalWeight;

        float runningSum = 0f;
        for (MaterialInfo materialInfo : materialInfos) {
            runningSum += materialInfo.weight();
            if (roll <= runningSum) {
                return materialInfo;
            }
        }

        return getRandomValue(materialInfos);
    }
}
