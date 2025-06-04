package io.github.chasehuegel.affixes;

import java.util.Map;

public class Rarity {
    public String name;
    public String color;
    public float chance;
    public boolean enchantable;
    public boolean unbreakable;

    public static Rarity fromMap(Map<?, ?> map) {
        Rarity rarity = new Rarity();
        rarity.name = (String) map.get("name");
        rarity.color = (String) map.get("color");
        rarity.chance = ((Number) map.get("chance")).floatValue();
        rarity.enchantable = map.get("enchantable") != null && (Boolean) map.get("enchantable");
        rarity.unbreakable = map.get("unbreakable") != null && (Boolean) map.get("unbreakable");
        return rarity;
    }
}

