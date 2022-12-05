package me.phil14052.CustomCobbleGen.databases;

import com.cryptomorin.xseries.XMaterial;
import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Setting;
import me.phil14052.CustomCobbleGen.Managers.GenPiston;
import me.phil14052.CustomCobbleGen.Utils.Response;
import me.phil14052.CustomCobbleGen.Utils.SelectedTiers;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * YamlPlayerDatabase.java
 */
public class YamlPlayerDatabase extends PlayerDatabase {

    private FileConfiguration playerConfig;
    private File playerConfigFile;

    public YamlPlayerDatabase() {
        super();
    }

    @Override
    public Response<String> establishConnection() {

        // Setup player configs
        playerConfig = null;
        playerConfigFile = null;

        PluginDescriptionFile pluginYml = plugin.getDescription();
        List<String> header = Arrays.asList(
                String.format("%s! Version: %s - By Phil14052", pluginYml.getName(), pluginYml.getVersion()),
                "IMPORTANT: ONLY EDIT THIS IF YOU KNOW WHAT YOU ARE DOING!!"
        );
        try{
            this.getPlayerConfig().options().setHeader(header);
        }catch(NoSuchMethodError ignored){
            plugin.debug("Server does not support FileConfigurationOptions#setHeader(List<String>) for some unknown reason");
        }

        if (this.getPlayerConfig().getConfigurationSection("players") == null) {
            this.getPlayerConfig().createSection("players");
        } else { //CONVERTER FROM 1.4.1 to 1.4.2+
            ConfigurationSection section = getPlayerConfig().getConfigurationSection("players");
            if (section == null) return new Response<>("Player section not found", true);
            for (String s : section.getKeys(false)) {
                if (this.getPlayerConfig().contains("players." + s + ".selected.class") || this.getPlayerConfig().contains("players." + s + ".selected.level")) { //This means that it is saved with the prev 1.4.2 format
                    plugin.log("&cAuto updating player selected data for UUID: " + s);
                    this.getPlayerConfig().set("players." + s + ".selected.0.class", this.getPlayerConfig().get("players." + s + ".selected.class"));
                    this.getPlayerConfig().set("players." + s + ".selected.0.level", this.getPlayerConfig().get("players." + s + ".selected.level"));
                    this.getPlayerConfig().set("players." + s + ".selected.level", null);
                    this.getPlayerConfig().set("players." + s + ".selected.class", null);
                }
            }
        }
        this.getPlayerConfig().options().copyDefaults(true);
        this.savePlayerConfig();

        plugin.debug("Players is now setup&2 ✓");
        return new Response<>("Success establishing connection to YAML file", false);
    }

    @Override
    public void reloadConnection() {
        if (!this.isConnectionEstablished()) return;
        this.reloadPlayerConfig();
    }

    @Override
    public void closeConnection() {
        if (!this.isConnectionEstablished()) return;
        this.saveEverythingToDatabase();
        this.playerConfig = null;
        this.playerConfigFile = null;
    }


    @Override
    protected void addToDatabase(PlayerData data) {
        if (!this.isConnectionEstablished()) return;
        this.playerData.put(data.getUUID(), data);
    }

