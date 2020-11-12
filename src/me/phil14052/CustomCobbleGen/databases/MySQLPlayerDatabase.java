/**
 * CustomCobbleGen By @author Philip Flyvholm
 * MySQLPlayerDatabase.java
 */
package me.phil14052.CustomCobbleGen.databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.cryptomorin.xseries.XMaterial;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Managers.TierManager;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;

/**
 * @author Philip
 *
 */
public class MySQLPlayerDatabase implements PlayerDatabase {

	private HikariConfig databaseConfig;
    private HikariDataSource ds;
    private String HOST;
    private String TABLE_NAME;
    private String DATABASE_NAME;
    
    
	
	private CustomCobbleGen plugin;
	private List<PlayerData> playerData;
	private BlockManager blockManager;
	private TierManager tierManager;
	
	public MySQLPlayerDatabase() {
		playerData = new ArrayList<>();
		plugin = CustomCobbleGen.getInstance();
		blockManager = BlockManager.getInstance();
		tierManager = TierManager.getInstance();
	}
	
	private Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
	
	@Override
	public void establishConnection() {
		databaseConfig = new HikariConfig();
		HOST = Setting.DATABASE_HOST.getString();
		DATABASE_NAME = Setting.DATABASE_DATABASE.getString();
		String jdbcUrl = "jdbc:mysql://" + HOST + "/" +  DATABASE_NAME + "?useSSL=false";
		databaseConfig.setJdbcUrl(jdbcUrl);
		databaseConfig.setUsername(Setting.DATABASE_USERNAME.getString());
		databaseConfig.setPassword(Setting.DATABASE_PASSWORD.getString());
		databaseConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
		databaseConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
		databaseConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
		TABLE_NAME = Setting.DATABASE_TABLE.getString();
        ds = new HikariDataSource( databaseConfig );
        try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = null;
	        ResultSet rs = null;
	        try {
	        	stmt = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + DATABASE_NAME + "' AND TABLE_NAME = '" + TABLE_NAME + "'");
				rs = stmt.executeQuery();
				if(!rs.last()) {
					plugin.log("Table (" + TABLE_NAME +  ") in database (" + DATABASE_NAME + ") does not exits. Trying to create it instead...");
					stmt.close();
					rs.close();
					stmt = connection.prepareStatement("CREATE TABLE " + TABLE_NAME + "(uuid VARCHAR(36), selected_tiers TEXT, purchased_tiers TEXT, pistons TEXT)");
					stmt.execute();
				}else {
					plugin.debug("Found table + " +  TABLE_NAME + " in database");
				}
	        }finally {
	        	if(stmt != null) stmt.close();
	        	if(rs != null) rs.close();
	        }
			
			plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
		} catch (SQLException e) {
        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
        	plugin.error(e.getMessage());
        	if(ds != null){
        		ds = null;
        	}
		}
	}

	@Override
	public void reloadConnection() {
		this.closeConnection();
		this.establishConnection();
	}

	@Override
	public void closeConnection() {
		if(ds != null) ds.close();
	}

	@Override
	public boolean isConnectionEstablished() {
		return ds != null && !ds.isClosed();
	}

	@Override
	public List<PlayerData> getAllPlayerData() {
		return this.playerData;
	}

	@Override
	public PlayerData getPlayerData(UUID uuid) {
		return this.getPlayerData(uuid, true);
	}

	
	private PlayerData getPlayerData(UUID uuid, boolean loadFromDatabase) {
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

	@Override
	public void setPlayerData(PlayerData data) {
		if(data == null) return;
		if(!this.containsPlayerData(data.getUUID())) {
			this.addToDatabase(data);
		}
		this.playerData.add(data);
	}
	
	private void addToDatabase(PlayerData data) {
		if(!this.isConnectionEstablished()) return;


		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					PreparedStatement stmt = null;
			        StringJoiner selectedTiers = new StringJoiner(",");
			        data.getSelectedTiers().getSelectedTiersMap().values().forEach(e -> 
			        	selectedTiers.add(e.getTierClass() + ":" + e.getLevel())
			        );
			        StringJoiner purchasedTiers = new StringJoiner(",");
			        data.getPurchasedTiers().forEach(e -> 
			        	purchasedTiers.add(e.getTierClass() + ":" + e.getLevel())
			        );
			        try {
			        	UUID uuid = data.getUUID();
			        	stmt = connection.prepareStatement("INSERT INTO " + TABLE_NAME +
			        			" (`uuid`, `selected_tiers`, `purchased_tiers`, `pistons`) VALUES "
			        			+ "('" + uuid.toString() + "', '" + selectedTiers.toString() +"', '" + purchasedTiers.toString() +"', '" + getPistonString(data.getUUID()) +"')");
						if(stmt.executeUpdate() <= 0) {
							plugin.error("Failed to add player data for uuid " + uuid.toString());
						}
						
			        }finally {
			        	if(stmt != null) stmt.close();
			        }
					
					plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
				} catch (SQLException e) {
		        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
		        	plugin.error(e.getMessage());
		        	if(ds != null){
		        		ds = null;
		        	}
				}
			}
		});
		
	}
	
	@Override
	public boolean containsPlayerData(UUID uuid) {
		return this.getPlayerData(uuid) != null;
	}

	@Override
	public void loadEverythingFromDatabase() {
		if(!this.isConnectionEstablished()) return;
		this.playerData = new ArrayList<>();
		blockManager.setKnownGenPistons(new HashMap<>());

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					PreparedStatement stmt = null;
			        ResultSet rs = null;
			        try {
			        	stmt = connection.prepareStatement("SELECT uuid, selected_tiers, purchased_tiers, pistons FROM " + TABLE_NAME);
						rs = stmt.executeQuery();
						while (rs.next()) {
							String result = rs.getString("uuid");
							if(result == null) continue;
							UUID uuid = UUID.fromString(result);
							if(uuid == null) {
								plugin.error("Failed loading player data for " + uuid);
								continue;
							}
							if(!load(uuid, rs.getString("selected_tiers"), rs.getString("purchased_tiers"))) {
								plugin.error("Failed loading user data for " + uuid.toString());
							}
							if(!loadPiston(uuid, rs.getString("pistons"))) {
								plugin.error("Failed loading piston data for " + uuid.toString());
							}
					     }
						
			        }finally {
			        	if(stmt != null) stmt.close();
			        	if(rs != null) rs.close();
			        }
					
					plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
				} catch (SQLException e) {
		        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
		        	plugin.error(e.getMessage());
		        	if(ds != null){
		        		ds = null;
		        	}
				}
			}
		});
		
		

	}

	@Override
	public void loadFromDatabase(UUID uuid) {
		if(!this.isConnectionEstablished()) return;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					PreparedStatement stmt = null;
			        ResultSet rs = null;
			        try {
			        	stmt = connection.prepareStatement("SELECT selected_tiers, purchased_tiers, pistons FROM " + TABLE_NAME + " WHERE uuid = '" + uuid.toString() + "'");
						rs = stmt.executeQuery();
						if(rs.first()) {
							if(!load(uuid, rs.getString("selected_tiers"), rs.getString("purchased_tiers"))) {
								plugin.error("Failed loading user data for " + uuid.toString());
							}
							if(!loadPiston(uuid, rs.getString("pistons"))) {
								plugin.error("Failed loading piston data for " + uuid.toString());
							}
						}else {
							plugin.debug(uuid.toString() + " is not in the database");
						}
			        }finally {
			        	if(stmt != null) stmt.close();
			        	if(rs != null) rs.close();
			        }
					
					plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
				} catch (SQLException e) {
		        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
		        	plugin.error(e.getMessage());
		        	if(ds != null){
		        		ds = null;
		        	}
				}
			}
			
		});
		

	}

	
	private boolean load(UUID uuid, String selected_tiers, String purchased_tiers) {
		if(uuid == null || selected_tiers == null || purchased_tiers == null) return false;
		//tierclass:tierlevel
		String[] selected = selected_tiers.split(",");
		SelectedTiers selectedTiers = new SelectedTiers(uuid, new ArrayList<Tier>());
		for(String tierString : selected) {
			String[] splitTier = tierString.split(":");
			String tierClass = splitTier[0];
			int tierLevel;
			try {
				tierLevel = Integer.parseInt(splitTier[1]);	
				Tier tier = this.tierManager.getTierByLevel(tierClass, tierLevel);
				if(tier == null) continue;
				selectedTiers.addTier(tier);
			}catch(NumberFormatException e) {
				//Do nothing (Continue)
			}
		}
		String[] purchased = selected_tiers.split(",");
		List<Tier> purchasedTiers = new ArrayList<>();
		for(String tierString : purchased) {
			String[] splitTier = tierString.split(":");
			String tierClass = splitTier[0];
			int tierLevel;
			try {
				tierLevel = Integer.parseInt(splitTier[1]);	
				Tier tier = this.tierManager.getTierByLevel(tierClass, tierLevel);
				if(tier == null) continue;
				purchasedTiers.add(tier);
			}catch(NumberFormatException e) {
				//Do nothing (Continue)
			}
		}
		PlayerData currentData = this.playerData.stream().filter(d -> d.getUUID().equals(uuid)).findFirst().orElse(null);
		if(currentData != null && this.playerData.contains(currentData)) {
			this.playerData.remove(currentData);
		}
		this.playerData.add(new PlayerData(uuid, selectedTiers, purchasedTiers));
		return true;
	}
	
	private boolean loadPiston(UUID uuid, String pistons) {
		if(uuid == null || pistons == null) return false;
		String[] pistonsArray = pistons.split(",");
		if(pistonsArray == null) return false;
		for(String pistonLoc : pistonsArray) {
			Location loc = StringUtils.deserializeLoc(pistonLoc);
			if(loc == null) continue;

			Block block = loc.getWorld().getBlockAt(loc);
			if(block == null) {
				plugin.error("Can't confirm block is piston in players.yml under UUID: " + uuid + ".pistons at " + pistonLoc);
				continue;
			}
			else if(loc.getWorld().getBlockAt(loc).getType()!= XMaterial.PISTON.parseMaterial()) continue;
			blockManager.getKnownGenPistons().remove(loc);
			GenPiston piston = new GenPiston(loc, uuid);
			piston.setHasBeenUsed(true);
			blockManager.addKnownGenPiston(piston);
		}
		return true;
	}
	
	@Override
	public void saveToDatabase(UUID uuid) {
		PlayerData data = this.getPlayerData(uuid);
		if(data == null) return;
		this.saveToDatabase(data);
	}

	@Override
	public void saveToDatabase(PlayerData data) {
		if(!this.isConnectionEstablished()) return;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					PreparedStatement stmt = null;
			        StringJoiner selectedTiers = new StringJoiner(",");
			        data.getSelectedTiers().getSelectedTiersMap().values().forEach(e -> 
			        	selectedTiers.add(e.getTierClass() + ":" + e.getLevel())
			        );
			        StringJoiner purchasedTiers = new StringJoiner(",");
			        data.getPurchasedTiers().forEach(e -> 
			        	purchasedTiers.add(e.getTierClass() + ":" + e.getLevel())
			        );
			        try {
			        	UUID uuid = data.getUUID();
			        	stmt = connection.prepareStatement("UPDATE " + TABLE_NAME +
			        			" SET selected_tiers = '" + selectedTiers.toString() + "', purchased_tiers = '" + purchasedTiers.toString() +
			        			"', pistons = '" + getPistonString(data.getUUID()) + "' WHERE uuid = '" + uuid.toString() + "'");
						if(stmt.executeUpdate() <= 0) {
							plugin.error("Failed to save player data for uuid " + uuid.toString());
						}
						
			        }finally {
			        	if(stmt != null) stmt.close();
			        }
					
					plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
				} catch (SQLException e) {
		        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
		        	plugin.error(e.getMessage());
		        	if(ds != null){
		        		ds = null;
		        	}
				}
			}
		});
		
	}

	private String getPistonString(UUID uuid) {
        StringJoiner pistonsString = new StringJoiner(",");
        GenPiston[] pistons = blockManager.getGenPistonsByUUID(uuid);
        if(pistons == null || pistons.length == 0) return "";
        for(int i = 0; i<pistons.length; i++) {
        	pistonsString.add(StringUtils.serializeLoc(pistons[i].getLoc()));
        }
        return pistonsString.toString();
	}
	
	@Override
	public void saveEverythingToDatabase() {
		this.getAllPlayerData().forEach(this::saveToDatabase);
	}

	@Override
	public void savePistonsToDatabase(UUID uuid) {
		if(!this.isConnectionEstablished()) return;

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					PreparedStatement stmt = null;
			        try {
			        	stmt = connection.prepareStatement("UPDATE " + TABLE_NAME +
			        			" SET pistons = '" + getPistonString(uuid) + "' WHERE uuid = '" + uuid.toString() + "'");
						if(stmt.executeUpdate() >= 0) {
							plugin.error("Failed to save piston data for uuid " + uuid.toString());
						}
						
			        }finally {
			        	if(stmt != null) stmt.close();
			        }
					
					plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
				} catch (SQLException e) {
		        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
		        	plugin.error(e.getMessage());
		        	if(ds != null){
		        		ds = null;
		        	}
				}
			}
		});
		

	}

	@Override
	public void loadPistonsFromDatabase(UUID uuid) {
		if(!this.isConnectionEstablished()) return;

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					PreparedStatement stmt = null;
			        ResultSet rs = null;
			        try {
			        	stmt = connection.prepareStatement("SELECT pistons FROM " + TABLE_NAME + " WHERE uuid = '" + uuid.toString() + "'");
						rs = stmt.executeQuery();
						if(rs.first()) {
							if(!loadPiston(uuid, rs.getString("pistons"))) {
								plugin.error("Failed loading piston data for " + uuid.toString());
							}
						}else {
							plugin.debug(uuid.toString() + " is not in the database");
						}
			        }finally {
			        	if(stmt != null) stmt.close();
			        	if(rs != null) rs.close();
			        }
					
					plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
				} catch (SQLException e) {
		        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
		        	plugin.error(e.getMessage());
		        	if(ds != null){
		        		ds = null;
		        	}
				}
			}
		});
		
	}
	
	@Override
	public String getType() {
		return "MYSQL";
	}

}
