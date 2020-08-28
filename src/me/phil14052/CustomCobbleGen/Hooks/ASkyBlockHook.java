/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
	public int getIslandLevel(Player p) {
		return (int) api.getLongIslandLevel(p.getUniqueId());
	}

	private Island getIslandFromPlayer(Player p) {
		Location loc = api.getIslandLocation(p.getUniqueId());
		if(loc == null) return null;
		Island island = api.getIslandAt(loc);
		if(island == null) return null;
		return island;
	}
	
	@Override
	public boolean isPlayerLeader(Player p) {
		return this.getIslandLeaderFromPlayer(p).equals(p.getUniqueId());
	}

	@Override
	public UUID getIslandLeaderFromPlayer(Player p) {
		Island island = this.getIslandFromPlayer(p);
		if(island == null) return null;
		return island.getOwner();
	}
	
}
