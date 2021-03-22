package net.slipcor.pvpstats.text;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.PlayerNameHandler;
import net.slipcor.pvpstats.core.Config;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextFormatter {


    public static TextComponent[] addPrefix(TextComponent[] message) {
        TextComponent[] prefix = TextFormatter.toTextComponent(Language.MSG_PREFIX.toString());
        TextComponent[] result = Arrays.copyOf(prefix, message.length + prefix.length);
        System.arraycopy(message, 0, result, prefix.length, message.length);
        return result;
    }

    private static TextComponent[] toTextComponent(String string) {
        if (string.contains("&")) {
            string = ChatColor.translateAlternateColorCodes('&', string);
        }

        if (!string.contains("§")) {
            // plain text
            return new TextComponent[]{new TextComponent(string)};
        }

        String[] parts = string.split("§");
        List<TextComponent> list = new ArrayList<>();

        boolean underline = false;
        boolean italic = false;
        boolean bold = false;
        boolean strike = false;
        ChatColor color  = ChatColor.WHITE;

        for (String entry : parts) {
            if (list.isEmpty()) {
                list.add(new TextComponent(entry));
            } else {
                ChatColor chatColor = ChatColor.getByChar(entry.charAt(0));
                switch (chatColor) {

                    case BLACK:
                    case DARK_BLUE:
                    case DARK_GREEN:
                    case DARK_AQUA:
                    case DARK_RED:
                    case DARK_PURPLE:
                    case GOLD:
                    case GRAY:
                    case DARK_GRAY:
                    case BLUE:
                    case GREEN:
                    case AQUA:
                    case RED:
                    case LIGHT_PURPLE:
                    case YELLOW:
                    case WHITE:
                        color = chatColor;
                        break;
                    case MAGIC:
                        // ignore
                        break;
                    case BOLD:
                        bold = true;
                        break;
                    case STRIKETHROUGH:
                        strike = true;
                        break;
                    case UNDERLINE:
                        underline = true;
                        break;
                    case ITALIC:
                        italic = true;
                        break;
                    case RESET:
                        bold = false;
                        strike = false;
                        underline = false;
                        italic = false;
                        color = ChatColor.WHITE;
                        break;
                }
                String content = entry.substring(1);
                if (!content.isEmpty()) {
                    list.add(new TextComponent(content).setUnderlined(underline).setColor(color).setBold(bold).setStriked(strike).setItalic(italic));
                }
            }
        }

        return list.toArray(new TextComponent[0]);
    }


    public static void send(CommandSender sender, TextComponent... components) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            StringBuffer rawCommand = new StringBuffer("tellraw " + player.getName() + " [\"\"");

            for (TextComponent component : components) {
                rawCommand.append(",");
                rawCommand.append(component.toString());
            }

            rawCommand.append(']');

            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    rawCommand.toString());
        } else {
            StringBuffer result = new StringBuffer("");
            for (TextComponent component : components) {
                result.append(component.getColor()).append(component.getText());
            }
            PVPStats.getInstance().getLogger().info(result.toString());
        }
    }

    public static void explainDisableOPMessages(CommandSender sender) {
        List<TextComponent> components = new ArrayList<>();

        String command = "/pvpstats config set " + Config.Entry.OTHER_OP_MESSAGES.getNode() + " false";

        components.add(new TextComponent("You can disable these messages by setting ").setColor(ChatColor.GRAY));
        components.add(new TextComponent(Config.Entry.OTHER_OP_MESSAGES.getNode()).setColor(ChatColor.YELLOW));
        components.add(new TextComponent(" to false or running command ").setColor(ChatColor.GRAY));
        components.add(new TextComponent(command).setColor(ChatColor.YELLOW).setCommand(command).setUnderlined(true));

        send(sender, components.toArray(new TextComponent[0]));
    }

    public static boolean hasContent(TextComponent[] components) {
        if (components == null || components.length < 1) {
            return false;
        }
        for (TextComponent component : components) {
            if (component.getText() != null && !component.getText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void explainAbusePrevention(OfflinePlayer attacker, OfflinePlayer victim) {
        List<TextComponent> message = new ArrayList<>();

        String abuseNode = Config.Entry.STATISTICS_CHECK_ABUSE.getNode();

        message.add(new TextComponent(PlayerNameHandler.getPlayerName(attacker)).setColor(ChatColor.YELLOW));
        message.add(new TextComponent(" killing "));
        message.add(new TextComponent(PlayerNameHandler.getPlayerName(victim)).setColor(ChatColor.YELLOW));
        message.add(new TextComponent(" was not counted as it triggered the 'anti-abuse' system. You can configure " +
                "the anti-abuse system with config nodes "));
        message.add(new TextComponent(abuseNode).setColor(ChatColor.AQUA).setUnderlined(true)
                .setCommand("/pvpstats config set " + abuseNode + " false")
                .setHoverText(new TextComponent("Click here to disable the abuse system completely!").setColor(ChatColor.RED)));
        message.add(new TextComponent(" & "));
        message.add(new TextComponent(Config.Entry.STATISTICS_ABUSE_SECONDS.getNode()).setColor(ChatColor.YELLOW)
                .setHoverText(new TextComponent("This is the grace period in seconds for which a kill of the same player does not count.")));

        PVPStats.getInstance().sendPrefixedOP(Arrays.asList(attacker.getPlayer(), victim.getPlayer()),
                message.toArray(new TextComponent[0]));
    }

    public static void explainNewbieStatus(OfflinePlayer attacker, OfflinePlayer victim) {
        List<TextComponent> message = new ArrayList<>();

        String killer = attacker == null ? "something unknown" : PlayerNameHandler.getPlayerName(attacker);
        String killed = victim == null ? "nothing" : PlayerNameHandler.getPlayerName(victim);

        message.add(new TextComponent(killer).setColor(ChatColor.YELLOW));
        message.add(new TextComponent(" killing "));
        message.add(new TextComponent(killed).setColor(ChatColor.YELLOW));
        message.add(new TextComponent(" was not recorded as one or both players have 'newbie' status. Add permission node '"));
        message.add(new TextComponent("pvpstats.nonewbie").setColor(ChatColor.YELLOW));
        message.add(new TextComponent("' to both players to fix this."));

        List<CommandSender> list = new ArrayList<>();
        if (attacker != null) {
            list.add(attacker.getPlayer());
        }
        if (victim != null) {
            list.add(victim.getPlayer());
        }

        PVPStats.getInstance().sendPrefixedOP(list, message.toArray(new TextComponent[0]));
    }

    public static void explainIgnoredWorld(Player player) {
        List<TextComponent> message = new ArrayList<>();

        String world = player.getWorld().getName();

        message.add(new TextComponent("The death of "));
        message.add(new TextComponent(PlayerNameHandler.getPlayerName(player)).setColor(ChatColor.YELLOW));
        message.add(new TextComponent(" was not counted because the world '"));
        message.add(new TextComponent(world).setColor(ChatColor.YELLOW));
        message.add(new TextComponent("' in is in the ignored list. Edit the config node "));
        message.add(new TextComponent(Config.Entry.IGNORE_WORLDS.getNode()).setColor(ChatColor.AQUA)
                .setHoverText(
                        new TextComponent("Click here to remove the world '" + world + "' from the list!").setColor(ChatColor.YELLOW)
                ).setCommand("/pvpstats config remove " + Config.Entry.IGNORE_WORLDS.getNode() + " " + world));
        message.add(new TextComponent(" to adjust this."));

        PVPStats.getInstance().sendPrefixedOP(Collections.singletonList(player),
                message.toArray(new TextComponent[0]));
    }
}
