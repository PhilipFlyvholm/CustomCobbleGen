package me.phil14052.CustomCobbleGen.GUI;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Icon {
	public final ItemStack itemStack;
	public final List<ClickAction> clickActions = new ArrayList<>();
	
	public Icon(ItemStack itemStack){
		this.itemStack = itemStack;
	}
	public Icon addClickAction(ClickAction clickAction){
		this.clickActions.add(clickAction);
		return this;
	}
	
	public List<ClickAction> getClickActions(){
		return this.clickActions;
	}
}
