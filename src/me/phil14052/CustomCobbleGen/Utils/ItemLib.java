package me.phil14052.CustomCobbleGen.Utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;


public class ItemLib {
/*
 * Made by Phil14052.
 * This is a free-to-use lib.
 * 
 * You may use,edit,share, etc.
 * But you may not take credits for the lib.
 * 
 * You may not delete this permission notice!
 * 
 * */
	ItemStack is;
	ItemMeta im;
	Material material;
	String displayName;
	short damagevalue;
	List<String> lore;
	int amount;
	
	public ItemLib(Material material, int amount, short damageValue, String displayName, List<String> lore){
		Validate.notNull(material);
		Validate.notNull(damageValue);
		Validate.notNull(displayName);
		Validate.notEmpty(displayName);
		Validate.notEmpty(lore);
		Validate.notNull(lore);
		Validate.notNull(amount);
		this.material = material;
		this.damagevalue = damageValue;
		this.displayName = displayName;
		this.lore = lore;
		this.amount = amount;
		
	}
	public ItemLib(Material material, int amount, short damageValue, String displayName){
		Validate.notNull(material);
		Validate.notNull(damageValue);
		Validate.notNull(displayName);
		Validate.notEmpty(displayName);
		Validate.notNull(amount);
		this.material = material;
		this.damagevalue = damageValue;
		this.displayName = displayName;
		this.amount = amount;
		this.lore = new ArrayList<String>();
		
	}
	public ItemLib(Material material, int amount, short damageValue){
		Validate.notNull(material);
		Validate.notNull(damageValue);
		Validate.notNull(amount);
		this.material = material;
		this.damagevalue = damageValue;
		this.amount = amount;
		this.lore = new ArrayList<String>();
		
	}
	public ItemLib(Material material, int amount){
		Validate.notNull(material);
		Validate.notNull(amount);
		this.material = material;
		this.damagevalue = 0;
		this.amount = amount;
		this.lore = new ArrayList<String>();
		
	}
	public ItemLib(Material material){
		Validate.notNull(material);
		this.material = material;
		this.damagevalue = 0;
		this.amount = 1;
		this.lore = new ArrayList<String>();
		
	}
	public ItemLib(){
		this.material = Material.STONE;
		this.damagevalue = 0;
		this.amount = 1;
		this.lore = new ArrayList<String>();
	};
	
	public Material getMaterial(){
		return material;
	}
	public String getDisplayName(){
		return displayName;
	}
	public short getDamageValue(){
		return damagevalue;
	}
	public int getAmount(){
		return amount;
	}
	public List<String> getLore(){
		return lore;
	}
	public ItemMeta getItemMeta(){
		return im;
	}
	
	public void setMaterial(Material material){
		this.material = material;
	}
	public void setItem(ItemStack itemStack){
		this.is = itemStack;
	}
	public ItemStack getItem(){
		return this.is;
	}
	
	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}
	
	public void setDamageValue(short damagevalue){
		this.damagevalue = damagevalue;
	}
	
	public void setAmount(int amount){
		this.amount = amount;
	}
	
	public void setLore(List<String> lore){
		this.lore = lore;
	}
	
	public void setItemMeta(ItemMeta itemmeta){
		this.im = itemmeta;
	}
	
	public void addLineToLore(String line){
		this.lore.add(line);
	}
	
	
	
	public ItemStack create(){
		if(is == null){
			is = new ItemStack(material, amount);
		}
		im = is.getItemMeta();
		if(displayName != null){
			im.setDisplayName(displayName);
		}
		if(lore != null){
			im.setLore(lore);
		}
		is.setItemMeta(im);
		is = this.setDamage(is);
		return is;
	}

	@SuppressWarnings("deprecation")
	public ItemStack setDamage(ItemStack is) {

		if(XMaterial.supports(13)) {

			Damageable damageMeta = (Damageable) im;
			damageMeta.setDamage(damagevalue);
			is.setItemMeta((ItemMeta) damageMeta);	
		}else {
			is.setDurability(damagevalue);
		}
		return is;
	}
}






	