/**
 * CustomOreGen By @author Philip Flyvholm
 * BlockEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.cryptomorin.xseries.XMaterial;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.API.GeneratorGenerateEvent;
import me.phil14052.CustomCobbleGen.API.PlayerBreakGeneratedBlock;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.GenBlock;
import me.phil14052.CustomCobbleGen.Managers.GenMode;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Managers.GeneratorModeManager;
import me.phil14052.CustomCobbleGen.Managers.PermissionManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Signs.BuySign;
import me.phil14052.CustomCobbleGen.Signs.ClickableSign;
import me.phil14052.CustomCobbleGen.Signs.GUISign;
import me.phil14052.CustomCobbleGen.Signs.SelectSign;
import me.phil14052.CustomCobbleGen.Signs.SignManager;

public class BlockEvents implements Listener{

	private TierManager tm = TierManager.getInstance();
	private BlockManager bm = BlockManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private SignManager signManager = SignManager.getInstance();
	private PermissionManager pm = new PermissionManager();
	private GeneratorModeManager genModeManager = GeneratorModeManager.getInstance();

	@EventHandler
	public void onBlockFlow(BlockFromToEvent e){
		Block b = e.getBlock();
		if(isWorldDisabled(b.getLocation().getWorld())) return;
		Material m = b.getType();
		List<GenMode> modes = genModeManager.getModesContainingMaterial(m);
		for(GenMode mode : modes) {
			if(!mode.containsLiquidBlock() || !mode.isValid() || mode.isWorldDisabled(b.getLocation().getWorld())) continue;
			Block toBlock = e.getToBlock();
			Material toBlockMaterial = toBlock.getType();
			if(toBlockMaterial.equals(Material.AIR) || mode.containsBlock(toBlockMaterial)){
				if(isGenerating(mode, m, toBlock)){
					Location l = toBlock.getLocation();
					//Checks if the block has been broken before and if it is a known gen location
					if(!bm.isGenLocationKnown(l) && mode.isSearchingForPlayersNearby()) {
						Double searchRadius = plugin.getConfig().getDouble("options.playerSearchRadius");
						Collection<Entity> entitiesNearby = l.getWorld().getNearbyEntities(l, searchRadius, searchRadius, searchRadius);
						Player closestPlayer = null;
						double closestDistance = 100D;
						for(Entity entity : entitiesNearby) {
							if(entity instanceof Player) {
								Player p = (Player) entity;
								double distance = l.distance(p.getLocation());
								if(closestPlayer == null || closestDistance > distance) {
									closestPlayer = p;
									closestDistance = distance;
								}
							}
						}
						if(closestPlayer != null) {
							bm.addKnownGenLocation(l);
							bm.setPlayerForLocation(closestPlayer.getUniqueId(), l, false);	
						}
					}
					if(bm.isGenLocationKnown(l)) {
						//it is a Known gen location
						if(!bm.getGenBreaks().containsKey(l)) return; //A player has not prev broken a block here
						//A player has prev broken a block here
						GenBlock gb = bm.getGenBreaks().get(l); //Get the GenBlock in this location
						if (gb.hasExpired()) {
							plugin.debug("GB has expired");
							bm.removeKnownGenLocation(l);
							return;
						}
						UUID uuid = gb.getUUID(); //Get the player who broke the blocks tier
						Tier tier = tm.getSelectedTier(uuid); // ^

						if(tier != null) {
							Material result = tier.getRandomResult();
							GeneratorGenerateEvent event = new GeneratorGenerateEvent(mode, tier, result, uuid, toBlock.getLocation());
							Bukkit.getPluginManager().callEvent(event);
							if(event.isCancelled()) return;
							if(event.getResult() == null) {
								plugin.log("&cUnkown material in " + event.getTierUsed().getName() + " tier.");
								return;
							}
							e.setCancelled(true);
							event.getGenerationLocation().getBlock().setType(result); //Get a random material and replace the block
							return;
						}
					}else {
						bm.addKnownGenLocation(l);
						return;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if(!signManager.areSignsEnabled()) return;
		Location l = e.getBlock().getLocation();
		if(isWorldDisabled(l.getWorld())) return;
		Player p = e.getPlayer();
		String[] lines = e.getLines();
		if(!lines[0].equalsIgnoreCase("[CCG]")) return;
		ClickableSign sign = null;
		boolean noPermission = false;
		if(lines[1].equalsIgnoreCase("GUI")) {
			if(pm.hasPermission(e.getPlayer(), "customcobblegen.signs.create.gui", true)) {
				sign = new GUISign(l);
				e.setLine(0, Lang.SIGN_GUI_0.toString());
				e.setLine(1, Lang.SIGN_GUI_1.toString());
				e.setLine(2, Lang.SIGN_GUI_2.toString());
				e.setLine(3, Lang.SIGN_GUI_3.toString());	
			} else {
				noPermission = true;
			}
		}else if(lines[1].equalsIgnoreCase("select")){
			if(pm.hasPermission(e.getPlayer(), "customcobblegen.signs.create.select", true)) {
				if(lines[2] == null) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS.toString());
				}else if(lines[3] == null || !lines[3].matches("-?\\d+")) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString());
				}else {
					String tierClass = lines[2];
					int tierLevel = Integer.parseInt(lines[3]);
					Tier tier = tm.getTierByLevel(tierClass, tierLevel);
					sign = new SelectSign(l, tier);
					if(sign.validateData()) {
		
						e.setLine(0, Lang.SIGN_SELECT_0.toString(tier));
						e.setLine(1, Lang.SIGN_SELECT_1.toString(tier));
						e.setLine(2, Lang.SIGN_SELECT_2.toString(tier));
						e.setLine(3, Lang.SIGN_SELECT_3.toString(tier));
					}else {
						p.sendMessage(Lang.TIER_NOT_FOUND.toString());
						sign = null;
					}
				}
			} else {
				noPermission = true;
			}
			
		}else if(lines[1].equalsIgnoreCase("buy")){
			if(pm.hasPermission(e.getPlayer(), "customcobblegen.signs.create.buy", true)) {
				if(lines[2] == null) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS.toString());
				}else if(lines[3] == null || !lines[3].matches("-?\\d+")) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL.toString());
				}else {
					String tierClass = lines[2];
					int tierLevel = Integer.parseInt(lines[3]);
					Tier tier = tm.getTierByLevel(tierClass, tierLevel);
					sign = new BuySign(l, tier);
					if(sign.validateData()) {
		
						e.setLine(0, Lang.SIGN_BUY_0.toString(tier));
						e.setLine(1, Lang.SIGN_BUY_1.toString(tier));
						e.setLine(2, Lang.SIGN_BUY_2.toString(tier));
						e.setLine(3, Lang.SIGN_BUY_3.toString(tier));
					}else {
						p.sendMessage(Lang.TIER_NOT_FOUND.toString());
						sign = null;
					}
				}
			} else {
				noPermission = true;
			}
			
		}

		if(sign == null) {
			if(noPermission) {
				e.setLine(0, Lang.SIGN_NO_PERMISSION_0.toString());
				e.setLine(1, Lang.SIGN_NO_PERMISSION_1.toString());
				e.setLine(2, Lang.SIGN_NO_PERMISSION_2.toString());
				e.setLine(3, Lang.SIGN_NO_PERMISSION_3.toString());
			}else {
				e.setLine(0, Lang.SIGN_NOT_VALID_0.toString());
				e.setLine(1, Lang.SIGN_NOT_VALID_1.toString());
				e.setLine(2, Lang.SIGN_NOT_VALID_2.toString());
				e.setLine(3, Lang.SIGN_NOT_VALID_3.toString());
			}
			return;
		}
		
		signManager.addSign(sign);
		p.sendMessage(Lang.PREFIX.toString() + Lang.SIGN_SUCCESS.toString());
	}
	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent e) {
		if(isWorldDisabled(e.getBlock().getWorld())) return;
		if(!plugin.getConfig().getBoolean("options.automation.pistons")) return;
		if(!bm.getKnownGenPistons().containsKey(e.getBlock().getLocation())) return;
		GenPiston piston = bm.getKnownGenPistons().get(e.getBlock().getLocation());
		Location genBlockLoc = e.getBlock().getRelative(e.getDirection()).getLocation();
		if(bm.isGenLocationKnown(genBlockLoc)) {
			piston.setHasBeenUsed(true);
			bm.setPlayerForLocation(piston.getUUID(), genBlockLoc, true);
		}
		
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(isWorldDisabled(e.getBlock().getWorld())) return;
		if(!plugin.getConfig().getBoolean("options.automation.pistons")) return;
		if(e.getBlock().getType() != XMaterial.PISTON.parseMaterial()) return;
		if(e.getPlayer() == null || !e.getPlayer().isOnline()) return;
		GenPiston piston = new GenPiston(e.getBlock().getLocation(), e.getPlayer().getUniqueId());
		bm.addKnownGenPiston(piston);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Location l = e.getBlock().getLocation();
		ClickableSign signAtLocation = signManager.getSignFromLocation(l);
		Player p = e.getPlayer();
		if(signAtLocation != null) {
			if(pm.hasPermission(e.getPlayer(), "customcobblegen.signs.create." + signAtLocation.getSignType().name().toLowerCase(), true)) {
				if(signManager.removeSign(signAtLocation)) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.SIGN_DELETED.toString());
					return;
				}
			}else {
				e.setCancelled(true);
				return;
			}
		}else {
			l.setY(l.getY()+1);
			signAtLocation = signManager.getSignFromLocation(l);
			if(signAtLocation != null) {
				e.setCancelled(true);
				return;
			}
			l.setY(l.getY()-1);
		}
		if(bm.getKnownGenPistons().containsKey(l)) {
			bm.getKnownGenPistons().remove(l);
			return;
		}
		if(isWorldDisabled(l.getWorld())) return;
		if(bm.isGenLocationKnown(l)) {
			PlayerBreakGeneratedBlock event = new PlayerBreakGeneratedBlock(p,l);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) return;
			bm.setPlayerForLocation(p.getUniqueId(), l, false);
		}
	}
	
	public boolean isWorldDisabled(World world) {
		return plugin.getConfig().getList("options.disabled.worlds").contains(world.getName());
	}
	
	private final BlockFace[] faces = new BlockFace[]{
			BlockFace.SELF,
		    BlockFace.UP,
		    BlockFace.DOWN,
		    BlockFace.NORTH,
		    BlockFace.EAST,
		    BlockFace.SOUTH,
		    BlockFace.WEST
	};
	
	private boolean isGenerating(GenMode mode, Material fromM, Block toB){
		if(!mode.isValid()) return false;
		int blocksFound = 0; /** We need all blocks to be correct */
		List<BlockFace> testedFaces = new ArrayList<>();
		if(mode.getFixedBlocks() != null && !mode.getFixedBlocks().isEmpty()) {
			for(Entry<BlockFace, Material> entry : mode.getFixedBlocks().entrySet()) {
				if(testedFaces.contains(entry.getKey())) continue;
				testedFaces.add(entry.getKey());
				if(this.isSameMaterial(entry.getValue().name(), fromM.name())) continue;  // Should not check for the original block
				Block r = toB.getRelative(entry.getKey(), 1);
				if(this.isSameMaterial(r.getType().name(), entry.getValue().name())){
					blocksFound++; /** This block is positioned correctly; */
				}else {
					return false; /** This block is not positioned correctly so we stop testing */
				}
			}	
		}
		if(mode.getBlocks() != null && !mode.getBlocks().isEmpty()) {
			for(BlockFace face : faces){
				if(testedFaces.contains(face)) continue;
				testedFaces.add(face);
				Block r = toB.getRelative(face, 1);
				if(this.isSameMaterial(r.getType().name(), fromM.name())) { // Should not check for the original block
					continue;
				}
				
				for(Material mirrorMaterial : mode.getBlocks()) {
					if(this.isSameMaterial(r.getType().name(), mirrorMaterial.name())){
						blocksFound++; /** This block is positioned correctly; */
					}
				}
			}
		}

		blocksFound++;
		int blocksNeeded = (mode.getBlocks().size() + mode.getFixedBlocks().size());
		plugin.debug("Blocks found: " + blocksFound + " - Blocks needed " + blocksNeeded + " - Success?? " + (blocksFound == blocksNeeded));
		
		return blocksFound == blocksNeeded;
	}
	

	private boolean isSameMaterial(String materialName1, String materialName2) {
		/** Version 1.12 and under have multiple names for lava and water so both needs to be tested for */
		if(materialName1.equalsIgnoreCase(materialName2)) return true;
		else if(isWater(materialName1) && isWater(materialName2)) return true;
		else if(isLava(materialName1) && isLava(materialName2)) return true;
		else return false;
	}
	
	private boolean isWater(String materialName) {
		return materialName.equalsIgnoreCase("WATER") 
		|| materialName.equalsIgnoreCase("STATIONARY_WATER");
	}


	private boolean isLava(String materialName) {
		return materialName.equalsIgnoreCase("LAVA") 
		|| materialName.equalsIgnoreCase("STATIONARY_LAVA");
	}
	
}
