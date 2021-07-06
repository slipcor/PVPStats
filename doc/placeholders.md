# [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) Placeholders

These are the placeholders you can use where PAPI Placeholders are supported:

**🔴 Shorthand needs to be enabled in the config: 🔴
`/pvpstats config set shortPlaceholders true`**

## Player based statistics

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcorpvpstats_kills | sps_k | The player's kill count
slipcorpvpstats_deaths | sps_d | The player's death count
slipcorpvpstats_streak | sps_s | The player's current streak
slipcorpvpstats_maxstreak | sps_m | The player's highest streak
slipcorpvpstats_elo | sps_e | The ELO player's ELO score 
slipcorpvpstats_ratio | sps_r | The player's kill/death ratio

## Top X list

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcorpvpstats_top_kills_head_5 | sps_t_kills_h_5 | heading ("Top 5 Kills")
slipcorpvpstats_top_kills_1 | sps_t_kills_1 | Top player entry ("1. SLiPCoR: 100")
slipcorpvpstats_top_kills_2 | sps_t_kills_2 | Second player entry ("2. garbagemule: 70")
slipcorpvpstats_top_kills_3 | sps_t_kills_3 | ...
slipcorpvpstats_top_kills_4 | sps_t_kills_4 | ...
slipcorpvpstats_top_kills_5 | sps_t_kills_5 | ...

## Flop X list

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcorpvpstats_flop_kills_head_5 | sps_f_kills_h_5 | heading ("Flop 5 Kills")
slipcorpvpstats_flop_kills_1 | sps_f_kills_1 | Worst player entry ("1. SLiPCoR: 0")
slipcorpvpstats_flop_kills_2 | sps_f_kills_2 | Second worst player entry ("2. garbagemule: 10")
slipcorpvpstats_flop_kills_3 | sps_f_kills_3 | ...
slipcorpvpstats_flop_kills_4 | sps_f_kills_4 | ...
slipcorpvpstats_flop_kills_5 | sps_f_kills_5 | ...

---

Valid statistical entries instead of "kills" for the above lists are:
* **deaths** (🟡 sorting ascending by default! 🟡)
* **streak** (maximum streak)
* **elo** (ELO score)
* **k-d** (kill/death ratio, can be defined to fancy things in the config)
