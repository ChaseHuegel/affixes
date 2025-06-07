package io.github.chasehuegel.affixes.models;

import java.util.List;

public record Affix (
    String attribute,
    String enchantment,
    List<String> slots,
    List<String> prefixes,
    List<String> suffixes
) {}