package at.petrak.hexcasting.api.pigment;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
    public static final Supplier<FrozenPigment> DEFAULT =
            () -> new FrozenPigment(new ItemStack(HexItems.DEFAULT_PIGMENT), Util.NIL_UUID);

    public static Codec<FrozenPigment> CODEC = RecordCodecBuilder.<FrozenPigment>create(inst ->
            inst.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(FrozenPigment::item),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(FrozenPigment::owner)
            ).apply(inst, FrozenPigment::new)).orElseGet(FrozenPigment.DEFAULT);
    //TODO port: maybe default here too somehow?..
    public static StreamCodec<RegistryFriendlyByteBuf, FrozenPigment> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, FrozenPigment::item,
            UUIDUtil.STREAM_CODEC, FrozenPigment::owner,
            FrozenPigment::new
    );

    public ColorProvider getColorProvider() {
        return IXplatAbstractions.INSTANCE.getColorProvider(this);
    }
}
