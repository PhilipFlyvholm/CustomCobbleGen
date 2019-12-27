package me.phil14052.CustomCobbleGen.Requirements;

import java.util.List;

import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;

public class XpRequirement implements Requirement{
	
	private int xpNeeded;
	
	public XpRequirement(int xpNeeded) {
		if(xpNeeded < 0) xpNeeded = 0;
		this.xpNeeded = xpNeeded;
	}
	
	@Override
	public boolean furfillsRequirement(Player p) {
		return p.getLevel() >= this.getRequirementValue();
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.XP;
	}

	@Override
	public int getRequirementValue() {
		return this.xpNeeded;
	}

	@Override
	public List<String> addAvailableString(Tier tier, List<String> lore) {
		lore.add(Lang.GUI_PRICE_XP_AFFORD.toString(tier));
		return lore;
	}

	@Override
	public List<String> addUnavailableString(Tier tier, List<String> lore) {
		lore.add(Lang.GUI_PRICE_XP_EXPENSIVE.toString(tier));
		return lore;
	}

	@Override
	public void onPurchase(Player p) {
		int xpPriceInLevels = this.getRequirementValue();
		p.setLevel(p.getLevel()-xpPriceInLevels);
	}
	
	

}
