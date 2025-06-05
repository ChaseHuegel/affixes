package io.github.chasehuegel.affixes;

import java.util.Map;

public class Rarity {
    public String name;
    public String color;
    public float weight;
    public int minAffixes;
    public int maxAffixes;
    public boolean enchantable;
    public boolean unbreakable;

    public static Rarity fromMap(Map<?, ?> map) {
        Rarity rarity = new Rarity();
        rarity.name = (String) map.get("name");
        rarity.color = (String) map.get("color");
        rarity.weight = ((Number) map.get("weight")).floatValue();
        rarity.minAffixes = ((Number) map.get("minAffixes")).intValue();
        rarity.maxAffixes = ((Number) map.get("maxAffixes")).intValue();
        rarity.enchantable = map.get("enchantable") != null && (Boolean) map.get("enchantable");
        rarity.unbreakable = map.get("unbreakable") != null && (Boolean) map.get("unbreakable");
        return rarity;
    }
}

