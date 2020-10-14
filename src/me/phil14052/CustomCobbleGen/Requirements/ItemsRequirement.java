package me.phil14052.CustomCobbleGen.Requirements;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

public class ItemsRequirement implements Requirement{
	
	private HashMap<Material, Integer> itemsNeeded;
	
	public ItemsRequirement(HashMap<Material, Integer> itemsNeeded) {
		this.setItemsNeeded(itemsNeeded);
	}
	
	@Override
	public boolean furfillsRequirement(Player p) {
		PlayerInventory inv = p.getInventory();
		for(Entry<Material, Integer> entry : this.itemsNeeded.entrySet()) {
			ItemStack is = new ItemStack(entry.getKey()); //Makes sure that a custom item can not be used instead.
			if(!inv.containsAtLeast(is, entry.getValue())) return false;
		}
		return true;
	}

	@Override
	public RequirementType getRequirementType() {
		return RequirementType.ITEMS;
	}
	
	public HashMap<Material, Integer> getItemsNeeded(){
		return this.itemsNeeded;
	}
	public void setItemsNeeded(HashMap<Material, Integer> itemsNeeded) {
		for(Entry<Material, Integer> entry : itemsNeeded.entrySet()) {
			if(entry.getKey() == null || entry.getValue() <= 0) itemsNeeded.remove(entry.getKey());
		}
		this.itemsNeeded = itemsNeeded;
	}

	@Override
	public int getRequirementValue() {
		return this.getItemsNeeded().size();
	}

	@Override
	public String toString() {
		
		StringJoiner sj = new StringJoiner(", ");
		for(Entry<Material, Integer> item : this.getItemsNeeded().entrySet()) {
			sj.add(item.getValue() + "x" + item.getKey());
		}
		return sj.toString();
	}
	
	@Override
	public List<String> addAvailableString(Tier tier, List<String> lore) {
		lore.add(Lang.GUI_PRICE_ITEMS_AFFORD_TOP.toString(tier));
		 for(Entry<Material, Integer> entry : tier.getPriceItems().entrySet()) {
			 lore.add(Lang.GUI_PRICE_ITEMS_AFFORD_LIST.toString(StringUtils.toCamelCase(entry.getKey().toString()), entry.getValue() + ""));
		 }
		return lore;
	}

	@Override
	public List<String> addUnavailableString(Tier tier, List<String> lore) {
		lore.add(Lang.GUI_PRICE_ITEMS_EXPENSIVE_TOP.toString(tier));
		 for(Entry<Material, Integer> entry : tier.getPriceItems().entrySet()) {
			 lore.add(Lang.GUI_PRICE_ITEMS_EXPENSIVE_LIST.toString(StringUtils.toCamelCase(entry.getKey().toString()), entry.getValue() + ""));
		 }
		return lore;
	}

	@Override
	public void onPurchase(Player p) {
		HashMap<Material, Integer> items = this.getItemsNeeded();
		PlayerInventory inv = p.getInventory();
		for(Entry<Material, Integer> m : items.entrySet()) {
			ItemStack is = new ItemStack(m.getKey(), m.getValue());
			inv.removeItem(is);
		}
		return;
	}

	

}
