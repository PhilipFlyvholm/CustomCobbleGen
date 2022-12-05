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

	void init();

	String pluginHookName();

	String getHookName();
	
	boolean isPlayerLeader(UUID uuid);
	
	UUID getIslandLeaderFromPlayer(UUID uuid);
	
	int getIslandLevel(UUID uuid);
	
	boolean hasIsland(UUID uuid);
	
	Player[] getArrayOfIslandMembers(UUID uuid);
	
	void sendMessageToIslandMembers(String message, UUID uuid);
	
	void sendMessageToIslandMembers(String message, UUID uuid, boolean withoutSender);
	
	double getBalance(UUID uuid);
	
	void removeFromBalance(UUID uuid, double amount);
	boolean supportsIslandBalance();
	
	void onGeneratorBlockBreak(UUID uuid);
	
}
