package me.phil14052.CustomCobbleGen.GUI;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

public class CustomHolder implements InventoryHolder{

	private final Map<Integer, Icon> icons = new HashMap<>();
	
	private final int size;
	private final String title;
	
	public CustomHolder(int size, String title){
		this.size = size;
		this.title = title;
	}
	
	public void setIcon(int position, Icon icon) {
        this.icons.put(position, icon);
    }
 
    public Icon getIcon(int position) {
        return this.icons.get(position);
    }
	
	@Override
	public Inventory getInventory() {
		Inventory inventory = Bukkit.createInventory(this, this.size, this.title);

        
        for (Entry<Integer, Icon> entry : this.icons.entrySet()) {
        	CustomCobbleGen.getInstance().debug(entry.getKey(),entry.getValue().itemStack.getType().name());
        	if(entry.getKey() > this.size) continue;
            inventory.setItem(entry.getKey(), entry.getValue().itemStack);
        }
   
        return inventory;
 
	}

}
