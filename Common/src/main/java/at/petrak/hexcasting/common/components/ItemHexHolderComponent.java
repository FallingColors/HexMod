package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.casting.iota.Iota;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public record ItemHexHolderComponent(List<Iota> hex) {
    public static final ItemHexHolderComponent EMPTY = new ItemHexHolderComponent(List.of());
    public static final Codec<ItemHexHolderComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Iota.IOTA_CODEC.listOf().fieldOf("datum").forGetter(ItemHexHolderComponent::hex)
    ).apply(instance, ItemHexHolderComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemHexHolderComponent> STREAM_CODEC = StreamCodec.composite(
            Iota.IOTA_STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemHexHolderComponent::hex,
            ItemHexHolderComponent::new
    );
    public static final DataComponentType<ItemHexHolderComponent> COMPONENT_TYPE = DataComponentType.<ItemHexHolderComponent>builder()
            .persistent(CODEC)
            .networkSynchronized(STREAM_CODEC)
            .build();
}
