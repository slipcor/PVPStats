# PVP Stats

**Keep track of your users' PVP actions - MySQL or SQLite recommended**

This plugin will keep records of how many kills, deaths, kills in a row a player has, and possibly keep an ELO score.

***

## Features

- Records kills, deaths, max streaks, current streak, kill/death ratio

### Options

- MySQL support (highly recommended)
- SQLite support
- ELO score calculation
- PVP Arena integration

***

## Dependencies

- Spigot 1.12 and up

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/pvp-stats.59124/)


***

## How to install

- Stop your server
- Place jar in plugins folder
- Run a first time to create config
- Configure database settings
- Reboot again, done!

***

## Documentation

- [API](doc/api.md)
- [Commands](doc/commands.md)
- [Permissions](doc/permissions.md)
- [Configuration](doc/configuration.md)
- [LeaderBoards](doc/leaderboards.md)

***

## Changelog

- v1.7.39 - rename API method, add API documentation
- [read more](doc/changelog.md)

***

## Phoning home

By default, the server contacts www.bstats.org to notify that you are using my plugin.

Please refer to their website to learn about what they collect and how they handle the data.

If you want to disable the tracker, set "bStats.enabled" to false in the __config.yml__ !

***

## Credits

- pandapipino for the idea


***

## Todos

- Move language nodes into proper block logic

***