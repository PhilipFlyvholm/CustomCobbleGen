package me.phil14052.CustomCobbleGen.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.EconomyManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
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
		private CustomHolder ch = new CustomHolder(guiSize, Lang.GUI_PREFIX.toString() + Lang.GUI_HOME_TITLE.toString());	
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
			Tier selectedTier = tm.getSelectedTier(p);
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
						plugin.debug(tierClass, j, firstItemInRow, itemsInRow);
						if(j == firstItemInRow) {
							int emptySpacesAtStart = (int) Math.ceil(((double)4.5-(itemsInRow/2)));
							i = (i+emptySpacesAtStart)-1;
							emptySpacesAtEnd = 9-emptySpacesAtStart-itemsInRow;
						}
					}
					ItemStack item = tier.getIcon().clone();
					ItemMeta itemMeta = item.getItemMeta();
					List<String> lore = itemMeta.getLore();
					lore.add("�a ");
					if(selectedTier != null && selectedTier.getLevel() == tier.getLevel() && selectedTier.getTierClass().equalsIgnoreCase(tier.getTierClass())) {
						GlowEnchant glow = new GlowEnchant(new NamespacedKey(plugin, "GlowEnchant"));
						itemMeta.addEnchant(glow, 1, true);
						lore.add("�aSelected");
					}else if(tm.hasPlayerPurchasedLevel(p, tier)){
						lore.add("�aClick to select");
					}else if(!tierClass.equalsIgnoreCase("DEFAULT") && !pm.hasPermission(p, "customcobblegen.generator." + tierClass, false)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(Material.BARRIER);
						if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
						lore.add("�cLocked - Missing permissions");
					}else if(!tm.hasPlayerPurchasedPreviousLevel(p, tier)){
						if(plugin.getConfig().getBoolean("options.gui.showBarrierBlockIfLocked")) item.setType(Material.BARRIER);
						if(plugin.getConfig().getBoolean("options.gui.hideInfoIfLocked")) lore = new ArrayList<String>();
						lore.add("�cLocked - Buy previous level first");
						lore.add("�c$" +  EconomyManager.getInstance().formatMoney(tier.getPrice())); 
					}else {
						if(tm.canPlayerBuyTier(p, tier)) {
							lore.add("�aClick to buy");
							lore.add("�a$" +  EconomyManager.getInstance().formatMoney(tier.getPrice())); 
						}else {
							lore.add("�cCan't afford");
							lore.add("�c$" +  EconomyManager.getInstance().formatMoney(tier.getPrice())); 
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
								tm.setPlayerSelectedTier(p, tier);
								p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(p));
								p.closeInventory();
							}else {
								//Player has not purchased the level. Now check if the player can buy the level
								if(tm.canPlayerBuyTier(p, tier)) {
									if(tm.purchaseTier(p, tier)) {
										tm.setPlayerSelectedTier(p, tier);
										p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_PURCHASED.toString(p));
										p.sendMessage(Lang.PREFIX.toString() + Lang.TIER_CHANGED.toString(p));
										p.closeInventory();
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
		
	}
	
	private int getGUISize(Map<String, List<Tier>> tiers, boolean closeLine) {
		//TODO: ADD PAGES
		int rows = 0;
		for(String tierClass : tiers.keySet()) {
			List<Tier> classTiers = tiers.get(tierClass);
			int classRows = classTiers.size()/9;
			if(classTiers.size()%9 > 0D) classRows++;
			rows += classRows;
		}
		
		/*int rows = itemSize/9;
		if(itemSize%9 > 0D) rows++;*/
		if(closeLine) rows++;
		if(rows > 6) rows = 6; //A GUI CAN MAX BE 6 ROWS
		if(rows < 1) rows = 1;
		return (rows*9);
	}
	
	@SuppressWarnings("unused")
	private int getGUISize(int itemsSize, boolean closeLine){
		//TODO: ADD PAGES
		int rows = itemsSize/9;
		if(itemsSize%9 > 0D) rows++;
		if(closeLine) rows++;
		if(rows > 6) rows = 6; //A GUI CAN MAX BE 6 ROWS
		if(rows < 1) rows = 1;
		return (rows*9);
	}
	
	
	public static GUIManager getInstance(){
		if(instance == null){
			instance = new GUIManager();
		}
		return instance;
	}
	
}
