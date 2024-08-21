package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ItemDelegatingEntityIotaHolder implements ADIotaHolder {
    private final Supplier<ItemStack> stackSupplier;

    private final Consumer<ItemStack> save;

    public ItemDelegatingEntityIotaHolder(Supplier<ItemStack> stackSupplier, Consumer<ItemStack> save) {
        this.stackSupplier = stackSupplier;
        this.save = save;
    }

    @Override
    public @Nullable CompoundTag readIotaTag() {
        var delegate = IXplatAbstractions.INSTANCE.findDataHolder(this.stackSupplier.get());
        return delegate == null ? null : delegate.readIotaTag();
    }

    @Override
    public boolean writeable() {
        var delegate = IXplatAbstractions.INSTANCE.findDataHolder(this.stackSupplier.get());
        return delegate != null && delegate.writeable();
    }

    @Override
    public boolean writeIota(@Nullable Iota datum, boolean simulate) {
        var stacc = this.stackSupplier.get();
        var delegate = IXplatAbstractions.INSTANCE.findDataHolder(stacc);
        var success = delegate != null && delegate.writeIota(datum, simulate);
        if (success && !simulate) {
            this.save.accept(stacc);
        }
        return success;
    }

    @Override
    public @Nullable Iota readIota(ServerLevel world) {
        var delegate = IXplatAbstractions.INSTANCE.findDataHolder(this.stackSupplier.get());
        return delegate == null ? null : delegate.readIota(world);
    }

    @Override
    public @Nullable Iota emptyIota() {
        var delegate = IXplatAbstractions.INSTANCE.findDataHolder(this.stackSupplier.get());
        return delegate == null ? null : delegate.emptyIota();
    }

    public static class ToItemEntity extends ItemDelegatingEntityIotaHolder {
        public ToItemEntity(ItemEntity entity) {
            super(entity::getItem, stack -> {
                // https://github.com/VazkiiMods/Botania/blob/e6d095ff5010074b45408d6cce8ee1e328af3383/Xplat/src/main/java/vazkii/botania/common/helper/EntityHelper.java#L16
                entity.setItem(ItemStack.EMPTY);
                entity.setItem(stack);
                entity.setUnlimitedLifetime();
            });
        }
    }

    public static class ToItemFrame extends ItemDelegatingEntityIotaHolder {
        public ToItemFrame(ItemFrame entity) {
            super(entity::getItem, entity::setItem);
        }
    }

    public static class ToWallScroll extends ItemDelegatingEntityIotaHolder {
        public ToWallScroll(EntityWallScroll entity) {
            super(() -> entity.scroll.copy(), stack -> {
            });
        }

        @Override
        public boolean writeIota(@Nullable Iota datum, boolean simulate) {
            return false;
        }
    }
}
