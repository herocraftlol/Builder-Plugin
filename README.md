# BuilderPlugin

> Un plugin Minecraft 1.21 Paper/Purpur permettant de gérer le rôle Builder avec des zones de construction restreintes.

## Description

**BuilderPlugin** permet aux administrateurs de serveur d'attribuer le rôle **Builder** aux joueurs. Voici les fonctionnalités incluses :

- 🎯 **Zones de construction restreintes** — Les builders ne peuvent poser et casser des blocs que dans leur zoneassignée
- 🚫 **Restriction des blocs de commande** — Les builders ne peuvent pas poser ou interagir avec les blocs de commande, les blocs de commande en chaîne, les blocs de commande répétitif, les blocs de structure, les vides de structure et les blocs jigsaw
- ⚔️ **Protection PvP** — Les builders ne peuvent pas blesser les autres joueurs ou entités
- 🛡️ **Verrouillage du mode de jeu** — Les builders sont automatiquement mis en mode Créatif et ne peuvent pas le changer
- 📋 **Tag Builder visuel** — Les builders obtiennent un préfixe coloré `[Builder]` dans la liste des joueurs et au-dessus de leur tête
- 💾 **Données persistantes** — Les rôles builder et les zones sont sauvegardés automatiquement et persistent au redémarrage du serveur

## Commandes

| Commande | Description |
|----------|-------------|
| `/builder give <joueur>` | Attribuer le rôle Builder à un joueur |
| `/builder remove <joueur>` | Retirer le rôle Builder à un joueur |
| `/builder setzone <joueur> <x1> <y1> <z1> <x2> <y2> <z2> [monde]` | Définir la zone de construction d'un builder |
| `/builder delzone <joueur>` | Supprimer la zone d'un builder |
| `/builder info <joueur>` | Afficher les infos d'un builder (coordonnées de la zone) |
| `/builder list` | Lister tous les builders enregistrés |

> Alias : `/b`

## Permissions

| Permission | Description | Défaut |
|------------|-------------|---------|
| `builderplugin.admin` | Accès à toutes les commandes de gestion des builders | OP |

## Installation

1. Téléchargez le dernier `BuilderPlugin-X.X.X.jar` depuis les [Releases](https://github.com/herocraftlol/Builder-Plugin/releases)
2. Placez le fichier JAR dans le dossier `plugins/` de votre serveur
3. Redémarrez votre serveur
4. Configurez les messages dans `plugins/BuilderPlugin/config.yml` si nécessaire

## Configuration

Tous les messages peuvent être personnalisés dans `plugins/BuilderPlugin/config.yml`. Le plugin utilise les codes couleur avec `&` (ex : `&6` pour l'or).

```yaml
messages:
  prefix: "&8[&6Builder&8] &r"
  builder-given: "&aLe role Builder a ete donne a &e%player%&a."
  ...
builder-tag:
  tab-prefix: "&6[Builder] &r"
  nametag-prefix: "&6[Builder]\n&r"
```

## Historique des versions

- **v1.0.0** — Version initiale : gestion du rôle Builder avec restriction de zone, protection des blocs de commande, protection PvP, verrouillage du mode de jeu et stockage persistant.

---

*Version Minecraft : 1.21 | API : Paper/Purpur*
