/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Settings.java
 */
package me.phil14052.CustomCobbleGen.Files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author Philip
 *
 */
public enum Setting {

	DEBUG("debug", false),
	SECTION_GENERATIONMODES("options.generationModes", "", true),
	PLAYERSEARCHRADIUS("options.playerSearchRadius", 4D),
	ONLY_LOAD_ONLINE_PLAYERS("options.ignoreOfflinePlayersOnLoad", false),
	AUTO_SAVE_ENABLED("options.auto-save.enabled", false),
	AUTO_SAVE_DELAY("options.auto-save.delay-in-sec", 300), //300 sec = 5 min
	MONEY_FORMAT("options.money.format", true),
	PREVIOUS_TIER_NEEDED("options.previous-tier-needed", true),
	GUI_PERMISSIONNEEDED("options.gui.permissionNeeded", false),
	GUI_SHOWBARRIERBLOCKIFLOCKED("options.gui.showBarrierBlockIfLocked", true),
	GUI_HIDEINFOIFLOCKED("options.gui.hideInfoIfLocked", false),
	GUI_CONFIRMPURCHASES("options.gui.confirmpurchases", true),
	GUI_ADMINGUI("options.gui.admingui", true),
	GUI_SEPERATECLASSESBYLINES("options.gui.seperateClassesByLines", true),
	GUI_CENTERTIERS("options.gui.centerTiers", true),
	GUI_SHOWSUPPORTEDMODES("options.gui.showSupportedModes", true),
	GUI_CUSTOM_GUI_ENABLED("options.gui.custom.enabled", false),
	GUI_CUSTOM_GUI_SIZE("options.gui.custom.size", 27),
	SIGNS_ENABLED("options.signs.enabled", true),
	ISLANDS_USEPERISLANDUNLOCKEDGENERATORS("options.islands.usePerIslandUnlockedGenerators", false),
	ISLANDS_SENDMESSAGESTOTEAM("options.islands.sendMessagesToTeam", true),
	ISLANDS_ONLYOWNER_BUY("options.islands.onlyOwnerCan.buy", false),
	ISLANDS_ONLYOWNER_SELECT("options.islands.onlyOwnerCan.select", false),
	ISLANDS_USEISLANDBALANCE("options.islands.useIslandBalance", false),
	AUTOMATION_PISTONS("options.automation.pistons", false),
	SUPPORTWATERLOGGEDBLOCKS("options.supportWaterloggedBlocks", true),
	SAVEONTIERPURCHASE("options.saveOnTierPurchase", true),
	REMOVEGRAVITY("options.removeGravity", false),
	DISABLEDWORLDS("options.disabled.worlds", new String[] {"world_the_end"}),
	DATABASE_TYPE("options.database.type", "YAML"),
	DATABASE_HOST("options.database.host", "localhost:8080"),
	DATABASE_DATABASE("options.database.database", "database_name"),
	DATABASE_TABLE("options.database.table", "CCG_PLAYERS"),
	DATABASE_USERNAME("options.database.username", "admin"),
	DATABASE_PASSWORD("options.database.password", ""),
	SECTION_TIERS("tiers", "", true);

	private final String path;
	private final Object defaultValue;
	private final boolean section;
	private static FileConfiguration CONFIG;

	Setting(String path, Object defaultValue) {
		this(path, defaultValue, false);
	}
	Setting(String path, Object defaultValue, boolean isSection){
		this.path = path;
		this.defaultValue = defaultValue;
		this.section = isSection;
	}

	/**
	 * Set the {@code FileConfiguration} to use.
	 * 
	 * @param config The config to set.
	 */
	public static void setFile(FileConfiguration config) {
		CONFIG = config;
	}
	public static boolean isConfigSet() {
		return CONFIG != null;
	}

	public String getPath() {
		return this.path;
	}

	public Object getDefaultValue() {
		return this.defaultValue;
	}

	public boolean getBoolean() {
		return CONFIG.getBoolean(this.getPath());
	}

	public List<Boolean> getBooleanList() {
		return CONFIG.getBooleanList(this.getPath());
	}
	
	public String getString() {
		return CONFIG.getString(this.getPath());
	}
	public List<String> getStringList() {
		return CONFIG.getStringList(this.getPath());
	}
	
	public int getInt() {
		return CONFIG.getInt(this.getPath());
	}
	public List<Integer> getIntegerList() {
		return CONFIG.getIntegerList(this.getPath());
	}
	public Double getDouble() {
		return CONFIG.getDouble(this.getPath());
	}
	
	public List<Double> getDoubleList() {
		return CONFIG.getDoubleList(this.getPath());
	}
	
	public List<Float> getFloatList() {
		return CONFIG.getFloatList(this.getPath());
	}
	
	public ConfigurationSection getConfigurationSection() {
		return CONFIG.getConfigurationSection(this.getPath());
	}
	public boolean isSection() {
		return section;
	}
}