    @Override
    public void loadEverythingFromDatabase() {
        if (!this.isConnectionEstablished()) return;
        this.playerData = new HashMap<>();
        blockManager.setKnownGenPistons(new HashMap<>());
        ConfigurationSection playerSection = this.getPlayerConfig().getConfigurationSection("players");

        if (playerSection == null) {
            plugin.error("Could not find player section in player config");
            return;
        }

        if (Setting.ONLY_LOAD_ONLINE_PLAYERS.getBoolean()) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()){
                if (p != null) this.loadFromDatabase(p.getUniqueId());
            }
        } else {
            for (String uuidString : playerSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                this.loadFromDatabase(uuid);
            }
        }

    }

    @Override
    public void loadFromDatabase(UUID uuid) {
        if (!this.isConnectionEstablished()){
            plugin.debug("Connection not established to players.yml");
            return;
        }

        if (uuid == null) {
            plugin.error("UUID in player.yml is null");
            return;
        }
        String path = this.getPlayerPath(uuid);
        if (!this.getPlayerConfig().contains(path)) return;
        if (this.containsPlayerData(uuid, false)){
            this.playerData.remove(uuid);
        }
        SelectedTiers selectedTiers = new SelectedTiers(uuid, new ArrayList<>());
        ConfigurationSection playerSection = this.getPlayerConfig().getConfigurationSection(path);
        if (playerSection == null) {
            plugin.debug("Could not find player section for " + uuid);
            return;
        }
        if (playerSection.contains("selected")) {
            ConfigurationSection selectedSection = playerSection.getConfigurationSection("selected");
            if (selectedSection == null) return;
            for (String s : selectedSection.getKeys(false)) {
                int tierLevel = playerSection.getInt("selected." + s + ".level");
                String tierClass = playerSection.getString("selected." + s + ".class");
                Tier tier = tierManager.getTierByLevel(tierClass, tierLevel);
                if (tier == null) {
                    plugin.error("Selected tiers loaded incorrectly for " + uuid + " - Skipping load");
                } else {
                    plugin.debug("Added " + tier + " tier to selected list for " + uuid);
                    selectedTiers.addTier(tier);
                }
            }
        } else {
            plugin.debug("Could not find selected section for " + uuid);
            selectedTiers.addTier(tierManager.getTierByLevel("DEFAULT", 0));
        }
        List<Tier> purchasedTiers = new ArrayList<>();
        if (playerSection.contains("purchased")) {
            ConfigurationSection purchasedSection = playerSection.getConfigurationSection("purchased");
            if (purchasedSection == null) return;
            for (String purchasedClass : purchasedSection.getKeys(false)) {
                List<Integer> purchasedLevels = purchasedSection.getIntegerList(purchasedClass);
                for (int purchasedLevel : purchasedLevels) {
                    Tier purchasedTier = tierManager.getTierByLevel(purchasedClass, purchasedLevel);
                    if (purchasedTier == null) {
                        plugin.error("Unknown purchased tier under the uuid &e" + uuid + "&c&l in the players.yml. Please remove this tier from the purchased list!", true);
                        plugin.log("&c&lIf not manually added then please report this to the dev - Line 158 in YamlPlayerDatabase - loadFromDatabase");
                        continue;
                    }
                    purchasedTiers.add(purchasedTier);
                }
            }
        }
        PlayerData data = new PlayerData(uuid, selectedTiers, purchasedTiers);
        this.playerData.put(data.getUUID(), data);
        this.loadPistonsFromDatabase(uuid);
    }

    @Override
    public void saveToDatabase(UUID uuid) {
        PlayerData data = this.getPlayerData(uuid);
        if (data == null) return;
        this.saveToDatabase(data);
    }

    public void saveToDatabase(PlayerData data) {
        if (!this.isConnectionEstablished()) return;
        UUID uuid = data.getUUID();
        String path = this.getPlayerPath(uuid);

        /* SAVING THE SELECTED TIERS */
        SelectedTiers selectedTiers = data.getSelectedTiers();
        if (selectedTiers != null
                && selectedTiers.getSelectedTiersMap() != null
                && !selectedTiers.getSelectedTiersMap().isEmpty()) {
            plugin.debug("Saving selected tier for " + uuid + ". Currently selected tiers " + selectedTiers);
            int i = 0;
            this.getPlayerConfig().set(path + ".selected", null);
            for (Tier tier : selectedTiers.getSelectedTiersMap().values()) {
                this.getPlayerConfig().set(path + ".selected." + i + ".class", tier.getTierClass());
                this.getPlayerConfig().set(path + ".selected." + i + ".level", tier.getLevel());
                i++;
            }
        }
        /* SAVING THE SELECTED TIERS */
        List<Tier> purchasedTiers = data.getPurchasedTiers();
        if (purchasedTiers != null
                && !purchasedTiers.isEmpty()) {
            this.getPlayerConfig().set(path + ".purchased", null);
            for (Tier purchasedTier : purchasedTiers) {
                List<Integer> purchasedLevels = new ArrayList<>();

                if (purchasedTier == null) {
                    plugin.error("Unknown purchased tier under the uuid &e" + uuid + "&c&l in the players.yml. Please remove this tier from the purchased list!", true);
                    plugin.log("&c&lIf not manually added then please report this to the dev - Line 200 in YamlPlayerDatabase - saveToDatabase");
                    continue;
                }
                plugin.debug("Saving purchased tier: " + purchasedTier.getName());
                if (this.getPlayerConfig().contains(path + ".purchased." + purchasedTier.getTierClass())) {
                    purchasedLevels = this.getPlayerConfig().getIntegerList(path + ".purchased." + purchasedTier.getTierClass());
                }
                if (!purchasedLevels.contains(purchasedTier.getLevel())) purchasedLevels.add(purchasedTier.getLevel());
                this.getPlayerConfig().set(path + ".purchased." + purchasedTier.getTierClass(), purchasedLevels);
            }

        }

        /* SAVING THE GENERATING PISTONS */
        savePistonsToDatabase(data.getUUID());

        this.savePlayerConfig();
    }

    private String getPlayerPath(UUID uuid) {
        return "players." + uuid.toString();
    }

    public void reloadPlayerConfig() {
        if (this.playerConfigFile == null) {
            this.playerConfigFile = new File(new File(plugin.getDataFolder(), "Data"), "players.yml");
            this.playerConfig = YamlConfiguration.loadConfiguration(this.playerConfigFile);

        }
    }

    //Return the player config
    public FileConfiguration getPlayerConfig() {

        if (this.playerConfigFile == null) this.reloadPlayerConfig();

        return this.playerConfig;

    }

    //Save the player config
    public void savePlayerConfig() {

        if (this.playerConfig == null || this.playerConfigFile == null) return;

        try {
            this.getPlayerConfig().save(this.playerConfigFile);
        } catch (IOException ex) {
            plugin.getServer().getLogger().log(Level.SEVERE, "Could not save Player config to " + this.playerConfigFile + "!", ex);
        }

    }

    @Override
    public boolean isConnectionEstablished() {
        return playerConfig != null && playerConfigFile != null;
    }

    @Override
    public void savePistonsToDatabase(UUID uuid) {
        GenPiston[] generatedPistons = blockManager.getGenPistonsByUUID(uuid);

        if (generatedPistons != null
                && generatedPistons.length > 0) {
            List<String> locations = new ArrayList<>();
            for (GenPiston piston : generatedPistons) {
                if (piston == null || piston.getLoc() == null || !piston.hasBeenUsed() || !piston.getLoc().getBlock().getType().equals(XMaterial.PISTON.parseMaterial()))
                    continue;

                String serializedLoc = StringUtils.serializeLoc(piston.getLoc());
                if (!locations.contains(serializedLoc)) locations.add(serializedLoc);
            }
            if (!locations.isEmpty()) this.getPlayerConfig().set(this.getPlayerPath(uuid) + ".pistons", locations);
        }
    }

    @Override
    public void loadPistonsFromDatabase(UUID uuid) {
        String path = this.getPlayerPath(uuid);
        if (!this.getPlayerConfig().contains(path + ".pistons")) return;
        List<String> locations = this.getPlayerConfig().getStringList(path + ".pistons");

        for (String stringLoc : locations) {
            Location loc = StringUtils.deserializeLoc(stringLoc);
            if (loc == null) {
                plugin.error("Unknown location in players.yml under UUID: " + uuid + ".pistons" + stringLoc);
                continue;
            }
            World world = loc.getWorld();
            if (world == null) {
                plugin.error("Unknown world in players.yml under UUID: " + uuid + ".pistons: " + stringLoc);
                continue;
            }
            if (loc.getWorld().getBlockAt(loc).getType() != XMaterial.PISTON.parseMaterial()) continue;
            blockManager.getKnownGenPistons().remove(loc);
            GenPiston piston = new GenPiston(loc, uuid);
            piston.setHasBeenUsed(true);
            blockManager.addKnownGenPiston(piston);
        }

    }

    @Override
    public String getType() {
        return "YAML";
    }


}
