package me.phil14052.CustomCobbleGen.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Utils.GlowEnchant;
import me.phil14052.CustomCobbleGen.Utils.ItemLib;

public class GUIManager {

	private static GUIManager instance = null;
	private final ItemStack backgroundItem = new ItemLib(Material.BLACK_STAINED_GLASS_PANE, 1, (short) 15, " ").create();
	//private final ItemStack noPermissionItem = new ItemLib(XMaterial.BARRIER.parseMaterial(), 1, (short) 0, Lang.GUI_NO_PERMISSION_TITLE.toString(), Arrays.asList(Lang.GUI_NO_PERMISSION_LORE.toString())).create();
	private TierManager tm = TierManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private PermissionManager pm = new PermissionManager();
	
	public class MainGUI {
		private Map<String, List<Tier>> tiers = tm.getTiers();
		int tiersSize = tm.getTiersSize();
		int guiSize = getGUISize(tiers, false);
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_PREFIX.toString());	
		private Player player;
		private boolean failedLoad = false;
		
		public MainGUI(Player p){
			player = p;
			int i = 0;
			if(tiers == null) {
				p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIERS_DEFINED.toString());
				failedLoad = true;
				return;
			}
			Tier selectedTier = tm.getSelectedTier(p.getUniqueId());
			for(String tierClass : tiers.keySet()) {
				int j = 0;
				List<Tier> classTiers = tiers.get(tierClass);
				int rows = 0;
				rows = (int) Math.ceil((double)classTiers.size()/9);
				int currentRow = 1;
				int emptySpacesAtEnd = 0;
				for(Tier tier : classTiers) {
					j++;
					currentRow = (int) Math.ceil((double)j/9);
					if(currentRow == rows) {
						int firstItemInRow = ((currentRow-1)*9)+1;
						int itemsInRow = classTiers.size()-firstItemInRow;
						if(j == firstItemInRow) {
							int emptySpacesAtStart = (int) Math.ceil(((double)4.5-(itemsInRow/2)));
							i = (i+emptySpacesAtStart)-1;
							emptySpacesAtEnd = 9-emptySpacesAtStart-itemsInRow;
						}
					}
					ItemStack item = tier.getIcon().clone();
					ItemMeta itemMeta = item.getItemMeta();
					List<String> lore = itemMeta.getLore();
					String emptyString = "&a ";
					emptyString = ChatColor.translateAlternateColorCodes('&', emptyString);
					lore.add(emptyString);
					if(selectedTier != null && selectedTier.getLevel() == tier.getLevel() && selectedTier.getTierClass().equalsIgnoreCase(tier.getTierClass())) {
						GlowEnchant glow = new GlowEnchant(new NamespacedKey(plugin, "GlowEnchant"));
						itemMeta.addEnchant(glow, 1, true);
						lore.add(Lang.GUI_SELECTED.toString());
					}else if(tm.hasPlayerPurchasedLevel(p, tier)){
						lore.add(Lang.GUI_SELECT.toString());
					}else if(!tierClass.equalsIgnoreCase("DEFAULT") && !pm.hasPermission(p, "customcobblegen.generator." + tierClass, false)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(Material.BARRIER);
						if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
						lore.add(Lang.GUI_LOCKED_PERMISSION.toString());
					}else if(!tm.hasPlayerPurchasedPreviousLevel(p, tier)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(Material.BARRIER);
						if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
					
						lore.add(Lang.GUI_LOCKED_PREV.toString());

						for(Requirement r : tier.getRequirements()) {
							if(!tier.hasRequirement(r.getRequirementType())) continue;
							lore = r.addUnavailableString(tier, lore);
						}
					}else {
						if(tm.canPlayerBuyTier(p, tier)) {
							lore.add(Lang.GUI_BUY.toString());
							for(Requirement r : tier.getRequirements()) {
								if(!tier.hasRequirement(r.getRequirementType())) continue;
								lore = r.addAvailableString(tier, lore);
							}
						}else {
							lore.add(Lang.GUI_CAN_NOT_AFFORD.toString());

							for(Requirement r : tier.getRequirements()) {
								if(!tier.hasRequirement(r.getRequirementType())) continue;
								lore = r.addUnavailableString(tier, lore);
							}
						}
					}

					if(plugin.getConfig().getBoolean("debug")) {
						lore.add(" ");
						lore.add("i: " + i);
					}
					itemMeta.setLore(lore);
					item.setItemMeta(itemMeta);
					Icon icon = new Icon(item);
					icon.addClickAction(new ClickAction() {
						@Override
						public void execute(Player p) {
							//Check if the player has purchased the level
							if(tm.hasPlayerPurchasedLevel(p, tier)) {
								tm.setPlayerSelectedTier(p.getUniqueId(), tier);
								p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(p));
								p.closeInventory();
							}else {
								//Player has not purchased the level. Now check if the player can buy the level
								if(tm.canPlayerBuyTier(p, tier)) {
									if(plugin.getConfig().getBoolean("options.gui.confirmpurchases")) {
										new ConfirmGUI(p, tier).open();	
									}else {
										if(tm.purchaseTier(p, tier)) {
											tm.setPlayerSelectedTier(p.getUniqueId(), tier);
											p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(p));
											p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(p));
											p.closeInventory();
										}
									}
								}
							}
						}
					});
					ch.setIcon(i, icon);
					if(j >= classTiers.size()) {
						i = i + emptySpacesAtEnd;
					}
					i++;
				}
			}
			for(int j = 0; j < guiSize; j++) {
				Icon icon = ch.getIcon(j);
				if(icon == null || icon.itemStack.getType().equals(Material.AIR)) {
					ch.setIcon(j, new Icon(backgroundItem));
				}
			}
		}
		public void open(){
			if(failedLoad) return;
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
		
		private int getGUISize(Map<String, List<Tier>> tiers, boolean closeLine) {
			int rows = 0;
			for(String tierClass : tiers.keySet()) {
				List<Tier> classTiers = tiers.get(tierClass);
				int classRows = classTiers.size()/9;
				if(classTiers.size()%9 > 0D) classRows++;
				rows += classRows;
			}
					if(closeLine) rows++;
			if(rows > 6) rows = 6; //A GUI CAN MAX BE 6 ROWS
			if(rows < 1) rows = 1;
			return (rows*9);
		}
	}
	
	public class ConfirmGUI {
		int tiersSize = tm.getTiersSize();
		int guiSize = 3*9;
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_PREFIX.toString());	
		private Player player;
		
		public ConfirmGUI(Player p, Tier tier){
			player = p;
			ItemStack cancelItem = new ItemStack(Material.RED_DYE);
			ItemMeta cancelItemMeta = cancelItem.getItemMeta();
			cancelItemMeta.setDisplayName(Lang.GUI_CONFIRM_CANCEL.toString());
			List<String> cancelLore = new ArrayList<String>();
			cancelLore.add(Lang.GUI_CONFIRM_CANCEL_LORE.toString());
			cancelItemMeta.setLore(cancelLore);
			cancelItem.setItemMeta(cancelItemMeta);
			
			Icon cancelIcon = new Icon(cancelItem);
			cancelIcon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player p) {
					p.closeInventory();
					new MainGUI(p).open();
					return;
				}
			});
			
			ItemStack buyItem = new ItemStack(Material.LIME_DYE);
			ItemMeta buyItemMeta = buyItem.getItemMeta();
			buyItemMeta.setDisplayName(Lang.GUI_CONFIRM_BUY.toString());
			List<String> buyLore = new ArrayList<String>();
			buyLore.add(Lang.GUI_CONFIRM_BUY_LORE.toString());
			buyItemMeta.setLore(buyLore);
			buyItem.setItemMeta(buyItemMeta);
			
			Icon buyIcon = new Icon(buyItem);
			
			buyIcon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player p) {
					if(tm.purchaseTier(p, tier)) {
						tm.setPlayerSelectedTier(p.getUniqueId(), tier);
						p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(p));
						p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(p));
						p.closeInventory();
					}
					return;
				}
			});
			
			ch.setIcon(12, cancelIcon);
			ItemStack is = tier.getIcon().clone();
			ItemMeta im = is.getItemMeta();
			List<String> lore = im.getLore();
			if(tm.canPlayerBuyTier(p, tier)) {
				for(Requirement r : tier.getRequirements()) {
					if(!tier.hasRequirement(r.getRequirementType())) continue;
					lore = r.addAvailableString(tier, lore);
				}
			}else {
				lore.add(Lang.GUI_CAN_NOT_AFFORD.toString());

				for(Requirement r : tier.getRequirements()) {
					if(!tier.hasRequirement(r.getRequirementType())) continue;
					lore = r.addUnavailableString(tier, lore);
				}
			}
			im.setLore(lore);
			is.setItemMeta(im);
			ch.setIcon(13, new Icon(is));
			ch.setIcon(14, buyIcon);
		}
		public void open(){
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
		
	}
	
	
	
	
	public static GUIManager getInstance(){
		if(instance == null){
			instance = new GUIManager();
		}
		return instance;
	}
	
}
