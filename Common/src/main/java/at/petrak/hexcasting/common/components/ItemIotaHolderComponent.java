package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ItemIotaHolderComponent(Iota iota) {
    public static final ItemIotaHolderComponent EMPTY = new ItemIotaHolderComponent(new NullIota());
    public static final Codec<ItemIotaHolderComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Iota.IOTA_CODEC.fieldOf("datum").forGetter(ItemIotaHolderComponent::iota)
    ).apply(instance, ItemIotaHolderComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemIotaHolderComponent> STREAM_CODEC = StreamCodec.composite(
            Iota.IOTA_STREAM_CODEC,
            ItemIotaHolderComponent::iota,
            ItemIotaHolderComponent::new
    );
    public static final DataComponentType<ItemIotaHolderComponent> COMPONENT_TYPE = DataComponentType.<ItemIotaHolderComponent>builder()
            .persistent(CODEC)
            .networkSynchronized(STREAM_CODEC)
            .build();
}


