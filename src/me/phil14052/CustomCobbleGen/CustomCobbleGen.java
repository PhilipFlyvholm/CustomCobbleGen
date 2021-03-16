package me.phil14052.CustomCobbleGen;


import com.cryptomorin.xseries.XMaterial;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Commands.MainCommand;
import me.phil14052.CustomCobbleGen.Commands.MainTabComplete;
import me.phil14052.CustomCobbleGen.Events.BlockEvents;
import me.phil14052.CustomCobbleGen.Events.MinionEvents;
import me.phil14052.CustomCobbleGen.Events.PlayerEvents;
import me.phil14052.CustomCobbleGen.Files.Files;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Files.updaters.ConfigUpdater;
import me.phil14052.CustomCobbleGen.Files.updaters.LangFileUpdater;
import me.phil14052.CustomCobbleGen.Files.updaters.SignsFileUpdater;
import me.phil14052.CustomCobbleGen.GUI.InventoryEvents;
import me.phil14052.CustomCobbleGen.Hooks.*;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
import me.phil14052.CustomCobbleGen.Managers.GeneratorModeManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Signs.SignManager;
import me.phil14052.CustomCobbleGen.Utils.GlowEnchant;
import me.phil14052.CustomCobbleGen.Utils.Metrics.Metrics;
import me.phil14052.CustomCobbleGen.Utils.TierPlaceholderExpansion;
import me.phil14052.CustomCobbleGen.databases.MySQLPlayerDatabase;
import me.phil14052.CustomCobbleGen.databases.PlayerDatabase;
import me.phil14052.CustomCobbleGen.databases.YamlPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * CustomCobbleGen.java
 */
public class CustomCobbleGen extends JavaPlugin {

	private static CustomCobbleGen plugin;
	public Files lang;
	private PlayerDatabase playerDatabase;
	private FileConfiguration signsConfig;
	private File signsConfigFile;
	private TierManager tierManager;
	private SignManager signManager;
	private GeneratorModeManager generatorModeManager;
	public boolean isUsingPlaceholderAPI = false;
	public static IslandHook islandPluginHooked = null;
	private static String connectedMinionPlugin = "None";
	private static String connectedIslandPlugin = "None";
	private final String CONSOLEPREFIX = "&8[&3&lCustomCobbleGen&8]: ";
	
