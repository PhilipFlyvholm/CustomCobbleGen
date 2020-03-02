/**
 * CustomCobbleGen By @author Philip Flyvholm
 * FabledHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import org.bukkit.entity.Player;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.island.Island;

/**
 * @author Philip
 *
 */
public class FabledHook implements IslandLevelHook{
	
	private SkyBlock fabledApi;
	
	public FabledHook() {
		fabledApi = SkyBlockAPI.getImplementation();
	}
	

	@Override
	public int getIslandLevel(Player p) {
		Island is = fabledApi.getIslandManager().getIsland(p.getPlayer());
		if(is == null) return 0;
		return (int) is.getLevel().getLevel();
	}

}
