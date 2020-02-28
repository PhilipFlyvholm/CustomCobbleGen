/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GenMode.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import org.bukkit.Material;

/**
 * @author Philip
 *
 */
public class GenMode {

	private Material firstBlock = null;
	private Material secondBlock = null;
	private boolean searchForPlayersNearby = false;
	
	private boolean valid = false;

	public GenMode(String firstBlock, String secondBlock) {
		this(Material.valueOf(firstBlock), Material.valueOf(secondBlock));
	}
	public GenMode(String firstBlock, String secondBlock, boolean searchForNearbyPlayers) {
		this(Material.valueOf(firstBlock), Material.valueOf(secondBlock), searchForNearbyPlayers);
	}
	public GenMode(Material firstBlock, Material secondBlock) {
		this(firstBlock, secondBlock, !((firstBlock.equals(Material.WATER) && secondBlock.equals(Material.LAVA)) ||
				(firstBlock.equals(Material.LAVA) && secondBlock.equals(Material.WATER))));
	}
	
	public GenMode(Material firstBlock, Material secondBlock, boolean searchForNearbyPlayers) {
		if(firstBlock == null || secondBlock == null) return;
		this.firstBlock = firstBlock;
		this.secondBlock = secondBlock;
		this.setSearchForPlayersNearby(searchForNearbyPlayers);
		if(!this.containsLiquidBlock()) return;
		valid = true;
	}
	
	public Material getMirrorMaterial(Material m) {
		if(m == null) return null;
		if(m.name().equals("LAVA") || m.name().equals("STATIONARY_LAVA")) m = Material.LAVA;
		if(m.name().equals("WATER") || m.name().equals("STATIONARY_WATER")) m = Material.WATER;
		if(m.equals(this.firstBlock)) return this.secondBlock;
		else if(m.equals(this.secondBlock)) return this.firstBlock;
		return null;
	}
	
	
	public boolean containsLiquidBlock() {
		return this.firstBlock.equals(Material.WATER) || 
				this.firstBlock.equals(Material.LAVA) || 
				this.secondBlock.equals(Material.WATER) || 
				this.secondBlock.equals(Material.LAVA);
	}
	
	
	public Material getFirstBlock() {
		return firstBlock;
	}

	public void setFirstBlock(Material firstBlock) {
		this.firstBlock = firstBlock;
	}

	public Material getSecondBlock() {
		return secondBlock;
	}

	public void setSecondBlock(Material secondBlock) {
		this.secondBlock = secondBlock;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public boolean containsBlock(Material m) {
		if( this.firstBlock.equals(m) || this.secondBlock.equals(m)) return true;
		
		if(m.name().equalsIgnoreCase("WATER") || m.name().equalsIgnoreCase("STATIONARY_WATER")) {
			if(this.firstBlock.name().equalsIgnoreCase("WATER") || this.firstBlock.name().equalsIgnoreCase("STATIONARY_WATER")) return true;
			if(this.secondBlock.name().equalsIgnoreCase("WATER") || this.secondBlock.name().equalsIgnoreCase("STATIONARY_WATER")) return true;			
		}else if(m.name().equalsIgnoreCase("LAVA") || m.name().equalsIgnoreCase("STATIONARY_LAVA")) {
			if(this.firstBlock.name().equalsIgnoreCase("LAVA") || this.firstBlock.name().equalsIgnoreCase("STATIONARY_LAVA")) return true;
			if(this.secondBlock.name().equalsIgnoreCase("LAVA") || this.secondBlock.name().equalsIgnoreCase("STATIONARY_LAVA")) return true;
		}
		return false;
	}
	


	public boolean isSearchingForPlayersNearby() {
		return searchForPlayersNearby;
	}

	public void setSearchForPlayersNearby(boolean searchForPlayersNearby) {
		this.searchForPlayersNearby = searchForPlayersNearby;
	}
	
}
