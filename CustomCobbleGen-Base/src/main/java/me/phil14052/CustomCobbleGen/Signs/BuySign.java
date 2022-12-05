
package me.phil14052.CustomCobbleGen.Signs;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.GUI.GUIManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SelectSign.java
 */
public class BuySign implements ClickableSign{

	private Location loc = null;
	private final ClickableSignType signType = ClickableSignType.BUY;
	private boolean valid = false;
	
	private Tier purchasableTier = null;
	private final TierManager tm = TierManager.getInstance();
	private final GUIManager guiManager = GUIManager.getInstance();
	private final PermissionManager pm = new PermissionManager();
	
	/**
	 * @param loc The location of the sign
	 * @param tier The tier available
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
	public String serializeSign() {
		// [World, x, y, z, type, data(Class,level)]
		List<String> items = new ArrayList<>();
		items.add(loc.getWorld() != null ? loc.getWorld().getName() + "" : "");
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
			SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
			selectedTiers.addTier(this.getPurchasableTier());
			tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
			p.sendMessage(Lang.PREFIX + Lang.TIER_CHANGED.toString(p));
		}else {
			String tierClass = this.getPurchasableTier().getTierClass();
			if(!tierClass.equalsIgnoreCase("DEFAULT") && !pm.hasPermission(p, "customcobblegen.generator." + tierClass, true)) return;
			if(tm.hasPlayerPurchasedPreviousLevel(p, this.getPurchasableTier())) {
				if(tm.canPlayerBuyTier(p, this.getPurchasableTier())) {
					guiManager.new ConfirmGUI(p, this.purchasableTier).open();	
				}else {
					p.sendMessage(Lang.PREFIX + Lang.TIER_CANT_AFFORD.toString(getPurchasableTier()));
				}
			}else {
				p.sendMessage(Lang.PREFIX + Lang.TIER_LOCKED.toString(getPurchasableTier()));
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
		valid = this.loc != null && this.purchasableTier != null;
		return this.isValid();
	}

	
	
}
