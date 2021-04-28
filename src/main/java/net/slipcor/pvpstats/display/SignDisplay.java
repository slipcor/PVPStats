    package net.slipcor.pvpstats.display;

    import net.slipcor.core.CoreDebugger;
    import net.slipcor.pvpstats.PVPStats;
    import net.slipcor.pvpstats.api.DatabaseAPI;
    import net.slipcor.pvpstats.api.InformationType;
    import net.slipcor.pvpstats.yml.Language;
    import org.bukkit.Bukkit;
    import org.bukkit.Location;
    import org.bukkit.World;
    import org.bukkit.block.Block;
    import org.bukkit.block.BlockFace;
    import org.bukkit.block.Sign;
    import org.bukkit.block.data.Directional;
    import org.bukkit.configuration.ConfigurationSection;
    import org.bukkit.configuration.file.FileConfiguration;

    import java.io.File;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;

    public class SignDisplay {

        final static List<SignDisplay> DISPLAYS = new ArrayList<>();
        private final BlockFace direction;  // the direction to look for sideways
        private final Location location;    // the initial top-left location
        private int signCount = 0;
        public static CoreDebugger debugger;

        Map<Integer, List<Location>> signMap = new LinkedHashMap<>();
        Map<Integer, InformationType> columns = new LinkedHashMap<>();
        private InformationType column = InformationType.DEATHS;

        public SignDisplay(Location location, BlockFace face) {
            this.location = location;
            this.direction = face;

            discover();
        }

        public static SignDisplay byLocation(Location location) {
            for (SignDisplay display : DISPLAYS) {
                if (display.location.getBlock().equals(location.getBlock())) {
                    return display;
                }
            }
            return null;
        }

        /**
         * Create an OfferingDisplay based on an initial Sign location
         *
         * @param location the location of the top left sign
         * @return an OfferingDisplay, if we found a valid setup
         */
        public static SignDisplay init(Location location) {
            SignDisplay display = null;
            if (location.getBlock().getState() instanceof Sign) {
                String line = ((Sign) location.getBlock().getState()).getLine(0);
                if (line == null || !line.toLowerCase().contains("pvpstats")) {
                    debugger.i("Does not contain PVP Stats: " + line);
                    return null;
                }

                BlockFace face = getPerpendicular(((Directional) location.getBlock().getBlockData()).getFacing());
                if (face != null) {
                    display = new SignDisplay(location, face);
                    if (display.isValid()) {
                        debugger.i("Display is valid");
                        DISPLAYS.add(display);
                    } else {
                        debugger.i("Display is invalid");
                    }
                }
            }
            return display;
        }

        /**
         * Calculate the direction the secondary column will be
         *
         * @param face the direction the sign is looking
         * @return the direction we will find more signs
         */
        private static BlockFace getPerpendicular(BlockFace face) {
            switch (face) {
                case NORTH:
                    return BlockFace.WEST;
                case EAST:
                    return BlockFace.NORTH;
                case SOUTH:
                    return BlockFace.EAST;
                case WEST:
                    return BlockFace.SOUTH;
            }
            return null;
        }

        /**
         * Update all leaderboards
         */
        public static void updateAll() {
            for (SignDisplay display : DISPLAYS) {
                Bukkit.getScheduler().runTask(PVPStats.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        display.update();
                    }
                });
            }
        }

        /**
         * Load all leaderboards from the config
         */
        public static void loadAllDisplays() {
            DISPLAYS.clear();

            FileConfiguration config = PVPStats.getInstance().getConfig();
            ConfigurationSection cs = config.getConfigurationSection("leaderboards");
            if (cs == null) {
                return;
            }
            for (String key : cs.getKeys(false)) {
                Location loc = locationFromString(cs.getString(key));
                if (loc != null) {
                    SignDisplay display = init(loc);
                    if (display != null && display.isValid()) {
                        InformationType column = getSortColumn(key);
                        if (column == null) {
                            column = InformationType.NAME;
                        }
                        display.column = column;
                    }
                }
            }

            PVPStats.getInstance().getLogger().info(DISPLAYS.size() + " leaderboards loaded!");

            for (SignDisplay display : DISPLAYS) {
                display.update();
            }
        }

        /**
         * Save all leaderboards to the config
         */
        public static void saveAllDisplays() {
            PVPStats.getInstance().getConfig().set("leaderboards", null);

            int position = 0;
            for (SignDisplay display : DISPLAYS) {
                debugger.i("Saving display: " + display.column);
                PVPStats.getInstance().getConfig().set("leaderboards." + display.column + (position++), locationToString(display.location));
            }
            try {
                PVPStats.getInstance().getConfig().save(new File(PVPStats.getInstance().getDataFolder(), "config.yml"));
            } catch (IOException e) {
                PVPStats.getInstance().getLogger().severe("Could not save leaderboards to config!");
                e.printStackTrace();
            }

            PVPStats.getInstance().config().appendComments();
        }

        /**
         * Parse a String to a Location
         *
         * @param value the String to parse
         * @return a Location or null if we failed
         */

        private static Location locationFromString(String value) {
            try {
                String[] colon = value.split(":");
                World world = Bukkit.getWorld(colon[0]);
                String[] ints = colon[1].split(",");
                Location result =
                        world.getBlockAt(
                                Integer.parseInt(ints[0]),
                                Integer.parseInt(ints[1]),
                                Integer.parseInt(ints[2])
                        ).getLocation();
                return result;
            } catch (Exception e) {
                PVPStats.getInstance().getLogger().severe("Could not parse location: " + value);
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Parse a Location to String
         *
         * @param location the Location to parse
         * @return a String identifier
         */
        private static String locationToString(Location location) {
            StringBuilder builder = new StringBuilder();
            builder.append(location.getWorld().getName());
            builder.append(':');
            builder.append(location.getBlockX());
            builder.append(',');
            builder.append(location.getBlockY());
            builder.append(',');
            builder.append(location.getBlockZ());
            return builder.toString();
        }

        /**
         * Check whether a location is part of one of our leaderboards
         *
         * @param location the location to check
         * @return whether this block should be protected
         */
        public static boolean needsProtection(Location location) {
            for (SignDisplay display : DISPLAYS) {
                for (List<Location> locs : display.signMap.values()) {
                    for (Location loc : locs) {
                        if (loc.getWorld().equals(location.getWorld()) &&
                                loc.distanceSquared(location) < 4) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Discover all the signs attached below and "to the right"
         */
        private void discover() {
            int offsetSide = 0;
            int offsetDown = 0;

            debugger.i("discovering sign matrix!");

            Block checkBlock = location.getBlock();

            this.signCount = 0;

            while (checkBlock.getRelative(direction, offsetSide).getState() instanceof Sign ||
                    checkBlock.getRelative(direction, offsetSide+1).getState() instanceof Sign) {

                debugger.i("we found a sign");

                List<Location> signs = new ArrayList<>();

                if (!(checkBlock.getRelative(direction, offsetSide).getState() instanceof Sign)) {
                    offsetSide++;
                }

                debugger.i("offsetSide: " + offsetSide);

                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) checkBlock.getRelative(BlockFace.DOWN, offsetDown)
                        .getRelative(direction, offsetSide).getState();

                InformationType sorting = offsetSide > 0 ? getSortColumn(sign) : InformationType.NAME;

                if (sorting == null) {
                    offsetSide++;

                    debugger.i("sorting is null!");

                    continue;
                }

                debugger.i("sorting: " + sorting);

                columns.put(offsetSide, sorting);

                offsetDown++;

                while (checkBlock.getRelative(BlockFace.DOWN, offsetDown).getRelative(direction, offsetSide).getState() instanceof Sign) {
                    debugger.i("discovering " + offsetSide + " - " + offsetDown);
                    signs.add(checkBlock.getRelative(BlockFace.DOWN, offsetDown).getRelative(direction, offsetSide).getLocation());
                    offsetDown++;
                }

                signMap.put(offsetSide, signs);
                signCount = Math.max(signCount, signs.size());

                debugger.i("signCount: " + signCount);

                offsetSide++;
                offsetDown = 0;
            }
        }

        private InformationType getSortColumn(Sign sign) {
            for (String s : sign.getLines()) {
                if (s != null) {
                    for (InformationType c : InformationType.values()) {
                        if (s.toUpperCase().contains(c.name())) {
                            return c;
                        }
                    }
                }
            }
            return null;
        }

        private static InformationType getSortColumn(String string) {
            for (InformationType c : InformationType.values()) {
                if (string.toUpperCase().contains(c.name())) {
                    return c;
                }
            }
            return null;
        }

        /**
         * @return whether we found some signs to show things on
         */
        public boolean isValid() {
            return signCount > 1 && signMap.size() > 1;
        }

        /**
         * Update a leaderboard by recalculating stats and updating the signs
         */
        private void update() {
            List<Map<InformationType, String>> entries = DatabaseAPI.detailedTop(signCount*4, column);

            if (location.getBlock().getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) location.getBlock().getState();
                sign.setLine(1, Language.MSG.MSG_DISPLAY_SORTEDBY.parse());
                sign.setLine(2, Language.MSG.MSG_DISPLAY_SORTEDCOLUMN.parse(column.name()));
                sign.update();
            } else {
                return;
            }

            if (entries.size() < 1) {
                debugger.i("No entries!");
                return;
            }

            for (Integer key : columns.keySet()) {
                debugger.i("column ID " + key);
                InformationType informationType = columns.get(key);
                List<Location> signs  = signMap.get(key);

                writeEntryToSigns(signs, informationType, entries);
            }
        }

        private void writeEntryToSigns(List<Location> signs, InformationType column, List<Map<InformationType, String>> entries) {
            int signPos = -1;

            for (Location loc : signs) {
                signPos++;
                debugger.i("sign position " + signPos);
                if (loc.getBlock().getState() instanceof org.bukkit.block.Sign)
                {
                    org.bukkit.block.Sign sign = (org.bukkit.block.Sign) loc.getBlock().getState();
                    debugger.i("we have a sign!");
                    for (int i= (signPos*4); i<((signPos+1)*4); i++) {
                        debugger.i("i: " + i);
                        if (entries.size() <= i) {
                            debugger.i("no more entries - " + entries.size() + " >= " + i);
                            break; // out of entries
                        }
                        debugger.i("trying to set to : " + entries.get(i).get(column));
                        sign.setLine((i%4), entries.get(i).get(column));
                    }
                    sign.update();
                }
            }
        }

        public InformationType getSortColumn() {
            return this.column;
        }

        public void cycleSortColumn() {
            int ordinal = this.column.ordinal();
            ordinal++;
            if (ordinal >= InformationType.values().length) {
                ordinal = 1;
            }
            this.column = InformationType.values()[ordinal];

            update();
        }
    }
