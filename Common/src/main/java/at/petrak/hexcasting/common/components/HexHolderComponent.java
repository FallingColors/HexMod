package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record HexHolderComponent(List<Iota> hex, FrozenPigment pigment) {
    public static final Codec<HexHolderComponent> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            IotaType.TYPED_CODEC.listOf().fieldOf("hex").forGetter(HexHolderComponent::hex),
            FrozenPigment.CODEC.fieldOf("pigment").forGetter(HexHolderComponent::pigment)
        ).apply(inst, HexHolderComponent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, HexHolderComponent> STREAM_CODEC = StreamCodec.composite(
        IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), HexHolderComponent::hex,
        FrozenPigment.STREAM_CODEC, HexHolderComponent::pigment,
        HexHolderComponent::new
    );
}
