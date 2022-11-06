package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.DataHolder;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class CCEntityDataHolder implements CCDataHolder {
    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        // NO-OP
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag) {
        // NO-OP
    }

    public static class ItemDelegating extends CCEntityDataHolder {
        private final Supplier<ItemStack> item;

        public ItemDelegating(Supplier<ItemStack> stackSupplier) {
            this.item = stackSupplier;
        }

        @Override
        public @Nullable CompoundTag readRawDatum() {
            DataHolder delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate == null ? null : delegate.readRawDatum();
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            DataHolder delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate != null && delegate.writeDatum(datum, simulate);
        }

        @Override
        public @Nullable SpellDatum<?> readDatum(ServerLevel world) {
            DataHolder delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate == null ? null : delegate.readDatum(world);
        }

        @Override
        public @Nullable SpellDatum<?> emptyDatum() {
            DataHolder delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate == null ? null : delegate.emptyDatum();
        }
    }

    public static class EntityItemDelegating extends ItemDelegating {
        public EntityItemDelegating(ItemEntity entity) {
            super(entity::getItem);
        }
    }

    public static class ItemFrameDelegating extends ItemDelegating {
        public ItemFrameDelegating(ItemFrame entity) {
            super(entity::getItem);
        }
    }

    public static class ScrollDelegating extends ItemDelegating {
        public ScrollDelegating(EntityWallScroll entity) {
            super(() -> entity.scroll);
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            return false;
        }
    }
}
