/**
 * CustomCobbleGen By @author Philip Flyvholm
 * CustomCobbleGen.java
 */
package me.phil14052.CustomCobbleGen;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.phil14052.CustomCobbleGen.Commands.MainCommand;
import me.phil14052.CustomCobbleGen.Commands.MainTabComplete;
import me.phil14052.CustomCobbleGen.Events.BlockEvents;
import me.phil14052.CustomCobbleGen.Events.PlayerEvents;
import me.phil14052.CustomCobbleGen.Files.ConfigUpdater;
import me.phil14052.CustomCobbleGen.Files.Files;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Files.LangFileUpdater;
import me.phil14052.CustomCobbleGen.Files.PlayerFileUpdater;
import me.phil14052.CustomCobbleGen.Files.SignsFileUpdater;
import me.phil14052.CustomCobbleGen.GUI.InventoryEvents;
import me.phil14052.CustomCobbleGen.Hooks.HookType;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Signs.SignManager;
import me.phil14052.CustomCobbleGen.Utils.GlowEnchant;
import me.phil14052.CustomCobbleGen.Utils.TierPlaceholderExpansion;
import me.phil14052.CustomCobbleGen.Utils.Metrics.Metrics;

public class CustomCobbleGen extends JavaPlugin {
	private static CustomCobbleGen plugin;
	public Files lang;
	private FileConfiguration playerConfig;
	private File playerConfigFile;
	private FileConfiguration signsConfig;
	private File signsConfigFile;
	private TierManager tierManager;
	private SignManager signManager;
	private EconomyManager econManager;
	public boolean isUsingPlaceholderAPI = false;
	public HookType islandPluginHooked = null;
	
	@Override
	public void onEnable(){
		double time = System.currentTimeMillis();
		plugin = this;
		tierManager = TierManager.getInstance();
		signManager = SignManager.getInstance();
		plugin.log("Enabling CustomCobbleGen plugin");
		// Setup config
		new ConfigUpdater();
		saveConfig();
		this.debug("The config is now setup");
		// Setup lang file
		lang = new Files(this, "lang.yml");
		new LangFileUpdater(plugin);
		Lang.setFile(lang);
		this.debug("Lang is now setup");
		// Setup player configs
		playerConfig = null;
		playerConfigFile = null;
		new PlayerFileUpdater(plugin);
		this.debug("Players is now setup");
		// Setup tiers
		tierManager.load();
		this.debug("Tiers is now setup");
		// Setup signs configs
		signsConfig = null;
		signsConfigFile = null;
		new SignsFileUpdater(plugin);
		signManager.loadSignsFromFile(true);
		this.debug("Signs is now setup");
		this.setupHooks();
	 
		registerEvents();
		plugin.debug("Events loaded&2 \u2713");
		plugin.getCommand("cobblegen").setExecutor(new MainCommand());
		plugin.getCommand("cobblegen").setTabCompleter(new MainTabComplete());
		plugin.debug("Commands loaded&2 \u2713");
		
		// Register a enchantment without effects to give items a glow effect
		registerGlow();
        
		// Connect to BStats
		Metrics metrics = new Metrics(this);
        Metrics.SingleLineChart genChart = new Metrics.SingleLineChart("generators", new Callable<Integer>() {
        	
			@Override
			public Integer call() throws Exception {
				return BlockManager.getInstance().getKnownGenLocations().size();
			}
        	
        });
        Metrics.SimplePie pistonChart = new Metrics.SimplePie("servers_using_pistons_for_automation", new Callable<String>() {
        	
			@Override
			public String call() throws Exception {
				return plugin.getConfig().getBoolean("options.automation.pistons") ? "Enabled" : "Disabled";
			}
        	
        });
        metrics.addCustomChart(genChart);
        metrics.addCustomChart(pistonChart);
        
		plugin.debug("CustomCobbleGen is now enabled&2 \u2713");
		double time2 = System.currentTimeMillis();
		double time3 = (time2-time)/1000;
		plugin.debug("Took " + String.valueOf(time3) + " seconds to setup CustomCobbleGen");
	}
	@Override
	public void onDisable(){
		// Unload everything
		
		tierManager.unload();
		this.savePlayerConfig();
    	signManager.saveSignsToFile();
		tierManager = null;
		signManager = null;
		plugin = null;
	}
	
	public void setupHooks() {
		this.connectToPlaceholderAPI();
		this.connectToVault();
		this.connectToIslandPlugin();
	}
	
	public void connectToIslandPlugin() {
		PluginManager pm = Bukkit.getPluginManager();
		if(pm.getPlugin("BentoBox") != null) {
			this.islandPluginHooked = HookType.BENTOBOX;
			plugin.debug("Found BentoBox&2 \u2713");
		}else if(pm.getPlugin("uSkyBlock") != null) {
			this.islandPluginHooked = HookType.USKYBLOCK;
			plugin.debug("Found uSkyBlock&2 \u2713");
		}
	}
	
	
	
	public void connectToPlaceholderAPI() {
		// Connect to PlaceholderAPI
		this.isUsingPlaceholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		if(this.isUsingPlaceholderAPI) {
			new TierPlaceholderExpansion(this).register();
			plugin.debug("Found PlaceholderAPI and registed placeholders&2 \u2713");
		}
	}
	
