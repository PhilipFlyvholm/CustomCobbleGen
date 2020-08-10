/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GeneratorModeManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
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
	private GenMode universalGenMode = null;
	
	
	public GeneratorModeManager() {
		plugin = CustomCobbleGen.getInstance();
		this.generatorModes = new ArrayList<>();
		List<Material> defaultBlocks = new ArrayList<>();
		defaultBlocks.add(Material.WATER);
		defaultBlocks.add(Material.LAVA);
		this.defaultGenMode = new GenMode(0, defaultBlocks, "Cobblestone generator", Material.COBBLESTONE); //THE ID IS 0 SINCE IT WILL ONLY BE USED IF NO OTHER GENMODES ARE LOADED
		this.universalGenMode = new GenMode(-1, defaultBlocks, "Universal generator", null);
	}
	
	public void loadFromConfig() {
		this.generatorModes = new ArrayList<>();
		if(plugin.getConfig().contains(this.generatorSection)) {

			ConfigurationSection section = plugin.getConfig().getConfigurationSection(this.generatorSection);
			for(String s : section.getKeys(false)) {
				List<String> blockNames = section.getStringList(s + ".blocks");
				List<Material> blockMaterials = null;
				if(blockNames != null) {
					blockMaterials = new ArrayList<>();
					for(String name : blockNames) {
						Material m = Material.valueOf(name.toUpperCase());
						if(m == null) {
							plugin.log("&c&lUser error: Unknown material in a generation mode under blocks - &e" +  name.toUpperCase());
							continue;
						}
						blockMaterials.add(m);
					}
				}
				Map<BlockFace, Material> fixedBlockMaterials = null;
				if(section.isConfigurationSection(s + ".fixedBlocks")) {
					fixedBlockMaterials = new HashMap<>();
					for(String fixedBlockFace : section.getConfigurationSection(s + ".fixedBlocks").getKeys(false)) {
						BlockFace blockFace = BlockFace.valueOf(fixedBlockFace.toUpperCase());
						if(blockFace == null || !this.isSupportedBlockFace(blockFace)) {
							plugin.log("&c&lUser error: &e" + fixedBlockFace.toUpperCase() + " &c&l is not a valid block face. Use UP, DOWN, EAST, NORTH, WEST or SOUTH");
							continue;
						}
						String materialName = section.getString(s + ".fixedBlocks." + fixedBlockFace);
						Material m = Material.valueOf(materialName.toUpperCase());
						if(m == null) {
							plugin.log("&c&lUser error: &e" + materialName.toUpperCase() + " &c&l is not a valid material under block face &e" + fixedBlockFace.toUpperCase());
							continue;
						}
						fixedBlockMaterials.put(blockFace, m);
					}
				}
				int id;
				try {
					id = Integer.parseInt(s);
				} catch(NumberFormatException e) {
					plugin.log("&c&lUser error: &e" + s + " is not a valid generation mode id. MOST BE A NUMBER");
					return;
				}
				if(id < 0) {
					plugin.log("&c&lUser error: &e" + id + " is not a valid generation mode id. MOST BE A POSITIVE NUMBER");
					return;
				}
				String name = null;
				if(section.contains(s + ".displayName")) {
					name = section.getString(s + ".displayName");
				}
				Material fallbackMaterial = null;
				if(section.contains(s + ".fallback")) {
					fallbackMaterial = Material.valueOf(section.getString(s + ".fallback").toUpperCase());
					if(fallbackMaterial == null) {
						plugin.log("&c&lUser error: &e" + section.getString(s + ".fallback") + " is not a valid fallback material");
					}
				}
				GenMode mode = new GenMode(id, blockMaterials, fixedBlockMaterials, name, fallbackMaterial);
				if(section.contains(s + ".searchForPlayersNearby")) {
					mode.setSearchForPlayersNearby(section.getBoolean(s + ".searchForPlayersNearby"));
				}
				if(section.contains(s + ".disabledWorlds")) {
					mode.setDisabledWorlds(section.getStringList(s + ".disabledWorlds"));
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
	
	
	public GenMode getModeById(int id) {
		for(GenMode mode : this.getModes()) {
			if(mode.getId() == id) return mode;
		}
		return null;
	}
	
	public boolean isSupportedBlockFace(BlockFace blockFace) {
		if(blockFace == null) return false;
		return blockFace.equals(BlockFace.DOWN) 
				|| blockFace.equals(BlockFace.UP) 
				|| blockFace.equals(BlockFace.WEST) 
				|| blockFace.equals(BlockFace.NORTH) 
				|| blockFace.equals(BlockFace.EAST) 
				|| blockFace.equals(BlockFace.SOUTH);
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

	public GenMode getUniversalGenMode() {
		return universalGenMode;
	}
	
}
