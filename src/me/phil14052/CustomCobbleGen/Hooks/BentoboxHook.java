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

/**
 * @author Philip
 *
 */
public class BentoboxHook implements IslandLevelHook{

	private HookType type = HookType.BENTOBOX;
	private BentoBox api;
	
	public BentoboxHook() {
		api = (BentoBox) Bukkit.getPluginManager().getPlugin("BentoBox");
	}
	
	@Override
	public HookType getHookType() {
		return this.type;
	}

	@Override
	public int getIslandLevel(Player p) {
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
}
