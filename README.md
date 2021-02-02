spawn-explorer-map
===================
A dead simple plugin to spawn an [Explorer Map](https://minecraft.gamepedia.com/Explorer_Map)

Explorer maps are special and can't be spawned using normal essentials commands.
Initially created to verify the correctness of PaperMC/Paper#3559

Uses [Server.createExplorerMap](https://papermc.io/javadocs/paper/1.16/org/bukkit/Server.html#createExplorerMap-org.bukkit.World-org.bukkit.Location-org.bukkit.StructureType-) to spawn the maps.

## Features
- Pure Java, no dependencies aside from Paper
- Java 11
- Tab completion
- Very lightweight, only 6.2K
- Zero configuration

## Usage
To compile the plugin, run `mvn clean package`.

### Commands
````
/spawnExplorerMap <structure type> [player] <>
````

Structure types are one of the structures available in [the StructureType](https://papermc.io/javadocs/paper/1.16/org/bukkit/StructureType.html) singleton.
Names are shown on server startup/plugin reload.

