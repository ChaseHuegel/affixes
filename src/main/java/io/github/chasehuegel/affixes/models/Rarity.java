package io.github.chasehuegel.affixes.models;

import java.util.Map;

public record Rarity (
    String name,
    String color,
    float weight,
    int minAffixes,
    int maxAffixes,
    boolean enchantable,
    boolean unbreakable
) {

    public static Rarity fromMap(Map<?, ?> map) {
        var name = (String) map.get("name");
        var color = (String) map.get("color");
        var weight = ((Number) map.get("weight")).floatValue();
        var minAffixes = ((Number) map.get("minAffixes")).intValue();
        var maxAffixes = ((Number) map.get("maxAffixes")).intValue();
        var enchantable = map.get("enchantable") != null && (Boolean) map.get("enchantable");
        var unbreakable = map.get("unbreakable") != null && (Boolean) map.get("unbreakable");
        
        return new Rarity(name, color, weight, minAffixes, maxAffixes, enchantable, unbreakable);
    }
}

