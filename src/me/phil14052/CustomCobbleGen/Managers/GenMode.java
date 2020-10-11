/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GenMode.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Philip
 *
 */
public class GenMode {
	private boolean searchForPlayersNearby = false;
	private List<Material> blocks = null;
	private Map<BlockFace, Material> fixedBlocks = null;
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private List<String> disabledWorlds = new ArrayList<>();
	private int id;
	private boolean valid = false;
	private String name = null;
	private Sound genSound = null;
	private Material fallbackMaterial = null;
	private ParticleEffect particleEffect = null;
	
	public GenMode(int id, List<Material> blocks, String name, Material fallbackMaterial) {
		this(id, blocks, null, true, null, name, fallbackMaterial);
	}
	
	public GenMode(int id, List<Material> blocks, Map<BlockFace, Material> fixedBlocks, String name, Material fallbackMaterial) {
		this(id, blocks, fixedBlocks, true, null, name, fallbackMaterial);
	}
	
	public GenMode(int id, List<Material> blocks, Map<BlockFace, Material> fixedBlocks, boolean searchForPlayersNearby, List<String> disabledWorlds, String name, Material fallbackMaterial) {
		if(blocks == null) { //CREATE A EMPTY LIST IF ONLY "FIXED BLOCKS" ARE USED
			blocks = new ArrayList<>();
		}
		if(fixedBlocks == null) { //CREATE A EMPTY LIST IF ONLY "BLOCKS" ARE USED
			fixedBlocks = new HashMap<>();
		}
		if(blocks.isEmpty() && fixedBlocks.isEmpty()) { //Throw user error if no blocks are defined
			plugin.error("Invalid generation mode. Empty lists", true);
			valid = false;
			return;
		}
		int totalBlocks = blocks.size() + fixedBlocks.size(); //Get the amount of blocks specified
		if(totalBlocks < 2) { // There needs to be at least two blocks. If not then throw error
			plugin.error("Invalid generation mode. There needs to be at least two blocks specified", true);
			valid = false;
			return;
		}
		if(!this.containsLiquidBlock(blocks, fixedBlocks)) { // There needs to flow from a liquid block
			plugin.error("Invalid generation mode. There needs to be at least one liquid block - i.e. LAVA or WATER", true);
			valid = false;
			return;
		}
		
		this.setDisabledWorlds(disabledWorlds);
		
		//CONGRATZ IT LOOKS VALID
		this.setId(id);
		this.setBlocks(blocks);
		this.setFixedBlocks(fixedBlocks);
		this.setSearchForPlayersNearby(searchForPlayersNearby);
		this.setFallbackMaterial(fallbackMaterial);
		if(name != null && !name.trim().equals("")) {
			this.setName(name);
		}else {
			StringJoiner sj = new StringJoiner(", ", "[", "]");
			if(!blocks.isEmpty()) {
				for(Material m : blocks) {
					if(m != null) sj.add(StringUtils.toCamelCase(m.name()));
					else sj.add("Unknown block");
				}
			}
			if(!fixedBlocks.isEmpty()) {
				for(Material m : fixedBlocks.values()) {
					if(m != null) sj.add(StringUtils.toCamelCase(m.name()));
					else sj.add("Unknown block");
				}
			}
			String sjName = sj.toString();
			this.setName(sjName);
			plugin.log("&aNo displayname found for generator with id: " + id + " - Created name: " + sjName);
		}
		valid = true;
	}
	
	public List<Material> getMirrorMaterials(Material m, BlockFace blockFace) {
		if(m == null) return null;
		if(m.name().equals("LAVA") || m.name().equals("STATIONARY_LAVA")) m = Material.LAVA;
		if(m.name().equals("WATER") || m.name().equals("STATIONARY_WATER")) m = Material.WATER;

		List<Material> possibleMirrors = new ArrayList<>();
		if(fixedBlocks.containsKey(blockFace.getOppositeFace())){
			possibleMirrors.add(fixedBlocks.get(blockFace.getOppositeFace()));
		}else {
			possibleMirrors.addAll(this.getBlocks());
			if(possibleMirrors.contains(m)) {
				possibleMirrors.remove(m);
			}else {
				possibleMirrors = new ArrayList<>();
			}
		}
		if(possibleMirrors.isEmpty()) possibleMirrors = null;
		
		return possibleMirrors;
	}
	
	
	public boolean containsLiquidBlock() {
		return this.containsLiquidBlock(this.getBlocks(), this.getFixedBlocks());
	}
	
