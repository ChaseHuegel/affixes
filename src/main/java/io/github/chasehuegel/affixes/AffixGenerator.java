package io.github.chasehuegel.affixes;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AffixGenerator {

    private final Random random = new Random();
    private final List<Rarity> rarities;
    private final Map<String, Affix> affixes;
    private final List<Affix> affixesValues;
    private final Map<String, List<EnchantmentDefinition>> enchantmentDefinitions;
    private final Map<String, List<AttributeDefinition>> attributeDefinitions;

    public AffixGenerator(
        List<Rarity> rarities,
        Map<String, Affix> affixes,
        Map<String, List<EnchantmentDefinition>> enchantmentDefinitions,
        Map<String, List<AttributeDefinition>> attributeDefinitions
    ) {
        this.rarities = rarities;
        this.affixes = affixes;
        this.affixesValues = new ArrayList<>(affixes.values());
        this.enchantmentDefinitions = enchantmentDefinitions;
        this.attributeDefinitions = attributeDefinitions;
    }

    public boolean generateAffix(ItemMeta meta, String slotName, int rarityLevel) {
        if (affixesValues.stream().noneMatch(affix -> affix.slots.contains(slotName))) {
            //  No affixes for the slot
            return false;
        }

        if (rarityLevel < 0 || rarityLevel >= rarities.size()) {
            //  rarity out of bounds
            return false;
        }

        //  Pick a random affix for the slot
        Affix affix;
        do {
            affix = getRandomValue(affixesValues);
        } while (!affix.slots.contains(slotName));

        if (!applyName(meta, affix)) {
            return false;
        }

        Rarity rarity = rarities.get(rarityLevel);
        if (!applyEffect(meta, slotName, affix, rarity, rarityLevel)) {
            return false;
        }

        return true;
    }

    public boolean applyAffix(ItemMeta meta, Affix affix, String slotName, int rarityLevel) {
        if (!affix.slots.contains(slotName)) {
            //  Affix doesn't support this slot
            return false;
        }

        if (rarityLevel < 0 || rarityLevel >= rarities.size()) {
            //  Rarity out of bounds
            return false;
        }

        if (!applyName(meta, affix)) {
            return false;
        }

        Rarity rarity = rarities.get(rarityLevel);
        if (!applyEffect(meta, slotName, affix, rarity, rarityLevel)) {
            return false;
        }

        return true;
    }

    public boolean applyName(ItemMeta meta, Affix affix) {
        Component displayName = meta.displayName();
        if (displayName == null) {
            return false;
        }

        //  Only allow a single suffix
        Boolean hasSuffix = getCustomMetadata(meta, "hasSuffix", PersistentDataType.BOOLEAN);
        boolean applyPrefix = (hasSuffix != null && hasSuffix) || random.nextBoolean();

        List<String> textOptions;
        if (applyPrefix) {
            textOptions = affix.prefixes;
        } else {
            textOptions = affix.suffixes;
        }

        String text = getRandomValue(textOptions);

        //  If this text has already been applied, reroll
        int rerolls = 0;
        boolean alreadyAppliedText = containsTextComponent(displayName, text);
        while (alreadyAppliedText && rerolls < 3) {
            text = getRandomValue(textOptions);
            alreadyAppliedText = containsTextComponent(displayName, text);
            rerolls++;
        }

        if (alreadyAppliedText) {
            //  Failed to pick an unapplied text
            return false;
        }

        Component prefixComponent;
        Component suffixComponent;
        if (applyPrefix) {
            prefixComponent = Component.text(text);
            suffixComponent = displayName;
            setCustomMetadata(meta, "prefix", PersistentDataType.BOOLEAN, true);
        } else {
            prefixComponent = displayName;
            suffixComponent = Component.text(text);
            setCustomMetadata(meta, "suffix", PersistentDataType.BOOLEAN, true);
        }

        Component newName = prefixComponent.append(suffixComponent);
        meta.displayName(newName);
        return true;
    }

    public boolean applyEffect(ItemMeta meta, String slotName, Affix affix, Rarity rarity, int rarityIndex) {
        boolean hasAttribute = affix.attribute != null && !affix.attribute.isEmpty();
        boolean hasEnchantment = affix.enchantment != null && !affix.enchantment.isEmpty();

        //  Choose to apply an attribute or enchantment
        boolean applyAttribute = hasAttribute;
        if (hasAttribute && hasEnchantment && rarity.enchantable) {
            applyAttribute = random.nextBoolean();
        }

        if (applyAttribute) {
            AttributeDefinition attributeDefinition = attributeDefinitions.get(affix.attribute).get(rarityIndex);
            return applyAttribute(meta, slotName, affix.attribute, attributeDefinition);
        }

        EnchantmentDefinition enchantmentDefinition = enchantmentDefinitions.get(affix.enchantment).get(rarityIndex);
        return applyEnchantment(meta, affix.enchantment, enchantmentDefinition);
    }

    public boolean applyEnchantment(ItemMeta meta, String enchantmentDefinitionKey, EnchantmentDefinition enchantmentDefinition) {
        NamespacedKey enchantmentKey = NamespacedKey.fromString(enchantmentDefinition.enchantment);
        if (enchantmentKey == null) {
            return false;
        }

        var enchantmentsMetadata = getCustomMetadata(meta, "enchantments", PersistentDataType.LIST.strings());
        if (enchantmentsMetadata.contains(enchantmentDefinitionKey)) {
            return false;
        }

        Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Enchantment enchantment = enchantmentRegistry.get(enchantmentKey);
        int level = random.nextInt(enchantmentDefinition.min, enchantmentDefinition.max + 1);

        boolean added = meta.addEnchant(enchantment, level, true);
        if (added) {
            enchantmentsMetadata.add(enchantmentDefinitionKey);
            setCustomMetadata(meta, "enchantments", PersistentDataType.LIST.strings(), enchantmentsMetadata);
        }

        return added;
    }

    public boolean applyAttribute(ItemMeta meta, String slotName, String attributeDefinitionKey, AttributeDefinition attributeDefinition) {
        NamespacedKey attributeKey = NamespacedKey.fromString(attributeDefinition.attribute);
        if (attributeKey == null) {
            return false;
        }

        var attributesMetadata = getCustomMetadata(meta, "attributes", PersistentDataType.LIST.strings());
        if (attributesMetadata.contains(attributeDefinitionKey)) {
            return false;
        }

        var attribute = Registry.ATTRIBUTE.get(attributeKey);
        var amount = random.nextFloat(attributeDefinition.min, attributeDefinition.max);
        var operation = getOperation(attributeDefinition.operation);
        var slot = EquipmentSlotGroup.getByName(slotName);
        var modifierKey = new NamespacedKey(AffixesPlugin.NAMESPACE, attributeDefinition.id);
        var modifier = new AttributeModifier(modifierKey, amount, operation, slot);

        boolean added = meta.addAttributeModifier(attribute, modifier);
        if (added) {
            attributesMetadata.add(attributeDefinitionKey);
            setCustomMetadata(meta, "attributes", PersistentDataType.LIST.strings(), attributesMetadata);
        }

        return added;
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

    private <P, C> void setCustomMetadata(ItemMeta meta, String key, PersistentDataType<P, C> type, C value) {
        if (meta == null) {
            return;
        }

        NamespacedKey namespacedKey = new NamespacedKey(AffixesPlugin.NAMESPACE, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, type, value);
    }

    private <P, C> C getCustomMetadata(ItemMeta meta, String key, PersistentDataType<P, C> type) {
        if (meta == null) {
            return null;
        }

        NamespacedKey namespacedKey = new NamespacedKey(AffixesPlugin.NAMESPACE, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(namespacedKey, type);
    }

    private boolean containsTextComponent(Component component, String text) {
        if (component instanceof TextComponent textComponent) {
            if (textComponent.content().contains(text)) {
                return true;
            }
        }

        for (Component child : component.children()) {
            if (containsTextComponent(child, text)) {
                return true;
            }
        }

        return false;
    }
}
