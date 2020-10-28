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
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Managers.BlockManager;
import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class MySQLPlayerDatabase implements PlayerDatabase {

	private HikariConfig databaseConfig;
    private HikariDataSource ds;
	
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
		String host = Setting.DATABASE_HOST.getString();
		String databaseName = Setting.DATABASE_DATABASE.getString();
		String jdbcUrl = "jdbc:mysql:" + host + "/" +  databaseName;
		databaseConfig.setJdbcUrl(jdbcUrl);
		databaseConfig.setUsername(Setting.DATABASE_USERNAME.getString());
		databaseConfig.setPassword(Setting.DATABASE_PASSWORD.getString());
		databaseConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
		databaseConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
		databaseConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
		String table = Setting.DATABASE_TABLE.getString();
        ds = new HikariDataSource( databaseConfig );
        try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = null;
	        ResultSet rs = null;
	        try {
	        	stmt = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + databaseName + "' AND TABLE_NAME = '" + table + "'");
				rs = stmt.executeQuery();
				if(rs.last()) {
					if(rs.getRow() == 0) {
						plugin.log("Table (" + table +  ") in database (" + databaseName + " does not exits. Trying to create it instead...");
						stmt.close();
						rs.close();
						stmt = connection.prepareStatement("CREATE TABLE " + table + "(uuid VARCHAR(36), selected_tiers text[], purchased_tiers text[], pistons text[])");
					}
				}else {
					plugin.error("Failed connecting to database - Unknown table");
				}
	        }finally {
	        	if(stmt != null) stmt.close();
	        	if(rs != null) rs.close();
	        }
			
			plugin.debug("Connected to " + host + "/" + databaseName);
		} catch (SQLException e) {
        	plugin.error("Failed to connect to " + host + "/" + databaseName);
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
		return this.getAllPlayerData();
	}

	@Override
	public PlayerData getPlayerData(UUID uuid) {
		return this.getAllPlayerData().stream()
				.filter(data -> data.getUUID() != null && data.getUUID().equals(uuid))
				.findFirst()
				.orElse(null);
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
		
		
		ConfigurationSection playerSection = this.getPlayerConfig().getConfigurationSection("players");
		for(String uuid : playerSection.getKeys(false)){
			this.loadFromDatabase(UUID.fromString(uuid));
		}

	}

	@Override
	public void loadFromDatabase(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToDatabase(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToDatabase(PlayerData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveEverythingToDatabase() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePistonsToDatabase(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPistonsFromDatabase(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getType() {
		return "MYSQL";
	}

}
