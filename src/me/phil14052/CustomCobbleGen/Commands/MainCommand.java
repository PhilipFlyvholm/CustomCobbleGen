package me.phil14052.CustomCobbleGen.Commands;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.GUI.GUIManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
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
			List<String> helpStrings = Lang.PLAYER_PLUGIN_HELP.toStringList();
			for(String s : helpStrings) {
				s = s.replaceAll("%command%", label);
				sender.sendMessage(s);
			}
			return true;
		}else if(args[0].equalsIgnoreCase("tier")) {
			if(!pm.hasPermission(sender, "customcobblegen.tier", true)) return false;
			if(args.length < 2) {
				//Get tier of player using the command
				if(!(sender instanceof Player)){
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY.toString());
					return false;
				}
				Player p = (Player) sender;
				Tier tier = tm.getSelectedTier(p);
				if(tier == null) {
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

				Tier tier = tm.getSelectedTier(p);
				if(tier == null) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIER_SELECTED_OTHER.toString(p));
					return false;
				}
				p.sendMessage(Lang.PREFIX.toString() + Lang.SHOW_TIER_OTHER.toString(p));
				return true;
				
			}
		}else if(args[0].equalsIgnoreCase("admin")){
			if(!pm.hasPermission(sender, "customcobblegen.admin", true)) return false;
			if(args.length < 2){
				sender.sendMessage(Lang.PREFIX.toString() + Lang.ADMIN_USAGE.toString().replaceAll("%command%", label));
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
				tm.setPlayerSelectedTier(p, tier);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGE_SUCCES.toString(p));
				return true;
			}else if(args[1].equalsIgnoreCase("givetier")) {
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
					sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ALREADY_OWNS_TIER.toString());
					return false;
				}
				tm.getPlayersPurchasedTiers(p).add(tier);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.TIER_GIVEN.toString());
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_GOTTEN.toString());
			}else if(args[1].equalsIgnoreCase("forcebuy")) {
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
				tm.setPlayerSelectedTier(p, tier);
				sender.sendMessage(Lang.PREFIX.toString() + Lang.FORCE_PURCHASED.toString(p));
				p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(p));
			}else {
				sender.sendMessage(Lang.PREFIX.toString() + Lang.ADMIN_USAGE.toString().replaceAll("%command%", label));
				return false;
			}
			return true;
		}else{
			List<String> helpStrings = Lang.PLAYER_PLUGIN_HELP.toStringList();
			for(String s : helpStrings) {
				s = s.replaceAll("%command%", label);
				sender.sendMessage(s);
			}
			return true;
		}
		   
	}
	
}

