package me.phil14052.CustomCobbleGen.Managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.time.Instant;
import java.time.Period;

public class GenBlock {
    private Location location;
    private Player player;
    private Instant timestamp;

    public GenBlock(Location l, Player p) {
        location = l;
        player = p;
        timestamp = Instant.now();
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

    public boolean hasExpired() {
        // Expire entries 4 seconds after they were created
        // It only needs enough time for lava/water to flow and generator a new block
        if (Instant.now().getEpochSecond() >= (timestamp.getEpochSecond() + 4)) {
            return true;
        }
        return false;
    }
}
