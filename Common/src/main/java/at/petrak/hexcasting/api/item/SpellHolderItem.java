package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpellHolderItem extends ManaHolderItem {

	boolean canDrawManaFromInventory(ItemStack stack);

	@Nullable
	List<HexPattern> getPatterns(ItemStack stack);

	void writePatterns(ItemStack stack, List<HexPattern> patterns, int mana);

	void clearPatterns(ItemStack stack);
}
