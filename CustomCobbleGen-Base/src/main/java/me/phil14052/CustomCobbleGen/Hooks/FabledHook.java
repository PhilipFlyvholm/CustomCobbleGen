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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Philip
 *
 */
public class FabledHook implements IslandHook{

	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();

	@Override
	public void init() {
		//DO NOTHING
	}

	private IslandManager getIslandManager() {
		return SkyBlockAPI.getIslandManager();
	}

	private Island getIslandFromPlayer(UUID uuid) {
		Island island = getIslandManager().getIsland(Bukkit.getOfflinePlayer(uuid));
		if(island.getIsland() == null) return null;
		return island;
	}

	@Override
	public int getIslandLevel(UUID uuid) {
//		Player p = plugin.getServer().getPlayer(uuid);
		Island is = this.getIslandFromPlayer(uuid);
		if(is == null) return 0;
		return (int) is.getLevel().getLevel();
	}

	public void updateIslandLevel(UUID uuid) {
		Island is = this.getIslandFromPlayer(uuid);
		if(is == null) return;
		SkyBlockAPI.getLevellingManager().calculatePoints(is);
	}

	@Override
	public boolean isPlayerLeader(UUID uuid) {
		if(uuid == null) return false;
		UUID leaderUUID = this.getIslandLeaderFromPlayer(uuid);
		if(leaderUUID == null) return false;
		return leaderUUID.equals(uuid);
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		if(uuid == null) return null;
		plugin.debug("#getIslandLeaderFromPlayer - UUID:" + uuid);
//		Player p = plugin.getServer().getPlayer(uuid);
//		plugin.debug("Player:" + p);
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return null;
		com.songoda.skyblock.island.Island is = island.getIsland();
		plugin.debug("#getIslandLeaderFromPlayer -" + (is != null ? is.getOwnerUUID().toString() + "'s island" : "NULL"));
		if(is == null) return null;
		return is.getOwnerUUID();
	}

	@Override
	public boolean hasIsland(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		return island != null && island.getIsland() != null;
	}

	@Override
	public Player[] getArrayOfIslandMembers(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);

		if(island == null) return new Player[0];
		List<Player> onlinePlayers = new ArrayList<>();
		for(UUID pUUID : getIslandManager().getMembersOnline(island)) {
			Player p = Bukkit.getServer().getPlayer(pUUID);
			if(p != null && p.isOnline()) onlinePlayers.add(p);
		}
		return onlinePlayers.toArray(new Player[0]);
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
		return Objects.requireNonNull(this.getIslandFromPlayer(uuid)).getIsland().getBankBalance();
	}

	@Override
	public void removeFromBalance(UUID uuid, double amount) {
		Objects.requireNonNull(this.getIslandFromPlayer(uuid)).getIsland().removeFromBank(amount);
	}

	@Override
	public boolean supportsIslandBalance() {
		return true;
	}


	@Override
	public String pluginHookName() {
		return "FabledSkyBlock";
	}

	@Override
	public String getHookName() {
		return "FabledSkyBlock";
	}

	@Override
	public void onGeneratorBlockBreak(UUID uuid) {
		this.updateIslandLevel(uuid);
	}
	@Override
	public void onGeneratorGenerate(UUID uuid, Block block) {
	}
}
