
package me.phil14052.CustomCobbleGen.Signs;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
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
public class SelectSign implements ClickableSign{

	private Location loc = null;
	private final ClickableSignType signType = ClickableSignType.SELECT;
	private boolean valid = false;
	
	private Tier selectableTier = null;
	private final TierManager tm = TierManager.getInstance();
	
	/**
	 * @param loc the location of the sign
	 * @param tier the tier available
	 */
	public SelectSign(Location loc, Tier tier) {
		this.setLocation(loc);
		this.setSelectableTier(tier);
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
		items.add(this.getSelectableTier().getTierClass());
		items.add(this.getSelectableTier().getLevel() + "");
		String s = items.toString();
		s = s.replaceAll(" ", "");
		return s;
	}

	@Override
	public void onInteract(Player p) {
		if(!this.isValid()) return;
		if(tm.hasPlayerPurchasedLevel(p, this.getSelectableTier())) {
			SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
			selectedTiers.addTier(this.getSelectableTier());
			tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
			p.sendMessage(Lang.PREFIX + Lang.TIER_CHANGED.toString(p));
		}else {
			p.sendMessage(Lang.PREFIX + Lang.TIER_NOT_PURCHASED.toString(this.getSelectableTier()));
		}
	}

	public Tier getSelectableTier() {
		return selectableTier;
	}

	public void setSelectableTier(Tier selectableTier) {
		this.selectableTier = selectableTier;
	}

	@Override
	public boolean isValid() {
		return this.valid;
	}

	@Override
	public boolean validateData() {
		valid = this.loc != null && this.selectableTier != null;
		return this.isValid();
	}

	
	
}
