/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerBreakGeneratedBlock.java
 */
package me.phil14052.CustomCobbleGen.API;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Philip
 *
 */
public class PlayerBreakGeneratedBlock extends Event implements Cancellable{
	private boolean isCancelled = false;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private Player player = null;
    private Location location = null;
    
    
    public PlayerBreakGeneratedBlock(Player player, Location location) {
    	this.location = location;
    	this.player = player;
    }
    
    public Location getLocation() {
    	return this.location;
    }
    
    public Block getBlock() {
    	return this.getLocation().getBlock();
    }
    
    public Player getPlayer() {
    	return player;
    }
    
	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
	
	public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
