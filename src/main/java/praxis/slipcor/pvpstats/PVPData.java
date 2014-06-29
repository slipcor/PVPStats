package praxis.slipcor.pvpstats;

@Deprecated
public final class PVPData {

    private PVPData() {
    }

    @Deprecated
    public static boolean addStreak(String name) {
        return net.slipcor.pvpstats.PVPData.addStreak(name);
    }

    @Deprecated
    public static Integer getDeaths(String name) {
        return net.slipcor.pvpstats.PVPData.getDeaths(name);
    }

    @Deprecated
    public static Integer getKills(String name) {
        return net.slipcor.pvpstats.PVPData.getKills(name);
    }

    @Deprecated
    public static Integer getMaxStreak(String name) {
        return net.slipcor.pvpstats.PVPData.getMaxStreak(name);
    }

    @Deprecated
    public static Integer getStreak(String name) {
        return net.slipcor.pvpstats.PVPData.getStreak(name);
    }

    @Deprecated
    public static Integer getEloScore(String name) {
        return net.slipcor.pvpstats.PVPData.getEloScore(name);
    }

    @Deprecated
    public static boolean hasMaxStreak(String name) {
        return net.slipcor.pvpstats.PVPData.hasMaxStreak(name);
    }

    @Deprecated
    public static boolean hasStreak(String name) {
        return net.slipcor.pvpstats.PVPData.hasStreak(name);
    }

    @Deprecated
    public static boolean hasEloScore(String name) {
        return net.slipcor.pvpstats.PVPData.hasEloScore(name);
    }

    @Deprecated
    public static void setDeaths(String name, int value) {
        net.slipcor.pvpstats.PVPData.setDeaths(name, value);
    }

    @Deprecated
    public static void setKills(String sPlayer, int value) {
        net.slipcor.pvpstats.PVPData.setKills(sPlayer, value);
    }

    @Deprecated
    public static void setMaxStreak(String name, int value) {
        net.slipcor.pvpstats.PVPData.setMaxStreak(name, value);
    }

    @Deprecated
    public static void setStreak(String name, int value) {
        net.slipcor.pvpstats.PVPData.setStreak(name, value);
    }
}
