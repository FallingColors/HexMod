package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ADHexHolder(HexHolderItem holder, ItemStack stack) {
    public boolean canDrawMediaFromInventory() {
        return holder.canDrawMediaFromInventory(stack);
    }

    public boolean hasHex() {
        return holder.hasHex(stack);
    }

    public @Nullable List<Iota> getHex(ServerLevel level) {
        return holder.getHex(stack, level);
    }

    public void writeHex(List<Iota> patterns, @Nullable FrozenPigment pigment, long media) {
        holder.writeHex(stack, patterns, pigment, media);
    }

    public void clearHex() {
        holder.clearHex(stack);
    }

    public @Nullable FrozenPigment getPigment() {
        return holder.getPigment(stack);
    }
}
