# v1.X Changelog

- v1.3.13 - round placeholder ratio to 2 digits
- v1.3.12 - add placeholder and API calculation for the ratio
- v1.3.11 - address SQL databases not getting any updates
- v1.3.10 - address #21 and allow to use /pvpstats [playername] if the name is valid
- v1.3.9 - address #20 and add players to database for SQLite databases
- v1.3.8 - stop displaying warnings for the world column addition - it does not matter
- v1.3.7 - support MySQL expansion without kill entries
- v1.3.6 - check kill stats table for column, not the normal stats table
- v1.3.5 - add kill world to the kill stats databases
- v1.3.4 - use OfflinePlayer when hooking to Placeholder API
- v1.3.3 - add warning if precise kill stat collection is activated, as these stats show up nowhere in the plugin or API
- v1.3.2 - address #15 and update kills and death for other plugins to get the current stats (e.g. placeholders)
- v1.3.1 - fix various weird issues with the config, causing all sorts of errors
- v1.3.0 - this will be the last rewrite for a while, a lot of classes have been renamed and a lot of JavaDocs have been added
- v1.2.1 - new command /pvp set [player] [type] [amount] to set specific player statistics
- v1.2.0 - add YML support - use double caution!
- v1.1.0 - add SQLite support - use with caution!
- v1.0.0 - build against 1.13 API, use my own updater, remove UUID updating. It's been years.
