package me.phil14052.CustomCobbleGen.Managers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Lang;

public class PermissionManager {
	public boolean hasPermisson(Player p, String permission, boolean withMessage){

		if(p.hasPermission(permission)) return true;
		
		String permissionClone = permission;
		CustomCobbleGen.getInstance().debug(permission);
		if(permission.contains(".")) {
			String permissionBuilder = "";
			//CustomCobbleGen.admin.reload
			String[] permissionChilds = permissionClone.split(".");
			for(String permissionChild : permissionChilds) {
				//CustomCobbleGen.admin
				permissionBuilder = permissionBuilder + permissionChild;
				//CustomCobbleGen.admin.*
				CustomCobbleGen.getInstance().debug(permissionBuilder + ".*");
				if(p.hasPermission(permissionBuilder + ".*")) return true;
				permissionBuilder = permissionBuilder + ".";
				//CustomCobbleGen.admin.
			}
		}
		if(withMessage == true){
			p.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS.toString());
		}
		return false;
		
	}
	
	public boolean hasPermission(CommandSender sender, String permission, boolean withMessage){
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(hasPermisson(p, permission, withMessage)){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
}
