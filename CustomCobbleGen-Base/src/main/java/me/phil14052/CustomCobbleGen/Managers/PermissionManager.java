package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.Files.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionManager {
	public boolean hasPermission(Player p, String permission, boolean withMessage){

		if(p.hasPermission(permission)) return true;
		
		String permissionClone = permission;
		if(permission.contains(".")) {
			String permissionBuilder = "";
			//CustomCobbleGen.admin.reload
			String[] permissionChilds = permissionClone.split(".");
			for(String permissionChild : permissionChilds) {
				//CustomCobbleGen.admin
				permissionBuilder = permissionBuilder + permissionChild;
				//CustomCobbleGen.admin.*
				if(p.hasPermission(permissionBuilder + ".*")) return true;
				permissionBuilder = permissionBuilder + ".";
				//CustomCobbleGen.admin.
			}
		}
		if(withMessage){
			p.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
		}
		return false;
		
	}
	
	public boolean hasPermission(CommandSender sender, String permission, boolean withMessage){
		if(sender instanceof Player p){
			return hasPermission(p, permission, withMessage);
		}else{
			return true;
		}
	}
}
