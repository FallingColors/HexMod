package at.petrak.hexcasting.api.player;

import at.petrak.paucal.api.PaucalCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A null sentinel means no sentinel
 */
public record Sentinel(boolean extendsRange, Vec3 position, ResourceKey<Level> dimension) {

    public static final StreamCodec<RegistryFriendlyByteBuf, Sentinel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Sentinel::extendsRange,
            PaucalCodecs.VEC3, Sentinel::position,
            ResourceKey.streamCodec(Registries.DIMENSION), Sentinel::dimension,
            Sentinel::new
    );
}
