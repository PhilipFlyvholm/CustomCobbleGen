package me.phil14052.CustomCobbleGen.GUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.cryptomorin.xseries.XMaterial;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Chat.ChatReturn;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnTierClass;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnTierDescription;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnTierIcon;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnTierLevel;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnTierName;
import me.phil14052.CustomCobbleGen.Chat.ChatReturnType;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Utils.GlowEnchant;
import me.phil14052.CustomCobbleGen.Utils.ItemLib;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;

public class GUIManager {

	private static GUIManager instance = null;
	private final ItemStack backgroundItem = new ItemLib(XMaterial.GRAY_STAINED_GLASS_PANE.parseMaterial(), 1, (short) 7, " ").create();
	private TierManager tm = TierManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private PermissionManager pm = new PermissionManager();
	private Map<Player, ChatReturn> playerChatting = new HashMap<Player, ChatReturn>();
	
	public Icon getNoPermissionsIcon(String permission) {
		return new Icon(
				new ItemLib(
						XMaterial.BARRIER.parseMaterial(),
						1,
						(short) 0,
						Lang.GUI_ADMIN_NO_PERMISSION_TITLE.toString(permission),
						Arrays.asList(Lang.GUI_ADMIN_NO_PERMISSION_LORE.toString(permission))
					).create()
				);
	}
	
	public class MainGUI {
		private Map<String, List<Tier>> tiers = tm.getTiers();
		int tiersSize = tm.getTiersSize();
		int guiSize = getGUISize(tiers, false);
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_PREFIX.toString());	
		private Player player;
		private boolean failedLoad = false;
		
