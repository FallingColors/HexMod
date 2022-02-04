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

    @SubscribeEvent
    public static void syncCapToNewPlayer(PlayerEvent evt) {
        var player = evt.getPlayer();
        // this apparently defines it in outside scope. the more you know.
        if (!(player instanceof ServerPlayer splayer)) {
            return;
        }

        var doSync = false;
        if (evt instanceof PlayerEvent.PlayerLoggedInEvent) {
            doSync = true;
        } else if (evt instanceof PlayerEvent.Clone clone) {
            doSync = clone.isWasDeath();
        }

        if (doSync) {
            var capSentinel = splayer.getCapability(HexCapabilities.SENTINEL).resolve();
            if (capSentinel.isEmpty()) {
                return;
            }
            HexMessages.getNetwork()
                .send(PacketDistributor.PLAYER.with(() -> splayer), new MsgSentinelStatusUpdateAck(capSentinel.get()));

            var capColorizer = splayer.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve();
            if (capColorizer.isEmpty()) {
                return;
            }
            HexMessages.getNetwork()
                .send(PacketDistributor.PLAYER.with(() -> splayer), new MsgColorizerUpdateAck(capColorizer.get()));
        }
    }
}
