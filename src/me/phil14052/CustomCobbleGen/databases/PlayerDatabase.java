/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Database.java
 */
package me.phil14052.CustomCobbleGen.databases;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Utils.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Philip
 *
 */
public abstract class PlayerDatabase {

	protected CustomCobbleGen plugin;
	protected List<PlayerData> playerData;
	protected BlockManager blockManager;
	protected TierManager tierManager;
	
	public PlayerDatabase() {
		playerData = new ArrayList<>();
		plugin = CustomCobbleGen.getInstance();
		blockManager = BlockManager.getInstance();
		tierManager = TierManager.getInstance();
	}
	
	public abstract Response<String> establishConnection();
	public abstract void reloadConnection();
	public abstract void closeConnection();
	public abstract boolean isConnectionEstablished();
	
	public List<PlayerData> getAllPlayerData() {
		return this.playerData;
	}

	protected abstract void addToDatabase(PlayerData data);
	
	public PlayerData getPlayerData(UUID uuid) {
		return this.getPlayerData(uuid, true);
	}

	
	protected PlayerData getPlayerData(UUID uuid, boolean loadFromDatabase) {
		PlayerData playerData =  this.getAllPlayerData().stream()
				.filter(data -> data.getUUID() != null && data.getUUID().equals(uuid))
				.findFirst()
				.orElse(null);
		if(playerData == null) {
			if(loadFromDatabase) {
				this.loadFromDatabase(uuid);
				return this.getPlayerData(uuid, false);
			}
			playerData = new PlayerData(uuid);
			this.addToDatabase(playerData);
		}
		return playerData;
	}

	protected boolean containsPlayerData(UUID uuid, boolean loadFromDatabase){
		return this.getPlayerData(uuid, loadFromDatabase) != null;
	}

	public boolean containsPlayerData(UUID uuid) {
		return this.getPlayerData(uuid) != null;
	}
	
	public void setPlayerData(PlayerData data) {
		if(data == null) return;
		if(!this.containsPlayerData(data.getUUID())) {
			this.addToDatabase(data);
		}
		this.playerData.add(data);
	}
	public void setAllPlayerData(List<PlayerData> playerData){
		this.playerData = playerData;
	}
	
	public abstract void loadEverythingFromDatabase();
	public abstract void loadFromDatabase(UUID uuid);
	public abstract void saveToDatabase(UUID uuid);
	public abstract void saveToDatabase(PlayerData data);
	public abstract void saveEverythingToDatabase();
	
	public abstract void savePistonsToDatabase(UUID uuid);
	public abstract void loadPistonsFromDatabase(UUID uuid);
	
	public abstract String getType();
	
}
