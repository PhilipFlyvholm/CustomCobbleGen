/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.cryptomorin.xseries.XMaterial;

import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
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
	private PermissionManager pm =  new PermissionManager();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		UUID uuid = e.getPlayer().getUniqueId();
		// If the player has previously used the plugin, then load the player info.
		if(!tm.selectedTierContainsUUID(uuid) && !tm.purchasedTiersContainsUUID(uuid)) tm.loadPlayerData(uuid);
		if(!tm.selectedTierContainsUUID(uuid)) tm.givePlayerStartSelect(uuid);
		if(!tm.purchasedTiersContainsUUID(uuid)) tm.givePlayerStartPurchases(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		// Cleanup
		bm.cleanupExpiredPistons(p.getUniqueId());
		tm.savePlayerData(p.getUniqueId());
		bm.cleanupExpiredLocations();
	}
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(!signManager.areSignsEnabled()) return;
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(isSign(e.getClickedBlock().getType())) {
			ClickableSign sign = signManager.getSignFromLocation(e.getClickedBlock().getLocation());
			if(sign == null) return;
			if(pm.hasPermisson(e.getPlayer(), "customcobblegen.signs.use." + sign.getSignType().name().toLowerCase(), true)) {
				sign.onInteract(e.getPlayer());	
			}
		}
	}
	
	public boolean isSign(Material material) {
		return material == XMaterial.ACACIA_SIGN.parseMaterial(true) || material == XMaterial.ACACIA_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.BIRCH_SIGN.parseMaterial(true) || material == XMaterial.BIRCH_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.DARK_OAK_SIGN.parseMaterial(true) || material == XMaterial.DARK_OAK_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.JUNGLE_SIGN.parseMaterial(true) || material == XMaterial.JUNGLE_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.OAK_SIGN.parseMaterial(true) || material == XMaterial.OAK_WALL_SIGN.parseMaterial(true);
	}
}
