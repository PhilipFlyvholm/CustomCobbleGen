/**
 * CustomCobbleGen By @author Philip Flyvholm
 * StringUtils.java
 */
package me.phil14052.CustomCobbleGen.Utils;

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
	
}