	@Override
	public void onEnable(){
		double time = System.currentTimeMillis();
		plugin = this;
		tierManager = TierManager.getInstance();
		signManager = SignManager.getInstance();
		Setting.setFile(plugin.getConfig());
		new ConfigUpdater();
		saveConfig();
		plugin.debug("Enabling CustomCobbleGen plugin");
		plugin.log("&cIF YOU ENCOUNTER ANY BUGS OR ERRORS PLEASE REPORT THEM ON SPIGOT!");
		plugin.log("&8Special thanks to lelesape (Idea), AddstarMC (Contribution on GitHub) and Fang_Zhijian (Chinese translation)"); // If you contribute to the plugin please add yourself here :D (As a thank you from me)
		plugin.log("&6&lIF YOU WANT TO SUPPORT US JOIN OUR PATERON: https://www.patreon.com/woollydevelopment");
		// Setup config
		generatorModeManager = GeneratorModeManager.getInstance();
		generatorModeManager.loadFromConfig();
		this.debug("The config is now setup&2 \u2713");
		// Setup player database
		setupPlayerDatabase();
		
		// Setup lang file
		lang = new Files(this, "lang.yml");
		new LangFileUpdater(plugin);
		Lang.setFile(lang);
		this.debug("Lang is now setup&2 \u2713");
		// Setup tiers
		tierManager.load();
		this.debug("Tiers is now setup&2 \u2713");
		// Setup signs configs
		signsConfig = null;
		signsConfigFile = null;
		new SignsFileUpdater(plugin);
		signManager.loadSignsFromFile(true);
		this.debug("Signs is now setup&2 \u2713");
		plugin.getPlayerDatabase().loadEverythingFromDatabase();
		
		this.setupHooks();

		if(Setting.AUTO_SAVE_ENABLED.getBoolean()){
			tierManager.startAutoSave();
			plugin.debug("Auto saver started&2 \u2713");
		}

		registerEvents();
		plugin.debug("Events loaded&2 \u2713");
		PluginCommand mainCommand = plugin.getCommand("cobblegen");
		if(mainCommand != null){
			mainCommand.setExecutor(new MainCommand());
			mainCommand.setTabCompleter(new MainTabComplete());
			plugin.debug("Commands loaded&2 \u2713");
		}else{
			plugin.error("Failed load of command");
		}
		
		// Register a enchantment without effects to give items a glow effect
		registerGlow();
        
		// Connect to BStats
		int pluginId = 5454;
		Metrics metrics = new Metrics(this, pluginId);
        Metrics.SingleLineChart genChart = new Metrics.SingleLineChart("generators", () -> {
			int numOfGenerators = BlockManager.getInstance().getKnownGenLocations().size();
			if(numOfGenerators > 10000) { // Over 10000 generators found - Prob a mistake
				plugin.warning("&c&lOver 10.000 generators in use. If you believe this is a mistake, then contact the dev (phil14052 on SpigotMC.org)");
				plugin.warning("&cQuick link: https://www.spigotmc.org/conversations/add?to=phil14052&title=CCG%20Support:%20" + numOfGenerators + "%20generators%20are%20active%20on%20my%20server");
			}
			return numOfGenerators;
		});
        Metrics.SimplePie pistonChart = new Metrics.SimplePie("servers_using_pistons_for_automation", () -> Setting.AUTOMATION_PISTONS.getBoolean() ? "Enabled" : "Disabled");
        Metrics.SimplePie signChart = new Metrics.SimplePie("servers_using_signs", () -> Setting.SIGNS_ENABLED.getBoolean() ? "Enabled" + (signManager.getSigns().isEmpty() ? " but not in use" : "") : "Disabled");
        Metrics.SimplePie minionChart = new Metrics.SimplePie("connected_minion_plugins", () -> connectedMinionPlugin);
        Metrics.SimplePie islandChart = new Metrics.SimplePie("connected_island_plugins", () -> connectedIslandPlugin);

        Metrics.SimplePie selectOptionChart = new Metrics.SimplePie("tier_unlock_system", () -> Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() ? "Island based" : "Player based");
        
        Metrics.SimplePie tiersActiveChart = new Metrics.SimplePie("tiers_active", () -> {
			int numOfTiers = 0;
			for(List<Tier> tiers : tierManager.getTiers().values()) {
				numOfTiers += tiers.size();
			}
			return numOfTiers + " tiers active";
		});
        
        Metrics.SimplePie modesActiveChart = new Metrics.SimplePie("modes_active", () -> generatorModeManager.getModes().size() + " generation modes active");
        metrics.addCustomChart(genChart);
        metrics.addCustomChart(pistonChart);
        metrics.addCustomChart(signChart);
        metrics.addCustomChart(minionChart);
        metrics.addCustomChart(islandChart);
        metrics.addCustomChart(selectOptionChart);
        metrics.addCustomChart(tiersActiveChart);
        metrics.addCustomChart(modesActiveChart);
        
		plugin.log("CustomCobbleGen is now enabled&2 \u2713");
		double time2 = System.currentTimeMillis();
		double time3 = (time2-time)/1000;
		plugin.debug("Took " + String.valueOf(time3) + " seconds to setup CustomCobbleGen");
	}
	@Override
	public void onDisable(){
		// Unload everything
		
		tierManager.unload();
		if(tierManager.isAutoSaveActive()) tierManager.stopAutoSave();
		if(this.getPlayerDatabase() != null) {
			this.getPlayerDatabase().saveEverythingToDatabase();
		}
    	signManager.saveSignsToFile();
		tierManager = null;
		signManager = null;
		plugin = null;
	}
	