	public void connectToVault() {
		// Connect to vault
		econManager = EconomyManager.getInstance();
		if(econManager.setupEconomy()) {
			this.debug("Economy is now setup");	
		}else {
			this.debug("Economy is not setup");
		}
	}
	
	public boolean isConnectedToIslandPlugin() {
		return this.islandPluginHooked != null;
	}
	
	public void reloadPlugin() {
		this.reloadConfig();
		this.lang.reload();
		this.reloadPlayerConfig();
		this.reloadSignsConfig();
		signManager.loadSignsFromFile(true);
		tierManager.reload();
		tierManager = TierManager.getInstance();
	}
	
	public void reloadPlayerConfig(){
		if(this.playerConfigFile == null){
			this.playerConfigFile = new File(new File(plugin.getDataFolder(), "Data"),"players.yml");
			this.playerConfig = YamlConfiguration.loadConfiguration(this.playerConfigFile);
			
		}
	}
	 //Return the player config
    public FileConfiguration getPlayerConfig() {
 
        if(this.playerConfigFile == null) this.reloadPlayerConfig();
 
        return this.playerConfig;
 
    }
 
    //Save the player config
    public void savePlayerConfig() {
 
        if(this.playerConfig == null || this.playerConfigFile == null) return;
 
        try {
            this.getPlayerConfig().save(this.playerConfigFile);
        } catch (IOException ex) {
            plugin.getServer().getLogger().log(Level.SEVERE, "Could not save Player config to " + this.playerConfigFile +"!", ex);
        }
 
    }
	
    public void reloadSignsConfig(){
		if(this.signsConfigFile == null){
			this.signsConfigFile = new File(new File(plugin.getDataFolder(), "Data"),"signs.yml");
			this.signsConfig = YamlConfiguration.loadConfiguration(this.signsConfigFile);
			
		}
	}
	 //Return the player config
    public FileConfiguration getSignsConfig() {
 
        if(this.signsConfigFile == null) this.reloadSignsConfig();
 
        return this.signsConfig;
 
    }
 
    //Save the player config
    public void saveSignsConfig() {
 
        if(this.signsConfig == null || this.signsConfigFile == null) return;
 
        try {
            this.getSignsConfig().save(this.signsConfigFile);
        } catch (IOException ex) {
            plugin.getServer().getLogger().log(Level.SEVERE, "Could not save signs config to " + this.signsConfigFile +"!", ex);
        }
 
    }
    
    public void registerGlow() {
    	// Creates a enchantment with no effect, but gives a glow effect on items.
    	
    	if(Enchantment.getByKey(new NamespacedKey(this, "GlowEnchant")) != null) return;
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GlowEnchant glow = new GlowEnchant(new NamespacedKey(this, "GlowEnchant"));
            Enchantment.registerEnchantment(glow);
        }
        catch (IllegalArgumentException e){
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
	private void registerEvents(){
	    PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new BlockEvents(), this);
		pm.registerEvents(new InventoryEvents(), this);
		pm.registerEvents(new PlayerEvents(), this);
	}
	
	public void debug(boolean overrideConfigOption, Object... objects) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object s : objects) {
			if(!first) {
				sb.append(", ");
			}else first = false;
			sb.append("[" + s.getClass().getTypeName() + ": " + s.toString() + "]");
		}
		this.debug(sb.toString());
	}
	
	public void debug(Object... objects) {
		this.debug(false, objects);
	}

	
	public void debug(Boolean booleanObject){
		this.debug(booleanObject.getClass().getTypeName() + ": "+ booleanObject);
	}
	public void debug(String message){
		this.debug(message, false);
	}
	public void debug(String message, boolean overrideConfigOption){
		if(overrideConfigOption == false && plugin.getConfig().getBoolean("debug") == false) return;
		Bukkit.getConsoleSender().sendMessage(("&8[&3&lCustomCobbleGen&8]: &c&lDebug &8-&7 " + message).replaceAll("&", "\u00A7"));
	}
	
	public void log(String message){
		Bukkit.getConsoleSender().sendMessage(("&8[&3&lCustomCobbleGen&8]: &c&lLog &8-&7 " + message).replaceAll("&", "\u00A7"));
	}
	
	//CREDIT TO CryptoMorin
	@Nonnull
    public String getMajorVersion(@Nonnull String version) {
        Validate.notEmpty(version, "Cannot get major Minecraft version from null or empty string");

        // getVersion()
        int index = version.lastIndexOf("MC:");
        if (index != -1) {
            version = version.substring(index + 4, version.length() - 1);
        } else if (version.endsWith("SNAPSHOT")) {
            // getBukkitVersion()
            index = version.indexOf('-');
            version = version.substring(0, index);
        }

        // 1.13.2, 1.14.4, etc...
        int lastDot = version.lastIndexOf('.');
        if (version.indexOf('.') != lastDot) version = version.substring(0, lastDot);

        return version;
    }
	
	//CREDIT TO CryptoMorin
	public boolean serverSupports(int version) {
        return Integer.parseInt(getMajorVersion(Bukkit.getVersion()).substring(2)) >= version;
    }
	
	public static CustomCobbleGen getInstance(){
		return plugin;
	}
}
