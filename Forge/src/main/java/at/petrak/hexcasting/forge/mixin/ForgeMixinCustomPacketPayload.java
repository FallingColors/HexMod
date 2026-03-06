package at.petrak.hexcasting.forge.mixin;

import at.petrak.hexcasting.common.msgs.*;
import at.petrak.hexcasting.forge.network.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * The encoder uses CustomPacketPayload$1.findCodec(id), which looks up id in a pre-built idToType map.
 * When our hex payloads aren't in that map, it falls back to DiscardedPayload codec → ClassCastException.
 * This mixin returns our real codecs for hexcasting payload IDs so encoding succeeds.
 */
@Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1", remap = true)
public abstract class ForgeMixinCustomPacketPayload {

    private static final Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, ? extends CustomPacketPayload>> HEX_CODECS = Map.ofEntries(
        Map.entry(modLoc("pat_sc"), MsgNewSpellPatternS2C.STREAM_CODEC),
        Map.entry(modLoc("pat_cs"), MsgNewSpellPatternC2S.STREAM_CODEC),
        Map.entry(modLoc("scroll"), MsgShiftScrollC2S.STREAM_CODEC),
        Map.entry(modLoc("sntnl"), MsgSentinelStatusUpdateAck.STREAM_CODEC),
        Map.entry(modLoc("color"), MsgPigmentUpdateAck.STREAM_CODEC),
        Map.entry(modLoc("altiora"), MsgAltioraUpdateAck.STREAM_CODEC),
        Map.entry(modLoc("cprtcl"), MsgCastParticleS2C.STREAM_CODEC),
        Map.entry(modLoc("cgui"), MsgOpenSpellGuiS2C.STREAM_CODEC),
        Map.entry(modLoc("beep"), MsgBeepS2C.STREAM_CODEC),
        Map.entry(modLoc("sweep"), MsgBrainsweepAck.STREAM_CODEC),
        Map.entry(modLoc("wallscr"), MsgNewWallScrollS2C.STREAM_CODEC),
        Map.entry(modLoc("redoscroll"), MsgRecalcWallScrollDisplayS2C.STREAM_CODEC),
        Map.entry(modLoc("spi_pats_sc"), MsgNewSpiralPatternsS2C.STREAM_CODEC),
        Map.entry(modLoc("clr_spi_pats_sc"), MsgClearSpiralPatternsS2C.STREAM_CODEC)
    );

    /**
     * Injected at HEAD of findCodec(ResourceLocation). When the payload id is hexcasting and we have
     * a codec, return it so the encoder uses our codec instead of falling back to DiscardedPayload.
     */
    @Inject(
        method = "findCodec(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/codec/StreamCodec;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void hexcasting$findCodec(ResourceLocation id, CallbackInfoReturnable<Object> cir) {
        StreamCodec<?, ?> codec = HEX_CODECS.get(id);
        if (codec != null) {
            cir.setReturnValue(codec);
            cir.cancel();
        }
    }
}
