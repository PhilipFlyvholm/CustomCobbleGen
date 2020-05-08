/**
 * CustomCobbleGen By @author Philip Flyvholm
 * ChatReturnTierClass.java
 */
package me.phil14052.CustomCobbleGen.Chat;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.phil14052.CustomCobbleGen.Tier;
import me.phil14052.CustomCobbleGen.Files.Lang;

/**
 * @author Philip
 *
 */
public class ChatReturnTierResultMaterial implements ChatReturn{

	private Player p;
	private Tier tier;
	private ChatReturnType type = ChatReturnType.MATERIAL;
	
	public ChatReturnTierResultMaterial(Player p, Tier tier) {
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
		input = input.toUpperCase();
		input = input.replace(" ", "_");
		if(Material.matchMaterial(input) == null) {
			return Lang.CHATINPUT_INVALID_NULL_MATERIAL.toString(input);
		}
		return "VALID";
	}

}
