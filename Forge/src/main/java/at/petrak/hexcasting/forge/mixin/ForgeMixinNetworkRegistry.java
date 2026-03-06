package at.petrak.hexcasting.forge.mixin;

import at.petrak.hexcasting.common.msgs.*;
import at.petrak.hexcasting.forge.network.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.neoforged.neoforge.network.registration.PayloadRegistration;

import static at.petrak.hexcasting.api.HexAPI.MOD_ID;
import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Bypasses NeoForge 21.1.x payload direction checks for hex casting client-bound packets.
 * Also ensures getCodec returns our codec when the encoder looks it up (avoids ClassCastException to DiscardedPayload).
 * When handleModdedPayload's lookup returns null (client-side play setup lacks hex channels), we handle hex payloads directly.
 */
@Mixin(value = NetworkRegistry.class, remap = false)
public class ForgeMixinNetworkRegistry {

    private static final Set<ResourceLocation> HEX_PLAY_PAYLOAD_IDS = Set.of(
        modLoc("pat_sc"), modLoc("sntnl"), modLoc("color"), modLoc("altiora"),
        modLoc("cprtcl"), modLoc("cgui"), modLoc("beep"), modLoc("sweep"),
        modLoc("wallscr"), modLoc("redoscroll"), modLoc("spi_pats_sc"), modLoc("clr_spi_pats_sc")
    );

    /** Client-to-server payload IDs (must be allowed when client sends). */
    private static final Set<ResourceLocation> HEX_C2S_PAYLOAD_IDS = Set.of(
        modLoc("pat_cs"), modLoc("scroll")
    );

    /** Handlers for hex client-bound payloads when NeoForge lookup returns null (e.g. client play setup). */
    private static final Map<ResourceLocation, Consumer<CustomPacketPayload>> HEX_CLIENT_HANDLERS = Map.ofEntries(
        Map.entry(modLoc("pat_sc"), p -> MsgNewSpellPatternS2C.handle((MsgNewSpellPatternS2C) p)),
        Map.entry(modLoc("sntnl"), p -> MsgSentinelStatusUpdateAck.handle((MsgSentinelStatusUpdateAck) p)),
        Map.entry(modLoc("color"), p -> MsgPigmentUpdateAck.handle((MsgPigmentUpdateAck) p)),
        Map.entry(modLoc("altiora"), p -> MsgAltioraUpdateAck.handle((MsgAltioraUpdateAck) p)),
        Map.entry(modLoc("cprtcl"), p -> MsgCastParticleS2C.handle((MsgCastParticleS2C) p)),
        Map.entry(modLoc("cgui"), p -> MsgOpenSpellGuiS2C.handle((MsgOpenSpellGuiS2C) p)),
        Map.entry(modLoc("beep"), p -> MsgBeepS2C.handle((MsgBeepS2C) p)),
        Map.entry(modLoc("sweep"), p -> MsgBrainsweepAck.handle((MsgBrainsweepAck) p)),
        Map.entry(modLoc("wallscr"), p -> MsgNewWallScrollS2C.handle((MsgNewWallScrollS2C) p)),
        Map.entry(modLoc("redoscroll"), p -> MsgRecalcWallScrollDisplayS2C.handle((MsgRecalcWallScrollDisplayS2C) p)),
        Map.entry(modLoc("spi_pats_sc"), p -> MsgNewSpiralPatternsS2C.handle((MsgNewSpiralPatternsS2C) p)),
        Map.entry(modLoc("clr_spi_pats_sc"), p -> MsgClearSpiralPatternsS2C.handle((MsgClearSpiralPatternsS2C) p))
    );

