/**
 * CustomCobbleGen By @author Philip Flyvholm
 * TierPlaceholderExpansion.java
 */
package me.phil14052.CustomCobbleGen.Utils;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.RequirementType;

/**
 * This class will be registered through the register-method in the 
 * plugins onEnable-method.
 */
public class TierPlaceholderExpansion extends PlaceholderExpansion {

    private CustomCobbleGen plugin;

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

        if(player == null){
            return "";
        }

		Tier tier = TierManager.getInstance().getSelectedTier(player);
        // %customcobblegen_selected_tier_level%
        if(identifier.equals("selected_tier_level")){
        	if(tier == null) return "0";
            return tier.getLevel() + "";
        }

        // %customcobblegen_selected_tier_class%
        if(identifier.equals("selected_tier_class")){
        	if(tier == null) return "default";
            return tier.getTierClass() + "";
        }

        // %customcobblegen_selected_tier_name%
        if(identifier.equals("selected_tier_name")){
        	if(tier == null) return "";
            return tier.getName() + "";
        }

        // %customcobblegen_selected_tier_price_money%
        if(identifier.equals("selected_tier_price_money")){
        	if(tier == null) return "0";
            return tier.getRequirementValue(RequirementType.MONEY) + "";
        }
        
        // %customcobblegen_selected_tier_price_xp%
        if(identifier.equals("selected_tier_price_xp")){
        	if(tier == null) return "0";
            return tier.getRequirementValue(RequirementType.XP) + "";
        }
        // %customcobblegen_selected_tier_price_level%
        if(identifier.equals("selected_tier_price_level")){
        	if(tier == null) return "0";
            return tier.getRequirementValue(RequirementType.LEVEL) + "";
        }
        // %customcobblegen_selected_tier_price_items%
        if(identifier.equals("selected_tier_price_items")){
        	if(tier == null) return "0";
        	StringBuilder sb = new StringBuilder();
        	boolean first = true;
        	for(Entry<Material, Integer> entry : tier.getPriceItems().entrySet()) {
        		if(!first) sb.append(", ");
        		else first = false;
        		sb.append(entry.getValue() + "x" + entry.getKey().name()); 
        	}
            return sb.toString();
        }
        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
        // was provided
        return null;
    }
}
