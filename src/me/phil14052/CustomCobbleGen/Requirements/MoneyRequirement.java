package me.phil14052.CustomCobbleGen.Requirements;

import java.util.List;

import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;

public class MoneyRequirement implements Requirement{
	
	private int moneyNeeded;
	private EconomyManager econManager;
	
	public MoneyRequirement(int moneyNeeded) {
		econManager = EconomyManager.getInstance();
		if(moneyNeeded < 0) moneyNeeded = 0;
		this.moneyNeeded = moneyNeeded;
	}
	
	@Override
	public boolean furfillsRequirement(Player p) {
		if(econManager.isConnectedToVault() && !econManager.canAfford(p, this.getRequirementValue())) return false;
		return true;
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.MONEY;
	}

	@Override
	public int getRequirementValue() {
		return this.moneyNeeded;
	}

	@Override
	public List<String> addAvailableString(Tier tier, List<String> lore) {
		lore.add(Lang.GUI_PRICE_MONEY_AFFORD.toString(tier));
		return lore;
	}

	@Override
	public List<String> addUnavailableString(Tier tier, List<String> lore) {
		lore.add(Lang.GUI_PRICE_MONEY_EXPENSIVE.toString(tier));
		return lore;
	}

	@Override
	public void onPurchase(Player p) {
		if(econManager.isConnectedToVault()) {
			econManager.takeMoney(p, this.getRequirementValue());
		}
	}
	
	

}
