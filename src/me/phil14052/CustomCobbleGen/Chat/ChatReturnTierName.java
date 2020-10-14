/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ChatReturnTierClass.java
 */
package me.phil14052.CustomCobbleGen.Chat;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import org.bukkit.entity.Player;

/**
 * @author Philip
 *
 */
public class ChatReturnTierName implements ChatReturn{

	private Player p;
	private Tier tier;
	private ChatReturnType type = ChatReturnType.NAME;
	
	public ChatReturnTierName(Player p, Tier tier) {
		this.p = p;
		this.tier = tier;
	}
	
	@Override
	public Player getPlayer() {
		return this.p;
	}

	@Override
	public void setPlayer(Player p) {
		this.p = p;
	}

	@Override
	public Tier getTier() {
		return this.tier;
	}

	@Override
	public void setTier(Tier tier) {
		this.tier = tier;
	}

	@Override
	public ChatReturnType getType() {
		return this.type;
	}

	@Override
	public void setType(ChatReturnType type) {
		this.type = type;
	}

	@Override
	public String validInput(String input) {
		if(input.trim().equals("")) {
			return Lang.CHATINPUT_INVALID.toString(input);
		}
		return "VALID";
	}
}
