package fr.builderplugin;

import fr.builderplugin.commands.BuilderCommand;
import fr.builderplugin.listeners.BuilderListener;
import fr.builderplugin.managers.BuilderManager;
import fr.builderplugin.managers.ZoneManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BuilderPlugin extends JavaPlugin {

    private static BuilderPlugin instance;
    private BuilderManager builderManager;
    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        instance = this;

        // Sauvegarde de la config par défaut
        saveDefaultConfig();

        // Initialisation des managers
        builderManager = new BuilderManager(this);
        zoneManager = new ZoneManager(this);

        // Chargement des données
        builderManager.load();
        zoneManager.load();

        // Enregistrement des commandes
        BuilderCommand builderCommand = new BuilderCommand(this);
        getCommand("builder").setExecutor(builderCommand);
        getCommand("builder").setTabCompleter(builderCommand);

        // Enregistrement des listeners
        getServer().getPluginManager().registerEvents(new BuilderListener(this), this);

        // Applique les tags à tous les joueurs déjà connectés
        builderManager.refreshAllOnlinePlayers();

        getLogger().info("BuilderPlugin activé avec succès !");
    }

    @Override
    public void onDisable() {
        if (builderManager != null) builderManager.save();
        if (zoneManager != null) zoneManager.save();
        getLogger().info("BuilderPlugin désactivé.");
    }

    public static BuilderPlugin getInstance() {
        return instance;
    }

    public BuilderManager getBuilderManager() {
        return builderManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public String getMessage(String key) {
        String prefix = colorize(getConfig().getString("messages.prefix", "&8[&6Builder&8] &r"));
        String msg = getConfig().getString("messages." + key, "&cMessage manquant: " + key);
        return prefix + colorize(msg);
    }

    public String getMessage(String key, String... placeholders) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public static String colorize(String text) {
        return text.replace("&", "\u00a7");
    }
}
