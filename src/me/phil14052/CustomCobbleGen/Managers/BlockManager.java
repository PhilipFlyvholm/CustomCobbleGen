
package me.phil14052.CustomCobbleGen.Managers;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;

import java.util.*;
import java.util.Map.Entry;
/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BlockManager.java
 */
public class BlockManager {

	private static BlockManager instance = null;
	private final List<Location> knownGenLocations = new ArrayList<>();
	private final Map<Location, GenBlock> genBreaks = new HashMap<>();
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
		this.genBreaks.remove(l);
	}
	
	public void setPlayerForLocation(UUID uuid, Location l, boolean pistonPowered) {
		this.addKnownGenLocation(l);
		this.getGenBreaks().remove(l);

		// Create a new GenBlock object to track the player+timestamp and add it to the genBreaks map
		GenBlock gb = new GenBlock(l, uuid, pistonPowered);
		this.getGenBreaks().put(l, gb);
	}

	public Map<Location, GenBlock> getGenBreaks() {
		return genBreaks;
	}


	
	public void cleanupExpiredLocations() {
		// Remove all expired GenBlock entries
		Set<Entry<Location, GenBlock>> entrySet = genBreaks.entrySet();
		if(entrySet.isEmpty()) return;
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
		if(knownGenPistons == null) return;
		Set<Entry<Location, GenPiston>> entrySet = knownGenPistons.entrySet();
		if(entrySet.isEmpty()) return;
		List<GenPiston> expiredPistons = new ArrayList<>();
		for (Entry<Location, GenPiston> entry : entrySet) {
			GenPiston piston = entry.getValue();
			piston.getLoc().getBlock();
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
		this.getKnownGenPistons().remove(location);
		this.getKnownGenPistons().put(location, piston);
	}

	public void removeKnownGenPiston(GenPiston piston) {
		Location location = piston.getLoc();
		this.getKnownGenPistons().remove(location);
	}

	
	public GenPiston[] getGenPistonsByUUID(UUID uuid){
		return this.getKnownGenPistons() != null ? this.getKnownGenPistons()
			.values()
			.stream()
			.filter(piston -> piston.getUUID() != null && piston.getUUID().equals(uuid))
			.toArray(GenPiston[]::new) : new GenPiston[0];
	}
	
	
}
