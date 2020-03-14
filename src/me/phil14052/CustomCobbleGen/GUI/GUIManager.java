package me.phil14052.CustomCobbleGen.GUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Requirements.Requirement;
import me.phil14052.CustomCobbleGen.Utils.GlowEnchant;
import me.phil14052.CustomCobbleGen.Utils.ItemLib;

public class GUIManager {

	private static GUIManager instance = null;
	private final ItemStack backgroundItem = new ItemLib(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial(), 1, (short) 15, " ").create();
	private TierManager tm = TierManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private PermissionManager pm = new PermissionManager();
	
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
						if(XMaterial.supports(13)) {

							GlowEnchant glow = new GlowEnchant(new NamespacedKey(plugin, "GlowEnchant"));
							itemMeta.addEnchant(glow, 1, true);	
						}
						lore.add(Lang.GUI_SELECTED.toString());
					}else if(tm.hasPlayerPurchasedLevel(p, tier)){
						lore.add(Lang.GUI_SELECT.toString());
					}else if(!tierClass.equalsIgnoreCase("DEFAULT") && !pm.hasPermission(p, "customcobblegen.generator." + tierClass, false)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(XMaterial.BARRIER.parseMaterial(true));
						if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
						lore.add(Lang.GUI_LOCKED_PERMISSION.toString());
					}else if(!tm.hasPlayerPurchasedPreviousLevel(p, tier)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(XMaterial.BARRIER.parseMaterial(true));
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
	
	
	public class AdminGUI {
		private CustomHolder ch = new CustomHolder(27, Lang.GUI_ADMIN_TITLE.toString());
		private Player player;
		public AdminGUI(Player p) {
			this.player = p;
			Icon reloadIcon = new Icon(new ItemLib(XMaterial.REDSTONE_TORCH.parseMaterial(), 1, (short) 0, Lang.GUI_ADMIN_RELOAD.toString(p), Lang.GUI_ADMIN_RELOAD_LORE.toStringList(p)).create());
			Icon saveIcon = new Icon(new ItemLib(XMaterial.REDSTONE_TORCH.parseMaterial(), 1, (short) 0, Lang.GUI_ADMIN_FORCESAVE.toString(p), Lang.GUI_ADMIN_FORCESAVE_LORE.toStringList(p)).create());
			Icon forceBuyIcon = new Icon(new ItemLib(XMaterial.REDSTONE_TORCH.parseMaterial(), 1, (short) 0, Lang.GUI_ADMIN_FORCEBUY.toString(p), Lang.GUI_ADMIN_FORCEBUY_LORE.toStringList(p)).create());
			Icon giveTierIcon = new Icon(new ItemLib(XMaterial.REDSTONE_TORCH.parseMaterial(), 1, (short) 0, Lang.GUI_ADMIN_GIVETIER.toString(p), Lang.GUI_ADMIN_GIVETIER_LORE.toStringList(p)).create());
			Icon setTierIcon = new Icon(new ItemLib(XMaterial.REDSTONE_TORCH.parseMaterial(), 1, (short) 0, Lang.GUI_ADMIN_SETTIER.toString(p), Lang.GUI_ADMIN_SETTIER_LORE.toStringList(p)).create());
			
			// RELOAD CONFIG
			if(pm.hasPermisson(p, "customcobblegen.admin.reload", false)) {
				reloadIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						p.performCommand("ccg admin reload");
						p.closeInventory();
					}
					
				});
				ch.setIcon(11, reloadIcon);
			}else {
				ch.setIcon(11, getNoPermissionsIcon("customcobblegen.admin.reload"));
			}
			
			// FORCE SAVE
			if(pm.hasPermisson(p, "customcobblegen.admin.forcesave", false)) {
				saveIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						p.performCommand("ccg admin forcesave");
						p.closeInventory();
					}
					
				});
				ch.setIcon(12, saveIcon);
			}else {
				ch.setIcon(12, getNoPermissionsIcon("customcobblegen.admin.forcesave"));
			}
			
			// FORCE BUY
			if(pm.hasPermisson(p, "customcobblegen.admin.forcebuy", false)) {
				forceBuyIcon.addClickAction(new ClickAction() {

					@Override
					public void execute(Player p) {
						
						GUIManager.getInstance().new PlayerSelectGUI(p, GUIActionType.FORCEBUY).open();
					}
					
				});
				
				ch.setIcon(13, forceBuyIcon);
			}else {
				ch.setIcon(13, getNoPermissionsIcon("customcobblegen.admin.forcebuy"));
			}
			
			// GIVE TIER
			if(pm.hasPermisson(p, "customcobblegen.admin.givetier", false)) {
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
			if(pm.hasPermisson(p, "customcobblegen.admin.settier", false)) {
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
			
			for(int i = 0; i < 27; i++) {
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
				ItemStack skull = createSkull(onlinePlayer.getName(), Lang.GUI_SELECT_PLAYER_SKULL_TITLE.toString(p), Lang.GUI_SELECT_PLAYER_SKULL_LORE.toStringList(p));


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
						GUIManager.getInstance().new TierSelectGUI(p, onlinePlayer, actionType).open();;
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
			Tier selectedTier = tm.getSelectedTier(selectedPlayer.getUniqueId());
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
					String emptyString = ChatColor.translateAlternateColorCodes('&', "&a ");
					lore.add(emptyString);
					boolean clickable = true;
					if(actionType.equals(GUIActionType.SETTIER) 
							&& selectedTier != null 
							&& selectedTier.getLevel() == tier.getLevel() 
							&& selectedTier.getTierClass().equalsIgnoreCase(tier.getTierClass())) {
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
	
	enum GUIActionType {
		FORCEBUY, SETTIER, GIVETIER;
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack createSkull(String name, String displayName, List<String> lore) {
		ItemStack skull = new ItemStack(XMaterial.PLAYER_HEAD.parseMaterial(), 1);
		Damageable damageMeta = (Damageable) skull.getItemMeta();
		damageMeta.setDamage(3);
		skull.setItemMeta((ItemMeta) damageMeta);
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
	
}
