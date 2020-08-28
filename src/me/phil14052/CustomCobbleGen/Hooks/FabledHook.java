/**
 * CustomCobbleGen By @author Philip Flyvholm
 * FabledHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.island.Island;

/**
 * @author Philip
 *
 */
public class FabledHook implements IslandHook{
	
	private SkyBlock fabledApi;
	
	public FabledHook() {
		fabledApi = SkyBlockAPI.getImplementation();
	}
	

	private Island getIslandFromPlayer(Player p) {
		return fabledApi.getIslandManager().getIsland(p.getPlayer());
		
	}
	
	@Override
	public int getIslandLevel(Player p) {
		Island is = this.getIslandFromPlayer(p);
		if(is == null) return 0;
		return (int) is.getLevel().getLevel();
	}


	@Override
	public boolean isPlayerLeader(Player p) {
		return this.getIslandLeaderFromPlayer(p).equals(p.getUniqueId());
	}


	@Override
	public UUID getIslandLeaderFromPlayer(Player p) {
		Island is = this.getIslandFromPlayer(p);
		if(is == null) return null;
		return is.getOwnerUUID();
	}

}
