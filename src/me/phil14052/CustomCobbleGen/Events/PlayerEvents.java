/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class PlayerEvents implements Listener {

	private TierManager tm = TierManager.getInstance();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		if(!tm.purchasedTiersContainsPlayer(p)) tm.givePlayerStartPurchases(p);
		if(!tm.selectedTierContainsPlayer(p)) tm.givePlayerStartSelect(p);
	}
	
	
}
