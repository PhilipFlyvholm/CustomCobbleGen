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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;

public class TierManager {

	private Map<Player, List<Tier>> purchasedTiers;
	private Map<Player, Tier> selectedTier;
	private static TierManager instance = null;
	private CustomCobbleGen plugin = null;
	private Map<String, List<Tier>> tiers;
	private EconomyManager econManager;
	
	public TierManager(){
		plugin = CustomCobbleGen.getInstance();
		econManager = EconomyManager.getInstance();
		setSelectedTier(new HashMap<Player, Tier>());
		setPurchasedTiers(new HashMap<Player, List<Tier>>());
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
				Map<Material, Integer> results = new HashMap<Material, Integer>();
				for(String resultMaterialString : tierSection.getConfigurationSection("contains").getKeys(false)){
					Material resultMaterial = Material.matchMaterial(resultMaterialString.toUpperCase());
					if(resultMaterial == null) {
						plugin.log("§c§lUser Error: The material " + resultMaterialString + " under class: "+ tierClass + " tier: " + tierLevel + " is not a material. Check spelling and if outdated material name");
						resultMaterial = Material.COBBLESTONE;
						levelNeedsUserChange = true;
						classNeedsUserChange = true;
					}
					results.put(resultMaterial, tierSection.getInt("contains." + resultMaterialString));
				}
				int priceMoney = 0;
				int priceXp = 0;
				if(tierSection.contains("price.money")) priceMoney = tierSection.getInt("price.money");
				if(tierSection.contains("price.xp")) priceXp = tierSection.getInt("price.xp");
				Tier tier = new Tier(name, tierClass.toUpperCase(), tierLevel, iconMaterial, results, priceMoney, priceXp);
				try {
					//If already defined override
					tierLevelsList.set(tierLevel, tier);
				} catch(IndexOutOfBoundsException e) {
					//If not defined then add
					tierLevelsList.add(tier);
				}
				if(!levelNeedsUserChange) plugin.debug("§aSuccessfully loaded level " + tierLevel + " under class " + tierClass);
				else plugin.debug("§cUser error in level " + tierLevel + " under class " + tierClass);
			}
			tiers.put(tierClass.toUpperCase(), tierLevelsList);
			if(!classNeedsUserChange)plugin.debug("§aSuccessfully loaded class " + tierClass);
			else plugin.debug("§cUser error in class " + tierClass);
		}
	}
	
	
	public void addPlayer(Player p, Tier selectedTier){
		if(!this.selectedTierContainsPlayer(p)) this.selectedTier.put(p, selectedTier);
	}
	
	public void addPlayer(Player p, List<Tier> purchasedTiers){
		if(!this.purchasedTiersContainsPlayer(p)) this.purchasedTiers.put(p, purchasedTiers);
	}
	
	public void addPlayer(Player p, Tier selectedTier, List<Tier> purchasedTiers){
		this.addPlayer(p, selectedTier);
		this.addPlayer(p, purchasedTiers);
	}
	
	public void setPlayerSelectedTier(Player p, Tier tier){
		if(this.selectedTierContainsPlayer(p)) this.selectedTier.remove(p);
		this.addPlayer(p, tier);
	}
	
	public boolean selectedTierContainsPlayer(Player p){
		return selectedTier.containsKey(p);
	}
	public boolean purchasedTiersContainsPlayer(Player p){
		return purchasedTiers.containsKey(p);
	}
	
	public Tier getSelectedTier(Player p){
		if(!this.selectedTierContainsPlayer(p)) return null;
		return getSelectedTierList().get(p);
	}
	
	public Map<Player, Tier> getSelectedTierList() {
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
	
	public void loadPlayerData(){
		selectedTier = new HashMap<Player, Tier>();
		purchasedTiers = new HashMap<Player, List<Tier>>();
		ConfigurationSection playerSection = plugin.getPlayerConfig().getConfigurationSection("players");
		for(String uuid : playerSection.getKeys(false)){
			Player p = Bukkit.getPlayer(UUID.fromString(uuid));
			if(p == null) continue;
			Tier tier;
			if(playerSection.contains(uuid + ".selected")) {
				int tierLevel = playerSection.getInt(uuid + ".selected.level");
				String tierClass = playerSection.getString(uuid + ".selected.class");
				tier = this.getTierByLevel(tierClass, tierLevel);
			}else{
				tier = this.getTierByLevel("DEFAULT", 0);
			}
			if(tier == null) continue;
			List<Tier> purchasedTiers = new ArrayList<Tier>();
			if(playerSection.contains(uuid + ".purchased")) {

				ConfigurationSection purchasedSection = playerSection.getConfigurationSection(uuid + ".purchased");
				for(String purchasedClass : purchasedSection.getKeys(false)){
					List<Integer> purchasedLevels = purchasedSection.getIntegerList(purchasedClass);
					for(int purchasedLevel : purchasedLevels){
						Tier purchasedTier = this.getTierByLevel(purchasedClass, purchasedLevel);
						purchasedTiers.add(purchasedTier);
					}
				}
			}
			this.addPlayer(p, tier, purchasedTiers);
		}
	}
	
	public void savePlayerData(){
		for(Player p : this.getSelectedTierList().keySet()){
			Tier tier = getSelectedTier(p);
			String uuid = p.getUniqueId().toString();
			plugin.debug("Saving selected tier for " + uuid + ". Currently selected level " + tier.getLevel() + " in class " + tier.getTierClass());
			plugin.getPlayerConfig().set("players." + uuid + ".selected.class", tier.getTierClass());
			plugin.getPlayerConfig().set("players." + uuid + ".selected.level", tier.getLevel());
		}
		for(Player p : this.getPurchasedTiers().keySet()) {
			String uuid = p.getUniqueId().toString();
			List<Tier> purchasedTiers = this.getPurchasedTiers().get(p);
			for(Tier purchasedTier : purchasedTiers){
				List<Integer> purchasedLevels = new ArrayList<Integer>();
				if(plugin.getPlayerConfig().contains("players." + uuid + ".purchased." + purchasedTier.getTierClass()))
					purchasedLevels = plugin.getPlayerConfig().getIntegerList("players." + uuid + ".purchased." + purchasedTier.getTierClass());
				if(!purchasedLevels.contains(purchasedTier.getLevel())) purchasedLevels.add(purchasedTier.getLevel());
				plugin.getPlayerConfig().set("players." + uuid + ".purchased." + purchasedTier.getTierClass(), purchasedLevels);
			}
		}
		plugin.savePlayerConfig();
	}
	
	public boolean canPlayerBuyTierWithMoney(Player p, Tier tier) {
		if(econManager.isConnectedToVault() && tier.hasMoneyPrice() && !econManager.canAfford(p, tier.getPriceMoney())) return false;
		return true;
	}
	
	public boolean canPlayerBuyTierWithXp(Player p, Tier tier) {
		if(!tier.hasXpPrice()) return true;
		if(p.getLevel() >= tier.getPriceXp()) return true;
		return false;
	}
	
	public boolean canPlayerBuyTier(Player p, Tier tier) {
		if(!canPlayerBuyTierWithMoney(p, tier)) return false;
		if(!canPlayerBuyTierWithXp(p,tier)) return false;
		return true;
	}
	
	public boolean purchaseTier(Player p, Tier tier) {
		return this.purchaseTier(p, tier, false);
	}
	
	public boolean purchaseTier(Player p, Tier tier, boolean forceBuy) {
		//Check if player can afford the tier and check if the have bought the previous level
		if(!forceBuy && !canPlayerBuyTier(p, tier)) return false;
		if(!forceBuy && !hasPlayerPurchasedPreviousLevel(p, tier)) return false;
		if(econManager.isConnectedToVault() && tier.hasMoneyPrice()) econManager.takeMoney(p, tier.getPriceMoney());
		if(tier.hasXpPrice()) {
			int xpPriceInLevels = tier.getPriceXp();
			p.setLevel(p.getLevel()-xpPriceInLevels);
		}
		//Buy the tier
		if(this.hasPlayerPurchasedLevel(p, tier)) return false;
		this.getPlayersPurchasedTiers(p).add(tier);
		return true;
	}

	public boolean hasPlayerPurchasedLevel(Player p, Tier tier) {
		if(this.purchasedTiers.size() == 0) {
			this.givePlayerStartPurchases(p);
			return hasPlayerPurchasedLevel(p, tier);
		}
		if(tier.getLevel() <= 0 && tier.getTierClass() == "DEFAULT") return true;
		List<Tier> purchasedTiersByClass = this.getPlayersPurchasedTiersByClass(p, tier.getTierClass());
		for(Tier tierI : purchasedTiersByClass) {
			if(tierI.getLevel() == tier.getLevel()) return true;
			else continue;
		}
		return false;
	}
	
	public boolean hasPlayerPurchasedPreviousLevel(Player p, Tier nextTier) {
		if(this.purchasedTiers.size() == 0) {
			this.givePlayerStartPurchases(p);
			return hasPlayerPurchasedPreviousLevel(p, nextTier);
		}
		if(nextTier.getLevel() <= 0) return true;
		List<Tier> purchasedTiersByClass = this.getPlayersPurchasedTiersByClass(p, nextTier.getTierClass());
		if(purchasedTiersByClass.size() <= 0) return false;
		for(Tier tier : purchasedTiersByClass) {
			if((nextTier.getLevel() -1 ) == tier.getLevel()) return true;
		}
		return false;
		
	}
	
	public void givePlayerStartSelect(Player p) {
		Tier tier = this.getTierByLevel("DEFAULT", 0);
		if(this.selectedTierContainsPlayer(p)) {
			this.selectedTier.remove(p);
		}
		this.setPlayerSelectedTier(p, tier);
	}
	
	//If the player is new then the player needs to have a tier they can use. So we give them the default 0
	public void givePlayerStartPurchases(Player p) {
		Tier tier = this.getTierByLevel("DEFAULT", 0);
		if(this.purchasedTiersContainsPlayer(p)) {
			this.purchasedTiers.remove(p);
		}
		List<Tier> startList = new ArrayList<Tier>();
		startList.add(tier);
		this.purchasedTiers.put(p, startList);
		this.purchaseTier(p, tier, true);
		
	}
	
	public void unload() {
		this.savePlayerData();
		purchasedTiers = null;
		selectedTier = null;
		tiers = null;
	}
	public void load() {
		setSelectedTier(new HashMap<Player, Tier>());
		setPurchasedTiers(new HashMap<Player, List<Tier>>());
		tiers = new HashMap<String, List<Tier>>();
		this.loadTiers();
		this.loadPlayerData();
	}
	
	public void reload() {
		unload();
		load();
	}
	
	public static TierManager getInstance(){
		if(instance == null) instance = new TierManager();
		return instance;
	}
	
	public List<Tier> getPlayersPurchasedTiersByClass(Player p, String tierClass) {
		return this.focusListOnClass(this.getPlayersPurchasedTiers(p), tierClass);
	}
	
	public List<Tier> getPlayersPurchasedTiers(Player p) {
		if(!this.getPurchasedTiers().containsKey(p)) {
			List<Tier> emptyTierList = new ArrayList<Tier>();
			return emptyTierList;
		}
		return this.getPurchasedTiers().get(p);
	}
	
	public List<Tier> focusListOnClass(List<Tier> list, String tierClass) {
		List<Tier> newList = new ArrayList<Tier>();
		for(Tier tier : list) {
			if(!tier.getTierClass().equals(tierClass)) continue;
			newList.add(tier);
		}
		return newList;
	}
	
	public Map<Player, List<Tier>> getPurchasedTiers() {
		return purchasedTiers;
	}

	public void setPurchasedTiers(Map<Player, List<Tier>> purchasedTiers) {
		this.purchasedTiers = purchasedTiers;
	}

	public Map<Player, Tier> getSelectedTier() {
		return selectedTier;
	}

	public void setSelectedTier(Map<Player, Tier> selectedTier) {
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
