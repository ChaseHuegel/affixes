package io.github.chasehuegel.affixes.util;

import io.github.chasehuegel.affixes.AffixesPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AffixesInspector {

    private static final String AFFIX_CODE_KEY = "affixCode";
    private static final String HAS_SUFFIX_KEY = "hasSuffix";
    private static final String HAS_PREFIX_KEY = "hasPrefix";

    public static boolean hasAnyAffixes(ItemStack item) {
        return hasAnyAffixes(item.getItemMeta());
    }

    public static boolean hasAnyAffixes(ItemMeta meta) {
        return getAffixCode(meta) != null;
    }

    public static String getAffixCode(ItemStack item) {
        return getAffixCode(item.getItemMeta());
    }

    public static String getAffixCode(ItemMeta meta) {
        return getCustomMetadata(meta, AFFIX_CODE_KEY, PersistentDataType.STRING);
    }

    public static void setAffixCode(ItemStack item, String value) {
        setAffixCode(item.getItemMeta(), value);
    }

    public static void setAffixCode(ItemMeta meta, String value) {
        setCustomMetadata(meta, AFFIX_CODE_KEY, PersistentDataType.STRING, value);
    }

    public static boolean getHasSuffix(ItemStack item) {
        return getHasSuffix(item.getItemMeta());
    }

    public static boolean getHasSuffix(ItemMeta meta) {
        Boolean value = getCustomMetadata(meta, HAS_SUFFIX_KEY, PersistentDataType.BOOLEAN);
        return value != null && value;
    }

    public static void setHasSuffix(ItemStack item, boolean value) {
        setHasSuffix(item.getItemMeta(), value);
    }

    public static void setHasSuffix(ItemMeta meta, boolean value) {
        setCustomMetadata(meta, HAS_SUFFIX_KEY, PersistentDataType.BOOLEAN, value);
    }

    public static boolean getHasPrefix(ItemStack item) {
        return getHasPrefix(item.getItemMeta());
    }

    public static boolean getHasPrefix(ItemMeta meta) {
        Boolean value = getCustomMetadata(meta, HAS_PREFIX_KEY, PersistentDataType.BOOLEAN);
        return value != null && value;
    }

    public static void setHasPrefix(ItemStack item, boolean value) {
        setHasPrefix(item.getItemMeta(), value);
    }

    public static void setHasPrefix(ItemMeta meta, boolean value) {
        setCustomMetadata(meta, HAS_PREFIX_KEY, PersistentDataType.BOOLEAN, value);
    }

    private static <P, C> void setCustomMetadata(ItemMeta meta, String key, PersistentDataType<P, C> type, C value) {
        if (meta == null) {
            return;
        }

        NamespacedKey namespacedKey = new NamespacedKey(AffixesPlugin.NAMESPACE, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, type, value);
    }

    private static <P, C> C getCustomMetadata(ItemMeta meta, String key, PersistentDataType<P, C> type) {
        if (meta == null) {
            return null;
        }

        NamespacedKey namespacedKey = new NamespacedKey(AffixesPlugin.NAMESPACE, key);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(namespacedKey, type);
    }
}
