/**
 * CustomCobbleGen By @author Philip Flyvholm
 * StringUtils.java
 */
package me.phil14052.CustomCobbleGen.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author Philip
 *
 */
public class StringUtils {

	public static String toCamelCase(String s){
		   String[] parts = s.split("_");
		   String camelCaseString = "";
		   for (String part : parts){
		      camelCaseString = camelCaseString + toProperCase(part);
		   }
		   return camelCaseString;
		}

	public static String toProperCase(String s) {
	    return s.substring(0, 1).toUpperCase() +
	           s.substring(1).toLowerCase();
	}
	
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	public static String serializeLoc(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}
	
	public static Location deserializeLoc(String seralizedLoc) {
		if(seralizedLoc == null || seralizedLoc.trim() == "") return null;
		String[] locParts = seralizedLoc.split(":");
		if(locParts.length != 4) return null;
		World world = Bukkit.getWorld(locParts[0]);
		Double x = Double.parseDouble(locParts[1]);
		Double y = Double.parseDouble(locParts[2]);
		Double z = Double.parseDouble(locParts[3]);
		return new Location(world, x,y,z);
	}
	
}
