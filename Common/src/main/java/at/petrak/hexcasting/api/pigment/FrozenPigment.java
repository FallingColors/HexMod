package at.petrak.hexcasting.api.pigment;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * A snapshot of a pigment item and its owner.
 * <p>
 * Due to capabilities being really slow to query many times a tick on Forge, this returns a colorizer <i>supplier</i>.
 * Get it once, and then query it a lot.
 */
public record FrozenPigment(ItemStack item, UUID owner) {

    public static final String TAG_STACK = "stack";
    public static final String TAG_OWNER = "owner";

    public static final Supplier<FrozenPigment> DEFAULT =
        () -> new FrozenPigment(new ItemStack(HexItems.DEFAULT_PIGMENT), Util.NIL_UUID);

    public static final Supplier<FrozenPigment> ANCIENT =
        () -> new FrozenPigment(new ItemStack(HexItems.ANCIENT_PIGMENT), Util.NIL_UUID);

    public CompoundTag serializeToNBT() {
        var out = new CompoundTag();
        out.putString(TAG_STACK, BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
        out.putUUID(TAG_OWNER, this.owner);
        return out;
    }

    public static FrozenPigment fromNBT(CompoundTag tag) {
        if (tag.isEmpty()) {
            return FrozenPigment.DEFAULT.get();
        }
        try {
            var itemLoc = ResourceLocation.tryParse(tag.getString(TAG_STACK));
            var item = itemLoc != null ? BuiltInRegistries.ITEM.get(itemLoc) : HexItems.DEFAULT_PIGMENT;
            var stack = new ItemStack(item);
            var uuid = tag.getUUID(TAG_OWNER);
            return new FrozenPigment(stack, uuid);
        } catch (NullPointerException exn) {
            return FrozenPigment.DEFAULT.get();
        }
    }

    public ColorProvider getColorProvider() {
        return IXplatAbstractions.INSTANCE.getColorProvider(this);
    }
}
