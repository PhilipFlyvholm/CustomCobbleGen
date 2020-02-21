/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GenPiston.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import java.util.UUID;

import org.bukkit.Location;

/**
 * @author Philip
 *
 */
public class GenPiston {

	private UUID uuid;
	private Location loc;
	private boolean hasBeenUsed = false;
	
	public GenPiston(Location loc, UUID uuid) {
		this.uuid = uuid;
		this.loc = loc;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
	public Location getLoc() {
		return loc;
	}
	public void setLoc(Location loc) {
		this.loc = loc;
	}
	public boolean hasBeenUsed() {
		return hasBeenUsed;
	}
	public void setHasBeenUsed(boolean hasBeenUsed) {
		this.hasBeenUsed = hasBeenUsed;
	}	
	
	public void setUsed() {
		this.hasBeenUsed = true;
	}
	
}
