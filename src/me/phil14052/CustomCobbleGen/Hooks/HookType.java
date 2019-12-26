/**
 * CustomCobbleGen By @author Philip Flyvholm
 * HookTypes.java
 */
package me.phil14052.CustomCobbleGen.Hooks;

/**
 * @author Philip
 *
 */
public enum HookType {
	BENTOBOX(new BentoboxHook()), USKYBLOCK(new uSkyBlockHook());
	
	private IslandLevelHook levelHook = null;
	
	HookType(IslandLevelHook islandLevelHook) {
		this.levelHook = islandLevelHook; 
	}
	
	public IslandLevelHook getLevelHook() {
		return this.levelHook;
	}
	
	public HookType getType() {
		return this;
	}
}
