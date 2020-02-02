/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Signs.ClickableSign;
import me.phil14052.CustomCobbleGen.Signs.SignManager;

/**
 * @author Philip
 *
 */
public class PlayerEvents implements Listener {

	private TierManager tm = TierManager.getInstance();
	private BlockManager bm = BlockManager.getInstance();
	private SignManager signManager = SignManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		// If the player has previously used the plugin, then load the player info.
		if(!tm.selectedTierContainsPlayer(p) && !tm.purchasedTiersContainsPlayer(p)) tm.loadPlayerData(p);
		if(!tm.selectedTierContainsPlayer(p)) tm.givePlayerStartSelect(p);
		if(!tm.purchasedTiersContainsPlayer(p)) tm.givePlayerStartPurchases(p);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		// Cleanup
		tm.savePlayerData(p);
		bm.cleanupExpiredLocations();
	}
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(isSign(e.getClickedBlock().getType())) {
			ClickableSign sign = signManager.getSignFromLocation(e.getClickedBlock().getLocation());
			if(sign == null) return;
			sign.onInteract(e.getPlayer());
		}
	}
	
	public boolean isSign(Material material) {
		if(plugin.serverSupports(14)) {
			if(material == Material.ACACIA_SIGN || material == Material.ACACIA_WALL_SIGN
					|| material == Material.BIRCH_SIGN || material == Material.BIRCH_WALL_SIGN
					|| material == Material.DARK_OAK_SIGN || material == Material.DARK_OAK_WALL_SIGN
					|| material == Material.JUNGLE_SIGN || material == Material.JUNGLE_WALL_SIGN
					|| material == Material.OAK_SIGN || material == Material.OAK_WALL_SIGN) {
				return true;
			}
		}else {
			return material.name().equalsIgnoreCase("sign");
		}
		return false;
	}
}
