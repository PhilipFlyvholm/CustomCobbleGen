package me.phil14052.CustomCobbleGen.JetsMinions;

import me.phil14052.CustomCobbleGen.JetsMinions.events.MinionEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CCGJetsMinions extends JavaPlugin {

    public static CCGJetsMinions plugin;
    private final String CONSOLEPREFIX = "&8[&3&lCustomCobbleGen-JetsMinions&8]: ";

    @Override
    public void onEnable(){
        plugin = this;
        PluginManager pm = Bukkit.getPluginManager();
        if(pm.getPlugin("JetsMinions") != null) {
            plugin.debug("Found JetsMinions");
            pm.registerEvents(new MinionEvents(), this);
        }else {
            plugin.error("JetsMinions not loaded so will disable");
            pm.disablePlugin(this);
        }
        plugin.log("Support for JetsMinions in CustomCobbleGen is now enabled&2 \u2713");

    }

    @Override
    public void onDisable(){
        plugin = null;
    }

    public void debug(boolean overrideConfigOption, Object... objects) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(Object s : objects) {
            if(!first) {
                sb.append(", ");
            }else first = false;
            if(s == null) {
                sb.append("NULL");
            }else if(s instanceof String) {
                sb.append((String) s);
            }else {
                sb.append("[" + s.getClass().getTypeName() + ": " + s.toString() + "]");
            }
        }
        this.debug(sb.toString());
    }

    public void debug(Object... objects) {
        this.debug(false, objects);
    }


    public void debug(Boolean booleanObject){
        this.debug(booleanObject.getClass().getTypeName() + ": "+ booleanObject);
    }
    public void debug(String message){
        this.debug(message, false);
    }
    public void debug(String message, boolean overrideConfigOption){
        if(!overrideConfigOption /*&& !Setting.DEBUG.getBoolean()*/) return;
        Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + " &c&lDebug &8-&7 " + message).replace("&", "§"));
    }

    public void log(Object... objects) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(Object s : objects) {
            if(!first) {
                sb.append(", ");
            }else first = false;
            if(s == null) {
                sb.append("NULL");
            }else if(s instanceof String) {
                sb.append((String) s);
            }else {
                sb.append("[" + s.getClass().getTypeName() + ": " + s.toString() + "]");
            }
        }
        this.log(sb.toString());
    }

    public void log(String message){
        Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&8&lLog &8-&7 " + message).replace("&", "\u00A7"));
    }

    public void error(String message) {
        this.error(message, false);
    }

    public void error(String message, boolean userError) {
        if(userError) {
            Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&4&lUser error &8-&c " + message).replace("&", "\u00A7"));
        }else {

            Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&4&lError &8-&c " + message).replace("&", "\u00A7"));
        }
    }

    public void warning(String message) {
        Bukkit.getConsoleSender().sendMessage((CONSOLEPREFIX + "&4&lWarning &8-&7 " + message).replace("&", "\u00A7"));
    }


}