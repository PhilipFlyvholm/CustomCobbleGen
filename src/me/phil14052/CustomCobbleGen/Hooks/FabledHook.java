/**
 * CustomCobbleGen By @author Philip Flyvholm
 * FabledHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandManager;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

/**
 * @author Philip
 *
 */
public class FabledHook implements IslandHook{
	
	private IslandManager fabledApi;
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public FabledHook() {
		fabledApi = SkyBlockAPI.getIslandManager();
	}

	private Island getIslandFromPlayer(UUID uuid) {
		
		return fabledApi.getIsland(Bukkit.getOfflinePlayer(uuid));
	}
	
	@Override
	public int getIslandLevel(UUID uuid) {
//		Player p = plugin.getServer().getPlayer(uuid);
		Island is = this.getIslandFromPlayer(uuid);
		if(is == null) return 0;
		return (int) is.getLevel().getLevel();
	}


	@Override
	public boolean isPlayerLeader(UUID uuid) {
		return this.getIslandLeaderFromPlayer(uuid).equals(uuid);
	}


	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		plugin.debug("#getIslandLeaderFromPlayer - UUID:" + uuid);
//		Player p = plugin.getServer().getPlayer(uuid);
//		plugin.debug("Player:" + p);
		Island is = this.getIslandFromPlayer(uuid);
		plugin.debug("#getIslandLeaderFromPlayer -" + (is != null ? is.getOwnerUUID().toString() + "'s island" : "NULL"));
		if(is == null) return null;
		return is.getOwnerUUID();
	}

	@Override
	public boolean hasIsland(UUID uuid) {
		return IslandManager.hasIsland(Bukkit.getOfflinePlayer(uuid));
	}

}
