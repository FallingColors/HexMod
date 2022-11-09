package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.spell.iota.Iota;
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

public abstract class CCEntityIotaHolder implements CCIotaHolder {
    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        // NO-OP
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag) {
        // NO-OP
    }

    public static class ItemDelegating extends CCEntityIotaHolder {
        private final Supplier<ItemStack> item;

        public ItemDelegating(Supplier<ItemStack> stackSupplier) {
            this.item = stackSupplier;
        }

        @Override
        public @Nullable CompoundTag readIotaTag() {
            var delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate == null ? null : delegate.readIotaTag();
        }

        @Override
        public boolean writeIota(@Nullable Iota datum, boolean simulate) {
            var delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate != null && delegate.writeIota(datum, simulate);
        }

        @Override
        public @Nullable Iota readIota(ServerLevel world) {
            var delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate == null ? null : delegate.readIota(world);
        }

        @Override
        public @Nullable Iota emptyIota() {
            var delegate = IXplatAbstractions.INSTANCE.findDataHolder(item.get());
            return delegate == null ? null : delegate.emptyIota();
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
        public boolean writeIota(@Nullable Iota datum, boolean simulate) {
            return false;
        }
    }
}
