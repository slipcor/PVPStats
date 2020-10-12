# LeaderBoard Setup

You set up a scoreboard by placing any type of Signs in the following way:

![LeaderBoard](doc/images/pvpstats_leaderboard.jpg) 

* Top left sign needs to contain ```PVPStats``` (case insensitive) in the first line
  * Second line contains configurable text ```sorted by```, see the Language file
  * Third line contains the column we sort by, formatting can be changed in the Language file
* Leftmost column and unrecognized columns fall back to player names
* The top sign over another column defines the contents below, it needs to contain one of the following, case insensitive, on any line: 
  * NAME - the player names
  * DEATHS - the amount of deaths
  * KILLS  - the amount of kills
  * ELO - the ELO score
  * CURRENTSTREAK - the current streak value
  * STREAK - the max streak value

The plugin will do its best to skip rows, skip missing signs if things go south, as long as the top left sign is untouched, you can repair it by replacing signs to form the board again.

The LeaderBoard sorting column can be switched by OPs by clicking it. Only OPs can remove the LeaderBoard or alter blocks in a 4 block radius
