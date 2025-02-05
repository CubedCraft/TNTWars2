# TNTWars Plugin

⚠️ WARNING Read and understand the statement underneath ⚠️

This is the open source version of the [Cubedcraft](https://cubedcraft.com) TNTWars plugin.
This is still a W.I.P. version and not made for generic minigame servers. It is designed to take **FULL CONTROL** 
over the server's players & state. This might change in future versions but for the time being, it will not change.

## Table of Contents

- [Setup](#Setup)
- [Contribute](#Contribute)
- [Liscence](#Liscence)

## Setup

After you uploaded the plugin to your server, make sure you have added the following dependencies as well

Dependencies:
- [PaperSpigot 1.20+](https://papermc.io/downloads/paper)
- [Fast Async World Edit](https://ci.athion.net/job/FastAsyncWorldEdit/)
- [Kotlin SDK](https://modrinth.com/plugin/mckotlin/versions)

Soft Dependency: (Optional)
- [Placeholder API](https://placeholderapi.com/)

To start off, you want to make sure you have the permissions `tntwars.mapmanager`, `tntwars.game` and `tntwars.game.
admin`

After you logged in you might notice that you start your server in a default world. This world will act as a backup 
world and will only show when some kind of error has occurred, but is no issue for now. Let's create a new map!

- Use `/mm create <name>` to make a new world.
- Next up, you can use `/mm info <name>` to view all the properties you can set and change.
- Let's build a map, making sure **BLUE** is facing **EAST** and **RED** is facing **WEST**
- Now, lets start by setting up the map:
  1. Set the spawn points with `/mm spawn add <RED/BLUE/SPECTATOR>` (The spawns will be rounded down so you don't 
     need to be super specific)
  2. Make a worldedit selection around a side and do `/mm setteamregion <TEAM>`. This will be the team boundaries 
     (except for the void). Also do this for the other team.
  3. Set the material for the item using `/mm setmaterial <MATERIAL>`
  4. Enable the map using `/mm setenabled true`, if it didn't enable, check `/mm info` and fix the missing options
  5. Optionally, you can change the void height using `/mm setvoidheight <y-axis>`
- Once completed, load the map using `/game loadmap <name>`

## Contribute

If you want to contribute, feel free! Take a look at one of the issues or the roadmap or suggest your own features 
in the issues / discussions!

## Liscence

The code and repository has been liscenced under GNU GPL 3.0, see the LISCENCE file for details