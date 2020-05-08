/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ChatReturnType.java
 */
package me.phil14052.CustomCobbleGen.Chat;

/**
 * @author Philip
 *
 */
public enum ChatReturnType { 
	CLASS(false), LEVEL(false), NAME(true), DESCRIPTION(true), ICON(false), MATERIAL(false);
	
	private boolean allowColor = false;
	
	ChatReturnType(boolean allowColor) {
		this.setAllowColor(allowColor);
	}

	public boolean doesAllowColor() {
		return allowColor;
	}

	public void setAllowColor(boolean allowColor) {
		this.allowColor = allowColor;
	}
}
