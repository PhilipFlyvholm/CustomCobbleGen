/**
 * CustomCobbleGen By @author Philip Flyvholm
 * PlayerEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Chat.ChatReturn;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnType;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.GUI.GUIManager;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Signs.ClickableSign;
import me.phil14052.CustomCobbleGen.Signs.SignManager;
import me.phil14052.CustomCobbleGen.databases.PlayerData;
import me.phil14052.CustomCobbleGen.databases.PlayerDatabase;

/**
 * @author Philip
 *
 */
public class PlayerEvents implements Listener {

	private final TierManager tm = TierManager.getInstance();
	private final BlockManager bm = BlockManager.getInstance();
	private final SignManager signManager = SignManager.getInstance();
	private final PermissionManager pm =  new PermissionManager();
	private final GUIManager guiManager = GUIManager.getInstance();
	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		UUID uuid = e.getPlayer().getUniqueId();
		// If the player has previously used the plugin, then load the player info.
		PlayerDatabase database = plugin.getPlayerDatabase();
		if(!database.containsPlayerData(uuid)) database.loadFromDatabase(uuid);
		PlayerData data = database.getPlayerData(uuid);
		if(data.getPurchasedTiers() == null || data.getPurchasedTiers().isEmpty()) tm.givePlayerStartPurchases(e.getPlayer());
		if(data.getSelectedTiers() == null || data.getSelectedTiers().getSelectedTiersMap().isEmpty()) tm.givePlayerStartSelect(uuid);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		// Cleanup
		bm.cleanupExpiredPistons(p.getUniqueId());
		plugin.getPlayerDatabase().saveToDatabase(p.getUniqueId());
		bm.cleanupExpiredLocations();
	}
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(!signManager.areSignsEnabled()) return;
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(e.getClickedBlock() == null) return;
		if(isSign(e.getClickedBlock().getType())) {
			ClickableSign sign = signManager.getSignFromLocation(e.getClickedBlock().getLocation());
			if(sign == null) return;
			if(pm.hasPermission(e.getPlayer(), "customcobblegen.signs.use." + sign.getSignType().name().toLowerCase(), true)) {
				sign.onInteract(e.getPlayer());	
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(guiManager.isPlayerChatting(p)) {
			e.setCancelled(true);

			ChatReturn chatReturn = guiManager.getPlayersReturn(p);
			String input = chatReturn.getType().doesAllowColor() ? e.getMessage() : this.stripcolor(e.getMessage());
			
			Tier tier = chatReturn.getTier();
			if(tier == null) tier = new Tier();
			if(input.equalsIgnoreCase("CANCEL")) {
				this.reopenInv(p, tier);
				return;
			}
			
			
			String valid = chatReturn.validInput(input);
			if(valid.equalsIgnoreCase("VALID")) {
				ChatReturnType type = chatReturn.getType();
				if(type.equals(ChatReturnType.CLASS)) {

					tier.setTierClass(input.trim());	
				}else if(type.equals(ChatReturnType.LEVEL)){
					//This has been tested in the validInput function!
					int level = Integer.parseInt(input);
					tier.setLevel(level);
				}else if(type.equals(ChatReturnType.NAME)){
					tier.setName(input);
				}else if(type.equals(ChatReturnType.DESCRIPTION)){
					if(this.stripcolor(input).equalsIgnoreCase("REMOVE")) {
						if(tier.hasDescription()) tier.setDescription(null); // Remove the description
					}else {
						List<String> description = Arrays.asList(input.split("%n%"));
						tier.setDescription(description);
					}
				}else if(type.equals(ChatReturnType.ICON)) {
					input = input.toUpperCase();
					input = input.replace(" ", "_");
					ItemStack icon = new ItemStack(Material.matchMaterial(input));
					tier.setIcon(icon);
				}else {
					plugin.error("Unkown ChatReturnType: " + type.name() + " in PlayerEvents.onPlayerChat()");
					guiManager.removePlayerChatting(p);
					return;
				}
				this.reopenInv(p, tier);
			}else {
				p.sendMessage(Lang.PREFIX.toString() + valid); //Invalid
			}
			
		}
	}
	private void reopenInv(Player p, Tier tier) {

		guiManager.removePlayerChatting(p);
		Bukkit.getScheduler().runTask(plugin, () -> { // Run on main thread
			guiManager.new CreateTierGUI(p, tier).open();
		});
	}
	
	public boolean isSign(Material material) {
		return material == XMaterial.ACACIA_SIGN.parseMaterial(true) || material == XMaterial.ACACIA_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.BIRCH_SIGN.parseMaterial(true) || material == XMaterial.BIRCH_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.DARK_OAK_SIGN.parseMaterial(true) || material == XMaterial.DARK_OAK_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.JUNGLE_SIGN.parseMaterial(true) || material == XMaterial.JUNGLE_WALL_SIGN.parseMaterial(true)
				|| material == XMaterial.OAK_SIGN.parseMaterial(true) || material == XMaterial.OAK_WALL_SIGN.parseMaterial(true);
	}
	
	private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('&') + "[0-9A-FK-OR]");
    public String stripcolor(String input) {
        return input == null?null:STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
