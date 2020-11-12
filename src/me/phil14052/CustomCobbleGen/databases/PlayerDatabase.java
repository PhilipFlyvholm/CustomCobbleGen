/**
 * CustomCobbleGen By @author Philip Flyvholm
 * Database.java
 */
package me.phil14052.CustomCobbleGen.databases;

import java.util.List;
import java.util.UUID;

/**
 * @author Philip
 *
 */
public interface PlayerDatabase {

	public void establishConnection();
	public void reloadConnection();
	public void closeConnection();
	public boolean isConnectionEstablished();
	
	public List<PlayerData> getAllPlayerData();
	public PlayerData getPlayerData(UUID uuid);
	public boolean containsPlayerData(UUID uuid);
	public void setPlayerData(PlayerData data);
	
	public void loadEverythingFromDatabase();
	public void loadFromDatabase(UUID uuid);
	public void saveToDatabase(UUID uuid);
	public void saveToDatabase(PlayerData data);
	public void saveEverythingToDatabase();
	
	public void savePistonsToDatabase(UUID uuid);
	public void loadPistonsFromDatabase(UUID uuid);
	
	public String getType();
	
}
