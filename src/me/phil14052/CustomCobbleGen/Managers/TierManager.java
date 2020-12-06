/**
 * CustomCobbleGen By @author Philip Flyvholm
 * TierManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Requirements.*;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import me.phil14052.CustomCobbleGen.databases.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class TierManager {
	
	private static TierManager instance = null;
	private CustomCobbleGen plugin = null;
	private Map<String, List<Tier>> tiers;
	private final PermissionManager pm;
	private final GeneratorModeManager gm;
	private int task = -1;

	private final BukkitScheduler scheduler;

	public TierManager(){
		plugin = CustomCobbleGen.getInstance();
		scheduler = Bukkit.getServer().getScheduler();
		pm = new PermissionManager();
		gm = GeneratorModeManager.getInstance();
	}
	
	public void loadTiers(){
		if(!plugin.getConfig().contains("tiers")) return;
		tiers = new LinkedHashMap<>();
		ConfigurationSection configTiers = plugin.getConfig().getConfigurationSection("tiers");
		if(configTiers == null || configTiers.getKeys(false) == null){
			plugin.error("Tiers are not defined", true);
			return;
		}
		for(String tierClass : configTiers.getKeys(false)){
			boolean classNeedsUserChange = false;
			List<Tier> tierLevelsList = new ArrayList<>();
			for(String tierLevelString : configTiers.getConfigurationSection(tierClass).getKeys(false)){
				boolean levelNeedsUserChange = false;
				if(!StringUtils.isInteger(tierLevelString)){plugin.error(tierClass + " has a text as level instead of a number.", true); continue;}
				ConfigurationSection tierSection = configTiers.getConfigurationSection(tierClass + "." + tierLevelString);
				int tierLevel = Integer.parseInt(tierLevelString);
				String name = tierSection.getString("name");
				Material iconMaterial = Material.matchMaterial(tierSection.getString("icon").toUpperCase());
				if(iconMaterial == null) iconMaterial = Material.COBBLESTONE;
				Map<Material, Double> results = new HashMap<>();
				double totalPercentage = 0D;
				for(String resultMaterialString : tierSection.getConfigurationSection("contains").getKeys(false)){
					Material resultMaterial = Material.matchMaterial(resultMaterialString.toUpperCase());
					if(resultMaterial == null) {
						plugin.error("The material " + resultMaterialString + " under class: "+ tierClass + " tier: " + tierLevel + " is not a material. Check spelling and if outdated material name", true);
						resultMaterial = Material.COBBLESTONE;
						levelNeedsUserChange = true;
						classNeedsUserChange = true;
					}
					double percentage = tierSection.getDouble("contains." + resultMaterialString);
					totalPercentage += percentage;
					results.put(resultMaterial, percentage);
				}
				if(totalPercentage > 100D) {
					plugin.warning("&c&lUser Error: &7Results total percentage is over 100% in the &e" + name + "&c&l tier. Total percentage = &e" + totalPercentage);
				}else if(totalPercentage < 100D) {
					plugin.error("Results total percentage is under 100% in the &e" + name + "&c&l tier. Total percentage = &e" + totalPercentage, true);
					plugin.error("&c&lTHIS CAN GIVE NULL POINTER ERRORS! THESE ARE USER ERRORS AND NEED TO BE FIXED BY YOU!", true);
				}
				
				List<Requirement> requirements = new ArrayList<>();
				
				if(tierSection.contains("price.money")) {
					int priceMoney = tierSection.getInt("price.money");
					if(priceMoney > 0) {
						requirements.add(new MoneyRequirement(priceMoney));
					}
				}
				if(tierSection.contains("price.xp")) {
					int priceXp = tierSection.getInt("price.xp");

					if(priceXp > 0) {
						requirements.add(new XpRequirement(priceXp));
					}
				}
				if(tierSection.contains("price.items")) {
					HashMap<Material, Integer> priceItems = new HashMap<>();
					for(String itemMaterial : tierSection.getConfigurationSection("price.items").getKeys(false)) {
						Material m = Material.getMaterial(itemMaterial.toUpperCase());
						if(m == null) continue;
						priceItems.put(m, tierSection.getInt("price.items." + itemMaterial));
					}

					if(priceItems != null && priceItems.size() > 0) {
						requirements.add(new ItemsRequirement(priceItems));
					}
				}
				
				if(tierSection.contains("price.level")) {
					int levelRequirement = tierSection.getInt("price.level");

					if(levelRequirement > 0) {
						requirements.add(new LevelRequirement(levelRequirement));
					}
				}
				
				List<String> description = null;
				if(tierSection.contains("description")) {
					description = new ArrayList<>();
					List<String> rawDescription = tierSection.getStringList("description");
					for(String s : rawDescription) {
						s = Lang.color(s);
						description.add(s);
					}
					
				}
				
				String permission = null;
				if(tierSection.contains("permission")) {
					permission = tierSection.getString("permission");
				}
				GenMode supportedMode = null;
				if(tierSection.contains("supportedGenerationMode")) {
					String supportedGenerationModeString = tierSection.getString("supportedGenerationMode");
					if(supportedGenerationModeString != null && !supportedGenerationModeString.equalsIgnoreCase("ALL")) {

						try {
							int id = Integer.parseInt(tierSection.getString("supportedGenerationMode"));
							supportedMode = gm.getModeById(id);
						}catch(NumberFormatException e) {
							plugin.error(supportedGenerationModeString + " is not a valid generation mode id. MOST BE A NUMBER or \"ALL\" - Fallback: Will allow all generators", true);
						}
					}
				}
				
				Tier tier = new Tier(name, tierClass.toUpperCase(), tierLevel, iconMaterial, results, requirements,description, permission, supportedMode);
				if(Setting.GUI_CUSTOM_GUI_ENABLED.getBoolean() && tierSection.contains("slot")){
					int slot = tierSection.getInt("slot");
					if(slot < 0){
						plugin.error("GUI Slot for " + tier.getName() + " needs to be positive", true);
					}else{
						if(slot > Setting.GUI_CUSTOM_GUI_SIZE.getInt()){
							plugin.warning("GUI Slot for " + tier.getName() + " may be bigger than the GUI Size and therefore not be able to be displayed.");
						}
						tier.setGUISlot(slot);
					}
				}
				try {
					//If already defined override
					tierLevelsList.set(tierLevel, tier);
				} catch(IndexOutOfBoundsException e) {
					//If not defined then add
					tierLevelsList.add(tier);
				}
				if(!levelNeedsUserChange) plugin.debug("&aSuccessfully loaded level " + tierLevel + " under class " + tierClass);
				else plugin.debug("&cUser error in level " + tierLevel + " under class " + tierClass);
			}
			tiers.put(tierClass.toUpperCase(), tierLevelsList);
			if(!classNeedsUserChange)plugin.debug("&aSuccessfully loaded class " + tierClass);
			else plugin.debug("&cUser error in class " + tierClass);
		}
	}

	public void setPlayerSelectedTiers(UUID uuid, SelectedTiers selectedTiers){
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		plugin.getPlayerDatabase().getPlayerData(uuid).setSelectedTiers(selectedTiers);
	}
	
	public boolean doesDatabaseContainUUID(UUID uuid) {
		if(uuid == null) return false;
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		return plugin.getPlayerDatabase().containsPlayerData(uuid);
	}
	
	
	public SelectedTiers getSelectedTiers(UUID uuid){
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		if(uuid == null) {
			return null;
		}
		if(!this.doesDatabaseContainUUID(uuid)) return null;
		return plugin.getPlayerDatabase().getPlayerData(uuid).getSelectedTiers();
	}
	
	public int getTiersSize() {
		int i = 0;
		if(this.getTiers() == null) return 0;
		for(String tierClass : this.getTiers().keySet()) {
			i += this.getTiers().get(tierClass).size();
		}
		return i;
	}

	
	public boolean canPlayerBuyTier(Player p, Tier tier) {
		if(!p.isOnline()) return false;
		
		if(!tier.getTierClass().equals("DEFAULT") && !pm.hasPermission(p, "customcobblegen.generator." + tier.getTierClass(), false)) return false;
		if(tier.hasCustomPermission() && !pm.hasPermission(p, tier.getCustomPermission(), false)) return false;
		for(Requirement r : tier.getRequirements()) {
			if(!r.furfillsRequirement(p)) return false;
		}
		return true;
	}
	
	public boolean purchaseTier(Player p, Tier tier) {
		return this.purchaseTier(p, tier, false);
	}
	
	public boolean purchaseTier(Player p, Tier tier, boolean forceBuy) {
		//Check if player can afford the tier and check if the have bought the previous level
		if(!forceBuy && !canPlayerBuyTier(p, tier)) return false;
		if(!forceBuy && !hasPlayerPurchasedPreviousLevel(p, tier)) return false;
		for(Requirement r : tier.getRequirements()) {
			r.onPurchase(p);
		}
		//Buy the tier
		if(this.hasPlayerPurchasedLevel(p, tier)) return false;
		UUID uuid = p.getUniqueId();
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		plugin.getPlayerDatabase().getPlayerData(uuid).addPurchasedTier(tier);
		//Save the players.yml file so players don't repay
		if(Setting.SAVEONTIERPURCHASE.getBoolean()) plugin.getPlayerDatabase().saveToDatabase(uuid);;
		return true;
	}
	
	public void withdrawPurchasedTier(UUID uuid, Tier tier) {
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
		}
		if(this.getPlayersPurchasedTiers(uuid).contains(tier)) plugin.getPlayerDatabase().getPlayerData(uuid).removePurchasedTier(tier);
	}

	public boolean hasPlayerPurchasedLevel(Player p, Tier tier) {
		if(plugin.getPlayerDatabase().getPlayerData(p.getUniqueId()).getPurchasedTiers().isEmpty()) {
			this.givePlayerStartPurchases(p);
			return hasPlayerPurchasedLevel(p, tier);
		}
		if(tier == null) return false;
		if(tier.getLevel() <= 0 && tier.getTierClass().equals("DEFAULT")) return true;
		UUID uuid = p.getUniqueId();
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		List<Tier> purchasedTiersByClass = this.getPlayersPurchasedTiersByClass(uuid, tier.getTierClass());
		if(purchasedTiersByClass == null) {
			plugin.warning("Unknown tier purchases from - " + p.getName());
			return false;
		}
		for(Tier tierI : purchasedTiersByClass) {
			if(tierI.getLevel() == tier.getLevel()) return true;
			else continue;
		}
		return false;
	}
	
	public boolean hasPlayerPurchasedPreviousLevel(Player p, Tier nextTier) {
		if(!Setting.PREVIOUS_TIER_NEEDED.getBoolean()) return true;
		UUID uuid = p.getUniqueId();
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		if(plugin.getPlayerDatabase().getPlayerData(p.getUniqueId()).getPurchasedTiers().isEmpty()) {
			this.givePlayerStartPurchases(p);
			return hasPlayerPurchasedPreviousLevel(p, nextTier);
		}
		if(nextTier.getLevel() <= 0) return true;
		List<Tier> purchasedTiersByClass = this.getPlayersPurchasedTiersByClass(uuid, nextTier.getTierClass());
		if(purchasedTiersByClass.size() <= 0) return false;
		for(Tier tier : purchasedTiersByClass) {
			if((nextTier.getLevel() -1 ) == tier.getLevel()) return true;
		}
		return false;
		
	}
	
	public void givePlayerStartSelect(UUID uuid) {
		Tier tier = this.getTierByLevel("DEFAULT", 0);
		SelectedTiers selectedTiers = new SelectedTiers(uuid, tier);
		this.setPlayerSelectedTiers(uuid, selectedTiers);
	}
	
	//If the Player  is new then the Player needs to have a tier they can use. So we give them the default 0
	public void givePlayerStartPurchases(Player p) {
		Tier tier = this.getTierByLevel("DEFAULT", 0);
		UUID uuid = p.getUniqueId();
		List<Tier> startList = new ArrayList<Tier>();
		startList.add(tier);
		plugin.getPlayerDatabase().getPlayerData(uuid).setPurchasedTiers(startList);
		this.purchaseTier(p, tier, true);
		
	}
	
	public void unload() {
		tiers = null;
	}
	public void load() {
		tiers = new HashMap<>();
		this.loadTiers();
	}
	
	public void reload() {
		unload();
		load();
	}
	
	public static TierManager getInstance(){
		if(instance == null) instance = new TierManager();
		return instance;
	}
	
	public List<Tier> getPlayersPurchasedTiersByClass(UUID uuid, String tierClass) {
		return this.focusListOnClass(this.getPlayersPurchasedTiers(uuid), tierClass);
	}
	
	public List<Tier> getPlayersPurchasedTiers(UUID uuid) {

		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			UUID ownerUUID = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
			if(ownerUUID != null) uuid = ownerUUID;
		}
		PlayerData data = plugin.getPlayerDatabase().getPlayerData(uuid);
		if(data == null) return new ArrayList<>();
		return data.getPurchasedTiers();
	}
	
	public List<Tier> focusListOnClass(List<Tier> list, String tierClass) {
		List<Tier> newList = new ArrayList<Tier>();
		if(list == null || list.isEmpty()) return newList;
		for(Tier tier : list) {
			if(tier == null || tier.getTierClass() == null) continue;
			if(!tier.getTierClass().equals(tierClass)) continue;
			newList.add(tier);
		}
		return newList;
	}

	public Tier getTierByLevel(String tierClass, int tierLevel){
		if(!this.getTiers().containsKey(tierClass)) return null;
		List<Tier> levelList = this.getTiers().get(tierClass);
		if(levelList.size()-1 < tierLevel) return null;
		return levelList.get(tierLevel);
	}
	
	public Map<String, List<Tier>> getTiers() {
		return tiers;
	}

	public void setTiers(Map<String, List<Tier>> tiers) {
		this.tiers = tiers;
	}

	public boolean isAutoSaveActive(){
		if(task < 0) return false;
		return scheduler.isCurrentlyRunning(task);
	}

	public void startAutoSave(){
		if(this.isAutoSaveActive()) this.stopAutoSave();
		task = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				plugin.log("Auto saving player data...");
				long time1 = System.currentTimeMillis();
				plugin.getPlayerDatabase().saveEverythingToDatabase();
				long time2 = System.currentTimeMillis();
				double time3 = Double.longBitsToDouble(time2-time1)/1000;
				plugin.log("Player data has been saved. It took: " + time3 + " seconds");
			}
		}, 0L, Setting.AUTO_SAVE_DELAY.getInt()*20L);
	}
	public void stopAutoSave(){
		if(task < 0) return;
		scheduler.cancelTask(task);
	}

	
}
