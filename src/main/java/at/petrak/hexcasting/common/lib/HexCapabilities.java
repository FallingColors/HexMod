package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.casting.colors.CapPreferredColorizer;
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight;
import at.petrak.hexcasting.common.casting.operators.spells.sentinel.CapSentinel;
import at.petrak.hexcasting.common.network.HexMessages;
import at.petrak.hexcasting.common.network.MsgColorizerUpdateAck;
import at.petrak.hexcasting.common.network.MsgSentinelStatusUpdateAck;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class HexCapabilities {
    public static final Capability<OpFlight.CapFlight> FLIGHT = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CapSentinel> SENTINEL = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CapPreferredColorizer> PREFERRED_COLORIZER =
        CapabilityManager.get(new CapabilityToken<>() {
        });

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(OpFlight.CapFlight.class);
        evt.register(CapSentinel.class);
        evt.register(CapPreferredColorizer.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof Player) {
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, OpFlight.CAP_NAME),
                // generate a new instance of the capability
                new OpFlight.CapFlight(false, 0, Vec3.ZERO, 0.0));
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, CapSentinel.CAP_NAME),
                new CapSentinel(false, false, Vec3.ZERO));
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, CapPreferredColorizer.CAP_NAME),
                new CapPreferredColorizer(FrozenColorizer.DEFAULT));
        }
    }

    // if I were forge i sould simply design an actually useful and useable cap system
    @SubscribeEvent
    public static void copyCapsOnDeath(PlayerEvent.Clone evt) {
        var eitherSidePlayer = evt.getPlayer();
        // this apparently defines it in outside scope. the more you know.
        if (!(eitherSidePlayer instanceof ServerPlayer player)) {
            return;
        }

        if (evt.isWasDeath()) {
            var proto = evt.getOriginal();
            // Copy caps from this to new player
            proto.reviveCaps();
            var protoCapSentinel = proto.getCapability(SENTINEL).resolve();
            protoCapSentinel.ifPresent(protoSentinel -> {
                var capSentinel = player.getCapability(SENTINEL);
                capSentinel.ifPresent(sentinel -> {
                    sentinel.hasSentinel = protoSentinel.hasSentinel;
                    sentinel.position = protoSentinel.position;
                    sentinel.extendsRange = protoSentinel.extendsRange;
                });
            });
            var protoCapColor = proto.getCapability(PREFERRED_COLORIZER).resolve();
            protoCapColor.ifPresent(protoColorizer -> {
                var capColorizer = player.getCapability(PREFERRED_COLORIZER);
                capColorizer.ifPresent(colorizer -> {
                    colorizer.colorizer = protoColorizer.colorizer;
                });
            });
            proto.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void syncCapsOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (!(evt.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        syncCaps(player);
    }

    @SubscribeEvent
    public static void syncCapsOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
        if (!(evt.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        syncCaps(player);
    }

    private static void syncCaps(ServerPlayer player) {
        var capSentinel = player.getCapability(HexCapabilities.SENTINEL).resolve();
        capSentinel.ifPresent(sentinel -> HexMessages.getNetwork()
            .send(PacketDistributor.PLAYER.with(() -> player), new MsgSentinelStatusUpdateAck(sentinel)));

        var capColorizer = player.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve();
        capColorizer.ifPresent(colorizer -> HexMessages.getNetwork()
            .send(PacketDistributor.PLAYER.with(() -> player), new MsgColorizerUpdateAck(colorizer)));
    }
}
