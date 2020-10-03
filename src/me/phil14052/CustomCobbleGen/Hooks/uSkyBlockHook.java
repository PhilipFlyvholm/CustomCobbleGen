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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Utils.Response;
import us.talabrek.ultimateskyblock.api.IslandInfo;
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

	@Override
	public boolean hasIsland(UUID uuid) {
		Player p = plugin.getServer().getPlayer(uuid);
		if(p == null || !p.isOnline()) return false;
		return api.getIslandInfo(p) != null;
	}
	
	@Override
	public Player[] getArrayOfIslandMembers(UUID uuid) {
		if(!this.hasIsland(uuid)) return new Player[0];
		Player player = Bukkit.getServer().getPlayer(uuid);
		if(player == null || !player.isOnline()) return new Player[0];
		IslandInfo island = api.getIslandInfo(player);
		if(island == null) return new Player[0];
		List<Player> onlinePlayers = new ArrayList<>();
		for(String pName : island.getMembers()) {
			Player p = Bukkit.getServer().getPlayer(pName);
			if(p != null && p.isOnline()) onlinePlayers.add(p);
		}
		return onlinePlayers.toArray(new Player[onlinePlayers.size()]);
	}


	@Override
	public void sendMessageToIslandMembers(String message, UUID uuid) {
		this.sendMessageToIslandMembers(message, uuid, false);
	}
	@Override
	public void sendMessageToIslandMembers(String message, UUID uuid, boolean withoutSender) {
		Player[] players = this.getArrayOfIslandMembers(uuid);
		if(players == null) return;
		for(Player p : players) {

			if(p.getUniqueId().equals(uuid) && withoutSender) continue;
			p.sendMessage(message);
		}
	}

	/**
	 * uSkyBlock does not support per island balances
	 */
	@Override
	public double getBalance(UUID uuid) {
		return 0;
	}

	/**
	 * uSkyBlock does not support per island balances
	 */
	@Override
	public void removeFromBalance(UUID uuid, double amount) {
		return;
	}

	@Override
	public boolean supportsIslandBalance() {
		return false;
	}
	@Override
	public String getHookName() {
		return "uSkyBlock";
	}
	
}
