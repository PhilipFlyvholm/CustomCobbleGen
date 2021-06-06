package me.phil14052.CustomCobbleGen.Signs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ClickableSign.java
 */
public interface ClickableSign {
	
	Location getLocation();
	void setLocation(Location loc);
	
	ClickableSignType getSignType();

	String serializeSign();
	
	void onInteract(Player p);
	
	boolean isValid();
	
	boolean validateData();
	
	
}
