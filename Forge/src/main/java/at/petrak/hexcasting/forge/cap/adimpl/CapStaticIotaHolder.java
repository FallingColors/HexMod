package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record CapStaticIotaHolder(Function<ItemStack, Iota> provider,
                                  ItemStack stack) implements ADIotaHolder {

    @Override
    public @Nullable
    CompoundTag readIotaTag() {
        var iota = provider.apply(stack);
        return iota == null ? null : HexIotaTypes.serialize(iota);
    }

    @Override
    public @Nullable
    Iota readIota(ServerLevel world) {
        return provider.apply(stack);
    }

    @Override
    public boolean writeIota(@Nullable Iota iota, boolean simulate) {
        return false;
    }
}
