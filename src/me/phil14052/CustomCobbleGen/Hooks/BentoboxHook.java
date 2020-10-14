package me.phil14052.CustomCobbleGen.Hooks;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 * @author Philip
 *
 */
public class BentoboxHook implements IslandHook{

	private final BentoBox api;
	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	/**
	 * LEFT SIDE UUID IS THE MEMBER
	 * RIGHT SIDE UUID IS THE OWNER
	 */
	private final Map<UUID, UUID> boundUUIDs;
	
	public BentoboxHook() {
		this.boundUUIDs = new HashMap<>();
		api = (BentoBox) Bukkit.getPluginManager().getPlugin("BentoBox");
	}

	@Override
	public int getIslandLevel(UUID uuid) {
//		TODO: Get the API version to work, instead of using reflections. The API currently only returns 0L...
//		
//		UUID uuid = p.getUniqueId();
//		Long result = (Long) new AddonRequestBuilder().addon("Level").label("island-level")
//			    .addMetaData(p.getLocation().getWorld().getName(), uuid)
//			    .request();
//		return Math.toIntExact(result);
		int[] level = new int[]{0};
		Player p = plugin.getServer().getPlayer(uuid);
		if(p == null) return 0;
		api.getAddonsManager().getAddonByName("Level").ifPresent(addon -> {
			try {
				Method method = addon.getClass().getMethod("getIslandLevel", World.class, UUID.class);
				long rawLevel = (long) method.invoke(addon, p.getWorld(), uuid);
				level[0] = Math.toIntExact(rawLevel);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				plugin.error("Level addon needed for BentoBox for level requirement to work", true);
			}
		});
		return level[0];
	}

	private Island getIslandFromPlayer(UUID uuid) {
		User user = api.getPlayers().getUser(uuid);
		return api.getIslands().getIsland(user.getWorld(), user);
	}
	
	@Override
	public boolean isPlayerLeader(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		if(p == null) return false;
		if(api.getIslands().hasIsland(p.getWorld(), uuid)) return true;
		return this.getIslandLeaderFromPlayer(uuid).equals(uuid);
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		if(uuid == null) {
			plugin.error("UUID given is null in BentoboxHook#getIslandLeaderFromPlayer(UUID uuid);");
			return null;
		}
		Player p = Bukkit.getPlayer(uuid);
		if(p != null && api.getIslands().hasIsland(p.getWorld(), uuid)) return uuid;

		if(this.boundUUIDs.containsKey(uuid)) return this.boundUUIDs.get(uuid);

		User user = api.getPlayers().getUser(uuid);
		if(user == null){
			plugin.debug(uuid + " user returns null BentoBox#getIslandFromPlayer()");
			return null;
		}

		UUID ownerUUID = api.getIslands().getOwner(user.getWorld(), user.getUniqueId());
		this.boundUUIDs.put(uuid, ownerUUID);
		return ownerUUID;
	}


	/**
	 * hasIsland = if they are member or owner of a island. NOT ONLY IF THEY ARE OWNER WHICH IS WHY I AM NOT USING
	 * THE hasIsland function in the API
	 */
	@Override
	public boolean hasIsland(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		return island != null;
	}
	
	@Override
	public Player[] getArrayOfIslandMembers(UUID uuid) {
		if(!this.hasIsland(uuid)) return new Player[0];
		Island island = this.getIslandFromPlayer(uuid);
		if(island == null) return new Player[0];
		List<Player> onlinePlayers = new ArrayList<>();
		for(UUID pUUID : island.getMembers().keySet()) {
			Player p = Bukkit.getServer().getPlayer(pUUID);
			if(p != null && p.isOnline()) onlinePlayers.add(p);
		}
		return onlinePlayers.toArray(new Player[onlinePlayers.size()]);
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


	/**
	 * BentoBox does not support balance. At least not known to me
	 */
	@Override
	public double getBalance(UUID uuid) {
		return 0;
	}

	/**
	 * BentoBox does not support balance. At least not known to me
	 */
	@Override
	public void removeFromBalance(UUID uuid, double amount) {}

	@Override
	public boolean supportsIslandBalance() {
		return false;
	}
	
	@Override
	public String getHookName() {
		return "BentoBox";
	}
	
}
