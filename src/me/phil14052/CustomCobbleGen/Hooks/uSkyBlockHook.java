/**
 * CustomCobbleGen By @author Philip Flyvholm
 * BentoboxHook.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Utils.Response;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

/**
 * @author Philip
 *
 */
public class uSkyBlockHook implements IslandHook{

	private uSkyBlockAPI api;
	private Map<String, UUID> playersUUID;
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	public uSkyBlockHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
		if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
		  api =  (uSkyBlockAPI) plugin;
		}
		this.playersUUID = new HashMap<>();
	}

	@Override
	public int getIslandLevel(UUID uuid) {
		Player p = plugin.getServer().getPlayer(uuid);
		return (int) Math.floor(api.getIslandLevel(p));
	}

	@Override
	public boolean isPlayerLeader(UUID uuid) {
		Player p = plugin.getServer().getPlayer(uuid);
		return api.getIslandInfo(p).isLeader(p);
	}

	@Override
	public UUID getIslandLeaderFromPlayer(UUID uuid) {
		Player p = plugin.getServer().getPlayer(uuid);
		String playerName = api.getIslandInfo(p).getLeader();
		
		if(playersUUID.containsKey(playerName)) {
			return playersUUID.get(playerName);
		}
		if(p != null) {
			playersUUID.put(playerName, p.getUniqueId());
			return p.getUniqueId();
		}
		
		final Response<String> uuidResult = this.getUUIDFromMojang(playerName);

		if (uuidResult.isError()) {
			plugin.error("Failed to connect to Mojang to get leader UUID");
			return uuid; //If fail then fallback to player
		}
		String result = uuidResult.getResult();
		result = result.replace("{", ""); /* Remove { */
		result = result.replace("}", ""); // Remove }
		result = result.replace("\"", ""); //Remove "
		String[] index = result.split(",");
		String[] idIndex = index[1].split(":");
		String uuidString = idIndex[1];
		UUID leaderUUID = UUID.fromString(uuidString);
		if(leaderUUID == null) {
			plugin.error("Unknown UUID " + uuidString + " for leader " + playerName);
			return uuid; //If fail then fallback to player
		}
		else return uuid;
		
	}
	
	private Response<String> getUUIDFromMojang(String playerName) {
		URL url = null;
		try {
			url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		HttpURLConnection con = null;
	    try {
	    	con = (HttpURLConnection) url.openConnection();
	         con.setRequestMethod("GET");
	         con.setDoOutput(true);
	     	final String response = buildResponse(con.getInputStream());
	     	return new Response<>(response, false);
	    }catch(IOException e) {
	     	return new Response<>("Unable to connect to Mojang", true);
	    }finally {
	    	if(con != null) {
	    		con.disconnect();
	    	}
	    }
	}
	
	private String buildResponse(@NotNull final InputStream source) throws IOException {
	    final BufferedReader inputReader = new BufferedReader(new InputStreamReader(source));
	    final StringBuilder responseBuilder = new StringBuilder();
	
	    for(String line; (line = inputReader.readLine()) != null;) {
	      responseBuilder.append(line);
	      responseBuilder.append('\n');
	    }
	
	    return responseBuilder.toString();
	 }
	
	
}
