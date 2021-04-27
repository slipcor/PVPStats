package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for fast temporary access to leaderboards
 *
 * Should never be publicly used to SET variables, only for quick access to existing values
 *
 * @author slipcor
 */
public class LeaderboardBuffer {
    private static final Map<String, Long> LASTCHECKEDTOP = new HashMap<>();
    private static final Map<String, Long> LASTCHECKEDFLOP = new HashMap<>();


    private static final Map<String, String[]> TOP = new HashMap<>();
    private static final Map<String, String[]> FLOP = new HashMap<>();
    private LeaderboardBuffer() {
    }

    static {

        LASTCHECKEDTOP.put("KILLS", 0L);    // last time we queried "top kills"
        LASTCHECKEDTOP.put("DEATHS", 0L);   // last time we queried "top deaths"
        LASTCHECKEDTOP.put("STREAK", 0L);   // last time we queried "top streak"
        LASTCHECKEDTOP.put("ELO", 0L);      // last time we queried "top elo"
        LASTCHECKEDTOP.put("K-D", 0L);      // last time we queried "top k-d"

        LASTCHECKEDFLOP.put("KILLS", 0L);    // last time we queried "flop kills"
        LASTCHECKEDFLOP.put("DEATHS", 0L);   // last time we queried "flop deaths"
        LASTCHECKEDFLOP.put("STREAK", 0L);   // last time we queried "flop streak"
        LASTCHECKEDFLOP.put("ELO", 0L);      // last time we queried "flop elo"
        LASTCHECKEDFLOP.put("K-D", 0L);      // last time we queried "flop k-d"

    }

    public static String[] top(int value, String type) {
        type = type.toUpperCase();
        long last = LASTCHECKEDTOP.get(type);
        long now = System.currentTimeMillis() / 1000;
        last -= now;

        if (last < 0) {
            last *= -1; // now we know that seconds have passed, are they enough?
            if (last > PVPStats.getInstance().config().getInt(Config.Entry.STATISTICS_LEADERBOARD_REFRESH)) {

                String[] array = DatabaseAPI.top(10, type);
                if (array == null) {
                    array = new String[0];
                }
                TOP.put(type, array);
                LASTCHECKEDTOP.put(type, now);
            }
        }

        // return saved state
        String[] values = TOP.get(type);

        int length = Math.min(value, values.length); // get a safe value to not overreach

        String[] result = new String[length];

        System.arraycopy(values, 0, result, 0 ,length);

        return result;
    }

    public static String[] flop(int value, String type) {
        type = type.toUpperCase();
        long last = LASTCHECKEDFLOP.get(type);
        long now = System.currentTimeMillis() / 1000;
        last -= now;

        if (last < 0) {
            last *= -1; // now we know that seconds have passed, are they enough?
            if (last > PVPStats.getInstance().config().getInt(Config.Entry.STATISTICS_LEADERBOARD_REFRESH)) {
                String[] array = DatabaseAPI.flop(10, type);
                if (array == null) {
                    array = new String[0];
                }
                FLOP.put(type, array);
                LASTCHECKEDFLOP.put(type, now);
            }
        }

        // return saved state
        String[] values = FLOP.get(type);
        int length = Math.min(value, values.length); // get a safe value to not overreach

        String[] result = new String[length];

        System.arraycopy(values, 0, result, 0 ,length);

        return result;
    }
}
