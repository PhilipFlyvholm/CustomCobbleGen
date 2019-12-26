/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

/**
 * @author Philip
 *
 */
public class uSkyBlockHook implements IslandLevelHook{

	private HookType type = HookType.USKYBLOCK;
	private uSkyBlockAPI api;
	
	public uSkyBlockHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
		if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
		  api =  (uSkyBlockAPI) plugin;
		}
	}
	
	@Override
	public HookType getHookType() {
		return this.type;
	}

	@Override
	public int getIslandLevel(Player p) {
		return (int) Math.floor(api.getIslandLevel(p));
	}
}
