package fr.builderplugin.managers;

import fr.builderplugin.BuilderPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZoneManager {

    private final BuilderPlugin plugin;
    private final Map<UUID, BuilderZone> zones = new HashMap<>();
    private File dataFile;

    public ZoneManager(BuilderPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "zones.yml");
    }

    public void setZone(UUID uuid, BuilderZone zone) {
        zones.put(uuid, zone);
    }

    public void removeZone(UUID uuid) {
        zones.remove(uuid);
    }

    public BuilderZone getZone(UUID uuid) {
        return zones.get(uuid);
    }

    public boolean hasZone(UUID uuid) {
        return zones.containsKey(uuid);
    }

    public void load() {
        if (!dataFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        zones.clear();

        if (config.getConfigurationSection("zones") == null) return;

        for (String key : config.getConfigurationSection("zones").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String world = config.getString("zones." + key + ".world");
                int x1 = config.getInt("zones." + key + ".x1");
                int y1 = config.getInt("zones." + key + ".y1");
                int z1 = config.getInt("zones." + key + ".z1");
                int x2 = config.getInt("zones." + key + ".x2");
                int y2 = config.getInt("zones." + key + ".y2");
                int z2 = config.getInt("zones." + key + ".z2");
                zones.put(uuid, new BuilderZone(world, x1, y1, z1, x2, y2, z2));
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur de chargement de la zone pour " + key);
            }
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, BuilderZone> entry : zones.entrySet()) {
            String key = "zones." + entry.getKey().toString();
            BuilderZone z = entry.getValue();
            config.set(key + ".world", z.getWorldName());
            config.set(key + ".x1", z.getMinX());
            config.set(key + ".y1", z.getMinY());
            config.set(key + ".z1", z.getMinZ());
            config.set(key + ".x2", z.getMaxX());
            config.set(key + ".y2", z.getMaxY());
            config.set(key + ".z2", z.getMaxZ());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les zones : " + e.getMessage());
        }
    }
}
