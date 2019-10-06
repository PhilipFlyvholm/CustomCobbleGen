package me.phil14052.CustomCobbleGen.Requirements;

import org.bukkit.entity.Player;

public interface Requirement {
	
	public boolean furfillsRequirement(Player p);
	
	public RequirementType getRequirementType();
	
	public int getRequirementValue();
	
}
