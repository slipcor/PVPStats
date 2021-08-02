# How to use the API

This documentation is not extensive, but explains up the methods that I would deem safe to use by other plugins, and eventual caveats.

## net.slipcor.pvpstats.api.DatabaseAPI

### AkilledB - emulate a Player kill

    OfflinePlayer attacker; // the player who kills      - can be null
    OfflinePlayer victim;   // the player who was killed - can be null

    // There will be checks for newbie status, whether both players are valid Player objects, etc
    DatabaseAPI.AkilledB(attacker, victim);

### setSpecificStat - set a specific statistic value

    OfflinePlayer player; // the player whose value to set
    String entry; // the attribute to set
    int value; // the value to set it to
    
    // valid values for entry: "elo", "kills", "deaths", "streak", "currentstreak"
    DatabaseAPI.setSpecificStat(player, entry, value)

## net.slipcor.pvpstats.api.LeaderboardBuffer

### top - get the top stats 

    int value = 10;         // how many entries to get (maximum is set in "maxListLength");
    String type = "kills";  // the information to get and to sort by
    int offset = 0;         // an offset for pagination. 0 for first page, value for second page, value * 2 for third page...
    
    // valid values for type: "elo", "kills", "deaths", "streak", "currentstreak"
    String[] lines = LeaderBoardBuffer.top(value, type, offset);

    // if you want to get the second page, you need to increase 0 by value
    
    // lines will be an array of formatted values, by default "1. {player}: {value}"


### flop - get the bottom stats

    int value = 10;     // how many entries to get (maximum is set in "maxListLength");
    String type = "kills";  // the information to get and to sort by
    
    // valid values for type: "elo", "kills", "deaths", "streak", "currentstreak"
    String[] lines = LeaderboardBuffer.flop(value, type);
    
    // lines will be an array of formatted values, by default "1. {player}: {value}"

## net.slipcor.api.PVPStatsPVPEvent

    // do not forget to register listeners in your main plugin!
    @EventHandler
    public void onPVPStatsKill(final PVPStatsPVPEvent event) {
        OfflinePlayer killer = event.getKiller();
        OfflinePlayer victim = event.getVictim();
        
        if (killer != null && killer.equals(victim)) {
            event.setCancelled(true);
        }
    }

## net.slipcor.api.PlayerStatisticsBuffer

All methods do **not** alter the database. Some of them do query the database if there is not a value loaded yet.


### remove temporary values to have them reload from the database

    OfflinePlayer player; // the player whose values to access
    
    // clear a player's statistics as a whole
    PlayerStatisticsBuffer.clear(player.getUniqueId());
    
    // clear a player's deaths
    PlayerStatisticsBuffer.clearDeaths(player.getUniqueId());
    
    // clear a player's kills
    PlayerStatisticsBuffer.clearKills(player.getUniqueId());
    
    // clear a player's max streak
    PlayerStatisticsBuffer.clearMaxStreak(player.getUniqueId());
    
    // clear a player's current streak
    PlayerStatisticsBuffer.clearStreak(player.getUniqueId());
    
    // clear a player's ELO score
    PlayerStatisticsBuffer.clearEloScore(player.getUniqueId());

### retrieve buffered values, query database if needed

    OfflinePlayer player; // the player whose values to access
    
    // read a player's deaths, query the database only if needed
    int deaths = PlayerStatisticsBuffer.getDeaths(player.getUniqueId());
    
    // read a player's kills, query the database only if needed
    int kills = PlayerStatisticsBuffer.getKills(player.getUniqueId());
    
    // read a player's max streak, query the database only if needed
    int maxstreak = PlayerStatisticsBuffer.getMaxStreak(player.getUniqueId());
    
    // read a player's current streak, query the database only if needed
    int streak = PlayerStatisticsBuffer.getStreak(player.getUniqueId());
    
    // read a player's ELO score, query the database only if needed
    int eloscore = PlayerStatisticsBuffer.getEloScore(player.getUniqueId());
    
    // read a player's kill/death ratio, query the database only if needed
    int ratio = PlayerStatisticsBuffer.getRatio(player.getUniqueId());

### miscellaneous

    OfflinePlayer player; // the player whose values to access
    
    // preload all player's values. Query the database if needed
    PlayerStatisticsBuffer.loadPlayer(player.getUniqueId());
    
    // clear all statistics and let them reload from the database
    PlayerStatisticsBuffer.refresh();