/**
 * CustomCobbleGen By @author Philip Flyvholm
 * IslandLevelHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Philip
 *
 */
public interface IslandHook {
	
	public String getHookName();
	
	public boolean isPlayerLeader(UUID uuid);
	
	public UUID getIslandLeaderFromPlayer(UUID uuid);
	
	public int getIslandLevel(UUID uuid);
	
	public boolean hasIsland(UUID uuid);
	
	public Player[] getArrayOfIslandMembers(UUID uuid);
	
	public void sendMessageToIslandMembers(String message, UUID uuid);
	
	public void sendMessageToIslandMembers(String message, UUID uuid, boolean withoutSender);
	
	public double getBalance(UUID uuid);
	
	public void removeFromBalance(UUID uuid, double amount);
	public boolean supportsIslandBalance();
	
	public void onGeneratorBlockBreak(UUID uuid);
	
}
