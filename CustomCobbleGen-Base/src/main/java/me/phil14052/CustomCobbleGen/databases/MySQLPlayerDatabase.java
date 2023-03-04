package me.phil14052.CustomCobbleGen.databases;

import com.cryptomorin.xseries.XMaterial;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Utils.Response;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public class MySQLPlayerDatabase extends PlayerDatabase{

    private SQLAPI sqlapi;

    public MySQLPlayerDatabase(){
        super();
    }
    String table;

    @Override
    public Response<String> establishConnection() {
        String host = Setting.DATABASE_HOST.getString();
        String database = Setting.DATABASE_DATABASE.getString();
        int port=Setting.DATABASE_PORT.getInt();
        table = Setting.DATABASE_TABLE.getString().toUpperCase();
        plugin.debug("Connecting to "+ host+"/"+database+"...");
        sqlapi = new SQLAPI(host,database,Setting.DATABASE_USERNAME.getString(),Setting.DATABASE_PASSWORD.getString(),port);
        sqlapi.connect();
        if(!sqlapi.isConnected()){
            return new Response<>("Failed to connect to " + host + "/" + database + " - Unsupported database",true);
        }
        plugin.debug("Connected to "+ host+"/"+database);
        try {
            sqlapi.execute("CREATE TABLE IF NOT EXISTS " + table + " (UUID VARCHAR(36), selected_tiers TEXT , purchased_tiers TEXT, pistons TEXT, UNIQUE (UUID))");
        }catch (Exception e){
            plugin.error("Failed to create table "+table);
            plugin.error(e.getMessage());
            return new Response<>("Failed to create table "+table,true);
        }
        return new Response<>("Successfully connected to " + host + "/" + database, false);
    }

    @Override
    public void reloadConnection() {
        if(!sqlapi.isConnected())return;
        sqlapi.reconnect();
    }

    @Override
    public void closeConnection() {
        sqlapi.close();
    }

    @Override
    public boolean isConnectionClosed() {
        return !sqlapi.isConnected();
    }
    @Override
    protected void addToDatabase(PlayerData data, boolean async) {
        if (!sqlapi.isConnected()) {
            plugin.error("Failed to add data to database. Not connected!");
            return;
        }
        Runnable r = () -> {
            String selectedTiers = getSelectedTiersString(data);
            String purchasedTiers = getPurchasedTiers(data);
            UUID uuid = data.getUUID();
            String pistons = getPistonString(uuid);
            try {
                sqlapi.insert(table, uuid, selectedTiers, purchasedTiers, pistons);
                plugin.debug("Added data to database for UUID: " + uuid);
            }catch (Exception e){
                plugin.error("Failed to add data to database for UUID: "+uuid);
                plugin.error(e.getMessage());
            }

        };
        if(async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin,r);
        else
            r.run();
    }

    @Override
    public void loadEverythingFromDatabase(boolean async) {
        if (!sqlapi.isConnected()) {
            plugin.error("Failed to load every data from database. Not connected!");
            return;
        }
        Runnable r = () ->{
            plugin.debug("Loading everything from database:",this.getType());
            try(ResultSet rs=sqlapi.query("SELECT * from `"+table+"`")){
                while (rs!=null&&rs.next()){
                    String uid=rs.getString("uuid");
                    if(uid==null)continue;
                    UUID uuid = UUID.fromString(uid);
                    if(!load(uuid,rs.getString("selected_tiers"),rs.getString("purchased_tiers")))
                        plugin.error("Failed to load data from database for UUID: "+ uuid);
                    if(!loadPiston(uuid,rs.getString("pistons")))
                        plugin.error("Failed to load data from database for UUID: "+ uuid);
                }
            }catch (Exception e){
                plugin.error("Failed to load every data from database.");
                plugin.error(e.getMessage());
            }
        };
        if(async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin,r);
        else
            r.run();
    }

    @Override
    public void loadFromDatabase(UUID uuid, boolean async) {
        if (!sqlapi.isConnected()) {
            plugin.error("Failed to load data from database. Not connected!");
            return;
        }
        Runnable r = () -> {
            try (ResultSet rs = sqlapi.query("SELECT * from " + table + " WHERE UUID='" + uuid.toString() + "'")) {
                while (rs != null && rs.next()) {
                    if (rs.getString("uuid").equals(uuid.toString())) {
                        if (!load(uuid, rs.getString("selected_tiers"), rs.getString("purchased_tiers")))
                            plugin.error("Failed to load data from database for UUID: " + uuid);
                        if (!loadPiston(uuid, rs.getString("pistons")))
                            plugin.error("Failed to load data from database for UUID: " + uuid);
                        return;
                    }
                }
                plugin.debug("Failed to load data from database for UUID: " + uuid);
            } catch (Exception e) {
                plugin.error("Failed to load data from database for UUID: " + uuid.toString());
                plugin.error(e.getMessage());
            }
        };
        if(async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin,r);
        else
            r.run();
    }


    private boolean load(UUID uuid, String selected_tiers, String purchased_tiers) {
        if (uuid == null || selected_tiers == null || purchased_tiers == null) return false;
        //tierclass:tierlevel
        String[] selected = selected_tiers.split(",");
        SelectedTiers selectedTiers = new SelectedTiers(uuid, new ArrayList<>());
        for (String tierString : selected) {
            String[] splitTier = tierString.split(":");
            String tierClass = splitTier[0];
            int tierLevel;
            try {
                tierLevel = Integer.parseInt(splitTier[1]);
                Tier tier = this.tierManager.getTierByLevel(tierClass, tierLevel);
                if (tier == null) continue;
                selectedTiers.addTier(tier);
            } catch (NumberFormatException ignored) {
                //Do nothing (Continue)
            }
        }
        String[] purchased = selected_tiers.split(",");
        List<Tier> purchasedTiers = new ArrayList<>();
        for (String tierString : purchased) {
            String[] splitTier = tierString.split(":");
            String tierClass = splitTier[0];
            int tierLevel;
            try {
                tierLevel = Integer.parseInt(splitTier[1]);
                Tier tier = this.tierManager.getTierByLevel(tierClass, tierLevel);
                if (tier == null) continue;
                purchasedTiers.add(tier);
            } catch (NumberFormatException ignored) {
                //Do nothing (Continue)
            }
        }
        PlayerData currentData = this.playerData.getOrDefault(uuid, null);
        if (currentData != null) {
            this.playerData.remove(currentData.getUUID());
        }
        this.playerData.put(uuid, new PlayerData(uuid, selectedTiers, purchasedTiers));
        return true;
    }
    @Override
    public void saveToDatabase(UUID uuid, boolean async) {
        PlayerData data = this.getPlayerData(uuid);
        if (data == null) return;
        this.saveToDatabase(data, async);
    }


    private String getSelectedTiersString(PlayerData data) {
        StringJoiner selectedTiers = new StringJoiner(",");
        data.getSelectedTiers().getSelectedTiersMap().values().forEach(e ->
                selectedTiers.add(e.getTierClass() + ":" + e.getLevel())
        );
        return selectedTiers.toString();
    }

    private String getPurchasedTiers(PlayerData data) {
        StringJoiner purchasedTiers = new StringJoiner(",");
        data.getPurchasedTiers().forEach(e ->
                purchasedTiers.add(e.getTierClass() + ":" + e.getLevel())
        );
        return purchasedTiers.toString();
    }

    @Override
    public void saveToDatabase(PlayerData data, boolean async) {
        if (!sqlapi.isConnected()) {
            plugin.error("Failed to save data. Not connected to database.");
            return;
        }
        Runnable r = () ->{
            String selectedTiers = getSelectedTiersString(data);
            String purchasedTiers = getPurchasedTiers(data);
            UUID uuid = data.getUUID();
            String pistons=getPistonString(uuid);
            try{
                sqlapi.execute("UPDATE "+ table+" SET selected_tiers = '" + selectedTiers + "', purchased_tiers = '" + purchasedTiers + "', pistons = '" + pistons + "'");
                plugin.debug("Saved player data to database for UUID: " + uuid);
            }catch (Exception e){
                plugin.error("Failed to save data to database for UUID: "+uuid.toString());
                plugin.error(e.getMessage());
            }
        };
        if(async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin,r);
        else r.run();

    }

    private String getPistonString(UUID uuid) {
        StringJoiner pistonsString = new StringJoiner(",");
        GenPiston[] pistons = blockManager.getGenPistonsByUUID(uuid);
        if (pistons == null || pistons.length == 0) return "";
        for (GenPiston piston : pistons) {
            pistonsString.add(StringUtils.serializeLoc(piston.getLoc()));
        }
        return pistonsString.toString();
    }

    @Override
    public void savePistonsToDatabase(UUID uuid) {
        if(!sqlapi.isConnected()){
            plugin.error("Failed to save pistons to database. Not connected to database.");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
           sqlapi.execute("UPDATE `"+table+"` SET `pistons` = '"+getPistonString(uuid)+"' WHERE `uuid` = '"+uuid.toString()+"'");
        });
    }

    @Override
    public void loadPistonsFromDatabase(UUID uuid) {
        if (!sqlapi.isConnected()) {
            plugin.error("Failed to load from databse. Not connected!");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            try (ResultSet rs = sqlapi.query("SELECT * FROM `pistons` WHERE `uuid` = '"+uuid.toString()+"'")){
                while (rs!=null&&rs.next()){
                    if(!loadPiston(uuid,rs.getString("pistons"))){
                        plugin.error("Failed to load piston data for UUID: "+uuid);
                    }else {
                        return;
                    }
                }
                plugin.debug(uuid + " is not in the database");
            }catch (Exception e){
                plugin.error("Failed to load pistons from database for UUID: "+uuid);
                plugin.error(e.getMessage());
            }

        });
    }

    private boolean loadPiston(UUID uuid, String pistons) {
        if (uuid == null || pistons == null) return false;
        String[] pistonsArray = pistons.split(",");
        for (String pistonLoc : pistonsArray) {
            Location loc = StringUtils.deserializeLoc(pistonLoc);
            if (loc == null) continue;
            World world = loc.getWorld();
            if (world == null) {
                plugin.error("Unknown world in database under UUID: " + uuid + " -> pistons with the value: " + pistonLoc);
                continue;
            }
            Block block = world.getBlockAt(loc);
            if (block == null) {
                plugin.error("Can't confirm block is piston in players.yml under UUID: " + uuid + ".pistons at " + pistonLoc);
                continue;
            } else if (loc.getWorld().getBlockAt(loc).getType() != XMaterial.PISTON.parseMaterial()) continue;
            blockManager.getKnownGenPistons().remove(loc);
            GenPiston piston = new GenPiston(loc, uuid);
            piston.setHasBeenUsed(true);
            blockManager.addKnownGenPiston(piston);
        }
        return true;
    }

    @Override
    public String getType() {
        return "MYSQL";
    }


    class SQLAPI {

        private Connection connection;
        private String host, database, username, password;
        private int port;

        boolean wasConnected;

        public SQLAPI(String host, String database, String username, String password, int port) {
            this.host = host;
            this.database = database;
            this.username = username;
            this.password = password;
            this.port = port;
        }


        public boolean connect() {
            boolean result;
            try {
                openConnection();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
            try {
                if (connection != null && !connection.isClosed() && !wasConnected && result) {
                    wasConnected = true;
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(CustomCobbleGen.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            if (isConnected())
                                execute("select 1");
                            else if (wasConnected)
                                try {openConnection();} catch (Exception e) {}
                        }
                    }, 0, 20 * 60 * 5);
                }
            } catch (Exception e) {e.printStackTrace();}
            return result;
        }

        public boolean isConnected() {
            try {
                return connection != null && !connection.isClosed();
            } catch (Exception ignored) {
                return false;
            }
        }

        public void close() {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
            connection = null;
        }

        public boolean reconnect() {
            close();
            return connect();
        }

        public PreparedStatement getPreparedStatement(String command) {
            if (command == null) return null;
            try {
                return connection.prepareStatement(command);
            } catch (Exception ignored) {
                return null;
            }
        }

        public boolean update(String command) {
            return update(getPreparedStatement(command));
        }

        public boolean update(PreparedStatement command) {
            if (command == null) return false;
            boolean result = false;
            try {
                command.executeUpdate();
                result = true;
            } catch (Exception ignored) {
            }
            return result;
        }


        public ResultSet query(String command) {
            return query(getPreparedStatement(command));
        }

        public ResultSet query(PreparedStatement command) {
            if (command == null) return null;
            ResultSet rs = null;
            try {
                rs = command.executeQuery();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rs;
        }



        public void insert(String table, Object... values) {
            String items = "";
            Object[] var7 = values;
            int var6 = values.length;
            Object command;
            for (int var5 = 0; var5 < var6; ++var5) {
                command = var7[var5];
                items = items + (command != null ? ", '" + command + "'" : ", null");
            }
            if (!items.trim().isEmpty()) {
                items = items.substring(2);
                command = "INSERT INTO " + table + " VALUES (" + items + ")";
                this.execute(command + "");
            }
        }

        public void set(String table, String path, String value, String identifier, String idValue) {
            String command = "UPDATE " + table + " SET " + path + "='" + value + "' WHERE " + identifier + "='" + idValue
                    + "'";
            try {
                update(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        public boolean getBoolean(String table, String lookingfor, String identifier, String idValue) {
            String command = "SELECT " + lookingfor + " FROM " + table + " WHERE " + identifier + "='" + idValue + "'";
            try {
                ResultSet s = query(command);
                if (s != null && s.next())
                    return s.getBoolean(lookingfor);
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean execute(String command) {
            return execute(getPreparedStatement(command));
        }

        public boolean execute(PreparedStatement command) {
            if (command == null) return false;
            try {
                command.execute();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private String at = "";

        public void setConnectAttributes(String attributes) {
            at = attributes;
        }

        private void openConnection() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    return;
                }
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + at,
                            username, password);
                } catch (Exception e) {
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + at,
                            username, password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
