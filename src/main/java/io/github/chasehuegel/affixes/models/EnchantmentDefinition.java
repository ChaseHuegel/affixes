package io.github.chasehuegel.affixes.models;

public record EnchantmentDefinition (
    String enchantment,
    int min,
    int max
) {}