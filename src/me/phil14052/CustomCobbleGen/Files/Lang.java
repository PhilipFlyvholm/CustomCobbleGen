package me.phil14052.CustomCobbleGen.Files;
 
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
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
    PLAYER_PLUGIN_HELP("player.plugin-help", "ARRAYLIST: &8&l&m--------------------- ,   , &3{CustomCobbleGen} - &8Help"
    		+ " , &3/%command%&8 - Show the GUI"
    		+ " , &3/%command% help&8 - Shows the help menu"
    		+ " , &3/%command% tier&8 - Shows the currently selected tier"
    		+ " , &3/%command% admin&8 - Shows list of admin commands"
    		+ " , &8&l&m---------------------"),
    GUI_PREFIX("gui.prefix", "&3&lCustomCobbleGen menu"),
    NO_TIERS_DEFINED("no-tiers-defined", "&cThere are no tiers defined in the config"),
    MONEY_FORMAT("money-format", "###,###,###,###,###.##"),
    TIER_CHANGED("tier-changed", "You have now selected the %selected_tier_name% tier"),
    TIER_PURCHASED("tier-purchased", "You have now purchased the %selected_tier_name% tier"),
    PLAYER_ALREADY_OWNS_TIER("player-already-owns-tier", "The player already owns this tier"),
    TIER_GIVEN("tier-given", "Tier has been given to the player"),
    TIER_GOTTEN("tier-gotten", "You have unlocked a new tier"),
    FORCE_PURCHASED("force-purchased", "You have now force bought %selected_tier_name% for %player_name%"),
    ADMIN_USAGE("admin-command-usage", "&cUsage: /%command% [reload, forcesave, settier, givetier, forcebuy]"),
    GUI_BUY("gui.buy", "&aClick to buy"),
    GUI_CAN_NOT_AFFORD("gui.can-not-afford", "&cCan't afford"),
    GUI_SELECTED("gui.selected", "&aSelected"),
    GUI_SELECT("gui.select", "&aClick to select"),
    GUI_LOCKED_PERMISSION("gui.locked.missing-permission", "&cLocked - Missing permissions"),
    GUI_LOCKED_PREV("gui.locked.prev-unowned", "&cLocked - Buy the previous level first"),
    GUI_PRICE_MONEY_AFFORD("gui.price.money.afford", "&a$%tier_price_money%"),
    GUI_PRICE_MONEY_EXPENSIVE("gui.price.money.expensive", "&c$%tier_price_money%"),
    GUI_PRICE_XP_AFFORD("gui.price.xp.afford", "&a%tier_price_xp% exp levels"),
    GUI_PRICE_XP_EXPENSIVE("gui.price.xp.expensive", "&c%tier_price_xp% exp levels"),
    GUI_ITEM_NAME("gui.item.name", "&6&l%tier_name%"),
    GUI_ITEM_LORE_TITLE("gui.item.lore.title", "&8&lThis tier will give the following results"),
    GUI_ITEM_LORE_RESULT("gui.item.lore.result", "&8%result_name%: &o%result_percentage%");
    
    
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
        		string = string.replaceAll("%selected_tier_price_money%", EconomyManager.getInstance().formatMoney(tier.getPriceMoney()) + "");		
        		string = string.replaceAll("%selected_tier_price_xp%", tier.getPriceXp() + "");	
    		}
    	}
        return string;
    }
    public String toString(Tier tier) {
    	String string = this.toString();
    	if(tier != null) {
        	string = string.replaceAll("%tier_level%", tier.getLevel() + "");
        	string = string.replaceAll("%tier_name%", tier.getName() + "");
        	string = string.replaceAll("%tier_class%", tier.getTierClass() + "");
        	string = string.replaceAll("%tier_price_money%", EconomyManager.getInstance().formatMoney(tier.getPriceMoney()) + "");
        	string = string.replaceAll("%tier_price_xp%", tier.getPriceXp() + "");
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