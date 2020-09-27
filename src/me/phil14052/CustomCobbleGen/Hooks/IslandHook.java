/**
 * CustomCobbleGen By @author Philip Flyvholm
 * IslandLevelHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

/**
 * @author Philip
 *
 */
public interface IslandHook {
	
	public boolean isPlayerLeader(UUID uuid);
	
	public UUID getIslandLeaderFromPlayer(UUID uuid);
	
	public int getIslandLevel(UUID uuid);
	
	public boolean hasIsland(UUID uuid);
	
}
