package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpellHolderItem extends ManaHolderItem {

	boolean canDrawManaFromInventory(ItemStack stack);

	boolean hasSpell(ItemStack stack);

	@Nullable
	List<SpellDatum<?>> getSpell(ItemStack stack, ServerLevel level);

	void writePatterns(ItemStack stack, List<SpellDatum<?>> patterns, int mana);

	void clearPatterns(ItemStack stack);
}
