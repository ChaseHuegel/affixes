package io.github.chasehuegel.affixes;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public final class AffixesPlugin extends JavaPlugin {

    private static AffixesPlugin instance;
    public static AffixesPlugin getInstance() {
        return instance;
    }

    public final static String NAMESPACE = "affixes";

    private final String[] defaultJsonResources = new String[] {
        "materials/swords.json",
        "materials/axes.json",
        "materials/tridents.json",
        "materials/maces.json",
        "materials/bows.json",
        "materials/crossbows.json",
        "materials/helmets.json",
        "materials/chestplates.json",
        "materials/leggings.json",
        "materials/boots.json",
        "materials/hoes.json",
        "materials/pickaxes.json",
        "materials/shovels.json",
        "materials/trinkets.json",
        "affixes/base.json",
        "enchantments/base.json",
        "attributes/base.json",
        "items/uniques.json",
        "items/custom.json",
    };

    @Override
    public void onEnable() {
        init();
    }

    public void reload() {
        getLogger().info("Reloading Affixes...");

        reloadConfig();
        init();

        getLogger().info("Reloaded Affixes.");
    }

    private void init() {
        instance = this;

        //  Init default config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        //  Init default resources
        for (String resource : defaultJsonResources) {
            File file = new File(getDataFolder(), resource);
            saveResourceIfNotExists(resource, file);
        }

        //  Load config
        List<Rarity> rarities = loadRaritiesFromConfig(getConfig());

        //  Load materials
        var materialDefinitions = new ArrayList<MaterialDefinition>();
        var materialsFolder = new File(getDataFolder(), "materials/");
        var materialsFiles = materialsFolder.listFiles();
        assert materialsFiles != null;

        for (File file : materialsFiles) {
            var loadedResource = loadJsonResource(file, MaterialDefinitions.class);
            if (loadedResource == null) {
                continue;
            }

            materialDefinitions.addAll(loadedResource);
        }

        //  Load affixes
        var affixes = new HashMap<String, Affix>();
        var affixesFolder = new File(getDataFolder(), "affixes/");
        var affixesFiles = affixesFolder.listFiles();
        assert affixesFiles != null;

        for (File file : affixesFiles) {
            var loadedResource = loadJsonResource(file, Affixes.class);
            if (loadedResource == null) {
                continue;
            }

            affixes.putAll(loadedResource);
        }

        //  Load enchantments
        var enchantmentDefinitions = new HashMap<String, List<EnchantmentDefinition>>();
        var enchantmentsFolder = new File(getDataFolder(), "enchantments/");
        var enchantmentsFiles = enchantmentsFolder.listFiles();
        assert enchantmentsFiles != null;

        for (File file : enchantmentsFiles) {
            var loadedResource = loadJsonResource(file, EnchantmentDefinitions.class);
            if (loadedResource == null) {
                continue;
            }

            enchantmentDefinitions.putAll(loadedResource);
        }

        //  Load attributes
        var attributeDefinitions = new HashMap<String, List<AttributeDefinition>>();
        var attributesFolder = new File(getDataFolder(), "attributes/");
        var attributesFiles = attributesFolder.listFiles();
        assert attributesFiles != null;

        for (File file : attributesFiles) {
            var loadedResource = loadJsonResource(file, AttributeDefinitions.class);
            if (loadedResource == null) {
                continue;
            }

            attributeDefinitions.putAll(loadedResource);
        }

        //  Load items
        var itemDefinitions = new HashMap<String, ItemDefinition>();
        var itemsFolder = new File(getDataFolder(), "items/");
        var itemsFiles = itemsFolder.listFiles();
        assert itemsFiles != null;

        for (File file : itemsFiles) {
            var loadedResource = loadJsonResource(file, ItemDefinitions.class);
            if (loadedResource == null) {
                continue;
            }

            itemDefinitions.putAll(loadedResource);
        }

        //  Create generators
        var affixGenerator = new AffixGenerator(rarities, affixes, enchantmentDefinitions, attributeDefinitions);
        var itemGenerator = new ItemGenerator(affixGenerator, materialDefinitions, itemDefinitions, enchantmentDefinitions, attributeDefinitions, rarities);

        //  Register commands
        Objects.requireNonNull(getCommand("affixes")).setExecutor(new AffixesCommandHandler(itemGenerator));
        Objects.requireNonNull(getCommand("affixes")).setTabCompleter(new AffixesCommandHandler(itemGenerator));
    }

    private <T> T loadJsonResource(File file, Class<T> tClass) {
        try {
            Reader reader = new FileReader(file);
            T value = new Gson().fromJson(reader, tClass);
            reader.close();
            return value;
        } catch (Exception ex) {
            getLogger().warning("Error loading resource: " + file + ", exception: " + ex.getMessage());
        }

        return null;
    }

    private void saveResourceIfNotExists(String resourcePath, File targetFile) {
        if (!targetFile.exists()) {
            saveResource(resourcePath, false);
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
