/**
 * CustomCobbleGen By @author Philip Flyvholm
 * MinionEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.jet315.minions.events.MinerBlockBreakEvent;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;

/**
 * @author Philip
 *
 */
public class MinionEvents implements Listener{

	private BlockManager bm = BlockManager.getInstance();
	

	@EventHandler
	public void onMinionBreak(MinerBlockBreakEvent e) {
		Location loc = e.getBlock().getLocation();
		Player p = e.getMinion().getPlayer();
		if(bm.isGenLocationKnown(loc)) {
			bm.setPlayerForLocation(p.getUniqueId(), loc, false);
		}
	}
}
