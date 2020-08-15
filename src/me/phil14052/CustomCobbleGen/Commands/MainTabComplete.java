/**
 * ClearChat-3_0 By @author Philip Flyvholm
 * MainTabComplete.java
 */
package me.phil14052.CustomCobbleGen.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class MainTabComplete implements TabCompleter{
	private PermissionManager pm = new PermissionManager();
	private TierManager tm = TierManager.getInstance();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if(args.length < 1 || !(sender instanceof Player)) return null;
		if(args.length < 2) {
			List<String> subCommands = new ArrayList<>();
			subCommands.add("help");
			if(pm.hasPermission(sender, "customcobblegen.tier", false)) subCommands.add("tier");
			if(pm.hasPermission(sender, "customcobblegen.admin", false)) subCommands.add("admin");
			if(pm.hasPermission(sender, "customcobblegen.upgrade", false)) subCommands.add("upgrade");
			return subCommands;	
		} else if(args.length < 3 && args[0].equalsIgnoreCase("tier") && pm.hasPermission(sender, "customcobblegen.tier.other", false)) {
			List<String> subArgs = new ArrayList<>();
			for(Player p : Bukkit.getOnlinePlayers()) {
				subArgs.add(p.getName());
			}
			return subArgs;	
		} else if(args.length < 3 && args[0].equalsIgnoreCase("admin")) {
			List<String> subArgs = new ArrayList<>();
			if(pm.hasPermission(sender, "customcobblegen.admin.reload", false)) subArgs.add("reload");
			if(pm.hasPermission(sender, "customcobblegen.admin.forcesave", false)) subArgs.add("forcesave");
			if(pm.hasPermission(sender, "customcobblegen.admin.settier", false)) subArgs.add("settier");
			if(pm.hasPermission(sender, "customcobblegen.admin.givetier", false)) subArgs.add("givetier");
			if(pm.hasPermission(sender, "customcobblegen.admin.forcebuy", false)) subArgs.add("forcebuy");
			if(pm.hasPermission(sender, "customcobblegen.admin.withdraw", false)) subArgs.add("withdraw");
			if(pm.hasPermission(sender, "customcobblegen.admin.pastebin", false)) subArgs.add("pastebin");
			subArgs.add("support");
			return subArgs;	
		} else if(args.length < 4 && args[0].equalsIgnoreCase("admin")) {
			if(args[1].equalsIgnoreCase("settier") || args[1].equalsIgnoreCase("givetier") || args[1].equalsIgnoreCase("forcebuy")  || args[1].equalsIgnoreCase("withdraw")) {
				List<String> subArgs = new ArrayList<>();
				for(Player p : Bukkit.getOnlinePlayers()) {
					subArgs.add(p.getName());
				}
				return subArgs;	
			}
		} else if(args.length < 5 && args[0].equalsIgnoreCase("admin")) {
			if(args[1].equalsIgnoreCase("settier") || args[1].equalsIgnoreCase("givetier") || args[1].equalsIgnoreCase("forcebuy")  || args[1].equalsIgnoreCase("withdraw")) {
				List<String> subArgs = new ArrayList<>();
				Map<String, List<Tier>> tiers = tm.getTiers();
				for(String className : tiers.keySet()) {
					className = className.toLowerCase();
					subArgs.add(className);
				}
				return subArgs;	
			}
		} else if(args.length < 6 && args[0].equalsIgnoreCase("admin")) {
			if(args[1].equalsIgnoreCase("settier") || args[1].equalsIgnoreCase("givetier") || args[1].equalsIgnoreCase("forcebuy")  || args[1].equalsIgnoreCase("withdraw")) {
				List<String> subArgs = new ArrayList<>();
				List<Tier> tiers = tm.getTiers().get(args[3].toUpperCase());
				if(tiers == null) return null;
				for(Tier tier : tiers) {
					subArgs.add("" + tier.getLevel());
				}
				return subArgs;	
			}
		}
	
		
		return new ArrayList<>();
	}
}
