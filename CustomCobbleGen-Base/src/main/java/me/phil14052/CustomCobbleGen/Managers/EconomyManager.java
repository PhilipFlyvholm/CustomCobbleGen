/**
 * CustomCobbleGen By @author Philip Flyvholm
 * EconomyManager.java
 */
package me.phil14052.CustomCobbleGen.Managers;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Files.Setting;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.util.UUID;

public class EconomyManager {
	private static EconomyManager instance = null;
	private final CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	
	private boolean connectedToVault = false;
	private Economy econ = null;
	
	public boolean setupEconomy() {
		if(this.isConnectedToVault() && econ != null) return connectedToVault;
		if(plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
			connectedToVault = false;
		}else {
			RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) {
	            return false;
	        }
	        econ = rsp.getProvider();
	        connectedToVault = (econ != null);
		}
		
		return connectedToVault;
	}
	
	
	public Economy getEconomy() {
		return econ;
	}
	

	public boolean isConnectedToVault() {
		return connectedToVault;
	}
	
	
	public double getBalance(Player p){
		OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
		return econ.getBalance(oPlayer);
	}
	public String formatMoney(double money){
		String m = "";
		if(Setting.MONEY_FORMAT.getBoolean()){
			DecimalFormat df = new DecimalFormat(Lang.MONEY_FORMAT.toString());
			m = df.format(money);
		}else{
			m = String.valueOf(money);
		}
		 
	     return m;
	     
	}
	
	public boolean canAfford(Player p, double amount){
		return this.canAfford(p.getUniqueId(), amount);
	}
	
	public boolean canAfford(UUID uuid, double amount){
		OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(uuid);
        return (econ.getBalance(oPlayer) - amount) >= 0;
	}
	
	public boolean hasMoney(Player p){
		return this.hasMoney(p.getUniqueId());
	}
	
	public boolean hasMoney(UUID uuid){
		OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(uuid);
        return econ.getBalance(oPlayer) >= 0;
	}
	
	public boolean takeMoney(Player p, double amount){
		if(canAfford(p, amount)){
			EconomyResponse r = econ.withdrawPlayer(p, amount);
		     if(r.transactionSuccess()) {
		         return true;
		     } else {
		         plugin.getLogger().warning(String.format("An error occured: %s", r.errorMessage));
		         return false;
		     }
		}else{
			return false;
		}
	}

	public boolean giveMoney(Player p, double amount){
		EconomyResponse r = econ.depositPlayer(p, amount);
		if(r.transactionSuccess()) {
			return true;
		} else {
			plugin.getLogger().warning(String.format("An error occured: %s", r.errorMessage));
			return false;
		}
	}
	
	
	public static EconomyManager getInstance() {
		if(instance == null) instance = new EconomyManager();
		return instance;
	}

}
