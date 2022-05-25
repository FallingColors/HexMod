package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.LegacySpellDatum;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HexHolderItem extends ManaHolderItem {

	boolean canDrawManaFromInventory(ItemStack stack);

	boolean hasHex(ItemStack stack);

	@Nullable
	List<LegacySpellDatum<?>> getHex(ItemStack stack, ServerLevel level);

	void writeHex(ItemStack stack, List<LegacySpellDatum<?>> patterns, int mana);

	void clearHex(ItemStack stack);
}
