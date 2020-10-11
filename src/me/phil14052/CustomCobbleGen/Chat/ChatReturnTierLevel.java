/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ChatReturnTierClass.java
 */
package me.phil14052.CustomCobbleGen.Chat;

import me.phil14052.CustomCobbleGen.API.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;
import me.phil14052.CustomCobbleGen.Utils.StringUtils;
import org.bukkit.entity.Player;

/**
 * @author Philip
 *
 */
public class ChatReturnTierLevel implements ChatReturn{

	private Player p;
	private Tier tier;
	private ChatReturnType type = ChatReturnType.LEVEL;
	
	public ChatReturnTierLevel(Player p, Tier tier) {
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
		}else if(input.contains(" ")) {
			return Lang.CHATINPUT_INVALID_NOSPACE.toString(input);
		}else if(!StringUtils.isInteger(input) || Integer.parseInt(input) < 0){
			return Lang.CHATINPUT_INVALID_NAN.toString(input);
		}
		return "VALID";
	}
}
