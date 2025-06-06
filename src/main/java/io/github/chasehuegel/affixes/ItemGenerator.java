package io.github.chasehuegel.affixes;

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

    record ArmorStats(double armor, double toughness, double knockbackResistance, EquipmentSlotGroup slot) {}
    record ToolStats(double attackDamage, double attackSpeed) {}

    private final Random random = new Random();
    private final AffixGenerator affixGenerator;
    private final List<MaterialDefinition> materialDefinitions;
    private final List<Rarity> rarities;
    private final Map<String, ItemDefinition> itemDefinitions;
    private final Map<String, List<EnchantmentDefinition>> enchantmentDefinitions;
    private final Map<String, List<AttributeDefinition>> attributeDefinitions;
    private final Map<Integer, List<ItemDefinition>> itemDefinitionsByRarityLevel;
    private final Map<String, Integer> rarityLevelsByName;
    private final Map<String, Rarity> raritiesByName;

    private final Map<Material, ToolStats> vanillaToolStats = Map.ofEntries(
        // Swords
        Map.entry(Material.WOODEN_SWORD,     new ToolStats(4.0, -2.4)),
        Map.entry(Material.STONE_SWORD,      new ToolStats(5.0, -2.4)),
        Map.entry(Material.IRON_SWORD,       new ToolStats(6.0, -2.4)),
        Map.entry(Material.GOLDEN_SWORD,     new ToolStats(4.0, -2.4)),
        Map.entry(Material.DIAMOND_SWORD,    new ToolStats(7.0, -2.4)),
        Map.entry(Material.NETHERITE_SWORD,  new ToolStats(8.0, -2.4)),

        // Axes
        Map.entry(Material.WOODEN_AXE,       new ToolStats(7.0, -3.2)),
        Map.entry(Material.STONE_AXE,        new ToolStats(9.0, -3.2)),
        Map.entry(Material.IRON_AXE,         new ToolStats(9.0, -3.1)),
        Map.entry(Material.GOLDEN_AXE,       new ToolStats(7.0, -3.0)),
        Map.entry(Material.DIAMOND_AXE,      new ToolStats(9.0, -3.0)),
        Map.entry(Material.NETHERITE_AXE,    new ToolStats(10.0, -3.0)),

        // Pickaxes
        Map.entry(Material.WOODEN_PICKAXE,   new ToolStats(2.0, -2.8)),
        Map.entry(Material.STONE_PICKAXE,    new ToolStats(3.0, -2.8)),
        Map.entry(Material.IRON_PICKAXE,     new ToolStats(4.0, -2.8)),
        Map.entry(Material.GOLDEN_PICKAXE,   new ToolStats(2.0, -2.8)),
        Map.entry(Material.DIAMOND_PICKAXE,  new ToolStats(5.0, -2.8)),
        Map.entry(Material.NETHERITE_PICKAXE,new ToolStats(6.0, -2.8)),

        // Shovels
        Map.entry(Material.WOODEN_SHOVEL,    new ToolStats(1.5, -3.0)),
        Map.entry(Material.STONE_SHOVEL,     new ToolStats(2.5, -3.0)),
        Map.entry(Material.IRON_SHOVEL,      new ToolStats(3.5, -3.0)),
        Map.entry(Material.GOLDEN_SHOVEL,    new ToolStats(1.5, -3.0)),
        Map.entry(Material.DIAMOND_SHOVEL,   new ToolStats(4.5, -3.0)),
        Map.entry(Material.NETHERITE_SHOVEL, new ToolStats(5.5, -3.0)),

        // Hoes
        Map.entry(Material.WOODEN_HOE,       new ToolStats(1.0, -3.0)),
        Map.entry(Material.STONE_HOE,        new ToolStats(1.0, -2.0)),
        Map.entry(Material.IRON_HOE,         new ToolStats(1.0, -1.0)),
        Map.entry(Material.GOLDEN_HOE,       new ToolStats(1.0, -3.0)),
        Map.entry(Material.DIAMOND_HOE,      new ToolStats(1.0, 0.0)),
        Map.entry(Material.NETHERITE_HOE,    new ToolStats(1.0, 0.0)),

        // Other
        Map.entry(Material.TRIDENT, new ToolStats(8.0, -2.9)),
        Map.entry(Material.MACE,    new ToolStats(6.0, -3.4))
    );

    Map<Material, ArmorStats> vanillaArmorStats = Map.ofEntries(
        // Leather Armor
        Map.entry(Material.LEATHER_HELMET,     new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.LEATHER_CHESTPLATE, new ArmorStats(3.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.LEATHER_LEGGINGS,   new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.LEATHER_BOOTS,      new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Chainmail Armor
        Map.entry(Material.CHAINMAIL_HELMET,     new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.CHAINMAIL_CHESTPLATE, new ArmorStats(5.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.CHAINMAIL_LEGGINGS,   new ArmorStats(4.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.CHAINMAIL_BOOTS,      new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Iron Armor
        Map.entry(Material.IRON_HELMET,     new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.IRON_CHESTPLATE, new ArmorStats(6.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.IRON_LEGGINGS,   new ArmorStats(5.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.IRON_BOOTS,      new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Gold Armor
        Map.entry(Material.GOLDEN_HELMET,     new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.GOLDEN_CHESTPLATE, new ArmorStats(5.0, 0.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.GOLDEN_LEGGINGS,   new ArmorStats(3.0, 0.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.GOLDEN_BOOTS,      new ArmorStats(1.0, 0.0, 0.0, EquipmentSlotGroup.FEET)),

        // Diamond Armor
        Map.entry(Material.DIAMOND_HELMET,     new ArmorStats(3.0, 2.0, 0.0, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.DIAMOND_CHESTPLATE, new ArmorStats(8.0, 2.0, 0.0, EquipmentSlotGroup.CHEST)),
        Map.entry(Material.DIAMOND_LEGGINGS,   new ArmorStats(6.0, 2.0, 0.0, EquipmentSlotGroup.LEGS)),
        Map.entry(Material.DIAMOND_BOOTS,      new ArmorStats(3.0, 2.0, 0.0, EquipmentSlotGroup.FEET)),

        // Netherite Armor
        Map.entry(Material.NETHERITE_HELMET,     new ArmorStats(3.0, 3.0, 0.1, EquipmentSlotGroup.HEAD)),
        Map.entry(Material.NETHERITE_CHESTPLATE, new ArmorStats(8.0, 3.0, 0.1,  EquipmentSlotGroup.CHEST)),
        Map.entry(Material.NETHERITE_LEGGINGS,   new ArmorStats(6.0, 3.0, 0.1,  EquipmentSlotGroup.LEGS)),
        Map.entry(Material.NETHERITE_BOOTS,      new ArmorStats(3.0, 3.0, 0.1,  EquipmentSlotGroup.FEET)),

        // Other
        Map.entry(Material.TURTLE_HELMET, new ArmorStats(2.0, 0.0, 0.0, EquipmentSlotGroup.HEAD))
    );

    public ItemGenerator(
            AffixGenerator affixGenerator,
            List<MaterialDefinition> materialDefinitions,
            Map<String, ItemDefinition> itemDefinitions,
            Map<String, List<EnchantmentDefinition>> enchantmentDefinitions,
            Map<String, List<AttributeDefinition>> attributeDefinitions,
            List<Rarity> rarities
    ) {
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

            rarityLevelsByName.put(rarity.name, i);
            raritiesByName.put(rarity.name, rarity);
            itemDefinitionsByRarityLevel.put(i, new ArrayList<>());
        }

        for (ItemDefinition itemDefinition : itemDefinitions.values()) {
            int rarityLevel = rarityLevelsByName.get(itemDefinition.rarity);
            itemDefinitionsByRarityLevel.get(rarityLevel).add(itemDefinition);
        }
    }

    public ItemStack generate() {
        int rarityLevel = getWeightedRandomRarityLevel();

        List<ItemDefinition> itemDefinitionsForRarity = itemDefinitionsByRarityLevel.get(rarityLevel);

        //  Pick a material or item def with equal weight to all items
        int totalDefinitionChoices = materialDefinitions.size() + itemDefinitionsForRarity.size();
        int definitionIndex = random.nextInt(totalDefinitionChoices);

        //  Within range of material defs
        if (definitionIndex < materialDefinitions.size()) {
            MaterialDefinition materialDefinition = materialDefinitions.get(definitionIndex);
            return generate(materialDefinition, rarityLevel);
        }

        //  Else, within range of item defs
        definitionIndex -= materialDefinitions.size();
        ItemDefinition itemDefinition = itemDefinitionsForRarity.get(definitionIndex);
        return generate(itemDefinition);
    }

    public ItemStack generate(MaterialDefinition materialDefinition) {
        int rarityLevel = getWeightedRandomRarityLevel();
        return generate(materialDefinition, rarityLevel);
    }

    public ItemStack generate(MaterialDefinition materialDefinition, int rarityLevel) {
        String itemName = getRandomValue(materialDefinition.names);
        MaterialInfo materialInfo = getWeightedRandomMaterialInfo(materialDefinition.materials);
        Rarity rarity = rarities.get(rarityLevel);

        var effectOptions = new EffectOptions();
        effectOptions.randomAffixes = true;
        effectOptions.minRandomAffixes = rarity.minAffixes;
        effectOptions.maxRandomAffixes = rarity.maxAffixes;

        return generate(itemName, materialInfo, rarityLevel, rarity, materialDefinition.slots, effectOptions);
    }

    public ItemStack generate(ItemDefinition itemDefinition) {
        Rarity rarity = raritiesByName.get(itemDefinition.rarity);
        if (rarity == null) {
            AffixesPlugin.Logger.warning("Unknown rarity: " + itemDefinition.rarity);
            return null;
        }

        int rarityLevel = rarityLevelsByName.get(itemDefinition.rarity);

        //  Define effect options for the generator
        var effectOptions = new EffectOptions();

        //  Collect any enchantment defs by rarity
        if (itemDefinition.enchantments != null) {
            effectOptions.enchantments = new ArrayList<>();
            for (String key : itemDefinition.enchantments) {
                effectOptions.enchantments.add(enchantmentDefinitions.get(key).get(rarityLevel));
            }
        }

        //  Collect any attribute defs by rarity
        if (itemDefinition.attributes != null) {
            effectOptions.attributes = new ArrayList<>();
            for (String key : itemDefinition.attributes) {
                effectOptions.attributes.add(attributeDefinitions.get(key).get(rarityLevel));
            }
        }

        //  Merge with any options the item def specifies
        effectOptions = effectOptions.merge(itemDefinition.effectOptions);

        return generate(itemDefinition.name, itemDefinition.material, rarityLevel, rarity, itemDefinition.slots, effectOptions);
    }

    public ItemStack generate(String itemName, MaterialInfo materialInfo, int rarityLevel, Rarity rarity, List<String> allowedSlotNames, EffectOptions effectOptions) {
        var material = Material.matchMaterial(materialInfo.name);
        if (material == null) {
            AffixesPlugin.Logger.warning("Unknown material: " + materialInfo.name);
            return null;
        }

        NamedTextColor rarityTextColor = NamedTextColor.NAMES.value(rarity.color.toLowerCase());
        if (rarityTextColor == null) {
            AffixesPlugin.Logger.warning("Unknown rarity color: " + rarity.color);
            return null;
        }

        //  Init the item
        var item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        //  Set any tool stats
        ToolStats toolStats = vanillaToolStats.get(item.getType());
        if (toolStats != null) {
            //  Use any custom stats
            if (materialInfo.stats != null) {
                toolStats = new ToolStats(materialInfo.stats.attackDamage, materialInfo.stats.attackSpeed);
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
        ArmorStats armorStats = vanillaArmorStats.get(item.getType());
        if (armorStats != null) {
            //  Use any custom stats
            if (materialInfo.stats != null) {
                armorStats = new ArmorStats(materialInfo.stats.armor, materialInfo.stats.toughness, materialInfo.stats.knockbackResistance, armorStats.slot);
            }

            //  Set armor
            if (armorStats.armor > 0) {
                NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
                var modifier = new AttributeModifier(modifierKey, armorStats.armor(), AttributeModifier.Operation.ADD_NUMBER, armorStats.slot);
                meta.addAttributeModifier(Attribute.ARMOR, modifier);
            }

            //  Set toughness
            if (armorStats.toughness > 0) {
                NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
                var modifier = new AttributeModifier(modifierKey, armorStats.toughness(), AttributeModifier.Operation.ADD_NUMBER, armorStats.slot);
                meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, modifier);
            }

            //  Set knockback resistance
            if (armorStats.knockbackResistance > 0) {
                NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
                var modifier = new AttributeModifier(modifierKey, armorStats.knockbackResistance(), AttributeModifier.Operation.ADD_NUMBER, armorStats.slot);
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, modifier);
            }
        }

        //  Determine then set model data
        int modelData;
        if (materialInfo.modelMax <= materialInfo.modelMin) {
            modelData = materialInfo.modelMin;
        } else {
            modelData = random.nextInt(materialInfo.modelMin, materialInfo.modelMax + 1);
        }

        if (modelData > 0) {
            meta.setCustomModelData(modelData);
        }

        //  If the rarity is unbreakable, make the item unbreakable
        if (rarity.unbreakable) {
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
        if (effectOptions.affixes != null) {
            for (Affix affix : effectOptions.affixes) {
                String slotName = getRandomValue(allowedSlotNames);
                appliedAnyEffects |= affixGenerator.applyEffect(meta, slotName, affix, rarity, rarityLevel);
            }
        }

        //  Apply any specified enchantments
        if (effectOptions.enchantments != null) {
            for (EnchantmentDefinition enchantmentDefinition : effectOptions.enchantments) {
                appliedAnyEffects |= affixGenerator.applyEnchantment(meta, enchantmentDefinition);
            }
        }

        //  Apply any specified attributes
        if (effectOptions.attributes != null) {
            for (AttributeDefinition attributeDefinition : effectOptions.attributes) {
                String slotName = getRandomValue(allowedSlotNames);
                appliedAnyEffects |= affixGenerator.applyAttribute(meta, slotName, attributeDefinition);
            }
        }

        //  Apply random affixes if enabled
        if (effectOptions.randomAffixes) {
            //  Determine number of affixes
            int affixCount;
            if (effectOptions.maxRandomAffixes <= effectOptions.minRandomAffixes) {
                affixCount = effectOptions.minRandomAffixes;
            } else {
                affixCount = random.nextInt(effectOptions.minRandomAffixes, effectOptions.maxRandomAffixes + 1);
            }

            //  Apply affixes to random allowed slots
            for (int i = 0; i < affixCount; i++) {
                String slotName = getRandomValue(allowedSlotNames);
                appliedAnyEffects |= affixGenerator.generateAffix(meta, slotName, rarityLevel);
            }
        }

        if (!appliedAnyEffects) {
            AffixesPlugin.Logger.warning("Failed to generate an item with any effects.");
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
            totalWeight += rarity.weight;
        }

        if (totalWeight == 0f) {
            return random.nextInt(rarities.size());
        }

        float roll = random.nextFloat() * totalWeight;

        float runningSum = 0f;
        for (int i = 0; i < rarities.size(); i++) {
            runningSum += rarities.get(i).weight;
            if (roll <= runningSum) {
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

        if (totalWeight == 0f) {
            return getRandomValue(materialInfos);
        }

        float roll = random.nextFloat() * totalWeight;

        float runningSum = 0f;
        for (MaterialInfo materialInfo : materialInfos) {
            runningSum += materialInfo.weight;
            if (roll <= runningSum) {
                return materialInfo;
            }
        }

        return getRandomValue(materialInfos);
    }
}
