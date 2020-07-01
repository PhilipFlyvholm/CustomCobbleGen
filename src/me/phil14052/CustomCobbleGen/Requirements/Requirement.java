package me.phil14052.CustomCobbleGen.Requirements;

import java.util.List;

import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.API.Tier;

public interface Requirement {
	
	public boolean furfillsRequirement(Player p);
	
	public RequirementType getRequirementType();
	
	public int getRequirementValue();
	
	public List<String> addAvailableString(Tier tier, List<String> lore);
	
	public List<String> addUnavailableString(Tier tier, List<String> lore);
	
	public void onPurchase(Player p);
}
