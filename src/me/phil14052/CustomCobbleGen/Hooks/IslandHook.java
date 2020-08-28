/**
 * CustomCobbleGen By @author Philip Flyvholm
 * IslandLevelHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * @author Philip
 *
 */
public interface IslandHook {
	
	public boolean isPlayerLeader(Player p);
	
	public UUID getIslandLeaderFromPlayer(Player p);
	
	public int getIslandLevel(Player p);
	
}
