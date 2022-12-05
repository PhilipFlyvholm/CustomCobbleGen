/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerData.java
 */
package me.phil14052.CustomCobbleGen.databases;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Philip
 *
 */
public class PlayerData {

	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	private UUID uuid;
	private SelectedTiers selectedTiers;
	private List<Tier> purchasedTiers;
	
	
	public PlayerData(UUID uuid, SelectedTiers selectedTiers, List<Tier> purchasedTiers) {
		if(uuid == null) {
			plugin.error("Failed to load player data: uuid is null");
			return;
		}
		if(selectedTiers == null) {
			plugin.error("Failed to load player data: selectedTiers is null for uuid " + uuid);
			return;
		}
		if(purchasedTiers == null) {
			plugin.error("Failed to load player data: purchasedTiers is null for uuid " + uuid);
			return;
		}
		this.uuid = uuid;
		this.selectedTiers = selectedTiers;
		this.purchasedTiers = purchasedTiers;
	}
	
	
	
	public PlayerData(UUID uuid) {
		this(uuid, new SelectedTiers(uuid, new ArrayList<>()), new ArrayList<>());
		selectedTiers.addTier(TierManager.getInstance().getTierByLevel("DEFAULT", 0));
	}

	public UUID getUUID() {
		return uuid;
	}

	public SelectedTiers getSelectedTiers() {
		return selectedTiers;
	}

	public void setSelectedTiers(SelectedTiers selectedTiers) {
		this.selectedTiers = selectedTiers;
	}
	
	public List<Tier> getPurchasedTiers() {
		return purchasedTiers;
	}

	public void setPurchasedTiers(List<Tier> purchasedTiers) {
		this.purchasedTiers = purchasedTiers;
	}

	public void addSelectedTiers(Tier tier) {
		if(this.isTierSelected(tier)) return;
		this.getSelectedTiers().addTier(tier);
	}
	
	public void removeSelectedTiers(Tier tier) {
		if(!this.isTierSelected(tier)) return;
		this.getSelectedTiers().removeTier(tier);
	}
	
	public boolean isTierSelected(Tier tier) {
		return this.getSelectedTiers().isTierSelected(tier);
	}
	
	public void addPurchasedTier(Tier tier) {
		if(this.isTierPurchased(tier)) return;
		this.getPurchasedTiers().add(tier);
	}
	
	public void removePurchasedTier(Tier tier) {
		if(!this.isTierPurchased(tier)) return;
		this.getPurchasedTiers().remove(tier);
	}
	
	public boolean isTierPurchased(Tier tier) {
		return this.getPurchasedTiers() != null && !this.getPurchasedTiers().isEmpty() && this.getPurchasedTiers().contains(tier);
	}
	
}
