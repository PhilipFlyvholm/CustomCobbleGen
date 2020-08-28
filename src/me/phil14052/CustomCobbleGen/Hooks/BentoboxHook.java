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

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author Philip
 *
 */
public class BentoboxHook implements IslandHook{

	private BentoBox api;
	
	public BentoboxHook() {
		api = (BentoBox) Bukkit.getPluginManager().getPlugin("BentoBox");
	}

	@Override
	public int getIslandLevel(Player p) {
//		TODO: Get the API version to work, instead of using reflections. The API currently only returns 0L...
//		
//		UUID uuid = p.getUniqueId();
//		Long result = (Long) new AddonRequestBuilder().addon("Level").label("island-level")
//			    .addMetaData(p.getLocation().getWorld().getName(), uuid)
//			    .request();
//		return Math.toIntExact(result);
		
		UUID uuid = p.getUniqueId();
		int level[] = new int[]{0};
	
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

	private Island getIslandFromPlayer(Player p) {

		return api.getIslands().getIsland(p.getWorld(), p.getUniqueId());
	}
	
	@Override
	public boolean isPlayerLeader(Player p) {
		return this.getIslandLeaderFromPlayer(p).equals(p.getUniqueId());
	}

	@Override
	public UUID getIslandLeaderFromPlayer(Player p) {
		Island island = this.getIslandFromPlayer(p);
		return island.getOwner();
	}
	
}
