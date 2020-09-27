/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author Philip
 *
 */
public class BentoboxHook implements IslandHook{

	private BentoBox api;
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public BentoboxHook() {
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
		int level[] = new int[]{0};
		Player p = plugin.getServer().getPlayer(uuid);
	
		api.getAddonsManager().getAddonByName("Level").ifPresent(addon -> {
			try {
				Method method = addon.getClass().getMethod("getIslandLevel", World.class, UUID.class);
				long rawLevel = (long) method.invoke(addon, p.getWorld(), uuid);
				level[0] = Math.toIntExact(rawLevel);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return level[0];
	}

	private Island getIslandFromPlayer(UUID uuid) {
		Player p = plugin.getServer().getPlayer(uuid);
		return api.getIslands().getIsland(p.getWorld(), uuid);
	}
	
	@Override
	public boolean isPlayerLeader(UUID uuid) {
		return this.getIslandLeaderFromPlayer(uuid).equals(uuid);
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		Island island = this.getIslandFromPlayer(uuid);
		return island.getOwner();
	}

	@Override
	public boolean hasIsland(UUID uuid) {
		Player p = plugin.getServer().getPlayer(uuid);
		return api.getIslands().hasIsland(p.getWorld(), uuid);
	}
	
}
