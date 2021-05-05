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
- My [Core](https://github.com/slipcor/Core) library (automatically added to the plugin before release)

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/pvp-stats.59124/)
- [Discord - #pvpstats-builds](https://discord.gg/BNkk46vRKa)


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

- v1.10.4 - fix language file default values - sorry to new users! 
- [read more](doc/changelog.md)

***

## Phoning home

By default, the server contacts www.bstats.org to notify that you are using my plugin.

Please refer to their website to learn about what they collect and how they handle the data.

If you want to disable the tracker, set "bStats.enabled" to false in the __config.yml__ !

***

## Support

I am developing this plugin in my free time, so if you have an issue, please create an issue describing the problem in detail. I will do my best to reply as soon as possible, but that can take a few days sometimes.

For some problems, it might be easier to join the [Discord](https://discord.gg/DSNfjYA) and open a ticket there. It allows for quicker transfer of log files, debug files and so on. Moreover, simple questions can be answered more quickly there rather than opening an issue for them.

Joining the Discord Server gets you early access to latest builds, and maybe it is your preferred method to interact with me? Be my guest!

***

## Credits

- pandapipino for the idea


***

## Todos

- Move language nodes into proper block logic

***