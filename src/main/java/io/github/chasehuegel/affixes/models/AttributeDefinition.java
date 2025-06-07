package io.github.chasehuegel.affixes.models;

public record AttributeDefinition (
    String attribute,
    String operation,
    float min,
    float max
) {}