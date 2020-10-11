package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import org.bukkit.Location;

import java.time.Instant;
import java.util.UUID;

public class GenBlock {
    private Location location;
    private UUID uuid;
    private Instant timestamp;
    private boolean pistonPowered = false;
    private static CustomCobbleGen plugin = CustomCobbleGen.getInstance();
    
    public GenBlock(Location l, UUID uuid) {
    	this(l, uuid, false);
    }
    public GenBlock(Location l, UUID uuid, boolean pistonPowered) {

    	this.location = l;
        this.uuid = uuid;
        this.timestamp = Instant.now();
        this.pistonPowered = pistonPowered;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getUUID() {
        return uuid;
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
