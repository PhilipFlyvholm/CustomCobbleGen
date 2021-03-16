package me.phil14052.CustomCobbleGen.Requirements;

import me.phil14052.CustomCobbleGen.API.Tier;
import org.bukkit.entity.Player;

import java.util.List;

public interface Requirement {
	
	boolean furfillsRequirement(Player p);
	
	RequirementType getRequirementType();
	
	int getRequirementValue();
	
	List<String> addAvailableString(Tier tier, List<String> lore);
	
	List<String> addUnavailableString(Tier tier, List<String> lore);
	
	void onPurchase(Player p);
}
