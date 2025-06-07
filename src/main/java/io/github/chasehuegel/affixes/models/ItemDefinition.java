package io.github.chasehuegel.affixes.models;

import java.util.List;

public record ItemDefinition (
    String name,
    String rarity,
    EffectOptions effectOptions,
    List<String> slots,
    List<String> attributes,
    List<String> enchantments,
    MaterialInfo material
) {}