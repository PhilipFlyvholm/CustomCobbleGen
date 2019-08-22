package me.phil14052.CustomCobbleGen.Files;
 
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
 
/**
* An enum for requesting strings from the language file.
* Made by gomeow.
* Lang added by phil14052.
* @author gomeow
*/

public enum Lang {
	
    PREFIX("prefix", "&8[&3CustomCobbleGen&8]"),
    INVALID_ARGS("invalid-args", "&cInvalid args!"),
    PLAYER_ONLY("player-only", "&cSorry but that can only be run by a player!"),
    PLAYER_OFFLINE("player-offline", "&cThat player is not online!"),
    UNDIFINED_CLASS("undifined-class", "&cThat class is not defined!"),
    UNDIFINED_LEVEL("undifined-level", "&cThat level is not defined!"),
    NO_TIER_SELECTED_SELF("no-tier-selected.self", "&cYou have not selected a tier yet"),
    NO_TIER_SELECTED_OTHER("no-tier-selected.other", "&c%player_name% has not selected a tier yet"),
    SHOW_TIER_SELF("show-tier.self", "You have currently selected level %selected_tier_class% in %selected_tier_level% class"),
    SHOW_TIER_OTHER("show-tier.other", "%player_name% has currently selected level %selected_tier_class% in %selected_tier_level% class"),
    TIER_CHANGE_SUCCES("tier-change-success", "&aSuccessfully changed %player_name% tier to %selected_tier_class% %selected_tier_level%"),
    NO_PERMS("no-permissions", "&cYou don't have permission for that!"),
    RELOAD_SUCCESS("reload-success", "&aReloaded the plugin in %time% seconds."),
    FORCE_SAVE_SUCCESS("force-save-success", "&aSuccessfully force saved the player data"),
    PLAYER_PLUGIN_HELP("player.plugin-help", "ARRAYLIST: &8&l&m--------------------- ,   , &3{plugin_name} - &8Help"
    		+ " , &3%suggest_command_/clearchat%&8 - Main command"
    		+ " , &3%suggest_command_/clearchat help% &8- Shows this"
    		+ " , &3%suggest_command_/clearchat gui% &8- Shows a clearchat menu"
    		+ " , &3%suggest_command_/clearchat global%&3  [-a,-s]&8 - Main command"
    		+ " , &3%suggest_command_/clearchat personal%&3 [-m]&8 - Main command"
    		+ " , &3%suggest_command_/clearchat mutechat global%&8 - Main command"
    		+ " , &3%suggest_command_/clearchat mutechat personal%&3 [player]&8 - Main command"
    		+ " , &3%suggest_command_/clearchat reload%&8 - Reload the config and lang file."
    		+ " ,  "
    		+ " , &8&l&m---------------------"),
    GUI_PREFIX("gui.prefix", "&3&lCustomCobbleGen menu"),
    GUI_HOME_TITLE("gui.home.title", "&6Home"),
    GUI_NO_PERMISSION_LORE("gui.no-permission.lore", "&cYou do not have the right permissions to use this gesture"),
    GUI_NO_PERMISSION_TITLE("gui.no-permission.title", "&cNo permission"),
    NO_TIERS_DEFINED("no-tiers-defined", "&cThere are no tiers defined in the config"),
    MONEY_FORMAT("money-format", "###,###,###,###,###.##"),
    TIER_CHANGED("tier-changed", "You have now selected the %selected_tier_name% tier"),
    TIER_PURCHASED("tier-purchased", "You have now purchased the %selected_tier_name% tier");
    
    
    private String path;
    private String def;
    private static YamlConfiguration LANG;
    private TierManager tm;

    /**
    * Lang enum constructor.
    * @param path The string path.
    * @param start The default string.
    */
    Lang(String path, String start) {
        this.path = path;
        this.def = start;
        this.tm = TierManager.getInstance();
    }
 
    /**
    * Set the {@code YamlConfiguration} to use.
    * @param config The config to set.
    */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }
 
    @Override
    public String toString() {
    	String string = ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path));
        if (this == PREFIX) string = string + " ";
        return string;
    }
    public String toString(Player p) {
    	String string = this.toString();
    	if(p != null && p.isOnline()) {
    		string = string.replaceAll("%player_name%", p.getName());
    		Tier tier = tm.getSelectedTier(p);
    		if(tier != null) {
        		string = string.replaceAll("%selected_tier_level%", tier.getLevel() + "");
        		string = string.replaceAll("%selected_tier_class%", tier.getTierClass() + "");	
        		string = string.replaceAll("%selected_tier_name%", tier.getName() + "");	
    		}
    	}
        return string;
    }
    
    public List<String> toStringList(){
    	List<String> s = LANG.getStringList(this.path);
    	List<String> colored_s = new ArrayList<String>();
    	for(String string : s){
    		colored_s.add(ChatColor.translateAlternateColorCodes('&', string));
    	}
    	return colored_s;
    }
    
    /**
    * Get the default value of the path.
    * @return The default value of the path.
    */
    public String getDefault() {
        return this.def;
    }
 
    /**
    * Get the path to the string.
    * @return The path to the string.
    */
    public String getPath() {
        return this.path;
    }
}