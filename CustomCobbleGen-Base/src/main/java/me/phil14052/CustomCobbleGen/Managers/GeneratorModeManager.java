/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GeneratorModeManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Setting;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.*;

/**
 * @author Philip
 *
 */
public class GeneratorModeManager {

	private static GeneratorModeManager instance = null;
	private static CustomCobbleGen plugin = null;
	
	private List<GenMode> generatorModes;
	private final GenMode defaultGenMode;
	private final GenMode universalGenMode;
	
	
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
		if(plugin.getConfig().contains(Setting.SECTION_GENERATIONMODES.getPath())) {

			ConfigurationSection section = plugin.getConfig().getConfigurationSection(Setting.SECTION_GENERATIONMODES.getPath());
			if(section == null){
				plugin.error("No generation mode section found");
				return;
			}
			for(String s : section.getKeys(false)) {
				List<String> blockNames = section.getStringList(s + ".blocks");
				List<Material> blockMaterials;
				blockMaterials = new ArrayList<>();
				for(String name : blockNames) {
					Material m = Material.valueOf(name.toUpperCase());
					blockMaterials.add(m);
				}
				Map<BlockFace, Material> fixedBlockMaterials = null;
				if(section.isConfigurationSection(s + ".fixedBlocks")) {
					fixedBlockMaterials = new HashMap<>();
					for(String fixedBlockFace : Objects.requireNonNull(section.getConfigurationSection(s + ".fixedBlocks")).getKeys(false)) {
						BlockFace blockFace = BlockFace.valueOf(fixedBlockFace.toUpperCase());
						if(!this.isSupportedBlockFace(blockFace)) {
							plugin.error(fixedBlockFace.toUpperCase() + " &c&l is not a valid block face. Use UP, DOWN, EAST, NORTH, WEST or SOUTH", true);
							continue;
						}
						String materialName = section.getString(s + ".fixedBlocks." + fixedBlockFace);
						if(materialName == null) {
							plugin.error("&c&lSyntax error under block face &e" + fixedBlockFace.toUpperCase() + " - No material name", true);
							continue;
						}
						Material m = Material.getMaterial(materialName.toUpperCase());
						if(m == null) {
							plugin.error(materialName.toUpperCase() + " &c&l is not a valid material under block face &e" + fixedBlockFace.toUpperCase(), true);
							continue;
						}
						fixedBlockMaterials.put(blockFace, m);
					}
				}
				int id;
				try {
					id = Integer.parseInt(s);
				} catch(NumberFormatException e) {
					plugin.error(s + " is not a valid generation mode id. MOST BE A NUMBER", true);
					return;
				}
				if(id < 0) {
					plugin.error(id + " is not a valid generation mode id. MOST BE A POSITIVE NUMBER", true);
					return;
				}
				String name = null;
				if(section.contains(s + ".displayName")) {
					name = section.getString(s + ".displayName");
				}
				Material fallbackMaterial = null;
				if(section.contains(s + ".fallback")) {
					fallbackMaterial = Material.getMaterial(Objects.requireNonNull(section.getString(s + ".fallback")).toUpperCase());
					if(fallbackMaterial == null) {
						plugin.error(section.getString(s + ".fallback") + " is not a valid fallback material", true);
					}
				}
				GenMode mode = new GenMode(id, blockMaterials, fixedBlockMaterials, name, fallbackMaterial);
				if(section.contains(s + ".searchForPlayersNearby")) {
					mode.setSearchForPlayersNearby(section.getBoolean(s + ".searchForPlayersNearby"));
				}
				if(section.contains(s + ".disabledWorlds")) {
					mode.setDisabledWorlds(section.getStringList(s + ".disabledWorlds"));
				}
				if(section.contains(s + ".generationSound")) {
					String soundString = section.getString(s+ ".generationSound");
					if(soundString != null && !soundString.equalsIgnoreCase("none")) {
						Arrays.stream(Sound.values())
								.filter(sound -> sound.name().equalsIgnoreCase(soundString))
								.findFirst()
								.ifPresentOrElse(mode::setGenSound, () -> plugin.error("The sound " + soundString + " does not exist", true));
					}
				}
				if(section.contains(s + ".particleEffect")) {
					String particle = section.getString(s+ ".particleEffect");
					if(particle != null) {
						ParticleEffect[] effects = ParticleEffect.values();
						Arrays.stream(effects)
								.filter(particleEffect -> particleEffect.name().equalsIgnoreCase(particle))
								.findFirst()
								.ifPresent(mode::setParticleEffect);
					}
				}

				if(section.contains(s + ".canGenerateWhileRaining")) {
					boolean canGenWhileRaining = section.getBoolean(s+ ".canGenerateWhileRaining");
					mode.setCanGenWhileRaining(canGenWhileRaining);
				}
				if(mode.isValid()) {
					this.generatorModes.add(mode);
				}
			}
		}
		
		if(this.generatorModes.isEmpty()){
			plugin.error("COULD NOT FIND ANY GENERATION MODES IN CONFIG. USING DEFAULT INSTEAD!");
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
