package me.phil14052.CustomCobbleGen.Files;

import org.bukkit.plugin.PluginDescriptionFile;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

public class PlayerFileUpdater {
	public PlayerFileUpdater(CustomCobbleGen plugin){
		PluginDescriptionFile pluginYml = plugin.getDescription();
		plugin.getPlayerConfig().options().header(pluginYml.getName() + "! Version: " + pluginYml.getVersion() + 
				" By Phil14052"
				+ "\nIMPORTANT: ONLY EDIT THIS IF YOU KNOW WHAT YOU ARE DOING!!");
		if(plugin.getPlayerConfig().getConfigurationSection("players") == null){
			plugin.getPlayerConfig().createSection("players");
		}
		
		plugin.getPlayerConfig().options().copyDefaults(true);
		plugin.savePlayerConfig();
	}

}
