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
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
public class Tier {

	private String name = "";
	private String tierClass = "";
	private ItemStack icon = null;
	private Map<Material, Double> results = null;
	private int level = -1;
	private List<Requirement> requirements = null;
	private List<String> description = null;
	
	public Tier() {
		this.name = "";
		this.tierClass = "";
		this.icon = null;
		this.requirements = null;
		this.results = null;
		this.description = null;
		this.level = -1;
	}
	
	public Tier(String name, String tierClass, int level, Material iconMaterial, Map<Material, Double> results, List<Requirement> requirements, List<String> description){
		this.name = name;
		this.tierClass = tierClass;
		this.level = level;

		this.requirements = requirements;
		ItemStack icon = new ItemStack(iconMaterial);
		ItemMeta im = icon.getItemMeta();
		im.setDisplayName(Lang.GUI_ITEM_NAME.toString(this));
		List<String> lore = new ArrayList<String>();
		lore.add(Lang.GUI_ITEM_LORE_TITLE.toString(this));
		if(description == null) {
			lore.addAll(this.getResultsLore(results));
		}else {
			lore.addAll(description);
		}
		im.setLore(lore);
		icon.setItemMeta(im);
		this.icon = icon;
		this.results = results;
		this.description = description;
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
	
	public int getRequirementValue(RequirementType type) {
		if(!this.hasRequirements()) return 0;
		for(Requirement r : this.requirements) {
			if(r.getRequirementType().equals(type)) {
				return r.getRequirementValue();
			}
		}
		return 0;
	}
	
	public boolean hasRequirement(RequirementType type) {
		boolean hasRequirement = this.getRequirementValue(type) > 0;
		return hasRequirement;
	}
	
	public HashMap<Material, Integer> getPriceItems() {
		if(!this.hasRequirements()) return null;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.ITEMS) return ((ItemsRequirement) r).getItemsNeeded();
		return null;
	}

	public void setPriceItems(HashMap<Material, Integer> priceItems) {
		if(!this.hasRequirements()) return;
		for(Requirement r : this.requirements) if(r.getRequirementType() == RequirementType.ITEMS) ((ItemsRequirement) r).setItemsNeeded(priceItems);
	}
	
	public List<Requirement> getRequirements(){
		return this.requirements;
	}
	
	public List<String> getResultsLore(Map<Material, Double> results){
		List<String> lore = new ArrayList<>();
		for(Material result : results.keySet()){
			String resultName = StringUtils.toCamelCase(result.name());
			String percentage = results.get(result) % 1 == 0 ? ((int) Math.round(results.get(result))) + "%" : ((double) results.get(result)) + "%";
			String resultString = Lang.GUI_ITEM_LORE_RESULT.toString(this);
			resultString = resultString.replaceAll("%result_name%", resultName);
			resultString = resultString.replaceAll("%result_percentage%", percentage);
			lore.add(resultString);
		}
		return lore;
	}
	
	
	public boolean hasDescription() {
		if(this.description == null || this.description.isEmpty()) return false;
		return true;
	}
	
	public List<String> getDescription(){
		return this.description;
	}
	
	public void setDescription(List<String> description) {
		this.description = description;
	}
}




