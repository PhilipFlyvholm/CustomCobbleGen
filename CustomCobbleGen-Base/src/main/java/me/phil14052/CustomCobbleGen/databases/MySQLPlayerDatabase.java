/**
 * CustomCobbleGen By @author Philip Flyvholm
 * MySQLPlayerDatabase.java
 */
package me.phil14052.CustomCobbleGen.databases;

import com.cryptomorin.xseries.XMaterial;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Utils.Response;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Philip
 *
 */
public class MySQLPlayerDatabase extends PlayerDatabase {

	private HikariDataSource ds;
    private String HOST;
    private String TABLE_NAME;
    private String DATABASE_NAME;
    
	
	public MySQLPlayerDatabase() {
		super();
	}
	
	private Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
	
	@Override
	public Response<String> establishConnection() {
		HikariConfig databaseConfig = new HikariConfig();
		HOST = Setting.DATABASE_HOST.getString();
		DATABASE_NAME = Setting.DATABASE_DATABASE.getString().toUpperCase();
		String jdbcUrl = "jdbc:mysql://" + HOST + "/" +  DATABASE_NAME + "?useSSL=false";
		databaseConfig.setJdbcUrl(jdbcUrl);
		databaseConfig.setUsername(Setting.DATABASE_USERNAME.getString());
		databaseConfig.setPassword(Setting.DATABASE_PASSWORD.getString());
		databaseConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
		databaseConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
		databaseConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
		TABLE_NAME = Setting.DATABASE_TABLE.getString().toUpperCase();
        ds = new HikariDataSource(databaseConfig);
        Response<String> response;
        try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = null;
	        ResultSet rs = null;
	        try {
	        	stmt = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?");
				stmt.setString(1, DATABASE_NAME);
				stmt.setString(2, TABLE_NAME);
				rs = stmt.executeQuery();
				if(!rs.last()) {
					plugin.log("Table (" + TABLE_NAME +  ") in database (" + DATABASE_NAME + ") does not exits. Trying to create it instead...");
					stmt.close();
					rs.close();
					stmt = connection.prepareStatement("CREATE TABLE ? (uuid VARCHAR(36), selected_tiers TEXT, purchased_tiers TEXT, pistons TEXT)");
					stmt.setString(1, TABLE_NAME);
					stmt.execute();
				}else {
					plugin.debug("Found table + " +  TABLE_NAME + " in database");
				}
	        }finally {
	        	if(stmt != null) stmt.close();
	        	if(rs != null) rs.close();
	        }
			
			plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
			response = new Response<>("Connected to " + HOST + "/" + DATABASE_NAME, false);
		} catch (SQLException e) {
        	plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
        	plugin.error(e.getMessage());
        	if(ds != null){
        		ds = null;
        	}
			response =  new Response<>("Failed to connect to " + HOST + "/" + DATABASE_NAME, false);
		}
		plugin.debug("Players is now setup&2 ✓");
        return response;
	}

	@Override
	public void reloadConnection() {
		if(!this.isConnectionEstablished()) return;
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
	protected void addToDatabase(PlayerData data) {
		if(!this.isConnectionEstablished()) return;


		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
					stmt = connection.prepareStatement("INSERT INTO ? (`uuid`, `selected_tiers`, `purchased_tiers`, `pistons`) VALUES (?,?,?,?)");
					stmt.setString(1, TABLE_NAME);
					stmt.setString(2, uuid.toString());
					stmt.setString(3, selectedTiers.toString());
					stmt.setString(4, purchasedTiers.toString());
					stmt.setString(5, getPistonString(uuid));
					if(stmt.executeUpdate() <= 0) {
						plugin.error("Failed to add player data for uuid " + uuid);
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
		});
		
	}
	
	@Override
	public void loadEverythingFromDatabase() {
		if(!this.isConnectionEstablished()) return;
		this.playerData = new HashMap<>();
		blockManager.setKnownGenPistons(new HashMap<>());

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				Connection connection = getConnection();
				PreparedStatement stmt = null;
				ResultSet rs = null;
				try {
					stmt = connection.prepareStatement("SELECT uuid, selected_tiers, purchased_tiers, pistons FROM ?");
					stmt.setString(1, TABLE_NAME);
					rs = stmt.executeQuery();
					while (rs.next()) {
						String result = rs.getString("uuid");
						if(result == null) continue;
						UUID uuid = UUID.fromString(result);
						if(!load(uuid, rs.getString("selected_tiers"), rs.getString("purchased_tiers"))) {
							plugin.error("Failed loading user data for " + uuid);
						}
						if(!loadPiston(uuid, rs.getString("pistons"))) {
							plugin.error("Failed loading piston data for " + uuid);
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
		});
		
		

	}

	@Override
	public void loadFromDatabase(UUID uuid) {
		if(!this.isConnectionEstablished()) return;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				Connection connection = getConnection();
				PreparedStatement stmt = null;
				ResultSet rs = null;
				try {
					stmt = connection.prepareStatement("SELECT selected_tiers, purchased_tiers, pistons FROM ? WHERE uuid = ?");
					stmt.setString(1, TABLE_NAME);
					stmt.setString(2, uuid.toString());
					rs = stmt.executeQuery();
					if(rs.first()) {
						if(!load(uuid, rs.getString("selected_tiers"), rs.getString("purchased_tiers"))) {
							plugin.error("Failed loading user data for " + uuid);
						}
						if(!loadPiston(uuid, rs.getString("pistons"))) {
							plugin.error("Failed loading piston data for " + uuid);
						}
					}else {
						plugin.debug(uuid + " is not in the database");
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
		});
		

	}

	private boolean load(UUID uuid, String selected_tiers, String purchased_tiers) {
		if(uuid == null || selected_tiers == null || purchased_tiers == null) return false;
		//tierclass:tierlevel
		String[] selected = selected_tiers.split(",");
		SelectedTiers selectedTiers = new SelectedTiers(uuid, new ArrayList<>());
		for(String tierString : selected) {
			String[] splitTier = tierString.split(":");
			String tierClass = splitTier[0];
			int tierLevel;
			try {
				tierLevel = Integer.parseInt(splitTier[1]);	
				Tier tier = this.tierManager.getTierByLevel(tierClass, tierLevel);
				if(tier == null) continue;
				selectedTiers.addTier(tier);
			}catch(NumberFormatException ignored) {
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
			}catch(NumberFormatException ignored) {
				//Do nothing (Continue)
			}
		}
		PlayerData currentData = this.playerData.getOrDefault(uuid, null);
		if(currentData != null) {
			this.playerData.remove(currentData.getUUID());
		}
		this.playerData.put(uuid, new PlayerData(uuid, selectedTiers, purchasedTiers));
		return true;
	}
	
	private boolean loadPiston(UUID uuid, String pistons) {
		if(uuid == null || pistons == null) return false;
		String[] pistonsArray = pistons.split(",");
		for(String pistonLoc : pistonsArray) {
			Location loc = StringUtils.deserializeLoc(pistonLoc);
			if(loc == null) continue;
			World world = loc.getWorld();
			if(world == null) {
				plugin.error("Unknown world in database under UUID: " + uuid + " -> pistons with the value: " + pistonLoc);
				continue;
			}
			Block block = world.getBlockAt(loc);
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
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
							" SET selected_tiers = '" + selectedTiers + "', purchased_tiers = '" + purchasedTiers +
							"', pistons = '" + getPistonString(data.getUUID()) + "' WHERE uuid = '" + uuid.toString() + "'");
					stmt.setString(1, TABLE_NAME);
					stmt.setString(2, selectedTiers.toString());
					stmt.setString(3, purchasedTiers.toString());
					stmt.setString(4, getPistonString(uuid));
					stmt.setString(5, uuid.toString());

					if(stmt.executeUpdate() <= 0) {
						plugin.error("Failed to save player data for uuid " + uuid);
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
		});
		
	}

	private String getPistonString(UUID uuid) {
        StringJoiner pistonsString = new StringJoiner(",");
        GenPiston[] pistons = blockManager.getGenPistonsByUUID(uuid);
        if(pistons == null || pistons.length == 0) return "";
		for (GenPiston piston : pistons) {
			pistonsString.add(StringUtils.serializeLoc(piston.getLoc()));
		}
        return pistonsString.toString();
	}

	@Override
	public void savePistonsToDatabase(UUID uuid) {
		if(!this.isConnectionEstablished()) return;

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				Connection connection = getConnection();
				try (PreparedStatement stmt = connection.prepareStatement("UPDATE ? SET pistons = ? WHERE uuid = ?")) {
					stmt.setString(1, TABLE_NAME);
					stmt.setString(2, getPistonString(uuid));
					stmt.setString(3, uuid.toString());

					if (stmt.executeUpdate() >= 0) {
						plugin.error("Failed to save piston data for uuid " + uuid);
					}

				}

				plugin.debug("Connected to " + HOST + "/" + DATABASE_NAME);
			} catch (SQLException e) {
				plugin.error("Failed to connect to " + HOST + "/" + DATABASE_NAME);
				plugin.error(e.getMessage());
				if(ds != null){
					ds = null;
				}
			}
		});
		

	}

	@Override
	public void loadPistonsFromDatabase(UUID uuid) {
		if(!this.isConnectionEstablished()) return;

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				Connection connection = getConnection();
				PreparedStatement stmt = null;
				ResultSet rs = null;
				try {
					stmt = connection.prepareStatement("SELECT pistons FROM ? WHERE uuid = ?");
					stmt.setString(1, TABLE_NAME);
					stmt.setString(2, uuid.toString());
					rs = stmt.executeQuery();
					if(rs.first()) {
						if(!loadPiston(uuid, rs.getString("pistons"))) {
							plugin.error("Failed loading piston data for " + uuid);
						}
					}else {
						plugin.debug(uuid + " is not in the database");
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
		});
		
	}
	
	@Override
	public String getType() {
		return "MYSQL";
	}

}
