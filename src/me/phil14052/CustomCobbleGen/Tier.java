/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Tier.java
 */
package me.phil14052.CustomCobbleGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Requirements.ItemsRequirement;
import me.phil14052.CustomCobbleGen.Requirements.LevelRequirement;
import me.phil14052.CustomCobbleGen.Requirements.MoneyRequirement;
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
import me.phil14052.CustomCobbleGen.Requirements.XpRequirement;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
public class Tier {

	private String name;
	private String tierClass = "";
	private ItemStack icon;
	private Map<Material, Double> results;
	private int level;
	private List<Requirement> requirements;
	
	public Tier(String name, String tierClass, int level, Material iconMaterial, Map<Material, Double> results, int priceMoney, int priceXp, HashMap<Material, Integer> priceItems, int levelRequirement){
		this.name = name;
		this.tierClass = tierClass;
		this.level = level;

		this.requirements = new ArrayList<Requirement>();
		if(priceMoney > 0) {
			requirements.add(new MoneyRequirement(priceMoney));
		}
		if(priceXp > 0) {
			requirements.add(new XpRequirement(priceXp));
		}
		if(priceItems != null && priceItems.size() > 0) {
			requirements.add(new ItemsRequirement(priceItems));
		}
		if(levelRequirement > 0) {
			requirements.add(new LevelRequirement(levelRequirement));
		}
		
		ItemStack icon = new ItemStack(iconMaterial);
		ItemMeta im = icon.getItemMeta();
		im.setDisplayName(Lang.GUI_ITEM_NAME.toString(this));
		List<String> lore = new ArrayList<String>();
		lore.add(Lang.GUI_ITEM_LORE_TITLE.toString(this));
		for(Material result : results.keySet()){
			String resultName = StringUtils.toCamelCase(result.name());
			String percentage = results.get(result) % 1 == 0 ? ((int) Math.round(results.get(result))) + "%" : ((double) results.get(result)) + "%";
			String resultString = Lang.GUI_ITEM_LORE_RESULT.toString(this);
			resultString = resultString.replaceAll("%result_name%", resultName);
			resultString = resultString.replaceAll("%result_percentage%", percentage);
			lore.add(resultString);
		}
		im.setLore(lore);
		icon.setItemMeta(im);
		this.icon = icon;
		this.results = results;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ItemStack getIcon() {
		return icon;
	}
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}
	public Map<Material, Double> getResults() {
		return results;
	}
	public void setResults(Map<Material, Double> results) {
		this.results = results;
	}

	public String getTierClass() {
		return tierClass;
	}

	public void setTierClass(String tierClass) {
		this.tierClass = tierClass;
	}
	
	public Material getRandomResult(){
		double r = Math.random()*100;
		double prev = 0;
		for(Material m : this.getResults().keySet()){
			double chance = this.getResults().get(m) + prev;
			if(r > prev && r <= chance) return m;
			else prev = chance;
			continue;
		}
		return null;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	public boolean hasRequirements() {
		return this.getRequirements() != null;
	}
	
	
	public int getLevelRequirement() {
		if(!this.hasRequirements()) return 0;
		for(Requirement r : this.requirements) {
			if(r.getRequirementType().equals(RequirementType.LEVEL)) {
				return ((LevelRequirement) r).getRequirementValue();
			}
		}
		return 0;
	}

	public boolean hasLevelRequirement() {
		return this.getLevelRequirement() > 0;
	}
	
	public int getPriceMoney() {
		
		if(!this.hasRequirements()) return 0;
		for(Requirement r : this.requirements) {
			if(r.getRequirementType().equals(RequirementType.MONEY)) {
				return ((MoneyRequirement) r).getMoneyNeeded();
			}
		}
		return 0;
	}

	
	public boolean hasMoneyPrice() {
		return this.getPriceMoney() > 0;
	}
	
	public void setPriceMoney(int priceMoney) {
		if(!this.hasRequirements()) return;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.MONEY) ((MoneyRequirement) r).setMoneyNeeded(priceMoney);
	}

	public int getPriceXp() {
		if(!this.hasRequirements()) return 0;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.XP) return ((XpRequirement) r).getXPNeeded();
		return 0;
	}

	public boolean hasXpPrice() {
		return this.getPriceXp() > 0;
	}

	public void setPriceXp(int priceXp) {
		if(!this.hasRequirements()) return;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.XP) ((XpRequirement) r).setXPNeeded(priceXp);
	}
	
	public HashMap<Material, Integer> getPriceItems() {
		if(!this.hasRequirements()) return null;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.ITEMS) return ((ItemsRequirement) r).getItemsNeeded();
		return null;
	}

	public boolean hasItemsPrice() {
		return this.getPriceItems() != null;
	}

	public void setPriceItems(HashMap<Material, Integer> priceItems) {
		if(!this.hasRequirements()) return;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.ITEMS) ((ItemsRequirement) r).setItemsNeeded(priceItems);
	}
	
	public List<Requirement> getRequirements(){
		return this.requirements;
	}
	
}




