package me.phil14052.CustomCobbleGen.API;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Hooks.IslandHook;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * CustomCobbleGenAPI.java
 */
public class CustomCobbleGenAPI {

	private static CustomCobbleGenAPI instance = null;
	private final TierManager tierManager;
	private final CustomCobbleGen plugin;
	private final BlockManager bm = BlockManager.getInstance();
	
	/**
	 * Instantiate the API
	 * Use CustomCobbleGenAPI.getAPI() to get instance!
	 */
	public CustomCobbleGenAPI() {
		plugin = CustomCobbleGen.getInstance();
		tierManager = TierManager.getInstance();
	}
	
	/**
	 * Get a list of active tiers
	 * @return This returns a map with class names as keys and a list of tiers in that class
	 */
	public Map<String, List<Tier>> getTiers() {
		return tierManager.getTiers();
	}
	
	
	/**
	 * Get a instance of the API
	 * @return returns an active instance of the API
	 */
	public static CustomCobbleGenAPI getAPI() {
		if(instance == null) instance = new CustomCobbleGenAPI();
		return instance;
	}

	/**
	 * Add an custom hook
	 * @param hook the new hook
	 */
	public void addIslandHook(IslandHook hook){
		plugin.getIslandHooks().add(hook);
		plugin.connectToIslandPlugin();
	}

	public void registerBlockBreak(Player player, Location location){
		if(bm.isGenLocationKnown(location)) {
			bm.setPlayerForLocation(player.getUniqueId(), location, false);
		}
	}
}
