package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record VariantItemComponent(int variant) {
    public static final VariantItemComponent EMPTY = new VariantItemComponent(0);
    public static final Codec<VariantItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf(VariantItem.TAG_VARIANT).forGetter(VariantItemComponent::variant)
    ).apply(instance, VariantItemComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, VariantItemComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            VariantItemComponent::variant,
            VariantItemComponent::new
    );
    public static final DataComponentType<VariantItemComponent> COMPONENT_TYPE = DataComponentType.<VariantItemComponent>builder()
            .persistent(CODEC)
            .networkSynchronized(STREAM_CODEC)
            .build();
}

