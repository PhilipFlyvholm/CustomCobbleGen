package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Setting;
import org.bukkit.Location;

import java.time.Instant;
import java.util.UUID;

public class GenBlock {
    private final Location location;
    private final UUID uuid;
    private final Instant timestamp;
    private boolean pistonPowered = false;
    private static final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
    
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
    	if(this.pistonPowered && Setting.AUTOMATION_PISTONS.getBoolean()) return false;
        // Expire entries 4 seconds after they were created
        // It only needs enough time for lava/water to flow and generator a new block
        if (Instant.now().getEpochSecond() >= (timestamp.getEpochSecond() + 4)) {
        	plugin.debug("GenMode has expired");
            return true;
        }
        return false;
    }
}
