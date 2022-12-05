package me.phil14052.CustomCobbleGen.Files;

import me.clip.placeholderapi.PlaceholderAPI;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.ItemsRequirement;
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
 
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
    SHOW_TIER_SELF("show-tier.self", "You have currently selected level %selected_tier_level% in %selected_tier_class% class"),
    SHOW_TIER_OTHER("show-tier.other", "%player_name% has currently selected level %selected_tier_level% in %selected_tier_class% class"),
    TIER_CHANGE_SUCCES("tier-change-success", "&aSuccessfully changed %player_name% tier to %selected_tier_class% %selected_tier_level%"),
    NO_PERMS("no-permissions", "&cYou don't have permission for that!"),
    RELOAD_SUCCESS("reload-success", "&aReloaded the plugin in %time% seconds."),
    FORCE_SAVE_SUCCESS("force-save-success", "&aSuccessfully force saved the player data"),
    PLAYER_PLUGIN_HELP("player.plugin-help", "ARRAYLIST: &8&l&m--------------------- ,   , &3CustomCobbleGen - &8Help"
    		+ " , &3/%command%&8 - Show the GUI"
    		+ " , &3/%command% help&8 - Shows the help menu"
    		+ " , &3/%command% tier&8 - Shows the currently selected tier"
    		+ " , &3/%command% upgrade&8 - Upgrade to the next tier"
    		+ " , &3/%command% admin&8 - Shows list of admin commands"
    		+ " , &8&l&m---------------------"),
    PLAYER_NO_ISLAND("player.has-no-island", "&cCreate a island to control generators"),
    GUI_PREFIX("gui.prefix", "&3&lCustomCobbleGen menu"),
    NO_TIERS_DEFINED("no-tiers-defined", "&cThere are no tiers defined in the config"),
    MONEY_FORMAT("money-format", "###,###,###,###,###.##"),
    TIER_CHANGED("tier-changed", "You have now selected the %tier_name% tier"),
    TIER_CHANGED_BY_TEAM("tier-changed-team", "%player_name% changed the islands selected tier to %tier_name%"),
    TIER_PURCHASED("tier-purchased", "You have now purchased the %tier_name% tier"),
    TIER_PURCHASED_BY_TEAM("tier-purchased-team", "%player_name% purchased the tier %tier_name% for the island"),
    TIER_UPGRADED_BY_TEAM("tier-upgraded-team", "%player_name% upgraded the selected tier"),
    PLAYER_ALREADY_OWNS_TIER("player-already-owns-tier", "The player already owns this tier"),
    PLAYER_DOES_NOT_OWN_TIER("player-does-not-own-tier", "The player does not own this tier"),
    TIER_UNSELECTED_SUCCESS_SELF("tier-unselected-success.self", "&aTier unselected for player"),
    TIER_UNSELECTED_SUCCESS_OTHER("tier-unselected-success.other", "&cYour currently selected tier has been unselected by an admin"),
    TIER_UNPURCHASED_SUCCESS_SELF("tier-unpurchased-success.self", "&aTier withdrawen from player"),
    TIER_UNPURCHASED_SUCCESS_OTHER("tier-unpurchased-success.other", "&cA tier owned by you has been withdrawen by an admin"),
    TIER_GIVEN("tier-given", "Tier has been given to the player"),
    TIER_GOTTEN("tier-gotten", "You have unlocked a new tier"),
    TIER_NOT_PURCHASED("tier-not-purchased", "&cYou have not purchased this tier yet"),
    TIER_LOCKED("tier-not-locked", "&cThis tier is locked. Buy the previous tier first."),
    TIER_NOT_FOUND("tier-not-found", "&cTier not found"),
    TIER_CANT_AFFORD("tier-cant-afford", "&cYou can not afford this tier"),
    NO_UPGRADES_AVAILABLE("no-upgrades-available", "&aYou do not have any upgrades available"),
    UPGRADE_NOT_PURCHASABLE("upgrade-not-purchasable", "&cYou do not fulfill the requirements to upgrade:"),
    UPGRADE_NOT_PURCHASABLE_MONEY("upgrade-not-purchasable-money", "&cYou need %tier_price_money%$ to buy the tier"),
    UPGRADE_NOT_PURCHASABLE_XP("upgrade-not-purchasable-xp", "&cYou need %tier_price_xp% XP to buy the tier"),
    UPGRADE_NOT_PURCHASABLE_LEVEL("upgrade-not-purchasable-level", "&cYou need a island level of atleast %tier_price_level% to buy the tier"),
    UPGRADE_NOT_PURCHASABLE_ITEMS("upgrade-not-purchasable-items", "&cYou need %tier_price_items% to buy this tier"),
    FORCE_PURCHASED("force-purchased", "You have now force bought %selected_tier_name% for %player_name%"),
    ADMIN_USAGE("admin-command-usage", "&cUsage: /%command% [reload, database, settier, givetier, forcebuy, withdraw]"),
    GUI_BUY("gui.main.buy", "&aClick to buy"),
    GUI_BUY_LEADER_ONLY("gui.main.buy-leader-only", "&cOnly leaders of the island can buy"),
    GUI_CAN_NOT_AFFORD("gui.main.can-not-afford", "&cCan't afford"),
    GUI_SELECTED("gui.main.selected", "&aSelected"),
    GUI_SELECT("gui.main.select", "&aClick to select"),
    GUI_SELECT_LEADER_ONLY("gui.main.select-leader-only", "&cOnly leaders of the island can select"),
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
    GUI_ITEM_LORE_SUPPORTEDMODE("gui.item.lore.supportedMode", "&8Supports generator: &6&l&o%tier_supported_mode%"),
    GUI_CONFIRM_CANCEL("gui.confirm.cancel.name", "&cCancel"),
    GUI_CONFIRM_CANCEL_LORE("gui.confirm.cancel.lore", "&8Click to cancel the purchase"),
    GUI_CONFIRM_BUY("gui.confirm.buy.name", "&aBuy"),
    GUI_CONFIRM_BUY_LORE("gui.confirm.buy.lore", "&8Click to confirm the purchase"),
    GUI_UPGRADE_TITLE("gui.upgrade.title", "&6&lChoose a tier to upgrade"),
    GUI_UPGRADE_LORE_UPGRADE("gui.upgrade.lore.upgrade", "&aClick to upgrade"),
    GUI_ADMIN_TITLE("gui.admin.title", "&4&lCCG: &cAdmin"),
    GUI_ADMIN_RELOAD("gui.admin.reload.title", "&6&lReload config"),
    GUI_ADMIN_RELOAD_LORE("gui.admin.reload.lore", "ARRAYLIST: &8Reloads all files , &7- config.yml , &7- lang.yml , &7- data/players.yml , &7- data/signs.yml"),
    GUI_ADMIN_FORCESAVE("gui.admin.forcesave.title", "&6&lForce save"),
    GUI_ADMIN_FORCESAVE_LORE("gui.admin.forcesave.lore", "ARRAYLIST: &8Forces the server to save player files , &7- data/players.yml , &r , &cUseful for big changes in player data"),
    GUI_ADMIN_FORCEBUY("gui.admin.forcebuy.title", "&6&lForce buy"),
    GUI_ADMIN_FORCEBUY_LORE("gui.admin.forcebuy.lore", "ARRAYLIST: &8Forces a player to buy a tier , &r  , &cThe player will pay for the tier , &ceven if they can't afford it!"),
    GUI_ADMIN_GIVETIER("gui.admin.givetier.title", "&6&lGive tier"),
    GUI_ADMIN_GIVETIER_LORE("gui.admin.givetier.lore", "ARRAYLIST: &8Gives a player a tier , &r  , &cThe player will get the tier for free"),
    GUI_ADMIN_SETTIER("gui.admin.settier.title", "&6&lSet tier"),
    GUI_ADMIN_SETTIER_LORE("gui.admin.settier.lore", "ARRAYLIST: &8Sets the current tier for a player , &r  , &cThis will not purchase a tier"),
    GUI_ADMIN_WITHDRAW("gui.admin.withdraw.title", "&6&lWithdraw tier from player"),
    GUI_ADMIN_WITHDRAW_LORE("gui.admin.withdraw.lore", "ARRAYLIST: &8Withdraws a purchased tier from a player , &r  , &cAlso deselects tier if selected"),
    GUI_ADMIN_WITHDRAW_SELECT("gui.admin.withdraw.select", "&aClick to withdraw tier"),
    GUI_ADMIN_CREATETIER("gui.admin.createtier.title", "&6&lCreate a new tier"),
    GUI_ADMIN_CRAETETIER_LORE("gui.admin.createtier.lore", "ARRAYLIST: &8Create a new tier with a GUI"),
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
    GUI_CREATE_TITLE("gui.create.title", "&4&lCCG: &cCreating a tier"),
    GUI_CREATE_REQUIRED("gui.create.info.required", "&c**REQUIRED**"),
    GUI_CREATE_EMPTY("gui.create.info.emptyList", "&aNone"),
    GUI_CREATE_CLASS("gui.create.class.title", "&6&lTier class"),
    GUI_CREATE_CLASS_LORE("gui.create.class.lore", "ARRAYLIST: &8Define the tier class , &r , &7Current: &a%s1"),
    GUI_CREATE_LEVEL("gui.create.level.title", "&6&lTier level"),
    GUI_CREATE_LEVEL_LORE("gui.create.level.lore", "ARRAYLIST: &8Define the tier level , &r , &7Current: &a%s1"),
    GUI_CREATE_ICON("gui.create.icon.title", "&6&lTier icon"),
    GUI_CREATE_ICON_LORE("gui.create.icon.lore", "ARRAYLIST: &8Define the tier icon material , &r , &7Current: &a%s1"),
    GUI_CREATE_NAME("gui.create.name.title", "&6&lTier name"),
    GUI_CREATE_NAME_LORE("gui.create.name.lore", "ARRAYLIST: &8Define the tier displayname , &r , &7Current: &a%s1"),
    GUI_CREATE_RESULTS("gui.create.results.title", "&6&lTier results"),
    GUI_CREATE_RESULTS_LORE("gui.create.results.lore", "ARRAYLIST: &8Define the results of the tier , &r , &7Current:"),
    GUI_CREATE_REQUIREMENTS("gui.create.requirements.title", "&6&lTier requirements"),
    GUI_CREATE_REQUIREMENTS_LORE("gui.create.requirements.lore", "ARRAYLIST: &8Define the requirements for the tier , &r , &7Currently &a%s1&7 requrirements in use"),
    GUI_CREATE_DESCRIPTION("gui.create.description.title", "&6&lTier description"),
    GUI_CREATE_DESCRIPTION_LORE("gui.create.description.lore", "ARRAYLIST: &8Define the description of the tier , &cWill replace displayed results in the GUI , &r , &7Current:"),
    CHATINPUT_INVALID("chatinput.invalid.default", "&4%s1 &cis a invalid input"),
    CHATINPUT_INVALID_NOSPACE("chatinput.invalid.no-space", "&cSpaces are not allowed"),
    CHATINPUT_INVALID_NAN("chatinput.invalid.no-number", "&4%s1&c is not a valid number!"),
    CHATINPUT_INVALID_NULL_MATERIAL("chatinput.invalid.unknown-material", "&4%s1&c is not a valid material!"),
    CHATINPUT_INFO_CLASS("chatinput.info.class", "ARRAYLIST: &7Editing tier &aclass , &7Write the tier &aclass&7 in chat , &cSpaces are not allowed"),
    CHATINPUT_INFO_ICON("chatinput.info.icon", "ARRAYLIST: &7Editing tier &aicon , &7Write the tier &aicon material name&7 in chat , &cUse Bukkit material names!"),
    CHATINPUT_INFO_LEVEL("chatinput.info.level", "ARRAYLIST: &7Editing tier &alevel , &7Write the tier &alevel&7 in chat , &cPositive numbers or zero only!"),
    CHATINPUT_INFO_NAME("chatinput.info.name", "ARRAYLIST: &7Editing tier &aname , &7Write the tier &aname&7 in chat , &cSpaces and colors are allowed!"),
    CHATINPUT_INFO_DESCRIPTION("chatinput.info.description", "ARRAYLIST: &7Editing tier &adescription , &7Write the tier &adescription&7 in chat , &7Write &a%s1&7 for new line  , &cSpaces and colors are allowed! , &cWrite &4&l\"%s2\"&c to delete description"),
    CHATINPUT_INFO_CANCEL("chatinput.info.CANCEL", "&cWrite &4&l\"%s1\" &c to cancel"),
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
    SIGN_DELETED("signs.deleted", "&cSign deleted"),
    CHATINPUT_INFO_RESULTS_MATERIAL("chatinput.info.results.material", "ARRAYLIST: &7Editing result &amaterial , &7Write the result &amaterial name , &cUse Bukkit material names!"),
    PLACEHOLDER_RESPONSE_OWNED("placeholders.response.owned", "owned"),
    PLACEHOLDER_RESPONSE_NOT_OWNED("placeholders.response.not-owned", "not owned"),
    PLACEHOLDER_RESPONSE_ALL("placeholder.response.all", "ALL"),
    DATABASE_USAGE("database.usage", "&cUsage: /%command% admin database [forcesave, migrate]"),
    DATABASE_MIGRATE_USAGE("database.migrate.usage", "&cUsage: /%command% admin database migrate (From) (To)"),
    DATABASE_MIGRATE_INVALID_DATABASE("database.migrate.invalid", "&cError: %s1 is not a valid database type"),
    DATABASE_MIGRATE_STARTING("database.migrate.starting", "&cStarting migration..."),
    DATABASE_MIGRATE_LOADING_START("database.migrate.loading.start", "&cDatabase &l%s1&c not loaded. Starting loading..."),
    DATABASE_MIGRATE_LOADING_DONE("database.migrate.loading.done", "&aDatabase &l%s1&a loaded. Continuing migration..."),
    DATABASE_MIGRATE_ESTABLISHING_CONNECTION("database.migrate.establishing-connection", "&aEstablishing connection to new &l%s1&a database"),
    DATABASE_MIGRATE_SAVING_START("database.migrate.saving.start", "&aStarting saving data to &l%s1&a database"),
    DATABASE_MIGRATE_SAVING_DONE("database.migrate.saving.done", "&a&lSuccessful migration to %s1 database");


    private final String path;
    private final String def;
    private static YamlConfiguration LANG;
    private final TierManager tm;
    private static final CustomCobbleGen plugin = CustomCobbleGen.getInstance();

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
    
    public static String replacePlaceholders(Player p, String string) {
    	if(p != null && p.isOnline()) {
    		if(CustomCobbleGen.getInstance().isUsingPlaceholderAPI) {
    			string = PlaceholderAPI.setPlaceholders(p, string);
    		}
    		string = string.replace("%player_name%", p.getName());

            UUID uuid = p.getUniqueId();
    		if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.isConnectedToIslandPlugin()) {
    			uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
    		}
    		SelectedTiers selectedTiers = TierManager.getInstance().getSelectedTiers(uuid);
    		if(selectedTiers != null && selectedTiers.getSelectedTiersMap() != null && selectedTiers.getSelectedTiersMap().values() != null) {

        		Collection<Tier> tiers = selectedTiers.getSelectedTiersMap().values();
        		if(!tiers.isEmpty()) {
            		StringJoiner levels = new StringJoiner(", ");
            		StringJoiner classes = new StringJoiner(", ");
            		StringJoiner names = new StringJoiner(", ");
            		StringJoiner priceMoney = new StringJoiner(", ");
            		StringJoiner priceXP = new StringJoiner(", ");
            		StringJoiner priceLevel = new StringJoiner(", ");
            		StringJoiner supportedModes = new StringJoiner(", ");
            		for(Tier tier : tiers) {
            			if(tier == null) continue;
            			levels.add(tier.getLevel() + "");
            			classes.add(tier.getTierClass());
            			names.add(tier.getName());
            			priceMoney.add(EconomyManager.getInstance().formatMoney(tier.getRequirementValue(RequirementType.MONEY)) + "");
            			priceXP.add(tier.getRequirementValue(RequirementType.XP) + "");
            			priceLevel.add(tier.getRequirementValue(RequirementType.LEVEL) + "");
            			supportedModes.add(tier.getSupportedMode().getId() != -1 ? tier.getSupportedMode().getName() : Lang.PLACEHOLDER_RESPONSE_ALL.toString());
            			
            		}
                	string = string.replace("%selected_tier_level%", levels.toString());
                	string = string.replace("%selected_tier_class%", classes.toString());	
                	string = string.replace("%selected_tier_name%", names.toString());		
                	string = string.replace("%selected_tier_price_money%", priceMoney.toString());		
                	string = string.replace("%selected_tier_price_xp%", priceXP.toString());
                	string = string.replace("%selected_tier_price_level%", priceLevel.toString());	
                	string = string.replace("%selected_tier_supported_mode%", supportedModes.toString());	
        		}
    		}
    	}
    	return string;
    }
    
    public String toString(Player p) {
    	String string = this.toString();
    	string = replacePlaceholders(p, string);
        return string;
    }
    public String toString(Tier tier) {
    	String string = this.toString();
    	if(tier != null) {
        	string = string.replace("%tier_level%", tier.getLevel() + "");
        	string = string.replace("%tier_name%", tier.getName() + "");
        	string = string.replace("%tier_class%", tier.getTierClass() + "");
        	string = string.replace("%tier_price_money%", tier.hasRequirement(RequirementType.MONEY) ? EconomyManager.getInstance().formatMoney(tier.getRequirementValue(RequirementType.MONEY)) + "" : "0?");
        	string = string.replace("%tier_price_xp%", tier.hasRequirement(RequirementType.XP) ? tier.getRequirementValue(RequirementType.XP) + "" : "0");
        	string = string.replace("%tier_price_level%", tier.hasRequirement(RequirementType.LEVEL) ? tier.getRequirementValue(RequirementType.LEVEL) + "" : "0");
        	String placeholder = "%tier_supported_mode%";
        	if(tier.getSupportedMode() == null) {        	
        		string = string.replaceAll(placeholder, Lang.PLACEHOLDER_RESPONSE_ALL.toString());
        	}else if(tier.getSupportedMode().getId() == -1) {
        		string = string.replaceAll(placeholder, Lang.PLACEHOLDER_RESPONSE_ALL.toString());
        	}else {
        		string = string.replaceAll(placeholder, tier.getSupportedMode().getName());
        		
        	}
        	if(string.contains("%tier_price_items%")) {
        		String result = "None";
        		if(tier.hasRequirements()) {

            		List<Requirement> requirements = tier.getRequirements();
            		for(Requirement requirement : requirements) {
            			if(requirement instanceof ItemsRequirement) {
            				result = requirement.toString();
            			}
            		}
        		}
        		string = string.replace("%tier_price_items%", result);
        		
        		
        	}
        	
    	}
        return string;
    }
    
    public String toString(Player p, Tier tier) {
    	return replacePlaceholders(p, this.toString(tier));
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
    	List<String> colored_s = new ArrayList<>();
    	for(String string : s){
    		string = color(string);
    		if(p != null && p.isOnline()) {
        		if(CustomCobbleGen.getInstance().isUsingPlaceholderAPI) {
        			string = PlaceholderAPI.setPlaceholders(p, string);
        		}
        		string = string.replace("%player_name%", p.getName());

                UUID uuid = p.getUniqueId();
        		if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.isConnectedToIslandPlugin()) {
        			uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
        		}
        		SelectedTiers selectedTiers = tm.getSelectedTiers(uuid);

        		if(selectedTiers != null && selectedTiers.getSelectedTiersMap() != null && selectedTiers.getSelectedTiersMap().values() != null) {
        			Collection<Tier> tiers = selectedTiers.getSelectedTiersMap().values();
            		if(!tiers.isEmpty()) {
                		StringJoiner levels = new StringJoiner(", ");
                		StringJoiner classes = new StringJoiner(", ");
                		StringJoiner names = new StringJoiner(", ");
                		StringJoiner priceMoney = new StringJoiner(", ");
                		StringJoiner priceXP = new StringJoiner(", ");
                		StringJoiner priceLevel = new StringJoiner(", ");
                		StringJoiner supportedModes = new StringJoiner(", ");
                		for(Tier tier : tiers) {
                			if(tier == null) continue;
                			levels.add(tier.getLevel() + "");
                			classes.add(tier.getTierClass());
                			names.add(tier.getName());
                			priceMoney.add(EconomyManager.getInstance().formatMoney(tier.getRequirementValue(RequirementType.MONEY)) + "");
                			priceXP.add(tier.getRequirementValue(RequirementType.XP) + "");
                			priceLevel.add(tier.getRequirementValue(RequirementType.LEVEL) + "");
                			supportedModes.add(tier.getSupportedMode().getId() != -1 ? tier.getSupportedMode().getName() : Lang.PLACEHOLDER_RESPONSE_ALL.toString());
                			
                		}
                    	string = string.replace("%selected_tier_level%", levels.toString());
                    	string = string.replace("%selected_tier_class%", classes.toString());	
                    	string = string.replace("%selected_tier_name%", names.toString());		
                    	string = string.replace("%selected_tier_price_money%", priceMoney.toString());		
                    	string = string.replace("%selected_tier_price_xp%", priceXP.toString());
                    	string = string.replace("%selected_tier_price_level%", priceLevel.toString());	
                    	string = string.replace("%selected_tier_supported_mode%", supportedModes.toString());	
            		}
        		}
        	}
    		colored_s.add(string);
    	}
    	return colored_s;
    }
    
    public List<String> toStringList(String... strings){
    	List<String> stringList = LANG.getStringList(this.path);
    	List<String> colored_s = new ArrayList<String>();
    	for(String string : stringList){
    		string = color(string);
    		int i = 0;
    		for(String s : strings) {
        		i++;
        		string = string.replaceFirst("%s" + i, s);
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