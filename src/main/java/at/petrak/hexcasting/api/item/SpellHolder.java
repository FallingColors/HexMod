package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpellHolder extends ManaHolder {

	boolean canDrawManaFromInventory(ItemStack stack);

	@Nullable
	List<HexPattern> getPatterns(ItemStack stack);

	void writePattern(ItemStack stack, List<HexPattern> patterns, int mana);

	void clearPatterns(ItemStack stack);
}
