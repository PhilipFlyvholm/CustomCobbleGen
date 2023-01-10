package me.phil14052.CustomCobbleGen.JetsMinions.events;

import me.jet315.minions.events.MinerBlockBreakEvent;
import me.phil14052.CustomCobbleGen.API.CustomCobbleGenAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MinionEvents implements Listener {

    @EventHandler
    public void onMinionBreak(MinerBlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Player player=Bukkit.getPlayer(e.getMinion().getPlayerUUID());
        if(player==null)return;
        CustomCobbleGenAPI.getAPI().registerBlockBreak(player,loc);
    }
}