package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADHexHolder;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.spell.iota.Iota;
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
    public void writeHex(List<Iota> patterns, int media) {
        holder.writeHex(stack, patterns, media);
    }

    @Override
    public void clearHex() {
        holder.clearHex(stack);
    }
}
