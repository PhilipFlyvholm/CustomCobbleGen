/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SuperiorSkyblock2Hook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.util.UUID;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * @author Philip
 *
 */
public class SuperiorSkyblock2Hook implements IslandHook {

	private Island getIslandFromPlayer(UUID uuid)  {
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(uuid);
		if(sp == null || sp.getIsland() == null) return null;
		return sp.getIsland();
	}
	
	@Override
	public int getIslandLevel(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return 0;
		return island.getIslandLevel().intValue();
		
	}

	@Override
	public boolean isPlayerLeader(UUID uuid) {
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(uuid);
		if(sp == null) return false;
		SuperiorPlayer leader = sp.getIslandLeader();
		if(leader == null) return false;
		return uuid.equals(leader.getUniqueId());
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(uuid);
		if(sp == null) return null;
		SuperiorPlayer leader = sp.getIslandLeader();
		if(leader == null) return null;
		return leader.getUniqueId();		
	}

}
