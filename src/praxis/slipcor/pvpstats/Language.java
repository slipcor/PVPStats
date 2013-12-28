package praxis.slipcor.pvpstats;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public enum Language {
	HEAD_LINE("head.line", "&7---------------"),
	HEAD_HEADLINE("head.headline", "&cPVP Stats Top &7%0% &c%1%"),
	HEAD_KILLS("head.kills", "Kills"),
	HEAD_DEATHS("head.deaths", "Deaths"),
	HEAD_STREAK("head.streaks", "Streaks"),
	HEAD_RATIO("head.ratio", ""),
	INFO_FORMAT("info.format", "&c%0%: &7%1%"),
	INFO_NAME("info.name", "Name"),
	INFO_KILLS("info.kills", "Kills"),
	INFO_DEATHS("info.deaths", "Deaths"),
	INFO_MAXSTREAK("info.maxstreak", "Max Streak"),
	INFO_STREAK("info.streak", "Streak"),
	INFO_RATIO("info.ratio", "Ratio"),
	INFO_PLAYERNOTFOUND("info.playernotfound", "Player not found: %0%"),
	MSG_CLEANED("msg.cleaned", "Statistics successfully cleaned up! %0% entries removed!"),
	MSG_NOPERMRELOAD("msg.nopermreload", "No permission to reload!"),
	MSG_NOPERMCLEANUP("msg.nopermcleanup", "No permission to cleanup!"),
	MSG_NOPERMWIPE("msg.nopermwipe", "No permission to wipe!"),
	MSG_PREFIX("msg.prefix", "[PVP Stats] "),
	MSG_RELOADED("msg.reloaded", "Configuration reloaded!"),
	MSG_UPDATE("msg.update","Update available check dev.bukkit.org!"),
	MSG_UPDATED("msg.updated","Update installed! please restart the server!"),
	MSG_WIPED("msg.wiped","Statistics wiped!"),
	MSG_WIPEDFOR("msg.wipedfor","Statistics wiped for %0%!");
	
	private final String sDefault;
	private final String node;
	private String value = null;
	
	private Language(final String node, final String content) {
		this.node = node;
		sDefault = content;
	}
	
	public void override(final String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		final String result = (value == null) ? sDefault : value;
		return ChatColor.translateAlternateColorCodes('&', result);
	}

	public String toString(String... args) {
		String result = value == null ? sDefault : value;
		for (int pos = 0; pos<args.length; pos++) {
			result = result.replace("%"+pos+"%", args[pos]);
		}
		return ChatColor.translateAlternateColorCodes('&', result);
	}
	
	/**
	 * Load a Configuration, possibly add defaults 
	 * @param cfg the Configuration to access
	 * @return true if the file has been updated and should be saved
	 */
	public static boolean load(final FileConfiguration cfg) {
		boolean changed = false;
		
		for (Language lang : Language.values()) {
			if (cfg.get(lang.node) == null) {
				cfg.set(lang.node, lang.sDefault);
				changed = true;
			} else {
				lang.override(cfg.getString(lang.node));
			}
		}
		
		return changed;
	}
}
