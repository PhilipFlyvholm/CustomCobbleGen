/**
 * CustomCobbleGen By @author Philip Flyvholm
 * SuperiorSkyblock2Hook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Philip
 *
 */
public class SuperiorSkyblock2Hook implements IslandHook {

	@Override
	public void init() {
		PluginModule generatorModule = SuperiorSkyblockAPI.getModules().getModule("generators");
		if(generatorModule != null)	generatorModule.disableModule();
		try {
		for (RegisteredListener l : HandlerList.getRegisteredListeners(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2")))){
			if(l.getListener().getClass() != Class.forName("com.bgsoftware.superiorskyblock.module.generators.listeners.GeneratorsListener")) continue;
			HandlerList.unregisterAll(l.getListener());
			CustomCobbleGen.getInstance().debug("Disabled event for SuperiorSkyblock2");
		}
		} catch (ClassNotFoundException ignored) {}
	}

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
		if(uuid == null) return null;
		SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(uuid);
		if(sp == null) return null;
		SuperiorPlayer leader = sp.getIslandLeader();
		if(leader == null) return null;
		return leader.getUniqueId();		
	}

	@Override
	public boolean hasIsland(UUID uuid) {
		return this.getIslandFromPlayer(uuid) != null;
	}

	@Override
	public Player[] getArrayOfIslandMembers(UUID uuid) {
		if(!this.hasIsland(uuid)) return new Player[0];
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return new Player[0];
		List<Player> onlinePlayers = new ArrayList<>();
		for(SuperiorPlayer superiorPlayer : island.getCoopPlayers()) {
			Player p = Bukkit.getServer().getPlayer(superiorPlayer.getUniqueId());
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

	@Override
	public double getBalance(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return 0;
		IslandBank bank = island.getIslandBank();
		if(bank == null) return 0;
		return bank.getBalance().doubleValue();
	}

	@Override
	public void removeFromBalance(UUID uuid, double amount) {
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return;
		IslandBank bank = island.getIslandBank();
		if(bank == null) return;
		BigDecimal amountBig = BigDecimal.valueOf(amount);
		bank.withdrawAdminMoney(Bukkit.getConsoleSender(), amountBig);
	}

	@Override
	public boolean supportsIslandBalance() {
		return true;
	}

	@Override
	public String pluginHookName() {
		return "SuperiorSkyblock2";
	}

	@Override
	public String getHookName() {
		return "SuperiorSkyBlock2";
	}

	@Override
	public void onGeneratorBlockBreak(UUID uuid, Block block) {
		Island i = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
		if(i != null){
			CustomCobbleGen.getInstance().debug("Block broken", block.getType());
			i.handleBlockBreak(block,1);
		}
	}

	@Override
	public void onGeneratorGenerate(UUID uuid, Block block) {
		Island i = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
		if(i != null){
			CustomCobbleGen.getInstance().debug("Block placed", block.getType());
			i.handleBlockPlace(block,1);
		}
	}
}
