package me.phil14052.CustomCobbleGen.Managers;

import java.time.Instant;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;

public class GenBlock {
    private Location location;
    private Player player;
    private Instant timestamp;
    private boolean pistonPowered = false;
    private static CustomCobbleGen plugin = CustomCobbleGen.getInstance();
    
    public GenBlock(Location l, Player player) {
    	this(l, player, false);
    }
    public GenBlock(Location l, Player player, boolean pistonPowered) {

    	this.location = l;
        this.player = player;
        this.timestamp = Instant.now();
        this.pistonPowered = pistonPowered;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
    
    public boolean isPistonPowered() {
    	return this.pistonPowered;
    }

    public boolean hasExpired() {
    	if(this.pistonPowered && plugin.getConfig().getBoolean("options.automation.pistons")) return false;
        // Expire entries 4 seconds after they were created
        // It only needs enough time for lava/water to flow and generator a new block
        if (Instant.now().getEpochSecond() >= (timestamp.getEpochSecond() + 4)) {
            return true;
        }
        return false;
    }
}
