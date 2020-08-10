/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SelectedTiers.java
 */
package me.phil14052.CustomCobbleGen.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Managers.GenMode;

/**
 * @author Philip
 *
 */
public class SelectedTiers {
	/**
	 * THIS CLASS HELPS WITH CONTROLLING THAT THERE ONLY WILL BE ONE TIER PER GENMODE
	 */
	private Map<GenMode, Tier> selectedTiers;
	private UUID uuid;
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public SelectedTiers(UUID uuid, List<Tier> tiers) {
		this.uuid = uuid;
		this.selectedTiers = new HashMap<>();
		for(Tier tier : tiers) {
			if(tier == null || this.selectedTiers.containsKey(tier.getSupportedMode())) continue;
			this.selectedTiers.put(tier.getSupportedMode(), tier);
		}
	}
	
	public SelectedTiers(UUID uuid, Tier tier) {
		this.uuid = uuid;
		this.selectedTiers = new HashMap<>();
		if(tier == null || this.selectedTiers.containsKey(tier.getSupportedMode())) return;
		this.selectedTiers.put(tier.getSupportedMode(), tier);
	}

	public Map<GenMode, Tier> getSelectedTiersMap() {
		return selectedTiers;
	}
	public void setSelectedTiersMap(Map<GenMode, Tier> selectedTiers) {
		this.selectedTiers = selectedTiers;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
	
	public void addTier(Tier tier) {
		plugin.debug(tier, tier.getSupportedMode(), this.selectedTiers);
		
		if(this.selectedTiers.containsKey(tier.getSupportedMode())) this.removeTier(tier.getSupportedMode());
		this.selectedTiers.put(tier.getSupportedMode(),tier);
	}
	
	public void removeTier(Tier tier) {
		this.removeTier(tier.getSupportedMode());
	}
	
	public void removeTier(GenMode mode) {
		if(this.selectedTiers.containsKey(mode)) this.selectedTiers.remove(mode);
	}
	
	public boolean isTierSelected(Tier tier) {
		if(this.getSelectedTiersMap().containsKey(tier.getSupportedMode())) {
			if(this.getSelectedTiersMap().get(tier.getSupportedMode()).getTierClass().equalsIgnoreCase(tier.getTierClass()) 
					&& this.getSelectedTiersMap().get(tier.getSupportedMode()).getLevel() == (tier.getLevel())) 
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		Collection<Tier> tiers = this.getSelectedTiersMap().values();
		if(tiers.isEmpty()) return "No tiers selected";
		if(tiers.size() == 1) {
			return tiers.iterator().next().getName(); //Get the name of the first tier
		}
		StringJoiner sj = new StringJoiner(",", "[", "]");
		for(Tier tier : tiers) {
			sj.add(tier.getName());
		}
		return sj.toString();
	}
}
