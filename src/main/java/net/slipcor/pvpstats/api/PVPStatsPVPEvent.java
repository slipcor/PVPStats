package net.slipcor.pvpstats.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PVPStatsPVPEvent extends Event implements Cancellable {
    private final OfflinePlayer killer;
    private final OfflinePlayer victim;

    boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    PVPStatsPVPEvent(OfflinePlayer killer, OfflinePlayer victim) {
        this.killer = killer;
        this.victim = victim;
    }

    public OfflinePlayer getKiller() {
        return killer;
    }

    public OfflinePlayer getVictim() {
        return victim;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
