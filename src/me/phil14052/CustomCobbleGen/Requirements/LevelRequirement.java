/**
 * CustomCobbleGen By @author Philip Flyvholm
 * LevelRequirement.java
 */
package me.phil14052.CustomCobbleGen.Requirements;

import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Hooks.IslandLevelHook;

/**
 * @author Philip
 *
 */
public class LevelRequirement implements Requirement {

	private int levelNeeded = 0;
	private CustomCobbleGen plugin;
	
	public LevelRequirement(int levelNeeded) {
		this.levelNeeded = levelNeeded;
		plugin = CustomCobbleGen.getInstance();
	}
	
	@Override
	public boolean furfillsRequirement(Player p) {
		if(!plugin.isConnectedToIslandPlugin()) {
			plugin.log("&cThere is a island requirement in use, but there are no island/skyblock plugins installed. Returning true.");
			return true;
		}
		IslandLevelHook hook = plugin.islandPluginHooked.getLevelHook();
		int currentLevel = hook.getIslandLevel(p);
		plugin.debug(currentLevel);
		return currentLevel >= this.getRequirementValue();
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.LEVEL;
	}

	@Override
	public int getRequirementValue() {
		return this.levelNeeded;
	}

}
