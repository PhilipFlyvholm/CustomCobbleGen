/**
 * CustomOreGen By @author Philip Flyvholm
 * BlockEvents.java
 */
package me.phil14052.CustomCobbleGen.Events;

import me.phil14052.CustomCobbleGen.Managers.GenBlock;
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

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;

public class BlockEvents implements Listener{

	private TierManager tm = TierManager.getInstance();
	private BlockManager bm = BlockManager.getInstance();
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();

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
	public void onBlockBreak(BlockBreakEvent e) {
		Location l = e.getBlock().getLocation();
		if(isWorldDisabled(l.getWorld())) return;
		if(bm.isGenLocationKnown(l)) {
			Player p = e.getPlayer();
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
