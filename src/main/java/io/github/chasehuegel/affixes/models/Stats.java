package io.github.chasehuegel.affixes.models;

public record Stats (
    double attackDamage,
    double attackSpeed,
    double armor,
    double toughness,
    double knockbackResistance
) {}