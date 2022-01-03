package at.petrak.hex.common.lib;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.casting.operators.spells.great.OpFlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LibCapabilities {
    public static final Capability<OpFlight.CapFlight> FLIGHT = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(OpFlight.CapFlight.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof ServerPlayer) {
            HexMod.LOGGER.info("Registering flight for {}", evt.getObject());
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, OpFlight.CAP_NAME),
                    OpFlight.INSTANCE.getDummyInstanceIHateForge().resolve().get());
        }
    }
}
