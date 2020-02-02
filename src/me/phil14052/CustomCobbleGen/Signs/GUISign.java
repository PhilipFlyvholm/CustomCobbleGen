/**
 * CustomCobbleGen By @author Philip Flyvholm
 * GUISign.java
 */
package me.phil14052.CustomCobbleGen.Signs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.GUI.GUIManager;

/**
 * @author Philip
 *
 */
public class GUISign implements ClickableSign{
	
	private Location loc = null;
	private ClickableSignType signType = ClickableSignType.GUI;
	private GUIManager guiManager = GUIManager.getInstance();
	private boolean valid = false;
	
	public GUISign(Location loc) {
		this.setLocation(loc);
		valid = true;
	}

	@Override
	public Location getLocation() {
		return this.loc;
	}

	@Override
	public void setLocation(Location loc) {
		this.loc = loc;
		
	}

	@Override
	public ClickableSignType getSignType() {
		return this.signType;
	}

	@Override
	public void setSignType(ClickableSignType signType) {
		this.signType = signType;
		
	}

	@Override
	public String serializeSign() {
		// [World, x, y, z, type, data]
		List<String> items = new ArrayList<>();
		items.add(loc.getWorld().getName() + "");
		items.add(loc.getX() + "");
		items.add(loc.getY() + "");
		items.add(loc.getZ() + "");
		items.add(this.getSignType().toString());
		String s = items.toString();
		s = s.replaceAll(" ", "");
		return s;
	}


	@Override
	public void onInteract(Player p) {
		if(this.isValid()) guiManager.new MainGUI(p).open();
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean validateData() {
		this.valid = true;
		return this.isValid();
	}	
	
}
