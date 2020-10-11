package me.phil14052.CustomCobbleGen.Files;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;

public class SignsFileUpdater {
	public SignsFileUpdater(CustomCobbleGen plugin){
		PluginDescriptionFile pluginYml = plugin.getDescription();
		plugin.getSignsConfig().options().header(pluginYml.getName() + "! Version: " + pluginYml.getVersion() + 
				" By Phil14052"
				+ "\nIMPORTANT: ONLY EDIT THIS IF YOU KNOW WHAT YOU ARE DOING!!");

		plugin.getSignsConfig().addDefault("signs", new ArrayList<String>());
		
		plugin.getSignsConfig().options().copyDefaults(true);
		plugin.saveSignsConfig();
	}

}