    @Inject(
        method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$bypassServerPacketCheck(Packet<?> packet, ServerCommonPacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundCustomPayloadPacket cp) {
            if (MOD_ID.equals(cp.payload().type().id().getNamespace())) {
                ci.cancel();
            }
        } else if (packet instanceof ServerboundCustomPayloadPacket cp) {
            if (HEX_C2S_PAYLOAD_IDS.contains(cp.payload().type().id())) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$bypassPacketCheck(Packet<?> packet, ClientCommonPacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundCustomPayloadPacket cp) {
            if (MOD_ID.equals(cp.payload().type().id().getNamespace())) {
                ci.cancel();
            }
        } else if (packet instanceof ServerboundCustomPayloadPacket cp) {
            if (HEX_C2S_PAYLOAD_IDS.contains(cp.payload().type().id())) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = "hasChannel(Lnet/neoforged/neoforge/common/extensions/ICommonPacketListener;Lnet/minecraft/resources/ResourceLocation;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$hasHexChannelListener(ICommonPacketListener listener, ResourceLocation id, CallbackInfoReturnable<Boolean> cir) {
        if (HEX_PLAY_PAYLOAD_IDS.contains(id) || HEX_C2S_PAYLOAD_IDS.contains(id)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
        method = "hasChannel(Lnet/minecraft/network/Connection;Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/resources/ResourceLocation;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$hasHexChannelConnection(Connection connection, ConnectionProtocol protocol, ResourceLocation id, CallbackInfoReturnable<Boolean> cir) {
        if (HEX_PLAY_PAYLOAD_IDS.contains(id) || HEX_C2S_PAYLOAD_IDS.contains(id)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /** handleModdedPayload uses hasAdhocChannel (not hasChannel) when setup.getChannel returns null. */
    @Inject(
        method = "hasAdhocChannel(Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/protocol/PacketFlow;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$hasAdhocHexChannel(ConnectionProtocol protocol, ResourceLocation id, PacketFlow flow, CallbackInfoReturnable<Boolean> cir) {
        if (flow == PacketFlow.CLIENTBOUND && HEX_PLAY_PAYLOAD_IDS.contains(id)) {
            cir.setReturnValue(true);
            cir.cancel();
        } else if (flow == PacketFlow.SERVERBOUND && HEX_C2S_PAYLOAD_IDS.contains(id)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /**
     * When handleModdedPayload runs, the client's connection setup often lacks hex play channels (they're not
     * negotiated). Dispatch hex payloads directly here so we never hit the "no registration" disconnect.
     */
    @Inject(
        method = "handleModdedPayload(Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$handleHexPayloadDirectly(ClientCommonPacketListener listener, ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        var payload = packet.payload();
        var id = payload.type().id();
        var handler = HEX_CLIENT_HANDLERS.get(id);
        if (handler != null) {
            handler.accept(payload);
            ci.cancel();
        }
    }

    /**
     * When handleModdedPayload runs on server, the registration lookup can return null for hex C2S payloads.
     * Intercept and dispatch directly.
     */
    @Inject(
        method = "handleModdedPayload(Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void hexcasting$handleHexServerPayloadDirectly(
        ServerCommonPacketListener listener,
        ServerboundCustomPayloadPacket packet,
        CallbackInfo ci
    ) {
        var payload = packet.payload();
        var id = payload.type().id();
        if (!HEX_C2S_PAYLOAD_IDS.contains(id)) return;
        if (!(listener instanceof ServerGamePacketListenerImpl gameListener)) return;
        var player = gameListener.player;
        var server = player.getServer();
        if (server == null) return;
        if (id.equals(modLoc("pat_cs"))) {
            ((MsgNewSpellPatternC2S) payload).handle(server, player);
            ci.cancel();
        } else if (id.equals(modLoc("scroll"))) {
            ((MsgShiftScrollC2S) payload).handle(server, player);
            ci.cancel();
        }
    }

    /** When handleModdedPayload's registration Map.get returns null for hex payloads, fallback to play map. */
    @Redirect(
        method = "handleModdedPayload(Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1),
        remap = false
    )
    private static Object hexcasting$fallbackPayloadRegistration(
        Map<?, ?> map,
        Object key,
        ClientCommonPacketListener listener,
        ClientboundCustomPayloadPacket packet
    ) {
        Object result = map.get(key);
        if (result != null) return result;
        if (!(key instanceof ResourceLocation id) || !HEX_PLAY_PAYLOAD_IDS.contains(id)) return null;
        try {
            Field f = NetworkRegistry.class.getDeclaredField("PAYLOAD_REGISTRATIONS");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            var regs = (Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration>>) f.get(null);
            var playMap = regs.get(ConnectionProtocol.PLAY);
            return playMap != null ? playMap.get(id) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Inject(
        method = "onConfigurationFinished",
        at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet$Builder;build()Lcom/google/common/collect/ImmutableSet;", ordinal = 1),
        locals = LocalCapture.CAPTURE_FAILHARD,
        remap = false
    )
    private static void hexcasting$addPlayChannels(
        ICommonPacketListener listener,
        CallbackInfo ci,
        NetworkPayloadSetup setup,
        ImmutableSet.Builder<?> notListeningAnymoreOn,
        ImmutableSet.Builder<ResourceLocation> nowListeningOn
    ) {
        nowListeningOn.addAll(HEX_PLAY_PAYLOAD_IDS);
    }

    @ModifyVariable(
        method = "getCodec",
        ordinal = 0,
        at = @At("STORE"),
        remap = false
    )
    private static Object hexcasting$fallbackPlayCodec(
        Object registration,
        ResourceLocation id,
        ConnectionProtocol protocol,
        PacketFlow flow
    ) {
        if (registration != null) return registration;
        if (!MOD_ID.equals(id.getNamespace())) return null;
        try {
            Field f = NetworkRegistry.class.getDeclaredField("PAYLOAD_REGISTRATIONS");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration>> regs =
                (Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration>>) f.get(null);
            Map<ResourceLocation, PayloadRegistration> playMap = regs.get(ConnectionProtocol.PLAY);
            if (playMap == null) return null;
            PayloadRegistration playReg = playMap.get(id);
            if (playReg == null) return null;
            if (playReg.flow().isPresent() && playReg.flow().get() != flow) return null;
            return playReg;
        } catch (Exception e) {
            return null;
        }
    }
}
