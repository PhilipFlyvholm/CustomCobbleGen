package me.phil14052.CustomCobbleGen.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * StringUtils.java
 * @author Philip
 */
public class StringUtils {

	public static String toCamelCase(String s){
		   String[] parts = s.split("_");
		   StringBuilder camelCaseString = new StringBuilder();
		   for (String part : parts){
		      camelCaseString.append(toProperCase(part));
		   }
		   return camelCaseString.toString();
		}

	public static String toProperCase(String s) {
	    return s.substring(0, 1).toUpperCase() +
	           s.substring(1).toLowerCase();
	}
	
	public static boolean isNotInteger(String s) {
	    return !isInteger(s, 10);
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
	
	public static String serializeLoc(@NotNull Location loc) {
		if(loc.getWorld() == null) return null;
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}
	
	public static Location deserializeLoc(String serializedLoc) {
		if(serializedLoc == null || serializedLoc.trim().equals("")) return null;
		String[] locParts = serializedLoc.split(":");
		if(locParts.length != 4) return null;
		World world = Bukkit.getWorld(locParts[0]);
		double x = Double.parseDouble(locParts[1]);
		double y = Double.parseDouble(locParts[2]);
		double z = Double.parseDouble(locParts[3]);
		return new Location(world, x,y,z);
	}
	
}
