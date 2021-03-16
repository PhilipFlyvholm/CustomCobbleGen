
package me.phil14052.CustomCobbleGen.Utils;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * CustomCobbleGen By @author Philip Flyvholm
 * * GlowEnchant.java
 **/
public class GlowEnchant extends Enchantment{

	/**
	 * @param key NamespacedKey
	 */
	public GlowEnchant(NamespacedKey key) {
		super(key);
	}

	@Override
	public boolean canEnchantItem(@NotNull ItemStack arg0) {
		return true;
	}

	@Override
	public boolean conflictsWith(@NotNull Enchantment arg0) {
		return false;
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		return null;
	}

	@Override
	public int getMaxLevel() {
		return 0;
	}

	@Deprecated @Override
	public String getName() {
		return "CCG_GLOW";
	}

	@Override
	public int getStartLevel() {
		return 0;
	}

	@Deprecated @Override
	public boolean isCursed() {
		return false;
	}

	@Override
	public boolean isTreasure() {
		return false;
	}

}
