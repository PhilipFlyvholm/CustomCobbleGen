/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SelectSign.java
 */
package me.phil14052.CustomCobbleGen.Signs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.GUI.GUIManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class BuySign implements ClickableSign{

	private Location loc = null;
	private ClickableSignType signType = ClickableSignType.BUY;
	private boolean valid = false;
	
	private Tier purchasableTier = null;
	private TierManager tm = TierManager.getInstance();
	private GUIManager guiManager = GUIManager.getInstance();
	private PermissionManager pm = new PermissionManager();
	
	/**
	 * @param loc
	 * @param tier
	 */
	public BuySign(Location loc, Tier tier) {
		this.setLocation(loc);
		this.setPurchasableTier(tier);
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
		// [World, x, y, z, type, data(Class,level)]
		List<String> items = new ArrayList<>();
		items.add(loc.getWorld().getName() + "");
		items.add(loc.getX() + "");
		items.add(loc.getY() + "");
		items.add(loc.getZ() + "");
		items.add(this.getSignType().toString());
		items.add(this.getPurchasableTier().getTierClass());
		items.add(this.getPurchasableTier().getLevel() + "");
		String s = items.toString();
		s = s.replaceAll(" ", "");
		return s;
	}

	@Override
	public void onInteract(Player p) {
		if(!this.isValid()) return;
		if(tm.hasPlayerPurchasedLevel(p, this.getPurchasableTier())) {
			tm.setPlayerSelectedTier(p, this.getPurchasableTier());
			p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(p));
		}else {
			String tierClass = this.getPurchasableTier().getTierClass();
			if(!tierClass.equalsIgnoreCase("DEFAULT") && !pm.hasPermission(p, "customcobblegen.generator." + tierClass, true)) return;
			if(tm.hasPlayerPurchasedPreviousLevel(p, this.getPurchasableTier())) {
				if(tm.canPlayerBuyTier(p, this.getPurchasableTier())) {
					guiManager.new ConfirmGUI(p, this.purchasableTier).open();	
				}else {
					p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CANT_AFFORD.toString(getPurchasableTier()));
				}
			}else {
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_LOCKED.toString(getPurchasableTier()));
			}
		}
	}

	public Tier getPurchasableTier() {
		return purchasableTier;
	}

	public void setPurchasableTier(Tier purchasableTier) {
		this.purchasableTier = purchasableTier;
	}

	@Override
	public boolean isValid() {
		return this.valid;
	}

	@Override
	public boolean validateData() {
		if(this.loc == null || this.purchasableTier == null) valid = false;
		else valid = true;
		return this.isValid();
	}

	
	
}
