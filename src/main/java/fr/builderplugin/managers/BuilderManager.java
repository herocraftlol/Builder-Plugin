package fr.builderplugin.managers;

import fr.builderplugin.BuilderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BuilderManager {

    private final BuilderPlugin plugin;
    private final Set<UUID> builders = new HashSet<>();
    // Stocke le gamemode d'origine (avant attribution du rôle builder)
    private final Map<UUID, GameMode> previousGameModes = new HashMap<>();
    private File dataFile;

    // Nom de l'équipe scoreboard pour le tag Builder
    private static final String TEAM_NAME = "builder_tag";

    public BuilderManager(BuilderPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "builders.yml");
    }

    // ─── Scoreboard ───────────────────────────────────────────────────────────

    private Scoreboard getOrCreateScoreboard() {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        // On utilise le scoreboard principal du serveur
        Scoreboard board = sm.getMainScoreboard();
        return board;
    }

    private Team getOrCreateBuilderTeam() {
        Scoreboard board = getOrCreateScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
        }
        String prefix = BuilderPlugin.colorize(
                plugin.getConfig().getString("builder-tag.tab-prefix", "&6[Builder] &r"));
        team.setPrefix(prefix);
        team.setDisplayName(BuilderPlugin.colorize("&6Builder"));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        return team;
    }

    private void addToBuilderTeam(Player player) {
        Team team = getOrCreateBuilderTeam();
        team.addEntry(player.getName());
    }

    private void removeFromBuilderTeam(Player player) {
        Scoreboard board = getOrCreateScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    // ─── Nametag au-dessus de la tête (via scoreboard prefix visible de tous) ─

    /**
     * Applique le tag [Builder] au-dessus de la tête via le scoreboard.
     * Le prefix de l'équipe apparaît dans la tab ET au-dessus de la tête.
     */
    public void applyBuilderTag(Player player) {
        addToBuilderTeam(player);
    }

    public void removeBuilderTag(Player player) {
        removeFromBuilderTeam(player);
    }

    // ─── Builder management ───────────────────────────────────────────────────

    public boolean isBuilder(UUID uuid) {
        return builders.contains(uuid);
    }

    public boolean isBuilder(Player player) {
        return isBuilder(player.getUniqueId());
    }

    public void addBuilder(Player player) {
        UUID uuid = player.getUniqueId();
        builders.add(uuid);
        previousGameModes.put(uuid, player.getGameMode());
        player.setGameMode(GameMode.CREATIVE);
        applyBuilderTag(player);
    }

    public void removeBuilder(Player player) {
        UUID uuid = player.getUniqueId();
        builders.remove(uuid);
        // Restaure le gamemode précédent
        GameMode previous = previousGameModes.remove(uuid);
        if (previous != null) {
            player.setGameMode(previous);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
        removeBuilderTag(player);
    }

    /**
     * Appelé quand un builder rejoint le serveur : réapplique le tag + creative.
     */
    public void onBuilderJoin(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        applyBuilderTag(player);
    }

    /**
     * Rafraîchit les tags de tous les joueurs en ligne.
     */
    public void refreshAllOnlinePlayers() {
        // S'assure que l'équipe existe avec le bon prefix
        getOrCreateBuilderTeam();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isBuilder(p)) {
                applyBuilderTag(p);
            }
        }
    }

    public Set<UUID> getBuilders() {
        return Collections.unmodifiableSet(builders);
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    public void load() {
        if (!dataFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        builders.clear();

        List<String> list = config.getStringList("builders");
        for (String s : list) {
            try {
                builders.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();
        List<String> list = new ArrayList<>();
        for (UUID uuid : builders) {
            list.add(uuid.toString());
        }
        config.set("builders", list);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les builders : " + e.getMessage());
        }
    }
}
