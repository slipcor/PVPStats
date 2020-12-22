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

- Spigot 1.8 and up

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

- [Commands](doc/commands.md)
- [Permissions](doc/permissions.md)
- [Configuration](doc/configuration.md)
- [LeaderBoards](doc/leaderboards.md)

***

## Changelog

- v1.6.21 - vanished check actually reversing the logic 
- [read more](doc/changelog.md)

***

## Phoning home

By default, the server contacts my private server for information purposes. It sends your port, IP (for proper server counting), and the plugin version.
That's it! If you want to disable that, set "tracker" to false in the config!

***

## Credits

- pandapipino for the idea


***