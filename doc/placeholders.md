# [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) Placeholders

These are the placeholders you can use where PAPI Placeholders are supported:

🟡 if you use [MVdWPlaceholderAPI](https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/), you need to prefix the placeholders with `placeholderapi_` 🟡

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

## Top X list PLUS

### Top values in the last X days

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcorpvpstats_topplus_kills_head_5_30 | sps_tp_kills_h_5_30 | heading ("Top 5 Kills")
slipcorpvpstats_topplus_kills_1_30 | sps_tp_kills_1_30 | Top player entry ("1. SLiPCoR: 100")
slipcorpvpstats_topplus_kills_2_30 | sps_tp_kills_2_30 | Second player entry ("2. garbagemule: 70")
slipcorpvpstats_topplus_kills_3_30 | sps_tp_kills_3_30 | ...
slipcorpvpstats_topplus_kills_4_30 | sps_tp_kills_4_30 | ...
slipcorpvpstats_topplus_kills_5_30 | sps_tp_kills_5_30 | ...

## Top X list WORLD

### Top values in world 'world'  in the last 30 days 

Default Placeholder |  Shorthand | Meaning
------------- | ------------- | -------------
slipcorpvpstats_topworld_kills_head_5_**world**_**3**0 | sps_tw_kills_h_5_**world**_**30** | heading ("Top 5 Kills")
slipcorpvpstats_topworld_kills_1_**world**_**3**0 | sps_tw_kills_1_**world**_**30** | Top player entry ("1. SLiPCoR: 100")
slipcorpvpstats_topworld_kills_2_**world**_**3**0 | sps_tw_kills_2_**world**_**30** | Second player entry ("2. garbagemule: 70")
slipcorpvpstats_topworld_kills_3_**world**_**3**0 | sps_tw_kills_3_**world**_**30** | ...
slipcorpvpstats_topworld_kills_4_**world**_**3**0 | sps_tw_kills_4_**world**_**30** | ...
slipcorpvpstats_topworld_kills_5_**world**_**3**0 | sps_tw_kills_5_**world**_**30** | ...
---

Valid statistical entries instead of "kills" for the above lists are:
* **deaths** (🟡 sorting ascending by default! 🟡)
* **streak** (maximum streak)
* **currentstreak** (current streak value)
* **elo** (ELO score)
* **k-d** (kill/death ratio, can be defined to fancy things in the config)
