package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.OverrideOnly
public interface HexHolderItem extends ManaHolderItem {

	boolean canDrawManaFromInventory(ItemStack stack);

	boolean hasHex(ItemStack stack);

	@Nullable
	List<SpellDatum<?>> getHex(ItemStack stack, ServerLevel level);

	void writeHex(ItemStack stack, List<SpellDatum<?>> patterns, int mana);

	void clearHex(ItemStack stack);
}
