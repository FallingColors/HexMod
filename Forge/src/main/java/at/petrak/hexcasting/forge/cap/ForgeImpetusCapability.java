package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public record ForgeImpetusCapability(BlockEntityAbstractImpetus impetus) implements IItemHandler {
	@Override
	public int getSlots() {
		return 1;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack originalStack, boolean simulate) {
		if (!isItemValid(slot, originalStack)) {
			return originalStack;
		}

		ItemStack stack = originalStack.copy();

		if (!simulate) {
			impetus.insertMana(stack);
		} else {
			impetus.extractManaFromItem(stack, false); // Mana goes nowhere, since nothing is actually being done
		}

		return stack;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return impetus.canPlaceItem(slot, stack);
	}
}
