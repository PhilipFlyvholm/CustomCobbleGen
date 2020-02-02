/**
 * CustomOreGen By @author Philip Flyvholm
 * BlockEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.SignChangeEvent;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.GenBlock;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Signs.ClickableSign;
import me.phil14052.CustomCobbleGen.Signs.GUISign;
import me.phil14052.CustomCobbleGen.Signs.SelectSign;
import me.phil14052.CustomCobbleGen.Signs.SignManager;

public class BlockEvents implements Listener{

	private TierManager tm = TierManager.getInstance();
	private BlockManager bm = BlockManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private SignManager signManager = SignManager.getInstance();

	@EventHandler
	public void onBlockFlow(BlockFromToEvent e){
		Block b = e.getBlock();
		if(isWorldDisabled(b.getLocation().getWorld())) return;
		Material m = b.getType();
		if(m.equals(Material.WATER) || m.equals(Material.LAVA)){
			Block toBlock = e.getToBlock();
			Material toBlockMaterial = toBlock.getType();
			if(toBlockMaterial.equals(Material.AIR) || toBlockMaterial.equals(Material.WATER) || toBlockMaterial.equals(Material.LAVA)){
				if(isGeneratingCobbleStone(m, toBlock)){
					Location l = toBlock.getLocation();
					//Checks if the block has been broken before and if it is a known gen location
					if(bm.isGenLocationKnown(l)) {
						//it is a Known gen location
						if(!bm.getGenBreaks().containsKey(l)) return; //A player has not prev broken a block here
						//A player has prev broken a block here
						GenBlock gb = bm.getGenBreaks().get(l); //Get the GenBlock in this location
						if (gb.hasExpired()) {
							bm.removeKnownGenLocation(l);
							return;
						}
						Player p = gb.getPlayer(); //Get the player who broke the blocks tier
						Tier tier = tm.getSelectedTier(p); // ^

						if(tier != null) {
							Material result = tier.getRandomResult();
							if(result == null) {
								plugin.log("§cUnkown material in " + tier.getName() + " tier.");
								return;
							}
							e.setCancelled(true);
							toBlock.setType(result); //Get a random material and replace the block
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
		Location l = e.getBlock().getLocation();
		if(isWorldDisabled(l.getWorld())) return;
		Player p = e.getPlayer();
		String[] lines = e.getLines();
		if(!lines[0].equalsIgnoreCase("[CCG]")) return;
		ClickableSign sign = null;
		plugin.debug(lines[1]);
		if(lines[1].equalsIgnoreCase("GUI")) {
			sign = new GUISign(l);
			e.setLine(0, Lang.SIGN_GUI_0.toString());
			e.setLine(1, Lang.SIGN_GUI_1.toString());
			e.setLine(2, Lang.SIGN_GUI_2.toString());
			e.setLine(3, Lang.SIGN_GUI_3.toString());
		}else if(lines[1].equalsIgnoreCase("select")){
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
				
		}
		if(sign == null) {
			e.setLine(0, Lang.SIGN_NOT_VALID_0.toString());
			e.setLine(1, Lang.SIGN_NOT_VALID_1.toString());
			e.setLine(2, Lang.SIGN_NOT_VALID_2.toString());
			e.setLine(3, Lang.SIGN_NOT_VALID_3.toString());
			return;
		}
		
		signManager.addSign(sign);
		p.sendMessage(Lang.PREFIX.toString() + Lang.SIGN_SUCCESS.toString());
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Location l = e.getBlock().getLocation();
		ClickableSign signAtLocation = signManager.getSignFromLocation(l);
		Player p = e.getPlayer();
		if(signManager.removeSign(signAtLocation)) {
			p.sendMessage(Lang.PREFIX.toString() + Lang.SIGN_DELETED.toString());
		}
		if(isWorldDisabled(l.getWorld())) return;
		if(bm.isGenLocationKnown(l)) {
			bm.setPlayerForLocation(p, l);
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
	
	private boolean isGeneratingCobbleStone(Material fromM, Block toB){
		Material mirrorMaterial = (fromM.equals(Material.WATER) ? Material.LAVA : Material.WATER);
		for(BlockFace face : faces){
			Block r = toB.getRelative(face, 1);
			if(r.getType().equals(mirrorMaterial)) {
				return true;
			}
		}
		
		return false;
	}
}
