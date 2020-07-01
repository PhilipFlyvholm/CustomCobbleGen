/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GenMode.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

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
	private boolean valid = false;

	public GenMode(List<Material> blocks) {
		this(blocks, null, true, null);
	}
	
	public GenMode(List<Material> blocks, Map<BlockFace, Material> fixedBlocks) {
		this(blocks, fixedBlocks, true, null);
	}
	
	public GenMode(List<Material> blocks, Map<BlockFace, Material> fixedBlocks, boolean searchForPlayersNearby, List<String> disabledWorlds) {
		if(blocks == null) { //CREATE A EMPTY LIST IF ONLY "FIXED BLOCKS" ARE USED
			blocks = new ArrayList<>();
		}
		if(fixedBlocks == null) { //CREATE A EMPTY LIST IF ONLY "BLOCKS" ARE USED
			fixedBlocks = new HashMap<>();
		}
		if(blocks.isEmpty() && fixedBlocks.isEmpty()) { //Throw user error if no blocks are defined
			plugin.log("&c&lUser error: Invalid generation mode. Empty lists");
			valid = false;
			return;
		}
		int totalBlocks = blocks.size() + fixedBlocks.size(); //Get the amount of blocks specified
		if(totalBlocks < 2) { // There needs to be at least two blocks. If not then throw error
			plugin.log("&c&lUser error: Invalid generation mode. There needs to be at least two blocks specified");
			valid = false;
			return;
		}
		if(!this.containsLiquidBlock(blocks, fixedBlocks)) { // There needs to flow from a liquid block
			plugin.log("&c&lUser error: Invalid generation mode. There needs to be at least one liquid block - i.e. LAVA or WATER");
			valid = false;
			return;
		}
		
		this.setDisabledWorlds(disabledWorlds);
		
		//CONGRATZ IT LOOKS VALID
		this.setBlocks(blocks);
		this.setFixedBlocks(fixedBlocks);
		this.setSearchForPlayersNearby(searchForPlayersNearby);
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
	
}
