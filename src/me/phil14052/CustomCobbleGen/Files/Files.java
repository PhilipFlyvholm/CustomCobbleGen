package me.phil14052.CustomCobbleGen.Files;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Copyright (c) 2015 nverdier
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class Files extends YamlConfiguration
{
    private Plugin plugin;
    private String fileName;
    private File   configFile;

    /**
     * Create a new Config.
     *
     * @param plugin   The class extending JavaPlugin.
     * @param fileName The name of the file being created.
     */
    public Files(Plugin plugin, String fileName)
    {
        this(plugin, fileName, new File(plugin.getDataFolder(), fileName + (fileName.endsWith(".yml") ? "" : ".yml")));
    }

    /**
     * Creates a new Config.
     *
     * @param plugin   The class extending JavaPlugin.
     * @param fileName The name of the file being created.
     * @param parent   The path of the parent
     */
    public Files(Plugin plugin, String fileName, String parent)
    {
        this(plugin, fileName, new File(plugin.getDataFolder() + File.separator + parent, fileName + (fileName.endsWith(".yml") ? "" : ".yml")));
    }

    /**
     * Creates a new Config.
     *
     * @param plugin   The class extending JavaPlugin.
     * @param fileName The name of the file being created.
     * @param file     The output file.
     */
    private Files(Plugin plugin, String fileName, File file)
    {
        this.plugin = plugin;
        this.fileName = fileName + (fileName.endsWith(".yml") ? "" : ".yml");
        this.configFile = file;

        create();
    }

    /**
     * Saves the FileConfiguration to the file.
     */
    public void save()
    {
        try
        {
            save(configFile);
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error saving config file \"" + fileName + "\"!");
        }
    }

    /**
     * Load the contents of the file to the FileConfiguration.
     */
    public void reload()
    {
        try
        {
            load(configFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error creating config file \"" + fileName + "\"!");
        }
    }

    private void create()
    {
        try
        {
            if (!configFile.exists())
            {
                if (plugin.getResource(fileName) != null)
                {
                    copy(fileName, configFile.getParentFile());
                    load(configFile);
                }
                else
                {
                    save(configFile);
                }
            }
            else
            {
                load(configFile);
                save(configFile);
            }
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error creating config file \"" + fileName + "\"!");
        }
    }

    /**
     * Gets a string from the config and translates all of the colors.
     *
     * @param path The path to get the colored string from.
     * @return The string from the config at specified path, with replaced colors.
     */
    public String getColored(String path)
    {
        String atPath = getString(path);
        return atPath == null ? null : ChatColor.translateAlternateColorCodes('&', atPath);
    }

    public void setLocation(String path, Location location)
    {
        ConfigurationSection section = getConfigurationSection(path);
        if (section == null) section = createSection(path);
        setLocation(section, location);
    }

    public void setLocation(ConfigurationSection section, Location location)
    {
        if (section == null) return;
        section.set(section + ".world", location.getWorld());
        section.set(section + ".x",     location.getX());
        section.set(section + ".y",     location.getY());
        section.set(section + ".z",     location.getZ());
        section.set(section + ".yaw",   location.getYaw());
        section.set(section + ".pitch", location.getPitch());
    }

    /**
     * Gets a Location from a specified ConfigurationSection.
     *
     * @param section The ConfigurationSection in which the Location is stored.
     * @return        The Location stored in the specified ConfigurationSection.
     */
    public Location getLocation(ConfigurationSection section)
    {
        if (section == null) return null;
        return new Location(Bukkit.getWorld(section.getString("world")),
                            section.getDouble("x"),
                            section.getDouble("y"),
                            section.getDouble("z"),
                            section.getInt("yaw"),
                            section.getInt("pitch"));
    }

    /**
     * Gets a Location from a specified path.
     *
     * @param path The path in which the Location is stored.
     * @return     The Location stored at specified path.
     */
    public Location getLocation(String path)
    {
        return getLocation(getConfigurationSection(path));
    }

    private void copy(String resourcePath, File parent)
    {
        try
        {
            FileUtils.copyInputStreamToFile(plugin.getResource(resourcePath), new File(parent, resourcePath));
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error copying file with path " + resourcePath + "!");
        }
    }
}