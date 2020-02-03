/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ClickableSign.java
 */
package me.phil14052.CustomCobbleGen.Signs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Philip
 *
 */
public interface ClickableSign {
	
	public Location getLocation();
	public void setLocation(Location loc);
	
	public ClickableSignType getSignType();
	public void setSignType(ClickableSignType signType);
	
	public String serializeSign();
	
	public void onInteract(Player p);
	
	public boolean isValid();
	
	public boolean validateData();
	
	
}
