package me.phil14052.CustomCobbleGen.Commands;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.GUI.GUIManager;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;

public class MainCommand implements CommandExecutor{

	private GUIManager guiManager = GUIManager.getInstance();
	private TierManager tm = TierManager.getInstance();
	private PermissionManager pm = new PermissionManager();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 1){
			if(!(sender instanceof Player)){
				sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY.toString());
				return false;
			}
			Player p = (Player) sender;
			guiManager.new MainGUI(p).open();
			return true;
		}else if(args[0].equalsIgnoreCase("help")) {

			sendHelp(sender, label);
			return true;
		}else if(args[0].equalsIgnoreCase("v")){
			sender.sendMessage("CCG V: " + plugin.getDescription().getVersion());
		}else if(args[0].equalsIgnoreCase("tier")) {
			if(!pm.hasPermission(sender, "customcobblegen.tier", true)) return false;
			if(args.length < 2) {
				//Get tier of player using the command
				if(!(sender instanceof Player)){
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY.toString());
					return false;
				}
				Player p = (Player) sender;
				Collection<Tier> tiers = tm.getSelectedTiers(p.getUniqueId()).getSelectedTiersMap().values();
				if(tiers == null || tiers.isEmpty()) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIER_SELECTED_SELF.toString(p));
					return false;
				}
				p.sendMessage(Lang.PREFIX.toString() + Lang.SHOW_TIER_SELF.toString(p));
				return true;
			}else {
				if(!pm.hasPermission(sender, "customcobblegen.tier.other", true)) return false;
				Player p = (Player) Bukkit.getPlayer(args[1]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				
				Collection<Tier> tiers = tm.getSelectedTiers(p.getUniqueId()).getSelectedTiersMap().values();
				if(tiers == null || tiers.isEmpty()) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIER_SELECTED_OTHER.toString(p));
					return false;
				}
				p.sendMessage(Lang.PREFIX.toString() + Lang.SHOW_TIER_OTHER.toString(p));
				return true;
				
			}
		}else if(args[0].equalsIgnoreCase("upgrade")){
			if(!pm.hasPermission(sender, "customcobblegen.upgrade", true)) return false;

			if(!(sender instanceof Player)){
				sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY.toString());
				return false;
			}
			Player p = (Player) sender;
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
				while(lookForNextTier) {
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
				p.sendMessage(Lang.PREFIX.toString() + Lang.NO_UPGRADES_AVAILABLE.toString(p));
				return true;
			}else {
				Tier nextTier = nextTiers.get(0);
				if(tm.canPlayerBuyTier(p, nextTier)) {
					tm.purchaseTier(p, nextTier); //Upgraded
					selectedTiers.addTier(nextTier);
					tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
					p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(nextTier));
					p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(nextTier));
					p.closeInventory();
					return true;
				}else {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UPGRADE_NOT_PURCHASABLE.toString(p));
					if(nextTier.hasRequirements()) {

						if(nextTier.getRequirementValue(RequirementType.MONEY) != 0) p.sendMessage(Lang.PREFIX.toString() + Lang.UPGRADE_NOT_PURCHASABLE_MONEY.toString(nextTier));
						if(nextTier.getRequirementValue(RequirementType.XP) != 0)p.sendMessage(Lang.PREFIX.toString() + Lang.UPGRADE_NOT_PURCHASABLE_XP.toString(nextTier));
						if(nextTier.getRequirementValue(RequirementType.ITEMS) != 0)p.sendMessage(Lang.PREFIX.toString() + Lang.UPGRADE_NOT_PURCHASABLE_ITEMS.toString(nextTier));
						if(nextTier.getRequirementValue(RequirementType.LEVEL) != 0)p.sendMessage(Lang.PREFIX.toString() + Lang.UPGRADE_NOT_PURCHASABLE_LEVEL.toString(nextTier));	
					}
				}
			}
			
		}else if(args[0].equalsIgnoreCase("admin")){
			if(!pm.hasPermission(sender, "customcobblegen.admin", true)) return false;
			if(args.length < 2){
				if(plugin.getConfig().getBoolean("options.gui.admingui") && sender instanceof Player) {
					Player p = (Player) sender;
					guiManager.new AdminGUI(p).open();
				}else {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.ADMIN_USAGE.toString().replaceAll("%command%", label));
				}
				return false;
			}
			if(args[1].equalsIgnoreCase("reload")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.reload", true)) return false;

				double time = System.currentTimeMillis();
				plugin.reloadPlugin();
				double time2 = System.currentTimeMillis();
				double time3 = (time2-time)/1000;
				if(sender instanceof Player) {
					Player p = (Player) sender;
					plugin.log(p.getName() + " reloaded the plugin");
				}
				sender.sendMessage(Lang.PREFIX.toString() + Lang.RELOAD_SUCCESS.toString().replaceAll("%time%", String.valueOf(time3)));
			}else if(args[1].equalsIgnoreCase("forcesave")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.forcesave", true)) return false;
				tm.saveAllPlayerData();
				if(sender instanceof Player) {
					Player p = (Player) sender;
					plugin.log(p.getName() + " force saved the player data");
				}
				sender.sendMessage(Lang.PREFIX.toString() + Lang.FORCE_SAVE_SUCCESS.toString());
				
			}else if(args[1].equalsIgnoreCase("settier")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.settier", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX.toString() + "Usage: /oregen admin settier (Player) (Class) (Level)");
					return false;
				}
				Player p = (Player) Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(!StringUtils.isInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
				selectedTiers.addTier(tier);
				tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGE_SUCCES.toString(p));
				return true;
			}else if(args[1].equalsIgnoreCase("givetier")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.givetier", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX.toString() + "Usage: /oregen admin givetier (Player) (Class) (Level)");
					return false;
				}
				Player p = (Player) Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(!StringUtils.isInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}

				if(tm.hasPlayerPurchasedLevel(p, tier)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ALREADY_OWNS_TIER.toString(p));
					return false;
				}
				tm.getPlayersPurchasedTiers(p.getUniqueId()).add(tier);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_GIVEN.toString());
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_GOTTEN.toString());
			}else if(args[1].equalsIgnoreCase("withdraw")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.withdraw", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX.toString() + "Usage: /oregen admin givetier (Player) (Class) (Level)");
					return false;
				}
				Player p = (Player) Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(!StringUtils.isInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				UUID uuid = p.getUniqueId();
				SelectedTiers selectedTiers = tm.getSelectedTiers(uuid);
				if(selectedTiers != null && selectedTiers.isTierSelected(tier)) {
					tm.getSelectedTiers(uuid).removeTier(tier);
					sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_UNSELECTED_SUCCESS_SELF.toString(p));
					p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_UNSELECTED_SUCCESS_OTHER.toString(p));
				}
				
				if(!tm.hasPlayerPurchasedLevel(p, tier)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_DOES_NOT_OWN_TIER.toString(p));
					return false;
				}
				tm.withdrawPurchasedTier(uuid, tier);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_UNPURCHASED_SUCCESS_SELF.toString(p));
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_UNPURCHASED_SUCCESS_OTHER.toString(p));
				tm.saveAllPlayerData();
				
			} else if(args[1].equalsIgnoreCase("forcebuy")) {
				if(!pm.hasPermission(sender, "customcobblegen.admin.forcebuy", true)) return false;
				if(args.length < 5){
					sender.sendMessage(Lang.PREFIX.toString() + "Usage: /oregen admin forcebuy (Player) (Class) (Level)");
					return false;
				}
				Player p = (Player) Bukkit.getPlayer(args[2]);
				if(p == null || !p.isOnline()) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_OFFLINE.toString(p));
					return false;
				}
				String tierClass = args[3].toUpperCase();
				if(!tm.getTiers().containsKey(tierClass)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS.toString(p));
					return false;
				}
				String levelString = args[4];
				if(!StringUtils.isInteger(levelString)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}
				int tierLevel = Integer.parseInt(levelString);
				Tier tier = tm.getTierByLevel(tierClass, tierLevel);
				if(tier == null) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString(p));
					return false;
				}

				if(tm.hasPlayerPurchasedLevel(p, tier)) {
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ALREADY_OWNS_TIER.toString());
					return false;
				}
				tm.purchaseTier(p, tier, true);
				SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
				selectedTiers.addTier(tier);
				tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.FORCE_PURCHASED.toString(p));
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(p));
			}else if(args[1].equalsIgnoreCase("debug")){
				if(!pm.hasPermission(sender, "customcobblegen.debugger", true)) return false;
				sender.sendMessage(" ");
				for(GenPiston piston : BlockManager.getInstance().getKnownGenPistons().values()) {
					sender.sendMessage(piston.getLoc().toString());
				}
			}else {
				if(!pm.hasPermission(sender, "customcobblegen.admin", true)) return false;
				sender.sendMessage(Lang.PREFIX.toString() + Lang.ADMIN_USAGE.toString().replaceAll("%command%", label));
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
}

