# v0.X Changelog

## v0.8 - UUID rewrite
- v0.8.36 - use correct placeholder ID
- v0.8.35 - update PlaceholderAPI hook. One year late to the party
- v0.8.34 - try addressing an issue with killstats - thanks to Ken for the help!
- v0.8.33 - allow SQL servers to auto reconnect
- v0.8.32 - thanks to ChrisLane for some cleanup! And some tracker additions for the future
- v0.8.31 - add debugging /pvpstats debug off|on
- v0.8.30 - priorize correct player name input over name LIKE that
- v0.8.29 - allow to disable certain language strings by setting them to ''
- v0.8.28 - add permission "pvpstats.newbie" to protect new players from generating scores when killed (default OFF!!!)
- v0.8.27 - wow guys, how should this ELO stuff work if I never update the internal values? Shame on me!
- v0.8.26 - add currentstreak to the database to not have players lose their precious streaks on server restart :P
- v0.8.25 - finally fix the message override with coloring :)
- v0.8.23 - try to tweak the ELO calculation precision even more
- v0.8.22 - add PlaceholderAPI hook - thanks to @Bonkozorus for the PR!
- v0.8.21 - fix ELO calculation precision
- v0.8.20 - fix override placeholders
- v0.8.19 - add message override
- v0.8.18 - fix startup issue due to wrong default value for TIME
- v0.8.16 - fix purge command and update database to allow for proper purging. NEW COMMAND LOGIC!
- v0.8.15 - add PURGE command to remove old entries from KILL table, add ELO score update message
- v0.8.14 - Add static methods to reset the internal PVPData HashMaps
- v0.8.13 - fix github misalignment
- v0.8.11 - allow ternary operator and comparisms for the KD calculation
- v0.8.10 - allow ternary operator and comparisms for the KD calculation
- v0.8.9 - sorry - fixing the logic, this time for real
- v0.8.8 - fix wrong sorting - thanks, rexbut!
- v0.8.7 - fix a little messup
- v0.8.6
 add "countregulardeaths" - count all deaths, not only PVP ones!
 add "resetkillstreakonquit" - reset killstreaks on server leave!
- v0.8.2 - add ELO rating
- v0.8.1 - remove debug spam ... sorry!
- v0.8 - use UUIDs instead of player names