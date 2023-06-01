package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Items which can cast a packaged Hex can implement this interface.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all items which implement this interface,
 * and the appropriate cap/CC will be attached.
 */
@ApiStatus.OverrideOnly
public interface HexHolderItem extends MediaHolderItem {

    boolean canDrawMediaFromInventory(ItemStack stack);

    boolean hasHex(ItemStack stack);

    @Nullable
    List<Iota> getHex(ItemStack stack, ServerLevel level);

    void writeHex(ItemStack stack, List<Iota> program, @Nullable FrozenPigment pigment, long media);

    void clearHex(ItemStack stack);

    @Nullable FrozenPigment getPigment(ItemStack stack);
}
