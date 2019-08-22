/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Tier.java
 */
package me.phil14052.CustomCobbleGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private int price = 0;
	
	public Tier(String name, String tierClass, int level, Material iconMaterial, Map<Material, Integer> results, int price){
		this.name = name;
		this.tierClass = tierClass;
		this.level = level;
		ItemStack icon = new ItemStack(iconMaterial);
		ItemMeta im = icon.getItemMeta();
		im.setDisplayName("§6§l" + name);
		List<String> lore = new ArrayList<String>();
		lore.add("§8§lThis tier will give the following results");
		for(Material result : results.keySet()){
			String resultName = StringUtils.toCamelCase(result.name());
			String percentage = results.get(result).toString() + "%";
			lore.add("§8" + resultName + ": §o" + percentage);
		}
		im.setLore(lore);
		icon.setItemMeta(im);
		this.icon = icon;
		this.results = results;
		this.price = price;
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

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
	
	
}




