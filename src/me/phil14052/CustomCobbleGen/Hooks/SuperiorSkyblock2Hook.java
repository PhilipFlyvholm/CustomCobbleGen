/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SuperiorSkyblock2Hook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * @author Philip
 *
 */
public class SuperiorSkyblock2Hook implements IslandHook {

	private Island getIslandFromPlayer(Player p)  {
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(p.getUniqueId());
		if(sp == null || sp.getIsland() == null) return null;
		return sp.getIsland();
	}
	
	@Override
	public int getIslandLevel(Player p) {
		Island island = this.getIslandFromPlayer(p);
		if(island == null) return 0;
		return island.getIslandLevel().intValue();
		
	}

	@Override
	public boolean isPlayerLeader(Player p) {
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(p.getUniqueId());
		if(sp == null) return false;
		SuperiorPlayer leader = sp.getIslandLeader();
		if(leader == null) return false;
		return p.getUniqueId().equals(leader.getUniqueId());
	}

	@Override
	public UUID getIslandLeaderFromPlayer(Player p) {
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(p.getUniqueId());
		if(sp == null) return null;
		SuperiorPlayer leader = sp.getIslandLeader();
		if(leader == null) return null;
		return leader.getUniqueId();		
	}

}
