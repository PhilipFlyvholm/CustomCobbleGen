
package me.phil14052.CustomCobbleGen.Hooks;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
public class ASkyBlockHook implements IslandHook{

	private ASkyBlockAPI api;

	public void init(){
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid) {
		return (int) api.getLongIslandLevel(uuid);
	}

	private Island getIslandFromPlayer(UUID uuid) {
		Location loc = api.getIslandLocation(uuid);
		if(loc == null) return null;
		return api.getIslandAt(loc);
	}
	
	@Override
	public boolean isPlayerLeader(UUID uuid) {
		return this.getIslandLeaderFromPlayer(uuid).equals(uuid);
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return null;
		return island.getOwner();
	}
	
	public boolean hasIsland(UUID uuid) {
		return api.hasIsland(uuid);
	}

	@Override
	public Player[] getArrayOfIslandMembers(UUID uuid) {
		if(!this.hasIsland(uuid)) return new Player[0];
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return new Player[0];
		List<Player> onlinePlayers = new ArrayList<>();
		for(UUID pUUID : island.getMembers()) {
			Player p = Bukkit.getServer().getPlayer(pUUID);
			if(p != null && p.isOnline()) onlinePlayers.add(p);
		}
		return onlinePlayers.toArray(new Player[0]);
	}


	@Override
	public void sendMessageToIslandMembers(String message, UUID uuid) {
		this.sendMessageToIslandMembers(message, uuid, false);
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

	public boolean supportsIslandBalance() {
		return false;
	}
	
	/**
	 * ASkyBlock does not support balance
	 */
	@Override
	public double getBalance(UUID uuid) {
		return 0;
	}
	
	/**
	 * ASkyBlock does not support balance
	 */
	@Override
	public void removeFromBalance(UUID uuid, double amount) {}

	@Override
	public String pluginHookName() {
		return "ASkyBlock";
	}

	@Override
	public String getHookName() {
		return "ASkyBlock";
	}

	@Override
	public void onGeneratorBlockBreak(UUID uuid) {}
	
}
