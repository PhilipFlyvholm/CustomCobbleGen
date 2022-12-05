package me.phil14052.CustomCobbleGen.Files.updaters;

import com.cryptomorin.xseries.XMaterial;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.List;

public class ConfigUpdater extends YamlConfiguration {
	
	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public ConfigUpdater() {
		PluginDescriptionFile pluginYml = plugin.getDescription();
		@SuppressWarnings("unused")
		String[] motdList = { "&5Welcome back on %newline% %ServerName%",
				"&cYou are so %newline% &a&lAWESOME" };
		FileConfiguration config = plugin.getConfig();
		config
				.options()
				.header(pluginYml.getName() + "! Version: "
						+ pluginYml.getVersion()
						+ " By " + pluginYml.getAuthors().get(0));
		config.options().copyHeader();
		if(!Setting.isConfigSet()) Setting.setFile(config);
		for(Setting setting : Setting.values()) {
			if(setting.isSection()) continue; //SECTIONS ARE FOR REFERENCE ONLY
			config.addDefault(setting.getPath(), setting.getDefaultValue());
		}
		String genertionModePath = Setting.SECTION_GENERATIONMODES.getPath();
		if(!config.contains(genertionModePath) || !config.contains(genertionModePath + ".0")) {
			config.addDefault(genertionModePath + ".0.blocks", new String[] {"WATER", "LAVA"});
			config.addDefault(genertionModePath + ".0.displayName", "Cobblestone generator");
			config.addDefault(genertionModePath + ".0.fallback", "COBBLESTONE");
			config.addDefault(genertionModePath + ".0.particleEffect", "SMOKE_LARGE");
			config.addDefault(genertionModePath + ".0.generationSound", XMaterial.supports(9) ? "ENTITY_EXPERIENCE_ORB_PICKUP" : "ORB_PICKUP");
		}

		String tiersPath = Setting.SECTION_TIERS.getPath();
		if(!config.contains(tiersPath)) {
			//Default tier 0
			config.addDefault(tiersPath + ".default." + 0 + ".name", "Default");
			config.addDefault(tiersPath + ".default." + 0 + ".icon", "COBBLESTONE");
			config.addDefault(tiersPath + ".default." + 0 + ".price.money", 0);
			config.addDefault(tiersPath + ".default." + 0 + ".contains.COBBLESTONE", 90);
			config.addDefault(tiersPath + ".default." + 0 + ".contains.COAL_ORE", 10);
			
			//Default tier 1
			config.addDefault(tiersPath + ".default." + 1 + ".name", "Basic");
			config.addDefault(tiersPath + ".default." + 1 + ".icon", "COAL_BLOCK");
			config.addDefault(tiersPath + ".default." + 1 + ".price.money", 1000);
			config.addDefault(tiersPath + ".default." + 1 + ".contains.COBBLESTONE", 70);
			config.addDefault(tiersPath + ".default." + 1 + ".contains.COAL_ORE", 20);
			config.addDefault(tiersPath + ".default." + 1 + ".contains.IRON_ORE", 10);
			
			//Default tier 2
			config.addDefault(tiersPath + ".default." + 2 + ".name", "Advanced");
			config.addDefault(tiersPath + ".default." + 2 + ".icon", "IRON_BLOCK");
			config.addDefault(tiersPath + ".default." + 2 + ".price.money", 2000);
			config.addDefault(tiersPath + ".default." + 2 + ".price.items.COBBLESTONE", 64);
			config.addDefault(tiersPath + ".default." + 2 + ".contains.COBBLESTONE", 50);
			config.addDefault(tiersPath + ".default." + 2 + ".contains.COAL_ORE", 30);
			config.addDefault(tiersPath + ".default." + 2 + ".contains.IRON_ORE", 20);
			
			//VIP tier 0
			config.addDefault(tiersPath + ".vip." + 0 + ".name", "Basic VIP");
			config.addDefault(tiersPath + ".vip." + 0 + ".icon", "COBBLESTONE");
			config.addDefault(tiersPath + ".vip." + 0 + ".price.money", 0);
			config.addDefault(tiersPath + ".vip." + 0 + ".price.xp", 5);
			config.addDefault(tiersPath + ".vip." + 0 + ".contains.COBBLESTONE", 90);
			config.addDefault(tiersPath + ".vip." + 0 + ".contains.COAL_ORE", 5);
			config.addDefault(tiersPath + ".vip." + 0 + ".contains.IRON_ORE", 5);
			
			//VIP tier 1
			config.addDefault(tiersPath + ".vip." + 1 + ".name", "Advanced VIP");
			config.addDefault(tiersPath + ".vip." + 1 + ".icon", "COAL_BLOCK");
			config.addDefault(tiersPath + ".vip." + 1 + ".price.money", 500);
			config.addDefault(tiersPath + ".vip." + 1 + ".contains.COBBLESTONE", 60);
			config.addDefault(tiersPath + ".vip." + 1 + ".contains.COAL_ORE", 20);
			config.addDefault(tiersPath + ".vip." + 1 + ".contains.IRON_ORE", 20);
			
			//VIP tier 2
			config.addDefault(tiersPath + ".vip." + 2 + ".name", "Pro VIP");
			config.addDefault(tiersPath + ".vip." + 2 + ".icon", "IRON_BLOCK");
			config.addDefault(tiersPath + ".vip." + 2 + ".price.money", 1000);
			config.addDefault(tiersPath + ".vip." + 2 + ".contains.COBBLESTONE", 25);
			config.addDefault(tiersPath + ".vip." + 2 + ".contains.COAL_ORE", 35);
			config.addDefault(tiersPath + ".vip." + 2 + ".contains.IRON_ORE", 30);
			config.addDefault(tiersPath + ".vip." + 2 + ".contains.DIAMOND_ORE", 10);
		}
		config.options().copyDefaults(true);
		plugin.saveDefaultConfig();
		
		/* This will try to update from version 1.4.0 and prev to 1.4.1 configs */
		ConfigurationSection modeSection = config.getConfigurationSection("options.generationModes");
		boolean changesMade = false;
		for(String mode : modeSection.getKeys(false)) {
			if(modeSection.contains(mode + ".blocks") || modeSection.contains(mode + ".fixedBlocks")) continue;
			else {
				plugin.log("&aFound prev 1.4.1 configuration of generation mode. Will now try to auto update configuration.");
			}
			List<String> blocks = new ArrayList<>();
			if(modeSection.contains(mode + ".firstBlock")) {
				blocks.add(modeSection.getString(mode + ".firstBlock"));
				modeSection.set(mode + ".firstBlock", null);
				changesMade = true;
			}
			if(modeSection.contains(mode + ".secondBlock")) {
				blocks.add(modeSection.getString(mode + ".secondBlock"));
				modeSection.set(mode + ".secondBlock", null);
				changesMade = true;
			}
			modeSection.set(mode + ".blocks", blocks);
			if(blocks.isEmpty()) {
				plugin.log("&cFAILED AUTO UPDATING GENERATION MODE CONFIGURATION");
			}else {
				plugin.log("&aSuccessfully auto updated generation mode configuration!");
			}
		}
		if(changesMade) {
			plugin.saveConfig();
			plugin.reloadConfig();
		}
	}

}
