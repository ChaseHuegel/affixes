package io.github.chasehuegel.affixes.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class Utils {

    public static double getPlayerLuck(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.LUCK);
        if (attribute == null) {
            return 0;
        }

        return attribute.getValue();
    }
}
