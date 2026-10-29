package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityChestLocus extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    public BlockEntityChestLocus(BlockPos blockPos, BlockState blockState) {
        super(HexBlockEntities.CHEST_LOCUS_TILE.get(), blockPos, blockState);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("hexcasting.container.chest_locus");
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return ChestMenu.threeRows(i, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items, provider);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items, provider);
        }
    }
}