		public MainGUI(Player p){
			boolean centerItems = plugin.getConfig().getBoolean("options.gui.centerTiers");
			boolean newLines = plugin.getConfig().getBoolean("options.gui.seperateClassesByLines");
			player = p;
			boolean isLeader = plugin.isConnectedToIslandPlugin() ? plugin.getIslandHook().isPlayerLeader(p.getUniqueId()) : true; //Return true if there is no island plugin connected
			if(tiers == null) {
				p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIERS_DEFINED.toString());
				failedLoad = true;
				return;
			}
			int i = 0; //Current pos
			SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
			for(String tierClass : tiers.keySet()) {
				int j = 0; //Current pos in class
				List<Tier> classTiers = tiers.get(tierClass); //Tiers in current class
				
				for(Tier tier : classTiers) {
					if(j == 0) {
						if(newLines) {
							if(i != 0 && i != 9) {
								i += 9-((i)%9);	
							}
							
							if(centerItems && classTiers.size() < 9) {
								i += Math.floor(4.5-(classTiers.size()/2));
							}
						}
					}
					
					ItemStack item = tier.getIcon().clone();
					ItemMeta itemMeta = item.getItemMeta();
					List<String> lore = itemMeta.getLore();
					lore.addAll(tier.getFormatetDescription(p));
					if(plugin.getConfig().getBoolean("options.gui.showSupportedModes")) lore.add(Lang.GUI_ITEM_LORE_SUPPORTEDMODE.toString(tier));
					String emptyString = "&a ";
					emptyString = ChatColor.translateAlternateColorCodes('&', emptyString);
					lore.add(emptyString);
					if(selectedTiers != null && selectedTiers.isTierSelected(tier)) {
						if(XMaterial.supports(13)) {
							GlowEnchant glow = new GlowEnchant(new NamespacedKey(plugin, "GlowEnchant"));
							itemMeta.addEnchant(glow, 1, true);	
						}
						lore.add(Lang.GUI_SELECTED.toString());
					}else if(tm.hasPlayerPurchasedLevel(p, tier)){
						if(plugin.isConnectedToIslandPlugin() 
								&& plugin.getConfig().getBoolean("options.islands.onlyOwnerCan.select") 
								&& !isLeader) {
							//Only owners can select and player is not a owner/leader
							lore.add(Lang.GUI_SELECT_LEADER_ONLY.toString());
						}else {
							lore.add(Lang.GUI_SELECT.toString());	
						}
					}else if(!tier.doesPlayerHavePermission(p)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(XMaterial.BARRIER.parseMaterial(true));
						if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
						lore.add(Lang.GUI_LOCKED_PERMISSION.toString());
					}else if(!tm.hasPlayerPurchasedPreviousLevel(p, tier)){
						if(plugin.isConnectedToIslandPlugin() 
								&& plugin.getConfig().getBoolean("options.islands.onlyOwnerCan.buy") 
								&& !isLeader) {
							//Only owners can select and player is not a owner/leader
							lore.add(Lang.GUI_BUY_LEADER_ONLY.toString());
						}else {
							if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(XMaterial.BARRIER.parseMaterial(true));
							if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
						
							lore.add(Lang.GUI_LOCKED_PREV.toString());

							for(Requirement r : tier.getRequirements()) {
								if(!tier.hasRequirement(r.getRequirementType())) continue;
								lore = r.addUnavailableString(tier, lore);
							}
						}
					}else {
						if(plugin.isConnectedToIslandPlugin() 
								&& plugin.getConfig().getBoolean("options.islands.onlyOwnerCan.buy") 
								&& !isLeader) {
							//Only owners can select and player is not a owner/leader
							lore.add(Lang.GUI_BUY_LEADER_ONLY.toString());
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
								if(plugin.isConnectedToIslandPlugin() 
										&& plugin.getConfig().getBoolean("options.islands.onlyOwnerCan.select") 
										&& !isLeader) {
									return;
								}
								SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
								selectedTiers.addTier(tier);
								tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
								p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(tier));
								if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.getConfig().getBoolean("options.islands.sendMessagesToTeam")) {
									plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX.toString() + Lang.TIER_CHANGED_BY_TEAM.toString(p, tier),
													p.getUniqueId(),
													true);
								}
								p.closeInventory();
							}else {
								if(plugin.isConnectedToIslandPlugin() 
										&& plugin.getConfig().getBoolean("options.islands.onlyOwnerCan.buy") 
										&& !isLeader) {
									return;
								}
								//Player has not purchased the level. Now check if the player can buy the level
								if(tm.canPlayerBuyTier(p, tier)) {
									if(plugin.getConfig().getBoolean("options.gui.confirmpurchases")) {
										new ConfirmGUI(p, tier).open();	
									}else {
										if(tm.purchaseTier(p, tier)) {
											SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
											selectedTiers.addTier(tier);
											tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
											p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(tier));
											p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(tier));
											if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.getConfig().getBoolean("options.islands.sendMessagesToTeam")) {
												plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX.toString() + Lang.TIER_PURCHASED_BY_TEAM.toString(p, tier),
														p.getUniqueId(),
														true);
												plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX.toString() + Lang.TIER_CHANGED_BY_TEAM.toString(p, tier),
																p.getUniqueId(),
																true);
											}
											p.closeInventory();
										}
									}
								}
							}
						}
					});
					ch.setIcon(i, icon);
					//numOfPreviousTiers += classTiers.size();
					if(j >= classTiers.size() && centerItems) {
						i += Math.ceil(4.5-(classTiers.size()/2));
					}
					j++;
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
			boolean newLines = plugin.getConfig().getBoolean("options.gui.seperateClassesByLines");
			if(newLines) {
				for(String tierClass : tiers.keySet()) {
					List<Tier> classTiers = tiers.get(tierClass);
					int classRows = classTiers.size()/9;
					if(classTiers.size()%9 > 0D) classRows++;
					rows += classRows;
				}	
			}else {
				int size = 0;
				for(String tierClass : tiers.keySet()) {
					size += tiers.get(tierClass).size();
				}
				int classRows = size/9;
				if(size%9 > 0D) classRows++;
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
		
		@SuppressWarnings("deprecation")
		public ConfirmGUI(Player p, Tier tier){
			player = p;
			ItemStack cancelItem;
			if(!XMaterial.supports(13)) {
				cancelItem = new ItemStack(Material.matchMaterial("INK_SACK"), 1, (short) 1);
			}else {
				cancelItem = new ItemStack(XMaterial.RED_DYE.parseMaterial());
			}
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
			
			ItemStack buyItem = XMaterial.LIME_DYE.parseItem(true);
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
						SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
						selectedTiers.addTier(tier);
						tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
						p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(tier));
						p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(tier));
						if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.getConfig().getBoolean("options.islands.sendMessagesToTeam")) {
							plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX.toString() + Lang.TIER_PURCHASED_BY_TEAM.toString(p, tier),
									p.getUniqueId(),
									true);
							plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX.toString() + Lang.TIER_CHANGED_BY_TEAM.toString(p, tier),
											p.getUniqueId(),
											true);
						}
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
	
	public class UpgradeGUI {
		private int GUISize = 9;
		private CustomHolder ch = new CustomHolder(GUISize, Lang.GUI_UPGRADE_TITLE.toString());
		private Player player;
		
		public UpgradeGUI(Player p, List<Tier> nextTiers) {
			int i = 0;
			boolean isEven = (nextTiers.size() % 2) == 0;
			this.player = p;
			for(Tier tier : nextTiers) {
				ItemStack item = tier.getIcon().clone();
				ItemMeta itemMeta = item.getItemMeta();
				List<String> lore = itemMeta.getLore();
				lore.addAll(tier.getFormatetDescription(p));
				if(plugin.getConfig().getBoolean("options.gui.showSupportedModes")) lore.add(Lang.GUI_ITEM_LORE_SUPPORTEDMODE.toString(tier));
				String emptyString = "&a ";
				emptyString = ChatColor.translateAlternateColorCodes('&', emptyString);
				lore.add(emptyString);
				if(tm.canPlayerBuyTier(p, tier)) {
					lore.add(Lang.GUI_UPGRADE_LORE_UPGRADE.toString());
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
				itemMeta.setLore(lore);
				item.setItemMeta(itemMeta);
				Icon icon = new Icon(item);
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player p) {
						//Check if the player has purchased the level
						if(tm.hasPlayerPurchasedLevel(p, tier)) {
							SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
							selectedTiers.addTier(tier);
							tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
							p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(tier));
							p.closeInventory();
						}else {
							//Player has not purchased the level. Now check if the player can buy the level
							if(tm.canPlayerBuyTier(p, tier)) {
								if(tm.purchaseTier(p, tier)) {
									SelectedTiers selectedTiers = tm.getSelectedTiers(p.getUniqueId());
									selectedTiers.addTier(tier);
									tm.setPlayerSelectedTiers(p.getUniqueId(), selectedTiers);
									p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(tier));
									p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(tier));
									if(plugin.getConfig().getBoolean("options.islands.usePerIslandUnlockedGenerators") && plugin.getConfig().getBoolean("options.islands.sendMessagesToTeam")) {
										plugin.getIslandHook().sendMessageToIslandMembers(Lang.PREFIX.toString() + Lang.TIER_UPGRADED_BY_TEAM.toString(p, tier),
												p.getUniqueId(),
												true);
									}
									p.closeInventory();
								}
							}
						}
					}
				});
				int position = i+(GUISize-nextTiers.size())/2;
				if(isEven && i > (nextTiers.size()-1)/2) position++;
				
				ch.setIcon(position, icon);
				i++;
			}
		}

		public void open(){
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
	}
	
	public class AdminGUI {
		private int GUISize = 27;
		private CustomHolder ch = new CustomHolder(GUISize, Lang.GUI_ADMIN_TITLE.toString());
		private Player player;
		public AdminGUI(Player p) {
			this.player = p;
			Material redstoneTorch = null;
			if(XMaterial.supports(13)) {
				redstoneTorch = Material.REDSTONE_TORCH;
			}else {
				redstoneTorch = Material.matchMaterial("REDSTONE_TORCH_ON");
			}
			Icon reloadIcon = new Icon(new ItemLib(redstoneTorch, 1, (short) 0, Lang.GUI_ADMIN_RELOAD.toString(p), Lang.GUI_ADMIN_RELOAD_LORE.toStringList(p)).create());
			Icon saveIcon = new Icon(new ItemLib(redstoneTorch, 1, (short) 0, Lang.GUI_ADMIN_FORCESAVE.toString(p), Lang.GUI_ADMIN_FORCESAVE_LORE.toStringList(p)).create());
			Icon forceBuyIcon = new Icon(new ItemLib(redstoneTorch, 1, (short) 0, Lang.GUI_ADMIN_FORCEBUY.toString(p), Lang.GUI_ADMIN_FORCEBUY_LORE.toStringList(p)).create());
			Icon giveTierIcon = new Icon(new ItemLib(redstoneTorch, 1, (short) 0, Lang.GUI_ADMIN_GIVETIER.toString(p), Lang.GUI_ADMIN_GIVETIER_LORE.toStringList(p)).create());
			Icon setTierIcon = new Icon(new ItemLib(redstoneTorch, 1, (short) 0, Lang.GUI_ADMIN_SETTIER.toString(p), Lang.GUI_ADMIN_SETTIER_LORE.toStringList(p)).create());
			Icon withdrawIcon = new Icon(new ItemLib(redstoneTorch, 1, (short) 0, Lang.GUI_ADMIN_WITHDRAW.toString(p), Lang.GUI_ADMIN_WITHDRAW_LORE.toStringList(p)).create());
			
			/*Icon createdTierIcon = new Icon(new ItemLib(XMaterial.PAPER.parseMaterial(), 1, (short) 0, Lang.GUI_ADMIN_CREATETIER.toString(p), Lang.GUI_ADMIN_CRAETETIER_LORE.toStringList(p)).create());
			
			//Create tier
			if(pm.hasPermission(p, "customcobblegen.admin.create", false)) {
				createdTierIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						GUIManager.getInstance().new CreateTierGUI(p, null).open();
					}
				});
				ch.setIcon(11, createdTierIcon);
			}else {
				ch.setIcon(11, getNoPermissionsIcon("customcobblegen.admin.reload"));
			}*/
			
			// RELOAD CONFIG
			if(pm.hasPermission(p, "customcobblegen.admin.reload", false)) {
				reloadIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						p.performCommand("ccg admin reload");
						p.closeInventory();
					}
					
				});
				ch.setIcon(10, reloadIcon);
			}else {
				ch.setIcon(10, getNoPermissionsIcon("customcobblegen.admin.reload"));
			}
			
			// FORCE SAVE
			if(pm.hasPermission(p, "customcobblegen.admin.forcesave", false)) {
				saveIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						p.performCommand("ccg admin forcesave");
						p.closeInventory();
					}
					
				});
				ch.setIcon(11, saveIcon);
			}else {
				ch.setIcon(11, getNoPermissionsIcon("customcobblegen.admin.forcesave"));
			}
			
			// FORCE BUY
			if(pm.hasPermission(p, "customcobblegen.admin.forcebuy", false)) {
				forceBuyIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						
						GUIManager.getInstance().new PlayerSelectGUI(p, GUIActionType.FORCEBUY).open();
					}
					
				});
				
				ch.setIcon(12, forceBuyIcon);
			}else {
				ch.setIcon(12, getNoPermissionsIcon("customcobblegen.admin.forcebuy"));
			}
			
			// GIVE TIER
			if(pm.hasPermission(p, "customcobblegen.admin.givetier", false)) {
				giveTierIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						
						GUIManager.getInstance().new PlayerSelectGUI(p, GUIActionType.GIVETIER).open();
					}
					
				});
				ch.setIcon(14, giveTierIcon);
			}else {
				ch.setIcon(14, getNoPermissionsIcon("customcobblegen.admin.givetier"));
			}
			
			// SET TIER
			if(pm.hasPermission(p, "customcobblegen.admin.settier", false)) {
				setTierIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						
						GUIManager.getInstance().new PlayerSelectGUI(p, GUIActionType.SETTIER).open();
					}
					
				});
				
				ch.setIcon(15, setTierIcon);
			}else {
				ch.setIcon(15, getNoPermissionsIcon("customcobblegen.admin.settier"));
			}
			// SET TIER
			if(pm.hasPermission(p, "customcobblegen.admin.withdraw", false)) {
				withdrawIcon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player p) {	
						GUIManager.getInstance().new PlayerSelectGUI(p, GUIActionType.WITHDRAW).open();
					}
								
				});
				ch.setIcon(16, withdrawIcon);
			}else {
				ch.setIcon(16, getNoPermissionsIcon("customcobblegen.admin.withdraw"));
			}
			
			for(int i = 0; i < GUISize; i++) {
				if(ch.getIcon(i) == null) {
					ch.setIcon(i, new Icon(backgroundItem));
				}
			}
			
		}
		public void open(){
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
	}
	
	public class CreateTierGUI{
		private int GUISize = 27;
		private CustomHolder ch = new CustomHolder(GUISize, Lang.GUI_CREATE_TITLE.toString());
		private Player player;
		
		public CreateTierGUI(Player p, Tier createdTier) {
			this.player = p;
			boolean tierCreated = createdTier != null;
			Material paperMaterial = XMaterial.PAPER.parseMaterial();
			
			List<String> classIconLore = Lang.GUI_CREATE_CLASS_LORE.toStringList(tierCreated 
					&& createdTier.getTierClass() != null 
						? "\"" + createdTier.getTierClass() + "\""
						: Lang.GUI_CREATE_EMPTY.toString());
			classIconLore.add(Lang.GUI_CREATE_REQUIRED.toString());
			Icon classIcon = new Icon(new ItemLib(paperMaterial, 1, (short) 0, Lang.GUI_CREATE_CLASS.toString(p), classIconLore).create());
			classIcon.addClickAction(new ClickAction() {

				@Override
				public void execute(Player p) {
					addPlayerChatting(p, createdTier, ChatReturnType.CLASS);
					for(String s : Lang.CHATINPUT_INFO_CLASS.toStringList(p)) {
						p.sendMessage(Lang.PREFIX.toString() + s);
					}
					p.sendMessage(Lang.PREFIX.toString() + Lang.CHATINPUT_INFO_CANCEL.toString("CANCEL"));
					p.closeInventory();
				}
				
			});
			
			
			List<String> levelIconLore = Lang.GUI_CREATE_LEVEL_LORE.toStringList(tierCreated 
					&& createdTier.getLevel() >= 0 
						? createdTier.getLevel() + "" 
						: Lang.GUI_CREATE_EMPTY.toString());
			levelIconLore.add(Lang.GUI_CREATE_REQUIRED.toString());
			Icon levelIcon = new Icon(new ItemLib(paperMaterial, 1, (short) 0, Lang.GUI_CREATE_LEVEL.toString(p), levelIconLore).create());
			levelIcon.addClickAction(new ClickAction() {

				@Override
				public void execute(Player p) {
					addPlayerChatting(p, createdTier, ChatReturnType.LEVEL);
					for(String s : Lang.CHATINPUT_INFO_LEVEL.toStringList(p)) {
						p.sendMessage(Lang.PREFIX.toString() + s);
					}
					p.sendMessage(Lang.PREFIX.toString() + Lang.CHATINPUT_INFO_CANCEL.toString("CANCEL"));
					p.closeInventory();
				}
				
			});
			
			
			List<String> nameIconLore = Lang.GUI_CREATE_NAME_LORE.toStringList(tierCreated 
					&& createdTier.getName() != null 
					&& !createdTier.getName().trim().equals("") 
						? "\"" + ChatColor.translateAlternateColorCodes('&', createdTier.getName() + "&a") + "\""
						: Lang.GUI_CREATE_EMPTY.toString());
			nameIconLore.add(Lang.GUI_CREATE_REQUIRED.toString());
			Icon nameIcon = new Icon(new ItemLib(paperMaterial, 1, (short) 0, Lang.GUI_CREATE_NAME.toString(p), nameIconLore).create());
			nameIcon.addClickAction(new ClickAction() {

				@Override
				public void execute(Player p) {
					addPlayerChatting(p, createdTier, ChatReturnType.NAME);
					for(String s : Lang.CHATINPUT_INFO_NAME.toStringList(p)) {
						p.sendMessage(Lang.PREFIX.toString() + s);
					}
					p.sendMessage(Lang.PREFIX.toString() + Lang.CHATINPUT_INFO_CANCEL.toString("CANCEL"));
					p.closeInventory();
				}
				
			});
			
			
			boolean iconMaterialSet = tierCreated && createdTier.getIcon() != null && !createdTier.getIcon().getType().equals(Material.AIR);
			List<String> materialIconLore = Lang.GUI_CREATE_ICON_LORE.toStringList(iconMaterialSet
						? "\"" + createdTier.getIcon().getType().name() + "\"" 
						: Lang.GUI_CREATE_EMPTY.toString());
			Icon materialIcon = new Icon(new ItemLib(iconMaterialSet ? createdTier.getIcon().getType() : paperMaterial, 1, (short) 0, Lang.GUI_CREATE_ICON.toString(p), materialIconLore).create());
			materialIcon.addClickAction(new ClickAction() {

				@Override
				public void execute(Player p) {
					addPlayerChatting(p, createdTier, ChatReturnType.ICON);
					for(String s : Lang.CHATINPUT_INFO_ICON.toStringList(p)) {
						p.sendMessage(Lang.PREFIX.toString() + s);
					}
					p.sendMessage(Lang.PREFIX.toString() + Lang.CHATINPUT_INFO_CANCEL.toString("CANCEL"));
					p.closeInventory();
				}
				
			});
			
			
			List<String> resultsIconLore = Lang.GUI_CREATE_RESULTS_LORE.toStringList();
			if(tierCreated && createdTier.getResults() != null && !createdTier.getResults().isEmpty()) {
				resultsIconLore.addAll(createdTier.getResultsLore(createdTier.getResults()));
			}else {
				resultsIconLore.add(Lang.GUI_CREATE_EMPTY.toString());
			}
			resultsIconLore.add(Lang.GUI_CREATE_REQUIRED.toString());
			Icon resultsIcon = new Icon(new ItemLib(paperMaterial, 1, (short) 0, Lang.GUI_CREATE_RESULTS.toString(p), resultsIconLore).create());
			
			
			
			List<String> requirementsIconLore = Lang.GUI_CREATE_REQUIREMENTS_LORE.toStringList(tierCreated && createdTier.getRequirements() != null ? createdTier.getRequirements().size() + "" : "0");
			Icon requirementsIcon = new Icon(new ItemLib(paperMaterial, 1, (short) 0, Lang.GUI_CREATE_REQUIREMENTS.toString(p), requirementsIconLore).create());
			
			List<String> descriptionIconLore = Lang.GUI_CREATE_DESCRIPTION_LORE.toStringList();
			if(tierCreated && createdTier.hasDescription()) {
				descriptionIconLore.addAll(createdTier.getDescription());
			}else {
				descriptionIconLore.add(Lang.GUI_CREATE_EMPTY.toString());
			}
			Icon descriptionIcon = new Icon(new ItemLib(paperMaterial, 1, (short) 0, Lang.GUI_CREATE_DESCRIPTION.toString(p), descriptionIconLore).create());
			descriptionIcon.addClickAction(new ClickAction() {

				@Override
				public void execute(Player p) {
					addPlayerChatting(p, createdTier, ChatReturnType.DESCRIPTION);
					for(String s : Lang.CHATINPUT_INFO_DESCRIPTION.toStringList("%n%", "REMOVE")) {
						p.sendMessage(Lang.PREFIX.toString() + s);
					}
					p.sendMessage(Lang.PREFIX.toString() + Lang.CHATINPUT_INFO_CANCEL.toString("CANCEL"));
					p.closeInventory();
				}
				
			});
			
			
			ch.setIcon(10, classIcon);
			ch.setIcon(11, levelIcon);
			ch.setIcon(12, nameIcon);
			ch.setIcon(13, resultsIcon);
			ch.setIcon(14, materialIcon);
			ch.setIcon(15, requirementsIcon);
			ch.setIcon(16, descriptionIcon);
			
			for(int i = 0; i < GUISize; i++) {
				if(ch.getIcon(i) == null) {
					ch.setIcon(i, new Icon(backgroundItem));
				}
			}
		}
		
		
		
		public void open() {
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
	}
	
	public class ResultsEditTierGUI{
		private int GUISize = 27;
		private CustomHolder ch = new CustomHolder(GUISize, Lang.GUI_CREATE_TITLE.toString());
		private Player player;
		
		public ResultsEditTierGUI(Player p, Tier createdTier) {
			this.player = p;
			boolean tierCreated = createdTier != null;
			@SuppressWarnings("unused")
			Material paperMaterial = XMaterial.PAPER.parseMaterial();
			Map<Material, Double> results = null;
			if(tierCreated) {
				results = createdTier.getResults();
			}else {
				results = new HashMap<Material, Double>();
				results.put(Material.COBBLESTONE, 100.0);
			}
			int row = 0;
			for(Material m : results.keySet()) {
				ItemStack materialIs = new ItemStack(m);
				ItemMeta materialIm = materialIs.getItemMeta();
				materialIm.setDisplayName("�6�l" + m.name());
				materialIs.setItemMeta(materialIm);
				Icon materialIcon = new Icon(materialIs);
				materialIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						addPlayerChatting(p, createdTier, ChatReturnType.MATERIAL);
						for(String s : Lang.CHATINPUT_INFO_RESULTS_MATERIAL.toStringList(p)) {
							p.sendMessage(Lang.PREFIX.toString() + s);
						}
						p.sendMessage(Lang.PREFIX.toString() + Lang.CHATINPUT_INFO_CANCEL.toString("CANCEL"));
						p.closeInventory();
					}
					
				});
				ch.setIcon(row, materialIcon);
			}
			
		}
		
		
		
		public void open() {
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
	}
	
	public class PlayerSelectGUI {
		private int guiSize = 54;
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_SELECT_TITLE.toString());
		private Player player;
		
		public PlayerSelectGUI(Player p, GUIActionType actionType) {
			this(p, 0,actionType, Bukkit.getOnlinePlayers());
		}
		
		public PlayerSelectGUI(Player p, int pageIndex, GUIActionType actionType, Collection<? extends Player> players) {
			player = p;
			int i = 0;
			int totalPage = players.size()/45; // Max of 45 player pr. page - 0 = first page
			int playerIndex = 0;
			for(Player onlinePlayer : players) {
				if(playerIndex < 45*pageIndex) continue;
				ItemStack skull = createSkull(onlinePlayer.getName(), Lang.GUI_SELECT_PLAYER_SKULL_TITLE.toString(onlinePlayer), Lang.GUI_SELECT_PLAYER_SKULL_LORE.toStringList(onlinePlayer));


				if(plugin.getConfig().getBoolean("debug")) {
					ItemMeta skullMeta = skull.getItemMeta();
					List<String> lore = skullMeta.getLore();
					lore.add(" ");
					lore.add("i: " + i);
					skullMeta.setLore(lore);
					skull.setItemMeta(skullMeta);
				}
				
				Icon skullIcon = new Icon(skull);
				
				skullIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						if(actionType.equals(GUIActionType.WITHDRAW)) {

							GUIManager.getInstance().new WithdrawGUI(p, onlinePlayer).open();
						}else {
							GUIManager.getInstance().new TierSelectGUI(p, onlinePlayer, actionType).open();	
						}
					}
					
				});
				ch.setIcon(i, skullIcon);
				i++;
			}
			for(int j = 0; j < 9; j++) {
				ch.setIcon(45+j, new Icon(backgroundItem));
			}
			Icon closeIcon = new Icon(new ItemLib(Material.BARRIER, 1, (short) 0, Lang.GUI_CLOSE_TITLE.toString(p), Lang.GUI_CLOSE_LORE.toStringList(p)).create());
			closeIcon.addClickAction(new ClickAction() {

				@Override
				public void execute(Player p) {
					p.closeInventory();					
				}
				
			});
			Icon previousPage = null;
			if(pageIndex <= 0) {
				previousPage = new Icon(createSkull("MHF_ArrowLeft", Lang.GUI_PREVIOUSPAGE_FIRST_TITLE.toString(p), Lang.GUI_PREVIOUSPAGE_FIRST_LORE.toStringList(p)));
			}else {

				previousPage = new Icon(createSkull("MHF_ArrowLeft", Lang.GUI_PREVIOUSPAGE_TITLE.toString(p), Lang.GUI_PREVIOUSPAGE_LORE.toStringList(p)));
				previousPage.addClickAction(new ClickAction() {
					@Override
					public void execute(Player p) {
						if(pageIndex <= 0) return;
						GUIManager.getInstance().new PlayerSelectGUI(p, (pageIndex-1), actionType, players).open();
					}
				});
			}
			
			
			Icon nextPage = null;
			if(pageIndex == totalPage) {
				nextPage = new Icon(createSkull("MHF_ArrowRight", Lang.GUI_NEXTPAGE_LAST_TITLE.toString(p), Lang.GUI_NEXTPAGE_LAST_LORE.toStringList(p)));
			}else {

				nextPage = new Icon(createSkull("MHF_ArrowRight", Lang.GUI_NEXTPAGE_TITLE.toString(p), Lang.GUI_NEXTPAGE_LORE.toStringList(p)));
				nextPage.addClickAction(new ClickAction() {
					@Override
					public void execute(Player p) {
						if(pageIndex >= totalPage) return;
						GUIManager.getInstance().new PlayerSelectGUI(p, (pageIndex+1), actionType, players).open();
					}
				});
			}
			
			
			ch.setIcon(guiSize-6, previousPage);
			ch.setIcon(guiSize-5, closeIcon);
			ch.setIcon(guiSize-4, nextPage);
		}
	
		
		public void open(){
			Inventory inventory = ch.getInventory();
			player.openInventory(inventory);
		}
	}
	
	public class TierSelectGUI {
		private Map<String, List<Tier>> tiers = tm.getTiers();
		int tiersSize = tm.getTiersSize();
		int guiSize = getGUISize(tiers, false);
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_SELECT_TIER_TITLE.toString());	
		private Player player;
		private boolean failedLoad = false;
		
		public TierSelectGUI(Player p, Player selectedPlayer, GUIActionType actionType){
			player = p;
			int i = 0;
			if(tiers == null) {
				p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIERS_DEFINED.toString());
				failedLoad = true;
				return;
			}
			SelectedTiers selectedTiers = tm.getSelectedTiers(selectedPlayer.getUniqueId());
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
					lore.addAll(tier.getFormatetDescription(selectedPlayer));
					String emptyString = ChatColor.translateAlternateColorCodes('&', "&a ");
					lore.add(emptyString);
					boolean clickable = true;
					if(actionType.equals(GUIActionType.SETTIER) 
							&& selectedTiers != null 
							&& selectedTiers.isTierSelected(tier)) {
						lore.add(Lang.GUI_SELECT_TIER_ALREADY_SELECTED.toString(p));
						clickable = false;
					}else if((actionType.equals(GUIActionType.GIVETIER) || actionType.equals(GUIActionType.FORCEBUY))
							&& tm.hasPlayerPurchasedLevel(selectedPlayer, tier)) {
						lore.add(Lang.GUI_SELECT_TIER_ALREADY_OWN.toString(p));
						clickable = false;
					}else {
						lore.add(Lang.GUI_SELECT.toString());
					}

					if(plugin.getConfig().getBoolean("debug")) {
						lore.add(" ");
						lore.add("i: " + i);
					}
					
					itemMeta.setLore(lore);
					item.setItemMeta(itemMeta);
					Icon icon = new Icon(item);
					if(clickable) {
						icon.addClickAction(new ClickAction() {
							@Override
							public void execute(Player p) {
								if(actionType.equals(GUIActionType.SETTIER)) {
									// RUNS /CCG ADMIN SETTIER (PLAYER) (CLASS) (LEVEL)
									p.performCommand("ccg admin settier " + selectedPlayer.getName() + " " + tierClass + " " + tier.getLevel());
								}else if(actionType.equals(GUIActionType.GIVETIER)) {
									// RUNS /CCG ADMIN GIVETIER (PLAYER) (CLASS) (LEVEL)
									p.performCommand("ccg admin givetier " + selectedPlayer.getName() + " " + tierClass + " " + tier.getLevel());
								}else if(actionType.equals(GUIActionType.FORCEBUY)) {
									// RUNS /CCG ADMIN FORCEBUY (PLAYER) (CLASS) (LEVEL)
									p.performCommand("ccg admin forcebuy " + selectedPlayer.getName() + " " + tierClass + " " + tier.getLevel());
								}

								p.closeInventory();
							}
						});
					}
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
	
	public class WithdrawGUI {
		private Map<String, List<Tier>> tiers = tm.getTiers();
		int tiersSize = tm.getTiersSize();
		int guiSize = getGUISize(tiers, false);
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_SELECT_TIER_TITLE.toString());	
		private Player player;
		private boolean failedLoad = false;
		
		public WithdrawGUI(Player p, Player selectedPlayer){
			player = p;
			int i = 0;
			if(tiers == null) {
				p.sendMessage(Lang.PREFIX.toString() + Lang.NO_TIERS_DEFINED.toString());
				failedLoad = true;
				return;
			}
			List<Tier> purchasedTiers = tm.getPlayersPurchasedTiers(selectedPlayer.getUniqueId());
			for(Tier tier : purchasedTiers) {
				ItemStack item = tier.getIcon().clone();
				ItemMeta itemMeta = item.getItemMeta();
				List<String> lore = itemMeta.getLore();
				lore.addAll(tier.getFormatetDescription(selectedPlayer));
				String emptyString = ChatColor.translateAlternateColorCodes('&', "&a ");
				lore.add(emptyString);
				lore.add(Lang.GUI_ADMIN_WITHDRAW_SELECT.toString());

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
						p.performCommand("ccg admin withdraw " + selectedPlayer.getName() + " " + tier.getTierClass() + " " + tier.getLevel());
						p.closeInventory();
					}
				});

				ch.setIcon(i, icon);
				i++;
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
	
	enum GUIActionType {
		FORCEBUY, SETTIER, GIVETIER, WITHDRAW;
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack createSkull(String name, String displayName, List<String> lore) {
		ItemStack skull = null;
		if(XMaterial.supports(13)) {
			skull = new ItemStack(XMaterial.PLAYER_HEAD.parseMaterial(), 1);
			Damageable damageMeta = (Damageable) skull.getItemMeta();
			damageMeta.setDamage(3);
			skull.setItemMeta((ItemMeta) damageMeta);
		}else {
			skull = new ItemStack(XMaterial.PLAYER_HEAD.parseMaterial(), 1, (byte) 3);
		}
		SkullMeta meta = ((SkullMeta) skull.getItemMeta());
		meta.setOwner(name);
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		skull.setItemMeta(meta);
		return skull;
	}
	
	public static GUIManager getInstance(){
		if(instance == null){
			instance = new GUIManager();
		}
		return instance;
	}

	public Map<Player, ChatReturn> getPlayerChatting() {
		return playerChatting;
	}

	public void setPlayerChatting(Map<Player, ChatReturn> playerChatting) {
		this.playerChatting = playerChatting;
	}
	
	public boolean isPlayerChatting(Player p) {
		return this.getPlayerChatting().containsKey(p);
	}
	
	public ChatReturn getPlayersReturn(Player p) {
		return this.getPlayerChatting().get(p);
	}
	
	public void removePlayerChatting(Player p) {
		if(this.isPlayerChatting(p)) this.getPlayerChatting().remove(p);
	}
	
	public void addPlayerChatting(Player p, Tier tier, ChatReturnType type) {
		if(type == null) {
			plugin.error("&cTYPE IS NULL IN GUIManager.addPlayerChatting(Player p, Tier tier, ChatReturnType type)");
			return;
		}
		ChatReturn chatReturn = null;
		if(type.equals(ChatReturnType.CLASS)) {
			chatReturn = new ChatReturnTierClass(p, tier);
		}else if(type.equals(ChatReturnType.LEVEL)) {
			chatReturn = new ChatReturnTierLevel(p, tier);
		}else if(type.equals(ChatReturnType.NAME)) {
			chatReturn = new ChatReturnTierName(p, tier);
		}else if(type.equals(ChatReturnType.DESCRIPTION)) {
			chatReturn = new ChatReturnTierDescription(p, tier);
		}else if(type.equals(ChatReturnType.ICON)) {
			chatReturn = new ChatReturnTierIcon(p, tier);
		}else if(type.equals(ChatReturnType.ICON)) {
			chatReturn = new ChatReturnTierIcon(p, tier);
		}else {
			plugin.error( type.name() + " does not have a class yet!");
			return;
		}
		
		if(this.isPlayerChatting(p)) this.removePlayerChatting(p);
		this.getPlayerChatting().put(p, chatReturn);
	}
	
}
