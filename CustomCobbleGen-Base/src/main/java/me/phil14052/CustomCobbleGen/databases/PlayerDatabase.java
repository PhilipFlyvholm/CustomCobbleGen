/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Database.java
 */
package me.phil14052.CustomCobbleGen.databases;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Utils.Response;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Philip
 */
public abstract class PlayerDatabase {

    protected CustomCobbleGen plugin;
    protected Map<UUID, PlayerData> playerData;
    protected BlockManager blockManager;
    protected TierManager tierManager;

    public PlayerDatabase() {
        playerData = new HashMap<>();
        plugin = CustomCobbleGen.getInstance();
        blockManager = BlockManager.getInstance();
        tierManager = TierManager.getInstance();
    }

    public abstract Response<String> establishConnection();

    public abstract void reloadConnection();

    public abstract void closeConnection();

    public abstract boolean isConnectionClosed();

    public Map<UUID, PlayerData> getAllPlayerData() {
        return this.playerData;
    }

    protected void addToDatabase(PlayerData data){
        addToDatabase(data, true);
    }

    protected abstract void addToDatabase(PlayerData data, boolean async);

    public PlayerData getPlayerData(UUID uuid){
        return this.getPlayerData(uuid, true, true);
    }

    protected PlayerData getPlayerData(UUID uuid, boolean loadFromDatabase, boolean saveToDatabaseIfNull) {
        PlayerData playerData = this.getAllPlayerData().getOrDefault(uuid, null);
        if (playerData == null) {
            if (loadFromDatabase) {
                this.loadFromDatabase(uuid,false);
                return this.getPlayerData(uuid, false, saveToDatabaseIfNull);
            }
        }
        return playerData;
    }

    protected boolean containsPlayerData(UUID uuid, boolean loadFromDatabase) {
        return this.getPlayerData(uuid, loadFromDatabase, false) != null;
    }

    public boolean containsPlayerData(UUID uuid) {
        return this.getPlayerData(uuid) != null;
    }

    public void setPlayerData(PlayerData data) {
        if (data == null) return;
        if (!this.playerData.containsKey(data.getUUID())) {
            this.addToDatabase(data);
        }
        this.playerData.put(data.getUUID(), data);
    }

    public void setAllPlayerData(Map<UUID, PlayerData> playerData) {
        this.playerData = playerData;
    }

    public void loadEverythingFromDatabase() {
        loadEverythingFromDatabase(true);
    }

    public abstract void loadEverythingFromDatabase(boolean async);

    public void loadFromDatabase(UUID uuid) {
        loadFromDatabase(uuid, true);
    }


    public abstract void loadFromDatabase(UUID uuid, boolean async);

    public void saveToDatabase(UUID uuid) {
        saveToDatabase(uuid, true);
    }

    public abstract void saveToDatabase(UUID uuid, boolean async);


    public abstract void saveToDatabase(PlayerData data, boolean async);

    public void saveEverythingToDatabase() {
        saveEverythingToDatabase(true);
    }

    public void saveEverythingToDatabase(boolean async) {
        if (this.isConnectionClosed()) return;
        if(getType().equals("MYSQL")){
            this.sqlMassDataSave(this.getAllPlayerData().values());
            return;
        }
        for (PlayerData data : this.getAllPlayerData().values()) {
            this.saveToDatabase(data, async);
        }
    }

    public abstract void savePistonsToDatabase(UUID uuid);

    public abstract void loadPistonsFromDatabase(UUID uuid);

    public abstract String getType();

    public abstract void sqlMassDataSave(Collection<PlayerData> data);

}