	public boolean containsLiquidBlock(List<Material> blocks, Map<BlockFace, Material> fixedBlocks) {
		for(Material m : blocks) {
			if(m == null) continue;
			if(m.name().equals("LAVA") || m.name().equals("STATIONARY_LAVA") || m.name().equals("WATER") || m.name().equals("STATIONARY_WATER")) return true;
		}
		for(Entry<BlockFace, Material> set : fixedBlocks.entrySet()) {
			if(set.getKey() == BlockFace.DOWN) continue; // If the liquid is DOWN then there can not be any flow, which means the event will never be called by this block.
			Material m = set.getValue();
			if(m == null) continue;
			if(m.name().equals("LAVA") || m.name().equals("STATIONARY_LAVA") || m.name().equals("WATER") || m.name().equals("STATIONARY_WATER")) return true;
		}
		return false;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public boolean containsBlock(Material m) {
		if(m == null) return false;
		if(m.name().equalsIgnoreCase("WATER") || m.name().equalsIgnoreCase("STATIONARY_WATER")) m = Material.WATER;
		if(m.name().equalsIgnoreCase("LAVA") || m.name().equalsIgnoreCase("STATIONARY_LAVA")) m = Material.LAVA;
		for(Material mat : blocks) {
			if(mat == null) continue;
			if(mat.equals(m)) return true;
		}
		for(Entry<BlockFace, Material> set : fixedBlocks.entrySet()) {
			if(set.getKey() == BlockFace.DOWN) continue; // If the liquid is DOWN then there can not be any flow, which means the event will never be called by this block.
			Material mat = set.getValue();
			if(mat == null) continue;

			if(mat.equals(m)) return true;
		}
		return false;
	}
	


	public boolean isSearchingForPlayersNearby() {
		return searchForPlayersNearby;
	}

	public void setSearchForPlayersNearby(boolean searchForPlayersNearby) {
		this.searchForPlayersNearby = searchForPlayersNearby;
	}

	public Map<BlockFace, Material> getFixedBlocks() {
		return fixedBlocks;
	}

	public void setFixedBlocks(Map<BlockFace, Material> fixedBlocks) {
		this.fixedBlocks = fixedBlocks;
	}

	public List<Material> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<Material> blocks) {
		this.blocks = blocks;
	}

	public List<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	public void setDisabledWorlds(List<String> disabledWorlds) {
		this.disabledWorlds = disabledWorlds;
	}
	
	public boolean isWorldDisabled(World world) {
		if(this.getDisabledWorlds() == null || world == null) return false;
		return this.getDisabledWorlds().contains(world.getName());
		
	}
	
	public boolean isWorldDisabled(String worldName) {
		World world = Bukkit.getWorld(worldName);
		return this.isWorldDisabled(world);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Material getFallbackMaterial() {
		return fallbackMaterial;
	}

	public boolean hasFallBackMaterial() {
		return fallbackMaterial != null;
	}
	
	public void setFallbackMaterial(Material fallbackMaterial) {
		this.fallbackMaterial = fallbackMaterial;
	}

	public Sound getGenSound() {
		return genSound;
	}
	public boolean hasGenSound() {
		return this.getGenSound() != null;
	}

	public void setGenSound(Sound genSound) {
		if(genSound == null) {
			plugin.error("Unkown sound for generator with id: " + this.getId(), true);
			return;
		}
		this.genSound = genSound;
	}
	
	public boolean hasParticleEffect() {
		return this.getParticleEffect() != null;
	}

	public ParticleEffect getParticleEffect() {
		return particleEffect;
	}

	public void setParticleEffect(ParticleEffect particleEffect) {
		this.particleEffect = particleEffect;
	}
	
	public void displayGenerationParticles(Location loc) {
//		loc = loc.add(0.5D, 1D, 0.5D);
		for(int i = 0; i < 10; i++) {
			Random rand = new Random();
			Location tempLoc = loc.clone().add(rand.nextDouble(), 1D, rand.nextDouble());
			float speed = 1/(i+1);
			this.particleEffect.display(tempLoc, 0F, 0F, 0F, speed, 1, null);
		}
	}
	
}
