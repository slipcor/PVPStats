package net.slipcor.pvpstats.uuid;

import net.slipcor.pvpstats.PSMySQL;
import net.slipcor.pvpstats.PVPStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class UUIDUpdater {

    public UUIDUpdater(PVPStats plugin, String dbTable) {

        List<String> players = PSMySQL.getAllPlayers(dbTable);

        if (players != null && players.size() > 0) {

            UUIDFetcher fetcher = new UUIDFetcher(players);

            try {
                Map<String, UUID> result = fetcher.call();
                for (Entry<String, UUID> set : result.entrySet()) {
                    add(dbTable, set);
                }
                commit(dbTable);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void commit(String dbTable) {
        PSMySQL.commit(dbTable, map);

        map.clear();
    }

    private final Map<String, UUID> map = new HashMap<String, UUID>();

    private void add(String dbTable, Entry<String, UUID> set) {
        map.put(set.getKey(), set.getValue());
        if (map.size() > 9) {
            commit(dbTable);
        }
    }
}
