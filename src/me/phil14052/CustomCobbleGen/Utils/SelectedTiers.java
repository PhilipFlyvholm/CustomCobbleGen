/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SelectedTiers.java
 */
package me.phil14052.CustomCobbleGen.Utils;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

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
	private HashMap<GenMode, Tier> selectedTiers;
	private Player player;
	
	public SelectedTiers(Player p, List<Tier> tiers) {
		this.player = p;
		for(Tier tier : tiers) {
			if(tier == null || this.selectedTiers.containsKey(tier)) continue;
			this.selectedTiers.put(tier.getSupportedMode(), tier);
		}
	}
	
	public HashMap<GenMode, Tier> getSelectedTiersMap() {
		return selectedTiers;
	}
	public void setSelectedTiersMap(HashMap<GenMode, Tier> selectedTiers) {
		this.selectedTiers = selectedTiers;
	}
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void addTier(Tier tier) {
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
			if(this.getSelectedTiersMap().get(tier.getSupportedMode()).equals(tier)) return true;
		}
		return false;
	}
}
