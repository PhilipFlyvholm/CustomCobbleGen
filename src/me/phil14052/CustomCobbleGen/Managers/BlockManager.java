/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BlockManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

import com.cryptomorin.xseries.XMaterial;

public class BlockManager {

	private static BlockManager instance = null;
	private List<Location> knownGenLocations = new ArrayList<>();
	private Map<Location, GenBlock> genBreaks = new HashMap<>();
	private Map<Location, GenPiston> knownGenPistons = new HashMap<>();
	
	//private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	
	public static BlockManager getInstance() {
		if(instance == null) instance = new BlockManager();
		return instance;
	}

	public List<Location> getKnownGenLocations() {
		return knownGenLocations;
	}

	public boolean isGenLocationKnown(Location l) {
		return this.getKnownGenLocations().contains(l);
	}
	
	public void addKnownGenLocation(Location l) {
		if(this.isGenLocationKnown(l)) return;
		this.getKnownGenLocations().add(l);
	}
	
	public void removeKnownGenLocation(Location l) {
		if(this.isGenLocationKnown(l)) this.getKnownGenLocations().remove(l);
		if(this.genBreaks.containsKey(l)) this.genBreaks.remove(l);
	}
	
	public void setKnownGenLocations(List<Location> knownGenLocations) {
		this.knownGenLocations = knownGenLocations;
	}

	
	
	public void setPlayerForLocation(UUID uuid, Location l, boolean pistonPowered) {
		this.addKnownGenLocation(l);
		if(this.getGenBreaks().containsKey(l)) this.getGenBreaks().remove(l);

		// Create a new GenBlock object to track the player+timestamp and add it to the genBreaks map
		GenBlock gb = new GenBlock(l, uuid, pistonPowered);
		this.getGenBreaks().put(l, gb);
	}
	
	public Map<Location, GenBlock> getGenBreaks() {
		return genBreaks;
	}

	public void setGenBreaks(Map<Location, GenBlock> genBreaks) {
		this.genBreaks = genBreaks;
	}

	
	public void cleanupExpiredLocations() {
		// Remove all expired GenBlock entries
		if(genBreaks == null || genBreaks.entrySet() == null) return;
		Set<Entry<Location, GenBlock>> entrySet = genBreaks.entrySet();
		if(entrySet == null || entrySet.isEmpty()) return;
		List<GenBlock> expiredBlocks = new ArrayList<>();
		for (Entry<Location, GenBlock> entry : entrySet) {
			GenBlock gb = entry.getValue();
			if (gb.hasExpired()) {
				expiredBlocks.add(gb);
			}
		}
		for(GenBlock genBlock : expiredBlocks) {
			removeKnownGenLocation(genBlock.getLocation());
		}
	}
	
	public void cleanupExpiredPistons(UUID uuid) {
		// Remove all expired GenBlock entries
		if(knownGenPistons == null || knownGenPistons.entrySet() == null) return;
		Set<Entry<Location, GenPiston>> entrySet = knownGenPistons.entrySet();
		if(entrySet == null || entrySet.isEmpty()) return;
		List<GenPiston> expiredPistons = new ArrayList<>();
		for (Entry<Location, GenPiston> entry : entrySet) {
			GenPiston piston = entry.getValue();
			if(piston.getLoc().getBlock() == null) { // Probs not loaded so ignore it. We can't confirm an unloaded block
				continue;
			}
			if(piston.getLoc().getBlock().getType() == XMaterial.PISTON.parseMaterial()) {
				expiredPistons.add(piston);
				continue;
			}
			if(piston.getUUID().equals(uuid) && !piston.hasBeenUsed()) {
				expiredPistons.add(piston);
			}			
		}
		for(GenPiston piston : expiredPistons) {
			this.removeKnownGenPiston(piston);
		}
	}

	public Map<Location, GenPiston> getKnownGenPistons() {
		return knownGenPistons;
	}

	public void setKnownGenPistons(Map<Location, GenPiston> knownGenPistons) {
		this.knownGenPistons = knownGenPistons;
	}

	public void addKnownGenPiston(GenPiston piston) {
		Location location = piston.getLoc();
		if(this.getKnownGenPistons().containsKey(location)) this.getKnownGenPistons().remove(location);
		this.getKnownGenPistons().put(location, piston);
	}

	public void removeKnownGenPiston(GenPiston piston) {
		Location location = piston.getLoc();
		if(this.getKnownGenPistons().containsKey(location)) this.getKnownGenPistons().remove(location);
	}

	
	public GenPiston[] getGenPistonsByUUID(UUID uuid){
		return this.getKnownGenPistons() != null && this.getKnownGenPistons().values() != null ? this.getKnownGenPistons()
			.values()
			.stream()
			.filter(piston -> piston.getUUID() != null && piston.getUUID().equals(uuid))
			.toArray(GenPiston[]::new) : new GenPiston[0];
	}
	
	
}
