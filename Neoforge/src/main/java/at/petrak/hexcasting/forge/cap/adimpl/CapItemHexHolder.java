package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADHexHolder;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CapItemHexHolder(HexHolderItem holder,
                               ItemStack stack) implements ADHexHolder {

    @Override
    public boolean canDrawMediaFromInventory() {
        return holder.canDrawMediaFromInventory(stack);
    }

    @Override
    public boolean hasHex() {
        return holder.hasHex(stack);
    }

    @Override
    public @Nullable List<Iota> getHex(ServerLevel level) {
        return holder.getHex(stack, level);
    }

    @Override
    public void writeHex(List<Iota> patterns, @Nullable FrozenPigment pigment, long media) {
        holder.writeHex(stack, patterns, pigment, media);
    }

    @Override
    public void clearHex() {
        holder.clearHex(stack);
    }

    @Override
    public @Nullable FrozenPigment getPigment() {
        return holder.getPigment(stack);
    }
}
