/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class PlayerEvents implements Listener {

	private TierManager tm = TierManager.getInstance();
	private BlockManager bm = BlockManager.getInstance();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		if(!tm.selectedTierContainsPlayer(p) && !tm.purchasedTiersContainsPlayer(p)) tm.loadPlayerData(p);
		if(!tm.selectedTierContainsPlayer(p)) tm.givePlayerStartSelect(p);
		if(!tm.purchasedTiersContainsPlayer(p)) tm.givePlayerStartPurchases(p);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		tm.savePlayerData(p);
		bm.cleanupExpiredLocations();
	}
	
}
