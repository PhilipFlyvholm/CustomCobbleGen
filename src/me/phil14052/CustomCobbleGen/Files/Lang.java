package me.phil14052.CustomCobbleGen.Files;
 
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
 
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
    PLAYER_PLUGIN_HELP("player.plugin-help", "ARRAYLIST: &8&l&m--------------------- ,   , &3CustomCobbleGen - &8Help"
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
    TIER_NOT_PURCHASED("tier-not-purchased", "&cYou have not purchased this tier yet"),
    TIER_LOCKED("tier-not-locked", "&cThis tier is locked. Buy the previous tier first."),
    TIER_NOT_FOUND("tier-not-found", "&cTier not found"),
    TIER_CANT_AFFORD("tier-cant-afford", "&cYou can not afford this tier"),
    FORCE_PURCHASED("force-purchased", "You have now force bought %selected_tier_name% for %player_name%"),
    ADMIN_USAGE("admin-command-usage", "&cUsage: /%command% [reload, forcesave, settier, givetier, forcebuy]"),
    GUI_BUY("gui.main.buy", "&aClick to buy"),
    GUI_CAN_NOT_AFFORD("gui.main.can-not-afford", "&cCan't afford"),
    GUI_SELECTED("gui.main.selected", "&aSelected"),
    GUI_SELECT("gui.main.select", "&aClick to select"),
    GUI_LOCKED_PERMISSION("gui.locked.missing-permission", "&cLocked - Missing permissions"),
    GUI_LOCKED_PREV("gui.locked.prev-unowned", "&cLocked - Buy the previous level first"),
    GUI_PRICE_MONEY_AFFORD("gui.price.money.afford", "&a$%tier_price_money%"),
    GUI_PRICE_MONEY_EXPENSIVE("gui.price.money.expensive", "&c$%tier_price_money%"),
    GUI_PRICE_XP_AFFORD("gui.price.xp.afford", "&a%tier_price_xp% exp levels"),
    GUI_PRICE_XP_EXPENSIVE("gui.price.xp.expensive", "&c%tier_price_xp% exp levels"),
    GUI_PRICE_LEVEL_ACHIEVED("gui.price.level.achieved", "&aLevel %tier_price_level% island required"),
    GUI_PRICE_LEVEL_NOT_ACHIEVED("gui.price.level.not-achieved", "&cLevel %tier_price_level% island required"),
    GUI_PRICE_ITEMS_AFFORD_TOP("gui.price.items.top.afford", "&aItems needed:"),
    GUI_PRICE_ITEMS_EXPENSIVE_TOP("gui.price.items.top.expensive", "&cItems needed:"),
    GUI_PRICE_ITEMS_AFFORD_LIST("gui.price.items.list.afford", "&a%s1 x %s2"),
    GUI_PRICE_ITEMS_EXPENSIVE_LIST("gui.price.items.list.expensive", "&c%s1 x %s2"),
    GUI_ITEM_NAME("gui.item.name", "&6&l%tier_name%"),
    GUI_ITEM_LORE_TITLE("gui.item.lore.title", "&8&lThis tier will give the following results"),
    GUI_ITEM_LORE_RESULT("gui.item.lore.result", "&8%result_name%: &o%result_percentage%"),
    GUI_CONFIRM_CANCEL("gui.confirm.cancel.name", "&cCancel"),
    GUI_CONFIRM_CANCEL_LORE("gui.confirm.cancel.lore", "&8Click to cancel the purchase"),
    GUI_CONFIRM_BUY("gui.confirm.buy.name", "&aBuy"),
    GUI_CONFIRM_BUY_LORE("gui.confirm.buy.lore", "&8Click to confirm the purchase"),
    GUI_ADMIN_TITLE("gui.admin.title", "&4&lCCG: &cAdmin"),
    GUI_ADMIN_RELOAD("gui.admin.reload.title", "&6&lReload config"),
    GUI_ADMIN_RELOAD_LORE("gui.admin.reload.lore", "ARRAYLIST: &8Reloads all files , &7- config.yml , &7- lang.yml , &7- data/players.yml"),
    GUI_ADMIN_FORCESAVE("gui.admin.forcesave.title", "&6&lForce save"),
    GUI_ADMIN_FORCESAVE_LORE("gui.admin.forcesave.lore", "ARRAYLIST: &8Forces the server to save player files , &7- data/players.yml , &r , &cUseful for big changes in player data"),
    GUI_ADMIN_FORCEBUY("gui.admin.forcebuy.title", "&6&lForce buy"),
    GUI_ADMIN_FORCEBUY_LORE("gui.admin.forcebuy.lore", "ARRAYLIST: &8Forces a player to buy a tier , &r  , &cThe player will pay for the tier , &ceven if they can't afford it!"),
    GUI_ADMIN_GIVETIER("gui.admin.givetier.title", "&6&lGive tier"),
    GUI_ADMIN_GIVETIER_LORE("gui.admin.givetier.lore", "ARRAYLIST: &8Gives a player a tier , &r  , &cThe player will get the tier for free"),
    GUI_ADMIN_SETTIER("gui.admin.settier.title", "&6&lSet tier"),
    GUI_ADMIN_SETTIER_LORE("gui.admin.settier.lore", "ARRAYLIST: &8Sets the current tier for a player , &r  , &cThis will not purchase a tier"),
    GUI_ADMIN_NO_PERMISSION_TITLE("gui.admin.no-permission.title", "&4&lNo permission"),
    GUI_ADMIN_NO_PERMISSION_LORE("gui.admin.no-permission.lore", "&cYou do not have the %s1 permission"),
    GUI_SELECT_PLAYER_SKULL_TITLE("gui.select.player-select-skull.title", "&e&l%player_name%"),
    GUI_SELECT_PLAYER_SKULL_LORE("gui.select.player-select-skull.lore", "ARRAYLIST: &aClick to select %player_name%"),
    GUI_SELECT_TITLE("gui.select.title", "&4&lCCG: &cSelect a player"),
    GUI_CLOSE_TITLE("gui.close.title", "&4&lClose"),
    GUI_CLOSE_LORE("gui.close.lore", "ARRAYLIST: &cClick to close the inventory"),
    GUI_NEXTPAGE_TITLE("gui.select.next-page.title", "&a&lNext page"),
    GUI_NEXTPAGE_LORE("gui.select.next-page.lore", "ARRAYLIST: &8Click to go the next page"),
    GUI_NEXTPAGE_LAST_TITLE("gui.select.next-page.last-page.title", "&a&lNext page"),
    GUI_NEXTPAGE_LAST_LORE("gui.select.next-page.last-page.lore", "ARRAYLIST: &8You are on the last page"),
    GUI_PREVIOUSPAGE_TITLE("gui.select.previous-page.title", "&a&lPrevious page"),
    GUI_PREVIOUSPAGE_LORE("gui.select.previous-page.lore", "ARRAYLIST: &8Click to go the previous page"),
    GUI_PREVIOUSPAGE_FIRST_TITLE("gui.select.previous-page.first-page.title", "&a&lPrevious page"),
    GUI_PREVIOUSPAGE_FIRST_LORE("gui.select.previous-page.first-page.lore", "ARRAYLIST: &8You are on the first page"),
    GUI_SELECT_TIER_ALREADY_SELECTED("gui.select.tier.already-selected", "&4&lTier currently selected"),
    GUI_SELECT_TIER_ALREADY_OWN("gui.select.tier.already-own", "&4&lTier already own"),
    GUI_SELECT_TIER_TITLE("gui.select.tier.title", "&4&lCCG: &cSelect a tier"),
    SIGN_GUI_0("signs.GUI.0", "&3CustomCobbleGen"),
    SIGN_GUI_1("signs.GUI.1", " "),
    SIGN_GUI_2("signs.GUI.2", "&lClick to open"),
    SIGN_GUI_3("signs.GUI.3", "&lthe GUI"),
    SIGN_SELECT_0("signs.select.0", "&3CustomCobbleGen"),
    SIGN_SELECT_1("signs.select.1", " "),
    SIGN_SELECT_2("signs.select.2", "&lClick to select"),
    SIGN_SELECT_3("signs.select.3", "&a&l%tier_name%"),
    SIGN_BUY_0("signs.buy.0", "&3CustomCobbleGen"),
    SIGN_BUY_1("signs.buy.1", ""),
    SIGN_BUY_2("signs.buy.2", "&lClick to buy"),
    SIGN_BUY_3("signs.buy.3", "&a&l%tier_name%"),
    SIGN_NOT_VALID_0("signs.not-valid.0", "&3CustomCobbleGen"),
    SIGN_NOT_VALID_1("signs.not-valid.1", ""),
    SIGN_NOT_VALID_2("signs.not-valid.2", "&cNot a valid sign"),
    SIGN_NOT_VALID_3("signs.not-valid.3", ""),
    SIGN_NO_PERMISSION_0("signs.no-permission.0", "&3CustomCobbleGen"),
    SIGN_NO_PERMISSION_1("signs.no-permission.1", "&cYou do not have"),
    SIGN_NO_PERMISSION_2("signs.no-permission.2", "&cpermissions to"),
    SIGN_NO_PERMISSION_3("signs.no-permission.3", "&ccreate this sign"),
    SIGN_SUCCESS("signs.success", "Successfully created a clickable sign"),
    SIGN_DELETED("signs.deleted", "&cSign deleted");
    
    
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
 
    public static String color(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    @Override
    public String toString() {
    	String string = color(LANG.getString(this.getPath()));
        if (this == PREFIX) string = string + " ";
        return string;
    }
    public String toString(Player p) {
    	String string = this.toString();
    	if(p != null && p.isOnline()) {
    		if(CustomCobbleGen.getInstance().isUsingPlaceholderAPI) {
    			string = PlaceholderAPI.setPlaceholders(p, string);
    		}
    		string = string.replaceAll("%player_name%", p.getName());
    		Tier tier = tm.getSelectedTier(p.getUniqueId());
    		if(tier != null) {
        		string = string.replaceAll("%selected_tier_level%", tier.getLevel() + "");
        		string = string.replaceAll("%selected_tier_class%", tier.getTierClass() + "");	
        		string = string.replaceAll("%selected_tier_name%", tier.getName() + "");		
        		string = string.replaceAll("%selected_tier_price_money%", EconomyManager.getInstance().formatMoney(tier.getRequirementValue(RequirementType.MONEY)) + "");		
        		string = string.replaceAll("%selected_tier_price_xp%", tier.getRequirementValue(RequirementType.XP) + "");	
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
        	string = string.replaceAll("%tier_price_money%", tier.hasRequirement(RequirementType.MONEY) ? EconomyManager.getInstance().formatMoney(tier.getRequirementValue(RequirementType.MONEY)) + "" : "0?");
        	string = string.replaceAll("%tier_price_xp%", tier.hasRequirement(RequirementType.XP) ? tier.getRequirementValue(RequirementType.XP) + "" : "0");
        	string = string.replaceAll("%tier_price_level%", tier.hasRequirement(RequirementType.LEVEL) ? tier.getRequirementValue(RequirementType.LEVEL) + "" : "0");
    	}
        return string;
    }
    
    public String toString(String... strings) {
    	String string = this.toString();
    	int i = 0;
    	for(String s : strings) {
    		i++;
    		string = string.replaceFirst("%s" + i, s);
    	}
    	return string;
    }
    
    public List<String> toStringList(){
    	List<String> s = LANG.getStringList(this.path);
    	List<String> colored_s = new ArrayList<String>();
    	for(String string : s){
    		colored_s.add(color(string));
    	}
    	return colored_s;
    }
    
    public List<String> toStringList(Player p){
    	List<String> s = LANG.getStringList(this.path);
    	List<String> colored_s = new ArrayList<String>();
    	for(String string : s){
    		string = color(string);
    		if(p != null && p.isOnline()) {
        		if(CustomCobbleGen.getInstance().isUsingPlaceholderAPI) {
        			string = PlaceholderAPI.setPlaceholders(p, string);
        		}
        		string = string.replaceAll("%player_name%", p.getName());
        		Tier tier = tm.getSelectedTier(p.getUniqueId());
        		if(tier != null) {
            		string = string.replaceAll("%selected_tier_level%", tier.getLevel() + "");
            		string = string.replaceAll("%selected_tier_class%", tier.getTierClass() + "");	
            		string = string.replaceAll("%selected_tier_name%", tier.getName() + "");		
            		string = string.replaceAll("%selected_tier_price_money%", EconomyManager.getInstance().formatMoney(tier.getRequirementValue(RequirementType.MONEY)) + "");		
            		string = string.replaceAll("%selected_tier_price_xp%", tier.getRequirementValue(RequirementType.XP) + "");	
        		}
        	}
    		colored_s.add(string);
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