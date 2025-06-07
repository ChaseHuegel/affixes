package io.github.chasehuegel.affixes.models;

public record MaterialInfo (
    String name,
    float weight,
    int modelMin,
    int modelMax,
    Stats stats
) {}