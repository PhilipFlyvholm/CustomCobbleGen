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
		}else { //CONVERTER FROM 1.4.1 to 1.4.2+
			for(String s : plugin.getPlayerConfig().getConfigurationSection("players").getKeys(false)) {
				if(plugin.getPlayerConfig().contains("players." + s + ".selected.class") || plugin.getPlayerConfig().contains("players." + s + ".selected.level")) { //This means that it is saved with the prev 1.4.2 format
					plugin.log("&cAuto updating player selected data for UUID: " + s);
					plugin.getPlayerConfig().set("players." + s + ".selected.0.class", plugin.getPlayerConfig().get("players." + s + ".selected.class"));
					plugin.getPlayerConfig().set("players." + s + ".selected.0.level", plugin.getPlayerConfig().get("players." + s + ".selected.level"));
					plugin.getPlayerConfig().set("players." + s + ".selected.level", null);
					plugin.getPlayerConfig().set("players." + s + ".selected.class", null);
				}
			}
		}
		
		plugin.getPlayerConfig().options().copyDefaults(true);
		plugin.savePlayerConfig();
	}

}