	private void setupPlayerDatabase() {
		switch (Setting.DATABASE_TYPE.getString().toUpperCase()) {
			case "YAML":
				this.playerDatabase = new YamlPlayerDatabase();
				break;
			case "YML":
				this.playerDatabase = new YamlPlayerDatabase();
				break;
			case "MYSQL":
				this.playerDatabase = new MySQLPlayerDatabase();
				break;
			default:
				plugin.error("Unknown database type. Will use YAML", true);
				this.playerDatabase = new YamlPlayerDatabase();
				break;
		}
		plugin.debug("Setting up a " + this.playerDatabase.getType() + " player database");
		try {
			this.playerDatabase.establishConnection();
		}catch(Exception e) {
			plugin.error("FAILED SETTING UP " + this.playerDatabase.getType() + " PLAYER DATABASE. DISABLING PLUGIN!");
			plugin.error(e.getLocalizedMessage());
			for(StackTraceElement s : e.getCause().getStackTrace()) {
				plugin.error(s.toString());
			}
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	private void setupHooks() {
		this.connectToPlaceholderAPI();
		this.connectToVault();
		connectToIslandPlugin();
		
	}
	
	public static void connectToIslandPlugin() {
		PluginManager pm = Bukkit.getPluginManager();
		if(pm.getPlugin("BentoBox") != null) {
			islandPluginHooked = new BentoboxHook();
		}else if(pm.getPlugin("uSkyBlock") != null) {
			islandPluginHooked = new uSkyBlockHook();
		}else if(pm.getPlugin("FabledSkyBlock") != null) {
			islandPluginHooked = new FabledHook();
		}else if(pm.getPlugin("ASkyBlock") != null) {
			islandPluginHooked = new ASkyBlockHook();
		}else if(pm.getPlugin("SuperiorSkyblock2") != null) {
			islandPluginHooked = new SuperiorSkyblock2Hook();
		}
		if(islandPluginHooked != null) {
			connectedIslandPlugin = islandPluginHooked.getHookName();
			plugin.debug("Found " + islandPluginHooked.getHookName() + "&2 \u2713");
			if(!islandPluginHooked.supportsIslandBalance()
				&& Setting.ISLANDS_USEISLANDBALANCE.getBoolean()) {
				plugin.error("Option 'options -> islands -> useIslandBalance' has been selected in the config, but " +  islandPluginHooked.getHookName() + " does not support island balances. Using player balances instead");
			}
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
		EconomyManager econManager = EconomyManager.getInstance();
		if(econManager.setupEconomy()) {
			this.debug("Economy is now setup");	
		}else {
			this.debug("Economy is not setup");
		}
	}
	
	public boolean isConnectedToIslandPlugin() {
		return islandPluginHooked != null;
	}
	
	public IslandHook getIslandHook() {
		return islandPluginHooked;
	}
	
	public void reloadPlugin() {
		if(tierManager.isAutoSaveActive()) tierManager.stopAutoSave();
		this.reloadConfig();
		Setting.setFile(this.getConfig());
		this.lang.reload();
		this.getPlayerDatabase().reloadConnection();
		this.reloadSignsConfig();
		generatorModeManager.loadFromConfig();
		signManager.loadSignsFromFile(true);
		tierManager.reload();
		tierManager = TierManager.getInstance();
		if(Setting.AUTO_SAVE_ENABLED.getBoolean()) tierManager.startAutoSave();
	}
	
	public PlayerDatabase getPlayerDatabase() {
		return this.playerDatabase;
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
    	// Creates a enchantment with no effect, but gives a glow effect on items. Pre 1.13 makes this a mess. So it disabled
    	if(XMaterial.supports(13)) {

        	if(Enchantment.getByKey(new NamespacedKey(this, "GlowEnchant")) != null) return;
            try {
                Field f = Enchantment.class.getDeclaredField("acceptingNew");
                f.setAccessible(true);
                f.set(null, true);
            }
            catch (Exception e) {
            	plugin.error("Failed to create enchament: " + e.getMessage());
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
    }
    
    
	private void registerEvents(){
	    PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new BlockEvents(), this);
		pm.registerEvents(new InventoryEvents(), this);
		pm.registerEvents(new PlayerEvents(), this);
		if(pm.getPlugin("JetsMinions") != null) {
			plugin.debug("Found JetsMinions");
			connectedMinionPlugin = "JetsMinions";
			pm.registerEvents(new MinionEvents(), this);
		}
	}
	
	public void debug(boolean overrideConfigOption, Object... objects) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object s : objects) {
			if(!first) {
				sb.append(", ");
			}else first = false;
			if(s == null) {
				sb.append("NULL");
			}else if(s instanceof String) {
				sb.append((String) s);
			}else {
				sb.append("[" + s.getClass().getTypeName() + ": " + s.toString() + "]");	
			}
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
		if(!overrideConfigOption && !Setting.DEBUG.getBoolean()) return;
		Bukkit.getConsoleSender().sendMessage(("&8[&3&lCustomCobbleGen&8]: &c&lDebug &8-&7 " + message).replace("&", "\u00A7"));
	}
	
	public void log(Object... objects) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object s : objects) {
			if(!first) {
				sb.append(", ");
			}else first = false;
			if(s == null) {
				sb.append("NULL");
			}else if(s instanceof String) {
				sb.append((String) s);
			}else {
				sb.append("[" + s.getClass().getTypeName() + ": " + s.toString() + "]");	
			}
		}
		this.log(sb.toString());
	}
	
	public void log(String message){
		Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&8&lLog &8-&7 " + message).replace("&", "\u00A7"));
	}
	
	public void error(String message) {
		this.error(message, false);
	}
	
	public void error(String message, boolean userError) {
		if(userError) {
			Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&4&lUser error &8-&c " + message).replace("&", "\u00A7"));
		}else {

			Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&4&lError &8-&c " + message).replace("&", "\u00A7"));
		}
	}
	
	public void warning(String message) {
		Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&4&lWarning &8-&7 " + message).replace("&", "\u00A7"));
	}
	
	
	public static CustomCobbleGen getInstance(){
		return plugin;
	}
}
