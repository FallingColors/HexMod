package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.addldata.ItemDelegatingEntityIotaHolder;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public abstract class CapEntityIotaHolder implements ADIotaHolder {
    public static class Wrapper extends CapEntityIotaHolder {
        private final ItemDelegatingEntityIotaHolder inner;

        public Wrapper(ItemDelegatingEntityIotaHolder inner) {
            this.inner = inner;
        }


        @Override
        public @Nullable CompoundTag readIotaTag() {
            return inner.readIotaTag();
        }

        @Override
        public boolean writeable() {
            return inner.writeable();
        }

        @Override
        public boolean writeIota(@Nullable Iota iota, boolean simulate) {
            return inner.writeIota(iota, simulate);
        }

        @Override
        public @Nullable Iota readIota(ServerLevel world) {
            return inner.readIota(world);
        }

        @Override
        public @Nullable Iota emptyIota() {
            return inner.emptyIota();
        }
    }
}
