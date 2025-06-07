package io.github.chasehuegel.affixes.models;

import java.util.List;

public record MaterialDefinition (
    List<String> slots,
    List<MaterialInfo> materials,
    List<String> names
) {}
