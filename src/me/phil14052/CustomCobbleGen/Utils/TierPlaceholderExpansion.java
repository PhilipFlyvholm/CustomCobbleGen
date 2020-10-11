/**
 * CustomCobbleGen By @author Philip Flyvholm
 * TierPlaceholderExpansion.java
 */
package me.phil14052.CustomCobbleGen.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * This class will be registered through the register-method in the 
 * plugins onEnable-method.
 */
public class TierPlaceholderExpansion extends PlaceholderExpansion {

    private CustomCobbleGen plugin;
    private TierManager tm = TierManager.getInstance();

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public TierPlaceholderExpansion(CustomCobbleGen plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "customcobblegen";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null || !player.isOnline()){
            return "";
        }
        UUID uuid = player.getUniqueId();
		if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.isConnectedToIslandPlugin()) {
			uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
		}
		SelectedTiers selectedTiers = tm.getSelectedTiers(uuid);
		String[] identifiers = identifier.split("_");
		if(identifier.startsWith("selected_tier")) {
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

    			switch (identifiers[2]) {
    			case "level":
    				if(levels.length() <= 0) return "0";
    	            return levels.toString();
    			case "class":
    				if(classes.length() <= 0) return "default";
    	            return classes.toString();
    			case "name":
    	        	if(names.length() <= 0) return "";
    	            return names.toString();
    			case "price":
    				switch(identifiers[3]) {
    				case "money":
    		        	if(priceMoney.length() <= 0) return "0";
    		            return priceMoney.toString();
    				case "xp":
    		        	if(priceXP.length() <= 0) return "0";
    		            return priceXP.toString();
    				case "level":
    		        	if(priceLevel.length() <= 0) return "0";
    		            return priceLevel.toString();
    				case "items":
    					Tier tier = tiers.iterator().next();
    					if(tier == null) return "none";
    		        	StringBuilder sb = new StringBuilder();
    		        	boolean first = true;
    		        	for(Entry<Material, Integer> entry : tier.getPriceItems().entrySet()) {
    		        		if(!first) sb.append(", ");
    		        		else first = false;
    		        		sb.append(entry.getValue() + "x" + entry.getKey().name()); 
    		        	}
    		            return sb.toString();
    				}
    				break;
    			case "supportedMode":
    				if(supportedModes.length() <= 0) return "null";
    	        	return supportedModes.toString();
    			}
    		}
		}else if(identifier.startsWith("is_tier") && identifiers[4].equals("purchased")) { //%customcobblegen_is_tier_<class>_<level>_purchased%
			String tierClass = identifiers[2];
			int tierLevel;
			try {
				tierLevel = Integer.parseInt(identifiers[3]);
			}catch(NumberFormatException e) {
				return "<Unknown Tier Level>";
			}
			
			Tier tier = tm.getTierByLevel(tierClass, tierLevel);
			if(tm.hasPlayerPurchasedLevel(player, tier)) return Lang.PLACEHOLDER_RESPONSE_OWNED.toString();
			else return Lang.PLACEHOLDER_RESPONSE_NOT_OWNED.toString();
		}
        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
        // was provided
        return null;
    }
}
