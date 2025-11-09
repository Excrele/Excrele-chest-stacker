# Excrele's Chest Stacker

![Logo Placeholder](https://via.placeholder.com/300x150/00ff00/000000?text=Chest+Stacker) <!-- Add a cool logo later! -->

A lightweight Spigot/Paper plugin that supercharges storage! Stack **up to 1000 items** in a single slot for chests, barrels, and more. Say goodbye to 64-item limits cluttering your mega-bases. Player inventories stay vanilla (normal limits)—no weirdness there.

## Features
- **Epic Stacking**: Up to 1000 items per stack in:
    - Chests (single & double)
    - Trapped Chests
    - Ender Chests
    - Shulker Boxes
    - Barrels
- **Vanilla-Friendly**: Player hotbar/inv untouched. Hoppers & dispensers work as expected.
- **No Config Needed**: Plug & play! Max stack is 1000 by default (tweakable in code).
- **Performance**: Event-based—light on your server. Tested on 1.20+.
- **Permissions**: `cheststacker.use` (default: everyone).

## Installation
1. Download the latest JAR from [Releases](https://github.com/Excrele/ChestStacker/releases) (or build your own—see below).
2. Drop `cheststacker.jar` into your server's `plugins/` folder.
3. Restart the server (or `/reload confirm`—but restart is safer).
4. Done! Open a chest, stack away. Logs will say "Excrele's Chest Stacker loaded!" if it's happy.

## Building from Source
Wanna tweak it? (You're a dev, right?)
- Clone: `git clone https://github.com/Excrele/ChestStacker.git` (or your repo).
- Open in IntelliJ (Maven project—see setup guide).
- Run `mvn clean package`.
- Grab `target/cheststacker-*.jar` for your server.

Requires Java 17+, Maven 3.8+, Spigot/Paper 1.20+.

## Supported Containers
| Container       | Stacking Enabled? | Notes |
|-----------------|-------------------|-------|
| Chest (Single)  | ✅ Yes            | Up to 1000 per slot. |
| Double Chest    | ✅ Yes            | Treats as one big inv. |
| Trapped Chest   | ✅ Yes            | Same as regular. |
| Ender Chest     | ✅ Yes            | Personal mega-storage! |
| Shulker Box     | ✅ Yes            | Portable stacks. |
| Barrel          | ✅ Yes            | Rustic vibes, huge capacity. |
| Player Inventory | ❌ No           | Keeps MC feeling classic. |

## Commands & Permissions
- No commands yet (keeping it simple). Future: `/stacker reload`.
- Perm: `cheststacker.use`—gives stacking powers.

## Configuration
None! Hardcoded max=1000 for now. Want custom? Edit `ChestStackerPlugin.MAX_STACK_SIZE` and rebuild.

## Known Issues / TODO
- Hoppers might need tuning for auto-stacking (WIP).
- Shulkers in inv: Stacks only when placed as blocks.
- Report bugs on GitHub.

## Contributing
Fork, PR, or yell ideas! Code's modular—add features in new listeners.

## License
MIT—use it, love it, share it. Author: Excrele (come.excrele).

**Stack smart, build epic!** 🚀