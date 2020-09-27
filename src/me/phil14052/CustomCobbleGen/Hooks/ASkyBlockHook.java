/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import org.bukkit.Location;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

/**
 * @author Philip
 *
 */
public class ASkyBlockHook implements IslandHook{

	private ASkyBlockAPI api;
	
	public ASkyBlockHook() {
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid) {
		return (int) api.getLongIslandLevel(uuid);
	}

	private Island getIslandFromPlayer(UUID uuid) {
		Location loc = api.getIslandLocation(uuid);
		if(loc == null) return null;
		Island island = api.getIslandAt(loc);
		if(island == null) return null;
		return island;
	}
	
	@Override
	public boolean isPlayerLeader(UUID uuid) {
		return this.getIslandLeaderFromPlayer(uuid).equals(uuid);
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return null;
		return island.getOwner();
	}
	
	public boolean hasIsland(UUID uuid) {
		return api.hasIsland(uuid);
	}
	
}
