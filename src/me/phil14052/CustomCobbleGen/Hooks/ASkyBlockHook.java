/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import org.bukkit.entity.Player;

import com.wasteofplastic.askyblock.ASkyBlockAPI;

/**
 * @author Philip
 *
 */
public class ASkyBlockHook implements IslandLevelHook{

	private ASkyBlockAPI api;
	
	public ASkyBlockHook() {
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	public int getIslandLevel(Player p) {
		return (int) api.getLongIslandLevel(p.getUniqueId());
	}
	
}
