package io.github.chasehuegel.affixes;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public final class AffixesPlugin extends JavaPlugin {

    private final String[] defaultMaterialResources = new String[] {
        "materials/swords.json",
        "materials/axes.json",
    };

    private final String[] defaultAffixResources = new String[] {
        "affixes/base.json",
    };

    private final String[] defaultEnchantmentResources = new String[] {
            "enchantments/base.json",
    };

    @Override
    public void onEnable() {
        //  Init the config
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();

        //  Load config
        List<Rarity> rarities = loadRaritiesFromConfig(config);

        //  Load materials
        var materialDefinitions = new ArrayList<ProceduralMaterialDefinition>();
        for (String resource : defaultMaterialResources) {
            var loadedResource = loadJsonResource(resource, ProceduralMaterialDefinitions.class);
            if (loadedResource == null) {
                continue;
            }

            materialDefinitions.addAll(Arrays.asList(loadedResource.items));
        }

        //  Load affixes
        var affixes = new HashMap<String, Affix>();
        for (String resource : defaultAffixResources) {
            var loadedResource = loadJsonResource(resource, Affixes.class);
            if (loadedResource == null) {
                continue;
            }

            affixes.putAll(loadedResource.items);
        }

        //  Load enchantments
        var enchantmentDefinitions = new HashMap<String, List<EnchantmentDefinition>>();
        for (String resource : defaultEnchantmentResources) {
            var loadedResource = loadJsonResource(resource, EnchantmentDefinitions.class);
            if (loadedResource == null) {
                continue;
            }

            enchantmentDefinitions.putAll(loadedResource.items);
        }

        //  Create generators
        var itemGenerator = new ProceduralItemGenerator(materialDefinitions.toArray(new ProceduralMaterialDefinition[0]));
        var affixGenerator = new AffixGenerator(rarities.toArray(new Rarity[0]), affixes);
    }

    @Override
    public void onDisable() {
        //  Do nothing
    }

    private <T> T loadJsonResource(String resourcePath, Class<T> tClass) {
        File swordMaterialsFile = new File(getDataFolder(), resourcePath);
        saveResourceIfNotExists(this, resourcePath, swordMaterialsFile);

        try {
            Reader reader = new FileReader(swordMaterialsFile);
            T value = new Gson().fromJson(reader, tClass);
            reader.close();
            return value;
        } catch (Exception ex) {
            getLogger().warning("Error loading resource: " + resourcePath + ", exception: " + ex.getMessage());
        }

        return null;
    }

    public void saveResourceIfNotExists(JavaPlugin plugin, String resourcePath, File targetFile) {
        if (!targetFile.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private List<Rarity> loadRaritiesFromConfig(FileConfiguration config) {
        List<Rarity> rarities = new ArrayList<>();

        for (Map<?, ?> entry : config.getMapList("rarities")) {
            rarities.add(Rarity.fromMap(entry));
        }

        return rarities;
    }
}
