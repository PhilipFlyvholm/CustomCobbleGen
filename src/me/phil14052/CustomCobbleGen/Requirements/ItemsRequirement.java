package me.phil14052.CustomCobbleGen.Requirements;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
		// TODO Auto-generated method stub
		return 0;
	}

	

}
