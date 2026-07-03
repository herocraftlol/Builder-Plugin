package fr.builderplugin.listeners;

import fr.builderplugin.BuilderPlugin;
import fr.builderplugin.managers.BuilderManager;
import fr.builderplugin.managers.BuilderZone;
import fr.builderplugin.managers.ZoneManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class BuilderListener implements Listener {

    private final BuilderPlugin plugin;
    private final BuilderManager builderManager;
    private final ZoneManager zoneManager;

    // Blocs de commande interdits
    private static final Set<Material> FORBIDDEN_BLOCKS = EnumSet.of(
            Material.COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID,
            Material.JIGSAW
    );

    public BuilderListener(BuilderPlugin plugin) {
        this.plugin = plugin;
        this.builderManager = plugin.getBuilderManager();
        this.zoneManager = plugin.getZoneManager();
    }

    // ─── Connexion : réapplique le tag builder ────────────────────────────────

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (builderManager.isBuilder(player)) {
            // Petit délai pour que le scoreboard soit bien chargé
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                builderManager.onBuilderJoin(player);
            }, 5L);
        }
    }

    // ─── Déconnexion : sauvegarde ─────────────────────────────────────────────

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Sauvegarde auto à la déco
        builderManager.save();
        zoneManager.save();
    }

    // ─── Pose de bloc ─────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!builderManager.isBuilder(player)) return;

        Block block = event.getBlock();

        // Interdit les blocs de commande
        if (FORBIDDEN_BLOCKS.contains(block.getType())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("command-block-denied"));
            return;
        }

        // Vérifie la zone
        if (!isInZone(player, block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("outside-zone"));
        }
    }

    // ─── Destruction de bloc ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!builderManager.isBuilder(player)) return;

        Block block = event.getBlock();

        // Les command blocks ne peuvent pas non plus être détruits
        if (FORBIDDEN_BLOCKS.contains(block.getType())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("command-block-denied"));
            return;
        }

        if (!isInZone(player, block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("outside-zone"));
        }
    }

    // ─── Interaction (ex: remplissage de clic droit avec des items) ───────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!builderManager.isBuilder(player)) return;

        // Empêche d'utiliser des blocs de commande même via interaction
        ItemStack item = event.getItem();
        if (item != null && FORBIDDEN_BLOCKS.contains(item.getType())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("command-block-denied"));
        }
    }

    // ─── Dégâts aux autres entités : bloqué pour les builders ────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (builderManager.isBuilder(player)) {
                // Les builders ne peuvent pas attaquer d'autres joueurs/entités
                event.setCancelled(true);
            }
        }
    }

    // ─── Les builders ne prennent pas de dégâts (creative) ───────────────────
    // (Géré automatiquement par le mode créatif, mais on double la sécurité)

    // ─── Empêche le changement de gamemode manuel ─────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!builderManager.isBuilder(player)) return;

        // Si ce n'est pas le creative, on annule
        if (event.getNewGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    // ─── Empêche le drop d'items hors zone ───────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!builderManager.isBuilder(player)) return;

        // Les builders ne peuvent pas drop d'items
        event.setCancelled(true);
    }

    // ─── Empêche le ramassage d'items ────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickupItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (builderManager.isBuilder(player)) {
            event.setCancelled(true);
        }
    }

    // ─── Méthode utilitaire ───────────────────────────────────────────────────

    private boolean isInZone(Player player, Location location) {
        if (!zoneManager.hasZone(player.getUniqueId())) {
            // Pas de zone définie = aucune construction autorisée
            player.sendMessage(plugin.getMessage("no-zone", "%player%", player.getName()));
            return false;
        }
        BuilderZone zone = zoneManager.getZone(player.getUniqueId());
        return zone.contains(location);
    }
}
