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

	private HashMap<GenMode, Tier> selectedTiers;
	private Player player;
	
	public SelectedTiers(Player p, List<Tier> tiers) {
		this.player = p;
		for(Tier tier : tiers) {
			if()
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
	
}
