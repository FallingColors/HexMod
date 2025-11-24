package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record ItemMediaHolderComponent(long media, long max_media) {
    public static final ItemMediaHolderComponent EMPTY = new ItemMediaHolderComponent(0, 0);
    public static final Codec<ItemMediaHolderComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf(ItemMediaHolder.TAG_MEDIA).forGetter(ItemMediaHolderComponent::media),
            Codec.LONG.fieldOf(ItemMediaHolder.TAG_MAX_MEDIA).forGetter(ItemMediaHolderComponent::max_media)
    ).apply(instance, ItemMediaHolderComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMediaHolderComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ItemMediaHolderComponent::media,
            ByteBufCodecs.VAR_LONG,
            ItemMediaHolderComponent::max_media,
            ItemMediaHolderComponent::new
    );
    public static final DataComponentType<ItemMediaHolderComponent> COMPONENT_TYPE = DataComponentType.<ItemMediaHolderComponent>builder()
            .persistent(CODEC)
            .networkSynchronized(STREAM_CODEC)
            .build();
}
