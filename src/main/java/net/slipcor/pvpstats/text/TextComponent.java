package net.slipcor.pvpstats.text;

import org.bukkit.ChatColor;

public class TextComponent {
    private final String text;
    private boolean underline;
    private boolean bold;
    private boolean italic;
    private boolean striked;
    private ChatColor color = ChatColor.WHITE;
    private String command;
    private TextComponent hoverText;

    public TextComponent(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty!");
        }
        this.text = text;
    }

    // GETTERS

    public ChatColor getColor() {
        return this.color;
    }

    public String getCommand() {
        return this.command;
    }

    public TextComponent getHoverText() {
        return this.hoverText;
    }

    public String getText() {
        return text;
    }

    public boolean isBold() {
        return this.bold;
    }

    public boolean isItalic() {
        return this.italic;
    }

    public boolean isStriked() {
        return this.striked;
    }

    public boolean isUnderlined() {
        return this.underline;
    }

    // SETTERS
    
    public TextComponent setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public TextComponent setCommand(String command) {
        this.command = command;
        return this;
    }

    public TextComponent setColor(ChatColor color) {
        this.color = color;
        return this;
    }

    public TextComponent setHoverText(TextComponent hoverText) {
        if (this.equals(hoverText)) {
            throw new IllegalArgumentException("Cannot set hover to itself!");
        }
        if (hoverText.hoverText != null) {
            throw new IllegalArgumentException("Hover cannot have hover!");
        }
        this.hoverText = hoverText;
        return this;
    }
    
    public TextComponent setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }
    
    public TextComponent setStriked(boolean striked) {
        this.striked = striked;
        return this;
    }
    
    public TextComponent setUnderlined(boolean underline) {
        this.underline = underline;
        return this;
    }

    @Override
    public String toString() {
        if (hoverText != null && hoverText.hoverText != null) {
            throw new IllegalArgumentException("Hover cannot have hover!");
        }
        StringBuffer result = new StringBuffer("{\"text\":\"");

        result.append(text);
        result.append('"'); // we are done with the main text, now let us look for variables

        if (isBold()) {
            result.append(",\"bold\":true");
        }
        if (isItalic()) {
            result.append(",\"italic\":true");
        }
        if (isStriked()) {
            result.append(",\"strikethrough\":true");
        }
        if (isUnderlined()) {
            result.append(",\"underlined\":true");
        }
        if (getColor() != ChatColor.WHITE) {
            result.append(",\"color\":\"");
            result.append(getColor().name().toLowerCase());
            result.append('"');
        }
        if (command != null) {
            result.append(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"");
            result.append(command);
            result.append("\"}");
        }
        if (hoverText != null) {
            result.append(",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":");
            result.append(hoverText.toString());
            result.append('}');
        }
        result.append('}');

        return result.toString();
    }
}
