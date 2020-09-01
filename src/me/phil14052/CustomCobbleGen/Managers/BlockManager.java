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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.cryptomorin.xseries.XMaterial;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

public class BlockManager {

	private static BlockManager instance = null;
	private List<Location> knownGenLocations = new ArrayList<>();
	private Map<Location, GenBlock> genBreaks = new HashMap<>();
	private Map<Location, GenPiston> knownGenPistons = new HashMap<>();
	
	private static CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	
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

	
	private Map<UUID, List<GenPiston>> convertGenPistonToUUIDMap(Map<Location, GenPiston> map) {
		Map<UUID, List<GenPiston>> uuidMap = new HashMap<>();
		for(GenPiston piston : this.getKnownGenPistons().values()) {
			UUID uuid = piston.getUUID();
			List<GenPiston> list;
			if(uuidMap.containsKey(uuid)) {
				list = uuidMap.get(uuid);
			}else {
				list = new ArrayList<>();
			}
			list.add(piston);
			uuidMap.put(uuid, list);
		}
		return uuidMap;
	}
	
	public void saveGenPistonData() {
		Map<UUID, List<GenPiston>> genPistons = this.convertGenPistonToUUIDMap(this.getKnownGenPistons());
		Set<Entry<UUID, List<GenPiston>>> entrySet = genPistons.entrySet();
		if(entrySet == null || entrySet.isEmpty()) return;
		for(Entry<UUID, List<GenPiston>> pistonSet : entrySet) {
			List<String> locations = new ArrayList<>();
			for(GenPiston piston : pistonSet.getValue()) {
				if(piston == null) {
					continue;
				}
				if(piston.getLoc().getBlock() == null) {
					plugin.error("Can't confirm block is piston in players.yml under UUID: " + pistonSet.getKey().toString() + ".pistons at " + piston.getLoc());
				}
				else if(!piston.getLoc().getBlock().getType().equals(XMaterial.PISTON.parseMaterial())) continue;
				if(!piston.hasBeenUsed()) continue;
				String serializedLoc = this.serializeLoc(piston.getLoc());
				if(!locations.contains(serializedLoc)) locations.add(serializedLoc);
			}
			if(!locations.isEmpty()) plugin.getPlayerConfig().set("players." + pistonSet.getKey().toString() + ".pistons", locations);
		}
		plugin.savePlayerConfig();
	}
	
	public void loadGenPistonData() {
		this.knownGenPistons = new HashMap<>();
		ConfigurationSection playerSection = plugin.getPlayerConfig().getConfigurationSection("players");
		for(String uuid : playerSection.getKeys(false)){
			if(plugin.getPlayerConfig().contains("players." + uuid + ".pistons")) {
				List<String> locations = plugin.getPlayerConfig().getStringList("players." + uuid + ".pistons");
				UUID uuidObject = UUID.fromString(uuid);
				for(String stringLoc : locations) {
					Location loc = this.deserializeLoc(stringLoc);
					if(loc == null) {
						plugin.error("Unknown location in players.yml under UUID: " + uuid + ".pistons" + stringLoc);
						continue;
					}
					Block block = loc.getWorld().getBlockAt(loc);
					if(block == null) {
						plugin.error("Can't confirm block is piston in players.yml under UUID: " + uuid + ".pistons at " + stringLoc);
						continue;
					}
					else if(loc.getWorld().getBlockAt(loc).getType()!= XMaterial.PISTON.parseMaterial()) continue;
					GenPiston piston = new GenPiston(loc, uuidObject);
					piston.setHasBeenUsed(true);
					this.addKnownGenPiston(piston);
				}
			}
		}
	}
	
	public String serializeLoc(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}
	
	public Location deserializeLoc(String seralizedLoc) {
		if(seralizedLoc == null || seralizedLoc.trim() == "") return null;
		String[] locParts = seralizedLoc.split(":");
		if(locParts.length != 4) return null;
		World world = Bukkit.getWorld(locParts[0]);
		Double x = Double.parseDouble(locParts[1]);
		Double y = Double.parseDouble(locParts[2]);
		Double z = Double.parseDouble(locParts[3]);
		return new Location(world, x,y,z);
	}
}
