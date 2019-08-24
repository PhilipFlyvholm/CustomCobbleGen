/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Tier.java
 */
package me.phil14052.CustomCobbleGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
public class Tier {

	private String name;
	private String tierClass = "";
	private ItemStack icon;
	private Map<Material, Integer> results;
	private int level;
	private int priceMoney = 0;
	private int priceXp = 0;
	
	public Tier(String name, String tierClass, int level, Material iconMaterial, Map<Material, Integer> results, int priceMoney, int priceXp){
		this.name = name;
		this.tierClass = tierClass;
		this.level = level;
		ItemStack icon = new ItemStack(iconMaterial);
		ItemMeta im = icon.getItemMeta();
		im.setDisplayName(Lang.GUI_ITEM_NAME.toString(this));
		List<String> lore = new ArrayList<String>();
		lore.add(Lang.GUI_ITEM_LORE_TITLE.toString(this));
		for(Material result : results.keySet()){
			String resultName = StringUtils.toCamelCase(result.name());
			String percentage = results.get(result).toString() + "%";
			String resultString = Lang.GUI_ITEM_LORE_RESULT.toString(this);
			resultString = resultString.replaceAll("%result_name%", resultName);
			resultString = resultString.replaceAll("%result_percentage%", percentage);
			lore.add(resultString);
		}
		im.setLore(lore);
		icon.setItemMeta(im);
		this.icon = icon;
		this.results = results;
		this.setPriceMoney(priceMoney);
		this.setPriceXp(priceXp);
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
	public Map<Material, Integer> getResults() {
		return results;
	}
	public void setResults(Map<Material, Integer> results) {
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
		int prev = 0;
		for(Material m : this.getResults().keySet()){
			int chance = this.getResults().get(m) + prev;
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

	public int getPriceMoney() {
		return priceMoney;
	}

	public boolean hasMoneyPrice() {
		return this.getPriceMoney() > 0;
	}
	
	public void setPriceMoney(int priceMoney) {
		this.priceMoney = priceMoney;
	}

	public int getPriceXp() {
		return priceXp;
	}

	public boolean hasXpPrice() {
		return this.getPriceXp() > 0;
	}

	public void setPriceXp(int priceXp) {
		this.priceXp = priceXp;
	}
	
	
}




