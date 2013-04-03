package praxis.slipcor.pvpstats;

import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * update manager class
 * 
 * -
 * 
 * provides access to update check and methods
 * 
 * @author slipcor
 * 
 * @version v0.0.2
 * 
 */

public final class UpdateManager {
	private UpdateManager() {

	}

	public static boolean msg = false;
	public static boolean outdated = false;
	private static String vOnline;
	private static String vThis;
	private static Plugin instance;

	/**
	 * check for updates, update variables
	 */
	public static void updateCheck(final Plugin plugin) {
		final String pluginUrlString = "http://dev.bukkit.org/server-mods/pvp-stats/files.rss";
		try {
			final URL url = new URL(pluginUrlString);
			final Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			final NodeList nodes = doc.getElementsByTagName("item");
			final Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				final Element firstElement = (Element) firstNode;
				final NodeList firstElementTagName = firstElement
						.getElementsByTagName("title");
				final Element firstNameElement = (Element) firstElementTagName
						.item(0);
				final NodeList firstNodes = firstNameElement.getChildNodes();

				String sOnlineVersion = firstNodes.item(0).getNodeValue();
				final String sThisVersion = instance.getDescription().getVersion();

				while (sOnlineVersion.contains(" ")) {
					sOnlineVersion = sOnlineVersion.substring(sOnlineVersion
							.indexOf(' ') + 1);
				}

				UpdateManager.vOnline = sOnlineVersion.replace("v", "");
				UpdateManager.vThis = sThisVersion.replace("v", "");

				calculateVersions();
				return;
			}
		} catch (Exception e) {
		}
	}

	/**
	 * calculate the message variables based on the versions
	 */
	private static void calculateVersions() {
		final String[] aOnline = vOnline.split("\\.");
		final String[] aThis = vThis.split("\\.");
		outdated = false;

		for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
			try {
				final int onlineVersion = Integer.parseInt(aOnline[i]);
				final int thisVersion = Integer.parseInt(aThis[i]);
				if (onlineVersion == thisVersion) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (onlineVersion > thisVersion);

				UpdateManager.message(null);
			} catch (Exception e) {
				calculateRadixString(aOnline[i], aThis[i]);
			}
		}
	}

	/**
	 * calculate a version part based on letters
	 * 
	 * @param sOnline
	 *            the online letter(s)
	 * @param sThis
	 *            the local letter(s)
	 */
	private static void calculateRadixString(final String sOnline, final String sThis) {
		try {
			final int onlineVersion = Integer.parseInt(sOnline, 46);
			final int thisVersion = Integer.parseInt(sThis, 46);
			if (onlineVersion == thisVersion) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (onlineVersion > thisVersion);

			UpdateManager.message(null);
		} catch (Exception e) {
		}
	}

	/**
	 * message a player if the version is different
	 * 
	 * @param player
	 *            the player to message
	 */
	public static void message(final Player player) {
		if (!(player instanceof Player)) {
			if (msg) {
				if (outdated) {
					instance.getLogger().warning(
							"You are using v" + vThis
									+ ", an outdated version! Latest: "
									+ vOnline);
				} else {
					instance.getLogger()
							.warning(
									"You are using v"
											+ vThis
											+ ", an experimental version! Latest stable: "
											+ vOnline);
				}
			} else {
				instance.getLogger().info("You are on latest version!");
			}
		}
		if (!msg) {
			return;
		}

		if (outdated) {
			player.sendMessage("You are using " + colorize("v" + vThis, 'o')
					+ ", an outdated version! Latest: "
					+ colorize("v" + vOnline, 's'));
		} else {
			player.sendMessage("You are using " + colorize("v" + vThis, 'e')
					+ ", an experimental version! Latest stable: "
					+ colorize("v" + vOnline, 's'));
		}
	}

	/**
	 * colorize a given string based on a char
	 * 
	 * @param string
	 *            the string to colorize
	 * @param color
	 *            the char that decides what color
	 * @return a colorized string
	 */
	private static String colorize(final String string, final char color) {
		if (color == 'o') {
			return ChatColor.RED + string + ChatColor.WHITE;
		} else if (color == 'e') {
			return ChatColor.GOLD + string + ChatColor.WHITE;
		} else if (color == 's') {
			return ChatColor.GREEN + string + ChatColor.WHITE;
		}
		return string;
	}
}
