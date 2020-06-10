/**
 * CustomCobbleGen By @author Philip Flyvholm
 * CustomCobbleGenAPI.java
 */
package me.phil14052.CustomCobbleGen.API;

import java.util.List;
import java.util.Map;

import me.phil14052.CustomCobbleGen.Managers.TierManager;

/**
 * @author Philip
 *
 */
public class CustomCobbleGenAPI {

	private CustomCobbleGenAPI instance = null;
	private TierManager tierManager;
	
	/**
	 * Instantiate the API
	 * Use CustomCobbleGenAPI.getAPI() to get instance!
	 */
	public CustomCobbleGenAPI() {
		tierManager = TierManager.getInstance();
	}
	
	/**
	 * Get a list of active tiers
	 * @return This returns a map with class names as keys and a list of tiers in that class
	 */
	public Map<String, List<Tier>> getTiers() {
		return tierManager.getTiers();
	}
	
	
	/**
	 * Get a instance of the API
	 * @return returns an active instance of the API
	 */
	public CustomCobbleGenAPI getAPI() {
		if(instance == null) instance = new CustomCobbleGenAPI();
		return instance;
	}
	
}
