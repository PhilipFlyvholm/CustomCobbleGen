package me.phil14052.CustomCobbleGen.Requirements;

import org.bukkit.entity.Player;

public class XpRequirement implements Requirement{
	
	private int xpNeeded;
	
	public XpRequirement(int xpNeeded) {
		if(xpNeeded < 0) xpNeeded = 0;
		this.setXPNeeded(xpNeeded);
	}
	
	@Override
	public boolean furfillsRequirement(Player p) {
		return p.getLevel() >= this.getXPNeeded();
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.XP;
	}

	public int getXPNeeded() {
		return xpNeeded;
	}

	public void setXPNeeded(int xpNeeded) {
		this.xpNeeded = xpNeeded;
	}

	@Override
	public int getRequirementValue() {
		return this.xpNeeded;
	}
	
	

}
