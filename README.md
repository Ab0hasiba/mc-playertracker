# PlayerTracker

**PlayerTracker** is a high-performance, production-ready Minecraft server plugin designed specifically for **Paper 1.21.1** (running on **Java 21**).

It introduces a craftable endgame compass item—the **Player Tracker**—that allows players to track other online players in real-time with an interactive chest GUI, dynamic compass needle updates, distance action bar, cross-dimension detection, and automatic 30-minute item expiration.

---

## 🌟 Features

- **Interactive GUI Selection**: Right-clicking an unused tracker opens a paginated chest GUI displaying heads of all online players.
- **Dynamic Compass Direction**: Tracks moving targets smoothly in real-time (default interval: 5 ticks = 0.25s).
- **HUD Distance Action Bar**: Shows live distance to target (e.g. `Tracking: Steve | Distance: 347m`) without chat spam.
- **Cross-Dimension Awareness**: Detects when target enters the Nether or End and warns the tracker holder without breaking the tracker.
- **Persistent Data Containers (PDC)**: All tracker metadata (tracker ID, target UUID, expiration timestamp) is stored securely on the item itself.
- **Server Restart Resilience**: Timers continue across restarts, disconnects, drops, deaths, and chest storage.
- **30-Minute Expiration**: When the 30-minute tracking timer finishes, the tracker expires, is removed from inventory, and holder is notified.
- **Target Privacy**: Tracked targets receive zero notifications.
- **Endgame Crafting Recipe**: Balanced, high-cost endgame crafting recipe with permission checks.

---

## 📋 Requirements

| Requirement | Value |
|---|---|
| **Server Software** | [Paper](https://papermc.io) (1.21 / 1.21.1 / 1.21.x) |
| **Java Runtime** | Java 21 or higher |
| **Architecture** | Server-side only (no client mod needed) |

---

## 📦 Installation

1. Download or build the plugin JAR (`PlayerTracker-1.0.0.jar`).
2. Place `PlayerTracker-1.0.0.jar` into your Paper server's `plugins/` directory.
3. Start or restart your server.
4. Customize configuration in `plugins/PlayerTracker/config.yml` if desired.

---

## 🛠️ Crafting Recipe

The tracker has an expensive endgame 3×3 shaped crafting recipe:

```
[ Diamond Block ] [ Netherite Ingot ] [ Wind Charge  ]
[ Eye of Ender  ] [     Compass     ] [ Ender Pearl  ]
[   Blaze Rod   ] [ Diamond Block   ] [ Netherite Ingot ]
```

### Ingredients:
- `2 × Diamond Block`
- `2 × Netherite Ingot`
- `1 × Wind Charge`
- `1 × Eye of Ender`
- `1 × Compass`
- `1 × Ender Pearl`
- `1 × Blaze Rod`

---

## ⏱️ How It Works & 30-Minute Lifetime

1. **Craft or Receive Tracker**: Crafting or receiving the tracker gives an unused `&b&lPlayer Tracker` compass.
2. **Selecting a Target**: Right-click the unused tracker to open the target selection GUI. Click any online player's head.
3. **Timer Starts**: The **30-minute timer** begins *only when a target is chosen*, not upon crafting.
4. **Tracking**:
   - The compass points toward the target's coordinates.
   - The action bar displays: `Tracking: <PlayerName> | Distance: <Distance>m`
5. **Cross-Dimension**:
   - If target enters Nether/End, the action bar displays `<PlayerName> is in another dimension.`
   - Once both players return to the same world, tracking resumes automatically.
6. **Expiration**:
   - After **30 minutes**, the tracker expires.
   - It is removed from the player's inventory or ground.
   - The holder receives: `&cYour player tracker has expired!`

---

## 🎮 Commands & Permissions

### Commands
| Command | Permission | Description |
|---|---|---|
| `/playertracker reload` | `playertracker.admin` | Reloads `config.yml` and updates recipe and tasks |
| `/playertracker give <player>` | `playertracker.admin` | Gives a fresh Player Tracker to a player |

Aliases: `/tracker`, `/ptracker`

### Permissions
| Permission | Default | Description |
|---|---|---|
| `playertracker.use` | `true` | Allows players to use Player Trackers |
| `playertracker.craft` | `true` | Allows players to craft Player Trackers |
| `playertracker.admin` | `op` | Allows administrative commands (`/playertracker reload`, `give`) |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# ==============================================================================
#                      PlayerTracker Configuration
# ==============================================================================

tracker:
  # Lifetime in seconds after selecting a target (1800 seconds = 30 minutes)
  lifetime-seconds: 1800
  # Update interval in ticks for compass and action bar updates (5 ticks = 0.25s)
  update-interval-ticks: 5

recipe:
  enabled: true

  shape:
    - "DNW"
    - "ECP"
    - "BND"

  ingredients:
    D: DIAMOND_BLOCK
    N: NETHERITE_INGOT
    W: WIND_CHARGE
    E: ENDER_EYE
    C: COMPASS
    P: ENDER_PEARL
    B: BLAZE_ROD

messages:
  tracker-created: "&aPlayer tracker created!"
  tracking-started: "&aNow tracking &f%player%&a!"
  tracker-expired: "&cYour player tracker has expired!"
  target-offline: "&cThe tracked player is offline."
  different-dimension: "&c%player% is in another dimension."
  no-permission: "&cYou don't have permission to use this."
  action-bar: "&bTracking: &f%player% &7| &bDistance: &f%distance%m"
  reload-success: "&aPlayerTracker configuration reloaded successfully!"
  give-success: "&aGiven a Player Tracker to &f%player%&a!"
  player-not-found: "&cPlayer '%player%' not found or not online."
  cannot-track-self: "&cYou cannot track yourself."
```

---

## 🔧 Troubleshooting

- **Normal compasses aren't tracking**: Normal vanilla compasses will function completely as vanilla compasses. Only compasses crafted with the recipe or given via `/playertracker give` have the PersistentDataContainer metadata required to be active trackers.
- **Tracking not pointing across dimensions**: In Minecraft, compasses cannot point to coordinates in an unloaded or different dimension. The plugin displays an action bar alert that the player is in another dimension and resumes tracking when both are in the same dimension.
- **Permissions not working**: By default, `playertracker.use` and `playertracker.craft` are given to all players (`default: true`). If using a permission plugin (e.g. LuckPerms), ensure nodes are granted or denied as desired.
