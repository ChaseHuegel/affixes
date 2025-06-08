package io.github.chasehuegel.affixes.generators;

import io.github.chasehuegel.affixes.AffixesPlugin;
import io.github.chasehuegel.affixes.models.Affix;
import io.github.chasehuegel.affixes.models.AttributeDefinition;
import io.github.chasehuegel.affixes.models.EnchantmentDefinition;
import io.github.chasehuegel.affixes.models.Rarity;
import io.github.chasehuegel.affixes.util.AffixesInspector;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AffixGenerator {

    private final Random random = new Random();
    private final AffixesPlugin plugin;
    private final List<Rarity> rarities;
    private final Map<String, Affix> affixes;
    private final Map<Affix, String> affixesKeys;
    private final List<Affix> affixesValues;
    private final Map<String, List<EnchantmentDefinition>> enchantmentDefinitions;
    private final Map<String, List<AttributeDefinition>> attributeDefinitions;

    public AffixGenerator(
        AffixesPlugin plugin,
        List<Rarity> rarities,
        Map<String, Affix> affixes,
        Map<String, List<EnchantmentDefinition>> enchantmentDefinitions,
        Map<String, List<AttributeDefinition>> attributeDefinitions
    ) {
        this.plugin = plugin;
        this.rarities = rarities;
        this.affixes = affixes;
        this.affixesValues = new ArrayList<>(affixes.values());
        this.enchantmentDefinitions = enchantmentDefinitions;
        this.attributeDefinitions = attributeDefinitions;

        affixesKeys = new HashMap<>();
        for (Map.Entry<String, Affix> entry : affixes.entrySet()) {
            affixesKeys.put(entry.getValue(), entry.getKey());
        }
    }

    public ItemMeta generateAffix(ItemStack item, ItemMeta meta, String slotName, int rarityLevel) {
        if (affixesValues.stream().noneMatch(affix -> affix.slots().contains(slotName))) {
            //  No affixes for the slot
            return null;
        }

        if (rarityLevel < 0 || rarityLevel >= rarities.size()) {
            //  rarity out of bounds
            return null;
        }

        //  Attempt up to 3 rerolls
        for (int i = 0; i < 3; i++) {
            Affix affix;
            do {
                affix = getRandomValue(affixesValues);
            } while (!affix.slots().contains(slotName));

            //  Don't mutate the original meta in the case of rerolls
            ItemMeta newMeta = meta.clone();

            if (!applyName(newMeta, affix)) {
                //  Reroll if the name couldn't be applied
                continue;
            }

            Rarity rarity = rarities.get(rarityLevel);
            if (!applyEffect(item, newMeta, slotName, affix, rarity, rarityLevel)) {
                //  Reroll if the effect couldn't be applied
                continue;
            }

            AffixesInspector.appendAffixCode(meta, affixesKeys.get(affix));
            return newMeta;
        }

        return null;
    }

    public boolean applyName(ItemMeta meta, Affix affix) {
        Component displayName = meta.displayName();
        if (displayName == null) {
            return false;
        }

        //  Only allow a single suffix
        boolean hasSuffix = AffixesInspector.getHasSuffix(meta);
        boolean applyPrefix = hasSuffix || random.nextBoolean();

        List<String> textOptions;
        if (applyPrefix) {
            textOptions = affix.prefixes();
        } else {
            textOptions = affix.suffixes();
        }

        String text = getRandomValue(textOptions);

        //  If this text has already been applied, reroll
        int rerolls = 0;
        boolean alreadyAppliedText = componentContainsText(displayName, text);
        while (alreadyAppliedText && rerolls < 3) {
            text = getRandomValue(textOptions);
            alreadyAppliedText = componentContainsText(displayName, text);
            rerolls++;
        }

        if (alreadyAppliedText) {
            //  Failed to pick an unapplied text
            return false;
        }

        Component prefixComponent;
        Component suffixComponent;
        if (applyPrefix) {
            suffixComponent = displayName;
            prefixComponent = Component.text(text + " ")
                    .decoration(TextDecoration.ITALIC, false)
                    .mergeStyle(displayName);

            AffixesInspector.setHasPrefix(meta, true);
        } else {
            prefixComponent = displayName;
            suffixComponent = Component.text(" " + text)
                    .decoration(TextDecoration.ITALIC, false)
                    .mergeStyle(displayName);

            AffixesInspector.setHasSuffix(meta, true);
        }

        Component newName = prefixComponent.append(suffixComponent);
        meta.displayName(newName);
        return true;
    }

    public boolean applyEffect(ItemStack item, ItemMeta meta, String slotName, Affix affix, Rarity rarity, int rarityIndex) {
        boolean hasAttribute = affix.attribute() != null && !affix.attribute().isEmpty();
        boolean hasEnchantment = affix.enchantment() != null && !affix.enchantment().isEmpty();

        //  Choose to apply an attribute or enchantment
        boolean applyAttribute = hasAttribute;
        if (hasAttribute && hasEnchantment && rarity.enchantable()) {
            applyAttribute = random.nextBoolean();
        }

        if (!applyAttribute) {
            var enchantmentDefinitionByRarity = enchantmentDefinitions.get(affix.enchantment());
            if (enchantmentDefinitionByRarity == null) {
                plugin.getLogger().warning("Unknown enchantment " + affix.enchantment());
                return false;
            }

            EnchantmentDefinition enchantmentDefinition = enchantmentDefinitionByRarity.get(rarityIndex);
            if (applyEnchantment(item, meta, enchantmentDefinition)) {
                return true;
            }
            //  If the enchantment couldn't be applied, try to fallback to attributes
        }

        if (!hasAttribute) {
            return false;
        }

        var attributeDefinitionsByRarity = attributeDefinitions.get(affix.attribute());
        if (attributeDefinitionsByRarity == null) {
            plugin.getLogger().warning("Unknown attribute " + affix.attribute());
            return false;
        }

        AttributeDefinition attributeDefinition = attributeDefinitionsByRarity.get(rarityIndex);
        return applyAttribute(meta, slotName, attributeDefinition);
    }

    public boolean applyEnchantment(ItemStack item, ItemMeta meta, EnchantmentDefinition enchantmentDefinition) {
        NamespacedKey enchantmentKey = NamespacedKey.fromString(enchantmentDefinition.enchantment());
        if (enchantmentKey == null) {
            return false;
        }

        Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Enchantment enchantment = enchantmentRegistry.get(enchantmentKey);
        int level = enchantmentDefinition.max() <= enchantmentDefinition.min() ? enchantmentDefinition.min() : random.nextInt(enchantmentDefinition.min(), enchantmentDefinition.max() + 1);

        assert enchantment != null;
        if (!canEnchant(item, enchantment)) {
            return false;
        }

        //  If the enchant is already present, and its max level is >1, just increase its level.
        if (meta.hasEnchant(enchantment)) {
            if (enchantment.getMaxLevel() <= 1) {
                //  This enchantment can't be applied or increased
                return false;
            }

            level = meta.getEnchantLevel(enchantment) + 1;
            meta.removeEnchant(enchantment);
        }

        return meta.addEnchant(enchantment, level, true);
    }

    private boolean canEnchant(ItemStack item, Enchantment enchantment) {
        //  Sharpness and fire aspect are allowed on anything that isn't a bow.
        //  These enchants work on all items even if they normally aren't allowed.
        if (enchantment == Enchantment.SHARPNESS || enchantment == Enchantment.FIRE_ASPECT) {
            return item.getType() != Material.BOW && item.getType() != Material.CROSSBOW;
        }

        return enchantment.canEnchantItem(item);
    }

    public boolean applyAttribute(ItemMeta meta, String slotName, AttributeDefinition attributeDefinition) {
        NamespacedKey attributeKey = NamespacedKey.fromString(attributeDefinition.attribute());
        if (attributeKey == null) {
            return false;
        }

        Attribute attribute = Registry.ATTRIBUTE.get(attributeKey);
        float amount = attributeDefinition.max() <= attributeDefinition.min() ? attributeDefinition.min() : random.nextFloat(attributeDefinition.min(), attributeDefinition.max());
        AttributeModifier.Operation operation = getOperation(attributeDefinition.operation());
        EquipmentSlotGroup slot = EquipmentSlotGroup.getByName(slotName);

        //  If the same kind of modifier is already present, remove it and add it onto the amount
        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers != null) {
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() != operation || modifier.getSlotGroup() != slot) {
                    continue;
                }

                meta.removeAttributeModifier(attribute, modifier);
                amount += modifier.getAmount();
                break;
            }
        }

        NamespacedKey modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, UUID.randomUUID().toString());
        AttributeModifier modifier = new AttributeModifier(modifierKey, amount, operation, slot);

        return meta.addAttributeModifier(attribute, modifier);
    }

    private AttributeModifier.Operation getOperation(String value) {
        switch (value) {
            case "add_value" -> {
                return AttributeModifier.Operation.ADD_NUMBER;
            }
            case "add_multiplied_base" -> {
                return AttributeModifier.Operation.ADD_SCALAR;
            }
            case "add_multiplied_total" -> {
                return AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            }
            case null, default -> {
                return null;
            }
        }
    }

    private <T> T getRandomValue(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private boolean componentContainsText(Component component, String text) {
        if (component instanceof TextComponent textComponent) {
            if (textComponent.content().contains(text)) {
                return true;
            }
        }

        for (Component child : component.children()) {
            if (componentContainsText(child, text)) {
                return true;
            }
        }

        return false;
    }
}
