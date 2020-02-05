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

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

public class BlockManager {

	private static BlockManager instance = null;
	private List<Location> knownGenLocations = new ArrayList<Location>();
	private Map<Location, GenBlock> genBreaks = new HashMap<Location, GenBlock>();
	
	
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

	public void setPlayerForLocation(Player p, Location l) {
		this.addKnownGenLocation(l);
		if(this.getGenBreaks().containsKey(l)) this.getGenBreaks().remove(l);

		// Create a new GenBlock object to track the player+timestamp and add it to the genBreaks map
		GenBlock gb = new GenBlock(l, p);
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
		if(entrySet == null || entrySet.size() <= 0) return;
		for (Map.Entry<Location, GenBlock> entry : entrySet) {
			GenBlock gb = entry.getValue();
			if (gb.hasExpired()) {
				removeKnownGenLocation(gb.getLocation());
			}
		}
	}
}
