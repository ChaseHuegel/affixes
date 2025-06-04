package io.github.chasehuegel.affixes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Affixes extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        List<Rarity> rarities = loadRaritiesFromConfig(config);

        var affixGenerator = new AffixGenerator(rarities.toArray(new Rarity[0]));
    }

    @Override
    public void onDisable() {
    }

    private List<Rarity> loadRaritiesFromConfig(FileConfiguration config) {
        List<Rarity> rarities = new ArrayList<>();

        for (Map<?, ?> entry : config.getMapList("rarities")) {
            rarities.add(Rarity.fromMap(entry));
        }

        return rarities;
    }
}
