/**
 * CustomOreGen By @author Philip Flyvholm
 * BlockEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import com.cryptomorin.xseries.XMaterial;
import me.phil14052.CustomCobbleGen.API.GeneratorGenerateEvent;
import me.phil14052.CustomCobbleGen.API.PlayerBreakGeneratedBlock;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Managers.*;
import me.phil14052.CustomCobbleGen.Signs.*;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class BlockEvents implements Listener{

	private final TierManager tm = TierManager.getInstance();
	private final BlockManager bm = BlockManager.getInstance();
	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private final SignManager signManager = SignManager.getInstance();
	private final PermissionManager pm = new PermissionManager();
	private final GeneratorModeManager genModeManager = GeneratorModeManager.getInstance();

	@EventHandler
	public void onBlockFlow(BlockFromToEvent e){
		Block b = e.getBlock();
		if(isWorldDisabled(Objects.requireNonNull(b.getLocation().getWorld()))) return;

		Material m = b.getType();
		if(XMaterial.supports(13) && Setting.SUPPORTWATERLOGGEDBLOCKS.getBoolean()) { //Check for waterlogged block
			if(b.getBlockData() instanceof Waterlogged waterloggedBlock) { //Checked separately so prev 1.13 do not try to use a class that does not exist
				if(waterloggedBlock.isWaterlogged()) m = Material.WATER; //If it is waterlogged then check for generators as if it is a water block. In the future check if stairs are flowing the right way. Possible bug here
			}
		}

		List<GenMode> modes = genModeManager.getModesContainingMaterial(m);
		for(GenMode mode : modes) {
			if(!mode.containsLiquidBlock() || !mode.isValid() || mode.isWorldDisabled(b.getLocation().getWorld())) continue;

			Block toBlock = e.getToBlock();
			Material toBlockMaterial = toBlock.getType();
			if(XMaterial.supports(13) && Setting.SUPPORTWATERLOGGEDBLOCKS.getBoolean()) { //Check for waterlogged block
				if(toBlock.getBlockData() instanceof Waterlogged waterloggedBlock) { //Checked separately so prev 1.13 do not try to use a class that does not exist
					if(waterloggedBlock.isWaterlogged()) { //If it is waterlogged then check for generators as if it is a water block. In the future check if stairs are flowing the right way. Possible bug here
						toBlockMaterial = Material.WATER;
					}
				}
			}

			if(toBlockMaterial.equals(Material.AIR) || mode.containsBlock(toBlockMaterial)){
				if(isGenerating(mode, m, toBlock)){
					Location l = toBlock.getLocation();
					if(l.getWorld() == null) return;
					//Checks if the block has been broken before and if it is a known gen location
					if(!bm.isGenLocationKnown(l) && mode.isSearchingForPlayersNearby()) {
						double searchRadius = Setting.PLAYERSEARCHRADIUS.getDouble();
						if(l.getWorld() == null) return;
						Collection<Entity> entitiesNearby = l.getWorld().getNearbyEntities(l, searchRadius, searchRadius, searchRadius);
						Player closestPlayer = getClosestPlayer(l, entitiesNearby);
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
							plugin.debug("GB has expired", gb.getLocation());
							bm.removeKnownGenLocation(l);
							return;
						}
						
						UUID uuid = gb.getUUID(); //Get the uuid of the player who broke the blocks tier
						SelectedTiers selectedTiers = tm.getSelectedTiers(uuid); // ^
						if(selectedTiers == null) return;

						Tier tier = selectedTiers.getSelectedTiersMap().get(mode);

						if(tier == null) tier = selectedTiers.getSelectedTiersMap().get(genModeManager.getUniversalGenMode());

						if(!mode.canGenerateWhileRaining() && toBlock.getWorld().hasStorm()) {
							e.setCancelled(true);
							if(!toBlock.getLocation().getBlock().getType().equals(Material.COBBLESTONE)) toBlock.getLocation().getBlock().setType(Material.COBBLESTONE);
							return;
						}

						float soundVolume = Setting.SOUND_VOLUME.getFloat();
						float pitch = Setting.SOUND_PITCH.getFloat();
						Material result = null;
						if(tier != null){
							result = tier.getRandomResult();
						}
						else if (mode.hasFallBackMaterial()){
							result = mode.getFallbackMaterial();
						}

						GeneratorGenerateEvent event = new GeneratorGenerateEvent(mode, tier, result, uuid, toBlock.getLocation());
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) return;
						e.setCancelled(true);
						if(event.getResult() == null) {
							plugin.error("&cUnknown material in " + event.getTierUsed().getName() + " tier.", true);
							return;
						}
						event.getGenerationLocation().getBlock().setType(event.getResult()); //Get a random material and replace the block
						if(mode.hasGenSound()) l.getWorld().playSound(l, mode.getGenSound(), soundVolume, pitch); //Play sound if configured
						if(mode.hasParticleEffect()) mode.displayGenerationParticles(l);
						if(plugin.isConnectedToIslandPlugin()) plugin.getIslandHook().onGeneratorGenerate(event.getPlayerGenerating(), event.getGenerationLocation().getBlock());
					}else {
						bm.addKnownGenLocation(l);
						return;
					}
				}
			}
		}
	}

	@Nullable
	private static Player getClosestPlayer(Location l, Collection<Entity> entitiesNearby) {
		Player closestPlayer = null;
		double closestDistance = 100D;
		for(Entity entity : entitiesNearby) {
			if(entity instanceof Player p) {
				double distance = l.distance(p.getLocation());
				if (closestPlayer != null && !(closestDistance > distance)) {
					continue;
				}
				closestPlayer = p;
				closestDistance = distance;
			}
		}
		return closestPlayer;
	}

	@EventHandler
	public void onBlockChange(EntityChangeBlockEvent e) { // Makes it possible to spawn sand, gravel and other falling blocks
		if(!e.getEntityType().equals(EntityType.FALLING_BLOCK) 
				|| !(e.getTo().equals(Material.AIR) 
						|| e.getTo().name().contains("WATER")  
						|| e.getTo().name().contains("LAVA"))) return;
		if(!Setting.SAVEONTIERPURCHASE.getBoolean()) return;
		
		Location loc = e.getBlock().getLocation();
		if(bm.isGenLocationKnown(loc)) {
			e.setCancelled(true);
			e.getBlock().getState().update(false, false);
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if(signManager.areSignsDisabled()) return;
		Location l = e.getBlock().getLocation();
		if(l.getWorld() == null || isWorldDisabled(l.getWorld())) return;
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
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS);
				}else if(lines[3] == null || !lines[3].matches("-?\\d+")) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL);
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
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_CLASS);
				}else if(lines[3] == null || !lines[3].matches("-?\\d+")) {
					p.sendMessage(Lang.PREFIX.toString() + Lang.UNDIFINED_LEVEL);
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
		p.sendMessage(Lang.PREFIX.toString() + Lang.SIGN_SUCCESS);
	}
	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent e) {
		if(isWorldDisabled(e.getBlock().getWorld())) return;
		if(!Setting.AUTOMATION_PISTONS.getBoolean()) return;
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
		if(!Setting.AUTOMATION_PISTONS.getBoolean()) return;
		if(e.getBlock().getType() != XMaterial.PISTON.parseMaterial()) return;
		Player p = e.getPlayer();
		if(!p.isOnline()) return;
		UUID uuid = p.getUniqueId();
		if(Setting.ISLANDS_USEPERISLANDUNLOCKEDGENERATORS.getBoolean() && plugin.isConnectedToIslandPlugin()) {
			uuid = plugin.getIslandHook().getIslandLeaderFromPlayer(uuid);
		}
		GenPiston piston = new GenPiston(e.getBlock().getLocation(), uuid);
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
					p.sendMessage(Lang.PREFIX.toString() + Lang.SIGN_DELETED);
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

		if(l.getWorld() == null || isWorldDisabled(l.getWorld())) return;
		if(bm.isGenLocationKnown(l)) {
			PlayerBreakGeneratedBlock event = new PlayerBreakGeneratedBlock(p,l);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) return;
			if(plugin.isConnectedToIslandPlugin()) plugin.getIslandHook().onGeneratorBlockBreak(p.getUniqueId(), event.getBlock());
			bm.setPlayerForLocation(p.getUniqueId(), l, false);
		}
	}
	
	public boolean isWorldDisabled(World world) {
		return Setting.DISABLEDWORLDS.getStringList().contains(world.getName());
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
		int blocksFound = 0; /* We need all blocks to be correct */
		List<BlockFace> testedFaces = new ArrayList<>();
		if(mode.getFixedBlocks() != null && !mode.getFixedBlocks().isEmpty()) {
			for(Entry<BlockFace, Material> entry : mode.getFixedBlocks().entrySet()) {
				if(testedFaces.contains(entry.getKey())) continue;
				testedFaces.add(entry.getKey());
				if(this.isSameMaterial(entry.getValue().name(), fromM.name())) continue;  // Should not check for the original block
				Block r = toB.getRelative(entry.getKey(), 1);
				Material rm = r.getType();
				if(XMaterial.supports(13) && Setting.SUPPORTWATERLOGGEDBLOCKS.getBoolean()) { //Check for waterlogged block
					if(r.getBlockData() instanceof Waterlogged waterloggedBlock) { //Checked separately so prev 1.13 do not try to use a class that does not exist
						if(waterloggedBlock.isWaterlogged()) rm = Material.WATER; //If it is waterlogged then check for generators as if it is a water block. In the future check if stairs are flowing the right way. Possible bug here
					}
				}
				if(this.isSameMaterial(rm.name(), entry.getValue().name())){
					blocksFound++; /* This block is positioned correctly; */
				}else {
					return false; /* This block is not positioned correctly so we stop testing */
				}
			}	
		}

		if(mode.getBlocks() != null && !mode.getBlocks().isEmpty()) {
			for(BlockFace face : faces){
				if(testedFaces.contains(face)) continue;
				testedFaces.add(face);
				Block r = toB.getRelative(face, 1);
				Material rm = r.getType();
				if(XMaterial.supports(13) && Setting.SUPPORTWATERLOGGEDBLOCKS.getBoolean()) { //Check for waterlogged block
					if(r.getBlockData() instanceof Waterlogged waterloggedBlock) { //Checked separately so prev 1.13 do not try to use a class that does not exist
						if(waterloggedBlock.isWaterlogged()) rm = Material.WATER; //If it is waterlogged then check for generators as if it is a water block. In the future check if stairs are flowing the right way. Possible bug here
					}
				}
				/*
				This also sadly disables LAVA and LAVA generators
				 */
				if(this.isSameMaterial(rm.name(), fromM.name())) { // Should not check for the original block

					continue;
				}
				
				for(Material mirrorMaterial : mode.getBlocks()) {
					if(this.isSameMaterial(rm.name(), mirrorMaterial.name())){
						blocksFound++; /* This block is positioned correctly; */
					}
				}
			}
		}

		blocksFound++;
		int blocksNeeded = (mode.getBlocks().size() + mode.getFixedBlocks().size());	
		return blocksFound >= blocksNeeded;
	}
	

	private boolean isSameMaterial(String materialName1, String materialName2) {
		/* Version 1.12 and under have multiple names for lava and water so both needs to be tested for */
		if(materialName1.equalsIgnoreCase(materialName2)) return true;
		else if(isWater(materialName1) && isWater(materialName2)) return true;
		else return isLava(materialName1) && isLava(materialName2);
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
