/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GeneratorModeManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

/**
 * @author Philip
 *
 */
public class GeneratorModeManager {

	private static GeneratorModeManager instance = null;
	private static CustomCobbleGen plugin = null;
	
	private List<GenMode> generatorModes = null;
	private String generatorSection = "options.generationModes";
	private GenMode defaultGenMode = null;
	
	public GeneratorModeManager() {
		plugin = CustomCobbleGen.getInstance();
		this.generatorModes = new ArrayList<>();
		this.defaultGenMode = new GenMode(Material.WATER, Material.LAVA);
	}
	
	public void loadFromConfig() {
		this.generatorModes = new ArrayList<>();
		if(plugin.getConfig().contains(this.generatorSection)) {

			ConfigurationSection section = plugin.getConfig().getConfigurationSection(this.generatorSection);
			for(String s : section.getKeys(false)) {
				String firstBlock = section.getString(s + ".firstBlock").toUpperCase();
				String secondBlock = section.getString(s + ".secondBlock").toUpperCase();
				GenMode mode = new GenMode(firstBlock, secondBlock);
				if(section.contains(s + ".searchForPlayersNearby")) {
					mode.setSearchForPlayersNearby(section.getBoolean(s + ".searchForPlayersNearby"));
				}
				if(mode.isValid()) {
					this.generatorModes.add(mode);
				}
			}
		}
		
		if(this.generatorModes.isEmpty()){
			plugin.log("&cERROR: COULD NOT FIND ANY GENERATION MODES IN CONFIG. USING DEFAULT INSTEAD!");
			this.generatorModes.add(this.defaultGenMode);
		}
		
	}
	
	public List<GenMode> getModesContainingMaterial(Material m) {
		List<GenMode> modesContainingMaterial = new ArrayList<>();
		for(GenMode mode : this.getModes()) {
			if(mode.containsBlock(m)) modesContainingMaterial.add(mode);
		}
		return modesContainingMaterial;
	}
	
	public List<GenMode> getModes(){
		return this.generatorModes;
	}
	
	
	public static GeneratorModeManager getInstance() {
		if(instance == null) instance = new GeneratorModeManager();
		return instance;
	}
	
}
