package me.phil14052.CustomCobbleGen.Requirements;

import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.Managers.EconomyManager;

public class MoneyRequirement implements Requirement{
	
	private int moneyNeeded;
	private EconomyManager econManager;
	
	public MoneyRequirement(int moneyNeeded) {
		econManager = EconomyManager.getInstance();
		if(moneyNeeded < 0) moneyNeeded = 0;
		this.setMoneyNeeded(moneyNeeded);
	}
	
	@Override
	public boolean furfillsRequirement(Player p) {
		if(econManager.isConnectedToVault() && !econManager.canAfford(p, this.getMoneyNeeded())) return false;
		return true;
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.MONEY;
	}

	public int getMoneyNeeded() {
		return moneyNeeded;
	}

	public void setMoneyNeeded(int moneyNeeded) {
		this.moneyNeeded = moneyNeeded;
	}

	@Override
	public int getRequirementValue() {
		return this.moneyNeeded;
	}
	
	

}
