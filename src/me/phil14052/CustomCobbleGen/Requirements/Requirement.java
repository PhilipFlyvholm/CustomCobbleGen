package me.phil14052.CustomCobbleGen.Requirements;

import me.phil14052.CustomCobbleGen.API.Tier;
import org.bukkit.entity.Player;

import java.util.List;

public interface Requirement {
	
	public boolean furfillsRequirement(Player p);
	
	public RequirementType getRequirementType();
	
	public int getRequirementValue();
	
	public List<String> addAvailableString(Tier tier, List<String> lore);
	
	public List<String> addUnavailableString(Tier tier, List<String> lore);
	
	public void onPurchase(Player p);
}
