/**
 * CustomCobbleGen By @author Philip Flyvholm
 * TierManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Requirements.ItemsRequirement;
import me.phil14052.CustomCobbleGen.Requirements.LevelRequirement;
import me.phil14052.CustomCobbleGen.Requirements.MoneyRequirement;
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Requirements.XpRequirement;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;

public class TierManager {

	private Map<UUID, List<Tier>> purchasedTiers;
	private Map<UUID, Tier> selectedTier;
	private static TierManager instance = null;
	private CustomCobbleGen plugin = null;
	private Map<String, List<Tier>> tiers;
	private BlockManager bm;
	
	public TierManager(){
		plugin = CustomCobbleGen.getInstance();
		bm = BlockManager.getInstance();
		setSelectedTier(new HashMap<UUID, Tier>());
		setPurchasedTiers(new HashMap<UUID, List<Tier>>());
	}
	
	public void loadTiers(){
		if(!plugin.getConfig().contains("tiers")) return;
		tiers = new LinkedHashMap<String, List<Tier>>();
		ConfigurationSection configTiers = plugin.getConfig().getConfigurationSection("tiers");
		for(String tierClass : configTiers.getKeys(false)){
			boolean classNeedsUserChange = false;
			List<Tier> tierLevelsList = new ArrayList<Tier>();
			for(String tierLevelString : configTiers.getConfigurationSection(tierClass).getKeys(false)){
				boolean levelNeedsUserChange = false;
				if(!StringUtils.isInteger(tierLevelString)){plugin.log(tierClass + " has a text as level instead of a number."); continue;}
				ConfigurationSection tierSection = configTiers.getConfigurationSection(tierClass + "." + tierLevelString);
				int tierLevel = Integer.parseInt(tierLevelString);
				String name = tierSection.getString("name");
				Material iconMaterial = Material.matchMaterial(tierSection.getString("icon").toUpperCase());
				if(iconMaterial == null) iconMaterial = Material.COBBLESTONE;
				Map<Material, Double> results = new HashMap<Material, Double>();
				for(String resultMaterialString : tierSection.getConfigurationSection("contains").getKeys(false)){
					Material resultMaterial = Material.matchMaterial(resultMaterialString.toUpperCase());
					if(resultMaterial == null) {
						plugin.log("&c&lUser Error: The material " + resultMaterialString + " under class: "+ tierClass + " tier: " + tierLevel + " is not a material. Check spelling and if outdated material name");
						resultMaterial = Material.COBBLESTONE;
						levelNeedsUserChange = true;
						classNeedsUserChange = true;
					}
					results.put(resultMaterial, tierSection.getDouble("contains." + resultMaterialString));
				}
				List<Requirement> requirements = new ArrayList<Requirement>();
				
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
					HashMap<Material, Integer> priceItems = new HashMap<Material, Integer>();
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
				Tier tier = new Tier(name, tierClass.toUpperCase(), tierLevel, iconMaterial, results, requirements,description);
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
	
	
	public void addUUID(UUID uuid, Tier selectedTier){
		if(!this.selectedTierContainsUUID(uuid)) this.selectedTier.put(uuid, selectedTier);
	}
	
	public void addUUID(UUID uuid, List<Tier> purchasedTiers){
		if(!this.purchasedTiersContainsUUID(uuid)) this.purchasedTiers.put(uuid, purchasedTiers);
	}
	
	public void addUUID(UUID uuid, Tier selectedTier, List<Tier> purchasedTiers){
		if(selectedTier != null) this.addUUID(uuid, selectedTier);
		if(purchasedTiers != null && !purchasedTiers.isEmpty()) this.addUUID(uuid, purchasedTiers);
	}
	
	public void setPlayerSelectedTier(UUID uuid, Tier tier){
		if(this.selectedTierContainsUUID(uuid)) this.selectedTier.remove(uuid);
		this.addUUID(uuid, tier);
	}
	
	public boolean selectedTierContainsUUID(UUID uuid){
		return selectedTier.containsKey(uuid);
	}
	public boolean purchasedTiersContainsUUID(UUID uuid){
		return purchasedTiers.containsKey(uuid);
	}
	
	public Tier getSelectedTier(UUID uuid){
		if(!this.selectedTierContainsUUID(uuid)) return null;
		return getSelectedTierList().get(uuid);
	}
	
	public Map<UUID, Tier> getSelectedTierList() {
		return selectedTier;
	}
	
	public int getTiersSize() {
		int i = 0;
		if(this.getTiers() == null) return 0;
		for(String tierClass : this.getTiers().keySet()) {
			i += this.getTiers().get(tierClass).size();
		}
		return i;
	}
	
	public void loadAllPlayerData() {
		selectedTier = new HashMap<UUID, Tier>();
		purchasedTiers = new HashMap<UUID, List<Tier>>();
		ConfigurationSection playerSection = plugin.getPlayerConfig().getConfigurationSection("players");
		for(String uuid : playerSection.getKeys(false)){
			this.loadPlayerData(UUID.fromString(uuid));
		}
		bm.loadGenPistonData();
	}
	
	public void loadPlayerData(UUID uuid){
		if(this.selectedTierContainsUUID(uuid)) this.selectedTier.remove(uuid);
		if(this.purchasedTiersContainsUUID(uuid)) this.purchasedTiers.remove(uuid);
		if(uuid == null) return;
		Tier tier;
		if(!plugin.getPlayerConfig().contains("players." + uuid)) return; //New Player
		ConfigurationSection playerSection = plugin.getPlayerConfig().getConfigurationSection("players." + uuid);
		if(playerSection.contains("selected")) {
			int tierLevel = playerSection.getInt("selected.level");
			String tierClass = playerSection.getString("selected.class");
			tier = this.getTierByLevel(tierClass, tierLevel);
		}else{
			tier = this.getTierByLevel("DEFAULT", 0);
		}
		List<Tier> purchasedTiers = new ArrayList<Tier>();
		if(playerSection.contains("purchased")) {
				ConfigurationSection purchasedSection = playerSection.getConfigurationSection("purchased");
			for(String purchasedClass : purchasedSection.getKeys(false)){
				List<Integer> purchasedLevels = purchasedSection.getIntegerList(purchasedClass);
				for(int purchasedLevel : purchasedLevels){
					Tier purchasedTier = this.getTierByLevel(purchasedClass, purchasedLevel);
					purchasedTiers.add(purchasedTier);
				}
			}
		}
		this.addUUID(uuid, tier, purchasedTiers);
	}
	
	public void saveAllPlayerData(){
		for(UUID uuid : this.getSelectedTierList().keySet()){
			this.saveSelectedTierPlayerData(uuid);
		}
		for(UUID uuid : this.getPurchasedTiers().keySet()) {
			this.savePurchasedTiersPlayerData(uuid);
		}
		bm.saveGenPistonData();
		plugin.savePlayerConfig();
	}
	
	public void savePlayerData(UUID uuid) {
		this.savePurchasedTiersPlayerData(uuid);
		this.saveSelectedTierPlayerData(uuid);
	}
	
	public void saveSelectedTierPlayerData(UUID uuid) {
		if(this.selectedTierContainsUUID(uuid)) {
			Tier tier = getSelectedTier(uuid);
			plugin.debug("Saving selected tier for " + uuid + ". Currently selected level " + tier.getLevel() + " in class " + tier.getTierClass());
			plugin.getPlayerConfig().set("players." + uuid + ".selected.class", tier.getTierClass());
			plugin.getPlayerConfig().set("players." + uuid + ".selected.level", tier.getLevel());
		}
	}
	
	public void savePurchasedTiersPlayerData(UUID uuid) {
		
		if(this.purchasedTiersContainsUUID(uuid)) {
			List<Tier> purchasedTiers = this.getPurchasedTiers().get(uuid);
			for(Tier purchasedTier : purchasedTiers){
				List<Integer> purchasedLevels = new ArrayList<Integer>();
				if(plugin.getPlayerConfig() == null){
					plugin.log("&cERROR: &7MISSING PLAYER.YML FILE");
					return;
				}
				plugin.debug("Saving purchased tier: ",purchasedTier);
				if(plugin.getPlayerConfig().contains("players." + uuid + ".purchased." + purchasedTier.getTierClass())) {
					purchasedLevels = plugin.getPlayerConfig().getIntegerList("players." + uuid + ".purchased." + purchasedTier.getTierClass());	
				}
				if(!purchasedLevels.contains(purchasedTier.getLevel())) purchasedLevels.add(purchasedTier.getLevel());
				plugin.getPlayerConfig().set("players." + uuid + ".purchased." + purchasedTier.getTierClass(), purchasedLevels);
			}
		}
	}
	
	
	public boolean canPlayerBuyTier(Player p, Tier tier) {
		if(!p.isOnline()) return false;
		if(!tier.getTierClass().equals("DEFAULT") && new PermissionManager().hasPermisson(p, "customcobblegen.generator." + tier.getTierClass(), false) == false) return false;
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
		this.getPlayersPurchasedTiers(p.getUniqueId()).add(tier);
		return true;
	}

	public boolean hasPlayerPurchasedLevel(Player p, Tier tier) {
		if(this.purchasedTiers.size() == 0) {
			this.givePlayerStartPurchases(p);
			return hasPlayerPurchasedLevel(p, tier);
		}
		if(tier.getLevel() <= 0 && tier.getTierClass() == "DEFAULT") return true;
		UUID uuid = p.getUniqueId();
		List<Tier> purchasedTiersByClass = this.getPlayersPurchasedTiersByClass(uuid, tier.getTierClass());
		for(Tier tierI : purchasedTiersByClass) {
			if(tierI.getLevel() == tier.getLevel()) return true;
			else continue;
		}
		return false;
	}
	
	public boolean hasPlayerPurchasedPreviousLevel(Player p, Tier nextTier) {
		UUID uuid = p.getUniqueId();
		if(this.purchasedTiers.size() == 0) {
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
		if(this.selectedTierContainsUUID(uuid)) {
			this.selectedTier.remove(uuid);
		}
		this.setPlayerSelectedTier(uuid, tier);
	}
	
	//If the Player  is new then the Player needs to have a tier they can use. So we give them the default 0
	public void givePlayerStartPurchases(Player p) {
		Tier tier = this.getTierByLevel("DEFAULT", 0);
		UUID uuid = p.getUniqueId();
		if(this.purchasedTiersContainsUUID(uuid)) {
			this.purchasedTiers.remove(uuid);
		}
		List<Tier> startList = new ArrayList<Tier>();
		startList.add(tier);
		this.purchasedTiers.put(uuid, startList);
		this.purchaseTier(p, tier, true);
		
	}
	
	public void unload() {
		this.saveAllPlayerData();
		purchasedTiers = null;
		selectedTier = null;
		tiers = null;
	}
	public void load() {
		setSelectedTier(new HashMap<UUID, Tier>());
		setPurchasedTiers(new HashMap<UUID, List<Tier>>());
		tiers = new HashMap<String, List<Tier>>();
		this.loadTiers();
		this.loadAllPlayerData();
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
		if(!this.getPurchasedTiers().containsKey(uuid)) {
			List<Tier> emptyTierList = new ArrayList<Tier>();
			return emptyTierList;
		}
		return this.getPurchasedTiers().get(uuid);
	}
	
	public List<Tier> focusListOnClass(List<Tier> list, String tierClass) {
		List<Tier> newList = new ArrayList<Tier>();
		for(Tier tier : list) {
			if(!tier.getTierClass().equals(tierClass)) continue;
			newList.add(tier);
		}
		return newList;
	}
	
	public Map<UUID, List<Tier>> getPurchasedTiers() {
		return purchasedTiers;
	}

	public void setPurchasedTiers(Map<UUID, List<Tier>> purchasedTiers) {
		this.purchasedTiers = purchasedTiers;
	}

	public Map<UUID, Tier> getSelectedTier() {
		return selectedTier;
	}

	public void setSelectedTier(Map<UUID, Tier> selectedTier) {
		this.selectedTier = selectedTier;
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
	
	
}
