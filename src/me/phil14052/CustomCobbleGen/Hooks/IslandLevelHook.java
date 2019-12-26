/**
 * CustomCobbleGen By @author Philip Flyvholm
 * IslandLevelHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import org.bukkit.entity.Player;

/**
 * @author Philip
 *
 */
public interface IslandLevelHook {
	
	public HookType getHookType();
	
	public int getIslandLevel(Player p);
	
}
