# Cleanup Command

## Description

This command removes duplicate entries from the database

## Usage Examples

Command |  Definition
------------- | -------------
/pvpstats cleanup | do the cleanup

## Details

When the plugin malfunctions or players actually rename, there can be a chance of player entries duplicating or simply put a player name being in two different UUID slots.

For ease of use, PVP Stats internally often uses player names (do people really get a player's uuid and type that to do stuff with them?).

So this cleanup might me necessary in the event of a player name being taken over by another player. Or an entry being duplicated.

