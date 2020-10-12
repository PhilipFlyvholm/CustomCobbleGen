/**
 * CustomCobbleGen By @author Philip Flyvholm
 * FabledHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandManager;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Philip
 *
 */
public class FabledHook implements IslandHook{
	
	private IslandManager fabledApi;
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public FabledHook() {
		fabledApi = SkyBlockAPI.getIslandManager();
	}

	private Island getIslandFromPlayer(UUID uuid) {
		return fabledApi.getIsland(Bukkit.getOfflinePlayer(uuid));
	}
	
	@Override
	public int getIslandLevel(UUID uuid) {
//		Player p = plugin.getServer().getPlayer(uuid);
		Island is = this.getIslandFromPlayer(uuid);
		if(is == null) return 0;
		return (int) is.getLevel().getLevel();
	}


	@Override
	public boolean isPlayerLeader(UUID uuid) {
		return this.getIslandLeaderFromPlayer(uuid).equals(uuid);
	}


	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		plugin.debug("#getIslandLeaderFromPlayer - UUID:" + uuid);
//		Player p = plugin.getServer().getPlayer(uuid);
//		plugin.debug("Player:" + p);
		com.songoda.skyblock.island.Island is = this.getIslandFromPlayer(uuid).getIsland();
		plugin.debug("#getIslandLeaderFromPlayer -" + (is != null ? is.getOwnerUUID().toString() + "'s island" : "NULL"));
		if(is == null) return null;
		return is.getOwnerUUID();
	}

	@Override
	public boolean hasIsland(UUID uuid) {
		return IslandManager.hasIsland(Bukkit.getOfflinePlayer(uuid));
	}

	@Override
	public Player[] getArrayOfIslandMembers(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);

		if(island == null) return new Player[0];
		List<Player> onlinePlayers = new ArrayList<>();
		for(UUID pUUID : fabledApi.getMembersOnline(island)) {
			Player p = Bukkit.getServer().getPlayer(pUUID);
			if(p != null && p.isOnline()) onlinePlayers.add(p);
		}
		return onlinePlayers.toArray(new Player[onlinePlayers.size()]);
	}
	
	@Override
	public void sendMessageToIslandMembers(String message, UUID uuid, boolean withoutSender) {
		Player[] players = this.getArrayOfIslandMembers(uuid);
		if(players == null) return;
		for(Player p : players) {

			if(p.getUniqueId().equals(uuid) && withoutSender) continue;
			p.sendMessage(message);
		}
	}

	@Override
	public void sendMessageToIslandMembers(String message, UUID uuid) {
		this.sendMessageToIslandMembers(message, uuid, false);
	}

	@Override
	public double getBalance(UUID uuid) {
		return this.getIslandFromPlayer(uuid).getIsland().getBankBalance();
	}

	@Override
	public void removeFromBalance(UUID uuid, double amount) {
		this.getIslandFromPlayer(uuid).getIsland().removeFromBank(amount);
	}

	@Override
	public boolean supportsIslandBalance() {
		return true;
	}

	@Override
	public String getHookName() {
		return "FabledSkyBlock";
	}

}
