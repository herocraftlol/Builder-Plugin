# BuilderPlugin

> A Minecraft 1.21 Paper/Purpur plugin for managing Builder roles with restricted building zones.

## Description

**BuilderPlugin** allows server administrators to assign a **Builder role** to players with the following features:

- 🎯 **Restricted Building Zones** — Builders can only place and break blocks within their assigned zone
- 🚫 **Command Block Restrictions** — Builders cannot place or interact with command blocks, repeating command blocks, chain command blocks, structure blocks, structure voids, and jigsaw blocks
- ⚔️ **PvP Protection** — Builders cannot damage other players or entities
- 🛡️ **Gamemode Lock** — Builders are automatically set to Creative mode and cannot change it
- 📋 **Visual Builder Tag** — Builders get a colored `[Builder]` prefix in the player list and above their name
- 💾 **Persistent Data** — Builder roles and zones are saved automatically and persist across restarts

## Commands

| Command | Description |
|---------|-------------|
| `/builder give <player>` | Assign the Builder role to a player |
| `/builder remove <player>` | Remove the Builder role from a player |
| `/builder setzone <player> <x1> <y1> <z1> <x2> <y2> <z2> [world]` | Define the building zone for a builder |
| `/builder delzone <player>` | Delete the zone of a builder |
| `/builder info <player>` | Show info about a builder (zone coordinates) |
| `/builder list` | List all registered builders |

> Alias: `/b`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `builderplugin.admin` | Access to all builder management commands | OP |

## Installation

1. Download the latest `BuilderPlugin-X.X.X.jar` from the [Releases](https://github.com/herocraftlol/Builder-Plugin/releases)
2. Place the JAR file in your server's `plugins/` folder
3. Restart your server
4. Configure messages in `plugins/BuilderPlugin/config.yml` if needed

## Configuration

All messages can be customized in `plugins/BuilderPlugin/config.yml`. The plugin uses color codes with `&` (e.g., `&6` for gold).

```yaml
messages:
  prefix: "&8[&6Builder&8] &r"
  builder-given: "&aLe role Builder a ete donne a &e%player%&a."
  ...
builder-tag:
  tab-prefix: "&6[Builder] &r"
  nametag-prefix: "&6[Builder]\n&r"
```

## Version History

- **v1.0.0** — Initial release: Builder role management with zone restriction, command block protection, PvP protection, gamemode lock, and persistent storage.

---

*Minecraft version: 1.21 | API: Paper/Purpur*
