package io.github.chasehuegel.affixes;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AffixGenerator {

    private final Rarity[] rarities;
    private final Map<String, Affix> affixes;

    public AffixGenerator(Rarity[] rarities, Map<String, Affix> affixes) {
        this.rarities = rarities;
        this.affixes = affixes;
    }

    public void AddAffix(ItemStack item) {
        var meta = item.getItemMeta();

        var attribute = Attribute.ATTACK_DAMAGE;

        var key = new NamespacedKey("io.github.chasehuegel.affixes", "1");
        var amount = 1.0f;
        var operation = AttributeModifier.Operation.MULTIPLY_SCALAR_1;
        var slot = EquipmentSlotGroup.MAINHAND;
        var modifier = new AttributeModifier(key, amount, operation, slot);

        meta.addAttributeModifier(attribute, modifier);
    }
}
