# Command list

Click on a command to view more information about it. The shorthand can be used instead of the command name, E.g. `/pvpstats !w` to wipe stats.

## Commands about getting stats
_Get your or other player's stats_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/pvpstats show](commands/show.md) | !sh | show your own stats
[/pvpstats show](commands/show.md) [player] | /stats !sh [player] | show the player's stats
[/pvpstats top](commands/top.md) [amount] | /stats !t [amount] | show the top [amount] killers
[/pvpstats top](commands/top.md) [type] [amount] | /stats !t [type] [amount] | show the top [amount] of a category

Valid categories are: kills, deaths, streak, elo

***

## Commands about setting stats
_Manually set a certain value of a player's statistic_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/pvpstats set](commands/set.md) | /stats !st [player] [type] [amount] | set a player's [type] statistic

Valid types are: kills, deaths, streak, currentstreak, elo

***

## Database management commands
_Heavy maintenance_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/pvpstats cleanup](commands/cleanup.md) | /stats !c | remove duplicate entries
[/pvpstats debug](commands/debug.md) [on/off] | /stats !d [on/off] | activate or deactivate debugging
[/pvpstats debugkill](commands/debugkill.md) {killer} {victim} | /stats !dk {killer} {victim} | manually add a kill for debugging
[/pvpstats migrate](commands/migrate.md) [from/to] [yml/sqlite/mysql] | migrate from / to other database method
[/pvpstats purge](commands/purge.md) [standard/specific/both] [days] | /stats !p [standard/specific/both] [days] | remove entries older than [days], defaults to 30
[/pvpstats reload](commands/reload.md) | /stats !r | reload config and language files
[/pvpstats wipe](commands/wipe.md) {player} | /stats !w {player} | wipe a player's or all statistics

***