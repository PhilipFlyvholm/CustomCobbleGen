/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ChatReturn.java
 */
package me.phil14052.CustomCobbleGen.Chat;

import me.phil14052.CustomCobbleGen.API.Tier;
import org.bukkit.entity.Player;

/**
 * @author Philip
 *
 */
public interface ChatReturn {

	Player getPlayer();
	void setPlayer(Player p);
	
	Tier getTier();
	void setTier(Tier tier);
	
	ChatReturnType getType();
	void setType(ChatReturnType type);
	
	String validInput(String input);
	
}
