package io.github.chasehuegel.affixes.models;

import java.util.List;

public class EffectOptions {

    public boolean randomAffixes;
    public int minRandomAffixes;
    public int maxRandomAffixes;
    public List<Affix> affixes;
    public List<EnchantmentDefinition> enchantments;
    public List<AttributeDefinition> attributes;
    
    public EffectOptions merge(EffectOptions other) {
        randomAffixes |= other.randomAffixes;
        
        minRandomAffixes = Math.max(minRandomAffixes, other.minRandomAffixes);
        maxRandomAffixes = Math.max(maxRandomAffixes, other.maxRandomAffixes);

        if (enchantments == null) {
            enchantments = other.enchantments;
        } else if (other.enchantments != null) {
            enchantments.addAll(other.enchantments);
        }

        if (attributes == null) {
            attributes = other.attributes;
        } else if (other.attributes != null) {
            attributes.addAll(other.attributes);
        }

        return this;
    }
}
