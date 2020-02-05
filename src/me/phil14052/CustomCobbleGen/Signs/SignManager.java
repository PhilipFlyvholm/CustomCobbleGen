/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SignManager.java
 */
package me.phil14052.CustomCobbleGen.Signs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class SignManager {

	
	private static SignManager instance = null;
	private CustomCobbleGen plugin = null;
	private TierManager tm = null;
	List<ClickableSign> signs;
	
	public SignManager() {
		plugin = CustomCobbleGen.getInstance();
		tm = TierManager.getInstance();
		signs = new ArrayList<>();
	}
	
	public boolean areSignsEnabled() {
		return plugin.getConfig().getBoolean("options.signs.enabled");
	}
	
	public List<ClickableSign> getSigns(){
		return this.signs;
	}
	
	public void saveSignsToFile() {
		List<String> serializedSigns = new ArrayList<>();
		
		for(ClickableSign sign : this.getSigns()) {
			serializedSigns.add(sign.serializeSign());
		}
		plugin.getSignsConfig().set("signs", serializedSigns);
		plugin.saveSignsConfig();
		
	}
	
	public void addSign(ClickableSign sign) {
		ClickableSign signAtLocation = this.getSignFromLocation(sign.getLocation());
		this.removeSign(signAtLocation, false);
		this.getSigns().add(sign);
		saveSignsToFile();
	}
	
	public boolean removeSign(ClickableSign sign) {
		return this.removeSign(sign, true);
	}
	
	public boolean removeSign(ClickableSign sign, boolean saveSigns) {

		if(sign != null) {
			this.getSigns().remove(sign);
			if(saveSigns) saveSignsToFile();
			return true;
		}
		return false;
	}
	
	
	
	public boolean loadSignsFromFile(boolean forceReload) {
		if(!this.getSigns().isEmpty() && !forceReload) return false;
		signs = new ArrayList<>();
		List<String> serilizedSigns = plugin.getSignsConfig().getStringList("signs");
		for(String s : serilizedSigns) {
			// [World, x, y, z, type]
			s = s.replace("[", "");
			s = s.replace("]", "");
			String[] items = s.split(",");
			World world = Bukkit.getServer().getWorld(items[0]);
			Double x = Double.parseDouble(items[1]);
			Double y = Double.parseDouble(items[2]);
			Double z = Double.parseDouble(items[3]);
			
			Location loc = new Location(world, x,y, z);
			ClickableSignType type = null;
			try {

				type = ClickableSignType.valueOf(items[4].toUpperCase());	
			}catch(IllegalArgumentException e) {
				plugin.log("§cThere is a error in a sign saved. Deleting it..."); 
				continue;
			}
			
			ClickableSign sign = null;
			if(type == ClickableSignType.GUI) sign = new GUISign(loc);
			if(type == ClickableSignType.SELECT || type == ClickableSignType.BUY) {
				
				String tierClass = items[5];
				int tierLevel = Integer.parseInt(items[6]);
				plugin.debug(tierClass, tierLevel);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(type == ClickableSignType.SELECT)  sign = new SelectSign(loc, tier);
				if(type == ClickableSignType.BUY) sign = new BuySign(loc, tier);
				if(sign == null || !sign.validateData()) continue;
			}
			this.getSigns().add(sign);
		}
		return true;
	}
	
	public ClickableSign getSignFromLocation(Location loc) {
		for(ClickableSign sign : this.getSigns()) {
			Location signLoc = sign.getLocation();
			if(loc.getX() == signLoc.getX() && loc.getY() == signLoc.getY() && loc.getZ() == signLoc.getZ() && loc.getWorld().getName().equals(signLoc.getWorld().getName())) {
				return sign;
			}
		}
		return null;
	}
	
	public static SignManager getInstance() {
		if(instance == null) instance = new SignManager();
		return instance;
	}
	
}
