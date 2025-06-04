package io.github.chasehuegel.affixes;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Affixes extends JavaPlugin {

    private final String[] defaultMaterialResources = new String[] {
        "materials/swords.json",
        "materials/axes.json",
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

        //  Create generators
        var itemGenerator = new ProceduralItemGenerator(materialDefinitions.toArray(new ProceduralMaterialDefinition[0]));
        var affixGenerator = new AffixGenerator(rarities.toArray(new Rarity[0]));
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
