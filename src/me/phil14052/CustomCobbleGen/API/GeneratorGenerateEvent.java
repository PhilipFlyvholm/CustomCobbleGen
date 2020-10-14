/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GeneratorGenerateEvent.java
 */
package me.phil14052.CustomCobbleGen.API;

import me.phil14052.CustomCobbleGen.Managers.GenMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Philip
 *
 */
public class GeneratorGenerateEvent extends Event implements Cancellable{

	private boolean isCancelled = false;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
	private GenMode mode = null;
	private Tier tier = null;
	private Material result = null;
	private UUID uuid = null;
	private Location toBlock = null;
	private boolean fallback = false;
    
    
    public GeneratorGenerateEvent(GenMode mode, Tier tier, Material result, UUID uuid, Location toBlock) {
    	this.mode = mode;
    	this.tier = tier;
    	this.result = result;
    	this.uuid = uuid;
    	this.toBlock = toBlock;
    	this.fallback = false;
    }
    
    public GeneratorGenerateEvent(GenMode mode, Tier tier, Material result, UUID uuid, Location toBlock, boolean isFallback) {
    	this.mode = mode;
    	this.tier = tier;
    	this.result = result;
    	this.uuid = uuid;
    	this.toBlock = toBlock;
    	this.fallback = isFallback;
    }
    
    /**
     * Get the generation mode used
     * @return Returns GenMode
     */
    public GenMode getGenerationMode() {
    	return mode;
    }
    
    /**
     * Gets the tier used
     * @return Returns Tier used
     */
    public Tier getTierUsed() {
    	return tier;
    }
    
    /**
     * Gets the player that is generating the block
     * @return Returns UUID of player
     */
    public UUID getPlayerGenerating() {
    	return uuid;
    }
    
    /**
     * Get the result of the generation
     * @return Returns the Material of the result
     */
    public Material getResult() {
    	return result;
    }
    
    /**
     * Sets the result that will spawn
     * @param result - Material that will generate (BLOCK TYPES ONLY)
     */
    public void setResult(Material result) {
    	this.result = result;
    }
    
    
    /**
     * Get the location the block will spawn
     * @return Returns the location where the generator will generate the blocks
     */
    public Location getGenerationLocation() {
    	return toBlock;
    }
    
    /**
     * Sets the location of where the generation will happen    
     * @param location - Bukkit#Location (Must be loaded)
     */
    public void setGenerationLocation(Location location) {
    	this.toBlock = location;
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

	public boolean isFallback() {
		return fallback;
	}
	
}
