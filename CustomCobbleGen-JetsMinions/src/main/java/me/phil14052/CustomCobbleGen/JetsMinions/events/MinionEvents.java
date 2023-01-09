package me.phil14052.CustomCobbleGen.JetsMinions.events;

import me.jet315.minions.events.MinerBlockBreakEvent;
import me.phil14052.CustomCobbleGen.API.CustomCobbleGenAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

public class MinionEvents implements Listener {

    @EventHandler
    public void onMinionBreak(MinerBlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        try {
            Field f = e.getMinion().getClass().getDeclaredField("player");
            f.setAccessible(true);
            Player p = (Player) f.get(e.getMinion());
            CustomCobbleGenAPI.getAPI().registerBlockBreak(p, loc);
        } catch (NoSuchFieldException | IllegalAccessException noSuchFieldException) {
            noSuchFieldException.printStackTrace();
        }
    }
}