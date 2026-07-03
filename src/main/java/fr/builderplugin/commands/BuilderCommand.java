package fr.builderplugin.commands;

import fr.builderplugin.BuilderPlugin;
import fr.builderplugin.managers.BuilderManager;
import fr.builderplugin.managers.BuilderZone;
import fr.builderplugin.managers.ZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BuilderCommand implements CommandExecutor, TabCompleter {

    private final BuilderPlugin plugin;
    private final BuilderManager builderManager;
    private final ZoneManager zoneManager;

    public BuilderCommand(BuilderPlugin plugin) {
        this.plugin = plugin;
        this.builderManager = plugin.getBuilderManager();
        this.zoneManager = plugin.getZoneManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("builderplugin.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "setzone" -> handleSetZone(sender, args);
            case "delzone" -> handleDelZone(sender, args);
            case "info" -> handleInfo(sender, args);
            case "list" -> handleList(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    // ─── /builder give <joueur> ───────────────────────────────────────────────

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("usage-give"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }

        if (builderManager.isBuilder(target)) {
            sender.sendMessage(plugin.getMessage("already-builder"));
            return;
        }

        builderManager.addBuilder(target);
        sender.sendMessage(plugin.getMessage("builder-given", "%player%", target.getName()));
        target.sendMessage(BuilderPlugin.colorize("&6Vous avez recu le role &l[Builder]&r&6 !"));
    }

    // ─── /builder remove <joueur> ─────────────────────────────────────────────

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("usage-remove"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }

        if (!builderManager.isBuilder(target)) {
            sender.sendMessage(plugin.getMessage("not-builder"));
            return;
        }

        builderManager.removeBuilder(target);
        sender.sendMessage(plugin.getMessage("builder-removed", "%player%", target.getName()));
        target.sendMessage(BuilderPlugin.colorize("&cVotre role &l[Builder]&r&c a ete retire."));
    }

    // ─── /builder setzone <joueur> <x1> <y1> <z1> <x2> <y2> <z2> [monde] ───

    private void handleSetZone(CommandSender sender, String[] args) {
        if (args.length < 8) {
            sender.sendMessage(plugin.getMessage("usage-setzone"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }

        if (!builderManager.isBuilder(target)) {
            sender.sendMessage(plugin.getMessage("not-builder"));
            return;
        }

        try {
            int x1 = Integer.parseInt(args[2]);
            int y1 = Integer.parseInt(args[3]);
            int z1 = Integer.parseInt(args[4]);
            int x2 = Integer.parseInt(args[5]);
            int y2 = Integer.parseInt(args[6]);
            int z2 = Integer.parseInt(args[7]);

            String world;
            if (args.length >= 9) {
                world = args[8];
                if (Bukkit.getWorld(world) == null) {
                    sender.sendMessage(BuilderPlugin.colorize("&cMonde introuvable : &e" + world));
                    return;
                }
            } else if (sender instanceof Player p) {
                world = p.getWorld().getName();
            } else {
                sender.sendMessage(BuilderPlugin.colorize("&cVeuillez specifier le nom du monde."));
                return;
            }

            BuilderZone zone = new BuilderZone(world, x1, y1, z1, x2, y2, z2);
            zoneManager.setZone(target.getUniqueId(), zone);
            sender.sendMessage(plugin.getMessage("zone-set", "%player%", target.getName()));
            target.sendMessage(BuilderPlugin.colorize(
                    "&aVotre zone de construction a ete definie !\n" +
                    "&7De &e(" + zone.getMinX() + ", " + zone.getMinY() + ", " + zone.getMinZ() + ")" +
                    "&7 a &e(" + zone.getMaxX() + ", " + zone.getMaxY() + ", " + zone.getMaxZ() + ")" +
                    "&7 dans &e" + world));
        } catch (NumberFormatException e) {
            sender.sendMessage(BuilderPlugin.colorize("&cLes coordonnees doivent etre des nombres entiers."));
        }
    }

    // ─── /builder delzone <joueur> ────────────────────────────────────────────

    private void handleDelZone(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("usage-delzone"));
            return;
        }

        // Chercher le joueur en ligne ou hors ligne
        Player target = Bukkit.getPlayer(args[1]);
        UUID uuid = null;
        String name = args[1];

        if (target != null) {
            uuid = target.getUniqueId();
        } else {
            // Cherche dans les joueurs builders par nom
            for (UUID bid : builderManager.getBuilders()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(bid);
                if (op.getName() != null && op.getName().equalsIgnoreCase(args[1])) {
                    uuid = bid;
                    name = op.getName();
                    break;
                }
            }
        }

        if (uuid == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }

        if (!zoneManager.hasZone(uuid)) {
            sender.sendMessage(plugin.getMessage("no-zone", "%player%", name));
            return;
        }

        zoneManager.removeZone(uuid);
        sender.sendMessage(plugin.getMessage("zone-deleted", "%player%", name));
        if (target != null) {
            target.sendMessage(BuilderPlugin.colorize("&cVotre zone de construction a ete supprimee."));
        }
    }

    // ─── /builder info <joueur> ───────────────────────────────────────────────

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("usage-info"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        UUID uuid = null;
        String name = args[1];

        if (target != null) {
            uuid = target.getUniqueId();
            name = target.getName();
        } else {
            for (UUID bid : builderManager.getBuilders()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(bid);
                if (op.getName() != null && op.getName().equalsIgnoreCase(args[1])) {
                    uuid = bid;
                    name = op.getName();
                    break;
                }
            }
        }

        if (uuid == null || !builderManager.isBuilder(uuid)) {
            sender.sendMessage(plugin.getMessage("not-builder"));
            return;
        }

        sender.sendMessage(BuilderPlugin.colorize("&6=== Info Builder : &e" + name + " &6==="));

        if (zoneManager.hasZone(uuid)) {
            BuilderZone z = zoneManager.getZone(uuid);
            sender.sendMessage(plugin.getMessage("zone-info",
                    "%player%", name,
                    "%x1%", String.valueOf(z.getMinX()),
                    "%y1%", String.valueOf(z.getMinY()),
                    "%z1%", String.valueOf(z.getMinZ()),
                    "%x2%", String.valueOf(z.getMaxX()),
                    "%y2%", String.valueOf(z.getMaxY()),
                    "%z2%", String.valueOf(z.getMaxZ()),
                    "%world%", z.getWorldName()));
        } else {
            sender.sendMessage(plugin.getMessage("no-zone", "%player%", name));
        }
    }

    // ─── /builder list ────────────────────────────────────────────────────────

    private void handleList(CommandSender sender) {
        Set<UUID> builders = builderManager.getBuilders();
        if (builders.isEmpty()) {
            sender.sendMessage(plugin.getMessage("no-builders"));
            return;
        }

        sender.sendMessage(BuilderPlugin.colorize("&6=== Liste des Builders (" + builders.size() + ") ==="));
        for (UUID uuid : builders) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            String name = op.getName() != null ? op.getName() : uuid.toString();
            boolean online = op.isOnline();
            boolean hasZone = zoneManager.hasZone(uuid);
            sender.sendMessage(BuilderPlugin.colorize(
                    (online ? "&a" : "&7") + "• " + name +
                    (hasZone ? " &8[zone définie]" : " &c[sans zone]")));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(BuilderPlugin.colorize("&6=== BuilderPlugin - Aide ==="));
        sender.sendMessage(BuilderPlugin.colorize("&e/builder give <joueur> &7- Donner le role Builder"));
        sender.sendMessage(BuilderPlugin.colorize("&e/builder remove <joueur> &7- Retirer le role Builder"));
        sender.sendMessage(BuilderPlugin.colorize("&e/builder setzone <joueur> <x1> <y1> <z1> <x2> <y2> <z2> [monde] &7- Definir la zone"));
        sender.sendMessage(BuilderPlugin.colorize("&e/builder delzone <joueur> &7- Supprimer la zone"));
        sender.sendMessage(BuilderPlugin.colorize("&e/builder info <joueur> &7- Voir les infos d'un builder"));
        sender.sendMessage(BuilderPlugin.colorize("&e/builder list &7- Lister tous les builders"));
    }

    // ─── Tab completion ───────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("builderplugin.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("give", "remove", "setzone", "delzone", "info", "list")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            List<String> players = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            if (sub.equals("give") || sub.equals("remove") || sub.equals("setzone")
                    || sub.equals("delzone") || sub.equals("info")) {
                return players;
            }
        }

        if (args[0].equalsIgnoreCase("setzone") && args.length >= 3 && args.length <= 8) {
            return Collections.singletonList("<coordonnee>");
        }

        if (args[0].equalsIgnoreCase("setzone") && args.length == 9) {
            return Bukkit.getWorlds().stream()
                    .map(w -> w.getName())
                    .filter(n -> n.startsWith(args[8]))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
