package io.github.chasehuegel.affixes.models;

import java.util.List;

public record EffectOptions (
    boolean randomAffixes,
    int minRandomAffixes,
    int maxRandomAffixes,
    List<Affix> affixes,
    List<EnchantmentDefinition> enchantments,
    List<AttributeDefinition> attributes
) {

    public EffectOptions merge(EffectOptions other) {
        var mergedRandomAffixes = this.randomAffixes | other.randomAffixes;
        
        var mergedMinRandomAffixes = Math.max(this.minRandomAffixes, other.minRandomAffixes);
        var mergedMaxRandomAffixes = Math.max(this.maxRandomAffixes, other.maxRandomAffixes);

        List<Affix> mergedAffixes = null;
        if (this.affixes == null) {
            mergedAffixes = other.affixes;
        } else if (other.affixes != null) {
            mergedAffixes = this.affixes;
            mergedAffixes.addAll(other.affixes);
        }

        List<EnchantmentDefinition> mergedEnchantments = null;
        if (this.enchantments == null) {
            mergedEnchantments = other.enchantments;
        } else if (other.enchantments != null) {
            mergedEnchantments = this.enchantments;
            mergedEnchantments.addAll(other.enchantments);
        }

        List<AttributeDefinition> mergedAttributes = null;
        if (this.attributes == null) {
            mergedAttributes = other.attributes;
        } else if (other.attributes != null) {
            mergedAttributes = this.attributes;
            mergedAttributes.addAll(other.attributes);
        }

        return new EffectOptions(mergedRandomAffixes, mergedMinRandomAffixes, mergedMaxRandomAffixes, mergedAffixes, mergedEnchantments, mergedAttributes);
    }
}
