package me.phil14052.CustomCobbleGen.Commands;


import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.GUI.GUIManager;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
import me.phil14052.CustomCobbleGen.Utils.Response;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import me.phil14052.CustomCobbleGen.Utils.pastebin.FileUploader;
import me.phil14052.CustomCobbleGen.databases.MySQLPlayerDatabase;
import me.phil14052.CustomCobbleGen.databases.PlayerDatabase;
import me.phil14052.CustomCobbleGen.databases.YamlPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MainCommand implements CommandExecutor{

	private final GUIManager guiManager = GUIManager.getInstance();
	private final TierManager tm = TierManager.getInstance();
	private final PermissionManager pm = new PermissionManager();
	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(args.length < 1){
			if(Setting.GUI_PERMISSIONNEEDED.getBoolean()) {
				if(!pm.hasPermission(sender, "customcobblegen.gui", true)) return false;
			}
			if(!(sender instanceof Player p)){
				sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
				return false;
			}
			if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean()
					&& plugin.isConnectedToIslandPlugin()
					&& !plugin.getIslandHook().hasIsland(p.getUniqueId())) {
				p.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_NO_ISLAND);
				return false;
			}
			guiManager.new MainGUI(p).open();
			return true;
		}else if(args[0].equalsIgnoreCase("help")) {

			sendHelp(sender, label);
			return true;
		}else if(args[0].equalsIgnoreCase("v")){
			sender.sendMessage("CCG Version: " + plugin.getDescription().getVersion());
		}else if(args[0].equalsIgnoreCase("tier")) {
			if(!pm.hasPermission(sender, "customcobblegen.tier", true)) return false;
			if(args.length < 2) {
				//Get tier of player using the command
				if(!(sender instanceof Player p)){
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
					return false;
				}
				UUID uuid = p.getUniqueId();
				if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean()  && plugin.isConnectedToIslandPlugin()) {
					uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
				}
				Collection<Tier> tiers = tm.getSelectedTiers(uuid).getSelectedTiersMap().values();
				if(tiers.isEmpty()) {
					p.sendMessage(Lang.PREFIX + Lang.NO_TIER_SELECTED_SELF.toString(p));
					return false;
				}
				p.sendMessage(Lang.PREFIX + Lang.SHOW_TIER_SELF.toString(p));
			}else {
				if(!pm.hasPermission(sender, "customcobblegen.tier.other", true)) return false;
				Player p = Bukkit.getPlayer(args[1]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				UUID uuid = p.getUniqueId();
				if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
					uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
				}
				
				Collection<Tier> tiers = tm.getSelectedTiers(uuid).getSelectedTiersMap().values();
				if(tiers.isEmpty()) {
					sender.sendMessage(Lang.PREFIX + Lang.NO_TIER_SELECTED_OTHER.toString(p));
					return false;
				}
				sender.sendMessage(Lang.PREFIX + Lang.SHOW_TIER_OTHER.toString(p));
				return true;

			}
		}else if(args[0].equalsIgnoreCase("upgrade")){
			if(!pm.hasPermission(sender, "customcobblegen.upgrade", true)) return false;

			if(!(sender instanceof Player p)){
				sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
				return false;
			}
			if(plugin.isConnectedToIslandPlugin()
					&& Setting.ISLANDS_ONLYOWNER_BUY.getBoolean()
					&& !plugin.getIslandHook().isPlayerLeader(p.getUniqueId())) {
				p.sendMessage(Lang.PREFIX.toString() + Lang.GUI_BUY_LEADER_ONLY);
				return false;
			}
			SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
			if(selectedTiers == null || selectedTiers.getSelectedTiersMap().isEmpty()) {
				p.performCommand("cobblegen"); //If no tiers selected then show them a GUI to select one
				return true;
			}
			List<Tier> nextTiers = new ArrayList<>();
			List<String> foundClasses = new ArrayList<>();
			for(Tier tier : selectedTiers.getSelectedTiersMap().values()) {
				boolean lookForNextTier = true;
				Tier nextTier = null;
				Tier testingTier = tier;
				while(lookForNextTier && testingTier != null) {
					String tierClass = testingTier.getTierClass();
					plugin.debug(foundClasses, foundClasses.contains(tierClass));
					if(foundClasses.contains(tierClass)) {
						lookForNextTier = false;
						continue;
					}
					int tierLevel = testingTier.getLevel();
					testingTier = tm.getTierByLevel(tierClass, (tierLevel + 1));
					if(testingTier != null) {
						//Tier exists
						if(!tm.hasPlayerPurchasedLevel(p, testingTier)) {
							nextTier = testingTier;
							foundClasses.add(tierClass);
							lookForNextTier = false;
						}
					}else {
						lookForNextTier = false;
					}
				}
				if(nextTier != null) nextTiers.add(nextTier);
			}
			if(nextTiers.size() > 1) { //Multiple tiers available
				//Since the player has selected multiple tiers we need to know which one they want to upgrade

				guiManager.new UpgradeGUI(p, nextTiers).open();
				return true;
			}else if(nextTiers.isEmpty()){ //No upgrades available
				p.sendMessage(Lang.PREFIX + Lang.NO_UPGRADES_AVAILABLE.toString(p));
				return true;
			}else {
				Tier nextTier = nextTiers.get(0);
				if(tm.canPlayerBuyTier(p, nextTier)) {
					tm.purchaseTier(p, nextTier); //Upgraded
					selectedTiers.addTier(nextTier);
					tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
					p.sendMessage(Lang.PREFIX + Lang.TIER_PURCHASED.toString(nextTier));
					p.sendMessage(Lang.PREFIX + Lang.TIER_CHANGED.toString(nextTier));
					if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && Setting.ISLANDS_SENDMESSAGESTOTEAM.getBoolean()) {
						plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX + Lang.TIER_UPGRADED_BY_TEAM.toString(p, nextTier),
								p.getUniqueId(),
								true);
					}
					p.closeInventory();
					return true;
				}else {
					p.sendMessage(Lang.PREFIX + Lang.UPGRADE_NOT_PURCHASABLE.toString(p));
					if(nextTier.hasRequirements()) {

						if(nextTier.getRequirementValue(RequirementType.MONEY) != 0) p.sendMessage(Lang.PREFIX + Lang.UPGRADE_NOT_PURCHASABLE_MONEY.toString(nextTier));
						if(nextTier.getRequirementValue(RequirementType.XP) != 0)p.sendMessage(Lang.PREFIX + Lang.UPGRADE_NOT_PURCHASABLE_XP.toString(nextTier));
						if(nextTier.getRequirementValue(RequirementType.ITEMS) != 0)p.sendMessage(Lang.PREFIX + Lang.UPGRADE_NOT_PURCHASABLE_ITEMS.toString(nextTier));
						if(nextTier.getRequirementValue(RequirementType.LEVEL) != 0)p.sendMessage(Lang.PREFIX + Lang.UPGRADE_NOT_PURCHASABLE_LEVEL.toString(nextTier));
					}
				}
			}
			
		}else if(args[0].equalsIgnoreCase("admin")){
			if(!pm.hasPermission(sender, "customcobblegen.admin", true)) return false;
			String adminUsage = Lang.PREFIX + Lang.ADMIN_USAGE.toString().replaceAll("%command%", label);
			if(args.length < 2){
				if(Setting.GUI_ADMINGUI.getBoolean() && sender instanceof Player p) {
					guiManager.new AdminGUI(p).open();
				}else {
					sender.sendMessage(adminUsage);
				}
				return false;
			}
			if(args[1].equalsIgnoreCase("reload")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.reload", true)) return false;

				double time = System.currentTimeMillis();
				plugin.reloadPlugin();
				double time2 = System.currentTimeMillis();
				double time3 = (time2-time)/1000;
				if(sender instanceof Player p) {
					plugin.log(p.getName() + " reloaded the plugin");
				}
				sender.sendMessage(Lang.PREFIX + Lang.RELOAD_SUCCESS.toString().replaceAll("%time%", String.valueOf(time3)));
			}else if(args[1].equalsIgnoreCase("settier")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.settier", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX + "Usage: /oregen admin settier (Player) (Class) (Level)");
					return false;
				}
				Player p = Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(StringUtils.isNotInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
				selectedTiers.addTier(tier);
				tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
				sender.sendMessage(Lang.PREFIX + Lang.TIER_CHANGE_SUCCES.toString(p));
				return true;
			}else if(args[1].equalsIgnoreCase("givetier")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.givetier", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX + "Usage: /ccg admin givetier (Player) (Class) (Level)");
					return false;
				}
				Player p = Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(StringUtils.isNotInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}

				if(tm.hasPlayerPurchasedLevel(p, tier)) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_ALREADY_OWNS_TIER.toString(p));
					return false;
				}
				tm.getPlayersPurchasedTiers(p.getUniqueId()).add(tier);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_GIVEN);
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_GOTTEN);
			}else if(args[1].equalsIgnoreCase("withdraw")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.withdraw", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX + "Usage: /ccg admin givetier (Player) (Class) (Level)");
					return false;
				}
				Player p = Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(StringUtils.isNotInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				UUID uuid = p.getUniqueId();
				SelectedTiers selectedTiers = tm.getSelectedTiers(uuid);
				if(selectedTiers != null && selectedTiers.isTierSelected(tier)) {
					tm.getSelectedTiers(uuid).removeTier(tier);
					sender.sendMessage(Lang.PREFIX + Lang.TIER_UNSELECTED_SUCCESS_SELF.toString(p));
					p.sendMessage(Lang.PREFIX + Lang.TIER_UNSELECTED_SUCCESS_OTHER.toString(p));
				}
				
				if(!tm.hasPlayerPurchasedLevel(p, tier)) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_DOES_NOT_OWN_TIER.toString(p));
					return false;
				}
				tm.withdrawPurchasedTier(uuid, tier);
				sender.sendMessage(Lang.PREFIX + Lang.TIER_UNPURCHASED_SUCCESS_SELF.toString(p));
				p.sendMessage(Lang.PREFIX + Lang.TIER_UNPURCHASED_SUCCESS_OTHER.toString(p));
				plugin.getPlayerDatabase().saveToDatabase(uuid);
				
			} else if(args[1].equalsIgnoreCase("forcebuy")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.forcebuy", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX + "Usage: /ccg admin forcebuy (Player) (Class) (Level)");
					return false;
				}
				Player p = Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(StringUtils.isNotInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}

				if(tm.hasPlayerPurchasedLevel(p, tier)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ALREADY_OWNS_TIER);
					return false;
				}
				tm.purchaseTier(p, tier, true);
				SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
				selectedTiers.addTier(tier);
				tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
				sender.sendMessage(Lang.PREFIX + Lang.FORCE_PURCHASED.toString(p));
				p.sendMessage(Lang.PREFIX + Lang.TIER_PURCHASED.toString(tier));
			}else if(args[1].equalsIgnoreCase("support")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Lang.PREFIX + "&7To get support join our discord: https://discord.gg/6UpwEDUm6V"));
			}else if(args[1].equalsIgnoreCase("pastebin")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.pastebin", true)) return false;
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Lang.PREFIX + "&7Getting contents of files..."));
				
	            final Response<String> postResult = new FileUploader().pastebinUpload("config.yml", "data//players.yml", "data//signs.yml", "lang.yml");

				if (postResult.isError()) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Lang.PREFIX + "&cError pasting to pastebin: " + postResult.getResult()));
					return false;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Lang.PREFIX + "&aSuccess pasting to pastebin! Send this link to the dev:"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Lang.PREFIX + "&a" + postResult.getResult()));
				
			}else if(args[1].equalsIgnoreCase("debug")){
				if(sender instanceof Player p) {
					//Giving my own user access so it is easier to help on servers. Pull requests adding own names will not be accepted
					if(p.getName().equals("PhilPlays") && !pm.hasPermission(p, "customcobblegen.debugger", true)) return false;
					UUID uuid = p.getUniqueId();
					//Console or player with permission
					if(args.length < 3) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCurrently no use for the /ccg admin debug command"));
						return true;
					}else if(args[2].equalsIgnoreCase("island")) {
						if(plugin.getIslandHook() == null) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo skyblock plugins available"));
							return true;
						}else if(!plugin.getIslandHook().hasIsland(uuid)) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have no island"));
							return true;
						}
						
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lDebug info on island accessible by plugin"));
						int level = plugin.getIslandHook().getIslandLevel(uuid);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Your uuid: &8" + uuid));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6isLeader: &8" + plugin.getIslandHook().isPlayerLeader(uuid)));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Level: &8" + level));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Players online: &8" + Arrays.toString(plugin.getIslandHook().getArrayOfIslandMembers(uuid))));
						
					}else if(args[2].equalsIgnoreCase("selected")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lDebug info on selected tiers accessible by plugin"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Number of players data loaded: &6" + plugin.getPlayerDatabase().getAllPlayerData().size()));
						
						SelectedTiers tiers = tm.getSelectedTiers(p.getUniqueId());
						if(tiers == null) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo tiers selected"));
						else{
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Your uuid: &8" + uuid));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Stored in uuid: &8" + tiers.getUUID()));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSelected tiers: &8"));
							if(tiers.getSelectedTiersMap().isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNone"));
							}else {
								StringJoiner sj = new StringJoiner("&8, &6","&8[&6","&8]&6");
								for(Tier tier : tiers.getSelectedTiersMap().values()) {
									sj.add(tier.getTierClass() + ":" + tier.getLevel());
								}
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + sj));
							}
						}
						
						
					}else if(args[2].equalsIgnoreCase("pistons")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lDebug info on pistons accessible by plugin"));
						BlockManager bm = BlockManager.getInstance();
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Number of pistons data loaded: &6" + bm.getKnownGenPistons().size()));
						
						
						Map<Location, GenPiston> pistons = bm.getKnownGenPistons();
						if(pistons == null) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo pistons loaded"));
						else{
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Your uuid: &8" + uuid));
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Pistons loaded:"));
							for(GenPiston piston : pistons.values()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + piston.getLoc().toString() + " " + (piston.getUUID().equals(uuid) ? "&a" : "&c") + piston.getUUID().toString()));
							}
						}
						
						
					}
				}else {
					sender.sendMessage(Lang.PLAYER_ONLY.toString());
					return false;
				}
				
			}else if(args[1].equalsIgnoreCase("database")){
				if(!pm.hasPermission(sender, "customcobblegen.admin.database", true)) return false;
				if(args.length < 3) {
					sender.sendMessage(Lang.DATABASE_USAGE.toString().replaceAll("%command%", label));
					return true;
				}
				if(args[2].equalsIgnoreCase("forcesave")) {
					if(!pm.hasPermission(sender, "customcobblegen.admin.database.forcesave", true)) return false;
					plugin.getPlayerDatabase().saveEverythingToDatabase();
					if(sender instanceof Player p) {
						plugin.log(p.getName() + " force saved the player data");
					}
					sender.sendMessage(Lang.PREFIX.toString() + Lang.FORCE_SAVE_SUCCESS);

				}else if(args[2].equalsIgnoreCase("migrate")){
					if(!pm.hasPermission(sender, "customcobblegen.admin.database.migrate", true)) return false;
					// label [0]    [1]      [2]   [3]   [4]
					//  ccg admin database migrate YAML MYSQL
					if(args.length < 5) {
						sender.sendMessage(Lang.DATABASE_MIGRATE_USAGE.toString().replaceAll("%command%", label));
						return true;
					}
					String fromType = args[3].toUpperCase();
					String newType = args[4].toUpperCase();
					PlayerDatabase fromDatabase = this.getDatabaseFromType(fromType);
					if(fromDatabase == null){
						sender.sendMessage(Lang.DATABASE_MIGRATE_INVALID_DATABASE.toString(fromType));
						return true;
					}
					PlayerDatabase newDatabase = this.getDatabaseFromType(newType);
					if(newDatabase == null){
						sender.sendMessage(Lang.DATABASE_MIGRATE_INVALID_DATABASE.toString(newType));
						return true;
					}
					sender.sendMessage(Lang.DATABASE_MIGRATE_STARTING.toString());
					if(fromDatabase.getAllPlayerData().isEmpty()){
						//TODO CHECK IF CONNECTION IS ESTABLISHED
						sender.sendMessage(Lang.DATABASE_MIGRATE_LOADING_START.toString(fromType));
						fromDatabase.loadEverythingFromDatabase();
						sender.sendMessage(Lang.DATABASE_MIGRATE_LOADING_DONE.toString(fromType));
					}
					if(!newDatabase.isConnectionEstablished()){
						sender.sendMessage(Lang.DATABASE_MIGRATE_ESTABLISHING_CONNECTION.toString(newType));
						Response<String> response = newDatabase.establishConnection();
						if(response.isError()){
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + response.getResult()));
							return true;
						}
					}
					newDatabase.setAllPlayerData(fromDatabase.getAllPlayerData());
					sender.sendMessage(Lang.DATABASE_MIGRATE_SAVING_START.toString(newType));
					newDatabase.saveEverythingToDatabase();
					sender.sendMessage(Lang.DATABASE_MIGRATE_SAVING_DONE.toString(newType));
				}
			}else {
				if(!pm.hasPermission(sender, "customcobblegen.admin", true)) return false;
				sender.sendMessage(adminUsage);
				return false;
			}
			return true;
		}else{
			sendHelp(sender, label);
		}
		return true;
		   
	}
	
	private void sendHelp(CommandSender sender, String label) {

		List<String> helpStrings = Lang.PLAYER_PLUGIN_HELP.toStringList();
		for(String s : helpStrings) {
			s = s.replace("%command%", label);
			sender.sendMessage(s);
		}
	}

	private PlayerDatabase getDatabaseFromType(String type){
		PlayerDatabase database = plugin.getPlayerDatabase();
		switch (type){
			case "MYSQL":
				if(database instanceof MySQLPlayerDatabase) return database;
				else return new MySQLPlayerDatabase();
			case "YAML":
				if(database instanceof YamlPlayerDatabase) return database;
				else return new YamlPlayerDatabase();
			default:
				return null;
		}
	}
}

